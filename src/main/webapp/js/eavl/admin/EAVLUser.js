/**
 * A contact is a simplified contact details
 */
Ext.define('eavl.admin.EAVLUser', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'userName', type: 'string' }, //String - id of this user
        { name: 'fullName', type: 'string' }, //String - Name of this user (can be null/empty)
        { name: 'email', type: 'string' }, //String - email of this user 
        { name: 'authorities', type: 'auto' } //String[] - Set of authorities this user has
    ]
});
