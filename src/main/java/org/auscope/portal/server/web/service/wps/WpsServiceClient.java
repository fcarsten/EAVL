/**
 *
 */
package org.auscope.portal.server.web.service.wps;

import java.io.IOException;

import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.eavl.wpsclient.ACF;
import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.n52.wps.client.WPSClientException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author fri096
 *
 */
@Component
@Scope("prototype")
public class WpsServiceClient {
    protected final Log log = LogFactory.getLog(getClass());

    // private WpsVm vm;
    // private ConditionalProbabilityWpsClient getWpsClient();
    private String endpoint;

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param data
     * @return
     * @throws WPSClientException
     * @throws IOException
     * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#imputationNA(double[][])
     */
    @Cacheable(value = "wpsCache")
    public double[][] imputationNA(double[][] data) throws WPSClientException,
            IOException {
        return getWpsClient().imputationNA(data);
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return getWpsClient().hashCode();
    }

    // /**
    // * @param nCols
    // * @param dataStr
    // * @return
    // * @throws WPSClientException
    // * @throws IOException
    // * @see
    // org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#imputationNA(int,
    // java.lang.String)
    // */
    // public double[][] imputationNA(int nCols, String dataStr)
    // throws WPSClientException, IOException {
    // return getWpsClient().imputationNA(nCols, dataStr);
    // }

    /**
     * @param data
     * @return
     * @throws WPSClientException
     * @throws IOException
     * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#logDensity(double[])
     */
    @Cacheable(value = "wpsCache")
    public double[][] logDensity(double[] data) throws WPSClientException,
            IOException {
        return getWpsClient().logDensity(data);
    }

    /**
     * @param data
     * @param q
     * @return
     * @throws WPSClientException
     * @throws IOException
     * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#quantile(double[],
     *      double[])
     */
    @Cacheable(value = "wpsCache")
    public double[] quantile(double[] data, double[] q)
            throws WPSClientException, IOException {
        return getWpsClient().quantile(data, q);
    }

    /**
     * @param data
     * @return
     * @throws WPSClientException
     * @throws IOException
     * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#meanACF(java.lang.Object[][])
     */
    @Cacheable(value = "wpsCache")
    public ACF meanACF(Object[][] data) throws WPSClientException, IOException {
        return getWpsClient().meanACF(data);
    }

    /**
     * @param data
     * @param v
     * @return
     * @throws WPSClientException
     * @throws IOException
     * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#doubleLogDensity(double[][],
     *      double)
     */
    @Cacheable(value = "wpsCache")
    public double[][] doubleLogDensity(double[][] data, double v)
            throws WPSClientException, IOException {
        return getWpsClient().doubleLogDensity(data, v);
    }

    /**
     * @param data
     * @return
     * @throws WPSClientException
     * @throws IOException
     * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#cenLR(double[][])
     */
    @Cacheable(value = "wpsCache")
    public double[][] cenLR(double[][] data) throws WPSClientException,
            IOException {
        return getWpsClient().cenLR(data);
    }

    /**
     * @param gclr3
     * @param evalpts
     * @return
     * @throws WPSClientException
     * @throws IOException
     * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#hpiKdeJSON(double[][],
     *      double[][])
     */
    @Cacheable(value = "wpsCache")
    public String hpiKdeJSON(double[][] gclr3, double[][] evalpts)
            throws WPSClientException, IOException {
        return getWpsClient().hpiKdeJSON(gclr3, evalpts);
    }

    /**
     * @return
     * @throws WPSClientException
     * @see org.auscope.eavl.wpsclient.EavlWpsClient#requestGetCapabilities()
     */
    public CapabilitiesDocument requestGetCapabilities()
            throws WPSClientException {
        return getWpsClient().requestGetCapabilities();
    }

    /**
     * @param processID
     * @return
     * @throws IOException
     * @see org.auscope.eavl.wpsclient.EavlWpsClient#requestDescribeProcess(java.lang.String)
     */
    @Cacheable(value = "wpsCache")
    public ProcessDescriptionType requestDescribeProcess(String processID)
            throws IOException {
        return getWpsClient().requestDescribeProcess(processID);
    }

    /**
     * @param proxies
     * @param cutoffCol
     * @param cutoffValue
     * @return
     * @throws WPSClientException
     * @throws IOException
     * @see org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient#hpiKdeJSON(double[][],
     *      double[], double)
     */
    @Cacheable(value = "wpsCache")
    public String hpiKdeJSON(double[][] proxies, double[] cutoffCol,
            double cutoffValue) throws WPSClientException, IOException {
        return getWpsClient().hpiKdeJSON(proxies, cutoffCol, cutoffValue);
    }

    private ConditionalProbabilityWpsClient getWpsClient() {
        return new ConditionalProbabilityWpsClient(getEndpoint());
    }

    public void setEndpoting(String endpoint) {
        this.endpoint = endpoint;
    }

    public WpsServiceClient() {
        log.info("WpsServiceClient created");
    }

    // public WpsServiceClient(WpsVm vm) {
    // this.vm=vm;
    // this.getWpsClient() = new
    // ConditionalProbabilityWpsClient(vm.getServiceUrl());
    // }

}
