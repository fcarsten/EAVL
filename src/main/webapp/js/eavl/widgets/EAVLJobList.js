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
     *
     * }
     */
    constructor : function(config) {
        this.emptyText = config.emptyText ? config.emptyText : "";


        var store = Ext.create('Ext.data.Store', {
            model : 'eavl.models.EAVLJob',
            data : config.jobs ? config.jobs : []
        });

        Ext.apply(config, {
            hideHeaders : true,
            store : store,
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
                    case eavl.models.EAVLJob.STATUS_PREDICTOR:
                        img = 'img/edit.png';
                        tip = 'This has finished imputation and is awaiting predictor selection.';
                        imgLink = "predictor.html?" + Ext.Object.toQueryString({sessionJobId: record.get('id')});
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
    }
});