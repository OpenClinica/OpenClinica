<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>
<link rel="stylesheet" href="includes/font-awesome-4.7.0/css/font-awesome.css">



<c:choose>
 <c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin && module=='admin'}">
   <c:import url="../include/admin-header.jsp"/>
 </c:when>
 <c:otherwise>
  <c:choose>
   <c:when test="${userRole.manageStudy}">
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
<script type="text/JavaScript" language="JavaScript">
  <!--
 function myCancel() {

    cancelButton=document.getElementById('cancel');
    if ( cancelButton != null) {
      if(confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>')) {

        window.location.href='ViewStudySubject?id=<c:out value="${studySubject.id}"/>';
       return true;
      } else {
        return false;
       }
     }
     return true;

  }
   //-->
</script>
<h1><c:choose>
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
  <fmt:message key="sign_event_for_subject" bundle="${resworkflow}">
          <fmt:param><c:out value="${studyEvent.studyEventDefinition.name}" /></fmt:param>
          <fmt:param><c:out value="${studySubject.label}" /></fmt:param>
  </fmt:message>
</div></h1>

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



<jsp:useBean scope='request' id='eventId' class='java.lang.String'/>
<c:set var="eventId" value="${eventId}"/>

<jsp:useBean scope="request" id="studyEvent" class="core.org.akaza.openclinica.bean.managestudy.StudyEventBean" />
<jsp:useBean scope="request" id="studySubject" class="core.org.akaza.openclinica.bean.managestudy.StudySubjectBean" />
<jsp:useBean scope="request" id="uncompletedEventDefinitionCRFs" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="displayEventCRFs" class="java.util.ArrayList" />



<br>
<form action="UpdateStudyEvent" method="post">
    <input type="hidden" name="event_id" value="<c:out value="${studyEvent.id}"/>">
    <input type="hidden" name="ss_id" value="<c:out value="${ss_id}"/>">

    <input type="hidden" name="action" value="confirm">
    <div style="width: 250px">
   <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

    <table border="0" cellpadding="0" cellspacing="0">
        <tr valign="top"><td colspan="2">&nbsp;&nbsp;</td></tr>
        <tr valign="top"><td class="formlabel"><fmt:message key="user_name" bundle="${resword}"/>:</td>
        <td>
            <table border="0" cellpadding="0" cellspacing="0">
                <tr><td>
                        <div class="formfieldM_BG"><input type="text" name="j_user" class="formfieldM" autocomplete="off"></div>
                    </td><td>&nbsp;</td>
                </tr>
            </table>
        </td></tr>
        <tr valign="top"><td class="formlabel"><fmt:message key="password" bundle="${resword}"/></td>
        <td>
            <table border="0" cellpadding="0" cellspacing="0">
                <tr><td>
                        <div class="formfieldM_BG"><input type="password" name="j_pass"  class="formfieldM" autocomplete="off"></div>
                    </td><td>&nbsp;</td>
                </tr>
            </table>
        </td></tr>

        <tr valign="top"><td colspan="2">&nbsp;&nbsp;</td></tr>
     </table>
   </div>
   </div></div></div></div></div></div></div></div>
    <br>
    <input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_long">
    <input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>"
                class="button_medium" onClick="javascript:myCancel();"/>
</form>

<div class="table_title_Admin">
<a name="global"><a href="javascript:leftnavExpand('globalRecord');javascript:setImage('ExpandGroup5','images/bt_Collapse.gif');">
    <img name="ExpandGroup5" src="images/bt_Expand.gif" border="0">&nbsp;Study Event </a></a></div>

 <div id="globalRecord" style="display:">
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
                               &nbsp;
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
                            <td class="table_header_column_top"><fmt:message key="study_subject_ID" bundle="${resword}"/></td>
                            <td class="table_cell_top"><c:out value="${studySubject.label}"/></td>
                        </tr>
                        <tr>
                            <td class="table_header_column"><fmt:message key="SE" bundle="${resword}"/></td>
                            <td class="table_cell"><c:out value="${studyEvent.studyEventDefinition.name}"/>&nbsp;</td>
                        </tr>
                        <tr>
                            <td class="table_header_column"><fmt:message key="location" bundle="${resword}"/></td>
                            <td class="table_cell"><c:out value="${studyEvent.location}"/></td>
                        </tr>
                        <tr>
                            <td class="table_divider" colspan="2">&nbsp;</td>
                        </tr>
                        <tr>
                            <td class="table_header_column"><fmt:message key="start_date" bundle="${resword}"/></td>
                            <td class="table_cell"><fmt:formatDate value="${studyEvent.dateStarted}" pattern="${dteFormat}"/> &nbsp;</td>
                        </tr>
                        <tr>
                            <td class="table_header_column"><fmt:message key="end_date_time" bundle="${resword}"/></td>
                            <td class="table_cell"><fmt:formatDate value="${studyEvent.dateEnded}" pattern="${dteFormat}"/></td>
                        </tr>
                        <tr>
                            <td class="table_header_column"><fmt:message key="subject_event_status" bundle="${resword}"/></td>
                            <td class="table_cell"><c:out value="${studyEvent.workflowStatus.displayValue}"/></td>
                        </tr>
                        <tr>
                            <td class="table_header_column"><fmt:message key="last_updated_by" bundle="${resword}"/></td>
                            <td class="table_cell"><c:out value="${studyEvent.updater.name}"/> (<fmt:formatDate value="${studyEvent.updatedDate}" pattern="${dteFormat}"/>)</td>
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

<p><div class="table_title_submit"><fmt:message key="CRFs_in_this_study_event" bundle="${resword}"/>:</div>

<div style="width: 600px">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<!-- Table Contents -->

<table border="0" cellpadding="0" cellspacing="0" width="100%">
<c:choose>
    <c:when test="${empty uncompletedEventDefinitionCRFs && empty displayEventCRFs}">
        <tr>
            <td class="table_cell_left"><fmt:message key="there_are_no_CRF" bundle="${resword}"/></td>
        </tr>
    </c:when>

    <c:otherwise>
        <tr>
            <td class="table_header_row_left"><fmt:message key="CRF_name" bundle="${resword}"/></td>
            <td class="table_header_row"><fmt:message key="version" bundle="${resword}"/></td>
            <td class="table_header_row" style="min-width: 100px"><fmt:message key="status" bundle="${resword}"/></td>
            <td class="table_header_row"><fmt:message key="initial_data_entry" bundle="${resword}"/></td>
            <td class="table_header_row"><fmt:message key="view_discrepancy_notes" bundle="${resword}"/></td>
            <td class="table_header_row"><fmt:message key="actions" bundle="${resword}"/></td>
        </tr>
        <c:set var="rowCount" value="${0}" />

        <c:forEach var="dedc" items="${uncompletedEventDefinitionCRFs}">
            <c:choose>
            <c:when test="${dedc.status.name=='locked'}">
            &nbsp;
            </c:when>
            <c:otherwise>
            <c:set var="getQuery" value="action=ide_s&eventDefinitionCRFId=${dedc.edc.id}&studyEventId=${studyEvent.id}&subjectId=${studySubject.subjectId}&eventCRFId=${dedc.eventCRF.id}" />
                <tr valign="top">
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

                       <c:set var="getQuery" value="action=ide_s&eventDefinitionCRFId=${dedc.edc.id}&studyEventId=${studyEvent.id}&subjectId=${studySub.subjectId}" />

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
                            <c:if test="${studyEvent.locked == true}">
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

                    <c:when test="${studyEvent.subjectEventStatus.name=='locked'}">

                        &nbsp;
                    </c:when>
                  </c:choose>
                    <a href="EnketoFormServlet?formLayoutId=<c:out value="${dedc.edc.defaultVersionId}"/>&studyEventId=<c:out value="${studyEvent.id}"/>&eventCrfId=<c:out value="${dedc.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
                      onMouseDown="javascript:setImage('bt_View','images/bt_View_d.gif');"
                      onMouseUp="javascript:setImage('bt_View','images/bt_View.gif');"><span
                      name="bt_View" align="left" class="icon icon-search" border="0" alt="<fmt:message key="view_default" bundle="${resword}"/>" title="<fmt:message key="view_default" bundle="${resword}"/>" hspace="2"></a>&nbsp;
                  </tr>
                </table>

                </td>

               </tr>

                <c:set var="rowCount" value="${rowCount + 1}" />
                </c:otherwise>
            </c:choose>

        </c:forEach>
        <%-- end of for each for dedc, uncompleted event crfs --%>
        <c:forEach var="dec" items="${displayEventCRFs}" varStatus="status">
                        <c:set var="discNoteMap" value="${discNoteByEventCRFid[dec.eventCRF.id]}"/>
            <tr>
                <td class="table_cell"><c:out value="${dec.eventCRF.crf.name}" />&nbsp;</td>
                <td class="table_cell">
                  <c:out value="${dec.eventCRF.formLayout.name}" />&nbsp;
                  <c:if test="${dec.eventCRF.formLayout.status.name == 'Removed'}">
                      <span class="icon icon-archived-new status" alt="<fmt:message key="archived" bundle="${resword}"/>" title="<fmt:message key="archived" bundle="${resword}"/>"/>
                  </c:if>
                </td>
                <td class="table_cell" bgcolor="#F5F5F5" align="center" style="min-width: 100px;">
                  <table style="width: 100%;">
                      <tr style="width: inherit;">
                        <td style="width: 54%; text-align: end;">
                          <c:choose>
                            <c:when test="${ (dec.eventCRF.workflowStatus != 'NOT_STARTED' && studyEvent.isRemoved()) || dec.eventCRF.isRemoved() }">
                                <span class="icon icon-file-excel red" alt="<fmt:message key="removed" bundle="${resword}"/>" title="<fmt:message key="invalid" bundle="${resword}"/>">
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
                                <span class="icon icon-file-excel red" alt="<fmt:message key="removed" bundle="${resword}"/>" title="<fmt:message key="invalid" bundle="${resword}"/>">
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td>
                          <c:if test="${studyEvent.signed == true && !dec.eventCRF.isArchived() && !dec.eventCRF.isRemoved() && dec.eventCRF.workflowStatus == 'COMPLETED'}">
                              <span class="icon icon-stamp-new status" alt="<fmt:message key="signed" bundle="${resword}"/>" title="<fmt:message key="signed" bundle="${resword}"/>"></span>
                          </c:if>
                          <c:if test="${studyEvent.locked == true}">
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
                        <c:set var="actionQuery" value="EnketoFormServlet?formLayoutId=${dec.eventCRF.formLayout.id}&studyEventId=${studyEvent.id}&eventCrfId=${dec.eventCRF.id}&originatingPage=${originatingPage}&mode=edit"/>
                    </c:if>

                    <c:if test="${dec.startDoubleDataEntryPermitted}">
                        <c:set var="actionQuery" value="EnketoFormServlet?formLayoutId=${dec.eventCRF.formLayout.id}&studyEventId=${studyEvent.id}&eventCrfId=${dec.eventCRF.id}&originatingPage=${originatingPage}&mode=edit"/>
                    </c:if>
                    <c:if test="${dec.continueDoubleDataEntryPermitted}">
                        <c:set var="actionQuery" value="EnketoFormServlet?formLayoutId=${dec.eventCRF.formLayout.id}&studyEventId=${studyEvent.id}&eventCrfId=${dec.eventCRF.id}&originatingPage=${originatingPage}&mode=edit"/>
                    </c:if>

                    <c:if test="${dec.performAdministrativeEditingPermitted}">
                        <c:set var="actionQuery" value="EnketoFormServlet?formLayoutId=${dec.eventCRF.formLayout.id}&studyEventId=${studyEvent.id}&eventCrfId=${dec.eventCRF.id}&originatingPage=${originatingPage}&mode=edit"/>
                    </c:if>

<%--
                    <c:if test="${dec.locked}">
                        locked
                    </c:if>

--%>

                    <c:choose>
                        <c:when test='${actionQuery == "" && dec.stage.name =="invalid" }'>
                                   <a href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
                                onMouseDown="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
                                onMouseUp="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
                                ><span name="bt_View<c:out value="${rowCount}"/>" class="icon icon-search" border="0" alt="<fmt:message key="view_data" bundle="${resword}"/>" title="<fmt:message key="view_data" bundle="${resword}"/>" align="left" hspace="2"></a>&nbsp;
                                &nbsp;
                        </c:when>

                        <c:when test='${actionQuery == ""}'>
                                   <a href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
                                onMouseDown="javascript:setImage('bt_View','images/bt_View_d.gif');"
                                onMouseUp="javascript:setImage('bt_View','images/bt_View.gif');"
                                ><span name="bt_View" class="icon icon-search" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="2"></a>
<!--
                            <a href="javascript:openDocWindow('PrintDataEntry?ecId=<c:out value="${dec.eventCRF.id}"/>')"
                            -->
                                &nbsp;
                            <%-- added above 112007, tbh --%>
                        </c:when>
                        <c:otherwise>
                          <a href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
                                onMouseDown="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View_d.gif');"
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
    </c:otherwise>
</c:choose>
</table>

<!-- End Table Contents -->

</div>
</div></div></div></div></div></div></div></div>
</div>




<jsp:include page="../include/footer.jsp"/>
