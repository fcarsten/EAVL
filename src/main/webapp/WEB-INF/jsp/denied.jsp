<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix='security' uri='http://www.springframework.org/security/tags' %>

<c:set var="context" value="${pageContext.request.contextPath}" />

<html xmlns:v="urn:schemas-microsoft-com:vml">
    <head>
        <title>Access Denied</title>
        
        <%@ include file="../../jsimports.htm"%>
                
        <script type="text/javascript">
        var CONTACT_EMAIL = ${OBSCURED_CONTACT_EMAIL};
        </script>
        
        <security:authorize ifAllGranted="ROLE_USER">
            <script type="text/javascript">
            var AUTHENTICATED_USER = true;
            </script>
         </security:authorize>

        <script type="text/javascript" src="${context}/js/feedback.js/feedback.min.js"></script>
        <link rel="stylesheet" type="text/css" href="${context}/js/feedback.js/feedback.css">
        
        <link rel="stylesheet" type="text/css" href="${context}/js/eavl/widgets/WorkflowLocationPanel.css">
        <link rel="stylesheet" type="text/css" href="${context}/js/eavl/widgets/WorkflowSelectionPanel.css">
        <link rel="stylesheet" type="text/css" href="${context}/js/eavl/widgets/WorkflowInspectPanel.css">
        <link rel="stylesheet" type="text/css" href="${context}/js/eavl/widgets/FeedbackWidget.css">
        <link rel="stylesheet" type="text/css" href="${context}/js/eavl/widgets/EAVLModalWindow.css">
        <link rel="stylesheet" type="text/css" href="${context}/js/eavl/EAVL-Common.css">
        <link rel="stylesheet" type="text/css" href="${context}/js/eavl/TaskWait-UI.css">
        
        <script type="text/javascript" src="${context}/js/eavl/widgets/SplashScreen.js"></script>
        <script type="text/javascript" src="${context}/js/eavl/widgets/FeedbackWidget.js"></script>
       
        <script type="text/javascript" src="${context}/js/eavl/Denied-UI.js"></script>
    </head>
    <body>
        <%@ include file="login_widget.jsp" %>
    </body>
</html>

