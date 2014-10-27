/**
 * Grid Panel extension for scrolling through a large CSV file
 * and highlighting a number of eavl specific features
 */
Ext.define('eavl.widgets.CSVGrid', {
    extend: 'Ext.grid.Panel',

    alias: 'widget.csvgrid',

    parameterDetails : null,

    /**
     * parameterDetails : eavl.models.ParameterDetails[] - The data columns in this CSVGrid
     */
    constructor: function(config) {

        this.parameterDetails = config.parameterDetails ? config.parameterDetails : [];

        var fields = [];
        var columns = [];
        for (var i = 0; i < this.parameterDetails.length; i++) {
            var name = this.parameterDetails[i].get('name');
            fields.push(name);
            columns.push({dataIndex: name, text: name});
        }

        var csvStore = Ext.create('Ext.data.Store', {
            remoteGroup: true,
            // allow the grid to interact with the paging scroller by buffering
            buffered: true,
            pageSize: 100,
            leadingBufferZone: 300,
            fields : fields,
            autoLoad: true,
            proxy: {
                // load using script tags for cross domain, if the data in on the same domain as
                // this page, an Ajax proxy would be better
                type: 'ajax',
                url: 'validation/streamRows.do',
                reader: {
                    type : 'array',
                    root: 'rows',
                    totalProperty: 'totalCount'
                }
            }
        });

        Ext.apply(config, {
            columns : columns,
            store : csvStore,
            loadMask: true,
            multiSelect: true,
            selModel: {
                pruneRemoved: false
            },
            viewConfig: {
                trackOver: false
            }
        });

        this.callParent(arguments);
    }
});