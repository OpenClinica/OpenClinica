<%--
    User: Hamid
  Date: May 8, 2009
  Time: 8:29:31 PM
  To change this template use File | Settings | File Templates.
--%>


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<jsp:useBean scope="request" id="label" class="java.lang.String"/>

<jsp:useBean scope="session" id="study" class="org.akaza.openclinica.bean.managestudy.StudyBean" />
<jsp:useBean scope="request" id="pageMessages" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />

<jsp:useBean scope="request" id="groups" class="java.util.ArrayList" />

<c:set var="uniqueIdentifier" value="" />
<c:set var="chosenGender" value="" />
<c:set var="label" value="" />
<c:set var="secondaryLabel" value="" />
<c:set var="enrollmentDate" value="" />
<c:set var="startDate" value=""/>
<c:set var="dob" value="" />
<c:set var="yob" value="" />
<c:set var="groupId" value="${0}" />
<c:set var="studyEventDefinition" value=""/>
<c:set var="location" value=""/>

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
	<c:if test='${presetValue.key == "startDate"}'>
		<c:set var="startDate" value="${presetValue.value}" />
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
	
	<c:if test='${presetValue.key == "studyEventDefinition"}'>
		<c:set var="studyEventDefinition" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "location"}'>
		<c:set var="location" value="${presetValue.value}" />
	</c:if>
</c:forEach>


<form name="subjectForm" action="AddNewSubject" method="post">
<input type="hidden" name="subjectOverlay" value="true">

<div style="width: 500px; height: 550px; overflow: scroll; background:#FFFFFF; cursor:default">
<table border="0" cellpadding="0" >
    <tr style="height:10px;">
        <td width="35%"><h3><fmt:message key="add_new_subject" bundle="${resword}"/></h3></td>
        <td >&nbsp;</td>
    </tr>
    <tr valign="top">
        <td class="formlabel">
            <jsp:include page="../include/showSubmitted.jsp" />
            <input type="hidden" name="addWithEvent" value="1"/>
            <fmt:message key="study_subject_ID" bundle="${resword}"/>:</td>
        <td valign="top">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td valign="top"><div class="formfieldXL_BG">
                    <c:choose>
                     <c:when test="${study.studyParameterConfig.subjectIdGeneration =='auto non-editable'}">
                      <input onfocus="this.select()" type="text" value="<c:out value="${label}"/>" size="45" class="formfield" disabled>
                      <input type="hidden" name="label" value="<c:out value="${label}"/>">
                     </c:when>
                     <c:otherwise>
                       <input onfocus="this.select()" type="text" name="label" value="<c:out value="${label}"/>" size="50" class="formfieldXL">
                     </c:otherwise>
                    </c:choose>
                    </div></td>
                    <td>*</td>
                </tr>
                <tr>
                    <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="label"/></jsp:include></td>
                </tr>
                
            </table>
        </td>
    </tr>
    <c:choose>
    <c:when test="${study.studyParameterConfig.subjectPersonIdRequired =='required'}">
    <tr valign="top">
        <td class="formlabel"><fmt:message key="person_ID" bundle="${resword}"/>:</td>
        <td valign="top">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td valign="top"><div class="formfieldXL_BG">
                        <input onfocus="this.select()" type="text" name="uniqueIdentifier" value="<c:out value="${uniqueIdentifier}"/>" size="50" class="formfieldXL">
                    </div></td>
                    <td>*</td>
                </tr>
                <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="uniqueIdentifier"/></jsp:include></td>
            </table>
        </td>
    </tr>
    </c:when>
    <c:when test="${study.studyParameterConfig.subjectPersonIdRequired =='optional'}">
    <tr valign="top">
        <td class="formlabel"><fmt:message key="person_ID" bundle="${resword}"/>:</td>
        <td valign="top">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td valign="top"><div class="formfieldXL_BG">
                        <input onfocus="this.select()" type="text" name="uniqueIdentifier" value="<c:out value="${uniqueIdentifier}"/>" size="50" class="formfieldXL">
                    </div></td>
                    <td>&nbsp;</td>
                </tr>
                <tr>
                    <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="uniqueIdentifier"/></jsp:include></td>
                </tr>
            </table>
        </td>
    </tr>
    </c:when>
    <c:otherwise>
      <input type="hidden" name="uniqueIdentifier" value="<c:out value="${uniqueIdentifier}"/>">
    </c:otherwise>
    </c:choose>

    <tr valign="top">

        <td class="formlabel">
            <fmt:message key="enrollment_date" bundle="${resword}"/>:
        </td>
        <td valign="top">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td valign="top">
            <!--layer-background-color:white;-->
            <div class="formfieldM_BG">
                        <input onfocus="this.select()" type="text" name="enrollmentDate" size="15" value="<c:out value="${enrollmentDate}" />" class="formfieldM" id="enrollmentDateField" />
                    </td>
                    <td>
                    <A HREF="#">
                      <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="enrollmentDateTrigger" />
                        <script type="text/javascript">
                        Calendar.setup({inputField  : "enrollmentDateField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "enrollmentDateTrigger", customPX: 300, customPY: 10 });
                        </script>
                    </a>
                        *
                    </td>
                </tr>
                <tr>
                    <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="enrollmentDate"/></jsp:include></td>
                </tr>
            </table>
        </td>
    </tr>

    <tr valign="top">
        <c:if test="${study.studyParameterConfig.genderRequired !='not used'}">
        <td class="formlabel"><fmt:message key="gender" bundle="${resword}"/>:</td>
        <td valign="top">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td valign="top"><div class="formfieldS_BG">
                        <select name="gender" class="formfieldS">
                            <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
                            <c:choose>
                                <c:when test="${!empty chosenGender}">
                                    <c:choose>
                                        <c:when test='${chosenGender == "m"}'>
                                            <option value="m" selected><fmt:message key="male" bundle="${resword}"/></option>
                                            <option value="f"><fmt:message key="female" bundle="${resword}"/></option>
                                        </c:when>
                                        <c:otherwise>
                                            <option value="m"><fmt:message key="male" bundle="${resword}"/></option>
                                            <option value="f" selected><fmt:message key="female" bundle="${resword}"/></option>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <option value="m"><fmt:message key="male" bundle="${resword}"/></option>
                                    <option value="f"><fmt:message key="female" bundle="${resword}"/></option>
                                </c:otherwise>
                            </c:choose>
                            </select>
                </td>
    <td align="left">
        <c:choose>
        <c:when test="${study.studyParameterConfig.genderRequired !='false'}">
           <span class="formlabel">*</span>
        </c:when>
        </c:choose>
    </td>
    </tr>
    </table>
        </td>
    </c:if>
    </tr>
    <tr>
        <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="gender"/></jsp:include></td>
    </tr>


    <c:choose>
    <c:when test="${study.studyParameterConfig.collectDob == '1'}">
    <tr valign="top">
        <td class="formlabel"><fmt:message key="date_of_birth" bundle="${resword}"/>:</td>
        <td valign="top">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td valign="top"><div class="formfieldM_BG">
                        <input onfocus="this.select()" type="text" name="dob" size="15" value="<c:out value="${dob}" />" class="formfieldM" id="dobField" />
                    </td>
                    <td>
                    <A HREF="#">
                      <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="dobTrigger" />
                        <script type="text/javascript">
                        Calendar.setup({inputField  : "dobField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "dobTrigger", customPX: 300, customPY: 10 });
                        </script>
                    </a>
                    </td>
                    <td>* </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="dob"/></jsp:include></td>
    </tr>

    </c:when>
    <c:when test="${study.studyParameterConfig.collectDob == '2'}">
    <tr valign="top">
        <td class="formlabel"><fmt:message key="year_of_birth" bundle="${resword}"/>:</td>
        <td valign="top">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td valign="top"><div class="formfieldM_BG">
                        <input onfocus="this.select()" type="text" name="yob" size="15" value="<c:out value="${yob}" />" class="formfieldM" />
                    </td>
                    <td>(<fmt:message key="date_format_year" bundle="${resformat}"/>) *</td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="yob"/></jsp:include></td>
    </tr>

  </c:when>
  <c:otherwise>
    <input type="hidden" name="dob" value="" />
  </c:otherwise>
 </c:choose>
<c:if test="${(!empty studyGroupClasses)}">
    <tr valign="top">
      <td class="formlabel"><fmt:message key="subject_group_class" bundle="${resword}"/>:
      <td class="table_cell">
      <c:set var="count" value="0"/>
      <table border="0" cellpadding="0">
        <c:forEach var="group" items="${studyGroupClasses}">
        <tr valign="top">
         <td><b><c:out value="${group.name}"/></b></td>
         <td><div class="formfieldM_BG">
             <select name="studyGroupId<c:out value="${count}"/>" class="formfieldM">
                 <option value=""><c:out value="${group.name}"/>:</option>
                  <c:forEach var="studyGroup" items="${group.studyGroups}">
                    <option value="<c:out value="${studyGroup.id}"/>"><c:out value="${studyGroup.name}"/></option>
                  </c:forEach>
              </select></div>
             <c:import url="../showMessage.jsp"><c:param name="key" value="studyGroupId${count}" /></c:import>

              </td>
              <c:if test="${group.subjectAssignment=='Required'}">
                <td align="left">*</td>
              </c:if>
              </tr>
             <c:set var="count" value="${count+1}"/>
        </c:forEach>
        </table>
      </td>
    </tr>
</c:if>

    <tr valign="top">
        <td class="formlabel"><fmt:message key="SED_2" bundle="${resword}"/>:</td>
        <td valign="top">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr><td>
                    <div class="formfieldM_BG">
                        <select name="studyEventDefinition" class="formfieldM">
                            <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
                            <c:forEach var="event" items="${allDefsArray}">
                                <option <c:if test="${studyEventDefinition == event.id}">SELECTED</c:if> value="<c:out value="${event.id}"/>"><c:out value="${event.name}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    </td>
                    <td><span class="formlabel">*</span></td>
                </tr>
                <tr>
                    <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="studyEventDefinition"/></jsp:include></td>
                </tr>

            </table>
        </td>
    </tr>

    <tr valign="top">
        <td class="formlabel">
            <fmt:message key="start_date" bundle="${resword}"/>:
        </td>
          <td valign="top">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td valign="top">
                        <div class="formfieldM_BG">
                        <input type="text" name="startDate" size="15" value="<c:out value="${startDate}" />" class="formfieldM" id="enrollmentDateField2" />
                    </td>
                    <td>
                        <A HREF="#" >
                         <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="enrollmentDateTrigger2"/></a>*
                         <script type="text/javascript">
                         Calendar.setup({inputField  : "enrollmentDateField2", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "enrollmentDateTrigger2" ,customPX: 300, customPY: 10 });
                         </script>
                    </td>
                </tr>
                <tr>
                    <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="startDate"/></jsp:include></td>
                </tr>

            </table>
          </td>
    </tr>
    <c:choose>
    <c:when test="${study.studyParameterConfig.eventLocationRequired == 'required'}">
    <tr valign="top">
        <td class="formlabel"><fmt:message key="location" bundle="${resword}"/>:</td>
        <td valign="top">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td valign="top"><div class="formfieldXL_BG">
                       <input type="text" name="location"size="50" value="<c:out value="${location}"/>" class="formfieldXL">
                    </div></td>
                    <td>*</td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="location"/></jsp:include></td>
    </tr>

    </c:when>
    <c:when test="${study.studyParameterConfig.eventLocationRequired == 'optional'}">
    <tr valign="top">
        <td class="formlabel"><fmt:message key="location" bundle="${resword}"/>:</td>
        <td valign="top">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td valign="top"><div class="formfieldXL_BG">
                       <input type="text" name="location"size="50" class="formfieldXL">
                    </div></td>
                    <td>&nbsp;</td>
                </tr>
            </table>
        </td>
    </tr>
    </c:when>
    <c:otherwise>
        <input type="hidden" name="location" value=""/>
    </c:otherwise>
    </c:choose>
    <tr>
        <td colspan="2" align="center">
        <input type="submit" name="addSubject" value="<fmt:message key="add2" bundle="${resword}"/>" class="button" />
        &nbsp;
        <input type="button" id="cancel" name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>" class="button"/>

        <div id="dvForCalander" style="width:1px; height:1px;"></div>
    </td>
    </tr>

</table>

</div>

</form>