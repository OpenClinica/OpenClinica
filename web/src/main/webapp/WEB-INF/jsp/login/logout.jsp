<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<link rel="stylesheet" href="includes/styles.css" type="text/css">
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Home Page</title>
    <link rel="stylesheet" type="text/css" href="/css/bootstrap.css">
    <link rel="stylesheet" type="text/css" href="/css/jumbotron-narrow.css">
    <link rel="stylesheet" type="text/css" href="/css/home.css">
    <link rel="stylesheet" type="text/css" href="/css/jquery.growl.css"/>
    <script src="http://code.jquery.com/jquery.js"></script>
    <script src="/js/jquery.growl.js" type="text/javascript"></script>
    <script src="//cdn.auth0.com/js/auth0/8.8/auth0.min.js"></script>
</head>
<body onload="logout()">

<script type="text/javascript">
    function logout() {
        webAuth.logout({
            client_id: '${clientId}',
            returnTo: '${fn:replace(pageContext.request.requestURL, pageContext.request.requestURI, '')}${logoutEndpoint}'
        });
    }
    // check SSO status
    var webAuth = new auth0.WebAuth({
        domain: '${domain}',
        clientID: '${clientId}',
        audience: 'https://www.openclinica.com',
        responseType: 'code'
    });

</script>
<center><h1><fmt:message key="logging_out" bundle="${resword}"/></h1></center>
</body>
</html>
