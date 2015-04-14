/**
 * Simple widget that applies itself to the top of the DOM and displays as a little
 * box for prompting the user for feedback about the current page 
 */
Ext.define('eavl.widgets.FeedbackWidget', {
    
    statics : {
        CONTACT : 'cg-admin' + '@' + 'csiro.' + 'au'
    },
    
    /**
     * {
     *  metadata - Object - KVP store for metadata to be recorded in the feedback request
     * }
     */
    constructor : function(config) {
        this.callParent(arguments);
        
        Feedback({h2cPath:'js/html2canvas/html2canvas.js',
            url: 'feedback/sendFeedback.do'});
    }
});
