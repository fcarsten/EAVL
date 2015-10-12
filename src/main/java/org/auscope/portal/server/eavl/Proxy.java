/**
 *
 */
package org.auscope.portal.server.eavl;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.common.collect.Sets;

/**
 * @author fri096
 *
 */
@Entity
public class Proxy implements Serializable {
    @Id
    @Column(name="job_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ElementCollection(fetch=FetchType.EAGER)
    private Set<String> denom;
    @Basic
    private String numerator;
    @Basic
    private String displayName;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the denom
     */
    public Set<String> getDenom() {
        return denom;
    }

    /**
     * @param denom the denom to set
     */
    public void setDenom(Set<String> denom) {
        this.denom = denom;
    }

    /**
     * @return the numerator
     */
    public String getNumerator() {
        return numerator;
    }

    /**
     * @param numerator the numerator to set
     */
    public void setNumerator(String numerator) {
        this.numerator = numerator;
    }


    /**
     * The name for this proxy for use when displaying its data. Returns numerator if displayname is null/empty
     * @return
     */
    public String getDisplayName() {
        if (displayName == null || displayName.isEmpty()) {
            return numerator;
        }

        return displayName;
    }

    /**
     * The name for this proxy for use when displaying its data
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Proxy() {
    }

    public Proxy(String numerator, String[] denom) {
        this.numerator=numerator;
        this.denom=Sets.newHashSet(denom);
        this.displayName=numerator;
    }

    public Proxy(String numerator, String[] denom, String displayName) {
        this.numerator=numerator;
        this.displayName=displayName;
        this.denom=Sets.newHashSet(denom);
    }

}
