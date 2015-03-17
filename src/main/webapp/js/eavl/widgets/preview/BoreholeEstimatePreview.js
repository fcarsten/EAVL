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

            }]
        });

        this.callParent(arguments);
    },

    preview: function(job, fileName) {
        var paramsToGroup = [eavl.models.EAVLJob.PARAMETER_ESTIMATE, job.get('predictionParameter')];
        
        var me = this;
        var mask = new Ext.LoadMask(me, {msg:"Please wait..."});
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

                var chart = me.down('#chart');
                chart.plot(responseObj.data)
            }
        });
    }
});