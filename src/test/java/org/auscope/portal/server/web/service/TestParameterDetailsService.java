package org.auscope.portal.server.web.service;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.auscope.portal.core.cloud.StagingInformation;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.ParameterDetails;
import org.jmock.Expectations;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestParameterDetailsService extends PortalTestClass {

    private static StagingInformation testStagingInfo;
    private static int testCounter = 0;

    private ParameterDetailsService pdService;
    private EAVLJob job;
    private CSVService mockCsvService = context.mock(CSVService.class);
    private FileStagingService fss;
    private InputStream mockIs = context.mock(InputStream.class);

    /**
     * This sets up a temporary directory in the target directory for the JobFileService
     * to utilise as a staging area
     */
    @BeforeClass
    public static void setup() {
        testStagingInfo = new StagingInformation(String.format(
                "target%1$sTestParameterDetailsService-%2$s%1$s",
                File.separator, new Date().getTime()));

        File dir = new File(testStagingInfo.getStageInDirectory());
        Assert.assertTrue("Failed setting up staging directory", dir.mkdirs());
    }

    /**
     * This tears down the staging area used by the tests
     */
    @AfterClass
    public static void tearDown() {
        File dir = new File(testStagingInfo.getStageInDirectory());
        FileIOUtil.deleteFilesRecursive(dir);

        //Ensure cleanup succeeded
        Assert.assertFalse(dir.exists());
    }

    /**
     * Creates a fresh job object for each unit test (with a unique fileStorageID).
     * @throws PortalServiceException
     */
    @Before
    public void setupJobObj() throws PortalServiceException {
        job = new EAVLJob();
        job.setId(testCounter++);

        fss = new FileStagingService(testStagingInfo); //mocking this out is too tough
        fss.generateStageInDirectory(job);

        pdService = new ParameterDetailsService(fss, mockCsvService);
    }

    @Test
    public void testCacheRead() throws Exception {
        final String file = "example-file.csv";
        final ParameterDetails pd = new ParameterDetails("col-0", 0);
        final List<ParameterDetails> expectedPds = Arrays.asList(pd);

        context.checking(new Expectations() {{
            oneOf(mockCsvService).extractParameterDetails(with(any(InputStream.class)));
            will(returnValue(expectedPds));
        }});

        List<ParameterDetails> originalList = pdService.getParameterDetails(job, file);
        List<ParameterDetails> cachedList = pdService.getParameterDetails(job, file);

        Assert.assertSame(expectedPds, originalList);
        Assert.assertNotNull(cachedList);
        Assert.assertEquals(expectedPds.size(), cachedList.size());
        Assert.assertEquals(expectedPds.get(0).getName(), cachedList.get(0).getName());
        Assert.assertEquals(expectedPds.get(0).getColumnIndex(), cachedList.get(0).getColumnIndex());
    }

    @Test
    public void testCachePurge() throws Exception {
        final String file = "example-file.csv";
        final ParameterDetails pd = new ParameterDetails("col-0", 0);
        final List<ParameterDetails> expectedPds = Arrays.asList(pd);

        context.checking(new Expectations() {{
            exactly(2).of(mockCsvService).extractParameterDetails(with(any(InputStream.class)));
            will(returnValue(expectedPds));
        }});

        List<ParameterDetails> originalList = pdService.getParameterDetails(job, file);
        pdService.getParameterDetails(job, file);
        pdService.getParameterDetails(job, file);
        pdService.purgeCache(job, file);
        pdService.getParameterDetails(job, file);
        pdService.getParameterDetails(job, file);
        List<ParameterDetails> cachedList = pdService.getParameterDetails(job, file);

        Assert.assertSame(expectedPds, originalList);
        Assert.assertNotNull(cachedList);
        Assert.assertEquals(expectedPds.size(), cachedList.size());
        Assert.assertEquals(expectedPds.get(0).getName(), cachedList.get(0).getName());
        Assert.assertEquals(expectedPds.get(0).getColumnIndex(), cachedList.get(0).getColumnIndex());
    }

    @Test
    public void testCacheWrite() throws Exception {
        final String file = "example-file.csv";
        final ParameterDetails pd = new ParameterDetails("col-0", 0);
        final ParameterDetails pd2 = new ParameterDetails("col-99", 99);
        final List<ParameterDetails> expectedPds = Arrays.asList(pd);

        context.checking(new Expectations() {{
            oneOf(mockCsvService).extractParameterDetails(with(any(InputStream.class)));
            will(returnValue(expectedPds));
        }});

        List<ParameterDetails> originalList = pdService.getParameterDetails(job, file);
        pdService.updateCache(job, file, Arrays.asList(pd2));
        List<ParameterDetails> cachedList = pdService.getParameterDetails(job, file);

        Assert.assertSame(expectedPds, originalList);
        Assert.assertNotNull(cachedList);
        Assert.assertEquals(expectedPds.size(), cachedList.size());
        Assert.assertEquals(pd2.getName(), cachedList.get(0).getName());
        Assert.assertEquals(pd2.getColumnIndex(), cachedList.get(0).getColumnIndex());
    }

    @Test
    public void testEmptyCacheWrite() throws Exception {
        final String file = "example-file.csv";
        final ParameterDetails pd = new ParameterDetails("col-0", 0);
        final ParameterDetails pd2 = new ParameterDetails("col-99", 99);
        final List<ParameterDetails> expectedPds = Arrays.asList(pd);

        context.checking(new Expectations() {{

        }});

        pdService.updateCache(job, file, Arrays.asList(pd2));
        List<ParameterDetails> cachedList = pdService.getParameterDetails(job, file);
        Assert.assertNotNull(cachedList);
        Assert.assertEquals(expectedPds.size(), cachedList.size());
        Assert.assertEquals(pd2.getName(), cachedList.get(0).getName());
        Assert.assertEquals(pd2.getColumnIndex(), cachedList.get(0).getColumnIndex());
    }
}
