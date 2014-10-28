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
     *  parameterDetails : eavl.models.ParameterDetails[] - The data columns in this CSVGrid
     * }
     *
     * Adds the following events
     *
     * parameterselect : function(this, parameterDetails)
     */
    constructor: function(config) {

        this.parameterDetails = config.parameterDetails ? config.parameterDetails : [];
        this.cachedStyles = null;

        var fields = [];
        var columns = [];
        for (var i = 0; i < this.parameterDetails.length; i++) {
            var pd = this.parameterDetails[i];
            var name = pd.get('name');

            var totalDataPoints = pd.get('totalNumeric') + pd.get('totalText') + pd.get('totalMissing');
            var percentageNumeric = (pd.get('totalNumeric') * 100) / totalDataPoints;
            var img = 'img/tick.png';
            var tip = 'This parameter contains more than 95% numeric values';

            if (percentageNumeric < 70) {
                img = 'img/exclamation.png';
                tip = 'This parameter contains less than 70% numeric values';
            } else if (percentageNumeric < 95) {
                img = 'img/error.png';
                tip = 'This parameter contains between 70% and 95% numeric values';
            }

            fields.push(name);
            columns.push({itemId: name, dataIndex: name, renderer : Ext.bind(this._handleCellRender, this),
                header : Ext.util.Format.format('<img data-qtip="{2}" class="csv-grid-header-icon" style="vertical-align:middle;margin-bottom:4px;" src="{0}"/>{1}', img, name, tip)});
        }

        var csvStore = Ext.create('Ext.data.Store', {
            remoteGroup: true,
            // allow the grid to interact with the paging scroller by buffering
            buffered: true,
            pageSize: 100,
            leadingBufferZone: 300,
            fields : fields,
            autoLoad: true,
            proxy: {
                // load using script tags for cross domain, if the data in on the same domain as
                // this page, an Ajax proxy would be better
                type: 'ajax',
                url: 'validation/streamRows.do',
                reader: {
                    type : 'array',
                    root: 'rows',
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
                trackOver: false,
            }
        });

        this.callParent(arguments);

        this.addEvents('parameterselect');
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
            if (this.parameterDetails[i].get('name') === name) {
                return this.parameterDetails[i];
            }
        }

        return null;
    },

    _handleCellRender : function(value, metadata, record, rowIndex, colIndex, store, view) {
        var isNumber = function(n) {return !isNaN(parseFloat(n)) && isFinite(n);}

        if (isNumber(value)) {
            return value;
        } else {
            if (value.trim() === "") {
                return '<span class="csv-grid-missing">Missing</span>';
            } else {
                return Ext.util.Format.format('<span class="csv-grid-text">{0}</span>', value);
            }
        }
    }
});