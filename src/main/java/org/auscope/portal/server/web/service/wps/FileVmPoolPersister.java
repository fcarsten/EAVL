/**
 *
 */
package org.auscope.portal.server.web.service.wps;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author fri096
 *
 */
public class FileVmPoolPersister implements VmPoolPersistor {

	/* (non-Javadoc)
	 * @see org.auscope.portal.server.web.service.wps.VmPoolPersistor#loadVmPool()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<WpsVm> loadVmPool() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(new File("wps.vms.json"), Set.class);
	}

	@Override
	public void saveVmPool(Set<WpsVm> vms) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(new File("wps.vms.json"), vms);
	}

}
