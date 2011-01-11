<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<jsp:include page="../include/managestudy-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean' />

<jsp:useBean scope='request' id='studySubjects' class='java.util.ArrayList' />
<jsp:useBean scope='request' id='studySubjectAudits' class='java.util.HashMap' />
<%--
<jsp:useBean scope='request' id='eventCRFAudits' class='java.util.HashMap' />
--%>
<jsp:useBean scope='request' id='events' class='java.util.HashMap' />
<jsp:useBean scope='request' id='subjects' class='java.util.HashMap' />

<%-- BWP>> for formatting dates --%>
<c:set var="dateFormatPattern" value="${requestScope['dateFormatPattern']}" />
<c:set var="dateTimeFormatPattern" value="${requestScope['dateTimeFormatPattern']}" />
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>
<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>

<h1><span class="title_manage">
<fmt:message key="view_study_log_for" bundle="${resword}"/> <c:out value="${study.name}"/>
</span></h1>

<jsp:include page="../include/alertbox.jsp" />

<%-- for each here --%>
<c:forEach var="studySubject" items="${studySubjects}">

<c:set var="subject" value="${subjects[studySubject.id]}"/>

<%-- subject summary, taken from view study subject audit --%>
<table border="0" cellpadding="0" cellspacing="0" width="650" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">				
	<tr>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="study_subject_ID" bundle="${resword}"/></b></td>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="secondary_subject_ID" bundle="${resword}"/></b></td>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date_of_birth" bundle="${resword}"/></b></td>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="person_ID" bundle="${resword}"/></b></td>
	 <!--  <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date_record_created" bundle="${resword}"/></b></td>-->
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="created_by" bundle="${resword}"/></b></td>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="status" bundle="${resword}"/></b></td>
	</tr>
	<tr>
	 <td class="table_header_column"><c:out value="${studySubject.label}"/></td>
	 <td class="table_header_column"><c:out value="${studySubject.secondaryLabel}"/></td>
	 <td class="table_header_column"><fmt:formatDate value="${subject.dateOfBirth}" pattern="${dteFormat}"/></td>
	 <td class="table_header_column"><c:out value="${subject.uniqueIdentifier}"/></td>
	 <!--  <td class="table_header_column"><fmt:formatDate value="${studySubject.createdDate}" pattern="${dteFormat}"/></td> -->
	 <td class="table_header_column"><c:out value="${studySubject.owner.name}"/></td>
	 <td class="table_header_column"><c:out value="${subject.status.name}"/></td>
	 	
	</tr> 
</table>

<c:set var="studySubjectAuditList" value="${studySubjectAudits[studySubject.id]}"/>

<%-- end of one line table, add 'expanding tables' below --%>
<%-- Subject Audit Events --%>

<a href="javascript:leftnavExpand('logs<c:out value="${studySubject.id}"/>');javascript:setImage('ExpandGroup4','images/bt_Collapse.gif');">
<img name="ExpandGroup4" src="images/bt_Expand.gif" border="0">
<fmt:message key="recent_activity_log" bundle="${restext}"/>: <c:out value="${studySubject.name}"/></a></a>

<div id="logs<c:out value="${studySubject.id}"/>" style="display:none">
 <div style="width: 600px">
	<!-- These DIVs define shaded box borders -->
		<div class="box_T">
		<div class="box_L">
		<div class="box_R">
		<div class="box_B">
		<div class="box_TL">
		<div class="box_TR">
		<div class="box_BL">
		<div class="box_BR">

			<div class="tablebox_center">


<table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">				
	<tr>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="audit_event" bundle="${resword}"/></b></td>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date_time_of_server" bundle="${resword}"/></b></td>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="user" bundle="${resword}"/></b></td>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="value_type" bundle="${resword}"/></b></td>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="old" bundle="${resword}"/></b></td>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="new" bundle="${resword}"/></b></td>
	</tr>
	<c:forEach var="studySubjectAudit" items="${studySubjectAuditList}">
	<tr>
	 <td class="table_header_column"><c:out value="${studySubjectAudit.auditEventTypeName}"/>&nbsp;</td>
	 <td class="table_header_column"><fmt:formatDate value="${studySubjectAudit.auditDate}" pattern="${dtetmeFormat}"/>&nbsp;</td>
	 <td class="table_header_column"><c:out value="${studySubjectAudit.userName}"/>&nbsp;</td>
	 <td class="table_header_column"><c:out value="${studySubjectAudit.entityName}"/>&nbsp;</td> 
	 <td class="table_header_column"><c:out value="${studySubjectAudit.oldValue}"/>&nbsp;</td>
	 <td class="table_header_column"><c:out value="${studySubjectAudit.newValue}"/>&nbsp;</td>
	 	
	</tr>
	</c:forEach> 
</table>
<br>

<c:set var="eventList" value="${events[studySubject.id]}"/>

<%-- Study Events--%>

<table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
	<tr>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="study_events" bundle="${resword}"/></b><br></td>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="location" bundle="${resword}"/></b><br></td>
	 <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date" bundle="${resword}"/></b><br></td>
	</tr>
	<c:forEach var="event" items="${eventList}">
		<tr>
		 <td class="table_header_column"><c:out value="${event.studyEventDefinition.name}"/>&nbsp;</td>
		 <td class="table_header_column"><c:out value="${event.location}"/>&nbsp;</td>
		 <td class="table_header_column"><fmt:formatDate value="${event.dateStarted}" pattern="${dteFormat}"/>&nbsp;</td>
		</tr>
	</c:forEach> 
</table>
<br>

</div></div></div></div></div></div></div></div></div></div></div>

<br/>
<a href="javascript:openDocWindow('ViewStudySubjectAuditLog?id=<c:out value="${studySubject.id}"/>')"><b><fmt:message key="complete_audit_log_for" bundle="${resword}"/> <c:out value="${studySubject.name}"/></a>
<%-- end for each --%>
<br/><br/>
</c:forEach>



<jsp:include page="../include/footer.jsp"/>
