<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>


<jsp:include page="../include/submit-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b>Instructions</b>

		<div class="sidebar_tab_content">
			Please confirm the subject information shown before proceeding.
		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b>Instructions</b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope="session" id="study" class="org.akaza.openclinica.bean.managestudy.StudyBean" />
<jsp:useBean scope="request" id="pageMessages" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />

<jsp:useBean scope="request" id="groups" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="fathers" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="mothers" class="java.util.ArrayList" />

<c:set var="uniqueIdentifier" value="" />
<c:set var="chosenGender" value="" />
<c:set var="label" value="" />
<c:set var="secondaryLabel" value="" />
<c:set var="enrollmentDate" value="" />
<c:set var="dob" value="" />
<c:set var="yob" value="" />
<c:set var="groupId" value="${0}" />
<c:set var="fatherId" value="${0}" />
<c:set var="motherId" value="${0}" />
<c:set var="gWarning" value="" />
<c:set var="dWarning" value="" />
<c:set var="yWarning" value="" />

<c:forEach var="presetValue" items="${presetValues}">
	<c:if test='${presetValue.key == "uniqueIdentifier"}'>
		<c:set var="uniqueIdentifier" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "gender"}'>
		<c:set var="chosenGender" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "label"}'>
		<c:set var="label" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "secondaryLabel"}'>
		<c:set var="secondaryLabel" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "enrollmentDate"}'>
		<c:set var="enrollmentDate" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "dob"}'>
		<c:set var="dob" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "yob"}'>
		<c:set var="yob" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "group"}'>
		<c:set var="groupId" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "mother"}'>
		<c:set var="motherId" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "father"}'>
		<c:set var="fatherId" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "editDob"}'>
		<c:set var="editDob" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "gWarning"}'>
		<c:set var="gWarning" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "dWarning"}'>
		<c:set var="dWarning" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "yWarning"}'>
		<c:set var="yWarning" value="${presetValue.value}" />
	</c:if>
</c:forEach>


<h1><span class="title_manage"><c:out value="${study.name}" />: <fmt:message key="enroll_subject" bundle="${resword}"/></span></h1>
<br>

<jsp:include page="../include/alertbox.jsp" />
<p><fmt:message key="based_ID_subject_exist_confirm" bundle="${restext}"/></p>
<!-- c:if test="${study.genetic && (!empty mothers) || (!empty fathers)}" -->
<!-- <p class="text">Indicate the subject's parents, if applicable. -->
<!-- /c:if -->
<br/><fmt:message key="field_required" bundle="${resword}"/></P>
<form action="AddNewSubject" method="post">
<jsp:include page="../include/showSubmitted.jsp" />
<input type="hidden" name="existingSubShown" value="1">
<input type="hidden" name="editDob" value="<c:out value="${editDob}"/>">
<div style="width: 550px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="5">
	<tr valign="top">
		<td class="formlabel"><fmt:message key="unique_identifier" bundle="${resword}"/><br> (<fmt:message key="study_subject_ID" bundle="${resword}"/>):</td>
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top"><div class="formfieldXL_BG">
					  <c:choose>
						 <c:when test="${study.studyParameterConfig.subjectIdGeneration =='auto non-editable'}">
						  <input type="text" value="<c:out value="${label}"/>" size="45" class="formfield" disabled>
						  <input type="hidden" name="label" value="<c:out value="${label}"/>">
						 </c:when>
						 <c:otherwise>
						   <input type="text" name="label" value="<c:out value="${label}"/>" size="50" class="formfieldXL">
						 </c:otherwise>
					</c:choose>
					</div></td>
					<td >*</td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="label"/></jsp:include></td>
				</tr>
			</table>
		</td>
	</tr>
	<tr valign="top">
	  	<td class="formlabel"><fmt:message key="person_ID" bundle="${resword}"/>:</td>
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
						&nbsp;<c:out value="${uniqueIdentifier}"/>
						<input type="hidden" name="uniqueIdentifier" value="<c:out value="${uniqueIdentifier}"/>" >
					</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="uniqueIdentifier"/></jsp:include></td>
				</tr>
			</table>
		</td>
	</tr>
	<tr valign="top">
	  	<td class="formlabel"><fmt:message key="secondary_ID" bundle="${resword}"/></td>
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top"><div class="formfieldXL_BG">
						<input type="text" name="secondaryLabel" value="<c:out value="${secondaryLabel}"/>" size="50" class="formfieldXL">
					</div></td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="secondaryLabel"/></jsp:include></td>
				</tr>
			</table>
		</td>
	</tr>
	<tr valign="top">
		<td class="formlabel"><fmt:message key="date_of_enrollment_for_study" bundle="${resword}"/><b><c:out value="${study.name}" />:</td>
	  	<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top"><div class="formfieldM_BG">
						<input type="text" name="enrollmentDate" size="15" value="<c:out value="${enrollmentDate}" />" class="formfieldM" id="enrollmentDate"/>
					</td>
					<td><A HREF="#" >
                        <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="trigger"/>
                        <script type="text/javascript">
                        Calendar.setup({inputField  : "enrollmentDate", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "trigger" });
                        </script>
                    </a>
					<%--(<fmt:message key="date_format" bundle="${resformat}"/>)--%> *
					<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}"><a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?name=studySub&field=enrollmentDate&column=enrollment_date','spanAlert-enrollmentDate'); return false;">
					<img name="flag_enrollmentDate" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a></c:if>
					</td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="enrollmentDate"/></jsp:include></td>
				</tr>
			</table>
	  	</td>
	</tr>

	<c:choose>
	<c:when test="${study.studyParameterConfig.genderRequired !='false'}">
	<tr valign="top">
		<td class="formlabel"><fmt:message key="gender" bundle="${resword}"/>:</td>
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
					 &nbsp;<c:out value="${chosenGender}"/>
					 <input type="hidden" name="gender" value="<c:out value="${chosenGender}"/>">
					</td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="gender"/></jsp:include></td>
				</tr>
				<tr>
					<c:choose>
						<c:when test="${gWarning=='emptytrue'}">
                            <!--The sex designation in the database is empty.  To overwrite the database with the current gender, please continue.  If you have any questions, please contact the administrator.-->
                            <td><fmt:message key="gender_empty_in_database" bundle="${resword}"/></td>
						</c:when>
						<c:when test="${gWarning=='true'}">
							<td><fmt:message key="gender_negative_match_database" bundle="${resword}"/></td>
						</c:when>
					</c:choose>
				</tr>
			</table>
		</td>
	</tr>
	</c:when>
	<c:otherwise>
		    <input type="hidden" name="gender" value="adfa<c:out value="${chosenGender}"/>">
	</c:otherwise>
	</c:choose>

	<c:choose>
	<c:when test="${study.studyParameterConfig.collectDob == '1'}">
	<tr valign="top">
		<td class="formlabel"><fmt:message key="date_of_birth" bundle="${resword}"/>:</td>
	  	<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
					  <c:choose>
					    <c:when test="${editDob=='yes'}">
						 <div class="formfieldM_BG"><input type="text" name="dob" size="15" value="<c:out value="${dob}" />" class="formfieldM" /></div>
					    </td>
					<td>
					<%-- (<fmt:message key="date_format" bundle="${resword}"/>)--%> *<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}">
					<a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?name=subject&field=dob&column=date_of_birth','spanAlert-dob'); return false;">
					<img name="flag_dob" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a>
					</c:if></td>
					    </c:when>
					    <c:otherwise>
					      &nbsp;<c:out value="${dob}"/>
					     <input type="hidden" name="dob" value="<c:out value="${dob}" />">
					    </td>
					    </c:otherwise>
					  </c:choose>

				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="dob"/></jsp:include></td>
				</tr>
				<tr>
					<c:choose>
						<c:when test="${dWarning=='dobYearWrong'}">
							<td>There is only year of birth in the database and it does not match the year you entered.  To accept the year in the database for current date of birth, please continue.  If you have any questions, please contact the administrator.</td>
						</c:when>
						<c:when test="${dWarning=='emptyD'}">
							<td>The date of birth in the database is empty.  To overwrite the database with the current date of birth, please continue.  If you have any questions, please contact the administrator.</td>
						</c:when>
						<c:when test="${dWarning=='dobUsed'}">
							<td>There is only year of birth in the database.  To overwrite the database with the current date of birth, please continue.  If you have any questions, please contact the administrator.</td>
						</c:when>
						<c:when test="${dWarning=='currentDOBWrong'}">
							<td>The date of birth you entered does not match what is in the database.  To accept the date of birth in the database, please continue.  If you need to change the date of birth, please contact the administrator.</td>
						</c:when>
					</c:choose>
				</tr>
			</table>
	  	</td>
	</tr>
	</c:when>
	<c:when test="${study.studyParameterConfig.collectDob == '2'}">
	<tr valign="top">
		<td class="formlabel"><fmt:message key="year_of_birth" bundle="${resword}"/>:</td>
	  	<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
					 &nbsp;<c:out value="${yob}" />
						<input type="hidden" name="yob" value="<c:out value="${yob}" />" />
					</td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="yob"/></jsp:include></td>
				</tr>
				<tr>
					<c:choose>
						<c:when test="${yWarning=='yobWrong'}">
							<td>The year of birth you entered does not match what is in the database.  To accept the date of birth in the database, please continue.  If you need to change the year of birth, please contact the administrator.</td>
						</c:when>
						<c:when test="${yWarning=='yearEmpty'}">
							<td>The year of birth in the database is empty.  To overwrite the database with the current year of birth, please continue.  If you have any questions, please contact the administrator.</td>
						</c:when>
					</c:choose>
				</tr>
			</table>
	  	</td>
	</tr>
  </c:when>
  <c:otherwise>
   <input type="hidden" name="dob" value="" />
  </c:otherwise>
 </c:choose>

 <!--
<c:if test="${study.genetic}">
	<c:if test="${!empty fathers}">
	<tr>
		<td class="formlabel"><fmt:message key="father" bundle="${resword}"/>:
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top"><div class="formfieldS_BG">
						<select name="father" class="formfieldS">
							<option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
							<c:forEach var="father" items="${fathers}">
								<c:choose>
									<c:when test="${fatherId == father.subject.id}">
										<option value="<c:out value="${father.subject.id}" />" selected>
										 <c:choose>
										 <c:when test="${father.subject.name!=null && father.subject.name!=''}">
										  <c:out value="${father.subject.name}"/>
										 </c:when>
										 <c:otherwise>
										   <c:out value="${father.studySubjectIds}"/>
										 </c:otherwise>
										</c:choose>
										</option>
									</c:when>
									<c:otherwise>
										<option value="<c:out value="${father.subject.id}" />">
										  <c:choose>
										    <c:when test="${father.subject.name!=null && father.subject.name!=''}">
										     <c:out value="${father.subject.name}"/>
										    </c:when>
										    <c:otherwise>
										     <c:out value="${father.studySubjectIds}"/>
										    </c:otherwise>
										</c:choose>
										</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
					</div></td>
					<td></td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="father"/></jsp:include></td>
				</tr>
			</table>
		</td>
	</tr>
	</c:if>

	<c:if test="${(!empty mothers)}">
	<tr>
		<td class="formlabel"><fmt:message key="mother" bundle="${resword}"/>:
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top"><div class="formfieldS_BG">
						<select name="mother" class="formfieldS">
							<option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
							<c:forEach var="mother" items="${mothers}">
								<c:choose>
									<c:when test="${motherId == mother.subject.id}">
										<option value="<c:out value="${mother.subject.id}" />" selected>
										 <c:choose>
										 <c:when test="${mother.subject.name!=null && mother.subject.name!=''}">
										  <c:out value="${mother.subject.name}"/>
										 </c:when>
										 <c:otherwise>
										   <c:out value="${mother.studySubjectIds}"/>
										 </c:otherwise>
										</c:choose>
										</option>
									</c:when>
									<c:otherwise>
										<option value="<c:out value="${mother.subject.id}" />">
										  <c:choose>
										   <c:when test="${mother.subject.name!=null && mother.subject.name!=''}">
										     <c:out value="${mother.subject.name}"/>
										   </c:when>
										   <c:otherwise>
										     <c:out value="${mother.studySubjectIds}"/>
										   </c:otherwise>
										 </c:choose>
										</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
					</td>
					<td></td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="mother"/></jsp:include></td>
				</tr>
			</table>
		</td>
	</tr>
	</c:if>
</c:if>
-->

</table>
</div>

</div></div></div></div></div></div></div></div>

</div>

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
	   <td><div class="formfieldM_BG">
	       <select name="studyGroupId<c:out value="${count}"/>" class="formfieldM">

	    	  <option value="0">--</option>

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
	    	<c:import url="../showMessage.jsp"><c:param name="key" value="studyGroupId${count}" /></c:import>

	    	</td>
	    	<c:if test="${group.subjectAssignment=='Required'}">
	    	  <td align="left">*</td>
	    	</c:if>
	    	</tr>
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

<br>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
<input type="submit" name="submitEvent" value="<fmt:message key="enroll_and_add_study_event" bundle="${resword}"/>" class="button_long">
</td>
<td><input type="submit" name="submitEnroll" value="<fmt:message key="enroll_and_add_another" bundle="${resword}"/>" class="button_long"></td>
<td><input type="submit" name="submitDone" value="<fmt:message key="enroll_and_finish" bundle="${resword}"/>" class="button_long"></td>
</tr>
</table>
</form>
<DIV ID="testdiv1" STYLE="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></DIV>
<br><br>

<c:import url="instructionsSetupStudyEvent.jsp">
	<c:param name="currStep" value="enroll" />
</c:import>

<jsp:include page="../include/footer.jsp"/>
