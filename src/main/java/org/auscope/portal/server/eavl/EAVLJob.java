package org.auscope.portal.server.eavl;

import java.util.Set;

import org.auscope.portal.core.cloud.CloudJob;

/**
 * EAVL Specialisation of a CloudJob
 * @author Josh Vote
 *
 */
public class EAVLJob extends CloudJob {

    private Double predictionCutoff;
    private String predictionParameter;
    private Set<String> savedParameters;
    private Set<String> proxyParameters;
    private String imputationTaskId;
    private String kdeTaskId;
    private String holeIdParameter;

    public EAVLJob(Integer id) {
        super(id);
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

}
