<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>


<jsp:include page="../include/submit-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope="request" id="displayEntities" type="java.util.List" />
<jsp:useBean scope="request" id="browseBy" class="java.lang.String" />
<jsp:useBean scope="request" id="pageNum" type="java.lang.Integer" />
<jsp:useBean scope="request" id="displayNextPage" class="java.lang.String" />

<c:choose>
<c:when test='${browseBy == "Subject"}'>
	<c:set var="browseByEnglish" value="Subject" />
</c:when>
<c:otherwise>
	<c:set var="browseByEnglish" value="Study Event Definition" />
</c:otherwise>
</c:choose>

<c:set var="prevPageNum" value="${pageNum - 1}" />
<c:set var="nextPageNum" value="${pageNum + 1}" />


<p class="breadcrumb">
	<a href="MainMenu"><fmt:message key="openclinica_home" bundle="${resword}"/></a> >
	<a href="SubmitData"><fmt:message key="submit_data_home" bundle="${resword}"/></a>
</p>

<p class="title">
<fmt:message key="find_an_existing_study_event_by" bundle="${restext}"/> <c:out value="${browseByEnglish}" />
</p>

<jsp:include page="../include/alertbox.jsp" />

<p>
<c:choose>
	<c:when test="${empty displayEntities}">
		<fmt:message key="there_are_not_with_SE" bundle="${restext}">
		 <fmt:param value="${browseByEnglish}"/>
		</fmt:message>
	</c:when>
	<c:otherwise>
		<fmt:message key="displaying_with_SE" bundle="${restext}">
		 <fmt:param value="${browseByEnglish}"/>
		</fmt:message>
		<ul>
		<c:forEach var="entity" items="${displayEntities}">
			<li> <a href="FindStudyEvent?browseBy=<c:out value="${browseBy}"/>&id=<c:out value="${entity.id}"/>"><c:out value="${entity.name}" /></a>
		</c:forEach>
		</ul>
		<p>
		<c:if test="${pageNum gt 0}">
			<a href="FindStudyEvent?browseBy=<c:out value="${browseBy}"/>&pageNum=<c:out value="${prevPageNum}" />">&lt;&lt; <fmt:message key="previous_page" bundle="${resword}"/></a>
		</c:if>
		<c:if test='${displayNextPage == "yes"}'>
			<a href="FindStudyEvent?browseBy=<c:out value="${browseBy}"/>&pageNum=<c:out value="${nextPageNum}" />"><fmt:message key="next_page" bundle="${resword}"/> &gt;&gt;</a>		
		</c:if>
	</c:otherwise>
</c:choose>

<jsp:include page="../include/footer.jsp"/>
