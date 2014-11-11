package org.auscope.portal.server.web.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.eavl.ParameterDetails;
import org.springframework.stereotype.Service;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

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

                Map<String, Integer> tValues = pd.getTextValues();
                Integer amount = tValues.get(value);
                if (amount == null) {
                    tValues.put(value, 1);
                } else {
                    tValues.put(value, amount + 1);
                }
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
        if (data == null) {
            return false;
        }

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
     * Converts and appends a data value to a list of numeric values. Appends null if the value is non numeric and includeNulls is set
     * @param data
     * @param data
     * @param index
     * @return
     */
    private Double appendValueToList(List<Double> values, String[] data, int index, boolean includeNulls) {
        try {
            Double d = new Double(Double.parseDouble(data[index]));
            values.add(d);
            return d;
        } catch (NumberFormatException ex) {
            if (includeNulls) {
                values.add(null);
            }
            return null;
        }
    }

    /**
     * Converts and appends an entire row of data to a list of rows. Appends nulls for doubles that don't parse
     * @return
     */
    private Double[] appendValuesToList(List<Double[]> rows, String[] data) {

        Double[] row = new Double[data.length];

        for (int i = 0; i < data.length; i++) {
            try {
                Double d = new Double(Double.parseDouble(data[i]));
                row[i] = d;
            } catch (NumberFormatException ex) { }
        }

        rows.add(row);
        return row;
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
        return this.getParameterValues(csvData, columnIndex, true);
    }

    /**
     * Streams through the CSV data, pulling out every numeric value as a floating point value. Null values can be optionally inserted
     * for any value that does not parse into a double.
     * Closes InputStream before returning.
     *
     * @param csvData InputStream containing CSV data. Will be closed by this method
     * @param columnIndex The column index to pull numbers from
     * @param includeMissing If true, missing values will be included as nulls. They will be skipped otherwise
     * @return
     * @throws PortalServiceException
     */
    public List<Double> getParameterValues(InputStream csvData, int columnIndex, boolean includeMissing) throws PortalServiceException {
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
                appendValueToList(values, headerLine, columnIndex, includeMissing);
            }

            String[] dataLine;
            while ((dataLine = getNextNonEmptyRow(reader)) != null) {
                appendValueToList(values, dataLine, columnIndex, includeMissing);
            }

            return values;
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to parse parameter values", ex);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvData);
        }
    }


    /**
     * Internal utility for performing the actual find/replace operation on dataLine. Returns dataLine
     */
    private String[] findReplace(String[] dataLine, int columnIndex, String find, String replace) {
        if (columnIndex >= 0 && columnIndex < dataLine.length) {
            if (find == null) {
                if (dataLine[columnIndex].trim().isEmpty()) {
                    dataLine[columnIndex] = replace;
                }
            } else {
                if (find.equals(dataLine[columnIndex])){
                    dataLine[columnIndex] = replace;
                }
            }
        }

        return dataLine;
    }

    /**
     * Streams csvData into replacedCsv data replacing all instances of find with the value replace in the specified column index
     * arbitrary Whitespace can be matched by setting find to null
     *
     * Any empty lines will be removed as part of this copying
     *
     * Closes InputStream and OutputStream before returning.
     *
     * Returns the number of lines written (including header, if any)
     *
     * @param csvData
     * @param replacedCsvData
     * @param columnIndex (if < 0, no finding/replacing will occur)
     * @param find (if null, match whitespace)
     * @param replace
     * @param forceHeaderLine Forces the creation of a header line if none exists.
     * @return
     * @throws PortalServiceException
     */
    public int findReplace(InputStream csvData, OutputStream replacedCsvData, int columnIndex, String find, String replace, boolean forceHeaderLine) throws PortalServiceException {
        CSVReader reader = null;
        CSVWriter writer = null;

        try {
            reader = new CSVReader(new InputStreamReader(csvData), ',', '\'', 0);
            writer = new CSVWriter(new OutputStreamWriter(replacedCsvData), ',', '\'');

            String[] headerLine = getNextNonEmptyRow(reader);
            if (headerLine == null) {
                return 0;
            }

            int linesWritten = 0;
            if (!isHeaderLine(headerLine) && forceHeaderLine) {
                //Insert a header line
                String[] newHeaderLine = new String[headerLine.length];
                for (int i = 0; i < headerLine.length; i++) {
                    newHeaderLine[i] = integerToHeaderName(i);
                }
                writer.writeNext(newHeaderLine);
                linesWritten++;
            }
            findReplace(headerLine, columnIndex, find, replace);
            writer.writeNext(headerLine);
            linesWritten++;

            String[] dataLine;
            while ((dataLine = getNextNonEmptyRow(reader)) != null) {
                findReplace(dataLine, columnIndex, find, replace);
                writer.writeNext(dataLine);
                linesWritten++;
            }

            return linesWritten;
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to find/replace", ex);
        } finally {
            //These can be sensitive to order (and we can't just close the readers incase we have issues generating them)
            //Ensure the writers close BEFORE we close the underlying streams
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(replacedCsvData);
            IOUtils.closeQuietly(csvData);
        }
    }

    /**
     * Streams csvData into replacedCsv data replacing all instances of find with the value replace in the specified column index
     * arbitrary Whitespace can be matched by setting find to null
     *
     * Any empty lines will be removed as part of this copying
     *
     * Closes InputStream and OutputStream before returning.
     *
     * Returns the number of lines written (including header, if any)
     *
     * @param csvData
     * @param replacedCsvData
     * @param columnIndex (if < 0, no finding/replacing will occur)
     * @param find (if null, match whitespace)
     * @param replace
     * @return
     * @throws PortalServiceException
     */
    public int findReplace(InputStream csvData, OutputStream replacedCsvData, int columnIndex, String find, String replace) throws PortalServiceException {
        return findReplace(csvData, replacedCsvData, columnIndex, find, replace, false);
    }

    /**
     * Streams CSV data from csvData and deletes the column indexes defined by "columnIndexes"
     *
     * Any empty lines will be removed as part of this copying
     *
     * Closes InputStream and OutputStream before returning.
     *
     * Returns the number of lines written (including header, if any)
     *
     * @param csvData
     * @param deletedCsvData
     * @param columnIndexes
     */
    public int deleteColumns(InputStream csvData, OutputStream deletedCsvData, Set<Integer> columnIndexes) throws PortalServiceException {
        CSVReader reader = null;
        CSVWriter writer = null;

        try {
            reader = new CSVReader(new InputStreamReader(csvData), ',', '\'', 0);
            writer = new CSVWriter(new OutputStreamWriter(deletedCsvData), ',', '\'');

            int colsToDelete = columnIndexes.size();

            String[] dataLine;
            String[] outputLine = null;
            int linesWritten = 0;
            while((dataLine = getNextNonEmptyRow(reader)) != null) {

                if (outputLine == null) {
                    outputLine = new String[dataLine.length - colsToDelete];
                }

                int j = 0;
                for (int i = 0; i < dataLine.length; i++) {
                    if (!columnIndexes.contains(i)) {
                        outputLine[j++] = dataLine[i];
                    }
                }

                writer.writeNext(outputLine);
                linesWritten++;
            }

            return linesWritten;
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to find/replace", ex);
        } finally {
            //These can be sensitive to order (and we can't just close the readers incase we have issues generating them)
            //Ensure the writers close BEFORE we close the underlying streams
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(deletedCsvData);
            IOUtils.closeQuietly(csvData);
        }
    }


    /**
     * Reads an entire CSV file into memory (in the form of a 2D double array). Missing/Invalid values will read
     * null.
     *
     * Closes InputStream before returning.
     *
     * @param csvData
     * @return
     * @throws PortalServiceException
     */
    public Double[][] getRawData(InputStream csvData) throws PortalServiceException {
        CSVReader reader = null;
        List<Double[]> rows = new ArrayList<Double[]>();

        try {
            reader = new CSVReader(new InputStreamReader(csvData), ',', '\'', 0);

            String[] headerLine = getNextNonEmptyRow(reader);
            if (headerLine == null) {
                return new Double[][] {};
            }
            int nCols = headerLine.length;

            //Initialize the parameters (one for each column)
            if (!isHeaderLine(headerLine)) {
                appendValuesToList(rows, headerLine);
            }

            String[] dataLine;
            while ((dataLine = getNextNonEmptyRow(reader)) != null) {
                appendValuesToList(rows, dataLine);
            }

            return rows.toArray(new Double[nCols][rows.size()]);
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to parse parameter values", ex);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvData);
        }
    }

    /**
     * Reads the headers of csvData into replacedCsvData and then follows it up by writing the entirety of
     * data into replacedCsvData
     *
     * Closes InputStream and OutputStream before returning.
     *
     * @param csvData
     * @return
     * @throws PortalServiceException
     */
    public void writeRawData(InputStream csvData, OutputStream replacedCsvData, double[][] data) throws PortalServiceException {
        CSVReader reader = null;
        CSVWriter writer = null;

        try {
            reader = new CSVReader(new InputStreamReader(csvData), ',', '\'', 0);
            writer = new CSVWriter(new OutputStreamWriter(replacedCsvData), ',', '\'');

            //Copy the header line (if it exists)
            String[] headerLine = getNextNonEmptyRow(reader);
            if (isHeaderLine(headerLine)) {
                writer.writeNext(headerLine);
            }

            if (data == null || data.length == 0 || data[0].length == 0) {
                return;
            }

            //Now write the body
            int ncols = data[0].length;
            for (int i = 0; i < data.length; i++) {
                String[] row = new String[ncols];
                for (int j = 0; j < ncols; j++) {
                    row[j] = Double.toString(data[i][j]);
                }
                writer.writeNext(row);
            }

            return;
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to writeRawData", ex);
        } finally {
            //These can be sensitive to order (and we can't just close the readers incase we have issues generating them)
            //Ensure the writers close BEFORE we close the underlying streams
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(replacedCsvData);
            IOUtils.closeQuietly(csvData);
        }
    }

}
