/**
 * Panel extension for graphing an aspect of a ParameterDetails object
 */
Ext.define('eavl.widgets.charts.BaseParameterDetailsChart', {
    extend: 'portal.charts.BaseD3Chart',

    parameterDetails : null,


    /**
     * Adds the following config
     * {
     *  parameterDetails - eavl.models.ParameterDetails - Details to plot initially (can be null)
     *  preserveAspectRatio - boolean - Should the graph preserve a 4x2 aspect ratio or should it stretch. Default false
     *  targetWidth - Number - (Only useful if preserveAspectRatio is set) - The target width to use in aspect ratio
     *  targetHeight - Number - (Only useful if preserveAspectRatio is set)  - The target height to use in aspect ratio
     * }
     *
     */
    constructor : function(config) {

        this.parameterDetails = config.parameterDetails ? config.parameterDetails : null;
        if (this.parameterDetails) {
            config.initialPlotData = this.parameterDetails;
        }

        this.callParent(arguments);
    },

    plot : function(data) {
        return this.plotParameterDetails(data);
    },

    /**
     * Requests data for the specified parameter details.
     * The resulting data will be displayed
     */
    plotParameterDetails : Ext.util.UnimplementedFunction

});