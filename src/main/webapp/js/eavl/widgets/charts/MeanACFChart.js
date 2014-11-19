/**
 * Panel extension for graphing a Mean ACF Chart
 */
Ext.define('eavl.widgets.charts.MeanACFChart', {
    extend: 'eavl.widgets.charts.BaseParameterDetailsChart',

    alias: 'widget.meanacfchart',


    /**
     * Adds the following config
     * {
     *  parameterDetails - eavl.models.ParameterDetails - Details to plot initially (can be null)
     * }
     *
     */
    constructor : function(config) {
        this.callParent(arguments);
    },

    plotParameterDetails : function(parameterDetails) {
        var me = this;
        if (parameterDetails == null) {
            me.clearPlot();
            return;
        }
        this.parameterDetails = parameterDetails;

        this.mask(Ext.util.Format.format("Loading data for '{0}'", parameterDetails.get('name')));

        d3.json(Ext.String.urlAppend("wps/getMeanACFData.do", Ext.Object.toQueryString({columnIndex : parameterDetails.get('columnIndex')})),
                function(error, data) {
            me.maskClear();
            if (error) {
                me.clearPlot("There was an error accessing ACF data");
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

            var x = me.d3.x ? me.d3.x : me.d3.x = d3.scale.ordinal().rangeRoundBands([0, width], .1);
            var y = me.d3.y ? me.d3.y : me.d3.y = d3.scale.linear().range([height, 0]);

            var xAxis = me.d3.xAxis ? me.d3.xAxis : me.d3.xAxis = d3.svg.axis().scale(x).orient("bottom").tickFormat("");
            var yAxis = me.d3.yAxis ? me.d3.yAxis : me.d3.yAxis = d3.svg.axis().scale(y).orient("left");

            x.domain(d3.range(data.data.acf.length));
            y.domain([0, 1]);

            //Either update existing line or create new one
            var title = Ext.util.Format.format('Mean ACF for "{0}"', parameterDetails.get('name'));
            if (update) {
                var bar = me.d3svg.selectAll("rect")
                    .data(data.data.acf);

                bar.transition()
                    .duration(750)
                    .attr("y", function(d) {return y(d);})
                    .attr("height", function(d) { return height - y(d); })

                me.d3svg.select('.title').text(title)
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
                    .text("TODO - What is this scale??");

                g.selectAll(".bar")
                    .data(data.data.acf)
                    .enter().append("rect")
                    .attr("class", "bar")
                    .attr("x", function(d,i) {return x(i);})
                    .attr("width", x.rangeBand())
                    .attr("y", function(d) {return y(d);})
                    .attr("height", function(d) { return height - y(d); });

                g.append("path")
                    .datum([{x:0, y: data.data.ci},{x:d3.max(x.domain()), y: data.data.ci}])
                    .attr("class", "acf-ci-line")
                    .attr("stroke-dasharray", "5, 5")
                    .attr("d", d3.svg.line()
                            .x(function(d) { return x(d.x); })
                            .y(function(d) { return y(d.y); }));

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