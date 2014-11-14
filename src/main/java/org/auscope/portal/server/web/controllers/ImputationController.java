package org.auscope.portal.server.web.controllers;

import javax.servlet.http.HttpServletRequest;

import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.auscope.portal.server.web.service.JobTaskService;
import org.auscope.portal.server.web.service.jobtask.ImputationCallable;
import org.auscope.portal.server.web.service.jobtask.JobTask;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ConditionalProbabilityWpsClient wpsClient;

    @Autowired
    public ImputationController(EAVLJobService jobService,
            JobTaskService jobTaskService, CSVService csvService,
            FileStagingService fss, ConditionalProbabilityWpsClient wpsClient) {
        super();
        this.jobService = jobService;
        this.jobTaskService = jobTaskService;
        this.csvService = csvService;
        this.fss = fss;
        this.wpsClient = wpsClient;
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
    public ModelAndView saveImputationConfig(HttpServletRequest request,
            @RequestParam(required=false, value="savedColName") String[] savedNames,
            @RequestParam("holeIdName") String holeIdName,
            @RequestParam("predictorName") String predictorName,
            @RequestParam("predictorCutoff") Double predictorCutoff) {

        if (savedNames == null) {
            savedNames = new String[] {};
        }

        try {
            EAVLJob job = jobService.getJobForSession(request);

            job.setSavedParameters(Sets.newHashSet(new ArrayIterator<String>(savedNames)));
            job.setPredictionCutoff(predictorCutoff);
            job.setPredictionParameter(predictorName);
            job.setHoleIdParameter(holeIdName);

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
