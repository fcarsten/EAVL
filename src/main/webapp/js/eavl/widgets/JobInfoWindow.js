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
        
        Ext.apply(config, {
            width: 1000,
            height: 600,
            title: config.job.get('name'),
            subtitle: config.job.get('status'),
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
                    height: 100,
                    items: [{
                        xtype: 'datadisplayfield',
                        fieldLabel: 'Predicted Parameter',
                        margin: '0 10 0 0',
                        value: config.job.get('predictionParameter')
                    },{
                        xtype: 'datadisplayfield',
                        fieldLabel: 'Threshold',
                        margin: '0 10 0 10',
                        value: '~' + Ext.util.Format.number(config.job.get('predictionCutoff'), '0.0000')
                    },{
                        xtype: 'datadisplayfield',
                        fieldLabel: 'Imputation Submit Date',
                        margin: '0 10 0 10',
                        value: impDateString
                    },{
                        xtype: 'datadisplayfield',
                        fieldLabel: 'KDE Submit Date',
                        margin: '0 0 0 10',
                        value: kdeDateString
                    }]
                },{
                    xtype: 'container',
                    flex: 1,
                    layout: {
                        type: 'hbox',
                        pack: 'center'
                    },
                    items: [{
                        xtype: 'jobinfowindow-proxypanel',
                        title: 'Proxy Ratio 1',
                        flex: 1,
                        margin: '0 10 0 0',
                        pp: config.job.get('proxyParameters')[0]
                    },{
                        xtype: 'jobinfowindow-proxypanel',
                        title: 'Proxy Ratio 2',
                        flex: 1,
                        margin: '0 10 0 0',
                        pp: config.job.get('proxyParameters')[1]
                    },{
                        xtype: 'jobinfowindow-proxypanel',
                        title: 'Proxy Ratio 3',
                        flex: 1,
                        pp: config.job.get('proxyParameters')[2]
                    }]
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
                height: Math.floor(25 *  (1 + (fakePdList.length / 2))), //tagfield grow is broken in 5.1 This is our workaround
                value: fakePdList,
            }]
        });
        this.callParent(arguments);
    }
});