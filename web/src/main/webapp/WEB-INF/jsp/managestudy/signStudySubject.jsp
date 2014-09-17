<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<jsp:useBean id="date" class="java.util.Date" />
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>


<c:choose>
    <c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin && module=='admin'}">
        <c:import url="../include/admin-header.jsp"/>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${userRole.manageStudy && module=='manage'}">
                <c:import url="../include/managestudy-header.jsp"/>
            </c:when>
            <c:otherwise>
                <c:import url="../include/submit-header.jsp"/>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
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
    //-->
</script>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${restext}"/></b>

        <div class="sidebar_tab_content">
        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${restext}"/></b>

    </td>
</tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope="request" id="subject" class="org.akaza.openclinica.bean.submit.SubjectBean"/>
<jsp:useBean scope="request" id="parentStudy" class="org.akaza.openclinica.bean.managestudy.StudyBean"/>
<jsp:useBean scope="request" id="studySub" class="org.akaza.openclinica.bean.managestudy.StudySubjectBean"/>
<jsp:useBean scope="request" id="children" class="java.util.ArrayList"/>
<jsp:useBean scope='request' id='table' class='org.akaza.openclinica.web.bean.EntityBeanTable'/>
<jsp:useBean scope="request" id="groups" class="java.util.ArrayList"/>
<jsp:useBean scope="request" id="from" class="java.lang.String"/>

<script language="JavaScript">
    <!--
    function leftnavExpand(strLeftNavRowElementName){

        var objLeftNavRowElement;

        objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
        if (objLeftNavRowElement != null) {
            if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
            objLeftNavRowElement.display = (objLeftNavRowElement.display == "none" ) ? "" : "none";
        }
    }

    //-->
</script>

<table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr><td>
        <h1>
            <c:choose>
            <c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin && module=='admin'}">
            <div class="title_manage">
                </c:when>
                <c:otherwise>

                <c:choose>
                <c:when test="${userRole.manageStudy}">
                <div class="title_manage">
                    </c:when>
                    <c:otherwise>
                    <div class="title_manage">
                        </c:otherwise>
                        </c:choose>

                        </c:otherwise>
                        </c:choose>


                        <fmt:message key="sign_subject" bundle="${resword}"/>&nbsp;<c:out value="${studySub.label}"/>
                    </div>
    </td>
        <td align="right">
            <!-- <span style="font-size:11px"><a href="#"><img
		    src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"></a>View Printable Record</div>-->
            </h1>

        </td></tr>
</table>
<p><fmt:message key="sure_to_sign_subject" bundle="${resword}"/></p>

<p><fmt:message key="sure_to_sign_subject1" bundle="${resword}"/></p>

<b><fmt:message key="user_full_name" bundle="${resword}"/>: <c:out value="${userBean.firstName}"/>&nbsp;<c:out value="${userBean.lastName}"/>
    <br/>
    <fmt:message key="date_time" bundle="${resword}"/>: <fmt:formatDate value="${date}" type="both" pattern="${dteFormat}" timeStyle="long"/>
    <br/>
    <fmt:message key="sure_to_sign_subject2" bundle="${resword}"/>
    <br/>
    <fmt:message key="role" bundle="${resword}"/>: <c:out value="${userRole.role.description}"/></b>
<br><br>
<form action="SignStudySubject" method="post">
    <input type="hidden" name="id" value="<c:out value="${studySub.id}"/>">
    <input type="hidden" name="action" value="confirm">
    <div style="width: 250px">
        <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

            <table border="0" cellpadding="0" cellspacing="0">
                <tr valign="top"><td colspan="2">&nbsp;&nbsp;</td></tr>
                <tr valign="top"><td class="formlabel"><fmt:message key="user_name" bundle="${resword}"/>:</td>
                    <td>
                        <table border="0" cellpadding="0" cellspacing="0">
                            <tr><td>
                                <div class="formfieldM_BG"><input type="text" name="j_user" autocomplete="off" class="formfieldM"></div>
                            </td><td>&nbsp;</td>
                            </tr>
                        </table>
                    </td></tr>
                <tr valign="top"><td class="formlabel"><fmt:message key="password" bundle="${resword}"/></td>
                    <td>
                        <table border="0" cellpadding="0" cellspacing="0">
                            <tr><td>
                                <div class="formfieldM_BG"><input type="password" name="j_pass"  autocomplete="off" class="formfieldM"></div>
                            </td><td>&nbsp;</td>
                            </tr>
                        </table>
                    </td></tr>

                <tr valign="top"><td colspan="2">&nbsp;&nbsp;</td></tr>
            </table>
        </div>
        </div></div></div></div></div></div></div></div>

    <input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_long">
    <input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>"
           class="button_medium" onClick="javascript:myCancel();"/>
</form>
<br>
<p>
    <%--
        <a href="#events"><fmt:message key="events" bundle="${resword}"/></a> &nbsp; &nbsp; &nbsp;
    --%>
    <a href="#group"><fmt:message key="group" bundle="${resword}"/></a> &nbsp;&nbsp;&nbsp;
    <a href="#global"><fmt:message key="global_subject_record" bundle="${resword}"/></a> &nbsp;&nbsp;&nbsp;
    <a href="javascript:openDocWindow('ViewStudySubjectAuditLog?id=<c:out value="${studySub.id}"/>')"><fmt:message key="audit_logs" bundle="${resword}"/></a>
</p>
<c:choose>
    <c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin && module=='admin'}">
        <div class="table_title_Admin">
    </c:when>
    <c:otherwise>

        <c:choose>
            <c:when test="${userRole.manageStudy}">
                <div class="table_title_Manage">
            </c:when>
            <c:otherwise>
                <div class="table_title_Submit">
            </c:otherwise>
        </c:choose>

    </c:otherwise>
</c:choose>

<a href="javascript:leftnavExpand('subjectRecord');javascript:setImage('ExpandGroup1','images/bt_Expand.gif');"><img
  name="ExpandGroup1" src="images/bt_Collapse.gif" border="0"> <fmt:message key="subject_record_for" bundle="${restext}"/><c:out value="${studySub.label}"/></a></div>
<%-- removed broken CSS from below element: <div id="subjectRecord" style="display: "> --%>
<div id="subjectRecord">
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td valign="top" width="330" style="padding-right: 20px">



<!-- These DIVs define shaded box borders -->

<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">

<table border="0" cellpadding="0" cellspacing="0" width="330">

<!-- Table Actions row (pagination, search, tools) -->

<tr>

    <!-- Table Tools/Actions cell -->

    <td align="right" valign="top" class="table_actions">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td class="table_tools">
                    <c:if test="${userRole != null }">
                        <c:set var="roleName" value="${userRole.role.name}"/>
                        <c:if test="${userRole.manageStudy}">
                            <c:if test="${studySub.status.available}">
                                <a href="UpdateStudySubject?id=<c:out value="${studySub.id}"/>&action=show"><fmt:message key="edit_record" bundle="${resword}"/></a>
                            </c:if>
                        </c:if>
                    </c:if></td>
            </tr>
        </table>
    </td>

    <!-- End Table Tools/Actions cell -->
</tr>

<!-- end Table Actions row (pagination, search, tools) -->

<tr>
    <td valign="top">

        <!-- Table Contents -->

        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td class="table_header_column_top"><fmt:message key="study_subject_ID" bundle="${resword}"/></td>
                <td class="table_cell_top"><c:out value="${studySub.label}"/></td>
            </tr>
            <tr>
                <td class="table_header_column"><fmt:message key="secondary_ID" bundle="${resword}"/></td>
                <td class="table_cell"><c:out value="${studySub.secondaryLabel}"/></td>
            </tr>
            <tr>
                <td class="table_header_column"><fmt:message key="OID" bundle="${resword}"/></td>
                <td class="table_cell"><c:out value="${studySub.oid}"/></td>
            </tr>
            <tr>
                <td class="table_divider" colspan="2">&nbsp;</td>
            </tr>

            <tr>
                <td class="table_header_column_top"><fmt:message key="person_ID" bundle="${resword}"/></td>
                <td class="table_cell_top"><c:out value="${subject.uniqueIdentifier}"/></td>
            </tr>
            <c:choose>
                <c:when test="${study.studyParameterConfig.collectDob == '1'}">
                    <tr>
                        <td class="table_header_column_top"><fmt:message key="date_of_birth" bundle="${resword}"/></td>
                        <td class="table_cell_top"><fmt:formatDate value="${subject.dateOfBirth}" pattern="${dteFormat}"/></td>
                    </tr>
                </c:when>
                <c:when test="${study.studyParameterConfig.collectDob == '3'}">
                    <tr>
                        <td class="table_header_column_top"><fmt:message key="date_of_birth" bundle="${resword}"/></td>
                        <td class="table_cell_top"><fmt:message key="not_used" bundle="${resword}"/></td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <tr>
                        <td class="table_header_column_top"><fmt:message key="year_of_birth" bundle="${resword}"/></td>
                        <td class="table_cell_top"><c:out value="${yearOfBirth}"/></td>
                    </tr>
                </c:otherwise>
            </c:choose>
            <tr>
                <td class="table_header_column"><fmt:message key="gender" bundle="${resword}"/></td>
                <td class="table_cell">
                    <c:choose>
                        <c:when test="${subject.gender==32}">
                            &nbsp;
                        </c:when>
                        <c:when test="${subject.gender==109 ||subject.gender==77}">
                            <fmt:message key="male" bundle="${resword}"/>
                        </c:when>
                        <c:otherwise>
                            <fmt:message key="female" bundle="${resword}"/>
                        </c:otherwise>
                    </c:choose>

                </td>
            </tr>
            <tr>
                <td class="table_header_column"><fmt:message key="enrollment_date" bundle="${resword}"/></td>
                <td class="table_cell_top"><fmt:formatDate value="${studySub.enrollmentDate}" pattern="${dteFormat}"/>&nbsp;</td>
            </tr>

        </table>

        <!-- End Table Contents -->

    </td>
</tr>
</table>


</div>

</div></div></div></div></div></div></div></div>

</td>


<td valign="top" width="350" style="padding-right: 20px">

    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

        <div class="tablebox_center">
            <table border="0" cellpadding="0" cellspacing="0" width="330">
                <tr>
                    <td colspan="2" align="right" valign="top" class="table_actions">&nbsp;
                    </td>
                </tr>
                <tr>
                    <td class="table_header_column_top"><fmt:message key="study_name" bundle="${resword}"/></td>
                    <td class="table_cell_top">
                        <c:choose>
                            <c:when test="${study.parentStudyId>0}">
                                <c:out value="${parentStudy.name}"/>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${study.name}"/>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <tr>
                    <td class="table_header_column"><fmt:message key="unique_protocol_ID" bundle="${resword}"/></td>
                    <td class="table_cell"><c:out value="${study.identifier}"/></td>
                </tr>
                <tr>
                    <td class="table_header_column"><fmt:message key="site_name" bundle="${resword}"/></td>
                    <td class="table_cell">
                        <c:if test="${study.parentStudyId>0}">
                            <c:out value="${study.name}"/>
                        </c:if>&nbsp;</td>
                </tr>


                <tr>
                    <td class="table_divider" colspan="2">&nbsp;</td>
                </tr>
                <tr>
                    <td class="table_header_column_top"><fmt:message key="date_record_created" bundle="${resword}"/></td>
                    <td class="table_cell_top"><fmt:formatDate value="${studySub.createdDate}" pattern="${dteFormat}"/></td>
                </tr>
                <tr>
                    <td class="table_header_column"><fmt:message key="created_by" bundle="${resword}"/></td>
                    <td class="table_cell"><c:out value="${studySub.owner.name}"/></td>
                </tr>
                <tr>
                    <td class="table_header_column"><fmt:message key="date_record_last_updated" bundle="${resword}"/></td>
                    <td class="table_cell"><fmt:formatDate value="${studySub.updatedDate}" pattern="${dteFormat}"/>&nbsp;</td>
                </tr>
                <tr>
                    <td class="table_header_column"><fmt:message key="updated_by" bundle="${resword}"/></td>
                    <td class="table_cell"><c:out value="${studySub.updater.name}"/>&nbsp;</td>
                </tr>
                <tr>
                    <td class="table_header_column"><fmt:message key="status" bundle="${resword}"/></td>
                    <td class="table_cell"><c:out value="${studySub.status.name}"/></td>
                </tr>
            </table>
        </div>

    </div></div></div></div></div></div></div></div>

</td>
</tr>
</table>
<br><br>
</div>
<c:choose>
    <c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin && module=='admin'}">
        <div class="table_title_Admin">
    </c:when>
    <c:otherwise>

        <c:choose>
            <c:when test="${userRole.manageStudy}">
                <div class="table_title_manage">
            </c:when>
            <c:otherwise>
                <div class="table_title_submit">
            </c:otherwise>
        </c:choose>

    </c:otherwise>
</c:choose>	<%--<a name="events"><a href="javascript:leftnavExpand('subjectEvents');javascript:setImage('ExpandGroup2','images/bt_Expand.gif');"><img
  name="ExpandGroup2" src="images/bt_Collapse.gif" border="0"> <fmt:message key="events" bundle="${resword}"/></a></a></div>
<div id="subjectEvents">
    <c:import url="../include/showTable.jsp"><c:param name="rowURL" value="showStudyEventRow.jsp" /></c:import>


    </br></br>
</div>--%>
<%-- Subject discrepancy note table--%>
<div id="subjDiscNoteDivTitle" class="subjDiscNoteDivTitle">
    <a id="discNoteDivParent" href="javascript:void(0)"
       onclick="showSummaryBox(document.getElementById('subjDiscNoteDiv'),document.getElementById('discNoteDivParent'),'<fmt:message key="show_event_notes" bundle="${resword}"/>','<fmt:message key="hide_event_notes" bundle="${resword}"/>')"><fmt:message key="show_event_notes" bundle="${resword}"/></a>
</div>
<div id="subjDiscNoteDiv" class="subjDiscNoteDiv" style="display:none">
    <table class="subjDiscNoteTable" cellpadding="0" cellspacing="0">
        <thead>
            <th class="table_header_row_left">Event Name</th>
            <th class="table_header_row">CRF Name</th>
            <th class="table_header_row">New</th>
            <th class="table_header_row">Updated</th>
            <th class="table_header_row">Resolution Proposed</th>
            <th class="table_header_row">Closed</th>
            <th class="table_header_row">Not Applicable</th>
            <th class="table_header_row">Actions</th>
        </thead>
        <tbody>
            <c:set var="hasEvents" value="${! (empty displayStudyEvents)}" />
            <c:set var="hasEventCRFs" value="${false}" />
            <c:forEach var="displayStudyEventBean" items="${displayStudyEvents}">
             <c:if test="${! (empty displayStudyEventBean.displayEventCRFs)}">
                 <c:set var="hasEventCRFs" value="${true}" />
             </c:if>
            </c:forEach>

            <c:choose>
                <c:when test="${(! hasEvents) || (! hasEventCRFs)}">
                    <tr>
                    <td class="table_cell_left"><fmt:message key="there_are_no_rows_because_no_events" bundle="${resword}"/></td>
                    </tr>
                </c:when>
                <c:otherwise>

                    <c:forEach var="displayStudyEventBean" items="${displayStudyEvents}">
                        <c:forEach var="displayEventCRFBean" items="${displayStudyEventBean.displayEventCRFs}">
                            <c:set var="discNoteMap" value="${discNoteByEventCRFid[displayEventCRFBean.eventCRF.id]}"/>

                            <tr>
                                <td class="table_cell_left">
                                        ${displayStudyEventBean.studyEvent.studyEventDefinition.name}</td>
                                <td class="table_cell">${displayEventCRFBean.eventCRF.crf.name}</td>
                                <td class="table_cell">
                                    <c:set var="discNoteCount" value="${discNoteMap['New']}"/>
                                    <c:if test="${discNoteCount > 0}">
                                        <img
                                          name="icon_Note" src="images/icon_Note.gif" border="0"
                                          alt="<fmt:message key="Open" bundle="${resterm}"/>" title="<fmt:message key="Open" bundle="${resterm}"/>" align="left"/>
                                        (${discNoteCount})
                                        <c:set var="discNoteCount" value="${0}"/>
                                    </c:if>
                                </td><%-- new --%>
                                <td class="table_cell">
                                    <c:set var="discNoteCount" value="${discNoteMap['Updated']}"/>
                                    <c:if test="${discNoteCount > 0}">
                                        <img
                                          name="icon_Note" src="images/icon_flagYellow.gif" border="0"
                                          alt="<fmt:message key="Updated" bundle="${resterm}"/>" title="<fmt:message key="Updated" bundle="${resterm}"/>" align="left"/>
                                        (${discNoteCount})
                                        <c:set var="discNoteCount" value="${0}"/>
                                    </c:if>
                                </td><%-- updated --%>
                                <td class="table_cell">
                                    <c:set var="discNoteCount" value="${discNoteMap['Resolution Proposed']}"/>
                                    <c:if test="${discNoteCount > 0}">
                                        <img
                                          name="icon_Note" src="images/icon_flagGreen.gif" border="0"
                                          alt="<fmt:message key="Resolved" bundle="${resterm}"/>" title="<fmt:message key="Resolved" bundle="${resterm}"/>" align="left"/>
                                        (${discNoteCount})
                                        <c:set var="discNoteCount" value="${0}"/>
                                    </c:if>
                                </td><%-- Resolution Proposed --%>
                                <td class="table_cell">
                                    <c:set var="discNoteCount" value="${discNoteMap['Closed']}"/>
                                    <c:if test="${discNoteCount > 0}">
                                        <img
                                          name="icon_Note" src="images/icon_flagBlack.gif" border="0"
                                          alt="<fmt:message key="Closed" bundle="${resterm}"/>" title="<fmt:message key="Closed" bundle="${resterm}"/>" align="left"/>
                                        (${discNoteCount})
                                        <c:set var="discNoteCount" value="${0}"/>
                                    </c:if>
                                </td><%-- closed --%>
                                <td class="table_cell">
                                    <c:set var="discNoteCount" value="${discNoteMap['Not Applicable']}"/>
                                    <c:if test="${discNoteCount > 0}">
                                        <img
                                          name="icon_Note" src="images/icon_flagWhite.gif" border="0"
                                          alt="<fmt:message key="Not_Applicable" bundle="${resterm}"/>" title="<fmt:message key="Not_Applicable" bundle="${resterm}"/>" align="left"/>
                                        (${discNoteCount})
                                        <c:set var="discNoteCount" value="${0}"/>
                                    </c:if>
                                </td><%-- N/A --%>
                                <td class="table_cell">
                                    <a onmouseup="javascript:setImage('bt_View1','images/bt_View.gif');" onmousedown="javascript:setImage('bt_View1','images/bt_View_d.gif');" href="EnterDataForStudyEvent?eventId=${displayStudyEventBean.studyEvent.id}">
                                        <img hspace="6" border="0" align="left" title="View" alt="View" src="images/bt_View.gif" name="bt_View1"/>
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>

<div style="width: 250px">

<c:choose>
<c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin && module=='admin'}">
<div class="table_title_Admin">
</c:when>
<c:otherwise>

<c:choose>
<c:when test="${userRole.manageStudy}">
<div class="table_title_Manage">
</c:when>
<c:otherwise>
<div class="table_title_Submit">
    </c:otherwise>
    </c:choose>

    </c:otherwise>
    </c:choose><a name="group"><a href="javascript:leftnavExpand('groups');javascript:setImage('ExpandGroup3','images/bt_Collapse.gif');"><img
  name="ExpandGroup3" src="images/bt_Expand.gif" border="0"> <fmt:message key="group" bundle="${resword}"/></a></a></div>
<div id="groups" style="display:none">
    <div style="width: 600px">
        <!-- These DIVs define shaded box borders -->
        <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

            <div class="tablebox_center">

                <table border="0" cellpadding="0" cellspacing="0" width="100%">

                    <!-- Table Actions row (pagination, search, tools) -->

                    <tr>

                        <!-- Table Tools/Actions cell -->

                        <td align="right" valign="top" class="table_actions">
                            <table border="0" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td class="table_tools"><a href="UpdateStudySubject?id=<c:out value="${studySub.id}"/>&action=show"><fmt:message key="assign_subject_to_group" bundle="${resworkflow}"/></a></td>
                                </tr>
                            </table>
                        </td>

                        <!-- End Table Tools/Actions cell -->
                    </tr>

                    <!-- end Table Actions row (pagination, search, tools) -->

                    <tr>
                        <td valign="top">

                            <!-- Table Contents -->

                            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                                <tr>
                                    <td class="table_header_row_left"><fmt:message key="subject_group_class" bundle="${resword}"/></td>
                                    <td class="table_header_row"><fmt:message key="study_group" bundle="${resword}"/></td>
                                    <td class="table_header_row"><fmt:message key="notes" bundle="${resword}"/></td>
                                </tr>
                                <c:choose>
                                    <c:when test="${!empty groups}">
                                        <c:forEach var="group" items="${groups}">
                                            <tr>
                                                <td class="table_cell_left"><c:out value="${group.groupClassName}"/></td>
                                                <td class="table_cell"><c:out value="${group.studyGroupName}"/></td>
                                                <td class="table_cell"><c:out value="${group.notes}"/>&nbsp;</td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td class="table_cell" colspan="2"><fmt:message key="currently_no_groups" bundle="${resword}"/></td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </table>

                            <!-- End Table Contents -->

                        </td>
                    </tr>
                </table>


            </div>

        </div></div></div></div></div></div></div></div>

    </div>

    <br><br>
</div>

<div style="width: 250px">

<c:choose>
<c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin && module=='admin'}">
<div class="table_title_Admin">
</c:when>
<c:otherwise>

<c:choose>
<c:when test="${userRole.manageStudy}">
<div class="table_title_Manage">
</c:when>
<c:otherwise>
<div class="table_title_Submit">
    </c:otherwise>
    </c:choose>

    </c:otherwise>
    </c:choose>
</div>
<c:choose>
<c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin && module=='admin'}">
<div class="table_title_Admin">
</c:when>
<c:otherwise>

<c:choose>
<c:when test="${userRole.manageStudy}">
<div class="table_title_manage">
</c:when>
<c:otherwise>
<div class="table_title_submit">
    </c:otherwise>
    </c:choose>

    </c:otherwise>
    </c:choose>	<a name="global"><a href="javascript:leftnavExpand('globalRecord');javascript:setImage('ExpandGroup5','images/bt_Collapse.gif');"><img
  name="ExpandGroup5" src="images/bt_Expand.gif" border="0"> <fmt:message key="global_subject_record" bundle="${resword}"/></a></a></div>

<div id="globalRecord" style="display:none">
<div style="width: 350px">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">

<table border="0" cellpadding="0" cellspacing="0" width="330">

<!-- Table Actions row (pagination, search, tools) -->

<tr>

    <!-- Table Tools/Actions cell -->

    <td align="right" valign="top" class="table_actions">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td class="table_tools">
                    <c:if test="${userBean.sysAdmin && subject.status.available }">
                        <a href="UpdateSubject?id=<c:out value="${subject.id}"/>&studySubId=<c:out value="${studySub.id}"/>&action=show"><fmt:message key="edit_record" bundle="${resword}"/></a>
                    </c:if>
                </td>
            </tr>
        </table>
    </td>

    <!-- End Table Tools/Actions cell -->
</tr>

<!-- end Table Actions row (pagination, search, tools) -->

<tr>
    <td valign="top">

        <!-- Table Contents -->

        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td class="table_header_column_top"><fmt:message key="person_ID" bundle="${resword}"/></td>
                <td class="table_cell_top"><c:out value="${subject.uniqueIdentifier}"/></td>
            </tr>
            <tr>
                <td class="table_divider" colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td class="table_header_column_top"><fmt:message key="date_record_created" bundle="${resword}"/></td>
                <td class="table_cell_top"><fmt:formatDate value="${subject.createdDate}" pattern="${dteFormat}"/></td>
            </tr>
            <tr>
                <td class="table_header_column"><fmt:message key="created_by" bundle="${resword}"/></td>
                <td class="table_cell"><c:out value="${subject.owner.name}"/></td>
            </tr>
            <tr>
                <td class="table_header_column"><fmt:message key="date_record_last_updated" bundle="${resword}"/></td>
                <td class="table_cell"><fmt:formatDate value="${subject.updatedDate}" pattern="${dteFormat}"/>&nbsp;</td>
            </tr>
            <tr>
                <td class="table_header_column"><fmt:message key="updated_by" bundle="${resword}"/></td>
                <td class="table_cell"><c:out value="${subject.updater.name}"/>&nbsp;</td>
            </tr>
            <tr>
                <td class="table_header_column"><fmt:message key="status" bundle="${resword}"/></td>
                <td class="table_cell"><c:out value="${subject.status.name}"/></td>
            </tr>
            <tr>
                <td class="table_divider" colspan="2">&nbsp;</td>
            </tr>
            <c:choose>
                <c:when test="${study.studyParameterConfig.collectDob == '1'}">
                    <tr>
                        <td class="table_header_column_top"><fmt:message key="date_of_birth" bundle="${resword}"/></td>
                        <td class="table_cell_top"><fmt:formatDate value="${subject.dateOfBirth}" pattern="${dteFormat}"/></td>
                    </tr>
                </c:when>
                <c:when test="${study.studyParameterConfig.collectDob == '3'}">
                    <tr>
                        <td class="table_header_column_top"><fmt:message key="date_of_birth" bundle="${resword}"/></td>
                        <td class="table_cell_top">&nbsp;</td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <tr>
                        <td class="table_header_column_top"><fmt:message key="year_of_birth" bundle="${resword}"/></td>
                        <td class="table_cell_top"><c:out value="${yearOfBirth}"/></td>
                    </tr>
                </c:otherwise>
            </c:choose>
            <tr>
                <td class="table_header_column"><fmt:message key="gender" bundle="${resword}"/></td>
                <td class="table_cell">
                    <c:choose>
                        <c:when test="${subject.gender==32}">
                            &nbsp;
                        </c:when>
                        <c:when test="${subject.gender==109 ||subject.gender==77}">
                            <fmt:message key="male" bundle="${resword}"/>
                        </c:when>
                        <c:otherwise>
                            <fmt:message key="female" bundle="${resword}"/>
                        </c:otherwise>
                    </c:choose>

                </td>
            </tr>
            
        </table>

        <!-- End Table Contents -->

    </td>
</tr>
</table>


</div>

</div></div></div></div></div></div></div></div>
</div>

</div>
<c:choose>
<c:when test="${from =='listSubject' && userBean.sysAdmin && module=='admin'}">
<p> <a href="ViewSubject?id=<c:out value="${subject.id}"/>"><fmt:message key="go_back_to_view_subject" bundle="${resword}"/></a>  </p>
</c:when>
<c:otherwise>

<c:choose>
<c:when test="${(userRole.manageStudy)&& module=='manage'}">
<p> <a href="ListStudySubjects"><fmt:message key="go_back_to_study_subject_list" bundle="${resword}"/></a>  </p>
</c:when>
<c:otherwise>
<p><a href="ListStudySubjects"><fmt:message key="go_back_to_subject_list" bundle="${resword}"/></a>  </p>
</c:otherwise>
</c:choose>
</c:otherwise>
</c:choose>


<!-- End Main Content Area -->


<c:import url="../include/workflow.jsp">
    <c:param name="module" value="manage"/>
</c:import>

<jsp:include page="../include/footer.jsp"/>
