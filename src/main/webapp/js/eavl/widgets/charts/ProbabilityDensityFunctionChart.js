/**
 * Panel extension for graphing a Probability Density Function
 */
Ext.define('eavl.widgets.charts.ProbabilityDensityFunctionChart', {
    extend: 'eavl.widgets.charts.BaseParameterDetailsChart',

    alias: 'widget.pdfchart',


    /**
     * Adds the following config
     * {
     *  allowCutoffSelection - Boolean (- If true, the chart will have a draggable cutoff slider added. Defaults to false
     *  cutoffValue - Number - Initial value for the cutoff brush (only valid if parameterDetails is set)
     *  file - String [Optional] - The file to pull numbers from. Defaults to eavl.models.EAVLJob.FILE_DATA_CSV
     * }
     *
     * Adds the following events
     * {
     *  cutoffchanged : function(this, cutoffXValue)
     * }
     */
    constructor : function(config) {

        config.svgClass = 'pdf-chart-svg';

        this.file = config.file ? config.file : eavl.models.EAVLJob.FILE_DATA_CSV;
        this.allowCutoffSelection = config.allowCutoffSelection ? true : false;
        this._cutoffOverride = Ext.isNumber(config.cutoffValue) ? config.cutoffValue : null;
        this.callParent(arguments);
    },

    /**
     * Gets the selected cutoff value (if enabled) otherwise returns null.
     */
    getCutoffValue : function() {
        if (!this.d3 || !this.d3.brush || this.d3.brush.empty()) {
            return null;
        }

        var logValue = this.d3.brush.extent()[0];
        return Math.pow(10, logValue); //inverse log to get the actual cutoff value
    },

    _handleBrush : function(fireEvent) {
        if (this.d3.brush.empty()) {
            this.d3.brushgroup.selectAll('.brush-text').attr("x", 9999);
            this.fireEvent('cutoffchanged', this, null);
            return;
        }

        var value = this.d3.brush.extent()[0];
        if (d3.event && d3.event.sourceEvent) { // not a programmatic event
            this.d3.brush.extent([value, this.d3.x.domain()[1]]);
        }

        var valueIndex = d3.bisect(this.d3.xData, value);
        var totalArea = 0;
        var domain = this.d3.x.domain();
        var range = domain[1] - domain[0];
        var previous = this.d3.xData[valueIndex], current;
        for (var i = valueIndex + 1; i < this.d3.xData.length; i++) {
            current = this.d3.xData[i];
            totalArea += this.d3.yData[i] * (current - previous);
            previous = current;
        }

        var x = this.d3.brushgroup.selectAll(".extent").attr("x");

        this.d3.brushgroup.selectAll(".extent").attr("width", 9999);
        this.d3.brushgroup.selectAll('.resize.w rect').attr("width", 9999);


        var text = Ext.util.Format.format("{0}% High Values", Ext.util.Format.number(totalArea * 100, '0'));
        this.d3.brushgroup.selectAll('.brush-text').attr("x", Number(x) + 100).text(text);

        if (fireEvent) {
            this.fireEvent('cutoffchanged', this, value);
        }
    },

    /**
     * Requests PDF data for the specified parameter details.
     * The resulting data will be displayed
     */
    plotParameterDetails : function(parameterDetails) {
        var me = this;
        if (parameterDetails == null) {
            me.clearPlot();
            return;
        }
        this.parameterDetails = parameterDetails;

        this.mask(Ext.util.Format.format("Loading data for '{0}'", parameterDetails.get('name')));
        d3.json(Ext.String.urlAppend("wps/getPDFData.do", Ext.Object.toQueryString({columnIndex : parameterDetails.get('columnIndex'), file: this.file})),
                function(error, data) {
            me.maskClear();
            if (error) {
                me.clearPlot("There was an error accessing PDF data");
                return;
            }

            //Lookup or create our lines and axes
            var margin = {top: 20, right: 20, bottom: 30, left: 75};
            var width = me.targetWidth - margin.left - margin.right;
            var height = me.targetHeight - margin.top - margin.bottom;
            var update = true;

            if (!me.d3) {
                update = false;
                me.d3 = {};
            }

            var x = me.d3.x ? me.d3.x : me.d3.x = d3.scale.linear().range([0, width]);
            var y = me.d3.y ? me.d3.y : me.d3.y = d3.scale.linear().range([height, 0]);

            var xAxis = me.d3.xAxis ? me.d3.xAxis : me.d3.xAxis = d3.svg.axis().scale(x).orient("bottom");
            var yAxis = me.d3.yAxis ? me.d3.yAxis : me.d3.yAxis = d3.svg.axis().scale(y).orient("left");

            if (me.allowCutoffSelection) {
                var brush = me.d3.brush ? me.d3.brush : me.d3.brush = d3.svg.brush()
                        .x(x)
                        .on("brush", Ext.bind(me._handleBrush, me, [true], false));
            }


            me.d3.xData = data.map(function(d) { return d[0]});
            me.d3.yData = data.map(function(d) { return d[1]});
            var valueline = me.d3.valueLine ? me.d3.valueLine : me.d3.valueLine = d3.svg.line()
                    .x(function(d) { return x(d[0]); })
                    .y(function(d) { return y(d[1]); });

            x.domain(d3.extent(data, function(d) { return d[0]; }));
            y.domain(d3.extent(data, function(d) { return d[1]; }));

            //Either update existing line or create new one
            var title = Ext.util.Format.format('Probability Density Function for log({0})', parameterDetails.get('name'));
            if (update) {
                var svg = me.d3svg.transition();

                svg.select(".line")
                    .duration(750)
                    .attr("d", valueline(data));
                svg.select(".x.axis") // change the x axis
                    .duration(750)
                    .call(xAxis);
                svg.select(".y.axis") // change the y axis
                    .duration(750)
                    .call(yAxis);

                svg.select('.title').text(title)
            } else {
                var g = me.d3svg.append("g")
                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                g.append("g")
                    .attr("class", "x axis")
                    .attr("transform", "translate(0," + height + ")")
                    .call(xAxis);

                g.append("g")
                    .attr("class", "y axis")
                    .call(yAxis)
                  .append("text")
                    .attr("transform", "rotate(-90)")
                    .attr("y", 6)
                    .attr("dy", ".71em")
                    .style("text-anchor", "end")
                    .text("density");

                g.append("path")
                    .datum(data)
                    .attr("class", "line")
                    .attr("d", valueline(data));

                if (me.d3.brush) {
                    me.d3.brushgroup = g.append("g")
                        .attr("class", "x brush")
                        .call(me.d3.brush)
                    me.d3.brushgroup.selectAll("rect")
                        //.attr("width", 9999)
                        .attr("height", height);

                    me.d3.brushgroup.append("text")
                    .attr("x", 9999) //initially off the side
                    .attr("y", 0 + (margin.top / 2) +  170)
                    .attr("class", "brush-text")
                    .text("High values");

                    //Setup our initial brush position
                    if (me._cutoffOverride !== null) {
                        me.d3.brushgroup
                            .call(brush.extent([me._cutoffOverride, 9999]))
                            .call(brush.event);
                        me._cutoffOverride = null;
                    }
                }

                me.d3svg.append("text")
                    .attr("x", margin.left + (width / 2))
                    .attr("y", 0 + (margin.top / 2) +  8)
                    .attr("class", "title")
                    .attr("text-anchor", "middle")
                    .style("font-size", "16px")
                    .style("text-decoration", "underline")
                    .text(title);
            }
        });


    }
});