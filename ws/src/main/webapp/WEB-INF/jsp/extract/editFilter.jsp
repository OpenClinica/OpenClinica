<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/> 
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='filter' class='org.akaza.openclinica.bean.extract.FilterBean' />
<jsp:useBean scope='request' id='statuses' class='java.util.ArrayList' />
<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />

<c:set var="fName" value="${filter.name}" />
<c:set var="fDesc" value="${filter.description}" />
<c:set var="fStatusId" value="${filter.status.id}" />

<%--
	set the values here, tbh
--%>

<c:forEach var="presetValue" items="${presetValues}">

   <c:choose>
	<c:when test='${presetValue.key == "fName"}'>
		<c:set var="fName" value="${presetValue.value}" />
	</c:when>
	<c:otherwise>
		<c:set var="fName" value="${filter.name}" />
	</c:otherwise>
	</c:choose>
	
	<c:choose>
	<c:when test='${presetValue.key == "fDesc"}'>
		<c:set var="fDesc" value="${presetValue.value}" />
	</c:when>
	<c:otherwise>
		<c:set var="fDesc" value="${filter.description}" />
	</c:otherwise>
	</c:choose>
	
	<c:choose>
	<c:when test='${presetValue.key == "fStatusId"}'>
		<c:set var="fStatusId" value="${presetValue.value}" />
	</c:when>
	<c:otherwise>
		<c:set var="fStatusId" value="${filter.status.id}" />
	</c:otherwise>
	</c:choose>
</c:forEach>

<h1><span class="title_manage"><fmt:message key="edit_filter" bundle="${restext}"/>: <c:out value="${filter.name}"/></span></h1>
<P><jsp:include page="../include/showPageMessages.jsp"/></P>

<form action="EditFilter">
<input type="hidden" name="action" value="validate"/>
<input type="hidden" name="filterId" value="<c:out value='${filter.id}'/>"/>
<table>
<tr>
	<td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="fName"/>
</jsp:include></td>

	<td><b><fmt:message key="name" bundle="${restext}"/>:</b></td>
	
	<td><input type="text" name="fName" size="30" value="<c:out value='${fName}' />"/></td>
	
</tr>
<tr>

	<td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="fDesc"/>
	</jsp:include></td>

	<td><b><fmt:message key="description" bundle="${restext}"/>:</b></td>
	
	<td><textarea name="fDesc" cols="40" rows="4">
		<c:out value="${fDesc}" />
	</textarea></td>
	
</tr>
<tr>
	<td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="fStatusId"/>
</jsp:include></td>

	<td><b><fmt:message key="status" bundle="${restext}"/>:</b></td>
	
	<td>
	<select name="fStatusId">
		<option value="0">-- <fmt:message key="select_status" bundle="${resword}"/> --</option>
			<c:forEach var="status" items="${statuses}">
				<c:choose>
					<c:when test="${fStatusId == status.id}">
						<option value="<c:out value='${status.id}' />" selected><c:out value="${status.name}" /></option>
					</c:when>
					<c:otherwise>
						<option value="<c:out value='${status.id}' />"><c:out value="${status.name}" /></option>
					</c:otherwise>
				</c:choose>
			</c:forEach>
	</select>
	</td>
</tr>
</table>
<br/>
<input type="submit" value="<fmt:message key="edit_this_filter" bundle="${resword}"/>" class="button_xlong"/>

</form>
<jsp:include page="../include/footer.jsp"/>
