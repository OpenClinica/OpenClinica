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

<jsp:useBean scope='session' id='newStudy' class='core.org.akaza.openclinica.domain.datamap.Study'/>
<jsp:useBean scope ="request" id="studyPhaseMap" class="java.util.HashMap"/>
<jsp:useBean scope="request" id="statuses" class="java.util.ArrayList"/>
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
<h1><span class="title_manage"><fmt:message key="update_study_details_continue" bundle="${resword}"/></span></h1>

<span class="title_Admin"><p><b><fmt:message key="section_f_study_parameter_configuration" bundle="${resword}"/> </b></p></span>
<P>* <fmt:message key="indicates_required_field" bundle="${resword}"/></P>

<form action="UpdateStudy" method="post">
<input type="hidden" name="action" value="confirm">
<div style="width: 600px">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">

  <tr valign="top"><td class="formlabel"><fmt:message key="collect_subject_date_of_birth" bundle="${resword}"/>:</td><td>
   <c:choose>
   <c:when test="${newStudy.collectDob == '1'}">
    <input type="radio" checked name="collectDob" value="1"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="collectDob" value="2"><fmt:message key="only_year_of_birth" bundle="${resword}"/>
     <input type="radio" name="collectDob" value="3"><fmt:message key="not_used" bundle="${resword}"/>
   </c:when>
   <c:when test="${newStudy.collectDob == '2'}">
    <input type="radio" name="collectDob" value="1"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="collectDob" value="2"><fmt:message key="only_year_of_birth" bundle="${resword}"/>
     <input type="radio" name="collectDob" value="3"><fmt:message key="not_used" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <input type="radio" name="collectDob" value="1"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="collectDob" value="2"><fmt:message key="only_year_of_birth" bundle="${resword}"/>
    <input type="radio" checked name="collectDob" value="3"><fmt:message key="not_used" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="allow_discrepancy_management" bundle="${resword}"/>:</td><td>
   <c:choose>
   <c:when test="${newStudy.discrepancyManagement == 'false'}">
    <input type="radio" name="discrepancyManagement" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="discrepancyManagement" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <input type="radio" checked name="discrepancyManagement" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="discrepancyManagement" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="gender_required" bundle="${resword}"/>:</td><td>
   <c:choose>
   <c:when test="${newStudy.genderRequired == 'false'}">
    <input type="radio" name="genderRequired" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="genderRequired" value="false"><fmt:message key="no" bundle="${resword}"/>
    <input type="radio" name="genderRequired" value="not used"><fmt:message key="not_used" bundle="${resword}"/>
   </c:when>
   <c:when test="${newStudy.genderRequired == 'true'}">
    <input type="radio" name="genderRequired" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="genderRequired" value="false"><fmt:message key="no" bundle="${resword}"/>
    <input type="radio" name="genderRequired" value="not used"><fmt:message key="not_used" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <input type="radio" name="genderRequired" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="genderRequired" value="false"><fmt:message key="no" bundle="${resword}"/>
    <input type="radio" checked name="genderRequired" value="not used"><fmt:message key="not_used" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>
  <tr><td>&nbsp;</td></tr>
  <tr valign="top"><td class="formlabel"><fmt:message key="subject_person_ID_required" bundle="${resword}"/>:</td><td>
   <c:choose>
   <c:when test="${newStudy.subjectPersonIdRequired == 'required'}">
    <input type="radio" checked name="subjectPersonIdRequired" value="required"><fmt:message key="required" bundle="${resword}"/>
    <input type="radio" name="subjectPersonIdRequired" value="optional"><fmt:message key="optional" bundle="${resword}"/>
    <input type="radio" name="subjectPersonIdRequired" value="not used"><fmt:message key="not_used" bundle="${resword}"/>
   </c:when>
    <c:when test="${newStudy.subjectPersonIdRequired == 'optional'}">
    <input type="radio" name="subjectPersonIdRequired" value="required"><fmt:message key="required" bundle="${resword}"/>
    <input type="radio" checked name="subjectPersonIdRequired" value="optional"><fmt:message key="optional" bundle="${resword}"/>
    <input type="radio" name="subjectPersonIdRequired" value="not used"><fmt:message key="not_used" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <input type="radio" name="subjectPersonIdRequired" value="required"><fmt:message key="required" bundle="${resword}"/>
    <input type="radio" name="subjectPersonIdRequired" value="optional"><fmt:message key="optional" bundle="${resword}"/>
    <input type="radio" checked name="subjectPersonIdRequired" value="not used"><fmt:message key="not_used" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>


  <!--
   <tr valign="top"><td class="formlabel"><fmt:message key="generate_study_subject_ID_automatically" bundle="${resword}"/>:</td><td>
   <c:choose>
   <c:when test="${newStudy.subjectIdPrefixSuffix == 'true'}">
    <input type="radio" checked name="subjectIdPrefixSuffix" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="subjectIdPrefixSuffix" value="false">No

   </c:when>
   <c:otherwise>
    <input type="radio" name="subjectIdPrefixSuffix" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="subjectIdPrefixSuffix" value="false">No

   </c:otherwise>
  </c:choose>
  </td>
  </tr>
  -->


  <tr valign="top"><td class="formlabel"><fmt:message key="show_person_id_on_crf_header" bundle="${resword}"/>:</td><td>
   <c:choose>
   <c:when test="${newStudy.personIdShownOnCRF == 'true'}">
    <input type="radio" checked name="personIdShownOnCRF" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="personIdShownOnCRF" value="false"><fmt:message key="no" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="personIdShownOnCRF" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="personIdShownOnCRF" value="false"><fmt:message key="no" bundle="${resword}"/>

   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <!-- Bads Changes for Mantis Issue 2201 -->

  <tr valign="top"><td class="formlabel"><fmt:message key="how_to_generate_the_study_subject_ID" bundle="${resword}"/>:</td><td>
    <c:choose>
    <c:when test="${newStudy.subjectIdGeneration == 'manual'}">
     <input type="radio" checked name="subjectIdGeneration" value="manual"><fmt:message key="manual_entry" bundle="${resword}"/>
     <input type="radio" name="subjectIdGeneration" value="auto editable"><fmt:message key="auto_generated_and_editable" bundle="${resword}"/>
     <input type="radio" name="subjectIdGeneration" value="auto non-editable"><fmt:message key="auto_generated_and_non_editable" bundle="${resword}"/>
    </c:when>
     <c:when test="${newStudy.subjectIdGeneration == 'auto editable'}">
     <input type="radio" name="subjectIdGeneration" value="manual"><fmt:message key="manual_entry" bundle="${resword}"/>
     <input type="radio" checked name="subjectIdGeneration" value="auto editable"><fmt:message key="auto_generated_and_editable" bundle="${resword}"/>
     <input type="radio" name="subjectIdGeneration" value="auto non-editable"><fmt:message key="auto_generated_and_non_editable" bundle="${resword}"/>
    </c:when>
    <c:otherwise>
     <input type="radio" name="subjectIdGeneration" value="manual"><fmt:message key="manual_entry" bundle="${resword}"/>
     <input type="radio" name="subjectIdGeneration" value="auto editable"><fmt:message key="auto_generated_and_editable" bundle="${resword}"/>
     <input type="radio" checked name="subjectIdGeneration" value="auto non-editable"><fmt:message key="auto_generated_and_non_editable" bundle="${resword}"/>
    </c:otherwise>
   </c:choose>
   </td>
   </tr>



   <tr><td>&nbsp;</td></tr>
   <tr valign="top"><td class="formlabel"><fmt:message key="when_entering_data_entry_interviewer" bundle="${resword}"/></td><td>
   <c:choose>
   <c:when test="${newStudy.interviewerNameRequired== 'true'}">
    <input type="radio" checked name="interviewerNameRequired" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="interviewerNameRequired" value="false"><fmt:message key="no" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="interviewerNameRequired" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="interviewerNameRequired" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="interviewer_name_default_as_blank" bundle="${resword}"/></td><td>
   <c:choose>
   <c:when test="${newStudy.interviewerNameDefault== 'blank'}">
    <input type="radio" checked name="interviewerNameDefault" value="blank"><fmt:message key="blank" bundle="${resword}"/>
    <input type="radio" name="interviewerNameDefault" value="pre-populated"><fmt:message key="pre_populated_from_active_user" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="interviewerNameDefault" value="blank"><fmt:message key="blank" bundle="${resword}"/>
    <input type="radio" checked name="interviewerNameDefault" value="re-populated"><fmt:message key="pre_populated_from_active_user" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="interviewer_name_editable" bundle="${resword}"/></td><td>
   <c:choose>
   <c:when test="${newStudy.interviewerNameEditable== 'true'}">
    <input type="radio" checked name="interviewerNameEditable" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="interviewerNameEditable" value="false"><fmt:message key="no" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="interviewerNameEditable" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="interviewerNameEditable" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="interviewer_date_required" bundle="${resword}"/></td><td>
   <c:choose>
   <c:when test="${newStudy.interviewDateRequired== 'true'}">
    <input type="radio" checked name="interviewDateRequired" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="interviewDateRequired" value="false"><fmt:message key="no" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="interviewDateRequired" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="interviewDateRequired" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="interviewer_date_default_as_blank" bundle="${resword}"/></td><td>
   <c:choose>
   <c:when test="${newStudy.interviewDateDefault== 'blank'}">
    <input type="radio" checked name="interviewDateDefault" value="blank"><fmt:message key="blank" bundle="${resword}"/>
    <input type="radio" name="interviewDateDefault" value="pre-populated"><fmt:message key="pre_populated_from_SE" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="interviewDateDefault" value="blank"><fmt:message key="blank" bundle="${resword}"/>
    <input type="radio" checked name="interviewDateDefault" value="re-populated"><fmt:message key="pre_populated_from_SE" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="interviewer_date_editable" bundle="${resword}"/></td><td>
   <c:choose>
   <c:when test="${newStudy.interviewDateEditable== 'true'}">
    <input type="radio" checked name="interviewDateEditable" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="interviewDateEditable" value="false"><fmt:message key="no" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="interviewDateEditable" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="interviewDateEditable" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

    <%-- adding forced reason for change here, tbh --%>

  <tr valign="top"><td class="formlabel"><fmt:message key="forced_reason_for_change" bundle="${resword}"/></td><td>
   <c:choose>
   <c:when test="${newStudy.adminForcedReasonForChange== 'true'}">
    <input type="radio" checked name="adminForcedReasonForChange" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="adminForcedReasonForChange" value="false"><fmt:message key="no" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="adminForcedReasonForChange" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="adminForcedReasonForChange" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
 <input type="submit" name="Submit" value="<fmt:message key="confirm_study" bundle="${resword}"/>" class="button_long">
</td>
<td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onClick="javascript:myCancel();"/></td>
</tr>
</table>
</form>
<DIV ID="testdiv1" STYLE="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></DIV>
<br><br>
<jsp:include page="../include/footer.jsp"/>
