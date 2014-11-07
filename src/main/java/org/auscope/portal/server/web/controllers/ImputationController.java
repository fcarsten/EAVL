package org.auscope.portal.server.web.controllers;

import javax.servlet.http.HttpServletRequest;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("imputation")
public class ImputationController extends BasePortalController {


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


        //TODO
        return generateJSONResponseMAV(true);
    }
}
