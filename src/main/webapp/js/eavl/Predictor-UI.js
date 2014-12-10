/**
 * Controls the Validate page
 */
Ext.application({
    name : 'eavl-imputation',

    init: function() {
        eavl.widgets.SplashScreen.showLoadingSplash('Loading predictor setup, please stand by ...');
    },

    viewport : null,

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {

        var initialParams = null;

        //Called if the init code fails badly
        var initError = function() {
            eavl.widgets.SplashScreen.hideLoadingScreen();
            eavl.widgets.SplashScreen.showErrorSplash('There was an error loading your data. Please try refreshing the page or contacting cg-admin@csiro.au if the problem persists.');
        };

        var initNotReady = function(message, url) {
            eavl.widgets.SplashScreen.hideLoadingScreen();
            eavl.widgets.SplashScreen.showErrorSplash(message + Ext.util.Format.format('<br><a href="{0}">Continue</a>', url));
        };

        var initSuccess = function(parameterDetails) {
            eavl.widgets.SplashScreen.hideLoadingScreen();

            Ext.tip.QuickTipManager.init();

            var holeIdValue = null;
            var predictorValue = null;
            var cutoffValue = null;
            if (initialParams.holeIdParameter) {
                holeIdValue = eavl.models.ParameterDetails.extractFromArray(parameterDetails, initialParams.holeIdParameter);
            }
            if (initialParams.predictionParameter) {
                predictorValue = eavl.models.ParameterDetails.extractFromArray(parameterDetails, initialParams.predictionParameter);
            }
            if (initialParams.predictionCutoff) {
                cutoffValue = Number(initialParams.predictionCutoff);
            }

            Ext.app.Application.viewport = Ext.create('Ext.container.Viewport', {
                layout: 'border',
                items: [{
                    xtype: 'workflowpanel',
                    region: 'north',
                    allowNext: function(callback) {
                        //Check our fields are all set, highlight what the user needs to do it they haven't finished it
                        var predictorField = Ext.getCmp('predictor-field');
                        if (!predictorField.isValid()) {
                            callback(false);
                            return;
                        }
                        var predictorPd = predictorField.getValue();

                        var holeIdField = Ext.getCmp('holeid-field');
                        if (!holeIdField.isValid()) {
                            callback(false);
                            return;
                        }
                        var holeIdPd = holeIdField.getValue();

                        var pdfChart = Ext.getCmp('predictor-pdf-chart');
                        var pdfCutoff = pdfChart.getCutoffValue();
                        if (pdfCutoff === null) {
                            eavl.widgets.util.HighlightUtil.highlight(pdfChart, eavl.widgets.util.HighlightUtil.ERROR_COLOR);
                            callback(false);
                            return;
                        }

                        eavl.widgets.SplashScreen.showLoadingSplash("Saving predictor...");
                        Ext.Ajax.request({
                            url: 'predictor/savePrediction.do',
                            params : {
                                predictorCutoff : pdfCutoff,
                                predictorName : predictorPd.get('name'),
                                holeIdName : holeIdPd.get('name')
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
                    xtype: 'container',
                    region: 'center',
                    layout: {
                        type: 'hbox',
                        align : 'stretch',
                        pack : 'center'
                    },
                    items: [{
                        xtype : 'pdlist',
                        title : 'Available Parameters',
                        width: 300,
                        margins: '0 10 0 10',
                        disableSelection: true,
                        parameterDetails : parameterDetails,
                        plugins: [{
                            ptype : 'modeldnd',
                            ddGroup : 'set-prediction-pd',
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
                        xtype: 'container',
                        flex: 1,
                        layout: 'vbox',
                        margins: '0 10 0 0',
                        items : [{
                            xtype : 'pdfield',
                            id : 'holeid-field',
                            width: '100%',
                            title: 'Hole Identifier',
                            height: 80,
                            emptyText : 'Drag a parameter here to select it.',
                            margins: '0 0 10 0',
                            allowBlank: false,
                            value: holeIdValue,
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

                                    pdfield.ownerCt.down('#predictor-pdf-chart').clearPlot();
                                }
                            }]
                        },{
                            xtype : 'pdfield',
                            id : 'predictor-field',
                            width: '100%',
                            title: 'Predictor',
                            height: 80,
                            emptyText : 'Drag a parameter here to select it.',
                            margins: '0 0 10 0',
                            allowBlank: false,
                            value: predictorValue,
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

                                    pdfield.ownerCt.down('#predictor-pdf-chart').plotParameterDetails(pd);
                                },
                                handleDrag : function(pdfield, pd) {
                                    pdfield.reset();

                                    pdfield.ownerCt.down('#predictor-pdf-chart').clearPlot();
                                }
                            }]
                        },{
                            xtype: 'panel',
                            title: 'Drag to select cutoff for predictor',
                            width: '100%',
                            flex: 1,
                            layout: 'fit',
                            items : [{
                                xtype: 'pdfchart',
                                id: 'predictor-pdf-chart',
                                parameterDetails: predictorValue,
                                cutoffValue: cutoffValue,
                                allowCutoffSelection : true
                            }]
                        }]
                    }]
                }]
            });
        };


        var pdStore = Ext.create('Ext.data.Store', {
            model : 'eavl.models.ParameterDetails',
            autoLoad : false,
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

        //Before loading
        Ext.Ajax.request({
            url: 'results/getJobStatus.do',
            callback: function(options, success, response) {
                if (!success) {
                    initError();
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    initError();
                    return;
                }

                if (responseObj.data.status === eavl.models.EAVLJob.STATUS_PREDICTOR ||
                    responseObj.data.status === eavl.models.EAVLJob.STATUS_PROXY ||
                    responseObj.data.status === eavl.models.EAVLJob.STATUS_SUBMITTED ||
                    responseObj.data.status === eavl.models.EAVLJob.STATUS_DONE) {
                    initialParams = responseObj.data;
                    pdStore.load();
                    return;
                }

                //At this point imputation hasn't been run/hasn't finished
                if (responseObj.data.status === eavl.models.EAVLJob.STATUS_UNSUBMITTED) {
                    initNotReady("There's no record of an imputation task running for this job. Did you complete the validation steps?", "validate.html");
                    return;
                }
                if (responseObj.data.status === eavl.models.EAVLJob.STATUS_IMPUTE_ERROR) {
                    initNotReady("Imputation failed. Did you remove all the non compositional parameters? You can try resubmitting.", "validate.html");
                    return;
                }

                //OK imputation is running - shift to loading page
                if (responseObj.data.status === eavl.models.EAVLJob.STATUS_IMPUTING) {
                    window.location.href = "taskwait.html?" + Ext.Object.toQueryString({taskId: responseObj.data.imputationTaskId, next: 'predictor.html'});
                }
            }
        });

    }

});
