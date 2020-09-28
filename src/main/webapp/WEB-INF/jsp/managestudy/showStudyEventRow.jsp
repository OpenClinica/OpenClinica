<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:useBean scope="request" id="currRow" class="core.org.akaza.openclinica.web.bean.DisplayStudyEventRow" />
<jsp:useBean scope="request" id="studySub" class="core.org.akaza.openclinica.bean.managestudy.StudySubjectBean"/>
<jsp:useBean scope="request" id="studyRelatedTostudySub" class="core.org.akaza.openclinica.domain.datamap.Study"/>
<script type="text/javascript" language="JavaScript" src="includes/permissionTagAccess.js"></script>
<tr>
    <td class="table_cell_left">
        <c:out value="${currRow.bean.studyEvent.studyEventDefinition.name}"/>
        <c:if test="${currRow.bean.studyEvent.studyEventDefinition.repeating}">
            (<c:out value="${currRow.bean.studyEvent.sampleOrdinal}"/>)
        </c:if>
        <br>
        <c:out value="${currRow.bean.studyEvent.additionalNotes}"/>
    </td>
    <td class="table_cell">
        <fmt:formatDate value="${currRow.bean.studyEvent.dateStarted}" pattern="${dteFormat}"/>
    </td>
    <td class="table_cell" width="20">
        <c:choose>
            <c:when test="${currRow.bean.studyEvent.removed == true}">
                <fmt:message key="removed" bundle="${resword}"/>
            </c:when>
            <c:otherwise>
                <c:out value="${currRow.bean.studyEvent.workflowStatus.displayValue}"/>
            </c:otherwise>
        </c:choose>
        <div style="white-space: nowrap;">
            <c:if test="${currRow.bean.studyEvent.signed == true}">
                <span class="icon icon-stamp-new status" alt="<fmt:message key="signed" bundle="${resword}"/>" title="<fmt:message key="signed" bundle="${resword}"/>"/>
            </c:if>
            <c:if test="${currRow.bean.studyEvent.locked == true}">
                <span class="icon icon-lock-new status" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>"/>
            </c:if>
            <c:if test="${currRow.bean.studyEvent.archived == true}">
                <span class="icon icon-archived-new status" alt="<fmt:message key="archived" bundle="${resword}"/>" title="<fmt:message key="archived" bundle="${resword}"/>"/>
            </c:if>
        </div>
    </td>
    <td class="table_cell">
        <table border="0" cellpadding="0" cellspacing="0">
            <%@include file="eventActions.jsp"%>
        </table>
    </td>
    <td class="table_cell">
        <c:choose>
            <c:when test="${empty currRow.bean.uncompletedCRFs && empty currRow.bean.displayEventCRFs}">
                <fmt:message key="no_CRFs" bundle="${resword}"/>
            </c:when>
            <c:otherwise>
                <table border="0" cellpadding="0" cellspacing="0">
                    <tr valign="top">
                      <td class="table_cell" width="180" style="background-color: #ccc;"><center><fmt:message key="name" bundle="${resword}"/></center></td>
                      <td class="table_cell" width="180" style="background-color: #ccc;"><center><fmt:message key="version" bundle="${resword}"/></center></td>
                      <td class="table_cell" width="180" style="background-color: #ccc;"><center><fmt:message key="status" bundle="${resword}"/></center></td>
                      <td class="table_cell" width="180" style="background-color: #ccc;"><center><fmt:message key="update" bundle="${resword}"/></center></td>
                      <td class="table_cell" width="180" style="background-color: #ccc;"><center><fmt:message key="actions" bundle="${resword}"/></center></td>
                    </tr>
                    <c:forEach var="dedc" items="${currRow.bean.uncompletedCRFs}">
                        <c:set var="getQuery" value="eventDefinitionCRFId=${dedc.edc.id}&studyEventId=${currRow.bean.studyEvent.id}&subjectId=${studySub.subjectId}&eventCRFId=${dedc.eventCRF.id}&exitTo=ViewStudySubject?id=${studySub.id}" />
                        <form name="startForm<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>" action="InitialDataEntry?<c:out value="${getQuery}"/>" method="POST">
                        <tr valign="top">
                            <td class="table_cell" width="180"><c:out value="${dedc.edc.crf.name}" /></td>
                            <td class="table_cell" width="70">
                                <c:choose>
                                    <c:when test="${dedc.eventCRF.id > 0}">
                                        <!-- found an event crf id -->
                                        <input type="hidden" name="formLayoutId" value="<c:out value="${dedc.eventCRF.formLayout.id}"/>">
                                    </c:when>
                                    <c:otherwise>
                                        <!-- did not find an event crf id -->
                                        <input type="hidden" name="formLayoutId" value="<c:out value="${dedc.edc.defaultVersionId}"/>">
                                    </c:otherwise>
                                </c:choose>
                                <c:set var="versionCount" value="0"/>
                                <c:forEach var="version" items="${dedc.edc.versions}">
                                    <c:set var="formLayoutOID" value="${version.oid}"/>
                                    <c:set var="versionCount" value="${versionCount+1}"/>
                                </c:forEach>
                                <c:choose>
                                    <c:when test="${versionCount<=1}">
                                        <c:forEach var="version" items="${dedc.edc.versions}">
                                            <c:out value="${version.name}"/>
                                        </c:forEach>
                                    </c:when>
                                    <c:when test="${dedc.eventCRF.id == 0}">
                                      <select name="versionId<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>" onchange="javascript:changeQuery<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>();">
                                        <c:forEach var="version" items="${dedc.edc.versions}">
                                            <c:set var="getQuery" value="action=ide_s&eventDefinitionCRFId=${dedc.edc.id}&studyEventId=${currRow.bean.studyEvent.id}&subjectId=${studySub.subjectId}" />
                                            <c:choose>
                                                <c:when test="${dedc.edc.defaultVersionId==version.id}">
                                                  <option value="<c:out value="${version.id}"/>" selected>
                                                    <c:out value="${version.name}"/>
                                                    <c:set var="formLayoutOID" value="${version.oid}"/>
                                                    </option>
                                                </c:when>
                                                <c:otherwise>
                                                    <option value="<c:out value="${version.id}"/>">
                                                        <c:out value="${version.name}"/>
                                                        <c:set var="formLayoutOID" value="${version.oid}"/>
                                                    </option>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:forEach>
                                      </select>
<SCRIPT LANGUAGE="JavaScript">
    function changeQuery<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>() {
        var qer = document.startForm<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>.versionId<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>.value;
        document.startForm<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>.formLayoutId.value=qer;
        document.getElementById('ide1-<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>').href =
            buildUrl(qer,'<c:out value="${currRow.bean.studyEvent.id}"/>','<c:out value="${dedc.eventCRF.id}"/>','<c:out value="${originatingPage}"/>' ,'<c:out value="edit"/>');
        document.getElementById('ide2-<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>').href =
            buildUrl(qer,'<c:out value="${currRow.bean.studyEvent.id}"/>','<c:out value="${dedc.eventCRF.id}"/>','<c:out value="${originatingPage}"/>','<c:out value="view"/>' );
    }

    function buildUrl(formLayoutId, studyEventId, eventCRFId, originatingPage,mode){
        return "EnketoFormServlet?formLayoutId="+ formLayoutId +
               "&studyEventId=" + studyEventId +
               "&eventCrfId=" + eventCRFId +
               "&originatingPage=" + originatingPage+
               "&mode=" + mode;
    }
</SCRIPT>

                                    </c:when>
                                    <c:otherwise>
                                        <c:out value="${dedc.eventCRF.formLayout.name}"/>
                                        <c:set var="formLayoutOID" value="${dedc.eventCRF.formLayout.oid}"/>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="table_cell" bgcolor="#F5F5F5" align="center" width="50">
                                 <table style="width: 100%;">
                                    <tr style="width: inherit;">
                                        <td style="width: 54%; text-align: end;">
                                            <c:choose>
                                                <c:when test="${dedc.status.name=='locked'}">
                                                    <span class="icon icon-lock" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>">
                                                </c:when>
                                                <c:when test="${dedc.eventCRF.id>0}">
                                                    <span class="icon icon-pencil-squared orange" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>" title="<fmt:message key="initial_data_entry" bundle="${resword}"/>">
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="icon icon-doc" alt="<fmt:message key="not_started" bundle="${resword}"/>" title="<fmt:message key="not_started" bundle="${resword}"/>">
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:if test="${currRow.bean.studyEvent.locked == true}">
                                                <span class="icon icon-lock-new status" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>"/>
                                            </c:if>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                            <c:choose>
                                <c:when test="${dedc.eventCRF.id == 0}">
                                    <td class="table_cell" width="80">&nbsp;&nbsp;</td>
                                </c:when>
                                <c:otherwise>
                                    <td class="table_cell" width="80">
                                        <c:choose>
                                            <c:when test="${dedc.eventCRF.updatedDate != null}">
                                                <fmt:formatDate value="${dedc.eventCRF.updatedDate}" pattern="${dteFormat}"/><br>
                                                (<c:out value="${dedc.eventCRF.updater.name}"/>)
                                            </c:when>
                                            <c:otherwise>
                                                (<c:out value="${dedc.eventCRF.owner.name}"/>)
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </c:otherwise>
                            </c:choose>
                            <td class="table_cell" width="140">
                                <table cellspacing="0" cellpadding="0" border="0">
                                    <tr>
                                        <%@include file="uncompletedCrfActions.jsp"%>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        </form>
                    </c:forEach>
                    <c:forEach var="dec" items="${currRow.bean.displayEventCRFs}">
                        <tr>
                            <td class="table_cell" width="180"><c:out value="${dec.eventCRF.crf.name}" /></td>
                            <td class="table_cell" width="70">
                                <c:out value="${dec.eventCRF.formLayout.name}" />
                                <c:if test="${dec.eventCRF.formLayout.status.name == 'Removed'}">
                                    <span class="icon icon-archived-new status" alt="<fmt:message key="archived" bundle="${resword}"/>" title="<fmt:message key="archived" bundle="${resword}"/>"/>
                                </c:if>
                            </td>
                            <td class="table_cell" bgcolor="#F5F5F5" align="center" width="50">
                                <table style="width: 100%;">
                                    <tr style="width: inherit;">
                                        <td style="width: 54%; text-align: end;">
                                            <c:choose>
                                                <c:when test="${ dec.eventCRF.removed == true || (currRow.bean.studyEvent.removed ==true && dec.eventCRF.workflowStatus != 'NOT_STARTED')}">
                                                    <span class="icon icon-file-excel red" alt="<fmt:message key="status_removed" bundle="${resword}"/>" title="<fmt:message key="status_removed" bundle="${resword}"/>">
                                                </c:when>
                                                <c:otherwise>
                                                    <c:choose>
                                                        <c:when test="${ dec.eventCRF.workflowStatus == 'NOT_STARTED'}">
                                                            <span class="icon icon-doc" alt="<fmt:message key="not_started" bundle="${resword}"/>" title="<fmt:message key="not_started" bundle="${resword}"/>">
                                                        </c:when>
                                                        <c:when test="${dec.eventCRF.workflowStatus == 'INITIAL_DATA_ENTRY'}">
                                                            <span class=" icon icon-pencil-squared orange" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>" title="<fmt:message key="data_entry_started" bundle="${resword}"/>">
                                                        </c:when>
                                                        <c:when test="${dec.eventCRF.workflowStatus == 'COMPLETED'}">
                                                            <span class="icon icon-checkbox-checked green" alt="<fmt:message key="data_entry_complete" bundle="${resword}"/>" title="<fmt:message key="data_entry_complete" bundle="${resword}"/>">
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="icon icon-file-excel red" alt="<fmt:message key="status_removed" bundle="${resword}"/>" title="<fmt:message key="status_removed" bundle="${resword}"/>">
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:if test="${currRow.bean.studyEvent.signed == true && dec.eventCRF.removed != true && !dec.eventCRF.isArchived() && dec.eventCRF.workflowStatus == 'COMPLETED'}">
                                                <span class="icon icon-stamp-new status" alt="<fmt:message key="signed" bundle="${resword}"/>" title="<fmt:message key="signed" bundle="${resword}"/>"/>
                                            </c:if>
                                            <c:if test="${currRow.bean.studyEvent.locked == true}">
                                                <span class="icon icon-lock-new status" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>"/>
                                            </c:if>
                                            <c:if test="${dec.eventCRF.isArchived()}">
                                                <span class="icon icon-archived-new status" alt="<fmt:message key="archived" bundle="${resword}"/>" title="<fmt:message key="archived" bundle="${resword}"/>"/>
                                            </c:if>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                            <td class="table_cell" width="80">
                                <c:choose>
                                    <c:when test="${dec.eventCRF.updatedDate != null}">
                                        <fmt:formatDate value="${dec.eventCRF.updatedDate}" pattern="${dteFormat}"/><br>
                                        (<c:out value="${dec.eventCRF.updater.name}"/>)
                                    </c:when>
                                    <c:otherwise>
                                        (<c:out value="${dec.eventCRF.owner.name}"/>)
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="table_cell" width="140">
                                <table border="0" cellpadding="0" cellspacing="0">
                                    <tr valign="top">
                                        <%@include file="eventActions.jsp"%>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </c:otherwise>
        </c:choose>
    </td>
</tr>

