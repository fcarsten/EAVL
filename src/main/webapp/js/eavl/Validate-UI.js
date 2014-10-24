/**
 * Controls the Validate page
 */
Ext.application({
    name : 'eavl-validate',

    init: function() {
        eavl.widget.SplashScren.showLoadingSplash('Loading Validator, please stand by ...');
    },

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {
        //Called if the init code fails badly
        var initError = function() {
            eavl.widget.SplashScren.hideLoadingScreen();
            eavl.widget.SplashScren.showErrorSplash('There was an error loading your data. Please try refreshing the page or contacting cg-admin@csiro.au if the problem persists.');
        };

        var initSuccess = function(colCount) {
            eavl.widget.SplashScren.hideLoadingScreen();

            var viewport = Ext.create('Ext.container.Viewport', {
                layout: 'border',
                items: [{
                    xtype: 'panel',
                    region: 'north',
                    height: 100,
                    layout: {
                        type: 'hbox',
                        pack: 'start'
                    },
                    items: [{
                        xtype: 'image',
                        src: 'img/eavl-banner.png',
                        height: 100
                    }]
                },{
                    xtype: 'panel',
                    region: 'center',
                    layout: 'fit',
                    items: [{
                        title : 'The data you\'ve uploaded needs to be validated',
                        xtype: 'csvgrid',
                        margin : '50 100 50 100',
                        columnCount : colCount
                    }]
                }]
            });
        };

        // Start off by figuring out how many columns we need. From here we can start the rest of the process
        Ext.Ajax.request({
            url: 'validation/getColumnCount.do',
            callback: function(options, success, response) {
                if (!success || !response.responseText) {
                    initError();
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    initError();
                    return;
                }

                initSuccess(responseObj.data);
            }
        });
    }

});
