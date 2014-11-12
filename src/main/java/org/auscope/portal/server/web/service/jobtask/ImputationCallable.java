package org.auscope.portal.server.web.service.jobtask;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.web.service.CSVService;

public class ImputationCallable implements Callable<Object> {

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

    @Override
    public Object call() throws Exception {
        InputStream in = null;
        OutputStream os = null;

        try {
            in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
            Double[][] rawData = csvService.getRawData(in);
            double[][] imputedData = wpsClient.imputationNA(rawData);

            IOUtils.closeQuietly(in);
            in = this.fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV); //need to reopen this because it gets shut above
            os = this.fss.writeFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);

            this.csvService.writeRawData(in, os, imputedData);
            return imputedData;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(os);
        }
    }

}
