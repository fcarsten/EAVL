package org.auscope.portal.server.web.service.jobtask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
import org.auscope.portal.server.web.service.WpsService;
import org.auscope.portal.server.web.service.wps.WpsServiceClient;

public class ImputationCallable implements Callable<Object> {

    private final Log log = LogFactory.getLog(getClass());

    protected EAVLJob job;
    protected CSVService csvService;
    protected FileStagingService fss;

	private WpsService wpsService;

    public ImputationCallable(EAVLJob job, WpsService wpsService, CSVService csvService, FileStagingService fss) {
        super();
        this.job = job;
        this.wpsService = wpsService;
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
        InputStream in = null, in2 = null;
        OutputStream os = null;

        try {
            List<Integer> excludedCols = getExcludedColumns();
            in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
            Double[][] rawData = csvService.getRawData(in, excludedCols, false);
        	int retries= WPSController.MAX_RETRIES;
        	double[][] imputedData=null;
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
            in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
            os = this.fss.writeFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);
            this.csvService.writeRawData(in, os, imputedData, excludedCols, false);

            //After getting the imputed data, re-insert the "excluded" columns into the imputed dataset
            in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
            in2 = this.fss.readFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);
            os = this.fss.writeFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            this.csvService.mergeFiles(in, in2, os, excludedCols, null);

            this.fss.deleteStageInFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);

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

}
