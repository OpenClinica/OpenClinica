<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean scope='request' id='strResStatus' class='java.lang.String' />
<jsp:useBean scope='request' id='writeToDB' class='java.lang.String' />

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>    
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/> 

<html>
<head>
<title><fmt:message key="openclinica" bundle="${resword}"/>- <fmt:message key="add_discrepancy_note" bundle="${resword}"/></title>
<link rel="stylesheet" href="includes/styles.css" type="text/css">
<script language="JavaScript" src="includes/global_functions_javascript.js"></script>

<script language="JavaScript" src="includes/CalendarPopup.js"></script>
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
<body class="popup_BG" style="margin: 25px;">
<div style="float: left;"><h1 class="table_title_Submit"><fmt:message key="add_discrepancy_note" bundle="${resword}"/></h1></div>
<div style="float: right;"><p><a href="#" onclick="javascript:window.close();"><fmt:message key="close_window" bundle="${resword}"/></a></p></div>
<br clear="all">
<div class="alert">    
<c:forEach var="message" items="${pageMessages}">
 <c:out value="${message}" escapeXml="false"/> 
</c:forEach>
</div>
<div class="alert" style="font-size: 12px; margin: 100px 0px">  
 <fmt:message key="attention_must_complete_and_submit" bundle="${restext}"/>
</div>
    <table border="0"> 
     <c:if test="${parent.id>0}">
     <tr valign="top">
            <td><fmt:message key="discrepancy_thread_id" bundle="${resword}"/></td>
            <td>
            <c:out value="${parent.id}"/>
            </td>
       </tr>       
     </c:if>
        <c:if test="${hasNotes == 'yes'}">        
        <tr valign="top">
            <td colspan="2"><a href="ViewDiscrepancyNote?id=<c:out value="${discrepancyNote.entityId}"/>&name=<c:out value="${discrepancyNote.entityType}"/>&field=<c:out value="${discrepancyNote.field}"/>&eventCRFId=<c:out value="${eventCRFId}"/>&column=<c:out value="${discrepancyNote.column}"/>">
            <fmt:message key="view_parent_and_related_note" bundle="${resword}"/></a> 
           </td>         
        </tr>
       </c:if> 
    </table>
    <br/>
    <br/>
    <br/>
    <br/>
    <br/>
    <br/>
    <br/>
    <table border="0"> 
     <tr>     
      <td><input type="submit" name="B1" value="<fmt:message key="close" bundle="${resword}"/>" class="button_medium" onclick="javascript:window.close();"></td> 
    </tr>
    </table>  
</html>
