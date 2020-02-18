<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>

<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='statuses' class='java.util.ArrayList' />
<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />

<c:set var="fName" value="" />
<c:set var="fDesc" value="" />
<c:set var="fStatusId" value="${1}" />

<%--
	set the values here, tbh
--%>

<c:forEach var="presetValue" items="${presetValues}">
	<c:if test='${presetValue.key == "fName"}'>
		<c:set var="fName" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "fDesc"}'>
		<c:set var="fDesc" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "fStatusId"}'>
		<c:set var="fStatusId" value="${presetValue.value}" />
	</c:if>
</c:forEach>

<h1><span class="title_manage"><fmt:message key="create_filter" bundle="${resword}"/>: <fmt:message key="specify_filter_properties" bundle="${resword}"/></span></h1>
<P><jsp:include page="../include/showPageMessages.jsp"/></P>
<jsp:include page="createFilterBoxes.jsp">
	<jsp:param name="save" value="1"/>
</jsp:include>
<c:if test='${newExp != null}'>
        <P><fmt:message key="generated_filter" bundle="${resword}"/>: <c:forEach var='str' items='${newExp}'>
        						<c:out value='${str}'/>
        					 </c:forEach>
        </P>
</c:if>
<P><fmt:message key="please_enter_all_the_filter" bundle="${restext}"/>
<font color="red"><fmt:message key="all_fields_are_required" bundle="${resword}"/></font></P>
<form action="CreateFiltersThree">

<input type="hidden" name="action" value="validate"/>
<table>
<tr>
<td><b><fmt:message key="name" bundle="${resword}"/>:</b></td>
	<td>
	<input type="text" name="fName" size="30" value="<c:out value='${fName}' />"/>
	</td>
</tr>
<tr>
<td><b><fmt:message key="description" bundle="${resword}"/>:</b></td>
	<td>
	<textarea name="fDesc" cols="40" rows="4"><c:out value="${fDesc}" /></textarea>
	</td>
</tr>
<tr>
<td><b><fmt:message key="status" bundle="${resword}"/>:</b></td>
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


<P><input type="submit" value="<fmt:message key="save_filter" bundle="${resword}"/>" class="button_xlong"/>

</form>

<jsp:include page="../include/footer.jsp"/>
