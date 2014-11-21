/**
 * Controls the Set Proxy page
 */
Ext.application({
    name : 'eavl-results',

    init: function() {
        eavl.widgets.SplashScreen.showLoadingSplash('Loading results browser, please stand by ...');
    },

    viewport : null,

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {
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
                        xtype : 'eavljoblist',
                        title : 'Available Jobs',
                        width: 300,
                        jobs : records,
                        viewConfig : {
                            deferEmptyText : false,
                            emptyText : '<div class="jobs-empty-container"><div class="jobs-empty-container-inner">No jobs available.</div></div>'
                        },
                        listeners: {
                            select: function(sm, job) {
                                Ext.getCmp('jobfilelist').showFilesForJob(job);
                            }
                        }
                    },{
                        xtype: 'container',
                        flex: 1,
                        layout: {
                            type: 'vbox',
                            align : 'center',
                            pack : 'center'
                        },
                        items: [{
                            xtype: 'jobfilelist',
                            id: 'jobfilelist',
                            width: '100%',
                            flex: 1,
                            title: 'File Browser'
                        },{
                            xtype: 'panel',
                            flex: 1,
                            title: 'File Preview',
                            html: 'TODO'
                        }]
                    }]
                }]
            });
        };

        var jobStore = Ext.create('Ext.data.Store', {
            model : 'eavl.models.EAVLJob',
            autoLoad: true,
            proxy : {
                type : 'ajax',
                url : 'results/getJobsForUser.do',
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
