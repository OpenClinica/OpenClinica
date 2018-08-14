<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean scope='request' id='eventId' class='java.lang.String'/>
<c:set var="eventId" value="${eventId}"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:include page="../include/submit-header.jsp"/>
<link rel="stylesheet" href="includes/font-awesome-4.7.0/css/font-awesome.css">
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/permissionTagAccess.js"></script>
<script type="text/javascript" language="javascript">
    function checkCRFLocked(ecId, url){
        jQuery.post(" return checkCRFLocked?ecId="+ ecId + "&ran="+Math.random(), function(data){
            if(data == 'true'){
                window.location = url;
            }else{
                alert(data);
            }
        });
    }
    function checkCRFLockedInitial(ecId, formName){
        if(ecId==0) {formName.submit(); return;}
        jQuery.post(" return checkCRFLocked?ecId="+ ecId + "&ran="+Math.random(), function(data){
            if(data == 'true'){
                formName.submit();
            }else{
                alert(data);
            }
        });
    }
</script>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></span></a>

        <fmt:message key="instructions" bundle="${resword}"/>

        <div class="sidebar_tab_content">

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></span></a>

        <fmt:message key="instructions" bundle="${resword}"/>

    </td>
</tr>
<jsp:include page="../include/eventOverviewSideInfo.jsp"/>

<jsp:useBean scope="request" id="studyEvent" class="org.akaza.openclinica.bean.managestudy.StudyEventBean" />
<jsp:useBean scope="request" id="studySubject" class="org.akaza.openclinica.bean.managestudy.StudySubjectBean" />
<jsp:useBean scope="request" id="uncompletedEventDefinitionCRFs" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="displayEventCRFs" class="java.util.ArrayList" />

<h1><span class="title_manage"><fmt:message key="enter_or_validate_data" bundle="${resword}"/><c:out value="${studyEvent.studyEventDefinition.name}" /></span></h1><br/>


&nbsp;
<a name="global"><a href="javascript:leftnavExpand('globalRecord');javascript:setImage('ExpandGroup5','images/bt_Collapse.gif');"><img
  name="ExpandGroup5" height="20px" src="images/bt_Expand.gif" border="0"></a></a></div>

<div id="globalRecord">
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
                            <c:if test="${studySubject.status.name != 'removed' && studySubject.status.name != 'auto-removed' && study.status.available && studyEvent.editable && !userRole.monitor}">
                                <a href="UpdateStudyEvent?event_id=<c:out value="${studyEvent.id}"/>&ss_id=<c:out value="${studySubject.id}"/>"></a>
                                &nbsp;
                                <a href="UpdateStudyEvent?event_id=<c:out value="${studyEvent.id}"/>&ss_id=<c:out value="${studySubject.id}"/>"><fmt:message key="edit_study_event" bundle="${resword}"/></a>
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
                        <td class="table_header_column_top"><fmt:message key="study_subject_ID" bundle="${resword}"/></td>
                        <td class="table_cell_top"><c:out value="${studySubject.label}"/></td>
                    </tr>
                    <tr>
                        <td class="table_header_column"><fmt:message key="SE" bundle="${resword}"/></td>
                        <td class="table_cell"><c:out value="${studyEvent.studyEventDefinition.name}"/>&nbsp;</td>
                    </tr>
                    <tr>
                        <td class="table_header_column"><fmt:message key="study_subject_oid" bundle="${resword}"/></td>
                        <td class="table_cell"><c:out value="${studySubject.oid}"/></td>
                    </tr>
                    <tr>
                        <td class="table_divider" colspan="2">&nbsp;</td>
                    </tr>
                    <tr>
                        <td class="table_header_column"><fmt:message key="start_date" bundle="${resword}"/></td>
                        <td class="table_cell"><span style="float:left"><fmt:formatDate value="${studyEvent.dateStarted}" pattern="${dteFormat}"/></span>
                         <c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}">
                                <c:set var="isNew" value="${hasStartDateNote eq 'yes' ? 0 : 1}"/>
                                <c:choose>
                                    <c:when test="${hasStartDateNote eq 'yes'}">
                                     <span style="float:right"><a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&id=${studyEvent.id}&subjectId=${studySubject.id}&name=studyEvent&field=start_date&column=start_date&strErrMsg','spanAlert-start_date'); return false;">
                                     <span id="flag_start_date" name="flag_start_date" class="${startDateNote.resStatus.iconFilePath}" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" ></span>
                                     </a>
                                     </span>
                                    </c:when>
                                    <c:otherwise>
                                       <c:if test="${!study.status.locked}">
                                        <span style="float:right">
                                        <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?writeToDB=1&id=${studyEvent.id}&subjectId=${studySubject.id}&name=studyEvent&field=start_date&column=start_date&strErrMsg=','spanAlert-start_date'); return false;">
                                            <span id="flag_start_date" name="flag_start_date" class="fa fa-bubble-white" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" ></span>
                                        </a></span>
                                       </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </td>
                    </tr>
                    <tr>
                        <td class="table_header_column"><fmt:message key="end_date_time" bundle="${resword}"/></td>
                        <td class="table_cell"><span style="float:left"><fmt:formatDate value="${studyEvent.dateEnded}" pattern="${dteFormat}"/></span>

                         <c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}">
                                <c:set var="isNew" value="${hasEndDateNote eq 'yes' ? 0 : 1}"/>
                                <c:choose>
                                    <c:when test="${hasEndDateNote eq 'yes'}">
                                     <span style="float:right"><a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&id=${studyEvent.id}&subjectId=${studySubject.id}&name=studyEvent&field=end_date&column=end_date&strErrMsg','spanAlert-end_date'); return false;">
                                     <span id="flag_end_date" name="flag_end_date" class="${endDateNote.resStatus.iconFilePath}" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" ></span>
                                     </a>
                                     </span>
                                    </c:when>
                                    <c:otherwise>
                                      <c:if test="${!study.status.locked}">
                                        <span style="float:right">
                                        <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?writeToDB=1&id=${studyEvent.id}&subjectId=${studySubject.id}&name=studyEvent&field=end_date&column=end_date&strErrMsg=','spanAlert-end_date'); return false;">
                                        <span id="flag_end_date" name="flag_end_date" class="fa fa-bubble-white" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" ></span>
                                        </a></span>
                                      </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </td>
                    </tr>
                    <tr>
                        <td class="table_header_column"><fmt:message key="subject_event_status" bundle="${resword}"/></td>
                        <td class="table_cell"><c:out value="${studyEvent.subjectEventStatus.name}"/></td>
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

<div style="width: 700px">
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
    <td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="last_modified_by" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="actions" bundle="${resword}"/></td>
</tr>
<c:set var="rowCount" value="${0}" />

<c:forEach var="dedc" items="${uncompletedEventDefinitionCRFs}">
<c:choose>
<c:when test="${dedc.status.name=='locked'}">
    <%-- nothing for right now --%>&nbsp;
</c:when>
<c:otherwise>
<c:set var="getQuery" value="action=ide_s&eventDefinitionCRFId=${dedc.edc.id}&studyEventId=${studyEvent.id}&subjectId=${studySubject.subjectId}&eventCRFId=${dedc.eventCRF.id}&exitTo=EnterDataForStudyEvent?eventId=${eventId}" />
<tr valign="top">
<td class="table_cell_left"><c:out value="${dedc.edc.crf.name}" /></td>

<td class="table_cell">
    <form name="startForm<c:out value="${dedc.edc.crf.id}"/>" action="InitialDataEntry?<c:out value="${getQuery}"/>" method="POST">
    <c:set var="defaultVersionOID"/>
    <c:set var="cvOID"/>
        <c:choose>
        <c:when test="${dedc.eventCRF.id > 0}">
        <!-- found an event crf id -->
        <input type="hidden" name="formLayoutId" value="<c:out value="${dedc.eventCRF.formLayout.id}"/>">
        </c:when>
        <c:otherwise>
        <!-- did not find an event crf id -->
        <input type="hidden" name="formLayoutId" value="<c:out value="${dedc.edc.defaultVersionId}"/>">
        </c:otherwise>
        </c:choose>


            <c:set var="versionCount" value="0"/>
        <c:forEach var="version" items="${dedc.edc.versions}">
            <c:set var="versionCount" value="${versionCount+1}"/>
        </c:forEach>

        <c:choose>

        <c:when test="${versionCount<=1}">

        <c:forEach var="version" items="${dedc.edc.versions}">

           <c:out value="${version.name}"/>
           <c:set var="formLayoutOID" value="${version.oid}"/>

        </c:forEach>

        </c:when>
        <c:when test="${dedc.eventCRF.id == 0}">

        <c:set var= "cvOID" value="${defaultVersionOID}"/>
        <select name="versionId<c:out value="${dedc.edc.crf.id}"/>" onchange="javascript:changeQuery<c:out value="${dedc.edc.crf.id}"/>();">

            <c:forEach var="version" items="${dedc.edc.versions}">

                <c:set var="getQuery" value="action=ide_s&eventDefinitionCRFId=${dedc.edc.id}&studyEventId=${currRow.bean.studyEvent.id}&subjectId=${studySub.subjectId}" />

                <c:choose>
                    <c:when test="${dedc.edc.defaultVersionId==version.id}">
                        <option value="<c:out value="${version.id}"/>" selected>
                            <c:out value="${version.name}"/>
                               <c:set var="formLayoutOID" value="${version.oid}"/>
                               <c:set var="formLayoutId" value="${version.id}"/>
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
                document.startForm<c:out value="${dedc.edc.crf.id}"/>.formLayoutId.value=qer;
                document.getElementById('ide1-<c:out value="${studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>').href =
                    buildUrl(qer,'<c:out value="${studyEvent.id}"/>','<c:out value="${dedc.eventCRF.id}"/>','<c:out value="${originatingPage}"/>','<c:out value="edit"/>'  );
                        document.getElementById('ide2-<c:out value="${studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>').href =
                    buildUrl(qer,'<c:out value="${studyEvent.id}"/>','<c:out value="${dedc.eventCRF.id}"/>','<c:out value="${originatingPage}"/>','<c:out value="view"/>' );
            }
                function buildUrl(formLayoutId, studyEventId, eventCRFStatusId, originatingPage , mode){
                     return "EnketoFormServlet?formLayoutId="+ formLayoutId +
                             "&studyEventId=" + studyEventId +
                             "&eventCrfId=" + eventCRFStatusId +
                             "&originatingPage=" + originatingPage+
                             "&mode=" + mode;
                 }

            </SCRIPT>


        </c:when>

        <c:otherwise>
        <c:out value="${dedc.eventCRF.formLayout.name}"/>
            <c:set var="formLayoutOID" value="${dedc.eventCRF.formLayout.oid}"/>
        </c:otherwise>

        </c:choose>

</td>

<c:choose>

    <c:when test="${studyEvent.subjectEventStatus.name=='locked'}">
        <%--<c:when test="${dedc.status.name=='locked'}">--%>
        <td class="table_cell" bgcolor="#F5F5F5" align="center">
            <span class="icon icon-lock" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>"></span>
        </td>
    </c:when>

    <c:when test="${studySubject.status.name != 'removed'&& studySubject.status.name != 'auto-removed'}">
        <c:choose>
            <c:when test="${dedc.eventCRF.id>0}">
                <td class="table_cell" bgcolor="#F5F5F5" align="center"><span class="icon icon-pencil-squared orange" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>" title="<fmt:message key="initial_data_entry" bundle="${resword}"/>"></span></td>
            </c:when>
            <c:otherwise>
                <td class="table_cell" bgcolor="#F5F5F5" align="center"><span class="icon icon-doc" alt="<fmt:message key="not_started" bundle="${resword}"/>" title="<fmt:message key="not_started" bundle="${resword}"/>"></span></td>
            </c:otherwise>
        </c:choose>
    </c:when>

    <c:otherwise>
        <td class="table_cell" bgcolor="#F5F5F5" align="center"><span class="icon icon-file-excel red" alt="<fmt:message key="invalid" bundle="${resword}"/>" title="<fmt:message key="invalid" bundle="${resword}"/>"></span></td>
    </c:otherwise>

</c:choose>

<td class="table_cell">
    <c:if test="${dedc.eventCRF != null && dedc.eventCRF.owner != null}">
        ${dedc.eventCRF.owner.name}
    </c:if>
    &nbsp;</td>



<td class="table_cell">
    <table >
        <tr align="left">
            <c:choose>

                <c:when test="${studyEvent.subjectEventStatus.name=='locked'}">
                    &nbsp;
                </c:when>

                <c:when test="${studySubject.status.name != 'removed'&& studySubject.status.name != 'auto-removed' && study.status.available && !studyEvent.status.deleted && !studyEvent.subjectEventStatus.locked &&!userRole.monitor}">
                    <td >
                        <c:choose>
                        <c:when test="${dedc.eventCRF.status.id != 0}">
                            <a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dedc.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${studyEvent.id}"/>&eventCrfId=<c:out value="${dedc.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="edit"/>"
                               onMouseDown="javascript:setImage('bt_EnterData<c:out value="${rowCount}"/>','images/bt_EnterData_d.gif');"
                               onMouseUp="javascript:setImage('bt_EnterData<c:out value="${rowCount}"/>','icon icon-pencil-squared');"
                               onclick="return checkCRFLocked('<c:out value="${currentPublicStudy.schemaName}"/><c:out value="${studyEvent.id}"/><c:out value="${dedc.eventCRF.formLayout.id}"/>', '<c:out value="${actionQuery}"/>');">
                            <span name="bt_EnterData<c:out value="${rowCount}"/>" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="enter_data" bundle="${resword}"/>" title="<fmt:message key="enter_data" bundle="${resword}"/>" align="left" hspace="2"></span></a>&nbsp;
                            </a>
                        </c:when>
                        <c:otherwise>
                              <a id="ide1-<c:out value="${studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>" class="accessCheck"
                               href="EnketoFormServlet?formLayoutId=<c:out value="${dedc.edc.defaultVersionId}"/>&studyEventId=<c:out value="${studyEvent.id}"/>&eventCrfId=<c:out value="${dedc.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="edit"/>"
                               onMouseDown="javascript:setImage('bt_EnterData<c:out value="${rowCount}"/>','images/bt_EnterData_d.gif');"
                               onMouseUp="javascript:setImage('bt_EnterData<c:out value="${rowCount}"/>','icon icon-pencil-squared');"
                               onclick="return checkCRFLocked('<c:out value="${publicStudy.schemaName}"/><c:out value="${studyEvent.id}"/><c:out value="${dedc.edc.defaultVersionId}"/>', '<c:out value="${actionQuery}"/>');">
                              <span name="bt_EnterData<c:out value="${rowCount}"/>" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="enter_data" bundle="${resword}"/>" title="<fmt:message key="enter_data" bundle="${resword}"/>" align="left" hspace="2"></span></a>&nbsp;
                            </a>
                        </c:otherwise>
                        </c:choose>
                    </td>
                </c:when>

                <c:otherwise></c:otherwise>
            </c:choose>
            <td >
                       <a id="ide2-<c:out value="${studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>" class="accessCheck"
                   href="EnketoFormServlet?formLayoutId=<c:out value="${dedc.edc.defaultVersionId}"/>&studyEventId=<c:out value="${studyEvent.id}"/>&eventCrfId=<c:out value="${dedc.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
                   onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
                   onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"
                   onclick="return checkCRFLocked('<c:out value="${publicStudy.schemaName}"/><c:out value="${studyEvent.id}"/><c:out value="${dedc.edc.defaultVersionId}"/>', '<c:out value="${actionQuery}"/>');">
<span
                  name="bt_View1" align="left" class="icon icon-search" border="0" alt="<fmt:message key="view_default" bundle="${resword}"/>" title="<fmt:message key="view_default" bundle="${resword}"/>" hspace="2"></span></a>&nbsp;
            </td>
        </tr>
    </table>
    </form>
</td>

</tr>

<c:set var="rowCount" value="${rowCount + 1}" />
</c:otherwise>
</c:choose>

</c:forEach>
<!-- end of for each for dedc, uncompleted event crfs, started CRFs below -->
<c:forEach var="dec" items="${displayEventCRFs}" varStatus="status">
<tr>
<td class="table_cell"><c:out value="${dec.eventCRF.crf.name}" />&nbsp;</td>
<td class="table_cell"><c:out value="${dec.eventCRF.formLayout.name}" />&nbsp;</td>
<td class="table_cell" bgcolor="#F5F5F5" align="center">

    <c:choose>
        <c:when test="${dec.stage.initialDE}">
            <span class="icon icon-pencil-squared orange" alt="<fmt:message key="data_entry_started" bundle="${resword}"/>" title="<fmt:message key="data_entry_started" bundle="${resword}"/>"></span>
        </c:when>
        <c:when test="${dec.stage.initialDE_Complete}">
            <span class="icon icon-pencil-squared orange" alt="<fmt:message key="initial_data_entry_complete" bundle="${resword}"/>" title="<fmt:message key="initial_data_entry_complete" bundle="${resword}"/>"></span>
        </c:when>
        <c:when test="${dec.stage.doubleDE}">
            <span class="icon icon-icon-doubleDataEntry orange" alt="<fmt:message key="double_data_entry" bundle="${resword}"/>" title="<fmt:message key="double_data_entry" bundle="${resword}"/>"></span>
        </c:when>
        <c:when test="${dec.stage.doubleDE_Complete}">
            <span class="icon icon-checkbox-checked green" alt="<fmt:message key="data_entry_complete" bundle="${resword}"/>" title="<fmt:message key="data_entry_complete" bundle="${resword}"/>"></span>
        </c:when>

        <c:when test="${dec.stage.admin_Editing}">
            <span class="icon icon-pencil" alt="<fmt:message key="administrative_editing" bundle="${resword}"/>" title="<fmt:message key="administrative_editing" bundle="${resword}"/>"></span>
        </c:when>

        <c:when test="${dec.stage.locked}">
            <span class="icon icon-lock" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>"></span>
        </c:when>

        <c:otherwise>
            <span class="icon icon-file-excel red" alt="<fmt:message key="invalid" bundle="${resword}"/>" title="<fmt:message key="invalid" bundle="${resword}"/>"></span>
        </c:otherwise>
    </c:choose>
</td>
<td class="table_cell"><c:out value="${dec.eventCRF.owner.name}" />&nbsp;</td>

<td class="table_cell" style="width:180px;">
    <c:set var="actionQuery" value="" />
    <c:if test="${study.status.available}">
        <c:if test="${dec.continueInitialDataEntryPermitted}">
            <c:set var="actionQuery" value="InitialDataEntry?eventCRFId=${dec.eventCRF.id}" />
        </c:if>

        <c:if test="${dec.startDoubleDataEntryPermitted}">
            <c:set var="actionQuery" value="DoubleDataEntry?eventCRFId=${dec.eventCRF.id}" />
        </c:if>

        <c:if test="${dec.continueDoubleDataEntryPermitted}">
            <c:set var="actionQuery" value="DoubleDataEntry?eventCRFId=${dec.eventCRF.id}" />
        </c:if>

        <c:if test="${dec.performAdministrativeEditingPermitted}">
            <c:set var="actionQuery" value="AdministrativeEditing?eventCRFId=${dec.eventCRF.id}" />
        </c:if>
    </c:if>


<table><tr align="left">
    <c:choose>
        <c:when test='${actionQuery == "" && dec.stage.name =="invalid" }'>
           <td><a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
               onMouseDown="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
               onMouseUp="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
               onclick="return checkCRFLocked('<c:out value="${dec.eventCRF.id}"/>', '<c:out value="${actionQuery}"/>');">
           <span name="bt_View<c:out value="${rowCount}"/>" class="icon icon-search" border="0" alt="<fmt:message key="view_data" bundle="${resword}"/>" title="<fmt:message key="view_data" bundle="${resword}"/>" align="left" hspace="2"></span></a>
</td>
			<c:if test="${(!userRole.monitor && dec.eventCRF.status.name != 'auto-removed') && (study.status.available) && (studySubject.status.available)}">
          <td>      <a class="accessCheck" href="RestoreEventCRF?action=confirm&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&studySubId=<c:out value="${studySubject.id}"/>"
                   onMouseDown="javascript:setImage('bt_Restore<c:out value="${rowCount}"/>','images/bt_Restore.gif');"
                   onMouseUp="javascript:setImage('bt_Restore<c:out value="${rowCount}"/>','images/bt_Restore.gif');"
                  ><span name="bt_Restore<c:out value="${rowCount}"/>" class="icon icon-ccw" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>"  hspace="2"></span></a>
           </td>
                           <td> <a class="accessCheck" href="DeleteEventCRF?action=confirm&ssId=<c:out value="${studySubject.id}"/>&ecId=<c:out value="${dec.eventCRF.id}"/>"
                               onMouseDown="javascript:setImage('bt_Delete<c:out value="${rowCount}"/>','images/bt_Delete.gif');"
                               onMouseUp="javascript:setImage('bt_Delete<c:out value="${rowCount}"/>','images/bt_Delete.gif');"
                              ><span name="bt_Remove<c:out value="${rowCount}"/>" class="icon icon-trash red" border="0" alt="<fmt:message key="delete" bundle="${resword}"/>" title="<fmt:message key="delete" bundle="${resword}"/>"  hspace="2"></span></a>
                              </td>

            </c:if>

        </c:when>

        <c:when test='${actionQuery == ""}'>
           <td><a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
               onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
               onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"
               onclick="return checkCRFLocked('<c:out value="${dec.eventCRF.id}"/>', '<c:out value="${actionQuery}"/>');">
               <span name="bt_View1" class="icon icon-search" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="2"></span></a>
</td>

        </c:when>
        <c:otherwise>
            <c:if test="${studySubject.status.name != 'removed'&& studySubject.status.name != 'auto-removed' && !userRole.monitor}">
                <c:if test="${dec.continueInitialDataEntryPermitted}">
                <td><a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="edit"/>"
                    onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
                    onMouseUp="javascript:setImage('bt_EnterData1','icon icon-pencil-squared');"
                    onclick="return checkCRFLocked'<c:out value="${dec.eventCRF.id}"/>', '<c:out value="${actionQuery}"/>');">
                    <span name="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="continue_entering_data" bundle="${resword}"/>" title="<fmt:message key="continue_entering_data" bundle="${resword}"/>" align="left" hspace="6"></span></a-->
                   </a>
               </td> </c:if>
                <c:if test="${dec.startDoubleDataEntryPermitted}">
                 <td><a href="#"
                    onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
                    onMouseUp="javascript:setImage('bt_EnterData1','icon icon-pencil-squared');"
                    onclick="return checkCRFLocked'<c:out value="${dec.eventCRF.id}"/>', '<c:out value="${actionQuery}"/>');">
                    <span class="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="begin_double_data_entry" bundle="${resword}"/>" title="<fmt:message key="begin_double_data_entry" bundle="${resword}"/>" align="left" hspace="6"></span></a>
                </td></c:if>
                <c:if test="${dec.continueDoubleDataEntryPermitted}">
                  <td><a href="#"
                    onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
                    onMouseUp="javascript:setImage('bt_EnterData1','icon icon-pencil-squared');"
                    onclick="return checkCRFLocked'<c:out value="${dec.eventCRF.id}"/>', '<c:out value="${actionQuery}"/>');">
                    <span class="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="continue_entering_data" bundle="${resword}"/>" title="<fmt:message key="continue_entering_data" bundle="${resword}"/>" align="left" hspace="6"></span></a>
               </td> </c:if>
                <c:if test="${dec.performAdministrativeEditingPermitted}">
                <td><a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="edit"/>"
                    onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
                    onMouseUp="javascript:setImage('bt_EnterData1','icon icon-pencil-squared');"
                    onclick="return checkCRFLocked'<c:out value="${dec.eventCRF.id}"/>', '<c:out value="${actionQuery}"/>');">
                    <span name="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="administrative_editing" bundle="${resword}"/>" title="<fmt:message key="administrative_editing" bundle="${resword}"/>" align="left" hspace="6"></span>
                    </a></td>
                </c:if>
            </c:if>


                <td><a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
               onMouseDown="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
               onMouseUp="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
               onclick="return checkCRFLocked'<c:out value="${dec.eventCRF.id}"/>', '<c:out value="${actionQuery}"/>');">
                    <span name="bt_View<c:out value="${rowCount}"/>" class="icon icon-search" border="0" alt="<fmt:message key="view_data" bundle="${resword}"/>" title="<fmt:message key="view_data" bundle="${resword}"/>"  hspace="2"></span></a>
              </td>

            <c:if test="${study.status.available && !userRole.monitor}">
               <td> <a class="accessCheck" href="RemoveEventCRF?action=confirm&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&studySubId=<c:out value="${studySubject.id}"/>"
                   onMouseDown="javascript:setImage('bt_Remove<c:out value="${rowCount}"/>','images/bt_Remove.gif');"
                   onMouseUp="javascript:setImage('bt_Remove<c:out value="${rowCount}"/>','images/bt_Remove.gif');"
                  ><span name="bt_Remove<c:out value="${rowCount}"/>" class="icon icon-cancel" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>"  hspace="2"></span></a>
                  </td>
               <td> <a class="accessCheck" href="DeleteEventCRF?action=confirm&ssId=<c:out value="${studySubject.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>"
                   onMouseDown="javascript:setImage('bt_Delete<c:out value="${rowCount}"/>','images/bt_Delete.gif');"
                   onMouseUp="javascript:setImage('bt_Delete<c:out value="${rowCount}"/>','images/bt_Delete.gif');"
                  ><span name="bt_Remove<c:out value="${rowCount}"/>" class="icon icon-trash red" border="0" alt="<fmt:message key="delete" bundle="${resword}"/>" title="<fmt:message key="delete" bundle="${resword}"/>"  hspace="2"></span></a>
                  </td>
            </c:if>

               <!--  reasign crf version -->


 <c:if test="${( userRole.director || userRole.coordinator) &&
 (study.status.available )
 && !(studyEvent.subjectEventStatus.locked || studyEvent.subjectEventStatus.skipped)}">

  <td>  <a class="accessCheck" href="pages/managestudy/chooseCRFVersion?crfId=<c:out value="${dec.eventCRF.crf.id}" />&crfName=<c:out value="${dec.eventCRF.crf.name}" />&formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}" />&formLayoutName=<c:out value="${dec.eventCRF.formLayout.name}" />&studySubjectLabel=<c:out value="${studySubject.label}"/>&studySubjectId=<c:out value="${studySubject.id}"/>&eventCRFId=<c:out value="${dec.eventCRF.id}"/>&eventDefinitionCRFId=<c:out value="${dec.eventDefinitionCRF.id}"/>"
   onMouseDown="javascript:setImage('bt_Reassign','images/bt_Reassign_d.gif');"
   onMouseUp="javascript:setImage('bt_Reassign','images/bt_Reassign.gif');"><span
      name="Reassign" class="icon icon-icon-reassign3" border="0" alt="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" title="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" align="left" hspace="6"></span></a>
   </td>
   </c:if>
            <c:if test="${doRuleSetsExist[status.index]}" >
             <td>   <a href="ExecuteCrossEditCheck?eventCrfId=<c:out value='${dec.eventCRF.id}'/>">execute Rule</a></td>
            </c:if>
        </c:otherwise>
    </c:choose>
    </tr></table>
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

<form method="POST" action="ViewStudySubject">
    <input type="hidden" name="id" value="<c:out value="${studySubject.id}"/>" />
    <input type="submit" name="Submit" value="<fmt:message key="view_this_subject_record" bundle="${resword}"/>" class="button_xlong">
    <input type="button" onclick="confirmExit('ListStudySubjects');"  name="exit" value="<fmt:message key="exit" bundle="${resword}"/>   " class="button_medium"/>
</form>


<c:import url="instructionsEnterData.jsp">
    <c:param name="currStep" value="eventOverview" />
</c:import>

<jsp:include page="../include/footer.jsp"/>
