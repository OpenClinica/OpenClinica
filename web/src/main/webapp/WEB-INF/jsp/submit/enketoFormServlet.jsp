<%@ page contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<%
    String currentURL = null;
    if (request.getAttribute("javax.servlet.forward.request_uri") != null) {
        currentURL = (String) request.getAttribute("javax.servlet.forward.request_uri");
    }
    if (currentURL != null && request.getQueryString() != null) {
        currentURL += "?" + request.getQueryString();
    }
%>

<script>
    var myContextPath = "${pageContext.request.contextPath}";
    var sessionTimeout = "<%= session.getMaxInactiveInterval() %>";
    var userName = "<%= userBean.getName() %>";
    var currentURL = "<%= currentURL %>";
    var crossStorageURL = "<%= session.getAttribute("crossStorageURL")%>";
    const ocAppTimeoutKey = "OCAppTimeout";
    var firstLoginCheck = "<%= session.getAttribute("firstLoginCheck")%>";
    const currentUser = "currentUser";
    var appName = "RT";
</script>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=8"/>
    <title><fmt:message key="openclinica" bundle="${resword}"/></title>
    <link rel="SHORTCUT ICON" href="images/favicon.ico" type="image/x-icon" />
    <link rel="stylesheet" href="includes/styles.css" type="text/css"/>
    <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
    <script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script>
    <script type="text/javascript" language="JavaScript" src="includes/moment.min.js"></script>
    <script type="text/javascript" src="js/lib/es6-promise.auto.min.js"></script>
    <script type="text/javascript" src="js/lib/client.js"></script>
    <script type="text/javascript" language="JavaScript" src="includes/sessionTimeout.js"></script>
    <script type="text/javascript" language="JavaScript" src="includes/auth0/captureKeyboardMouseEvents.js"></script>
    <script type="text/javascript" language="javascript">

        $(document).ready(function(){
            var fullEnketoURL = "${formURL1}" + '&parentWindowOrigin='+encodeURIComponent(window.location.protocol + '//' + window.location.host) +'&PID='+"${studySubjectId}"+ "${formURL2}";
            iframe = document.getElementById("enketo");
            iframe.setAttribute('src', fullEnketoURL);
        });

        window.addEventListener("message", receiveMessage, false);
        function receiveMessage(event) {
            var postMessage = JSON.parse(event.data);
            if ((postMessage.enketoEvent === 'submissionsuccess') ||
                (postMessage.enketoEvent === 'close')) {
                iframe = document.getElementById("enketo");
                jQuery("#enketo").hide();
                if ("${originatingPage}") window.location.replace("${originatingPage}");
                else window.close();
            }
        }
    </script>
    <script type="text/javascript">
        var realInterval = 60;
        if (sessionTimeout < realInterval)
            realInterval = sessionTimeout;

        setInterval(function () {
                isSessionTimedOut(true, false);
            },
            realInterval * 1000
        );
    </script>
</head>
<c:set var="urlPrefix" value=""/>
<c:set var="requestFromSpringController" value="${param.isSpringController}" />
<c:if test="${requestFromSpringController == 'true' }">
    <c:set var="urlPrefix" value="../"/>
</c:if>
<body style="width:1024px;" class="main_BG">
<script type="application/javascript">
    var storage = new CrossStorageClient(crossStorageURL);
    updateOCAppTimeout();
    isSessionTimedOut(true, false);
</script>
<iframe id="enketo" style="position:fixed;z-index:1011;top:0;left:0;width:100vw;height:100vh;">

</iframe>
</body>

</html>
