package org.auscope.portal.server.web.controllers;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.eavl.Parameter;
import org.auscope.portal.server.security.oauth2.EavlUser;
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

/**
 * Controller for supporting validation of a new data upload
 * @author Josh Vote
 *
 */
@Controller
@RequestMapping("cp/identify")
public class IdentifyController extends BasePortalController {

    private EAVLJobService jobService;
    private ViewEAVLJobFactory viewFactory;
    private ParameterDetailsService pdService;

    @Autowired
    public IdentifyController(EAVLJobService jobService, ViewEAVLJobFactory viewFactory, ParameterDetailsService pdService) {
        this.jobService = jobService;
        this.viewFactory = viewFactory;
        this.pdService = pdService;
    }

    @RequestMapping("/getConfig.do")
    public ModelAndView getParameterDetails(HttpServletRequest request, @AuthenticationPrincipal EavlUser user,
            @RequestParam(required=false,value="jobId") Integer jobId) {
        try {
            EAVLJob job;
            if (jobId != null) {
                job = jobService.getUserJobById(request, user, jobId);
            } else {
                job = jobService.getJobForSession(request, user);
            }

            ModelMap response = new ModelMap();
            response.put("parameterDetails", pdService.getParameterDetails(job, EAVLJobConstants.FILE_DATA_CSV));
            response.put("job", viewFactory.toView(job));

            return generateJSONResponseMAV(true, response, "");
        } catch (Exception ex) {
            log.warn("Unable to get parameter details: ", ex);
            return generateJSONResponseMAV(false, null, "Error reading file");
        }
    }

    /**
     * Saves config options into the specified job object (or the current session job)
     *
     * @param request
     * @param user
     * @param jobId
     * @param holeIdName
     * @param predictorName
     * @param saveColNames
     * @return
     */
    @RequestMapping("/saveConfig.do")
    public ModelAndView saveConfig(HttpServletRequest request, @AuthenticationPrincipal EavlUser user,
            @RequestParam(required=false,value="jobId") Integer jobId,
            @RequestParam("holeIdName") String holeIdName,
            @RequestParam("holeIdIndex") Integer holeIdIndex,
            @RequestParam("predictorName") String predictorName,
            @RequestParam(value="saveColName", required=false) String[] saveColNames,
            @RequestParam(value="saveColIndex", required=false) Integer[] saveColIndexes) {

        if (saveColIndexes != null && saveColNames != null && saveColNames.length != saveColIndexes.length) {
            return generateJSONResponseMAV(false, null, "");
        }

        try {
            EAVLJob job;

            if (jobId == null) {
                job = jobService.getJobForSession(request, user);
            } else {
                job = jobService.getUserJobById(request, user, jobId);
            }


            HashSet<Parameter> set = new HashSet<Parameter>();
            if (saveColNames != null) {
                for (int i = 0; i < saveColNames.length; i++) {
                    set.add(new Parameter(saveColNames[i], saveColIndexes[i]));
                }
            }
            set.add(new Parameter(holeIdName, holeIdIndex)); //The hole ID column is automatically "saved" (by definition it's non compositional)

            job.setSavedParameters(set);
            job.setHoleIdParameter(holeIdName);
            job.setPredictionParameter(predictorName);
            jobService.save(job);

            return generateJSONResponseMAV(true, null, "");
        } catch (Exception ex) {
            log.error("Unable to save identify config: ", ex);
            return generateJSONResponseMAV(false, null, "");
        }
    }
}
