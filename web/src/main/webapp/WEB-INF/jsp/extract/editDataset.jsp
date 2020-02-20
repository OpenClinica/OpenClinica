<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="../include/extract-header.jsp"/>


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


<h1><span class="title_manage"><fmt:message key="edit_dataset" bundle="${resword}"/>: <c:out value='${dataset.name}'/></span></h1>

<form action="EditDataset" method="post">
<input type="hidden" name="action" value="validate"/>
<input type="hidden" name="dsId" value="<c:out value='${dataset.id}'/>"/>
<input type ="hidden" name="dsStatus" value="<c:out value='${dsStatusId}'/>">
<div style="width: 450px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top"><td class="table_header_column"><fmt:message key="name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${dataset.name}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="description" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${dataset.description}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="item_status" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${dataset.datasetItemStatus.description}"/>
  </td></tr>
</table>
 </div>
</div></div></div></div></div></div></div></div>
</div>
</form>
<br><br>

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


<form action="CreateDataset" method="post" name="cl">
<input type="hidden" name="action" value="beginsubmit"/>
<input type="hidden" name="crfId" value="-1">
<input type="hidden" name="defId" value="<c:out value="${definition.id}"/>">
<P><B><fmt:message key="show_items_this_dataset" bundle="${restext}"/></b></p>
<table border="0" cellpadding="0" cellspacing="0" >
  <tr>
   <td><input type="submit" name="save" value="<fmt:message key="add_or_remove_items" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="submit" name="saveContinue" value="<fmt:message key="continue_to_define_scope" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="button" onclick="confirmCancel('ViewDatasets');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/></td>
  </tr>
</table>
<br>

<jsp:include page="selected.jsp"/>

<table border="0" cellpadding="0" cellspacing="0" >
  <tr>
   <td><input type="submit" name="save" value="<fmt:message key="add_or_remove_items" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="submit" name="saveContinue" value="<fmt:message key="continue_to_define_scope" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="button" onclick="confirmCancel('ViewDatasets');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/></td>
  </tr>
</table>
</form>
<br><br><br>

<c:import url="../include/workflow.jsp">
   <c:param name="module" value="extract"/>
</c:import>
<jsp:include page="../include/footer.jsp"/>
