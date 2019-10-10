<%@tag body-content="scriptless" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<%-- begin standard study info: BWP issue 2560: this has largely been cut and pasted from the
 existing sidebar JSP--%>
<c:if test="${panel.studyInfoShown}">
    <c:choose>
        <c:when test="${study.status.name != 'removed' && study.status.name != 'auto-removed'}">

            <c:choose>
                <c:when test="${study.parentStudyId>0}">
                    <b><fmt:message key="study" bundle="${resword}"/>:</b>&nbsp;
                    <a href="ViewStudy?id=${study.parentStudyId}&viewFull=yes">${study.parentStudyName}</a>
                    <br><br>
                    <b>Site:</b>&nbsp;
                    <a href="ViewSite?id=${study.id}">
                </c:when>
                <c:otherwise>
                    <b><fmt:message key="study" bundle="${resword}"/>:</b>&nbsp;
                    <a href="ViewStudy?id=${study.id}&viewFull=yes">
                </c:otherwise>
            </c:choose>
            <c:out value="${study.name}"/></a>

            <br><br>
            <c:if test="${studySubject != null}">
                <b><a href="ViewStudySubject?id=${studySubject.id}"><fmt:message key="study_subject_ID" bundle="${resword}"/></a>:</b>&nbsp; ${studySubject.label}

                <br><br>
            </c:if>
            <c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

            <b><fmt:message key="start_date" bundle="${resword}"/>:</b>&nbsp;
            <c:choose>
                <c:when test="${study.datePlannedStart != null}">

                    <fmt:formatDate value="${study.datePlannedStart}" pattern="${dteFormat}" />
                </c:when>
                <c:otherwise>
                    <fmt:message key="na" bundle="${resword}"/>
                </c:otherwise>
            </c:choose>
            <br><br>

            <b><fmt:message key="end_date" bundle="${resword}"/>:</b>&nbsp;
            <c:choose>
                <c:when test="${study.datePlannedEnd != null}">
                    <fmt:formatDate value="${study.datePlannedEnd}" pattern="${dteFormat}"/>
                </c:when>
                <c:otherwise>
                    <fmt:message key="na" bundle="${resword}"/>
                </c:otherwise>
            </c:choose>
            <br><br>

            <b><fmt:message key="pi" bundle="${resword}"/>:</b>&nbsp; ${study.principalInvestigator}

            <br><br>

            <b><fmt:message key="protocol_verification" bundle="${resword}"/>:</b>&nbsp;
            <fmt:formatDate value="${study.protocolDateVerification}" pattern="${dteFormat}"/>

            <br><br>

            <b><fmt:message key="collect_subject" bundle="${resword}"/></b>&nbsp;
            <c:choose>
                <c:when test="${study.studyParameterConfig.collectDob == '1'}">
                    <fmt:message key="yes" bundle="${resword}"/>
                </c:when>
                <c:when test="${study.studyParameterConfig.collectDob == '2'}">
                    <fmt:message key="only_year_of_birth" bundle="${resword}"/>
                </c:when>
                <c:otherwise>
                    <fmt:message key="not_used" bundle="${resword}"/>
                </c:otherwise>
            </c:choose>

        </c:when>
        <c:otherwise>
            Your last active study/site was ${study.name}, but it has been deleted.
        </c:otherwise>
    </c:choose>
    <br><br>
</c:if>
<c:choose>
    <c:when test="${panel.orderedData}">
        <c:forEach var='line' items="${panel.userOrderedData}">
            <b><c:out value="${line.title}" escapeXml="false"/>
                    :</b>&nbsp;
            <c:out value="${line.info}" escapeXml="false"/>
            <br><br>
        </c:forEach>
    </c:when>
    <c:otherwise>
        <c:forEach var='line' items="${panel.data}">
            <b><c:out value="${line.key}" escapeXml="false"/>:</b>&nbsp;
            <c:out value="${line.value}" escapeXml="false"/>
            <br><br>
        </c:forEach>
    </c:otherwise>
</c:choose>

<%-- end standard study info --%>