/**
 * Controls the Validate page
 */
Ext.application({
    name : 'eavl-validate',

    init: function() {
        eavl.widget.SplashScren.showLoadingSplash('Loading Validator, please stand by ...');
    },

    viewport : null,

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

            Ext.tip.QuickTipManager.init();

            //If we are just reloading the store, tell our widgets to update instead of recreating everything
            if (Ext.app.Application.viewport) {
                Ext.app.Application.viewport.queryById('csvpanel').reloadParameterDetails(parameterDetails);

                //Update parameter details panel
                var pdpanel = Ext.app.Application.viewport.queryById('pdpanel');
                var newPd = null;
                if (pdpanel.parameterDetails) {
                    var name = pdpanel.parameterDetails.get('name');
                    Ext.each(parameterDetails, function(pd) {
                       if (pd.get('name') === name) {
                           newPd = pd;
                       }
                    });
                }
                pdpanel.showParameterDetails(newPd);
                return;
            }
            Ext.app.Application.viewport = Ext.create('Ext.container.Viewport', {
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
                        itemId : 'trashpanel',
                        xtype : 'pdlist',
                        title : 'Trashed Parameters',
                        width : 200,
                        margin : '0 10 0 0',
                        viewConfig : {
                            emptyText : '<h1>TESTING</h1><br><p>HTML</p>'
                        },
                        plugins : [{
                            ptype : 'headerdraglink',
                            removeOnDrop : true,
                            allowDrag : true,
                            grid : function(pdlist) {return pdlist.ownerCt.queryById('csvpanel');},
                            dropFn : function(pdlist, columnId) {
                                var paramDetails = pdStore.getById(columnId)
                                pdlist.getStore().add(paramDetails);
                            }
                        }]
                    },{
                        itemId : 'csvpanel',
                        xtype: 'csvgrid',
                        title : 'Uploaded data file',
                        parameterDetails : parameterDetails,
                        flex : 1,
                        margin : '0 10 0 10',
                        listeners : {
                            parameterchanged : function(pdpanel, parameterDetails) {
                                eavl.widget.SplashScren.showLoadingSplash('Reloading CSV Data...');
                                pdStore.load();
                            }
                        }
                    },{
                        itemId : 'pdpanel',
                        xtype : 'pdpanel',
                        title : 'Parameter Details',
                        emptyText : 'Drag a column header into this panel to inspect it.',
                        flex : 1,
                        margin : '0 0 0 10',
                        plugins : [{
                            ptype : 'headerdraglink',
                            grid : function(pdpanel) {return pdpanel.ownerCt.queryById('csvpanel');},
                            dropFn : function(pdpanel, columnId) {
                                var paramDetails = pdStore.getById(columnId)
                                pdpanel.showParameterDetails(paramDetails);
                            }
                        }],
                        listeners : {
                            parameterchanged : function(pdpanel, parameterDetails) {
                                eavl.widget.SplashScren.showLoadingSplash('Reloading CSV Data...');
                                pdStore.load();
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
