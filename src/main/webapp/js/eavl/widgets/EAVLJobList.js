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
        var me = this;
        
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
        
        this.jobInfoAction = new Ext.Action({
            text: 'Info',
            iconCls: 'joblist-info-icon',
            cls: 'joblist-inline-button',
            scope : this,
            handler: this._infoClick
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
                actions: [this.errorMessageAction, this.jobInfoAction, this.deleteJobAction]
            }],
            columns : [{
                dataIndex : 'name',
                flex : 1,
                renderer : function(value, md, record) {
                    var renderDetails = me._jobRenderDetails(record);
                    return Ext.DomHelper.markup({
                        tag : 'div',
                        style : {
                            display: 'table'
                        },
                        children : [{
                            tag: 'a',
                            href: renderDetails.imgLink,
                            children:[{
                                tag: 'img',
                                'data-qtip' : renderDetails.tip,
                                src : renderDetails.img,
                                cls: 'job-row-img'
                            }],
                        },{
                            tag : 'span',
                            cls : 'job-row-text',
                            html : record.get('name')
                        }]
                    });
                }
            }]
        });

        this.callParent(arguments);
        
        this.on('select', this._rowSelect, this);
    },
    
    _jobRenderDetails : function(job) {
        var details = {
            img: '../img/tick.png',
            tip: 'This job has finished and its results are ready.',
            imgLink: '#'
        };
        

        switch(job.get('status')) {
        case eavl.models.EAVLJob.STATUS_UNSUBMITTED:
            details.img = '../img/edit.png';
            details.tip = 'This job hasn\'t been submitted for imputation.';
            details.imgLink = "identify.html?" + Ext.Object.toQueryString({sessionJobId: job.get('id')});
            break;
        case eavl.models.EAVLJob.STATUS_KDE_ERROR:
            details.img = '../img/exclamation.png';
            details.tip = 'There was an error during the conditional probability calculations.';
            details.imgLink = "setproxy.html?" + Ext.Object.toQueryString({sessionJobId: job.get('id')});
            break;
        case eavl.models.EAVLJob.STATUS_IMPUTE_ERROR:
            details.img = '../img/exclamation.png';
            details.tip = 'There was an error during the imputation calculations.';
            details.imgLink = "validate.html?" + Ext.Object.toQueryString({sessionJobId: job.get('id')});
            break;
        case eavl.models.EAVLJob.STATUS_IMPUTING:
            details.img = '../img/loading-bars.svg';
            details.tip = 'This job is currently undergoing imputation.';
            details.imgLink = "../taskwait.html?" + Ext.Object.toQueryString({taskId: job.get('imputationTaskId'), next: 'cp/predictor.html'});
            break;
        case eavl.models.EAVLJob.STATUS_THRESHOLD:
            details.img = '../img/edit.png';
            details.tip = 'This has finished imputation and is awaiting threshold selection.';
            details.imgLink = "threshold.html?" + Ext.Object.toQueryString({sessionJobId: job.get('id')});
            break;
        case eavl.models.EAVLJob.STATUS_PROXY:
            details.img = '../img/edit.png';
            details.tip = 'This has finished imputation and is awaiting proxy selection.';
            details.imgLink = "setproxy.html?" + Ext.Object.toQueryString({sessionJobId: job.get('id')});
            break;
        case eavl.models.EAVLJob.STATUS_SUBMITTED:
            details.img = '../img/loading-bars.svg';
            details.tip = 'This job is currently undergoing conditional probability calculations.';
            details.imgLink = "../taskwait.html?" + Ext.Object.toQueryString({taskId: job.get('kdeTaskId'), next: 'cp/results.html'});
            break;
        }
        
        return details;
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
    
    _infoClick : function() {
        var selection = this.getSelection();
        if (!selection) {
            return;
        }

        var details = this._jobRenderDetails(selection[0]);
        var popup = Ext.create('eavl.widgets.JobInfoWindow', {
            job: selection[0],
            title: Ext.DomHelper.markup({
                tag : 'div',
                style : {
                    display: 'table',
                    'margin-top': -10
                },
                children : [{
                    tag: 'a',
                    href: details.imgLink,
                    children:[{
                        tag: 'img',
                        'data-qtip' : details.tip,
                        src : details.img,
                        cls: 'job-row-img'
                    }],
                },{
                    tag : 'div',
                    id: 'eavl-jobtitle-editable',
                    html : selection[0].get('name')
                }]
            }),
            subtitle: 'Status: ' + selection[0].get('status'),
            listeners: {
                //This adds our editable functionality to the div's onclick handler
                afterrender: function(jobInfoWindow) {
                    var titleEl = Ext.get('eavl-jobtitle-editable');
                    titleEl.on('click', function(evt, el) {
                        var returnToDiv = function() {
                            titleEl.setStyle('display', '');
                            
                            var editParent = Ext.get('eavl-jobtitle-editparent');
                            if (editParent) {
                                var inputValue = editParent.down('input', true).value;
                                
                                if (inputValue !== selection[0].get('name')) {
                                    var mask = new Ext.LoadMask({
                                        msg: 'Renaming...',
                                        target: jobInfoWindow
                                    });
                                    Ext.Ajax.request({
                                        url: 'validation/renameJob.do',
                                        params: {
                                            jobId: selection[0].get('id'),
                                            name: inputValue
                                        },
                                        callback: function(options, success, response) {
                                            mask.hide();
                                            if (!success) {
                                                return;
                                            }
                                            
                                            var responseObj = Ext.JSON.decode(response.responseText);
                                            if (!responseObj.success) {
                                                return;
                                            }
                                            
                                            titleEl.setHtml(inputValue);
                                            selection[0].set('name', inputValue);
                                        }
                                    });
                                }
                                
                                editParent.destroy();
                            }
                        };
                        
                        var parentNode = el.parentNode;

                        //Hide our current node and put an input over the top of it
                        Ext.fly(el).setStyle('display', 'none');
                        var formEl = Ext.fly(parentNode).createChild({
                            tag: 'form',
                            id: 'eavl-jobtitle-editparent',
                            children: [{
                                tag: 'input',
                                name: 'name',
                                value: el.textContent
                            }]
                        });

                        //Let's setup our new form/input 
                        var inputEl = formEl.down('input');
                        formEl.on('submit', returnToDiv);
                        inputEl.on('blur', returnToDiv);
                        inputEl.dom.select();
                    });
                }
            }
        });
        
        popup.show();
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