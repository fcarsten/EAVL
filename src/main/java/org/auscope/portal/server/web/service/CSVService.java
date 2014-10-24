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

    /*private String integerToHeaderName(int index) {

    }*/

    public List<ParameterDetails> extractParameterDetails(InputStream csvData) throws PortalServiceException {
        CSVReader reader = null;
        List<ParameterDetails> details = new ArrayList<ParameterDetails>();

        try {
            reader = new CSVReader(new InputStreamReader(csvData), ',', '\'', 0);

            String[] headerLine = getNextNonEmptyRow(reader);
            if (headerLine == null) {
                return details;
            }

            //If our first line has non numeric values (and at least 1 one text value) assume it's a header
            //Otherwise assume it's data. This isn't a perfect test but it should catch all but the most ugly edge cases
            int missingCount = 0, textCount = 0, numericCount = 0;
            for (String value : headerLine) {
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

            if (numericCount == 0 && textCount > 1) {

            } else {
                //We don't have a header line (probably). Just treat this as data
                for (int i = 0; i < headerLine.length; i++) {

                }


                //applyRowToDetails(details, value);
            }


        } catch (Exception ex) {
            throw new PortalServiceException((HttpRequestBase)null, ex);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvData);
        }

        return details;
    }
}
