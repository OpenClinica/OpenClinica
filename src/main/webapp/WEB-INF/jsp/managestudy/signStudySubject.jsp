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
<link rel="stylesheet" href="includes/font-awesome-4.7.0/css/font-awesome.css">


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

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

        <fmt:message key="instructions" bundle="${restext}"/>

        <div class="sidebar_tab_content">
        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

        <fmt:message key="instructions" bundle="${restext}"/>

    </td>
</tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope="request" id="subject" class="core.org.akaza.openclinica.bean.submit.SubjectBean"/>
<jsp:useBean scope="request" id="parentStudy" class="core.org.akaza.openclinica.domain.datamap.Study"/>
<jsp:useBean scope="request" id="studySub" class="core.org.akaza.openclinica.bean.managestudy.StudySubjectBean"/>
<jsp:useBean scope="request" id="children" class="java.util.ArrayList"/>
<jsp:useBean scope='request' id='table' class='core.org.akaza.openclinica.web.bean.EntityBeanTable'/>
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

<p><fmt:message key="sure_to_sign_subject3" bundle="${resword}"/></p>

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
    <input type="hidden" name="studyId" value="<c:out value="${studySub.studyId}"/>">
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
                <c:when test="${study.collectDob == '1'}">
                    <tr>
                        <td class="table_header_column_top"><fmt:message key="date_of_birth" bundle="${resword}"/></td>
                        <td class="table_cell_top"><fmt:formatDate value="${subject.dateOfBirth}" pattern="${dteFormat}"/></td>
                    </tr>
                </c:when>
                <c:when test="${study.collectDob == '3'}">
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
                            <c:when test="${study.study != null && study.study.studyId>0}">
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
                    <td class="table_cell"><c:out value="${study.uniqueIdentifier}"/></td>
                </tr>
                <tr>
                    <td class="table_header_column"><fmt:message key="site_name" bundle="${resword}"/></td>
                    <td class="table_cell">
                        <c:if test="${study.study != null && study.study.studyId > 0}">
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
</c:choose> <%--<a name="events"><a href="javascript:leftnavExpand('subjectEvents');javascript:setImage('ExpandGroup2','images/bt_Expand.gif');"><img
  name="ExpandGroup2" src="images/bt_Collapse.gif" border="0"> <fmt:message key="events" bundle="${resword}"/></a></a></div>
<div id="subjectEvents">
    <c:import url="../include/showTable.jsp"><c:param name="rowURL" value="showStudyEventRow.jsp" /></c:import>


    </br></br>
</div>--%>
<%-- Subject discrepancy note table--%>

<div style="width: 900px">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<c:choose>
    <c:when test="${empty displayStudyEvents}">

    </c:when>

    <c:otherwise>
        <tr>
            <td class="table_header_row_left"><fmt:message key="events" bundle="${resword}"/></td>
            <td class="table_header_row"><fmt:message key="start_date" bundle="${resword}"/></td>
            <td class="table_header_row"><fmt:message key="CRF_name" bundle="${resword}"/></td>
            <td class="table_header_row"><fmt:message key="version" bundle="${resword}"/></td>
            <td class="table_header_row" style="min-width: 100px"><fmt:message key="status" bundle="${resword}"/></td>
            <td class="table_header_row"><fmt:message key="initial_data_entry" bundle="${resword}"/></td>
            <td class="table_header_row"><fmt:message key="view_discrepancy_notes" bundle="${resword}"/></td>
            <td class="table_header_row"><fmt:message key="actions" bundle="${resword}"/></td>
        </tr>
        <c:set var="rowCount" value="${0}" />
        <c:forEach var="dse" items="${displayStudyEvents}">
        <c:forEach var="dedc" items="${dse.uncompletedCRFs}">

            <c:set var="getQuery" value="action=ide_s&eventDefinitionCRFId=${dedc.edc.id}&studyEventId=${studyEvent.id}&subjectId=${studySub.subjectId}&eventCRFId=${dedc.eventCRF.id}" />
                <tr valign="top">
                            <c:set var="repeat" value="${dse.studyEvent.studyEventDefinition.name}(${dse.studyEvent.sampleOrdinal})" />
            <c:set var="non_repeat" value="${dse.studyEvent.studyEventDefinition.name}" />

                <td class="table_cell"><c:out value="${ dse.studyEvent.studyEventDefinition.repeating ? repeat :non_repeat }" />&nbsp;</td>
                <td class="table_cell"><fmt:formatDate value="${dse.studyEvent.dateStarted}" pattern="${dteFormat}"/>&nbsp;</td>

                    <td class="table_cell_left"><c:out value="${dedc.edc.crf.name}" /></td>
                  <td class="table_cell">

                <c:choose>
                    <c:when test="${dedc.eventCRF.id > 0}">
                        <!-- found an event crf id -->
                        <input type="hidden" name="crfVersionId" value="<c:out value="${dedc.eventCRF.CRFVersionId}"/>">
                    </c:when>
                    <c:otherwise>
                        <!-- did not find an event crf id -->
                        <input type="hidden" name="crfVersionId" value="<c:out value="${dedc.edc.defaultVersionId}"/>">
                    </c:otherwise>
                </c:choose>

                    <%--<input type="hidden" name="crfVersionId" value="<c:out value="${dedc.edc.defaultVersionId}"/>">--%>
                  <c:set var="versionCount" value="0"/>
                  <c:forEach var="version" items="${dedc.edc.versions}">
                    <c:set var="versionCount" value="${versionCount+1}"/>
                  </c:forEach>

                  <c:choose>
                    <c:when test="${versionCount<=1}">
                      <c:forEach var="version" items="${dedc.edc.versions}">
                        <c:out value="${version.name}"/>
                      </c:forEach>
                    </c:when>

                    <%--<c:otherwise>--%>
                    <c:when test="${dedc.eventCRF.id == 0}">

                    <select name="versionId<c:out value="${dedc.edc.crf.id}"/>" onchange="javascript:changeQuery<c:out value="${dedc.edc.crf.id}"/>();">

                      <c:forEach var="version" items="${dedc.edc.versions}">

                       <c:set var="getQuery" value="action=ide_s&eventDefinitionCRFId=${dedc.edc.id}&studyEventId=${currRow.bean.studyEvent.id}&subjectId=${studySub.subjectId}" />

                       <c:choose>
                         <c:when test="${dedc.edc.defaultVersionId==version.id}">
                           <option value="<c:out value="${version.id}"/>" selected>
                            <c:out value="${version.name}"/>
                            </option>
                         </c:when>
                         <c:otherwise>
                            <option value="<c:out value="${version.id}"/>">
                                <c:out value="${version.name}"/>
                            </option>
                         </c:otherwise>
                       </c:choose>

                      </c:forEach><%-- end versions --%>

                    </select>

                      <SCRIPT LANGUAGE="JavaScript">
                        function changeQuery<c:out value="${dedc.edc.crf.id}"/>() {
                          var qer = document.startForm<c:out value="${dedc.edc.crf.id}"/>.versionId<c:out value="${dedc.edc.crf.id}"/>.value;
                          document.startForm<c:out value="${dedc.edc.crf.id}"/>.crfVersionId.value=qer;

                        }
                     </SCRIPT>

                     <%--</c:otherwise>--%>
                     </c:when>

                     <c:otherwise>
                        <c:out value="${dedc.eventCRF.crfVersion.name}"/>
                     </c:otherwise>

                     </c:choose>

                    </td>
                    <td class="table_cell" bgcolor="#F5F5F5" align="center">
                        <table style="width: 100%;">
                            <tr style="width: inherit;">
                              <td style="width: 54%; text-align: end;">
                                <c:choose>
                                    <c:when test="${dedc.status.name=='locked'}">
                                        <span class="icon icon-lock" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>"></span>
                                    </c:when>
                                    <c:when test="${dedc.eventCRF.id>0}">
                                        <span class="icon icon-pencil-squared orange" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>" title="<fmt:message key="initial_data_entry" bundle="${resword}"/>">
                                    </c:when>
                                    <c:otherwise>
                                        <span class="icon icon-doc" alt="<fmt:message key="not_started" bundle="${resword}"/>" title="<fmt:message key="not_started" bundle="${resword}"/>">
                                    </c:otherwise>
                                </c:choose>
                              </td>
                              <td>
                                <c:if test="${dse.studyEvent.locked}">
                                    <span class="icon icon-lock-new status" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>"/>
                                </c:if>
                              </td>
                            </tr>
                        </table>
                    </td>

                    <td class="table_cell">&nbsp;</td>

                    <td class="table_cell">&nbsp;</td>

                    <td class="table_cell">
                <table>
                 <tr>
                 <c:choose>

                    <c:when test="${studyEvent.locked == true}">

                        &nbsp;
                    </c:when>

                    <c:otherwise></c:otherwise>
                    </c:choose>
                                   <a href="EnketoFormServlet?formLayoutId=<c:out value="${dedc.edc.defaultVersionId}"/>&studyEventId=<c:out value="${dse.studyEvent.id}"/>&eventCrfId=<c:out value="${dedc.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
                      onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
                      onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><span
                      name="bt_View1" align="left" class="icon icon-search" border="0" alt="<fmt:message key="view_default" bundle="${resword}"/>" title="<fmt:message key="view_default" bundle="${resword}"/>" hspace="2"></a>&nbsp;
                  </tr>
                    </table>

                </td>

               </tr>

                <c:set var="rowCount" value="${rowCount + 1}" />


        </c:forEach>
        <%-- end of for each for dedc, uncompleted event crfs --%>
        <c:forEach var="dec" items="${dse.displayEventCRFs}" varStatus="status">
            <c:set var="discNoteMap" value="${discNoteByEventCRFid[dec.eventCRF.id]}"/>

            <tr>
            <c:set var="repeat" value="${dse.studyEvent.studyEventDefinition.name}(${dse.studyEvent.sampleOrdinal})" />
            <c:set var="non_repeat" value="${dse.studyEvent.studyEventDefinition.name}" />

                <td class="table_cell"><c:out value="${ dse.studyEvent.studyEventDefinition.repeating ? repeat :non_repeat }" />&nbsp;</td>
                <td class="table_cell"><fmt:formatDate value="${dse.studyEvent.dateStarted}" pattern="${dteFormat}"/>&nbsp;</td>
                <td class="table_cell"><c:out value="${dec.eventCRF.crf.name}" />&nbsp;</td>
                <td class="table_cell">
                    <c:out value="${dec.eventCRF.crfVersion.name}" />&nbsp;
                    <c:if test="${dec.eventCRF.formLayout.status.name == 'Removed'}">
                        <span class="icon icon-archived-new status" alt="<fmt:message key="archived" bundle="${resword}"/>" title="<fmt:message key="archived" bundle="${resword}"/>"/>
                    </c:if>
                </td>
                <td class="table_cell" bgcolor="#F5F5F5" align="center">
                    <table style="width: 100%;">
                        <tr style="width: inherit;">
                            <td style="width: 54%; text-align: end;">
                                <c:choose>
                                    <c:when test="${ (dec.eventCRF.workflowStatus != 'NOT_STARTED' && dse.studyEvent.isRemoved()) || dec.eventCRF.isRemoved() }">
                                        <span class="icon icon-file-excel red" alt="<fmt:message key="invalid" bundle="${resword}"/>" title="<fmt:message key="removed" bundle="${resword}"/>">
                                    </c:when>
                                    <c:when test="${ dec.eventCRF.workflowStatus == 'NOT_STARTED'}">
                                        <span class="icon icon-doc" alt="<fmt:message key="not_started" bundle="${resword}"/>" title="<fmt:message key="not_started" bundle="${resword}"/>">
                                    </c:when>
                                    <c:when test="${dec.eventCRF.workflowStatus == 'INITIAL_DATA_ENTRY'}">
                                        <span class=" icon icon-pencil-squared orange" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>" title="<fmt:message key="data_entry_started" bundle="${resword}"/>">
                                    </c:when>
                                    <c:when test="${dec.eventCRF.workflowStatus == 'COMPLETED'}">
                                        <span class="icon icon-checkbox-checked green" alt="<fmt:message key="data_entry_complete" bundle="${resword}"/>" title="<fmt:message key="data_entry_complete" bundle="${resword}"/>">
                                    </c:when>
                                    <c:otherwise>
                                        <span class="icon icon-file-excel red" alt="<fmt:message key="invalid" bundle="${resword}"/>" title="<fmt:message key="removed" bundle="${resword}"/>">
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:if test="${dse.studyEvent.signed == true && !dec.eventCRF.isArchived() && !dec.eventCRF.isRemoved() && dec.eventCRF.workflowStatus == 'COMPLETED'}">
                                    <span class="icon icon-stamp-new status" alt="<fmt:message key="signed" bundle="${resword}"/>" title="<fmt:message key="signed" bundle="${resword}"/>"></span>
                                </c:if>
                                <c:if test="${dse.studyEvent.locked == true}">
                                    <span class="icon icon-lock-new status" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>"></span>
                                </c:if>
                                <c:if test="${dec.eventCRF.isArchived()}">
                                    <span class="icon icon-archived-new status" alt="<fmt:message key="archived" bundle="${resword}"/>" title="<fmt:message key="archived" bundle="${resword}"/>"></span>
                                </c:if>
                            </td>
                        </tr>
                    </table>
                </td>
                <td class="table_cell"><c:out value="${dec.eventCRF.owner.name}" />&nbsp;</td>


                <td class="table_cell">

                <table>
                                <tr><td>
                                        <span class="fa fa-bubble-red"  border="0"
                                          alt="<fmt:message key="Open" bundle="${resterm}"/>" title="<fmt:message key="Open" bundle="${resterm}"/>" align="left"/>
                                        ${discNoteMap['New']}
                                         &nbsp;New
                                </td></tr>
                                <tr><td>
                                        <span class="fa fa-bubble-orange" border="0"
                                          alt="<fmt:message key="Updated" bundle="${resterm}"/>" title="<fmt:message key="Updated" bundle="${resterm}"/>" align="left"/>
                                        ${discNoteMap['Updated']}
                                        &nbsp;Updated
                                </td></tr>
                                <tr><td>
                                        <span class="fa fa-bubble-black" border="0"
                                          alt="<fmt:message key="Closed" bundle="${resterm}"/>" title="<fmt:message key="Closed" bundle="${resterm}"/>" align="left"/>
                                        ${discNoteMap['Closed']}
                                        &nbsp;Closed
                                </td></tr>

                                <tr><td>
                                        <span class="fa fa-bubble-black" border="0"
                                          alt="<fmt:message key="Closed_Modified" bundle="${resterm}"/>" title="<fmt:message key="Closed_Modified" bundle="${resterm}"/>" align="left"/>
                                        ${discNoteMap['Closed-Modified']}
                                       &nbsp;Closed-Modified
                                </td></tr>
                    </table>
                    &nbsp;
                 </td>



                <td class="table_cell">
                    <c:set var="actionQuery" value="" />

                    <c:if test="${dec.continueInitialDataEntryPermitted}">
                        <c:set var="actionQuery" value="EnketoFormServlet?formLayoutId=${dec.eventCRF.formLayoutId}&studyEventId=${dse.studyEvent.id}&eventCrfId=${dec.eventCRF.id}&originatingPage=${originatingPage}&mode=edit"/>
                    </c:if>

                    <c:if test="${dec.startDoubleDataEntryPermitted}">
                        <c:set var="actionQuery" value="EnketoFormServlet?formLayoutId=${dec.eventCRF.formLayoutId}&studyEventId=${dse.studyEvent.id}&eventCrfId=${dec.eventCRF.id}&originatingPage=${originatingPage}&mode=edit"/>
                    </c:if>
                    <c:if test="${dec.continueDoubleDataEntryPermitted}">
                        <c:set var="actionQuery" value="EnketoFormServlet?formLayoutId=${dec.eventCRF.formLayoutId}&studyEventId=${dse.studyEvent.id}&eventCrfId=${dec.eventCRF.id}&originatingPage=${originatingPage}&mode=edit"/>
                    </c:if>


                    <c:if test="${dec.performAdministrativeEditingPermitted}">
                        <c:set var="actionQuery" value="EnketoFormServlet?formLayoutId=${dec.eventCRF.formLayoutId}&studyEventId=${dse.studyEvent.id}&eventCrfId=${dec.eventCRF.id}&originatingPage=${originatingPage}&mode=edit"/>
                    </c:if>

<%--
                    <c:if test="${dec.locked}">
                        locked
                    </c:if>

--%>

                    <c:choose>
                        <c:when test='${actionQuery == "" && dec.stage.name =="invalid" }'>
                                   <a href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayoutId}"/>&studyEventId=<c:out value="${dse.studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
                                onMouseDown="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
                                onMouseUp="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
                                ><span name="bt_View<c:out value="${rowCount}"/>" class="icon icon-search" border="0" alt="<fmt:message key="view_data" bundle="${resword}"/>" title="<fmt:message key="view_data" bundle="${resword}"/>" align="left" hspace="2"></a>&nbsp;
<!--
                            -->
                                &nbsp;

                        </c:when>

                        <c:when test='${actionQuery == ""}'>
                                   <a href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayoutId}"/>&studyEventId=<c:out value="${dse.studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
                                onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
                                onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"
                                ><span name="bt_View1" class="icon icon-search" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="2"></a>
<!--
                            <a href="javascript:openDocWindow('PrintDataEntry?ecId=<c:out value="${dec.eventCRF.id}"/>')"
                            -->
                                &nbsp;
                            <%-- added above 112007, tbh --%>
                        </c:when>
                        <c:otherwise>
                                   <a href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayoutId}"/>&studyEventId=<c:out value="${dse.studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
                                onMouseDown="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
                                onMouseUp="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
                                ><span name="bt_View<c:out value="${rowCount}"/>" class="icon icon-search" border="0" alt="<fmt:message key="view_data" bundle="${resword}"/>" title="<fmt:message key="view_data" bundle="${resword}"/>"  hspace="2"></a>&nbsp;
<!--
         <a href="javascript:openDocWindow('PrintDataEntry?ecId=<c:out value="${dec.eventCRF.id}"/>')"
   -->



                            <c:if test="${doRuleSetsExist[status.index]}" >
                            <a href="ExecuteCrossEditCheck?eventCrfId=<c:out value='${dec.eventCRF.id}'/>">execute Rule</a>
                            </c:if>
                    </c:otherwise>
                    </c:choose>
                </td>
            </tr>
            <c:set var="rowCount" value="${rowCount + 1}" />
        </c:forEach>
        </c:forEach>
    </c:otherwise>
</c:choose>
</table>
</div>
</div></div></div></div></div></div></div></div>
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
    </c:choose> <a name="global"><a href="javascript:leftnavExpand('globalRecord');javascript:setImage('ExpandGroup5','images/bt_Collapse.gif');"><img
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
                <c:when test="${study.collectDob == '1'}">
                    <tr>
                        <td class="table_header_column_top"><fmt:message key="date_of_birth" bundle="${resword}"/></td>
                        <td class="table_cell_top"><fmt:formatDate value="${subject.dateOfBirth}" pattern="${dteFormat}"/></td>
                    </tr>
                </c:when>
                <c:when test="${study.collectDob == '3'}">
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

<jsp:include page="../include/footer.jsp"/>
