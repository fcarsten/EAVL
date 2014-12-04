package org.auscope.portal.server.web.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.security.oauth2.PortalUser;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.eavl.EAVLJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

/**
 * Service class for reading/writing instances of EAVLJob objects from a persistent store
 * @author Josh Vote
 *
 */
@Service
public class EAVLJobService {

    private static final String SESSION_ATTR_ID = "eavl-session-job-id";

    protected final Log log = LogFactory.getLog(getClass());

    private List<EAVLJob> debugJobSingleton; // TODO: This is a horrible in memory store for debug purposes
    private volatile int debugIdCounter = 1; // TODO: This is a horrible in memory store for debug purposes

    @Autowired
    public EAVLJobService(FileStagingService fss) {

        log.error("This is debug code - ITS NOT A PROPER IMPLEMENTATION");

        debugJobSingleton = new CopyOnWriteArrayList<EAVLJob>();
        EAVLJob job = new EAVLJob(debugIdCounter);
        try {
            while (fss.stageInDirectoryExists(job)) {
                job.setName("file-" + job.getId() + ".csv");
                job.setProxyParameters(Sets.newHashSet("Sb_ppm", "Cr_ppm", "Rb_ppm"));
                debugJobSingleton.add(job);
                job = new EAVLJob(++debugIdCounter);
            }
        } catch (PortalServiceException e) {

        }
    }

    private EAVLJob debugGetById(Integer id) {
        if (id == null) {
            return null;
        }
        for (EAVLJob j : debugJobSingleton) {
            if (j.getId() == id) {
                return j;
            }
        }

        return null;
    }

    /**
     * Creates an EAVLJob for a particular user session.
     *
     * @param request Used to identify session
     * @return
     */
    public EAVLJob createJobForSession(HttpServletRequest request) throws PortalServiceException {
        log.warn("TODO - createJobForSession");

        EAVLJob newJob = new EAVLJob(debugIdCounter++);
        request.getSession().setAttribute(SESSION_ATTR_ID, newJob.getId());

        debugJobSingleton.add(newJob);

        return newJob;
    }

    /**
     * Gets an EAVLJob for a particular user session. If the job DNE this will return null
     *
     * @param request Used to identify session
     * @param user Owner of the session
     * @return
     */
    public EAVLJob getJobForSession(HttpServletRequest request, PortalUser user) throws PortalServiceException {
        log.warn("TODO - getJobForSession");

        Integer id = (Integer) request.getSession().getAttribute(SESSION_ATTR_ID);


        return debugGetById(id);
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
    public void setSessionJob(EAVLJob job, HttpServletRequest request, PortalUser user) throws PortalServiceException {
        request.getSession().setAttribute(SESSION_ATTR_ID, job == null ? null : job.getId());
    }

    /**
     * Gets an EAVLJob with a particular ID (no checks made against permissions). If the job DNE this will return null
     * @see getUserJobById
     * @param id
     * @return
     */
    public EAVLJob getJobById(Integer id) throws PortalServiceException {
        return debugGetById(id);
    }

    /**
     * Gets an EAVLJob with a particular ID. If the job DNE this will return null. If the requesting user does not have
     * permissions to access the job, this will also return null.
     * @see getJobById
     * @param id
     * @return
     */
    public EAVLJob getUserJobById(HttpServletRequest request, PortalUser user, Integer id) throws PortalServiceException {
        return debugGetById(id);
    }

    /**
     * Deletes the specified job from the persistent store.
     * @param job Job to delete
     * @throws PortalServiceException
     */
    public void delete(EAVLJob job) throws PortalServiceException {
        debugJobSingleton.remove(job);
    }

    /**
     * Persists the current state of this job such that subsequent calls to getJobById/getJobForSession will
     * return a clone of job
     * @param job The job to persist.
     * @throws PortalServiceException
     */
    public void save(EAVLJob job) throws PortalServiceException {
        //TODO - save job
        log.warn("TODO - save job");
    }

    /**
     * Returns every Job belonging to a particular user
     * @param request The users request
     * @param user The authenticated user object
     * @return
     */
    public List<EAVLJob> getJobsForUser(HttpServletRequest request, PortalUser user) throws PortalServiceException {
        return debugJobSingleton;
    }
}
