<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.admin" var="resadmin"/>

<jsp:include page="../include/admin-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></span></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		<div class="sidebar_tab_content">
        <fmt:message key="enter_the_study_and_protocol" bundle="${resword}"/>
		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></span></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='newStudy' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope ="request" id="studyPhaseMap" class="java.util.HashMap"/>
<jsp:useBean scope="request" id="statuses" class="java.util.ArrayList"/>
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
<h1><span class="title_manage"><fmt:message key="create_a_new_study_continue" bundle="${resword}"/></span></h1>

<span class="title_Admin"><p><b><fmt:message key="section_b_study_status" bundle="${resword}"/> - <fmt:message key="status" bundle="${resword}"/> </b></p></span>
<P>* <fmt:message key="indicates_required_field" bundle="${resword}"/></P>
<c:set var="startDate" value="" />
<c:set var="endDate" value="" />
<c:set var="protocolDateVerification" value="" />
<c:forEach var="presetValue" items="${presetValues}">
	<c:if test='${presetValue.key == "startDate"}'>
		<c:set var="startDate" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "endDate"}'>
		<c:set var="endDate" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "protocolDateVerification"}'>
		<c:set var="protocolDateVerification" value="${presetValue.value}" />
	</c:if>
</c:forEach>

<DIV ID="testdiv1" STYLE="position:absolute;z-index:8;visibility:hidden;background-color:white;"></DIV>
<form action="CreateStudy" method="post">
<input type="hidden" name="action" value="next">
<input type="hidden" name="pageNum" value="2">
<div style="width: 600px">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0" >
 <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#StudyPhase" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#StudyPhase'); return false;"><fmt:message key="study_phase" bundle="${resword}"/></a>:</td><td>
  <c:set var="phase1" value="${newStudy.phase}"/>
  <div class="formfieldL_BG"><select name="phase" class="formfieldL">
   <c:forEach var="phase" items="${studyPhaseMap}">
       <c:choose>
        <c:when test="${phase1 == phase.key}">
         <option value="<c:out value="${phase.key}"/>" selected><c:out value="${phase.value}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${phase.key}"/>"><c:out value="${phase.value}"/>
        </c:otherwise>
       </c:choose>
    </c:forEach>
  </select></div>
  </td><td>*</td></tr>

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

  

    <c:choose>
     <c:when test="${newStudy.parentStudyId == 0}">
        <c:set var="key" value="study_system_status"/>
     </c:when>
     <c:otherwise>
         <c:set var="key" value="site_system_status"/>
     </c:otherwise>
    </c:choose>

  <tr valign="top"><td class="formlabel"><fmt:message key="${key}" bundle="${resword}"/>:</td><td><div class="formfieldL_BG">
   <%--
   <c:set var="status1" value="${newStudy.status.id}"/>
   <select class="formfieldL" name="statusId">
      <c:forEach var="status" items="${statuses}">
       <c:choose>
        <c:when test="${status1 == status.id}">
         <option value="<c:out value="${status.id}"/>" selected><c:out value="${status.name}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${status.id}"/>"><c:out value="${status.name}"/>
        </c:otherwise>
       </c:choose>
    </c:forEach>
   </select></div> --%>
   <input type="text" name="statusName" value="Available" class="formfieldL" disabled>
   <input type="hidden" name="statusId" value="1"></div>
  </td><td>*</td></tr>


  <tr valign="top">
      <td class="formlabel" width="33%">
          <a href="http://prsinfo.clinicaltrials.gov/definitions.html#VerificationDate" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#VerificationDate'); return false;"><fmt:message key="protocol_verification" bundle="${resword}"/>:</a>
      </td>
      <td width="33%">
          <div class="formfieldL_BG">
              <input type="text" name="protocolDateVerification" value="<c:out value="${protocolDateVerification}" />" class="formfieldL" id="protocolDateVerificationField" >
          </div>
          <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="protocolDateVerification"/></jsp:include>
      </td>
      <td width="34%">
          <A HREF="#" >
              <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="protocolDateVerificationTrigger"/>
              <script type="text/javascript">
              Calendar.setup({inputField  : "protocolDateVerificationField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "protocolDateVerificationTrigger" });
              </script>

          </a>
      </td>
  </tr>


  <tr valign="top"><td class="formlabel"><fmt:message key="study_start_date" bundle="${resword}"/>:</td><td><div class="formfieldL_BG">
  <input type="text" name="startDate" value="<c:out value="${startDate}" />" class="formfieldL" id="startDateField"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="startDate"/></jsp:include></td>
  <td>
      <A HREF="#" >
          <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="startDateTrigger"/>
          <script type="text/javascript">
          Calendar.setup({inputField  : "startDateField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "startDateTrigger" });
          </script>
      </a>*
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="study_completion_date" bundle="${resword}"/>:</td><td><div class="formfieldL_BG">
  <input type="text" name="endDate" value="<c:out value="${endDate}" />" class="formfieldL" id="endDateField"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="endDate"/></jsp:include></td>
  <td>
  <A HREF="#" >
      <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="endDateTrigger"/>
      <script type="text/javascript">
      Calendar.setup({inputField  : "endDateField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "endDateTrigger" });
      </script>
  </a>
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
<td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onClick="javascript:myCancel('<fmt:message key="sure_to_cancel" bundle="${resword}"/>');"/></td>
</tr>
</table>

</form>
<DIV ID="testdiv1" STYLE="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></DIV>
<br><br>

<jsp:include page="../include/footer.jsp"/>
