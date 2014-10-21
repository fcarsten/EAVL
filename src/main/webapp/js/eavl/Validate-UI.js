/**
 * Controls the Validate page
 */
Ext.application({
    name : 'eavl-validate',

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {

        var csvStore = Ext.create('Ext.data.Store', {
            remoteGroup: true,
            // allow the grid to interact with the paging scroller by buffering
            buffered: true,
            leadingBufferZone: 300,
            pageSize: 100,
            proxy: {
                // load using script tags for cross domain, if the data in on the same domain as
                // this page, an Ajax proxy would be better
                type: 'ajax',
                url: 'validation/streamRows.do',
                reader: {
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

        var viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items: [{
                xtype: 'csvgrid',
                region: 'center',
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
            }]
        });
    }

});
