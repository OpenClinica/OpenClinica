<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<html>
<body>
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
    var ocAppTimeoutKey = "OCAppTimeout";
    var crossStorageURL = '<%= request.getAttribute("crossStorageURL")%>';
</script>


<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery.min.js"></script>
<script type="text/javascript" language="JavaScript" src="../includes/jmesa/jquery.blockUI.js"></script>
<link rel="stylesheet" href="../includes/css/icomoon-style.css">
<script src="https://cdnjs.cloudflare.com/ajax/libs/bluebird/3.3.4/bluebird.min.js"></script>
<script type="text/javascript" src="../js/lib/es6-promise.auto.min.js"></script>
<script type="text/javascript" src="../js/lib/client.js"></script>
<script type="text/javascript">
    var storage = new CrossStorageClient(crossStorageURL, {
        timeout: 7000});
</script>
<script type="text/javascript" language="JavaScript" src="../includes/sessionTimeout.js"></script>
<script type="text/javascript" language="JavaScript" src="../includes/moment.min.js"></script>


<script type="text/javaScript">
    resetOCAppTimeout();
</script>
<B>Test OCAppTimeout</B>
</body>
</html>