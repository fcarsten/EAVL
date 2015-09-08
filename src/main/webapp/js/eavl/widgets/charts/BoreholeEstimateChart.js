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
    scrollMargin: 20,
    
    statics : {
        PERCENTILE_BRACKETS: [{text: '&gt; 95th Percentile', color: '#eb403b'},
                              {text: '80th - 95th Percentile', color: '#e98931'},
                              {text: '65th - 80th Percentile', color: '#fbb735'},
                              {text: '50th - 65th Percentile', color: '#39c0b3'},
                              {text: '&lt; 50th Percentile', color: '#227fb0'}],
                              
        percentileToColor : function(e) {
            if (e < 50) {
                return '#227fb0';
            } else if (e < 65) {
                return '#39c0b3';
            } else if (e < 80) {
                return '#fbb735';
            } else if (e < 95) {
                return '#e98931';
            } else {
                return '#eb403b';
            }
        },
        
        /**
         * Generates HTML markup for rendering a legend for this chart. The top level element of this markup can be stamped with
         * a particular ID if required. 
         */
        legendMarkup: function(parentId, parentCls, parentStyle) {
            var parentClasses = ['bhe-legend'];
            if (parentCls) {
                parentClasses.push(parentCls);
            }
            
            var children = [];
            var brackets = eavl.widgets.charts.BoreholeEstimateChart.PERCENTILE_BRACKETS;
            for (var i = 0; i < brackets.length; i++) {
                var bracket = brackets[i];
                children.push({
                    tag: 'div',
                    cls: 'bhe-legend-row',
                    children: [{
                        tag: 'div',
                        cls: 'bhe-legend-box',
                        style: 'background-color:' + bracket.color + ';'
                    },{
                        tag: 'div',
                        cls: 'bhe-legend-text',
                        html: bracket.text
                    }]
                });
            }
            
            return {
                tag: 'div',
                id: parentId,
                cls: parentClasses.join(' '),
                style: parentStyle,
                children: children
            };
        }
    },

    /**
     * Adds the following config
     * {
     *  data - see plot function
     *  scrollMargin: Number- How many pixels to reserve for a scroll bar on the right
     * }
     *
     */
    constructor : function(config) {
        if (config.data) {
            config.initialPlotData = config.data;
        }
        
        this.scrollMargin = Ext.isNumber(config.scrollMargin) ? config.scrollMargin : this.scrollMargin; 

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
            .attr("width", Math.max(width - this.scrollMargin, 0))
            .attr("height", this.data == null ? 0 : (this.data.length + 1) * this.rowHeight);

        if (requireReplot) {
            this.plot(this.data);
        }
    },

    clearPlot : function(message) {
        this.data = null;
        this._onResize(this, this.viewport.attr('width'), this.viewport.attr('height'))
        this.callParent(arguments);
        this.d3svg.select('g.bhe-group').remove();
    },

    plot : function(data) {
        this.clearPlot();
        this.data = data;
        if (!data) {
            return;
        }

        this.d3svg.attr("height", (data.length + 1) * this.rowHeight);
        var width = Math.max(this.viewport.attr('width') - this.scrollMargin, 0);
        var chartWidth = width - this.textWidth;

        var tip = d3.tip()
            .attr('class', 'd3-tip')
            .offset([-10, -20])
            .direction('w')
            .html(function(d) {
              return "<strong>Estimate:</strong> <span>" + this.getAttribute('estimate') + "</span>";
            });

        this.d3svg.call(tip);

        //Plot probability at each depth
        for (var i = 0; i < data.length; i++) {
            var bh = data[i];
            var bhGroup = this.d3svg.append("g")
                .attr('class', 'bhe-group')
                .attr("transform", "translate(0, " + i * this.rowHeight + ")");

            //We will be rendering a LOT of rectangles. Using the d3.append for each element is
            //a HUGE performance hit. There is no appendAll or bulkAppend so we're forced to step 
            //outside D3js and do the appending manually using document fragments.
            var rectWidth = chartWidth / bh.values.length;
            var frag = document.createDocumentFragment(); //This is a virtual element so we can append everything in one hit
            for (var j = bh.values.length - 1; j >= 0; j--) {
                var rectEl = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
                rectEl.setAttributeNS(null, 'x', this.textWidth + this.rowMargin + j * rectWidth);
                rectEl.setAttributeNS(null, 'y', this.rowMargin);
                rectEl.setAttributeNS(null, 'width', rectWidth + 1)
                rectEl.setAttributeNS(null, 'estimate', Ext.util.Format.number(bh.values[j][0], '0.0000'));
                rectEl.setAttributeNS(null, 'height', this.rowHeight - this.rowMargin * 2);
                rectEl.setAttributeNS(null, 'fill', eavl.widgets.charts.BoreholeEstimateChart.percentileToColor(bh.values[j][2])); //Percentile is used for colouring            
                rectEl.addEventListener('mouseover', tip.show);
                rectEl.addEventListener('mouseout', tip.hide);
                frag.appendChild(rectEl);
            }
            
            var textEl = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            textEl.setAttributeNS(null, 'transform', "translate(10," + (this.rowHeight / 2 + 8) + ")");
            textEl.appendChild(document.createTextNode(bh.group));
            frag.appendChild(textEl);
            
            bhGroup[0][0].appendChild(frag);
            
            //Plot predictor quantity at each location
            var lineGroup = bhGroup.append("g")
                .attr("transform", "translate(" + (this.textWidth + this.rowMargin + rectWidth / 2) + ", " + (this.rowMargin) + ")");
            var x = d3.scale.linear().range([0, chartWidth]);
            var y = d3.scale.linear().range([this.rowHeight - this.rowMargin * 2, 0]);
            
            var xyPredictorData = bh.values.map(function(d, i) { return [i, d[1]]});
            var valueline = d3.svg.line()
                    .x(function(d) { return x(d[0]); })
                    .y(function(d) { return y(d[1]); });

            x.domain(d3.extent(xyPredictorData, function(d) { return d[0]; }));
            y.domain(d3.extent(xyPredictorData, function(d) { return d[1]; }));
            
            lineGroup.append("path")
                .datum(xyPredictorData)
                .attr("class", "bhe-line")
                .attr("d", valueline(xyPredictorData));
        }
    }
});