/**
 *
 */
package org.auscope.portal.server.web.service.wps;

import java.io.IOException;
import java.net.ConnectException;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.BasicHttpParams;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @author fri096
 *
 */
@Entity
@Table(name="wps_vm")
public class WpsVm {

	@JsonIgnore
	@Transient
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Maximum time in milliseconds that we wait for VM start-up until we give up.
	 *
	 */
    @JsonIgnore
	@Transient
	final public int VM_STARTUP_TIMEOUT = 1000*60*10;

    @JsonIgnore
    @Transient
    final public int VM_UNRESPONSIVE_TIMEOUT = 1000*60*3;

	/**
	 * @author fri096
	 *
	 */
	public enum VmStatus {
		READY, UNKNOWN, STARTING, FAILED, DECOMMISSIONED
	}

	@Id
	@Basic
	private String id;

    @Basic
	private String ipAddress;

	@JsonIgnore
	@Transient
	private VmStatus status = VmStatus.UNKNOWN;

    @Basic
	private long orderTime;

	/**
	 * @return the orderTime
	 */
	public long getOrderTime() {
		return orderTime;
	}

	@JsonIgnore
	public void setStatus(VmStatus status) {
		this.status = status;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public WpsVm() {

	}

	public WpsVm(String id, String ipAddress) {
		this.id = id;
		this.ipAddress = ipAddress;
	}

	@JsonIgnore
	public String getServiceUrl() {
		return "http://" + ipAddress + ":8080/wps/WebProcessingService";
	}

	@JsonIgnore
	public VmStatus getStatus() {
		return this.status;
	}

	@JsonIgnore
	public VmStatus getOrWaitForStableState() {
		if (isStable(status)) {
			return status;
		}

		while (true) {
			updateStatus();
			if (isStable(status)) {
				return status;
			}
			try {
				Thread.sleep(1000); // wait a second, not stable yet. Probably booting ...
			} catch (InterruptedException e) {
				log.warn("Ignoring InterruptedException. Porbably should do something more appropriate ....");
			}
		}
	}

	public void updateStatus() {
		if (status == VmStatus.DECOMMISSIONED)
			return;

		 CloseableHttpClient client = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom()
		        .setConnectionRequestTimeout(30*1000)
		        .setConnectTimeout(30*1000)
		        .setSocketTimeout(30*1000)
		        .build();
		log.info("Updating status of VM: "+id + " ("+ipAddress+")");
		HttpGet method = new HttpGet("http://" + ipAddress + ":8080/wps");
		method.setConfig(requestConfig);
		try {
			CloseableHttpResponse response = client.execute(method);
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    status = VmStatus.READY;
                    log.info(" VM: " + id + " (" + ipAddress + ") is ready.");
                } else if (status == VmStatus.STARTING
                        && System.currentTimeMillis() - getOrderTime() > VM_STARTUP_TIMEOUT) {
                    status = VmStatus.FAILED;
                    log.info(" VM: " + id + " (" + ipAddress
                            + ") has failed to start up within "
                            + (VM_STARTUP_TIMEOUT / 1000)
                            + " seconds. Giving up.");
                } else if (status == VmStatus.STARTING) {
                    log.info(" VM: " + id + " (" + ipAddress
                            + ") no ready yet.");
                } else { // TODO: Handle other status codes
                    status = VmStatus.FAILED;
                    log.info(" VM: " + id + " (" + ipAddress + ") has failed.");
                }
            }
			finally {
			    response.close();
			}
		} catch (IOException e) {
			if (status == VmStatus.UNKNOWN && System.currentTimeMillis() - getOrderTime() > VM_UNRESPONSIVE_TIMEOUT) {
				status = VmStatus.FAILED;
				log.info("Can't connect to WPS VM: "+id + " ("+ipAddress+"). I assume its dead and will remove from VM Pool. Error: "+e.getMessage());
			} else {
				log.info("Can't connect to WPS VM: "+id + " ("+ipAddress+"). Will try again shortly. Error: "+e.getMessage());
			}
		}
	}

	private boolean isStable(VmStatus s) {
		return s == VmStatus.READY || s == VmStatus.FAILED
				|| s == VmStatus.DECOMMISSIONED;
	}

	public void setOrderTime(long orderTime) {
		this.orderTime=orderTime;
	}
}
