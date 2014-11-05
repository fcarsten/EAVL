/**
 * Field extension for holding a single instance of a
 * eavl.models.ParameterDetails
 */
Ext.define('eavl.widgets.ParameterDetailsField', {
    extend: 'eavl.widgets.ParameterDetailsList',

    mixins: {
        field: 'Ext.form.field.Field'
    },

    alias: 'widget.pdfield',

    /**
     * Adds the following config to Ext.form.field.Base
     * {
     *  emptyText - Text to show if this field is empty
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {
        Ext.apply(config, {
            viewConfig : {
                deferEmptyText : false,
                emptyText: Ext.util.Format.format('<span class="pdl-row-text" style="font-style:italic;font-weight:normal;color:#aaaaaa;padding:5px 0 0 20px;">{0}</span>', config.emptyText ? config.emptyText : "")
            }
        });
        this.callParent(arguments);
    },

    initComponent : function() {
        this.initField();

        this.callParent(arguments);
    },


    setValue : function(parameterDetails) {
        this.getStore().removeAll();
        if (parameterDetails != null) {
            this.getStore().add(parameterDetails);
        }
    },

    reset : function() {
        this.setValue(null);
    },

    getValue : function() {
        if (this.getStore().getCount() === 0) {
            return null;
        }

        return this.getStore().getAt(0);
    }
});