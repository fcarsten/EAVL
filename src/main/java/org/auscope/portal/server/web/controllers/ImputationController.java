package org.auscope.portal.server.web.controllers;

import javax.servlet.http.HttpServletRequest;

import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.server.security.oauth2.PortalUser;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.auscope.portal.server.web.service.JobTaskService;
import org.auscope.portal.server.web.service.WpsService;
import org.auscope.portal.server.web.service.jobtask.ImputationCallable;
import org.auscope.portal.server.web.service.jobtask.JobTask;
import org.auscope.portal.server.web.service.wps.WpsServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.util.iterator.ArrayIterator;

@Controller
@RequestMapping("imputation")
public class ImputationController extends BasePortalController {

    private EAVLJobService jobService;
    private JobTaskService jobTaskService;
    private CSVService csvService;
    private FileStagingService fss;
    private WpsService wpsService;

    @Autowired
    public ImputationController(EAVLJobService jobService,
            JobTaskService jobTaskService, CSVService csvService,
            FileStagingService fss, WpsService wpsService) {
        super();
        this.jobService = jobService;
        this.jobTaskService = jobTaskService;
        this.csvService = csvService;
        this.fss = fss;
        this.wpsService = wpsService;
    }

    /**
     * Handles saving all of the imputation screen settings. Creates an Imputation Task
     * and submits it. Returns the task ID
     * @param request
     * @param savedColIndexes
     * @param predictorColIndex
     * @param predictorCutoff
     * @return
     */
    @RequestMapping("saveAndSubmitImputation.do")
    public ModelAndView saveImputationConfig(HttpServletRequest request, @AuthenticationPrincipal PortalUser user,
            @RequestParam(required=false, value="savedColName") String[] savedNames,
            @RequestParam("holeIdName") String holeIdName,
            @RequestParam("predictorName") String predictorName,
            @RequestParam("predictorCutoff") Double predictorCutoff) {

        if (savedNames == null) {
            savedNames = new String[] {};
        }

        try {
            EAVLJob job = jobService.getJobForSession(request, user);

            job.setSavedParameters(Sets.newHashSet(new ArrayIterator<String>(savedNames)));
            job.setPredictionCutoff(predictorCutoff);
            job.setPredictionParameter(predictorName);
            job.setHoleIdParameter(holeIdName);
            WpsServiceClient wpsClient = wpsService.getWpsClient();
            JobTask newTask = new JobTask(new ImputationCallable(job, wpsClient, csvService, fss), job);
            String taskId = jobTaskService.submit(newTask);

            job.setImputationTaskId(taskId);
            jobService.save(job);

            return generateJSONResponseMAV(true, taskId, "");
        } catch (Exception ex) {
            log.error("Unable to save imputation config", ex);
            return generateJSONResponseMAV(false);
        }
    }
}
