package org.auscope.portal.server.web.view;

import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.web.service.JobTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

/**
 * Class for converting EAVLJob objects into a simplified Map for communication with the view
 * @author Josh Vote
 *
 */
@Service
public class ViewEAVLJobFactory {

    public static final String STATUS_UNSUBMITTED = "unsubmitted";
    public static final String STATUS_KDE_ERROR = "kde-error";
    public static final String STATUS_IMPUTE_ERROR = "impute-error";
    public static final String STATUS_IMPUTING = "imputing";
    public static final String STATUS_THRESHOLD = "threshold";
    public static final String STATUS_PROXY = "proxy";
    public static final String STATUS_SUBMITTED = "submitted";
    public static final String STATUS_DONE = "done";

    private JobTaskService jobTaskService;
    private FileStagingService fss;


    @Autowired
    public ViewEAVLJobFactory(JobTaskService jobTaskService,
            FileStagingService fss) {
        super();
        this.jobTaskService = jobTaskService;
        this.fss = fss;
    }

    public ModelMap toView(EAVLJob job) {
        String status;

        if (jobTaskService.isExecuting(job.getKdeTaskId())) {
            status = STATUS_SUBMITTED;
        } else if (fss.stageInFileExists(job, EAVLJobConstants.FILE_KDE_CSV)) {
            status = STATUS_DONE;
        } else if (job.getKdeTaskId() != null) {
            status = STATUS_KDE_ERROR;
        } else if (jobTaskService.isExecuting(job.getImputationTaskId())) {
            status = STATUS_IMPUTING;
        } else if (fss.stageInFileExists(job, EAVLJobConstants.FILE_IMPUTED_CSV)) {
            if (job.getPredictionCutoff() == null) {
                status = STATUS_THRESHOLD;
            } else {
                status = STATUS_PROXY;
            }
        } else if (job.getImputationTaskId() != null) {
            status = STATUS_IMPUTE_ERROR;
        } else {
            status = STATUS_UNSUBMITTED;
        }

        ModelMap m = new ModelMap();
        m.put("id", job.getId());
        m.put("name", job.getName());
        m.put("status", status);
        m.put("predictionCutoff", job.getPredictionCutoff());
        m.put("predictionParameter", job.getPredictionParameter());
        m.put("savedParameters", job.getSavedParameters());
        m.put("proxyParameters", job.getProxyParameters());
        m.put("imputationTaskId", job.getImputationTaskId());
        m.put("kdeTaskId", job.getKdeTaskId());
        m.put("holeIdParameter", job.getHoleIdParameter());

        return m;
    }
}
