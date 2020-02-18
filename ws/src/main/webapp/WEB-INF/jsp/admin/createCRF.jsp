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
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${restext}"/></b>

		<div class="sidebar_tab_content">
        <fmt:message key="if_created_CRF_using_excel" bundle="${restext}"/><br><br>


		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${restext}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='crf' class='org.akaza.openclinica.bean.admin.CRFBean'/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>

<h1><span class="title_manage"><fmt:message key="create_a_new_CRF_case_report_form" bundle="${resworkflow}"/>
 </span>
</h1>
<p class="aka_para"><fmt:message key="can_download_blank_CRF_excel" bundle="${restext}"/><a href="DownloadVersionSpreadSheet?template=1"><b><fmt:message key="here" bundle="${resword}"/></b></a>.</p>
 <p class="aka_para"><strong><fmt:message key="CRF_name_case_sensitive" bundle="${restext}"/></strong></p>

<div style="font-family: Tahoma, Arial, Helvetica, Sans-Serif;font-size:12px;">
    <p><fmt:message key="openclinica_excel_support" bundle="${restext}"/></p>

 </div>


<P>
<P><fmt:message key="field_required" bundle="${resword}"/></P>
<form action="CreateCRF" method="post">
<input type="hidden" name="action" value="confirm">
<!-- These DIVs define shaded box borders -->
<div style="width: 450px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">
  <tr valign="top"><td class="formlabel"><fmt:message key="CRF_name" bundle="${resword}"/></td><td>
  <div class="formfieldXL_BG"><input type="text" name="name" value="<c:out value="${crf.name}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="name"/></jsp:include></td><td class="formlabel">*</td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="CRF_description" bundle="${resword}"/></td><td>
  <div class="formtextareaXL4_BG">
  <textarea class="formtextareaXL4" name="description" rows="4" cols="50"><c:out value="${crf.description}"/></textarea>
  </div><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="description"/></jsp:include></td></tr>

</table>
  </div>
</div></div></div></div></div></div></div></div>

</div>
</div>
  <input type="submit" name="Submit" value="<fmt:message key="submit_CRF" bundle="${resword}"/>" class="button_long">
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
