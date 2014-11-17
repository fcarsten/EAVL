package org.auscope.portal.server.web.service.jobtask;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.web.service.CSVService;

public class ImputationCallable implements Callable<Object> {

    protected final Log log = LogFactory.getLog(getClass());

    private EAVLJob job;
    private ConditionalProbabilityWpsClient wpsClient;
    private CSVService csvService;
    private FileStagingService fss;

    public ImputationCallable(EAVLJob job, ConditionalProbabilityWpsClient wpsClient, CSVService csvService, FileStagingService fss) {
        super();
        this.job = job;
        this.wpsClient = wpsClient;
        this.csvService = csvService;
        this.fss = fss;
    }

    protected List<Integer> getExcludedColumns() throws PortalServiceException {
        List<Integer> exclusions = new ArrayList<Integer>();

        InputStream in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
        Integer index = csvService.columnNameToIndex(in, job.getHoleIdParameter());
        if (index != null) {
            exclusions.add(index);
        }

        //Not the most efficient method - should't really be run that often or over many items so I doubt it will matter
        for (String name : job.getSavedParameters()) {
            in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
            index = csvService.columnNameToIndex(in, name);
            if (index != null && !exclusions.contains(index)) {
                exclusions.add(index);
            }
        }

        return exclusions;
    }

    @Override
    public Object call() throws Exception {
        InputStream in = null;
        OutputStream os = null;

        try {
            List<Integer> excludedCols = getExcludedColumns();
            in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
            Double[][] rawData = csvService.getRawData(in, excludedCols, false);
            double[][] imputedData = wpsClient.imputationNA(rawData);

            IOUtils.closeQuietly(in);
            in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV); //need to reopen this because it gets shut above
            os = this.fss.writeFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);

            this.csvService.writeRawData(in, os, imputedData);
            return imputedData;
        } catch (Exception ex) {
            log.error("Imputation Error: ", ex);
            throw new PortalServiceException("", ex);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(os);
        }
    }

}
