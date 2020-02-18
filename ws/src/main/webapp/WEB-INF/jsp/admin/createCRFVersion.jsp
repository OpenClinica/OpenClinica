<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>

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

<tr id="sidebar_Instructions_open" style="display: inherit">

		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open');
		leftnavExpand('sidebar_Instructions_closed');">
            <img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">
		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open');
		leftnavExpand('sidebar_Instructions_closed');">
            <img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>


<jsp:useBean scope='session' id='version' class='org.akaza.openclinica.bean.submit.CRFVersionBean'/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='crfName' class='java.lang.String'/>

<h1>
<span class="title_manage">
 <c:choose>
     <c:when test="${empty crfName}">
         <fmt:message key="create_a_new_CRF_case_report_form" bundle="${resworkflow}"/>
     </c:when>
     <c:otherwise>
        <fmt:message key="create_CRF_version" bundle="${resworkflow}"/> <c:out value="${crfName}"/>
     </c:otherwise>
 </c:choose>

</span>
</h1>

<script type="text/JavaScript" language="JavaScript">
  <!--
 function myCancel() {

    cancelButton=document.getElementById('cancel');
    if ( cancelButton != null) {
      if(confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>')) {
       window.location.href="ListCRF?module=" + "<c:out value="${module}"/>";
       return true;
      } else {
        return false;
       }
     }
     return true;

  }
   //-->
</script>

<p><fmt:message key="can_download_blank_CRF_excel" bundle="${restext}"/><a href="DownloadVersionSpreadSheet?template=1"><b><fmt:message key="here" bundle="${resword}"/></b></a>.</p>

<div style="font-family: Tahoma, Arial, Helvetica, Sans-Serif;font-size:12px;">
    <p><fmt:message key="openclinica_excel_support" bundle="${restext}"/></p>

 </div>




<form action="CreateCRFVersion?action=confirm&crfId=<c:out value="${version.crfId}"/>&name=<c:out value="${version.name}"/>" method="post" ENCTYPE="multipart/form-data">
<div style="width: 400px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">

<tr>
<td class="formlabel"><fmt:message key="ms_excel_file_to_upload" bundle="${resword}"/>:</td>
<td><div class="formfieldFile_BG"><input type="file" name="excel_file"></div>
<br><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="excel_file"/></jsp:include></td>
</tr>
<input type="hidden" name="crfId" value="<c:out value="${version.crfId}"/>">


</table>

</div>

</div></div></div></div></div></div></div></div>
</div>

<br clear="all">
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
<input type="submit" value="<fmt:message key="preview_CRF_version" bundle="${resword}"/>" class="button_long">
</td>
<td>
<input type="button" onclick="confirmExit('ListCRF?module=<c:out value="${module}"/>')" name="exit" value="<fmt:message key="exit" bundle="${resword}"/>   "class="button_medium"/>
</tr></table>
</form>

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
