/**
 * Panel extension for rendering a set of BaseFilePreview implementations
 */
Ext.define('eavl.widgets.FilePreviewPanel', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.filepreviewpanel',

    /**
     * Adds the following config to Ext.grid.Panel
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

        var previewers = [
            {
                border: false,
                itemId: 'empty',
                html: '<div class="preview-empty-container"><div class="preview-empty-container-inner">Select a file above to activate the preview window.</div></div>'
            },
            Ext.create('eavl.widgets.preview.CSVFilePreview', {itemId: 'csv'}),
            Ext.create('eavl.widgets.preview.3DScatterPlotPreview', {itemId: 'threedscatterplot'}),
            Ext.create('eavl.widgets.preview.BoreholeEstimatePreview', {itemId: 'bhestimate'})
        ];


        Ext.apply(config, {
            layout: 'card',
            items: previewers

        });

        this.callParent(arguments);
    },

    /**
     * Previews a file for a job
     * @param job EAVLJob owning fileName
     * @param fileName The name of hte file to preview
     * @param type String Which file previewer should be used?
     */
    preview : function(job, fileName, type) {
        var l = this.getLayout();
        l.setActiveItem(type);
        var previewer = l.getActiveItem();
        previewer.preview(job, fileName);
    },


    /**
     * Clears the current file preview (if any)
     */
    clearPreview : function() {
        this.getLayout().setActiveItem('empty');
    }
});