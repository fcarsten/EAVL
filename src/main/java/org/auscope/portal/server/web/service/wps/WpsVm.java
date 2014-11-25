/**
 *
 */
package org.auscope.portal.server.web.service.wps;

import org.codehaus.jackson.annotate.JsonIgnore;


/**
 * @author fri096
 *
 */
public class WpsVm {

	private String id;
	private String ipAddress;

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

	public WpsVm(String id, String ipAddress) {
		this.id=id;
		this.ipAddress=ipAddress;
	}

	@JsonIgnore
	public String getServiceUrl() {
		return "http://"+ipAddress+":8080/wps/WebProcessingService";
	}
 }
