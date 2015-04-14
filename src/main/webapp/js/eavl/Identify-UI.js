/**
 * Controls the Identify page
 */
Ext.application({
    name : 'eavl-identify',

    init: function() {
        eavl.widgets.SplashScreen.showLoadingSplash('Loading Identification, please stand by ...');
    },

    viewport : null,

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {
        var initialConfig = null;

        //Called if the init code fails badly
        var initError = function() {
            eavl.widgets.SplashScreen.hideLoadingScreen();
            eavl.widgets.SplashScreen.showErrorSplash('There was an error loading your data. Please try refreshing the page or contacting ' + eavl.widgets.FeedbackWidget.CONTACT + ' if the problem persists.');
        };

        var initSuccess = function(parameterDetails) {
            eavl.widgets.SplashScreen.hideLoadingScreen();
            Ext.tip.QuickTipManager.init();

            var predictionPd = null;
            if (initialConfig.predictionParameter) {
                predictionPd = eavl.models.ParameterDetails.extractFromArray(parameterDetails, initialConfig.predictionParameter);
            }

            var holeIdPd = null;
            if (initialConfig.holeIdParameter) {
                holeIdPd = eavl.models.ParameterDetails.extractFromArray(parameterDetails, initialConfig.holeIdParameter);
            }

            var savedParamDetails = [];
            Ext.each(initialConfig.savedParameters, function(paramName) {
                if (paramName !== initialConfig.holeIdParameter) {
                    var savePd = eavl.models.ParameterDetails.extractFromArray(parameterDetails, paramName)
                    if (savePd) {
                        savedParamDetails.push(savePd);
                    }
                }
            });


            Ext.app.Application.viewport = Ext.create('Ext.container.Viewport', {
                layout: 'border',
                items: [{
                    xtype: 'workflowpanel',
                    region: 'north',
                    allowNext: function(callback) {
                        var holeField = Ext.getCmp("holeid-field");
                        var holePd = holeField.getValue();
                        if (holePd == null) {
                            eavl.widgets.util.HighlightUtil.highlight(holeField, eavl.widgets.util.HighlightUtil.ERROR_COLOR);
                            callback(false);
                            return;
                        }

                        var predictorField = Ext.getCmp("predictor-field");
                        var predictorPd = predictorField.getValue();
                        if (predictorPd == null) {
                            eavl.widgets.util.HighlightUtil.highlight(predictorField, eavl.widgets.util.HighlightUtil.ERROR_COLOR);
                            callback(false);
                            return;
                        }



                        var ds = Ext.getCmp("noncomppanel").getStore();
                        var saveColNames = [];
                        for (var i = 0; i < ds.getCount(); i++) {
                            saveColNames.push(ds.getAt(i).get('name'));
                        }

                        eavl.widgets.SplashScreen.showLoadingSplash("Saving selection...");
                        Ext.Ajax.request({
                            url: 'identify/saveConfig.do',
                            params : {
                                saveColName : saveColNames,
                                predictorName : predictorPd.get('name'),
                                holeIdName : holePd.get('name')
                            },
                            callback : function(options, success, response) {
                                eavl.widgets.SplashScreen.hideLoadingScreen();

                                if (!success) {
                                    callback(false);
                                    return;
                                }

                                var responseObj = Ext.JSON.decode(response.responseText);
                                if (!responseObj.success) {
                                    callback(false);
                                    return;
                                }

                                callback(true);
                            }
                        });
                    }
                },{
                    xtype: 'panel',
                    region: 'center',
                    border: false,
                    layout: {
                        type: 'hbox',
                        align : 'stretch',
                        pack : 'center'
                    },
                    bodyPadding : '10 10 10 10',
                    items: [{
                        id : 'noncomppanel',
                        xtype : 'pdlist',
                        disableSelection: true,
                        title : 'Non Compositional Parameters',
                        width: 300,
                        sortFn : eavl.models.ParameterDetails.sortColIndexFn,
                        parameterDetails: savedParamDetails,
                        viewConfig : {
                            deferEmptyText : false,
                            emptyText : '<div class="save-empty-container"><div class="save-empty-container-inner"><img src="img/save.svg" width="100"/><br>Drag a parameter here to exclude it from calculations but include it in the final results.</div></div>'
                        },
                        plugins : [{
                            ptype : 'modeldnd',
                            ddGroup : 'identify-dnd-pd',
                            highlightBody : false,
                            handleDrop : function(pdlist, pd) {
                                pdlist.getStore().add(pd);
                            },
                            handleDrag : function(pdlist, pd, source) {
                                if (source == Ext.getCmp("pdpanel")) {
                                    return;
                                }
                                pdlist.getStore().remove(pd);
                            }
                        },{
                            ptype: 'headerhelp',
                            text: 'These parameters will not be imputed or used in calculations. They will however be included in the final output.'
                        }]
                    },{
                        id : 'comppanel',
                        xtype : 'pdlist',
                        title : 'Compositional Parameters',
                        width : 300,
                        margin: '0 10 0 10',
                        parameterDetails : parameterDetails,
                        sortFn : eavl.models.ParameterDetails.sortColIndexFn,
                        viewConfig : {
                            deferEmptyText : false,
                            emptyText : '<div class="trash-empty-container"><div class="trash-empty-container-inner">No parameters could be extracted. Try uploading again.</div></div>'
                        },
                        plugins : [{
                            ptype : 'modeldnd',
                            ddGroup : 'identify-dnd-pd',
                            highlightBody : false,
                            handleDrop : function(pdlist, pd) {
                                pdlist.getStore().add(pd);
                            },
                            handleDrag : function(pdlist, pd, source) {
                                if (source == Ext.getCmp("pdpanel")) {
                                    return;
                                }
                                pdlist.getStore().remove(pd);
                            }
                        },{
                            ptype: 'headerhelp',
                            text: 'These parameters will be imputed and used in conditional probability calculations.'
                        }]
                    },{
                        xtype: 'container',
                        flex: 1,
                        layout: 'vbox',
                        margin: '0 10 0 0',
                        items : [{
                            xtype : 'pdfield',
                            id : 'holeid-field',
                            width: '100%',
                            title: 'Hole Identifier',
                            height: 85,
                            emptyText : 'Drag a parameter here to select it.',
                            margin: '0 0 10 0',
                            allowBlank: false,
                            value: holeIdPd,
                            plugins: [{
                                ptype : 'modeldnd',
                                ddGroup : 'identify-dnd-pd',
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
                            },{
                                ptype: 'headerhelp',
                                text: 'This parameter will be used to group measurements that share the same identifier.'
                            }]
                        },{
                            xtype : 'pdfield',
                            id : 'predictor-field',
                            width: '100%',
                            title: 'Element to predict',
                            height: 85,
                            emptyText : 'Drag a parameter here to select it.',
                            margin: '0 0 10 0',
                            allowBlank: false,
                            value: predictionPd,
                            plugins: [{
                                ptype : 'modeldnd',
                                ddGroup : 'identify-dnd-pd',
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
                        }]
                    }]
                }]
            });
        };

        Ext.Ajax.request({
            url : 'identify/getConfig.do',
            callback : function(options, success, response) {
                if (!success) {
                    initError();
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    initError();
                    return;
                }

                var pds = [];
                Ext.each(responseObj.data.parameterDetails, function(pd) {
                    pds.push(Ext.create('eavl.models.ParameterDetails', pd));
                })
                initialConfig = responseObj.data.job;
                initSuccess(pds);
            }
        })
    }

});
