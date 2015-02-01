package org.auscope.portal.server.web.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.eavl.ParameterDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

/**
 * Thin alternative to CSVService for extracting ParameterDetails.
 *
 * This particular implementation relies on the CSVService but also caches
 * ParameterDetails lookups on disk and also allows writing to the disk cache.
 *
 * @author Josh Vote
 *
 */
@Service
public class ParameterDetailsService {
    private FileStagingService fss;
    private CSVService csvService;

    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    public ParameterDetailsService(FileStagingService fss, CSVService csvService) {
        super();
        this.fss = fss;
        this.csvService = csvService;
    }

    /**
     * Returns the list of ParameterDetails object for the given CSV file. The ParameterDetails
     * will first be read from the on disk cache (if it exists) otherwise it will be written.
     *
     * If the cache read errors the cache will be purged and the parameter details calculated using
     * a CSVService.
     *
     * @param job
     * @param file
     * @return
     * @throws PortalServiceException
     */
    public List<ParameterDetails> getParameterDetails(EAVLJob job, String file) throws PortalServiceException {
        if (fss.stageInFileExists(job, file + EAVLJobConstants.PD_CACHE_SUFFIX)) {
            try {
                return readCache(job, file);
            } catch (PortalServiceException ex) {
                log.error("Error reading PD cache. Purging!", ex);

                //If we can't read the cache - purge it.
                //and get the parameter details normally
                purgeCache(job, file);
            }
        }

        return readCsv(job, file);
    }

    /**
     * Deletes the underlying cache for a given CSV file.
     * @param job
     * @param file
     */
    public void purgeCache(EAVLJob job, String file) {
        fss.deleteStageInFile(job, file + EAVLJobConstants.PD_CACHE_SUFFIX);
    }

    /**
     * Writes the specified parameter details to the cache for a given file. The parameter details
     * will form the new cache for the specified file until purgeCache is called.
     * @param job
     * @param file
     * @param parameterDetails
     * @throws PortalServiceException
     */
    public void updateCache(EAVLJob job, String file, List<ParameterDetails> parameterDetails) throws PortalServiceException {
        OutputStream os = fss.writeFile(job, file + EAVLJobConstants.PD_CACHE_SUFFIX);
        JsonWriter jw = null;
        try {
            jw = new JsonWriter(os);
            jw.write(parameterDetails);
        } catch (IOException e) {
            throw new PortalServiceException("Error writing PD cache", e);
        } finally {
            IOUtils.closeQuietly(jw);
            IOUtils.closeQuietly(os);
        }
    }

    private List<ParameterDetails> readCache(EAVLJob job, String file) throws PortalServiceException {
        InputStream is = fss.readFile(job, file + EAVLJobConstants.PD_CACHE_SUFFIX);
        JsonReader jr = null;
        try {
            jr = new JsonReader(is);
            return (List<ParameterDetails>) jr.readObject();
        } catch (IOException e) {
            throw new PortalServiceException("Error reading PD cache", e);
        } finally {
            IOUtils.closeQuietly(jr);
            IOUtils.closeQuietly(is);
        }
    }

    private List<ParameterDetails> readCsv(EAVLJob job, String file) throws PortalServiceException {
        InputStream is = null;
        try {
            is = fss.readFile(job, file);
            List<ParameterDetails> pds = csvService.extractParameterDetails(is);
            updateCache(job, file, pds);
            return pds;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
