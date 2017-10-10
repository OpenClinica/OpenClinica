<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

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


<jsp:include page="../include/extract-header.jsp"/>


<%--<jsp:include page="../include/sidebar.jsp"/>--%>
<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">

    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></a>

    <fmt:message key="instructions" bundle="${resword}"/>

    <div class="sidebar_tab_content">

    </div>

    </td>

  </tr>
  <tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">

    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></a>

    <fmt:message key="instructions" bundle="${resword}"/>

    </td>
  </tr>

<jsp:include page="../include/createDatasetSideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="eventlist" class="java.util.HashMap"/>

<c:choose>
<c:when test="${newDataset.id>0}">
<h1><span class="title_manage"><fmt:message key="edit_dataset" bundle="${resword}"/> - <fmt:message key="view_selected_items" bundle="${resword}"/> <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/edit-dataset')"><span class="" border="0" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="help" bundle="${resword}"/>"></a>
: <c:out value='${newDataset.name}'/></span></h1>
</c:when>
<c:otherwise>
<h1><span class="title_manage"><fmt:message key="create_dataset" bundle="${resword}"/>: <fmt:message key="view_selected_items" bundle="${resword}"/> <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/create-dataset')"><span class="" border="0" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="help" bundle="${resword}"/>"></a></span></h1>
</c:otherwise>
</c:choose>

<c:if test="${newDataset.id<=0}">
<p><fmt:message key="can_view_items_selected_inclusion" bundle="${restext}"/><fmt:message key="select_all_items_inclusion_clicking" bundle="${restext}"/></p>
<form action="EditSelected" method="post" name="cl">
 <input type="hidden" name="all" value="1">
 <input type="submit" name="submit" value="<fmt:message key="select_all_items_in_study" bundle="${resword}"/>" class="button_xlong" onClick='return confirm("<fmt:message key="there_a_total_of" bundle="${resword}"><fmt:param value="${numberOfStudyItems}"/></fmt:message>")' />
</form>

<br><br>
</c:if>
<c:if test="${!empty allSelectedItems}">
<form action="CreateDataset" method="post" name="cl">
<input type="hidden" name="action" value="beginsubmit"/>
<input type="hidden" name="crfId" value="-1">
<input type="hidden" name="defId" value="<c:out value="${definition.id}"/>">
<p><b><fmt:message key="show_items_this_dataset" bundle="${restext}"/></b></p>
<table border="0" cellpadding="0" cellspacing="0" >
  <tr>
   <td><input type="submit" name="save" value="<fmt:message key="save_and_add_more_items" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="submit" name="saveContinue" value="<fmt:message key="save_and_define_scope" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="button" onclick="confirmCancel('ViewDatasets');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/></td>
  </tr>
</table>
<br>

<jsp:include page="selected.jsp"/>

<table border="0" cellpadding="0" cellspacing="0" >
  <tr>
   <td><input type="submit" name="save" value="<fmt:message key="save_and_add_more_items" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="submit" name="saveContinue" value="<fmt:message key="save_and_define_scope" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="button" onclick="confirmCancel('ViewDatasets');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/></td>
  </tr>
</table>
</form>
</c:if>
<br><br><br>

<jsp:include page="../include/footer.jsp"/>
