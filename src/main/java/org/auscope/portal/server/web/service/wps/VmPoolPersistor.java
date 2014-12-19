/**
 *
 */
package org.auscope.portal.server.web.service.wps;

import java.io.IOException;
import java.util.Set;

import org.apache.http.annotation.ThreadSafe;

/**
 * @author fri096
 *
 */
@ThreadSafe
public interface VmPoolPersistor {

    Set<WpsVm> loadVmPool() throws IOException;

    void saveVmPool(Set<WpsVm> vms) throws IOException;
}
