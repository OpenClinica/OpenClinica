<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>


<jsp:include page="../include/home-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

		<fmt:message key="instructions" bundle="${restext}"/>

		<div class="sidebar_tab_content">

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>

		<fmt:message key="instructions" bundle="${restext}"/>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>


<h1><span class="title_manage"><fmt:message key="openclinica_enterprise" bundle="${resworkflow}"/></span></h1>


<fmt:message key="openclinica_enterprise_is_an_enhaced_version" bundle="${restext}"/>
<fmt:message key="user_support_network" bundle="${restext}"/>
<fmt:message key="technical_administration_network" bundle="${restext}"/>
<fmt:message key="developer_network" bundle="${restext}"/>

<jsp:include page="../include/footer.jsp"/>
