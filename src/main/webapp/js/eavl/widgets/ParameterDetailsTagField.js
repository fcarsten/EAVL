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
        
        var valueStrings = [];
        Ext.each(config.value, function(v) {
            valueStrings.push(v.get('name'));
        });
        config.value = valueStrings;
        
        Ext.apply(config, {
            cls: 'pdtf',
            store : store,
            displayField: 'name',
            valueField: 'name',
            emptyText: Ext.util.Format.format('<span class="pdl-row-text" style="font-style:italic;font-weight:normal;color:#aaaaaa;padding:0px;padding-top:13px;">{0}</span>', config.emptyText ? config.emptyText : ""),
            listConfig : {
                cls: 'pdcombo',
                getInnerTpl: function() {
                    return '<div style="display:table;"><img class="pdtf-popup-icon" data-qtip="{tip}" src="{img}"><span class="pdtf-popup-text">{name}</span></div>';
                }
            },
            triggers: {
                all: {
                    cls: 'pdtf-all-trigger',
                    handler: Ext.bind(this._handleAllClicked, this),
                    weight: -998
                },
                clear: {
                    cls: 'x-form-clear-trigger',
                    handler: Ext.bind(this._handleClearClicked, this),
                    weight: -999
                }
            }
        });
        this.callParent(arguments);
        
        this.on('change', this._emptyTextWorkaround);
        this.on('render', function() {
            //Another extjs empty text workaround...
            if (config.value) {
                this._emptyTextWorkaround(this, config.value, []);
            }
        }, this);
    },
    
    _handleClearClicked : function() {
        this.setValue([]);
    },
    
    _handleAllClicked : function() {
        var values = [];
        Ext.each(this.parameterDetails, function(pd) {
            values.push(pd.get('name'));
        });
        this.setValue(values);
    },
    
    //Workaround for empty text not getting removed
    //see http://www.sencha.com/forum/showthread.php?285390
    _emptyTextWorkaround : function(me, newValue, oldValue) {
        newValue = this.getValue(); //Another workaround for out of order events...
        if (!Ext.isEmpty(newValue) && Ext.isEmpty(oldValue)) {
            me.getEl().down('.x-tagfield-input div.x-form-empty-field').setStyle({'display' : 'none'});
        } else if (Ext.isEmpty(newValue) && !Ext.isEmpty(oldValue)) {
            me.getEl().down('.x-tagfield-input div.x-form-empty-field').setStyle({'display' : 'block'});
        }
    },
    
    /**
     * Overidden from ExtJS base
     *      Removed tag selection
     *      Added icon with unique tooltip to each tag
     * 
     * Build the markup for the labelled items. Template must be built on demand due to ComboBox initComponent
     * lifecycle for the creation of on-demand stores (to account for automatic valueField/displayField setting)
     * @private
     */
    getMultiSelectItemMarkup: function() {
        var me = this,
            cssPrefix = Ext.baseCSSPrefix,
            valueField = me.valueField;

        if (!me.multiSelectItemTpl) {
            if (!me.labelTpl) {
                me.labelTpl = '{' + me.displayField + '}';
            }
            me.labelTpl = me.getTpl('labelTpl');

            me.multiSelectItemTpl = new Ext.XTemplate([
                '<tpl for=".">',
                    '<li data-selectionIndex="{[xindex - 1]}" data-recordId="{internalId}" data-value="{[this.getItemValue(values)]}" class="' + cssPrefix + 'tagfield-item',
                    '{%',
                        'values = values.data;',
                    '%}',
                    '" qtip="{' + me.displayField + '}">' ,
                    '<img class="pdtf-tag-icon" data-qtip="{[this.getItemTip(values)]}" src="{[this.getItemIcon(values)]}">',
                    '<div class="' + cssPrefix + 'tagfield-item-text">{[this.getItemLabel(values)]}</div>',
                    '<div class="' + cssPrefix + 'tagfield-item-close"></div>' ,
                    '</li>' ,
                '</tpl>',
                {
                    getItemLabel: function(values) {
                        return me.labelTpl.apply(values);
                    },
                    getItemValue: function(rec) {
                        return rec.get(valueField);
                    },
                    getItemIcon: function(rec) {
                        return rec.img;
                    },
                    getItemTip: function(rec) {
                        return rec.tip;
                    },
                    strict: true
                }
            ]);
        }
        if (!me.multiSelectItemTpl.isTemplate) {
            me.multiSelectItemTpl = this.getTpl('multiSelectItemTpl');
        }

        return me.multiSelectItemTpl.apply(me.valueCollection.getRange());
    }
});