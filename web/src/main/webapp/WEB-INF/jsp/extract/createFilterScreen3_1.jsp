<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>


<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>

<h1><span class="title_manage"><fmt:message key="create_filter" bundle="${resword}"/>: <fmt:message key="select_section" bundle="${resworkflow}"/></span></h1>
<P><jsp:include page="../include/showPageMessages.jsp"/></P>
<jsp:include page="createFilterBoxes.jsp">
	<jsp:param name="selectSection" value="1"/>
</jsp:include>
<form action="CreateFiltersTwo" method="post" name="cf2">
<input type="hidden" name="action" value="sectionselected"/>
&nbsp;<b><fmt:message key="CRF" bundle="${resword}"/></b>&nbsp;&nbsp;<c:out value='${cBean.name}'/>&nbsp;
<c:out value='${cvBean.name}'/><br/>
&nbsp;<b><fmt:message key="section" bundle="${resword}"/></b>&nbsp;&nbsp;
<select name="sectionId">
	<option value="0" selected><fmt:message key="select_section" bundle="${resworkflow}"/></option>
<c:forEach var='item' items='${sections}'>
			<option value="<c:out value='${item.id}'/>">
			<c:out value='${item.name}'/>
		</option>
</c:forEach>
</select><br/>
<input type="submit" value="<fmt:message key="select_section_and_continue" bundle="${resword}"/>" class="button_xlong"/>
</form>
<jsp:include page="../include/footer.jsp"/>
