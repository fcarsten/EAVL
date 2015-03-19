/**
 * Controls the Set Proxy page
 */
Ext.application({
    name : 'eavl-setproxy',

    init: function() {
        eavl.widgets.SplashScreen.showLoadingSplash('Loading Proxy Selection, please stand by ...');
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

        var initSuccess = function(records) {
            eavl.widgets.SplashScreen.hideLoadingScreen();

            Ext.tip.QuickTipManager.init();

            var p1Value = null;
            var p2Value = null;
            var p3Value = null;
            if (initialParams && initialParams.proxyParameters) {
                p1Value = eavl.models.ParameterDetails.extractFromArray(records, initialParams.proxyParameters[0]);
                p2Value = eavl.models.ParameterDetails.extractFromArray(records, initialParams.proxyParameters[1]);
                p3Value = eavl.models.ParameterDetails.extractFromArray(records, initialParams.proxyParameters[2]);
            }
            
            var p1Denoms = [];
            var p2Denoms = [];
            var p3Denoms = [];
            if (initialParams && initialParams.proxyDenominators) {
                p1Denoms = initialParams.proxyDenominators[0];
                p2Denoms = initialParams.proxyDenominators[1];
                p3Denoms = initialParams.proxyDenominators[2];
            }

            Ext.app.Application.viewport = Ext.create('Ext.container.Viewport', {
                layout: 'border',
                items: [{
                    xtype: 'workflowpanel',
                    region: 'north',
                    allowNext: function(callback) {
                        var pdField1 = Ext.getCmp('setproxy-1').down('#pdfield');
                        if (!pdField1.isValid()) {
                            callback(false);
                            return;
                        }

                        var pdField2 = Ext.getCmp('setproxy-2').down('#pdfield');
                        if (!pdField2.isValid()) {
                            callback(false);
                            return;
                        }

                        var pdField3 = Ext.getCmp('setproxy-3').down('#pdfield');
                        if (!pdField3.isValid()) {
                            callback(false);
                            return;
                        }

                        Ext.Ajax.request({
                            url: 'setproxy/saveAndSubmitProxySelection.do',
                            params : {
                                proxy : [pdField1.getValue().get('name'),
                                         pdField2.getValue().get('name'),
                                         pdField3.getValue().get('name')]
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

                                callback("taskwait.html?" + Ext.Object.toQueryString({taskId: responseObj.data, next: 'results.html'}));
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
                    items: [{
                        xtype : 'pdlist',
                        title : 'Compositional Parameters',
                        width: 300,
                        parameterDetails : records,
                        disableSelection: true,
                        margin: '0 10 0 10',
                        plugins: [{
                            ptype : 'modeldnd',
                            ddGroup : 'set-proxy-pd',
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
                            emptyText : '<div class="pdlist-empty-container"><div class="pdlist-empty-container-inner">No parameters available.</div></div>'
                        }
                    },{
                        xtype: 'container',
                        flex: 1,
                        layout: {
                            type: 'hbox',
                            align : 'stretch',
                            pack : 'center'
                        },
                        items: [{
                            xtype: 'setproxyselection',
                            flex: 1,
                            title: 'Proxy 1',
                            margin: '0 10 0 0',
                            id: 'setproxy-1',
                            allPds : records,
                            pdNumerator: p1Value,
                            pdDenominator: p1Denoms
                        },{
                            xtype: 'setproxyselection',
                            flex: 1,
                            title: 'Proxy 2',
                            margin: '0 10 0 0',
                            id: 'setproxy-2',
                            allPds : records,
                            pdNumerator: p2Value,
                            pdDenominator: p2Denoms
                        },{
                            xtype: 'setproxyselection',
                            flex: 1,
                            title: 'Proxy 3',
                            margin: '0 10 0 0',
                            id: 'setproxy-3',
                            allPds : records,
                            pdNumerator: p3Value,
                            pdDenominator: p3Denoms
                        }]
                    }]
                }]
            });
        };

        var pdStore = Ext.create('Ext.data.Store', {
            model : 'eavl.models.ParameterDetails',
            proxy : {
                type : 'ajax',
                url : 'validation/getCompositionalParameterDetails.do',
                extraParams: {
                    file: eavl.models.EAVLJob.FILE_IMPUTED_CSV
                },
                reader : {
                    type : 'json',
                    rootProperty : 'data'
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

                if (responseObj.data.status === eavl.models.EAVLJob.STATUS_THRESHOLD ||
                    responseObj.data.status === eavl.models.EAVLJob.STATUS_PROXY ||
                    responseObj.data.status === eavl.models.EAVLJob.STATUS_SUBMITTED ||
                    responseObj.data.status === eavl.models.EAVLJob.STATUS_KDE_ERROR ||
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



/**
 * Internal grouping of a ParameterDetails field and ProxyDetailsPanel. Not really designed for
 * use outside of the SetProxy GUI
 */
Ext.define('eavl.setproxy.ProxySelectionPanel', {
    extend: 'Ext.container.Container',

    alias: 'widget.setproxyselection',

    constructor : function(config) {
        var me = this;

        var pd = config.pdNumerator ? config.pdNumerator : null;
        var denom = config.pdDenominator ? config.pdDenominator : null;
        var allPds = config.allPds ? config.allPds : null;
        
        Ext.apply(config, {
            xtype: 'container',
            layout: {
                type: 'vbox',
                align : 'center',
                pack : 'center'
            },
            items : [{
                xtype : 'pdfield',
                width: '100%',
                height: 85,
                title: config.title,
                itemId : 'pdfield',
                emptyText : 'Drag a parameter here to select it.',
                margin: '0 0 10 0',
                allowBlank: false,
                value: pd,
                plugins: [{
                    ptype : 'modeldnd',
                    ddGroup : 'set-proxy-pd',
                    highlightBody : false,
                    handleDrop : function(pdfield, pd, source) {
                        //Swap if we already have a value
                        if (pdfield.getValue()) {
                            var currentValue = pdfield.getValue();
                            source.getStore().add(currentValue);
                        }
                        pdfield.setValue(pd);

                        pdfield.ownerCt.down('#proxy-panel').showParameterDetails(pd);
                    },
                    handleDrag : function(pdfield, pd) {
                        pdfield.reset();
                        pdfield.ownerCt.down('#proxy-panel').hideParameterDetails();
                    }
                }]
            },{
                xtype : 'pdtagfield',
                width: '100%',
                title: config.title,
                itemId : 'pdtagfield',
                emptyText : 'Drag a parameter here to select it.',
                margin: '0 0 10 0',
                allowBlank: false,
                value: denom,
                parameterDetails: allPds,
                plugins: [{
                    ptype : 'modeldnd',
                    ddGroup : 'set-proxy-pd',
                    highlightBody : false,
                    handleDrop : function(pdfield, pd, source) {
                        //Swap if we already have a value
                        if (pdfield.getValue()) {
                            var currentValue = pdfield.getValue();
                            source.getStore().add(currentValue);
                        }
                        pdfield.setValue(pd);

                        pdfield.ownerCt.down('#proxy-panel').showParameterDetails(pd);
                    },
                    handleDrag : function(pdfield, pd) {
                        pdfield.reset();
                        pdfield.ownerCt.down('#proxy-panel').hideParameterDetails();
                    }
                }]
            },{
                xtype: 'proxypanel',
                itemId: 'proxy-panel',
                width: '100%',
                emptyText: 'Drag a parameter above to select it as a proxy',
                flex: 1,
                targetChartWidth: 400,
                targetChartHeight: 400,
                preserveAspectRatio: true,
                parameterDetails: pd,
                plugins: [{
                    ptype : 'modeldnd',
                    ddGroup : 'set-proxy-pd',
                    highlightBody : false,
                    handleDrop : function(proxypanel, pd, source) {
                        var pdfield = proxypanel.ownerCt.down("#pdfield");

                        //Swap if we already have a value
                        if (pdfield.getValue()) {
                            var currentValue = pdfield.getValue();
                            source.getStore().add(currentValue);
                        }
                        pdfield.setValue(pd);

                        proxypanel.showParameterDetails(pd);
                    },
                    handleDrag : function(pdfield, pd) {
                        pdfield.reset();
                        pdfield.ownerCt.down('#proxy-panel').hideParameterDetails();
                    }
                }]
            }]
        });

        this.callParent(arguments);
    }
});
