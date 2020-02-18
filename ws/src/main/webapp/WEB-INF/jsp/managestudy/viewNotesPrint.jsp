<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<html>
<head>
<title><fmt:message key="print_discrepancy_notes" bundle="${resword}"/></title>
<link rel="stylesheet" href="includes/styles.css" type="text/css">
</head>
<body onload="javascript:alert('<fmt:message key="alert_to_print" bundle="${restext}"/>')">

<h1><span class="title_manage"><fmt:message key="view_discrepancy_notes" bundle="${resword}"/></span></h1>

<jsp:include page="../include/alertbox.jsp" />

<table border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td> 	
	 <!-- These DIVs define shaded box borders -->
  <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
  <div class="tablebox_center">

  <table border="0" cellpadding="0" cellspacing="0"> 		
	<tr valign="top">						
	<td class="table_header_row_left"><fmt:message key="study_subject_ID" bundle="${resword}"/></td>
	<td class="table_header_row"><fmt:message key="date_created" bundle="${resword}"/></td>
	<td class="table_header_row"><fmt:message key="event_date" bundle="${resword}"/></td>	
	<td class="table_header_row"><fmt:message key="event" bundle="${resword}"/></td>
	<td class="table_header_row"><fmt:message key="CRF" bundle="${resword}"/></td>
	<td class="table_header_row"><fmt:message key="entity_name" bundle="${resword}"/></td>
	<td class="table_header_row"><fmt:message key="entity_value" bundle="${resword}"/></td>		
	<td class="table_header_row"><fmt:message key="description" bundle="${resword}"/></td>	
	<td class="table_header_row"><fmt:message key="detailed_notes" bundle="${resword}"/></td>
	<td class="table_header_row"><fmt:message key="description" bundle="${resword}"/></td>
	<td class="table_header_row"><fmt:message key="resolution_status" bundle="${resword}"/></td>
	<td class="table_header_row"><fmt:message key="type" bundle="${resword}"/></td>						
	<td class="table_header_row"><fmt:message key="entity_type" bundle="${resword}"/></td>						
	<td class="table_header_row"><fmt:message key="created_by" bundle="${resword}"/></td>			
  </tr>
   <c:forEach var="note" items="${allNotes}">
  <tr valign="top">
    <td class="table_cell_left"><c:out value="${note.studySub.label}" /></td>
    <td class="table_cell"><fmt:formatDate value="${note.createdDate}" pattern="${dteFormat}"/></td>
    <td class="table_cell">
     <c:if test="${note.eventStart != null}">
       <fmt:formatDate value="${note.eventStart}" pattern="${dteFormat}"/>
     </c:if>&nbsp;
    </td>
    <td class="table_cell"><c:out value="${note.eventName}" />&nbsp;</td>
    <td class="table_cell"><c:out value="${note.crfName}" />&nbsp;</td>
    <td class="table_cell">	 
	    <c:out value="${note.entityName}"/>&nbsp;	 
	</td>
	<td class="table_cell"><c:out value="${note.entityValue}" />&nbsp;</td>
    <td class="table_cell"><c:out value="${note.description}" /></td>
    <td class="table_cell" width="400">		
	 <c:out value="${note.detailedNotes}" />&nbsp; 
	</td>
	<td class="table_cell" align="right"><c:out value="${note.numChildren+1}" /></td>
    <td class="table_cell"><c:out value="${note.resStatus.name}" /></td>
    <td class="table_cell"><c:out value="${note.disType.name}" /></td>   
   
    <td class="table_cell"><c:out value="${note.entityType}" />&nbsp;</td>
	
	<td class="table_cell"><c:out value="${note.owner.name}" /></td>	
	
 </tr>
</c:forEach>
</table>

</div>
</div></div></div></div></div></div></div></div>
		</td>
	</tr>
</table>
</body>
</html>
