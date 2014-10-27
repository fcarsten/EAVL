package org.auscope.portal.server.eavl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ParameterDetails implements Serializable {
    /** Unique name of this parameter */
    private String name;
    /** Total number of numeric values in this parameter*/
    private int totalNumeric;
    /** Total number of missing values in this parameter (null or whitespace only strings)*/
    private int totalMissing;
    /** Total number of values that are NOT parseable as a number and are non empty*/
    private int totalText;
    /** A map of every non numeric value (including null string) and their associated counts.*/
    private Map<String, Integer> textValues;
    /** The index of the column in the CSV file (0 based)*/
    private int columnIndex;
    /**
     * @param name Unique name of this parameter
     * @param columnIndex The index of the column in the CSV file (0 based)
     */
    public ParameterDetails(String name, int columnIndex) {
        this.name = name;
        this.columnIndex = columnIndex;
        this.textValues = new HashMap<String, Integer>();
    }

    /**
     * Unique name of this parameter
     * @return
     */
    public String getName() {
        return name;
    }
    /**
     * Unique name of this parameter
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Total number of numeric values in this parameter
     * @return
     */
    public int getTotalNumeric() {
        return totalNumeric;
    }
    /**
     * Total number of numeric values in this parameter
     * @param totalNumeric
     */
    public void setTotalNumeric(int totalNumeric) {
        this.totalNumeric = totalNumeric;
    }
    /**
     * Total number of missing values in this parameter (null or whitespace only strings)
     * @return
     */
    public int getTotalMissing() {
        return totalMissing;
    }
    /**
     * Total number of missing values in this parameter (null or whitespace only strings)
     * @param totalMissing
     */
    public void setTotalMissing(int totalMissing) {
        this.totalMissing = totalMissing;
    }
    /**
     * Total number of values that are NOT parseable as a number and are non empty
     * @return
     */
    public int getTotalText() {
        return totalText;
    }
    /**
     * Total number of values that are NOT parseable as a number and are non empty
     * @param totalText
     */
    public void setTotalText(int totalText) {
        this.totalText = totalText;
    }
    /**
     * A map of every non numeric value (including null string) and their associated counts.
     * @return
     */
    public Map<String, Integer> getTextValues() {
        return textValues;
    }
    /**
     * A map of every non numeric value (including null string) and their associated counts.
     * @param textValues
     */
    public void setTextValues(Map<String, Integer> textValues) {
        this.textValues = textValues;
    }

    /** The index of the column in the CSV file*/
    public int getColumnIndex() {
        return columnIndex;
    }

    /** The index of the column in the CSV file*/
    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    @Override
    public String toString() {
        return "ParameterDetails [name=" + name + "]";
    }
}
