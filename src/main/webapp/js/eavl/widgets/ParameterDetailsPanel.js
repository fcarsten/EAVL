/**
 * Panel extension for inspecting a ParameterDetails object
 * and highlighting a number of eavl specific features
 */
Ext.define('eavl.widgets.ParameterDetailsPanel', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.pdpanel',

    parameterDetails : null,
    emptyText : null,
    pieStore : null,
    textValuesStore : null,

    /**
     * Adds the following config to Ext.panel.Panel
     * {
     *  emptyText : String - text to show when nothing is selected
     * }
     *
     * Adds the following events
     * {
     *  parameterchanged : function(this, parameterDetails) - Fired whenever the CSV data changes
     * }
     */
    constructor : function(config) {
        this.emptyText = config.emptyText ? config.emptyText : "";

        this.pieStore = Ext.create('Ext.data.Store', {
            fields : ['name', 'total']
        });

        this.textValuesStore = Ext.create('Ext.data.Store', {
            fields : ['name', 'total']
        });

        var me = this;
        Ext.apply(config, {
            layout : {
                type : 'card',
                deferredRender: false
            },
            items: [{
                itemId : 'card-empty',
                xtype : 'container',
                html : Ext.util.Format.format('<div class="pdp-empty-container"><div class="pdp-empty-container-inner"><img src="img/inspect.svg" width="100"/><br>{0}</div></div>', this.emptyText)
            },{
                itemId : 'card-inspect',
                layout : 'fit',
                items : [{
                    xtype : 'container',
                    region : 'north',
                    layout : {
                        type : 'hbox',
                        pack : 'center',
                        align : 'middle'
                    },
                    items : [{
                        xtype: 'pduompanel',
                        itemId : 'pduompanel',
                        width: 440,
                        height: 400,
                    },{
                        itemId : 'valuespie',
                        xtype : 'chart',
                        animate: true,
                        store: this.pieStore,
                        flex: 1,
                        height: 400,
                        style: {
                            background : 'white'
                        },
                        series: [{
                            type: 'pie',
                            field: 'total',
                            colorSet : ['#F7977A', '#FFF79A', '#82CA9D', '#779ECB'],
                            tips: {
                                trackMouse: true,
                                renderer: function(storeItem, item, panel) {
                                    //calculate percentage.
                                    var total = 0;
                                    me.pieStore.each(function(rec) {
                                        total += rec.get('total');
                                    });
                                    this.setTitle(storeItem.get('name') + ': ' + Math.round(storeItem.get('total') / total * 100) + '%');
                                }
                            },
                            highlightCfg: {
                                segment: {
                                    margin: 8, //workaround for extjs bug. Need to double up the margin size for the shadow
                                }
                            },
                            highlight: {
                                segment: {
                                    margin: 8
                                }
                            },
                            label: {
                                field: 'name',
                                display: 'rotate',
                                contrast: true,
                                font: '16px Arial'
                            }
                        }]
                    },{
                        itemId : 'textlist',
                        flex: 1,
                        height: 400,
                        xtype : 'grid',
                        store : this.textValuesStore,
                        hideHeaders : true,
                        disableSelection : true,
                        maxHeight: 400,
                        viewConfig : {
                            deferEmptyText : false,
                            emptyText : '<div class="text-empty-container"><div class="text-empty-container-inner"><img src="img/check.svg" width="100"/><br>No invalid values!</div></div>'
                        },
                        listeners : {
                            cellclick : Ext.bind(this._handleTextValueClick, this)
                        },
                        columns : [{
                            dataIndex : 'name',
                            flex : 1,
                            renderer : function(value, md, record) {
                                var emptyString = value === '';

                                return Ext.DomHelper.markup({
                                    tag : 'div',
                                    style : {
                                        cursor: 'pointer'
                                    },
                                    children : [{
                                        tag : 'b',
                                        style : {
                                            'font-size' : '170%',
                                            'font-style' : emptyString ? 'italic' : 'normal'
                                        },
                                        html : emptyString ? '(Zero Values)' : value
                                    },{tag : 'br'},{tag : 'br'},{
                                        tag : 'span',
                                        style : {
                                            color : '#555',
                                            'font-size' : '120%'
                                        },
                                        children : [{
                                            html : Ext.util.Format.format('{0} occurrence(s)', record.get('total'))
                                        }]
                                    }]
                                });
                            }
                        }]
                    }]
                }]
            }]
        });

        this.callParent(arguments);

        this.addEvents(['parameterchanged']);
    },

    initComponent : function() {
        this.callParent(arguments);
        this.hideParameterDetails();
    },

    /**
     * Fired when a user clicks a cell value
     */
    _handleTextValueClick : function(grid, td, cellIndex, record, tr, rowIndex, e, eOpts) {
        Ext.MessageBox.show({
            title: 'Find and Replace',
            msg: Ext.util.Format.format('Replace <b>{0}</b> with what?<br>Leave blank to enter a missing value', record.get('name') === '' ? '<i>(Zero Values)</i>' : record.get('name')),
            animateTarget: grid.getEl(),
            icon: Ext.window.MessageBox.QUESTION,
            prompt: true,
            scope : this,
            buttons : Ext.MessageBox.OK,
            fn : function(buttonId, text, opt) {
                if (buttonId === 'ok') {
                    this._handleFindReplace(record.get('name'), text);
                }
            }
        });
    },

    _handleFindReplace : function(find, replace) {
        if (replace === null || replace === undefined) {
            return;
        }

        Ext.Ajax.request({
            url: 'validation/findReplace.do',
            params: {
                find: find,
                replace: replace,
                columnIndex: this.parameterDetails.get('columnIndex')
            },
            scope: this,
            callback: function(options, success, response) {
                if (!success) {
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    return;
                }

                this.fireEvent('parameterchanged', this, this.parameterDetails);
            }
        });
    },

    /**
     * Loads the pie data store from the specified param details.
     */
    _loadPieStore : function(parameterDetails) {
        var data = [];

        if (parameterDetails.get('totalMissing') > 0) {
            data.push({name : 'No sample', total : parameterDetails.get('totalMissing')});
        } else {
            data.push({name : '', total : 0});
        }

        if (parameterDetails.get('totalText') > 0) {
            data.push({name : 'Text', total : parameterDetails.get('totalText')});
        } else {
            data.push({name : '', total : 0});
        }

        if (parameterDetails.get('totalNumeric') > 0) {
            data.push({name : 'Numeric', total : parameterDetails.get('totalNumeric')});
        } else {
            data.push({name : '', total : 0});
        }

        if (parameterDetails.get('totalZeroes') > 0) {
            data.push({name : 'Zeroes', total : parameterDetails.get('totalZeroes')});
        } else {
            data.push({name : '', total : 0});
        }

        this.pieStore.loadData(data);
    },

    /**
     * Loads the text value data store from the specified param details.
     */
    _loadTextValueStore : function(parameterDetails) {
        var data = [];

        var values = parameterDetails.get('textValues');
        for (textValue in values) {
            data.push({name : textValue, total : values[textValue]});
        }

        if (parameterDetails.get('totalZeroes') > 0) {
            data.push({name: '', total : parameterDetails.get('totalZeroes')});
        }

        this.textValuesStore.loadData(data);
    },

    /**
     * Inspects the specified parameter details. Removes any parameter details that is current being inspected
     *
     * @param parameterDetails Can be null, if so this will call hideParameterDetails
     */
    showParameterDetails : function(parameterDetails) {
        if (parameterDetails == null) {
            this.hideParameterDetails();
            return;
        }

        this.parameterDetails = parameterDetails;


        this._loadPieStore(parameterDetails);
        this._loadTextValueStore(parameterDetails);
        this.down('#pduompanel').showParameterDetails(parameterDetails);

        this.setTitle(Ext.util.Format.format('Comparing numeric and text values for "{0}"', parameterDetails.get('name')));

        if (this.getLayout().getActiveItem().getItemId() !== 'card-inspect') {
            this.getLayout().setActiveItem('card-inspect');
        }
    },

    /**
     * Hides any inspected parameter
     */
    hideParameterDetails : function() {
        this.parameterDetails = null;

        this.getLayout().setActiveItem('card-empty');

        this.setTitle('No parameter selected');
    }
});

/**
 * Panel extension for rendering the unit of measure info for a ParameterDetails instance.
 */
Ext.define('eavl.widgets.ParameterDetailsUomPanel', {
    extend: 'Ext.form.Panel',

    alias: 'widget.pduompanel',

    /**
     * Adds the following config to Ext.grid.Panel
     * {
     *  
     * }
     *
     * Adds the following events
     * {
     *
     * }
     */
    constructor : function(config) {

        Ext.apply(config, {
            layout : {
                type: 'vbox',
                align: 'center',
                pack: 'center'
            },
            items : [{
                xtype: 'container',
                itemId: 'uom-container',
                width: '100%',
                height: 100,
                html: '<div class="pdp-uom-container"><div class="pdp-uom-container-inner"><span>uom</span><img src="img/exclamation.svg" width="100"/></div></div>'
            },{
                xtype: 'container',
                itemId: 'edit-container',
                height: 100,
                layout: {
                    type: 'hbox',
                    pack: 'center',
                    align: 'middle'
                },
                items :[{
                    xtype: 'container',
                    flex: 1,
                    cls: 'uom-edit-text',
                    items: [{
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            pack: 'center',
                            align: 'middle'
                        },
                        items: [{
                            xtype: 'label',
                            text: 'Convert to ppm by '
                        },{
                            xtype: 'numberfield',
                            flex: 1,
                            itemId: 'scalefactor',
                            hideTrigger: true,
                            allowBlank: false,
                            decimalPrecision: 16
                        }]
                    },{
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            pack: 'center',
                            align: 'middle'
                        },
                        items: [{
                            xtype: 'label',
                            text: 'Change name to '
                        },{
                            xtype: 'textfield',
                            flex: 1,
                            itemId: 'editname',
                            value: 'Au_Assay_ppm'
                        }]
                    }]
                },{
                    xtype: 'container',
                    width: 60,
                    html: '<div class="pdp-convert-container"><div class="pdp-convert-container-inner"><img data-qtip="Convert all numerical values in this parameter to ppm using the specified scaling factor" src="img/convert-ppm.svg" width="50"/></div></div>'
                }]
            }]
        });

        this.callParent(arguments);
        
        this.on('afterrender', this._afterRender, this);
    },
    
    _afterRender : function() {
        this.down('#edit-container').getEl().down('.pdp-convert-container img').on('click', this._handleConvert, this);
    },
    
    _handleConvert : function() {
        var editContainer = this.down('#edit-container');
        var scaleCmp = editContainer.down('#scalefactor');
        var nameCmp = editContainer.down('#editname');
        
        if (!scaleCmp.isValid()) {
            return;
        }
        
        this.pd.set('displayName', nameCmp.getValue());
        this.pd.set('uom', eavl.models.ParameterDetails.UOM_PPM);
        this.pd.set('scaleFactor', scaleCmp.getValue());
        
        this.showParameterDetails(this.pd);
    },
    
    _generateNewName : function(name) {
        name = name.replace(/pct/i, eavl.models.ParameterDetails.UOM_PPM);
        name = name.replace(/percentage/i, eavl.models.ParameterDetails.UOM_PPM);
        name = name.replace(/percent/i, eavl.models.ParameterDetails.UOM_PPM);
        name = name.replace(/perc/i, eavl.models.ParameterDetails.UOM_PPM);
        
        if (!name.contains(eavl.models.ParameterDetails.UOM_PPM)) {
            name += ' [' + eavl.models.ParameterDetails.UOM_PPM + ']';
        }
        
        return name;
    },
    
    _lookupUomConversion : function(editContainer) {
        
        var loadMask = new Ext.LoadMask(editContainer, {msg:"Please wait..."});
        loadMask.show();
        Ext.Ajax.request({
            url: 'validation/oxidePctToTracePpm.do',
            params: {
                name: this.pd.get('name')
            },
            scope: this,
            callback: function(options, success, response) {
                loadMask.hide();
                loadMask.destroy();
                if (!success) {
                    editContainer.down('#scalefactor').setValue(null);
                    editContainer.down('#editname').setValue( this._generateNewName(this.pd.get('name')));
                    return;
                }
                
                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj.success) {
                    editContainer.down('#scalefactor').setValue(null);
                    editContainer.down('#editname').setValue( this._generateNewName(this.pd.get('name')));
                    return;
                }
                
                editContainer.down('#scalefactor').setValue(responseObj.data.conversion);
                editContainer.down('#editname').setValue(responseObj.data.element + '_' + eavl.models.ParameterDetails.UOM_PPM);
            }
        });
    },

    showParameterDetails : function(pd) {
        this.pd = pd;
        
        var uomContainer = this.down('#uom-container');
        var editContainer = this.down('#edit-container');
        
        var uomName = pd.get('uom');
        var valid = uomName === eavl.models.ParameterDetails.UOM_PPM;
        
        //update title container
        var spanEl = uomContainer.getEl().down('.pdp-uom-container span');
        var imgEl = uomContainer.getEl().down('.pdp-uom-container img');
        if (uomName) {
            spanEl.setStyle('font-size', '');
            spanEl.setStyle('font-style', '');
            spanEl.setStyle('margin-top', '');
        } else {
            uomName = 'Unknown unit of measure';
            spanEl.setStyle('font-size', '200%');
            spanEl.setStyle('font-style', 'italic');
            spanEl.setStyle('margin-top', '-70px');
        }
        spanEl.setHTML(uomName);
        
        if (valid) {
            imgEl.set({'src': 'img/check.svg'});
            imgEl.set({'data-qtip': 'The parameter has the correct unit of measure.'});
        } else {
            imgEl.set({'src': 'img/exclamation.svg'});
            imgEl.set({'data-qtip': 'All compositional parameters must have \'ppm\' as the unit of measure.'});
        }
        
        //Update edit container
        editContainer.setVisible(!valid);
        if (!valid) {
            this._lookupUomConversion(editContainer);
        }
    }
});

