<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 

<html xmlns:v="urn:schemas-microsoft-com:vml">
    <head>
        <title>EAVL - Results</title>
        
        <%@ include file="../../../jsimports.htm"%>
        
        <script type="text/javascript">
        var CONTACT_EMAIL = ${OBSCURED_CONTACT_EMAIL};
        </script>
        
        <script type="text/javascript" src="../js/feedback.js/feedback.min.js"></script>
        <link rel="stylesheet" type="text/css" href="../js/feedback.js/feedback.css">
        
        <script src="../portal-core/js/d3/d3.min.js" type="text/javascript" ></script>
        <script src="../portal-core/js/threejs/three.min.js" type="text/javascript"></script>
        <script src="../portal-core/js/threejs/controls/OrbitControls.js" type="text/javascript"></script>
        <script src="../js/d3plugins/d3-tip.js" type="text/javascript" ></script>
        
        <script src="../portal-core/js/portal/util/UnimplementedFunction.js" type="text/javascript"></script>
        <script src="../portal-core/js/portal/util/FileDownloader.js" type="text/javascript"></script>
        <script src="../portal-core/js/portal/util/GoogleAnalytic.js" type="text/javascript"></script>
        <script src="../portal-core/js/portal/util/PiwikAnalytic.js" type="text/javascript"></script>
        <script src="../portal-core/js/portal/widgets/grid/column/ClickColumn.js" type="text/javascript"></script>
        <script src="../portal-core/js/portal/widgets/grid/plugin/CellTips.js" type="text/javascript"></script>
        <script src="../portal-core/js/portal/widgets/grid/plugin/RowExpanderContainer.js" type="text/javascript"></script>
        <script src="../portal-core/js/portal/widgets/grid/plugin/InlineContextMenu.js" type="text/javascript"></script>
        <script src="../portal-core/js/portal/widgets/field/DataDisplayField.js" type="text/javascript"></script>
        <script src="../portal-core/js/portal/charts/BaseD3Chart.js" type="text/javascript"></script>
        <script src="../portal-core/js/portal/charts/3DScatterPlot.js" type="text/javascript"></script>
        
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/CSVGrid.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/WorkflowLocationPanel.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/EAVLJobList.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/JobFileList.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/charts/ProbabilityDensityFunctionChart.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/charts/BoreholeEstimateChart.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/FilePreviewPanel.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/FeedbackWidget.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/EAVLModalWindow.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/ParameterDetailsTagField.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/ParameterDetailsList.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/ErrorWindow.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/JobInfoWindow.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/preview/3DScatterPlotPreview.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/Results-UI.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/EAVL-Common.css">
        
        <script type="text/javascript" src="../js/eavl/models/ParameterDetails.js"></script>
        <script type="text/javascript" src="../js/eavl/models/EAVLJob.js"></script>
        <script type="text/javascript" src="../js/eavl/models/JobFile.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/utils/HighlightUtil.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/plugins/HeaderDragLink.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/plugins/HeaderIcons.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/plugins/HeaderHelp.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/EAVLJobList.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/JobFileList.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/charts/BoreholeEstimateChart.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/CSVGrid.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/FeedbackWidget.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/WorkflowLocationPanel.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/SplashScreen.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/ParameterDetailsList.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/ParameterDetailsField.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/ParameterDetailsTagField.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/preview/BaseFilePreview.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/preview/CSVFilePreview.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/preview/3DScatterPlotPreview.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/preview/BoreholeEstimatePreview.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/FilePreviewPanel.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/EAVLModalWindow.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/JobInfoWindow.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/ErrorWindow.js"></script>
        
        <script type="text/javascript" src="../js/eavl/Results-UI.js"></script>
    </head>
    <body>
        <%@ include file="../login_widget.jsp" %>
    </body>
</html>

