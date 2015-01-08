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

            //We will be rendering a LOT of rectangles. Using the d3.append for each element is
            //a HUGE performance hit. There is no appendAll or bulkAppend so we're forced to step 
            //outside D3js and do the appending manually using document fragments.
            var rectWidth = barWidth / bh.values.length;
            var frag = document.createDocumentFragment(); //This is a virtual element so we can append everything in one hit
            for (var j = 0; j < bh.values.length; j++) {
                var rectEl = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
                rectEl.setAttributeNS(null, 'x', this.textWidth + this.rowMargin + j * rectWidth);
                rectEl.setAttributeNS(null, 'y', this.rowMargin);
                rectEl.setAttributeNS(null, 'width', rectWidth)
                rectEl.setAttributeNS(null, 'estimate', bh.values[j]);
                rectEl.setAttributeNS(null, 'height', this.rowHeight - this.rowMargin * 2);
                rectEl.setAttributeNS(null, 'fill', estimateToColor(bh.values[j]));                
                frag.appendChild(rectEl);
            }
            
            var textEl = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            textEl.setAttributeNS(null, 'transform', "translate(10," + (this.rowHeight / 2 + 8) + ")");
            textEl.appendChild(document.createTextNode(bh.group));
            frag.appendChild(textEl);
            
            bhGroup[0][0].appendChild(frag);
            
            //The downside to using document fragments is that we dont inherit the D3 JS event wrappers
            //which force us to "step back into" D3 for event handling
            bhGroup.selectAll("rect")
                .on('mouseover', tip.show)
                .on('mouseout', tip.hide);
        }
    }
});