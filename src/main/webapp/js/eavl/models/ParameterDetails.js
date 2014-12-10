/**
 * A ParameterDetails is a simple set of statistics about a single parameter (column)
 * in a CSV dataset
 */
Ext.define('eavl.models.ParameterDetails', {
    extend: 'Ext.data.Model',

    statics : {
        STATUS_GOOD : 0,
        STATUS_WARNING : 1,
        STATUS_ERROR : 2,

        /**
         * Compares two parameter details based on "status"
         *
         * @param a eavl.models.ParameterDetails First parameter to compare
         * @param b eavl.models.ParameterDetails Second parameter to compare
         */
        sortSeverityFn : function(a, b) {
            var aStatus = a.calculateStatus();
            var bStatus = b.calculateStatus();

            if (aStatus === bStatus) {
                return a.get('name').localeCompare(b.get('name'));
            }

            return bStatus - aStatus;
        },

        /**
         * Given an array of eavl.models.ParameterDetails, find an instance with matching name
         * and remove it from the array. Return the removed element (or null) if it DNE.
         *
         * @param pdArray eavl.models.ParameterDetails[] Will have the first matching instance removed
         * @param name String The name to search for
         */
        extractFromArray : function(pdArray, name) {
            var matchingPd = null;
            var index = -1;
            Ext.each(pdArray, function(pd, i) {
                if (pd.get('name') === name) {
                    index = i;
                    matchingPd = pd;
                    return false;
                }
            });

            if (index < 0) {
                return null;
            }

            Ext.Array.erase(pdArray, index, 1);
            return matchingPd;
        }
    },

    fields: [
        { name: 'name', type: 'string' }, //(ideally) unique name of this details
        { name: 'totalNumeric', type: 'int' }, //Total number of numeric values in this parameter
        { name: 'totalMissing', type: 'int' }, //Total number of missing values in this parameter
        { name: 'totalText', type: 'int' }, //Total number of text values in this parameter
        { name: 'columnIndex', type: 'int' }, //The index of the column in the CSV file (0 based)
        { name: 'textValues', type: 'auto' } //Hashmap of totals keyed by each text (non numeric) value in this parameter
    ],

    idProperty : 'name',

    /**
     * Calculates one of [STATUS_GOOD, STATUS_WARNING, STATUS_ERROR] for this ParameterDetails object.
     *
     */
    calculateStatus : function() {

        if (this.get('totalText') > 0) {
            return eavl.models.ParameterDetails.STATUS_ERROR;
        }

        var totalDataPoints = this.get('totalNumeric') + this.get('totalMissing');
        var percentageNumeric = (this.get('totalNumeric') * 100) / totalDataPoints;

        if (percentageNumeric < 70) {
            return eavl.models.ParameterDetails.STATUS_WARNING;
        }

        return eavl.models.ParameterDetails.STATUS_GOOD;
    }
});
