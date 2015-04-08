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

import com.google.common.primitives.Doubles;

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
        Zero,
        Text
    }

    private CSVWriter generateWriter(OutputStream os) {
        return new CSVWriter(new OutputStreamWriter(os), ',', '\'');
    }

    private CSVReader generateReader(InputStream is, int startLine) {
        return new CSVReader(new InputStreamReader(is), ',', '\'', startLine);
    }

    private CSVReader generateReader(InputStream is) {
        return generateReader(is, 0);
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
            reader = generateReader(csvData, startLine);

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
            reader = generateReader(csvData);
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
            reader = generateReader(csvData);

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
            if (Double.parseDouble(trimmed) == 0) {
                return DataValueType.Zero;
            }

            return DataValueType.Numeric;
        } catch (NumberFormatException ex) {
            return DataValueType.Text;
        }
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
            case Zero:
                pd.setTotalZeroes(pd.getTotalZeroes() + 1);
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
            reader = generateReader(csvData);

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
    private double appendNumericValueToList(List<Double> values, String[] data, int index, boolean includeNulls) {
        try {
            Double d =Double.parseDouble(data[index]);
            values.add(d);
            return d;
        } catch (NumberFormatException ex) {
            if (includeNulls) {
                values.add(Double.NaN);
            }
            return Double.NaN;
        }
    }

    /**
     * Converts and appends an entire row of data to a list of rows. Appends nulls for doubles that don't parse
     * @return
     */
    private double[] appendNumericValuesToList(List<double[]> rows, String[] data, boolean skipEmptyLines) {

        double[] row = new double[data.length];
        boolean hasValues = false;
        for (int i = 0; i < data.length; i++) {
            try {
                row[i] = Double.parseDouble(data[i]);
                hasValues = true;
            } catch (NumberFormatException ex) {
                row[i]=Double.NaN;
            }
        }

        if (skipEmptyLines && !hasValues) {
            return null;
        }
        if (rows != null) {
            rows.add(row);
        }
        return row;
    }

    /**
     * Converts and appends a subset of a row of data to a list of rows. Appends nulls for doubles that don't parse
     * @return
     */
    private double[] appendNumericValuesToList(List<double[]> rows, String[] data, List<Integer> columnIndexes, boolean includeColumnIndexes, boolean skipEmptyLines) {
        double[] row = null;

        if (includeColumnIndexes) {
            boolean hasValues = false;
            row = new double[columnIndexes.size()];
            for (int i = 0; i < row.length; i++) {
                try {
                    row[i] = Double.parseDouble(data[columnIndexes.get(i)]);
                    hasValues = true;
                } catch (NumberFormatException ex) {
                    row[i]=Double.NaN;
                }
            }

            if (skipEmptyLines && !hasValues) {
                row = null;
            }
        } else {
            boolean hasValues = false;
            row = new double[data.length - columnIndexes.size()];
            int idx = 0;

            for (int i = 0; i < data.length; i++) {
                if (columnIndexes.contains(i)) {
                    continue;
                }

                try {
                    double d = Double.parseDouble(data[i]);
                    row[idx++] = d;
                    hasValues = true;
                } catch (NumberFormatException ex) {
                    row[idx++]=Double.NaN;

                }
            }

            if (skipEmptyLines && !hasValues) {
                row = null;
            }
        }

        if (rows != null && row!=null) {
            rows.add(row);
        }
        return row;
    }

    /**
     * Converts and appends an entire row of data to a list of rows.
     * @return
     */
    private String[] appendValuesToList(List<String[]> rows, String[] data) {
        rows.add(data);
        return data;
    }

    /**
     * Converts and appends a subset of a row of data to a list of rows.
     * @return
     */
    private String[] appendValuesToList(List<String[]> rows, String[] data, List<Integer> columnIndexes, boolean includeColumnIndexes) {
        String[] row = null;
        if (includeColumnIndexes) {
            row = new String[columnIndexes.size()];

            for (int i = 0; i < row.length; i++) {
                row[i] = data[columnIndexes.get(i)];
            }

        } else {
            row = new String[data.length - columnIndexes.size()];
            int idx = 0;

            for (int i = 0; i < data.length; i++) {
                if (columnIndexes.contains(i)) {
                    continue;
                }

                row[idx++] = data[i];
            }
        }

        if (rows != null) {
            rows.add(row);
        }
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
    public double[] getParameterValues(InputStream csvData, int columnIndex) throws PortalServiceException {
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
    public double[] getParameterValues(InputStream csvData, int columnIndex, boolean includeMissing) throws PortalServiceException {
        CSVReader reader = null;
        List<Double> values = new ArrayList<Double>();

        try {
            reader = generateReader(csvData);

            String[] headerLine = getNextNonEmptyRow(reader);
            if (headerLine == null) {
                return new double[0];
            }

            //Initialize the parameters (one for each column)
            if (!isHeaderLine(headerLine)) {
                appendNumericValueToList(values, headerLine, columnIndex, includeMissing);
            }

            String[] dataLine;
            while ((dataLine = getNextNonEmptyRow(reader)) != null) {
                appendNumericValueToList(values, dataLine, columnIndex, includeMissing);
            }

            return Doubles.toArray(values);
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to parse parameter values", ex);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvData);
        }
    }

    /**
     * Internal utility for performing the actual find/replace operation on dataLine. Returns dataLine
     * @param find String to match. If null either emptyString or "0" is matched depending on matchZero
     * @param matchZero only applicable if find is null.
     */
    private String[] findReplace(String[] dataLine, int columnIndex, String find, boolean matchZero, String replace) {
        if (columnIndex >= 0 && columnIndex < dataLine.length) {
            if (find == null && !matchZero) {
                //Match empty
                if (dataLine[columnIndex].trim().isEmpty()) {
                    dataLine[columnIndex] = replace;
                }
            } else if (find == null && matchZero) {
                //Match zero
                try {
                    if (Double.parseDouble(dataLine[columnIndex].trim()) == 0) {
                        dataLine[columnIndex] = replace;
                    }
                } catch (NumberFormatException ex) { }
            } else {
                //Match find string
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
        return findReplace(csvData, replacedCsvData, columnIndex, find, false, replace, forceHeaderLine);
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
    private int findReplace(InputStream csvData, OutputStream replacedCsvData, int columnIndex, String find, boolean matchZeroes, String replace, boolean forceHeaderLine) throws PortalServiceException {
        CSVReader reader = null;
        CSVWriter writer = null;

        try {
            reader = generateReader(csvData);
            writer = generateWriter(replacedCsvData);

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
            findReplace(headerLine, columnIndex, find, matchZeroes, replace);
            writer.writeNext(headerLine);
            linesWritten++;

            String[] dataLine;
            while ((dataLine = getNextNonEmptyRow(reader)) != null) {
                findReplace(dataLine, columnIndex, find, matchZeroes, replace);
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
     * Streams csvData into replacedCsv data replacing all values that parse to Double "0" with the value replace in the specified column index
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
    public int findReplaceZeroes(InputStream csvData, OutputStream replacedCsvData, int columnIndex, String replace, boolean forceHeaderLine) throws PortalServiceException {
        return findReplace(csvData, replacedCsvData, columnIndex, null, true, replace, forceHeaderLine);
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
            reader = generateReader(csvData);
            writer = generateWriter(deletedCsvData);

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
     * null. Header line (if it exists) will be skipped
     *
     * Closes InputStream before returning.
     *
     * @param csvData
     * @return
     * @throws PortalServiceException
     */
    public double[][] getRawData(InputStream csvData) throws PortalServiceException {
        return getRawData(csvData, null, true, false);
    }

    /**
     * Reads an entire CSV file into memory (in the form of a 2D double array). Missing/Invalid values will read
     * null. Header line (if it exists) will be skipped
     *
     * Closes InputStream before returning.
     *
     * @param columnIndexes What column indexes to read
     * @return
     * @throws PortalServiceException
     */
    public double[][] getRawData(InputStream csvData, List<Integer> columnIndexes) throws PortalServiceException {
        return getRawData(csvData, columnIndexes, true, false);
    }

    /**
     * Reads a subset of entire CSV file into memory (in the form of a 2D double array). Missing/Invalid values will read
     * null. Header line (if it exists) will be skipped
     *
     * Closes InputStream before returning.
     *
     * @param csvData
     * @param columnIndexes What column indexes to read (columnIndex[x] will output as column x) if includeColumnIndexes is true. What columns to exclude otherwise.. If null, all will be read.
     * @param includeColumnIndexes If true, columnIndexes will refer to columns to extract. If false, columnIndexes will refer to columns to exclude
     * @return
     * @throws PortalServiceException
     */
    public double[][] getRawData(InputStream csvData, List<Integer> columnIndexes, boolean includeColumnIndexes) throws PortalServiceException {
        return getRawData(csvData, columnIndexes, includeColumnIndexes, false);
    }

    /**
     * Reads a subset of entire CSV file into memory (in the form of a 2D double array). Missing/Invalid values will read
     * null. Header line (if it exists) will be skipped
     *
     * Closes InputStream before returning.
     *
     * @param csvData
     * @param columnIndexes What column indexes to read (columnIndex[x] will output as column x) if includeColumnIndexes is true. What columns to exclude otherwise.. If null, all will be read.
     * @param includeColumnIndexes If true, columnIndexes will refer to columns to extract. If false, columnIndexes will refer to columns to exclude
     * @param skipEmptyLines If true, any line reading as all nulls (all nulls for the returned columns) will NOT be included
     * @return
     * @throws PortalServiceException
     */
    public double[][] getRawData(InputStream csvData, List<Integer> columnIndexes, boolean includeColumnIndexes, boolean skipEmptyLines) throws PortalServiceException {
        CSVReader reader = null;
        List<double[]> rows = new ArrayList<double[]>();

        try {
            reader = generateReader(csvData);

            String[] headerLine = getNextNonEmptyRow(reader);
            if (headerLine == null) {
                return new double[][] {};
            }
            int nCols = columnIndexes == null ? headerLine.length : (includeColumnIndexes ? columnIndexes.size() : headerLine.length - columnIndexes.size());

            //Initialize the parameters (one for each column)
            if (!isHeaderLine(headerLine)) {
                if (columnIndexes == null) {
                    appendNumericValuesToList(rows, headerLine, skipEmptyLines);
                } else {
                    appendNumericValuesToList(rows, headerLine, columnIndexes, includeColumnIndexes, skipEmptyLines);
                }

            }

            String[] dataLine;
            while ((dataLine = getNextNonEmptyRow(reader)) != null) {
                if (columnIndexes == null) {
                    appendNumericValuesToList(rows, dataLine, skipEmptyLines);
                } else {
                    appendNumericValuesToList(rows, dataLine, columnIndexes, includeColumnIndexes, skipEmptyLines);
                }
            }

            return rows.toArray(new double[nCols][rows.size()]);
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to parse parameter values", ex);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvData);
        }
    }

    /**
     * Reads a subset of entire CSV file into memory (in the form of a 2D double array). Header line (if it exists) will be skipped
     *
     * Closes InputStream before returning.
     *
     * @param csvData
     * @param columnIndexes What column indexes to read (columnIndex[x] will output as column x) if includeColumnIndexes is true. What columns to exclude otherwise.. If null, all will be read.
     * @param includeColumnIndexes If true, columnIndexes will refer to columns to extract. If false, columnIndexes will refer to columns to exclude
     * @return
     * @throws PortalServiceException
     */
    public String[][] getRawStringData(InputStream csvData, List<Integer> columnIndexes, boolean includeColumnIndexes) throws PortalServiceException {
        CSVReader reader = null;
        List<String[]> rows = new ArrayList<String[]>();

        try {
            reader = generateReader(csvData);

            String[] headerLine = getNextNonEmptyRow(reader);
            if (headerLine == null) {
                return new String[][] {};
            }
            int nCols = columnIndexes == null ? headerLine.length : columnIndexes.size();

            //Initialize the parameters (one for each column)
            if (!isHeaderLine(headerLine)) {
                if (columnIndexes == null) {
                    appendValuesToList(rows, headerLine);
                } else {
                    appendValuesToList(rows, headerLine, columnIndexes, includeColumnIndexes);
                }

            }

            String[] dataLine;
            while ((dataLine = getNextNonEmptyRow(reader)) != null) {
                if (columnIndexes == null) {
                    appendValuesToList(rows, dataLine);
                } else {
                    appendValuesToList(rows, dataLine, columnIndexes, includeColumnIndexes);
                }
            }

            return rows.toArray(new String[nCols][rows.size()]);
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
     * @param headerColIndexes What column indexes to read (columnIndex[x] will output as column x) if includeColumnIndexes is true. What columns to exclude otherwise.. If null, all will be read.
     * @param includeHeaderColIndexes If true, headerColIndexes will refer to columns to extract. If false, headerColIndexes will refer to columns to exclude
     * @return
     * @throws PortalServiceException
     */
    public void writeRawData(InputStream csvData, OutputStream replacedCsvData, double[][] data) throws PortalServiceException {
    	writeRawData(csvData, replacedCsvData, data, null, true);
    }

    /**
     * Reads the headers of csvData into replacedCsvData and then follows it up by writing the entirety of
     * data into replacedCsvData
     *
     * Closes InputStream and OutputStream before returning.
     *
     * @param csvData
     * @param headerColIndexes What column indexes to read (columnIndex[x] will output as column x) if includeColumnIndexes is true. What columns to exclude otherwise.. If null, all will be read.
     * @param includeHeaderColIndexes If true, headerColIndexes will refer to columns to extract. If false, headerColIndexes will refer to columns to exclude
     * @return
     * @throws PortalServiceException
     */
    public void writeRawData(InputStream csvData, OutputStream replacedCsvData, double[][] data, List<Integer> headerColIndexes, boolean includeHeaderColIndexes) throws PortalServiceException {
        CSVReader reader = null;
        CSVWriter writer = null;

        try {
            reader = generateReader(csvData);
            writer = generateWriter(replacedCsvData);

            //Copy the header line (if it exists)
            String[] headerLine = getNextNonEmptyRow(reader);
            if (isHeaderLine(headerLine)) {
            	if (headerColIndexes == null) {
            		writer.writeNext(headerLine);
            	} else {
            		if (includeHeaderColIndexes) {
                        String[] newHeader = new String[headerColIndexes.size()];
                        for (int i = 0; i < newHeader.length; i++) {
                        	newHeader[i] = headerLine[headerColIndexes.get(i)];
                        }
                        writer.writeNext(newHeader);
                    } else {
                    	String[] newHeader = new String[headerLine.length - headerColIndexes.size()];
                        int idx = 0;
                        for (int i = 0; i < headerLine.length; i++) {
                            if (headerColIndexes.contains(i)) {
                                continue;
                            }

                            newHeader[idx++] = headerLine[i];
                        }
                        writer.writeNext(newHeader);
                    }
            	}
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

    /**
     * Attempts to take a column header name to find out what the
     * numerical (0 based) index is. Returns null if the name DNE or
     * this dataset doesn't have a header row
     *
     * Closes InputStream before returning.
     *
     * @param csvData input CSV data
     * @param name The column header name
     * @return
     */
    public Integer columnNameToIndex(InputStream csvData, String name) throws PortalServiceException {
        CSVReader reader = null;

        try {
            reader = generateReader(csvData);

            //Copy the header line (if it exists)
            String[] headerLine = getNextNonEmptyRow(reader);
            if (!isHeaderLine(headerLine)) {
                return null;
            }

            for (int i = 0; i < headerLine.length; i++) {
                if (headerLine[i].equals(name)) {
                    return i;
                }
            }

            return null;
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to nameToIndex for name:" + name, ex);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvData);
        }
    }

    /**
     * Attempts to take a list of column header names to find out what the
     * numerical (0 based) index of each one is. Returns a list of indexes in
     * a list corresponding 1-1 with the specified names
     *
     * The return value will contain null elements if entries in column names couldn't be
     * found.
     *
     * Closes InputStream before returning.
     *
     * @param csvData input CSV data
     * @param name The column header name
     * @return
     */
    public List<Integer> columnNameToIndex(InputStream csvData, List<String> names) throws PortalServiceException {
        CSVReader reader = null;

        try {
            reader = generateReader(csvData);

            //Copy the header line (if it exists)
            String[] headerLine = getNextNonEmptyRow(reader);
            boolean isHeader = isHeaderLine(headerLine);

            List<Integer> indexes = new ArrayList<Integer>(names.size());
            for (String name : names) {
                boolean found = false;
                if (isHeader) {
                    for (int i = 0; i < headerLine.length; i++) {
                        if (headerLine[i].equals(name)) {
                            indexes.add(i);
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    indexes.add(null);
                }
            }

            return indexes;
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to nameToIndex for names:" + names, ex);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvData);
        }
    }


    /**
     * Streams CSV data from csvData and swaps the columns at index1 and index2.
     *
     * Any empty lines will be removed as part of this copying.
     *
     * Closes InputStream and OutputStream before returning.
     *
     * Returns the number of lines written (including header, if any)
     *
     * @param csvData
     * @param deletedCsvData
     * @param index1 Will be replaced with column data at index2
     * @param index2 Will be replaced with column data at index1
     */
    public int swapColumns(InputStream csvData, OutputStream deletedCsvData, int index1, int index2) throws PortalServiceException {
        CSVReader reader = null;
        CSVWriter writer = null;

        try {
            reader = generateReader(csvData);
            writer = generateWriter(deletedCsvData);

            String[] dataLine;
            int linesWritten = 0;
            while((dataLine = getNextNonEmptyRow(reader)) != null) {

                String swap = dataLine[index1];
                dataLine[index1] = dataLine[index2];
                dataLine[index2] = swap;

                writer.writeNext(dataLine);
                linesWritten++;
            }

            return linesWritten;
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to swap columns", ex);
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
     * Streams CSV data from in1 and in2 and merges the streams column by column. That is if
     * in1 contains columns A B and in2 contains C D the output file will read A B C D
     *
     * If the number of lines varies between the in1 and in2, nulls will be inserted
     *
     * Any empty lines will be removed as part of this copying.
     *
     * Closes all InputStreams and OutputStream before returning.
     *
     * Returns the number of lines written (including header, if any)
     *
     * @param in1 The first input CSV stream (will be closed)
     * @param in2 The second input CSV stream (will be closed)
     * @param mergedCsvData Will receive merged output CSV stream.
     * @param in1Columns The subset of column indexes to copy from in1
     * @param in2Columns The subset of column indexes to copy from in2
     *
     */
    public int mergeFiles(InputStream in1, InputStream in2, OutputStream mergedCsvData, List<Integer> in1Columns, List<Integer> in2Columns) throws PortalServiceException {
        CSVReader reader1 = null;
        CSVReader reader2 = null;
        CSVWriter writer = null;

        try {
            reader1 = generateReader(in1);
            reader2 = generateReader(in2);
            writer = generateWriter(mergedCsvData);

            String[] dataLine1 = getNextNonEmptyRow(reader1);
            String[] dataLine2 = getNextNonEmptyRow(reader2);
            String[] outputLine = null;
            int linesWritten = 0;
            int in1Cols = 0, in2Cols = 0;
            while(dataLine1 != null || dataLine2 != null) {

                if (outputLine == null) {
                    in1Cols = (in1Columns == null ? dataLine1.length : in1Columns.size());
                    in2Cols = (in2Columns == null ? dataLine2.length : in2Columns.size());
                    outputLine = new String[in1Cols + in2Cols];
                }

                //Copy across in1
                int outIdx = 0;
                if (dataLine1 != null) {
                    if (in1Columns == null) {
                        for (int i = 0; i < dataLine1.length; i++) {
                            outputLine[outIdx++] = dataLine1[i];
                        }
                    } else {
                        for (Integer i : in1Columns) {
                            outputLine[outIdx++] = dataLine1[i];
                        }
                    }
                } else {
                    for (int i = 0; i < in1Cols; i++) {
                        outputLine[outIdx++] = "";
                    }
                }


                if (dataLine2 != null) {
                    if (in2Columns == null) {
                        for (int i = 0; i < dataLine2.length; i++) {
                            outputLine[outIdx++] = dataLine2[i];
                        }
                    } else {
                        for (Integer i : in2Columns) {
                            outputLine[outIdx++] = dataLine2[i];
                        }
                    }
                } else {
                    for (int i = 0; i < in2Cols; i++) {
                        outputLine[outIdx++] = "";
                    }
                }

                writer.writeNext(outputLine);
                linesWritten++;

                dataLine1 = getNextNonEmptyRow(reader1);
                dataLine2 = getNextNonEmptyRow(reader2);
            }

            return linesWritten;
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to merge files", ex);
        } finally {
            //These can be sensitive to order (and we can't just close the readers incase we have issues generating them)
            //Ensure the writers close BEFORE we close the underlying streams
            IOUtils.closeQuietly(reader1);
            IOUtils.closeQuietly(reader2);
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(mergedCsvData);
            IOUtils.closeQuietly(in1);
            IOUtils.closeQuietly(in2);
        }
    }

    /**
     * Reads through CSV data and pipes it straight to cullCsvData. Any (non header) row that
     * contains only nulls will NOT be piped to culledCsvData
     *
     * Irregardless of what columnIndexes are specified (if any), all columns will be still written.
     *
     * Closes InputStream/OutputStream before returning.
     *
     * @param csvData
     * @param columnIndexes What column indexes to read when checking for nulls.. If null, all will be read.
     * @param includeColumnIndexes If true, columnIndexes will refer to columns to read. If false, columnIndexes will refer to columns to exclude
     * @return
     * @throws PortalServiceException
     */
    public void cullEmptyRows(InputStream csvData, OutputStream culledCsvData, List<Integer> columnIndexes, boolean includeColumnIndexes) throws PortalServiceException {
        CSVReader reader = null;
        CSVWriter writer = null;

        try {
            reader = generateReader(csvData);
            writer = generateWriter(culledCsvData);

            String[] headerLine = getNextNonEmptyRow(reader);
            if (headerLine == null) {
                return;
            }
            //Initialize the parameters (one for each column)
            if (isHeaderLine(headerLine)) {
                writer.writeNext(headerLine);
            } else {
                if (columnIndexes == null) {
                    if (appendNumericValuesToList(null, headerLine, true) != null) {
                        writer.writeNext(headerLine);
                    }
                } else {
                    if (appendNumericValuesToList(null, headerLine, columnIndexes, includeColumnIndexes, true) != null) {
                        writer.writeNext(headerLine);
                    }
                }
            }

            String[] dataLine;
            while ((dataLine = getNextNonEmptyRow(reader)) != null) {
                if (columnIndexes == null) {
                    if (appendNumericValuesToList(null, dataLine, true) != null) {
                        writer.writeNext(dataLine);
                    }
                } else {
                    if (appendNumericValuesToList(null, dataLine, columnIndexes, includeColumnIndexes, true) != null) {
                        writer.writeNext(dataLine);
                    }
                }
            }

        } catch (Exception ex) {
            throw new PortalServiceException("Unable to cull empty lines", ex);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(culledCsvData);
            IOUtils.closeQuietly(csvData);
        }
    }

    /**
     * Stream csvData into scaledData. Each column whose index is in columnIndexes
     * will have their numerical values multiplied by the corresponding value in scalingFactors (if non null)
     * and header replaced with the name in newColumnNames (if non null)
     *
     * There must be a 1-1-1 correspondence between values in columnIndexes, scalingFactors and newColumnNames
     *
     * Closes InputStream/OutputStream before returning.
     *
     * @param csvData Wi
     * @param scaledData
     * @param columnIndexes
     * @param scalingFactors
     * @param newColumnNames
     * @throws PortalServiceException
     */
    public void scaleColumns(InputStream csvData, OutputStream scaledData, List<Integer> columnIndexes, List<Double> scalingFactors, List<String> newColumnNames) throws PortalServiceException {
       CSVReader reader = null;
       CSVWriter writer = null;

       try {
           if (columnIndexes.size() != scalingFactors.size() || columnIndexes.size() != newColumnNames.size()) {
               throw new IllegalArgumentException();
          }

           reader = generateReader(csvData);
           writer = generateWriter(scaledData);

           String[] dataLine = getNextNonEmptyRow(reader);
           if (dataLine == null) {
               return;
           }

           //Rewrite header line
           if (isHeaderLine(dataLine)) {
               for (int i = 0; i < columnIndexes.size(); i++) {
                   String newName = newColumnNames.get(i);
                   if (newName != null && !newName.isEmpty()) {
                       dataLine[columnIndexes.get(i)] = newName;
                   }
               }

               //Read until we hit a data line
               writer.writeNext(dataLine);
               dataLine = getNextNonEmptyRow(reader);
               if (dataLine == null) {
                   return;
               }
           }

           do {
               //scale our numeric values
               for (int i = 0; i < columnIndexes.size(); i++) {
                   Double sf = scalingFactors.get(i);
                   if (sf == null) {
                       continue;
                   }

                   int index = columnIndexes.get(i);
                   try {
                       double unscaledValue = Double.parseDouble(dataLine[index].trim());
                       dataLine[index] = Double.toString(unscaledValue * sf);
                   } catch (NumberFormatException ex) {
                       continue;
                   }
               }

               writer.writeNext(dataLine);
           } while ((dataLine = getNextNonEmptyRow(reader)) != null);
       } catch (Exception ex) {
           throw new PortalServiceException("Unable to scale columns ", ex);
       } finally {
           IOUtils.closeQuietly(reader);
           IOUtils.closeQuietly(writer);
           IOUtils.closeQuietly(scaledData);
           IOUtils.closeQuietly(csvData);
       }
    }
}
