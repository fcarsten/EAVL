/**
 * Window extension for rendering metadata about a given eavl.models.EAVLJob
 */
Ext.define('eavl.widgets.JobInfoWindow', {
    extend: 'eavl.widgets.EAVLModalWindow',
    
    alias: 'widget.jobinfowindow',

    /**
     * Adds the following config to Ext.grid.Panel
     * {
     *  job: eavl.models.EAVLJob - Job to display
     * }
     *
     * Adds the following events
     * {
     *  
     * }
     */
    constructor : function(config) {
        this.job = config.job;

        var impDateString = Ext.util.Format.date(config.job.get('imputationSubmitDate'));
        if (Ext.isEmpty(impDateString)) {
            impDateString = 'N/A';
        }
        
        var kdeDateString = Ext.util.Format.date(config.job.get('kdeSubmitDate'));
        if (Ext.isEmpty(kdeDateString)) {
            kdeDateString = 'N/A';
        }
        
        var predictionString = config.job.get('predictionParameter');
        if (Ext.isEmpty(predictionString)) {
            predictionString = 'N/A';
        }
        
        var thresholdString = '';
        if (config.job.get('predictionCutoff') !== null) {
            thresholdString = '~' + Ext.util.Format.number(config.job.get('predictionCutoff'), '0.0000');
        } else {
            thresholdString = 'N/A';
        }
        
        var bottomPanelItems = null;
        if (Ext.isEmpty(config.job.get('proxyParameters'))) {
            bottomPanelItems = [{
                xtype: 'container',
                height: '100%',
                html: '<div class="eavl-job-info-nothing-text">No proxies have been selected for this job.</div>'
            }];
        } else {
            bottomPanelItems = [{
                xtype: 'jobinfowindow-proxypanel',
                title: config.job.get('proxyParameters')[0].displayName ? config.job.get('proxyParameters')[0].displayName : '<Untitled Proxy Ratio>',
                flex: 1,
                margin: '0 10 0 0',
                pp: config.job.get('proxyParameters')[0]
            },{
                xtype: 'jobinfowindow-proxypanel',
                title: config.job.get('proxyParameters')[1].displayName ? config.job.get('proxyParameters')[1].displayName : '<Untitled Proxy Ratio>',
                flex: 1,
                margin: '0 10 0 0',
                pp: config.job.get('proxyParameters')[1]
            },{
                xtype: 'jobinfowindow-proxypanel',
                title: config.job.get('proxyParameters')[2].displayName ? config.job.get('proxyParameters')[2].displayName : '<Untitled Proxy Ratio>',
                flex: 1,
                pp: config.job.get('proxyParameters')[2]
            }];
        }
        
        
        Ext.apply(config, {
            width: 1000,
            height: 600,
            layout: 'fit',
            items: [{
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    pack: 'start',
                    align: 'stretch'
                },
                items: [{
                    xtype: 'container',
                    layout: {
                        type: 'hbox',
                        pack: 'center'
                    },
                    cls: 'eavl-job-info-window',
                    height: 160,
                    padding: '30 0 0 0',
                    items: [{
                        xtype: 'datadisplayfield',
                        fieldLabel: 'Predicted Parameter',
                        margin: '0 20 0 0',
                        value: predictionString
                    },{
                        xtype: 'datadisplayfield',
                        fieldLabel: 'Threshold',
                        margin: '0 20 0 20',
                        value: thresholdString
                    },{
                        xtype: 'datadisplayfield',
                        fieldLabel: 'Imputation Submit Date',
                        margin: '0 20 0 20',
                        value: impDateString
                    },{
                        xtype: 'datadisplayfield',
                        fieldLabel: 'KDE Submit Date',
                        margin: '0 0 0 20',
                        value: kdeDateString
                    }]
                },{
                    xtype: 'container',
                    flex: 1,
                    layout: {
                        type: 'hbox',
                        pack: 'center'
                    },
                    items: bottomPanelItems
                }]
            }],
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'bottom',
                items: [{
                    xtype: 'tbfill'
                },{
                    xtype: 'button',
                    cls: 'important-button',
                    scale: 'large',
                    text: 'OK',
                    handler: function(btn) {
                        btn.findParentByType('jobinfowindow').close();
                    }
                }]
            }]
        });

        this.callParent(arguments);
    }
    
});

Ext.define('eavl.widgets.JobInfoWindow.ProxyPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.jobinfowindow-proxypanel',
    
    constructor: function(config) {
        this.pp = config.pp;
        
        //To save a lookup - fake the parameter detail list from the list of numerator/denominators
        var fakePdList = [];
        if (config.pp) {
            Ext.each(config.pp.denom, function(pdName) {
                fakePdList.push(Ext.create('eavl.models.ParameterDetails', {
                    name: pdName,
                    totalNumeric: 1
                }));
            });
        }
        
        var numValue = null;
        if (this.pp) {
            numValue = Ext.create('eavl.models.ParameterDetails', {name: this.pp.numerator, totalNumeric: 1});
        }
        
        Ext.apply(config, {
            width: 200,
            bodyStyle: {
                'border-style': 'none'
            },
            layout: {
                type: 'vbox',
                pack: 'center',
                align: 'stretch'
            },
            items: [{
                xtype: 'pdfield',
                margin: '0 0 10 0',
                value: numValue
            },{
                xtype: 'pdtagfield',
                parameterDetails: fakePdList,
                readOnly: true,
                height: Math.floor(25 *  (1 + (fakePdList.length / 3))), //tagfield grow is broken in 5.1 This is our workaround
                value: fakePdList,
            }]
        });
        this.callParent(arguments);
    }
});