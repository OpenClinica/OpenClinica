<%@ page contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title><fmt:message key="openclinica" bundle="${resword}"/></title> 
    <link rel="SHORTCUT ICON" href="images/favicon.ico" type="image/x-icon" />
    <link rel="stylesheet" href="includes/styles.css" type="text/css"/>
    <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
    <script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script>
    <jsp:include page="../auth0/ssoLogout.jsp"/>
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
</head>
<body style="width:1024px;" class="main_BG">
<iframe id="enketo" style="position:fixed;z-index:1011;top:0;left:0;width:100vw;height:100vh;"/>
</body>

</html>