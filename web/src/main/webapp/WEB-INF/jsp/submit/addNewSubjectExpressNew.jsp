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
<c:set var="rand"><%= java.lang.Math.round(java.lang.Math.random() * 101) %></c:set>

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

<table border="0" cellpadding="0" align="center" style="cursor:default;">
    <tr style="height:10px;">
        <td class="formlabel" align="left"><h3 class="addNewSubjectTitle"><fmt:message key="add_new_subject" bundle="${resword}"/></h3></td>
    </tr>
    <tr>
        <td><div class="lines"></div></td>
    </tr>
    <tr>
        <td>
            <div style="max-height: 550px; min-width:400px; background:#FFFFFF; overflow-y: auto;">
            <table>
                <tr valign="top">
                    <td class="formlabel" align="left">
                        <jsp:include page="../include/showSubmitted.jsp" />
                        <input class="form-control" type="hidden" name="addWithEvent" value="1"/><span class="addNewStudyLayout">
                        <fmt:message key="study_subject_ID" bundle="${resword}"/></span>&nbsp;<small class="required">*</small></td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <td valign="top"><div class="formfieldXL_BG">
                                <c:choose>
                                 <c:when test="${study.studyParameterConfig.subjectIdGeneration =='auto non-editable'}">
                                  <input onfocus="this.select()" type="text" value="<c:out value="${label}"/>" size="45" class="formfield form-control" disabled>
                                  <input class="form-control" type="hidden" name="label" value="<c:out value="${label}"/>">
                                 </c:when>
                                 <c:otherwise>
                                   <input onfocus="this.select()" type="text" name="label" value="<c:out value="${label}"/>" width="30" class="formfieldXL form-control">
                                 </c:otherwise>
                                </c:choose>
                                </div></td>
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
                    <td class="formlabel" align="left">
                        <span class="addNewStudyLayout"><fmt:message key="person_ID" bundle="${resword}"/></span>&nbsp;<small class="required">*</small>
                    </td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <td valign="top"><div class="formfieldXL_BG">
                                    <input onfocus="this.select()" type="text" name="uniqueIdentifier" value="<c:out value="${uniqueIdentifier}"/>" width="30" class="formfieldXL form-control">
                                </div></td>
                            </tr>
                            <tr>
                                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="uniqueIdentifier"/></jsp:include></td>
                            </tr>
                        </table>
                    </td>
                </tr>
                </c:when>
                <c:when test="${study.studyParameterConfig.subjectPersonIdRequired =='optional'}">
                <tr valign="top">
                    <td class="formlabel" align="left">
                        <span class="addNewStudyLayout"><fmt:message key="person_ID" bundle="${resword}"/></span>
                    </td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <td valign="top"><div class="formfieldXL_BG">
                                    <input onfocus="this.select()" type="text" name="uniqueIdentifier" value="<c:out value="${uniqueIdentifier}"/>" width="30" class="formfieldXL form-control">
                                </div></td>
                            </tr>
                            <tr>
                                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="uniqueIdentifier"/></jsp:include></td>
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
                    <td class="formlabel" align="left">
                        <span class="addNewStudyLayout"><fmt:message key="secondary_ID" bundle="${resword}"/></span>
                    </td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <td valign="top"><div class="formfieldXL_BG">
                                    <input onfocus="this.select()" type="text" name="secondaryLabel" value="<c:out value="${secondaryLabel}"/>" width="30" class="formfieldXL form-control">
                                </div></td>
                            </tr>
                            <tr>
                                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="secondaryLabel"/></jsp:include></td>
                            </tr>
                        </table>
                    </td>
                </tr>

                <tr valign="top" >
                    <td class="formlabel" align="left">
                        <span class="addNewStudyLayout"><fmt:message key="enrollment_date" bundle="${resword}"/></span>&nbsp;<small class="required">*</small>
                    </td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <td>
                                    <input onfocus="this.select()" type="text" name="enrollmentDate" size="16" class="formfieldM form-control" id="enrollmentDateField_${rand}" />
                                </td>
                                <td valign="top" class="icon-container">
                                    <a href="#">
                                         <span class="icon icon-calendar" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="enrollmentDateTrigger_${rand}" />
                                            <script type="text/javascript">
                                            Calendar.setup({inputField  : "enrollmentDateField_${rand}", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "enrollmentDateTrigger_${rand}", customPX: 300, customPY: 10, randomize: "${rand}" });
                                            </script>
                                        </a>
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
                    <td class="formlabel" align="left">
                        <span class="addNewStudyLayout"><fmt:message key="gender" bundle="${resword}"/></span>
                        <c:choose>
                            <c:when test="${study.studyParameterConfig.genderRequired !='false'}">
                               &nbsp;<small class="required">*</small>
                            </c:when>
                        </c:choose>
                    </td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0">
                            <tr>
                                <td valign="top"><div class="selectS">
                                    <select name="gender">
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
                                        </select></div>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="gender"/></jsp:include></td>
                            </tr>
                        </table>
                    </td>
                </c:if>
                </tr>


                <c:choose>
                <c:when test="${study.studyParameterConfig.collectDob == '1'}">
                <tr valign="top">
                    <td class="formlabel" align="left"><span class="addNewStudyLayout"><fmt:message key="date_of_birth" bundle="${resword}"/></span>&nbsp;<small class="required">*</small></td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <td>
                                    <input onfocus="this.select()" type="text" name="dob" size="16" value="<c:out value="${dob}" />" class="formfieldM form-control" id="dobField_${rand}" />
                                </td>
                                <td valign="top" class="icon-container">
                                    <a href="#">
                                        <span class="icon icon-calendar" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="dobTrigger_${rand}" />
                                        <script type="text/javascript">
                                        Calendar.setup({inputField  : "dobField_${rand}", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "dobTrigger_${rand}", customPX: 300, customPY: 10, randomize: "${rand}" });
                                        </script>
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="dob"/></jsp:include></td>
                            </tr>
                        </table>
                    </td>
                </tr>

                </c:when>
                <c:when test="${study.studyParameterConfig.collectDob == '2'}">
                <tr valign="top">
                    <td class="formlabel" align="left"><span class="addNewStudyLayout"><fmt:message key="year_of_birth" bundle="${resword}"/></span>&nbsp;<small class="required">*</small></td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <td valign="top"><div class="formfieldM_BG">
                                    <input onfocus="this.select()" type="text" name="yob" size="15" value="<c:out value="${yob}" />" class="formfieldM form-control" />
                                </td>
                                <td class="formlabel" align="left">(<fmt:message key="date_format_year" bundle="${resformat}"/>)</td>
                            </tr>
                            <tr>
                                <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="yob"/></jsp:include></td>
                            </tr>
                        </table>
                    </td>
                </tr>

              </c:when>
              <c:otherwise>
                <input type="hidden" name="dob" value="" />
              </c:otherwise>
             </c:choose>
            <c:if test="${(!empty studyGroupClasses)}">
                <tr valign="top">
                  <td class="formlabel" align="left"><span class="addNewStudyLayout"><fmt:message key="subject_group_class" bundle="${resword}"/></span>
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
                            <td align="left">&nbsp;*</td>
                          </c:if>
                          </tr>
                         <c:set var="count" value="${count+1}"/>
                    </c:forEach>
                    </table>
                  </td>
                </tr>
            </c:if>

                <tr valign="top">
                    <td class="formlabel" align="left"><span class="addNewStudyLayout"><fmt:message key="SED_2" bundle="${resword}"/></span></td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0">
                            <tr><td>
                                <div class="selectS">
                                    <select name="studyEventDefinition" class="formfieldM">
                                        <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
                                        <c:forEach var="event" items="${allDefsArray}">
                                            <option <c:if test="${studyEventDefinition == event.id}">SELECTED</c:if> value="<c:out value="${event.id}"/>"><c:out value="${event.name}" />
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="studyEventDefinition"/></jsp:include></td>
                            </tr>
                        </table>
                    </td>
                </tr>

                <tr valign="top">
                    <td class="formlabel" align="left">
                        <span class="addNewStudyLayout"><fmt:message key="start_date" bundle="${resword}"/></span>
                        <c:if test="${studyEventDefinition > 0}">&nbsp;<small class="required">*</c:if>
                    </td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <td>
                                    <input type="text" name="startDate" size="15" class="formfieldM form-control" id="enrollmentDateField2_${rand}" />
                                </td>
                                <td valign="top" class="icon-container">
                                     <a href="#">
                                         <span class="icon icon-calendar" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="enrollmentDateTrigger2_${rand}"/></a>
                                         <script type="text/javascript">
                                         Calendar.setup({inputField  : "enrollmentDateField2_${rand}", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "enrollmentDateTrigger2_${rand}" ,customPX: 300, customPY: 10, randomize: "${rand}" });
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
                    <td class="formlabel" align="left">
                        <span class="addNewStudyLayout"><fmt:message key="location" bundle="${resword}"/></span>&nbsp;<small class="required">*</small>
                    </td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <td valign="top"><div class="formfieldXL_BG">
                                   <input type="text" name="location"size="50" value="<c:out value="${location}"/>" class="formfieldXL form-control">
                                </div></td>
                            </tr>
                            <tr>
                                <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="location"/></jsp:include></td>
                            </tr>
                        </table>
                    </td>
                </tr>

                </c:when>
                <c:when test="${study.studyParameterConfig.eventLocationRequired == 'optional'}">
                <tr valign="top">
                    <td class="formlabel" align="left">
                        <span class="addNewStudyLayout"><fmt:message key="location" bundle="${resword}"/></span>
                    </td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <td valign="top"><div class="formfieldXL_BG">
                                   <input type="text" name="location"size="50" class="formfieldXL form-control">
                                </div></td>
                            </tr>
                        </table>
                    </td>
                </tr>
                </c:when>
                <c:otherwise>
                    <input type="hidden" name="location" value=""/>
                </c:otherwise>
                </c:choose>

            </table>
            </div>
        </td>
    </tr>
    <tr>
        <td><div class="lines"></div></td>
    </tr>
    <tr>
        <td colspan="2" style="text-align: center;">
            <input type="submit" name="addSubject" value="Add"/>
            &nbsp;
            <input type="button" id="cancel" name="cancel" value="Cancel"/>

            <div id="dvForCalander_${rand}" style="width:1px; height:1px;"></div>
        </td>
    </tr>

</table>

</form>
