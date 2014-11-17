package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;

import org.apache.commons.io.IOUtils;
import org.auscope.eavl.wpsclient.ACF;
import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.view.JSONView;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("wps")
public class WPSController extends BasePortalController {

    private FileStagingService fss;
    private CSVService csvService;
    private ConditionalProbabilityWpsClient wpsClient;
    private EAVLJobService jobService;

    @Autowired
    public WPSController(FileStagingService fss, CSVService csvService, ConditionalProbabilityWpsClient wpsClient, EAVLJobService jobService) {
        this.fss = fss;
        this.csvService = csvService;
        this.wpsClient = wpsClient;
        this.jobService = jobService;
    }

    @RequestMapping("/getPDFData.do")
    public ModelAndView getPDFData(HttpServletRequest request,
            @RequestParam("columnIndex") int columnIndex) {

        try {
            EAVLJob job = jobService.getJobForSession(request);
            InputStream csvData = fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
            List<Double> columnData = csvService.getParameterValues(csvData, columnIndex, false);

            double[][] response = wpsClient.logDensity(columnData.toArray(new Double[columnData.size()]));

            JSONArray xyPairs = new JSONArray();
            for (int i = 0; i < response[0].length; i++) {
                JSONArray xy = new JSONArray();
                xy.add(response[0][i]);
                xy.add(response[1][i]);
                xyPairs.add(xy);
            }

            return new ModelAndView(new JSONView(xyPairs), null);
        } catch (Exception ex) {
            log.warn("Unable to get pdf values: ", ex);
            return generateJSONResponseMAV(false, null, "Error fetching pdf data");
        }
    }

    @RequestMapping("/getDoublePDFData.do")
    public ModelAndView getDoublePDFData(HttpServletRequest request,
            @RequestParam("columnIndex") int columnIndex) {

        try {
            EAVLJob job = jobService.getJobForSession(request);
            if (job == null) {
                return generateJSONResponseMAV(false, null, "No job");
            }

            String predictionColumnName = job.getPredictionParameter();
            if (predictionColumnName == null) {
                return generateJSONResponseMAV(false, null, "No prediction set");
            }

            Double predictionCutoff = job.getPredictionCutoff();
            if (predictionCutoff == null) {
                return generateJSONResponseMAV(false, null, "No prediction cutoff set");
            }

            InputStream csvData = fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            Integer predictionColumnIndex = csvService.columnNameToIndex(csvData, predictionColumnName);
            if (predictionColumnIndex == null) {
                return generateJSONResponseMAV(false, null, "Prediction column DNE");
            }

            IOUtils.closeQuietly(csvData);
            csvData = fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            Double[][] data = csvService.getRawData(csvData, Arrays.asList(predictionColumnIndex, columnIndex));

            double[][] response = wpsClient.doubleLogDensity(data, predictionCutoff);
            JSONArray xyPairs = new JSONArray();
            for (int i = 0; i < response.length; i++) {
                JSONArray xyxy = new JSONArray();
                xyxy.add(response[i][0]);
                xyxy.add(response[i][1]);
                xyxy.add(response[i][2]);
                xyxy.add(response[i][3]);
                xyPairs.add(xyxy);
            }

            return new ModelAndView(new JSONView(xyPairs), null);
        } catch (Exception ex) {
            log.warn("Unable to get double pdf values: ", ex);
            return generateJSONResponseMAV(false, null, "Error fetching double pdf data");
        }
    }

    @RequestMapping("/getMeanACFData.do")
    public ModelAndView getMeanACFData(HttpServletRequest request,
            @RequestParam("columnIndex") int columnIndex) {
        try {
            EAVLJob job = jobService.getJobForSession(request);
            if (job == null) {
                return generateJSONResponseMAV(false, null, "No job");
            }

            String holeIdParam = job.getHoleIdParameter();
            if (holeIdParam == null) {
                return generateJSONResponseMAV(false, null, "Hole ID column DNE");
            }
            InputStream csvData = fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            int holeIdIndex = csvService.columnNameToIndex(csvData, holeIdParam);
            IOUtils.closeQuietly(csvData);

            csvData = fss.readFile(job, EAVLJobConstants.FILE_IMPUTED_CSV);
            String[][] data = csvService.getRawStringData(csvData, Arrays.asList(holeIdIndex, columnIndex), true);

            ACF response = wpsClient.meanACF(data);
            return generateJSONResponseMAV(true, response, "");
        } catch (Exception ex) {
            log.warn("Unable to get mean ACF values: ", ex);
            return generateJSONResponseMAV(false, null, "Error fetching double pdf data");
        }
    }
}
