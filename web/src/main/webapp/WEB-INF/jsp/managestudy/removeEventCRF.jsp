<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:include page="../include/managestudy-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">		
        
        <fmt:message key="confirm_removal_of_this_CRF_from_event"  bundle="${resword}"/> <c:out value="${event.studyEventDefinition.name}"/> (<fmt:message key="date"  bundle="${resword}"/> <fmt:formatDate value="${event.dateStarted}" pattern="${dteFormat}"/>). <fmt:message key="all_data_associated_with_the_CRF_in_this_event_will_be_removed"  bundle="${resword}"/>
        
		</div>

		</td>
	
	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope="request" id="displayEventCRF" class="org.akaza.openclinica.bean.submit.DisplayEventCRFBean"/>
<jsp:useBean scope="request" id="event" class="org.akaza.openclinica.bean.managestudy.StudyEventBean"/>
<jsp:useBean scope="request" id="items" class="java.util.ArrayList"/>
<jsp:useBean scope="request" id="studySub" class="org.akaza.openclinica.bean.managestudy.StudySubjectBean"/>

<h1><span class="title_manage">
<fmt:message key="remove_CRF_from_event"  bundle="${resword}"/>
</span></h1>
<br>
<div style="width: 600px">   
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%"> 
  <tr valign="top"><td class="table_header_column"><fmt:message key="event_definition_name" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${event.studyEventDefinition.name}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="location" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${event.location}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="visit" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${event.sampleOrdinal}"/></td></tr>
    
  <tr valign="top"><td class="table_header_column"><fmt:message key="date_started" bundle="${resword}"/>:</td><td class="table_cell"><fmt:formatDate value="${event.dateStarted}" pattern="${dteFormat}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="date_ended" bundle="${resword}"/>:</td><td class="table_cell"><fmt:formatDate value="${event.dateEnded}" pattern="${dteFormat}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="status" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${event.status.name}"/>
  </td></tr>

 </table> 
</div>
</div></div></div></div></div></div></div></div>

</div>
<br>
<span class="table_title_manage"><fmt:message key="event_CRF" bundle="${resword}"/></span>
<div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%"> 
    <tr>
		<td class="table_header_column_top"><fmt:message key="CRF_name" bundle="${resword}"/></td>
		<td class="table_header_column_top"><fmt:message key="version" bundle="${resword}"/></td>
		<td class="table_header_column_top"><fmt:message key="date_interviewed" bundle="${resword}"/></td>
		<td class="table_header_column_top"><fmt:message key="interviewer_name" bundle="${resword}"/></td>
		<td class="table_header_column_top"><fmt:message key="owner" bundle="${resword}"/></td>
		<td class="table_header_column_top"><fmt:message key="completion_status" bundle="${resword}"/></td>	
		
	 </tr> 
	<tr>
		<td class="table_cell"><c:out value="${displayEventCRF.eventCRF.crf.name}" /></td>
		<td class="table_cell"><c:out value="${displayEventCRF.eventCRF.crfVersion.name}" /></td>
		<td class="table_cell"><fmt:formatDate value="${displayEventCRF.eventCRF.dateInterviewed}" pattern="${dteFormat}"/>&nbsp;</td>
		<td class="table_cell"><c:out value="${displayEventCRF.eventCRF.interviewerName}"/>&nbsp;</td>
		<td class="table_cell"><c:out value="${displayEventCRF.eventCRF.owner.name}" /></td>
		<td class="table_cell"><c:out value="${displayEventCRF.stage.name}" /></td>	
		
	 </tr>
 
 </table> 
</div>
</div></div></div></div></div></div></div></div>

</div>
<br>
 <c:if test="${!empty items}">
 <span class="table_title_manage"><fmt:message key="item_data" bundle="${resword}"/></span>
 <div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%"> 
    <tr>
		<td class="table_header_column_top"><fmt:message key="Id" bundle="${resword}"/></td>
		<td class="table_header_column_top"><fmt:message key="value" bundle="${resword}"/></td>	
	 </tr>
	 <c:set var="count" value="0"/>
  <c:forEach var="item" items="${items}"> 
	<tr>
		<td class="table_cell"><c:out value="${item.itemId}" /></td>
		<td class="table_cell"><c:out value="${item.value}" />&nbsp;</td>	
	
		
		<c:if test="${!item.status.deleted}">
		<c:set var="count" value="${count+1}"/>
		</c:if>
	 </tr>
	</c:forEach> 
 
 </table>
 
</div>
</div></div></div></div></div></div></div></div>

</div>
<br>
 </c:if>
   <c:choose>
    <c:when test="${!empty items && count>0}">
     <form action='RemoveEventCRF?action=submit&id=<c:out value="${displayEventCRF.eventCRF.id}"/>&studySubId=<c:out value="${studySub.id}"/>' method="POST">
      <input type="submit" name="submit" value="<fmt:message key="remove_event_CRF" bundle="${resword}"/>" class="button_xlong" onClick='return confirm("<fmt:message key="this_crf_has_data_remove" bundle="${resword}"/>");'>
      <input type="button" onclick="confirmCancel('ViewStudyEvents');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
     </form>
    </c:when>
    <c:otherwise>
      <form action='RemoveEventCRF?action=submit&id=<c:out value="${displayEventCRF.eventCRF.id}"/>&studySubId=<c:out value="${studySub.id}"/>' method="POST">
      <input type="submit" name="submit" value="<fmt:message key="remove_event_CRF" bundle="${resword}"/>" class="button_xlong" onClick='return confirm("<fmt:message key="are_you_sure_you_want_to_remove_it" bundle="${resword}"/>");'>
      <input type="button" onclick="confirmCancel('ViewStudyEvents');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
     </form>
    </c:otherwise>
   </c:choose>  
 

<jsp:include page="../include/footer.jsp"/>
