<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="events" class="java.util.HashMap"/>

<h1><span class="title_manage"><fmt:message key="create_filter" bundle="${resword}"/>: <fmt:message key="specify_parameters" bundle="${resword}"/></span></h1>
<P><jsp:include page="../include/showPageMessages.jsp"/></P>
<jsp:include page="createFilterBoxes.jsp">
	<jsp:param name="selectCrf" value="1"/>
</jsp:include>
<c:if test='${newExp != null}'>
        <P><fmt:message key="generated_filter" bundle="${resword}"/>: <c:forEach var='str' items='${newExp}'>
        						<c:out value='${str}'/>
        					 </c:forEach>
        </P>
</c:if>
<form action="CreateFiltersTwo" method="post" name="cf2">
<input type="hidden" name="action" value="crfselected"/>
&nbsp;<b><fmt:message key="CRF" bundle="${resword}"/></b>&nbsp;&nbsp;

<select name="crfId">
	<option value="0" selected>-- <fmt:message key="select_CRF" bundle="${resworkflow}"/> --</option>
<c:forEach var='item' items='${events}'>

	<c:set var="crf_name" value='${item.key.name}'/>

	<c:forEach var='crf' items='${item.value}'>
		<option value="<c:out value='${crf.id}'/>">
			<c:out value='${crf_name}'/> --
			<c:out value='${crf.name}'/>
		</option>
	</c:forEach>
</c:forEach>
</select><br/>
<input type="submit" value="<fmt:message key="select_crf_and_continue" bundle="${resword}"/>" class="button_xlong"/>
</form>
<jsp:include page="../include/footer.jsp"/>
