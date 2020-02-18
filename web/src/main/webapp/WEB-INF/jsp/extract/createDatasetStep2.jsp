<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="../include/extract-header.jsp"/>

<script language="JavaScript">
<!--

function selectAll() {
    if (document.cl.all.checked) {
      for (var i=0; i <document.cl.elements.length; i++) {
        if (document.cl.elements[i].name.indexOf('itemSelected') != -1) {
            document.cl.elements[i].checked = true;
        }
      }
    } else {
      for (var i=0; i <document.cl.elements.length; i++) {
        if (document.cl.elements[i].name.indexOf('itemSelected') != -1) {
            document.cl.elements[i].checked = false;
        }
      }
    }
}
function notSelectAll() {
    if (!this.checked){
        document.cl.all.checked = false;
    }

}
//-->
</script>

<%--<jsp:include page="../include/sidebar.jsp"/>--%>
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

<jsp:include page="../include/createDatasetSideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="eventlist" class="java.util.HashMap"/>

<c:choose>
<c:when test="${newDataset.id>0}">
<h1>
 <span class="title_manage">
   <fmt:message key="edit_dataset" bundle="${resword}"/> - <fmt:message key="select_items" bundle="${resword}"/>: <c:out value="${newDataset.name}"/>
 </span>
</h1>
</c:when>
<c:otherwise>
<h1>
 <span class="title_manage">
   <fmt:message key="create_dataset" bundle="${resword}"/>: <fmt:message key="select_items" bundle="${resword}"/>
 </span>
</h1>
</c:otherwise>
</c:choose>

<%--
<jsp:include page="createDatasetBoxes.jsp" flush="true">
<jsp:param name="selectStudyEvents" value="1"/>
</jsp:include>
--%>

<p><fmt:message key="please_select_one_CRF_from_the" bundle="${restext}"/> <b><fmt:message key="left_side_info_panel" bundle="${restext}"/></b><fmt:message key="select_items_in_CRF_include_dataset" bundle="${restext}"/></p>
<p><fmt:message key="click_event_subject_attributes_specify" bundle="${restext}"/></p>
<table border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td><img src="images/arrow_left.gif" alt="<fmt:message key="select_CRF_on_the_left" bundle="${restext}"/>" title="<fmt:message key="select_CRF_on_the_left" bundle="${restext}"/>"></td>
    <td>
      <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
		<div class="textbox_center" align="center">
			<span class="title_extract">
				 <b><fmt:message key="use_task_pane_to_select_CRF" bundle="${restext}"/></b>
			</span>
		</div>

	</div></div></div></div></div></div></div></div>

    </td>
  </tr>
</table>

<c:if test="${crf != null && crf.id>0}">

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center" align="center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tr valign="top" ><td class="table_header_column"><fmt:message key="event_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${definition.name}"/>
   </td></tr>
 <tr valign="top" ><td class="table_header_column"><fmt:message key="CRF_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${crf.name}"/>
   </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="description" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${crf.description}"/>
  </td></tr>
 </table>
</div>

</div></div></div></div></div></div></div></div>
</div>

</c:if>
<br>
<c:if test="${!empty allItems}">
<form action="CreateDataset" method="post" name="cl">
<input type="hidden" name="action" value="beginsubmit"/>
<input type="hidden" name="crfId" value="<c:out value="${crf.id}"/>">
<input type="hidden" name="defId" value="<c:out value="${definition.id}"/>">

<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="all"/></jsp:include>
<table border="0" cellpadding="0" cellspacing="0" >
  <tr>
  <td><input type="checkbox" name="all" value="1"
	onClick="javascript:selectAll();"> <fmt:message key="select_all_items" bundle="${resword}"/>&nbsp;&nbsp;&nbsp;</td>
   <td><input type="submit" name="save" value="<fmt:message key="save_and_add_more_items" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="submit" name="saveContinue" value="<fmt:message key="save_and_define_scope" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="button" onclick="confirmCancel('ViewDatasets');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/></td>
  </tr>
</table>
<br/>
<P><B><fmt:message key="show_items_this_dataset" bundle="${restext}"/></b></p>
<!--javascript to select all-->
<div style="width: 100%">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center" align="center">
<%-- TODO: i18n --%>
<table border="0" cellpadding="0" cellspacing="0" width="100%">
 <tr>
    <td class="table_header_column_top">&nbsp;</td>
    <td class="table_header_column_top"><fmt:message key="name" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="description" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="version2" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="section_s" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="group_s" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="data_type" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="units" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="response_type" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="response_label" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="PHI" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="required" bundle="${resword}"/>?</td>
    <td class="table_header_column_top"><fmt:message key="validation" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="default_value" bundle="${resword}"/></td>
    <td class="table_header_column_top"><fmt:message key="max_repeats" bundle="${resword}"/></td>

  </tr>
<c:set var="count" value="0"/>
<c:forEach var='item' items='${allItems}'>
  <tr>
   <td class="table_cell">
   <c:choose>
    <c:when test="${item.selected}">
      <input type="checkbox" name="itemSelected<c:out value="${count}"/>" checked value="yes" onclick="javascript:notSelectAll();">
    </c:when>
    <c:otherwise>
      <input type="checkbox" name="itemSelected<c:out value="${count}"/>" value="yes" onclick="javascript:notSelectAll();">
    </c:otherwise>
   </c:choose>
   </td>
   <td class="table_cell"><a href="javascript: openDocWindow('ViewItemDetail?itemId=<c:out value="${item.id}"/>')"><c:out value="${item.name}"/></a></td>
   <td class="table_cell"><c:out value="${item.description}"/>&nbsp;</td>
   <td class="table_cell">
      <c:forEach var="meta" items="${item.itemMetas}" varStatus="status">
        <c:choose>
          <c:when test="${status.last}">
           <c:out value="${meta.crfVersionName}"/>
          </c:when>
          <c:otherwise>
           <c:out value="${meta.crfVersionName}"/>,<br>
          </c:otherwise>
        </c:choose>
      </c:forEach>
      &nbsp;
    </td>

   <%-- SECTION --%>

   <td class="table_cell">
   <c:forEach var="meta" items="${item.itemMetas}" varStatus="status">
        <c:choose>
          <c:when test="${status.last}">
           <c:out value="${meta.sectionName}"/>
          </c:when>
          <c:otherwise>
            <c:out value="${meta.sectionName}"/>,<br>
          </c:otherwise>
        </c:choose>
      </c:forEach>
   &nbsp;</td>

   <%-- GROUP --%>

   <%--
   <td class="table_cell"><c:out value="${item.itemMeta.groupLabel}"/>&nbsp;</td>
   --%>

   <td class="table_cell">
      <c:forEach var="meta" items="${item.itemMetas}" varStatus="status">
        <c:choose>
          <c:when test="${meta.groupLabel==null || meta.groupLabel==''}"></c:when>
          <c:when test="${status.last}">
           <c:out value="${meta.groupLabel}" default="Ungrouped"/>
          </c:when>
          <c:otherwise>
            <c:out value="${meta.groupLabel}" default="Ungrouped"/>,<br>
          </c:otherwise>
        </c:choose>
      </c:forEach>
    &nbsp;</td>

   <%-- DATA TYPE --%>

   <td class="table_cell"><c:out value="${item.dataType.name}"/>&nbsp;</td>

   <%-- UNITS --%>

   <td class="table_cell"><c:out value="${item.units}"/>&nbsp;</td>

   <%-- RESPONSE TYPE --%>

   <%-- <td class="table_cell"><c:out value="${item.itemMeta.responseSet.responseType.name}"/>&nbsp;</td>--%>

    <td class="table_cell">
      <c:forEach var="meta" items="${item.itemMetas}" varStatus="status">
        <c:choose>
          <c:when test="${status.last}">
           <c:out value="${meta.responseSet.responseType.name}"/>
          </c:when>
          <c:otherwise>
            <c:out value="${meta.responseSet.responseType.name}"/>,<br>
          </c:otherwise>
        </c:choose>
      </c:forEach>
      &nbsp;
    </td>

   <%-- response set label --%>

    <td class="table_cell">
      <c:forEach var="meta" items="${item.itemMetas}" varStatus="status">
        <c:choose>
          <c:when test="${status.last}">
           <c:out value="${meta.responseSet.label}"/>
          </c:when>
          <c:otherwise>
            <c:out value="${meta.responseSet.label}"/>,<br>
          </c:otherwise>
        </c:choose>
      </c:forEach>
      &nbsp;
    </td>

    <%-- PHI --%>

    <td class="table_cell">
     <c:choose>
      <c:when test="${item.phiStatus}">
        <fmt:message key="yes" bundle="${resword}"/>
      </c:when>
      <c:otherwise>
        <fmt:message key="no" bundle="${resword}"/>
      </c:otherwise>
    </c:choose>
    &nbsp;
   </td>

   <%-- REQUIRED --%>

   <td class="table_cell">
   	<c:forEach var="meta" items="${item.itemMetas}" varStatus="status">
     <c:choose>

      	<c:when test="${!status.last}"></c:when>
      	<c:when test="${meta.required}">
        	Yes
      	</c:when>
      	<c:otherwise>
        	No
      	</c:otherwise>

    </c:choose>

    </c:forEach>
   &nbsp;</td>

   <%-- VALIDATION --%>

   <td class="table_cell">
      <c:forEach var="meta" items="${item.itemMetas}" varStatus="status">
        <c:choose>
          <c:when test="${meta.regexp==null || meta.regexp==''}"></c:when>
          <c:when test="${status.last}">
           <c:out value="${meta.regexp}"/>
          </c:when>
          <c:otherwise>
            <c:out value="${meta.regexp}"/>,<br>
          </c:otherwise>
        </c:choose>
      </c:forEach>
    &nbsp;</td>

   <%-- DEFAULT VALUE --%>

   <td class="table_cell">
      <c:forEach var="meta" items="${item.itemMetas}" varStatus="status">
        <c:choose>
          <c:when test="${meta.defaultValue==null || meta.defaultValue==''}"></c:when>
          <c:when test="${status.last}">
           <c:out value="${meta.defaultValue}" default="None"/>
          </c:when>
          <c:otherwise>
            <c:out value="${meta.defaultValue}" default="None"/>,<br>
          </c:otherwise>
        </c:choose>
      </c:forEach>
    &nbsp;</td>

   <%-- MAX REPEATS --%>

   <td class="table_cell">
   <c:forEach var="meta" items="${item.itemMetas}" varStatus="status">
        <c:choose>
          <c:when test="${status.last}">
           <c:out value="${meta.repeatMax}"/>
          </c:when>
          <c:otherwise>
            <c:out value="${meta.repeatMax}"/>,<br>
          </c:otherwise>
        </c:choose>
      </c:forEach>
   &nbsp;</td>

  </tr>
  <c:set var="count" value="${count+1}"/>
</c:forEach>
</table>
</div>

</div></div></div></div></div></div></div></div>
</div>
<table border="0" cellpadding="0" cellspacing="0" >
  <tr>
   <td><input type="submit" name="save" value="<fmt:message key="save_and_add_more_items" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="submit" name="saveContinue" value="<fmt:message key="save_and_define_scope" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="button" onclick="confirmCancel('ViewDatasets');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/></td>
  </tr>
</table>
</form>
</c:if>

<c:import url="../include/workflow.jsp">
   <c:param name="module" value="extract"/>
</c:import>
<jsp:include page="../include/footer.jsp"/>
