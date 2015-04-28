<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<c:import url="../include/admin-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10" alt="-" title=""></a>

		<b>Instructions</b>

		<div class="sidebar_tab_content">

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10" alt="v" title=""></a>

		<b>Instructions</b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='request' id='eventCRFs' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='itemDataForVersion' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='eventsForVersion' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='version' class='org.akaza.openclinica.bean.submit.CRFVersionBean'/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<h1><span class="title_manage">
<fmt:message key="confirm_deletion_of_CRF_version" bundle="${resword}"/></span>
</h1>
<p>
<fmt:message key="you_choose_to_delete_the_following" bundle="${restext}"/>
</p>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center" align="center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top" ><td class="table_header_column_top"><fmt:message key="name" bundle="${resword}"/>:</td><td class="table_cell_top">
  <c:out value="${version.name}"/>
   </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="description" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${version.description}"/>
  </td></tr>

</table>
</div>

</div></div></div></div></div></div></div></div>
</div>
<br/>
<c:if test="${!empty definitions}">
<span class="table_title_Admin">
<fmt:message key="associated_ED" bundle="${resword}"/></span>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center" align="center">
 <table border="0" cellpadding="0" cellspacing="0" width="100%">
   <tr valign="top">
    <td class="table_header_row_left"><fmt:message key="SE" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="study_ID" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="date_created" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="owner" bundle="${resword}"/></td>
   </tr>
  <c:forEach var="eventDefinitionCRF" items="${definitions}">
    <tr valign="top">
    <td class="table_cell_left"><c:out value="${eventDefinitionCRF.eventName}"/></td>
      <td class="table_cell"><c:out value="${eventDefinitionCRF.studyId}"/></td>
    <td class="table_cell">
      <c:if test="${eventDefinitionCRF.createdDate != null}">
      <fmt:formatDate value="${eventDefinitionCRF.createdDate}" pattern="${dteFormat}"/>
      </c:if>&nbsp;
    </td>
    <td class="table_cell"><c:out value="${eventDefinitionCRF.owner.name}"/></td>
    </tr>
 </c:forEach>
</table>
</div>
</div></div></div></div></div></div></div></div>
</div>
</c:if>
<br>
<c:if test="${!empty eventsForVersion}">
<span class="table_title_Admin">
<fmt:message key="associated_event_CRFs" bundle="${resword}"/></span>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center" align="center">
 <table border="0" cellpadding="0" cellspacing="0" width="100%">
   <tr valign="top">
    <td class="table_header_row_left"><fmt:message key="SE_ID" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="date_interviewed" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
   </tr>
  <c:forEach var="eventCRF" items="${eventsForVersion}">
    <tr valign="top">
    <td class="table_cell_left"><c:out value="${eventCRF.studyEventId}"/></td>
    <td class="table_cell">
      <c:if test="${eventCRF.dateInterviewed != null}">
      <fmt:formatDate value="${eventCRF.dateInterviewed}" pattern="${dteFormat}"/>
      </c:if>&nbsp;
    </td>
    <td class="table_cell"><c:out value="${eventCRF.status.name}"/></td>
    </tr>
 </c:forEach>
</table>
</div>
</div></div></div></div></div></div></div></div>
</div>
</c:if>
<br/>
<c:if test="${!empty itemDataForVersion}">
<span class="table_title_Admin">
<fmt:message key="associated_item_Data" bundle="${resword}"/></span>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center" align="center">
 <table border="0" cellpadding="0" cellspacing="0" width="100%">
   <tr valign="top">
    <td class="table_header_row_left"><fmt:message key="study_event_definition" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="event_ordinal" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="study_subject" bundle="${resword}"/></td>
   </tr>
  <c:forEach var="eCRF" items="${eventCRFs}">
    <tr valign="top">
    <td class="table_cell_left"><c:out value="${eCRF.studyEvent.studyEventDefinition.name}"/></td>
    <td class="table_cell"><c:out value="${eCRF.studyEvent.sampleOrdinal}"/></td>
    <td class="table_cell"><c:out value="${eCRF.studySubject.label}"/></td>
    </tr>
 </c:forEach>
</table>
</div>
</div></div></div></div></div></div></div></div>
</div>
</c:if>

<br/>
<c:choose>
 <c:when test="${empty eventsForVersion && empty definitions && empty itemDataForVersion}">
  <form action='DeleteCRFVersion?action=submit&verId=<c:out value="${version.id}"/>' method="POST">
   <input type="submit" name="submit" value="<fmt:message key="delete_CRF_version" bundle="${resword}"/>" class="button_xlong" onClick='return confirm("<fmt:message key="if_you_delete_this_CRF_version" bundle="${restext}"/>");'>
      &nbsp;
   <input type="button" onclick="confirmCancel('ListCRF');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
  </form>
 </c:when>
 <c:otherwise>
  <p><a href="ListCRF"><fmt:message key="go_back_to_CRF_list" bundle="${resword}"/></a></p>
 </c:otherwise>
</c:choose>

<jsp:include page="../include/footer.jsp"/>
