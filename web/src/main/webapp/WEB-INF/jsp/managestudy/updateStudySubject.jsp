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
<c:when test="${!useBean.monitor}">
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
<c:if test="${!useBean.monitor}">
 <input type="submit" name="Submit" value="<fmt:message key="confirm_changes" bundle="${resword}"/>" class="button_long">
 </c:if>
 <a href="javascript:history.back()"><input type="button"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/></a>
</form>
<DIV ID="testdiv1" STYLE="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></DIV>

</body>

<jsp:include page="../include/footer.jsp"/>
