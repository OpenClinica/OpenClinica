<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="../include/submit-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope="request" id="entityWithStudyEvents" class="org.akaza.openclinica.bean.core.EntityBean" />
<jsp:useBean scope="request" id="displayEntities" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="browseBy" class="java.lang.String" />

<c:choose>
	<c:when test='${browseBy == "Subject"}'>
		<c:set var="browseByEnglish" value="Subject" />
	</c:when>
	<c:otherwise>
		<c:set var="browseByEnglish" value="Study Event Definition" />
	</c:otherwise>
</c:choose>

<p class="breadcrumb">
	<a href="MainMenu"><fmt:message key="openclinica_home" bundle="${resword}"/></a> >
	<a href="SubmitData"><fmt:message key="submit_data_home" bundle="${resword}"/></a>
</p>

<p class="title">
<fmt:message key="find_an_existing_SE_for" bundle="${restext}"/> <c:out value="${browseByEnglish}" /> <b><c:out value="${entityWithStudyEvents.name}" /></b>
</p>

<jsp:include page="../include/showPageMessages.jsp" />

<p>
<c:choose>
	<c:when test="${empty displayEntities}">
		<fmt:message key="there_are_no_SE_for_the" bundle="${restext}"/> <b><c:out value="${entityWithStudyEvents.name}" /></b> <c:out value="${browseByEnglish}" />.
	</c:when>
	<c:otherwise>
		<fmt:message key="displaying_SE_for_the" bundle="${restext}"/> <b><c:out value="${entityWithStudyEvents.name}" /></b> <c:out value="${browseByEnglish}" />:
		<table>
			<tr>
				<td><fmt:message key="location" bundle="${resword}"/></td>
				<td><fmt:message key="started" bundle="${resword}"/></td>
				<td><fmt:message key="ended" bundle="${resword}"/></td>
				<td><fmt:message key="action" bundle="${resword}"/></td>
			</tr>
		<c:forEach var="event" items="${displayEntities}">
			<tr>
				<td><c:out value="${event.location}" /></td>
				<td><c:out value="${event.dateStarted}" /></td>
				<td><c:out value="${event.dateEnded}" /></td>
				<td><a href="EnterDataForStudyEvent?eventId=<c:out value="${event.id}" />"><fmt:message key="enter_data" bundle="${resword}"/></a></td>
			</tr>
		</c:forEach>
		</table>
		<p>
	</c:otherwise>
</c:choose>

<jsp:include page="../include/footer.jsp"/>
