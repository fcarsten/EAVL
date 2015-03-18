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
     *  parameterDetails: eavl.models.ParameterDetails [Optional] The parameter details to initialise this panel with.
     *  emptyText : String - text to show when nothing is selected
     *
     *  preserveAspectRatio - boolean - Should the charts preserve a 4x2 aspect ratio or should it stretch. Default false
     *  targetChartWidth - Number - (Only useful if preserveAspectRatio is set) - The target width to use in aspect ratio for each chart
     *  targetChartHeight - Number - (Only useful if preserveAspectRatio is set)  - The target height to use in aspect ratio for each chart
     *
     *
     * Adds the following events
     * {
     *  parameterchanged : function(this, parameterDetails) - Fired whenever the CSV data changes
     * }
     */
    constructor : function(config) {
        this.emptyText = config.emptyText ? config.emptyText : "";
        this.parameterDetails = config.parameterDetails ? config.parameterDetails : null;

        var emptyCard = {
            itemId : 'card-empty',
            xtype : 'container',
            html : Ext.util.Format.format('<div class="proxydp-empty-container"><div class="proxydp-empty-container-inner"><img src="img/inspect.svg" width="100"/><br>{0}</div></div>', this.emptyText)
        };

        var inspectCard = {
            itemId : 'card-inspect',
            xtype: 'container',
            layout: {
                type: 'vbox',
                align : 'center',
                pack : 'start'
            },
            items: [{
                xtype: 'panel',
                width: '100%',
                flex: 1,
                layout: 'fit',
                items : [{
                    xtype: 'doublepdfchart',
                    itemId: 'dpdfchart',
                    file: eavl.models.EAVLJob.FILE_IMPUTED_CSV,
                    preserveAspectRatio : config.preserveAspectRatio,
                    parameterDetails : this.parameterDetails,
                    targetWidth: config.targetChartWidth,
                    targetHeight: config.targetChartHeight
                }]
            },{
                xtype: 'panel',
                width: '100%',
                flex: 1,
                layout: 'fit',
                items : [{
                    xtype: 'meanacfchart',
                    itemId: 'meanacfchart',
                    parameterDetails : this.parameterDetails,
                    preserveAspectRatio : config.preserveAspectRatio,
                    targetWidth: config.targetChartWidth,
                    targetHeight: config.targetChartHeight
                }]
            }]
        };

        //extjs doesnt have a "initial card" config option (annoying!)
        //So we need this little bit of code to ensure our starting card
        //is first in the list
        var cards;
        if (this.parameterDetails) {
            cards = [inspectCard, emptyCard];
        } else {
            cards = [emptyCard, inspectCard];
        }

        var me = this;
        Ext.apply(config, {
            layout : {
                type : 'card',
                deferredRender: false
            },
            items: cards
        });

        this.callParent(arguments);
    },

    initComponent : function() {
        this.callParent(arguments);
        if (!this.parameterDetails) {
            this.hideParameterDetails();
        }
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