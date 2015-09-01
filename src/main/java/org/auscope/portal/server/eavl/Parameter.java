package org.auscope.portal.server.eavl;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Represents a Parameter (column) in a CSV file
 * @author Josh Vote (CSIRO)
 *
 */
@Entity
public class Parameter implements Serializable {
    private static final long serialVersionUID = -5683798392250663819L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Basic
    private String name;
    @Basic
    private Integer originalIndex;

    public Parameter() {
    }

    public Parameter(String name, Integer originalIndex) {
        this.name = name;
        this.originalIndex = originalIndex;
    }

    /**
     * The name of the column (the header of the CSV file)
     * @return
     */
    public String getName() {
        return name;
    }
    /**
     * The name of the column (the header of the CSV file)
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * The 0 based index of this column in the original CSV file
     * @return
     */
    public Integer getOriginalIndex() {
        return originalIndex;
    }

    /**
     * The 0 based index of this column in the original CSV file
     * @param originalIndex
     */
    public void setOriginalIndex(Integer originalIndex) {
        this.originalIndex = originalIndex;
    }

    /**
     * Unique ID for this parameter
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Unique ID for this parameter
     * @return
     */
    public Integer getId() {
        return id;
    }
}
