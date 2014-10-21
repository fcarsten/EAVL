package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.core.cloud.StagedFile;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.view.JSONView;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.FileInformation;
import org.auscope.portal.server.web.service.CSVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

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

    @Autowired
    public ValidationController(FileStagingService fss, CSVService csvService) {
        this.fss = fss;
        this.csvService = csvService;
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
            HttpServletResponse response) {

        //Lookup our job - TODO - use temp job at the moment
        EAVLJob job = new EAVLJob(1);

        //Handle incoming file
        StagedFile file = null;
        try {
            fss.generateStageInDirectory(job);
            file = fss.handleFileUpload(job, (MultipartHttpServletRequest) request);
        } catch (Exception ex) {
            log.error("Error uploading file", ex);
            return generateHTMLResponseMAV(false, null, "Error uploading file");
        }

        File internalFile = file.getFile();
        long length = internalFile == null ? 0 : internalFile.length();
        FileInformation fileInfo = new FileInformation(file.getName(), length, false, "");

        //We have to use a HTML response due to ExtJS's use of a hidden iframe for file uploads
        //Failure to do this will result in the upload working BUT the user will also get prompted
        //for a file download containing the encoded response from this function (which we don't want).
        return generateHTMLResponseMAV(true, Arrays.asList(fileInfo), "");
    }

    @RequestMapping("/getColumnCount.do")
    public ModelAndView getColumnCount(HttpServletRequest request) {
        //Lookup our job - TODO - use temp job at the moment
        EAVLJob job = new EAVLJob(1);

        try {
            InputStream csvData = fss.readFile(job, "example-data.csv");
            return generateJSONResponseMAV(false, new Integer(csvService.estimateColumnCount(csvData)), "Error reading file");
        } catch (Exception e) {
            return generateJSONResponseMAV(false, null, "Error reading file");
        }
    }


    @RequestMapping("/streamRows.do")
    public ModelAndView streamRows(HttpServletRequest request,
            @RequestParam("limit") Integer limit,
            @RequestParam("page") Integer page,
            @RequestParam("start") Integer start) {

        //Lookup our job - TODO - use temp job at the moment
        EAVLJob job = new EAVLJob(1);

        try {
            InputStream csvData = fss.readFile(job, "example-data.csv");
            List<String[]> data = csvService.readLines(csvData, start, limit);

            ModelMap response = new ModelMap();
            response.put("totalCount", data.size());
            response.put("rows", data);
            return new ModelAndView(new JSONView(), response);
        } catch (Exception e) {
            return generateJSONResponseMAV(false, null, "Error reading file");
        }
    }
}
