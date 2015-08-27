/**
 * A ParameterDetails is a simple set of statistics about a single parameter (column)
 * in a CSV dataset
 */
Ext.define('eavl.models.ParameterDetails', {
    extend: 'Ext.data.Model',

    statics : {
        UOM_PPM : 'ppm',
        UOM_PCT : 'pct',
        
        STATUS_GOOD : 0,
        STATUS_WARNING : 1,
        STATUS_ERROR : 2,

        /**
         * Compares two parameter details based on "status" and then on column index
         *
         * @param a eavl.models.ParameterDetails First parameter to compare
         * @param b eavl.models.ParameterDetails Second parameter to compare
         */
        sortSeverityFn : function(a, b) {
            var aStatus = a.calculateStatus();
            var bStatus = b.calculateStatus();

            if (aStatus === bStatus) {
                return a.get('columnIndex') - b.get('columnIndex');
            }

            return bStatus - aStatus;
        },

        /**
         * Compares two parameter details based on "columnIndex"
         *
         * @param a eavl.models.ParameterDetails First parameter to compare
         * @param b eavl.models.ParameterDetails Second parameter to compare
         */
        sortColIndexFn : function(a, b) {
            return a.get('columnIndex') - b.get('columnIndex');
        },

        /**
         * Given an array of eavl.models.ParameterDetails, find an instance with matching name
         * and remove it from the array. Return the removed element (or null) if it DNE.
         *
         * @param pdArray eavl.models.ParameterDetails[] Will have the first matching instance removed
         * @param name String The name to search fo
         * @param dontDelete if set to true pdArray will not be modifiedr
         */
        extractFromArray : function(pdArray, name, dontDelete) {
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

            if (!dontDelete) {
                Ext.Array.erase(pdArray, index, 1);
            }
            return matchingPd;
        },

        /**
         * Attempts to convert a string name to a unit of measure guess. Returns null if the name
         * cannot be reliably converted into a uom
         * 
         * @param name String based name to look for a uom identifier
         */
        nameToUom : function(name) {
            var lowerName = name.toLowerCase();
            if (lowerName.indexOf('ppm') >= 0) {
                return eavl.models.ParameterDetails.UOM_PPM;
            } else if (lowerName.indexOf('pct') >= 0 || lowerName.indexOf('percent')  >= 0) {
                return eavl.models.ParameterDetails.UOM_PCT;
            }
            return null;
        }
    },

    fields: [
        { name: 'name', type: 'string' }, //(ideally) unique name of this details
        { name: 'totalNumeric', type: 'int' }, //Total number of numeric values in this parameter
        { name: 'totalMissing', type: 'int' }, //Total number of missing values in this parameter
        { name: 'totalText', type: 'int' }, //Total number of text values in this parameter
        { name: 'totalZeroes', type: 'int' }, //Total number of numerical 0 values in this parameter
        { name: 'columnIndex', type: 'int' }, //The index of the column in the CSV file (0 based)
        { name: 'maxValue', type: 'number' }, //The maximum (non zero) value in this parameter
        { name: 'minValue', type: 'number' }, //The minimum (non zero) value in this parameter
        { name: 'textValues', type: 'auto' }, //Hashmap of totals keyed by each text (non numeric) value in this parameter
        { name: 'scaleFactor', type: 'number'}, //Will not be sent by the backend, but will be entered by the user
        { name: 'displayName', type: 'string', convert: function(v, data) { //The name of this ParameterDetails with any edits applied by the user
            if (!v) {
                return data.get('name');
            }
            return v;
        }},
        { name: 'uom', type: 'string', convert: function(v, data) { //The unit of measure (or null)
            if (v) {
                return v;
            }
            
            //Generate the uom from the layer name by guessing
            return eavl.models.ParameterDetails.nameToUom(data.get('name'));
        }} 

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

        if (this.get('totalZeroes') > 0) {
            return eavl.models.ParameterDetails.STATUS_ERROR;
        }
        
        if (this.get('uom') !== eavl.models.ParameterDetails.UOM_PPM) {
            return eavl.models.ParameterDetails.STATUS_ERROR;
        }

        var totalDataPoints = this.get('totalNumeric') + this.get('totalMissing');
        var percentageNumeric = (this.get('totalNumeric') * 100) / totalDataPoints;

        if (percentageNumeric < 70) {
            return eavl.models.ParameterDetails.STATUS_WARNING;
        }

        return eavl.models.ParameterDetails.STATUS_GOOD;
    },
    
    /**
     * Copies across all "additional" parameters from pd into this instance.
     * 
     * Additional parameters are the parameters NOT sent by the backend and are
     * instead modified by the user.
     */
    mergeAdditionalParams : function(pd) {
        this.set({
            uom: pd.get('uom'),
            scaleFactor: pd.get('scaleFactor'),
            displayName: pd.get('displayName')
        });
    }
});
