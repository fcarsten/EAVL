/**
 * Previewer for rendering 3D Scatter Plots
 */
Ext.define('eavl.widgets.preview.3DScatterPlotPreview', {
    extend : 'Ext.container.Container',

    innerId : null,
    threeJs : null,

    constructor : function(config) {
        this.innerId = Ext.id();
        this._md = {};
        Ext.apply(config,{
            html : Ext.util.Format.format('<div id="{0}" style="width:100%;height:100%;"></div>',this.innerId)
        });

        this.callParent(arguments);

    },

    initThreeJs : function(job, fileName) {
        this.threeJs = {
            camera : null,
            controls : null,
            scene : null,
            renderer : null,
            pointSize : 10
        };

        var el = this.getEl();
        this.threeJs.camera = new THREE.PerspectiveCamera(60, el.getWidth() / el.getHeight(), 1, 10000);
        this.threeJs.camera.position.z = 180;
        this.threeJs.camera.position.y = 18;

        el.on('mousedown', this._handleMouseDown, this);
        el.on('mouseup', this._handleMouseUp, this);

        this.threeJs.controls = new THREE.OrbitControls( this.threeJs.camera);
        this.threeJs.controls.damping = 0.2;
        this.threeJs.controls.target = new THREE.Vector3(0, 0, 0);
        this.threeJs.controls.addEventListener('change', Ext.bind(this.render, this));

        this.threeJs.scene = new THREE.Scene();

        // world
        this._getGeometry(job, fileName, true);

        // renderer
        this.threeJs.renderer = new THREE.WebGLRenderer({antialias : false});
        this.threeJs.renderer.setClearColor(0xffffff, 1);
        this.threeJs.renderer.setSize(el.getWidth(), el.getHeight());

        this.threeJs.raycaster = new THREE.Raycaster();
        this.threeJs.raycaster.params.PointCloud.threshold = this.threeJs.pointSize;

        var container = document.getElementById(this.innerId);
        container.appendChild(this.threeJs.renderer.domElement);

        this.on('resize', this._resizeHandler, this);

        // Kickoff animation loop
        var me = this;
        var animate = function() {
            requestAnimationFrame(animate);
            me.threeJs.controls.update();
        };
        animate();
        this.render();
    },

    render : function() {
        console.log('render from: ', callee);
        this.threeJs.renderer.render(this.threeJs.scene, this.threeJs.camera);
    },

    _resizeHandler : function() {
        if (!this.threeJs) {
            return;
        }

        var el = this.getEl();
        this.threeJs.camera.aspect = el.getWidth() / el.getHeight();
        this.threeJs.camera.updateProjectionMatrix();

        this.threeJs.renderer.setSize(el.getWidth(), el.getHeight());

        this.render();
    },

    /**
     * Originally sourced but then adapted from
     * http://bl.ocks.org/phil-pedruco/9852362
     */
    _renderEstimatePoints : function(suppressRender, data) {
        function v(x, y, z) {
            return new THREE.Vector3(x, y, z);
        }

        function createTextCanvas(text, color, font, size) {
            size = size || 16;
            var canvas = document.createElement('canvas');
            var ctx = canvas.getContext('2d');
            var fontStr = (size + 'px ') + (font || 'Arial');
            ctx.font = fontStr;
            var w = ctx.measureText(text).width;
            var h = Math.ceil(size);
            canvas.width = w;
            canvas.height = h;
            ctx.font = fontStr;
            ctx.fillStyle = color || 'black';
            ctx.fillText(text, 0, Math.ceil(size * 0.8));
            return canvas;
        }

        function createText2D(text, color, font, size, segW,
                segH) {
            var canvas = createTextCanvas(text, color, font, size);
            var plane = new THREE.PlaneGeometry(canvas.width, canvas.height, segW, segH);
            var tex = new THREE.Texture(canvas);
            tex.needsUpdate = true;
            var planeMat = new THREE.MeshBasicMaterial({
                map : tex,
                color : 0xffffff,
                transparent : true
            });

            // This is how we view the reversed text from behind
            // see:
            // http://stackoverflow.com/questions/20406729/three-js-double-sided-plane-one-side-reversed
            var backPlane = plane.clone();
            plane.merge(backPlane, new THREE.Matrix4().makeRotationY(Math.PI), 1);

            var mesh = new THREE.Mesh(plane, planeMat);
            mesh.scale.set(0.5, 0.5, 0.5);
            mesh.material.side = THREE.FrontSide;
            return mesh;
        }

        var xExtent = d3.extent(data, function(d) {
            return d.x;
        }), yExtent = d3.extent(data, function(d) {
            return d.y;
        }), zExtent = d3.extent(data, function(d) {
            return d.z;
        }), eExtent = d3.extent(data, function(d) {
            return d.estimate;
        });

        var format = d3.format("+.3f");

        var vpts = {
            xMax : xExtent[1],
            xCen : (xExtent[1] + xExtent[0]) / 2,
            xMin : xExtent[0],
            yMax : yExtent[1],
            yCen : (yExtent[1] + yExtent[0]) / 2,
            yMin : yExtent[0],
            zMax : zExtent[1],
            zCen : (zExtent[1] + zExtent[0]) / 2,
            zMin : zExtent[0]
        };

        var xScale = d3.scale.linear()
            .domain(xExtent)
            .range([ -50, 50 ]);
        var yScale = d3.scale.linear()
            .domain(yExtent)
            .range([ -50, 50 ]);
        var zScale = d3.scale.linear()
            .domain(zExtent)
            .range([ -50, 50 ]);
        var eScale = d3.scale.log()
            .domain(eExtent)
            .range([ 0, 240 / 255 ]);

        // Build our axes
        var lineGeo = new THREE.Geometry();
        lineGeo.vertices.push(
            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMin)), v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zMin)),
            v(xScale(vpts.xMax), yScale(vpts.yMax), zScale(vpts.zMin)), v(xScale(vpts.xMin), yScale(vpts.yMax), zScale(vpts.zMin)),
            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMin)),

            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zCen)), v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zCen)),
            v(xScale(vpts.xMax), yScale(vpts.yMax), zScale(vpts.zCen)), v(xScale(vpts.xMin), yScale(vpts.yMax), zScale(vpts.zCen)),
            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zCen)),

            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMax)), v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zMax)),
            v(xScale(vpts.xMax), yScale(vpts.yMax), zScale(vpts.zMax)), v(xScale(vpts.xMin), yScale(vpts.yMax), zScale(vpts.zMax)),
            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMax)),

            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMin)), v(xScale(vpts.xMin), yScale(vpts.yMax), zScale(vpts.zMin)),
            v(xScale(vpts.xMin), yScale(vpts.yMax), zScale(vpts.zMax)), v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMax)),
            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMax)), v(xScale(vpts.xCen), yScale(vpts.yMin), zScale(vpts.zMax)),

            v(xScale(vpts.xCen), yScale(vpts.yMin), zScale(vpts.zMin)), v(xScale(vpts.xCen), yScale(vpts.yMax), zScale(vpts.zMin)),
            v(xScale(vpts.xCen), yScale(vpts.yMax), zScale(vpts.zMax)), v(xScale(vpts.xCen), yScale(vpts.yMin), zScale(vpts.zMax)),
            v(xScale(vpts.xCen), yScale(vpts.yMin), zScale(vpts.zMax)), v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zMax)),

            v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zMin)), v(xScale(vpts.xMax), yScale(vpts.yMax), zScale(vpts.zMin)),
            v(xScale(vpts.xMax), yScale(vpts.yMax), zScale(vpts.zMax)), v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zMax)),
            v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zMax)),

            v(xScale(vpts.xMax), yScale(vpts.yCen), zScale(vpts.zMax)), v(xScale(vpts.xMax), yScale(vpts.yCen), zScale(vpts.zMin)),
            v(xScale(vpts.xMin), yScale(vpts.yCen), zScale(vpts.zMin)), v(xScale(vpts.xMin), yScale(vpts.yCen), zScale(vpts.zMax)),
            v(xScale(vpts.xMax), yScale(vpts.yCen), zScale(vpts.zMax)),

            v(xScale(vpts.xCen), yScale(vpts.yCen), zScale(vpts.zMax)), v(xScale(vpts.xCen), yScale(vpts.yCen), zScale(vpts.zMin)),
            v(xScale(vpts.xCen), yScale(vpts.yMin), zScale(vpts.zMin)), v(xScale(vpts.xCen), yScale(vpts.yMin), zScale(vpts.zCen)),
            v(xScale(vpts.xCen), yScale(vpts.yMax), zScale(vpts.zCen)), v(xScale(vpts.xMax), yScale(vpts.yMax), zScale(vpts.zCen)),
            v(xScale(vpts.xMax), yScale(vpts.yCen), zScale(vpts.zCen)), v(xScale(vpts.xMin), yScale(vpts.yCen), zScale(vpts.zCen))
        );
        var lineMat = new THREE.LineBasicMaterial({
            color : 0x000000,
            lineWidth : 1
        });
        var line = new THREE.Line(lineGeo, lineMat);
        line.type = THREE.Lines;
        this.threeJs.scene.add(line);

        var titleX = createText2D('-X');
        titleX.position.x = xScale(vpts.xMin) - 12, titleX.position.y = 5;
        this.threeJs.scene.add(titleX);

        var valueX = createText2D(format(xExtent[0]));
        valueX.position.x = xScale(vpts.xMin) - 12;
        valueX.position.y = -5;
        this.threeJs.scene.add(valueX);

        var titleX = createText2D('X');
        titleX.position.x = xScale(vpts.xMax) + 12;
        titleX.position.y = 5;
        this.threeJs.scene.add(titleX);

        var valueX = createText2D(format(xExtent[1]));
        valueX.position.x = xScale(vpts.xMax) + 12;
        valueX.position.y = -5;
        this.threeJs.scene.add(valueX);

        var titleY = createText2D('-Y');
        titleY.position.y = yScale(vpts.yMin) - 5;
        this.threeJs.scene.add(titleY);

        var valueY = createText2D(format(yExtent[0]));
        valueY.position.y = yScale(vpts.yMin) - 15;
        this.threeJs.scene.add(valueY);

        var titleY = createText2D('Y');
        titleY.position.y = yScale(vpts.yMax) + 15;
        this.threeJs.scene.add(titleY);

        var valueY = createText2D(format(yExtent[1]));
        valueY.position.y = yScale(vpts.yMax) + 5;
        this.threeJs.scene.add(valueY);

        var titleZ = createText2D('-Z ' + format(zExtent[0]));
        titleZ.position.z = zScale(vpts.zMin) + 2;
        this.threeJs.scene.add(titleZ);

        var titleZ = createText2D('Z ' + format(zExtent[1]));
        titleZ.position.z = zScale(vpts.zMax) + 2;
        this.threeJs.scene.add(titleZ);

        // Build our scatter plot points
        var mat = new THREE.PointCloudMaterial({
            vertexColors : true,
            size : this.threeJs.pointSize
        });

        var pointCount = data.length;
        var pointGeo = new THREE.Geometry();
        for (var i = 0; i < pointCount; i++) {
            var x = xScale(data[i].x);
            var y = yScale(data[i].y);
            var z = zScale(data[i].z);
            var e = eScale(data[i].estimate);

            pointGeo.vertices.push(new THREE.Vector3(x, y, z));
            pointGeo.colors.push(new THREE.Color().setHSL(e, 1.0, 0.5));
        }

        this.threeJs.pointCloud = new THREE.PointCloud(pointGeo, mat);
        this.threeJs.scene.add(this.threeJs.pointCloud);

        if (!suppressRender) {
            this.render();
        }
    },

    _getGeometry : function(job, fileName, suppressRender) {
        // Clear scene first
        for (var i = this.threeJs.scene.children.length - 1; i >= 0; i--) {
            this.threeJs.scene.remove(this.threeJs.scene.children[i]);
        }

        var me = this;
        var mask = new Ext.LoadMask(me, {
            msg : "Please wait..."
        });
        mask.show();
        Ext.Ajax.request({
            url : 'results/getKDEGeometry.do',
            params : {
                jobId : job.get('id'),
                name : fileName
            },
            scope : this,
            callback : function(options, success, response) {
                mask.hide();
                mask.destroy();
                if (!success) {
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj || !responseObj.success) {
                    return;
                }

                this._renderEstimatePoints(suppressRender, responseObj.data);
            }
        });
    },

    /**
     * Utility for turning a click event on dom element target
     * into an X/Y offset relative to that element
     *
     * From:
     * http://stackoverflow.com/questions/55677/how-do-i-get-the-coordinates-of-a-mouse-click-on-a-canvas-element
     */
    _relMouseCoords : function(event, target) {
        var totalOffsetX = 0;
        var totalOffsetY = 0;
        var canvasX = 0;
        var canvasY = 0;
        var currentElement = target;

        do {
            totalOffsetX += currentElement.offsetLeft - currentElement.scrollLeft;
            totalOffsetY += currentElement.offsetTop - currentElement.scrollTop;
        } while (currentElement = currentElement.offsetParent)

        canvasX = event.pageX - totalOffsetX;
        canvasY = event.pageY - totalOffsetY;

        return {
            x : canvasX,
            y : canvasY
        };
    },

    _handleMouseDown : function(e, t) {
        this._md = this._relMouseCoords(e.browserEvent, t);
    },

    _handleMouseUp : function(e, t) {
        var xy = this._relMouseCoords(e.browserEvent, t);
        var x = xy.x;
        var y = xy.y;

        // If the mouse has moved too far, dont count this as a
        // click
        if (Math.abs(this._md.x - x) + Math.abs(this._md.y - y) > 10) {
            return;
        }

        // Otherwise cast a ray and see what we intersect
        var mouse3D = new THREE.Vector3(x, y, 0.5).unproject(this.threeJs.camera);
        var direction = mouse3D.clone()
            .sub(this.threeJs.camera.position)
            .normalize();

        this.threeJs.raycaster.ray.set(this.threeJs.camera.position, direction);
        var intersections = this.threeJs.raycaster.intersectObject(this.threeJs.pointCloud);
        console.log(intersections);

        // This is debug code for the ray caster
        var material2 = new THREE.LineBasicMaterial({
            color : 0x0000ff
        });
        var geometry2 = new THREE.Geometry();
        var vector = mouse3D.clone();

        geometry2.vertices.push(this.threeJs.camera.position);
        var end = this.threeJs.camera.position.clone().add(direction.clone().multiplyScalar(1000));
        geometry2.vertices.push(end);

        var line = new THREE.Line(geometry2, material2);
        this.threeJs.scene.add(line);
        this.render();
        //end debug code
    },

    /**
     * function(job, fileName) job - EAVLJob - Job to preview
     * fileName - String - name of the job file to preview
     *
     * Setup this preview page to preview the specified file for
     * the specified job
     *
     * returns nothing
     */
    preview : function(job, fileName) {

        if (!this.threeJs) {
            this.initThreeJs(job, fileName);
            this.render();
        } else {
            this._getGeometry(job, fileName, false);
        }
    }
});