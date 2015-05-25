<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 

<html xmlns:v="urn:schemas-microsoft-com:vml">
    <head>
        <title>EAVL - User Administration</title>
        
        <%@ include file="../../../jsimports.htm"%>
        
        <script type="text/javascript">
        var CONTACT_EMAIL = ${OBSCURED_CONTACT_EMAIL};
        </script>
        
        <link rel="stylesheet" type="text/css" href="../js/eavl/widgets/WorkflowLocationPanel.css">
        <link rel="stylesheet" type="text/css" href="../js/eavl/EAVL-Common.css">
        
        <script type="text/javascript" src="../js/eavl/widgets/plugins/HeaderIcons.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/plugins/HeaderHelp.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/utils/HighlightUtil.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/SplashScreen.js"></script>
        <script type="text/javascript" src="../js/eavl/widgets/WorkflowLocationPanel.js"></script>
        
        <script type="text/javascript" src="../js/eavl/admin/EAVLUser.js"></script>
        <script type="text/javascript" src="../js/eavl/admin/Users-UI.js"></script>
    </head>
    <body>
        <%@ include file="../login_widget.jsp" %>
    </body>
</html>

