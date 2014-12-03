/**
 * Previewer for rendering 3D Scatter Plots
 */
Ext.define('eavl.widgets.preview.3DScatterPlotPreview', {
    extend : 'Ext.container.Container',

    innerId : null,
    threeJs : null,
    d3 : null,

    constructor : function(config) {
        this.innerId = Ext.id();
        this._md = {};
        Ext.apply(config,{
            layout : 'fit',
            items : [{
                xtype : '3dscatterplot',
                itemId : 'plot',
                valueAttr : 'estimate',
                valueScale : 'log'
            }]
        });

        this.callParent(arguments);
    },

    /**
     * function(job, fileName) job - EAVLJob - Job to preview
     * fileName - String - name of the job file to preview
     *
     * Setup this preview page to preview the specified file for
     * the specified job
     *
     * returns nothing
     */
    preview : function(job, fileName) {
        var me = this;
        var mask = new Ext.LoadMask(me, {
            msg : "Please wait..."
        });
        mask.show();
        Ext.Ajax.request({
            url : 'results/getKDEGeometry.do',
            params : {
                jobId : job.get('id'),
                name : fileName
            },
            scope : this,
            callback : function(options, success, response) {
                mask.hide();
                mask.destroy();
                if (!success) {
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj || !responseObj.success) {
                    return;
                }

                var scatterPlot = this.down('#plot');
                scatterPlot.plot(responseObj.data);
            }
        });
    }
});