/**
 * Panel extension for highlighting where in the EAVL workflow the user is currently located
 */
Ext.define('eavl.widgets.WorkflowLocationPanel', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.workflowpanel',


    steps : [{url:'upload.html', title:'Upload', help: 'Upload a CSV file for processing.'},
             {url:'validate.html', title:'Validate', help: 'The conditional probability algorithm requires numeric values.'},
             {url:'setprediction.html', title:'Imputation', help: 'Select a parameter to be predicted. It\'s missing values will be imputed.'},
             {url:'setproxies.html', title:'Proxies', help: 'Select three parameters to act as proxies for the predicted element.'},
             {url:'results.html', title:'Results', help: 'Browse the results of existing jobs.'}],



    /**
     * Adds the following config to Ext.panel.Panel
     * {
     *
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {

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


        var markup = Ext.DomHelper.markup({
            tag: 'div',
            id : 'workflow-container',
            children : [{
                tag : 'div',
                id : 'arrows-container',
                children : [{
                    tag: 'div',
                    id : 'arrows-internal',
                    children : [{
                        tag : 'a',
                        id : 'back',
                        cls : previousStep == null ? 'disabled' : '',
                        href : previousStep == null ? '' : previousStep.url
                    },{
                        tag : 'a',
                        id : 'next',
                        cls : nextStep == null ? 'disabled' : '',
                        href : nextStep == null ? '' : nextStep.url
                    }]
                },{
                    tag: 'ud',
                    id: 'titles',
                    children: [{
                        tag: 'li',
                        html : previousStep == null ? '' : previousStep.title
                    },{
                        tag: 'li',
                        html : nextStep == null ? '' : nextStep.title
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
                        tag: 'p',
                        html: currentStep.help
                    }]
                }]
            }]
        });

        Ext.apply(config, {
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