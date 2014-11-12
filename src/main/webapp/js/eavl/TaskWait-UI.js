/**
 * Controls the Upload page
 */
Ext.application({
    name : 'eavl-taskwait',


    init: function() {
        eavl.widget.SplashScren.showLoadingSplash('Please stand by ...');
    },

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {

        eavl.widget.SplashScren.hideLoadingScreen();


        var urlParams = Ext.Object.fromQueryString(window.location.search.substring(1));
        var taskId = urlParams.taskId ? urlParams.taskId : '';
        var next = urlParams.next ? urlParams.next : '';

        var taskName = "???";
        if (next.endsWith("setproxy.html")) {
            taskName = "Imputation";
        }

        var viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items : [{
                xtype: 'workflowpanel',
                region: 'north',
                height: 200,
                hideNavigator : true,
                urlOverride : next,
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
                    pack: 'center',
                    align: 'center'
                },
                items : [{
                    xtype: 'container',
                    height : 200,
                    width : 500,
                    html : Ext.DomHelper.markup({
                        tag : 'div',
                        cls : 'loading-box',
                        children : [{
                            tag : 'img',
                            src : 'img/loading-bars.svg'
                        },{
                            tag : 'div',
                            html : 'Performing ' + taskName
                        }]
                    })
                },{
                    xtype: 'container',
                    height : 200,
                    items : [{
                        xtype: 'checkboxfield',
                        boxLabel: 'Email me when this is done'
                    }]
                }]
            }]
        });
    }
});
