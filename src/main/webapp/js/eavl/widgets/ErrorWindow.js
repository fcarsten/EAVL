/**
 * Window extension for rendering a simple error message with links to support emails.
 */
Ext.define('eavl.widgets.ErrorWindow', {
    extend: 'Ext.window.Window',

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
            layout: 'border',
            width: 800,
            height: 400,
            plain: true,
            header: false,
            resizable: false,
            modal: true,
            bodyStyle: {
                'background-color' : 'white'
            },
            items: [{
                xtype: 'container',
                region: 'north',
                height: 60,
                modal: true,
                html: Ext.DomHelper.markup({
                    tag: 'div',
                    cls: 'error-title-container',
                    children: [{
                        tag: 'div',
                        cls: 'error-title', 
                        html: config.title
                    },{
                        tag: 'div',
                        cls: 'error-contact', 
                        html: 'For support contact: <a href="mailto:cg-admin@csiro.au?Subject=EAVL%20error%20job%20' + escape(config.job.get('id')) + '&body=' + escape(config.message) + '">cg-admin@csiro.au</a>'
                    }]
                })
            },{
                xtype: 'container',
                region: 'center',
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
            }],
            listeners: {
                'afterrender' : function(win) {
                    if (config.modal) {
                        win.mon(Ext.getBody(), 'click', function(el, e){
                            win.close(win.closeAction);
                        }, win, { delegate: '.x-mask' });
                    }
                }
            }
        });

        this.callParent(arguments);
    }
});