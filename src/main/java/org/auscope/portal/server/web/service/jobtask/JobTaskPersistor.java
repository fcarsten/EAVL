package org.auscope.portal.server.web.service.jobtask;

import java.util.Collection;

import org.apache.http.annotation.ThreadSafe;

/**
 * An interface for "persisting" running job tasks in the event of a shutdown. The actual
 * work will NOT be persisted, instead the starting conditions of the job will be remembered
 * and will be used to regenerate the task at a future point in time.
 * @author Josh Vote
 *
 */
@ThreadSafe
public interface JobTaskPersistor {
    /**
     * Persist t so that future calls to getPersistedJobs will return a copy of t.
     *
     * Must be threadsafe
     *
     * @param t The task to persist
     * @param id The ID of the task to persist
     * @return
     */
    public void persist(String id, JobTask t);

    /**
     * Forget a task so that future calls to getPersistedJobs will NOT return a copy of t. If id
     * is not already persisted, do nothing.
     *
     * Must be threadsafe
     *
     * @param id id of the task to forget
     */
    public void forget(String id);

    /**
     * Generates a new set of JobTask objects (linked with their ids) matching the parameters passed in via persist.
     *
     * Must be threadsafe
     *
     * @return
     */
    public Collection<JobTaskId> getPersistedJobs();
}
