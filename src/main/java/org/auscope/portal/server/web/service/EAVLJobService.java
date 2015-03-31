package org.auscope.portal.server.web.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.security.oauth2.EavlUser;
import org.auscope.portal.server.web.service.jobtask.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class for reading/writing instances of EAVLJob objects from a persistent store
 * @author Josh Vote
 *
 */
@Service
public class EAVLJobService {

    private static final String SESSION_ATTR_ID = "eavl-session-job-id";

    protected final Log log = LogFactory.getLog(getClass());

//    private List<EAVLJob> debugJobSingleton; // TODO: This is a horrible in memory store for debug purposes
//    private volatile int debugIdCounter = 1; // TODO: This is a horrible in memory store for debug purposes

    @Autowired
    private JobRepository persistor;

    @Autowired
    private JobTaskService jobTaskService;

    @Autowired
    public EAVLJobService(FileStagingService fss) {
    }

    private EAVLJob getJobById(Integer id) {
        if (id == null) {
            return null;
        }

        return persistor.findOne(id);
    }

    /**
     * Creates an EAVLJob for a particular user session.
     *
     * @param request Used to identify session
     * @return
     */
    public EAVLJob createJobForSession(HttpServletRequest request, EavlUser user) throws PortalServiceException {
        EAVLJob newJob = new EAVLJob();
        newJob.setUser(user);
        persistor.saveAndFlush(newJob);

        request.getSession().setAttribute(SESSION_ATTR_ID, newJob.getId());
        return newJob;
    }

    /**
     * Gets an EAVLJob for a particular user session. If the job DNE this will return null
     *
     * @param request Used to identify session
     * @param user Owner of the session
     * @return
     */
    public EAVLJob getJobForSession(HttpServletRequest request, EavlUser user) throws PortalServiceException {
        log.warn("TODO - getJobForSession");

        Integer id = (Integer) request.getSession().getAttribute(SESSION_ATTR_ID);

        return getJobById(id);
    }

    /**
     * Sets the specified job as the current job for this user's session. The current session job (if any) will be
     * replaced but not overwritten.
     *
     * @param job The job to set
     * @param request Used to identify session
     * @param user Owner of the session
     * @return
     * @throws PortalServiceException
     */
    public void setSessionJob(EAVLJob job, HttpServletRequest request, EavlUser user) throws PortalServiceException {
        request.getSession().setAttribute(SESSION_ATTR_ID, job == null ? null : job.getId());
    }

    /**
     * Gets an EAVLJob with a particular ID. If the job DNE this will return null. If the requesting user does not have
     * permissions to access the job, this will also return null.
     * @see getJobById
     * @param id
     * @return
     */
    public EAVLJob getUserJobById(HttpServletRequest request, EavlUser user, Integer id) throws PortalServiceException {
        return getJobById(id);
    }

    /**
     * Deletes the specified job from the persistent store.
     * @param job Job to delete
     * @throws PortalServiceException
     */
    public void delete(EAVLJob job) throws PortalServiceException {
        jobTaskService.removeTasksForJob(job);
        persistor.delete(job);
    }

    /**
     * Persists the current state of this job such that subsequent calls to getJobById/getJobForSession will
     * return a clone of job
     * @param job The job to persist.
     * @throws PortalServiceException
     */
    public void save(EAVLJob job) throws PortalServiceException {
        persistor.saveAndFlush(job);
    }

    /**
     * Returns every Job belonging to a particular user
     * @param request The users request
     * @param user The authenticated user object
     * @return
     */
    public List<EAVLJob> getJobsForUser(HttpServletRequest request, EavlUser user) throws PortalServiceException {
        List<EAVLJob> res = persistor.findByUser(user);
        return res;
    }
}
