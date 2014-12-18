package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;

import org.apache.commons.io.IOUtils;
import org.auscope.eavl.wpsclient.ACF;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.server.security.oauth2.PortalUser;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.view.JSONView;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.web.service.CSVService;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.auscope.portal.server.web.service.WpsService;
import org.auscope.portal.server.web.service.wps.WpsServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("wps")
public class WPSController extends BasePortalController {

    public static final int MAX_RETRIES = 3;
	private FileStagingService fss;
    private CSVService csvService;
    private WpsService wpsService;
    private EAVLJobService jobService;

    @Autowired
    public WPSController(FileStagingService fss, CSVService csvService, WpsService wpsService, EAVLJobService jobService) {
        this.fss = fss;
        this.csvService = csvService;
        this.wpsService = wpsService;
        this.jobService = jobService;
    }

    @RequestMapping("/getPDFData.do")
    public ModelAndView getPDFData(HttpServletRequest request, @AuthenticationPrincipal PortalUser user,
            @RequestParam("columnIndex") int columnIndex) {

    	int retries= MAX_RETRIES;
    	List<Double> columnData;
		try {
			EAVLJob job = jobService.getJobForSession(request, user);
			InputStream csvData = fss.readFile(job,
					EAVLJobConstants.FILE_DATA_CSV);
			columnData = csvService.getParameterValues(
					csvData, columnIndex, false);
		} catch (PortalServiceException e) {
			log.warn("Unable to get pdf values: ", e);
			return generateJSONResponseMAV(false, null,
					"Error fetching pdf data");
		}

		while (retries-->0) {
			WpsServiceClient wpsClient=null;
			try {
				wpsClient = wpsService.getWpsClient();
				double[][] response = wpsClient.logDensity(columnData
						.toArray(new Double[columnData.size()]));

				JSONArray xyPairs = new JSONArray();
				if (response.length > 0) {
					for (int i = 0; i < response[0].length; i++) {
						JSONArray xy = new JSONArray();
						xy.add(response[0][i]);
						xy.add(response[1][i]);
						xyPairs.add(xy);
					}
				}
				return new ModelAndView(new JSONView(xyPairs), null);
			} catch (IOException ex) {
				log.warn("Unable to get pdf values: ", ex);
				log.warn("Assuming bad VM");
				wpsService.checkVM(wpsClient);
			} catch (Exception ex) {
				log.warn("Unable to get pdf values: ", ex);
				return generateJSONResponseMAV(false, null,
						"Error fetching pdf data");
			}
		}
		return generateJSONResponseMAV(false, null,
				"Error fetching pdf data");
    }

    @RequestMapping("/getDoublePDFData.do")
    public ModelAndView getDoublePDFData(HttpServletRequest request, @AuthenticationPrincipal PortalUser user,
            @RequestParam("columnIndex") int columnIndex) {

        try {
            EAVLJob job = jobService.getJobForSession(request, user);
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

        	double[][] response=null;
            int retries = MAX_RETRIES;
            while (response == null && retries-- > 0) {
                WpsServiceClient wpsClient = null;
                try {
                    wpsClient = wpsService.getWpsClient();
                    response = wpsClient.doubleLogDensity(data,
                            predictionCutoff);
                } catch (IOException e) {
                    log.warn("Unable to get double pdf values: ", e);
                    log.warn("Assuming bad VM");
                    wpsService.checkVM(wpsClient);
                }
            }
    		if(response==null) {
                return generateJSONResponseMAV(false, null, "Error fetching double pdf data");
    		}
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

    private Double parseDouble(double d) {
        Double parsed = new Double(d);

        if (parsed.isInfinite() || parsed.isNaN()) {
            return null;
        }

        return parsed;
    }

    @RequestMapping("/getMeanACFData.do")
    public ModelAndView getMeanACFData(HttpServletRequest request, @AuthenticationPrincipal PortalUser user,
            @RequestParam("columnIndex") int columnIndex) {
        try {
            EAVLJob job = jobService.getJobForSession(request, user);
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

        	int retries= MAX_RETRIES;
        	ACF response=null;
			while (response == null && retries--> 0) {
				WpsServiceClient wpsClient = null;
				try {
					wpsClient = wpsService.getWpsClient();
					 response = wpsClient.meanACF(data);

				} catch (IOException ex) {
					log.warn("Unable to get pdf values: ", ex);
					log.warn("Assuming bad VM");
					wpsService.checkVM(wpsClient);
				}
			}
            ModelMap responseModel = new ModelMap();

            if(response==null){
                return generateJSONResponseMAV(false, null, "Error fetching mean ACF data");
            }
            responseModel.put("ci", parseDouble(response.getCi()));
            List<Double> acf = new ArrayList<Double>();
            for (double d : response.getAcf()) {
                acf.add(parseDouble(d));
            }
            responseModel.put("acf", acf);

            return generateJSONResponseMAV(true, responseModel, "");
        } catch (Exception ex) {
            log.warn("Unable to get mean ACF values: ", ex);
            return generateJSONResponseMAV(false, null, "Error fetching mean ACF data");
        }
    }
}
