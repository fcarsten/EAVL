/**
 * Panel extension for graphing a Doubled Probability Density Function
 */
Ext.define('eavl.widgets.charts.DoublePDFChart', {
    extend: 'eavl.widgets.charts.BaseParameterDetailsChart',

    alias: 'widget.doublepdfchart',


    /**
     * Adds the following config
     * {
     *  parameterDetails - eavl.models.ParameterDetails - Details to plot initially (can be null)
     *  file - String [Optional] - The file to pull numbers from. Defaults to eavl.models.EAVLJob.FILE_DATA_CSV
     * }
     *
     */
    constructor : function(config) {
        config.svgClass = 'pdf-chart-svg';
        this.file = config.file ? config.file : eavl.models.EAVLJob.FILE_DATA_CSV;
        this.callParent(arguments);
    },

    _generateLegendItem : function(gLegend, dashPattern, text, index) {
        var legendLinePoints = [[0, 0], [20, 0]];
        var lineLegend = this.d3.lineLegend ? this.d3.lineLegend : this.d3.lineLegend = d3.svg.line()
                .x(function(d) { return d[0]; })
                .y(function(d) { return d[1]; });
        
        var gLegendItem = gLegend.append("g")
            .attr("transform","translate(5," + (5 + index * 15) + " )");
        gLegendItem.append("path")
            .attr("class", "line")
            .attr("stroke-dasharray", dashPattern)
            .attr("transform", "translate(0, -4)")
            .attr("d", lineLegend(legendLinePoints));
        gLegendItem.append("text")
            .attr("transform", "translate(30, 0)")
            .text(text);
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
        d3.json(Ext.String.urlAppend("wps/getDoublePDFData.do", Ext.Object.toQueryString({columnIndex : parameterDetails.get('columnIndex'), file: this.file})),
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

            var lineMain = me.d3.lineMain ? me.d3.lineMain : me.d3.lineMain = d3.svg.line()
                    .x(function(d) { return x(d[0]); })
                    .y(function(d) { return y(d[1]); });

            var lineSecondary = me.d3.lineSecondary ? me.d3.lineSecondary : me.d3.lineSecondary = d3.svg.line()
                    .x(function(d) { return x(d[2]); })
                    .y(function(d) { return y(d[3]); });

            x.domain(d3.extent(data, function(d) { return d[0]; }));
            y.domain(d3.extent(data, function(d) { return d[1]; }));

            //Either update existing line or create new one
            var title = Ext.util.Format.format('Probability Density Function for log({0})', parameterDetails.get('name'));
            if (update) {
                var svg = me.d3svg.transition();

                svg.select(".line-main")
                    .duration(750)
                    .attr("d", lineMain(data));
                svg.select(".line-secondary")
                    .duration(750)
                    .attr("d", lineSecondary(data));
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
                    .attr("class", "line-main line")
                    .attr("d", lineMain(data));

                g.append("path")
                    .datum(data)
                    .attr("class", "line-secondary line")
                    .attr("stroke-dasharray", "5, 5")
                    .attr("d", lineSecondary(data));

                if (me.d3.brush) {
                    me.d3.brushgroup = g.append("g")
                        .attr("class", "x brush")
                        .call(me.d3.brush)
                    me.d3.brushgroup.selectAll("rect")
                        .attr("height", height);
                }
                
                //Generate our legend
                var legendWidth = 50;
                me.legend = me.d3svg.append("g")
                    .attr("class","legend")
                    .attr("transform","translate(" + (width - legendWidth) + ",50)")
                    .style("font-size","12px");
                me.legend.append("rect")
                    .attr("width", 105)
                    .attr("height", 40)
                    .attr("transform","translate(0, -12)")
                    
                me._generateLegendItem(me.legend, "5, 5", "High Values", 0);
                me._generateLegendItem(me.legend, "", "All Values", 1);
                

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