package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.cloud.StagedFile;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.server.security.oauth2.PortalUser;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.auscope.portal.server.web.service.JobTaskService;
import org.auscope.portal.server.web.view.ViewEAVLJobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller methods for the results page
 * @author Josh Vote
 *
 */
@RequestMapping("results")
@Controller
public class ResultsController extends BasePortalController {

    private EAVLJobService jobService;
    private JobTaskService jobTaskService;
    private FileStagingService fss;
    private ViewEAVLJobFactory viewFactory;
    private CSVService csvService;

    @Autowired
    public ResultsController(EAVLJobService jobService,
            JobTaskService jobTaskService, FileStagingService fss, ViewEAVLJobFactory viewFactory, CSVService csvService) {
        this.jobService = jobService;
        this.jobTaskService = jobTaskService;
        this.fss = fss;
        this.viewFactory = viewFactory;
        this.csvService = csvService;
    }

    /**
     * Gets the list of jobs for a particular user
     * @param request
     * @param user
     * @return
     */
    @RequestMapping("getJobsForUser.do")
    public ModelAndView getJobsForUser(HttpServletRequest request, @AuthenticationPrincipal PortalUser user) {
        try {
            List<EAVLJob> jobs = jobService.getJobsForUser(request, user);
            List<ModelMap> jobModels = new ArrayList<ModelMap>(jobs.size());
            for (EAVLJob job : jobs) {
                jobModels.add(viewFactory.toView(job));
            }
            return generateJSONResponseMAV(true, jobModels, "");
        } catch (PortalServiceException ex) {
            log.error("Unable to read user jobs: ", ex);
            return generateJSONResponseMAV(false, null, "");
        }
    }

    /**
     * Returns a list of File objects containing filename and filesize for a given job
     * @param request
     * @param user
     * @param jobId
     * @return
     */
    @RequestMapping("getFilesForJob.do")
    public ModelAndView getFilesForJob(HttpServletRequest request, @AuthenticationPrincipal PortalUser user,
            @RequestParam("jobId") Integer jobId) {
        try {
            EAVLJob job = jobService.getUserJobById(request, user, jobId);
            if (job == null) {
                log.warn(String.format("getFilesForJob - User %1$s attempting to access job %2$s was rejected. (Job might not exist)", user, jobId));
                return generateJSONResponseMAV(false);
            }

            List<ModelMap> files = new ArrayList<ModelMap>();
            for (StagedFile file : fss.listStageInDirectoryFiles(job)) {
                ModelMap m = new ModelMap();
                m.put("name", file.getName());
                m.put("size", file.getFile().length());
                files.add(m);
            }

            return generateJSONResponseMAV(true, files, "");
        } catch (Exception ex) {
            log.error("Unable to get files for job: ", ex);
            return generateJSONResponseMAV(false);
        }
    }

    @RequestMapping("getKDEGeometry.do")
    public ModelAndView getKDEGeometry(HttpServletRequest request,
            @AuthenticationPrincipal PortalUser user,
            @RequestParam("jobId") Integer jobId,
            @RequestParam("name") String fileName) {

        InputStream is = null;
        try {
            EAVLJob job = jobService.getUserJobById(request, user, jobId);
            if (job == null) {
                log.warn(String.format("getKDEGeometry - User %1$s attempting to access job %2$s was rejected. (Job might not exist)", user, jobId));
                return generateJSONResponseMAV(false);
            }

            is = fss.readFile(job, fileName);
            String jsonData = IOUtils.toString(is, StandardCharsets.UTF_8.name());
            JSONObject json = JSONObject.fromObject(jsonData);
            List<ModelMap> responsePoints = new ArrayList<ModelMap>();

            JSONObject gkde = (JSONObject) json.get("gkde");
            JSONArray x1 = (JSONArray) ((JSONObject) gkde.get("eval.points")).get("X1");
            JSONArray x2 = (JSONArray) ((JSONObject) gkde.get("eval.points")).get("X2");
            JSONArray x3 = (JSONArray) ((JSONObject) gkde.get("eval.points")).get("X3");
            JSONObject estimate = (JSONObject) gkde.get("estimate");

            if (x1.size() != x2.size() || x2.size() != x3.size()) {
                log.error("JSON response points contain a differing number of X1/X2/X3 values for jobId:" + jobId);
                return generateJSONResponseMAV(false);
            }

            for (int i = 0; i < x1.size(); i++) {
                ModelMap point = new ModelMap();
                point.put("x", x1.get(i));
                point.put("y", x2.get(i));
                point.put("z", x3.get(i));
                point.put("estimate", estimate.get(Integer.toString((i + 1))));
                responsePoints.add(point);
            }

            Iterator<String> proxies = job.getProxyParameters().iterator(); //TODO: This may potentially fail due to set order being undefined

            ModelMap response = new ModelMap();
            response.put("points", responsePoints);
            response.put("xLabel", proxies.next());
            response.put("yLabel", proxies.next());
            response.put("zLabel", proxies.next());

            return generateJSONResponseMAV(true, response, "");
        } catch (Exception ex) {
            log.error("Unable to get KDE geometry for job: ", ex);
            return generateJSONResponseMAV(false);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Sends the contents of one or more job files to the client.
     *
     * @param request The servlet request including a jobId parameter and a
     *                filename parameter
     * @param response The servlet response receiving the data
     *
     * @return null on success or the joblist view with an error parameter on
     *         failure.
     */
    @RequestMapping("downloadFiles.do")
    public void downloadFiles(HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal PortalUser user,
            @RequestParam("jobId") Integer jobId,
            @RequestParam("name") String[] fileNames) throws Exception {

        EAVLJob job = jobService.getUserJobById(request, user, jobId);
        if (job == null) {
            log.warn(String.format("downloadFile - User %1$s attempting to access job %2$s was rejected. (Job might not exist)", user, jobId));
            response.sendError(HttpStatus.SC_FORBIDDEN);
            return;
        }

        //Create a filename that is semi-unique to the job (and slightly human readable)
        String downloadFileName = "";
        if (fileNames.length == 1) {
            downloadFileName = fileNames[0];
        } else {
            String jobName = job.getName() == null ? "" : job.getName();
            Date submitDate = job.getSubmitDate();
            if (submitDate == null) {
                downloadFileName = String.format("files_%1$s.zip", jobName);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                downloadFileName = String.format("files_%1$s_%2$s.zip", jobName, sdf.format(job.getSubmitDate()));
            }
            downloadFileName = downloadFileName.replaceAll("[^0-9a-zA-Z_.]", "_");
        }


        //Start writing our data to a zip archive (which is being streamed to user)
        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%1$s\"", downloadFileName));

            boolean readOneOrMoreFiles = false;
            ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());

            for (String fileName : fileNames) {
                InputStream is = null;
                try {
                    is = fss.readFile(job, fileName);
                    zout.putNextEntry(new ZipEntry(fileName));
                    IOUtils.copy(is, zout);
                    zout.closeEntry();
                    readOneOrMoreFiles = true;
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }

            if (readOneOrMoreFiles) {
                zout.finish();
                zout.flush();
                zout.close();
            } else {
                zout.close();
            }

        } catch (IOException e) {
            log.warn("Could not create ZIP file", e);
        } catch (Exception e) {
            log.warn("Error getting cloudObject data", e);
        }
    }

    /**
     * Returns a JSON Object encoding of the current session job (or other specified job).
     *
     * @param request
     * @param user
     * @return
     */
    @RequestMapping("getJobStatus.do")
    public ModelAndView getJobStatus(HttpServletRequest request,
            @AuthenticationPrincipal PortalUser user,
            @RequestParam(value="jobId", required=false) Integer jobId) {

        EAVLJob job;
        try {
            if (jobId == null) {
                job = jobService.getJobForSession(request, user);
            } else {
                job = jobService.getUserJobById(request, user, jobId);
            }
        } catch (PortalServiceException ex) {
            log.error("Unable to lookup job:", ex);
            return generateJSONResponseMAV(false);
        }

        if (job == null) {
            return generateJSONResponseMAV(false, null, "");
        }

        return generateJSONResponseMAV(true, viewFactory.toView(job), "");
    }

    /**
     * Returns a JSON Object encoding the specified jobfiles data grouped by
     * a particular column.
     *
     * Data is returned in the form
     * [
     *  {group : holeid1, values : [param1Value, param2Value...]}
     *  {group : holeid2, values : [param1Value, param2Value...]}
     *        ...
     * ]
     *
     * @param request
     * @param user
     * @return
     */
    @RequestMapping("getGroupedValues.do")
    public ModelAndView getJobStatus(HttpServletRequest request,
            @AuthenticationPrincipal PortalUser user,
            @RequestParam(value="jobId", required=false) Integer jobId,
            @RequestParam("fileName") String fileName,
            @RequestParam("groupName") String groupName,
            @RequestParam("paramName") String[] paramNames) {

        EAVLJob job;
        InputStream is = null;
        try {
            if (jobId == null) {
                job = jobService.getJobForSession(request, user);
            } else {
                job = jobService.getUserJobById(request, user, jobId);
            }


            List<String> columnNames = new ArrayList<String>(paramNames.length + 1);
            columnNames.add(groupName);
            columnNames.addAll(Arrays.asList(paramNames));

            is = fss.readFile(job, fileName);
            List<Integer> indexes = csvService.columnNameToIndex(is, columnNames);

            is = fss.readFile(job, fileName);
            String[][] rawData = csvService.getRawStringData(is, indexes, true);

            //Group our input data
            Map<String, List<String[]>> groupedDataMap = new HashMap<String, List<String[]>>();
            for (String[] row : rawData) {
                String holeId = row[0];
                List<String[]> data = groupedDataMap.get(holeId);
                if (data == null) {
                    data = new ArrayList<String[]>();
                    groupedDataMap.put(holeId, data);
                }

                data.add(Arrays.copyOfRange(row, 1, row.length));
            }

            //Convert to an array of objects
            List<ModelMap> response = new ArrayList<ModelMap>();
            for (String group : groupedDataMap.keySet()) {
                ModelMap item = new ModelMap();
                item.put("group", group);
                item.put("values", groupedDataMap.get(group));
                response.add(item);
            }

            return generateJSONResponseMAV(true, response, "");
        } catch (PortalServiceException ex) {
            log.error("Unable to lookup job:", ex);
            return generateJSONResponseMAV(false);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
