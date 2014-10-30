/**
 * Grid Panel extension for rendering a list of ParameterDetails
 */
Ext.define('eavl.widgets.ParameterDetailsList', {
    extend: 'Ext.grid.Panel',

    alias: 'widget.pdlist',

    /**
     * Adds the following config to Ext.grid.Panel
     * {
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {
        this.emptyText = config.emptyText ? config.emptyText : "";


        var store = Ext.create('Ext.data.Store', {
            model : 'eavl.models.ParameterDetails'
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

                    var totalDataPoints = record.get('totalNumeric') + record.get('totalText') + record.get('totalMissing');
                    var percentageNumeric = (record.get('totalNumeric') * 100) / totalDataPoints;
                    var img = 'img/tick.png';
                    var tip = 'This parameter contains more than 95% numeric values';

                    if (percentageNumeric < 70) {
                        img = 'img/exclamation.png';
                        tip = 'This parameter contains less than 70% numeric values';
                    } else if (percentageNumeric < 95) {
                        img = 'img/error.png';
                        tip = 'This parameter contains between 70% and 95% numeric values';
                    }

                    return Ext.DomHelper.markup({
                        tag : 'div',
                        style : {
                            cursor: 'pointer'
                        },
                        children : [{
                            tag: 'img',
                            'data-qtip' : tip,
                            src : img,
                            width : 32,
                            height : 32
                        },{
                            tag : 'b',
                            style : {
                                'font-size' : '170%'
                            },
                            html : value
                        }]});
                }
            }]
        });

        this.callParent(arguments);
    }
});