/**
 * A workflow is the collection of metadata about a top level EAVL workflow
 */
Ext.define('eavl.models.Workflow', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'id', type: 'string' }, //ID of this workflow     
        { name: 'name', type: 'string' }, //Name of this workflow
        { name: 'version', type: 'string' }, //Version string
        { name: 'description', type: 'string' }, //long description of this workflow
        { name: 'background', type: 'string' }, //URL to background image
        { name: 'contacts', type: 'auto' } //eavl.models.Contact[]
    ],
    
    idProperty: 'id'
});
