<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.admin" var="resadmin"/>

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
<jsp:useBean scope="request" id="facRecruitStatusMap" class="java.util.HashMap"/>
<script type="text/JavaScript" language="JavaScript">
  <!--
 <%--function myCancel() {

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

<h1><span class="title_manage"><fmt:message key="create_a_new_study" bundle="${resword}"/></span></h1>

<p>
<fmt:message key="ClinicalTrials.gov" bundle="${restext}"/>
</p>
<span class="title_Admin"><p><b><fmt:message key="section_a_study_description" bundle="${resword}"/></b></p></span>
<P>* <fmt:message key="indicates_required_field" bundle="${resword}"/></P>
<form action="CreateStudy" method="post">
<input type="hidden" name="action" value="next">
<input type="hidden" name="pageNum" value="1">

 <div style="width: 600px">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">
  <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId'); return false;"><b><fmt:message key="unique_protocol_ID" bundle="${resword}"/></b>:</a></td><td><div class="formfieldXL_BG">
  <input type="text" name="uniqueProId" value="<c:out value="${newStudy.identifier}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="uniqueProId"/></jsp:include></td><td class="formlabel">*</td></tr>

  <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#BriefTitle" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#BriefTitle'); return false;"><b><fmt:message key="brief_title" bundle="${resword}"/></b></a>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="name" value="<c:out value="${newStudy.name}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="name"/></jsp:include></td><td class="formlabel">*</td></tr>

  <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#OfficialTitle" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#OfficialTitle'); return false;"><b><fmt:message key="official_title" bundle="${resword}"/></b></a>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="officialTitle" value="<c:out value="${newStudy.officialTitle}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="officialTitle"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#SecondaryIds" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#SecondaryIds'); return false;"><b><fmt:message key="secondary_IDs" bundle="${resword}"/></b>:</a><br>(<fmt:message key="separate_by_commas" bundle="${resword}"/>)</td>
  <td><div class="formtextareaXL4_BG">
   <textarea class="formtextareaXL4" name="secondProId" rows="4" cols="50"><c:out value="${newStudy.secondaryIdentifier}"/></textarea></div>
   <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="secondProId"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="principal_investigator" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="prinInvestigator" value="<c:out value="${newStudy.principalInvestigator}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="prinInvestigator"/></jsp:include></td><td class="formlabel">*</td></tr>

  <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#StudyType" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#StudyType'); return false;"><fmt:message key="protocol_type" bundle="${resword}"/></a>:</td><td>
   <c:set var="type1" value="observational"/>
   <c:choose>
    <c:when test="${newStudy.protocolTypeKey == type1}">
      <input type="radio" name="protocolType" value="interventional"><fmt:message key="interventional" bundle="${resword}"/>
      <input type="radio" checked name="protocolType" value="observational"><fmt:message key="observational" bundle="${resadmin}"/>
    </c:when>
    <c:otherwise>
      <input type="radio" checked name="protocolType" value="interventional"><fmt:message key="interventional" bundle="${resword}"/>
      <input type="radio" name="protocolType" value="observational"><fmt:message key="observational" bundle="${resadmin}"/>
    </c:otherwise>
   </c:choose>
   <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="protocolType"/></jsp:include></td><td>*</td></tr>


  </table>
  </div>
  </div></div></div></div></div></div></div></div>
   </div>

  <div style="width: 600px">
  <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

  <div class="textbox_center">
  <table border="0" cellpadding="0" cellspacing="0">
  <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#BriefSummary" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#BriefSummary'); return false;"><fmt:message key="brief_summary" bundle="${resword}"/></a>:</td><td><div class="formtextareaXL4_BG">
  <textarea class="formtextareaXL4" name="description" rows="4" cols="50"><c:out value="${newStudy.summary}"/></textarea></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="description"/></jsp:include></td><td class="formlabel">*</td></tr>

   <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#DetailedDescription" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#DetailedDescription'); return false;"><fmt:message key="detailed_description" bundle="${resword}"/></a>:</td><td>
   <div class="formtextareaXL4_BG"><textarea class="formtextareaXL4" name="protocolDescription" rows="4" cols="50"><c:out value="${newStudy.protocolDescription}"/></textarea></div>
   <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="protocolDescription"/></jsp:include>
  </td></tr>

  </table>
  </div>
  </div></div></div></div></div></div></div></div>
  </div>

  <div style="width: 600px">

  <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

  <div class="textbox_center">
  <table border="0" cellpadding="0" cellspacing="0">

  <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#LeadSponsor" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#LeadSponsor'); return false;"><fmt:message key="sponsor" bundle="${resword}"/></a>:</td><td>
  <div class="formfieldXL_BG"><input type="text" name="sponsor" value="<c:out value="${newStudy.sponsor}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="sponsor"/></jsp:include></td><td class="formlabel">*</td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="collaborators" bundle="${resword}"/>:<br>(<fmt:message key="separate_by_commas" bundle="${resword}"/>)</td><td>
  <div class="formtextareaXL4_BG">
  <textarea class="formtextareaXL4" name="collaborators" rows="4" cols="50"><c:out value="${newStudy.collaborators}"/></textarea></div>
   <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="collaborators"/></jsp:include>
  </td></tr>

</table>
</div>
</div></div></div></div></div></div></div></div>

</div>

    <div style="width: 600px">
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

    <div class="textbox_center">
    <table border="0" cellpadding="0" cellspacing="0">
        <tr><fmt:message key="select_user_for_edit_update" bundle="${restext}"/> </tr>
        <br>
        <br>
        <tr valign="top"><td class="formlabel"><fmt:message key="Select_User" bundle="${restext}"/> :</td><td>
         <div class="formfieldL_BG">
          <select name="selectedUser" class="formfieldL">1
          <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
          <c:forEach var="user" items="${users}">
              <option value="<c:out value="${user.id}"/>"> <c:out value="${user.name}"/>
                  (<c:out value="${user.firstName}"/> <c:out value="${user.lastName}"/>)
              </option>
           </c:forEach>
         </select></div>
         </td></tr>

    </table>
    </div>
    </div></div></div></div></div></div></div></div>
    </div>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
 <input type="submit" name="Save" value="<fmt:message key="save" bundle="${resword}"/>" class="button_medium">
</td>
<td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onClick="javascript:myCancel('<fmt:message key="sure_to_cancel" bundle="${resword}"/>');"/></td>
</tr>
</table>

</form>
<br><br>

<!-- EXPANDING WORKFLOW BOX -->



<!-- END WORKFLOW BOX -->

<jsp:include page="../include/footer.jsp"/>
