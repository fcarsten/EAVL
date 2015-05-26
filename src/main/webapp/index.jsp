<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix='security' uri='http://www.springframework.org/security/tags' %>

<html xmlns:v="urn:schemas-microsoft-com:vml">
    <head>
        <title>EAVL Portal</title>
        
        <%@ include file="jsimports.htm"%>
                
        <security:authorize ifAllGranted="ROLE_USER">
            <script type="text/javascript">
            var AUTHENTICATED_USER = true;
		    </script>
         </security:authorize>

        <script type="text/javascript" src="js/feedback.js/feedback.min.js"></script>
        <link rel="stylesheet" type="text/css" href="js/feedback.js/feedback.css">
        
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/WorkflowLocationPanel.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/WorkflowSelectionPanel.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/WorkflowInspectPanel.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/FeedbackWidget.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/EAVLModalWindow.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/EAVL-Common.css">
        
        <script type="text/javascript" src="js/eavl/widgets/SplashScreen.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/FeedbackWidget.js"></script>
        <script type="text/javascript" src="js/eavl/models/Workflow.js"></script>
        <script type="text/javascript" src="js/eavl/models/Contact.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/WorkflowSelectionPanel.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/WorkflowInspectPanel.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/EAVLModalWindow.js"></script>
        <script type="text/javascript" src="js/eavl/Index-UI.js"></script>
    </head>
    <body>
        <%@ include file="WEB-INF/jsp/login_widget.jsp" %>
    </body>
</html>

