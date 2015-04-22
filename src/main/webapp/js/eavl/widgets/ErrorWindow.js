/**
 * Window extension for rendering a simple error message with links to support emails.
 */
Ext.define('eavl.widgets.ErrorWindow', {
    extend: 'eavl.widgets.EAVLModalWindow',

    alias: 'widget.errorwindow',

    /**
     * Adds the following config to Ext.window.Window
     * {
     *    message - String - error message to display
     *    job - eavl.models.EAVLJob - The job causing this error
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {
        var htmlMessage = '<pre><code>' + config.message.replace(/</g, '&lt;').replace(/>/g, '&gt;') + '</code></pre>';
        
        Ext.apply(config, {
            width: 800,
            height: 400,
            subtitle: 'For support contact: <a href="mailto:' + eavl.widgets.FeedbackWidget.CONTACT + '?Subject=EAVL%20error%20job%20' + escape(config.job.get('id')) + '&body=' + escape(config.message) + '">' + eavl.widgets.FeedbackWidget.CONTACT + '</a>',
            items: [{
                xtype: 'container',
                cls: 'error-message',
                margin: '10',
                html: htmlMessage
            }],
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'bottom',
                items: [{
                    xtype: 'tbfill'
                },{
                    xtype: 'button',
                    cls: 'important-button',
                    scale: 'large',
                    text: 'OK',
                    handler: function(btn) {
                        btn.findParentByType('errorwindow').close();
                    }
                }]
            }]
        });

        this.callParent(arguments);
    }
});