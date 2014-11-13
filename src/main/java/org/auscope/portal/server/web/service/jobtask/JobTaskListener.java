package org.auscope.portal.server.web.service.jobtask;

import org.apache.http.annotation.ThreadSafe;

/**
 * An interface for handling the status changing of job tasks.
 * @author Josh Vote
 *
 */
@ThreadSafe
public interface JobTaskListener {
    /**
     * Called whenever a JobTask finishes executing. This method must be threadsafe
     * or use appropriate synchronization.
     * @param id The ID of the task
     * @param task The task that finished
     */
    public void handleTaskFinish(String id, JobTask task);
}
