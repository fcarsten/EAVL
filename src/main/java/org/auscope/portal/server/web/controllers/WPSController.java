package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;

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

    @Autowired
    public WPSController(FileStagingService fss, CSVService csvService) {
        this.fss = fss;
        this.csvService = csvService;
    }

    @RequestMapping("/getPDFData.do")
    public ModelAndView getPDFData(HttpServletRequest request,
            @RequestParam("columnIndex") int columnIndex) {
        //Lookup our job - TODO - use temp job at the moment
        EAVLJob job = new EAVLJob(1);

        try {
            InputStream csvData = fss.readFile(job, EAVLJobConstants.FILE_DATA_CSV);
            List<Double> columnData = csvService.getParameterValues(csvData, columnIndex);

            //TODO - Send this off to WPS
            // Let's just fake it for now.
            JSONArray xyPairs = new JSONArray();
            double[] scales =  new double[] {Math.random() * 0.1, Math.random() * 5, Math.random() * 4};
            for (double x = 0; x < 1000.0; x+=1.1) {
                double y = scales[0] * Math.pow(x, 3) - scales[1] * Math.pow(x, 2) + scales[2] * x + 1000; //fancy looking graph

                xyPairs.add(new Double[] {x, y});
            }

            return new ModelAndView(new JSONView(xyPairs), null);
        } catch (Exception ex) {
            log.warn("Unable to get pdf values: ", ex);
            return generateJSONResponseMAV(false, null, "Error fetching pdf data");
        }
    }
}
