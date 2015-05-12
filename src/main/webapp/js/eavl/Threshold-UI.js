/**
 * Controls the Threshold page
 */
Ext.application({
    name : 'eavl-threshold',

    init: function() {
        eavl.widgets.SplashScreen.showLoadingSplash('Loading threshold selection, please stand by ...');
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
            eavl.widgets.SplashScreen.showErrorSplash('There was an error loading your data. Please try refreshing the page or contacting ' + eavl.widgets.FeedbackWidget.CONTACT + ' if the problem persists.');
        };

        var initNotReady = function(message, url) {
            eavl.widgets.SplashScreen.hideLoadingScreen();
            eavl.widgets.SplashScreen.showErrorSplash(message + Ext.util.Format.format('<br><a href="{0}">Continue</a>', url));
        };

        var initSuccess = function(predictionParam, predictionThreshold) {
            eavl.widgets.SplashScreen.hideLoadingScreen();

            Ext.tip.QuickTipManager.init();

            Ext.app.Application.viewport = Ext.create('Ext.container.Viewport', {
                layout: 'border',
                items: [{
                    xtype: 'workflowpanel',
                    region: 'north',
                    allowNext: function(callback) {
                        //Check our fields are all set, highlight what the user needs to do it they haven't finished it
                        var pdfChart = Ext.getCmp('threshold-pdf-chart');
                        var pdfCutoff = pdfChart.getCutoffValue();
                        if (pdfCutoff === null) {
                            eavl.widgets.util.HighlightUtil.highlight(pdfChart, eavl.widgets.util.HighlightUtil.ERROR_COLOR);
                            callback(false);
                            return;
                        }

                        eavl.widgets.SplashScreen.showLoadingSplash("Saving threshold...");
                        Ext.Ajax.request({
                            url: 'threshold/saveConfig.do',
                            params : {
                                predictorCutoff : pdfCutoff
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
                    border: false,
                    layout: {
                        type: 'vbox',
                        pack : 'center',
                        align: 'center'
                    },
                    style : {
                        'background-color': 'white'
                    },
                    items: [{
                        xtype: 'panel',
                        title: 'Drag to select threshold for the element to predict',
                        width: 1000,
                        height: 600,
                        layout: 'fit',
                        items : [{
                            xtype: 'pdfchart',
                            id: 'threshold-pdf-chart',
                            parameterDetails: predictionParam,
                            cutoffValue: predictionThreshold,
                            allowCutoffSelection : true,
                            targetWidth: 1000,
                            targetHeight: 600,
                            file: eavl.models.EAVLJob.FILE_IMPUTED_SCALED_CSV,
                            preserveAspectRatio: true
                        }]
                    }]
                }]
            });
        };
        
        var feedback = Ext.create('eavl.widgets.FeedbackWidget', {});

        //Before loading
        Ext.Ajax.request({
            url: 'threshold/getConfig.do',
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

                if (responseObj.data.job.status === eavl.models.EAVLJob.STATUS_THRESHOLD ||
                    responseObj.data.job.status === eavl.models.EAVLJob.STATUS_PROXY ||
                    responseObj.data.job.status === eavl.models.EAVLJob.STATUS_SUBMITTED ||
                    responseObj.data.job.status === eavl.models.EAVLJob.STATUS_KDE_ERROR ||
                    responseObj.data.job.status === eavl.models.EAVLJob.STATUS_DONE) {
                    var pds = [];
                    Ext.each(responseObj.data.parameterDetails, function(o) {
                        pds.push(Ext.create('eavl.models.ParameterDetails', o));
                    })
                    var pd = eavl.models.ParameterDetails.extractFromArray(pds, responseObj.data.job.predictionParameter);
                    initialParams = responseObj.data.job;
                    initSuccess(pd, responseObj.data.job.predictionCutoff);
                    return;
                }

                //At this point imputation hasn't been run/hasn't finished
                if (responseObj.data.job.status === eavl.models.EAVLJob.STATUS_UNSUBMITTED) {
                    initNotReady("There's no record of an imputation task running for this job. Did you complete the validation steps?", "validate.html");
                    return;
                }
                if (responseObj.data.job.status === eavl.models.EAVLJob.STATUS_IMPUTE_ERROR) {
                    initNotReady("Imputation failed. Did you remove all the non compositional parameters? You can try resubmitting.", "identify.html");
                    return;
                }

                //OK imputation is running - shift to loading page
                if (responseObj.data.job.status === eavl.models.EAVLJob.STATUS_IMPUTING) {
                    window.location.href = "../taskwait.html?" + Ext.Object.toQueryString({taskId: responseObj.data.job.imputationTaskId, next: 'cp/threshold.html'});
                }
            }
        });

    }

});
