/**
 * Panel extension for highlighting where in the EAVL workflow the user is currently located
 */
Ext.define('eavl.widgets.WorkflowLocationPanel', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.workflowpanel',

    statics : {
        handleAllowNext : function(url) {
            if (!Ext.getCmp('workflow-location-panel').allowNext) {
                window.location.href = url;
            } else {
                Ext.getCmp('workflow-location-panel').allowNext(function(proceed) {
                    if (proceed === true) {
                        window.location.href = url;
                    } else if (proceed !== false) {
                        window.location.href = proceed;
                    }
                });
            }
        },
        handleAllowPrevious : function(url) {
            if (!Ext.getCmp('workflow-location-panel').allowPrevious) {
                window.location.href = url;
            } else {
                Ext.getCmp('workflow-location-panel').allowPrevious(function(proceed) {
                    if (proceed === true) {
                        window.location.href = url;
                    } else if (proceed !== false) {
                        window.location.href = proceed;
                    }
                });
            }
        }
    },

    steps : [{url:'upload.html', title:'Upload', help: 'Upload a CSV file for processing.'},
             {url:'identify.html', title:'Identify', help: 'Identify what your parameters represent.'},
             {url:'validate.html', title:'Validate', help: 'Remove any non numeric values (or trash the parameter entirely)'},
             {url:'threshold.html', title:'Threshold', help: 'Select a threshold for the parameter to predict.'},
             {url:'setproxy.html', title:'Proxies', help: 'Select three parameter ratios to act as proxies for the predicted element.'},
             {url:'results.html', title:'Results', help: 'Browse the results of existing jobs.'}],

    allowNext : null,
    allowPrevious : null,


    /**
     * Adds the following config to Ext.panel.Panel
     * {
     *  allowNext : function(callback) - A function that should return a boolean (via the callback argument) if the user is allowed to proceed (defaults to always true).
     *                                   If a string is returned, its value will be used as the URL instead.
     *  allowPrevious : function(callback) - A function that should return a boolean (via the callback argument) if the user is allowed to proceed (defaults to always true)
     *                                       If a string is returned, its value will be used as the URL instead.
     *  hideNavigator : boolean - If true, the Navigator panel (big left/right arrows) will be omitted
     *  urlOverride : String - if set, highlight location based on this string rather than window.location
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {

        if (config.allowNext) {
            this.allowNext = config.allowNext;
        }

        if (config.allowPrevious) {
            this.allowPrevious = config.allowPrevious;
        }

        this.hideNavigator = config.hideNavigator ? true : false;

        var currentIndex = this.getActiveStepIndex(config.urlOverride ? config.urlOverride : undefined);

        var currentStep = currentIndex >= 0 ? this.steps[currentIndex] : null;
        var previousStep = null;
        var nextStep = null;

        if (currentIndex !== -1 && currentIndex > 0) {
            previousStep = this.steps[currentIndex - 1];
        }

        if (currentIndex !== -1 && currentIndex < (this.steps.length - 1)) {
            nextStep = this.steps[currentIndex + 1];
        }

        var menuItems = [];
        for (var i = 0; i < this.steps.length; i++) {
            menuItems.push({
                tag : 'li',
                cls : i === currentIndex ? 'selected' : '',
                children: [{
                    tag: 'a',
                    href: this.steps[i].url,
                    html: this.steps[i].title
                }]
            });
        }

        var markup = Ext.DomHelper.markup({
            tag: 'div',
            id : 'workflow-container',
            children : [{
                tag : 'div',
                id : 'top-bar-container',
                children: [{
                    tag : 'div',
                    id: 'top-bar-logo'
                },{
                    tag: 'ul',
                    children : menuItems
                }]
            },{
                tag : 'div',
                id : 'arrows-container',
                style : this.hideNavigator ? 'display:none;' : '',
                children : [{
                    tag: 'div',
                    id : 'arrows-internal',
                    children : [{
                        tag : 'a',
                        id : 'back',
                        cls : previousStep == null ? 'disabled' : '',
                        href : previousStep == null ? '' : Ext.util.Format.format('javascript:eavl.widgets.WorkflowLocationPanel.handleAllowPrevious(\'{0}\')', previousStep.url),
                        children : [{
                            tag: 'div',
                            html : previousStep == null ? '' : previousStep.title
                        }]
                    },{
                        tag : 'a',
                        id : 'next',
                        cls : nextStep == null ? 'disabled' : '',
                        href : nextStep == null ? '' : Ext.util.Format.format('javascript:eavl.widgets.WorkflowLocationPanel.handleAllowNext(\'{0}\')', nextStep.url),
                        children : [{
                            tag: 'div',
                            html : nextStep == null ? '' : nextStep.title
                        }]
                    }]
                }]
            },{
                tag: 'div',
                id: 'text-container',
                style : this.hideNavigator ? 'display:none;' : '',
                children: [{
                    tag: 'div',
                    id: 'text-internal',
                    children: [{
                        tag: 'h1',
                        html: currentStep ? currentStep.title : ''
                    },{
                        tag: 'h2',
                        html: currentStep ? currentStep.help : ''
                    }]
                }]
            }]
        });

        Ext.apply(config, {
            id: 'workflow-location-panel',
            border: false,
            html : markup
        });

        this.callParent(arguments);
    },


    getActiveStepIndex : function(url) {
        if (!url) {
            url = window.location.pathname;
        }

        for (var i = 0; i < this.steps.length; i++) {
            if (url.endsWith(this.steps[i].url)) {
                return i;
            }
        }

        return -1;
    }
});