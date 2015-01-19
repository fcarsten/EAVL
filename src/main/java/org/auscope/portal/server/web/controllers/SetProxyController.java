package org.auscope.portal.server.web.controllers;

import javax.servlet.http.HttpServletRequest;

import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.security.oauth2.EavlUser;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.auscope.portal.server.web.service.JobTaskService;
import org.auscope.portal.server.web.service.WpsService;
import org.auscope.portal.server.web.service.jobtask.JobTask;
import org.auscope.portal.server.web.service.jobtask.KDECallable;
import org.auscope.portal.server.web.service.wps.WpsServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Sets;

@Controller
@RequestMapping("setproxy")
public class SetProxyController extends BasePortalController {
    private JobTaskService jobTaskService;
    private EAVLJobService jobService;
    private FileStagingService fss;
    private WpsService wpsService;
    private CSVService csvService;

    @Autowired
    public SetProxyController(JobTaskService jobTaskService, EAVLJobService jobService, FileStagingService fss, WpsService wpsService, CSVService csvService) {
        super();
        this.jobTaskService = jobTaskService;
        this.jobService = jobService;
        this.fss = fss;
        this.wpsService = wpsService;
        this.csvService = csvService;
    }

    @RequestMapping("saveAndSubmitProxySelection.do")
    public ModelAndView saveAndSubmitProxySelection(HttpServletRequest request,
            @AuthenticationPrincipal EavlUser user,
            @RequestParam("proxy") String[] proxies) {

        EAVLJob job;
        try {
            job = jobService.getJobForSession(request, user);
        } catch (PortalServiceException ex) {
            log.error("Unable to lookup job:", ex);
            return generateJSONResponseMAV(false);
        }

        try {
            job.setProxyParameters(Sets.newHashSet(proxies));
            JobTask newTask = new JobTask(job);
            newTask.setTask(new KDECallable(job, wpsService, csvService, fss));
            String taskId = jobTaskService.submit(newTask);
            job.setKdeTaskId(taskId);
            jobService.save(job);

            return generateJSONResponseMAV(true, taskId, "");
        } catch (PortalServiceException ex) {
            log.error("Unable to save job:", ex);
            return generateJSONResponseMAV(false);
        }
    }
}
