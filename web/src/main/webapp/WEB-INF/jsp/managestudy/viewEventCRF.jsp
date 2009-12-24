<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>

<c:choose>
<c:when test="${userBean.sysAdmin}">
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

<jsp:useBean scope='request' id='displayItemData' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='studySubId' type='java.lang.String'/>
<c:choose>
<c:when test="${userBean.sysAdmin}">
  <h1><span class="title_manage">
</c:when>
<c:otherwise>
  <h1><span class="title_manage">
</c:otherwise>
</c:choose>
<fmt:message key="view_event_CRF_properties" bundle="${resworkflow}"/>: <c:out value="${studySub.label}"/> - <c:out value="${crf.name}"/></span></h1>
<c:forEach var="section" items="${sections}">
 <div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top">
  <td class="table_header_column_top"><fmt:message key="section_name" bundle="${resword}"/></td>
  <td class="table_header_column_top"><fmt:message key="title" bundle="${resword}"/></td>
  <td class="table_header_column_top"><fmt:message key="subtitle" bundle="${resword}"/></td>
  <td class="table_header_column_top"><fmt:message key="instructions" bundle="${resword}"/></td>
  <td class="table_header_column_top"><fmt:message key="page_number_label" bundle="${resword}"/></td>
 </tr>
  <tr valign="top">
   <td class="table_cell"><c:out value="${section.name}"/></td>
   <td class="table_cell"><c:out value="${section.title}"/></td>
   <td class="table_cell"><c:out value="${section.subtitle}"/>&nbsp;</td>
   <td class="table_cell"><c:out value="${section.instructions}"/>&nbsp;</td>
   <td class="table_cell"><c:out value="${section.pageNumberLabel}"/>&nbsp;</td>
  </tr>
</table>
 </div>
</div></div></div></div></div></div></div></div>

</div>
<br>
<div style="width: 100%">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
 <tr valign="top">
    <td class="table_header_column_top"><fmt:message key="name" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="description" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="units" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="PHI_status" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="page_number_label" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="question_number_label" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="left_item_text" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="right_item_text" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="response_label" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="response_value" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="required" bundle="${resword}"/></td>
  </tr>

 <c:forEach var="did" items="${section.items}">
   <tr valign="top">
    <td class="table_cell"><c:out value="${did.item.name}"/></td>
    <td class="table_cell"><c:out value="${did.item.description}"/>&nbsp;</td>
    <td class="table_cell"><c:out value="${did.item.units}"/>&nbsp;</td>
    <td class="table_cell"><c:out value="${did.item.phiStatus}"/>&nbsp;</td>
    <td class="table_cell"><c:out value="${did.metadata.pageNumberLabel}"/>&nbsp;</td>
    <td class="table_cell"><c:out value="${did.metadata.questionNumberLabel}"/>&nbsp;</td>
    <td class="table_cell"><c:out value="${did.metadata.leftItemText}"/></td>
    <td class="table_cell"><c:out value="${did.metadata.rightItemText}"/>&nbsp;</td>
    <td class="table_cell"><c:out value="${did.metadata.responseSet.label}"/>&nbsp;</td>
    <td class="table_cell"><c:out value="${did.data.value}"/>&nbsp;</td>
    <td class="table_cell">
     <c:choose>
      <c:when test="${did.metadata.required==true}">
       <fmt:message key="yes" bundle="${resword}"/>
      </c:when>
      <c:otherwise>
        <fmt:message key="no" bundle="${resword}"/>
      </c:otherwise>
      </c:choose>
    </td>
  </tr>

 </c:forEach>
 </table>
 </div>
</div></div></div></div></div></div></div></div>

</div>
<br>
</c:forEach>
<br>

<c:import url="../include/workflow.jsp">
  <c:param name="module" value="manage"/>
 </c:import>

<jsp:include page="../include/footer.jsp"/>
