/**
 * Panel extension for graphing a Probability Density Function
 */
Ext.define('eavl.widgets.ProbabilityDensityFunctionChart', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.pdfchart',

    parameterDetails : null,
    d3svg : null, //D3 SVG element. Will always be set after render
    d3 : null, //D3 elements (graphs, lines etc). Can be null
    targetWidth: null,
    targetHeight : null,
    allowCutoffSelection : false,


    /**
     * Adds the following config
     * {
     *  parameterDetails - eavl.models.ParameterDetails - Details to plot initially (can be null)
     *  preserveAspectRatio - boolean - Should the graph preserve a 4x2 aspect ratio or should it stretch. Default false
     *  targetWidth - Number - (Only useful if preserveAspectRatio is set) - The target width to use in aspect ratio
     *  targetHeight - Number - (Only useful if preserveAspectRatio is set)  - The target height to use in aspect ratio
     *  allowCutoffSelection - Boolean (- If true, the chart will have a draggable cutoff slider added. Defaults to false
     * }
     *
     * Adds the following events
     * {
     *  cutoffchanged : function(this, cutoffXValue)
     * }
     */
    constructor : function(config) {

        this.parameterDetails = config.parameterDetails ? config.parameterDetails : null;
        this.d3svg = null;
        this.d3 = null;
        this.preserveAspectRatio = config.preserveAspectRatio ? true : false;
        this.targetWidth = config.targetWidth ? config.targetWidth : 800;
        this.targetHeight = config.targetHeight ? config.targetHeight : 400;
        this.allowCutoffSelection = config.allowCutoffSelection ? true : false;

        this.innerId = Ext.id();

        Ext.apply(config, {
            html : Ext.util.Format.format('<div id="{0}" style="width:100%;height:100%;"></div>', this.innerId)
        });

        this.callParent(arguments);

        this.addEvents(['cutoffchanged']);

        this.on('render', this._afterRender, this);
        this.on('resize', this._onResize, this);
    },

    /**
     * Removes any chart elements. Optionally displays a message
     */
    clearPlot : function(message) {
        this.parameterDetails = null;
        this.d3svg.select('*').remove();
        this.d3svg.select('.title').remove();
        this.d3 = null;

        if (message) {
            this.d3svg.append("text")
            .attr('x', this.targetWidth/2)
            .attr('y', (this.targetHeight / 2) - 20)
            .attr("width", this.targetWidth)
            .style("text-anchor", "middle")
            .style("font-size", "32px")
            .text(message)
        }

    },

    /**
     * Gets the selected cutoff value (if enabled) otherwise returns null.
     */
    getCutoffValue : function() {
        if (this.d3.brush.empty()) {
            return null;
        }

        return this.d3.brush.extent()[0];
    },

    _handleBrush : function(fireEvent) {
        if (this.d3.brush.empty()) {
            this.fireEvent('cutoffchanged', this, null);
            return;
        }

        var value = this.d3.brush.extent()[0];
        if (d3.event.sourceEvent) { // not a programmatic event
            this.d3.brush.extent([value, this.d3.x.domain()[1]]);
        }

        this.d3.brushgroup.selectAll(".extent").attr("width", 9999);
        this.d3.brushgroup.selectAll('.resize.w rect').attr("width", 9999);

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

        d3.json(Ext.String.urlAppend("wps/getPDFData.do", Ext.Object.toQueryString({columnIndex : parameterDetails.get('columnIndex')})),
                function(error, data) {

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

            var valueline = me.d3.valueLine ? me.d3.valueLine : me.d3.valueLine = d3.svg.line()
                    .x(function(d) { return x(d[0]); })
                    .y(function(d) { return y(d[1]); });

            x.domain(d3.extent(data, function(d) { return d[0]; }));
            y.domain(d3.extent(data, function(d) { return d[1]; }));

            //Either update existing line or create new one
            var title = Ext.util.Format.format('Probability Density Function for "{0}"', parameterDetails.get('name'));
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
                    .text("log(density)");

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


    },

    _afterRender : function() {
        //Load our D3 Instance
        var container = d3.select("#" + this.innerId);
        this.d3svg = container.append("svg")
            .attr("preserveAspectRatio", this.preserveAspectRatio ? "xMidYMid" : "none")
            .attr("class", "pdf-chart-svg")
            .attr("viewBox",  Ext.util.Format.format("0 0 {0} {1}", this.targetWidth, this.targetHeight));

        //Force a resize
        this._onResize(container.node().offsetWidth, container.node().offsetHeight);

        //Load our data
        if (this.parameterDetails) {
            this._loadData(this.parameterDetails);
        }
    },

    _onResize : function(me, width, height) {
        this.d3svg
            .attr("width", width)
            .attr("height", height);
    }


});