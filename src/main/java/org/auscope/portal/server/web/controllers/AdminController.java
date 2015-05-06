package org.auscope.portal.server.web.controllers;

import java.util.List;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.server.web.service.WpsService;
import org.auscope.portal.server.web.service.wps.WpsVm.VmStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller methods for various admin tasks
 * @author Josh Vote
 *
 */
@RequestMapping("admin")
@Controller
public class AdminController extends BasePortalController {
    private static final String VM_STATUS_LIST_KEY = "AdminController.vmPoolStatus.do";

    @Autowired
    private ApplicationContext appContext;
    private WpsService wpsService;

    @Autowired
    public AdminController(WpsService wpsService) {
        super();
        this.wpsService = wpsService;
    }


    public List<VmStatus> getCachedVmStatus() {
        return wpsService.calculatePoolStatus();
    }

    /**
     * Checks each and every VM in the pool for its status. The return value will be success: true IFF
     * all VM's are in the READY/STARTING state.
     *
     * This has a 1 hour cache for successful responses. Failure responses will NOT be cached.
     *
     * @return
     */
    @RequestMapping("/vmPoolStatus.do")
    public synchronized ModelAndView vmPoolStatus() {
        Cache c = ((EhCacheCacheManager) appContext.getBean("cacheManager")).getCache("adminCache");
        ValueWrapper vw = c.get(VM_STATUS_LIST_KEY);
        List<VmStatus> statusList = vw == null ? null : (List<VmStatus>) vw.get();
        if (statusList == null) {
            statusList = wpsService.calculatePoolStatus();
        }

        boolean allOk = true;
        for (VmStatus status : statusList) {
            switch(status) {
            case READY:
            case STARTING:
                break;
            default:
                allOk = false;
                break;
            }

            if (!allOk) {
                break;
            }
        }

        //We don't want to cache failing values as we'd like to detect the moment
        //the services recover
        if (allOk) {
            c.putIfAbsent(VM_STATUS_LIST_KEY, statusList);
        } else {
            c.evict(VM_STATUS_LIST_KEY);
        }

        return generateJSONResponseMAV(allOk);
    }
}
