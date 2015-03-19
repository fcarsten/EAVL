/**
 * Field extension for holding a zero to many instances of a
 * eavl.models.ParameterDetails in a compact single field manner
 */
Ext.define('eavl.widgets.ParameterDetailsTagField', {
    extend: 'Ext.form.field.Tag',

    alias: 'widget.pdtagfield',

    allowBlank : null,
    
    /**
     * Adds the following config to Ext.form.field.Base
     * {
     *  emptyText - Text to show if this field is empty,
     *  parameterDetails - The entire set of possible values for this tag field.
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {
        var store = Ext.create('Ext.data.Store', {
            model: 'eavl.models.ParameterDetails',
            data: config.parameterDetails
        });
        
        var valueStrings = [];
        Ext.each(config.value, function(v) {
            valueStrings.push(v.get('name'));
        });
        config.value = valueStrings;
        
        Ext.apply(config, {
            cls: 'pdtf',
            //displayTpl: '<img src="img/tick.png" width="16" height"16">{[values]}',
            store : store,
            displayField: 'name',
            valueField: 'name',
            emptyText: Ext.util.Format.format('<span class="pdl-row-text" style="font-style:italic;font-weight:normal;color:#aaaaaa;padding:0px;">{0}</span>', config.emptyText ? config.emptyText : "")
        });
        this.callParent(arguments);
        
        this.on('change', this._emptyTextWorkaround);
        this.on('select', this._tagSelect);
    },
    
    //Workaround for empty text not getting removed
    //see http://www.sencha.com/forum/showthread.php?285390
    _emptyTextWorkaround : function(me, newValue, oldValue) {
        if (!Ext.isEmpty(newValue) && Ext.isEmpty(oldValue)) {
            me.getEl().down('.x-tagfield-input div.x-form-empty-field').setStyle({'display' : 'none'});
        } else if (Ext.isEmpty(newValue) && !Ext.isEmpty(oldValue)) {
            me.getEl().down('.x-tagfield-input div.x-form-empty-field').setStyle({'display' : 'block'});
        }
    },
    
    _tagSelect : function(me, record) {
        console.log(record);
    }
});