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
                layout : {
                    type : 'vbox',
                    pack : 'center',
                    align : 'center'
                },
                padding: '50 25 50 25',
                items : [{
                    xtype : 'image',
                    maxWidth: 200,
                    src : 'img/inspect.svg',
                },{
                    margin : '20 0 0 0',
                    html : this.emptyText
                }]
            },{
                itemId : 'card-inspect',
                layout : {
                    type : 'border',
                },
                items : [{
                    xtype : 'container',
                    region : 'north',
                    height : 300,
                    layout : {
                        type : 'hbox',
                        pack : 'center',
                        align : 'stretch'
                    },
                    items : [{
                        itemId : 'valuespie',
                        xtype : 'chart',
                        animate: true,
                        store: this.pieStore,
                        flex: 1,
                        style: {
                            background : 'white'
                        },
                        series: [{
                            type: 'pie',
                            field: 'total',
                            colorSet : ['#F7977A', '#FFF79A', '#82CA9D'],
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
                            highlight: {
                                segment: {
                                    margin: 20
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
                        xtype : 'grid',
                        store : this.textValuesStore,
                        hideHeaders : true,
                        disableSelection : true,
                        viewConfig : {
                            emptyText : '<b>This parameter doesn\'t have any non numeric values</b>'
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
                                        html : emptyString ? '(empty string)' : value
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
                },{
                    itemId : 'values',
                    region: 'center',
                    xtype : 'panel',
                    html : 'This will be a wicked line graph'
                }]
            }]
        });

        this.callParent(arguments);
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
            msg: Ext.util.Format.format('Replace <b>{0}</b> with what:', record.get('name') === '' ? '<i>(empty string)</i>' : record.get('name')),
            animateTarget: grid.getEl(),
            icon: Ext.window.MessageBox.QUESTION,
            prompt: true,
            scope : this,
            buttons : Ext.MessageBox.OK,
            fn : function(buttonId, text, opt) {
                if (buttonId === 'ok' && text) {
                    this._handleFindReplace(record.get('name'), text);
                }
            }
        });
    },

    _handleFindReplace : function(find, replace) {
        if (replace === null || replace === undefined) {
            return;
        }

        console.log('Finding: ', find, ' and replacing with ', replace);
    },

    /**
     * Loads the pie data store from the specified param details.
     */
    _loadPieStore : function(parameterDetails) {
        var data = [];

        if (parameterDetails.get('totalMissing') > 0) {
            data.push({name : 'Missing', total : parameterDetails.get('totalMissing')});
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

        this.pieStore.loadData(data);
    },

    /**
     * Loads the text value data store from the specified param details.
     */
    _loadTextValueStore : function(parameterDetails) {
        var data = [];

        values = parameterDetails.get('textValues');
        for (textValue in values) {
            data.push({name : textValue, total : values[textValue]});
        }

        if (parameterDetails.get('totalMissing') > 0) {
            data.push({name : '', total : parameterDetails.get('totalMissing')});
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

        if (this.parameterDetails != null && this.parameterDetails.get('name') === parameterDetails.get('name')) {
            return;
        }

        this.parameterDetails = parameterDetails;


        this._loadPieStore(parameterDetails);
        this._loadTextValueStore(parameterDetails);

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
    }
});