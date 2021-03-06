/**
 * Panel extension for rendering a single eavl.models.Workflow object in detail
 */
Ext.define('eavl.widgets.WorkflowInspectPanel', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.workflowinspectpanel',

    workflow : null,

    /**
     * Adds the following config to Ext.panel.Panel
     * {
     *  workflow: eavl.models.Workflow 
     * }
     *
     * Adds the following events
     * {
     *  
     * }
     */
    constructor : function(config) {
        this.workflow = config.workflow;
      
        
        var contactStore = Ext.create('Ext.data.Store',{
            model: 'eavl.models.Contact',
            groupField: 'group',
            groupDir: 'DESC'
        });
        contactStore.loadData(config.workflow.get('contacts'));
        
        Ext.apply(config, {
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            bodyStyle: {
                'border-style': 'none'
            },
            items: [{
                xtype: 'container',
                flex: 1,
                cls: 'wip-description',
                margin: '0 0 10 10',
                html: config.workflow.get('description')
            },{
                xtype: 'grid',
                width: 400,
                store: contactStore,
                hideHeaders: true,
                bodyCls: 'wip-contact-grid',
                margin: '0 10 10 10',
                viewConfig: {
                    enableTextSelection: true,
                    getRowClass: function() {
                        return 'wip-contact-grid-row';
                    }
                },
                features: [
                    Ext.create('Ext.grid.feature.Grouping',{
                        groupHeaderTpl: '{name}',
                        startCollapsed : false
                    })
                ],
                columns: [{
                    dataIndex: 'name',
                    flex: 1,
                    renderer: function(value, md, record) {
                        return Ext.DomHelper.markup({
                            tag: 'div',
                            style: {
                                width: '100%',
                                height: '25px',
                                'padding-top': '5px'
                            },
                            children: [{
                                tag: 'div',
                                style: {
                                    position: 'absolute',
                                    left: '5px'
                                },
                                html: record.get('name')
                            },{
                                tag: 'a',
                                href: 'mailto:' + record.get('email') + "?Subject=EAVL%20" + escape(config.workflow.get('name')),
                                style: {
                                    position: 'absolute',
                                    right: '5px'
                                },
                                html: record.get('email')
                            }]
                        });
                    }
                }]
            }]
        });

        this.callParent(arguments);
    }
});