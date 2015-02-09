package org.auscope.portal.server.web.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
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
    private InputStream mockStream2;
    private OutputStream mockOutputStream;

    @Before
    public void setup() {
        service = new CSVService();
        mockStream = context.mock(InputStream.class, "mockStream");
        mockStream2 = context.mock(InputStream.class, "mockStream2");
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
        Assert.assertEquals(7, details.get(0).getTotalNumeric());
        Assert.assertEquals(1, details.get(0).getTotalZeroes());
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
        Assert.assertEquals(7, details.get(0).getTotalNumeric());
        Assert.assertEquals(1, details.get(0).getTotalZeroes());
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

        double[] vals = service.getParameterValues(is, 0);

        Assert.assertNotNull(vals);
        Assert.assertEquals(0, vals.length);
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

        double[] data = service.getParameterValues(is, 2, true);

        Assert.assertNotNull(data);
        Assert.assertEquals(8, data.length);

        Assert.assertEquals(100, data[0], 0.01);
        Assert.assertEquals(Double.NaN, data[1], 0.01);
        Assert.assertEquals(103, data[2], 0.01);
        Assert.assertEquals(101, data[3], 0.01);
        Assert.assertEquals(103, data[4], 0.01);
        Assert.assertEquals(100, data[5], 0.01);
        Assert.assertEquals(Double.NaN, data[6], 0.01);
        Assert.assertEquals(101, data[7], 0.01);
    }

    @Test
    public void testGetParameterValuesNoNulls() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");

        double[] data = service.getParameterValues(is, 2, false);

        Assert.assertNotNull(data);
        Assert.assertEquals(6, data.length);

        Assert.assertEquals(100, data[0], 0.01);
        Assert.assertEquals(103, data[1], 0.01);
        Assert.assertEquals(101, data[2], 0.01);
        Assert.assertEquals(103, data[3], 0.01);
        Assert.assertEquals(100, data[4], 0.01);
        Assert.assertEquals(101, data[5], 0.01);
    }

    @Test
    public void testGetParameterValuesNoHeaders() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");

        double[] data = service.getParameterValues(is, 2);

        Assert.assertNotNull(data);
        Assert.assertEquals(8, data.length);

        Assert.assertEquals(Double.NaN, data[0], 0.01);
        Assert.assertEquals(102, data[1], 0.01);
        Assert.assertEquals(103, data[2], 0.01);
        Assert.assertEquals(101, data[3], 0.01);
        Assert.assertEquals(103, data[4], 0.01);
        Assert.assertEquals(100, data[5], 0.01);
        Assert.assertEquals(Double.NaN, data[6], 0.01);
        Assert.assertEquals(101, data[7], 0.01);
    }

    @Test
    public void testFindReplaceZeroes() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-zeroes-data.csv");
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Assert.assertEquals(6, service.findReplaceZeroes(is, os, 0, "ZERO_VALUE", false));
        String expected = "'sample',' gold (au) ppm'\n" +
                "'0.00001','40'\n" +
                "'','42'\n" +
                "'ZERO_VALUE','DL'\n" +
                "'ZERO_VALUE','DLVal'\n" +
                "'ZERO_VALUE','DL'\n";

        Assert.assertEquals(expected, os.toString());
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

    @Test
    public void testFindReplaceCreateHeaders() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Assert.assertEquals(9, service.findReplace(is, os, -1, null, null, true));
        String expected = "'A','B','C','D','E'\n" +
        "'0',' 40',' D/L',' ',' 12'\n" +
        "'1',' 42',' 102',' 52',' 12'\n" +
        "'2',' 16',' 103',' 6',' 15'\n" +
        "'3',' 13',' 101',' 43',' '\n" +
        "'4',' 16',' 103',' 74',' 16'\n" +
        "'5',' 48',' 100',' 32',' '\n" +
        "'6',' 41',' D/L',' 72',' 14'\n" +
        "'7',' 11',' 101',' 69',' '\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test
    public void testDeleteColumns() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HashSet<Integer> indexes = new HashSet<Integer>();

        indexes.add(0);
        indexes.add(2);
        indexes.add(4);

        Assert.assertEquals(8, service.deleteColumns(is, os, indexes));
        String expected = "' 40',' '\n" +
                "' 42',' 52'\n" +
                "' 16',' 6'\n" +
                "' 13',' 43'\n" +
                "' 16',' 74'\n" +
                "' 48',' 32'\n" +
                "' 41',' 72'\n" +
                "' 11',' 69'\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test(expected=PortalServiceException.class)
    public void testDeleteColumnsClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            allowing(mockOutputStream).flush();

            atLeast(1).of(mockStream).close();
            atLeast(1).of(mockOutputStream).close();
        }});

        service.deleteColumns(mockStream, mockOutputStream, new HashSet<Integer>());
    }

    @Test
    public void testDeleteAllColumns() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HashSet<Integer> indexes = new HashSet<Integer>();

        indexes.add(0);
        indexes.add(1);
        indexes.add(2);
        indexes.add(3);
        indexes.add(4);

        Assert.assertEquals(8, service.deleteColumns(is, os, indexes));
        String expected = "\n\n\n\n\n\n\n\n";

        Assert.assertEquals(expected, os.toString());
    }

    private void assertRawEquals(double[][] expected, double[][] actual) {
        if (expected == null || actual == null) {
            Assert.assertNull(actual);
            Assert.assertNull(expected);
            return;
        }

        Assert.assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals(expected[i].length, actual[i].length);
            for (int j = 0; j < expected[i].length; j++) {
                    Assert.assertEquals(expected[i][j], actual[i][j], 0.001);
            }
        }
    }

    private void assertRawEquals(String[][] expected, String[][] actual) {
        if (expected == null || actual == null) {
            Assert.assertNull(actual);
            Assert.assertNull(expected);
            return;
        }

        Assert.assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals(expected[i].length, actual[i].length);
            for (int j = 0; j < expected[i].length; j++) {
                if (expected[i][j] == null || actual[i][j] == null) {
                    Assert.assertNull(actual[i][j]);
                    Assert.assertNull(expected[i][j]);

                } else {
                    Assert.assertEquals(expected[i][j], actual[i][j]);
                }


            }
        }
    }

    @Test
    public void testGetRawData() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        double[][] expected = new double[][] {
                {0.0, 40.0, 100.0, 59.0, 12.0},
                {1.0, 42.0, Double.NaN, 52.0, 12.0},
                {2.0, 16.0, 103.0, 6.0, 15.0},
                {3.0, 13.0, 101.0, 43.0, Double.NaN},
                {4.0, 16.0, 103.0, 74.0, 16.0},
                {5.0, 48.0, 100.0, 32.0, Double.NaN},
                {6.0, 41.0, Double.NaN, 72.0, 14.0},
                {7.0, 11.0, 101.0, 69.0, Double.NaN}
        };

        double[][] actual = service.getRawData(is);
        assertRawEquals(expected, actual);
    }

    @Test
    public void testGetRawDataColIndexes() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        double[][] expected = new double[][] {
                {12.0, 59.0},
                {12.0, 52.0},
                {15.0, 6.0},
                {Double.NaN, 43.0},
                {16.0, 74.0},
                {Double.NaN, 32.0},
                {14.0, 72.0},
                {Double.NaN, 69.0}
        };

        double[][] actual = service.getRawData(is, Arrays.asList(4, 3));
        assertRawEquals(expected, actual);
    }

    @Test
    public void testGetRawDataColIndexesExclusion() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        double[][] expected = new double[][] {
                {59.0, 12.0},
                {52.0, 12.0},
                {6.0, 15.0},
                {43.0, Double.NaN},
                {74.0, 16.0},
                {32.0, Double.NaN},
                {72.0, 14.0},
                {69.0, Double.NaN}
        };

        double[][] actual = service.getRawData(is, Arrays.asList(0, 2, 1), false, false);
        assertRawEquals(expected, actual);
    }

    @Test
    public void testGetRawDataColIndexesExclusion2() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        double[][] expected = new double[][] {
                {0.0, 40.0, 59.0, 12.0},
                {1.0, 42.0, 52.0, 12.0},
                {2.0, 16.0, 6.0, 15.0},
                {3.0, 13.0, 43.0, Double.NaN},
                {4.0, 16.0, 74.0, 16.0},
                {5.0, 48.0, 32.0, Double.NaN},
                {6.0, 41.0, 72.0, 14.0},
                {7.0, 11.0, 69.0, Double.NaN}
        };

        double[][] actual = service.getRawData(is, Arrays.asList(2), false, false);
        assertRawEquals(expected, actual);
    }

    @Test
    public void testGetRawDataColIndexesExclusionSkipEmpty() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-data.csv");
        double[][] expected = new double[][] {
                {40.0}
        };

        double[][] actual = service.getRawData(is, Arrays.asList(1), false, true);
        assertRawEquals(expected, actual);
    }

    @Test
    public void testGetRawDataColIndexesInclusionSkipEmpty() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-data.csv");
        double[][] expected = new double[][] {
                {40.0, 40.0},
                {42.0, Double.NaN}
        };

        double[][] actual = service.getRawData(is, Arrays.asList(1, 0), true, true);
        assertRawEquals(expected, actual);
    }

    @Test
    public void testGetRawDataSkipEmpty() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-data.csv");
        double[][] expected = new double[][] {
                {40.0, 40.0},
                {Double.NaN, 42.0}
        };

        double[][] actual = service.getRawData(is, null, true, true);
        assertRawEquals(expected, actual);
    }

    @Test(expected=PortalServiceException.class)
    public void testGetRawDataClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            atLeast(1).of(mockStream).close();
        }});


        service.getRawData(mockStream);
    }

    @Test
    public void testGetRawStringDataColIndexes() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        String[][] expected = new String[][] {
                {" 12", " 59"},
                {" 12", " 52"},
                {" 15", " 6"},
                {" ", " 43"},
                {" 16", " 74"},
                {" ", " 32"},
                {" 14", " 72"},
                {" ", " 69"}
        };

        String[][] actual = service.getRawStringData(is, Arrays.asList(4, 3), true);
        assertRawEquals(expected, actual);
    }

    @Test
    public void testGetRawStringDataColIndexesExclusion() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        String[][] expected = new String[][] {
                {" 59", " 12"},
                {" 52", " 12"},
                {" 6", " 15"},
                {" 43", " "},
                {" 74", " 16",},
                {" 32", " ",},
                {" 72", " 14"},
                {" 69", " "}
        };

        String[][] actual = service.getRawStringData(is, Arrays.asList(2, 1, 0), false);
        assertRawEquals(expected, actual);
    }

    @Test(expected=PortalServiceException.class)
    public void testGetRawStringDataClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            atLeast(1).of(mockStream).close();
        }});


        service.getRawStringData(mockStream, Arrays.asList(1), true);
    }

    @Test
    public void testWriteRawData() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        double[][] newData = new double[][] {
                {0.4, 0.2, 0.5, 0.1, 0.8},
                {440.4, 230.2, 40.2, 13.45, 88.8}
        };
        ByteArrayOutputStream os = new ByteArrayOutputStream();


        service.writeRawData(is, os, newData);

        String expected = "'sample',' gold (au) ppm','something-else','',' data'\n" +
                "'0.4','0.2','0.5','0.1','0.8'\n" +
                "'440.4','230.2','40.2','13.45','88.8'\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test
    public void testWriteRawDataNoHeader() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");
        double[][] newData = new double[][] {
                {0.4, 0.2, 0.5, 0.1, 0.8},
                {440.4, 230.2, 40.2, 13.45, 88.8}
        };
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        service.writeRawData(is, os, newData);

        String expected = "'0.4','0.2','0.5','0.1','0.8'\n" +
                "'440.4','230.2','40.2','13.45','88.8'\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test
    public void testWriteRawDataColIncludes() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        double[][] newData = new double[][] {
                {0.8, 0.5},
                {13.45, 40.2}
        };
        ByteArrayOutputStream os = new ByteArrayOutputStream();


        service.writeRawData(is, os, newData, Arrays.asList(4,2), true);

        String expected = "' data','something-else'\n" +
                "'0.8','0.5'\n" +
                "'13.45','40.2'\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test
    public void testWriteRawDataExcludes() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        double[][] newData = new double[][] {
                {0.4, 0.8},
                {440.4, 88.8}
        };
        ByteArrayOutputStream os = new ByteArrayOutputStream();


        service.writeRawData(is, os, newData, Arrays.asList(1,3,2), false);

        String expected = "'sample',' data'\n" +
                "'0.4','0.8'\n" +
                "'440.4','88.8'\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test(expected=PortalServiceException.class)
    public void testWriteRawDataClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            allowing(mockOutputStream).flush();

            atLeast(1).of(mockStream).close();
            atLeast(1).of(mockOutputStream).close();
        }});

        service.writeRawData(mockStream, mockOutputStream, new double[][] {{0.4, 0.2, 0.5, 0.1, 0.8}});
    }

    @Test
    public void testColNameToIndex() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");
        Assert.assertNull(service.columnNameToIndex(is, (String) null));

        is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");
        Assert.assertNull(service.columnNameToIndex(is, "DNE"));

        is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/empty-data.csv");
        Assert.assertNull(service.columnNameToIndex(is, "DNE"));

        is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        Assert.assertNull(service.columnNameToIndex(is, "DNE"));

        is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        Assert.assertEquals((Integer) 1, service.columnNameToIndex(is, " gold (au) ppm"));

        is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        Assert.assertEquals((Integer) 4, service.columnNameToIndex(is, " data"));
    }

    @Test
    public void testColNamesToIndex() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");
        Assert.assertEquals(service.columnNameToIndex(is, Arrays.asList((String) null)), Arrays.asList((Integer) null));

        is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");
        Assert.assertEquals(service.columnNameToIndex(is, Arrays.asList("DNE")), Arrays.asList((Integer) null));

        is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/empty-data.csv");
        Assert.assertEquals(service.columnNameToIndex(is, Arrays.asList("DNE")), Arrays.asList((Integer) null));

        is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        Assert.assertEquals(service.columnNameToIndex(is, Arrays.asList("DNE")), Arrays.asList((Integer) null));

        is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        Assert.assertEquals(service.columnNameToIndex(is, Arrays.asList(" gold (au) ppm")), Arrays.asList((Integer) 1));

        is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        Assert.assertEquals(service.columnNameToIndex(is, Arrays.asList(" data")), Arrays.asList((Integer) 4));

        is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        Assert.assertEquals(service.columnNameToIndex(is, Arrays.asList(" data", (String) null, "DNE", " gold (au) ppm")),
                Arrays.asList((Integer) 4, (Integer) null, (Integer) null, (Integer) 1));
    }

    @Test(expected=PortalServiceException.class)
    public void testColNameToIndexClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            atLeast(1).of(mockStream).close();
        }});

        service.columnNameToIndex(mockStream, " data");
    }

    @Test
    public void testSwapColumn() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Assert.assertEquals(8, service.swapColumns(is, os, 0, 2));
        String expected = "' D/L',' 40','0',' ',' 12'\n" +
        "' 102',' 42','1',' 52',' 12'\n" +
        "' 103',' 16','2',' 6',' 15'\n" +
        "' 101',' 13','3',' 43',' '\n" +
        "' 103',' 16','4',' 74',' 16'\n" +
        "' 100',' 48','5',' 32',' '\n" +
        "' D/L',' 41','6',' 72',' 14'\n" +
        "' 101',' 11','7',' 69',' '\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test
    public void testSwapColumnEqualIndex() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Assert.assertEquals(8, service.swapColumns(is, os, 2, 2));
        String expected = "'0',' 40',' D/L',' ',' 12'\n" +
        "'1',' 42',' 102',' 52',' 12'\n" +
        "'2',' 16',' 103',' 6',' 15'\n" +
        "'3',' 13',' 101',' 43',' '\n" +
        "'4',' 16',' 103',' 74',' 16'\n" +
        "'5',' 48',' 100',' 32',' '\n" +
        "'6',' 41',' D/L',' 72',' 14'\n" +
        "'7',' 11',' 101',' 69',' '\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test(expected=PortalServiceException.class)
    public void testSwapColumnsClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            allowing(mockOutputStream).flush();

            atLeast(1).of(mockStream).close();
            atLeast(1).of(mockOutputStream).close();
        }});

        service.swapColumns(mockStream, mockOutputStream, 0, 2);
    }


    @Test
    public void testMergeFiles() throws Exception {
        InputStream is1 = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");
        InputStream is2 = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Assert.assertEquals(8, service.mergeFiles(is1, is2, os, Arrays.asList(0, 2, 4), Arrays.asList(3, 1)));

        String expected = "'0',' D/L',' 12',' ',' 40'\n" +
                "'1',' 102',' 12',' 52',' 42'\n" +
                "'2',' 103',' 15',' 6',' 16'\n" +
                "'3',' 101',' ',' 43',' 13'\n" +
                "'4',' 103',' 16',' 74',' 16'\n" +
                "'5',' 100',' ',' 32',' 48'\n" +
                "'6',' D/L',' 14',' 72',' 41'\n" +
                "'7',' 101',' ',' 69',' 11'\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test
    public void testMergeFilesDiffSize() throws Exception {
        InputStream is1 = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");
        InputStream is2 = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-data.csv");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Assert.assertEquals(8, service.mergeFiles(is1, is2, os, Arrays.asList(0), Arrays.asList(1)));

        String expected = "'0',' gold (au) ppm'\n" +
                          "'1','40'\n" +
                          "'2','42'\n" +
                          "'3','DL'\n" +
                          "'4','DLVal'\n" +
                          "'5','DL'\n" +
                          "'6',''\n" +
                          "'7',''\n";

        Assert.assertEquals(expected, os.toString());
        is1 = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");
        is2 = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-data.csv");

        os = new ByteArrayOutputStream();
        Assert.assertEquals(8, service.mergeFiles(is2, is1, os, Arrays.asList(1), Arrays.asList(0)));

        expected = "' gold (au) ppm','0'\n" +
                "'40','1'\n" +
                "'42','2'\n" +
                "'DL','3'\n" +
                "'DLVal','4'\n" +
                "'DL','5'\n" +
                "'','6'\n" +
                "'','7'\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test(expected=PortalServiceException.class)
    public void testMergeFilesClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            allowing(mockOutputStream).flush();

            atLeast(1).of(mockStream).close();
            atLeast(2).of(mockStream2).close();
            atLeast(1).of(mockOutputStream).close();
        }});

        service.mergeFiles(mockStream, mockStream2, mockOutputStream, null, null);
    }

    @Test
    public void testMergeFilesNoCols() throws Exception {
        InputStream is1 = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-data.csv");
        InputStream is2 = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-data.csv");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Assert.assertEquals(6, service.mergeFiles(is1, is2, os, null, null));

        String expected = "'sample',' gold (au) ppm','sample',' gold (au) ppm'\n" +
                "'40','40','40','40'\n" +
                "'','42','','42'\n" +
                "'DL','DL','DL','DL'\n" +
                "'DLVal','DLVal','DLVal','DLVal'\n" +
                "'DL','DL','DL','DL'\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test
    public void testCullRowsColIndexesExclusion() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-data.csv");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String expected = "'sample',' gold (au) ppm'\n" +
                "'40','40'\n";

        service.cullEmptyRows(is, os, Arrays.asList(1), false);
        Assert.assertEquals(expected, os.toString());
    }

    @Test
    public void testCullRowsColIndexesInclusion() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-data.csv");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String expected = "'sample',' gold (au) ppm'\n" +
                "'40','40'\n" +
                "'','42'\n";

        service.cullEmptyRows(is, os, Arrays.asList(1, 0), true);
        Assert.assertEquals(expected, os.toString());
    }

    @Test
    public void testCullRowsData() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/find-replace-data.csv");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String expected = "'sample',' gold (au) ppm'\n" +
                "'40','40'\n" +
                "'','42'\n";

        service.cullEmptyRows(is, os, null, true);
        Assert.assertEquals(expected, os.toString());
    }

    @Test
    public void testCullWhenExcludingLots() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data-noheaders.csv");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String expected = "'0',' 40',' D/L',' ',' 12'\n" +
                "'1',' 42',' 102',' 52',' 12'\n" +
                "'2',' 16',' 103',' 6',' 15'\n" +
                "'3',' 13',' 101',' 43',' '\n" +
                "'4',' 16',' 103',' 74',' 16'\n" +
                "'5',' 48',' 100',' 32',' '\n" +
                "'6',' 41',' D/L',' 72',' 14'\n" +
                "'7',' 11',' 101',' 69',' '\n";

        service.cullEmptyRows(is, os, Arrays.asList(4, 1, 2, 3), false);
        Assert.assertEquals(expected, os.toString());
    }

    @Test(expected=PortalServiceException.class)
    public void testCullEmptyClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            allowing(mockOutputStream).flush();

            atLeast(1).of(mockStream).close();
            atLeast(1).of(mockOutputStream).close();
        }});

        service.cullEmptyRows(mockStream, mockOutputStream, null, true);
    }

    @Test
    public void testScaleCols() throws Exception {
        InputStream is = ResourceUtil.loadResourceAsStream("org/auscope/portal/server/web/service/example-data.csv");
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        service.scaleColumns(is, os, Arrays.asList(2, 0), Arrays.asList(2.0, 10.0), Arrays.asList("newcol1", "newcol2"));

        String expected = "'newcol2',' gold (au) ppm','newcol1','',' data'\n" +
                "'0.0',' 40','200.0',' 59',' 12'\n" +
                "'10.0',' 42','',' 52',' 12'\n" +
                "'20.0',' 16','206.0',' 6',' 15'\n" +
                "'30.0',' 13','202.0',' 43',' '\n" +
                "'40.0',' 16','206.0',' 74',' 16'\n" +
                "'50.0',' 48','200.0',' 32',' '\n" +
                "'60.0',' 41',' D/L',' 72',' 14'\n" +
                "'70.0',' 11','202.0',' 69',' '\n";

        Assert.assertEquals(expected, os.toString());
    }

    @Test(expected=PortalServiceException.class)
    public void testScaleColsClosesStream() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockStream).read(with(any(byte[].class)), with(any(Integer.class)), with(any(Integer.class)));
            will(throwException(new IOException()));

            allowing(mockOutputStream).flush();

            atLeast(1).of(mockStream).close();
            atLeast(1).of(mockOutputStream).close();
        }});

        service.scaleColumns(mockStream, mockOutputStream, Arrays.asList(2, 0), Arrays.asList(2.0, 10.0), Arrays.asList("newcol1", "newcol2"));
    }
}

