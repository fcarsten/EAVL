/**
 * Grid Panel extension for rendering a list of JobFile objects
 */
Ext.define('eavl.widgets.JobFileList', {
    extend: 'Ext.grid.Panel',

    alias: 'widget.jobfilelist',

    job: null,

    /**
     * Adds the following config to Ext.grid.Panel
     * {
     *
     * }
     *
     * Adds the following events
     * {
     *  preview : function(this, fileName, job)
     *  dataview : function(this, fileName, job)
     * }
     */
    constructor : function(config) {
        this.emptyText = config.emptyText ? config.emptyText : "";

        var me = this;
        var store = Ext.create('Ext.data.Store', {
            model : 'eavl.models.JobFile',
            proxy : {
                type : 'ajax',
                url : 'results/getFilesForJob.do',
                reader : {
                    type : 'json',
                    root : 'data'
                },
                listeners : {
                    exception : function(proxy, response, operation) {
                        responseObj = Ext.JSON.decode(response.responseText);
                        errorMsg = responseObj.msg;
                        errorInfo = responseObj.debugInfo;
                    }
                }
            }
        });

        Ext.apply(config, {
            hideHeaders : true,
            store : store,
            plugins: [{
                ptype: 'celltips'
            }],
            columns : [{
                xtype: 'clickcolumn',
                dataIndex : 'name',
                width: 48,
                renderer: function() {
                    return Ext.DomHelper.markup({
                        tag : 'img',
                        width : 32,
                        height : 32,
                        style: {
                            cursor: 'pointer'
                        },
                        src: 'img/download.png'
                    });
                },
                hasTip : true,
                tipRenderer: function() {
                    return "Click to download this file as a ZIP";
                },
                listeners : {
                    columnclick : Ext.bind(this._downloadClickHandler, this)
                }
            },{
                xtype: 'clickcolumn',
                dataIndex : 'name',
                width: 48,
                renderer: function() {
                    return Ext.DomHelper.markup({
                        tag : 'img',
                        width : 32,
                        height : 32,
                        style: {
                            cursor: 'pointer'
                        },
                        src: 'portal-core/img/binary.png'
                    });
                },
                hasTip : true,
                tipRenderer: function() {
                    return "Click to view the raw file data.";
                },
                listeners : {
                    columnclick : Ext.bind(this._inspectClickHandler, this)
                }
            },{
                xtype: 'clickcolumn',
                dataIndex : 'name',
                width: 48,
                renderer: function() {
                    return Ext.DomHelper.markup({
                        tag : 'img',
                        width : 32,
                        height : 32,
                        style: {
                            cursor: 'pointer'
                        },
                        src: 'img/inspect.png'
                    });
                },
                hasTip : true,
                tipRenderer: function() {
                    return "Click to visualise this file.";
                },
                listeners : {
                    columnclick : Ext.bind(this._dataClickHandler, this)
                }
            },{
                dataIndex : 'name',
                flex : 1,
                renderer : function(value, md, record) {
                    return Ext.DomHelper.markup({
                        tag : 'div',
                        style : {
                            display: 'table',
                            height: 28
                        },
                        children : [{
                            tag : 'span',
                            cls : 'jobfile-row-text',
                            html : value
                        }]});
                }
            }]
        });

        this.callParent(arguments);

        this.addEvents(['preview', 'dataview']);
    },

    _downloadClickHandler :  function(value, record, column, tip) {
        portal.util.FileDownloader.downloadFile("results/downloadFiles.do", {
            jobId : this.job.get('id'),
            name : record.get('name')
        });
    },

    _inspectClickHandler :  function(value, record, column, tip) {
        this.fireEvent('preview', this, record.get('name'), this.job);
    },

    _dataClickHandler :  function(value, record, column, tip) {
        this.fireEvent('dataview', this, record.get('name'), this.job);
    },

    /**
     * Reload this list with files for the specified job
     *
     * @param job EAVLJob instance to show
     */
    showFilesForJob : function(job) {
        this.job = job;

        var store = this.getStore();
        var ajaxProxy = store.getProxy();
        ajaxProxy.extraParams.jobId = job.get('id');
        store.removeAll(false);
        store.load();
    }
});