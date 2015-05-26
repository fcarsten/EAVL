/**
 * Controls the Index page
 */
Ext.application({
    name : 'eavl-index',

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {

        var userStore = Ext.create('Ext.data.Store', {
            model : 'eavl.admin.EAVLUser',
            autoLoad: true,
            proxy : {
                type : 'ajax',
                url : 'getUsers.do',
                reader : {
                    type : 'json',
                    rootProperty : 'data'
                }
            },
        });
        
        var roleStore = Ext.create('Ext.data.Store', {
            fields: ['role'],
            autoLoad: false
        });
        
        var handleAddRole =  function(btn) {
            var user = Ext.getCmp('usercombo').getSelection();
            if (user) {
                Ext.MessageBox.show({
                    title: 'New Role',
                    msg: Ext.util.Format.format('Enter a new user role to add to user <b>{0}</b>: ', user.get('email')),
                    animateTarget: btn.getEl(),
                    icon: Ext.window.MessageBox.QUESTION,
                    prompt: true,
                    scope : this,
                    buttons : Ext.MessageBox.OK,
                    fn : function(buttonId, text, opt) {
                        if (buttonId === 'ok' && text) {
                            Ext.Ajax.request({
                                url: "addUserRole.do",
                                params: {
                                    userName: user.get('userName'),
                                    role: text
                                },
                                callback: function(options, success, response) {
                                    if (!success) {
                                        return;
                                    }
                                    
                                    var responseObj = Ext.JSON.decode(response.responseText);
                                    if (!responseObj.success) {
                                        return;
                                    }
                                    
                                    roleStore.loadData([{role: text}], true);
                                    user.get('authorities').push(text);
                                }
                            });
                        }
                    }
                });
            }
        };
        
        var handleDeleteRole =  function(btn) {
            var user = Ext.getCmp('usercombo').getSelection();
            var roles = Ext.getCmp('rolegrid').getSelection();
            
            if (!Ext.isEmpty(roles) && user) {
                var roleText = roles[0].get('role');
                Ext.MessageBox.show({
                    title: 'New Role',
                    msg: Ext.util.Format.format('Really delete <b>{1}</b> from user <b>{0}</b>: ', user.get('email'), roleText),
                    animateTarget: btn.getEl(),
                    icon: Ext.window.MessageBox.QUESTION,
                    scope : this,
                    buttons : Ext.MessageBox.OKCANCEL,
                    fn : function(buttonId) {
                        if (buttonId === 'ok') {
                            Ext.Ajax.request({
                                url: "deleteUserRole.do",
                                params: {
                                    userName: user.get('userName'),
                                    role: roleText
                                },
                                callback: function(options, success, response) {
                                    if (!success) {
                                        return;
                                    }
                                    
                                    var responseObj = Ext.JSON.decode(response.responseText);
                                    if (!responseObj.success) {
                                        return;
                                    }
                                    
                                    roleStore.remove(roles[0]);
                                    Ext.Array.remove(user.get('authorities'), roleText);
                                }
                            });
                        }
                    }
                });
            }
        };
        
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
                xtype: 'container',
                region: 'center',
                margin: '10 0 0 0',
                border: false,
                layout: { type: 'hbox', pack : 'center'},
                items: [{
                    xtype: 'form',
                    width: 800,
                    height: 600,
                    border: false,
                    layout: { type: 'vbox', pack : 'start', align: 'stretch'},
                    items: [{
                        xtype: 'combo',
                        model: 'eavl.admin.EAVLUser',
                        store: userStore,
                        queryMode: 'local',
                        displayField: 'email',
                        id: 'usercombo',
                        editable: false,
                        anchor: '100%',
                        emptyText: 'Select a user to edit their permissions',
                        listeners: {
                            select: function(combo, newValue) {
                                roleStore.removeAll();
                                if (newValue) {
                                    var auths = [];
                                    Ext.each(newValue.get('authorities'), function(auth) {
                                        auths.push({role: auth});
                                    });
                                    roleStore.loadData(auths);
                                }
                            }
                        }
                    },{
                        xtype: 'grid',
                        store: roleStore,
                        flex: 1,
                        id: 'rolegrid',
                        hideHeaders: true,
                        columns: [{dataIndex: 'role', flex: 1}],
                        dockedItems: [{
                            xtype: 'toolbar',
                            dock: 'bottom',
                            items: [{
                                xtype: 'tbfill'
                            },{
                                xtype: 'button',
                                scale: 'large',
                                text: 'Delete Role',
                                handler: handleDeleteRole
                            },{
                                xtype: 'button',
                                cls: 'important-button',
                                scale: 'large',
                                text: 'Add Role',
                                handler: handleAddRole
                            }]
                        }]
                    }]
                }]
            }]
        });
        
        
    }
});
