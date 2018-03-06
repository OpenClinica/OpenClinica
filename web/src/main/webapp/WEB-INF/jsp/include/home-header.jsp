<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean' />
<jsp:useBean scope='session' id='userRole' class='org.akaza.openclinica.bean.login.StudyUserRoleBean' />
<jsp:useBean scope='request' id='isAdminServlet' class='java.lang.String' />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<script>
    var myContextPath = "${pageContext.request.contextPath}";
    var sessionTimeoutVal = '<%= session.getMaxInactiveInterval() %>';
    console.log("***********************************sessionTimeoutVal:"+ sessionTimeoutVal);
    var userName = "<%= userBean.getName() %>";
    var crossStorageURL = '<%= session.getAttribute("crossStorageURL")%>';
    var prevPageParams = '<%= request.getAttribute("prevPageParams")%>';
    console.log("^^^^^^^^^^^^^^^^^^" + prevPageParams);
    console.log("***********************************Getting crossStorage:"+ crossStorageURL);
    var ocAppTimeoutKey = "OCAppTimeout";
    var firstLoginCheck = '<%= session.getAttribute("firstLoginCheck")%>';
    console.log("Firstr time first:" + firstLoginCheck);
    var currentUser = "currentUser";
    var appName = "RT";
</script>
<head>
    <c:set var="contextPath" value="${fn:replace(pageContext.request.requestURL, fn:substringAfter(pageContext.request.requestURL, pageContext.request.contextPath), '')}" />
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=11" />

    <!-- comment title on this page to fixed edge bug(https://jira.openclinica.com/browse/OC-8814) -->
    <!-- <title><fmt:message key="openclinica" bundle="${resword}"/></title> -->
    <link rel="SHORTCUT ICON" href="images/favicon.png" type="image/x-icon" />


    <link rel="stylesheet" href="includes/styles.css" type="text/css"/>
    <%-- <link rel="stylesheet" href="includes/styles2.css" type="text/css">--%>
    <%-- <link rel="stylesheet" href="includes/NewNavStyles.css" type="text/css" />--%>
    <script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript.js"></script>
    <%-- <script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript2.js"></script> --%>
    <script type="text/JavaScript" language="JavaScript" src="includes/Tabs.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/CalendarPopup.js"></script>
    <script type="text/JavaScript" language="JavaScript" src=
            "includes/repetition-model/repetition-model.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/prototype.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/scriptaculous.js?load=effects"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/effects.js"></script>
    <!-- Added for the new Calender -->

    <link rel="stylesheet" type="text/css" media="all" href="includes/new_cal/skins/aqua/theme.css" title="Aqua" />
    <script type="text/javascript" src="includes/new_cal/calendar.js"></script>
    <!--  fix for issue 14427 Removed the mapping with wrong file name calendar_en.js-->
    <script type="text/javascript" src="includes/new_cal/lang/calendar-en.js"></script>
    <script type="text/javascript" src="includes/new_cal/lang/<fmt:message key="jscalendar_language_file" bundle="${resformat}"/>"></script>
    <script type="text/javascript" src="includes/new_cal/calendar-setup.js"></script>
    <!-- End -->

    <script language="JavaScript">
        function reportBug() {
            var bugtrack = "https://www.openclinica.com/OpenClinica/bug.php?version=<fmt:message key="version_number" bundle="${resword}"/>&user=";
            var user= "<c:out value="${userBean.name}"/>";
            bugtrack = bugtrack + user+ "&url=" + window.location.href;
            openDocWindow(bugtrack);
        }
    </script>

</head>

<body class="main_BG" topmargin="0" leftmargin="0" marginwidth="0" marginheight="0"
<jsp:include page="../include/showPopUp.jsp"/>
>
<script src="https://cdnjs.cloudflare.com/ajax/libs/bluebird/3.3.4/bluebird.min.js"></script>
<script type="text/javascript" src="js/lib/es6-promise.auto.min.js"></script>
<script type="text/javascript" src="js/lib/client.js"></script>
<script type="text/javascript">
    var storage = new CrossStorageClient(crossStorageURL, {
        timeout: 7000});
</script>
<script type="text/javascript" language="JavaScript" src="includes/sessionTimeout.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/auth0/captureKeyboardMouseEvents.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/moment.min.js"></script>
<script type="text/javaScript">
    processTimedOuts(true, false);
</script>

<table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%" class="background">
    <tr>
        <td valign="top">
            <!-- Header Table -->
            <script language="JavaScript">
                var StatusBoxValue=1;
            </script>

            <SCRIPT LANGUAGE="JavaScript">

                document.write('<table border="0" cellpadding="0" cellspacing="0" width="' + document.body.clientWidth + '" class="header">');

            </script>
    <tr>
        <td valign="top">

            <!-- Main Navigation -->

            <jsp:include page="../include/navBar.jsp"/>
            <!-- End Main Navigation -->