/**
 * Controls the Upload page
 */
Ext.application({
    name : 'eavl-taskwait',


    init: function() {
        eavl.widgets.SplashScreen.showLoadingSplash('Please stand by ...');
    },

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {

        eavl.widgets.SplashScreen.hideLoadingScreen();


        var urlParams = Ext.Object.fromQueryString(window.location.search.substring(1));
        var taskId = urlParams.taskId ? urlParams.taskId : '';
        var next = urlParams.next ? urlParams.next : '';

        var taskName = "???";
        if (next.endsWith("setproxy.html")) {
            taskName = "Imputation";
        } else if (next.endsWith("results.html")) {
            taskName = "Calculations";
        }

        var viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items : [{
                xtype: 'workflowpanel',
                region: 'north',
                height: 200,
                hideNavigator : true,
                urlOverride : next,
                allowNext : function(callback) {
                    callback(Ext.getCmp('upload-form').getForm().isValid());
                }
            },{
                xtype: 'container',
                region: 'center',
                style: {
                    'background-color' : 'white'
                },
                layout : {
                    type: 'vbox',
                    pack: 'start',
                    align: 'center'
                },
                items : [{
                    xtype: 'container',
                    height : 200,
                    margin: '100 0 0 0',
                    width : '100%',
                    html : Ext.DomHelper.markup({
                        tag : 'div',
                        cls : 'loading-box',
                        children : [{
                            tag : 'img',
                            src : 'img/loading-bars.svg'
                        },{
                            tag : 'h1',
                            html : 'Performing ' + taskName + "..."
                        },{
                            tag : 'div',
                            html : 'This page will refresh when complete.'
                        }]
                    })
                },{
                    xtype: 'container',
                    margins: '20 0 0 0',
                    height: 200,
                    layout : {
                        type: 'vbox',
                        pack: 'center',
                        align: 'center'
                    },
                    items : [{
                        xtype: 'checkboxfield',
                        itemId: 'check',
                        boxLabel: 'Email me when this is done',
                        cls: 'cb-email',
                        listeners : {
                            change: function(cb, newValue, oldValue) {
                                cb.ownerCt.down('#loadingimg').show();
                                cb.ownerCt.down('#statuslabel').hide();
                                Ext.Ajax.request({
                                    url: 'taskwait/setEmailNotification.do',
                                    params: {
                                        notify: newValue ? 'true' : 'false',
                                        taskId: taskId
                                    },
                                    callback: function(options, success, response) {
                                        var statusMsg = "";
                                        if (!success || !Ext.JSON.decode(response.responseText).success) {
                                            statusMsg = "Oops, there was a problem changing your email notification settings.";
                                        } else if (newValue) {
                                            statusMsg = "We'll email you when this task finishes.";
                                        } else {
                                            statusMsg = "You've been unsubscribed.";
                                        }

                                        cb.ownerCt.down('#loadingimg').hide();
                                        cb.ownerCt.down('#statuslabel').setText(statusMsg);
                                        cb.ownerCt.down('#statuslabel').show();
                                    }
                                });
                            }
                        }
                    },{
                        xtype: 'image',
                        itemId: 'loadingimg',
                        src: 'img/loading.gif',
                        width: 16,
                        height: 16,
                        hidden: true
                    },{
                        xtype: 'label',
                        cls: 'lbl-status',
                        itemId: 'statuslabel',
                        hidden: true,
                    }]
                }]
            }]
        });

        //We poll for requests
        Ext.TaskManager.start({
            interval: 1000 * 10, //every 10 seconds
            run: function() {
                Ext.Ajax.request({
                    url: 'taskwait/isExecuting.do',
                    params: {
                        taskId: taskId
                    },
                    callback: function(options, success, response) {
                        if (success) {
                            var responseObj = Ext.JSON.decode(response.responseText);
                            if (responseObj.success && !responseObj.data) {
                                window.location.href = next;
                            }
                        }
                    }
                });
            }
        });

    }
});
