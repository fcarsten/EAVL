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
        STATUS_PROXY : "proxy",
        STATUS_SUBMITTED : "submitted",
        STATUS_DONE : "done"
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
