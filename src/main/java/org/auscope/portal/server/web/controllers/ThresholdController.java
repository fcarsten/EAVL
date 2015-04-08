package org.auscope.portal.server.web.controllers;

import javax.servlet.http.HttpServletRequest;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.security.oauth2.EavlUser;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.auscope.portal.server.web.service.ParameterDetailsService;
import org.auscope.portal.server.web.view.ViewEAVLJobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("threshold")
public class ThresholdController extends BasePortalController {

    private EAVLJobService jobService;
    private FileStagingService fss;
    private CSVService csvService;
	private ViewEAVLJobFactory viewFactory;
	private ParameterDetailsService pdService;

    @Autowired
    public ThresholdController(EAVLJobService jobService, FileStagingService fss, CSVService csvService, ViewEAVLJobFactory viewFactory, ParameterDetailsService pdService) {
        this.jobService = jobService;
        this.fss = fss;
        this.csvService = csvService;
        this.viewFactory = viewFactory;
        this.pdService = pdService;
    }

    /**
     * Handles saving all of the threshold screen settings.
     * @param request
     * @param savedColIndexes
     * @param predictorColIndex
     * @param predictorCutoff
     * @return
     */
    @RequestMapping("getConfig.do")
    public ModelAndView getConfig(HttpServletRequest request, @AuthenticationPrincipal EavlUser user) {

        try {
            EAVLJob job = jobService.getJobForSession(request, user);
            ModelMap response = new ModelMap();



            if (fss.stageInFileExists(job, EAVLJobConstants.FILE_IMPUTED_SCALED_CSV)) {
                response.put("parameterDetails", pdService.getParameterDetails(job, EAVLJobConstants.FILE_IMPUTED_SCALED_CSV));
            }
            response.put("job", viewFactory.toView(job));

            return generateJSONResponseMAV(true, response, "");
        } catch (Exception ex) {
            log.error("Unable to save imputation config", ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Handles saving all of the threshold screen settings.
     * @param request
     * @param savedColIndexes
     * @param predictorColIndex
     * @param predictorCutoff
     * @return
     */
    @RequestMapping("saveConfig.do")
    public ModelAndView saveConfig(HttpServletRequest request, @AuthenticationPrincipal EavlUser user,
            @RequestParam("predictorCutoff") Double predictorCutoff) {

        try {
            EAVLJob job = jobService.getJobForSession(request, user);

            job.setPredictionCutoff(predictorCutoff);
            jobService.save(job);

            return generateJSONResponseMAV(true, null, "");
        } catch (Exception ex) {
            log.error("Unable to save imputation config", ex);
            return generateJSONResponseMAV(false);
        }
    }
}
