package org.auscope.portal.server.web.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.web.service.wps.VmPool;
import org.auscope.portal.server.web.service.wps.WpsServiceClient;
import org.auscope.portal.server.web.service.wps.WpsVm;
import org.auscope.portal.server.web.service.wps.WpsVm.VmStatus;
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

    public WpsServiceClient getWpsClient() throws PortalServiceException {
        WpsVm vm = vmPool.getFreeVm();
        WpsServiceClient res = (WpsServiceClient) appContext
                .getBean("wpsClient");
        // WpsServiceClient res = new WpsServiceClient();
        res.setEndpoting(vm.getServiceUrl());
        return res;
    }

    public void checkVM(WpsServiceClient wpsClient) {
        vmPool.verifyVm(wpsClient);
    }

    /**
     * Passthrough method to VmPool.calculatePoolStatus. This is a blocking method,
     * use sparingly.
     *
     * @see org.auscope.portal.server.web.service.wps.VmPool
     * @return
     */
    public List<VmStatus> calculatePoolStatus() {
        return vmPool.calculatePoolStatus();
    }
}
