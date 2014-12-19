package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.cloud.StagedFile;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.server.security.oauth2.PortalUser;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.view.JSONView;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.eavl.ParameterDetails;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.auscope.portal.server.web.service.JobTaskService;
import org.auscope.portal.server.web.service.WpsService;
import org.auscope.portal.server.web.service.jobtask.ImputationCallable;
import org.auscope.portal.server.web.service.jobtask.JobTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.util.iterator.ArrayIterator;

/**
 * Controller for supporting validation of a new data upload
 * @author Josh Vote
 *
 */
@Controller
@RequestMapping("validation")
public class ValidationController extends BasePortalController {

    private FileStagingService fss;
    private CSVService csvService;
    private EAVLJobService jobService;
    private JobTaskService jobTaskService;
    private WpsService wpsService;

    @Autowired
    public ValidationController(FileStagingService fss, CSVService csvService, EAVLJobService jobService, JobTaskService jobTaskService, WpsService wpsService) {
        this.fss = fss;
        this.csvService = csvService;
        this.jobService = jobService;
        this.jobTaskService = jobTaskService;
        this.wpsService = wpsService;
    }

    /**
     * Processes a file upload request returning a JSON object which indicates
     * whether the upload was successful and contains the filename and file
     * size.
     *
     * @param request The servlet request
     * @param response The servlet response containing the JSON data
     *
     * @return null
     */
    @RequestMapping("/uploadFile.do")
    public ModelAndView uploadFile(HttpServletRequest request,
            @AuthenticationPrincipal PortalUser user,
            @RequestParam(required=false,value="jobId") Integer jobId) {


        EAVLJob job;
        try {
            if (jobId == null) {
                job = jobService.createJobForSession(request);
            } else {
                job = jobService.getUserJobById(request, user, jobId);
            }
        } catch (PortalServiceException ex) {
            log.error("Error creating/fetching job during upload file", ex);
            return generateHTMLResponseMAV(false, null, "");
        }

        InputStream inputCsv = null;
        OutputStream outputCsv = null;

        //Handle incoming file
        StagedFile file = null;
        try {
            if (!fss.stageInDirectoryExists(job)) {
                fss.generateStageInDirectory(job);
            }
            file = fss.handleFileUpload(job, (MultipartHttpServletRequest) request);

            job.setName(file.getName());
            jobService.save(job);

            //Rename it to a temp file, remove missing lines and put it into the correct file
            if (!fss.renameStageInFile(job, file.getName(), EAVLJobConstants.FILE_TEMP_DATA_CSV)) {
                throw new IOException("Unable to operate on files in staging area - rename file failed");
            }

            inputCsv = fss.readFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);
            outputCsv = fss.writeFile(job, EAVLJobConstants.FILE_DATA_CSV);
            csvService.findReplace(inputCsv, outputCsv, 0, null, null, true); //This just culls empty lines and autogenerates a header (if missing)

            IOUtils.closeQuietly(inputCsv);
            IOUtils.closeQuietly(outputCsv);

            inputCsv = fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
            List<ParameterDetails> pdList = csvService.extractParameterDetails(inputCsv);

            ModelMap response = new ModelMap();
            response.put("id", job.getId());
            response.put("parameterDetails", pdList);

            //We have to use a HTML response due to ExtJS's use of a hidden iframe for file uploads
            //Failure to do this will result in the upload working BUT the user will also get prompted
            //for a file download containing the encoded response from this function (which we don't want).
            return generateHTMLResponseMAV(true, response, "");
        } catch (Exception ex) {
            log.error("Error uploading file", ex);
            return generateHTMLResponseMAV(false, null, "Error uploading file");
        } finally {
            IOUtils.closeQuietly(inputCsv);
            IOUtils.closeQuietly(outputCsv);
        }
    }

    @RequestMapping("/getParameterDetails.do")
    public ModelAndView getParameterDetails(HttpServletRequest request, @AuthenticationPrincipal PortalUser user,
            @RequestParam(required=false,value="file") String file,
            @RequestParam(required=false,value="jobId") Integer jobId) {
        InputStream csvData = null;
        try {
            EAVLJob job;
            if (jobId != null) {
                job = jobService.getUserJobById(request, user, jobId);
            } else {
                job = jobService.getJobForSession(request, user);
            }

            String fileToRead = (file == null || file.isEmpty()) ?  EAVLJobConstants.FILE_DATA_CSV : file;
            csvData = fss.readFile(job, fileToRead);
            return generateJSONResponseMAV(true, csvService.extractParameterDetails(csvData), "");
        } catch (Exception ex) {
            log.warn("Unable to get parameter details: ", ex);
            return generateJSONResponseMAV(false, null, "Error reading file");
        } finally {
            IOUtils.closeQuietly(csvData);
        }
    }

    @RequestMapping("/getCompositionalParameterDetails.do")
    public ModelAndView getCompositionalParameterDetails(HttpServletRequest request, @AuthenticationPrincipal PortalUser user,
            @RequestParam(required=false,value="file") String file,
            @RequestParam(required=false,value="jobId") Integer jobId) {
        InputStream csvData = null;
        try {
            EAVLJob job;
            if (jobId != null) {
                job = jobService.getUserJobById(request, user, jobId);
            } else {
                job = jobService.getJobForSession(request, user);
            }

            String fileToRead = (file == null || file.isEmpty()) ?  EAVLJobConstants.FILE_DATA_CSV : file;
            csvData = fss.readFile(job, fileToRead);

            List<ParameterDetails> pds = csvService.extractParameterDetails(csvData);
            if (job.getSavedParameters() != null) {
                for (int i = pds.size() - 1; i >= 0; i--) {
                    if (job.getSavedParameters().contains(pds.get(i).getName())) {
                        pds.remove(i);
                    }
                }
            }

            return generateJSONResponseMAV(true, pds, "");
        } catch (Exception ex) {
            log.warn("Unable to get parameter details: ", ex);
            return generateJSONResponseMAV(false, null, "Error reading file");
        } finally {
            IOUtils.closeQuietly(csvData);
        }
    }

    @RequestMapping("/getParameterValues.do")
    public ModelAndView getParameterValues(HttpServletRequest request, @AuthenticationPrincipal PortalUser user,
            @RequestParam("columnIndex") int columnIndex) {
        try {
            EAVLJob job = jobService.getJobForSession(request, user);
            InputStream csvData = fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
            return generateJSONResponseMAV(true, csvService.getParameterValues(csvData, columnIndex), "");
        } catch (Exception ex) {
            log.warn("Unable to get parameter values: ", ex);
            return generateJSONResponseMAV(false, null, "Error reading file");
        }
    }


    @RequestMapping("/streamRows.do")
    public ModelAndView streamRows(HttpServletRequest request, @AuthenticationPrincipal PortalUser user,
            @RequestParam("limit") Integer limit,
            @RequestParam("page") Integer page,
            @RequestParam("start") Integer start,
            @RequestParam(required=false,value="file") String file,
            @RequestParam(required=false,value="jobId") Integer jobId) {
        try {
            EAVLJob job;
            if (jobId != null) {
                job = jobService.getUserJobById(request, user, jobId);
            } else {
                job = jobService.getJobForSession(request, user);
            }

            String fileToRead = (file == null || file.isEmpty()) ?  EAVLJobConstants.FILE_DATA_CSV : file;
            List<String[]> data = csvService.readLines(fss.readFile(job, fileToRead), start + 1, limit); //we add 1 to start to skip header
            int totalData = csvService.countLines(fss.readFile(job, fileToRead)) - 1; //we skip 1 for the header too (This could be cached)

            ModelMap response = new ModelMap();
            response.put("totalCount", totalData);
            response.put("rows", data);
            return new ModelAndView(new JSONView(), response);
        } catch (Exception ex) {
            log.warn("Unable to stream rows: ", ex);
            return generateJSONResponseMAV(false, null, "Error reading file");
        }
    }

    @RequestMapping("/findReplace.do")
    public ModelAndView findReplace(HttpServletRequest request, @AuthenticationPrincipal PortalUser user,
            @RequestParam("find") String find,
            @RequestParam("replace") String replace,
            @RequestParam("columnIndex") int columnIndex) {

        OutputStream os = null;
        InputStream is = null;

        //An empty find string means search for zeroes
        if (find.trim().isEmpty()) {
            find = null;
        }

        try {
            EAVLJob job = jobService.getJobForSession(request, user);
            os = fss.writeFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);
            is = fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);

            if (find == null) {
                csvService.findReplaceZeroes(is, os, columnIndex, replace, false);
            } else {
                csvService.findReplace(is, os, columnIndex, find, replace);
            }

            fss.renameStageInFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV, EAVLJobConstants.FILE_DATA_CSV);
        } catch (Exception ex) {
            log.error("Error replacing within file: ", ex);
            return generateJSONResponseMAV(false, null, "Unable to find/replace");
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }

        return generateJSONResponseMAV(true, null, "");
    }

    @RequestMapping("/deleteParameters.do")
    public ModelAndView deleteParameters(HttpServletRequest request, @AuthenticationPrincipal PortalUser user,
            @RequestParam("columnIndex") Integer[] columnIndexes) {

        OutputStream os = null;
        InputStream is = null;

        try {
            EAVLJob job = jobService.getJobForSession(request, user);
            os = fss.writeFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);
            is = fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);

            csvService.deleteColumns(is, os, Sets.newHashSet(new ArrayIterator<Integer>(columnIndexes)));

            fss.renameStageInFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV, EAVLJobConstants.FILE_DATA_CSV);
        } catch (Exception ex) {
            log.error("Error deleting columns: ", ex);
            return generateJSONResponseMAV(false, null, "Unable to find/replace");
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }

        return generateJSONResponseMAV(true, null, "");
    }

    @RequestMapping("/saveValidationSubmitImputation.do")
    public ModelAndView saveValidationSubmitImputation(HttpServletRequest request, @AuthenticationPrincipal PortalUser user,
            @RequestParam(value="deleteColIndex", required=false) Integer[] delColIndexes) {

        OutputStream os = null;
        InputStream is = null;

        try {
            EAVLJob job = jobService.getJobForSession(request, user);
            os = fss.writeFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV);
            is = fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);

            if (delColIndexes != null && delColIndexes.length > 0) {
                csvService.deleteColumns(is, os, Sets.newHashSet(new ArrayIterator<Integer>(delColIndexes)));
                fss.renameStageInFile(job, EAVLJobConstants.FILE_TEMP_DATA_CSV, EAVLJobConstants.FILE_DATA_CSV);
            }

            JobTask newTask = new JobTask(new ImputationCallable(job, wpsService, csvService, fss), job);
            String taskId = jobTaskService.submit(newTask);

            job.setImputationTaskId(taskId);
            jobService.save(job);

            return generateJSONResponseMAV(true, taskId, "");
        } catch (Exception ex) {
            log.error("Error deleting columns: ", ex);
            return generateJSONResponseMAV(false, null, "Unable to find/replace");
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }
    }
}
