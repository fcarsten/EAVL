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
            border: false,
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
                    border: false,
                    layout: {
                        type: 'vbox',
                        align: 'center'
                    },
                    itemId: 'details',
                    items: [{
                        xtype: 'label',
                        margin: '50 0 0 0',
                        style: {
                            color: '#888888',
                            'font-style': 'italic',
                            'font-size': '14.5px'
                        },
                        text: 'Click a point'
                    },{
                        xtype: 'label',
                        margin: '2 0 0 0',
                        style: {
                            color: '#888888',
                            'font-style': 'italic',
                            'font-size': '14.5px'
                        },
                        text: 'for more information'
                    }]
                },{
                    xtype : 'threedscatterplot',
                    border: false,
                    itemId : 'plot',
                    valueAttr : 'percentile',
                    valueRenderer : eavl.widgets.charts.BoreholeEstimateChart.percentileToColor,
                    xHideValueLabel: true,
                    yHideValueLabel: true,
                    zHideValueLabel: true,
                    pointSize: 4,
                    allowSelection : true,
                    flex: 1,
                    listeners: {
                        afterrender: function(plot) {
                            var domSpec = eavl.widgets.charts.BoreholeEstimateChart.legendMarkup(null, null, 'position: absolute; right: 10px; top: 10px;');
                            Ext.DomHelper.insertAfter(plot.getEl(), domSpec);
                        },
                        
                        select: function(plot, data) {
                            var parent = plot.ownerCt.down('#details');

                            if (parent.items.getCount() !== 0) {
                                parent.removeAll(true);
                            }

                            parent.add({
                                xtype: 'datadisplayfield',
                                border: false,
                                fieldLabel: plot.xLabel,
                                margin : '10 0 0 0',
                                value: Ext.util.Format.number(data.x, '0.0000')
                            });
                            parent.add({
                                xtype: 'datadisplayfield',
                                fieldLabel: plot.yLabel,
                                margin : '10 0 0 0',
                                value: Ext.util.Format.number(data.y, '0.0000')
                            });
                            parent.add({
                                xtype: 'datadisplayfield',
                                fieldLabel: plot.zLabel,
                                margin : '10 0 0 0',
                                value: Ext.util.Format.number(data.z, '0.0000')
                            });
                            parent.add({
                                xtype: 'datadisplayfield',
                                fieldLabel: 'Estimate',
                                margin : '10 0 0 0',
                                value: Ext.util.Format.number(data.estimate, '0.0000')
                            });
                            parent.add({
                                xtype: 'datadisplayfield',
                                fieldLabel: 'Percentile',
                                margin : '10 0 0 0',
                                value: Ext.util.Format.number(data.percentile, '0.00') + '%'
                            });
                        },
                        deselect: function(plot) {
                            var parent = plot.ownerCt.down('#details');

                            parent.removeAll(true);
                            parent.add({
                                xtype: 'label',
                                margin: '50 0 0 0',
                                style: {
                                    color: '#888888',
                                    'font-style': 'italic',
                                    'font-size': '14.5px'
                                },
                                text: 'Click a point'
                            });
                            parent.add({
                                xtype: 'label',
                                margin: '2 0 0 0',
                                style: {
                                    color: '#888888',
                                    'font-style': 'italic',
                                    'font-size': '14.5px'
                                },
                                text: 'for more information'
                            });
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
        var mask = new Ext.LoadMask({
            target: me,
            msg : "Please wait..."
        });
        mask.show();
        Ext.Ajax.request({
            url : 'results/getCPGeometry.do',
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

                scatterPlot.xLabel = responseObj.data.xLabel.indexOf('clr.') == 0 ? responseObj.data.xLabel : 'clr.' + responseObj.data.xLabel;
                scatterPlot.yLabel = responseObj.data.yLabel.indexOf('clr.') == 0 ? responseObj.data.yLabel : 'clr.' + responseObj.data.yLabel;
                scatterPlot.zLabel = responseObj.data.zLabel.indexOf('clr.') == 0 ? responseObj.data.zLabel : 'clr.' + responseObj.data.zLabel;
                
                //Calculate a percentile field based on incoming data
                var totalItems = responseObj.data.points.length / 100; //we divide by 100 so our final result comes out *100 (i.e. a percentile) 
                Ext.Array.sort(responseObj.data.points, function(a, b) { return a.estimate - b.estimate; });
                Ext.each(responseObj.data.points, function(item, index) {
                    item.percentile = (index / totalItems);
                });
                
                scatterPlot.plot(responseObj.data.points);
            }
        });
    }
});