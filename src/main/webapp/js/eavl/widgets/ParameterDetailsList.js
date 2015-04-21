/**
 * Grid Panel extension for rendering a list of ParameterDetails
 */
Ext.define('eavl.widgets.ParameterDetailsList', {
    extend: 'Ext.grid.Panel',

    alias: 'widget.pdlist',

    /**
     * Adds the following config to Ext.grid.Panel
     * {
     *  showUom : Boolean [Optional] - Whether the unit of measure for this PD should display. Defaults false
     *  parameterDetails : eavl.model.ParameterDetails[] [Optional] The set of parameter details to initialise this list with
     *  forcedIconUrl : String [Optional] - If set, all icons will be forced to this value (instead of dynamic lookup)
     *  forcedIconTip : String [Optional] - If set, all icons will be forced to have this tip value (instead of dynamic lookup)
     *  sortFn : function(a,b) - Sorter to apply to this list
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {
        var me = this;
        this.emptyText = config.emptyText ? config.emptyText : "";
        this.showUom = config.showUom === true ? true : false;
        this.forcedIconUrl = Ext.isEmpty(config.forcedIconUrl) ? null : config.forcedIconUrl;
        this.forcedIconTip = Ext.isEmpty(config.forcedIconTip) ? null : config.forcedIconTip;
        
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
        
        var cols = [{
            dataIndex : 'uom',
            flex : 1,
            renderer : function(value, md, record) {
                var name = record.get('displayName');
                var emptyString = name === '';
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
                
                if (me.forcedIconUrl) {
                    img = me.forcedIconUrl;
                }
                
                if (me.forcedIconTip) {
                    tip = me.forcedIconTip;
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
                        html : name
                    }]});
            }
        }];
        if (this.showUom) {
            cols.push({
                dataIndex : 'uom',
                width: 50,
                renderer: function(value, md, record) {
                    var text = value ? value : '???';
                    var cls = value === eavl.models.ParameterDetails.UOM_PPM ? 'pdl-uom-parent-ok' : 'pdl-uom-parent-action';
                    
                    return Ext.DomHelper.markup({
                        tag: 'div',
                        cls: 'pdl-uom-parent ' + cls,
                        html: text
                    });
                }
            });
        }

        Ext.apply(config, {
            cls: 'pdl',
            hideHeaders : true,
            store : store,
            columns : cols
        });

        this.callParent(arguments);
    }
});