package org.auscope.portal.server.eavl;

import java.util.Date;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.auscope.portal.core.cloud.StagedFileOwner;
import org.auscope.portal.server.security.oauth2.EavlUser;

/**
 * EAVL Job
 * @author Josh Vote
 *
 */
@Entity
public class EAVLJob implements StagedFileOwner {

    @Basic
    private Double predictionCutoff;
    @Basic
    private String predictionParameter;

    @ManyToOne
    private EavlUser user;

    /**
     * @return the user
     */
    public EavlUser getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(EavlUser user) {
        this.user = user;
    }

    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(name="job_saved_params", joinColumns=@JoinColumn(name="job_id"))
//    @Column(name="saved_params")
    private Set<String> savedParameters;

    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(name="job_proxy_params", joinColumns=@JoinColumn(name="job_id"))
//    @Column(name="proxy_params")
    private Set<String> proxyParameters;

    @Basic
    private String imputationTaskId;
    @Basic
    @Column(length=4096)
    private String imputationTaskError;
    @Basic
    private String kdeTaskId;
    @Basic
    @Column(length=4096)
    private String kdeTaskError;
    @Basic
    private String holeIdParameter;

    @Id
    @Column(name="job_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Basic
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    private Date submitDate;

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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @param submitDate the submitDate to set
     */
    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

//    public EAVLJob(Integer id) {
//        this.id=id;
//    }

    public EAVLJob() {
        // JPA only
    }

    /**
     * Gets the prediction cutoff configured before Imputation
     * @return
     */
    public Double getPredictionCutoff() {
        return predictionCutoff;
    }

    /**
     * Sets the prediction cutoff configured before Imputation
     *
     * @param predictionCutoff
     */
    public void setPredictionCutoff(Double predictionCutoff) {
        this.predictionCutoff = predictionCutoff;
    }

    /**
     * Gets the name of the parameter saved for prediction. The name corresponds to
     * the column header of the CSV file.
     *
     * Can be null if prediction not yet set
     *
     * @return
     */
    public String getPredictionParameter() {
        return predictionParameter;
    }

    /**
     * Sets the name of the parameter saved for prediction. The name corresponds to
     * the column header of the CSV file.
     *
     * Can be null if prediction not yet set
     * @return
     */
    public void setPredictionParameter(String predictionParameter) {
        this.predictionParameter = predictionParameter;
    }

    /**
     * Gets the parameter names of columns which have been "saved" for inclusion
     * in the output data file. The name corresponds to the column header of the CSV file.
     *
     * Can be null if not yet set.
     *
     * @return
     */
    public Set<String> getSavedParameters() {
        return savedParameters;
    }

    /**
     * Sets the parameter names of columns which have been "saved" for inclusion
     * in the output data file. The name corresponds to the column header of the CSV file.
     *
     * Can be null if not yet set.
     * @param savedParameters can be null or empty
     */
    public void setSavedParameters(Set<String> savedParameters) {
        this.savedParameters = savedParameters;
    }

    /**
     * Gets the ID of the last run imputation task (or null if it hasn't been run yet)
     * @return
     */
    public String getImputationTaskId() {
        return imputationTaskId;
    }

    /**
     * Sets the ID of the last run imputation task (or null if it hasn't been run yet)
     * @param imputationTaskId
     */
    public void setImputationTaskId(String imputationTaskId) {
        this.imputationTaskId = imputationTaskId;
    }

    /**
     * Gets the ID of the last run kernel density estimator task (or null if it hasn't been run yet)
     * @return
     */
    public String getKdeTaskId() {
        return kdeTaskId;
    }

    /**
     * Sets the ID of the last run kernel density estimator task (or null if it hasn't been run yet)
     * @param imputationTaskId
     */
    public void setKdeTaskId(String kdeTaskId) {
        this.kdeTaskId = kdeTaskId;
    }

    /**
     * Gets the name of the parameter saved for hole identification. The name corresponds to
     * the column header of the CSV file. This column will work like a "group by" clause
     * for some operations
     *
     * Can be null if not yet set
     * @return
     */
    public String getHoleIdParameter() {
        return holeIdParameter;
    }

    /**
     * Sets the name of the parameter saved for hole identification. The name corresponds to
     * the column header of the CSV file. This column will work like a "group by" clause
     * for some operations
     *
     * Can be null if not yet set
     * @return
     */
    public void setHoleIdParameter(String holeIdParameter) {
        this.holeIdParameter = holeIdParameter;
    }

    /**
     * Gets the set of Proxy Parameters chosen for this job (or null if they haven't been chosen yet)
     * @return
     */
    public Set<String> getProxyParameters() {
        return proxyParameters;
    }

    /**
     * Sets the set of Proxy Parameters chosen for this job (or null if they haven't been chosen yet)
     * @param proxyParameters
     */
    public void setProxyParameters(Set<String> proxyParameters) {
        this.proxyParameters = proxyParameters;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    /**
     * Gets the error string for the most recently run imputation task or null if
     * there is no error or no imputation task.
     * @return
     */
    public String getImputationTaskError() {
        return imputationTaskError;
    }

    /**
     * Sets the error string for the most recently run imputation task or null if
     * there is no error or no imputation task.
     * @param imputationTaskError can be null
     */
    public void setImputationTaskError(String imputationTaskError) {
        this.imputationTaskError = imputationTaskError;
    }

    /**
     * Gets the error string for the most recently run KDE task or null if
     * there is no error or no KDE task.
     * @return
     */
    public String getKdeTaskError() {
        return kdeTaskError;
    }

    /**
     * Sets the error string for the most recently run KDE task or null if
     * there is no error or no KDE task.
     * @param kdeTaskError Can be null
     * @return
     */
    public void setKdeTaskError(String kdeTaskError) {
        this.kdeTaskError = kdeTaskError;
    }


}
