<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.admin" var="resadmin"/>


<jsp:include page="../include/admin-header.jsp"/>


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

<jsp:useBean scope='session' id='newStudy' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope ="request" id="interPurposeMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="allocationMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="maskingMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="controlMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="assignmentMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="endpointMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="interTypeMap" class="java.util.HashMap"/>
<jsp:useBean scope ="session" id="interventions" class="java.util.ArrayList"/>
<jsp:useBean scope ="request" id="interventionError" class="java.lang.String"/>
<script type="text/JavaScript" language="JavaScript">
  <!--
 function myCancel() {

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

  }
   //-->
</script>
<h1><span class="title_manage">
<fmt:message key="update_study_details_continue" bundle="${resword}"/>
</span></h1>
<span class="title_Admin"><p><b><fmt:message key="section_b_study_status" bundle="${resword}"/> - <fmt:message key="design_details" bundle="${resword}"/> - <fmt:message key="interventional" bundle="${resword}"/></b></p></span>
<P>* <fmt:message key="indicates_required_field" bundle="${resword}"/></P>
<form action="UpdateStudy" method="post">
<input type="hidden" name="action" value="next">
<input type="hidden" name="pageNum" value="3">
<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0">
  <tr valign="top"><td class="formlabel"><fmt:message key="purpose" bundle="${resword}"/>:</td><td>
  <c:set var="purpose1" value="${newStudy.purpose}"/>
  <div class="formfieldXL_BG"><select name="purpose" class="formfieldXL">
   <c:forEach var="purpose" items="${interPurposeMap}">
       <c:set var="purposekey">
		<fmt:message key="${purpose.key}" bundle="${resadmin}"/>
       </c:set>
       <c:choose>
        <c:when test="${purpose1 == purposekey}">
         <option value="<c:out value="${purpose.key}"/>" selected><c:out value="${purpose.value}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${purposekey}"/>"><c:out value="${purpose.value}"/>
        </c:otherwise>
       </c:choose>
    </c:forEach>
  </select></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="purpose"/></jsp:include></td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="allocation" bundle="${resword}"/>:</td><td>
   <c:set var="allocation1" value="${newStudy.allocation}"/>
  <div class="formfieldXL_BG"><select name="allocation" class="formfieldXL">
   <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
   <c:forEach var="allocation" items="${allocationMap}">
       <c:set var="allocationkey">
		<fmt:message key="${allocation.key}" bundle="${resadmin}"/>
       </c:set>
       <c:choose>
        <c:when test="${allocation1 == allocationkey}">
         <option value="<c:out value="${allocation.key}"/>" selected><c:out value="${allocation.value}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${allocation.key}"/>"><c:out value="${allocation.value}"/>
        </c:otherwise>
       </c:choose>
    </c:forEach>
  </select></div>
  </td></tr>

  <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId'); return false;"><fmt:message key="masking" bundle="${resword}"/>:</td><td>
  <c:set var="masking1" value="${newStudy.masking}"/>
  <div class="formfieldXL_BG"><select name="masking" class="formfieldXL">
   <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
   <c:forEach var="masking" items="${maskingMap}">
       <c:set var="maskingkey">
		<fmt:message key="${masking.key}" bundle="${resadmin}"/>
       </c:set>

       <c:choose>
        <c:when test="${masking1 == maskingkey}">
         <option value="<c:out value="${masking.key}"/>" selected><c:out value="${masking.value}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${masking.key}"/>"><c:out value="${masking.value}"/>
        </c:otherwise>
       </c:choose>
    </c:forEach>
  </select></div>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="control" bundle="${resword}"/>:</td><td>
   <c:set var="control1" value="${newStudy.control}"/>
   <div class="formfieldXL_BG"><select name="control" class="formfieldXL">
   <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
    <c:forEach var="control" items="${controlMap}">
       <c:set var="controlkey">
		<fmt:message key="${control.key}" bundle="${resadmin}"/>
       </c:set>
       <c:choose>
        <c:when test="${control1 == controlkey}">
         <option value="<c:out value="${control.key}"/>" selected><c:out value="${control.value}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${control.key}"/>"><c:out value="${control.value}"/>
        </c:otherwise>
       </c:choose>
    </c:forEach>
  </select></div>
 </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="assignment" bundle="${resword}"/>:</td><td>
  <c:set var="assignment1" value="${newStudy.assignment}"/>
   <div class="formfieldXL_BG"><select name="assignment" class="formfieldXL">
   <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
    <c:forEach var="assignment" items="${assignmentMap}">
       <c:set var="assignmentkey">
		<fmt:message key="${assignment.key}" bundle="${resadmin}"/>
       </c:set>

       <c:choose>
        <c:when test="${assignment1 == assignmentkey}">
         <option value="<c:out value="${assignment.key}"/>" selected><c:out value="${assignment.value}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${assignment.key}"/>"><c:out value="${assignment.value}"/>
        </c:otherwise>
       </c:choose>
    </c:forEach>
  </select></div>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="endpoint" bundle="${resword}"/>:</td><td>
   <c:set var="endpoint1" value="${newStudy.endpoint}"/>
   <div class="formfieldXL_BG"><select name="endpoint" class="formfieldXL">
   <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
    <c:forEach var="endpoint" items="${endpointMap}">
       <c:set var="endpointkey">
		<fmt:message key="${endpoint.key}" bundle="${resadmin}"/>
       </c:set>

       <c:choose>
        <c:when test="${endpoint1 == endpointkey}">
         <option value="<c:out value="${endpoint.key}"/>" selected><c:out value="${endpoint.value}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${endpoint.key}"/>"><c:out value="${endpoint.value}"/>
        </c:otherwise>
       </c:choose>
    </c:forEach>
  </select></div>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="interventions" bundle="${resword}"/>:<br> (<fmt:message key="one_name_per_line" bundle="${resword}"/>)</td><td>
   <c:set var="count" value="0"/>

   <c:forEach var ="intervention" items="${interventions}">

     <fmt:message key="type" bundle="${resword}"/>:

     <c:set var="type1" value="${intervention.type}"/>
      <select name="interType<c:out value="${count}"/>">
       <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
       <c:forEach var="type" items="${interTypeMap}">
        <c:set var="typekey">
		<fmt:message key="${type.key}" bundle="${resadmin}"/>
       </c:set>
        <c:choose>
        <c:when test="${type1 == typekey}">
         <option value="<c:out value="${type.key}"/>" selected><c:out value="${type.value}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${type.key}"/>"><c:out value="${type.value}"/>
        </c:otherwise>
        </c:choose>
       </c:forEach>
      </select>
      <fmt:message key="name" bundle="${resword}"/>:<input type="text" name="interName<c:out value="${count}"/>" value="<c:out value="${intervention.name}"/>" size="30">
      <br>
     <c:set var="count" value="${count+1}"/>
   </c:forEach>
   <c:if test="${count < 9}">
    <c:forEach begin="${count}" end="9">
     <fmt:message key="type" bundle="${resword}"/>:
      <select name="interType<c:out value="${count}"/>">
        <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
        <c:forEach var="type" items="${interTypeMap}">
         <option value="<c:out value="${type.key}"/>"><c:out value="${type.value}"/>
        </c:forEach>
      </select>
     <fmt:message key="name" bundle="${resword}"/>:<input type="text" name="interName<c:out value="${count}"/>" value="" size="30"><br>
    <c:set var="count" value="${count+1}"/>
    </c:forEach>
   </c:if>
    <br>
   <span class="alert"><c:out value="${interventionError}"/></span>


</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
<input type="submit" name="Submit" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_medium">
</td>
<td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onClick="javascript:myCancel();"/></td>
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


							<b><fmt:message key="status_and_design" bundle="${resword}"/></b>


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

					        <fmt:message key="conditions_and_eligibility" bundle="${resword}"/><br><br>

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


							 <fmt:message key="other_related_information" bundle="${resword}"/>


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
