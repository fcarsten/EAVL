package org.auscope.portal.server.web.service.jobtask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.auscope.eavl.wpsclient.HpiKdeJSON;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.web.controllers.Proxy;
import org.auscope.portal.server.web.controllers.WPSController;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.auscope.portal.server.web.service.WpsService;
import org.auscope.portal.server.web.service.wps.WpsServiceClient;
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
    protected EAVLJobService jobService;

    public KDECallable(EAVLJob job, WpsService wpsService,
            CSVService csvService, FileStagingService fss, EAVLJobService jobService) {
        super();
        this.job = job;
        this.wpsService = wpsService;
        this.csvService = csvService;
        this.fss = fss;
        this.jobService = jobService;
    }

//    protected List<Integer> getProxyCols(String file)
//            throws PortalServiceException {
//        List<Integer> inclusions = new ArrayList<Integer>();
//
//        List<Proxy> proxyParamList = new ArrayList<String>(
//                job.getProxyParameters());
//        InputStream in = this.fss.readFile(job, file);
//        List<Integer> savedParamIndexes = csvService.columnNameToIndex(in,
//                proxyParamList);
//        inclusions.addAll(savedParamIndexes);
//
//        return inclusions;
//    }

    protected List<Integer> getExcludedColumns() throws PortalServiceException {
        List<Integer> exclusions = new ArrayList<Integer>();

        List<String> savedParamList = new ArrayList<String>(
                job.getSavedParameters());
        InputStream in = this.fss.readFile(job,
                EAVLJobConstants.FILE_IMPUTED_SCALED_CSV);
        List<Integer> savedParamIndexes = csvService.columnNameToIndex(in,
                savedParamList);
        exclusions.addAll(savedParamIndexes);

        in = this.fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_SCALED_CSV);
        Integer index = csvService.columnNameToIndex(in,
                job.getHoleIdParameter());
        if (index != null && !exclusions.contains(index)) {
            exclusions.add(index);
        }

        return exclusions;
    }

    private HpiKdeJSON hpiKde(double cutOff)
            throws PortalServiceException, WPSClientException {

        InputStream in = null;
        OutputStream os = null;
        HpiKdeJSON kdeJson = null;

        try {

            in = this.fss.readFile(job,
                    EAVLJobConstants.FILE_IMPUTED_CENLR_CSV);
            Integer predictorIndex = csvService.columnNameToIndex(in,
                    job.getPredictionParameter());
            IOUtils.closeQuietly(in);

            in = this.fss.readFile(job,
                    EAVLJobConstants.FILE_IMPUTED_CENLR_CSV);
            double[][] proxyCenlrData = csvService.getRawData(in, Arrays.asList(predictorIndex), false);
            IOUtils.closeQuietly(in);

            in = this.fss.readFile(job,
                    EAVLJobConstants.FILE_IMPUTED_CENLR_CSV);
            double[] predictorCenlrData = this.csvService
                    .getParameterValues(in, predictorIndex, true);

            int retries = WPSController.MAX_RETRIES;
            Exception error=null;
            while (kdeJson == null && retries-- > 0) {
                WpsServiceClient wpsClient = null;
                try {
                    wpsClient = wpsService.getWpsClient();

                    checkDataBeforeKde(proxyCenlrData);
                    checkDataBeforeKde(predictorCenlrData);

                    kdeJson = wpsClient.hpiKdeJSON(proxyCenlrData,
                            predictorCenlrData,
                            cutOff);
                    return kdeJson;
                } catch (IOException e) {
                    error=e;
                    log.warn("Unable to get double pdf values: ", e);
                    log.warn("Assuming bad VM");
                    wpsService.checkVM(wpsClient);
                }
            }
            throw new PortalServiceException("WPS Sever HpiKde call exceeded max retries", error);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(os);
        }
    }

    private void checkDataBeforeKde(double[] ds) {
        for (double d : ds) {
            if (Double.isNaN(d))
                throw new IllegalArgumentException(
                        "Input to centered log ratio function can't be NaN");
        }
    }

    @Override
    public Object call() throws Exception {
        InputStream in = null, in2 = null;
        OutputStream os = null;
        OutputStreamWriter writer = null;

        try {
            centeredLogRatio();

            HpiKdeJSON kdeJsonHigh = hpiKde((double) job.getPredictionCutoff());
            HpiKdeJSON kdeJsonAll = hpiKde(Double.NEGATIVE_INFINITY);

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
            in2 = this.fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_SCALED_CSV);
            os = this.fss.writeFile(job, EAVLJobConstants.FILE_CP_CSV);
            csvService.mergeFiles(in, in2, os, null, null);

            return "";
        } catch (Exception ex) {
            log.error("KDE Error: ", ex);

            //Record an error message into the job
            String errorMessage = null;
            if (ex instanceof WPSClientException) {
                errorMessage = String.format("The remote computation service has returned an error when processing your data. Please contact EAVL support for more information.\nRemote Error: %1$s", ex.getMessage());
            } else if (ex instanceof IllegalArgumentException) {
                errorMessage = String.format("Your selected threshold or proxies are invalid and cannot be processed.\nMessage: %1$s", ex.getMessage());
            } else if (ex instanceof IOException) {
                errorMessage = String.format("There was an error communicating to the remote processing service. Please try again later.\nMessage: %1$s", ex.getMessage());
            } else {
                errorMessage = String.format("There was an error when performing calculations. Please try again later.\nMessage: %1$s", ex.getMessage());
            }
            try {
                EAVLJob updatedJob = jobService.getJobById(job.getId());
                updatedJob.setKdeTaskError(errorMessage);
                jobService.save(updatedJob);
            } catch (Exception saveEx) {
                log.error("Unable to write error message to job with ID" + job.getId(), saveEx);
            }

            throw new PortalServiceException("", ex);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(in2);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(os);
        }
    }

    private void centeredLogRatio() throws PortalServiceException, WPSClientException {
        InputStream in = null;
        OutputStream os = null;
        try {
            Set<Proxy> proxies = job.getProxyParameters();
            double[][] cenlrData = new double[proxies.size() + 1][];
            String[] cenlrHeader = new String[proxies.size() + 1];

            in = this.fss.readFile(job,
                    EAVLJobConstants.FILE_IMPUTED_SCALED_CSV);
            Integer predictionColumnIndex = csvService.columnNameToIndex(in,
                    job.getPredictionParameter());

            in = this.fss.readFile(job,
                    EAVLJobConstants.FILE_IMPUTED_SCALED_CSV);
            cenlrData[0] = this.csvService.getParameterValues(in, predictionColumnIndex);
            cenlrHeader[0] = job.getPredictionParameter();

            int index=1;
            for (Proxy proxy : proxies) {
                List<String> proxyNames = new ArrayList<>(proxy.getDenom());

                in = this.fss.readFile(job,
                        EAVLJobConstants.FILE_IMPUTED_SCALED_CSV);
                List<Integer> clrIndeces = csvService.columnNameToIndex(in,
                        proxyNames);
                IOUtils.closeQuietly(in);

                cenlrHeader[index] = proxy.getNumerator();
                int proxyNameIndex =  proxyNames.indexOf(proxy.getNumerator());
                cenlrData[index++] = CSVService.extractColumn(centredLogRatio(clrIndeces), proxyNameIndex);
            }

            cenlrData = CSVService.transpose(cenlrData);

            os = this.fss.writeFile(job, EAVLJobConstants.FILE_IMPUTED_CENLR_CSV);
            this.csvService.writeRawData(os, cenlrHeader, cenlrData);

        } finally {
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

    private double[][] centredLogRatio(List<Integer> proxyCols) throws PortalServiceException, WPSClientException {
        InputStream in1 = null, in2 = null;
        OutputStream os = null;

//        List<Integer> includedColumns = new ArrayList<Integer>(proxyCols);
//        includedColumns.add(predictionColumnIndex);

        try {
            in1 = this.fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_SCALED_CSV);
            double[][] imputedData = csvService.getRawData(in1, proxyCols, true);

            checkDataBeforeCLR(imputedData);
            IOException error=null;
            int retries = WPSController.MAX_RETRIES;
            while (retries-- > 0) {
                WpsServiceClient wpsClient = null;
                try {
                    wpsClient = wpsService.getWpsClient();

                    return wpsClient.cenLR(imputedData);
                } catch (IOException e) {
                    error=e;
                    log.warn("Unable to get double pdf values: ", e);
                    log.warn("Assuming bad VM");
                    wpsService.checkVM(wpsClient);
                }
            }
            throw new PortalServiceException("WPS Server call failed max retries" , error);
        } finally {
            IOUtils.closeQuietly(in1);
            IOUtils.closeQuietly(in2);
            IOUtils.closeQuietly(os);
        }
    }

    private void checkDataBeforeCLR(double[][] imputedData) {
        for (double[] ds : imputedData) {
            for (double d : ds) {
                if(Double.isNaN(d))
                    throw new IllegalArgumentException("Input to centered log ratio function can't be NaN");
                else if (d<=0)
                    throw new IllegalArgumentException("Input to centered log ratio function can't be 0");
            }
        }
    }

    private void checkDataBeforeKde(double[][] imputedData) {
        for (double[] ds : imputedData) {
            for (double d : ds) {
                if(Double.isNaN(d))
                    throw new IllegalArgumentException("Input to centered log ratio function can't be NaN");
            }
        }
    }
}
