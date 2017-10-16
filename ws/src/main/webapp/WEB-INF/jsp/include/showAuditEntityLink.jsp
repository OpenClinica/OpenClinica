<%@ page contentType="text/html; charset=UTF-8" %>
<%-- proper format: usually called from a showAuditRow.jsp page:
	<c:import url="../include/showAuditEntityLink.jsp">
			<c:param name="auditTable" value="${currRow.bean.auditTable}" />
			<c:param name="entityId" value="${currRow.bean.entityId}" />
			<c:param name="rolename" value="${userRole.role.name}"/>
	</c:import>
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/> 


<c:set var="auditTable" value="${param.auditTable}" />
<c:set var="entityId" value="${param.entityId}" />
<c:choose>
	<c:when test='${(auditTable != null)}'>
		<!-- not blank form here -->
		<c:if test="${auditTable == 'Subject'}">
		<!-- subject here -->
		<a href="ViewSubject?id=<c:out value="${entityId}"/>"
	  onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
	  onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img 
	  name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view_subject" bundle="${resworkflow}"/>" title="<fmt:message key="view_subject" bundle="${resworkflow}"/>" align="left" hspace="6"></a>
		</c:if>
		<c:if test="${auditTable == 'Study Subject'}">
		<!-- study subject here -->
		
		</c:if>
		<c:if test="${auditTable == 'User Account'}">
		<!-- user account here -->
			<c:if test="${userBean.sysAdmin}">  
		<a href="ViewUserAccount?userId=<c:out value="${entityId}"/>"
	  onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
	  onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img 
	  name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view_user_account" bundle="${resworkflow}"/>" title="<fmt:message key="view_user_account" bundle="${resworkflow}"/>" align="left" hspace="6"></a>
			  </c:if>
		</c:if>
	</c:when>
	<c:otherwise>
		<!-- blank form here -->
	</c:otherwise>
</c:choose>
