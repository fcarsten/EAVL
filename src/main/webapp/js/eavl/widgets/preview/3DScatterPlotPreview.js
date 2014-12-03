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
                xtype : 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items : [{
                    xtype: 'panel',
                    width: 150,
                    layout: {
                        type: 'vbox',
                        align: 'center'
                    },
                    itemId: 'details'
                },{
                    xtype : '3dscatterplot',
                    itemId : 'plot',
                    valueAttr : 'estimate',
                    valueScale : 'log',
                    pointSize: 4,
                    flex: 1,
                    listeners: {
                        select: function(plot, data) {
                            var parent = plot.ownerCt.down('#details');

                            if (parent.items.getCount() !== 0) {
                                parent.removeAll(true);
                            }

                            parent.add({
                                xtype: 'datadisplayfield',
                                fieldLabel: plot.xLabel,
                                value: data.x
                            });
                            parent.add({
                                xtype: 'datadisplayfield',
                                fieldLabel: plot.yLabel,
                                value: data.y
                            });
                            parent.add({
                                xtype: 'datadisplayfield',
                                fieldLabel: plot.zLabel,
                                value: data.z
                            });
                            parent.add({
                                xtype: 'datadisplayfield',
                                fieldLabel: 'Estimate',
                                value: data.estimate
                            });
                        },
                        deselect: function(plot) {
                            var parent = plot.ownerCt.down('#details');

                            if (parent.items.getCount() !== 0) {
                                parent.removeAll(true);
                            }
                        }
                    }
                }]
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

                scatterPlot.xLabel = responseObj.data.xLabel;
                scatterPlot.yLabel = responseObj.data.yLabel;
                scatterPlot.zLabel = responseObj.data.zLabel;
                scatterPlot.plot(responseObj.data.points);
            }
        });
    }
});