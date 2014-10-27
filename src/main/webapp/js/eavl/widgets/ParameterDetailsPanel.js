/**
 * Panel extension for inspecting a ParameterDetails object
 * and highlighting a number of eavl specific features
 */
Ext.define('eavl.widgets.CSVGrid', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.pdpanel',

    parameterDetails : null,

    constructor : function(config) {

        Ext.apply(config, {

        });

        this.callParent(arguments);
    }
});