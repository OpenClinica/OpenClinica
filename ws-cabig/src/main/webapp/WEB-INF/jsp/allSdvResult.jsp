<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="include/managestudy_top_pages.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="include/sideAlert.jsp"/>

<div id="sdvResult">
    allParams: ${allParams}    <br />
    verified: ${verified} 
</div>

<c:import url="include/workflow.jsp">
    <c:param name="module" value="manage"/>
</c:import>

<jsp:include page="include/footer.jsp"/>