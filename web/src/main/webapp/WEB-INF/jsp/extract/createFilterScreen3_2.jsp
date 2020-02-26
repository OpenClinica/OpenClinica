<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>


<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>

<h1><span class="title_manage"><fmt:message key="create_filter" bundle="${resword}"/>: <fmt:message key="specify_parameters" bundle="${resword}"/></span></h1>
<P><jsp:include page="../include/showPageMessages.jsp"/></P>
<jsp:include page="createFilterBoxes.jsp">
	<jsp:param name="selectParameters" value="1"/>
</jsp:include>
<form action="CreateFiltersTwo" name="cf2">
<input type="hidden" name="action" value="questionsselected"/>

&nbsp;<b><fmt:message key="CRF" bundle="${resword}"/></b>&nbsp;&nbsp;<c:out value='${cBean.name}'/>&nbsp;<c:out value='${cvBean.name}'/><br/>
&nbsp;<b><fmt:message key="section" bundle="${resword}"/></b>&nbsp;&nbsp;<c:out value='${secBean.name}'/><br/>

<c:forEach var='item' items='${metadatas}'>
	&nbsp;&nbsp;<input type="checkbox" name="ID<c:out value='${item.id}'/>">&nbsp;
			<c:out value='${item.questionNumberLabel}'/>
			<c:out value='${item.header}'/>
			<c:out value='${item.leftItemText}'/> ...
			<c:out value='${item.rightItemText}'/><br/>
</c:forEach>

<br/><br/>
<input type="submit" value="<fmt:message key="select_data_elements_and_continue" bundle="${restext}"/>" class="button_xlong"/>
</form>
<jsp:include page="../include/footer.jsp"/>
