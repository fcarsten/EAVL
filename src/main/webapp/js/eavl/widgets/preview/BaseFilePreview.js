/**
 * Base Container extension for rendering a specific file previewer
 */
Ext.define('eavl.widgets.preview.BaseFilePreview', {
    extend: 'Ext.container.Container',

    constructor : function(config) {
        this.callParent(arguments);
    },

    /**
     * function(job, fileName)
     * job - EAVLJob - Job to preview
     * fileName - String - name of the job file to preview
     *
     * Setup this preview page to preview the specified file for the specified job
     *
     * returns nothing
     */
    preview : portal.util.UnimplementedFunction
});