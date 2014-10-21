/**
 * Controls the Upload page
 */
Ext.application({
    name : 'eavl-upload',

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {

        var viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items: [{
                xtype: 'form',
                region: 'center',
                itemId: 'upload-form',
                title: 'Choose File to Process',
                items : [{
                    xtype: 'filefield',
                    name: 'file',
                    anchor : '100%',
                    labelWidth: 150,
                    allowBlank: false,
                    fieldLabel: 'Local File'
                }],
                bbar:[{
                    xtype: 'button',
                    text: 'Add File',
                    iconCls: 'add',
                    handler: function(btn) {
                        var formPanel = btn.findParentByType('viewport').queryById('upload-form');
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
                }]
            }]
        });



    }
});
