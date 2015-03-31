/**
 * A contact is a simplified contact details
 */
Ext.define('eavl.models.Contact', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'name', type: 'string' }, //Name of this workflow
        { name: 'group', type: 'string' }, //
        { name: 'email', type: 'string' }
    ]
});
