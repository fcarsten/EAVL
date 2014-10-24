/**
 * Utility functions for rendering full screen splashes.
 */
Ext.define('eavl.widget.SplashScren', {
    singleton: true
}, function() {


    /**
     * Loads a full screen splash screen with the specified message. If a splash is already showing, it will be hidden
     */
    eavl.widget.SplashScren.showLoadingSplash = function(message) {
        if (!eavl.widget.SplashScren._splashscreen) {
            eavl.widget.SplashScren.hideLoadingScreen();
        }
        eavl.widget.SplashScren._splashscreen = Ext.getBody().mask(message, 'splashscreen');
    };

    /**
     * Hides any open loading splash screens, otherwise  has no effect
     */
    eavl.widget.SplashScren.hideLoadingScreen = function() {
        if (!eavl.widget.SplashScren._splashscreen) {
            return;
        }
        eavl.widget.SplashScren._splashscreenfading = eavl.widget.SplashScren._splashscreen;
        eavl.widget.SplashScren._splashscreen = null;
        var task = new Ext.util.DelayedTask(function() {

            // fade out the body mask
            eavl.widget.SplashScren._splashscreenfading.fadeOut({
                duration: 500,
                remove: true
            });

            // fade out the message
            eavl.widget.SplashScren._splashscreenfading.next().fadeOut({
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
    eavl.widget.SplashScren.showErrorSplash = function(message) {
        var viewport = Ext.ComponentQuery.query('viewport')[0];
        if (!viewport) {
            viewport = Ext.create('Ext.container.Viewport', {
                layout: 'border',
                items: []
            });
        }

        viewport.removeAll(true);
        viewport.add({
            xtype : 'panel',
            region : 'center',
            layout : 'fit',
            items : [{
                title : message,
                xtype : 'panel',
                layout : 'fit',
                items : [{
                    xtype : 'image',
                    src : 'img/alert.svg',
                }]
            }]
        });
    };
});