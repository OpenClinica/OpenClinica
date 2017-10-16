<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>


<jsp:include page="../include/submit-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<p class="breadcrumb">
	<a href="MainMenu"><fmt:message key="openclinica_home" bundle="${resword}"/></a> >
	<a href="SubmitData"><fmt:message key="submit_data_home" bundle="${resword}"/></a>
</p>

<p class="title">
<fmt:message key="find_an_existing_study_event" bundle="${restext}"/>
</p>

<jsp:include page="../include/alertbox.jsp" />

<p><a href="FindStudyEvent?browseBy=Subject"><fmt:message key="browse_by_subject" bundle="${restext}"/></a>

<p><a href="FindStudyEvent?browseBy=StudyEventDefinition"><fmt:message key="browse_by_SED" bundle="${restext}"/></a>

<jsp:include page="../include/footer.jsp"/>
