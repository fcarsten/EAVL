package org.auscope.portal.server.web.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.web.service.wps.VmPool;
import org.auscope.portal.server.web.service.wps.WpsServiceClient;
import org.auscope.portal.server.web.service.wps.WpsVm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @author Carsten Friedrich
 *
 */
@Service
public class WpsService {

    protected final Log log = LogFactory.getLog(getClass());

    private VmPool vmPool;

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    public WpsService(VmPool vmPool) {
        this.vmPool = vmPool;
    }

    public WpsServiceClient getWpsClient() throws PortalServiceException  {
    	WpsVm vm= vmPool.getFreeVm();
    	WpsServiceClient res = (WpsServiceClient) appContext.getBean("wpsClient");
//    	WpsServiceClient res = new WpsServiceClient();
    	res.setEndpoting(vm.getServiceUrl());
    	return res;
    }

}
