/**
 * Simple widget that applies itself to the top of the DOM and displays as a little
 * box for prompting the user for feedback about the current page 
 */
Ext.define('eavl.widgets.FeedbackWidget', {
    
    statics : {
        CONTACT : Ext.isEmpty(window.CONTACT_EMAIL) ? '' : window.CONTACT_EMAIL
    },
    
    /**
     * {
     *  metadata - Object - KVP store for metadata to be recorded in the feedback request
     *  disabled - Boolean - If true the feedback widget will disabled any attempts to open it
     *  disabledCallback - function() - Called if disabled is true and the feedback widget is clicked.
     * }
     */
    constructor : function(config) {
        var metadata = config.metadata ? config.metadata : {};
        
        metadata.url = window.location.href;
        metadata.platform = navigator.platform;
        metadata.userAgent = navigator.userAgent;
        
        this.callParent(arguments);
        
        Feedback({h2cPath: window.WEB_CONTEXT + '/js/html2canvas/html2canvas.min.js',
            url: window.WEB_CONTEXT + '/eavl/feedback/sendFeedback.do',
            metadata: metadata,
            disabled: config.disabled,
            disabledCallback: config.disabledCallback,
            buttonCls: 'eavl-feedback-button'});
    }
});
