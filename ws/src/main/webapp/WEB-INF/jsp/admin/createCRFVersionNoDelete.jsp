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


<jsp:useBean scope='session' id='itemsHaveData' class='java.util.ArrayList'/>
<jsp:useBean scope='session' id='eventsForVersion' class='java.util.ArrayList'/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='version' class='org.akaza.openclinica.bean.submit.CRFVersionBean'/>
<h1><span class="title_manage"><fmt:message key="create_a_new_CRF_version" bundle="${resword}"/> - <fmt:message key="remove_previous_same_version_error" bundle="${resword}"/>
</span></h1>


 <c:if test="${not empty eventsForVersion}">

<span class="alert">
	<fmt:message key="the_previous_CRF_version_has_associated_SE" bundle="${restext}">
	  <fmt:param><c:out value="${version.crfId}"/></fmt:param>
	</fmt:message>
</span>
 <div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
   <tr valign="top">
      <td class="table_header_column_top"><fmt:message key="event_ID" bundle="${resword}"/></td>
      <td class="table_header_column_top"><fmt:message key="owner" bundle="${resword}"/></td>
    </tr>
  <c:forEach var="event" items="${eventsForVersion}">
  <tr valign="top">
      <td class="table_cell"><c:out value="${event.id}"/></td>
      <td class="table_cell"><c:out value="${event.owner.name}"/></td>
    </tr>
 </c:forEach>
 </table>
 <br>
</div>

</div></div></div></div></div></div></div></div>
</div>
 </c:if>
 <br><br>
 <c:if test="${!empty itemsHaveData}">
 <span class="alert">
	 <fmt:message key="some_items_in_the_previous_version_have_related" bundle="${restext}">
	 	<fmt:param><c:out value="${version.crfId}"/></fmt:param>
	 </fmt:message>
 </span>
 <div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center" >
<table border="0" cellpadding="0" cellspacing="0" width="100%">
   <tr valign="top">
      <td class="table_header_column_top"><fmt:message key="item_ID" bundle="${resword}"/></td>
      <td class="table_header_column_top"><fmt:message key="name" bundle="${resword}"/></td>
      <td class="table_header_column_top"><fmt:message key="owner" bundle="${resword}"/></td>
    </tr>
  <c:forEach var="item" items="${itemsHaveData}">
  <tr valign="top">
      <td class="table_cell"><c:out value="${item.id}"/></td>
      <td class="table_cell"><c:out value="${item.name}"/></td>
      <td class="table_cell"><c:out value="${item.owner.name}"/></td>
    </tr>
 </c:forEach>

</table>
<br>
<br>
</div>

</div></div></div></div></div></div></div></div>
</div>
</c:if>
<c:choose>
  <c:when test="${userBean.sysAdmin && module=='admin'}">
  <c:import url="../include/workflow.jsp">
   <c:param name="module" value="admin"/>
  </c:import>
 </c:when>
  <c:otherwise>
   <c:import url="../include/workflow.jsp">
   <c:param name="module" value="manage"/>
  </c:import>
  </c:otherwise>
 </c:choose>

<jsp:include page="../include/footer.jsp"/>
