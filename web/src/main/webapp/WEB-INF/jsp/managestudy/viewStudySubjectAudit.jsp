<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<!-- Added to replace Harcode with i18n key -->
<fmt:setBundle basename="org.akaza.openclinica.i18n.audit_events" var="resaudit"/>

<html>
<head>
    <link rel="stylesheet" href="includes/styles.css" type="text/css">
    <script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript.js"></script>
</head>
<jsp:useBean scope="request" id="subject" class="org.akaza.openclinica.bean.submit.SubjectBean"/>
<jsp:useBean scope="request" id="study" class="org.akaza.openclinica.bean.managestudy.StudyBean"/>
<jsp:useBean scope="request" id="studySub" class="org.akaza.openclinica.bean.managestudy.StudySubjectBean"/>
<jsp:useBean scope="request" id="events" class="java.util.ArrayList"/>
<jsp:useBean scope="request" id="eventCRFAudits" class="java.util.ArrayList"/>
<jsp:useBean scope="request" id="allDeletedEventCRFs" class="java.util.ArrayList"/>

<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>
<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>

<body>
<!-- Head Anchor-->
<a name="root"></a>

<form action="ExportExcelStudySubjectAuditLog">
<input type="hidden" value="<c:out value="${id}"/>" name="id"/><br>    

<h1><span class="title_manage">
    <c:out value="${studySub.label}"/> <fmt:message key="audit_logs" bundle="${resword}"/></span>
    <input type="image" src="images/bt_Download.gif"  border="0" width="24 " height="15" alt="<fmt:message key="download_spreadsheet" bundle="${resword}"/>" title="<fmt:message key="download_spreadsheet" bundle="${resword}"/>"/><br>   
</h1>
</form>

<%-- Subject Summary --%>
<table border="0" cellpadding="0" cellspacing="0" width="650" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
    <tr>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="study_subject_ID" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="secondary_subject_ID" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date_of_birth" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="person_ID" bundle="${resword}"/></b></td>
        <!--  <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date_record_created" bundle="${resword}"/></b></td> -->
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="created_by" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="status" bundle="${resword}"/></b></td>
    </tr>
    <tr>
        <td class="table_header_column"><c:out value="${studySub.label}"/></td>
        <td class="table_header_column"><c:out value="${studySub.secondaryLabel}"/></td>
        <td class="table_header_column"><fmt:formatDate value="${subject.dateOfBirth}" pattern="${dteFormat}" /></td>
        <td class="table_header_column"><c:out value="${subject.uniqueIdentifier}"/></td>
        <!--   <td class="table_header_column"><fmt:formatDate value="${studySub.createdDate}" pattern="${dteFormat}"/></td> -->
        <td class="table_header_column"><c:out value="${studySub.owner.name}"/></td>
        <td class="table_header_column">
            <!-- HARDCODE <c:out value="${studySub.status.name}"/>-->
            <c:choose>
                <c:when test="${studySub.status.id eq '1'}">
                    <fmt:message key="__available" bundle="${resaudit}"/>
                </c:when>
                <c:when test="${studySub.status.id eq '2'}">
                    <fmt:message key="__unavailable" bundle="${resaudit}"/>
                </c:when>
                <c:when test="${studySub.status.id eq '3'}">
                    <fmt:message key="__private" bundle="${resaudit}"/>
                </c:when>
                <c:when test="${studySub.status.id eq '4'}">
                    <fmt:message key="__pending" bundle="${resaudit}"/>
                </c:when>
                <c:when test="${studySub.status.id eq '5'}">
                    <fmt:message key="__removed" bundle="${resaudit}"/>
                </c:when>
                <c:when test="${studySub.status.id eq '6'}">
                    <fmt:message key="__locked" bundle="${resaudit}"/>
                </c:when>
                <c:when test="${studySub.status.id eq '7'}">
                    <fmt:message key="__auto-removed" bundle="${resaudit}"/>
                </c:when>
                <c:when test="${studySub.status.id eq '8'}">
                    <fmt:message key="__signed" bundle="${resaudit}"/>
                </c:when>
                <c:when test="${studySub.status.id eq '9'}">
                    <fmt:message key="__frozen" bundle="${resaudit}"/>
                </c:when>
                <c:when test="${studySub.status.id eq '10'}">
                    <fmt:message key="__source_data_verification" bundle="${resaudit}"/>
                </c:when>
                <c:otherwise><c:out value="${studySub.status.name}"/></c:otherwise>
            </c:choose>
        </td>

    </tr>
</table><br><br>

        <!-- excel encoding -->
    
<%-- Subject Audit Events --%>
<table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
    <tr>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="audit_event" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date_time_of_server" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="user" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="value_type" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="old" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="new" bundle="${resword}"/></b></td>
    </tr>
    <c:forEach var="studySubjectAudit" items="${studySubjectAudits}">
        <tr>
            <td class="table_header_column">
                <!-- HARDCODE 
                <c:out value="${studySubjectAudit.auditEventTypeName}"/>&nbsp;
                -->
                <c:choose>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '1'}">
                        <fmt:message key="__item_data_value_updated" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '2'}">
                        <fmt:message key="__study_subject_created" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '3'}">
                        <fmt:message key="__study_subject_status_changed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '4'}">
                        <fmt:message key="__study_subject_value_changed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '5'}">
                        <fmt:message key="__subject_created" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '6'}">
                        <fmt:message key="__subject_status_changed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '7'}">
                        <fmt:message key="__subject_global_value_changed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '8'}">
                        <fmt:message key="__event_crf_marked_complete" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '9'}">
                        <fmt:message key="__event_crf_properties_changed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '10'}">
                        <fmt:message key="__event_crf_initial_data_entry_complete" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '11'}">
                        <fmt:message key="__event_crf_double_data_entry_complete" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '12'}">
                        <fmt:message key="__item_data_status_changed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '13'}">
                        <fmt:message key="__item_data_deleted" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '14'}">
                        <fmt:message key="__event_crf_complete_with_password" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '15'}">
                        <fmt:message key="__event_crf_initial_data_entry_complete_with_password" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '16'}">
                        <fmt:message key="__event_crf_double_data_entry_complete_with_password" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '17'}">
                        <fmt:message key="__study_event_scheduled" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '18'}">
                        <fmt:message key="__study_event_data_entry_started" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '19'}">
                        <fmt:message key="__study_event_completed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '20'}">
                        <fmt:message key="__study_event_stopped" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '21'}">
                        <fmt:message key="__study_event_skipped" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '22'}">
                        <fmt:message key="__study_event_locked" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '23'}">
                        <fmt:message key="__study_event_removed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '24'}">
                        <fmt:message key="__study_event_start_date_changed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '25'}">
                        <fmt:message key="__study_event_end_date_changed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '26'}">
                        <fmt:message key="__study_event_location_changed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '27'}">
                        <fmt:message key="__subject_site_assignment" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '28'}">
                        <fmt:message key="__subject_group_assignment" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '29'}">
                        <fmt:message key="__subject_group_changed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '30'}">
                        <fmt:message key="__item_data_inserted_for_repeating_row" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '31'}">
                        <fmt:message key="__study_event_signed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '32'}">
                        <fmt:message key="__event_crf_sdv_status" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '33'}">
                        <fmt:message key="__change_crf_version" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.auditEventTypeId eq '35'}">
                        <fmt:message key="__study_event_restored" bundle="${resaudit}"/>
                    </c:when>
                    
                    <c:otherwise><c:out value="${studySubjectAudit.auditEventTypeId}"/></c:otherwise>
                </c:choose>
                
            </td>
            <!-- YW 12-06-2007, use dateStyle and timeStyle to display datetime -->
            <td class="table_header_column"><fmt:formatDate value="${studySubjectAudit.auditDate}" type="both" pattern="${dtetmeFormat}" timeStyle="short"/>&nbsp;</td>
            <td class="table_header_column"><c:out value="${studySubjectAudit.userName}"/>&nbsp;</td>
            <td class="table_header_column">
                <!-- HARDCODE 
                <c:out value="${studySubjectAudit.entityName}"/>&nbsp;
                -->
                <c:choose>
                    <c:when test="${studySubjectAudit.entityName eq 'Status'}">
                        <fmt:message key="__status" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.entityName eq 'Enrollment Date'}">
                        <fmt:message key="__enrollment_date" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.entityName eq 'Study id'}">
                        <fmt:message key="__study_id" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.entityName eq 'Date of Birth'}">
                        <fmt:message key="__date_of_birth" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.entityName eq 'Person ID'}">
                        <fmt:message key="__person_id" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.entityName eq 'Secondary Subject ID'}">
                        <fmt:message key="__secondary_subject_id" bundle="${resaudit}"/>
                    </c:when>
                    <c:otherwise><c:out value="${studySubjectAudit.entityName}"/></c:otherwise>
                </c:choose>
            </td>
            <td class="table_header_column">
                <!-- HARDCODE 
                <c:out value="${studySubjectAudit.oldValue}"/>&nbsp;
                -->
                <c:choose>
                    <c:when test="${studySubjectAudit.oldValue eq 'available'}">
                        <fmt:message key="__available" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.oldValue eq 'signed'}">
                        <fmt:message key="__signed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.oldValue eq 'Adult'}">
                        <fmt:message key="__adult" bundle="${resaudit}"/>
                    </c:when>
                </c:choose>
                
            </td>
            <td class="table_header_column">
                <!-- HARDCODE 
                <c:out value="${studySubjectAudit.newValue}"/>&nbsp;
                -->
                
                <c:choose>
                    <c:when test="${studySubjectAudit.newValue eq 'available'}">
                        <fmt:message key="__available" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.newValue eq 'signed'}">
                        <fmt:message key="__signed" bundle="${resaudit}"/>
                    </c:when>
                    <c:when test="${studySubjectAudit.newValue eq 'Adult'}">
                        <fmt:message key="__adult" bundle="${resaudit}"/>
                    </c:when>
                </c:choose>
            </td>

        </tr>
    </c:forEach>
</table>
<br>
<%-- Study Events--%>
<table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
    <tr>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="study_events" bundle="${resword}"/></b><br></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="location" bundle="${resword}"/></b><br></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date" bundle="${resword}"/></b><br></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="occurrence_number" bundle="${resword}"/></b><br></td>
    </tr>
    <c:forEach var="event" items="${events}">
        <tr>
            <!-- Link to Dynamic Anchor -->
            <td class="table_header_column"><a href="#<c:out value="${event.studyEventDefinition.name}"/><c:out value="${event.sampleOrdinal}"/>"><c:out value="${event.studyEventDefinition.name}"/>&nbsp;</a></td>
            <%-- <td class="table_header_column"><c:out value="${event.studyEventDefinition.name}"/>&nbsp;</td> --%>
            <td class="table_header_column"><c:out value="${event.location}"/>&nbsp;</td>
            <c:choose>
                <c:when test="${event.startTimeFlag=='false'}">
                    <td class="table_header_column"><fmt:formatDate value="${event.dateStarted}" pattern="${dteFormat}"/>&nbsp;</td>
                </c:when>
                <c:otherwise>
                    <td class="table_header_column"><fmt:formatDate value="${event.dateStarted}" type="both" pattern="${dteFormat}" timeStyle="short"/>&nbsp;</td>
                </c:otherwise>
            </c:choose>
            <td class="table_header_column"><c:out value="${event.sampleOrdinal}"/>&nbsp;</td>
        </tr>
    </c:forEach>
</table>
<br>
<c:forEach var="event" items="${events}">
<%-- Study Event Summary --%>
<!-- Embedded Anchor -->
<a name="<c:out value="${event.studyEventDefinition.name}"/><c:out value="${event.sampleOrdinal}"/>"></a>
<table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
<tr>
    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="name" bundle="${resword}"/></b></td>
    <td class="table_header_column_top" style="color: #789EC5"><b><c:out value="${event.studyEventDefinition.name}"/></b>&nbsp;</td>
</tr>
<tr>
    <td class="table_header_column">
        <!-- HARDCODE <c:out value="Location"/> -->
        <fmt:message key="__location" bundle="${resaudit}"/>
    </td>
    <td class="table_header_column"><c:out value="${event.location}"/>&nbsp;</td>
</tr>
<tr>
    
    <td class="table_header_column">
        <!-- HARDCODE <c:out value="Start Date"/> -->
        <fmt:message key="__start_date" bundle="${resaudit}"/>
    </td>
    <c:choose>
        <c:when test="${event.startTimeFlag=='false'}">
            <td class="table_header_column"><fmt:formatDate value="${event.dateStarted}" pattern="${dteFormat}"/>&nbsp;</td>
        </c:when>
        <c:otherwise>
            <td class="table_header_column"><fmt:formatDate value="${event.dateStarted}" type="both" pattern="${dteFormat}" timeStyle="short"/>&nbsp;</td>
        </c:otherwise>
    </c:choose>
</tr>
<tr>
    <td class="table_header_column">
        <!-- HARDCODE <c:out value="Status"/> -->
        <fmt:message key="__status" bundle="${resaudit}"/>
    </td>
    <td class="table_header_column">
        <!-- HARDCODE <c:out value="${event.subjectEventStatus.name}"/>&nbsp;-->
        <c:choose>
            <c:when test="${event.subjectEventStatus.id eq '0'}">
                <!-- HARDCODE invalid-->
                <fmt:message key="__invalid" bundle="${resaudit}"/>
            </c:when>   
            <c:when test="${event.subjectEventStatus.id eq '1'}">
                <!-- HARDCODE scheduled-->
                <fmt:message key="__scheduled" bundle="${resaudit}"/>
            </c:when>
            <c:when test="${event.subjectEventStatus.id eq '2'}">
                <!-- HARDCODE not_scheduled-->
                <fmt:message key="__not_scheduled" bundle="${resaudit}"/>
            </c:when>
            <c:when test="${event.subjectEventStatus.id eq '3'}">
                <!-- HARDCODE data_entry_started-->
                <fmt:message key="__data_entry_started" bundle="${resaudit}"/>
            </c:when>
            <c:when test="${event.subjectEventStatus.id eq '4'}">
                <!-- HARDCODE completed-->
                <fmt:message key="__completed" bundle="${resaudit}"/>
            </c:when>
            <c:when test="${event.subjectEventStatus.id eq '5'}">
                <!-- HARDCODE stopped-->
                <fmt:message key="__stopped" bundle="${resaudit}"/>
            </c:when>
            <c:when test="${event.subjectEventStatus.id eq '6'}">
                <!-- HARDCODE skipped-->
                <fmt:message key="__skipped" bundle="${resaudit}"/>
            </c:when>
            <c:when test="${event.subjectEventStatus.id eq '7'}">
                <!-- HARDCODE locked-->
                <fmt:message key="__locked" bundle="${resaudit}"/>
            </c:when>
            <c:when test="${event.subjectEventStatus.id eq '8'}">
                <!-- HARDCODE signed-->
                <fmt:message key="__signed" bundle="${resaudit}"/>
            </c:when>
            <c:otherwise>
                <c:out value="${event.subjectEventStatus.name}"/>&nbsp;
            </c:otherwise>
        </c:choose>
    </td>
</tr>
<tr>
    <td class="table_header_column"><fmt:message key="occurrence_number" bundle="${resword}"/></td>
    <td class="table_header_column"><c:out value="${event.sampleOrdinal}"/>&nbsp;</td>
</tr>
<tr>
<tr><td colspan="2">&nbsp;</td></tr>
<tr>
    <td colspan="2">
            <%--Audit for deleted event crfs --%>
        <table border="0"><tr><td width="20">&nbsp;</td><td><%-- Margin --%>
            <table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
                <tr>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="name" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="version" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="deleted_by" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="delete_date" bundle="${resword}"/></b></td>
                </tr>
                <c:forEach var="deletedEventCRF" items="${allDeletedEventCRFs}">
                    <c:if test="${deletedEventCRF.studyEventId==event.id}">

                        <tr>
                            <td class="table_header_column"><c:out value="${deletedEventCRF.crfName}"/>&nbsp;</td>
                            <td class="table_header_column"><c:out value="${deletedEventCRF.crfVersion}"/>&nbsp;</td>
                            <td class="table_header_column"><c:out value="${deletedEventCRF.deletedBy}"/>&nbsp;</td>
                            <td class="table_header_column"><fmt:formatDate value="${deletedEventCRF.deletedDate}" type="both" pattern="${dteFormat}" timeStyle="short"/>&nbsp;</td>
                        </tr>
                    </c:if>
                </c:forEach>
            </table><%-- Event CRFs Audit Events --%>
        </td></tr></table><%-- Margin --%>

    </td>
</tr>

<tr><td colspan="2">&nbsp;</td></tr>
<tr>
    <td colspan="2">
            <%--Audit Events for Study Event --%>
        <table border="0"><tr><td width="20">&nbsp;</td><td><%-- Margin --%>
            <table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
                <tr>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="audit_event" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date_time_of_server" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="user" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="value_type" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="old" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="new" bundle="${resword}"/></b></td>
                </tr>

                <c:forEach var="studyEventAudit" items="${studyEventAudits}">
                    <c:if test="${studyEventAudit.entityId==event.id}">
                        <tr>
                            
                            <td class="table_header_column">
                                <!-- HARDCODE 
                                    <c:out value="${studyEventAudit.auditEventTypeName}"/>&nbsp;
                                -->
                                
                                <c:choose>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '1'}">
                                        <fmt:message key="__item_data_value_updated" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '2'}">
                                        <fmt:message key="__study_subject_created" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '3'}">
                                        <fmt:message key="__study_subject_status_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '4'}">
                                        <fmt:message key="__study_subject_value_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '5'}">
                                        <fmt:message key="__subject_created" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '6'}">
                                        <fmt:message key="__subject_status_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '7'}">
                                        <fmt:message key="__subject_global_value_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '8'}">
                                        <fmt:message key="__event_crf_marked_complete" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '9'}">
                                        <fmt:message key="__event_crf_properties_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '10'}">
                                        <fmt:message key="__event_crf_initial_data_entry_complete" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '11'}">
                                        <fmt:message key="__event_crf_double_data_entry_complete" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '12'}">
                                        <fmt:message key="__item_data_status_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '13'}">
                                        <fmt:message key="__item_data_deleted" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '14'}">
                                        <fmt:message key="__event_crf_complete_with_password" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '15'}">
                                        <fmt:message key="__event_crf_initial_data_entry_complete_with_password" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '16'}">
                                        <fmt:message key="__event_crf_double_data_entry_complete_with_password" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '17'}">
                                        <fmt:message key="__study_event_scheduled" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '18'}">
                                        <fmt:message key="__study_event_data_entry_started" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '19'}">
                                        <fmt:message key="__study_event_completed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '20'}">
                                        <fmt:message key="__study_event_stopped" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '21'}">
                                        <fmt:message key="__study_event_skipped" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '22'}">
                                        <fmt:message key="__study_event_locked" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '23'}">
                                        <fmt:message key="__study_event_removed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '24'}">
                                        <fmt:message key="__study_event_start_date_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '25'}">
                                        <fmt:message key="__study_event_end_date_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '26'}">
                                        <fmt:message key="__study_event_location_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '27'}">
                                        <fmt:message key="__subject_site_assignment" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '28'}">
                                        <fmt:message key="__subject_group_assignment" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '29'}">
                                        <fmt:message key="__subject_group_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '30'}">
                                        <fmt:message key="__item_data_inserted_for_repeating_row" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '31'}">
                                        <fmt:message key="__study_event_signed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '32'}">
                                        <fmt:message key="__event_crf_sdv_status" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '33'}">
                                        <fmt:message key="__change_crf_version" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.auditEventTypeId eq '35'}">
                                        <fmt:message key="__study_event_restored" bundle="${resaudit}"/>
                                    </c:when>
                                    
                                    <c:otherwise><c:out value="${studyEventAudit.auditEventTypeId}"/></c:otherwise>
                                </c:choose>

                            </td>
                            <td class="table_header_column"><fmt:formatDate value="${studyEventAudit.auditDate}" type="both" pattern="${dtetmeFormat}" timeStyle="short"/>&nbsp;</td>
                            <td class="table_header_column"><c:out value="${studyEventAudit.userName}"/>&nbsp;</td>
                    
                            <td class="table_header_column">
                                <!-- HARDCODE 
                                    <c:out value="${studySubjectAudit.entityName}"/>&nbsp;
                                -->
                                <c:choose>
                                    <c:when test="${studyEventAudit.entityName eq 'Status'}">
                                        <fmt:message key="__status" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.entityName eq 'Enrollment Date'}">
                                        <fmt:message key="__enrollment_date" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.entityName eq 'Study id'}">
                                        <fmt:message key="__study_id" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.entityName eq 'Date of Birth'}">
                                        <fmt:message key="__date_of_birth" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${studyEventAudit.entityName eq 'Person ID'}">
                                        <fmt:message key="__person_id" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:otherwise><c:out value="${studyEventAudit.entityName}"/></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="table_header_column">
                                        <c:choose>
                                            <%--BWP issue 3300; 02/24/2009: The displayed old value is the SubjectEventStatus id;
                                the new value is the Status id (both objects in a StudyEventBean) --%>
                                            <c:when test="${studyEventAudit.oldValue eq '0'}">
                                                <!-- HARDCODE invalid-->
                                                <fmt:message key="__invalid" bundle="${resaudit}"/>
                                            </c:when>   
                                            <c:when test="${studyEventAudit.oldValue eq '1'}">
                                                <!-- HARDCODE scheduled-->
                                                <fmt:message key="__scheduled" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '2'}">
                                                <!-- HARDCODE not_scheduled-->
                                                <fmt:message key="__not_scheduled" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '3'}">
                                                <!-- HARDCODE data_entry_started-->
                                                <fmt:message key="__data_entry_started" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '4'}">
                                                <!-- HARDCODE completed-->
                                                <fmt:message key="__completed" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '5'}">
                                                <!-- HARDCODE stopped-->
                                                <fmt:message key="__stopped" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '6'}">
                                                <!-- HARDCODE skipped-->
                                                <fmt:message key="__skipped" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '7'}">
                                                <!-- HARDCODE locked-->
                                                <fmt:message key="__locked" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '8'}">
                                                <!-- HARDCODE signed-->
                                                <fmt:message key="__signed" bundle="${resaudit}"/>
                                            </c:when>

                                            <c:otherwise><c:out value="${studyEventAudit.oldValue}"/></c:otherwise>
                                        </c:choose>
                                &nbsp;</td>
                            <td class="table_header_column">
                                <c:choose>
                                    <%-- A removed Study Event ...--%>
                                    <c:when test="${studyEventAudit.newValue eq '5'}">
                                        <c:choose>
                                            <c:when test="${studyEventAudit.newValue eq '0'}">
                                                <!-- HARDCODE invalid-->
                                                <fmt:message key="__invalid" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '1'}">
                                                <!-- HARDCODE available-->
                                                <fmt:message key="__available" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '2'}">
                                                <!-- HARDCODE pending-->
                                                <fmt:message key="__pending" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '3'}">
                                                <!-- HARDCODE private-->
                                                <fmt:message key="__private" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '4'}">
                                                <!-- HARDCODE unavailable-->
                                                <fmt:message key="__unavailable" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '5'}">
                                                <!-- HARDCODE removed-->
                                                <fmt:message key="__removed" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '6'}">
                                                <!-- HARDCODE locked-->
                                                <fmt:message key="__locked" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '7'}">
                                                <!-- HARDCODE auto-removed-->
                                                <fmt:message key="__auto-removed" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '8'}">
                                                <!-- HARDCODE signed-->
                                                <fmt:message key="__signed" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '9'}">
                                                <!-- HARDCODE frozen-->
                                                <fmt:message key="__frozen" bundle="${resaudit}"/>
                                            </c:when>

                                            <c:otherwise><c:out value="${studyEventAudit.newValue}"/></c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${studyEventAudit.newValue eq '0'}">
                                                <!-- HARDCODE invalid-->
                                                <fmt:message key="__invalid" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '1'}">
                                                <!-- HARDCODE scheduled-->
                                                <fmt:message key="__scheduled" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '2'}">
                                                <!-- HARDCODE not_scheduled-->
                                                <fmt:message key="__not_scheduled" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '3'}">
                                                <!-- HARDCODE data_entry_started-->
                                                <fmt:message key="__data_entry_started" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '4'}">
                                                <!-- HARDCODE completed-->
                                                <fmt:message key="__completed" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '5'}">
                                                <!-- HARDCODE stopped-->
                                                <fmt:message key="__stopped" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '6'}">
                                                <!-- HARDCODE skipped-->
                                                <fmt:message key="__skipped" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '7'}">
                                                <!-- HARDCODE locked-->
                                                <fmt:message key="__locked" bundle="${resaudit}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '8'}">
                                                <!-- HARDCODE signed-->
                                                <fmt:message key="__signed" bundle="${resaudit}"/>
                                            </c:when>

                                            <c:otherwise><c:out value="${studyEventAudit.newValue}"/></c:otherwise>
                                        </c:choose>
                                    </c:otherwise>
                                </c:choose>
                                &nbsp;</td>
                        </tr>
                    </c:if>
                </c:forEach>
            </table><%-- Event CRFs Audit Events --%>
        </td></tr></table><%-- Margin --%>

    </td>
</tr>

    <%-- Event CRFs for this Study Event --%>
<c:forEach var="eventCRF" items="${event.eventCRFs}">
    <tr>
        <td colspan="2">
            <table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
                <tr>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="name" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="version" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date_interviewed" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="interviewer_name" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="owner" bundle="${resword}"/></b></td>
                </tr>
                <tr>
                    <td class="table_header_column"><c:out value="${eventCRF.crf.name}"/>&nbsp;</td>
                    <td class="table_header_column"><c:out value="${eventCRF.crfVersion.name}"/>&nbsp;</td>
                    <td class="table_header_column">
                        <fmt:formatDate value="${eventCRF.dateInterviewed}" type="both" pattern="${dteFormat}" timeStyle="short"/>&nbsp;
                        <%--<c:out value="${eventCRF.dateInterviewed}"/>&nbsp;--%>
                    </td>
                    <td class="table_header_column"><c:out value="${eventCRF.interviewerName}"/>&nbsp;</td>
                    <td class="table_header_column"><c:out value="${eventCRF.owner.name}"/>&nbsp;</td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
    <tr><td colspan="2">&nbsp;</td></tr>
    <td colspan="2">

            <%-- Event CRFs Audit Events --%>
        <table border="0"><tr><td width="20">&nbsp;</td><td><%-- Margin --%>
            <table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
                <tr>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="audit_event" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date_time_of_server" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="user" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="value_type" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="old" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="new" bundle="${resword}"/></b></td>
                </tr>

                <c:forEach var="eventCRFAudit" items="${eventCRFAudits}">
                    <c:if test="${eventCRFAudit.eventCRFId==eventCRF.id}">
                        <tr>
                            <!-- HARDCODE -->
                            <td class="table_header_column">
                                <!-- HARDCODE 
                                    <c:out value="${eventCRFAudit.auditEventTypeName}"/>&nbsp;
                                -->
                                <c:choose>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '1'}">
                                        <fmt:message key="__item_data_value_updated" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '2'}">
                                        <fmt:message key="__study_subject_created" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '3'}">
                                        <fmt:message key="__study_subject_status_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '4'}">
                                        <fmt:message key="__study_subject_value_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '5'}">
                                        <fmt:message key="__subject_created" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '6'}">
                                        <fmt:message key="__subject_status_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '7'}">
                                        <fmt:message key="__subject_global_value_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '8'}">
                                        <fmt:message key="__event_crf_marked_complete" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '9'}">
                                        <fmt:message key="__event_crf_properties_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '10'}">
                                        <fmt:message key="__event_crf_initial_data_entry_complete" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '11'}">
                                        <fmt:message key="__event_crf_double_data_entry_complete" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '12'}">
                                        <fmt:message key="__item_data_status_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '13'}">
                                        <fmt:message key="__item_data_deleted" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '14'}">
                                        <fmt:message key="__event_crf_complete_with_password" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '15'}">
                                        <fmt:message key="__event_crf_initial_data_entry_complete_with_password" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '16'}">
                                        <fmt:message key="__event_crf_double_data_entry_complete_with_password" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '17'}">
                                        <fmt:message key="__study_event_scheduled" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '18'}">
                                        <fmt:message key="__study_event_data_entry_started" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '19'}">
                                        <fmt:message key="__study_event_completed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '20'}">
                                        <fmt:message key="__study_event_stopped" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '21'}">
                                        <fmt:message key="__study_event_skipped" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '22'}">
                                        <fmt:message key="__study_event_locked" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '23'}">
                                        <fmt:message key="__study_event_removed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '24'}">
                                        <fmt:message key="__study_event_start_date_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '25'}">
                                        <fmt:message key="__study_event_end_date_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '26'}">
                                        <fmt:message key="__study_event_location_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '27'}">
                                        <fmt:message key="__subject_site_assignment" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '28'}">
                                        <fmt:message key="__subject_group_assignment" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '29'}">
                                        <fmt:message key="__subject_group_changed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '30'}">
                                        <fmt:message key="__item_data_inserted_for_repeating_row" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '31'}">
                                        <fmt:message key="__study_event_signed" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '32'}">
                                        <fmt:message key="__event_crf_sdv_status" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '33'}">
                                        <fmt:message key="__change_crf_version" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.auditEventTypeId eq '35'}">
                                        <fmt:message key="__study_event_restored" bundle="${resaudit}"/>
                                    </c:when>
                                    
                                    <c:otherwise><c:out value="${eventCRFAudit.auditEventTypeId}"/></c:otherwise>
                                </c:choose>
                                
                            </td>
                            <td class="table_header_column"><fmt:formatDate value="${eventCRFAudit.auditDate}" type="both" pattern="${dtetmeFormat}" timeStyle="short"/>&nbsp;</td>
                            <td class="table_header_column"><c:out value="${eventCRFAudit.userName}"/>&nbsp;</td>
                            <!-- HARDCODE -->
                            <td class="table_header_column">
                                <!-- HARDCODE 
                                    <c:out value="${eventCRFAudit.entityName}"/>&nbsp;
                                -->
                                <c:choose>
                                    <c:when test="${eventCRFAudit.entityName eq 'Status'}">
                                        <fmt:message key="__status" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.entityName eq 'Enrollment Date'}">
                                        <fmt:message key="__enrollment_date" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.entityName eq 'Study id'}">
                                        <fmt:message key="__study_id" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.entityName eq 'Date of Birth'}">
                                        <fmt:message key="__date_of_birth" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:when test="${eventCRFAudit.entityName eq 'Person ID'}">
                                        <fmt:message key="__person_id" bundle="${resaudit}"/>
                                    </c:when>
                                    <c:otherwise><c:out value="${eventCRFAudit.entityName}"/></c:otherwise>
                                </c:choose>
                                &nbsp;(<c:out value="${eventCRFAudit.ordinal}"/>)


                            </td>
                            <td class="table_header_column">
                                
                                <c:choose>
                                    <c:when test='${eventCRFAudit.auditEventTypeId == 12 or eventCRFAudit.entityName eq "Status"}'>
                                        <c:if test="${eventCRFAudit.oldValue eq '0'}">
                                            <!-- HARDCODE invalid-->
                                            <fmt:message key="__invalid" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '1'}">
                                            <!-- HARDCODE available-->
                                            <fmt:message key="__available" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '2'}">
                                            <!-- HARDCODE unavailable-->
                                            <fmt:message key="__unavailable" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '3'}">
                                            <!-- HARDCODE private-->
                                            <fmt:message key="__private" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '4'}">
                                            <!-- HARDCODE pending-->
                                            <fmt:message key="__pending" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '5'}">
                                            <!-- HARDCODE removed-->
                                            <fmt:message key="__removed" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '6'}">
                                            <!-- HARDCODE locked-->
                                            <fmt:message key="__locked" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '7'}">
                                            <!-- HARDCODE auto-removed-->
                                            <fmt:message key="__auto-removed" bundle="${resaudit}"/>
                                        </c:if>
                                    </c:when>
                                    <c:when test='${eventCRFAudit.auditEventTypeId == 32}' >
                                        <c:choose>
                                        <c:when test="${eventCRFAudit.oldValue eq '1'}">
                                            <!-- HARDCODE TRUE-->
                                            <fmt:message key="__true" bundle="${resaudit}"/>
                                        </c:when>
                                        <c:when test="${eventCRFAudit.oldValue eq '0'}">
                                            <!-- HARDCODE FALSE-->
                                            <fmt:message key="__false" bundle="${resaudit}"/>
                                        </c:when>
                                        <c:when test="${eventCRFAudit.oldValue eq 'true'}">
                                            <!-- HARDCODE FALSE-->
                                            <fmt:message key="__true_lwrcase" bundle="${resaudit}"/>
                                        </c:when>
                                        <c:when test="${eventCRFAudit.oldValue eq 'false'}">
                                            <!-- HARDCODE FALSE-->
                                            <fmt:message key="__false__lwrcase" bundle="${resaudit}"/>
                                        </c:when>
                                        <c:otherwise><c:out value="${eventCRFAudit.oldValue}"/></c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${eventCRFAudit.itemDataTypeId == 11}">
                                                <c:set var="path" value="${eventCRFAudit.oldValue}"/>
                                                <c:set var="sep" value="\\"/>
                                                <c:set var="sep2" value="\\\\"/>
                                                <a href="DownloadAttachedFile?eventCRFId=<c:out value="${eventCRFAudit.eventCRFId}"/>&fileName=${fn:replace(fn:replace(path,'+','%2B'),sep,sep2)}"><c:out value="${eventCRFAudit.oldValue}"/></a>
                                            </c:when>
                                            <c:otherwise><c:out value="${eventCRFAudit.oldValue}"/></c:otherwise>
                                        </c:choose>
                                    </c:otherwise>
                                </c:choose>
                                &nbsp;</td>
                            <td class="table_header_column">
                                <c:choose>
                                    <c:when test='${eventCRFAudit.auditEventTypeId == 12 or eventCRFAudit.entityName eq "Status"}'>
                                        <c:if test="${eventCRFAudit.newValue eq '0'}">
                                            <!-- HARDCODE invalid-->
                                            <fmt:message key="__invalid" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '1'}">
                                            <!-- HARDCODE available-->
                                            <fmt:message key="__available" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '2'}">
                                            <!-- HARDCODE unavailable-->
                                            <fmt:message key="__unavailable" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '3'}">
                                            <!-- HARDCODE private-->
                                            <fmt:message key="__private" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '4'}">
                                            <!-- HARDCODE pending-->
                                            <fmt:message key="__pending" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '5'}">
                                            <!-- HARDCODE removed-->
                                            <fmt:message key="__removed" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '6'}">
                                            <!-- HARDCODE locked-->
                                            <fmt:message key="__locked" bundle="${resaudit}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '7'}">
                                            <!-- HARDCODE auto-removed-->
                                            <fmt:message key="__auto-removed" bundle="${resaudit}"/>
                                        </c:if>
                                    </c:when>
                                    <c:when test='${eventCRFAudit.auditEventTypeId == 32}' >
                                        <c:choose>
                                        <c:when test="${eventCRFAudit.newValue eq '1'}">
                                            <!-- HARDCODE TRUE-->
                                            <fmt:message key="__true" bundle="${resaudit}"/>
                                        </c:when>
                                        <c:when test="${eventCRFAudit.newValue eq '0'}">
                                            <!-- HARDCODE FALSE-->
                                            <fmt:message key="__false" bundle="${resaudit}"/>
                                        </c:when>
                                        <c:when test="${eventCRFAudit.newValue eq 'true'}">
                                            <!-- HARDCODE FALSE-->
                                            <fmt:message key="__true_lwrcase" bundle="${resaudit}"/>
                                        </c:when>
                                        <c:when test="${eventCRFAudit.newValue eq 'false'}">
                                            <!-- HARDCODE FALSE-->
                                            <fmt:message key="__false__lwrcase" bundle="${resaudit}"/>
                                        </c:when>
                                        <c:otherwise><c:out value="${eventCRFAudit.newValue}"/></c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${eventCRFAudit.itemDataTypeId == 11}">
                                                <c:set var="path" value="${eventCRFAudit.newValue}"/>
                                                <c:set var="sep" value="\\"/>
                                                <c:set var="sep2" value="\\\\"/>
                                                <a href="DownloadAttachedFile?eventCRFId=<c:out value="${eventCRFAudit.eventCRFId}"/>&fileName=${fn:replace(fn:replace(path,'+','%2B'),sep,sep2)}"><c:out value="${eventCRFAudit.newValue}"/></a>
                                            </c:when>
                                            <c:otherwise><c:out value="${eventCRFAudit.newValue}"/></c:otherwise>
                                        </c:choose>
                                    </c:otherwise>
                                </c:choose>
                                &nbsp;</td>
                        </tr>
                    </c:if>
                </c:forEach>
            </table><%-- Event CRFs Audit Events --%>
        </td></tr></table><%-- Margin --%>
    </td>
    </tr>
    <!-- Return to Root -->
    <tr><td colspan="2" class="table_header_column_top" style="color: #789EC5"><a href="#root"><fmt:message key="return_to_top" bundle="${resword}"/></a>&nbsp;</td></tr>
    

</c:forEach>
</table>
<br>
</c:forEach>
<hr>
</body>