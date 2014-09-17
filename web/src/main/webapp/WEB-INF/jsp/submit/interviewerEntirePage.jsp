<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<head><title>OpenClinica interview</title>
   <%-- <fmt:message key="view_data_entry" bundle="${resword}"/>--%>
    <link rel="stylesheet" href="includes/styles.css" type="text/css" media="screen">
<%-- <link rel="stylesheet" href="includes/styles2.css" type="text/css">--%>
    <link rel="stylesheet" href="includes/print.css" type="text/css" media="print">
    <script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/Tabs.js"></script>
    <!-- <script type="text/JavaScript" language="JavaScript" src="includes/CalendarPopup.js"></script> -->
    <script type="text/javascript" language="JavaScript" src=
      "includes/repetition-model/repetition-model.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/prototype.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/scriptaculous.js?load=effects"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/effects.js"></script>
    <!-- Added for the new Calender -->

    <link rel="stylesheet" type="text/css" media="all" href="includes/new_cal/skins/aqua/theme.css" title="Aqua" />
    <script type="text/javascript" src="includes/new_cal/calendar.js"></script>
    <script type="text/javascript" src="includes/new_cal/lang/calendar-en.js"></script>
    <script type="text/javascript" src="includes/new_cal/lang/<fmt:message key="jscalendar_language_file" bundle="${resformat}"/>"></script>
    <script type="text/javascript" src="includes/new_cal/calendar-setup.js"></script>
    <!-- End -->

</head>
<body class="aka_bodywidth" onload="<c:if test='${popUpURL != ""}'>
		openDNoteWindow('<c:out value="${popUpURL}" />');</c:if>">

<div id="centralContainer" style=
  "padding-left:3em; margin-top:1em; background-color: white; color:black;">

<jsp:useBean scope="request" id="section" class="org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<jsp:useBean scope="request" id="displayItem" class="org.akaza.openclinica.bean.submit.DisplayItemBean" />
<jsp:useBean scope='request' id='formMessages' class='java.util.HashMap'/>

<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<c:set var="interviewer" value="${toc.eventCRF.interviewerName}" />
<c:set var="interviewDate" value="${toc.eventCRF.dateInterviewed}" />
<c:set var="itemId" value="${displayItem.item.id}" />
<%--<c:set var="inputVal" value="input${itemId}" />--%>

<c:set var="hasNameNote" value="${param.hasNameNote}"/>
<c:set var="hasDateNote" value="${param.hasDateNote}"/>

<c:forEach var="presetValue" items="${presetValues}">
    <c:if test='${presetValue.key == "interviewer"}'>
        <c:set var="interviewer" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "interviewDate"}'>
        <c:set var="interviewDate" value="${presetValue.value}" />
    </c:if>
</c:forEach>

<!-- End of Alert Box -->
<table border="0" cellpadding="0" cellspacing="0">
<tr id="CRF_infobox_closed"  style="display: none;">
    <td style="padding-top: 3px; padding-left: 6px; width: 90px;" nowrap>
        <a href="javascript:leftnavExpand('CRF_infobox_closed'); leftnavExpand('CRF_infobox_open');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="CRF_info" bundle="${resword}"/></b>
    </td>
</tr>
<tr id="CRF_infobox_open">

<td>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
    <td valign="bottom">
        <table border="0" cellpadding="0" cellspacing="0" width="100">
            <tr>
                <td nowrap>
                    <div class="tab_BG_h"><div class="tab_R_h" style="padding-right: 0px;"><div class="tab_L_h" style="padding: 3px 11px 0px 6px; text-align: left;">
                        <a href="javascript:leftnavExpand('CRF_infobox_closed'); leftnavExpand('CRF_infobox_open');"><img src="images/sidebar_collapse.gif" border="0" align="right"></a>

                        <b><fmt:message key="CRF_info" bundle="${resword}"/></b>
                    </div></div></div>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>

<td valign="top">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">

<table border="0" cellpadding="0" cellspacing="0" width="450">
<tr>
    <td colspan="2" class="table_header_row_left">
        <b><a
          href="ViewCRF?crfId=<c:out value="${toc.crf.id}"/>"> <c:out
          value="${toc.crf.name}" /> <c:out value="${toc.crfVersion.name}" />
        </a> <c:choose>
            <c:when test="${eventCRF.stage.initialDE}">
                <img src="images/icon_InitialDE.gif" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>"
                     title="<fmt:message key="initial_data_entry" bundle="${resword}"/>">
            </c:when>
            <c:when
              test="${eventCRF.stage.initialDE_Complete}">
                <img src="images/icon_InitialDEcomplete.gif"
                     alt="<fmt:message key="initial_data_entry_complete" bundle="${resword}"/>"
                     title="<fmt:message key="initial_data_entry_complete" bundle="${resword}"/>">
            </c:when>
            <c:when test="${eventCRF.stage.doubleDE}">
                <img src="images/icon_DDE.gif" alt="<fmt:message key="double_data_entry" bundle="${resword}"/>"
                     title="<fmt:message key="double_data_entry" bundle="${resword}"/>">
            </c:when>
            <c:when test="${eventCRF.stage.doubleDE_Complete}">
                <img src="images/icon_DEcomplete.gif" alt="<fmt:message key="data_entry_complete" bundle="${resword}"/>"
                     title="<fmt:message key="data_entry_complete" bundle="${resword}"/>">
            </c:when>
            <c:when test="${eventCRF.stage.admin_Editing}">
                <img src="images/icon_AdminEdit.gif"
                     alt="<fmt:message key="administrative_editing" bundle="${resword}"/>" title="<fmt:message key="administrative_editing" bundle="${resword}"/>">
            </c:when>
            <c:when test="${eventCRF.stage.locked}">
                <img src="images/icon_Locked.gif" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>">
            </c:when>
            <c:when test="${eventCRF.stage.invalid}">
                <img src="images/icon_Invalid.gif" alt="<fmt:message key="invalid" bundle="${resword}"/>" title="<fmt:message key="invalid" bundle="${resword}"/>">
            </c:when>
            <c:otherwise>
                <!-- leave blank -->
            </c:otherwise>
        </c:choose></b>

    </td>
    <td class="table_header_row" style="color: #789EC5"><b><fmt:message key="crf_notes" bundle="${resword}"/>:</b> </td>
    <td class="table_header_row_left" style="color: #789EC5"><font color="#CC0000"><c:out value="${openNum}"/> <fmt:message key="open" bundle="${resword}"/></font>, <font color="#D4A718"><c:out value="${updatedNum}"/> <fmt:message key="updated" bundle="${resword}"/></font>, <font color="#7CB98F"><c:out value="${resolvedNum}"/> <fmt:message key="resolved" bundle="${resword}"/></font>,
        <br><font color="#000000"><c:out value="${closedNum}"/> <fmt:message key="closed" bundle="${resword}"/></font>, <font color="#000000"><c:out value="${notAppNum}"/> <fmt:message key="not_applicable" bundle="${resword}"/></font>
    </td>

</tr>
<tr>
    <td class="table_cell_left" style="color: #789EC5">
        <b><fmt:message key="study_subject_ID" bundle="${resword}"/>:</b><br />
    </td>
    <td class="table_cell_left" style="color: #789EC5">
        <c:out value="${studySubject.label}" /><br />
    </td>
    <c:choose>
        <c:when test="${study.studyParameterConfig.personIdShownOnCRF == 'true'}">
            <td class="table_cell" style="color: #789EC5">
                <b><fmt:message key="person_ID" bundle="${resword}"/>:</b><br />
            </td>
            <td class="table_cell_left" style="color: #789EC5">
                <c:out value="${subject.uniqueIdentifier}" /><br />
            </td>

        </c:when>
        <c:otherwise>
            <td class="table_cell" style="color: #789EC5"><b><fmt:message key="person_ID" bundle="${resword}"/>:</b></td>
            <td class="table_cell_left" style="color: #789EC5"><fmt:message key="na" bundle="${resword}"/></td>
        </c:otherwise>
    </c:choose>
</tr>

<tr>
    <td class="table_cell_noborder" style="color: #789EC5">

        <b><fmt:message key="study_site" bundle="${resword}"/>:</b><br>
    </td>
    <td class="table_cell_noborder" style="color: #789EC5">
        <c:choose>
        <c:when test="${study.parentStudyId>0}">
        <a href="ViewSite?id=<c:out value="${study.id}"/>">
            </c:when>
            <c:otherwise>
            <a href="ViewStudy?id=<c:out value="${study.id}"/>&viewFull=yes">
                </c:otherwise>
                </c:choose> <c:out value="${studyTitle}" /></a><br>
    </td>
    <td class="table_cell_top" style="color: #789EC5">

        <b><fmt:message key="age_at_enrollment" bundle="${resword}"/>:</b><br>

    </td>
    <td class="table_cell_noborder" style="color: #789EC5">
        <c:out value="${age}" /><br>
    </td>
</tr>

<tr>
    <td class="table_cell_noborder" style="color: #789EC5">
        <b><fmt:message key="event" bundle="${resword}"/>:</b>
    </td>
    <td class="table_cell_noborder" style="color: #789EC5">
        <a
          href="EnterDataForStudyEvent?eventId=<c:out value="${toc.studyEvent.id}"/>"><c:out
          value="${toc.studyEventDefinition.name}" /></a>(<fmt:formatDate
      value="${toc.studyEvent.dateStarted}" pattern="${dteFormat}" />)
    </td>

    <td class="table_cell_top" style="color: #789EC5">
        <b><fmt:message key="date_of_birth" bundle="${resword}"/>:</b><br />
    </td>

    <%-- BWP>>5/24/07 : moved Gender in span tag to next to DOB --%>

    <td class="table_cell_noborder" style="color: #789EC5">
        <fmt:formatDate value="${subject.dateOfBirth}" pattern="${dteFormat}" />
        <span style="padding-left:6px;padding-right:6px"><b><fmt:message key="gender" bundle="${resword}"/>:</b></span>  <c:choose>
        <c:when test="${subject.gender==109}"><fmt:message key="M" bundle="${resword}"/></c:when>
        <c:when test="${subject.gender==102}"><fmt:message key="F" bundle="${resword}"/></c:when>
        <c:otherwise>
            <c:out value="${subject.gender}" />
        </c:otherwise>
    </c:choose><br />
    </td>

    <td class="table_cell_noborder" style="color: #789EC5">
        <!--<b>Gender:</b>-->
    </td>

    <td class="table_cell_noborder" style="color: #789EC5;padding-left:3px">
        <%--<c:choose>
          <c:when test="${subject.gender==109}">M</c:when>
          <c:when test="${subject.gender==102}">F</c:when>
          <c:otherwise>
            <c:out value="${subject.gender}" />
          </c:otherwise>
        </c:choose>--%>
    </td>
</tr>
<c:if test="${toc.studyEventDefinition.repeating}">
    <tr>
        <td class="table_cell_noborder" style="color: #789EC5">
            <b><fmt:message key="occurrence_number" bundle="${resword}"/>:</b>
        </td>
        <td class="table_cell_noborder" style="color: #789EC5">
            <c:out value="${toc.studyEvent.sampleOrdinal}" />
        </td>
        <td class="table_cell_top" style="color: #789EC5">

        </td>
        <td class="table_cell_noborder" style="color: #789EC5">
        </td>

    </tr>
</c:if>
<%--<tr>
  <td class="table_cell_noborder" style="color: #789EC5">

  </td>
  <td class="table_cell_noborder" style="color: #789EC5">
  </td>
  <td class="table_cell_top" style="color: #789EC5">
    <b><fmt:message key="gender" bundle="${resword}"/>:</b>
  </td>
  <td class="table_cell_noborder" style="color: #789EC5">
    <c:choose>
      <c:when test="${subject.gender==109}">M</c:when>
      <c:when test="${subject.gender==102}">F</c:when>
      <c:otherwise>
        <c:out value="${subject.gender}" />
      </c:otherwise>
    </c:choose>
  </td>
</tr>--%>
<%-- find out whether the item is involved with an error message, and if so, outline the
form element in red <c:out value="FORMMESSAGES: ${formMessages} "/><br/>--%>

<c:forEach var="frmMsg" items="${formMessages}">
    <c:if test="${frmMsg.key eq 'interviewer'}">
        <c:set var="isInError_Int" value="${true}" />
    </c:if>
    <c:if test="${frmMsg.key eq 'interviewDate'}">
        <c:set var="isInError_Dat" value="${true}" />
    </c:if>
</c:forEach>

<tr>
<td class="table_cell_left" nowrap>
    <c:choose>
        <c:when test="${isInError_Int}">
            <fmt:message key="interviewer_name" bundle="${resword}"/>: <span class="aka_exclaim_error">! </span> &nbsp;
        </c:when>

        <c:otherwise>
            <fmt:message key="interviewer_name" bundle="${resword}"/>:
            <c:if test="${study.studyParameterConfig.interviewerNameRequired=='true'}">
                *
            </c:if>
            &nbsp;
        </c:otherwise>
    </c:choose>
</td>
<td class="table_cell_left">
    <table border="0" cellpadding="0" cellspacing="0">
        <tr>

            <td valign="top">
                <!--  formfieldM_BG-->

                <c:choose>
                <c:when
                  test="${study.studyParameterConfig.interviewerNameEditable=='true'}">
                <c:choose>
                <c:when test="${isInError_Int}">
                <div class="aka_input_error">
                    <label for="interviewer"></label><input id="interviewer" type="text" name="interviewer" size="15"
                                                            value="<c:out value="${interviewer}" />" class="aka_input_error">
                    </c:when>
                    <c:otherwise>
                    <div class=" formfieldM_BG">
                        <input type="text" name="interviewer" size="15"
                               value="<c:out value="${interviewer}" />" class="formfieldM">
                        </c:otherwise>
                        </c:choose>
                        </c:when>
                        <c:otherwise>
                        <div class=" formfieldM_BG">
                            <input type="text" disabled size="15"
                                   value="<c:out value="${interviewer}" />" class="formfieldM">
                            <input type="hidden" name="interviewer"
                                   value="<c:out value="${interviewer}" />">
                            </c:otherwise>
                            </c:choose></div>
                        <%--BWP>>new error message design:  <jsp:include page="../showMessage.jsp">
                          <jsp:param name="key" value="interviewer" />
                        </jsp:include>--%>
            </td>
            <td valign="top" nowrap>
                <c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}">
                    <c:if test="${! (enclosingPage eq 'viewSectionData')}">
                        <a href="#" onClick="openDSNoteWindow('CreateDiscrepancyNote?viewData=y&id=<c:out value="${toc.eventCRF.id}"/>&name=eventCrf&field=interviewer&column=interviewer_name','spanAlert-interviewer'); return false;">
                            <c:choose>
                                <c:when test="${hasNameNote eq 'yes'}">
                                    <img id="flag_interviewer" name="flag_interviewer" src="images/icon_Note.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>">
                                </c:when>
                                <c:otherwise>
                                    <img id="flag_interviewer" name="flag_interviewer" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>">
                                </c:otherwise>
                            </c:choose>
                        </a>
                    </c:if>
                </c:if>
            </td>
        </tr>
        <tr>
            <td valign="top">
                <span ID="spanAlert-interviewer" class="alert"></span>
            </td>
        </tr>
    </table>
</td>

<td class="table_cell" nowrap>
    <c:choose>
        <c:when test="${isInError_Dat}">
            <fmt:message key="interview_date" bundle="${resword}"/>: <span class="aka_exclaim_error">! </span>&nbsp;<br />
            <%--(<fmt:message key="date_format" bundle="${resformat}"/>)--%>
        </c:when>
        <c:otherwise>
            <fmt:message key="interview_date" bundle="${resword}"/>:
            <c:if test="${study.studyParameterConfig.interviewDateRequired=='true'}">
                *
            </c:if>&nbsp;<br />
            <%--(<fmt:message key="date_format" bundle="${resformat}"/>)--%>
        </c:otherwise>
    </c:choose>
</td><!--</a>-->
<td class="table_cell_left">
    <table border="0" cellpadding="0" cellspacing="0">

        <tr>
            <%----%>
            <td valign="top">
                <c:choose>
                <c:when
                  test="${study.studyParameterConfig.interviewDateEditable=='true'}">
                <c:choose>
                <c:when test="${isInError_Dat}">
                <div class="aka_input_error">
                    <label for="interviewDate"></label>
                    <input id="interviewDate" type="text" name="interviewDate" size="15"
                           value="<c:out value="${interviewDate}" />" class="aka_input_error">
                    </c:when>
                    <c:otherwise>
                    <div class="formfieldM_BG">
                        <input id="interviewDate" type="text" name="interviewDate" size="15"
                               value="<c:out value="${interviewDate}" />" class="formfieldM">
                        </c:otherwise>
                        </c:choose>
                        </c:when>
                        <c:otherwise>
                        <div class="formfieldM_BG">
                            <input id="interviewDate" type="text" disabled size="15"
                                   value="<c:out value="${interviewDate}" />" class="formfieldM">
                            <input type="hidden" name="interviewDate"
                                   value="<c:out value="${interviewDate}" />">
                            </c:otherwise>
                            </c:choose>

                        </div>
                        <%-- BWP>>new error message design: <jsp:include page="../showMessage.jsp">
                          <jsp:param name="key" value="interviewDate" />
                        </jsp:include>--%>
            </td>
            <%--        document.getElementById('testdiv1').style.top=(parseInt(document.getElementById('testdiv1').style.top) - 10)+'px'; --%>
            <td valign="top" nowrap>

                <a href="#">
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="interviewDateTrigger" /></a>
                <script type="text/javascript">
                    Calendar.setup({inputField  : "interviewDate", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "interviewDateTrigger" });
                </script>

                <c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}">
                    <%--BWP: 2808 related>> --%>
                    <c:if test="${! (enclosingPage eq 'viewSectionData')}">
                        <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?id=<c:out value="${toc.eventCRF.id}"/>&name=eventCrf&field=interviewDate&column=date_interviewed','spanAlert-interviewDate'); return false;">
                            <c:choose>
                                <c:when test="${hasDateNote eq 'yes'}">
                                    <img id="flag_interviewDate" name="flag_interviewDate" src="images/icon_Note.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                                </c:when>
                                <c:otherwise>
                                    <img id="flag_interviewDate" name="flag_interviewDate" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                                </c:otherwise>
                            </c:choose>
                        </a>
                    </c:if>
                </c:if>
            </td>
        </tr>
        <tr>
            <td valign="top">
                <span ID="spanAlert-interviewDate" class="alert"></span>
            </td>
        </tr>
    </table>

</td>
</tr>
</table>

</div>

</div></div></div></div></div></div></div>


</td>
</tr>

</table>

</td>
</tr>
</table>
</div>
</body>
</html>