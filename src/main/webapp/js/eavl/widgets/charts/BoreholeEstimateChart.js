/**
 * Panel for charting a set of estimate values grouped by
 * borehole ID
 */
Ext.define('eavl.widgets.charts.BoreholeEstimateChart', {
    extend: 'portal.charts.BaseD3Chart',

    alias: 'widget.bhestimatechart',

    data : null,
    viewport : null,
    rowHeight: 30,
    rowMargin: 2,

    /**
     * Adds the following config
     * {
     *  data - see plot function
     * }
     *
     */
    constructor : function(config) {
        if (config.data) {
            config.initialPlotData = config.data;
        }

        config.svgClass = 'scroll-svg';

        this.callParent(arguments);
    },

    //Custom render that removes aspect ratio/target width/height and installs
    //a viewport
    _afterRender : function() {
        //Load our D3 Instance
        this.viewport = d3.select("#" + this.innerId).attr('class', 'viewport');
        this.d3svg = this.viewport.append("svg");

        if (this.svgClass) {
            this.d3svg.attr("class", this.svgClass)
        }

        //Force a resize
        this._onResize(this.viewport.node().offsetWidth, this.viewport.node().offsetHeight);

        //Load our data
        if (this.initialPlotData) {
            this.plot(this.initialPlotData);
        }
    },

    _onResize : function(me, width, height) {
        var requireReplot = false;
        if (width !== this.viewport.attr("width") ||
            height !== this.viewport.attr("height")) {
            requireReplot = true;
        }

        this.viewport
            .attr("width", width)
            .attr("height", height);

        this.d3svg
            .attr("width", width)
            .attr("height", this.data == null ? 0 : this.data.length * this.rowHeight);

        if (requireReplot) {
            this.plot(this.data);
        }
    },

    clearPlot : function(message) {
        this.data = null;
        this._onResize(this, this.viewport.attr('width'), this.viewport.attr('height'))
        this.callParent(arguments);
    },

    plot : function(data) {
        this.data = data;
        if (!data) {
            this.clearPlot();
            return;
        }

        this.d3svg.attr("height", data.length * this.rowHeight);
        var width = this.viewport.attr('width');
        var estimateToColor = function(e) {
            if (e < 0.1) {
                return '#eeeeee';
            } else if (e < 0.15) {
                return '#cccccc';
            } else if (e < 0.19) {
                return '#aaaaaa';
            } else if (e < 0.25) {
                return '#777777';
            } else {
                return '#000000';
            }
        };

        for (var i = 0; i < data.length; i++) {
            var bh = data[i];
            var bhGroup = this.d3svg.append("g")
                .attr("transform", "translate(0, " + i * this.rowHeight + ")");

            var rectWidth = width / bh.values.length;
            for (var j = 0; j < bh.values.length; j++) {
                bhGroup.append("rect")
                     .attr('x', this.rowMargin + j * rectWidth)
                     .attr('y', this.rowMargin)
                     .attr('width', rectWidth)
                     .attr('height', this.rowHeight - this.rowMargin * 2)
                     .attr('fill', estimateToColor(bh.values[j]));
            }

            bhGroup.append("text")
                .attr("transform", "translate(10," + this.rowHeight / 2 + ")")
                .text(bh.group);
        }

        /*var colorScale = d3.scale.category20();

        var rowEnter = function(rowSelection) {
            console.log("Enter: ", arguments);

            for (var i = 0; i < data.length)

            rowSelection.append("rect")
                .attr("rx", 3)
                .attr("ry", 3)
                .attr("width", "250")
                .attr("height", "24")
                .attr("fill-opacity", 0.25)
                .attr("stroke", "#999999")
                .attr("stroke-width", "2px");
            rowSelection.append("text")
                .attr("transform", "translate(10,15)");
        };
        var rowUpdate = function(rowSelection) {
            console.log("Update: ", arguments);
            rowSelection.select("rect")
                .attr("fill", function(d) {
                    return colorScale(d.values[0][0]);
                });
            rowSelection.select("text")
                .text(function (d) {
                    return d.group;
                });
        };

        var rowExit = function(rowSelection) {
        };


        var virtualScroller = d3.VirtualScroller()
            .rowHeight(30)
            .enter(rowEnter)
            .update(rowUpdate)
            .exit(rowExit)
            .svg(this.d3svg)
            .totalRows(data.length)
            .viewport(viewport);

        virtualScroller.data(data, function(d) {return d.group;});


        var chartGroup = this.d3svg.append("g")
            .attr("class", "chartGroup")
            //.attr("filter", "url(#dropShadow1)"); // sometimes causes issues in chrome

        chartGroup.append("rect")
            .attr("fill", "#FFFFFF");

        chartGroup.call(virtualScroller);*/

        /*for (var bhName in data) {
            var estimates = data[bhName];
            console.log(bhName);
            var bhGroup = parent.append("g")
                .attr("class", "bhe-inner")
                .attr("transform", "translate(" + innerMargin.left + "," + innerMargin.top + index * innerHeight + ")");

            bhGroup.append("text")
                .attr('x', 0)
                .attr('y', 0)
                .attr("width", textWidth)
                .style("text-anchor", "left")
                .text(bhName);

            bhGroup.append("rect")
                .attr('x', textWidth + 10)
                .attr('y', (innerHeight - barHeight) / 2 )
                .attr('height', barHeight)
                .attr('width', barWidth)

            index++;
        }*/


    }
});