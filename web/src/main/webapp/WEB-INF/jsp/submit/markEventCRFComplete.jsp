<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:useBean scope="request" id="toc" class="org.akaza.openclinica.bean.submit.DisplayTableOfContentsBean" />

<jsp:include page="../include/submit-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<h1><span class="title_manage"><fmt:message key="mark_event_CRF_complete" bundle="${resworkflow}"/>: <c:out value="${toc.crf.name}"/></span></h1>

<c:import url="instructionsEnterData.jsp">
	<c:param name="currStep" value="markComplete" />
</c:import>


<jsp:include page="../include/alertbox.jsp" />

<form method="POST" action="MarkEventCRFComplete">
<jsp:include page="../include/showSubmitted.jsp" />
<input type="hidden" name="eventCRFId" value="<c:out value="${toc.eventCRF.id}"/>" />

<fmt:message key="marking_crf_complete_finalize_DE" bundle="${restext}"/>
		<input type="submit" value="<fmt:message key="yes" bundle="${resword}"/>" name="markComplete" class="button_medium" />
		<input type="submit" value="<fmt:message key="no" bundle="${resword}"/>" name="markComplete" class="button_medium" />
</form>

<jsp:include page="../include/footer.jsp"/>
