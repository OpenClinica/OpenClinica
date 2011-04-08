<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean scope='request' id='strResStatus' class='java.lang.String' />
<jsp:useBean scope='request' id='writeToDB' class='java.lang.String' />
<jsp:useBean scope='request' id='submitClose' class='java.lang.String' />

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>    
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/> 

<html>
<head>
<title><fmt:message key="openclinica" bundle="${resword}"/>- <fmt:message key="add_discrepancy_note" bundle="${resword}"/></title>
<link rel="stylesheet" href="includes/styles.css" type="text/css">

<script language="JavaScript" src="includes/CalendarPopup.js"></script>
<script language="JavaScript" src="includes/global_functions_javascript.js"></script>

<style type="text/css">

.popup_BG { background-image: url(images/main_BG.gif);
	background-repeat: repeat-x;
	background-position: top;
	background-color: #FFFFFF;
	}


</style>
    <script type="text/javascript" language="javascript">
        setImageInParentWin('flag_<c:out value="${discrepancyNote.field}"/>', '<c:out value="${discrepancyNote.resStatus.iconFilePath}"/>');
    </script>

</head>
<body class="popup_BG" style="margin: 25px;" onload="javascript:refreshSource('true','/ViewNotes?');javascript:window.setTimeout('window.close()',3000);">
<div style="float: left;"><h1 class="table_title_Submit"><fmt:message key="add_discrepancy_note" bundle="${resword}"/></h1></div>
<div style="float: right;"><p><a href="#" onclick="javascript:window.close();"><fmt:message key="close_window" bundle="${resword}"/></a></p></div>
<br clear="all">
<p class="alert" style="font-size: 14px; margin: 120px 50px;" >    
<c:forEach var="message" items="${pageMessages}">
 <c:out value="${message}" escapeXml="false"/><br><br>
</c:forEach>
</p>
</html>
