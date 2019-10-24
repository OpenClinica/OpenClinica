<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
 <c:import url="../include/admin-header.jsp"/>
</c:when>
<c:otherwise>
 <c:import url="../include/managestudy-header.jsp"/>
</c:otherwise>
</c:choose>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='request' id='definitions' class='java.util.ArrayList'/>
<jsp:useBean scope='session' id='userBean' class='core.org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='version' class='core.org.akaza.openclinica.bean.submit.CRFVersionBean'/>

<h1><span class="title_manage"><fmt:message key="create_a_new_CRF_version" bundle="${resword}"/> - <fmt:message key="version_already_exits" bundle="${resword}"/> </span></h1>

<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="alertbox_center">
<fmt:message key="the_CRF_version_you_try_to_upload_already_exists" bundle="${restext}">
  <fmt:param><c:out value="${version.crfId}"/></fmt:param>
</fmt:message>
<fmt:message key="or_you_can_change_the_version_name" bundle="${restext}"/>
<br><br></div>

</div></div></div></div></div></div></div></div>
<br>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center" align="center">

<table border="0" cellpadding="0" cellspacing="0" width="100%">
   <tr valign="top">
      <td class="table_header_column_top"><fmt:message key="SED_ID" bundle="${resword}"/></td>
      <td class="table_header_column_top"><fmt:message key="study_ID" bundle="${resword}"/></td>
      <td class="table_header_column_top"><fmt:message key="date_created" bundle="${resword}"/></td>
      <td class="table_header_column_top"><fmt:message key="owner" bundle="${resword}"/></td>
    </tr>
  <c:forEach var="def" items="${definitions}">
  <tr valign="top">
      <td class="table_cell"><c:out value="${def.studyEventDefinitionId}"/></td>
      <td class="table_cell"><c:out value="${def.studyId}"/></td>
      <td class="table_cell"><fmt:formatDate value="${def.createdDate}" pattern="${dteFormat}"/></td>
      <td class="table_cell"><c:out value="${def.owner.name}"/></td>
    </tr>
 </c:forEach>

</table>
</div>

</div></div></div></div></div></div></div></div>

<jsp:include page="../include/footer.jsp"/>
