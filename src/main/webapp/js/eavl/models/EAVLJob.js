/**
 * A EAVLJob is a representation of a single run through the EAVL workflow
 */
Ext.define('eavl.models.EAVLJob', {
    extend: 'Ext.data.Model',

    statics : {
        STATUS_UNSUBMITTED : "unsubmitted",
        STATUS_KDE_ERROR : "kde-error",
        STATUS_IMPUTE_ERROR : "impute-error",
        STATUS_IMPUTING : "imputing",
        STATUS_THRESHOLD : "threshold",
        STATUS_PROXY : "proxy",
        STATUS_SUBMITTED : "submitted",
        STATUS_DONE : "done",

        PARAMETER_ESTIMATE : "eavl-kde-estimate",


        FILE_DATA_CSV : "data.csv",
        FILE_TEMP_DATA_CSV : "data-tmp.csv",
        FILE_VALIDATED_DATA_CSV : "data-validated.csv",
        FILE_IMPUTED_CSV : "data-imputed.csv",
        FILE_KDE_JSON : "data-kde.json",
        FILE_KDE_CSV : "data-kde.csv",
    },

    fields: [
        { name: 'id', type: 'int' }, //Unique identifier for the series
        { name: 'name', type: 'string' }, //Descriptive name of the series
        { name: 'status', type: 'string' },
        { name: 'predictionCutoff', type: 'float' },
        { name: 'predictionParameter', type: 'string' },
        { name: 'savedParameters', type: 'auto' }, //String[] Parameter names listed as saved
        { name: 'proxyParameters', type: 'auto' }, //String[] Parameter names chosen as proxies
        { name: 'imputationTaskId', type: 'string' },
        { name: 'kdeTaskId', type: 'string' },
        { name: 'holeIdParameter', type: 'string' }
    ],

    idProperty : 'id'
});
