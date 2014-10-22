package org.auscope.portal.server.web.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.Assert;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for CSVService
 * @author Josh Vote
 *
 */
public class TestCSVService extends PortalTestClass{
    private CSVService service;
    private InputStream mockStream;

    @Before
    public void setup() {
        service = new CSVService();
        mockStream = context.mock(InputStream.class);
    }

    @Test
    public void testReadLines() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");

        List<String[]> lines = service.readLines(is, 4, 2);

        Assert.assertNotNull(lines);
        Assert.assertEquals(2, lines.size());

        Assert.assertEquals(5, lines.get(0).length);
        Assert.assertEquals("sample", lines.get(0)[0]);
        Assert.assertEquals(" gold (au) ppm", lines.get(0)[1]);
        Assert.assertEquals("", lines.get(0)[3]);

        Assert.assertEquals(5, lines.get(1).length);
        Assert.assertEquals("0", lines.get(1)[0]);
        Assert.assertEquals(" 59", lines.get(1)[3]);
    }

    @Test(expected=PortalServiceException.class)
    public void testReadLinesClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            atLeast(1).of(mockStream).close();
        }});

        service.readLines(mockStream, 10, 100);
    }

    @Test
    public void testEstimateColumnCount() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");

        Assert.assertEquals(5, service.estimateColumnCount(is));
    }

    @Test(expected=PortalServiceException.class)
    public void testEstimateColumnCountClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            atLeast(1).of(mockStream).close();
        }});

        service.estimateColumnCount(mockStream);
    }
}
