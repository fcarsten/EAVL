/**
 * Previews a CSV file by printing estimate down the length of each borehole
 */
Ext.define('eavl.widgets.preview.BoreholeEstimatePreview', {
    extend: 'eavl.widgets.preview.BaseFilePreview',

    constructor : function(config) {

        Ext.apply(config, {
            layout: 'fit',
            border: false,
            items: [{
                xtype: 'bhestimatechart',
                itemId: 'chart',
                listeners: {
                    afterrender: function(chart) {
                        var domSpec = eavl.widgets.charts.BoreholeEstimateChart.legendMarkup(null, 'horizontal', 'position: absolute; right: 10px; bottom: 10px;');
                        Ext.DomHelper.insertAfter(chart.getEl(), domSpec);
                    }
                }
            }]
        });

        this.callParent(arguments);
    },

    preview: function(job, fileName) {
        var paramsToGroup = [eavl.models.EAVLJob.PARAMETER_ESTIMATE, job.get('predictionParameter')];
        
        var me = this;
        var mask = new Ext.LoadMask({
            target: me,
            msg:"Please wait..."
        });
        mask.show();
        Ext.Ajax.request({
            url: "results/getGroupedNumericValues.do",
            params: {
                fileName: fileName,
                jobId: job.get('id'),
                groupName : job.get('holeIdParameter'),
                paramName : paramsToGroup
            },
            callback: function(options, success, response) {
                mask.hide();
                mask.destroy();

                if (!success) {
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    return;
                }
                
                //We want to calculate percentiles across the entire dataset but the values are grouped into seperate arrays
                var allValues = [];
                Ext.each(responseObj.data, function(grp, grpIndex) {                    
                    allValues = allValues.concat(grp.values);
                });
                
                //Sort our values by estimate
                Ext.Array.sort(allValues, function(a, b) { return a[0] - b[0]; });
                
                //Add percentiles to each item
                var totalValuesDenom = allValues.length / 100;
                Ext.each(allValues, function(value, valueIndex) {
                    value.push(valueIndex / totalValuesDenom);
                });
                

                var chart = me.down('#chart');
                chart.plot(responseObj.data)
            }
        });
    }
});