/**
 * Previews a CSV file using CSV grid
 */
Ext.define('eavl.widgets.preview.CSVFilePreview', {
    extend: 'eavl.widgets.preview.BaseFilePreview',

    constructor : function(config) {

        Ext.apply(config, {
            layout: 'fit',
            items: []
        });

        this.callParent(arguments);
    },

    preview: function(job, fileName) {
        var me = this;
        var mask = new Ext.LoadMask(me, {msg:"Please wait..."});
        mask.show();
        Ext.Ajax.request({
            url: "validation/getParameterDetails.do",
            params: {
                file: fileName,
                jobId: job.get('id')
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

                var parameterDetails = [];
                for (var i = 0; i < responseObj.data.length; i++) {
                    parameterDetails.push(Ext.create('eavl.models.ParameterDetails', responseObj.data[i]));
                }

                var csvGrid = me.down('#csvgrid');
                if (csvGrid) {
                    csvGrid.destroy();
                }

                me.add(Ext.create('eavl.widgets.CSVGrid', {
                    border: false,
                    itemId: 'csvGrid',
                    jobId: job.get('id'),
                    file: fileName,
                    parameterDetails : parameterDetails,
                    readOnly: true,
                    sortColumns: false
                }));
                me.doLayout();
            }
        });
    }
});