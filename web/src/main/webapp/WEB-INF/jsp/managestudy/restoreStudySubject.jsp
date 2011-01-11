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
         <fmt:message key="confirm_restoration_of_this_subject_to_study"  bundle="${resword}"/> <c:out value="${study.name}"/>. <fmt:message key="the_subject_and_all_data_associated_with_it_in" bundle="${resword}"/> 
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

<jsp:useBean scope="session" id="studySub" class="org.akaza.openclinica.bean.managestudy.StudySubjectBean"/>
<jsp:useBean scope="request" id="subject" class="org.akaza.openclinica.bean.submit.SubjectBean"/>
<jsp:useBean scope="request" id="study" class="org.akaza.openclinica.bean.managestudy.StudyBean"/>
<jsp:useBean scope="request" id="events" class="java.util.ArrayList"/>
<h1><span class="title_manage">
<fmt:message key="restore_subject_to_study" bundle="${resword}"/>
</span></h1>

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <%-- <tr valign="top"><td class="table_header_column"><fmt:message key="subject_ID" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${subject.id}"/></td></tr> --%>
  <tr valign="top"><td class="table_header_column"><fmt:message key="study_subject_ID" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${studySub.label}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="person_ID" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${subject.uniqueIdentifier}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="gender" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${subject.gender}"/></td></tr>
    
  <%-- <tr valign="top"><td class="table_header_column"><fmt:message key="label" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${studySub.label}"/></td></tr> --%>
  <tr valign="top"><td class="table_header_column"><fmt:message key="secondary_ID" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${studySub.secondaryLabel}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="enrollment_date" bundle="${resword}"/>:</td>
  <td class="table_cell"><fmt:formatDate value="${studySub.enrollmentDate}" pattern="${dteFormat}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="created_by" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${studySub.owner.name}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="date_created" bundle="${resword}"/>:</td><td class="table_cell"><fmt:formatDate value="${studySub.createdDate}" pattern="${dteFormat}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="last_updated_by" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${studySub.updater.name}"/>&nbsp;
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="date_updated" bundle="${resword}"/>:</td><td class="table_cell"><fmt:formatDate value="${studySub.updatedDate}" pattern="${dteFormat}"/>&nbsp;
  </td></tr>
 </table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<br>
 <c:if test="${!empty events}">
 <span class="table_title_manage"><fmt:message key="subject_events" bundle="${resword}"/> </span>
 <div style="width: 600px">
   <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

   <div class="tablebox_center">
   <table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
        <td class="table_header_row"><fmt:message key="last_update" bundle="${resword}"/></td>
        <td class="table_header_row"><fmt:message key="event" bundle="${resword}"/></td>
        <td class="table_header_row"><fmt:message key="start_date" bundle="${resword}"/></td>
        <td class="table_header_row"><fmt:message key="end_date" bundle="${resword}"/></td>
        <td class="table_header_row"><fmt:message key="location" bundle="${resword}"/></td>
        <td class="table_header_row"><fmt:message key="update_by" bundle="${resword}"/></td>
        <td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
      </tr>
    <%--<c:forEach var="event" items="${events}">--%>
    <%--<tr>      --%>
        <%--<td class="table_cell"><fmt:formatDate value="${event.updatedDate}" pattern="${dteFormat}"/></td>--%>
        <%--<td class="table_cell"><c:out value="${event.studyEventDefinition.name}"/></td>--%>
        <%--<td class="table_cell"><fmt:formatDate value="${event.dateStarted}" pattern="${dteFormat}"/></td>--%>
        <%--<td class="table_cell"><fmt:formatDate value="${event.dateEnded}" pattern="${dteFormat}"/></td>--%>
        <%--<td class="table_cell"><c:out value="${event.location}"/></td>--%>
        <%--<td class="table_cell"><c:out value="${event.updater.name}"/></td>--%>
        <%--<td class="table_cell"><c:out value="${event.status.name}"/></td>--%>
     <%--</tr>--%>
     <%--</c:forEach>  --%>
       <c:forEach var="displayEvents" items="${events}">
       <tr>
           <td class="table_cell"><fmt:formatDate value="${displayEvents.studyEvent.updatedDate}" pattern="${dteFormat}"/></td>
           <td class="table_cell"><c:out value="${displayEvents.studyEvent.studyEventDefinition.name}"/>
           <c:if test="${displayEvents.studyEvent.studyEventDefinition.repeating}">
               (<c:out value="${displayEvents.studyEvent.sampleOrdinal}"/>)
           </c:if>
           </td>
           <td class="table_cell"><fmt:formatDate value="${displayEvents.studyEvent.dateStarted}" pattern="${dteFormat}"/></td>
           <td class="table_cell"><fmt:formatDate value="${displayEvents.studyEvent.dateEnded}" pattern="${dteFormat}"/></td>
           <td class="table_cell"><c:out value="${displayEvents.studyEvent.location}"/></td>
           <td class="table_cell"><c:out value="${displayEvents.studyEvent.updater.name}"/></td>
           <td class="table_cell"><c:out value="${displayEvents.studyEvent.status.name}"/></td>
        </tr>
        </c:forEach>
   </table>  
</div>
</div></div></div></div></div></div></div></div>

</div>
<br>
   </c:if>  
   
   <c:choose>
    <c:when test="${!empty events}">
     <form action='RestoreStudySubject?action=submit&id=<c:out value="${studySub.id}"/>&subjectId=<c:out value="${studySub.subjectId}"/>&studyId=<c:out value="${studySub.studyId}"/>' method="POST">
       <input type="submit" name="submit" value="<fmt:message key="restore_subject_to_study" bundle="${resword}"/>" class="button_xlong" onClick='return confirm("<fmt:message key="this_subject_has_data_from_events_restore" bundle="${resword}"/>");'>
       <input type="button" onclick="confirmCancel('ListStudySubjects');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
     </form>   
    </c:when>
    <c:otherwise>
     <form action='RestoreStudySubject?action=submit&id=<c:out value="${studySub.id}"/>&subjectId=<c:out value="${studySub.subjectId}"/>&studyId=<c:out value="${studySub.studyId}"/>' method="POST">
       <input type="submit" name="submit" value="<fmt:message key="restore_subject_to_study" bundle="${resword}"/>" class="button_xlong" onClick='return confirm("<fmt:message key="are_you_sure_you_want_to_restore_it" bundle="${resword}"/>");'>
       <input type="button" onclick="confirmCancel('ListStudySubjects');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>

     </form> 
     
    </c:otherwise>
   </c:choose>  
 

<jsp:include page="../include/footer.jsp"/>
