package org.auscope.portal.server.web.service.jobtask;


/**
 * A union of a Jobtask with a unique task Identifier
 * @author Josh Vote
 *
 * @param <T>
 * @param <U>
 */
public class JobTaskId {
    private JobTask jobTask;
    private String id;

    /**
     * Creates a new instance
     * @param jobTask
     * @param id
     */
    public JobTaskId(JobTask jobTask, String id) {
        super();
        this.jobTask = jobTask;
        this.id = id;
    }

    /**
     * Gets the task
     * @return
     */
    public JobTask getJobTask() {
        return jobTask;
    }

    /**
     * Gets the ID
     * @return
     */
    public String getId() {
        return id;
    }
}
