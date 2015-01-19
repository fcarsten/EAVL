package org.auscope.portal.server.web.service.jobtask;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.eavl.EAVLJob;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * An idempotent task associated with a particular Job.
 *
 * A Task differs from job submission in that it requires a running instance
 * to exist for as long as the processing job is running.
 * @author Josh Vote
 *
 */
@Entity
public class JobTask  {

    @Transient
    private FutureTask<Object> task;

    /**
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @see java.util.concurrent.FutureTask#get()
     */
    public Object get() throws InterruptedException, ExecutionException {
        return task.get();
    }

    /**
     * @return the task
     */
    public FutureTask<Object> getTask() {
        return task;
    }

    /**
     * @param imputationCallable the task to set
     */
    public void setTask(Callable<Object> c) {
        this.task = new FutureTask<Object>(c);
    }

    @Transient
    protected final Log log = LogFactory.getLog(getClass());

    @ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
    protected EAVLJob job;

    @Basic
    protected String email;

    @Id
    private String id;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    public JobTask() {
        // JPA
    }

    /**
     * @param task the task to set
     */
    public void setTask(FutureTask<Object> task) {
        this.task = task;
    }

    /**
     * @param job the job to set
     */
    public void setJob(EAVLJob job) {
        this.job = job;
    }

    public JobTask(EAVLJob job) {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        this.job = job;
    }

    /**
     * Get a notification email for task completion (or null if none set)
     * @return
     */
    public String getEmail() {
        return email;
    }


    /**
     * Set a notification email for task completion (or null if none set)
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public EAVLJob getJob() {
        return job;
    }

    public void run() {
        log.warn("Starting job run");

        task.run();
    }
}
