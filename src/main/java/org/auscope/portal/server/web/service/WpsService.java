package org.auscope.portal.server.web.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.web.service.wps.VmPool;
import org.auscope.portal.server.web.service.wps.WpsVm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Utility for executing JobTask's using an internal executor. Optional
 * listeners can be notified soon after a task finishes execution.
 *
 * This service does not persist any data by default but the
 *
 * @author Josh Vote
 *
 */
@Service
public class WpsService {

    protected final Log log = LogFactory.getLog(getClass());

    private VmPool vmPool;

    public WpsService() {
        this(null);
    }

    @Autowired
    public WpsService(VmPool vmPool) {
        this.vmPool = vmPool;
    }

    public ConditionalProbabilityWpsClient getWpsClient() throws PortalServiceException  {
    	WpsVm vm= vmPool.getFreeVm();
    	return new ConditionalProbabilityWpsClient(vm.getServiceUrl());
    }

}
