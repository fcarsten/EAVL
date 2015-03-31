<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 

<html xmlns:v="urn:schemas-microsoft-com:vml">
    <head>
        <title>EAVL Portal</title>
        
        <jsp:include page="jsimports.htm"/>
        
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/WorkflowLocationPanel.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/WorkflowSelectionPanel.css">
        
        <script type="text/javascript" src="js/eavl/widgets/SplashScreen.js"></script>
        <script type="text/javascript" src="js/eavl/models/Workflow.js"></script>
        <script type="text/javascript" src="js/eavl/models/Contact.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/WorkflowSelectionPanel.js"></script>
        <script type="text/javascript" src="js/eavl/Index-UI.js"></script>
    </head>
    <body>
        <div id="workflow-container">
            <div id="top-bar-container">
                <div id="top-bar-logo" style="top:0px;"></div>
                <h1 style="display:inline-block;color:white;position:relative;top:-10px;">Early Access Virtual Laboratory</h1>
            </div>                
        </div>
        <div id="workflow-vp" style="width:100%;height:100%;margin-top:50px"></div>
    </body>
</html>

