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

    public Proxy() {
    }

    public Proxy(String numerator, String[] denom) {
        this.numerator=numerator;
        this.denom=Sets.newHashSet(denom);
    }

}
