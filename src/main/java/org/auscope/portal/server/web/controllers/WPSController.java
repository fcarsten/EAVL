package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;

import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.view.JSONView;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.web.service.CSVService;
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

    @Autowired
    public WPSController(FileStagingService fss, CSVService csvService, ConditionalProbabilityWpsClient wpsClient) {
        this.fss = fss;
        this.csvService = csvService;
        this.wpsClient = wpsClient;
    }

    @RequestMapping("/getPDFData.do")
    public ModelAndView getPDFData(HttpServletRequest request,
            @RequestParam("columnIndex") int columnIndex) {
        //Lookup our job - TODO - use temp job at the moment
        EAVLJob job = new EAVLJob(1);

        try {
            InputStream csvData = fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
            List<Double> columnData = csvService.getParameterValues(csvData, columnIndex, false);

            double[][] response = wpsClient.logDensity(columnData.toArray(new Double[columnData.size()]));

            //TODO - Send this off to WPS
            // Let's just fake it for now.
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
}
