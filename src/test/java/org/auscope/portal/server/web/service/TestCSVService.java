package org.auscope.portal.server.web.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.server.eavl.ParameterDetails;
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
    private OutputStream mockOutputStream;

    @Before
    public void setup() {
        service = new CSVService();
        mockStream = context.mock(InputStream.class);
        mockOutputStream = context.mock(OutputStream.class);
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

    @Test(expected=PortalServiceException.class)
    public void testParameterDetailsClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            atLeast(1).of(mockStream).close();
        }});

        service.extractParameterDetails(mockStream);
    }

    @Test
    public void testExtractParameterDetailsParsing() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");

        List<ParameterDetails> details = service.extractParameterDetails(is);

        Assert.assertNotNull(details);
        Assert.assertEquals(5, details.size());

        Assert.assertEquals("sample", details.get(0).getName());
        Assert.assertEquals(0, details.get(0).getColumnIndex());
        Assert.assertEquals("gold (au) ppm", details.get(1).getName());
        Assert.assertEquals(1, details.get(1).getColumnIndex());
        Assert.assertEquals("something-else", details.get(2).getName());
        Assert.assertEquals(2, details.get(2).getColumnIndex());
        Assert.assertEquals("D", details.get(3).getName());
        Assert.assertEquals(3, details.get(3).getColumnIndex());
        Assert.assertEquals("data", details.get(4).getName());
        Assert.assertEquals(4, details.get(4).getColumnIndex());

        Assert.assertEquals(0, details.get(0).getTotalText());
        Assert.assertEquals(8, details.get(0).getTotalNumeric());
        Assert.assertEquals(0, details.get(0).getTotalMissing());

        Assert.assertEquals(1, details.get(2).getTotalText());
        Assert.assertEquals(6, details.get(2).getTotalNumeric());
        Assert.assertEquals(1, details.get(2).getTotalMissing());
        Assert.assertEquals(1, details.get(2).getTextValues().size());
        Assert.assertTrue(details.get(2).getTextValues().containsKey((" D/L")));
        Assert.assertEquals(1, (int)details.get(2).getTextValues().get(" D/L"));

        Assert.assertEquals(0, details.get(4).getTotalText());
        Assert.assertEquals(5, details.get(4).getTotalNumeric());
        Assert.assertEquals(3, details.get(4).getTotalMissing());
    }

    @Test
    public void testExtractParameterDetailsNoHeaders() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");

        List<ParameterDetails> details = service.extractParameterDetails(is);

        Assert.assertNotNull(details);
        Assert.assertEquals(5, details.size());

        Assert.assertEquals("A", details.get(0).getName());
        Assert.assertEquals(0, details.get(0).getColumnIndex());
        Assert.assertEquals("B", details.get(1).getName());
        Assert.assertEquals(1, details.get(1).getColumnIndex());
        Assert.assertEquals("C", details.get(2).getName());
        Assert.assertEquals(2, details.get(2).getColumnIndex());
        Assert.assertEquals("D", details.get(3).getName());
        Assert.assertEquals(3, details.get(3).getColumnIndex());
        Assert.assertEquals("E", details.get(4).getName());
        Assert.assertEquals(4, details.get(4).getColumnIndex());

        Assert.assertEquals(0, details.get(0).getTotalText());
        Assert.assertEquals(8, details.get(0).getTotalNumeric());
        Assert.assertEquals(0, details.get(0).getTotalMissing());

        Assert.assertEquals(2, details.get(2).getTotalText());
        Assert.assertEquals(6, details.get(2).getTotalNumeric());
        Assert.assertEquals(0, details.get(2).getTotalMissing());
        Assert.assertEquals(1, details.get(2).getTextValues().size());
        Assert.assertTrue(details.get(2).getTextValues().containsKey((" D/L")));
        Assert.assertEquals(2, (int)details.get(2).getTextValues().get(" D/L"));

        Assert.assertEquals(0, details.get(4).getTotalText());
        Assert.assertEquals(5, details.get(4).getTotalNumeric());
        Assert.assertEquals(3, details.get(4).getTotalMissing());
    }

    @Test
    public void testExtractParameterDetailsEmptyInput() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/empty-data.csv");

        List<ParameterDetails> details = service.extractParameterDetails(is);

        Assert.assertNotNull(details);
        Assert.assertEquals(0, details.size());
    }

    @Test
    public void testExtractParameterValuesEmptyInput() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/empty-data.csv");

        List<Double> vals = service.getParameterValues(is, 0);

        Assert.assertNotNull(vals);
        Assert.assertEquals(0, vals.size());
    }

    @Test(expected=PortalServiceException.class)
    public void testParameterValuesClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            atLeast(1).of(mockStream).close();
        }});

        service.getParameterValues(mockStream, 0);
    }

    @Test
    public void testGetParameterValuesParsing() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");

        List<Double> data = service.getParameterValues(is, 2);

        Assert.assertNotNull(data);
        Assert.assertEquals(8, data.size());

        Assert.assertEquals(100, data.get(0), 0.01);
        Assert.assertNull(data.get(1));
        Assert.assertEquals(103, data.get(2), 0.01);
        Assert.assertEquals(101, data.get(3), 0.01);
        Assert.assertEquals(103, data.get(4), 0.01);
        Assert.assertEquals(100, data.get(5), 0.01);
        Assert.assertNull(data.get(6));
        Assert.assertEquals(101, data.get(7), 0.01);
    }

    @Test
    public void testGetParameterValuesNoHeaders() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");

        List<Double> data = service.getParameterValues(is, 2);

        Assert.assertNotNull(data);
        Assert.assertEquals(8, data.size());

        Assert.assertNull(data.get(0));
        Assert.assertEquals(102, data.get(1), 0.01);
        Assert.assertEquals(103, data.get(2), 0.01);
        Assert.assertEquals(101, data.get(3), 0.01);
        Assert.assertEquals(103, data.get(4), 0.01);
        Assert.assertEquals(100, data.get(5), 0.01);
        Assert.assertNull(data.get(6));
        Assert.assertEquals(101, data.get(7), 0.01);
    }

    @Test
    public void testFindReplace() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-data.csv");
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Assert.assertEquals(6, service.findReplace(is, os, 1, "DL", "999"));
        String expected = "'sample',' gold (au) ppm'\n" +
                           "'40','40'\n" +
                           "'','42'\n" +
                           "'DL','999'\n" +
                           "'DLVal','DLVal'\n" +
                           "'DL','999'\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test
    public void testFindReplaceNoReplace() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-data.csv");
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Assert.assertEquals(6, service.findReplace(is, os, -1, "40", "SHOULD NOT REPLACE"));
        String expected = "'sample',' gold (au) ppm'\n" +
                           "'40','40'\n" +
                           "'','42'\n" +
                           "'DL','DL'\n" +
                           "'DLVal','DLVal'\n" +
                           "'DL','DL'\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test
    public void testFindReplaceWhitespaceReplace() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-data.csv");
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Assert.assertEquals(6, service.findReplace(is, os, 0, null, "REPLACED"));
        String expected = "'sample',' gold (au) ppm'\n" +
                           "'40','40'\n" +
                           "'REPLACED','42'\n" +
                           "'DL','DL'\n" +
                           "'DLVal','DLVal'\n" +
                           "'DL','DL'\n";

        Assert.assertEquals(expected, os.toString());
    }

    /**
     * This is an edge case but there was a bug where CSVWriter was being sensitive to the underlying stream being closed
     * before the writer resulting in partially written files. It could only be reproduced with a file stream so hence this
     * temporary file nonsense.
     * @throws Exception
     */
    @Test
    public void testFindReplaceLargeFileOnDisk() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-large.csv");

        File tmp = File.createTempFile("TestCSVService", "csv-tmp");
        FileOutputStream fos = null;
        FileReader reader = null;
        String actual = null;
        try {
            fos = new FileOutputStream(tmp);

            Assert.assertEquals(10001, service.findReplace(is, fos, 1, null, null));

            reader = new FileReader(tmp);
            actual = IOUtils.toString(reader);
        } finally {
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(reader);
            if (!tmp.delete()) {
                tmp.deleteOnExit();
            }
        }

        //This is to test that the output streams get properly flushed
        Assert.assertTrue(actual.endsWith("'9999',' 35',' D/L',' 80',' '\n"));
    }

    @Test(expected=PortalServiceException.class)
    public void testFindReplaceClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            allowing(mockOutputStream).flush();

            atLeast(1).of(mockStream).close();
            atLeast(1).of(mockOutputStream).close();
        }});

        service.findReplace(mockStream, mockOutputStream, 1, "DL", "999");
    }
}
