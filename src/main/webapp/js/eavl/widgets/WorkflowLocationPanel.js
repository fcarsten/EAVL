/**
 * Panel extension for highlighting where in the EAVL workflow the user is currently located
 */
Ext.define('eavl.widgets.WorkflowLocationPanel', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.workflowpanel',

    statics : {
        handleAllowNext : function(url) {
            console.log(url);
            if (Ext.getCmp('workflow-location-panel').allowNext()) {
                window.location.href = url;
            }
        },
        handleAllowPrevious : function(url) {
            if (Ext.get('workflow-location-panel').allowPrevious()) {
                window.location.href = url;
            }
        }
    },

    steps : [{url:'upload.html', title:'Upload', help: 'Upload a CSV file for processing.'},
             {url:'validate.html', title:'Validate', help: 'Remove any non numeric values (or trash the parameter entirely)'},
             {url:'setprediction.html', title:'Imputation', help: 'Select a parameter to be predicted. It\'s missing values will be imputed.'},
             {url:'setproxies.html', title:'Proxies', help: 'Select three parameters to act as proxies for the predicted element.'},
             {url:'results.html', title:'Results', help: 'Browse the results of existing jobs.'}],

    allowNext : function() { return true; },
    allowPrevious : function() { return true; },


    /**
     * Adds the following config to Ext.panel.Panel
     * {
     *  allowNext : function() - A function that should return a boolean if the user is allowed to proceed (defaults to always true)
     *  allowPrevious : function() - A function that should return a boolean if the user is allowed to proceed (defaults to always true)
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

        var currentIndex = this.getActiveStepIndex();

        var currentStep = this.steps[currentIndex];
        var previousStep = null;
        var nextStep = null;

        if (currentIndex > 0) {
            previousStep = this.steps[currentIndex - 1];
        }

        if (currentIndex < (this.steps.length - 1)) {
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
                children: [{
                    tag: 'div',
                    id: 'text-internal',
                    children: [{
                        tag: 'h1',
                        html: currentStep.title
                    },{
                        tag: 'h2',
                        html: currentStep.help
                    }]
                }]
            }]
        });

        Ext.apply(config, {
            id: 'workflow-location-panel',
            html : markup
        });

        this.callParent(arguments);
    },


    getActiveStepIndex : function() {
        for (var i = 0; i < this.steps.length; i++) {
            if (window.location.pathname.endsWith(this.steps[i].url)) {
                return i;
            }
        }

        return -1;
    }
});