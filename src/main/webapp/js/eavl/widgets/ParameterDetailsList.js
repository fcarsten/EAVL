/**
 * Grid Panel extension for rendering a list of ParameterDetails
 */
Ext.define('eavl.widgets.ParameterDetailsList', {
    extend: 'Ext.grid.Panel',

    alias: 'widget.pdlist',

    /**
     * Adds the following config to Ext.grid.Panel
     * {
     *  parameterDetails : eavl.model.ParameterDetails[] [Optional] The set of parameter details to initialise this list with
     *  sortFn : function(a,b) - Sorter to apply to this list
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {
        this.emptyText = config.emptyText ? config.emptyText : "";

        var sorters = [];
        if (config.sortFn) {
            sorters.push({
                sorterFn : config.sortFn
            });
        }

        var store = Ext.create('Ext.data.Store', {
            model : 'eavl.models.ParameterDetails',
            data : config.parameterDetails ? config.parameterDetails : [],
            sorters: sorters
        });

        Ext.apply(config, {
            disableSelection : true,
            hideHeaders : true,
            store : store,
            columns : [{
                dataIndex : 'name',
                flex : 1,
                renderer : function(value, md, record) {
                    var emptyString = value === '';
                    var status = record.calculateStatus();

                    var img = 'img/tick.png';
                    var tip = 'This parameter contains more than 70% numeric values';
                    if (status === eavl.models.ParameterDetails.STATUS_ERROR) {
                        img = 'img/exclamation.png';
                        tip = 'This parameter contains non numeric values.';
                    } else if (status === eavl.models.ParameterDetails.STATUS_WARNING) {
                        img = 'img/error.png';
                        tip = 'This parameter less than 70% numeric values.';
                    }

                    return Ext.DomHelper.markup({
                        tag : 'div',
                        style : {
                            cursor: 'pointer',
                            display: 'table'
                        },
                        children : [{
                            tag: 'img',
                            'data-qtip' : tip,
                            src : img,
                            cls: 'pdl-row-img'
                        },{
                            tag : 'span',
                            cls : 'pdl-row-text',
                            html : value
                        }]});
                }
            }]
        });

        this.callParent(arguments);
    }
});