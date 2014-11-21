/**
 * A JobFile is a simple representation of a file stored against a particular EAVLJob
 */
Ext.define('eavl.models.JobFile', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'name', type: 'string' }, //unique name of this file
        { name: 'size', type: 'int' } //Length of the file in bytes
    ],

    idProperty : 'name'
});
