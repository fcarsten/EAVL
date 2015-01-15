/**
 *
 */
package org.auscope.portal.server.web.service.wps;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author fri096
 *
 */
@Repository
@Transactional
public class DbVmPoolPersister {
    protected final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager em;

	public DbVmPoolPersister() {
	}

	/* (non-Javadoc)
	 * @see org.auscope.portal.server.web.service.wps.VmPoolPersistor#loadVmPool()
	 */
	synchronized public Set<WpsVm> loadVmPool() throws IOException {
			return new HashSet<>(0);
	}

	public void save(WpsVm vm) {
	    em.persist(vm);
	    em.flush();
	}

}
