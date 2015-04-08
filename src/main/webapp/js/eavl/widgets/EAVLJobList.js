/**
 * Grid Panel extension for rendering a list of EAVLJob objects
 */
Ext.define('eavl.widgets.EAVLJobList', {
    extend: 'Ext.grid.Panel',

    alias: 'widget.eavljoblist',

    /**
     * Adds the following config to Ext.grid.Panel
     * {
     *  jobs : eavl.model.EAVLJob[] [Optional] The set of parameter details to initialise this list with
     * }
     *
     * Adds the following events
     * {
     *  jobdelete : function(this, job) - fired whenever a job is succesfully deleted
     * }
     */
    constructor : function(config) {
        this.deleteJobAction = new Ext.Action({
            text: 'Delete',
            iconCls: 'joblist-trash-icon',
            cls: 'joblist-inline-button',
            scope : this,
            handler: this._deleteClick
        });
        
        this.errorMessageAction = new Ext.Action({
            text: 'Error Message',
            iconCls: 'joblist-error-icon',
            cls: 'joblist-inline-button',
            scope : this,
            handler: this._errorMessageClick
        });
        
        
        this.emptyText = config.emptyText ? config.emptyText : "";

        var store = Ext.create('Ext.data.Store', {
            model : 'eavl.models.EAVLJob',
            data : config.jobs ? config.jobs : []
        });

        Ext.apply(config, {
            hideHeaders : true,
            store : store,
            plugins : [{
                ptype : 'inlinecontextmenu',
                align : 'right',
                actions: [this.errorMessageAction, this.deleteJobAction]
            }],
            columns : [{
                dataIndex : 'name',
                flex : 1,
                renderer : function(value, md, record) {
                    var emptyString = value === '';

                    var totalDataPoints = record.get('totalNumeric') + record.get('totalText') + record.get('totalMissing');
                    var percentageNumeric = (record.get('totalNumeric') * 100) / totalDataPoints;
                    var img = 'img/tick.png';
                    var tip = 'This job has finished and its results are ready.';
                    var imgLink = '#';

                    switch(record.get('status')) {
                    case eavl.models.EAVLJob.STATUS_UNSUBMITTED:
                        img = 'img/edit.png';
                        tip = 'This job hasn\'t been submitted for imputation.';
                        imgLink = "validate.html?" + Ext.Object.toQueryString({sessionJobId: record.get('id')});
                        break;
                    case eavl.models.EAVLJob.STATUS_KDE_ERROR:
                        img = 'img/exclamation.png';
                        tip = 'There was an error during the conditional probability calculations.';
                        imgLink = "setproxy.html?" + Ext.Object.toQueryString({sessionJobId: record.get('id')});
                        break;
                    case eavl.models.EAVLJob.STATUS_IMPUTE_ERROR:
                        img = 'img/exclamation.png';
                        tip = 'There was an error during the imputation calculations.';
                        imgLink = "validate.html?" + Ext.Object.toQueryString({sessionJobId: record.get('id')});
                        break;
                    case eavl.models.EAVLJob.STATUS_IMPUTING:
                        img = 'img/loading-bars.svg';
                        tip = 'This job is currently undergoing imputation.';
                        imgLink = "taskwait.html?" + Ext.Object.toQueryString({taskId: record.get('imputationTaskId'), next: 'predictor.html'});
                        break;
                    case eavl.models.EAVLJob.STATUS_THRESHOLD:
                        img = 'img/edit.png';
                        tip = 'This has finished imputation and is awaiting threshold selection.';
                        imgLink = "threshold.html?" + Ext.Object.toQueryString({sessionJobId: record.get('id')});
                        break;
                    case eavl.models.EAVLJob.STATUS_PROXY:
                        img = 'img/edit.png';
                        tip = 'This has finished imputation and is awaiting proxy selection.';
                        imgLink = "setproxy.html?" + Ext.Object.toQueryString({sessionJobId: record.get('id')});
                        break;
                    case eavl.models.EAVLJob.STATUS_SUBMITTED:
                        img = 'img/loading-bars.svg';
                        tip = 'This job is currently undergoing conditional probability calculations.';
                        imgLink = "taskwait.html?" + Ext.Object.toQueryString({taskId: record.get('kdeTaskId'), next: 'results.html'});
                        break;
                    }

                    return Ext.DomHelper.markup({
                        tag : 'div',
                        style : {
                            display: 'table'
                        },
                        children : [{
                            tag: 'a',
                            href: imgLink,
                            children:[{
                                tag: 'img',
                                'data-qtip' : tip,
                                src : img,
                                cls: 'job-row-img'
                            }],
                        },{
                            tag : 'span',
                            cls : 'job-row-text',
                            html : record.get('name')
                        }]});
                }
            }]
        });

        this.callParent(arguments);
        
        this.on('select', this._rowSelect, this);
    },
    
    /**
     * Turns on/off various inline selection actions based on the selected job
     */ 
    _rowSelect : function() {
        var selection = this.getSelection();
        if (!selection) {
            return;
        }
        
        var job = selection[0];
        if (job.get('kdeTaskError') || job.get('imputationTaskError')) {
            this.errorMessageAction.show();
        } else {
            this.errorMessageAction.hide();
        }
    },
    
    _errorMessageClick : function() {
        var selection = this.getSelection();
        if (!selection) {
            return;
        }
        
        var job = selection[0];
        var message = job.get('kdeTaskError') ? job.get('kdeTaskError') : job.get('imputationTaskError');
        if (!message) {
            return;
        }
        Ext.create('eavl.widgets.ErrorWindow', {
            title: 'Error Message',
            message: message,
            job: job
        }).show();
    },
    
    _deleteJob : function(job) {
        var mask = new Ext.LoadMask({
            msg    : 'Deleting job...',
            target : this
        });
        mask.show();
        
        Ext.Ajax.request({
            url: 'results/deleteJob.do',
            params: {
                jobId: job.get('id')
            },
            scope: this,
            callback: function(options, success, response) {
                mask.hide();
                mask.destroy();
                
                if (!success) {
                    Ext.Msg.alert('Error', 'Error contacting EAVL server. Please try refreshing the page.');
                    return;
                }
                
                if (!Ext.JSON.decode(response.responseText).success) {
                    Ext.Msg.alert('Error', 'Error deleting job. Please try refreshing the page before retrying.');
                    return;
                }
                
                this.getStore().remove(job);
                this.fireEvent('jobdelete', this, job);
            }
        });
    },
    
    _deleteClick : function() {
        var selection = this.getSelection();
        if (!selection) {
            return;
        }
        
        Ext.Msg.show({
            title:'Confirm deletion',
            message: 'You are about to completely delete this job and all input/output files. Are you sure you wish to continue?',
            buttons: Ext.Msg.YESNO,
            icon: Ext.Msg.ERROR,
            scope: this,
            fn: function(btn) {
                if (btn === 'yes') {
                    this._deleteJob(selection[0]);
                }
            }
        });        
    }
});