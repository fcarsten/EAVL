<%@ taglib prefix='security' uri='http://www.springframework.org/security/tags' %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="context" value="${pageContext.request.contextPath}" />

<style type="text/css">

    .login-widget {
        position: fixed;
        right: 10px;
        top: 3px;
        padding: 5px 0 0 0;
    }
    
    .login-widget.bold {
        border-color: #ccc;
        border-radius: 3px;
        border-style: solid;
        border-width: 1px;
        background-color: #1782d3;
    }
    
    .login-widget a {
        padding: 10px 3px;
        display: inline-block;
        text-decoration: none;
    }
    
    .login-widget a span {
        font-size: 13px;
        padding: 0 10px 0 10px;
        color: white;
        font-weight: bold;
    }
    
    .login-widget a.login span {
        font-size: 16px;
        font-weight: 700;
        color: white;
        padding: 0 10px 0 10px;
    }
    
    .login-widget div {
        display: inline-block;
        color: #ccc;
        font-style: italic;
    }
</style>

<security:authorize ifNotGranted="ROLE_USER">
    <div class="login-widget bold">
    <a class="login" href="login.html"><span>Login</span></a>
    </div>
</security:authorize>

<security:authorize ifAllGranted="ROLE_USER">
    <div class="login-widget">
        <div>Hello <security:authentication property="principal.email" />.</div>
        <a class="logout" href="${context}/j_spring_security_logout"><span>Logout</span></a>
    </div>
</security:authorize>
