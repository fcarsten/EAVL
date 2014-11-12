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
    private String imputationTaskId;

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
}
