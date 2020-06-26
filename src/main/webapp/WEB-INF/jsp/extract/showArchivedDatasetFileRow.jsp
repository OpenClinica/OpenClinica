<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>
<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>
<c:set var="viewSingleJob" value="${param.viewSingleJob}" />
<c:set var="exportDataset" value="${param.exportDataset}"/>
<c:set var="jobUuid" value="${param.jobUuid}" />
<c:set var="triggerName" value="${param.tname}" />
<c:set var="gName" value="${param.gname}" />

<jsp:useBean scope="request" id="currRow" class="core.org.akaza.openclinica.web.bean.ArchivedDatasetFileRow" />
<tr>
	<td class="table_cell"><c:out value="${currRow.bean.format}" /></td>
	<td class="table_cell_left"><c:out value="${currRow.bean.name}" /></td>
	<td class="table_cell"><c:out value="${currRow.bean.runTime}" /></td>
	<td class="table_cell"><c:out value="${currRow.bean.fileSize}" /></td>
	<td class="table_cell"><fmt:formatDate value="${currRow.bean.dateCreated}"  pattern="${dtetmeFormat}" /> </td>
	<td class="table_cell"><c:out value="${currRow.bean.owner.name}" /></td>
    <td class="table_cell"><c:out value="${currRow.bean.status}" /></td>
    <c:if test="${exportDataset == 'true'}">
        <td class="table_cell"><c:out value="${currRow.bean.jobType}" /></td>
    </c:if>

    <td class="table_cell" style="width:70px">
		<c:if test="${(currRow.bean.status == 'COMPLETED' || currRow.bean.status == '') && currRow.bean.fileReference != ''}">

            <a target="_new" href="AccessFile?fileId=<c:out value="${currRow.bean.id}"/>">
                <span name="bt_Download1" class="icon icon-download" border="0" align="left" hspace="6"
                     alt="<fmt:message key="download" bundle="${resword}"/>" title="<fmt:message key="download" bundle="${resword}"/>">
            </a>
            <c:choose>
                <c:when test="${viewSingleJob=='true'}">
                    <a href="ViewSingleJob?action=delete&datasetId=<c:out value="${currRow.bean.datasetId}"/>&adfId=<c:out value="${currRow.bean.id}"/>&jobUuid=<c:out value="${jobUuid}"/>&tname=<c:out value="${triggerName}"/>&gname=<c:out value="${gName}"/>">
                        <span name="bt_Delete1" class="icon icon-trash red" border="0" alt="<fmt:message key="delete" bundle="${resword}"/>"
                             title="<fmt:message key="delete" bundle="${resword}"/>" align="left" hspace="6"
                             onClick='return confirm("<fmt:message key="if_you_delete_this_dataset" bundle="${restext}"/>");'>
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="ExportDataset?action=delete&datasetId=<c:out value="${currRow.bean.datasetId}"/>&adfId=<c:out value="${currRow.bean.id}"/>">
                        <span name="bt_Delete1" class="icon icon-trash red" border="0" alt="<fmt:message key="delete" bundle="${resword}"/>"
                             title="<fmt:message key="delete" bundle="${resword}"/>" align="left" hspace="6"
                             onClick='return confirm("<fmt:message key="if_you_delete_this_dataset" bundle="${restext}"/>");'>
                    </a>
                </c:otherwise>
            </c:choose>
        </c:if>

    </td>
</tr>
