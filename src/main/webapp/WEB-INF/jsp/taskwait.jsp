<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 

<html xmlns:v="urn:schemas-microsoft-com:vml">
    <head>
        <title>EAVL - Waiting...</title>
        
        <%@ include file="../../jsimports.htm"%>
                
        <script type="text/javascript">
        var CONTACT_EMAIL = ${OBSCURED_CONTACT_EMAIL};
        </script>
        
        <script type="text/javascript" src="js/feedback.js/feedback.min.js"></script>
        <link rel="stylesheet" type="text/css" href="js/feedback.js/feedback.css">
        
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/WorkflowLocationPanel.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/TaskWait-UI.css">
        <link rel="stylesheet" type="text/css" href="js/eavl/widgets/FeedbackWidget.css">
        
        <script type="text/javascript" src="js/eavl/widgets/SplashScreen.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/WorkflowLocationPanel.js"></script>
        <script type="text/javascript" src="js/eavl/widgets/FeedbackWidget.js"></script>
        <script type="text/javascript" src="js/eavl/TaskWait-UI.js"></script>
    </head>
    <body>
        <%@ include file="login_widget.jsp" %>
    </body>
</html>

