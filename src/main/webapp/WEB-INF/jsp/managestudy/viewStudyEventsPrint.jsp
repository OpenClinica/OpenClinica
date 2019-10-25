<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<html>
<head>
<title><fmt:message key="openclinica_print_study_events" bundle="${resword}"/></title>
<link rel="stylesheet" href="includes/styles.css" type="text/css">
</head>
<body onload="javascript:alert('<fmt:message key="alert_to_print" bundle="${restext}"/>')">

<c:forEach var="presetValue" items="${presetValues}">
	<c:if test='${presetValue.key == "startDate"}'>
		<c:set var="startDate" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "endDate"}'>
		<c:set var="endDate" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "definitionId"}'>
		<c:set var="definitionId" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "statusId"}'>
		<c:set var="statusId" value="${presetValue.value}" />
	</c:if>

</c:forEach>
<!-- the object inside the array is StudySubjectBean-->

<h1><c:choose>
   <c:when test="${userRole.manageStudy}">
     <div class="title_manage">
   </c:when>
   <c:otherwise>
    <div class="title_manage">
   </c:otherwise>
  </c:choose>
 <fmt:message key="view_all_events_in" bundle="${resword}"/><c:out value="${study.name}"/>
</div></h1>

<jsp:include page="../include/alertbox.jsp"/>

<p><fmt:message key="events_month_shown_default" bundle="${restext}"/></p>
<p><fmt:message key="subject_scheduled_no_DE_yellow" bundle="${restext}"/></p>
 <c:forEach var="eventView" items="${allEvents}">
      <c:choose>
        <c:when test="${userRole.manageStudy}">
         <span class="table_title_manage">
        </c:when>
        <c:otherwise>
          <span class="table_title_submit">
        </c:otherwise>
      </c:choose>
 <fmt:message key="event_name" bundle="${resword}"/>: <c:out value="${eventView.definition.name}"/></span>
	<p><b><fmt:message key="event_type" bundle="${resword}"/></b>: <fmt:message key="${eventView.definition.type}" bundle="${resword}"/>,

	<c:choose>
     <c:when test="${eventView.definition.repeating}">
      <fmt:message key="repeating" bundle="${resword}"/>
     </c:when>
     <c:otherwise>
      <fmt:message key="non_repeating" bundle="${resword}"/>
     </c:otherwise>
    </c:choose>
	<b><fmt:message key="category" bundle="${resword}"/></b>:
	<c:choose>
	 <c:when test="${eventView.definition.category == null || eventView.definition.category ==''}">
	  <fmt:message key="na" bundle="${resword}"/>
	 </c:when>
	 <c:otherwise>
	   <c:out value="${eventView.definition.category}"/>
	 </c:otherwise>
	</c:choose>
	<br>
	<b><fmt:message key="subjects_who_scheduled" bundle="${resword}"/></b>: <c:out value="${eventView.subjectScheduled}"/> (<fmt:message key="start_date_of_first_event" bundle="${resword}"/>:
	<c:choose>
      <c:when test="${eventView.firstScheduledStartDate== null}">
       <fmt:message key="na" bundle="${resword}"/>
      </c:when>
     <c:otherwise>
       <fmt:formatDate value="${eventView.firstScheduledStartDate}" pattern="${dteFormat}"/>
     </c:otherwise>
     </c:choose>) <b><fmt:message key="completed" bundle="${resword}"/></b>: <c:out value="${eventView.subjectCompleted}"/> (<fmt:message key="completion_date_of_last_event" bundle="${resword}"/>:
   <c:choose>
   <c:when test="${eventView.lastCompletionDate== null}">
    <fmt:message key="na" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <fmt:formatDate value="${eventView.lastCompletionDate}" pattern="${dteFormat}"/>
   </c:otherwise>
 </c:choose>) <b><fmt:message key="discontinued" bundle="${resword}"/></b>: <c:out value="${eventView.subjectDiscontinued}"/><br></p>
	<c:set var="events" value="${eventView.studyEvents}" scope="request" />
	<div style="width: 600px">
	 <!-- These DIVs define shaded box borders -->
  <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
  <div class="textbox_center">

   <table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
	 <td class="table_header_row"><fmt:message key="subject_unique_identifier" bundle="${resword}"/></td>
	 <td class="table_header_row"><fmt:message key="event_date_started" bundle="${resword}"/></td>
	 <td class="table_header_row"><fmt:message key="subject_event_status" bundle="${resword}"/></td>
	</tr>
	<c:forEach var="currRow" items="${events}">
	 <c:choose>
    <c:when test="${currRow.scheduledDatePast}">
      <tr valign="top" bgcolor="#FFFF80">
    </c:when>
    <c:otherwise>
    <tr valign="top">
   </c:otherwise>
   </c:choose>
      <td class="table_cell"><c:out value="${currRow.studySubjectLabel}"/></td>
      <td class="table_cell"><fmt:formatDate value="${currRow.dateStarted}"  type="both" timeStyle="short" pattern="${dteFormat}"/></td>
      <td class="table_cell"><c:out value="${currRow.subjectEventStatus.name}"/></td>


   </tr>


	</c:forEach>
	</table>

</div>
</div></div></div></div></div></div></div></div>
</div>
<br><br>
</c:forEach>
</body>
</html>

