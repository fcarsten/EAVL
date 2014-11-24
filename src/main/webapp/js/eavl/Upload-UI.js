/**
 * Controls the Upload page
 */
Ext.application({
    name : 'eavl-upload',


    init: function() {
        eavl.widgets.SplashScreen.showLoadingSplash('Loading upload form, please stand by ...');
    },

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {

        eavl.widgets.SplashScreen.hideLoadingScreen();

        var viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items : [{
                xtype: 'workflowpanel',
                region: 'north',
                height: 200,
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
                    align: 'center',
                    pack: 'center'
                },
                items: [{
                    xtype: 'form',
                    id: 'upload-form',
                    title: 'Choose CSV file to upload for processing',
                    width: 300,
                    bodyPadding : '30 10 10 10',
                    items : [{
                        xtype: 'filefield',
                        name: 'file',
                        anchor : '100%',
                        allowBlank: false,
                        hideLabel: true,

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
                                    success: function(form, action) {
                                        if (!action.result.success) {
                                            Ext.Msg.alert('Error uploading file. ' + action.result.error);
                                            return;
                                        }
                                        window.location.href = "validate.html";
                                    },
                                    failure: function() {
                                        Ext.Msg.alert('Failure', 'File upload failed. Please try again in a few minutes.');
                                    },
                                    params: {
                                        jobId : this.jobId
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



    }
});
