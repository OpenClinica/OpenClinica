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

<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script>
<script type="text/javascript" language="javascript">
    function checkCRFLocked(ecId, url){
        jQuery.post("CheckCRFLocked?ecId="+ ecId + "&ran="+Math.random(), function(data){
            if(data == 'true'){
                window.location = url;
            }else{
                alert(data);
            }
        });
    }
    function checkCRFLockedInitial(ecId, formName){
        if(ecId==0) {formName.submit(); return;}
        jQuery.post("CheckCRFLocked?ecId="+ ecId + "&ran="+Math.random(), function(data){
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
<jsp:include page="../include/eventOverviewSideInfo.jsp"/>

<jsp:useBean scope="request" id="studyEvent" class="org.akaza.openclinica.bean.managestudy.StudyEventBean" />
<jsp:useBean scope="request" id="studySubject" class="org.akaza.openclinica.bean.managestudy.StudySubjectBean" />
<jsp:useBean scope="request" id="uncompletedEventDefinitionCRFs" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="displayEventCRFs" class="java.util.ArrayList" />

<h1><span class="title_manage"><fmt:message key="enter_or_validate_data" bundle="${resword}"/><c:out value="${studyEvent.studyEventDefinition.name}" /></span></h1>



<a name="global"><a href="javascript:leftnavExpand('globalRecord');javascript:setImage('ExpandGroup5','images/bt_Collapse.gif');"><img
  name="ExpandGroup5" src="images/bt_Expand.gif" border="0"></a></a></div>

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
                            <c:if test="${studySubject.status.name != 'removed' && studySubject.status.name != 'auto-removed' && study.status.available && studyEvent.editable}">
                                <a href="UpdateStudyEvent?event_id=<c:out value="${studyEvent.id}"/>&ss_id=<c:out value="${studySubject.id}"/>"><img src="images/bt_Edit.gif" border="0" align="left"></a>
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
                        <td class="table_header_column"><fmt:message key="location" bundle="${resword}"/></td>
                        <td class="table_cell">
                            <c:set var="eventLocation" value="${studyEvent.location}"/>
                            <c:if test="${studyEvent.location eq ''}">
                                <c:set var="eventLocation" value="N/A"/>
                            </c:if>
                            <span style="float:left">
                                <c:out value="${eventLocation}"/>
                            </span>
                            <c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}">
                                <c:set var="isNew" value="${hasLocationNote eq 'yes' ? 0 : 1}"/>
                                <c:choose>
                                    <c:when test="${hasLocationNote eq 'yes'}">
                                     <span style="float:right"><a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&id=${studyEvent.id}&subjectId=${studySubject.id}&name=studyEvent&field=location&column=location&strErrMsg','spanAlert-location'); return false;">
                                     <img id="flag_location" name="flag_location" src="images/icon_Note.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                                     </a>
                                     </span>
                                    </c:when>
                                    <c:otherwise>
                                       <c:if test="${!study.status.locked}">
                                        <span style="float:right">
                                        <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?writeToDB=1&id=${studyEvent.id}&subjectId=${studySubject.id}&name=studyEvent&field=location&column=location&strErrMsg=','spanAlert-location'); return false;">
                                        <img id="flag_location" name="flag_location" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                                        </a></span>
                                       </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </c:if></td>
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
                                     <img id="flag_start_date" name="flag_start_date" src="images/icon_Note.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                                     </a>
                                     </span>
                                    </c:when>
                                    <c:otherwise>
                                       <c:if test="${!study.status.locked}">
                                        <span style="float:right">
                                        <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?writeToDB=1&id=${studyEvent.id}&subjectId=${studySubject.id}&name=studyEvent&field=start_date&column=start_date&strErrMsg=','spanAlert-start_date'); return false;">
                                            <img id="flag_start_date" name="flag_start_date" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
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
                                     <img id="flag_end_date" name="flag_end_date" src="images/icon_Note.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                                     </a>
                                     </span>
                                    </c:when>
                                    <c:otherwise>
                                      <c:if test="${!study.status.locked}">
                                        <span style="float:right">
                                        <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?writeToDB=1&id=${studyEvent.id}&subjectId=${studySubject.id}&name=studyEvent&field=end_date&column=end_date&strErrMsg=','spanAlert-end_date'); return false;">
                                        <img id="flag_end_date" name="flag_end_date" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
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
    <td class="table_header_row"><fmt:message key="initial_data_entry" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="validation" bundle="${resword}"/></td>
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
        <input type="hidden" name="crfVersionId" value="<c:out value="${dedc.eventCRF.CRFVersionId}"/>">
        </c:when>
        <c:otherwise>
        <!-- did not find an event crf id -->
        <input type="hidden" name="crfVersionId" value="<c:out value="${dedc.edc.defaultVersionId}"/>">
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
           <c:set var="crfVersionOID" value="${version.oid}"/>
          
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
                               <c:set var="crfVersionOID" value="${version.oid}"/>
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

        </c:when>

        <c:otherwise>
        <c:out value="${dedc.eventCRF.crfVersion.name}"/>
            <c:set var="crfVersionOID" value="${dedc.eventCRF.crfVersion.oid}"/>
        </c:otherwise>

        </c:choose>

</td>

<c:choose>

    <c:when test="${studyEvent.subjectEventStatus.name=='locked'}">
        <%--<c:when test="${dedc.status.name=='locked'}">--%>
        <td class="table_cell" bgcolor="#F5F5F5" align="center">
            <img src="images/icon_Locked.gif" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>">
        </td>
    </c:when>

    <c:when test="${studySubject.status.name != 'removed'&& studySubject.status.name != 'auto-removed'}">
        <c:choose>
            <c:when test="${dedc.eventCRF.id>0}">
                <td class="table_cell" bgcolor="#F5F5F5" align="center"><img src="images/icon_InitialDE.gif" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>" title="<fmt:message key="initial_data_entry" bundle="${resword}"/>"></td>
            </c:when>
            <c:otherwise>
                <td class="table_cell" bgcolor="#F5F5F5" align="center"><img src="images/icon_NotStarted.gif" alt="<fmt:message key="not_started" bundle="${resword}"/>" title="<fmt:message key="not_started" bundle="${resword}"/>"></td>
            </c:otherwise>
        </c:choose>
    </c:when>

    <c:otherwise>
        <td class="table_cell" bgcolor="#F5F5F5" align="center"><img src="images/icon_Invalid.gif" alt="<fmt:message key="invalid" bundle="${resword}"/>" title="<fmt:message key="invalid" bundle="${resword}"/>"></td>
    </c:otherwise>

</c:choose>

<td class="table_cell">
    <c:if test="${dedc.eventCRF != null && dedc.eventCRF.owner != null}">
        ${dedc.eventCRF.owner.name}
    </c:if>
    &nbsp;</td>

<td class="table_cell">&nbsp;</td>

<td class="table_cell">
    <table >
        <tr align="left">
            <c:choose>

                <c:when test="${studyEvent.subjectEventStatus.name=='locked'}">
                    &nbsp;
                </c:when>

                <c:when test="${studySubject.status.name != 'removed'&& studySubject.status.name != 'auto-removed' && study.status.available && !studyEvent.status.deleted && !userRole.monitor}">
                    <td >
                        <a href="#" onclick="checkCRFLockedInitial('<c:out value="${dedc.eventCRF.id}"/>',document.startForm<c:out value="${dedc.edc.crf.id}"/>);"
                           onMouseDown="javascript:setImage('bt_EnterData<c:out value="${rowCount}"/>','images/bt_EnterData_d.gif');"
                           onMouseUp="javascript:setImage('bt_EnterData<c:out value="${rowCount}"/>','images/bt_EnterData.gif');">
                           <img name="bt_EnterData<c:out value="${rowCount}"/>" src="images/bt_EnterData.gif" border="0" alt="<fmt:message key="enter_data" bundle="${resword}"/>" title="<fmt:message key="enter_data" bundle="${resword}"/>" align="left" hspace="2"></a>&nbsp;
                    </td>
                </c:when>

                <c:otherwise></c:otherwise>
            </c:choose>
            <td >
                <a href="ViewSectionDataEntry?eventDefinitionCRFId=<c:out value="${dedc.edc.id}"/>&crfVersionId=<c:out value="${dedc.edc.defaultVersionId}"/>&studySubjectId=<c:out value="${studySubject.id}"/>&tabId=1&eventId=<c:out value="${eventId}"/>"
                   onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
                   onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
                  name="bt_View1" align="left" src="images/bt_View.gif" border="0" alt="<fmt:message key="view_default" bundle="${resword}"/>" title="<fmt:message key="view_default" bundle="${resword}"/>" hspace="2"></a>&nbsp;
            </td><td >
 <a href="javascript:openPrintCRFWindow('rest/clinicaldata/html/print/${study.oid}/${studySubject.oid}/${studyEvent.studyEventDefinition.oid}<c:if test="${studyEvent.studyEventDefinition.repeating}">[${studyEvent.sampleOrdinal}]</c:if>/${crfVersionOID}')"
            
               onMouseDown="javascript:setImage('bt_Print1','images/bt_Print_d.gif');"
               onMouseUp="javascript:setImage('bt_Print1','images/bt_Print.gif');"><img
              name="bt_Print1" align="left" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print_default" bundle="${resword}"/>" title="<fmt:message key="print_default" bundle="${resword}"/>"  hspace="2"></a>&nbsp;
       
      <c:if test="${ study.status.available   && 
        (userRole.director || userRole.coordinator)
        && !(studyEvent.subjectEventStatus.locked || studyEvent.subjectEventStatus.skipped)
        && dedc.eventCRF.id>0}">
   
    <a href="pages/managestudy/chooseCRFVersion?crfId=<c:out value="${dedc.eventCRF.crf.id}" />&crfName=<c:out value="${dedc.eventCRF.crf.name}" />&crfversionId=<c:out value="${dedc.eventCRF.crfVersion.id}" />&crfVersionName=<c:out value="${dedc.eventCRF.crfVersion.name}" />&studySubjectLabel=<c:out value="${studySubject.label}"/>&studySubjectId=<c:out value="${studySubject.id}"/>&eventCRFId=<c:out value="${dedc.eventCRF.id}"/>&eventDefinitionCRFId=<c:out value="${dedc.edc.id}" />"
   onMouseDown="javascript:setImage('bt_Reassign','images/bt_Reassign_d.gif');"
   onMouseUp="javascript:setImage('bt_Reassign','images/bt_Reassign.gif');"><img
      name="Reassign" src="images/bt_Reassign.gif" border="0" alt="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" title="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" align="left" hspace="6"></a>
                </c:if>
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
<td class="table_cell"><c:out value="${dec.eventCRF.crfVersion.name}" />&nbsp;</td>
<td class="table_cell" bgcolor="#F5F5F5" align="center">

    <c:choose>
        <c:when test="${dec.stage.initialDE}">
            <img src="images/icon_InitialDE.gif" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>" title="<fmt:message key="initial_data_entry" bundle="${resword}"/>">
        </c:when>
        <c:when test="${dec.stage.initialDE_Complete}">
            <img src="images/icon_InitialDEcomplete.gif" alt="<fmt:message key="initial_data_entry_complete" bundle="${resword}"/>" title="<fmt:message key="initial_data_entry_complete" bundle="${resword}"/>">
        </c:when>
        <c:when test="${dec.stage.doubleDE}">
            <img src="images/icon_DDE.gif" alt="<fmt:message key="double_data_entry" bundle="${resword}"/>" title="<fmt:message key="double_data_entry" bundle="${resword}"/>">
        </c:when>
        <c:when test="${dec.stage.doubleDE_Complete}">
            <img src="images/icon_DEcomplete.gif" alt="<fmt:message key="data_entry_complete" bundle="${resword}"/>" title="<fmt:message key="data_entry_complete" bundle="${resword}"/>">
        </c:when>

        <c:when test="${dec.stage.admin_Editing}">
            <img src="images/icon_AdminEdit.gif" alt="<fmt:message key="administrative_editing" bundle="${resword}"/>" title="<fmt:message key="administrative_editing" bundle="${resword}"/>">
        </c:when>

        <c:when test="${dec.stage.locked}">
            <img src="images/icon_Locked.gif" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>">
        </c:when>

        <c:otherwise>
            <img src="images/icon_Invalid.gif" alt="<fmt:message key="invalid" bundle="${resword}"/>" title="<fmt:message key="invalid" bundle="${resword}"/>">
        </c:otherwise>
    </c:choose>
</td>
<td class="table_cell"><c:out value="${dec.eventCRF.owner.name}" />&nbsp;</td>
<td class="table_cell">
    <c:choose>
        <c:when test="${!dec.eventDefinitionCRF.doubleEntry}">
            n/a
        </c:when>
        <c:otherwise>
            <c:choose>
                <c:when test="${dec.stage.doubleDE || dec.stage.doubleDE_Complete || dec.stage.admin_Editing || dec.stage.locked}">
                    <c:out value="${dec.eventCRF.updater.name}" />&nbsp;
                </c:when>
                <c:otherwise>
                    &nbsp;
                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>

</td>
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
       <td>     <a href="ViewSectionDataEntry?eventDefinitionCRFId=<c:out value="${dec.eventDefinitionCRF.id}"/>&ecId=<c:out value="${dec.eventCRF.id}"/>&tabId=1&eventId=<c:out value="${eventId}"/>"
               onMouseDown="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
               onMouseUp="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
              ><img name="bt_Print<c:out value="${rowCount}"/>" src="images/bt_View.gif" border="0" alt="<fmt:message key="view_data" bundle="${resword}"/>" title="<fmt:message key="view_data" bundle="${resword}"/>" align="left" hspace="2"></a>
</td><td>
 <a href="javascript:openPrintCRFWindow('rest/clinicaldata/html/print/${study.oid}/${studySubject.oid}/${studyEvent.studyEventDefinition.oid}<c:if test="${studyEvent.studyEventDefinition.repeating}">[${studyEvent.sampleOrdinal}]</c:if>/${dec.eventCRF.crfVersion.oid}')"
            
               onMouseDown="javascript:setImage('bt_Print<c:out value="${rowCount}"/>','images/bt_Print.gif');"
               onMouseUp="javascript:setImage('bt_Print<c:out value="${rowCount}"/>','images/bt_Print.gif');"
              ><img name="bt_Print<c:out value="${rowCount}"/>" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print" bundle="${resword}"/>" title="<fmt:message key="print" bundle="${resword}"/>"  hspace="2"></a>
</td>

            <c:if test="${(studySubject.status.name != 'removed'&& studySubject.status.name != 'auto-removed') && (study.status.available)}">
          <td>      <a href="RestoreEventCRF?action=confirm&id=<c:out value="${dec.eventCRF.id}"/>&studySubId=<c:out value="${studySubject.id}"/>"
                   onMouseDown="javascript:setImage('bt_Restore<c:out value="${rowCount}"/>','images/bt_Restore.gif');"
                   onMouseUp="javascript:setImage('bt_Restore<c:out value="${rowCount}"/>','images/bt_Restore.gif');"
                  ><img name="bt_Restore<c:out value="${rowCount}"/>" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>"  hspace="2"></a>
           </td> </c:if>
        </c:when>

        <c:when test='${actionQuery == ""}'>
        <td>    <a href="ViewSectionDataEntry?eventDefinitionCRFId=<c:out value="${dedc.edc.id}"/>&ecId=<c:out value="${dec.eventCRF.id}"/>&tabId=1&eventId=<c:out value="${eventId}"/>"
               onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
               onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"
              ><img name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="2"></a>
</td><td>
 <a href="javascript:openPrintCRFWindow('rest/clinicaldata/html/print/${study.oid}/${studySubject.oid}/${studyEvent.studyEventDefinition.oid}<c:if test="${studyEvent.studyEventDefinition.repeating}">[${studyEvent.sampleOrdinal}]</c:if>/${dec.eventCRF.crfVersion.oid}')"
               onMouseDown="javascript:setImage('bt_Print<c:out value="${rowCount}"/>','images/bt_Print.gif');"
               onMouseUp="javascript:setImage('bt_Print<c:out value="${rowCount}"/>','images/bt_Print.gif');"
              ><img name="bt_Print<c:out value="${rowCount}"/>" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print" bundle="${resword}"/>" title="<fmt:message key="print" bundle="${resword}"/>"  hspace="2"></a>
  </td>            
            
        </c:when>
        <c:otherwise>
            <c:if test="${studySubject.status.name != 'removed'&& studySubject.status.name != 'auto-removed' && !userRole.monitor}">
                <c:if test="${dec.continueInitialDataEntryPermitted}">
                <td><a href="#"
                    onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
                    onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');"
                    onclick="checkCRFLocked('<c:out value="${dec.eventCRF.id}"/>', '<c:out value="${actionQuery}"/>');">
                    <img name="bt_EnterData1" src="images/bt_EnterData.gif" border="0" alt="<fmt:message key="continue_entering_data" bundle="${resword}"/>" title="<fmt:message key="continue_entering_data" bundle="${resword}"/>" align="left" hspace="6"></a>
               </td> </c:if>
                <c:if test="${dec.startDoubleDataEntryPermitted}">
                 <td><a href="#"
                    onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
                    onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');"
                    onclick="checkCRFLocked('<c:out value="${dec.eventCRF.id}"/>', '<c:out value="${actionQuery}"/>');">
                    <img name="bt_EnterData1" src="images/bt_EnterData.gif" border="0" alt="<fmt:message key="begin_double_data_entry" bundle="${resword}"/>" title="<fmt:message key="begin_double_data_entry" bundle="${resword}"/>" align="left" hspace="6"></a>
                </td></c:if>
                <c:if test="${dec.continueDoubleDataEntryPermitted}">
                  <td><a href="#"
                    onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
                    onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');"
                    onclick="checkCRFLocked('<c:out value="${dec.eventCRF.id}"/>', '<c:out value="${actionQuery}"/>');">
                    <img name="bt_EnterData1" src="images/bt_EnterData.gif" border="0" alt="<fmt:message key="continue_entering_data" bundle="${resword}"/>" title="<fmt:message key="continue_entering_data" bundle="${resword}"/>" align="left" hspace="6"></a>
               </td> </c:if>
                <c:if test="${dec.performAdministrativeEditingPermitted}">
                <td> <a href="#"
                    onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
                    onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');"
                    onclick="checkCRFLocked('<c:out value="${dec.eventCRF.id}"/>', '<c:out value="${actionQuery}"/>');">
                    <img name="bt_EnterData1" src="images/bt_EnterData.gif" border="0" alt="<fmt:message key="administrative_editing" bundle="${resword}"/>" title="<fmt:message key="administrative_editing" bundle="${resword}"/>" align="left" hspace="6">
                    </a></td>
                </c:if>
            </c:if>


          <td>  <a href="ViewSectionDataEntry?eventDefinitionCRFId=<c:out value="${dec.eventDefinitionCRF.id}"/>&ecId=<c:out value="${dec.eventCRF.id}"/>&tabId=1&eventId=<c:out value="${eventId}"/>"
               onMouseDown="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
               onMouseUp="javascript:setImage('bt_View<c:out value="${rowCount}"/>','images/bt_View.gif');"
              ><img name="bt_Print<c:out value="${rowCount}"/>" src="images/bt_View.gif" border="0" alt="<fmt:message key="view_data" bundle="${resword}"/>" title="<fmt:message key="view_data" bundle="${resword}"/>"  hspace="2"></a>
              </td>

           <td> 
 <a href="javascript:openPrintCRFWindow('rest/clinicaldata/html/print/${study.oid}/${studySubject.oid}/${studyEvent.studyEventDefinition.oid}<c:if test="${studyEvent.studyEventDefinition.repeating}">[${studyEvent.sampleOrdinal}]</c:if>/${dec.eventCRF.crfVersion.oid}')"
           
               onMouseDown="javascript:setImage('bt_Print<c:out value="${rowCount}"/>','images/bt_Print.gif');"
               onMouseUp="javascript:setImage('bt_Print<c:out value="${rowCount}"/>','images/bt_Print.gif');"
              ><img name="bt_Print<c:out value="${rowCount}"/>" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print" bundle="${resword}"/>" title="<fmt:message key="print" bundle="${resword}"/>"  hspace="2"></a>
              </td>

            <c:if test="${(userRole.director || userBean.sysAdmin) && (study.status.available)}">
               <td> <a href="RemoveEventCRF?action=confirm&id=<c:out value="${dec.eventCRF.id}"/>&studySubId=<c:out value="${studySubject.id}"/>"
                   onMouseDown="javascript:setImage('bt_Remove<c:out value="${rowCount}"/>','images/bt_Remove.gif');"
                   onMouseUp="javascript:setImage('bt_Remove<c:out value="${rowCount}"/>','images/bt_Remove.gif');"
                  ><img name="bt_Remove<c:out value="${rowCount}"/>" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>"  hspace="2"></a>
                  </td>
            </c:if>

            <c:if test="${userBean.sysAdmin && (study.status.available)}">
               <td> <a href="DeleteEventCRF?action=confirm&ssId=<c:out value="${studySubject.id}"/>&ecId=<c:out value="${dec.eventCRF.id}"/>"
                   onMouseDown="javascript:setImage('bt_Delete<c:out value="${rowCount}"/>','images/bt_Delete.gif');"
                   onMouseUp="javascript:setImage('bt_Delete<c:out value="${rowCount}"/>','images/bt_Delete.gif');"
                  ><img name="bt_Remove<c:out value="${rowCount}"/>" src="images/bt_Delete.gif" border="0" alt="<fmt:message key="delete" bundle="${resword}"/>" title="<fmt:message key="delete" bundle="${resword}"/>"  hspace="2"></a>
                  </td>
            </c:if>

			   <!--  reasign crf version -->
            
    
 <c:if test="${( userRole.director || userRole.coordinator) &&
 (study.status.available ) 
 && !(studyEvent.subjectEventStatus.locked || studyEvent.subjectEventStatus.skipped)}">
   
  <td>  <a href="pages/managestudy/chooseCRFVersion?crfId=<c:out value="${dec.eventCRF.crf.id}" />&crfName=<c:out value="${dec.eventCRF.crf.name}" />&crfversionId=<c:out value="${dec.eventCRF.crfVersion.id}" />&crfVersionName=<c:out value="${dec.eventCRF.crfVersion.name}" />&studySubjectLabel=<c:out value="${studySubject.label}"/>&studySubjectId=<c:out value="${studySubject.id}"/>&eventCRFId=<c:out value="${dec.eventCRF.id}"/>&eventDefinitionCRFId=<c:out value="${dec.eventDefinitionCRF.id}"/>"
   onMouseDown="javascript:setImage('bt_Reassign','images/bt_Reassign_d.gif');"
   onMouseUp="javascript:setImage('bt_Reassign','images/bt_Reassign.gif');"><img
      name="Reassign" src="images/bt_Reassign.gif" border="0" alt="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" title="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" align="left" hspace="6"></a>
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