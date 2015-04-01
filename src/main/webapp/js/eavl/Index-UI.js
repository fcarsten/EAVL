/**
 * Controls the Index page
 */
Ext.application({
    name : 'eavl-index',

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {
        
        var wfCp = Ext.create('eavl.models.Workflow', {
            id: 'eavl-cp',
            name: 'Conditional Probability',
            version: '0.0.1-BETA',
            background: 'img/workflows/cp.png',
            initialLink: 'upload.html',
            resultsLink: 'results.html',
            description: '<a href="#todo">Research Paper</a><br>' + 
                         '<a href="#todo">Website</a><br>' +
                         '<br>' +
                         'The conditional probability description goes in here.<br>' +
                         'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.',
            contacts: [
                Ext.create('eavl.models.Contact', {
                    name: 'Joshua Vote',
                    email: 'Josh.Vote@csiro.au',
                    group: 'Developers'
                }),
                Ext.create('eavl.models.Contact', {
                    name: 'Carsten Friedrich',
                    email: 'Carsten.Friedrich@csiro.au',
                    group: 'Developers'
                }),
                Ext.create('eavl.models.Contact', {
                    name: 'June Hill',
                    email: 'June.Hill@csiro.au',
                    group: 'Researchers'
                })
            ]
        });
        
        var test1 = Ext.create('eavl.models.Workflow', {
            id: 'eavl-vgl',
            name: 'Geophysical Inversions',
            version: 'VGL-1.0.0',
            background: 'img/workflows/vgl.png',
            description: '<a href="#todo">Research Paper</a><br>' + 
                         '<a href="#todo">Website</a><br>' +
                         '<br>' +
                         'The conditional probability description goes in here.<br>' +
                         'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.',
            contacts: [
                Ext.create('eavl.models.Contact', {
                    name: 'Joshua Vote',
                    email: 'Josh.Vote@csiro.au',
                    group: 'Developers'
                }),
                Ext.create('eavl.models.Contact', {
                    name: 'Carsten Friedrich',
                    email: 'Carsten.Friedrich@csiro.au',
                    group: 'Developers'
                }),
                Ext.create('eavl.models.Contact', {
                    name: 'June Hill',
                    email: 'June.Hill@csiro.au',
                    group: 'Researchers'
                })
            ]
        });
        
        
        Ext.app.Application.viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items: [{
                xtype: 'panel',
                region: 'north',
                border: false,
                height: 50,
                html: Ext.DomHelper.markup({
                    tag: 'div',
                    id : 'workflow-container',
                    children : [{
                        tag : 'div',
                        id : 'top-bar-container',
                        children: [{
                            tag : 'div',
                            id: 'top-bar-logo',
                            style: {
                                top: '0px'
                            }
                        },{
                            tag: 'h1',
                            html: 'Early Access Virtual Laboratory',
                            style: {
                                display: 'inline-block',
                                color: 'white',
                                position: 'relative',
                                top: '-10px'
                            }
                        }]
                    }]
                })
            },{
                xtype: 'workflowselectionpanel',
                region: 'center',
                workflows: [wfCp, test1],
                listeners: {
                    select: function(panel, wf, e) {
                        Ext.create('Ext.Window', {
                            layout: 'fit',
                            modal: true,
                            width: 1000,
                            height: 600,
                            plain: true,
                            header: false,
                            resizable: false,
                            items: [{
                                xtype: 'workflowinspectpanel',
                                workflow: wf
                            }],
                            dockedItems: [{
                                xtype: 'toolbar',
                                dock: 'bottom',
                                items: [{
                                    xtype: 'tbfill'
                                },{
                                    xtype: 'button',
                                    scale: 'large',
                                    text: 'See existing jobs',
                                    handler: function() {
                                        if (wf.get('resultsLink')) {
                                            document.location.href = wf.get('resultsLink');
                                        } else {
                                            Ext.Msg.alert('Coming soon...', "Thankyou for your interest in this workflow. The Early Access Virtual Laboratory is still in development and this workflow is not yet complete. If you\'d like more information please contact cg-admin@csiro.au.");
                                        }
                                    }
                                },{
                                    xtype: 'button',
                                    cls: 'important-button',
                                    scale: 'large',
                                    text: 'Start a new job',
                                    handler: function() {
                                        if (wf.get('initialLink')) {
                                            document.location.href = wf.get('initialLink');
                                        } else {
                                            Ext.Msg.alert('Coming soon...', "Thankyou for your interest in this workflow. The Early Access Virtual Laboratory is still in development and this workflow is not yet complete. If you\'d like more information please contact cg-admin@csiro.au.");
                                        }
                                    }
                                }]
                            }],
                            listeners: {
                                'afterrender' : function(win) {
                                    win.mon(Ext.getBody(), 'click', function(el, e){
                                        win.close(win.closeAction);
                                    }, win, { delegate: '.x-mask' });
                                }
                            }
                        }).show(e);
                    }
                }
            }]
        });
    }
});
