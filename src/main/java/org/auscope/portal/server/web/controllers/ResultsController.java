package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.cloud.StagedFile;
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

    public static final String STATUS_UNSUBMITTED = "unsubmitted";
    public static final String STATUS_KDE_ERROR = "kde-error";
    public static final String STATUS_IMPUTE_ERROR = "impute-error";
    public static final String STATUS_IMPUTING = "imputing";
    public static final String STATUS_PROXY = "proxy";
    public static final String STATUS_SUBMITTED = "submitted";
    public static final String STATUS_DONE = "done";

    private EAVLJobService jobService;
    private JobTaskService jobTaskService;
    private FileStagingService fss;

    @Autowired
    public ResultsController(EAVLJobService jobService,
            JobTaskService jobTaskService, FileStagingService fss) {
        this.jobService = jobService;
        this.jobTaskService = jobTaskService;
        this.fss = fss;
    }

    private ModelMap jobToModel(EAVLJob job) {
        String status;

        if (jobTaskService.isExecuting(job.getKdeTaskId())) {
            status = STATUS_SUBMITTED;
        } else if (fss.stageInFileExists(job, EAVLJobConstants.FILE_KDE_JSON)) {
            status = STATUS_DONE;
        } else if (job.getKdeTaskId() != null) {
            status = STATUS_KDE_ERROR;
        } else if (jobTaskService.isExecuting(job.getImputationTaskId())) {
            status = STATUS_IMPUTING;
        } else if (fss.stageInFileExists(job, EAVLJobConstants.FILE_IMPUTED_CSV)) {
            status = STATUS_PROXY;
        } else if (job.getImputationTaskId() != null) {
            status = STATUS_IMPUTE_ERROR;
        } else {
            status = STATUS_UNSUBMITTED;
        }

        ModelMap m = new ModelMap();
        m.put("id", job.getId());
        m.put("name", job.getName());
        m.put("status", status);
        m.put("predictionCutoff", job.getPredictionCutoff());
        m.put("predictionParameter", job.getPredictionParameter());
        m.put("savedParameters", job.getSavedParameters());
        m.put("proxyParameters", job.getProxyParameters());
        m.put("imputationTaskId", job.getImputationTaskId());
        m.put("kdeTaskId", job.getKdeTaskId());
        m.put("holeIdParameter", job.getHoleIdParameter());

        return m;
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
                jobModels.add(jobToModel(job));
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
}
