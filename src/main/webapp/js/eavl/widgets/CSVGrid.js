/**
 * Grid Panel extension for scrolling through a large CSV file
 * and highlighting a number of eavl specific features
 */
Ext.define('eavl.widgets.CSVGrid', {
    extend: 'Ext.grid.Panel',

    alias: 'widget.csvgrid',

    dataModelDef : null,

    /**
     * columnCount : int - How many data columns will this CSVGrid represent?
     */
    constructor: function(config) {

        var fields = [];
        var columns = [];
        for (var i = 0; i < config.columnCount; i++) {
            var tempName = 'C' + i;
            fields.push(tempName);
            columns.push({dataIndex: tempName});
        }

        var csvStore = Ext.create('Ext.data.Store', {
            remoteGroup: true,
            // allow the grid to interact with the paging scroller by buffering
            buffered: true,
            leadingBufferZone: 300,
            pageSize: 100,
            fields : fields,
            proxy: {
                // load using script tags for cross domain, if the data in on the same domain as
                // this page, an Ajax proxy would be better
                type: 'ajax',
                url: 'validation/streamRows.do',
                reader: {
                    type : 'array',
                    root: 'rows',
                    totalProperty: 'totalCount'
                },
                // sends single sort as multi parameter
                simpleSortMode: true,
                // sends single group as multi parameter
                simpleGroupMode: true,

                // This particular service cannot sort on more than one field, so grouping === sorting.
                groupParam: undefined,
                groupDirectionParam: undefined
            },
            autoLoad: true
        });

        Ext.apply(config, {
            columns : columns,
            store : csvStore,
            loadMask: true,
            selModel: {
                pruneRemoved: false
            },
            multiSelect: true,
            viewConfig: {
                trackOver: false
            },
            verticalScroller : {
                variableRowHeight: true
            }
        });

        this.callParent(arguments);
    }
});