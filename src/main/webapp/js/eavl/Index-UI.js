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
                       name: 'June Hill',
                       email: ['June.Hill', 'csiro.au'].join('@'),
                       group: 'Contact'
                   }),
	                Ext.create('eavl.models.Contact', {
	                    name: 'Rob Woodcock',
	                    email: ['Rob.Woodcock', 'csiro.au'].join('@'),
	                    group: 'Contact'
	                }),
            ]
        });
        
        var test1 = Ext.create('eavl.models.Workflow', {
            id: 'eavl-vgl',
            name: 'Geophysical Inversions',
            version: 'VGL-1.0.0',
            background: 'img/workflows/vgl.png',
            initialLink: 'https://vgl.auscope.org',
            resultsLink: 'https://vgl.auscope.org/joblist.html',            
            description: '<p>The Virtual Geophysics Laboratory (VGL) allows users to browse and visualise large repositories of NCI and Geoscience' +
            ' Australia hosted datasets. After selecting a target dataset and region, users can select a model that they want to run on the' +
            ' dataset (e.g. magnetic or gravity inversion for the purpose of understanding what\'s under the observable surface in an' +
            ' area). The analytic model gets submitted to a cloud provider of choice (e.g. Amazon cloud or NCI HPC facility) by VGL and' +
            ' results are made available to the user after completion.</p>',
            contacts: [
                       Ext.create('eavl.models.Contact', {
                           name: 'Ryan Fraser',
                           email: ['Ryan.Fraser', 'csiro.au'].join('@'),
                           group: 'Contact'
                       }),
    	                Ext.create('eavl.models.Contact', {
    	                    name: 'Carsten Friedrich',
    	                    email: ['Carsten.Friedrich', 'csiro.au'].join('@'),
    	                    group: 'Contact'
    	                }),
    	                Ext.create('eavl.models.Contact', {
    	                    name: 'Rob Woodcock',
    	                    email: ['Rob.Woodcock', 'csiro.au'].join('@'),
    	                    group: 'Contact'
    	                }),
            ]
        });
        
        var test2 = Ext.create('eavl.models.Workflow', {
            id: 'eavl-data',
            name: 'Data Browser',
            version: '0.0.1-BETA',
            background: 'img/workflows/data.png',
            initialLink: 'https://vgl.auscope.org',
            description: '<p>The AuScope Discovery provides a web based interface for searching and accessing data, information, imagery,' +
            'services and applications connected to the Grid. It allows users to discover, browse, save, and process geospatial information '+
            'from Earth science data sources around Australia. Hyperspectral, borehole, global navigation satellite, geodesy, mineral '+ 
            'occurrence and geology data are all available through the portal.</p>',
            contacts: [
   	                Ext.create('eavl.models.Contact', {
	                    name: 'Rob Woodcock',
	                    email: ['Rob.Woodcock', 'csiro.au'].join('@'),
	                    group: 'Contact'
	                }),
            ]
        });
        
        var eavlDomaining = Ext.create('eavl.models.Workflow', {
            id: 'eavl-spatial-domaining',
            name: 'Data Mosaic Virtual Laboratory',
            version: '0.0.1-BETA',
            background: 'img/workflows/domaining.png',
            initialLink: 'https://datamosaicvl.csiro.au/DataMosaicVL/about',
            description: '<p>Drilling for mineral exploration typically includes the collection of continuous down hole data, including multi-element '+
            'geochemical analysis and wire line natural gamma logging. When this data is numerical, it is desirable to convert it to geological'+
            ' units (i.e. categorical data) to enable geological interpretation and construction of a 3D geology model. Manual interpretation of '+
            'numerical data is slow and subjective and can be particularly challenging in the presence of high levels of noise.</p>'+
            '<p>The Data Mosaic method automatically determines the locations of geological boundaries at multiple scales. Data Mosaic provides '+
            'the results as a user-friendly plot from which it is easy to identify spatial patterns in the data at a range of scales of'+
            ' analysis. When interpreting the Data Mosaic plot, the distance that each boundary extends along the coefficient index axis is '+
            'an indication of its relative strength. Pairs of adjacent boundaries define each spatial domain. Spatial domains can be coloured '+
            'by statistical values, e.g. mean, variance or inter-quartile range, to provide extra information.</p>',
            contacts: [
                       Ext.create('eavl.models.Contact', {
                           name: 'June Hill',
                           email: ['June.Hill', 'csiro.au'].join('@'),
                           group: 'Contact'
                       }),
    	                Ext.create('eavl.models.Contact', {
    	                    name: 'Rob Woodcock',
    	                    email: ['Rob.Woodcock', 'csiro.au'].join('@'),
    	                    group: 'Contact'
    	                }),
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
                    workflows: [wfCp, test1, test2, eavlDomaining],
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
