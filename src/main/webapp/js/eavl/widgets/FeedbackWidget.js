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
     * }
     */
    constructor : function(config) {
        var metadata = config.metadata ? config.metadata : {};
        
        metadata.url = window.location.href;
        metadata.platform = navigator.platform;
        metadata.userAgent = navigator.userAgent;
        
        this.callParent(arguments);
        
        Feedback({h2cPath:'js/html2canvas/html2canvas.js',
            url: 'feedback/sendFeedback.do',
            metadata: metadata});
    }
});
