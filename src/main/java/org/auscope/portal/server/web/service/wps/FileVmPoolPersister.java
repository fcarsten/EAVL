/**
 *
 */
package org.auscope.portal.server.web.service.wps;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.annotation.ThreadSafe;
import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * @author fri096
 *
 */
@ThreadSafe
public class FileVmPoolPersister implements VmPoolPersistor {
    protected final Log log = LogFactory.getLog(getClass());
    protected String baseDirectory = null;

	public FileVmPoolPersister() {
		this.baseDirectory = "";
	}

	public FileVmPoolPersister(PortalPropertyPlaceholderConfigurer propertyConfigurer) {
	    this.baseDirectory = propertyConfigurer.resolvePlaceholder("HOST.localStageInDir");
	    if (this.baseDirectory == null) {
	        this.baseDirectory = "";
	    }

		log.info("FileVmPoolPersister created");
	}
	/* (non-Javadoc)
	 * @see org.auscope.portal.server.web.service.wps.VmPoolPersistor#loadVmPool()
	 */
	@Override
	synchronized public Set<WpsVm> loadVmPool() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(baseDirectory, "wps.vms.json");
		if(file.exists()) {
			return mapper.readValue(file, new TypeReference<Set<WpsVm>>() {});
		} else {
			log.warn("VM Pool state definition file not found: "+file.getAbsolutePath());
			return new HashSet<>(0);
		}
	}

	@Override
	synchronized public void saveVmPool(Set<WpsVm> vms) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(new File(baseDirectory, "wps.vms.json"), vms);
	}

}
