/**
 * A plugin for an Ext.Component class that will link
 * a number of widgets together via drag and drop sharing of Ext.data.Model objects
 *
 * Dragging from a container will remove the ParameterDetails. Dragging to a container
 * will insert it
 *
 * To use this plugin, assign the following field to the plugin constructor
 * {
 *   ddGroup : String - The name of the drag/drop group this will belong to
 *   highlightBody : Boolean - Should drop notifications highlight the body (better performance/interactivity). If false, drop notification will be done via a temporary mask. Default true
 *
 *   handleDrop: function(Ext.Component me, Ext.data.Model model, Ext.Component source) - A function that should handle the insertion of the specified Ext.data.Model
 *   handleDrag: function(Ext.Component me, Ext.data.Model model, Ext.Component source) - A function that should handle the removal of the specified Ext.data.Model (it's been dragged elsewhere)
 * }
 */
Ext.define('eavl.widgets.plugins.ModelDragDrop', {
    alias: 'plugin.modeldnd',

    component : null,
    ddGroup : null,

    dragZone : null,
    dropTarget : null,
    highlightBody : null,
    handleDrop : null,
    handleDrag : null,

    constructor : function(cfg) {
        this.ddGroup = cfg.ddGroup;
        this.highlightBody = cfg.highlightBody === undefined ? false : cfg.highlightBody;
        this.handleDrop = cfg.handleDrop;
        this.handleDrag = cfg.handleDrag;

        this.callParent(arguments);
    },

    init: function(component) {
        this.component = component;

        this.component.on('render', this.registerDDZones, this, {single: true});
        this.component.on('beforedestroy', this.beforeDestroy, this, {single: true});
    },

    registerDDZones : function(component) {
        //This handles drop events
        var dragEl = component.body ? component.body : component.getEl();
        var dropEl = component.getEl();
        var me = this;

        this.dropTarget = new Ext.dd.DropTarget(dropEl, {
            ddGroup : this.ddGroup,
            notifyEnter: function(ddSource, e, data) {
                if (data.source === me) {
                    return;
                }

                if (me.highlightBody) {
                    dropEl.stopAnimation();
                    dropEl.highlight();
                } else {
                    eavl.widgets.util.HighlightUtil.highlight(component);
                }
            },
            notifyDrop: function(ddSource, e, data) {
                if (data.source === me) {
                    return;
                }
                data.source.handleDrag(data.source.component, data.draggedRecord, component);
                me.handleDrop(component, data.draggedRecord, data.source.component);
            }
        });

        //Dragging means we have to do a bit of investigating to work out how
        //we can generate the drag data element (ideally we want to clone it directly
        //from the component.
        var generateSourceEl = null;
        var generateRecord = null;
        if (component.getView) {
            //Anything with a view is easy enough
            generateSourceEl = function(e) {
                return e.getTarget(component.getView().itemSelector, 10);
            };
            generateRecord = function(e, sourceEl) {
                return component.getView().getRecord(sourceEl);
            };
        } else if (component.getValue) {
            //Form fields are workable too...
            generateSourceEl = function(e) {
                return component.body;
            };
            generateRecord = function(e, sourceEl) {
                return component.getValue();
            }
        }

        this.dragZone = new Ext.dd.DragZone(dragEl, {
            ddGroup: this.ddGroup,
            getDragData: function(e) {
                var sourceEl = generateSourceEl(e);
                if (sourceEl) {
                    d = sourceEl.cloneNode(true);
                    d.id = Ext.id();

                    return {
                        ddel: d,
                        sourceEl: sourceEl,
                        repairXY: Ext.fly(sourceEl).getXY(),
                        source: me,
                        draggedRecord: generateRecord(e, sourceEl)
                    }
                }
            },

//          Provide coordinates for the proxy to slide back to on failed drag.
//          This is the original XY coordinates of the draggable element captured
//          in the getDragData method.
            getRepairXY: function() {
                return this.dragData.repairXY;
            }
        });
    },

    beforeDestroy: function() {
        if (this.dropTarget) {
            this.dropTarget.unreg();
            this.dropTarget = null;
        }

        if (this.dragZone) {
            this.dragZone.unreg();
            this.dragZone = null;
        }

        this.callParent();
    }
});