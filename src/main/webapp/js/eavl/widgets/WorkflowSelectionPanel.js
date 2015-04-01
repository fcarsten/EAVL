/**
 * Panel extension for rendering a collection eavl.models.Workflow objects
 */
Ext.define('eavl.widgets.WorkflowSelectionPanel', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.workflowselectionpanel',

    _store : null,

    /**
     * Adds the following config to Ext.panel.Panel
     * {
     *  workflows: eavl.models.Workflow[]
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {
        var me = this;
        
        this._store = Ext.create('Ext.data.Store', {
            model: 'eavl.models.Workflow'
        });
        this._store.loadData(config.workflows);
      
        
        var markupBlocks = [];
        Ext.each(config.workflows, function(workflow) {
            markupBlocks.push(me._markupWorkflowBlock(workflow));
        });
        
        Ext.apply(config, {
            html: Ext.DomHelper.markup({
                tag: 'div',
                cls: 'wfsp-container',
                children: markupBlocks
            }) 
        });

        this.callParent(arguments);
        
        this.on('afterrender', this._registerClickEvents, this, {single: true});
    },
    
    _registerClickEvents : function() {
        var me = this;
        Ext.each(this.getEl().query(".wfsp-workflow"), function(el) {
            Ext.get(el).on('click', me._handleClick, me);
        });
    },
    
    _handleClick: function(e, t) {
        var id = Ext.fly(t).getAttribute('workflowId');
        var workflow = this._store.getById(id);
        if (workflow) {
            console.log("Clicked:", workflow.get('name'));
        }
    },
    
    _markupWorkflowBlock : function(workflow) {
        return {
            tag: 'div',
            cls: 'wfsp-workflow hvr-grow',
            workflowId: workflow.get('id'),
            style: {
                'background-image': 'url(' + workflow.get('background') + ')'
            },
            children: [{
                tag: 'div',
                cls: 'wfsp-title',
                children: [{
                    tag: 'div',
                    cls: 'wfsp-name',
                    html: workflow.get('name')
                },{
                    tag: 'div',
                    cls: 'wfsp-version',
                    html: workflow.get('version')
                }]
            }]
        }
    },
    
    getStore : function() {
        return this._store;
    }

});