/**
 * Utility functions for rendering full screen splashes.
 */
Ext.define('eavl.widgets.SplashScreen', {
    singleton: true
}, function() {


    /**
     * Loads a full screen splash screen with the specified message. If a splash is already showing, it will be hidden
     */
    eavl.widgets.SplashScreen.showLoadingSplash = function(message) {
        if (eavl.widgets.SplashScreen._splashscreen) {
            eavl.widgets.SplashScreen.hideLoadingScreen();
        }
        eavl.widgets.SplashScreen._splashscreen = Ext.getBody().mask(message, 'splashscreen');
    };

    /**
     * Hides any open loading splash screens, otherwise  has no effect
     */
    eavl.widgets.SplashScreen.hideLoadingScreen = function() {
        if (!eavl.widgets.SplashScreen._splashscreen) {
            return;
        }
        eavl.widgets.SplashScreen._splashscreenfading = eavl.widgets.SplashScreen._splashscreen;
        eavl.widgets.SplashScreen._splashscreen = null;
        var task = new Ext.util.DelayedTask(function() {

            // fade out the body mask
            eavl.widgets.SplashScreen._splashscreenfading.fadeOut({
                duration: 500,
                remove: true
            });

       });
       task.delay(1000);
    };

    /**
     * Shows a full screen error splash that overrides the current viewport. This is a "nuclear" option and
     * will destroy everything in the current viewport. Only use if there is no chance of recovery
     */
    eavl.widgets.SplashScreen.showErrorSplash = function(message) {
        var viewport = Ext.ComponentQuery.query('viewport')[0];
        if (!viewport) {
            viewport = Ext.create('Ext.container.Viewport', {
                layout: 'border',
                items: []
            });
        }

        //Update everything after a short delay to ensure we catch everything
        new Ext.util.DelayedTask(function(){
            viewport.removeAll(true);
            viewport.add({
                xtype: 'workflowpanel',
                region: 'north',
                height: 200,
                hideNavigator : true,
                hideText : true,
                allowNext : function(callback) {
                    callback(false);
                }
            },{
                xtype : 'container',
                region : 'center',
                layout : {
                    type: 'vbox',
                    align: 'center',
                    pack: 'start'
                },
                style: {
                    'background-color': 'white'
                },
                items : [{
                    xtype : 'image',
                    width: 500,
                    height: 500,
                    src : 'img/alert.svg'
                },{
                    xtype: 'panel',
                    width: '100%',
                    bodyStyle: {
                        'text-align': 'center',
                        'font-size': 24
                    },
                    html: message
                }]
            });
        }).delay(500);

    };
});