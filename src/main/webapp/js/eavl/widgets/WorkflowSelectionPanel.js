/**
 * Panel extension for rendering a collection eavl.models.Workflow objects
 */
Ext.define('eavl.widgets.WorkflowSelectionPanel', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.workflowselectionpanel',


    /**
     * Adds the following config to Ext.panel.Panel
     * {
     *  workflows: eavl.models.Workflow[]
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