<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

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

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></span></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		<div class="sidebar_tab_content">

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></span></a>

    <fmt:message key="instructions" bundle="${resword}"/>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>
<jsp:useBean scope='request' id='eventCRFs' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='crfToRemove' class='org.akaza.openclinica.bean.admin.CRFBean'/>

<h1><span class="title_manage"><fmt:message key="confirm_removal_of_CRF" bundle="${resword}"/></span></h1>

<p><fmt:message key="you_choose_to_remove_the_following_CRF" bundle="${restext}"/>:</p>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center" align="center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top" ><td class="table_header_column"><fmt:message key="name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${crfToRemove.name}"/>
   </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="description" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${crfToRemove.description}"/>
  </td></tr>
</table>
 </div>
</div></div></div></div></div></div></div></div>

</div>
<br/>
<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
<span class="table_title_Admin">
</c:when>
<c:otherwise>
<span class="table_title_manage">
</c:otherwise>
</c:choose><fmt:message key="CRF_versions" bundle="${resword}"/></span>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center" align="center">
 <table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top">
    <td class="table_header_column_top"><fmt:message key="CRF_name" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="version_name" bundle="${resword}"/></td>

    <td class="table_header_column_top"><fmt:message key="description" bundle="${resword}"/></td>

     <td class="table_header_column_top"><fmt:message key="status" bundle="${resword}"/></td>

     <td class="table_header_column_top"><fmt:message key="revision_notes" bundle="${resword}"/></td>
    </tr>
  <c:forEach var ="version" items="${crfToRemove.versions}">
    <tr valign="top">
    <td class="table_cell"><c:out value="${crfToRemove.name}"/></td>
    <td class="table_cell"><c:out value="${version.name}"/></td>

    <td class="table_cell"><c:out value="${version.description}"/></td>

     <td class="table_cell"><c:out value="${version.status.name}"/></td>

     <td class="table_cell"><c:out value="${version.revisionNotes}"/></td>
    </tr>
 </c:forEach>
</table>
 </div>
</div></div></div></div></div></div></div></div>

</div>
<br>
<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
<span class="table_title_Admin">
</c:when>
<c:otherwise>
<span class="table_title_manage">
</c:otherwise>
</c:choose><fmt:message key="event_CRFs" bundle="${resword}"/></span>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center" align="center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
 <tr valign="top">
    <td class="table_header_column_top"><fmt:message key="SE" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="date_interviewed" bundle="${resword}"/></td>
     <td class="table_header_column_top"><fmt:message key="status" bundle="${resword}"/></td>
    </tr>
  <c:forEach var="eventCRF" items="${eventCRFs}">
    <tr valign="top">
    <td class="table_cell"><c:out value="${eventCRF.eventName}"/></td>
    <td class="table_cell"><c:out value="${eventCRF.dateInterviewed}"/></td>
    <td class="table_cell"><c:out value="${eventCRF.status.name}"/></td>
    </tr>
 </c:forEach>
</table>
 </div>
</div></div></div></div></div></div></div></div>

</div>
<br>
<form action='RemoveCRF?action=submit&id=<c:out value="${crfToRemove.id}"/>' method="POST">
<input type="submit" name="submit" value="<fmt:message key="remove_CRF" bundle="${resword}"/>" class="button_long" onClick='return confirm("<fmt:message key="if_you_remove_this_CRF" bundle="${restext}"/>");'>
<input type="button" onclick="confirmCancel('ListCRF');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
</form>

<jsp:include page="../include/footer.jsp"/>
