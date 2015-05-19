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
            initialLink: 'cp/upload.html',
            resultsLink: 'cp/results.html',
            description: '<a href="docs/Hill2014.pdf">Research Paper</a><br>' + 
                         '<p>Gold distribution in vein-hosted hydrothermal ore deposits is commonly nuggety (i.e. occurs as very localised concentrations of gold). In such cases, samples for gold assay from diamond drill core may return low gold grades despite their host rocks being mineralised. It is common practice in nuggety orebodies to use more spatially continuous proxies for mineralisation to help define the boundaries of mineralised regions. </p>' +
                         '<p>This workflow provides a method for automating the use of geochemical data as a proxy for mineralisation. Automation has the advantage of repeatability and fast processing time. The workflow uses a probabilistic approach to quantify the relationship between gold assay values and a chemical sub-composition. The conditional probability method allows several chemical elements or ratios to be combined to produce a single numerical indicator of probability of mineralisation for each sample. The probability is calculated using Bayes theorem:</p>' +
                         '<img src="docs/cp-equation.png" width="414" height="59"/>' +
                         '<p>where <i>Au</i> is gold assay, <i>v</i> is a threshold value, and <i>G1, G2, G3</i>... are the geochemical proxies.</p>',
            contacts: [
                Ext.create('eavl.models.Contact', {
                    name: 'Joshua Vote',
                    email: ['Josh.Vote', 'csiro.au'].join('@'),
                    group: 'Developers'
                }),
                Ext.create('eavl.models.Contact', {
                    name: 'Carsten Friedrich',
                    email: ['Carsten.Friedrich', 'csiro.au'].join('@'),
                    group: 'Developers'
                }),
                Ext.create('eavl.models.Contact', {
                    name: 'June Hill',
                    email: ['June.Hill', 'csiro.au'].join('@'),
                    group: 'Researchers'
                }),
                Ext.create('eavl.models.Contact', {
                    name: 'Andrew Rodger',
                    email: ['Andrew.Rodger', 'csiro.au'].join('@'),
                    group: 'Project Lead'
                })
            ]
        });
        
        var test1 = Ext.create('eavl.models.Workflow', {
            id: 'eavl-vgl',
            name: 'Geophysical Inversions',
            version: 'VGL-1.0.0',
            background: 'img/workflows/vgl.png',
            description: '<p>This workflow is still a work in progress. Please check back later.</p>' +
                         '<p>In the interim the <a href="http://vgl.csiro.au">Virtual Geophysics Laboratory<a> is available for performing inversions.</p>',
            contacts: [
                Ext.create('eavl.models.Contact', {
                    name: 'Joshua Vote',
                    email: ['Josh.Vote', 'csiro.au'].join('@'),
                    group: 'Developers'
                })
            ]
        });
        
        var test2 = Ext.create('eavl.models.Workflow', {
            id: 'eavl-data',
            name: 'Data Browser',
            version: '0.0.1-BETA',
            background: 'img/workflows/data.png',
            description: '<p>This workflow is still a work in progress. Please check back later.</p>',
            contacts: [
                Ext.create('eavl.models.Contact', {
                    name: 'Joshua Vote',
                    email: ['Josh.Vote', 'csiro.au'].join('@'),
                    group: 'Developers'
                }),
                Ext.create('eavl.models.Contact', {
                    name: 'Carsten Friedrich',
                    email: ['Carsten.Friedrich', 'csiro.au'].join('@'),
                    group: 'Developers'
                }),
                Ext.create('eavl.models.Contact', {
                    name: 'Robert Woodcock',
                    email: ['Rober.Woodcock', 'csiro.au'].join('@'),
                    group: 'Project Lead'
                })
            ]
        });
        
        Ext.app.Application.viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            style: {
                'background-color': 'white'
            },
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
                xtype: 'tabpanel',
                region: 'center',
                layout: 'fit',
                plain: true,
                margin: '10 0 0 0',
                items: [{
                    title: 'Workflows',
                    xtype: 'workflowselectionpanel',
                    workflows: [wfCp, test1, test2],
                    listeners: {
                        select: function(panel, wf, e) {
                            Ext.create('eavl.widgets.EAVLModalWindow', {
                                width: 1000,
                                height: 600,
                                title: wf.get('name'),
                                subtitle: wf.get('version'),
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
                                            portal.util.PiwikAnalytic.trackevent('Navigation', 'Click', 'View Jobs', wf.get('id'));
                                            if (wf.get('resultsLink')) {
                                                document.location.href = wf.get('resultsLink');
                                            } else {
                                                Ext.Msg.alert('Coming soon...', "Thankyou for your interest in this workflow. The Early Access Virtual Laboratory is still in development and this workflow is not yet complete. If you\'d like more information please contact " + eavl.widgets.FeedbackWidget.CONTACT + ".");
                                            }
                                        }
                                    },{
                                        xtype: 'button',
                                        cls: 'important-button',
                                        scale: 'large',
                                        text: 'Start a new job',
                                        handler: function() {
                                            portal.util.PiwikAnalytic.trackevent('Navigation', 'Click', 'Start Job', wf.get('id'));
                                            if (wf.get('initialLink')) {
                                                document.location.href = wf.get('initialLink');
                                            } else {
                                                Ext.Msg.alert('Coming soon...', "Thankyou for your interest in this workflow. The Early Access Virtual Laboratory is still in development and this workflow is not yet complete. If you\'d like more information please contact " + eavl.widgets.FeedbackWidget.CONTACT + ".");
                                            }
                                        }
                                    }]
                                }]
                            }).show(e);
                        }
                    }
                }]
            }]
        });
        
        var feedback = Ext.create('eavl.widgets.FeedbackWidget', {
            disabled : !window.AUTHENTICATED_USER,
            disabledCallback : function() {
                Ext.Msg.alert('Login Required', 'In order to cut down on spam we require you to please <a href="login.html">login</a> before attempting to send feedback.');
            }
        });
        if (window.ANON_USER) {
            
        }
    }
});
