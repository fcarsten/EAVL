/**
 * A ParameterDetails is a simple set of statistics about a single parameter (column)
 * in a CSV dataset
 */
Ext.define('eavl.models.ParameterDetails', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'name', type: 'string' }, //(ideally) unique name of this details
        { name: 'totalNumeric', type: 'int' }, //Total number of numeric values in this parameter
        { name: 'totalMissing', type: 'int' }, //Total number of missing values in this parameter
        { name: 'totalText', type: 'int' }, //Total number of text values in this parameter
        { name: 'columnIndex', type: 'int' }, //The index of the column in the CSV file (0 based)
        { name: 'textValues', type: 'auto' } //Array of Strings representing each text (non numeric) value in this parameter
    ]
});
