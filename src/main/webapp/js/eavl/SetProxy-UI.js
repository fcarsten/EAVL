/**
 * Controls the Set Proxy page
 */
Ext.application({
    name : 'eavl-setproxy',

    init: function() {
        eavl.widget.SplashScren.showLoadingSplash('Loading Proxy Selection, please stand by ...');
    },

    viewport : null,

    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {
        //Called if the init code fails badly
        var initError = function() {
            eavl.widget.SplashScren.hideLoadingScreen();
            eavl.widget.SplashScren.showErrorSplash('There was an error loading your data. Please try refreshing the page or contacting cg-admin@csiro.au if the problem persists.');
        };

        var initNotReady = function(message, url) {
            eavl.widget.SplashScren.hideLoadingScreen();
            eavl.widget.SplashScren.showErrorSplash(message + Ext.util.Format.format('<br><a href="{0}">Continue</a>', url));
        };

        var initSuccess = function() {
            eavl.widget.SplashScren.hideLoadingScreen();

            Ext.tip.QuickTipManager.init();

            Ext.app.Application.viewport = Ext.create('Ext.container.Viewport', {
                layout: 'border',
                items: [{
                    xtype: 'workflowpanel',
                    region: 'north',
                    allowNext: function(callback) {
                        callback(false);
                    }
                },{
                    xtype: 'container',
                    region: 'center',
                    html : 'TODO - this content'

                }]
            });
        };

        //Before loading
        Ext.Ajax.request({
            url: 'setproxy/getImputationStatus.do',
            callback: function(options, success, response) {
                if (!success) {
                    initError();
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    initError();
                    return;
                }

                if (responseObj.data == true) {
                    initSuccess();
                    return;
                }

                //At this point imputation hasn't been run/hasn't finished
                if (responseObj.msg === "nodata") {
                    initNotReady("There's no record of an imputation task running for this job. Did you complete the prediction steps?", "setprediction.html");
                    return;
                }
                if (responseObj.msg === "nojob") {
                    initNotReady("There's no job selected. Did you upload a file?", "upload.html");
                    return;
                }
                if (responseObj.msg === "failed") {
                    initNotReady("Imputation failed. You can try resubmitting.", "setprediction.html");
                    return;
                }

                //OK imputation is running - shift to loading page
                window.location.href = "taskwait.html?" + Ext.Object.toQueryString({taskId: responseObj.msg, next: 'setproxy.html'});
            }
        });
    }

});
