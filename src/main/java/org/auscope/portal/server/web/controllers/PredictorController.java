package org.auscope.portal.server.web.controllers;

import javax.servlet.http.HttpServletRequest;

import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.server.security.oauth2.PortalUser;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.auscope.portal.server.web.service.JobTaskService;
<<<<<<< HEAD:src/main/java/org/auscope/portal/server/web/controllers/ImputationController.java
import org.auscope.portal.server.web.service.WpsService;
import org.auscope.portal.server.web.service.jobtask.ImputationCallable;
import org.auscope.portal.server.web.service.jobtask.JobTask;
import org.auscope.portal.server.web.service.wps.WpsServiceClient;
=======
>>>>>>> upstream/master:src/main/java/org/auscope/portal/server/web/controllers/PredictorController.java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("predictor")
public class PredictorController extends BasePortalController {

    private JobTaskService jobTaskService;
    private EAVLJobService jobService;
    private FileStagingService fss;
<<<<<<< HEAD:src/main/java/org/auscope/portal/server/web/controllers/ImputationController.java
    private WpsService wpsService;

    @Autowired
    public ImputationController(EAVLJobService jobService,
            JobTaskService jobTaskService, CSVService csvService,
            FileStagingService fss, WpsService wpsService) {
        super();
        this.jobService = jobService;
=======
    private ConditionalProbabilityWpsClient wpsClient;
    private CSVService csvService;

    @Autowired
    public PredictorController(JobTaskService jobTaskService, EAVLJobService jobService, FileStagingService fss, ConditionalProbabilityWpsClient wpsClient, CSVService csvService) {
>>>>>>> upstream/master:src/main/java/org/auscope/portal/server/web/controllers/PredictorController.java
        this.jobTaskService = jobTaskService;
        this.jobService = jobService;
        this.fss = fss;
<<<<<<< HEAD:src/main/java/org/auscope/portal/server/web/controllers/ImputationController.java
        this.wpsService = wpsService;
=======
        this.wpsClient = wpsClient;
        this.csvService = csvService;
>>>>>>> upstream/master:src/main/java/org/auscope/portal/server/web/controllers/PredictorController.java
    }

    /**
     * Handles saving all of the prediction screen settings.
     * @param request
     * @param savedColIndexes
     * @param predictorColIndex
     * @param predictorCutoff
     * @return
     */
    @RequestMapping("savePrediction.do")
    public ModelAndView savePrediction(HttpServletRequest request, @AuthenticationPrincipal PortalUser user,
            @RequestParam("holeIdName") String holeIdName,
            @RequestParam("predictorName") String predictorName,
            @RequestParam("predictorCutoff") Double predictorCutoff) {

        try {
            EAVLJob job = jobService.getJobForSession(request, user);

            job.setPredictionCutoff(predictorCutoff);
            job.setPredictionParameter(predictorName);
            job.setHoleIdParameter(holeIdName);
<<<<<<< HEAD:src/main/java/org/auscope/portal/server/web/controllers/ImputationController.java
            WpsServiceClient wpsClient = wpsService.getWpsClient();
            JobTask newTask = new JobTask(new ImputationCallable(job, wpsClient, csvService, fss), job);
            String taskId = jobTaskService.submit(newTask);

            job.setImputationTaskId(taskId);
=======
>>>>>>> upstream/master:src/main/java/org/auscope/portal/server/web/controllers/PredictorController.java
            jobService.save(job);

            return generateJSONResponseMAV(true, null, "");
        } catch (Exception ex) {
            log.error("Unable to save imputation config", ex);
            return generateJSONResponseMAV(false);
        }
    }

    @RequestMapping("getImputationStatus.do")
    public ModelAndView setEmailNotification(HttpServletRequest request,
            @AuthenticationPrincipal PortalUser user) {

        EAVLJob job;
        try {
            job = jobService.getJobForSession(request, user);
        } catch (PortalServiceException ex) {
            log.error("Unable to lookup job:", ex);
            return generateJSONResponseMAV(false);
        }

        if (job == null) {
            return generateJSONResponseMAV(true, false, "nojob");
        }


        if (fss.stageInFileExists(job, EAVLJobConstants.FILE_IMPUTED_CSV)) {
            return generateJSONResponseMAV(true, true, "");
        }

        if (job.getImputationTaskId() != null && !job.getImputationTaskId().isEmpty()) {
            if (jobTaskService.isExecuting(job.getImputationTaskId())) {
                return generateJSONResponseMAV(true, false, job.getImputationTaskId());
            } else {
                //This is an odd case - there was an imputation task but it's no longer running
                //We know the imputed data is unavailable (we tested above) so it's likely imputation failed
                return generateJSONResponseMAV(true, false, "failed");
            }
        }

        return generateJSONResponseMAV(true, false, "nodata");
    }
}
