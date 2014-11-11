package org.auscope.portal.server.web.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.web.service.jobtask.JobTask;
import org.auscope.portal.server.web.service.jobtask.JobTaskId;
import org.auscope.portal.server.web.service.jobtask.JobTaskListener;
import org.auscope.portal.server.web.service.jobtask.JobTaskPersistor;
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
    private JobTaskPersistor persistor;
    private ConcurrentHashMap<String, ExecutingTask> activeTasks = new ConcurrentHashMap<String, ExecutingTask>();

    @Autowired
    public JobTaskService(ExecutorService executor, JobTaskListener listener) {
        this(executor, listener, null);
    }

    @Autowired
    public JobTaskService(ExecutorService executor, JobTaskListener listener, JobTaskPersistor persistor) {
        this.executor = MoreExecutors.listeningDecorator(executor);
        this.listener = listener;
        this.persistor = persistor;

        if (this.persistor != null) {
            for (JobTaskId jt : this.persistor.getPersistedJobs()) {
                this.submit(jt.getJobTask(), jt.getId());
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
        return submit(task, null);
    }

    /**
     * Returns true IFF GUID exists in this service AND it's still executing
     * @param guid The GUI returned from submit
     * @return
     */
    public boolean isExecuting(String guid) {
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
        ExecutingTask et = activeTasks.get(guid);
        if (et == null) {
            return null;
        }

        return et.jobTask;
    }

    private String submit(JobTask task, String guid)  {
        if (guid == null) {
            guid = UUID.randomUUID().toString();
        }

        if (persistor != null) {
            persistor.persist(guid, task);
        }

        final ExecutingTask et = new ExecutingTask(guid, task, executor.submit(task));
        final JobTaskListener l = listener;
        final ConcurrentHashMap<String, ExecutingTask> a = this.activeTasks;
        final JobTaskPersistor p = persistor;
        et.future.addListener(new Runnable() {
            @Override
            public void run() {
                if (persistor != null) {
                    p.forget(et.id);
                }
                a.remove(et.id);
                l.handleTaskFinish(et.id, et.jobTask);
            }
        }, MoreExecutors.directExecutor());
        activeTasks.put(guid, et);

        return guid;
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
