/**
 * Panel extension for inspecting a ParameterDetails object
 * and highlighting a number of charts associated with
 * the ParameterDetailsObject
 */
Ext.define('eavl.widgets.ProxyDetailsPanel', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.proxypanel',

    parameterDetails : null,
    emptyText : null,


    /**
     * Adds the following config to Ext.panel.Panel
     * {
     *  emptyText : String - text to show when nothing is selected
     * }
     *
     * Adds the following events
     * {
     *  parameterchanged : function(this, parameterDetails) - Fired whenever the CSV data changes
     * }
     */
    constructor : function(config) {
        this.emptyText = config.emptyText ? config.emptyText : "";


        var me = this;
        Ext.apply(config, {
            layout : {
                type : 'card',
                deferredRender: false
            },
            items: [{
                itemId : 'card-empty',
                xtype : 'container',
                html : Ext.util.Format.format('<div class="proxydp-empty-container"><div class="proxydp-empty-container-inner"><img src="img/inspect.svg" width="100"/><br>{0}</div></div>', this.emptyText)
            },{
                itemId : 'card-inspect',
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    align : 'center',
                    pack : 'start'
                },
                items: [{
                    xtype: 'panel',
                    title: 'Double PDF - (GET BETTER NAME)',
                    width: '100%',
                    flex: 1,
                    layout: 'fit',
                    items : [{
                        xtype: 'doublepdfchart',
                        itemId: 'dpdfchart'
                    }]
                },{
                    xtype: 'panel',
                    title: 'Mean ACF',
                    width: '100%',
                    flex: 1,
                    layout: 'fit',
                    items : [{
                        xtype: 'meanacfchart',
                        itemId: 'meanacfchart'
                    }]
                }]
            }]
        });

        this.callParent(arguments);

        this.addEvents(['parameterchanged']);
    },

    initComponent : function() {
        this.callParent(arguments);
        this.hideParameterDetails();
    },

    /**
     * Inspects the specified parameter details. Removes any parameter details that is current being inspected
     *
     * @param parameterDetails Can be null, if so this will call hideParameterDetails
     */
    showParameterDetails : function(parameterDetails) {
        this.parameterDetails = parameterDetails;


        this.down('#dpdfchart').plotParameterDetails(parameterDetails);
        this.down('#meanacfchart').plotParameterDetails(parameterDetails);

        if (this.getLayout().getActiveItem().getItemId() !== 'card-inspect') {
            this.getLayout().setActiveItem('card-inspect');
        }
    },

    /**
     * Hides any inspected parameter
     */
    hideParameterDetails : function() {
        this.parameterDetails = null;

        this.down('#dpdfchart').clearPlot();
        this.down('#meanacfchart').clearPlot();

        this.getLayout().setActiveItem('card-empty');
    }
});