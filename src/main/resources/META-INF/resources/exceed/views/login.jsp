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
    <link id="application-styles" href="${contextPath}/res/exceed/style/exceed.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="${contextPath}/res/${appName}/js/html5shiv.min.js"></script>
    <script src="${contextPath}/res/${appName}/js/respond.min.js"></script>
    <![endif]-->
    <meta name="token" content="${_csrf.token}"/>
    <meta name="token-type" content="${_csrf.headerName}"/>

</head>
<body>
<div class="container">
    <h1>Login</h1>
    <form action="${pageContext.request.contextPath}/login_check" method="POST">
        <div class="row">
            <div class="col-md-6">
                <div class="form-group">
                    <label for="loginField">Username</label>
                    <input id="loginField" type="text" class="form-control" name="username">
                </div>
                <div class="form-group">
                    <label for="pwField">Username</label>
                    <input id="pwField" type="password" class="form-control" name="password">
                </div>
                <div class="checkbox">
                    <label>
                        <input type="checkbox" name="remember-me"> Remember me on this computer.
                    </label>
                </div>
                <input class="btn btn-primary" type="submit" value="Login">
            </div>
        </div>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    </form>
</div>
</body>
</html>
