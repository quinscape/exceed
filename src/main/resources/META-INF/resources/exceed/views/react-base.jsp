<%@page pageEncoding="UTF-8"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"
%><%@taglib prefix="sec" uri="http://www.springframework.org/security/tags"
%><!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <title>${title} - local base</title>

    <!-- Bootstrap -->
    <link id="application-styles" href="${contextPath}/res/${appName}/style/${appName}.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="${contextPath}/res/${appName}/js/html5shiv.min.js"></script>
    <script src="${contextPath}/res/${appName}/js/respond.min.js"></script>
    <![endif]-->
    <meta name="token" content="${_csrf.token}"/>
    <meta name="token-type" content="${_csrf.headerName}"/>

    <script src="${contextPath}/res/${appName}/js/${applicationScope.reactVersion}"></script>
    <script src="${contextPath}/res/${appName}/js/${applicationScope.reactDOMVersion}"></script>
    <script src="${contextPath}/res/${appName}/js/main.js"></script>
</head>
<body data-app-name="${appName}" data-roles="${userRoles}" data-connection-id="${connectionId}">
<div id="root">
</div>
<script id="root-model" type="x-ceed/view-model">
    ${viewModel}
</script>
<script id="root-data" type="x-ceed/view-data">
    ${viewData}
</script>
<script id="system-info" type="x-ceed/system-info">
    ${systemInfo}
</script>
<footer>
    <c:choose>
        <c:when test="${userName == 'Anonymous'}">
            <a class="btn btn-link" href="${contextPath}/login">Login</a>
        </c:when>
        <c:otherwise>
            <form class="form-inline" action="${contextPath}/logout" method="POST">
                <input type="submit" class="btn btn-link" value="Logout: ${userName}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            </form>
        </c:otherwise>
    </c:choose>
</footer>
</body>
</html>
