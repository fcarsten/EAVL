/**
 * A plugin for an Ext.container.Container class that will link
 * the container to receiving drag events from a seperate Ext.grid.Panel
 * column header (not the actual rows).
 *
 * To use this plugin, assign the following field to the plugin constructor
 * {
 *   grid : The Ext.grid.Panel to link to OR a function that when passed the container instance will return an instance of grid
 *   dropFn : function(container, columnId, columnName) - called whenever a drop is received from grid
 *   removeOnDrop : Boolean - If true, delete the column from the source grid on a successful drop (Default false)
 *   allowDrag : Boolean - If true, this container can drag elements BACK to the grid. (Default false) - NOTE - only works with containers that have a view.
 * }
 */
Ext.define('eavl.widgets.plugins.HeaderDragLink', {
    alias: 'plugin.headerdraglink',

    grid : null,
    container : null,
    dropFn : null,
    dropTarget : null,
    removeOnDrop : false,
    allowDrag : false,
    dragZone : null,


    constructor : function(cfg) {
        this.grid = cfg.grid;
        this.dropFn = cfg.dropFn;
        this.allowDrag = cfg.allowDrag;
        this.removeOnDrop = cfg.removeOnDrop ? true : false;
        this.callParent(arguments);
    },

    init: function(container) {
        this.container = container;

        this.container.on('boxready', this.registerDDZones, this);
        this.container.on('beforedestroy', this.beforeDestroy, this);
    },

    registerDDZones : function(container) {
        if (Ext.isFunction(this.grid)) {
            this.grid = this.grid(container);
        }

        //OK - we hack into the grid's header's reordering plugin to pull out
        //the drag/drop group ID. We create a new drop zone using that ID
        //and apply it to the inspect panel.
        var header = this.grid.getView().headerCt;
        var reorderPlugin = header.plugins[1]; //header.getPlugin doesn't work during afterrender
        var ddGroup = reorderPlugin.dropZone.ddGroup;

        //This handles drop events
        var body = container.body;
        var me = this;
        this.dropTarget = new Ext.dd.DropTarget(body, {
            ddGroup : ddGroup,
            notifyEnter: function(ddSource, e, data) {
                body.stopAnimation();
                body.highlight();
            },
            notifyDrop: function(ddSource, e, data) {
                if (me.removeOnDrop) {
                    me.grid.getView().headerCt.remove(data.header);
                    me.grid.getView().refresh();
                }
                console.log(data);
                me.dropFn(container, data.header.itemId, data.header.name);
            }
        });

        //This is to enable dragging back
        if (this.allowDrag) {
            var v = this.container.getView();
            this.dragZone = new Ext.dd.DragZone(v.getEl(), {
                ddGroup: ddGroup,

                getDragData: function(e) {

                    var sourceEl = e.getTarget(v.itemSelector, 10);
                    if (sourceEl) {
                        d = sourceEl.cloneNode(true);
                        d.id = Ext.id();

                        var record = v.getRecord(sourceEl);
                        var header = Ext.create('Ext.grid.column.Column', {itemId: record.get('name'), dataIndex: record.get('name'), sortable: false});
                        console.log(record);
                        return {
                            ddel: d,
                            header : header,
                            sourceEl: sourceEl,
                            repairXY: Ext.fly(sourceEl).getXY(),
                            sourceStore: v.store,
                            draggedRecord: record
                        }
                    }
                },

//              Provide coordinates for the proxy to slide back to on failed drag.
//              This is the original XY coordinates of the draggable element captured
//              in the getDragData method.
                getRepairXY: function() {
                    return this.dragData.repairXY;
                }
            });
        }
    },

    beforeDestroy: function() {
        if (this.dropTarget) {
            this.dropTarget.unreg();
            this.dropTarget = null;
        }
        this.callParent();
    }
});