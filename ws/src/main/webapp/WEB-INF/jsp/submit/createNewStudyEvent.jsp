<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>


<jsp:include page="../include/submit-header.jsp"/>


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
<jsp:useBean scope="session" id="currentRole" class="org.akaza.openclinica.bean.login.StudyUserRoleBean" />
<jsp:useBean scope="request" id="pageMessages" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="formMessages" class="java.util.HashMap" />
<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />

<jsp:useBean scope="request" id="subjects" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="eventDefinitions" class="java.util.ArrayList" />

<c:set var="location" value="" />
<c:set var="requestStudySubjectFalse" value="no" />

<!-- TODO: HOW TO DEAL WITH PRESET VALUES THAT AREN'T STRINGS? -->
<!-- TODO: CAN I USE PUBLIC STATIC MEMBERS HERE? -->
<c:forEach var="presetValue" items="${presetValues}">
	<c:if test='${presetValue.key == "location"}'>
		<c:set var="location" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "studyEventDefinition"}'>
		<c:set var="chosenDefinition" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "studySubject"}'>
		<c:set var="chosenSubject" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "studySubjectLabel"}'>
		<c:set var="chosenSubjectLabel" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "requestStudySubject"}'>
		<c:set var="requestStudySubject" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "locationScheduled0"}'>
		<c:set var="locationScheduled0" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "locationScheduled1"}'>
		<c:set var="locationScheduled1" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "locationScheduled2"}'>
		<c:set var="locationScheduled2" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "locationScheduled3"}'>
		<c:set var="locationScheduled3" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "studyEventDefinitionScheduled0"}'>
		<c:set var="chosenDefinitionScheduled0" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "studyEventDefinitionScheduled1"}'>
		<c:set var="chosenDefinitionScheduled1" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "studyEventDefinitionScheduled2"}'>
		<c:set var="chosenDefinitionScheduled2" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "studyEventDefinitionScheduled3"}'>
		<c:set var="chosenDefinitionScheduled3" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "display0"}'>
		<c:set var="display0" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "display1"}'>
		<c:set var="display1" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "display2"}'>
		<c:set var="display2" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "display3"}'>
		<c:set var="display3" value="${presetValue.value}" />
	</c:if>
</c:forEach>

<h1><span class="title_manage">
<c:choose>
	<c:when test="${requestStudySubject == requestStudySubjectFalse}">
		<fmt:message key="schedule_study_event_for" bundle="${resword}"/><b> <c:out value="${chosenSubject.name}" /></b>
	</c:when>
	<c:otherwise>
		<fmt:message key="schedule_study_event_for" bundle="${resword}"/>
	</c:otherwise>
</c:choose>
</span></h1>
<DIV ID="testdiv1" STYLE="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></DIV>
<script type="text/JavaScript" language="JavaScript">
  <!--
 function myCancel() {

    cancelButton=document.getElementById('cancel');
    if ( cancelButton != null) {
      if(confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>')) {
        window.location.href="ListStudySubjects";
       return true;
      } else {
        return false;
       }
     }
     return true;

  }

  function leftnavExpand(strLeftNavRowElementName){
    var objLeftNavRowElement;

    objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
    if (objLeftNavRowElement != null) {
      if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
        objLeftNavRowElement.display = (objLeftNavRowElement.display == "none" ) ? "" : "none";
        objExCl = MM_findObj("excl_"+strLeftNavRowElementName);
        if(objLeftNavRowElement.display == "none"){
            objExCl.src = "images/bt_Expand.gif";
        }else{
            objExCl.src = "images/bt_Collapse.gif";
        }
      }
    }
  
   //-->
</script>
<P><fmt:message key="field_required" bundle="${resword}"/></P>

<form action="CreateNewStudyEvent" method="post">
<jsp:include page="../include/showSubmitted.jsp" />

<div style="width: 600px">

<!-- These DIVs define shaded box borders -->
	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

		<div class="textbox_center">

<table border="0" cellpadding="3" cellspacing="0">
	<tr>
		<td class="formlabel"><fmt:message key="study_subject_ID" bundle="${resword}"/>:</td>
		<td valign="top">
			<c:choose>
				<c:when test="${requestStudySubject == requestStudySubjectFalse}">
					<b><c:out value="${chosenSubject.name}" /></b>
					<input type="hidden" name="studySubject" value="<c:out value="${chosenSubject.id}" />" />
					<input type="hidden" name="studySubjectLabel" value="<c:out value="${chosenSubject.name}" />" />
					<input type="hidden" name="requestStudySubject" value="<c:out value="${requestStudySubject}" />" />
					<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="studySubject"/></jsp:include>
				</c:when>
				<c:otherwise>
					<table border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td valign="top">
						<div class="formfieldXL_BG">
						<!-- added/removed tbh 11/09/2009 -->
						<input type="text" class="formfieldXL" size="50" value="<c:out value="${chosenSubjectLabel}"/>" name="studySubjectLabel"/>
				
							<%-- <select name="studySubject" class="formfieldXL">
							<option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
							<c:forEach var="subject" items="${subjects}">
								<c:choose>
									<c:when test="${chosenSubject.id == subject.id}">
										<option value="<c:out value="${subject.id}" />" selected><c:out value="${subject.name}"/></option>
									</c:when>
									<c:otherwise>
										<option value="<c:out value="${subject.id}" />"><c:out value="${subject.name}"/></option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
							</select> --%>
						</div>
						</td>
						<td>*</td>
					</tr>
					<tr>
						<td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="studySubject"/></jsp:include></td>
					</tr>
					</table>
				</c:otherwise>
			</c:choose>
		</td>
	</tr>

	<tr>
	  	<td class="formlabel"><fmt:message key="study_event_definition" bundle="${resword}"/>:</td>
	  	<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="top">
				<div class="formfieldXL_BG">
					<select name="studyEventDefinition" class="formfieldXL">
						<option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
						<c:forEach var="definition" items="${eventDefinitions}">
							<c:choose>
								<c:when test="${definition.repeating}">
									<c:set var="repeating">
									 (<fmt:message key="repeating" bundle="${resword}"/>)
									</c:set>
								</c:when>
								<c:otherwise>
									<c:set var="repeating">
									(<fmt:message key="non_repeating" bundle="${resword}"/>)
									</c:set>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${chosenDefinition.id == definition.id}">
									<option value="<c:out value="${definition.id}" />" selected><c:out value="${definition.name}"/> <c:out value="${repeating}"/></option>
								</c:when>
								<c:otherwise>
									<option value="<c:out value="${definition.id}"/>"><c:out value="${definition.name}"/> <c:out value="${repeating}"/></option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</div>
				</td>
				<td>*</td>
			</tr>
			<tr>
				<td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="studyEventDefinition"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>

	<tr>
		<td class="formlabel"><fmt:message key="location" bundle="${resword}"/>:</td>
	  	<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="top">
				<div class="formfieldXL_BG">
					<input type="text" name="location" value="<c:out value="${location}"/>" size="50" class="formfieldXL">
				</div>
				</td>
				<td>*<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=location&column=location','spanAlert-location'); return false;">
				<img name="flag_location" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="location"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>

	<tr>
		<td class="formlabel"><fmt:message key="start_date_time" bundle="${resword}"/>:</td>
	  	<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<c:import url="../include/showDateTimeInput.jsp">
				<c:param name="prefix" value="start"/>
				<c:param name="count" value="1"/>
				</c:import>

				<td>(<fmt:message key="date_time_format" bundle="${resformat}"/>) *<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=start&column=date_start','spanAlert-start'); return false;">
				<img name="flag_start" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td colspan="7"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="start"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>

	<tr valign="top">
		<td class="formlabel"><fmt:message key="end_date_time" bundle="${resword}"/>:</td>
		<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<c:import url="../include/showDateTimeInput.jsp">
				<c:param name="prefix" value="end"/>
				<c:param name="count" value="2"/>
				</c:import>

				<td>(<fmt:message key="date_time_format" bundle="${resformat}"/>)<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=end&column=date_end','spanAlert-end'); return false;">
				<img name="flag_end" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td colspan="7">
					<fmt:message key="leave_this_field_blank_if_not_applicable" bundle="${restext}"/><br/>
					<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="end" /></jsp:include>
				</td>
			</tr>
			</table>
		</td>
	</tr>
</table>

</div>

</div></div></div></div></div></div></div></div>
</div> </div><br>

<a href="javascript:leftnavExpand('schedule0');">
    <img id="excl_schedule0" src="images/bt_Expand.gif" border="0"> <fmt:message key="schedule_another_event" bundle="${resword}"/></a>

<div id="schedule0" style="display: <c:out value="${display0}"/>">
<div style="width: 100%">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">

<table border="0" cellpadding="3" cellspacing="0">
 <tr>
  	<td class="formlabel"><fmt:message key="study_event_definition" bundle="${resword}"/>:</td>
  	<td valign="top">
    	<table border="0" cellpadding="0" cellspacing="0">
		<tr>
				<td valign="top">
				<div class="formfieldXL_BG">
					<select name="studyEventDefinitionScheduled0" class="formfieldXL">
						<option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
						<c:forEach var="definition" items="${eventDefinitionsScheduled}">
							<c:choose>
								<c:when test="${definition.repeating}">
									<c:set var="repeating">
									 (<fmt:message key="repeating" bundle="${resword}"/>)
									</c:set>
								</c:when>
								<c:otherwise>
									<c:set var="repeating">
									(<fmt:message key="non_repeating" bundle="${resword}"/>)
									</c:set>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${chosenDefinitionScheduled0.id == definition.id}">
									<option value="<c:out value="${definition.id}" />" selected><c:out value="${definition.name}"/> <c:out value="${repeating}"/></option>
								</c:when>
								<c:otherwise>
									<option value="<c:out value="${definition.id}"/>"><c:out value="${definition.name}"/> <c:out value="${repeating}"/></option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</div>
				</td>
				<td>*</td>
			</tr>
			<tr>
			 <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="studyEventDefinitionScheduled0"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td class="formlabel"><fmt:message key="location" bundle="${resword}"/>:</td>
	  	<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="top">
				<div class="formfieldXL_BG">
					<input type="text" name="locationScheduled0" value="<c:out value="${locationScheduled0}"/>" size="50" class="formfieldXL">
				</div>
				</td>
				<td>*<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=schLocation&column=location','spanAlert-locationScheduled0'); return false;">
				<img name="flag_schLocation" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="locationScheduled0"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td class="formlabel"><fmt:message key="start_date_time" bundle="${resword}"/>:</td>
	  	<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<c:import url="../include/showDateTimeInput.jsp"><c:param name="prefix" value="startScheduled0"/><c:param name="count" value="3"/></c:import>
				<td>(<fmt:message key="date_time_format" bundle="${resformat}"/>)*<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=startScheduled0&column=date_startScheduled0','spanAlert-startScheduled0'); return false;">
				<img name="flag_startScheduled0" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td colspan="7"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="startScheduled0"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>
    <tr valign="top">
		<td class="formlabel"><fmt:message key="end_date_time" bundle="${resword}"/>:</td>
		<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<c:import url="../include/showDateTimeInput.jsp"><c:param name="prefix" value="endScheduled0"/><c:param name="count" value="4"/></c:import>
				<td>(<fmt:message key="date_time_format" bundle="${resformat}"/>)<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=endScheduled0&column=date_endScheduled0','spanAlert-endScheduled0'); return false;">
				<img name="flag_endScheduled0" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td colspan="7">
					<fmt:message key="leave_this_field_blank_if_not_applicable" bundle="${restext}"/><br/>
					<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="endScheduled0" /></jsp:include>
				</td>
			</tr>
			</table>
		</td>
	</tr>
</table>

</div>

</div></div></div></div></div></div></div></div>

</div>
</div><br>

<a href="javascript:leftnavExpand('schedule1');">
    <img id="excl_schedule1" src="images/bt_Expand.gif" border="0"> <fmt:message key="schedule_another_event" bundle="${resword}"/> </a></div>

<div id="schedule1" style="display: <c:out value="${display1}"/>">
<div style="width: 100%">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">

<table border="0" cellpadding="3" cellspacing="0">
 <tr>
  	<td class="formlabel"><fmt:message key="study_event_definition" bundle="${resword}"/>:</td>
  	<td valign="top">
    	<table border="0" cellpadding="0" cellspacing="0">
		<tr>
				<td valign="top">
				<div class="formfieldXL_BG">
					<select name="studyEventDefinitionScheduled1" class="formfieldXL">
						<option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
						<c:forEach var="definition" items="${eventDefinitionsScheduled}">
							<c:choose>
								<c:when test="${definition.repeating}">
									<c:set var="repeating">
									 (<fmt:message key="repeating" bundle="${resword}"/>)
									</c:set>
								</c:when>
								<c:otherwise>
									<c:set var="repeating">
									(<fmt:message key="non_repeating" bundle="${resword}"/>)
									</c:set>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${chosenDefinitionScheduled1.id == definition.id}">
									<option value="<c:out value="${definition.id}" />" selected><c:out value="${definition.name}"/> <c:out value="${repeating}"/></option>
								</c:when>
								<c:otherwise>
									<option value="<c:out value="${definition.id}"/>"><c:out value="${definition.name}"/> <c:out value="${repeating}"/></option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</div>
				</td>
				<td>*</td>
			</tr>
			<tr>
			 <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="studyEventDefinitionScheduled1"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td class="formlabel"><fmt:message key="location" bundle="${resword}"/>:</td>
	  	<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="top">
				<div class="formfieldXL_BG">
					<input type="text" name="locationScheduled1" value="<c:out value="${locationScheduled1}"/>" size="50" class="formfieldXL">
				</div>
				</td>
				<td>*<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=schLocation&column=location','spanAlert-locationScheduled1'); return false;">
				<img name="flag_schLocation" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="locationScheduled1"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td class="formlabel"><fmt:message key="start_date_time" bundle="${resword}"/>:</td>
	  	<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<c:import url="../include/showDateTimeInput.jsp"><c:param name="prefix" value="startScheduled1"/><c:param name="count" value="3"/></c:import>
				<td>(<fmt:message key="date_time_format" bundle="${resformat}"/>)*<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=startScheduled1&column=date_startScheduled1','spanAlert-startScheduled1'); return false;">
				<img name="flag_startScheduled1" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td colspan="7"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="startScheduled1"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>
    <tr valign="top">
		<td class="formlabel"><fmt:message key="end_date_time" bundle="${resword}"/>:</td>
		<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<c:import url="../include/showDateTimeInput.jsp"><c:param name="prefix" value="endScheduled1"/><c:param name="count" value="4"/></c:import>
				<td>(<fmt:message key="date_time_format" bundle="${resformat}"/>)<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=endScheduled1&column=date_endScheduled1','spanAlert-endScheduled1'); return false;">
				<img name="flag_endScheduled1" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td colspan="7">
					<fmt:message key="leave_this_field_blank_if_not_applicable" bundle="${restext}"/><br/>
					<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="endScheduled1" /></jsp:include>
				</td>
			</tr>
			</table>
		</td>
	</tr>
</table>

</div>

</div></div></div></div></div></div></div></div>

</div>
</div><br>


<a href="javascript:leftnavExpand('schedule2');">
    <img id="excl_schedule2" src="images/bt_Expand.gif" border="0"> <fmt:message key="schedule_another_event" bundle="${resword}"/> </a></div>

<div id="schedule2" style="display:<c:out value="${display2}"/>">
<div style="width: 100%">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">

<table border="0" cellpadding="3" cellspacing="0">
 <tr>
  	<td class="formlabel"><fmt:message key="study_event_definition" bundle="${resword}"/>:</td>
  	<td valign="top">
    	<table border="0" cellpadding="0" cellspacing="0">
		<tr>
				<td valign="top">
				<div class="formfieldXL_BG">
					<select name="studyEventDefinitionScheduled2" class="formfieldXL">
						<option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
						<c:forEach var="definition" items="${eventDefinitionsScheduled}">
							<c:choose>
								<c:when test="${definition.repeating}">
									<c:set var="repeating">
									 (<fmt:message key="repeating" bundle="${resword}"/>)
									</c:set>
								</c:when>
								<c:otherwise>
									<c:set var="repeating">
									(<fmt:message key="non_repeating" bundle="${resword}"/>)
									</c:set>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${chosenDefinitionScheduled2.id == definition.id}">
									<option value="<c:out value="${definition.id}" />" selected><c:out value="${definition.name}"/> <c:out value="${repeating}"/></option>
								</c:when>
								<c:otherwise>
									<option value="<c:out value="${definition.id}"/>"><c:out value="${definition.name}"/> <c:out value="${repeating}"/></option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</div>
				</td>
				<td>*</td>
			</tr>
			<tr>
			 <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="studyEventDefinitionScheduled2"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td class="formlabel"><fmt:message key="location" bundle="${resword}"/>:</td>
	  	<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="top">
				<div class="formfieldXL_BG">
					<input type="text" name="locationScheduled2" value="<c:out value="${locationScheduled2}"/>" size="50" class="formfieldXL">
				</div>
				</td>
				<td>*<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=schLocation&column=location','spanAlert-locationScheduled2'); return false;">
				<img name="flag_schLocation" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="locationScheduled2"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td class="formlabel"><fmt:message key="start_date_time" bundle="${resword}"/>:</td>
	  	<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<c:import url="../include/showDateTimeInput.jsp"><c:param name="prefix" value="startScheduled2"/><c:param name="count" value="3"/></c:import>
				<td>(<fmt:message key="date_time_format" bundle="${resformat}"/>)*<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=startScheduled2&column=date_startScheduled2','spanAlert-startScheduled2'); return false;">
				<img name="flag_startScheduled2" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td colspan="7"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="startScheduled2"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>
    <tr valign="top">
		<td class="formlabel"><fmt:message key="end_date_time" bundle="${resword}"/>:</td>
		<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<c:import url="../include/showDateTimeInput.jsp"><c:param name="prefix" value="endScheduled2"/><c:param name="count" value="4"/></c:import>
				<td>(<fmt:message key="date_time_format" bundle="${resformat}"/>)<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=endScheduled2&column=date_endScheduled2','spanAlert-endScheduled2'); return false;">
				<img name="flag_endScheduled2" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td colspan="7">
					<fmt:message key="leave_this_field_blank_if_not_applicable" bundle="${restext}"/><br/>
					<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="endScheduled2" /></jsp:include>
				</td>
			</tr>
			</table>
		</td>
	</tr>
</table>

</div>

</div></div></div></div></div></div></div></div>

</div>
</div><br>


<a href="javascript:leftnavExpand('schedule3');">
    <img id="excl_schedule3" src="images/bt_Expand.gif" border="0"> <fmt:message key="schedule_another_event" bundle="${resword}"/> </a></div>

<div id="schedule3" style="display:<c:out value="${display3}"/>">
<div style="width: 100%">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">

<table border="0" cellpadding="3" cellspacing="0">
 <tr>
  	<td class="formlabel"><fmt:message key="study_event_definition" bundle="${resword}"/>:</td>
  	<td valign="top">
    	<table border="0" cellpadding="0" cellspacing="0">
		<tr>
				<td valign="top">
				<div class="formfieldXL_BG">
					<select name="studyEventDefinitionScheduled3" class="formfieldXL">
						<option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
						<c:forEach var="definition" items="${eventDefinitionsScheduled}">
							<c:choose>
								<c:when test="${definition.repeating}">
									<c:set var="repeating">
									 (<fmt:message key="repeating" bundle="${resword}"/>)
									</c:set>
								</c:when>
								<c:otherwise>
									<c:set var="repeating">
									(<fmt:message key="non_repeating" bundle="${resword}"/>)
									</c:set>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${chosenDefinitionScheduled3.id == definition.id}">
									<option value="<c:out value="${definition.id}" />" selected><c:out value="${definition.name}"/> <c:out value="${repeating}"/></option>
								</c:when>
								<c:otherwise>
									<option value="<c:out value="${definition.id}"/>"><c:out value="${definition.name}"/> <c:out value="${repeating}"/></option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</div>
				</td>
				<td>*</td>
			</tr>
			<tr>
			 <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="studyEventDefinitionScheduled3"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td class="formlabel"><fmt:message key="location" bundle="${resword}"/>:</td>
	  	<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="top">
				<div class="formfieldXL_BG">
					<input type="text" name="locationScheduled3" value="<c:out value="${locationScheduled3}"/>" size="50" class="formfieldXL">
				</div>
				</td>
				<td>*<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=schLocation&column=location','spanAlert-locationScheduled3'); return false;">
				<img name="flag_schLocation" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="locationScheduled3"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td class="formlabel"><fmt:message key="start_date_time" bundle="${resword}"/>:</td>
	  	<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<c:import url="../include/showDateTimeInput.jsp"><c:param name="prefix" value="startScheduled3"/><c:param name="count" value="3"/></c:import>
				<td>(<fmt:message key="date_time_format" bundle="${resformat}"/>)*<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=startScheduled3&column=date_startScheduled3','spanAlert-startScheduled3'); return false;">
				<img name="flag_startScheduled3" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td colspan="7"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="startScheduled3"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>
    <tr valign="top">
		<td class="formlabel"><fmt:message key="end_date_time" bundle="${resword}"/>:</td>
		<td valign="top">
		  	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<c:import url="../include/showDateTimeInput.jsp"><c:param name="prefix" value="endScheduled3"/><c:param name="count" value="4"/></c:import>
				<td>(<fmt:message key="date_time_format" bundle="${resformat}"/>)<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${chosenSubject.id}&name=studyEvent&field=endScheduled3&column=date_endScheduled3','spanAlert-endScheduled3'); return false;">
				<img name="flag_endScheduled3" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if></td>
			</tr>
			<tr>
				<td colspan="7">
					<fmt:message key="leave_this_field_blank_if_not_applicable" bundle="${restext}"/><br/>
					<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="endScheduled3" /></jsp:include>
				</td>
			</tr>
			</table>
		</td>
	</tr>
</table>

</div>

</div></div></div></div></div></div></div></div>

</div>
</div><br>


<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
<input type="submit" name="Submit" value="<fmt:message key="proceed_to_enter_data" bundle="${resword}"/>" class="button_long" />
</td>
<td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_long" onClick="javascript:myCancel();"/></td>
</tr></table>
</form>
<c:set var="role" value="${currentRole.role}" />
<c:if test="${currentRole.manageStudy}">
<c:if test="${requestStudySubject == requestStudySubjectFalse}">
<p>	<form method="POST" action="UpdateStudySubject">
	<input type="hidden" name="id" value="<c:out value="${chosenSubject.id}"/>" />
	<input type="hidden" name="action" value="show" />
	<input type="submit" name="editstudy" value="<fmt:message key="edit_subject_properties1" bundle="${resword}"/>" class="button_xlong" />
	</form>

<p>	<form method="POST" action="UpdateSubject">
	<input type="hidden" name="id" value="<c:out value="${chosenSubject.subjectId}"/>" />
	<input type="hidden" name="studySubjId" value="<c:out value="${chosenSubject.id}"/>" />
	<input type="hidden" name="action" value="show" />
	<input type="submit" name="editstudy" value="<fmt:message key="edit_subject_properties2" bundle="${resword}"/>" class="button_xlong" />
	</form>
</c:if>
</c:if>


<jsp:include page="../include/footer.jsp"/>

