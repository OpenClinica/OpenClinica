<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>


<jsp:include page="../include/admin-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">
        <fmt:message key="enter_the_study_and_protocol" bundle="${resword}"/>
		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='newStudy' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>

<h1><span class="title_manage">
<fmt:message key="create_a_new_study_continue" bundle="${resword}"/>
</span></h1>
<script type="text/JavaScript" language="JavaScript">
  <!--
<%-- function myCancel() {

    cancelButton=document.getElementById('cancel');
    if ( cancelButton != null) {
      if(confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>')) {
        window.location.href="ListStudy";
       return true;
      } else {
        return false;
       }
     }
     return true;

  }--%>
   //-->
</script>
<form action="CreateStudy" method="post">
<span class="title_Admin"><p><b><fmt:message key="section_e_related_information" bundle="${resword}"/></b></p></span>
* <fmt:message key="indicates_required_field" bundle="${resword}"/><br>
<input type="hidden" name="action" value="next">
<input type="hidden" name="pageNum" value="6">
<div style="width: 600px">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0">

  <tr valign="top"><td class="formlabel"><fmt:message key="MEDLINE_identifier" bundle="${resword}"/>:</td><td>
  <div class="formfieldXL_BG"><input type="text" name="medlineIdentifier" value="<c:out value="${newStudy.medlineIdentifier}"/>" class="formfieldXL"></div>
   <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="medlineIdentifier"/></jsp:include>
   </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="results_reference" bundle="${resword}"/>:</td><td>
   <div class="formfieldS_BG"><select name="resultsReference" class="formfieldS">
    <c:choose>
     <c:when test="${newStudy.resultsReference == true}">
      <option value="1" selected><fmt:message key="yes" bundle="${resword}"/></option>
      <option value="0"><fmt:message key="no" bundle="${resword}"/></option>
     </c:when>
     <c:otherwise>
      <option value="1"><fmt:message key="yes" bundle="${resword}"/></option>
      <option value="0" selected><fmt:message key="no" bundle="${resword}"/></option>
     </c:otherwise>
    </c:choose>
   </select></div>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="URL_reference" bundle="${resword}"/>:</td><td>
  <div class="formfieldXL_BG"><input type="text" name="url" value="<c:out value="${newStudy.url}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="url"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="URL_description" bundle="${resword}"/>:</td><td>
  <div class="formfieldXL_BG"><input type="text" name="urlDescription" value="<c:out value="${newStudy.urlDescription}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="urlDescription"/></jsp:include>
  </td></tr>

</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
 <table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
 <input type="submit" name="Submit" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_medium">
</td>
<td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onClick="myCancel('<fmt:message key="sure_to_cancel" bundle="${resword}"/>');"/></td>
</tr>
</table>
</form>
<br><br>

<!-- EXPANDING WORKFLOW BOX -->

<table border="0" cellpadding="0" cellspacing="0" style="position: relative; left: -14px;">
	<tr>
		<td id="sidebar_Workflow_closed" style="display: none">
		<a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="images/<fmt:message key="image_dir" bundle="${resformat}"/>/tab_Workflow_closed.gif" border="0"></a>
	</td>
	<td id="sidebar_Workflow_open" style="display: all">
	<table border="0" cellpadding="0" cellspacing="0" class="workflowBox">
		<tr>
			<td class="workflowBox_T" valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="workflow_tab">
					<a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

					<b><fmt:message key="workflow" bundle="${resword}"/></b>

					</td>
				</tr>
			</table>
			</td>
			<td class="workflowBox_T" align="right" valign="top"><img src="images/workflowBox_TR.gif"></td>
		</tr>
		<tr>
			<td colspan="2" class="workflowbox_B">
			<div class="box_R"><div class="box_B"><div class="box_BR">
				<div class="workflowBox_center">


		<!-- Workflow items -->

				<table border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">

							<fmt:message key="study_title_description" bundle="${resword}"/><br><br>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">


							<fmt:message key="status_and_design" bundle="${resword}"/>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">

					       <fmt:message key="conditions_and_eligibility" bundle="${resword}"/>

							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">


							<fmt:message key="specify_facility" bundle="${resword}"/>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">

					        <b><fmt:message key="other_related_information" bundle="${resword}"/></b>

							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">

					         <fmt:message key="study_parameter_configuration" bundle="${resword}"/>

							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">


							 <fmt:message key="confirm_and_submit_study" bundle="${resword}"/>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>

					</tr>
				</table>


		<!-- end Workflow items -->

				</div>
			</div></div></div>
			</td>
		</tr>
	</table>
	</td>
   </tr>
</table>

<!-- END WORKFLOW BOX -->
<jsp:include page="../include/footer.jsp"/>
