package org.auscope.portal.server.web.service;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.eavl.EAVLJob;
import org.springframework.stereotype.Service;

/**
 * Service class for reading/writing instances of EAVLJob objects from a persistent store
 * @author Josh Vote
 *
 */
@Service
public class EAVLJobService {

    protected final Log log = LogFactory.getLog(getClass());

    public EAVLJobService() {

    }

    /**
     * Creates an EAVLJob for a particular user session.
     *
     * @param request Used to identify session
     * @return
     */
    public EAVLJob createJobForSession(HttpServletRequest request) throws PortalServiceException {
        log.warn("TODO - createJobForSession");
        return new EAVLJob(1);
    }

    /**
     * Gets an EAVLJob for a particular user session. If the job DNE this will return null
     *
     * @param request Used to identify session
     * @return
     */
    public EAVLJob getJobForSession(HttpServletRequest request) throws PortalServiceException {
        log.warn("TODO - getJobForSession");
        return new EAVLJob(1);
    }

    /**
     * Gets an EAVLJob with a particular ID. If the job DNE this will return null
     * @param id
     * @return
     */
    public EAVLJob getJobById(Integer id) throws PortalServiceException {
        throw new NotImplementedException();
    }

    /**
     * Deletes the specified job from the persistent store.
     * @param job Job to delete
     * @throws PortalServiceException
     */
    public void delete(EAVLJob job) throws PortalServiceException{
        throw new NotImplementedException();
    }

    /**
     * Persists the current state of this job such that subsequent calls to getJobById/getJobForSession will
     * return a clone of job
     * @param job The job to persist.
     * @throws PortalServiceException
     */
    public void save(EAVLJob job) throws PortalServiceException {
        throw new NotImplementedException();
    }
}
