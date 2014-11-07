package org.auscope.portal.server.web.controllers;

import javax.servlet.http.HttpServletRequest;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("imputation")
public class ImputationController extends BasePortalController {

    private EAVLJobService jobService;

    @Autowired
    public ImputationController(EAVLJobService jobService) {
        this.jobService = jobService;
    }

    /**
     * Handles saving all of the imputation screen settings
     * @param request
     * @param savedColIndexes
     * @param predictorColIndex
     * @param predictorCutoff
     * @return
     */
    @RequestMapping("saveImputationConfig.do")
    public ModelAndView saveImputationConfig(HttpServletRequest request,
            @RequestParam(required=false, value="savedColIndex") Integer[] savedColIndexes,
            @RequestParam("predictorColIndex") Integer predictorColIndex,
            @RequestParam("predictorCutoff") Double predictorCutoff) {

        try {
            EAVLJob job = jobService.getJobForSession(request);

        } catch (Exception ex) {
            log.error("Unable to save imputation config", ex);
            return generateJSONResponseMAV(false);
        }

        return generateJSONResponseMAV(true);
    }
}
