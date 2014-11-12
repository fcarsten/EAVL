package org.auscope.portal.server.web.service.jobtask;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

/**
 * A class for persisting JobTasks into a database
 * @author Josh Vote
 *
 */
@Component
public class DatabasePersistor implements JobTaskPersistor {

    protected final Log log = LogFactory.getLog(getClass());

    @Override
    public void persist(String id, JobTask t) {
        //TODO - Implement this
        log.error("TODO - persist task: " + id);

    }

    @Override
    public void forget(String id) {
        //TODO - Implement this
        log.error("TODO - forget task: " + id);
    }

    @Override
    public Collection<JobTaskId> getPersistedJobs() {
        //TODO - Implement this
        log.error("TODO - get persisted task: ");
        return new ArrayList<JobTaskId>();
    }

}
