package org.auscope.portal.server.web.controllers;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("setproxy")
public class SetProxyController extends BasePortalController {
    private JobTaskService jobTaskService;
    private EAVLJobService jobService;
    private FileStagingService fss;

    @Autowired
    public SetProxyController(JobTaskService jobTaskService, EAVLJobService jobService, FileStagingService fss) {
        super();
        this.jobTaskService = jobTaskService;
        this.jobService = jobService;
        this.fss = fss;
    }

    @RequestMapping("getImputationStatus.do")
    public ModelAndView setEmailNotification(HttpServletRequest request,
            @AuthenticationPrincipal PortalUser user) {

        EAVLJob job;
        try {
            job = jobService.getJobForSession(request);
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
