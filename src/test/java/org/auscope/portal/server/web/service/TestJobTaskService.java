package org.auscope.portal.server.web.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.web.service.jobtask.JobTask;
import org.auscope.portal.server.web.service.jobtask.JobTaskListener;
import org.auscope.portal.server.web.service.jobtask.JobTaskRepository;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestJobTaskService extends PortalTestClass {

    private long POLL_INTERVAL = 10; //Hacky way to ensure thread timing
    private long TASK1_TIME = 100; //Hacky way to ensure thread timing
    private long TASK2_TIME = 100; //Hacky way to ensure thread timing
    private long TASK3_TIME = 100; //Hacky way to ensure thread timing

    final String guid1 = "124151241";
    final String guid2 = "422378990";
    final String guid3 = "shadlsahlsda";

    private JobTaskService service;
    private JobTaskRepository mockPersistor = context.mock(JobTaskRepository.class);
    private JobTaskListener mockListener = context.mock(JobTaskListener.class);
    private ExecutorService executor;
    private JobTask mockTask1 = context.mock(JobTask.class, "mockTask1");
    private JobTask mockTask2 = context.mock(JobTask.class, "mockTask2");
    private JobTask mockTask3 = context.mock(JobTask.class, "mockTask3");

    @SuppressWarnings("unchecked")
    private FutureTask<Object> mockFuture1 = context.mock(FutureTask.class, "mockFuture1");
    @SuppressWarnings("unchecked")
    private FutureTask<Object> mockFuture2 = context.mock(FutureTask.class, "mockFuture2");
    @SuppressWarnings("unchecked")
    private FutureTask<Object> mockFuture3 = context.mock(FutureTask.class, "mockFuture3");


    @After
    public void teardown() throws Exception {
        executor.shutdown();
        executor.awaitTermination(1000L, TimeUnit.MILLISECONDS);
    }

    /**
     * Tests persistance and listeners all are called as appropriate
     * when the jobs run as expected
     * @throws Exception
     */
    @Test
    public void testNormalOperation() throws Exception {
        executor = Executors.newFixedThreadPool(1);

        context.checking(new Expectations() {
            {
                oneOf(mockPersistor).findAll();
                will(returnValue(new ArrayList<JobTask>()));

                allowing(mockTask1).getId();
                will(returnValue(guid1));
                allowing(mockTask2).getId();
                will(returnValue(guid2));
                allowing(mockTask3).getId();
                will(returnValue(guid3));

                allowing(mockTask1).getTask();
                will(returnValue(mockFuture1));
                allowing(mockTask2).getTask();
                will(returnValue(mockFuture2));
                allowing(mockTask3).getTask();
                will(returnValue(mockFuture3));

                oneOf(mockPersistor).saveAndFlush(mockTask1);
                oneOf(mockPersistor).saveAndFlush(mockTask2);
                oneOf(mockPersistor).saveAndFlush(mockTask3);

                oneOf(mockFuture1).run();
                will(delayReturnValue(TASK1_TIME, ""));
                oneOf(mockFuture2).run();
                will(delayReturnValue(TASK2_TIME, ""));
                oneOf(mockFuture3).run();
                will(delayReturnValue(TASK3_TIME, ""));
            }});


        service = new JobTaskService(executor, mockListener);
        service.setPersistor(mockPersistor);
        service.initService();

        context.checking(new Expectations() {{
        }});

        Assert.assertEquals(guid1, service.submit(mockTask1));
        context.checking(new Expectations() {{
            oneOf(mockPersistor).delete(guid1);
            oneOf(mockListener).handleTaskFinish(guid1, mockTask1);
        }});
        Assert.assertTrue(service.isExecuting(guid1));


        Assert.assertEquals(guid2, service.submit(mockTask2));
        context.checking(new Expectations() {{
            oneOf(mockPersistor).delete(guid2);
            oneOf(mockListener).handleTaskFinish(guid2, mockTask2);
        }});
        Assert.assertTrue(service.isExecuting(guid2));

        Assert.assertEquals(guid3, service.submit(mockTask3));
        context.checking(new Expectations() {{
            oneOf(mockPersistor).delete(guid3);
            oneOf(mockListener).handleTaskFinish(guid3, mockTask3);
        }});
        Assert.assertTrue(service.isExecuting(guid3));

        Assert.assertNotNull(guid1);
        Assert.assertFalse(guid1.isEmpty());
        Assert.assertNotNull(guid2);
        Assert.assertFalse(guid2.isEmpty());
        Assert.assertNotNull(guid3);
        Assert.assertFalse(guid3.isEmpty());

        Assert.assertFalse(guid1.equals(guid2));
        Assert.assertFalse(guid2.equals(guid3));
        Assert.assertFalse(guid1.equals(guid3));

        //Wait for job 1 to stop
        Assert.assertTrue(service.isExecuting(guid1));
        long start = new Date().getTime();
        while (service.isExecuting(guid1)) {
            Thread.sleep(POLL_INTERVAL);
            Assert.assertTrue(new Date().getTime() - start < 1000L); //1 second timeout
        }

        //Wait for job 2 to stop
        Assert.assertTrue(service.isExecuting(guid2));
        start = new Date().getTime();
        while (service.isExecuting(guid2)) {
            Thread.sleep(POLL_INTERVAL);
            Assert.assertTrue(new Date().getTime() - start < 1000L); //1 second timeout
        }

        //Wait for job 3 to stop
        Assert.assertTrue(service.isExecuting(guid3));
        start = new Date().getTime();
        while (service.isExecuting(guid3)) {
            Thread.sleep(POLL_INTERVAL);
            Assert.assertTrue(new Date().getTime() - start < 1000L); //1 second timeout
        }
    }

    /**
     * Tests fetching null guids returns false/null as expected
     * @throws Exception
     */
    @Test
    public void testNullGuids() throws Exception {
        executor = Executors.newFixedThreadPool(1);
        context.checking(new Expectations() {{
            oneOf(mockPersistor).findAll();will(returnValue(new ArrayList<JobTask>()));
        }});
        service = new JobTaskService(executor, mockListener);
        service.setPersistor(mockPersistor);
        service.initService();
        Assert.assertFalse(service.isExecuting(null));
        Assert.assertNull(service.getTask(null));
    }

    /**
     * Tests persistance and listeners all are called as appropriate
     * when the jobs run as expected
     * @throws Exception
     */
    @Test
    public void testSubmitNoPersistance() throws Exception {
        executor = Executors.newFixedThreadPool(1);
        service = new JobTaskService(executor, mockListener);

        context.checking(new Expectations() {{
            allowing(mockTask1).getId();will(returnValue(guid1));
            allowing(mockTask2).getId();will(returnValue(guid2));
            allowing(mockTask3).getId();will(returnValue(guid3));

            allowing(mockTask1).getTask();will(returnValue(mockFuture1));
            allowing(mockTask2).getTask();will(returnValue(mockFuture2));
            allowing(mockTask3).getTask();will(returnValue(mockFuture3));

            oneOf(mockFuture1).run();will(delayReturnValue(TASK1_TIME, ""));
            oneOf(mockFuture2).run();will(delayReturnValue(TASK2_TIME, ""));
            oneOf(mockFuture3).run();will(delayReturnValue(TASK3_TIME, ""));
        }});

        final String guid1 = service.submit(mockTask1);
        context.checking(new Expectations() {{
            oneOf(mockListener).handleTaskFinish(guid1, mockTask1);
        }});
        Assert.assertTrue(service.isExecuting(guid1));


        final String guid2 = service.submit(mockTask2);
        context.checking(new Expectations() {{
            oneOf(mockListener).handleTaskFinish(guid2, mockTask2);
        }});
        Assert.assertTrue(service.isExecuting(guid2));

        final String guid3 = service.submit(mockTask3);
        context.checking(new Expectations() {{
            oneOf(mockListener).handleTaskFinish(guid3, mockTask3);
        }});
        Assert.assertTrue(service.isExecuting(guid3));

        Assert.assertNotNull(guid1);
        Assert.assertFalse(guid1.isEmpty());
        Assert.assertNotNull(guid2);
        Assert.assertFalse(guid2.isEmpty());
        Assert.assertNotNull(guid3);
        Assert.assertFalse(guid3.isEmpty());

        Assert.assertFalse(guid1.equals(guid2));
        Assert.assertFalse(guid2.equals(guid3));
        Assert.assertFalse(guid1.equals(guid3));

        //Wait for job 1 to stop
        Assert.assertTrue(service.isExecuting(guid1));
        long start = new Date().getTime();
        while (service.isExecuting(guid1)) {
            Thread.sleep(POLL_INTERVAL);
            Assert.assertTrue(new Date().getTime() - start < 1000L); //1 second timeout
        }

        //Wait for job 2 to stop
        Assert.assertTrue(service.isExecuting(guid2));
        start = new Date().getTime();
        while (service.isExecuting(guid2)) {
            Thread.sleep(POLL_INTERVAL);
            Assert.assertTrue(new Date().getTime() - start < 1000L); //1 second timeout
        }

        //Wait for job 3 to stop
        Assert.assertTrue(service.isExecuting(guid3));
        start = new Date().getTime();
        while (service.isExecuting(guid3)) {
            Thread.sleep(POLL_INTERVAL);
            Assert.assertTrue(new Date().getTime() - start < 1000L); //1 second timeout
        }
    }

    /**
     * Tests things still operate when jobs fail
     * when the jobs run as expected
     * @throws Exception
     */
    @Test
    public void testSubmitErrors() throws Exception {
        executor = Executors.newFixedThreadPool(1);
        service = new JobTaskService(executor, mockListener);

        context.checking(new Expectations() {{
            allowing(mockTask1).getId();will(returnValue(guid1));
            allowing(mockTask2).getId();will(returnValue(guid2));
            allowing(mockTask3).getId();will(returnValue(guid3));

            allowing(mockTask1).getTask();will(returnValue(mockFuture1));
            allowing(mockTask2).getTask();will(returnValue(mockFuture2));
            allowing(mockTask3).getTask();will(returnValue(mockFuture3));

            oneOf(mockFuture1).run();will(delayReturnValue(TASK1_TIME, ""));
            oneOf(mockFuture2).run();will(delayReturnValue(TASK2_TIME, new IOException()));
            oneOf(mockFuture3).run();will(delayReturnValue(TASK3_TIME, ""));
        }});

        Assert.assertEquals(guid1, service.submit(mockTask1));
        context.checking(new Expectations() {{
            oneOf(mockListener).handleTaskFinish(guid1, mockTask1);
        }});
        Assert.assertTrue(service.isExecuting(guid1));


        Assert.assertEquals(guid2, service.submit(mockTask2));
        context.checking(new Expectations() {{
            oneOf(mockListener).handleTaskFinish(guid2, mockTask2);
        }});
        Assert.assertTrue(service.isExecuting(guid2));

        Assert.assertEquals(guid3, service.submit(mockTask3));
        context.checking(new Expectations() {{
            oneOf(mockListener).handleTaskFinish(guid3, mockTask3);
        }});
        Assert.assertTrue(service.isExecuting(guid3));

        Assert.assertNotNull(guid1);
        Assert.assertFalse(guid1.isEmpty());
        Assert.assertNotNull(guid2);
        Assert.assertFalse(guid2.isEmpty());
        Assert.assertNotNull(guid3);
        Assert.assertFalse(guid3.isEmpty());

        Assert.assertFalse(guid1.equals(guid2));
        Assert.assertFalse(guid2.equals(guid3));
        Assert.assertFalse(guid1.equals(guid3));

        //Wait for job 1 to stop
        Assert.assertTrue(service.isExecuting(guid1));
        long start = new Date().getTime();
        while (service.isExecuting(guid1)) {
            Thread.sleep(POLL_INTERVAL);
            Assert.assertTrue(new Date().getTime() - start < 1000L); //1 second timeout
        }

        //Wait for job 2 to stop
        Assert.assertTrue(service.isExecuting(guid2));
        start = new Date().getTime();
        while (service.isExecuting(guid2)) {
            Thread.sleep(POLL_INTERVAL);
            Assert.assertTrue(new Date().getTime() - start < 1000L); //1 second timeout
        }

        //Wait for job 3 to stop
        Assert.assertTrue(service.isExecuting(guid3));
        start = new Date().getTime();
        while (service.isExecuting(guid3)) {
            Thread.sleep(POLL_INTERVAL);
            Assert.assertTrue(new Date().getTime() - start < 1000L); //1 second timeout
        }
    }

    /**
     * Tests persistance will restore correctly at startup
     * @throws Exception
     */
    @Test
    public void testPersistanceRestore() throws Exception {

        context.checking(new Expectations() {{
            allowing(mockTask1).getId();will(returnValue(guid1));
            allowing(mockTask2).getId();will(returnValue(guid2));
            allowing(mockTask3).getId();will(returnValue(guid3));

            allowing(mockTask1).getTask();will(returnValue(mockFuture1));
            allowing(mockTask2).getTask();will(returnValue(mockFuture2));
            allowing(mockTask3).getTask();will(returnValue(mockFuture3));

            oneOf(mockFuture1).run();will(delayReturnValue(TASK1_TIME, ""));
            oneOf(mockFuture2).run();will(delayReturnValue(TASK2_TIME, ""));
            oneOf(mockFuture3).run();will(delayReturnValue(TASK3_TIME, ""));
        }});

        context.checking(new Expectations() {{
            oneOf(mockPersistor).findAll();will(returnValue(Arrays.asList(mockTask1, mockTask2, mockTask3)));

            oneOf(mockPersistor).delete(guid1);
            oneOf(mockPersistor).delete(guid2);
            oneOf(mockPersistor).delete(guid3);

            oneOf(mockListener).handleTaskFinish(guid1, mockTask1);
            oneOf(mockListener).handleTaskFinish(guid2, mockTask2);
            oneOf(mockListener).handleTaskFinish(guid3, mockTask3);
        }});

        executor = Executors.newFixedThreadPool(1);
        service = new JobTaskService(executor, mockListener);
        service.setPersistor(mockPersistor);
        service.initService();

        Assert.assertTrue(service.isExecuting(guid1));
        Assert.assertTrue(service.isExecuting(guid2));
        Assert.assertTrue(service.isExecuting(guid3));

        //Wait for job 1 to stop
        Assert.assertTrue(service.isExecuting(guid1));
        long start = new Date().getTime();
        while (service.isExecuting(guid1)) {
            Thread.sleep(POLL_INTERVAL);
            Assert.assertTrue(new Date().getTime() - start < 1000L); //1 second timeout
        }

        //Wait for job 2 to stop
        Assert.assertTrue(service.isExecuting(guid2));
        start = new Date().getTime();
        while (service.isExecuting(guid2)) {
            Thread.sleep(POLL_INTERVAL);
            Assert.assertTrue(new Date().getTime() - start < 1000L); //1 second timeout
        }

        //Wait for job 3 to stop
        Assert.assertTrue(service.isExecuting(guid3));
        start = new Date().getTime();
        while (service.isExecuting(guid3)) {
            Thread.sleep(POLL_INTERVAL);
            Assert.assertTrue(new Date().getTime() - start < 1000L); //1 second timeout
        }
    }
}
