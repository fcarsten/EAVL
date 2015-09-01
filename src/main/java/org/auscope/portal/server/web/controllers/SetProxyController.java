package org.auscope.portal.server.web.controllers;

import java.util.Date;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.Proxy;
import org.auscope.portal.server.security.oauth2.EavlUser;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.auscope.portal.server.web.service.JobTaskService;
import org.auscope.portal.server.web.service.WpsService;
import org.auscope.portal.server.web.service.jobtask.JobTask;
import org.auscope.portal.server.web.service.jobtask.KDECallable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("cp/setproxy")
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
            @RequestParam("numerator1") String numerator1,
            @RequestParam("denom1") String[] denom1,
            @RequestParam("numerator2") String numerator2,
            @RequestParam("denom2") String[] denom2,
            @RequestParam("numerator3") String numerator3,
            @RequestParam("denom3") String[] denom3) {

        EAVLJob job;
        try {
            job = jobService.getJobForSession(request, user);
        } catch (PortalServiceException ex) {
            log.error("Unable to lookup job:", ex);
            return generateJSONResponseMAV(false);
        }

        HashSet<Proxy> proxies = new HashSet<Proxy>();
        proxies.add(new Proxy(numerator1, denom1));
        proxies.add(new Proxy(numerator2, denom2));
        proxies.add(new Proxy(numerator3, denom3));
        try {
            job.setProxyParameters(proxies);
            JobTask newTask = new JobTask(job);
            newTask.setTask(new KDECallable(job, wpsService, csvService, fss, jobService));
            String taskId = jobTaskService.submit(newTask);
            job.setKdeTaskId(taskId);
            job.setKdeTaskError(null);
            job.setKdeSubmitDate(new Date());
            jobService.save(job);

            return generateJSONResponseMAV(true, taskId, "");
        } catch (PortalServiceException ex) {
            log.error("Unable to save job:", ex);
            return generateJSONResponseMAV(false);
        }
    }
}
