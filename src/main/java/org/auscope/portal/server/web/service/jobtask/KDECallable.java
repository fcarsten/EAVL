package org.auscope.portal.server.web.service.jobtask;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.wps.WpsServiceClient;

/**
 * Implements the Kernel Density Estimator workflow using a remote WPS
 * @author Josh Vote
 */
public class KDECallable implements Callable<Object> {

    private final Log log = LogFactory.getLog(getClass());

    protected EAVLJob job;
    protected WpsServiceClient wpsClient;
    protected CSVService csvService;
    protected FileStagingService fss;

    public KDECallable(EAVLJob job, WpsServiceClient wpsClient, CSVService csvService, FileStagingService fss) {
        super();
        this.job = job;
        this.wpsClient = wpsClient;
        this.csvService = csvService;
        this.fss = fss;
    }

    protected List<Integer> getProxyCols() throws PortalServiceException {
        List<Integer> inclusions = new ArrayList<Integer>();

        //Not the most efficient method - should't really be run that often or over many items so I doubt it will matter
        for (String name : job.getProxyParameters()) {
            InputStream in = this.fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            Integer index = csvService.columnNameToIndex(in, name);
            if (index != null && !inclusions.contains(index)) {
                inclusions.add(index);
            }
        }

        return inclusions;
    }

    @Override
    public Object call() throws Exception {
        InputStream in = null, in2 = null;
        OutputStream os = null;
        OutputStreamWriter writer = null;

        try {
            job.getProxyParameters();

            List<Integer> includedCols = getProxyCols();
            in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
            Double[][] proxyData = csvService.getRawData(in, includedCols, true);

            in = this.fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            Integer predictorIndex = csvService.columnNameToIndex(in, job.getPredictionParameter());

            in = this.fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            List<Double> predictorData = this.csvService.getParameterValues(in, predictorIndex, true);

            String kdeJson = wpsClient.hpiKdeJSON(proxyData, predictorData.toArray(new Double[predictorData.size()]), job.getPredictionCutoff());

            os = this.fss.writeFile(job, EAVLJobConstants.FILE_KDE_JSON);
            writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            writer.write(kdeJson);

            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(os);

            //Create a "fake" CSV file containing just the estimate data
            os = this.fss.writeFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);
            writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            writer.write("eavl-kde-estimate\n");
            JSONObject json = JSONObject.fromObject(kdeJson);
            JSONObject gkde = (JSONObject) json.get("gkde");
            JSONObject estimate = (JSONObject) gkde.get("estimate");
            for (int i = 0; i < estimate.size(); i++) {
                writer.write(estimate.get(Integer.toString((i + 1))).toString() + "\n");
            }
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(os);

            //Merge that fake CSV file with the imputed CSV data
            in = this.fss.readFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);
            in2 = this.fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            os = this.fss.writeFile(job, EAVLJobConstants.FILE_KDE_CSV);
            csvService.mergeFiles(in, in2, os, null, null);

            return kdeJson;
        } catch (Exception ex) {
            log.error("Imputation Error: ", ex);
            throw new PortalServiceException("", ex);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(in2);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(os);
        }
    }

}
