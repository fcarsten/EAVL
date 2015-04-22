/**
 * Window extension for an EAVL styled modal window
 */
Ext.define('eavl.widgets.EAVLModalWindow', {
    extend: 'Ext.window.Window',

    /**
     * Adds the following config to Ext.window.Window
     * {
     *    subtitle - [Optional] String Displayed in the top right  
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {        
        Ext.apply(config, {
            layout: 'border',
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
                        cls: 'eavl-modal-title', 
                        html: config.title
                    },{
                        tag: 'div',
                        cls: 'eavl-modal-subtitle', 
                        html: config.subtitle
                    }]
                })
            },{
                xtype: 'container',
                region: 'center',
                margin: '10',
                layout: 'fit',
                style: {
                    'border-style' : 'none'
                },
                items: config.items
            }]
        });

        this.callParent(arguments);
        
        //Make click events outside the window close the popup
        this.on('afterrender', function(win) {
            if (config.modal) {
                win.mon(Ext.getBody(), 'click', function(el, e){
                    win.close(win.closeAction);
                }, win, { delegate: '.x-mask' });
            }
        });
    }
});