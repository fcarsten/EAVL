/**
 * Utilities for highlighting a component with a temporary
 * glow or other effects
 */
Ext.define('eavl.widgets.util.HighlightUtil', {
    singleton: true
}, function() {
    /**
     * Similar to Ext.Dom.highlight but can be applied to a component whose
     * body is partially obscured by a view or other components.
     *
     * @param component Ext.Component which will be highlighted via a temporary mask.
     */
    eavl.widgets.util.HighlightUtil.highlight = function(component) {
        var body = component.getEl();

        //We can't use the standard el.highlight as it doesn't play nice with grid view
        //So instead we make a "mask" and make it behave like a highlight
        Ext.DomHelper.append(body.dom, [{
            cls : Ext.baseCSSPrefix + "mask" + ' mask-highlight',
            style : {
                'background-color' : 'ffff9c'
            }
        }]);

        maskEl = Ext.get(body.dom.lastChild);
        maskEl.setDisplayed(true);
        maskEl.fadeOut({
            opacity : 0,
            easing : 'ease-in',
            duration : 1000,
            remove: true
        });
    };
});