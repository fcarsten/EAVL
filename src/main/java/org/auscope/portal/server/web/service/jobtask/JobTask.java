package org.auscope.portal.server.web.service.jobtask;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.eavl.EAVLJob;

/**
 * An idempotent task associated with a particular Job.
 *
 * A Task differs from job submission in that it requires a running instance
 * to exist for as long as the processing job is running.
 * @author Josh Vote
 *
 */
public class JobTask extends FutureTask<Object> {

    protected final Log log = LogFactory.getLog(getClass());

    protected EAVLJob job;
    protected String email;


    public JobTask(Callable<Object> c, EAVLJob job) {
        super(c);
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

    @Override
    public void run() {
        log.warn("Starting job run");

        super.run();
    }
}
