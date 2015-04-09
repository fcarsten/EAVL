/**
 * Grid Panel extension for scrolling through a large CSV file
 * and highlighting a number of eavl specific features
 */
Ext.define('eavl.widgets.CSVGrid', {
    extend: 'Ext.grid.Panel',

    alias: 'widget.csvgrid',

    parameterDetails : null,
    cachedStyles : null,

    /**
     * Adds the following config options
     * {
     *  parameterDetails : eavl
     *  .models.ParameterDetails[] - The data columns in this CSVGrid
     *  jobId : String - [Optional] EavlJob ID to request CSV data from (defaults to session job)
     *  file : String - [Optional] EavlJob file name to request CSV data from (defaults to session job)
     *  readOnly: Boolean - [Optional - default false] Is this grid read only?
     *  sortColumns: Boolean - [Optional - default true] Sort the column headers so bad columns are first
     * }
     *
     * Adds the following events
     * {
     *  parameterchanged : function(this, parameterDetails)
     * }
     */
    constructor: function(config) {

        this.parameterDetails = config.parameterDetails ? config.parameterDetails : [];
        this.cachedStyles = null;

        this.readOnly = config.readOnly ? true : false;
        this.sortColumns = config.sortColumns === false ? false : true;

        var proxyParams = {};
        if (config.jobId) {
            proxyParams['jobId'] = config.jobId;
        }
        if (config.file) {
            proxyParams['file'] = config.file;
        }

        //Sort our columns so that "bad" columns are first
        if (this.sortColumns) {
            this.parameterDetails = Ext.Array.sort(this.parameterDetails, eavl.models.ParameterDetails.sortSeverityFn);
        }

        var fields = [];
        var columns = [];
        for (var i = 0; i < this.parameterDetails.length; i++) {
            var pd = this.parameterDetails[i];
            var name = pd.get('name');

            fields.push(name);
            columns.push(this.generateColumnForParameterDetails(pd));
        }

        var csvStore = Ext.create('Ext.data.Store', {
            remoteGroup: true,
            // allow the grid to interact with the paging scroller by buffering
            buffered: true,
            pageSize: 100,
            leadingBufferZone: 100,
            fields : fields,
            autoLoad: true,
            proxy: {
                // load using script tags for cross domain, if the data in on the same domain as
                // this page, an Ajax proxy would be better
                type: 'ajax',
                url: 'validation/streamRows.do',
                extraParams: proxyParams,
                reader: {
                    type : 'array',
                    rootProperty: 'rows',
                    totalProperty: 'totalCount'
                }
            }
        });

        Ext.apply(config, {
            columns : columns,
            store : csvStore,
            loadMask: true,
            multiSelect: true,
            selModel: {
                pruneRemoved: false
            },
            bodyCls : 'csv-grid-body',
            viewConfig: {
                trackOver: false
            }
        });

        this.callParent(arguments);

        this.on('cellclick', this._handleCellClick, this);
    },

    _handleCellClick : function(me, td, cellIndex, record, tr, rowIndex, e, eOpts ) {
        if (this.readOnly) {
            return;
        }

        var pd = this._getParameterDetails(cellIndex);
        var find = record.get(pd.get('name')).trim();

        //Don't do popup if we selected a number
        if (this._isNumber(find)) {
            return;
        }

        Ext.MessageBox.show({
            title: 'Find and Replace',
            msg: Ext.util.Format.format('Replace <b>{0}</b> with what:', find ? find : '<i>(No sample)</i>'),
            animateTarget: this.getEl(),
            icon: Ext.window.MessageBox.QUESTION,
            prompt: true,
            scope : this,
            buttons : Ext.MessageBox.OK,
            fn : function(buttonId, text, opt) {
                if (buttonId === 'ok' && text) {
                    Ext.Ajax.request({
                        url: 'validation/findReplace.do',
                        params: {
                            find: find,
                            replace: text,
                            columnIndex: pd.get('columnIndex')
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

                            this.fireEvent('parameterchanged', this, pd);
                        }
                    });
                }
            }
        });
    },

    _parameterDetailsToColHeader : function(pd) {
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

        return Ext.util.Format.format('<img data-qtip="{2}" class="csv-grid-header-icon" style="vertical-align:middle;margin-bottom:4px;" src="{0}"/>{1}', img, pd.get('name'), tip);
    },

    /**
     * Turns an arbitrary string into something that is safe to use for an ExtJS item ID
     */
    _extjsSafeId : function(name) {
        return 'csv-' + name.replace(/ /g, '_').replace(/[^a-zA-Z0-9_\-]/g,'');
    },
    
    /**
     * Generates a grid column for the specified parameterDetails (does not add the column to this grid)
     *
     * @param parameterDetails a eavl.models.ParameterDetails object
     */
    generateColumnForParameterDetails : function(parameterDetails) {
        var name = parameterDetails.get('name');
        return Ext.create('Ext.grid.column.Column', {
            itemId: this._extjsSafeId(name),
            dataIndex: name,
            renderer : Ext.bind(this._handleCellRender, this),
            header : this._parameterDetailsToColHeader(parameterDetails), sortable: false
        });
    },

    /**
     * Causes this grid to completely reload its data and headers in accordance to updated parameter details.
     *
     * @param parameterDetails eavl.models.ParameterDetails[] - The updated parameter details (must be the same set of parameter details with updated values)
     */
    reloadParameterDetails : function(parameterDetails) {
        this.parameterDetails = parameterDetails;
        for (var i = 0; i < this.columns.length; i++) {
            var parameterDetails = this._getParameterDetails(i);

            //This will dive into the guts of the column header and replace the text
            var colEl = Ext.get(this.columns[i].id);
            var textParent = colEl.down('span.x-column-header-text');
            Ext.each(textParent.select('*'), function(el) {
                el.destroy();
            });
            textParent.dom.innerHTML = this._parameterDetailsToColHeader(parameterDetails);
        }

        //Updating the view is really tough! Unfortunately I can only go with the nuclear option.
        //This means scroll position is NOT preserved. Would be great to figure out a fix!
        //(preserveScrollOnRefresh doesn't seem to work either)
        this.getStore().removeAll();
        this.getStore().load();
    },

    /**
     * Gets a parameter details for a column based on the relative view of the grid.
     *
     * Returns null if it cant be found
     *
     * @param cellIndex - The displayed column index (not the actual columnIndex)
     */
    _getParameterDetails : function(cellIndex) {
        var column = this.columns[cellIndex];
        var name = column.getItemId();

        for (var i = 0; i < this.parameterDetails.length; i++) {
            if (this._extjsSafeId(this.parameterDetails[i].get('name')) === name) {
                return this.parameterDetails[i];
            }
        }

        return null;
    },

    /**
     * Return true if n is a number
     */
    _isNumber : function(n) {
        return !isNaN(parseFloat(n)) && isFinite(n);
    },

    _handleCellRender : function(value, metadata, record, rowIndex, colIndex, store, view) {
        if (this._isNumber(value)) {
            return value;
        } else {
            if (value.trim() === "") {
                return '<span class="csv-grid-missing">No sample</span>';
            } else if (this.readOnly) {
                return Ext.util.Format.format('<span class="csv-grid-text">{0}</span>', value);
            } else {
                return Ext.util.Format.format('<span class="csv-grid-text csv-grid-text-clickable">{0}</span>', value);
            }
        }
    }
});