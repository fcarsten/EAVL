/**
 *
 */
package org.auscope.portal.server.web.service.wps;

import java.io.IOException;

import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.auscope.eavl.wpsclient.ACF;
import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.n52.wps.client.WPSClientException;

/**
 * @author fri096
 *
 */
public class WpsServiceClient {

	private WpsVm vm;
	private ConditionalProbabilityWpsClient conProbClient;

	/**
	 * @param data
	 * @return
	 * @throws WPSClientException
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#imputationNA(double[][])
	 */
	public double[][] imputationNA(double[][] data) throws WPSClientException,
			IOException {
		return conProbClient.imputationNA(data);
	}

	/**
	 * @param data
	 * @return
	 * @throws WPSClientException
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#imputationNA(java.lang.Double[][])
	 */
	public double[][] imputationNA(Double[][] data) throws WPSClientException,
			IOException {
		return conProbClient.imputationNA(data);
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return conProbClient.hashCode();
	}

//	/**
//	 * @param nCols
//	 * @param dataStr
//	 * @return
//	 * @throws WPSClientException
//	 * @throws IOException
//	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#imputationNA(int, java.lang.String)
//	 */
//	public double[][] imputationNA(int nCols, String dataStr)
//			throws WPSClientException, IOException {
//		return conProbClient.imputationNA(nCols, dataStr);
//	}

	/**
	 * @param data
	 * @return
	 * @throws WPSClientException
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#logDensity(double[])
	 */
	public double[][] logDensity(double[] data) throws WPSClientException,
			IOException {
		return conProbClient.logDensity(data);
	}

	/**
	 * @param data
	 * @param q
	 * @return
	 * @throws WPSClientException
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#quantile(double[], double[])
	 */
	public double[] quantile(double[] data, double[] q)
			throws WPSClientException, IOException {
		return conProbClient.quantile(data, q);
	}

	/**
	 * @param data
	 * @return
	 * @throws WPSClientException
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#logDensity(java.lang.Double[])
	 */
	public double[][] logDensity(Double[] data) throws WPSClientException,
			IOException {
		return conProbClient.logDensity(data);
	}

	/**
	 * @param data
	 * @return
	 * @throws WPSClientException
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#meanACF(java.lang.Object[][])
	 */
	public ACF meanACF(Object[][] data) throws WPSClientException, IOException {
		return conProbClient.meanACF(data);
	}

	/**
	 * @param data
	 * @param v
	 * @return
	 * @throws WPSClientException
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#doubleLogDensity(double[][], double)
	 */
	public double[][] doubleLogDensity(double[][] data, double v)
			throws WPSClientException, IOException {
		return conProbClient.doubleLogDensity(data, v);
	}

	/**
	 * @param data
	 * @param v
	 * @return
	 * @throws WPSClientException
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#doubleLogDensity(java.lang.Double[][], double)
	 */
	public double[][] doubleLogDensity(Double[][] data, double v)
			throws WPSClientException, IOException {
		return conProbClient.doubleLogDensity(data, v);
	}

	/**
	 * @param data
	 * @return
	 * @throws WPSClientException
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#cenLR(double[][])
	 */
	public double[][] cenLR(double[][] data) throws WPSClientException,
			IOException {
		return conProbClient.cenLR(data);
	}

	/**
	 * @param gclr3
	 * @param evalpts
	 * @return
	 * @throws WPSClientException
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#hpiKdeJSON(double[][], double[][])
	 */
	public String hpiKdeJSON(double[][] gclr3, double[][] evalpts)
			throws WPSClientException, IOException {
		return conProbClient.hpiKdeJSON(gclr3, evalpts);
	}

	/**
	 * @return
	 * @throws WPSClientException
	 * @see org.auscope.eavl.wpsclient.EavlWpsClient#requestGetCapabilities()
	 */
	public CapabilitiesDocument requestGetCapabilities()
			throws WPSClientException {
		return conProbClient.requestGetCapabilities();
	}

	/**
	 * @param processID
	 * @return
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.EavlWpsClient#requestDescribeProcess(java.lang.String)
	 */
	public ProcessDescriptionType requestDescribeProcess(String processID)
			throws IOException {
		return conProbClient.requestDescribeProcess(processID);
	}

	/**
	 * @param gclr3
	 * @param evalpts
	 * @return
	 * @throws WPSClientException
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#hpiKdeJSON(java.lang.Double[][], java.lang.Double[][])
	 */
	public String hpiKdeJSON(Double[][] gclr3, Double[][] evalpts)
			throws WPSClientException, IOException {
		return conProbClient.hpiKdeJSON(gclr3, evalpts);
	}

	/**
	 * @param proxies
	 * @param cutoffCol
	 * @param cutoffValue
	 * @return
	 * @throws WPSClientException
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#hpiKdeJSON(java.lang.Double[][], java.lang.Double[], double)
	 */
	public String hpiKdeJSON(Double[][] proxies, Double[] cutoffCol,
			double cutoffValue) throws WPSClientException, IOException {
		return conProbClient.hpiKdeJSON(proxies, cutoffCol, cutoffValue);
	}

	/**
	 * @param proxies
	 * @param cutoffCol
	 * @param cutoffValue
	 * @return
	 * @throws WPSClientException
	 * @throws IOException
	 * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#hpiKdeJSON(double[][], double[], double)
	 */
	public String hpiKdeJSON(double[][] proxies, double[] cutoffCol,
			double cutoffValue) throws WPSClientException, IOException {
		return conProbClient.hpiKdeJSON(proxies, cutoffCol, cutoffValue);
	}

	public WpsServiceClient(WpsVm vm) {
		this.vm=vm;
		this.conProbClient = new ConditionalProbabilityWpsClient(vm.getServiceUrl());
	}

}
