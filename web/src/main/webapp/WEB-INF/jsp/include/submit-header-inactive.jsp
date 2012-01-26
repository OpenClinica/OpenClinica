<%@ page contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean' />
<jsp:useBean scope='session' id='userRole' class='org.akaza.openclinica.bean.login.StudyUserRoleBean' />
<jsp:useBean scope='request' id='isAdminServlet' class='java.lang.String' />

<html>

<head>

<title><fmt:message key="openclinica" bundle="${resword}"/></title>

  <link rel="stylesheet" href="includes/styles.css" type="text/css">
<%-- <link rel="stylesheet" href="includes/styles2.css" type="text/css">--%>
<%-- <link rel="stylesheet" href="includes/NewNavStyles.css" type="text/css" />--%>
  <script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript.js"></script>
<%-- <script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript2.js"></script> --%>
  <script type="text/JavaScript" language="JavaScript" src="includes/Tabs.js"></script>
  <script type="text/JavaScript" language="JavaScript" src="includes/CalendarPopup.js"></script>
  <%--<script type="text/javascript"  language="JavaScript" src="includes/repetition-model/repetition-model.js"></script>--%>
  <script type="text/JavaScript" language="JavaScript" src="includes/prototype.js"></script>
  <script type="text/JavaScript" language="JavaScript" src="includes/scriptaculous.js?load=effects"></script>
  <script type="text/JavaScript" language="JavaScript" src="includes/effects.js"></script>
<!-- Added for the new Calender -->

    <link rel="stylesheet" type="text/css" media="all" href="includes/new_cal/skins/aqua/theme.css" title="Aqua" />
    <script type="text/javascript" src="includes/new_cal/calendar.js"></script>
    <script type="text/javascript" src="includes/new_cal/lang/calendar-en.js"></script>
    <script type="text/javascript" src="includes/new_cal/lang/<fmt:message key="jscalendar_language_file" bundle="${resformat}"/>"></script>
    <script type="text/javascript" src="includes/new_cal/calendar-setup.js"></script>
<!-- End -->

</head>

<body class="main_BG aka_bodywidth" onload="TabsForwardByNum(<c:out value="${tabId}"/>)">
<%--<div style="width:100%;height:100%;opacity:0.1;filter: alpha(opacity=1);">--%>
<div style="width:100%;height:100%">
<table border="0" cellpadding="0" cellspacing="0" width="100%" height=
  "100%" class="background">
<tr>
<td valign="top">
<!-- Header Table -->

<%--<SCRIPT LANGUAGE="JavaScript">

document.write('<table border="0" cellpadding="0" cellspacing="0" width="' +
               document.body.clientWidth + '" class="header">');

</script>--%>
<table border="0" cellpadding="0" cellspacing="0" class="header">
  <tr>
    <td valign="top">

      <!-- Logo -->

    <div class="logo"><img src="images/Logo.gif"></div>

    <!-- Main Navigation -->

   <jsp:include page="../include/navBar.jsp"/>
    <!-- End Main Navigation -->
