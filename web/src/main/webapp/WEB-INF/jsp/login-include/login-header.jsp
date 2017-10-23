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

<head>
<c:set var="contextPath" value="${fn:replace(pageContext.request.requestURL, fn:substringAfter(pageContext.request.requestURL, pageContext.request.contextPath), '')}" />
  <meta http-equiv="X-UA-Compatible" content="IE=8" />

<title><fmt:message key="openclinica" bundle="${resword}"/></title> 
<link rel="SHORTCUT ICON" href="images/favicon.ico" type="image/x-icon" />

<link rel="stylesheet" href="includes/styles.css" type="text/css">
<%-- <link rel="stylesheet" href="includes/styles2.css" type="text/css">--%>
<%-- <link rel="stylesheet" href="includes/NewNavStyles.css" type="text/css" />--%>
<script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript.js"></script>
<%-- <script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript2.js"></script> --%>
<script type="text/JavaScript" language="JavaScript" src="includes/Tabs.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/CalendarPopup.js"></script>
    <!-- Added for the new Calender -->

    <link rel="stylesheet" type="text/css" media="all" href="includes/new_cal/skins/aqua/theme.css" title="Aqua" />
    <script type="text/javascript" src="includes/new_cal/calendar.js"></script>
    <script type="text/javascript" src="includes/new_cal/lang/calendar-en.js"></script>
    <script type="text/javascript" src="includes/new_cal/lang/<fmt:message key="jscalendar_language_file" bundle="${resformat}"/>"></script>
    <script type="text/javascript" src="includes/new_cal/calendar-setup.js"></script>
<!-- End -->

<%--<script type="text/javascript"  language="JavaScript" src=
    "includes/repetition-model/repetition-model.js"></script>--%>
<script type="text/JavaScript" language="JavaScript" src="includes/prototype.js"></script>
<%--<script type="text/JavaScript" language="JavaScript" src="includes/scriptaculous.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/effects.js"></script>--%></head>


<body style="width:100%;" class="main_BG"

<c:choose>

<c:when test="${tabId!= null && tabId>0}">
onload="TabsForwardByNum(<c:out value="${tabId}"/>);<jsp:include page="../include/showPopUp2.jsp"/>"
</c:when>

<c:otherwise>

<jsp:include page="../include/showPopUp.jsp"/>

</c:otherwise>
</c:choose>
>
<table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%" class=
  "background">
    <tr>
        <td valign="top">
<!-- Header Table -->

<!-- NEW 06-22 -->
    <script language="JavaScript">
    var StatusBoxValue=1;
    </script>

            <table border="0" cellpadding="0" cellspacing="0" class="header">
                <tr>
                    <td valign="top">
                        <div class="disabled_header"><img src="images/spacer.gif"/></div>
                        <!-- Logo -->
                        <div class="logo"><img src="images/logo-color-on-dark.svg"></div>
                        <!-- Main Navigation -->
                        <%-- <jsp:include page="../include/navBar.jsp"/> --%>

<!-- End Main Navigation -->
