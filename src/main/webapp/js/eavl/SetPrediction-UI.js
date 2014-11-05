/**
 * Controls the Validate page
 */
Ext.application({
    name : 'eavl-setprediction',

    init: function() {
        eavl.widget.SplashScren.showLoadingSplash('Loading Prediction Selection, please stand by ...');
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

            Ext.app.Application.viewport = Ext.create('Ext.container.Viewport', {
                layout: 'border',
                items: [{
                    xtype: 'workflowpanel',
                    region: 'north'
                },{
                    xtype: 'container',
                    region: 'center',
                    layout: {
                        type: 'hbox',
                        align : 'stretch',
                        pack : 'center'
                    },
                    items: [{
                        xtype : 'container',
                        width: 300,
                        layout: 'vbox',
                        margins: '0 10 0 10',
                        items: [{
                            xtype : 'pdlist',
                            title : 'Available Parameters',
                            width: '100%',
                            flex : 1,
                            parameterDetails : parameterDetails,
                            plugins: [{
                                ptype : 'modeldnd',
                                ddGroup : 'set-prediction-pd',
                                drag : true,
                                drop : false,
                                highlightBody : false,
                                handleDrop : function(pdlist, pd) {
                                    pdlist.getStore().add(pd);
                                },
                                handleDrag : function(pdlist, pd) {
                                    pdlist.getStore().remove(pd);
                                }
                            }],
                            viewConfig : {
                                deferEmptyText : false,
                                emptyText : '<div class="save-empty-container"><div class="save-empty-container-inner">You will want at least three parameters here to serve as proxies for the predictor.</div></div>'
                            }
                        },{
                            xtype : 'pdlist',
                            title : 'Saved Parameters',
                            width: '100%',
                            height : 250,
                            margins: '10 0 0 0',
                            plugins: [{
                                ptype : 'modeldnd',
                                ddGroup : 'set-prediction-pd',
                                drag : false,
                                drop : true,
                                highlightBody : false,
                                handleDrop : function(pdlist, pd) {
                                    pdlist.getStore().add(pd);
                                },
                                handleDrag : function(pdlist, pd) {
                                    pdlist.getStore().remove(pd);
                                }
                            }],
                            viewConfig : {
                                deferEmptyText : false,
                                emptyText : '<div class="save-empty-container"><div class="save-empty-container-inner"><img src="img/save.svg" width="100"/><br>Drag a parameter here to save it<br>(But not use it in any calculations)</div></div>'
                            }
                        }]
                    },{
                        xtype: 'container',
                        flex: 1,
                        layout: 'vbox',
                        margins: '0 10 0 0',
                        items : [{
                            xtype : 'pdfield',
                            width: '100%',
                            title: 'Predictor',
                            height: 80,
                            emptyText : 'Drag a parameter here to select it.',
                            margins: '0 0 10 0',
                            plugins: [{
                                ptype : 'modeldnd',
                                ddGroup : 'set-prediction-pd',
                                highlightBody : false,
                                handleDrop : function(pdfield, pd, source) {
                                    //Swap if we already have a value
                                    if (pdfield.getValue()) {
                                        var currentValue = pdfield.getValue();
                                        source.getStore().add(currentValue);
                                    }
                                    pdfield.setValue(pd);
                                },
                                handleDrag : function(pdfield, pd) {
                                    pdfield.reset();
                                }
                            }]
                        },{
                            xtype: 'panel',
                            title : 'PDF Graph goes in here',
                            width: '100%',
                            flex : 1
                        }]
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
