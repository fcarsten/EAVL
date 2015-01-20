package org.auscope.portal.server.web.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.web.service.jobtask.JobTask;
import org.auscope.portal.server.web.service.jobtask.JobTaskListener;
import org.auscope.portal.server.web.service.jobtask.JobTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Utility for executing JobTask's using an internal executor. Optional
 * listeners can be notified soon after a task finishes execution.
 *
 * This service does not persist any data by default but the
 *
 * @author Josh Vote
 *
 */
@Service
public class JobTaskService {

    protected final Log log = LogFactory.getLog(getClass());

    private ListeningExecutorService executor;
    private JobTaskListener listener;

    @Autowired
    private JobTaskRepository persistor;
    /**
     * @return the persistor
     */
    public JobTaskRepository getPersistor() {
        return persistor;
    }


    /**
     * @param persistor the persistor to set
     */
    public void setPersistor(JobTaskRepository persistor) {
        this.persistor = persistor;
    }


    private ConcurrentHashMap<String, ExecutingTask> activeTasks = new ConcurrentHashMap<String, ExecutingTask>();

    @Autowired
    public JobTaskService(ExecutorService executor, JobTaskListener listener) {
        this.executor = MoreExecutors.listeningDecorator(executor);
        this.listener = listener;
    }

    @PostConstruct
    public void initService() {
        if (this.persistor != null) {
            List<JobTask> jobs = persistor.findAll();
            for (JobTask jt : jobs) {
                this.submit(jt, false);
            }
        }
    }

    /**
     * Submits a task for execution. Returns a GUID for identifying this submission.
     *
     * If a persistor has been configured for this instance then the task will be persisted
     * outside the lifetime of this class. Note - task is supposed to be idempotent, there is
     * every possibility that a persisted task may be restarted without notification
     *
     * @param task The task to submit for execution.
     * @return
     */
    public String submit(JobTask task)  {
        return submit(task, true);
    }

    /**
     * Returns true IFF GUID exists in this service AND it's still executing
     * @param guid The GUI returned from submit
     * @return
     */
    public boolean isExecuting(String guid) {
        if (guid == null) {
            return false;
        }

        ExecutingTask et = activeTasks.get(guid);
        if (et == null) {
            return false;
        }

        return !et.future.isCancelled() && !et.future.isDone();
    }

    /**
     * Gets a JobTask with the specified guid (as returned by submit). Returns null if the
     * JobTask DNE
     *
     * Note - complete JobTask objects are automatically removed from this service
     *
     * @param guid The GUI returned from submit
     * @return
     */
    public JobTask getTask(String guid) {
        if (guid == null) {
            return null;
        }

        ExecutingTask et = activeTasks.get(guid);
        if (et == null) {
            return null;
        }

        return et.jobTask;
    }

    private String submit(JobTask task, boolean persist)  {

        if (persistor != null && persist) {
            persistor.saveAndFlush(task);
//            persistor.persist(guid, task);
        }

        final ExecutingTask et = new ExecutingTask(task.getId(), task, executor.submit(task.getTask()));
        final JobTaskListener l = listener;
        final ConcurrentHashMap<String, ExecutingTask> a = this.activeTasks;
//        final JobRepository p = persistor;
        et.future.addListener(new Runnable() {
            @Override
            public void run() {
                if (persistor != null) {
                    persistor.delete(et.id);
//                    p.forget(et.id);
                }
                a.remove(et.id);
                l.handleTaskFinish(et.id, et.jobTask);
            }
        }, MoreExecutors.directExecutor());
        activeTasks.put(task.getId(), et);

        return task.getId();
    }


    private class ExecutingTask {
        public String id;
        public JobTask jobTask;
        public ListenableFuture future;
        public ExecutingTask(String id, JobTask jobTask, ListenableFuture future) {
            super();
            this.id = id;
            this.jobTask = jobTask;
            this.future = future;
        }


    }
}
