package org.auscope.portal.server.web.service.jobtask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.web.controllers.WPSController;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.auscope.portal.server.web.service.WpsService;
import org.auscope.portal.server.web.service.wps.WpsServiceClient;

/**
 * Performs Imputation and Unit of Measure scaling
 * @author Josh Vote
 *
 */
public class ImputationCallable implements Callable<Object> {

    private final Log log = LogFactory.getLog(getClass());

    protected EAVLJob job;
    protected WpsService wpsService;
    protected CSVService csvService;
    protected FileStagingService fss;
    protected EAVLJobService jobService;

    protected String[] uomNameKeys;
    protected String[] uomChangedNames;
    protected Double[] uomScaleFactors;

    public ImputationCallable(EAVLJob job, WpsService wpsService, CSVService csvService, FileStagingService fss,
            EAVLJobService jobService, String[] uomNameKeys, String[] uomChangedNames, Double[] uomScaleFactors) {
        super();
        this.job = job;
        this.wpsService = wpsService;
        this.csvService = csvService;
        this.fss = fss;
        this.uomNameKeys = uomNameKeys;
        this.uomChangedNames = uomChangedNames;
        this.uomScaleFactors = uomScaleFactors;
        this.jobService = jobService;
    }

    protected List<Integer> getExcludedColumns() throws PortalServiceException {
        List<Integer> exclusions = new ArrayList<Integer>();

        List<String> savedParamList = new ArrayList<String>(job.getSavedParameters());
        InputStream in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
        List<Integer> savedParamIndexes = csvService.columnNameToIndex(in, savedParamList);
        exclusions.addAll(savedParamIndexes);

        in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
        Integer index = csvService.columnNameToIndex(in, job.getHoleIdParameter());
        if (index != null && !exclusions.contains(index)) {
            exclusions.add(index);
        }

        //Don't impute prediction parameter
        in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
        index = csvService.columnNameToIndex(in, job.getPredictionParameter());;
        if (index != null && !exclusions.contains(index)) {
            exclusions.add(index);
        }

        return exclusions;
    }

    @Override
    public Object call() throws Exception {
        InputStream in = null, in2 = null;
        OutputStream os = null;

        try {
            List<Integer> excludedCols = getExcludedColumns();

            //Start by culling any empty rows (based on compositional params)
            in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
            os = this.fss.writeFile(job, EAVLJobConstants.FILE_VALIDATED_DATA_CSV);
            this.csvService.cullEmptyRows(in, os, excludedCols, false);

            //Impute the validated data
            in = this.fss.readFile(job, EAVLJobConstants.FILE_VALIDATED_DATA_CSV);
            double[][] rawData = csvService.getRawData(in, excludedCols, false);
            int retries= WPSController.MAX_RETRIES;
            double[][] imputedData= validateValues(rawData);
            while (imputedData == null && retries-- > 0) {
                WpsServiceClient wpsClient = null;
                try {
                    wpsClient = wpsService.getWpsClient();
                    imputedData = wpsClient.imputationNA(rawData);
                } catch (IOException e) {
                    log.warn("Unable to get imputation values: ", e);
                    log.warn("Assuming bad VM");
                    wpsService.checkVM(wpsClient);
                }
            }

            if(imputedData==null) {
                throw new PortalServiceException("Could not compute imputation data.");
            }

            //Write the imputed data to a temporary file
            in = this.fss.readFile(job, EAVLJobConstants.FILE_VALIDATED_DATA_CSV);
            os = this.fss.writeFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);
            this.csvService.writeRawData(in, os, imputedData, excludedCols, false);

            //After getting the imputed data, re-insert the "excluded" columns into the imputed dataset
            in = this.fss.readFile(job, EAVLJobConstants.FILE_VALIDATED_DATA_CSV);
            in2 = this.fss.readFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);
            os = this.fss.writeFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            this.csvService.mergeFiles(in, in2, os, excludedCols, null);

            this.fss.deleteStageInFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);

            if (uomNameKeys != null && uomNameKeys.length > 0) {
                performUomScaling();
            } else {
                this.fss.renameStageInFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV, EAVLJobConstants.FILE_IMPUTED_SCALED_CSV);
            }

            return imputedData;
        } catch (Exception ex) {
            log.error("Imputation Error: ", ex);
            throw new PortalServiceException("", ex);
        } finally {
            IOUtils.closeQuietly(in2);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(os);
        }
    }

    private void performUomScaling() throws PortalServiceException {
        OutputStream os = null;
        InputStream is = null;

        try {
            is = fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            List<Integer> colIndexes = csvService.columnNameToIndex(is, Arrays.asList(uomNameKeys));
            is = fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            os = fss.writeFile(job, EAVLJobConstants.FILE_IMPUTED_SCALED_CSV);
            csvService.scaleColumns(is, os, colIndexes, Arrays.asList(uomScaleFactors), Arrays.asList(uomChangedNames));

            //If our element to predict had a name change, update the job too
            int index = Arrays.asList(uomNameKeys).indexOf(job.getPredictionParameter());
            if (!uomChangedNames[index].equals(job.getPredictionParameter())) {
                job.setPredictionParameter(uomChangedNames[index]);
                jobService.save(job);
            }
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }
    }

    private double[][] validateValues(double[][] x) {
        boolean needsImp = false;
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                double v = x[i][j];
                if(0==v) throw new IllegalArgumentException("Imputation matrix may not contain '0'");

                if(Double.isNaN(v)) {
                    needsImp=true;
                }
            }
        }
        return needsImp? null:x;
    }

}
