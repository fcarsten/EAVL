/**
 * Panel for charting a set of estimate values grouped by
 * borehole ID
 */
Ext.define('eavl.widgets.charts.BoreholeEstimateChart', {
    extend: 'portal.charts.BaseD3Chart',

    alias: 'widget.bhestimatechart',

    data : null,
    viewport : null,
    rowHeight: 50,
    rowMargin: 3,
    textWidth: 100,

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
        var barWidth = width - this.textWidth;

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

        var tip = d3.tip()
            .attr('class', 'd3-tip')
            .offset([-10, -20])
            .direction('w')
            .html(function(d) {
              return "<strong>Estimate:</strong> <span>" + this.getAttribute('estimate') + "</span>";
            });

        this.d3svg.call(tip);

        for (var i = 0; i < data.length; i++) {
            var bh = data[i];
            var bhGroup = this.d3svg.append("g")
                .attr('class', 'bhe-group')
                .attr("transform", "translate(0, " + i * this.rowHeight + ")");

            var rectWidth = barWidth / bh.values.length;
            for (var j = 0; j < bh.values.length; j++) {
                bhGroup.append("rect")
                     .attr('x', this.textWidth + this.rowMargin + j * rectWidth)
                     .attr('y', this.rowMargin)
                     .attr('width', rectWidth)
                     .attr('estimate', bh.values[j])
                     .attr('height', this.rowHeight - this.rowMargin * 2)
                     .on('mouseover', tip.show)
                     .on('mouseout', tip.hide)
                     .attr('fill', estimateToColor(bh.values[j]));

            }

            bhGroup.append("text")
                .attr("transform", "translate(10," + (this.rowHeight / 2 + 8) + ")")
                .text(bh.group);
        }
    }
});