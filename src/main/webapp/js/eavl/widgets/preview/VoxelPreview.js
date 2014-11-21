/**
 * Previewer for rendering 3D voxels
 */
Ext.define('eavl.widgets.preview.VoxelPreview', {
    extend: 'Ext.container.Container',

    constructor : function(config) {
        this.innerId = Ext.id();
        Ext.apply(config, {
            html : Ext.util.Format.format('<div id="{0}" style="width:100%;height:100%;"></div>', this.innerId)
        });

        this.callParent(arguments);
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
        var container;

        var camera, controls, scene, renderer;
        var me = this;

        var animate = function() {

            requestAnimationFrame(animate);
            controls.update();

        };

        var init = function() {

            camera = new THREE.PerspectiveCamera( 60, window.innerWidth / window.innerHeight, 1, 1000 );
            camera.position.z = 500;

            controls = new THREE.OrbitControls( camera );
            controls.damping = 0.2;
            controls.addEventListener( 'change', render );

            scene = new THREE.Scene();
            scene.fog = new THREE.FogExp2( 0xcccccc, 0.002 );

            // world

            var geometry = new THREE.CylinderGeometry( 0, 10, 30, 4, 1 );
            var material =  new THREE.MeshLambertMaterial( { color:0xffffff, shading: THREE.FlatShading } );

            for ( var i = 0; i < 500; i ++ ) {

                var mesh = new THREE.Mesh( geometry, material );
                mesh.position.x = ( Math.random() - 0.5 ) * 1000;
                mesh.position.y = ( Math.random() - 0.5 ) * 1000;
                mesh.position.z = ( Math.random() - 0.5 ) * 1000;
                mesh.updateMatrix();
                mesh.matrixAutoUpdate = false;
                scene.add( mesh );

            }


            // lights

            light = new THREE.DirectionalLight( 0xffffff );
            light.position.set( 1, 1, 1 );
            scene.add( light );

            light = new THREE.DirectionalLight( 0x002288 );
            light.position.set( -1, -1, -1 );
            scene.add( light );

            light = new THREE.AmbientLight( 0x222222 );
            scene.add( light );


            // renderer

            renderer = new THREE.WebGLRenderer( { antialias: false } );
            renderer.setClearColor( scene.fog.color, 1 );
            renderer.setSize( window.innerWidth, window.innerHeight );

            container = document.getElementById( me.innerId );
            container.appendChild( renderer.domElement );

            //

            window.addEventListener( 'resize', onWindowResize, false );

            animate();

        };

        var onWindowResize = function() {

            camera.aspect = window.innerWidth / window.innerHeight;
            camera.updateProjectionMatrix();

            renderer.setSize( window.innerWidth, window.innerHeight );

            render();

        };

        var render = function() {

            renderer.render( scene, camera );

        };

        init();
        render();
    }
});