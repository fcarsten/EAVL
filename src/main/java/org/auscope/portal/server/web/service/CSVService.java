package org.auscope.portal.server.web.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.eavl.ParameterDetails;
import org.springframework.stereotype.Service;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Service for reading/writing subsets of a CSV file
 * @author Josh Vote
 *
 */
@Service
public class CSVService {

    private enum DataValueType {
        Missing,
        Numeric,
        Text
    }

    /**
     * Reads up to "maximum" lines starting from line number "startLine"
     *
     * Closes InputStream before returning
     *
     * @param startLine 0 based starting line index
     * @param maximum Maximum number of lines to read
     * @return
     * @throws IOException
     */
    public List<String[]> readLines(InputStream csvData, int startLine, int maximum) throws PortalServiceException {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new InputStreamReader(csvData), ',', '\'', startLine);

            List<String[]> lines = new ArrayList<String[]>(maximum);
            String[] nextLine;
            while (lines.size() < maximum && (nextLine = reader.readNext()) != null) {
                lines.add(nextLine);
            }

            return lines;
        } catch (Exception ex) {
            throw new PortalServiceException((HttpRequestBase)null, ex);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvData);
        }
    }

    /**
     * Reads lines, incrementing a counter for every line read.
     *
     * Closes InputStream before returning
     *
     * @param startLine 0 based starting line index
     * @param maximum Maximum number of lines to read
     * @return
     * @throws PortalServiceException
     * @throws IOException
     */
    public int countLines(InputStream csvData) throws PortalServiceException {
        CSVReader reader = null;
        int count = 0;
        try {
            reader = new CSVReader(new InputStreamReader(csvData), ',', '\'');
            while (reader.readNext() != null) {
                count++;
            }

            return count;
        } catch (Exception ex) {
            throw new PortalServiceException((HttpRequestBase)null, ex);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvData);
        }
    }

    /**
     * Reads into the CSV data until it finds a non whitespace line and returns the number of columns for that line
     *
     * Returns 0 if the CSV file is empty
     *
     * Closes InputStream before returning.
     *
     * @param csvData
     * @return
     * @throws IOException
     */
    public int estimateColumnCount(InputStream csvData) throws PortalServiceException {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new InputStreamReader(csvData), ',', '\'', 0);

            String[] nextLine = getNextNonEmptyRow(reader);
            if (nextLine != null) {
                return nextLine.length;
            }

            return 0; //Empty CSV files will return 0 columns
        } catch (Exception ex) {
            throw new PortalServiceException((HttpRequestBase)null, ex);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvData);
        }
    }

    /**
     * Reads the next non empty row (that is a row with no values, not even null values). returns null if no such row exists
     * @param reader
     * @return
     * @throws IOException
     */
    private String[] getNextNonEmptyRow(CSVReader reader) throws IOException {
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length == 1) {
                String data = nextLine[0];
                if (data.trim().length() > 0) {
                    return nextLine;
                }
            } else {
                return nextLine;
            }
        }
        return null;
    }

    /**
     * Turns a raw value from CSV into a DataValueType enum
     * @param dataValue
     * @return
     */
    private DataValueType getDataValueType(String dataValue) {
        String trimmed = dataValue.trim();

        if (trimmed.length() == 0) {
            return DataValueType.Missing;
        }

        try {
            Float.parseFloat(trimmed);
        } catch (NumberFormatException ex) {
            return DataValueType.Text;
        }

        return DataValueType.Numeric;
    }

    /**
     * Given a row of values, merge the stats into 1-1 corresponding ParameterDetails object
     *
     * @param details
     * @param dataValues
     * @return True if the stats are applied. False if the row was ignored
     */
    private boolean applyRowToDetails(List<ParameterDetails> details, String[] dataValues) {
        if (dataValues.length != details.size()) {
            return false;
        }

        for (int i = 0; i < dataValues.length; i++) {
            ParameterDetails pd = details.get(i);
            String value = dataValues[i];

            switch (getDataValueType(value)) {
            case Missing:
                pd.setTotalMissing(pd.getTotalMissing() + 1);
                break;
            case Numeric:
                pd.setTotalNumeric(pd.getTotalNumeric() + 1);
                break;
            case Text:
                pd.setTotalText(pd.getTotalText() + 1);
                pd.getTextValues().add(value.trim());
                break;
            }
        }

        return true;
    }

    /**
     * Utility for generating a column header based on a number in a spreadsheet format:
     *
     * eg: Columns read A, B, C ... Y, Z, AA, AB, AC ...
     * @param index 0 based index
     * @return
     */
    private String integerToHeaderName(int index) {
        String name = "";
        while(index >= 0) {
            name += (char) ('A' + (index % 26));
            index -= 26;
        }
        return name;
    }

    /**
     * Returns true if a line of data could be considered a header line (if it's at the top of the CSV file)
     * @param data
     * @return
     */
    private boolean isHeaderLine(String[] data) {
        //If our first line has non numeric values (and at least 1 one text value) assume it's a header
        //Otherwise assume it's data. This isn't a perfect test but it should catch all but the most ugly edge cases
        int missingCount = 0, textCount = 0, numericCount = 0;
        for (String value : data) {
            switch(getDataValueType(value)) {
            case Missing:
                missingCount++;
                break;
            case Numeric:
                numericCount++;
                break;
            case Text:
                textCount++;
                break;
            }
        }

        return numericCount == 0 && textCount > 1;
    }

    /**
     * Iterates through the entire CSV dataset generating statistics on each column. Each column will
     * be treated as an independent parameter. Returns the statistics as a list of "ParameterDetails"
     * objects.
     *
     * Returns 0 if the CSV file is empty
     *
     * Closes InputStream before returning.
     *
     * @param csvData InputStream containing CSV data. Will be closed by this method
     * @return
     * @throws PortalServiceException
     */
    public List<ParameterDetails> extractParameterDetails(InputStream csvData) throws PortalServiceException {
        CSVReader reader = null;
        List<ParameterDetails> details = new ArrayList<ParameterDetails>();

        try {
            reader = new CSVReader(new InputStreamReader(csvData), ',', '\'', 0);

            String[] headerLine = getNextNonEmptyRow(reader);
            if (headerLine == null) {
                return details;
            }

            //Initialize the parameters (one for each column)
            if (isHeaderLine(headerLine)) {
                //It looks like we have a header line, let's try and parse it
                for (int i = 0; i < headerLine.length; i++) {
                    if (headerLine[i].trim().isEmpty()) {
                        details.add(new ParameterDetails(integerToHeaderName(i), i));
                    } else {
                        details.add(new ParameterDetails(headerLine[i].trim(), i));
                    }
                }
            } else {
                //We don't have a header line (probably). This row will be data.
                //Instead autogenerate details
                for (int i = 0; i < headerLine.length; i++) {
                    details.add(new ParameterDetails(integerToHeaderName(i), i));
                }
                applyRowToDetails(details, headerLine); // Make sure we don't forget to treat this line as data
            }

            String[] dataLine;
            while ((dataLine = getNextNonEmptyRow(reader)) != null) {
                applyRowToDetails(details, dataLine);
            }
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to parse Parameter Details", ex);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvData);
        }

        return details;
    }

    /**
     * Converts and appends a data value to a list of numeric values. Appends null if the value is non numeric
     * @param data
     * @param data
     * @param index
     * @return
     */
    private void appendValueToList(List<Double> values, String[] data, int index) {
        try {
            values.add(new Double(Double.parseDouble(data[index])));
        } catch (NumberFormatException ex) {
            values.add(null);
        }
    }

    /**
     * Streams through the CSV data, pulling out every numeric value as a floating point value. Null values will be inserted
     * for any value that does not parse into a double.
     * Closes InputStream before returning.
     *
     * @param csvData InputStream containing CSV data. Will be closed by this method
     * @param columnIndex The column index to pull numbers from
     * @return
     * @throws PortalServiceException
     */
    public List<Double> getParameterValues(InputStream csvData, int columnIndex) throws PortalServiceException {
        CSVReader reader = null;
        List<Double> values = new ArrayList<Double>();

        try {
            reader = new CSVReader(new InputStreamReader(csvData), ',', '\'', 0);

            String[] headerLine = getNextNonEmptyRow(reader);
            if (headerLine == null) {
                return values;
            }

            //Initialize the parameters (one for each column)
            if (!isHeaderLine(headerLine)) {
                appendValueToList(values, headerLine, columnIndex);
            }

            String[] dataLine;
            while ((dataLine = getNextNonEmptyRow(reader)) != null) {
                appendValueToList(values, dataLine, columnIndex);
            }

            return values;
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to parse parameter values", ex);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvData);
        }
    }
}
