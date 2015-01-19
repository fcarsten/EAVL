package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.security.oauth2.EavlUser;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.auscope.portal.server.web.service.JobTaskService;
import org.auscope.portal.server.web.service.WpsService;
import org.auscope.portal.server.web.view.ViewEAVLJobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Sets;

/**
 * Controller for supporting validation of a new data upload
 * @author Josh Vote
 *
 */
@Controller
@RequestMapping("identify")
public class IdentifyController extends BasePortalController {

    private FileStagingService fss;
    private CSVService csvService;
    private EAVLJobService jobService;
    private JobTaskService jobTaskService;
    private WpsService wpsService;
    private ViewEAVLJobFactory viewFactory;

    @Autowired
    public IdentifyController(FileStagingService fss, CSVService csvService, EAVLJobService jobService,
            JobTaskService jobTaskService, WpsService wpsService, ViewEAVLJobFactory viewFactory) {
        this.fss = fss;
        this.csvService = csvService;
        this.jobService = jobService;
        this.jobTaskService = jobTaskService;
        this.wpsService = wpsService;
        this.viewFactory = viewFactory;
    }

    @RequestMapping("/getConfig.do")
    public ModelAndView getParameterDetails(HttpServletRequest request, @AuthenticationPrincipal EavlUser user,
            @RequestParam(required=false,value="jobId") Integer jobId) {

        InputStream csvData = null;
        try {
            EAVLJob job;
            if (jobId != null) {
                job = jobService.getUserJobById(request, user, jobId);
            } else {
                job = jobService.getJobForSession(request, user);
            }

            csvData = fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);

            ModelMap response = new ModelMap();
            response.put("parameterDetails", csvService.extractParameterDetails(csvData));
            response.put("job", viewFactory.toView(job));

            return generateJSONResponseMAV(true, response, "");
        } catch (Exception ex) {
            log.warn("Unable to get parameter details: ", ex);
            return generateJSONResponseMAV(false, null, "Error reading file");
        } finally {
            IOUtils.closeQuietly(csvData);
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
            @RequestParam("predictorName") String predictorName,
            @RequestParam(value="saveColName", required=false) String[] saveColNames) {


        try {
            EAVLJob job;

            if (jobId == null) {
                job = jobService.getJobForSession(request, user);
            } else {
                job = jobService.getUserJobById(request, user, jobId);
            }

            //The hole ID column is automatically "saved" (by definition it's non compositional)
            HashSet<String> set;
            if (saveColNames != null) {
                set = Sets.newHashSet(saveColNames);
            } else {
                set = new HashSet<String>();
            }
            set.add(holeIdName);

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
