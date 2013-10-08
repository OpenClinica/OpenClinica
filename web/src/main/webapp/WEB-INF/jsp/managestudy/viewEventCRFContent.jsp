<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>


<c:choose>
<c:when test="${userBean.sysAdmin}">
 <c:import url="../include/admin-header.jsp"/>
</c:when>
<c:otherwise>
  <c:choose>
   <c:when test="${userRole.manageStudy}">
     <c:import url="../include/managestudy-header.jsp"/>
   </c:when>
   <c:otherwise>
    <c:import url="../include/submit-header.jsp"/>
   </c:otherwise>
  </c:choose>
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

<jsp:useBean scope="request" id="toc" class="org.akaza.openclinica.bean.submit.DisplayTableOfContentsBean" />

<c:choose>
<c:when test="${userBean.sysAdmin}">
  <h1><span class="title_manage">
</c:when>
<c:otherwise>
  <h1>
    <c:choose>
      <c:when test="${userRole.manageStudy}">
       <span class="title_manage">
      </c:when>
      <c:otherwise>
       <span class="title_manage">
      </c:otherwise>
    </c:choose>
</c:otherwise>
</c:choose>
<fmt:message key="view_event_CRF" bundle="${resword}"/>: <c:out value="${studySub.label}"/>-<c:out value="${toc.crf.name}"/>
</span>
</h1>

<fmt:message key="click_view_icon_for_each_section" bundle="${restext}"/></p>
<p>
<div class="homebox_bullets"><a href="ViewEventCRF?id=<c:out value="${toc.eventCRF.id}"/>&studySubId=<c:out value="${studySub.id}"/>"><fmt:message key="view_event_CRF_properties" bundle="${resworkflow}"/></a></div>
<p>
<p>
<div class="homebox_bullets" style="width:117">
     <a href="javascript:processPrintCRFRequest('rest/metadata/html/print/<c:out value="${study.oid}"/>/<c:out value="${dec.eventCRF.id}"/>/*')"
					onMouseDown="javascript:setImage('bt_Print<c:out value="${rowCount}"/>','images/bt_Print_d.gif');"
					onMouseUp="javascript:setImage('bt_Print<c:out value="${rowCount}"/>','images/bt_Print.gif');">
					<img name="bt_Print<c:out value="${rowCount}"/>" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print_out_form" bundle="${resword}"/>" title="<fmt:message key="print_out_form" bundle="${resword}"/>" align="right" hspace="6"></a>
     <a href="javascript:processPrintCRFRequest('rest/metadata/html/print/<c:out value="${study.oid}"/>/<c:out value="${dec.eventCRF.id}"/>/*')"
					
					<fmt:message key="print_entire_CRF" bundle="${resword}"/></a>

</div>
					<p>
<c:choose>
  <c:when test="${userBean.sysAdmin}">
   <div class="table_title_Admin">
  </c:when>
  <c:otherwise>
     <c:choose>
      <c:when test="${userRole.manageStudy}">
       <div class="table_title_manage">
      </c:when>
      <c:otherwise>
       <div class="table_title_submit">
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
<fmt:message key="section_properties" bundle="${resword}"/></div>

<c:choose>
	<c:when test="${empty toc.sections}">
		<br/><fmt:message key="there_are_not_sections_in_this_CRF" bundle="${resword}"/>
	</c:when>
	<c:otherwise>
	<div style="width: 600px">

	<!-- These DIVs define shaded box borders -->
		<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

			<div class="tablebox_center">
<!-- Table Contents -->
<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<tr>
		<td class="table_header_row"><fmt:message key="section_title" bundle="${resword}"/></td>
		<td class="table_header_row"><fmt:message key="uncompleted_items" bundle="${resword}"/></td>

		<c:if test="${toc.eventDefinitionCRF.doubleEntry}">
		<td class="table_header_row"><fmt:message key="items_pending_validation" bundle="${resword}"/></td>
		</c:if>

		<td class="table_header_row"><fmt:message key="completed_items" bundle="${resword}"/></td>
		<td class="table_header_row"><fmt:message key="total_items" bundle="${resword}"/></td>
		<td class="table_header_row"><fmt:message key="actions" bundle="${resword}"/></td>
	</tr>
	<c:set var="rowCount" value="${0}" />
	<c:forEach var="section" items="${toc.sections}">
		<c:set var="actionLink" value="ViewSectionDataEntry?sectionId=${section.id}&ecId=${toc.eventCRF.id}" />

		<%-- set the action label --%>

		<tr>
			<td class="table_cell"><c:out value="${section.title}"/></td>
            <td class="table_cell"><c:out value="${section.numItems - section.numItemsNeedingValidation - section.numItemsCompleted}" /></td>

			<c:if test="${toc.eventDefinitionCRF.doubleEntry}">
			<td class="table_cell"><c:out value="${section.numItemsNeedingValidation}" /></td>
			</c:if>

			<td class="table_cell"><c:out value="${section.numItemsCompleted}" /></td>
			<td class="table_cell"><c:out value="${section.numItems}" /></td>
			<td class="table_cell">
			 <table border="0" cellpadding="0" cellspacing="0">
			 <tr>
			  <td>
				<a href="<c:out value="${actionLink}"/>"
					onMouseDown="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View_d.gif');"
					onMouseUp="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');">
					<img name="bt_View<c:out value="${rowCount}"/>" src="images/bt_View.gif" border="0" alt="<fmt:message key="view_data_entry" bundle="${resword}"/>" title="<fmt:message key="view_data_entry" bundle="${resword}"/>" align="left" hspace="6"></a>
			  </td>
			  <td>
			    <a href="javascript:openDocWindow('ViewSectionDataEntry?sectionId=<c:out value="${section.id}"/>&ecId=<c:out value="${toc.eventCRF.id}"/>&print=yes')"
					onMouseDown="javascript:setImage('bt_Print<c:out value="${rowCount}"/>','images/bt_Print_d.gif');"
					onMouseUp="javascript:setImage('bt_Print<c:out value="${rowCount}"/>','images/bt_Print.gif');">
					<img name="bt_Print<c:out value="${rowCount}"/>" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print_out_form" bundle="${resword}"/>" title="<fmt:message key="print_out_form" bundle="${resword}"/>" align="left" hspace="6"></a>
			  </td>
			  </tr>
			</table>
			</td>
		</tr>
		<c:set var="rowCount" value="${rowCount + 1}" />
	</c:forEach>
</table>
</div>

</div></div></div></div></div></div></div></div>

</div>
</c:otherwise>
</c:choose>

<c:choose>
  <c:when test="${userBean.sysAdmin}">
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
