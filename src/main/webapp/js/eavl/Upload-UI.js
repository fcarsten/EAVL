/**
 * Controls the Upload page
 */
Ext.application({
    name : 'eavl-upload',


    init: function() {
        Ext.QuickTips.init();
        
        eavl.widgets.SplashScreen.showLoadingSplash('Loading upload form, please stand by ...');
    },

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {
        eavl.widgets.SplashScreen.hideLoadingScreen();
        
        var jobId = null;

        var showCSVGrid = function(id, parameterDetails, jobName) {
            jobId = id;
            var parent = Ext.getCmp('parent-container');
            if (parent.down('#csvGrid')) {
                parent.down('#csvGrid').destroy();
            }

            
            parent.down('#upload-field').destroy();
            parent.down('#upload-label').setText('Choose a name for this job');
            parent.down('#upload-form').setWidth(600);
            parent.down('#upload-form').add(Ext.create('Ext.form.field.Text', {
                anchor : '100%',
                itemId: 'job-name',
                hideLabel: true,
                margin: '20 0 10 0',
                height: 60,
                fieldStyle: {
                    'font-family': 'helvetica,​arial,​verdana,​sans-serif',
                    'font-size': 32,
                    'font-weight': 700,
                    'line-height': '40px'
                },
                value: jobName
            }));
            
            //I've been having troubles with ExtJS layout and dynamically adding a flex component
            //This is the workaround I'm going with...
            var width = window.innerWidth - 120;

            parent.add(Ext.create('eavl.widgets.CSVGrid', {
                itemId: 'csvGrid',
                jobId: id,
                readOnly: true,
                sortColumns: false,
                width: width,
                flex: 1,
                parameterDetails : parameterDetails,
                title: 'If this looks correct, press the big arrow at the top of the page.',
                margin: '0 0 10 0',
                plugins: [{
                    ptype: 'headerhelp',
                    text: 'You will have the opportunity to correct "bad" data values at a later stage.'
                }]
            }));


        };

        var viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items : [{
                xtype: 'workflowpanel',
                region: 'north',
                height: 200,
                allowNext : function(callback) {
                    if (jobId == null) {
                        eavl.widgets.util.HighlightUtil.highlight(Ext.getCmp('upload-form'), eavl.widgets.util.HighlightUtil.ERROR_COLOR);
                        callback(false);
                        return;
                    }
                    
                    var parent = Ext.getCmp('parent-container');
                    var nameField = parent.down('#job-name');
                    var name = nameField.getValue().trim();
                    if (Ext.isEmpty(name)) {
                        eavl.widgets.util.HighlightUtil.highlight(nameField, eavl.widgets.util.HighlightUtil.ERROR_COLOR);
                        callback(false);
                        return;
                    }
                    
                    Ext.Ajax.request({
                        url: 'validation/renameJob.do',
                        params: {
                            name : name,
                            jobId: jobId
                        },
                        callback : function(options, success, response) {
                            if (!success) {
                                callback(false);
                                return;
                            }
                            
                            var obj = Ext.JSON.decode(response.responseText);
                            if (!obj.success) {
                                callback(false);
                                return;
                            }
                            
                            callback(true);
                        }
                    });
                    

                    
                }
            },{
                xtype: 'container',
                id : 'parent-container',
                region: 'center',
                border: false,
                style: {
                    'background-color' : 'white'
                },
                layout : {
                    type: 'vbox',
                    align: 'center',
                    pack: 'start'
                },
                items: [{
                    xtype: 'form',
                    id: 'upload-form',
                    width: 300,
                    margin: '30 0 10 0',
                    bodyPadding : '30 10 10 10',
                    items : [{
                        xtype: 'label',
                        itemId: 'upload-label',
                        style: {
                            'font-size': '15px'
                        },
                        text: 'Choose CSV file to upload for processing'
                    },{
                        xtype: 'filefield',
                        name: 'file',
                        itemId: 'upload-field',
                        anchor : '100%',
                        hideLabel: true,
                        margin: '20 0 10 0',
                        listeners : {
                            change : function(ff, value, eOpts) {
                                var formPanel = ff.findParentByType('form');
                                var form = formPanel.getForm();
                                if (!form.isValid()) {
                                    return;
                                }

                                //Submit our form so our files get uploaded...
                                form.submit({
                                    url: 'validation/uploadFile.do',
                                    scope : this,
                                    params : {
                                        jobId : jobId
                                    },
                                    success: function(form, action) {
                                        if (!action.result.success) {
                                            Ext.Msg.alert('Error uploading file. ' + action.result.error);
                                            return;
                                        }

                                        var pdList = [];
                                        Ext.each(action.result.data.parameterDetails, function(o) {
                                            pdList.push(Ext.create('eavl.models.ParameterDetails', o));
                                        });

                                        showCSVGrid(action.result.data.id, pdList, action.result.data.name);
                                    },
                                    failure: function() {
                                        Ext.Msg.alert('Failure', 'File upload failed. Please try again in a few minutes.');
                                    },
                                    waitMsg: 'Uploading file, please wait...',
                                    waitTitle: 'Upload file'
                                });
                            }
                        }
                    }]
                }]
            }]
        });

        var feedback = Ext.create('eavl.widgets.FeedbackWidget', {});
    }
});
