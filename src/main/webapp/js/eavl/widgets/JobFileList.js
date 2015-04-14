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
     *  hasPreview : [Optional] function(fileName, job) - Callback to return true if the particular fileName has a previewer
     *  hasDataView : [Optional] function(fileName, job) - Callback to return true if the particular fileName has a dataView
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
        this.hasPreview = Ext.isFunction(config.hasPreview) ? config.hasPreview : function() {return true;};
        this.hasDataView = Ext.isFunction(config.hasDataView) ? config.hasDataView : function() {return true;};

        var me = this;
        var store = Ext.create('Ext.data.Store', {
            model : 'eavl.models.JobFile',
            grouper: Ext.create('eavl.widgets.JobFileListGrouper', {property: 'group'}),
            proxy : {
                type : 'ajax',
                url : 'results/getFilesForJob.do',
                reader : {
                    type : 'json',
                    rootProperty : 'data'
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
        
        this.groupingFeature = Ext.create('Ext.grid.feature.Grouping',{
            groupHeaderTpl: '{name}'
        });
        
        Ext.apply(config, {
            hideHeaders : true,
            store : store,
            features: [this.groupingFeature],
            plugins: [{
                ptype: 'celltips'
            },{
                ptype: 'headericons',
                icons: [{
                    location: 'left',
                    src: 'img/download.png',
                    tip: 'Click to download all files as a ZIP',
                    width: 32,
                    height: 32,
                    style: {
                        'cursor': 'pointer',
                        'margin-top': '-3px',
                        'margin-left': '-3px'
                    },
                    handler: Ext.bind(this._downloadAllClickHandler, this) 
                }]
            }],
            columns : [{
                xtype: 'clickcolumn',
                dataIndex : 'name',
                width: 48,
                renderer: Ext.bind(function(value, md, jobFile) {
                    return Ext.DomHelper.markup({
                        tag : 'img',
                        width : 32,
                        height : 32,
                        style: {
                            cursor: 'pointer'
                        },
                        src: 'img/download.png'
                    });
                }, this),
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
                renderer: Ext.bind(function(value, md, jobFile) {
                    if (!this.hasDataView(this.job, jobFile.get('name'))) {
                        return '';
                    }
                    
                    return Ext.DomHelper.markup({
                        tag : 'img',
                        width : 32,
                        height : 32,
                        style: {
                            cursor: 'pointer'
                        },
                        src: 'portal-core/img/binary.png'
                    });
                }, this),
                hasTip : true,
                tipRenderer: Ext.bind(function(value, jobFile) {
                    if (!this.hasDataView(this.job, jobFile.get('name'))) {
                        return "There is currently no way to visualise this data in your browser.";
                    }
                    
                    return "Click to view the raw file data.";
                }, this),
                listeners : {
                    columnclick : Ext.bind(this._dataClickHandler, this)
                }
            },{
                xtype: 'clickcolumn',
                dataIndex : 'name',
                width: 48,
                renderer: Ext.bind(function(value, md, jobFile) {
                    if (!this.hasPreview(this.job, jobFile.get('name'))) {
                        return '';
                    }
                    
                    return Ext.DomHelper.markup({
                        tag : 'img',
                        width : 32,
                        height : 32,
                        style: {
                            cursor: 'pointer'
                        },
                        src: 'img/inspect.png'
                    });
                }, this),
                hasTip : true,
                tipRenderer: Ext.bind(function(value, jobFile) {
                    if (!this.hasPreview(this.job, jobFile.get('name'))) {
                        return "There is currently no way to visualise this file in your browser.";
                    }
                    return "Click to visualise this file.";
                }, this),
                listeners : {
                    columnclick : Ext.bind(this._inspectClickHandler, this)
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
        
        store.on('load', this._storeLoad, this);
    },
    
    _storeLoad : function(store, records, successful) {
        if (successful) {
            this.groupingFeature.collapseAll();
            
            //Expand the "first" group in the list (that exists)
            Ext.each(eavl.widgets.JobFileListGrouper.ordering, function(group) {
                if (group in this.groupingFeature.groupCache) {
                    this.groupingFeature.expand(group);
                    return false;
                }
            }, this);
        }
    },

    _downloadAllClickHandler : function() {
        if (!this.job) {
            return;
        }
        
        var ds = this.getStore();
        
        var fileNames = [];
        for (var i = 0; i < ds.getCount(); i++) {
            fileNames.push(ds.getAt(i).get('name'));
        }
        
        portal.util.FileDownloader.downloadFile("results/downloadFiles.do", {
            jobId : this.job.get('id'),
            name : fileNames
        });
    },
    
    _downloadClickHandler :  function(value, record, column, tip) {
        portal.util.FileDownloader.downloadFile("results/downloadFiles.do", {
            jobId : this.job.get('id'),
            name : [record.get('name')]
        });
    },

    _inspectClickHandler :  function(value, record, column, tip) {
        if (this.hasPreview(this.job, record.get('name'))) {
            this.fireEvent('preview', this, record.get('name'), this.job);
        }
    },

    _dataClickHandler :  function(value, record, column, tip) {
        if (this.hasDataView(this.job, record.get('name'))) {
            this.fireEvent('dataview', this, record.get('name'), this.job);
        }
    },

    /**
     * Reload this list with files for the specified job
     *
     * @param job EAVLJob instance to show or null if you wish to clear this list
     */
    showFilesForJob : function(job) {
        this.job = job;

        var store = this.getStore();
        store.removeAll(false);
        
        if (job) {
            var ajaxProxy = store.getProxy();
            ajaxProxy.extraParams.jobId = job.get('id');
            store.load();
        }
    }
});

/**
 * This is our custom grouper to override the ordering of group names
 */
Ext.define('eavl.widgets.JobFileListGrouper', {
    extend: 'Ext.util.Grouper',
    
    statics : {
        ordering : ['Conditional Probability Results', 'Imputation Results', 'Validation Results', 'Input Data'],
    },
    
    sortFn : function(item1, item2) {
        var index1 = Ext.Array.indexOf(eavl.widgets.JobFileListGrouper.ordering, item1.get('group'));
        var index2 = Ext.Array.indexOf(eavl.widgets.JobFileListGrouper.ordering, item2.get('group'));
        
        if (index1 < 0) {
            index1 = eavl.widgets.JobFileListGrouper.ordering.length;
        }
        if (index2 < 0) {
            index2 = eavl.widgets.JobFileListGrouper.ordering.length;
        }
        
        return index2 - index1;
    }
});