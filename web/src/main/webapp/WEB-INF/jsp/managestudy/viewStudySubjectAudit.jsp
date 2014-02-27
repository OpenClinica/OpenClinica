<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

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
        <td class="table_header_column"><c:out value="${studySub.status.name}"/></td>

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
            <c:set var="string1" value="${studySubjectAudit.auditEventTypeName}"/>
            <c:set var="string2" value="${fn:toLowerCase(fn:substring(string1,0,1))}${fn:substring(string1, 1,fn:length(string1))}"/>
            <td class="table_header_column">
                <fmt:message  key="${fn:replace(string2,' ','_')}" bundle="${resword}"/>&nbsp;
            </td>

        
            <!-- YW 12-06-2007, use dateStyle and timeStyle to display datetime -->
            <td class="table_header_column"><fmt:formatDate value="${studySubjectAudit.auditDate}" type="both" pattern="${dtetmeFormat}" timeStyle="short"/>&nbsp;</td>

            <td class="table_header_column"><c:out value="${studySubjectAudit.userName}"/>&nbsp;</td>
            <c:set var="string1" value="${studySubjectAudit.entityName}"/>
            <c:set var="string2" value="${fn:toLowerCase(fn:substring(string1,0,fn:length(string1)))}"/>
            <c:set var="string3" value="${fn:substring(string2,fn:length(string2)-2,fn:length(string2))}"/>
            <td class="table_header_column">
            <c:if test="${string2 != ''}">
                <c:choose>
                    <c:when test="${string3 == 'id'}">
                        <fmt:message  key="${fn:replace(fn:replace(string2,'id','ID'),' ','_')}" bundle="${resword}"/>&nbsp;
                    </c:when>
                    <c:otherwise>
                        <fmt:message  key="${fn:replace(string2,' ','_')}" bundle="${resword}"/>&nbsp;
                    </c:otherwise>
                </c:choose>
            </c:if>
            </td>

            <td class="table_header_column"><c:out value="${studySubjectAudit.oldValue}"/>&nbsp;</td>
            <td class="table_header_column"><c:out value="${studySubjectAudit.newValue}"/>&nbsp;</td>

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
    <td class="table_header_column"><fmt:message key="location" bundle="${resword}"/></td>
    <td class="table_header_column"><c:out value="${event.location}"/>&nbsp;</td>
</tr>
<tr>
    <td class="table_header_column"><fmt:message key="start_date" bundle="${resword}"/></td>
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
    <td class="table_header_column"><fmt:message key="status" bundle="${resword}"/></td>
    <td class="table_header_column"><c:out value="${event.subjectEventStatus.name}"/>&nbsp;</td>
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


            <c:set var="string1" value="${studyEventAudit.auditEventTypeName}"/>
            <c:set var="string2" value="${fn:toLowerCase(fn:substring(string1,0,1))}${fn:substring(string1, 1,fn:length(string1))}"/>
            <td class="table_header_column"><fmt:message  key="${fn:replace(string2,' ','_')}" bundle="${resword}"/>&nbsp;</td>

                            <td class="table_header_column"><fmt:formatDate value="${studyEventAudit.auditDate}" type="both" pattern="${dtetmeFormat}" timeStyle="short"/>&nbsp;</td>
                            <td class="table_header_column"><c:out value="${studyEventAudit.userName}"/>&nbsp;</td>

            <c:set var="string1" value="${studyEventAudit.entityName}"/>
            <c:set var="string2" value="${fn:toLowerCase(fn:substring(string1,0,fn:length(string1)))}"/>
            <td class="table_header_column">
                 <fmt:message  key="${fn:replace(string2,' ','_')}" bundle="${resword}"/>&nbsp;
            </td>               
                            <td class="table_header_column">
                                        <c:choose>
                                            <%--BWP issue 3300; 02/24/2009: The displayed old value is the SubjectEventStatus id;
                                the new value is the Status id (both objects in a StudyEventBean) --%>
                                            <c:when test="${studyEventAudit.oldValue eq '0'}">
                                                <fmt:message  key="invalid" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '1'}">
                                                <fmt:message  key="scheduled" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '2'}">
                                                <fmt:message  key="not_scheduled" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '3'}">
                                                <fmt:message  key="data_entry_started" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '4'}">
                                                <fmt:message  key="completed" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '5'}">
                                                <fmt:message  key="stopped" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '6'}">
                                                <fmt:message  key="skipped" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '7'}">
                                                <fmt:message  key="locked" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '8'}">
                                                <fmt:message  key="signed" bundle="${resword}"/>
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
                                                <fmt:message  key="invalid" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '1'}">
                                                <fmt:message  key="available" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '2'}">
                                                <fmt:message  key="pending" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '3'}">
                                                <fmt:message  key="private" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '4'}">
                                                <fmt:message  key="unavailable" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '5'}">
                                                <fmt:message  key="removed" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '6'}">
                                                <fmt:message  key="locked" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '7'}">
                                                <fmt:message  key="auto-removed" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '8'}">
                                                <fmt:message  key="signed" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '9'}">
                                                <fmt:message  key="frozen" bundle="${resword}"/>
                                            </c:when>

                                            <c:otherwise><c:out value="${studyEventAudit.newValue}"/></c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${studyEventAudit.newValue eq '0'}">
                                                <fmt:message  key="invalid" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '1'}">
                                                <fmt:message  key="scheduled" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '2'}">
                                                <fmt:message  key="not_scheduled" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '3'}">
                                                <fmt:message  key="data_entry_started" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '4'}">
                                                <fmt:message  key="completed" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '5'}">
                                                <fmt:message  key="stopped" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '6'}">
                                                <fmt:message  key="skipped" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '7'}">
                                                <fmt:message  key="locked" bundle="${resword}"/>
                                            </c:when>
                                            <c:when test="${studyEventAudit.newValue eq '8'}">
                                                <fmt:message  key="signed" bundle="${resword}"/>
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

            <c:set var="string1" value="${eventCRFAudit.auditEventTypeName}"/>
            <c:set var="string2" value="${fn:toLowerCase(fn:substring(string1,0,1))}${fn:substring(string1, 1,fn:length(string1))}"/>
            <td class="table_header_column"><fmt:message  key="${fn:replace(string2,' ','_')}" bundle="${resword}"/>&nbsp;</td>

                            <td class="table_header_column"><fmt:formatDate value="${eventCRFAudit.auditDate}" type="both" pattern="${dtetmeFormat}" timeStyle="short"/>&nbsp;</td>
                            <td class="table_header_column"><c:out value="${eventCRFAudit.userName}"/>&nbsp;</td>


            <c:set var="string1" value="${eventCRFAudit.entityName}"/>
            <c:set var="string2" value="${fn:toLowerCase(fn:substring(string1,0,fn:length(string1)))}"/>
            <td class="table_header_column">
                <c:if test="${string2 != ''}">
                    <c:choose>
                        <c:when test="${string2 == 'status'}">
                            <fmt:message  key="${string2}" bundle="${resword}"/>&nbsp;
                        </c:when>
                        <c:otherwise>
                            <c:out value="${string1}"/>
                        </c:otherwise>
                    </c:choose>
                    &nbsp;(<c:out value="${eventCRFAudit.ordinal}"/>)

                </c:if>
            </td>  
                            <td class="table_header_column">
                                <c:choose>
                                    <c:when test='${eventCRFAudit.auditEventTypeId == 12 or eventCRFAudit.entityName eq "Status"}'>
                                        <c:if test="${eventCRFAudit.oldValue eq '0'}">
                                            <fmt:message  key="invalid" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '1'}">
                                            <fmt:message  key="available" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '2'}">
                                            <fmt:message  key="unavailable" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '3'}">
                                            <fmt:message  key="private" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '4'}">
                                            <fmt:message  key="pending" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '5'}">
                                            <fmt:message  key="removed" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '6'}">
                                            <fmt:message  key="locked" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '7'}">
                                            <fmt:message  key="auto-removed" bundle="${resword}"/>
                                        </c:if>
                                    </c:when>
                                    <c:when test='${eventCRFAudit.auditEventTypeId == 32}' >
                                        <c:choose>
                                        <c:when test="${eventCRFAudit.oldValue eq '1'}">
                                            <fmt:message  key="true" bundle="${resword}"/>
                                        </c:when>
                                        <c:when test="${eventCRFAudit.oldValue eq '0'}">
                                            <fmt:message  key="false" bundle="${resword}"/>
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
                                            <fmt:message  key="invalid" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '1'}">
                                            <fmt:message  key="available" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '2'}">
                                            <fmt:message  key="unavailable" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '3'}">
                                            <fmt:message  key="private" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '4'}">
                                            <fmt:message  key="pending" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '5'}">
                                            <fmt:message  key="removed" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '6'}">
                                            <fmt:message  key="locked" bundle="${resword}"/>
                                        </c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '7'}">
                                            <fmt:message  key="auto-removed" bundle="${resword}"/>
                                        </c:if>
                                    </c:when>
                                    <c:when test='${eventCRFAudit.auditEventTypeId == 32}' >
                                        <c:choose>
                                        <c:when test="${eventCRFAudit.newValue eq '1'}">
                                             <fmt:message  key="true" bundle="${resword}"/>
                                        </c:when>
                                        <c:when test="${eventCRFAudit.newValue eq '0'}">
                                            <fmt:message  key="false" bundle="${resword}"/>
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