<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<c:choose>
    <c:when test="${study.status.name != 'removed' && study.status.name != 'auto-removed'}">
        <c:url var="viewStudy" value="/ViewStudy"/>
        <c:choose>
            <c:when test="${study.parentStudyId>0}">
                <b><fmt:message key="study" bundle="${resword}"/>:</b>&nbsp;<a href="${viewStudy}?id=<c:out value="${study.parentStudyId}"/>&viewFull=yes"><c:out value="${study.parentStudyName}"/></a>
                <br><br>
                <b>Site:</b>&nbsp;<a href="${viewStudy}?id=<c:out value="${study.id}"/>">
            </c:when>   
            <c:otherwise>
                <b><fmt:message key="study" bundle="${resword}"/>:</b>&nbsp;<a href="${viewStudy}?id=<c:out value="${study.id}"/>&viewFull=yes">
            </c:otherwise>
        </c:choose>
        <c:out value="${study.name}"/></a><br/>
        <c:if test="${studySubject != null}">
            <b><a href="ViewStudySubject?id=<c:out value="${studySubject.id}"/>"><fmt:message key="study_subject_ID" bundle="${resword}"/></a>:</b>&nbsp; <c:out value="${studySubject.label}"/>
        </c:if>   
        &nbsp;&nbsp;-&nbsp;<fmt:message key="study_xml" bundle="${resword}"/>:&nbsp;<b><a href="DownloadStudyMetadata?studyId=<c:out value="${study.id}"/>"></b><fmt:message key="download" bundle="${resword}"/></a><br/>
        &nbsp;&nbsp;-&nbsp;<fmt:message key="unique_crfs" bundle="${resword}"/>:&nbsp; <c:out value="${crfCount}"/><br/>
        &nbsp;&nbsp;-&nbsp;<fmt:message key="unique_items" bundle="${resword}"/>:&nbsp; <c:out value="${itemCount}"/><br/>
        &nbsp;&nbsp;-&nbsp;<fmt:message key="unique_rule_assignments" bundle="${resword}"/>:&nbsp; <c:out value="${ruleSetCount}"/><br/>
    </c:when>
    <c:otherwise>
        Your last active study/site was <c:out value="${study.name}"/>, but it has been deleted.
    </c:otherwise>
</c:choose>