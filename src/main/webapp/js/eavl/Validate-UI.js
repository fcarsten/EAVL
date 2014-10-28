/**
 * Controls the Validate page
 */
Ext.application({
    name : 'eavl-validate',

    init: function() {
        eavl.widget.SplashScren.showLoadingSplash('Loading Validator, please stand by ...');
    },

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {
        //Called if the init code fails badly
        var initError = function() {
            eavl.widget.SplashScren.hideLoadingScreen();
            eavl.widget.SplashScren.showErrorSplash('There was an error loading your data. Please try refreshing the page or contacting cg-admin@csiro.au if the problem persists.');
        };

        var initSuccess = function(parameterDetails) {
            eavl.widget.SplashScren.hideLoadingScreen();

            var viewport = Ext.create('Ext.container.Viewport', {
                layout: 'border',
                items: [{
                    xtype: 'panel',
                    region: 'north',
                    height: 100,
                    layout: {
                        type: 'hbox',
                        pack: 'start'
                    },
                    items: [{
                        xtype: 'image',
                        src: 'img/eavl-banner.png',
                        height: 100
                    }]
                },{
                    xtype: 'panel',
                    region: 'center',
                    layout: {
                        type: 'hbox',
                        align : 'stretch',
                        pack : 'center'
                    },
                    bodyPadding : '50 100 50 100',
                    items: [{
                        itemId : 'csvpanel',
                        xtype: 'csvgrid',
                        title : 'Uploaded data file',
                        parameterDetails : parameterDetails,
                        flex : 1,
                        margin : '0 10 0 0'
                    },{
                        itemId : 'inspectpanel',
                        xtype : 'pdpanel',
                        title : 'Parameter Details',
                        emptyText : 'Drag a column header into this panel to inspect it.',
                        flex : 1,
                        margin : '0 0 0 10',
                        listeners : {
                            afterrender : function(pdpanel) {

                                //OK - we hack into the grid's header's reordering plugin to pull out
                                //the drag/drop group ID. We create a new drop zone using that ID
                                //and apply it to the inspect panel.
                                var csvPanel = pdpanel.ownerCt.queryById('csvpanel');
                                var header = csvPanel.getView().headerCt;
                                var reorderPlugin = header.plugins[1]; //header.getPlugin doesn't work during afterrender
                                var ddGroup = reorderPlugin.dropZone.ddGroup;

                                var body = pdpanel.body;
                                pdpanel.dropTarget = new Ext.dd.DropTarget(body, {
                                    ddGroup : ddGroup,
                                    notifyEnter: function(ddSource, e, data) {
                                        body.stopAnimation();
                                        body.highlight();
                                    },
                                    notifyDrop: function(ddSource, e, data) {
                                        var paramDetails = pdStore.getById(data.header.itemId)
                                        pdpanel.showParameterDetails(paramDetails);
                                        return true
                                    }
                                });
                            },
                            beforeDestroy: function() {
                                if (this.dropTarget) {
                                    this.dropTarget.unreg();
                                    this.dropTarget = null;
                                }
                                this.callParent();
                            }
                        }
                    }]
                }]
            });
        };


        var pdStore = Ext.create('Ext.data.Store', {
            model : 'eavl.models.ParameterDetails',
            autoLoad : true,
            proxy : {
                type : 'ajax',
                url : 'validation/getParameterDetails.do',
                reader : {
                    type : 'json',
                    root : 'data'
                }
            },
            listeners: {
                load : function(pdStore, records, successful, eOpts) {
                    if (successful) {
                        initSuccess(records)
                    } else {
                        initError();
                    }
                }
            }
        });
    }

});
