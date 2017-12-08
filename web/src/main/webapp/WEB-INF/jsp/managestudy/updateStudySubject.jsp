<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<link rel="stylesheet" href="includes/font-awesome-4.7.0/css/font-awesome.css">

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>

<link rel="stylesheet" href="includes/font-awesome-4.7.0/css/font-awesome.css">

<c:choose>
<c:when test="${userBean.sysAdmin || userBean.techAdmin || userRole.manageStudy}">
    <jsp:include page="../include/managestudy-header.jsp"/>
</c:when>
<c:otherwise>
    <jsp:include page="../include/home-header.jsp"/>
</c:otherwise>
</c:choose>
<%-- <jsp:include page="../include/managestudy-header.jsp"/> --%>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
        <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></span></a>

        <fmt:message key="instructions" bundle="${resword}"/>

        <div class="sidebar_tab_content">
        </div>

        </td>

    </tr>
    <tr id="sidebar_Instructions_closed" style="display: all">
        <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></span></a>

        <fmt:message key="instructions" bundle="${resword}"/>

        </td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope="session" id="studySub" class="org.akaza.openclinica.bean.managestudy.StudySubjectBean"/>
<jsp:useBean scope="session" id="enrollDateStr" class="java.lang.String"/>

<body class="aka_bodywidth" onload=
  "if(! detectFirefoxWindows(navigator.userAgent)){document.getElementById('centralContainer').style.display='none';new Effect.Appear('centralContainer', {duration:1});};
        <c:if test='${popUpURL != ""}'>
        openDNoteWindow('<c:out value="${popUpURL}" />');</c:if>">

<c:choose>
<c:when test="${userBean.sysAdmin || userBean.techAdmin || userRole.manageStudy}">
    <h1><span class="title_manage">
    <fmt:message key="update_study_subject_details" bundle="${resword}"/>
    </span></h1>
</c:when>
<c:otherwise>
    <h1><span class="title_manage">
    <fmt:message key="update_study_subject_details" bundle="${resword}"/>
    </span></h1>
</c:otherwise>
</c:choose>

<form action="UpdateStudySubject" method="post">
<input type="hidden" name="action" value="confirm">
<input type="hidden" name="id" value="<c:out value="${studySub.id}"/>">
<c:choose>
<c:when test="${userBean.techAdmin || userBean.sysAdmin || userRole.manageStudy || userRole.investigator
    || (study.parentStudyId > 0 && userRole.researchAssistant ||study.parentStudyId > 0 && userRole.researchAssistant2)}">
     <div style="width: 1050px">
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

    <div class="tablebox_center">
    <table border="0" cellpadding="0" cellspacing="0">

      <tr valign="top">
        <td class="formlabel">
          <fmt:message key="study_subject_ID" bundle="${resword}"/>:
        </td>
        <td>
          <div class="formfieldXL_BG">
          <input type="text" name="label" value="<c:out value="${studySub.label}"/>" class="formfieldXL">*
          </div>
        </td>
      </tr>
      <tr valign="top">
        <td></td>
        <td>
          <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="label"/></jsp:include>
        </td>
      </tr>

      <tr valign="top">
        <td class="formlabel"><fmt:message key="secondary_ID" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="text" name="secondaryLabel" value="<c:out value="${studySub.secondaryLabel}"/>" class="formfieldXL"></div>
        </td>
      </tr>
      <tr valign="top">
        <td></td>
        <td>
          <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="secondaryLabel"/></jsp:include>
        </td>
      </tr>

      <tr valign="top">
      <td class="formlabel"><fmt:message key="enrollment_date" bundle="${resword}"/>:</td>
      <td>
      <div class="formfieldXL_BG">

        <input type="text" name="enrollmentDate" value="<c:out value="${enrollDateStr}" />" class="formfieldXL" id="enrollmentDateField">
        <span class="icon icon-calendar" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="enrollmentDateTrigger"/>
          <script type="text/javascript">
          Calendar.setup({inputField  : "enrollmentDateField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "enrollmentDateTrigger" });
          </script>*
          <%-- DN for enrollment date goes here --%>
          <c:if test="${study.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
              <c:set var="isNew" value="${hasEnrollmentNote eq 'yes' ? 0 : 1}"/>
                  <c:choose>
                      <c:when test="${hasEnrollmentNote eq 'yes'}">
                          <a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${studySub.id}&name=studySub&field=enrollmentDate&column=enrollment_date','spanAlert-enrollmentDate'); return false;">
                              <span id="flag_enrollmentDate" name="flag_enrollmentDate" class="${enrollmentNote.resStatus.iconFilePath}" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                          </a>
                      </c:when>
                      <c:otherwise>
                          <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?subjectId=${studySub.id}&id=${studySub.id}&writeToDB=1&name=studySub&field=enrollmentDate&column=enrollment_date','spanAlert-enrollmentDate'); return false;">
                              <span id="flag_enrollmentDate" name="flag_enrollmentDate" class="fa fa-bubble-white" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                          </a>
                      </c:otherwise>
                  </c:choose>
              </c:if>
      </div>
      <br><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="enrollmentDate"/></jsp:include></td>
     </tr>

    <c:if test="${study.studyParameterConfig.subjectPersonIdRequired=='required' || study.studyParameterConfig.subjectPersonIdRequired=='optional'}">
        <tr valign="top">
            <td class="form_label"><fmt:message key="person_ID" bundle="${resword}"/>:</td>
            <td colspan="1">
              <div class="formfieldXL_BG"><input type="text" name="uniqueIdentifier" value="<c:out value="${subject.uniqueIdentifier}"/>" class="formfieldXL"></div>
              <c:if test="${study.studyParameterConfig.subjectPersonIdRequired == 'required'}">&nbsp;*</c:if>
              <%-- DN for person ID goes here --%>
              <c:if test="${study.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
                <c:set var="isNew" value="${hasUniqueIDNote eq 'yes' ? 0 : 1}"/>
                <c:choose>
                  <c:when test="${hasUniqueIDNote eq 'yes'}">
                    <a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=uniqueIdentifier&column=unique_identifier','spanAlert-uniqueIdentifier'); return false;">
                      <span id="flag_uniqueIdentifier" name="flag_uniqueIdentifier" class="${uniqueIDNote.resStatus.iconFilePath}" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                    </a>
                  </c:when>
                  <c:otherwise>
                    <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=uniqueIdentifier&column=unique_identifier','spanAlert-uniqueIdentifier'); return false;">
                      <span id="flag_uniqueIdentifier" name="flag_uniqueIdentifier" class="fa fa-bubble-white" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                    </a>
                  </c:otherwise>
                </c:choose>
              </c:if>
            </td>
        </tr>
        <tr valign="top">
          <td></td>
          <td>
            <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="uniqueIdentifier"/></jsp:include>
          </td>
        </tr>
    </c:if>

    <tr valign="top">
        <td class="formlabel"><fmt:message key="gender" bundle="${resword}"/>:</td>
        <td colspan="2">
          <input type="radio" name="gender" <c:if test="${subject.gender == 109}">checked</c:if> value="m"><fmt:message key="male" bundle="${resword}"/>
          <input type="radio" name="gender" <c:if test="${subject.gender == 102}">checked</c:if> value="f"><fmt:message key="female" bundle="${resword}"/>
          <input type="radio" name="gender"  <c:if test="${!(subject.gender == 109 || subject.gender == 102)}">checked</c:if> value=" "><fmt:message key="not_specified" bundle="${resword}"/>
          <c:if test="${study.studyParameterConfig.genderRequired}">&nbsp;*</c:if>
              <%-- DN for gender goes here --%>
              <c:if test="${study.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
                <c:set var="isNew" value="${hasGenderNote eq 'yes' ? 0 : 1}"/>
                <c:choose>
                  <c:when test="${hasGenderNote eq 'yes'}">
                    <a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=gender&column=gender','spanAlert-gender'); return false;">
                      <span id="flag_gender" name="flag_gender" class="${genderNote.resStatus.iconFilePath}" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                    </a>
                  </c:when>
                  <c:otherwise>
                    <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=gender&column=gender','spanAlert-gender'); return false;">
                      <span id="flag_gender" name="flag_gender" class="fa fa-bubble-white" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                    </a>
                  </c:otherwise>
                </c:choose>
              </c:if>
         <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="gender"/></jsp:include>
        </td>
    </tr>


    <c:if test="${study.studyParameterConfig.collectDob=='1' || study.studyParameterConfig.collectDob=='2' }">
    <tr valign="top">
        <td class="formlabel" >
        <c:if test="${study.studyParameterConfig.collectDob=='1'}"><fmt:message key="date_of_birth" bundle="${resword}"/></c:if>
        <c:if test="${study.studyParameterConfig.collectDob=='2'}"><fmt:message key="year_of_birth" bundle="${resword}"/></c:if>
        :</td>
        <td>
          <div class="formfieldXL_BG">
            <input type="text" name="localBirthDate" value="<c:out value="${localBirthDate}"/>" class="formfieldXL">
            (
            <c:if test="${study.studyParameterConfig.collectDob=='1'}"><fmt:message key="date_format" bundle="${resformat}"/></c:if>
            <c:if test="${study.studyParameterConfig.collectDob=='2'}"><fmt:message key="date_format_year" bundle="${resformat}"/></c:if>
            ) *


              <%-- DN for DOB goes here --%>
              <c:if test="${study.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
                <c:set var="isNew" value="${hasDOBNote eq 'yes' ? 0 : 1}"/>
                <c:choose>
                  <c:when test="${hasDOBNote eq 'yes'}">
                    <a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=dateOfBirth&column=date_of_birth','spanAlert-dateOfBirth'); return false;">
                      <span id="flag_dateOfBirth" name="flag_dateOfBirth" class="${dOBNote.resStatus.iconFilePath}" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                    </a>
                  </c:when>
                  <c:otherwise>
                    <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=dateOfBirth&column=date_of_birth','spanAlert-dateOfBirth'); return false;">
                      <span id="flag_dateOfBirth" name="flag_dateOfBirth" class="fa fa-bubble-white" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                    </a>
                  </c:otherwise>
                </c:choose>
              </c:if>
          </div>
        </td>
        </tr>
     <tr valign="top">
      <td><fmt:message key="field_required" bundle="${resword}"/></td>
      <td>
        <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="localBirthDate"/></jsp:include>
      </td>
      
      </tr>

    </c:if>

    </table>
    </div>
    </div></div></div></div></div></div></div></div>
    </div>
</c:when>
<c:otherwise>
    <div style="width: 400px">
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

    <div class="tablebox_center">
    <table border="0" cellpadding="0" cellspacing="0" width="100%">

      <tr valign="top"><td class="table_header_column"><fmt:message key="label" bundle="${resword}"/>:</td><td class="table_cell">
      <input type="text" name="label" value="<c:out value="${studySub.label}"/>" disabled="disabled" class="formfieldM">
      </td></tr>

      <tr valign="top"><td class="table_header_column"><fmt:message key="secondary_ID" bundle="${resword}"/>:</td><td class="table_cell">
      <input type="text" name="secondaryLabel" value="<c:out value="${studySub.secondaryLabel}"/>" disabled="disabled" class="formfieldM">
      </td></tr>

      <tr valign="top"><td class="table_header_column"><fmt:message key="enrollment_date" bundle="${resword}"/>:</td><td class="table_cell">
      <input type="text" name="enrollmentDate" value="<c:out value="${enrollDateStr}" />" disabled="disabled" class="formfieldM" id="enrollmentDateField">
      </td></tr>

    <c:if test="${study.studyParameterConfig.subjectPersonIdRequired=='required' || study.studyParameterConfig.subjectPersonIdRequired=='optional'}">
        <tr valign="top">
            <td class="table_header_column"><fmt:message key="person_ID" bundle="${resword}"/>:</td>
            <td class="table_cell">
              <input type="text" name="uniqueIdentifier" disabled="disabled" value="<c:out value="${subject.uniqueIdentifier}"/>" class="formfieldXL">
            </td>
        </tr>
    </c:if>

      <tr valign="top"><td class="table_header_column"><fmt:message key="gender" bundle="${resword}"/>:</td><td class="table_cell">
          <input type="radio" name="gender" disabled="disabled" <c:if test="${subject.gender == 109}">checked</c:if> value="m"><fmt:message key="male" bundle="${resword}"/>
          <input type="radio" name="gender" disabled="disabled" <c:if test="${subject.gender == 102}">checked</c:if> value="f"><fmt:message key="female" bundle="${resword}"/>
          <input type="radio" name="gender" disabled="disabled"  <c:if test="${!(subject.gender == 109 || subject.gender == 102)}">checked</c:if> value=" "><fmt:message key="not_specified" bundle="${resword}"/>
      </td></tr>

      <c:if test="${study.studyParameterConfig.collectDob=='1' || study.studyParameterConfig.collectDob=='2' }">
      <tr valign="top"><td class="table_header_column"><fmt:message key="date_of_birth" bundle="${resword}"/>:</td><td class="table_cell">
      <input type="text" name="localBirthDate" disabled="disabled" value="<c:out value="${localBirthDate}"/>" class="formfieldM">
      </td></tr>
      </c:if>

     </table>

     </div>
    </div></div></div></div></div></div></div></div>

    </div>
</c:otherwise>
</c:choose>

<br>
<c:if test="${(!empty groups)}">
<br>
<div style="width: 550px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0">

  <tr valign="top">
    <td class="formlabel"><fmt:message key="subject_group_class" bundle="${resword}"/>:
    <td class="table_cell">
    <c:set var="count" value="0"/>
    <table border="0" cellpadding="0">
      <c:forEach var="group" items="${groups}">
      <tr valign="top">
       <td><b><c:out value="${group.name}"/></b></td>
       <td><div class="formfieldM_BG"> <select name="studyGroupId<c:out value="${count}"/>" class="formfieldM">
            <c:if test="${group.subjectAssignment=='Optional'}">
              <option value="0">--</option>
            </c:if>
            <c:forEach var="sg" items="${group.studyGroups}">
              <c:choose>
                <c:when test="${group.studyGroupId == sg.id}">
                    <option value="<c:out value="${sg.id}" />" selected><c:out value="${sg.name}"/></option>
                </c:when>
                <c:otherwise>
                    <option value="<c:out value="${sg.id}"/>"><c:out value="${sg.name}"/></option>
                </c:otherwise>
             </c:choose>
            </c:forEach>
            </select></div>
            <c:if test="${group.subjectAssignment=='Required'}">
              <td align="left">*</td>
            </c:if>
            </td></tr>
            <tr valign="top">
            <td><fmt:message key="notes" bundle="${resword}"/>:</td>
            <td>
            <div class="formfieldXL_BG"><input type="text" class="formfieldXL" name="notes<c:out value="${count}"/>"  value="<c:out value="${group.groupNotes}"/>"></div>
              <c:import url="../showMessage.jsp"><c:param name="key" value="notes${count}" /></c:import>
            </td></tr>
           <c:set var="count" value="${count+1}"/>
      </c:forEach>
      </table>
    </td>
  </tr>



</table>
</div>

</div></div></div></div></div></div></div></div>

</div>
</c:if>
<c:if test="${userBean.techAdmin || userBean.sysAdmin || userRole.manageStudy || userRole.investigator
    || (study.parentStudyId > 0 && userRole.researchAssistant ||study.parentStudyId > 0 && userRole.researchAssistant2)}">
 <input type="submit" name="Submit" value="<fmt:message key="confirm_changes" bundle="${resword}"/>" class="button_long">
 </c:if>
 <a href="javascript:history.back()"><input type="button"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/></a>
</form>
<DIV ID="testdiv1" STYLE="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></DIV>

</body>

<jsp:include page="../include/footer.jsp"/>
