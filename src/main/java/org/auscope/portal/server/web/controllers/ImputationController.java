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

import com.google.common.collect.Sets;
import com.hp.hpl.jena.util.iterator.ArrayIterator;

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
            @RequestParam(required=false, value="savedColName") String[] savedNames,
            @RequestParam("predictorName") String predictorName,
            @RequestParam("predictorCutoff") Double predictorCutoff) {

        try {
            EAVLJob job = jobService.getJobForSession(request);

            job.setSavedParameters(Sets.newHashSet(new ArrayIterator<String>(savedNames)));
            job.setPredictionCutoff(predictorCutoff);
            job.setPredictionParameter(predictorName);

            jobService.save(job);
        } catch (Exception ex) {
            log.error("Unable to save imputation config", ex);
            return generateJSONResponseMAV(false);
        }

        return generateJSONResponseMAV(true);
    }
}
