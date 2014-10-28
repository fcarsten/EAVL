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
            var name = this.parameterDetails[i].get('name');
            fields.push(name);
            columns.push({itemId: name, dataIndex: name, text: name, renderer : Ext.bind(this._handleCellRender, this)});
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
            viewConfig: {
                trackOver: false
            }
        });

        this.callParent(arguments);

        this.addEvents('parameterselect');

        this._calculateCachedStyles();
    },

    /**
     * Creates a map of styles based on parameter details.
     */
    _calculateCachedStyles : function() {
        this.cachedStyles = {};

        for (var i = 0; i < this.parameterDetails.length; i++) {
            var pd = this.parameterDetails[i];

            var totalDataPoints = pd.get('totalNumeric') + pd.get('totalText') + pd.get('totalMissing');
            var percentageNumeric = (pd.get('totalNumeric') * 100) / totalDataPoints;


            var bgColor = '#FFFFFF';
            var textColor = '#000000';

            if (percentageNumeric < 70) {
                textColor = '#FF6961';
            } else if (percentageNumeric < 95) {
                textColor = '#FFB347';
            }

            this.cachedStyles[pd.get('name')] = Ext.util.Format.format("color:{0};", textColor);
        }
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
        var pd = this._getParameterDetails(colIndex);
        if (!pd) {
            return value;
        }

        metadata.style = this.cachedStyles[pd.get('name')];
        return value;
    }
});