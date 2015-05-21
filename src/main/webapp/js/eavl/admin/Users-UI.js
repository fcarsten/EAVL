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
                html: 'todo'
                //items: []
            }]
        });
        
        
    }
});
