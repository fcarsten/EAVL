package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.server.security.oauth2.PortalUser;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.auscope.portal.server.web.service.JobTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller methods for the results page
 * @author Josh Vote
 *
 */
@RequestMapping("results")
@Controller
public class ResultsController extends BasePortalController {

    public static final String STATUS_UNSUBMITTED = "unsubmitted";
    public static final String STATUS_KDE_ERROR = "kde-error";
    public static final String STATUS_IMPUTE_ERROR = "impute-error";
    public static final String STATUS_IMPUTING = "imputing";
    public static final String STATUS_PROXY = "proxy";
    public static final String STATUS_SUBMITTED = "submitted";
    public static final String STATUS_DONE = "done";

    private EAVLJobService jobService;
    private JobTaskService jobTaskService;
    private FileStagingService fss;

    @Autowired
    public ResultsController(EAVLJobService jobService,
            JobTaskService jobTaskService, FileStagingService fss) {
        this.jobService = jobService;
        this.jobTaskService = jobTaskService;
        this.fss = fss;
    }

    private ModelMap jobToModel(EAVLJob job) {
        String status;

        if (jobTaskService.isExecuting(job.getKdeTaskId())) {
            status = STATUS_SUBMITTED;
        } else if (fss.stageInFileExists(job, EAVLJobConstants.FILE_KDE_JSON)) {
            status = STATUS_DONE;
        } else if (job.getKdeTaskId() != null) {
            status = STATUS_KDE_ERROR;
        } else if (jobTaskService.isExecuting(job.getImputationTaskId())) {
            status = STATUS_IMPUTING;
        } else if (fss.stageInFileExists(job, EAVLJobConstants.FILE_IMPUTED_CSV)) {
            status = STATUS_PROXY;
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

    @RequestMapping("getJobsForUser.do")
    public ModelAndView getJobsForUser(HttpServletRequest request, @AuthenticationPrincipal PortalUser user) {
        try {
            List<EAVLJob> jobs = jobService.getJobsForUser(request, user);
            List<ModelMap> jobModels = new ArrayList<ModelMap>(jobs.size());
            for (EAVLJob job : jobs) {
                jobModels.add(jobToModel(job));
            }
            return generateJSONResponseMAV(true, jobModels, "");
        } catch (PortalServiceException ex) {
            log.error("Unable to read user jobs: ", ex);
            return generateJSONResponseMAV(false, null, "");
        }
    }

}
