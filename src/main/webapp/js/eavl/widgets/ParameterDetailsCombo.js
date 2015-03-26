/**
 * Field extension for holding a single instance of a
 * eavl.models.ParameterDetails
 */
Ext.define('eavl.widgets.ParameterDetailsCombo', {
    extend: 'Ext.form.field.ComboBox',


    alias: 'widget.pdcombo',

    allowBlank : null,
    parameterDetails : null,

    //This is the normal fieldSubTpl but with an additional image added beforehand
    //This image will be used to render our status icon with some trickery 
    fieldSubTpl: [ 
       '<img class="pdcombo-icon" src=""/>',
       '<input id="{id}" data-ref="inputEl" type="{type}" role="{role}" {inputAttrTpl}',
           ' size="1"', // allows inputs to fully respect CSS widths across all browsers
           '<tpl if="name"> name="{name}"</tpl>',
           '<tpl if="value"> value="{[Ext.util.Format.htmlEncode(values.value)]}"</tpl>',
           '<tpl if="placeholder"> placeholder="{placeholder}"</tpl>',
           '{%if (values.maxLength !== undefined){%} maxlength="{maxLength}"{%}%}',
           '<tpl if="readOnly"> readonly="readonly"</tpl>',
           '<tpl if="disabled"> disabled="disabled"</tpl>',
           '<tpl if="tabIdx != null"> tabindex="{tabIdx}"</tpl>',
           '<tpl if="fieldStyle"> style="{fieldStyle}"</tpl>',
       ' class="{fieldCls} {typeCls} {typeCls}-{ui} {editableCls} {inputCls}" autocomplete="off"/>',
       {
           disableFormats: true
       }
    ],
    
    
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
        
        var sorters = [];
        if (config.sortFn) {
            sorters.push({
                sorterFn : config.sortFn
            });
        }
        
        var items = [];
        Ext.each(config.parameterDetails, function(pd) {
            
            var status = pd.calculateStatus();
            var img = 'img/tick.png';
            var tip = 'This parameter contains more than 70% numeric values';
            if (status === eavl.models.ParameterDetails.STATUS_ERROR) {
                img = 'img/exclamation.png';
                tip = 'This parameter contains non numeric values.';
            } else if (status === eavl.models.ParameterDetails.STATUS_WARNING) {
                img = 'img/error.png';
                tip = 'This parameter less than 70% numeric values.';
            }
            
            items.push({
                name: pd.get('name'),
                displayName: pd.get('displayName'),
                img: img,
                tip: tip
            });
        });
        
        var store = Ext.create('Ext.data.Store', {
            fields: ["name", "displayName", "img", "tip"],
            data : items
        });
        
        this.parameterDetails = config.parameterDetails;
        
        var tpl = '<div style="display:table;"><img class="pdcombo-popup-icon" data-qtip="{tip}" src="{img}"><span class="pdcombo-popup-text">{name}</span></div>';
        Ext.apply(config, {
           store: store,
           displayField: 'displayName',
           valueField: 'name',
           fieldCls: 'pdcombo-field',
           listConfig : {
               cls: 'pdcombo',
               getInnerTpl: function() {
                   return tpl;
               }
           },
           forceSelection: true,
           typeAhead: true,
           typeAheadDelay: 10,
           queryMode: 'local'
        });
        this.callParent(arguments);
        
        this.on('change', this._updateIcon, this);
        this.on('afterrender', function(me) {
            me._updateIcon(me, me.getValue());
        });
    },

    _updateIcon : function(me, newValue) {
        var iconEl = me.getEl().down('.pdcombo-icon');
        var pd = me._getPdSelection(newValue);
        
        var img = '';
        var tip = '';
        var display = 'none';
        if (pd) {
            img = pd.get('img');
            tip = pd.get('tip');
            display = '';
        }
        
        iconEl.set({
            src: img,
            'data-qtip': tip
        });
        iconEl.setStyle('display', display);
    },
    
    _getPdSelection : function(name) {
        if (!name) {
            name = this.getValue();
        }
        
        var match = null;
        this.getStore().each(function(pd) {
            if (pd.get('name') === name) {
                match = pd;
                return false;
            }
        });
        
        return match;
    },
    
    /**
     * If name is undefined, gets the selected parameter details object (or null).
     * 
     * If name is specified as a string, gets the internal PD with the specified name. 
     */
    getParameterDetails : function(name) {
        if (!name) {
            name = this.getValue();
        }
        
        var match = null;
        Ext.each(this.parameterDetails, function(pd) {
            if (pd.get('name') === name) {
                match = pd;
                return false;
            }
        });
        
        return match;
    }
    
});