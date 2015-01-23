package org.auscope.portal.server.web.service.jobtask;

import java.io.IOException;
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
import org.auscope.eavl.wpsclient.EavlWpsClient;
import org.auscope.eavl.wpsclient.HpiKde;
import org.auscope.eavl.wpsclient.HpiKdeJSON;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.web.controllers.WPSController;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.WpsService;
import org.auscope.portal.server.web.service.wps.WpsServiceClient;
import org.hibernate.result.NoMoreReturnsException;
import org.n52.wps.client.WPSClientException;

/**
 * Implements the Kernel Density Estimator workflow using a remote WPS
 *
 * @author Josh Vote
 */
public class KDECallable implements Callable<Object> {

    private final Log log = LogFactory.getLog(getClass());

    protected EAVLJob job;
    protected WpsService wpsService;
    protected CSVService csvService;
    protected FileStagingService fss;

    public KDECallable(EAVLJob job, WpsService wpsService,
            CSVService csvService, FileStagingService fss) {
        super();
        this.job = job;
        this.wpsService = wpsService;
        this.csvService = csvService;
        this.fss = fss;
    }

    protected List<Integer> getProxyCols(String file)
            throws PortalServiceException {
        List<Integer> inclusions = new ArrayList<Integer>();

        List<String> proxyParamList = new ArrayList<String>(
                job.getProxyParameters());
        InputStream in = this.fss.readFile(job, file);
        List<Integer> savedParamIndexes = csvService.columnNameToIndex(in,
                proxyParamList);
        inclusions.addAll(savedParamIndexes);

        return inclusions;
    }

    protected List<Integer> getExcludedColumns() throws PortalServiceException {
        List<Integer> exclusions = new ArrayList<Integer>();

        List<String> savedParamList = new ArrayList<String>(
                job.getSavedParameters());
        InputStream in = this.fss.readFile(job,
                EAVLJobConstants.FILE_IMPUTED_CSV);
        List<Integer> savedParamIndexes = csvService.columnNameToIndex(in,
                savedParamList);
        exclusions.addAll(savedParamIndexes);

        in = this.fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
        Integer index = csvService.columnNameToIndex(in,
                job.getHoleIdParameter());
        if (index != null && !exclusions.contains(index)) {
            exclusions.add(index);
        }

        return exclusions;
    }

    public HpiKdeJSON hpiKde(List<Integer> nonCompCols, double cutOff)
            throws PortalServiceException, WPSClientException {

        InputStream in = null;
        OutputStream os = null;
        HpiKdeJSON kdeJson = null;

        try {
            List<Integer> includedCols = getProxyCols(EAVLJobConstants.FILE_IMPUTED_CENLR_CSV);
            in = this.fss.readFile(job,
                    EAVLJobConstants.FILE_IMPUTED_CENLR_CSV);
            double[][] proxyCenlrData = csvService.getRawData(in,
                    includedCols, true);

            in = this.fss.readFile(job,
                    EAVLJobConstants.FILE_IMPUTED_CENLR_CSV);
            Integer predictorIndex = csvService.columnNameToIndex(in,
                    job.getPredictionParameter());
            in = this.fss.readFile(job,
                    EAVLJobConstants.FILE_IMPUTED_CENLR_CSV);
            double[] predictorCenlrData = this.csvService
                    .getParameterValues(in, predictorIndex, true);

            int retries = WPSController.MAX_RETRIES;
            while (kdeJson == null && retries-- > 0) {
                WpsServiceClient wpsClient = null;
                try {
                    wpsClient = wpsService.getWpsClient();

                    kdeJson = wpsClient.hpiKdeJSON(proxyCenlrData,
                            predictorCenlrData,
                            cutOff);
                } catch (IOException e) {
                    log.warn("Unable to get double pdf values: ", e);
                    log.warn("Assuming bad VM");
                    wpsService.checkVM(wpsClient);
                }
            }
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(os);
        }
        return kdeJson;
    }

    @Override
    public Object call() throws Exception {
        InputStream in = null, in2 = null;
        OutputStream os = null;
        OutputStreamWriter writer = null;

        try {
            job.getProxyParameters();

            List<Integer> nonCompCols = getExcludedColumns();

            centredLogRation(nonCompCols);

            HpiKdeJSON kdeJsonHigh = hpiKde(nonCompCols, (double) job.getPredictionCutoff());
            HpiKdeJSON kdeJsonAll = hpiKde(nonCompCols, 0);

            if (kdeJsonHigh == null || kdeJsonAll==null) {
                return new PortalServiceException(
                        "Error computing kernel density estimate data");
            }

            os = this.fss.writeFile(job, EAVLJobConstants.FILE_KDE_JSON_ALL);
            writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            writer.write(kdeJsonAll.getJson());

            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(os);

            os = this.fss.writeFile(job, EAVLJobConstants.FILE_KDE_JSON_HIGH);
            writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            writer.write(kdeJsonHigh.getJson());

            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(os);

            double[] estimateHigh = getEstimate(kdeJsonHigh.getJson());
            double[] estimateAll = getEstimate(kdeJsonAll.getJson());

            double[] condProb = ConditionalProbabilityWpsClient.conditionalProbability(kdeJsonHigh.getNumAll(), kdeJsonHigh.getNumHigh(), estimateAll, estimateHigh);

            // Create a "fake" CSV file containing just the estimate data
            os = this.fss.writeFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);
            writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            writer.write(EAVLJobConstants.PARAMETER_ESTIMATE + "\n");

            for (int i = 0; i < condProb.length; i++) {
                writer.write(""+ condProb[i] + "\n");
            }
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(os);

            // Merge that fake CSV file with the imputed CSV data
            in = this.fss.readFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);
            in2 = this.fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            os = this.fss.writeFile(job, EAVLJobConstants.FILE_KDE_CSV);
            csvService.mergeFiles(in, in2, os, null, null);

            return "";
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

    private double[] getEstimate(String kdeJson) {
        JSONObject json = JSONObject.fromObject(kdeJson);
        JSONObject gkde = (JSONObject) json.get("gkde");
        JSONObject estimate = (JSONObject) gkde.get("estimate");
        double[] res = new double[estimate.size()];

        for (int i = 0; i < estimate.size(); i++) {
            res[i]= Double.parseDouble(estimate.get(Integer.toString((i + 1))).toString());
//            writer.write(estimate.get(Integer.toString((i + 1))).toString()
//                    + "\n");
        }
        return res;
    }

    private void centredLogRation(List<Integer> nonCompCols) throws PortalServiceException, WPSClientException {
        InputStream in = null;
        OutputStream os = null;

        try {
            in = this.fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            double[][] imputedData = csvService.getRawData(in, nonCompCols,
                    false);

            int retries = WPSController.MAX_RETRIES;
            while (retries-- > 0) {
                WpsServiceClient wpsClient = null;
                try {
                    wpsClient = wpsService.getWpsClient();

                    double[][] cenlrImputedData = wpsClient.cenLR(imputedData);

                    // Write the cenlr imputed data to a temporary file
                    in = this.fss.readFile(job,
                            EAVLJobConstants.FILE_IMPUTED_CSV);
                    os = this.fss.writeFile(job,
                            EAVLJobConstants.FILE_IMPUTED_CENLR_CSV);
                    this.csvService.writeRawData(in, os, cenlrImputedData,
                            nonCompCols, false);
                } catch (IOException e) {
                    log.warn("Unable to get double pdf values: ", e);
                    log.warn("Assuming bad VM");
                    wpsService.checkVM(wpsClient);
                }
            }
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(os);
        }
    }

}
