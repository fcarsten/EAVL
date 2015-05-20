<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 

<html xmlns:v="urn:schemas-microsoft-com:vml">
    <head>
        <title>EAVL - Validate Dataset</title>
        
        <%@ include file="../../../jsimports.htm"%>
        
        <script type="text/javascript">
        var CONTACT_EMAIL = ${OBSCURED_CONTACT_EMAIL};
        </script>
        
        <script type="text/javascript" src="../js/feedback.js/feedback.min.js"></script>
        <link rel="stylesheet" type="text/css" href="../js/feedback.js/feedback.css">
        
        <script type="text/javascript" src="../portal-core/js/ext-5.1.0/build/packages/sencha-charts/build/sencha-charts.js"></script>
        <script type="text/javascript" src="../portal-core/js/d3/d3.min.js"></script>
        
        <script src="../portal-core/js/portal/charts/BaseD3Chart.js" type="text/javascript"></script>
        <script src="../portal-core/js/portal/util/UnimplementedFunction.js" type="text/javascript"></script>
        
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/CSVGrid.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/Validate-UI.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/WorkflowLocationPanel.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/ParameterDetailsList.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/ParameterDetailsPanel.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/charts/ProbabilityDensityFunctionChart.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/FeedbackWidget.css">
        
        <script type="text/javascript" src="../js/eavl/models/ParameterDetails.js"></script>
        <script type="text/javascript" src="../js/eavl/models/EAVLJob.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/utils/HighlightUtil.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/plugins/ModelDragDrop.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/plugins/HeaderIcons.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/plugins/HeaderHelp.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/ParameterDetailsList.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/ParameterDetailsPanel.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/CSVGrid.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/SplashScreen.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/WorkflowLocationPanel.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/charts/BaseParameterDetailsChart.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/charts/ProbabilityDensityFunctionChart.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/FeedbackWidget.js"></script>
        <script type="text/javascript" src="../js/eavl/Validate-UI.js"></script>
    </head>
    <body>
        <%@ include file="../login_widget.jsp" %>
    </body>
</html>

