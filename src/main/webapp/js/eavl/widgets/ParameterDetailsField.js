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

    allowBlank : null,

    /**
     * Adds the following config to Ext.form.field.Base
     * {
     *  emptyText - Text to show if this field is empty,
     *  allowBlank - Whether this can be empty or not, defaults true
     *  preventMark - Boolean - Whether this can be marked invalid or not
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {

        this.allowBlank = config.allowBlank === undefined ? true : config.allowBlank;
        this.preventMark = config.preventMark ? true : false;

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
    },

    getErrors : function(value) {
        var errors = [];

        if (!this.allowBlank) {
            if (!this.getValue()) {
                errors.push('This field cannot be blank');
            }
        }

        return errors;
    },

    markInvalid: function(errors) {
        eavl.widgets.util.HighlightUtil.highlight(this, '#ff6961');
    },

    clearInvalid: function() {
        this.getView().getEl().removeCls('pdl-row-error');
    },

    validateValue: function(value) {
        var me = this,
            errors = me.getErrors(value),
            isValid = Ext.isEmpty(errors);
        if (!me.preventMark) {
            if (isValid) {
                me.clearInvalid();
            } else {
                me.markInvalid(errors);
            }
        }

        return isValid;
    },

    isValid : function() {
        var me = this,
            disabled = me.disabled,
            validate = me.forceValidation || !disabled;


        return validate ? me.validateValue(me.getValue()) : disabled;
    },
});