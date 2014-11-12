package org.auscope.portal.server.web.service.jobtask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Assert;

import org.auscope.eavl.wpsclient.ConditionalProbabilityWpsClient;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.eavl.EAVLJobConstants;
import org.auscope.portal.server.web.service.CSVService;
import org.jmock.Expectations;
import org.junit.Test;

public class TestImputationCallable extends PortalTestClass {
    private EAVLJob mockJob = context.mock(EAVLJob.class);
    private ConditionalProbabilityWpsClient mockWpsClient = context.mock(ConditionalProbabilityWpsClient.class);
    private CSVService mockCsvService = context.mock(CSVService.class);
    private FileStagingService mockFss = context.mock(FileStagingService.class);

    @Test
    public void testNormalOperation() throws Exception {
        ImputationCallable ic = new ImputationCallable(mockJob, mockWpsClient, mockCsvService, mockFss);

        final InputStream mockIs1 = context.mock(InputStream.class, "mockIs1");
        final InputStream mockIs2 = context.mock(InputStream.class, "mockIs2");
        final OutputStream mockOs = context.mock(OutputStream.class);

        final Double[][] data = new Double[][] {{0.2, 0.4, null}};
        final double[][] imputedData = new double[][] {{0.2, 0.4, 0.9}};

        context.checking(new Expectations() {{
            oneOf(mockFss).readFile(mockJob, EAVLJobConstants.FILE_DATA_CSV);will(returnValue(mockIs1));
            oneOf(mockFss).readFile(mockJob, EAVLJobConstants.FILE_DATA_CSV);will(returnValue(mockIs2));
            oneOf(mockFss).writeFile(mockJob, EAVLJobConstants.FILE_IMPUTED_CSV);will(returnValue(mockOs));

            oneOf(mockCsvService).getRawData(mockIs1);will(returnValue(data));
            oneOf(mockCsvService).writeRawData(mockIs2, mockOs, imputedData);

            oneOf(mockWpsClient).imputationNA(data);will(returnValue(imputedData));

            oneOf(mockIs1).close();
            oneOf(mockIs2).close();
            oneOf(mockOs).close();
        }});

        Assert.assertSame(imputedData, ic.call());
    }

    @Test(expected=IOException.class)
    public void testWPSError() throws Exception {
        ImputationCallable ic = new ImputationCallable(mockJob, mockWpsClient, mockCsvService, mockFss);

        final InputStream mockIs1 = context.mock(InputStream.class, "mockIs1");

        final Double[][] data = new Double[][] {{0.2, 0.4, null}};
        final double[][] imputedData = new double[][] {{0.2, 0.4, 0.9}};

        context.checking(new Expectations() {{
            oneOf(mockFss).readFile(mockJob, EAVLJobConstants.FILE_DATA_CSV);will(returnValue(mockIs1));
            oneOf(mockCsvService).getRawData(mockIs1);will(returnValue(data));
            oneOf(mockWpsClient).imputationNA(data);will(throwException(new IOException()));
            oneOf(mockIs1).close();
        }});

        Assert.assertSame(imputedData, ic.call());
    }
}
