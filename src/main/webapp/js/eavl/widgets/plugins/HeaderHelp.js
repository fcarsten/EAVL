/**
 * A plugin for an Ext.panel.Panel class that provides a single help icon
 * that upon mouseover shows a tooltip.
 */
Ext.define('eavl.widgets.plugins.HeaderHelp', {
    extend: 'eavl.widgets.plugins.HeaderIcons',
    alias: 'plugin.headerhelp',
    
    /**
     * Adds the following constructor args
     * {
     *  text: String - help text to display when the help icon is hovered over.
     * }
     */
    constructor : function(cfg) {
        var me = this;
        cfg.icons = [{
            location: 'text',
            tip: cfg.text,
            src: '../img/info.svg',
            width: 24,
            height: 24
        }];
        this.callParent(arguments);
    }
});