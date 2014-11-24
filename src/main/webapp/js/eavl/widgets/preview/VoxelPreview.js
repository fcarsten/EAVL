/**
 * Previewer for rendering 3D voxels
 */
Ext.define('eavl.widgets.preview.VoxelPreview', {
    extend: 'Ext.container.Container',

    innerId: null,
    threeJs: null,
    fragmentShader: '\
varying vec3 vCustomColor;\n \
void main() {\n \
    gl_FragColor = vec4( vCustomColor, 1.0 );\n \
}\n',

    vertexShader: '\
#define USE_SIZEATTENUATION\n \
attribute vec3 customColor;\n \
varying vec3 vCustomColor;\n \
void main() {\n \
    vCustomColor = customColor;\n \
    vec4 mvPosition = modelViewMatrix * vec4( position, 1.0 );\n \
    gl_PointSize = 16.0;\n \
    gl_Position = projectionMatrix * mvPosition;\n \
}\n',

    constructor : function(config) {
        this.innerId = Ext.id();
        Ext.apply(config, {
            html : Ext.util.Format.format('<div id="{0}" style="width:100%;height:100%;"></div>', this.innerId)
        });

        this.callParent(arguments);
    },

    initThreeJs: function(job, fileName) {
        this.threeJs = {
            camera: null,
            controls : null,
            scene: null,
            renderer: null
        };

        var el = this.getEl();
        this.threeJs.camera = new THREE.PerspectiveCamera( 60, el.getWidth() / el.getHeight(), 1, 1000 );
        this.threeJs.camera.position.z = 500;

        this.threeJs.controls = new THREE.OrbitControls( this.threeJs.camera );
        this.threeJs.controls.damping = 0.2;
        this.threeJs.controls.addEventListener( 'change', Ext.bind(this.render, this));

        this.threeJs.scene = new THREE.Scene();
        this.threeJs.scene.fog = new THREE.FogExp2( 0xcccccc, 0.002 );

        // world
        this._getGeometry(job, fileName, true);

        // renderer
        this.threeJs.renderer = new THREE.WebGLRenderer( { antialias: false } );
        this.threeJs.renderer.setClearColor( this.threeJs.scene.fog.color, 1 );
        this.threeJs.renderer.setSize( el.getWidth(), el.getHeight());

        var container = document.getElementById( this.innerId );
        container.appendChild( this.threeJs.renderer.domElement );

        this.on('resize', this._resizeHandler, this);

        //Kickoff animation loop
        var me = this;
        var animate = function() {
            requestAnimationFrame(animate);
            me.threeJs.controls.update();
        };
        animate();
    },

    render: function() {
        this.threeJs.renderer.render( this.threeJs.scene, this.threeJs.camera );
    },

    _resizeHandler : function() {
        if (!this.threeJs) {
            return;
        }

        var el = this.getEl();
        this.threeJs.camera.aspect = el.getWidth() / el.getHeight();
        this.threeJs.camera.updateProjectionMatrix();

        this.threeJs.renderer.setSize( el.getWidth(), el.getHeight() );

        this.render();
    },

    /**
     * Creates a Object3D containing labelled axes
     *
     * Original Source - http://soledadpenades.com/articles/three-js-tutorials/drawing-the-coordinate-axes/
     */
    _buildAxes : function() {
        var buildAxis = function( src, dst, colorHex ) {
            var geom = new THREE.Geometry(),
                mat;

            mat = new THREE.LineBasicMaterial({ linewidth: 3, color: colorHex });

            geom.vertices.push( src.clone() );
            geom.vertices.push( dst.clone() );

            var axis = new THREE.Line( geom, mat, THREE.LinePieces );

            return axis;
        };

        var axes = new THREE.Object3D();

        var length = 1000;
        axes.add( buildAxis( new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( length, 0, 0 ), 0xFF0000 ) ); // +X
        axes.add( buildAxis( new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( -length, 0, 0 ), 0xFF0000) ); // -X
        axes.add( buildAxis( new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 0, length, 0 ), 0x00FF00 ) ); // +Y
        axes.add( buildAxis( new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 0, -length, 0 ), 0x00FF00 ) ); // -Y
        axes.add( buildAxis( new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 0, 0, length ), 0x0000FF ) ); // +Z
        axes.add( buildAxis( new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 0, 0, -length ), 0x0000FF ) ); // -Z
        return axes;
    },

    _getGeometry : function(job, fileName, suppressRender) {
        //Clear scene first
        for (var i = this.threeJs.scene.children.length - 1; i >= 0; i--) {
            this.threeJs.scene.remove(this.threeJs.scene.children[i]);
        }

        Ext.Ajax.request({
            url: 'results/getKDEGeometry.do',
            params: {
                jobId : job.get('id'),
                name: fileName
            },
            scope: this,
            callback: function(options, success, response) {

                console.log(this.vertexShader);
                console.log(this.fragmentShader);

                if (!success) {
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                if (!responseObj || !responseObj.success) {
                    return;
                }

                //Build our geometry (all the points received)
                var geometry = new THREE.Geometry();
                var maxEstimate = Number.MIN_VALUE;
                var minEstimate = Number.MAX_VALUE;
                var target = new THREE.Vector3();
                for (var i = 0; i < responseObj.data.length; i++) {
                    var vertex = new THREE.Vector3();

                    vertex.x = responseObj.data[i].x;
                    vertex.y = responseObj.data[i].y;
                    vertex.z = responseObj.data[i].z;

                    target.x += vertex.x;
                    target.y += vertex.y;
                    target.z += vertex.z;

                    var e = responseObj.data[i].estimate;
                    if (e > maxEstimate) {
                        maxEstimate = e;
                    }
                    if (e < minEstimate) {
                        minEstimate = e;
                    }

                    geometry.vertices.push(vertex);
                }
                target.x /= responseObj.data.length;
                target.y /= responseObj.data.length;
                target.z /= responseObj.data.length;
                this.threeJs.controls.target = target;

                //Build a custom material that can shade each point based on an RGB
                // attributes
                attributes = {
                    customColor: { type: "c", value: []}
                };

                // uniforms
                uniforms = {};

                // point cloud material using our custom shaders
                var shaderMaterial = new THREE.ShaderMaterial( {
                    uniforms:       uniforms,
                    attributes:     attributes,
                    vertexShader:   this.vertexShader,
                    fragmentShader: this.fragmentShader,
                    transparent:    true
                });

                //Setup colors for each vertex based on estimate
                for( var i = 0; i < geometry.vertices.length; i ++ ) {
                    var e = responseObj.data[i].estimate;
                    var ratio = (e - minEstimate) / (maxEstimate - minEstimate);
                    var rainbow = 'hsl(' +  + ',100%,50%)'; //Create a rainbow from blue (low) to red (high)

                    var color = new THREE.Color();
                    color.setHSL(((1 - ratio) * 240 / 255), 1.0, 0.5);

                    attributes.customColor.value[ i ] = color;
                }

                var pointCloud = new THREE.PointCloud(geometry, shaderMaterial);
                this.threeJs.scene.add(pointCloud);

                var light = new THREE.AmbientLight( 0x222222 );
                this.threeJs.scene.add( light );

                //Create axes
                this.threeJs.scene.add(this._buildAxes());

                if (!suppressRender) {
                    this.render();
                }
            }
        });
    },

    /**
     * function(job, fileName)
     * job - EAVLJob - Job to preview
     * fileName - String - name of the job file to preview
     *
     * Setup this preview page to preview the specified file for the specified job
     *
     * returns nothing
     */
    preview : function(job, fileName) {

        if (!this.threeJs) {
            this.initThreeJs(job, fileName);
            this.render();
        } else {
            this._getGeometry(job, fileName);
        }
    }
});