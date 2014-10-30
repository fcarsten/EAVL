/**
 * Panel extension for highlighting where in the EAVL workflow the user is currently located
 */
Ext.define('eavl.widgets.WorkflowLocationPanel', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.workflowpanel',



    /**
     * Adds the following config to Ext.panel.Panel
     * {
     *
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {

        Ext.apply(config, {

        });

        this.callParent(arguments);
    }
});