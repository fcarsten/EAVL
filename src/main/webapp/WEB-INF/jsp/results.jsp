<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 

<html xmlns:v="urn:schemas-microsoft-com:vml">
    <head>
        <title>EAVL - Results</title>
        
        <jsp:include page="../../jsimports.htm"/>
        
        <style>
	        .x-form-value-field {
			  margin: 0 0 0 0;
			  padding: 0 0 0 0;
			  font-size: 22px;
			  font-weight: 700;
			}
			
			.x-form-value-field-uom {
			  margin: 0 0 0 0;
			  padding: 0 0 0 0;
			  font: normal 18px impact, sans-serif;
			  color: #D05353;
			}
			
			.x-form-value-field-label {
			  margin: 0 0 0 0;
			  padding: 0 0 0 0;
			  font: normal 11px tahoma,arial,verdana,sans-serif;;
			  text-align: center;
			  color: black;
			}
			
			.x-form-image-field {
			  text-align: center;
			}
        </style>
        
        <script src="portal-core/js/d3/d3.min.js" type="text/javascript" ></script>
        <script src="portal-core/js/threejs/three.min.js" type="text/javascript"></script>
        <script src="portal-core/js/threejs/controls/OrbitControls.js" type="text/javascript"></script>
        <script src="js/d3plugins/d3-tip.js" type="text/javascript" ></script>
        
        <script src="portal-core/js/portal/util/UnimplementedFunction.js" type="text/javascript"></script>
        <script src="portal-core/js/portal/util/FileDownloader.js" type="text/javascript"></script>
        <script src="portal-core/js/portal/util/GoogleAnalytic.js" type="text/javascript"></script>
        <script src="portal-core/js/portal/widgets/grid/column/ClickColumn.js" type="text/javascript"></script>
        <script src="portal-core/js/portal/widgets/grid/plugin/CellTips.js" type="text/javascript"></script>
        <script src="portal-core/js/portal/widgets/field/DataDisplayField.js" type="text/javascript"></script>
        <script src="portal-core/js/portal/charts/BaseD3Chart.js" type="text/javascript"></script>
        <script src="portal-core/js/portal/charts/3DScatterPlot.js" type="text/javascript"></script>
        
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/CSVGrid.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/WorkflowLocationPanel.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/EAVLJobList.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/JobFileList.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/charts/ProbabilityDensityFunctionChart.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/charts/BoreholeEstimateChart.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/FilePreviewPanel.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/Results-UI.css">
        
        <script type="text/javascript" src="js/eavl/models/ParameterDetails.js"></script>
        <script type="text/javascript" src="js/eavl/models/EAVLJob.js"></script>
        <script type="text/javascript" src="js/eavl/models/JobFile.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/utils/HighlightUtil.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/plugins/HeaderDragLink.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/plugins/HeaderIcons.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/plugins/HeaderHelp.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/EAVLJobList.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/JobFileList.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/charts/BoreholeEstimateChart.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/CSVGrid.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/SplashScreen.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/WorkflowLocationPanel.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/SplashScreen.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/preview/BaseFilePreview.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/preview/CSVFilePreview.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/preview/3DScatterPlotPreview.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/preview/BoreholeEstimatePreview.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/FilePreviewPanel.js"></script>
        
        <script type="text/javascript" src="js/eavl/Results-UI.js"></script>
    </head>
    <body>
        
    </body>
</html>

