/**
 * Controls the Set Proxy page
 */
Ext.application({
    name : 'eavl-setproxy',

    init: function() {
        eavl.widget.SplashScren.showLoadingSplash('Loading Proxy Selection, please stand by ...');
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

        var initNotReady = function(message, url) {
            eavl.widget.SplashScren.hideLoadingScreen();
            eavl.widget.SplashScren.showErrorSplash(message + Ext.util.Format.format('<br><a href="{0}">Continue</a>', url));
        };

        var initSuccess = function(records) {
            eavl.widget.SplashScren.hideLoadingScreen();

            Ext.tip.QuickTipManager.init();

            Ext.app.Application.viewport = Ext.create('Ext.container.Viewport', {
                layout: 'border',
                items: [{
                    xtype: 'workflowpanel',
                    region: 'north',
                    allowNext: function(callback) {
                        callback(false);
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
                        parameterDetails : records,
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
                            xtype: 'container',
                            flex: 1,
                            layout: {
                                type: 'vbox',
                                align : 'center',
                                pack : 'center'
                            },
                            items : [{
                                xtype : 'pdfield',
                                id : 'proxy1-field',
                                width: '100%',
                                title: 'Proxy 1',
                                height: 80,
                                emptyText : 'Drag a parameter here to select it.',
                                margins: '0 0 10 0',
                                allowBlank: false,
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

                                        pdfield.ownerCt.down('#proxy-panel-1').showParameterDetails(pd);
                                    },
                                    handleDrag : function(pdfield, pd) {
                                        pdfield.reset();
                                        pdfield.ownerCt.down('#proxy-panel-1').hideParameterDetails();
                                    }
                                }]
                            },{
                                xtype: 'proxypanel',
                                itemId: 'proxy-panel-1',
                                width: '100%',
                                emptyText: 'Drag a parameter above to select it as a proxy',
                                flex: 1
                            }]
                        }]
                    }]
                }]
            });
        };

        var pdStore = Ext.create('Ext.data.Store', {
            model : 'eavl.models.ParameterDetails',
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
            url: 'setproxy/getImputationStatus.do',
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

                if (responseObj.data == true) {
                    pdStore.load();
                    return;
                }

                //At this point imputation hasn't been run/hasn't finished
                if (responseObj.msg === "nodata") {
                    initNotReady("There's no record of an imputation task running for this job. Did you complete the prediction steps?", "setprediction.html");
                    return;
                }
                if (responseObj.msg === "nojob") {
                    initNotReady("There's no job selected. Did you upload a file?", "upload.html");
                    return;
                }
                if (responseObj.msg === "failed") {
                    initNotReady("Imputation failed. You can try resubmitting.", "setprediction.html");
                    return;
                }

                //OK imputation is running - shift to loading page
                window.location.href = "taskwait.html?" + Ext.Object.toQueryString({taskId: responseObj.msg, next: 'imputation.html'});
            }
        });
    }

});
