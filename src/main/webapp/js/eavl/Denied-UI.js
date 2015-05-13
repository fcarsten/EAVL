/**
 * Controls the Index page
 */
Ext.application({
    name : 'eavl-index',

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {
        
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
                            tag : 'a',
                            href: WEB_CONTEXT,
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
                style: {
                    'background-color' : 'white'
                },
                layout : {
                    type: 'vbox',
                    pack: 'start',
                    align: 'center'
                },
                items : [{
                    xtype: 'container',
                    height : 200,
                    margin: '100 0 0 0',
                    width : '100%',
                    html : Ext.DomHelper.markup({
                        tag: 'div',
                        cls: 'loading-box', //we repurpose the TaskWait-UI 
                        children: [{
                            tag : 'h1',
                            html : 'Access Denied'
                        },{
                            tag : 'div',
                            html : Ext.util.Format.format('If you believe this is an error, please contact: <a href="mailto:{0}?Subject=Access%20Denied&body={1}">{0}</a>', CONTACT_EMAIL, escape('I was trying to access: ' + window.location.href))
                        }]
                    })
                }]
            }]
        });
        
        var feedback = Ext.create('eavl.widgets.FeedbackWidget', {
            disabled : !window.AUTHENTICATED_USER,
            disabledCallback : function() {
                Ext.Msg.alert('Login Required', 'In order to cut down on spam we require you to please <a href="login.html">login</a> before attempting to send feedback.');
            }
        });
    }
});
