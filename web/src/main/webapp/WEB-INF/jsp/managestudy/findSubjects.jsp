<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>


<jsp:include page="../include/submit-header.jsp"/>
<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">
<style>
    .icon > span {
        font-family: 'Open Sans', arial, helvetica, sans-serif;
    }
</style>

<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
<%-- <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa-original.js"></script> --%>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js"></script>

<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.4.1.js"></script>

<c:if test="${participantIDVerification == 'true'}">
    <script type="text/javascript" language="JavaScript" src="js/lib/bootstrap-tour.js"></script>
</c:if>

<script type="text/javascript">
    function onInvokeAction(id,action) {
        if(id.indexOf('findSubjects') == -1)  {
        setExportToLimit(id, '');
        }
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        location.href = '${pageContext.request.contextPath}/ListStudySubjects?'+ parameterString;
    }
    function URLSearchParams(name){
        var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
        if (results == null){
           return null;
        }
        else {
           return decodeURI(results[1]) || 0;
        }
    }

    jQuery(document).ready(function() {
        jQuery('#addSubject').click(function() {
            $('#sidebar_Alerts_open .alert').empty();
            jQuery.blockUI({ message: jQuery('#addSubjectForm'), css:{left: "300px", top:"10px" } });
        });

        jQuery('input#cancel').click(function() {
            jQuery.unblockUI();
            return false;
        });

        if (URLSearchParams('addNewSubject')) {
            jQuery('#addSubject').click();
        }

        sessionStorage.setItem("pageContextPath", "<c:out value='${pageContext.request.contextPath}' />");
        sessionStorage.setItem("studyOid", "<c:out value='${study.oid}' />");
        sessionStorage.setItem("studyName", "<c:out value='${study.name}' />");
        sessionStorage.setItem("studyParentId", "<c:out value='${study.parentStudyId}' />");
        sessionStorage.setItem("siteSubStringMark", "<c:out value='${siteSubStringMark}' />");
    });

    window.onload = function() {
        document.getElementById("btn").focus();
            <c:if test="${showOverlay}">
                jQuery.blockUI({ message: jQuery('#addSubjectForm'), css:{left: "300px", top:"10px" } });
            </c:if>
    };


</script>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

        <fmt:message key="instructions" bundle="${resword}"/>

        <div class="sidebar_tab_content">

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>

        <fmt:message key="instructions" bundle="${resword}"/>

    </td>
</tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='crf' class='org.akaza.openclinica.bean.admin.CRFBean'/>


<h1>
    <span class="title_manage">
        <fmt:message key="view_subjects_in" bundle="${restext}"/> <c:out value="${study.name}"/>
    </span>
</h1>
<br/>

<div id="box" class="dialog">
    <span id="mbm">
        <br>
        <c:if test="${(!study.status.pending)}">
            <fmt:message key="study_frozen_locked_note" bundle="${restext}"/>
        </c:if>

        <c:if test="${(study.status.pending)}">
            <fmt:message key="study_design_note" bundle="${restext}"/>
        </c:if>
    </span><br>
    <div style="text-align:center; width:100%;">
        <button id="btn" onclick="hm('box');">OK</button>
    </div>
</div>

<div id="findSubjectsDiv">
    <form  action="${pageContext.request.contextPath}/ListStudySubjects">
        <input type="hidden" name="module" value="admin">
        ${findSubjectsHtml}
    </form>
</div>

<c:if test="${userRole.monitor || userRole.coordinator || userRole.director || userRole.investigator || userRole.researchAssistant || userRole.researchAssistant2}">
    <div id="addSubjectForm" style="display:none;">
          <c:import url="../submit/addNewSubjectExpressNew.jsp">
          </c:import>
    </div>
</c:if>

<br>
<jsp:include page="../include/footer.jsp"/>

<table id="findSubjects" border="0" cellpadding="0" cellspacing="0" class="table">
    <thead>
    <tr class="header" width="100%">
    </tr>
    <tr class="toolbar">
        <td colspan="8">
        <table border="0" cellpadding="0" cellspacing="1">
            <tbody><tr>
                <td><img src="/OpenClinica/images/table/firstPageDisabled.gif" alt="First"></td>
                <td><img src="/OpenClinica/images/table/prevPageDisabled.gif" alt="Prev"></td>
                <td><a href="javascript:jQuery.jmesa.setPageToLimit('findSubjects','2');onInvokeAction('findSubjects','next_page')"><img src="/OpenClinica/images/table/nextPage.gif" title="Next Page" alt="Next"></a></td>
                <td><a href="javascript:jQuery.jmesa.setPageToLimit('findSubjects','2');onInvokeAction('findSubjects','last_page')"><img src="/OpenClinica/images/table/lastPage.gif" title="Last Page" alt="Last"></a></td>
                <td><img src="/OpenClinica/images/table/separator.gif" alt="Separator"></td>
                <td><select name="maxRows" onchange="jQuery.jmesa.setMaxRowsToLimit('findSubjects', this.options[this.selectedIndex].value);onInvokeAction('findSubjects','max_rows')">
                <option value="15">15 </option><option value="25">25 </option><option value="50" selected="selected">50 </option><option value="100">100 </option>
                </select></td>
                <td><img src="/OpenClinica/images/table/separator.gif" alt="Separator"></td>
                <td><a id="showMore" style="text-decoration: none" href="javascript:hideCols('findSubjects',[1,2,3,4],true);"><div>&nbsp;Show More&nbsp;</div></a><a id="hide" style="display: none;text-decoration: none;" href="javascript:hideCols('findSubjects',[1,2,3,4],false);"><div>&nbsp;Hide&nbsp;</div></a><script type="text/javascript">$j = jQuery.noConflict(); $j(document).ready(function(){ hideCols('findSubjects',[1,2,3,4],false);});</script></td>
                <td><img src="/OpenClinica/images/table/separator.gif" alt="Separator"></td>
                <td><select id="sedDropDown" onchange="var selectedValue = document.getElementById('sedDropDown').options[document.getElementById('sedDropDown').selectedIndex].value;  var maxrows = $('select[name=maxRows]').val(); if (selectedValue != null  ) { window.location='ListEventsForSubjects?module=submit&amp;defId=' + selectedValue + '&amp;listEventsForSubject_mr_=' + maxrows; } "><option>Select An Event</option><option value="1">Visits</option><option value="3">Visits 2</option></select></td>
                <td><input id="showMoreLink" type="hidden" name="showMoreLink" value="true"></td>
                <td><a style="text-decoration: none" href="javascript:;" id="addSubject" ""="">&nbsp;Add New Participant&nbsp;</a></td>
            </tr>
        </tbody></table>
        </td>
    </tr>
    <tr class="header">
        <td><div onmouseover="this.style.cursor='pointer'" onmouseout="this.style.cursor='default'" onclick="jQuery.jmesa.addSortToLimit('findSubjects','0','studySubject.label','asc');onInvokeAction('findSubjects', 'sort')">Participant ID</div></td>
        <td style="display: none;"><div onmouseover="this.style.cursor='pointer'" onmouseout="this.style.cursor='default'" onclick="jQuery.jmesa.addSortToLimit('findSubjects','1','enrolledAt','asc');onInvokeAction('findSubjects', 'sort')">Site ID</div></td>
        <td style="display: none;"><div onmouseover="this.style.cursor='pointer'" onmouseout="this.style.cursor='default'" onclick="jQuery.jmesa.addSortToLimit('findSubjects','2','studySubject.status','asc');onInvokeAction('findSubjects', 'sort')">Status</div></td>
        <td style="display: none;"><div onmouseover="this.style.cursor='pointer'" onmouseout="this.style.cursor='default'" onclick="jQuery.jmesa.addSortToLimit('findSubjects','3','studySubject.oid','asc');onInvokeAction('findSubjects', 'sort')">OID</div></td>
        <td style="display: none;"><div onmouseover="this.style.cursor='pointer'" onmouseout="this.style.cursor='default'" onclick="jQuery.jmesa.addSortToLimit('findSubjects','4','participate.status','asc');onInvokeAction('findSubjects', 'sort')">Participate Status</div></td>
        <td><div>Visits</div></td>
        <td><div>Visits 2</div></td>
        <td><div>Actions&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div></td>
    </tr>
    <tr class="filter">
        <td><div class="dynFilter" onclick="jQuery.jmesa.createDynFilter(this, 'findSubjects','studySubject.label')"></div></td>
        <td style="display: none;"><div class="dynFilter" onclick="jQuery.jmesa.createDynFilter(this, 'findSubjects','enrolledAt')"></div></td>
        <td style="display: none;"><div class="dynFilter" onclick="jQuery.jmesa.createDroplistDynFilter(this,'findSubjects','studySubject.status',{'Available':'Available','signed':'signed','Removed':'Removed','auto-removed':'auto-removed'})"></div></td>
        <td style="display: none;"><div class="dynFilter" onclick="jQuery.jmesa.createDynFilter(this, 'findSubjects','studySubject.oid')"></div></td>
        <td style="display: none;"><div class="dynFilter" onclick="jQuery.jmesa.createDroplistDynFilter(this,'findSubjects','participate.status',{'':'','Active':'Active','Inactive':'Inactive','Created':'Created','Invited':'Invited'})"></div></td>
        <td><div class="dynFilter" onclick="jQuery.jmesa.createDroplistDynFilter(this,'findSubjects','sed_1',{'scheduled':'scheduled','not scheduled':'not scheduled','data entry started':'data entry started','completed':'completed','stopped':'stopped','skipped':'skipped','signed':'signed','Locked':'Locked'})"></div></td>
        <td><div class="dynFilter" onclick="jQuery.jmesa.createDroplistDynFilter(this,'findSubjects','sed_3',{'scheduled':'scheduled','not scheduled':'not scheduled','data entry started':'data entry started','completed':'completed','stopped':'stopped','skipped':'skipped','signed':'signed','Locked':'Locked'})"></div></td>
        <td><a style="text-decoration: none" href="javascript:onInvokeAction('findSubjects','filter')">&nbsp;Apply Filter&nbsp;</a> <a style="text-decoration: none" href="javascript:jQuery.jmesa.removeAllFiltersFromLimit('findSubjects');onInvokeAction('findSubjects','clear')">&nbsp;Clear Filter&nbsp;</a></td>
    </tr>
    </thead>
    <tbody class="tbody">
    <tr id="findSubjects_row1" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="AJASON" class="pidVerification" id="pid-39" href="ViewStudySubject?id=39">AJASON</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Removed</td>
        <td style="display: none;">SS_AJASON</td>
        <td style="display: none;">Inactive</td>
        <td>
<div id="Lock_AJASON_1_1" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_AJASON_1_1'); javascript:leftnavExpand('Menu_off_AJASON_1_1'); " onmouseover="layersShowOrHide('visible','Event_AJASON_1_1'); javascript:setImage('ExpandIcon_AJASON_1_1','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_AJASON_1_1'); layersShowOrHide('hidden','Lock_AJASON_1_1'); javascript:setImage('ExpandIcon_AJASON_1_1','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_AJASON_1_1" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: AJASON<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_AJASON_1_1'); javascript:leftnavExpand('Menu_off_AJASON_1_1'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_AJASON_1_1'); layersShowOrHide('hidden','Lock_AJASON_1_1'); javascript:setImage('ExpandIcon_AJASON_1_1','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_AJASON_1_1" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_AJASON_1_1" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="EnterDataForStudyEvent?eventId="></a><a href="EnterDataForStudyEvent?eventId=" <span="" border="0" align="left" class="icon icon-search"></a>&nbsp;&nbsp;<a href="EnterDataForStudyEvent?eventId=">View</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_AJASON_1_1'); javascript:leftnavExpand('Menu_off_AJASON_1_1'); " onmouseover="moveObject('Event_AJASON_1_1', event); setImage('ExpandIcon_AJASON_1_1','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_AJASON_1_1'); setImage('ExpandIcon_AJASON_1_1','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_AJASON_1_1',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_AJASON_3_1" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_AJASON_3_1'); javascript:leftnavExpand('Menu_off_AJASON_3_1'); " onmouseover="layersShowOrHide('visible','Event_AJASON_3_1'); javascript:setImage('ExpandIcon_AJASON_3_1','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_AJASON_3_1'); layersShowOrHide('hidden','Lock_AJASON_3_1'); javascript:setImage('ExpandIcon_AJASON_3_1','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_AJASON_3_1" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: AJASON<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_AJASON_3_1'); javascript:leftnavExpand('Menu_off_AJASON_3_1'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_AJASON_3_1'); layersShowOrHide('hidden','Lock_AJASON_3_1'); javascript:setImage('ExpandIcon_AJASON_3_1','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_AJASON_3_1" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_AJASON_3_1" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="EnterDataForStudyEvent?eventId="></a><a href="EnterDataForStudyEvent?eventId=" <span="" border="0" align="left" class="icon icon-search"></a>&nbsp;&nbsp;<a href="EnterDataForStudyEvent?eventId=">View</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_AJASON_3_1'); javascript:leftnavExpand('Menu_off_AJASON_3_1'); " onmouseover="moveObject('Event_AJASON_3_1', event); setImage('ExpandIcon_AJASON_3_1','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_AJASON_3_1'); setImage('ExpandIcon_AJASON_3_1','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_AJASON_3_1',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="AJASON" class="pidVerification" id="pid-39" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=39"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-ccw');" onmousedown="javascript:setImage('bt_View1','icon icon-ccw');" href="RestoreStudySubject?action=confirm&amp;id=39&amp;subjectId=39&amp;studyId=62"><span hspace="2" border="0" title="Restore" alt="Restore" class="icon icon-ccw" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row2" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="AJASONA" class="pidVerification" id="pid-40" href="ViewStudySubject?id=40">AJASONA</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Removed</td>
        <td style="display: none;">SS_AJASONA</td>
        <td style="display: none;">Inactive</td>
        <td>
<div id="Lock_AJASONA_1_2" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_AJASONA_1_2'); javascript:leftnavExpand('Menu_off_AJASONA_1_2'); " onmouseover="layersShowOrHide('visible','Event_AJASONA_1_2'); javascript:setImage('ExpandIcon_AJASONA_1_2','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_AJASONA_1_2'); layersShowOrHide('hidden','Lock_AJASONA_1_2'); javascript:setImage('ExpandIcon_AJASONA_1_2','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_AJASONA_1_2" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: AJASONA<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_AJASONA_1_2'); javascript:leftnavExpand('Menu_off_AJASONA_1_2'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_AJASONA_1_2'); layersShowOrHide('hidden','Lock_AJASONA_1_2'); javascript:setImage('ExpandIcon_AJASONA_1_2','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_AJASONA_1_2" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_AJASONA_1_2" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="EnterDataForStudyEvent?eventId="></a><a href="EnterDataForStudyEvent?eventId=" <span="" border="0" align="left" class="icon icon-search"></a>&nbsp;&nbsp;<a href="EnterDataForStudyEvent?eventId=">View</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_AJASONA_1_2'); javascript:leftnavExpand('Menu_off_AJASONA_1_2'); " onmouseover="moveObject('Event_AJASONA_1_2', event); setImage('ExpandIcon_AJASONA_1_2','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_AJASONA_1_2'); setImage('ExpandIcon_AJASONA_1_2','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_AJASONA_1_2',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_AJASONA_3_2" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_AJASONA_3_2'); javascript:leftnavExpand('Menu_off_AJASONA_3_2'); " onmouseover="layersShowOrHide('visible','Event_AJASONA_3_2'); javascript:setImage('ExpandIcon_AJASONA_3_2','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_AJASONA_3_2'); layersShowOrHide('hidden','Lock_AJASONA_3_2'); javascript:setImage('ExpandIcon_AJASONA_3_2','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_AJASONA_3_2" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: AJASONA<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_AJASONA_3_2'); javascript:leftnavExpand('Menu_off_AJASONA_3_2'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_AJASONA_3_2'); layersShowOrHide('hidden','Lock_AJASONA_3_2'); javascript:setImage('ExpandIcon_AJASONA_3_2','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_AJASONA_3_2" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_AJASONA_3_2" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="EnterDataForStudyEvent?eventId="></a><a href="EnterDataForStudyEvent?eventId=" <span="" border="0" align="left" class="icon icon-search"></a>&nbsp;&nbsp;<a href="EnterDataForStudyEvent?eventId=">View</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_AJASONA_3_2'); javascript:leftnavExpand('Menu_off_AJASONA_3_2'); " onmouseover="moveObject('Event_AJASONA_3_2', event); setImage('ExpandIcon_AJASONA_3_2','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_AJASONA_3_2'); setImage('ExpandIcon_AJASONA_3_2','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_AJASONA_3_2',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="AJASONA" class="pidVerification" id="pid-40" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=40"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-ccw');" onmousedown="javascript:setImage('bt_View1','icon icon-ccw');" href="RestoreStudySubject?action=confirm&amp;id=40&amp;subjectId=40&amp;studyId=62"><span hspace="2" border="0" title="Restore" alt="Restore" class="icon icon-ccw" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row3" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="ANCHOVY" class="pidVerification" id="pid-19" href="ViewStudySubject?id=19">ANCHOVY</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_ANCHOVY</td>
        <td style="display: none;">Invited</td>
        <td>
<div id="Lock_ANCHOVY_1_3" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_ANCHOVY_1_3'); javascript:leftnavExpand('Menu_off_ANCHOVY_1_3'); " onmouseover="layersShowOrHide('visible','Event_ANCHOVY_1_3'); javascript:setImage('ExpandIcon_ANCHOVY_1_3','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_ANCHOVY_1_3'); layersShowOrHide('hidden','Lock_ANCHOVY_1_3'); javascript:setImage('ExpandIcon_ANCHOVY_1_3','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_ANCHOVY_1_3" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: ANCHOVY<br>Event: Visits<br><br><b>completed<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_ANCHOVY_1_3'); javascript:leftnavExpand('Menu_off_ANCHOVY_1_3'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_ANCHOVY_1_3'); layersShowOrHide('hidden','Lock_ANCHOVY_1_3'); javascript:setImage('ExpandIcon_ANCHOVY_1_3','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_ANCHOVY_1_3" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_ANCHOVY_1_3" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="EnterDataForStudyEvent?eventId=3"></a><a href="EnterDataForStudyEvent?eventId=3" <span="" border="0" align="left" class="icon icon-search"></a>&nbsp;&nbsp;<a href="EnterDataForStudyEvent?eventId=3">View</a></td>
</tr>
<tr valign="top">
<td class="table_cell_left"><a href="UpdateStudyEvent?event_id=3&amp;ss_id=19"><span border="0" align="left" class="icon icon-pencil">&nbsp;&nbsp;</span></a><a href="UpdateStudyEvent?event_id=3&amp;ss_id=19">Edit</a></td>
</tr>
<tr valign="top">
<td class="table_cell_left"><a href="RemoveStudyEvent?action=confirm&amp;id=3&amp;studySubId=19"><span border="0" align="left" class="icon icon-cancel">&nbsp;&nbsp;</span></a><a href="RemoveStudyEvent?action=confirm&amp;id=3&amp;studySubId=19">Remove</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_ANCHOVY_1_3'); javascript:leftnavExpand('Menu_off_ANCHOVY_1_3'); " onmouseover="moveObject('Event_ANCHOVY_1_3', event); setImage('ExpandIcon_ANCHOVY_1_3','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_ANCHOVY_1_3'); setImage('ExpandIcon_ANCHOVY_1_3','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_ANCHOVY_1_3',event); "><span class="icon icon-checkbox-checked green" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_ANCHOVY_3_3" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_ANCHOVY_3_3'); javascript:leftnavExpand('Menu_off_ANCHOVY_3_3'); " onmouseover="layersShowOrHide('visible','Event_ANCHOVY_3_3'); javascript:setImage('ExpandIcon_ANCHOVY_3_3','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_ANCHOVY_3_3'); layersShowOrHide('hidden','Lock_ANCHOVY_3_3'); javascript:setImage('ExpandIcon_ANCHOVY_3_3','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_ANCHOVY_3_3" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: ANCHOVY<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_ANCHOVY_3_3'); javascript:leftnavExpand('Menu_off_ANCHOVY_3_3'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_ANCHOVY_3_3'); layersShowOrHide('hidden','Lock_ANCHOVY_3_3'); javascript:setImage('ExpandIcon_ANCHOVY_3_3','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_ANCHOVY_3_3" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_ANCHOVY_3_3" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=19&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=19&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=19&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_ANCHOVY_3_3'); javascript:leftnavExpand('Menu_off_ANCHOVY_3_3'); " onmouseover="moveObject('Event_ANCHOVY_3_3', event); setImage('ExpandIcon_ANCHOVY_3_3','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_ANCHOVY_3_3'); setImage('ExpandIcon_ANCHOVY_3_3','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_ANCHOVY_3_3',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="ANCHOVY" class="pidVerification" id="pid-19" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=19"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=19&amp;subjectId=19&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=19"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row4" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="ARTICHOKE" class="pidVerification" id="pid-18" href="ViewStudySubject?id=18">ARTICHOKE</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_ARTICHOK</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_ARTICHOKE_1_4" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_ARTICHOKE_1_4'); javascript:leftnavExpand('Menu_off_ARTICHOKE_1_4'); " onmouseover="layersShowOrHide('visible','Event_ARTICHOKE_1_4'); javascript:setImage('ExpandIcon_ARTICHOKE_1_4','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_ARTICHOKE_1_4'); layersShowOrHide('hidden','Lock_ARTICHOKE_1_4'); javascript:setImage('ExpandIcon_ARTICHOKE_1_4','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_ARTICHOKE_1_4" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: ARTICHOKE<br>Event: Visits<br><br><b>completed<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_ARTICHOKE_1_4'); javascript:leftnavExpand('Menu_off_ARTICHOKE_1_4'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_ARTICHOKE_1_4'); layersShowOrHide('hidden','Lock_ARTICHOKE_1_4'); javascript:setImage('ExpandIcon_ARTICHOKE_1_4','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_ARTICHOKE_1_4" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_ARTICHOKE_1_4" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="EnterDataForStudyEvent?eventId=6"></a><a href="EnterDataForStudyEvent?eventId=6" <span="" border="0" align="left" class="icon icon-search"></a>&nbsp;&nbsp;<a href="EnterDataForStudyEvent?eventId=6">View</a></td>
</tr>
<tr valign="top">
<td class="table_cell_left"><a href="UpdateStudyEvent?event_id=6&amp;ss_id=18"><span border="0" align="left" class="icon icon-pencil">&nbsp;&nbsp;</span></a><a href="UpdateStudyEvent?event_id=6&amp;ss_id=18">Edit</a></td>
</tr>
<tr valign="top">
<td class="table_cell_left"><a href="RemoveStudyEvent?action=confirm&amp;id=6&amp;studySubId=18"><span border="0" align="left" class="icon icon-cancel">&nbsp;&nbsp;</span></a><a href="RemoveStudyEvent?action=confirm&amp;id=6&amp;studySubId=18">Remove</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_ARTICHOKE_1_4'); javascript:leftnavExpand('Menu_off_ARTICHOKE_1_4'); " onmouseover="moveObject('Event_ARTICHOKE_1_4', event); setImage('ExpandIcon_ARTICHOKE_1_4','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_ARTICHOKE_1_4'); setImage('ExpandIcon_ARTICHOKE_1_4','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_ARTICHOKE_1_4',event); "><span class="icon icon-checkbox-checked green" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_ARTICHOKE_3_4" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_ARTICHOKE_3_4'); javascript:leftnavExpand('Menu_off_ARTICHOKE_3_4'); " onmouseover="layersShowOrHide('visible','Event_ARTICHOKE_3_4'); javascript:setImage('ExpandIcon_ARTICHOKE_3_4','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_ARTICHOKE_3_4'); layersShowOrHide('hidden','Lock_ARTICHOKE_3_4'); javascript:setImage('ExpandIcon_ARTICHOKE_3_4','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_ARTICHOKE_3_4" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: ARTICHOKE<br>Event: Visits 2<br><br><b>completed<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_ARTICHOKE_3_4'); javascript:leftnavExpand('Menu_off_ARTICHOKE_3_4'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_ARTICHOKE_3_4'); layersShowOrHide('hidden','Lock_ARTICHOKE_3_4'); javascript:setImage('ExpandIcon_ARTICHOKE_3_4','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_ARTICHOKE_3_4" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_ARTICHOKE_3_4" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="EnterDataForStudyEvent?eventId=7"></a><a href="EnterDataForStudyEvent?eventId=7" <span="" border="0" align="left" class="icon icon-search"></a>&nbsp;&nbsp;<a href="EnterDataForStudyEvent?eventId=7">View</a></td>
</tr>
<tr valign="top">
<td class="table_cell_left"><a href="UpdateStudyEvent?event_id=7&amp;ss_id=18"><span border="0" align="left" class="icon icon-pencil">&nbsp;&nbsp;</span></a><a href="UpdateStudyEvent?event_id=7&amp;ss_id=18">Edit</a></td>
</tr>
<tr valign="top">
<td class="table_cell_left"><a href="RemoveStudyEvent?action=confirm&amp;id=7&amp;studySubId=18"><span border="0" align="left" class="icon icon-cancel">&nbsp;&nbsp;</span></a><a href="RemoveStudyEvent?action=confirm&amp;id=7&amp;studySubId=18">Remove</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_ARTICHOKE_3_4'); javascript:leftnavExpand('Menu_off_ARTICHOKE_3_4'); " onmouseover="moveObject('Event_ARTICHOKE_3_4', event); setImage('ExpandIcon_ARTICHOKE_3_4','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_ARTICHOKE_3_4'); setImage('ExpandIcon_ARTICHOKE_3_4','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_ARTICHOKE_3_4',event); "><span class="icon icon-checkbox-checked green" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="ARTICHOKE" class="pidVerification" id="pid-18" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=18"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=18&amp;subjectId=18&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=18"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row5" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="ATEST-9" class="pidVerification" id="pid-41" href="ViewStudySubject?id=41">ATEST-9</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_ATEST9</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_ATEST-9_1_5" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 401px; left: 343px; display: block;"><a href="javascript:leftnavExpand('Menu_on_ATEST-9_1_5'); javascript:leftnavExpand('Menu_off_ATEST-9_1_5'); " onmouseover="layersShowOrHide('visible','Event_ATEST-9_1_5'); javascript:setImage('ExpandIcon_ATEST-9_1_5','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_ATEST-9_1_5'); layersShowOrHide('hidden','Lock_ATEST-9_1_5'); javascript:setImage('ExpandIcon_ATEST-9_1_5','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_ATEST-9_1_5" style="position: absolute; visibility: hidden; z-index: 3; width: 180px; top: 430px; float: left; left: 368px; display: block;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: ATEST-9<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_ATEST-9_1_5'); javascript:leftnavExpand('Menu_off_ATEST-9_1_5'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_ATEST-9_1_5'); layersShowOrHide('hidden','Lock_ATEST-9_1_5'); javascript:setImage('ExpandIcon_ATEST-9_1_5','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_ATEST-9_1_5" style="">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_ATEST-9_1_5" style="display: none;">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=41&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=41&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=41&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_ATEST-9_1_5'); javascript:leftnavExpand('Menu_off_ATEST-9_1_5'); " onmouseover="moveObject('Event_ATEST-9_1_5', event); setImage('ExpandIcon_ATEST-9_1_5','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_ATEST-9_1_5'); setImage('ExpandIcon_ATEST-9_1_5','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_ATEST-9_1_5',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_ATEST-9_3_5" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_ATEST-9_3_5'); javascript:leftnavExpand('Menu_off_ATEST-9_3_5'); " onmouseover="layersShowOrHide('visible','Event_ATEST-9_3_5'); javascript:setImage('ExpandIcon_ATEST-9_3_5','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_ATEST-9_3_5'); layersShowOrHide('hidden','Lock_ATEST-9_3_5'); javascript:setImage('ExpandIcon_ATEST-9_3_5','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_ATEST-9_3_5" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: ATEST-9<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_ATEST-9_3_5'); javascript:leftnavExpand('Menu_off_ATEST-9_3_5'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_ATEST-9_3_5'); layersShowOrHide('hidden','Lock_ATEST-9_3_5'); javascript:setImage('ExpandIcon_ATEST-9_3_5','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_ATEST-9_3_5" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_ATEST-9_3_5" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=41&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=41&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=41&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_ATEST-9_3_5'); javascript:leftnavExpand('Menu_off_ATEST-9_3_5'); " onmouseover="moveObject('Event_ATEST-9_3_5', event); setImage('ExpandIcon_ATEST-9_3_5','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_ATEST-9_3_5'); setImage('ExpandIcon_ATEST-9_3_5','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_ATEST-9_3_5',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="ATEST-9" class="pidVerification" id="pid-41" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=41"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=41&amp;subjectId=41&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=41"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row6" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="BACON" class="pidVerification" id="pid-7" href="ViewStudySubject?id=7">BACON</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_BACON</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_BACON_1_6" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_BACON_1_6'); javascript:leftnavExpand('Menu_off_BACON_1_6'); " onmouseover="layersShowOrHide('visible','Event_BACON_1_6'); javascript:setImage('ExpandIcon_BACON_1_6','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_BACON_1_6'); layersShowOrHide('hidden','Lock_BACON_1_6'); javascript:setImage('ExpandIcon_BACON_1_6','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_BACON_1_6" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: BACON<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_BACON_1_6'); javascript:leftnavExpand('Menu_off_BACON_1_6'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_BACON_1_6'); layersShowOrHide('hidden','Lock_BACON_1_6'); javascript:setImage('ExpandIcon_BACON_1_6','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_BACON_1_6" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_BACON_1_6" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=7&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=7&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=7&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_BACON_1_6'); javascript:leftnavExpand('Menu_off_BACON_1_6'); " onmouseover="moveObject('Event_BACON_1_6', event); setImage('ExpandIcon_BACON_1_6','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_BACON_1_6'); setImage('ExpandIcon_BACON_1_6','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_BACON_1_6',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_BACON_3_6" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_BACON_3_6'); javascript:leftnavExpand('Menu_off_BACON_3_6'); " onmouseover="layersShowOrHide('visible','Event_BACON_3_6'); javascript:setImage('ExpandIcon_BACON_3_6','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_BACON_3_6'); layersShowOrHide('hidden','Lock_BACON_3_6'); javascript:setImage('ExpandIcon_BACON_3_6','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_BACON_3_6" style="position: absolute; visibility: hidden; z-index: 3; width: 180px; top: 456px; float: left; left: 481px; display: block;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: BACON<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_BACON_3_6'); javascript:leftnavExpand('Menu_off_BACON_3_6'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_BACON_3_6'); layersShowOrHide('hidden','Lock_BACON_3_6'); javascript:setImage('ExpandIcon_BACON_3_6','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_BACON_3_6" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_BACON_3_6" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=7&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=7&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=7&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_BACON_3_6'); javascript:leftnavExpand('Menu_off_BACON_3_6'); " onmouseover="moveObject('Event_BACON_3_6', event); setImage('ExpandIcon_BACON_3_6','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_BACON_3_6'); setImage('ExpandIcon_BACON_3_6','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_BACON_3_6',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="BACON" class="pidVerification" id="pid-7" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=7"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=7&amp;subjectId=7&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=7"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row7" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="BASIL" class="pidVerification" id="pid-1" href="ViewStudySubject?id=1">BASIL</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_BASIL</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_BASIL_1_7" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 485px; left: 342px; display: block;"><a href="javascript:leftnavExpand('Menu_on_BASIL_1_7'); javascript:leftnavExpand('Menu_off_BASIL_1_7'); " onmouseover="layersShowOrHide('visible','Event_BASIL_1_7'); javascript:setImage('ExpandIcon_BASIL_1_7','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_BASIL_1_7'); layersShowOrHide('hidden','Lock_BASIL_1_7'); javascript:setImage('ExpandIcon_BASIL_1_7','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_BASIL_1_7" style="position: absolute; visibility: hidden; z-index: 3; width: 180px; top: 504px; float: left; left: 344px; display: block;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: BASIL<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_BASIL_1_7'); javascript:leftnavExpand('Menu_off_BASIL_1_7'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_BASIL_1_7'); layersShowOrHide('hidden','Lock_BASIL_1_7'); javascript:setImage('ExpandIcon_BASIL_1_7','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_BASIL_1_7" style="">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_BASIL_1_7" style="display: none;">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=1&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=1&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=1&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_BASIL_1_7'); javascript:leftnavExpand('Menu_off_BASIL_1_7'); " onmouseover="moveObject('Event_BASIL_1_7', event); setImage('ExpandIcon_BASIL_1_7','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_BASIL_1_7'); setImage('ExpandIcon_BASIL_1_7','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_BASIL_1_7',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_BASIL_3_7" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_BASIL_3_7'); javascript:leftnavExpand('Menu_off_BASIL_3_7'); " onmouseover="layersShowOrHide('visible','Event_BASIL_3_7'); javascript:setImage('ExpandIcon_BASIL_3_7','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_BASIL_3_7'); layersShowOrHide('hidden','Lock_BASIL_3_7'); javascript:setImage('ExpandIcon_BASIL_3_7','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_BASIL_3_7" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: BASIL<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_BASIL_3_7'); javascript:leftnavExpand('Menu_off_BASIL_3_7'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_BASIL_3_7'); layersShowOrHide('hidden','Lock_BASIL_3_7'); javascript:setImage('ExpandIcon_BASIL_3_7','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_BASIL_3_7" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_BASIL_3_7" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=1&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=1&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=1&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_BASIL_3_7'); javascript:leftnavExpand('Menu_off_BASIL_3_7'); " onmouseover="moveObject('Event_BASIL_3_7', event); setImage('ExpandIcon_BASIL_3_7','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_BASIL_3_7'); setImage('ExpandIcon_BASIL_3_7','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_BASIL_3_7',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="BASIL" class="pidVerification" id="pid-1" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=1"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=1&amp;subjectId=1&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=1"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row8" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="CAPICOLA" class="pidVerification" id="pid-21" href="ViewStudySubject?id=21">CAPICOLA</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_CAPICOLA</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_CAPICOLA_1_8" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_CAPICOLA_1_8'); javascript:leftnavExpand('Menu_off_CAPICOLA_1_8'); " onmouseover="layersShowOrHide('visible','Event_CAPICOLA_1_8'); javascript:setImage('ExpandIcon_CAPICOLA_1_8','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_CAPICOLA_1_8'); layersShowOrHide('hidden','Lock_CAPICOLA_1_8'); javascript:setImage('ExpandIcon_CAPICOLA_1_8','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_CAPICOLA_1_8" style="position: absolute; visibility: hidden; z-index: 3; width: 180px; top: 557px; float: left; left: 364px; display: block;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: CAPICOLA<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_CAPICOLA_1_8'); javascript:leftnavExpand('Menu_off_CAPICOLA_1_8'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_CAPICOLA_1_8'); layersShowOrHide('hidden','Lock_CAPICOLA_1_8'); javascript:setImage('ExpandIcon_CAPICOLA_1_8','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_CAPICOLA_1_8" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_CAPICOLA_1_8" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=21&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=21&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=21&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_CAPICOLA_1_8'); javascript:leftnavExpand('Menu_off_CAPICOLA_1_8'); " onmouseover="moveObject('Event_CAPICOLA_1_8', event); setImage('ExpandIcon_CAPICOLA_1_8','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_CAPICOLA_1_8'); setImage('ExpandIcon_CAPICOLA_1_8','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_CAPICOLA_1_8',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_CAPICOLA_3_8" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_CAPICOLA_3_8'); javascript:leftnavExpand('Menu_off_CAPICOLA_3_8'); " onmouseover="layersShowOrHide('visible','Event_CAPICOLA_3_8'); javascript:setImage('ExpandIcon_CAPICOLA_3_8','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_CAPICOLA_3_8'); layersShowOrHide('hidden','Lock_CAPICOLA_3_8'); javascript:setImage('ExpandIcon_CAPICOLA_3_8','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_CAPICOLA_3_8" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: CAPICOLA<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_CAPICOLA_3_8'); javascript:leftnavExpand('Menu_off_CAPICOLA_3_8'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_CAPICOLA_3_8'); layersShowOrHide('hidden','Lock_CAPICOLA_3_8'); javascript:setImage('ExpandIcon_CAPICOLA_3_8','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_CAPICOLA_3_8" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_CAPICOLA_3_8" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=21&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=21&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=21&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_CAPICOLA_3_8'); javascript:leftnavExpand('Menu_off_CAPICOLA_3_8'); " onmouseover="moveObject('Event_CAPICOLA_3_8', event); setImage('ExpandIcon_CAPICOLA_3_8','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_CAPICOLA_3_8'); setImage('ExpandIcon_CAPICOLA_3_8','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_CAPICOLA_3_8',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="CAPICOLA" class="pidVerification" id="pid-21" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=21"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=21&amp;subjectId=21&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=21"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row9" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="CAPSICUM" class="pidVerification" id="pid-24" href="ViewStudySubject?id=24">CAPSICUM</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_CAPSICUM</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_CAPSICUM_1_9" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_CAPSICUM_1_9'); javascript:leftnavExpand('Menu_off_CAPSICUM_1_9'); " onmouseover="layersShowOrHide('visible','Event_CAPSICUM_1_9'); javascript:setImage('ExpandIcon_CAPSICUM_1_9','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_CAPSICUM_1_9'); layersShowOrHide('hidden','Lock_CAPSICUM_1_9'); javascript:setImage('ExpandIcon_CAPSICUM_1_9','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_CAPSICUM_1_9" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: CAPSICUM<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_CAPSICUM_1_9'); javascript:leftnavExpand('Menu_off_CAPSICUM_1_9'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_CAPSICUM_1_9'); layersShowOrHide('hidden','Lock_CAPSICUM_1_9'); javascript:setImage('ExpandIcon_CAPSICUM_1_9','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_CAPSICUM_1_9" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_CAPSICUM_1_9" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=24&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=24&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=24&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_CAPSICUM_1_9'); javascript:leftnavExpand('Menu_off_CAPSICUM_1_9'); " onmouseover="moveObject('Event_CAPSICUM_1_9', event); setImage('ExpandIcon_CAPSICUM_1_9','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_CAPSICUM_1_9'); setImage('ExpandIcon_CAPSICUM_1_9','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_CAPSICUM_1_9',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_CAPSICUM_3_9" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_CAPSICUM_3_9'); javascript:leftnavExpand('Menu_off_CAPSICUM_3_9'); " onmouseover="layersShowOrHide('visible','Event_CAPSICUM_3_9'); javascript:setImage('ExpandIcon_CAPSICUM_3_9','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_CAPSICUM_3_9'); layersShowOrHide('hidden','Lock_CAPSICUM_3_9'); javascript:setImage('ExpandIcon_CAPSICUM_3_9','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_CAPSICUM_3_9" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: CAPSICUM<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_CAPSICUM_3_9'); javascript:leftnavExpand('Menu_off_CAPSICUM_3_9'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_CAPSICUM_3_9'); layersShowOrHide('hidden','Lock_CAPSICUM_3_9'); javascript:setImage('ExpandIcon_CAPSICUM_3_9','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_CAPSICUM_3_9" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_CAPSICUM_3_9" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=24&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=24&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=24&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_CAPSICUM_3_9'); javascript:leftnavExpand('Menu_off_CAPSICUM_3_9'); " onmouseover="moveObject('Event_CAPSICUM_3_9', event); setImage('ExpandIcon_CAPSICUM_3_9','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_CAPSICUM_3_9'); setImage('ExpandIcon_CAPSICUM_3_9','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_CAPSICUM_3_9',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="CAPSICUM" class="pidVerification" id="pid-24" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=24"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=24&amp;subjectId=24&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=24"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row10" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="CHEDDAR" class="pidVerification" id="pid-3" href="ViewStudySubject?id=3">CHEDDAR</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_CHEDDAR</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_CHEDDAR_1_10" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_CHEDDAR_1_10'); javascript:leftnavExpand('Menu_off_CHEDDAR_1_10'); " onmouseover="layersShowOrHide('visible','Event_CHEDDAR_1_10'); javascript:setImage('ExpandIcon_CHEDDAR_1_10','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_CHEDDAR_1_10'); layersShowOrHide('hidden','Lock_CHEDDAR_1_10'); javascript:setImage('ExpandIcon_CHEDDAR_1_10','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_CHEDDAR_1_10" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: CHEDDAR<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_CHEDDAR_1_10'); javascript:leftnavExpand('Menu_off_CHEDDAR_1_10'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_CHEDDAR_1_10'); layersShowOrHide('hidden','Lock_CHEDDAR_1_10'); javascript:setImage('ExpandIcon_CHEDDAR_1_10','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_CHEDDAR_1_10" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_CHEDDAR_1_10" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=3&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=3&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=3&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_CHEDDAR_1_10'); javascript:leftnavExpand('Menu_off_CHEDDAR_1_10'); " onmouseover="moveObject('Event_CHEDDAR_1_10', event); setImage('ExpandIcon_CHEDDAR_1_10','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_CHEDDAR_1_10'); setImage('ExpandIcon_CHEDDAR_1_10','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_CHEDDAR_1_10',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_CHEDDAR_3_10" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_CHEDDAR_3_10'); javascript:leftnavExpand('Menu_off_CHEDDAR_3_10'); " onmouseover="layersShowOrHide('visible','Event_CHEDDAR_3_10'); javascript:setImage('ExpandIcon_CHEDDAR_3_10','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_CHEDDAR_3_10'); layersShowOrHide('hidden','Lock_CHEDDAR_3_10'); javascript:setImage('ExpandIcon_CHEDDAR_3_10','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_CHEDDAR_3_10" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: CHEDDAR<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_CHEDDAR_3_10'); javascript:leftnavExpand('Menu_off_CHEDDAR_3_10'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_CHEDDAR_3_10'); layersShowOrHide('hidden','Lock_CHEDDAR_3_10'); javascript:setImage('ExpandIcon_CHEDDAR_3_10','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_CHEDDAR_3_10" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_CHEDDAR_3_10" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=3&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=3&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=3&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_CHEDDAR_3_10'); javascript:leftnavExpand('Menu_off_CHEDDAR_3_10'); " onmouseover="moveObject('Event_CHEDDAR_3_10', event); setImage('ExpandIcon_CHEDDAR_3_10','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_CHEDDAR_3_10'); setImage('ExpandIcon_CHEDDAR_3_10','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_CHEDDAR_3_10',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="CHEDDAR" class="pidVerification" id="pid-3" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=3"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=3&amp;subjectId=3&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=3"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row11" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="GARLIC" class="pidVerification" id="pid-8" href="ViewStudySubject?id=8">GARLIC</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_GARLIC</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_GARLIC_1_11" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 659px; left: 343px; display: block;"><a href="javascript:leftnavExpand('Menu_on_GARLIC_1_11'); javascript:leftnavExpand('Menu_off_GARLIC_1_11'); " onmouseover="layersShowOrHide('visible','Event_GARLIC_1_11'); javascript:setImage('ExpandIcon_GARLIC_1_11','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_GARLIC_1_11'); layersShowOrHide('hidden','Lock_GARLIC_1_11'); javascript:setImage('ExpandIcon_GARLIC_1_11','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_GARLIC_1_11" style="position: absolute; visibility: hidden; z-index: 3; width: 180px; top: 683px; float: left; left: 357px; display: block;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: GARLIC<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_GARLIC_1_11'); javascript:leftnavExpand('Menu_off_GARLIC_1_11'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_GARLIC_1_11'); layersShowOrHide('hidden','Lock_GARLIC_1_11'); javascript:setImage('ExpandIcon_GARLIC_1_11','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_GARLIC_1_11" style="">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_GARLIC_1_11" style="display: none;">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=8&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=8&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=8&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_GARLIC_1_11'); javascript:leftnavExpand('Menu_off_GARLIC_1_11'); " onmouseover="moveObject('Event_GARLIC_1_11', event); setImage('ExpandIcon_GARLIC_1_11','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_GARLIC_1_11'); setImage('ExpandIcon_GARLIC_1_11','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_GARLIC_1_11',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_GARLIC_3_11" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_GARLIC_3_11'); javascript:leftnavExpand('Menu_off_GARLIC_3_11'); " onmouseover="layersShowOrHide('visible','Event_GARLIC_3_11'); javascript:setImage('ExpandIcon_GARLIC_3_11','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_GARLIC_3_11'); layersShowOrHide('hidden','Lock_GARLIC_3_11'); javascript:setImage('ExpandIcon_GARLIC_3_11','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_GARLIC_3_11" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: GARLIC<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_GARLIC_3_11'); javascript:leftnavExpand('Menu_off_GARLIC_3_11'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_GARLIC_3_11'); layersShowOrHide('hidden','Lock_GARLIC_3_11'); javascript:setImage('ExpandIcon_GARLIC_3_11','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_GARLIC_3_11" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_GARLIC_3_11" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=8&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=8&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=8&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_GARLIC_3_11'); javascript:leftnavExpand('Menu_off_GARLIC_3_11'); " onmouseover="moveObject('Event_GARLIC_3_11', event); setImage('ExpandIcon_GARLIC_3_11','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_GARLIC_3_11'); setImage('ExpandIcon_GARLIC_3_11','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_GARLIC_3_11',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="GARLIC" class="pidVerification" id="pid-8" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=8"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=8&amp;subjectId=8&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=8"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row12" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="HAM" class="pidVerification" id="pid-14" href="ViewStudySubject?id=14">HAM</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_HAM</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_HAM_1_12" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_HAM_1_12'); javascript:leftnavExpand('Menu_off_HAM_1_12'); " onmouseover="layersShowOrHide('visible','Event_HAM_1_12'); javascript:setImage('ExpandIcon_HAM_1_12','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_HAM_1_12'); layersShowOrHide('hidden','Lock_HAM_1_12'); javascript:setImage('ExpandIcon_HAM_1_12','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_HAM_1_12" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: HAM<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_HAM_1_12'); javascript:leftnavExpand('Menu_off_HAM_1_12'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_HAM_1_12'); layersShowOrHide('hidden','Lock_HAM_1_12'); javascript:setImage('ExpandIcon_HAM_1_12','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_HAM_1_12" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_HAM_1_12" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=14&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=14&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=14&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_HAM_1_12'); javascript:leftnavExpand('Menu_off_HAM_1_12'); " onmouseover="moveObject('Event_HAM_1_12', event); setImage('ExpandIcon_HAM_1_12','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_HAM_1_12'); setImage('ExpandIcon_HAM_1_12','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_HAM_1_12',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_HAM_3_12" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_HAM_3_12'); javascript:leftnavExpand('Menu_off_HAM_3_12'); " onmouseover="layersShowOrHide('visible','Event_HAM_3_12'); javascript:setImage('ExpandIcon_HAM_3_12','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_HAM_3_12'); layersShowOrHide('hidden','Lock_HAM_3_12'); javascript:setImage('ExpandIcon_HAM_3_12','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_HAM_3_12" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: HAM<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_HAM_3_12'); javascript:leftnavExpand('Menu_off_HAM_3_12'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_HAM_3_12'); layersShowOrHide('hidden','Lock_HAM_3_12'); javascript:setImage('ExpandIcon_HAM_3_12','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_HAM_3_12" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_HAM_3_12" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=14&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=14&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=14&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_HAM_3_12'); javascript:leftnavExpand('Menu_off_HAM_3_12'); " onmouseover="moveObject('Event_HAM_3_12', event); setImage('ExpandIcon_HAM_3_12','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_HAM_3_12'); setImage('ExpandIcon_HAM_3_12','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_HAM_3_12',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="HAM" class="pidVerification" id="pid-14" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=14"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=14&amp;subjectId=14&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=14"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row13" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JALAPENO" class="pidVerification" id="pid-16" href="ViewStudySubject?id=16">JALAPENO</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JALAPENO</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JALAPENO_1_13" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JALAPENO_1_13'); javascript:leftnavExpand('Menu_off_JALAPENO_1_13'); " onmouseover="layersShowOrHide('visible','Event_JALAPENO_1_13'); javascript:setImage('ExpandIcon_JALAPENO_1_13','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JALAPENO_1_13'); layersShowOrHide('hidden','Lock_JALAPENO_1_13'); javascript:setImage('ExpandIcon_JALAPENO_1_13','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JALAPENO_1_13" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JALAPENO<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JALAPENO_1_13'); javascript:leftnavExpand('Menu_off_JALAPENO_1_13'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JALAPENO_1_13'); layersShowOrHide('hidden','Lock_JALAPENO_1_13'); javascript:setImage('ExpandIcon_JALAPENO_1_13','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JALAPENO_1_13" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JALAPENO_1_13" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=16&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=16&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=16&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JALAPENO_1_13'); javascript:leftnavExpand('Menu_off_JALAPENO_1_13'); " onmouseover="moveObject('Event_JALAPENO_1_13', event); setImage('ExpandIcon_JALAPENO_1_13','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JALAPENO_1_13'); setImage('ExpandIcon_JALAPENO_1_13','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JALAPENO_1_13',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JALAPENO_3_13" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JALAPENO_3_13'); javascript:leftnavExpand('Menu_off_JALAPENO_3_13'); " onmouseover="layersShowOrHide('visible','Event_JALAPENO_3_13'); javascript:setImage('ExpandIcon_JALAPENO_3_13','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JALAPENO_3_13'); layersShowOrHide('hidden','Lock_JALAPENO_3_13'); javascript:setImage('ExpandIcon_JALAPENO_3_13','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JALAPENO_3_13" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JALAPENO<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JALAPENO_3_13'); javascript:leftnavExpand('Menu_off_JALAPENO_3_13'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JALAPENO_3_13'); layersShowOrHide('hidden','Lock_JALAPENO_3_13'); javascript:setImage('ExpandIcon_JALAPENO_3_13','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JALAPENO_3_13" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JALAPENO_3_13" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=16&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=16&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=16&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JALAPENO_3_13'); javascript:leftnavExpand('Menu_off_JALAPENO_3_13'); " onmouseover="moveObject('Event_JALAPENO_3_13', event); setImage('ExpandIcon_JALAPENO_3_13','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JALAPENO_3_13'); setImage('ExpandIcon_JALAPENO_3_13','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JALAPENO_3_13',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JALAPENO" class="pidVerification" id="pid-16" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=16"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=16&amp;subjectId=16&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=16"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row14" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JANAA" class="pidVerification" id="pid-37" href="ViewStudySubject?id=37">JANAA</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JANAA</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JANAA_1_14" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JANAA_1_14'); javascript:leftnavExpand('Menu_off_JANAA_1_14'); " onmouseover="layersShowOrHide('visible','Event_JANAA_1_14'); javascript:setImage('ExpandIcon_JANAA_1_14','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JANAA_1_14'); layersShowOrHide('hidden','Lock_JANAA_1_14'); javascript:setImage('ExpandIcon_JANAA_1_14','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JANAA_1_14" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JANAA<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JANAA_1_14'); javascript:leftnavExpand('Menu_off_JANAA_1_14'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JANAA_1_14'); layersShowOrHide('hidden','Lock_JANAA_1_14'); javascript:setImage('ExpandIcon_JANAA_1_14','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JANAA_1_14" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JANAA_1_14" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=37&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=37&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=37&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JANAA_1_14'); javascript:leftnavExpand('Menu_off_JANAA_1_14'); " onmouseover="moveObject('Event_JANAA_1_14', event); setImage('ExpandIcon_JANAA_1_14','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JANAA_1_14'); setImage('ExpandIcon_JANAA_1_14','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JANAA_1_14',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JANAA_3_14" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JANAA_3_14'); javascript:leftnavExpand('Menu_off_JANAA_3_14'); " onmouseover="layersShowOrHide('visible','Event_JANAA_3_14'); javascript:setImage('ExpandIcon_JANAA_3_14','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JANAA_3_14'); layersShowOrHide('hidden','Lock_JANAA_3_14'); javascript:setImage('ExpandIcon_JANAA_3_14','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JANAA_3_14" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JANAA<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JANAA_3_14'); javascript:leftnavExpand('Menu_off_JANAA_3_14'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JANAA_3_14'); layersShowOrHide('hidden','Lock_JANAA_3_14'); javascript:setImage('ExpandIcon_JANAA_3_14','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JANAA_3_14" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JANAA_3_14" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=37&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=37&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=37&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JANAA_3_14'); javascript:leftnavExpand('Menu_off_JANAA_3_14'); " onmouseover="moveObject('Event_JANAA_3_14', event); setImage('ExpandIcon_JANAA_3_14','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JANAA_3_14'); setImage('ExpandIcon_JANAA_3_14','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JANAA_3_14',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JANAA" class="pidVerification" id="pid-37" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=37"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=37&amp;subjectId=37&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=37"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row15" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JASON" class="pidVerification" id="pid-26" href="ViewStudySubject?id=26">JASON</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASON</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASON_1_15" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASON_1_15'); javascript:leftnavExpand('Menu_off_JASON_1_15'); " onmouseover="layersShowOrHide('visible','Event_JASON_1_15'); javascript:setImage('ExpandIcon_JASON_1_15','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASON_1_15'); layersShowOrHide('hidden','Lock_JASON_1_15'); javascript:setImage('ExpandIcon_JASON_1_15','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASON_1_15" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASON<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASON_1_15'); javascript:leftnavExpand('Menu_off_JASON_1_15'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASON_1_15'); layersShowOrHide('hidden','Lock_JASON_1_15'); javascript:setImage('ExpandIcon_JASON_1_15','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASON_1_15" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASON_1_15" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=26&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=26&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=26&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASON_1_15'); javascript:leftnavExpand('Menu_off_JASON_1_15'); " onmouseover="moveObject('Event_JASON_1_15', event); setImage('ExpandIcon_JASON_1_15','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASON_1_15'); setImage('ExpandIcon_JASON_1_15','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASON_1_15',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASON_3_15" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASON_3_15'); javascript:leftnavExpand('Menu_off_JASON_3_15'); " onmouseover="layersShowOrHide('visible','Event_JASON_3_15'); javascript:setImage('ExpandIcon_JASON_3_15','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASON_3_15'); layersShowOrHide('hidden','Lock_JASON_3_15'); javascript:setImage('ExpandIcon_JASON_3_15','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASON_3_15" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASON<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASON_3_15'); javascript:leftnavExpand('Menu_off_JASON_3_15'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASON_3_15'); layersShowOrHide('hidden','Lock_JASON_3_15'); javascript:setImage('ExpandIcon_JASON_3_15','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASON_3_15" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASON_3_15" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=26&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=26&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=26&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASON_3_15'); javascript:leftnavExpand('Menu_off_JASON_3_15'); " onmouseover="moveObject('Event_JASON_3_15', event); setImage('ExpandIcon_JASON_3_15','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASON_3_15'); setImage('ExpandIcon_JASON_3_15','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASON_3_15',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASON" class="pidVerification" id="pid-26" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=26"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=26&amp;subjectId=26&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=26"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row16" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JASON2" class="pidVerification" id="pid-28" href="ViewStudySubject?id=28">JASON2</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASON2</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASON2_1_16" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASON2_1_16'); javascript:leftnavExpand('Menu_off_JASON2_1_16'); " onmouseover="layersShowOrHide('visible','Event_JASON2_1_16'); javascript:setImage('ExpandIcon_JASON2_1_16','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASON2_1_16'); layersShowOrHide('hidden','Lock_JASON2_1_16'); javascript:setImage('ExpandIcon_JASON2_1_16','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASON2_1_16" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASON2<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASON2_1_16'); javascript:leftnavExpand('Menu_off_JASON2_1_16'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASON2_1_16'); layersShowOrHide('hidden','Lock_JASON2_1_16'); javascript:setImage('ExpandIcon_JASON2_1_16','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASON2_1_16" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASON2_1_16" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=28&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=28&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=28&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASON2_1_16'); javascript:leftnavExpand('Menu_off_JASON2_1_16'); " onmouseover="moveObject('Event_JASON2_1_16', event); setImage('ExpandIcon_JASON2_1_16','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASON2_1_16'); setImage('ExpandIcon_JASON2_1_16','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASON2_1_16',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASON2_3_16" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASON2_3_16'); javascript:leftnavExpand('Menu_off_JASON2_3_16'); " onmouseover="layersShowOrHide('visible','Event_JASON2_3_16'); javascript:setImage('ExpandIcon_JASON2_3_16','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASON2_3_16'); layersShowOrHide('hidden','Lock_JASON2_3_16'); javascript:setImage('ExpandIcon_JASON2_3_16','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASON2_3_16" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASON2<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASON2_3_16'); javascript:leftnavExpand('Menu_off_JASON2_3_16'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASON2_3_16'); layersShowOrHide('hidden','Lock_JASON2_3_16'); javascript:setImage('ExpandIcon_JASON2_3_16','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASON2_3_16" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASON2_3_16" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=28&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=28&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=28&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASON2_3_16'); javascript:leftnavExpand('Menu_off_JASON2_3_16'); " onmouseover="moveObject('Event_JASON2_3_16', event); setImage('ExpandIcon_JASON2_3_16','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASON2_3_16'); setImage('ExpandIcon_JASON2_3_16','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASON2_3_16',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASON2" class="pidVerification" id="pid-28" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=28"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=28&amp;subjectId=28&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=28"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row17" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JASON3" class="pidVerification" id="pid-30" href="ViewStudySubject?id=30">JASON3</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASON3</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASON3_1_17" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASON3_1_17'); javascript:leftnavExpand('Menu_off_JASON3_1_17'); " onmouseover="layersShowOrHide('visible','Event_JASON3_1_17'); javascript:setImage('ExpandIcon_JASON3_1_17','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASON3_1_17'); layersShowOrHide('hidden','Lock_JASON3_1_17'); javascript:setImage('ExpandIcon_JASON3_1_17','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASON3_1_17" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASON3<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASON3_1_17'); javascript:leftnavExpand('Menu_off_JASON3_1_17'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASON3_1_17'); layersShowOrHide('hidden','Lock_JASON3_1_17'); javascript:setImage('ExpandIcon_JASON3_1_17','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASON3_1_17" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASON3_1_17" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=30&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=30&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=30&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASON3_1_17'); javascript:leftnavExpand('Menu_off_JASON3_1_17'); " onmouseover="moveObject('Event_JASON3_1_17', event); setImage('ExpandIcon_JASON3_1_17','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASON3_1_17'); setImage('ExpandIcon_JASON3_1_17','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASON3_1_17',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASON3_3_17" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASON3_3_17'); javascript:leftnavExpand('Menu_off_JASON3_3_17'); " onmouseover="layersShowOrHide('visible','Event_JASON3_3_17'); javascript:setImage('ExpandIcon_JASON3_3_17','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASON3_3_17'); layersShowOrHide('hidden','Lock_JASON3_3_17'); javascript:setImage('ExpandIcon_JASON3_3_17','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASON3_3_17" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASON3<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASON3_3_17'); javascript:leftnavExpand('Menu_off_JASON3_3_17'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASON3_3_17'); layersShowOrHide('hidden','Lock_JASON3_3_17'); javascript:setImage('ExpandIcon_JASON3_3_17','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASON3_3_17" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASON3_3_17" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=30&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=30&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=30&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASON3_3_17'); javascript:leftnavExpand('Menu_off_JASON3_3_17'); " onmouseover="moveObject('Event_JASON3_3_17', event); setImage('ExpandIcon_JASON3_3_17','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASON3_3_17'); setImage('ExpandIcon_JASON3_3_17','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASON3_3_17',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASON3" class="pidVerification" id="pid-30" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=30"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=30&amp;subjectId=30&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=30"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row18" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JASONA" class="pidVerification" id="pid-32" href="ViewStudySubject?id=32">JASONA</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONA</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JASONA_1_18" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONA_1_18'); javascript:leftnavExpand('Menu_off_JASONA_1_18'); " onmouseover="layersShowOrHide('visible','Event_JASONA_1_18'); javascript:setImage('ExpandIcon_JASONA_1_18','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONA_1_18'); layersShowOrHide('hidden','Lock_JASONA_1_18'); javascript:setImage('ExpandIcon_JASONA_1_18','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONA_1_18" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONA<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONA_1_18'); javascript:leftnavExpand('Menu_off_JASONA_1_18'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONA_1_18'); layersShowOrHide('hidden','Lock_JASONA_1_18'); javascript:setImage('ExpandIcon_JASONA_1_18','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONA_1_18" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONA_1_18" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=32&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=32&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=32&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONA_1_18'); javascript:leftnavExpand('Menu_off_JASONA_1_18'); " onmouseover="moveObject('Event_JASONA_1_18', event); setImage('ExpandIcon_JASONA_1_18','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONA_1_18'); setImage('ExpandIcon_JASONA_1_18','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONA_1_18',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONA_3_18" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONA_3_18'); javascript:leftnavExpand('Menu_off_JASONA_3_18'); " onmouseover="layersShowOrHide('visible','Event_JASONA_3_18'); javascript:setImage('ExpandIcon_JASONA_3_18','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONA_3_18'); layersShowOrHide('hidden','Lock_JASONA_3_18'); javascript:setImage('ExpandIcon_JASONA_3_18','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONA_3_18" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONA<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONA_3_18'); javascript:leftnavExpand('Menu_off_JASONA_3_18'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONA_3_18'); layersShowOrHide('hidden','Lock_JASONA_3_18'); javascript:setImage('ExpandIcon_JASONA_3_18','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONA_3_18" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONA_3_18" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=32&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=32&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=32&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONA_3_18'); javascript:leftnavExpand('Menu_off_JASONA_3_18'); " onmouseover="moveObject('Event_JASONA_3_18', event); setImage('ExpandIcon_JASONA_3_18','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONA_3_18'); setImage('ExpandIcon_JASONA_3_18','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONA_3_18',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONA" class="pidVerification" id="pid-32" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=32"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=32&amp;subjectId=32&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=32"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row19" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JASONB" class="pidVerification" id="pid-42" href="ViewStudySubject?id=42">JASONB</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONB</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JASONB_1_19" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONB_1_19'); javascript:leftnavExpand('Menu_off_JASONB_1_19'); " onmouseover="layersShowOrHide('visible','Event_JASONB_1_19'); javascript:setImage('ExpandIcon_JASONB_1_19','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONB_1_19'); layersShowOrHide('hidden','Lock_JASONB_1_19'); javascript:setImage('ExpandIcon_JASONB_1_19','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONB_1_19" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONB<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONB_1_19'); javascript:leftnavExpand('Menu_off_JASONB_1_19'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONB_1_19'); layersShowOrHide('hidden','Lock_JASONB_1_19'); javascript:setImage('ExpandIcon_JASONB_1_19','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONB_1_19" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONB_1_19" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=42&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=42&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=42&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONB_1_19'); javascript:leftnavExpand('Menu_off_JASONB_1_19'); " onmouseover="moveObject('Event_JASONB_1_19', event); setImage('ExpandIcon_JASONB_1_19','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONB_1_19'); setImage('ExpandIcon_JASONB_1_19','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONB_1_19',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONB_3_19" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONB_3_19'); javascript:leftnavExpand('Menu_off_JASONB_3_19'); " onmouseover="layersShowOrHide('visible','Event_JASONB_3_19'); javascript:setImage('ExpandIcon_JASONB_3_19','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONB_3_19'); layersShowOrHide('hidden','Lock_JASONB_3_19'); javascript:setImage('ExpandIcon_JASONB_3_19','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONB_3_19" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONB<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONB_3_19'); javascript:leftnavExpand('Menu_off_JASONB_3_19'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONB_3_19'); layersShowOrHide('hidden','Lock_JASONB_3_19'); javascript:setImage('ExpandIcon_JASONB_3_19','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONB_3_19" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONB_3_19" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=42&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=42&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=42&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONB_3_19'); javascript:leftnavExpand('Menu_off_JASONB_3_19'); " onmouseover="moveObject('Event_JASONB_3_19', event); setImage('ExpandIcon_JASONB_3_19','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONB_3_19'); setImage('ExpandIcon_JASONB_3_19','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONB_3_19',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONB" class="pidVerification" id="pid-42" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=42"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=42&amp;subjectId=42&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=42"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row20" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JASONC" class="pidVerification" id="pid-43" href="ViewStudySubject?id=43">JASONC</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONC</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASONC_1_20" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONC_1_20'); javascript:leftnavExpand('Menu_off_JASONC_1_20'); " onmouseover="layersShowOrHide('visible','Event_JASONC_1_20'); javascript:setImage('ExpandIcon_JASONC_1_20','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONC_1_20'); layersShowOrHide('hidden','Lock_JASONC_1_20'); javascript:setImage('ExpandIcon_JASONC_1_20','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONC_1_20" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONC<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONC_1_20'); javascript:leftnavExpand('Menu_off_JASONC_1_20'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONC_1_20'); layersShowOrHide('hidden','Lock_JASONC_1_20'); javascript:setImage('ExpandIcon_JASONC_1_20','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONC_1_20" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONC_1_20" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=43&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=43&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=43&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONC_1_20'); javascript:leftnavExpand('Menu_off_JASONC_1_20'); " onmouseover="moveObject('Event_JASONC_1_20', event); setImage('ExpandIcon_JASONC_1_20','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONC_1_20'); setImage('ExpandIcon_JASONC_1_20','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONC_1_20',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONC_3_20" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONC_3_20'); javascript:leftnavExpand('Menu_off_JASONC_3_20'); " onmouseover="layersShowOrHide('visible','Event_JASONC_3_20'); javascript:setImage('ExpandIcon_JASONC_3_20','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONC_3_20'); layersShowOrHide('hidden','Lock_JASONC_3_20'); javascript:setImage('ExpandIcon_JASONC_3_20','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONC_3_20" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONC<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONC_3_20'); javascript:leftnavExpand('Menu_off_JASONC_3_20'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONC_3_20'); layersShowOrHide('hidden','Lock_JASONC_3_20'); javascript:setImage('ExpandIcon_JASONC_3_20','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONC_3_20" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONC_3_20" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=43&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=43&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=43&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONC_3_20'); javascript:leftnavExpand('Menu_off_JASONC_3_20'); " onmouseover="moveObject('Event_JASONC_3_20', event); setImage('ExpandIcon_JASONC_3_20','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONC_3_20'); setImage('ExpandIcon_JASONC_3_20','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONC_3_20',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONC" class="pidVerification" id="pid-43" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=43"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=43&amp;subjectId=43&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=43"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row21" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JASOND" class="pidVerification" id="pid-44" href="ViewStudySubject?id=44">JASOND</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASOND</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JASOND_1_21" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASOND_1_21'); javascript:leftnavExpand('Menu_off_JASOND_1_21'); " onmouseover="layersShowOrHide('visible','Event_JASOND_1_21'); javascript:setImage('ExpandIcon_JASOND_1_21','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASOND_1_21'); layersShowOrHide('hidden','Lock_JASOND_1_21'); javascript:setImage('ExpandIcon_JASOND_1_21','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASOND_1_21" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASOND<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASOND_1_21'); javascript:leftnavExpand('Menu_off_JASOND_1_21'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASOND_1_21'); layersShowOrHide('hidden','Lock_JASOND_1_21'); javascript:setImage('ExpandIcon_JASOND_1_21','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASOND_1_21" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASOND_1_21" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=44&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=44&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=44&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASOND_1_21'); javascript:leftnavExpand('Menu_off_JASOND_1_21'); " onmouseover="moveObject('Event_JASOND_1_21', event); setImage('ExpandIcon_JASOND_1_21','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASOND_1_21'); setImage('ExpandIcon_JASOND_1_21','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASOND_1_21',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASOND_3_21" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASOND_3_21'); javascript:leftnavExpand('Menu_off_JASOND_3_21'); " onmouseover="layersShowOrHide('visible','Event_JASOND_3_21'); javascript:setImage('ExpandIcon_JASOND_3_21','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASOND_3_21'); layersShowOrHide('hidden','Lock_JASOND_3_21'); javascript:setImage('ExpandIcon_JASOND_3_21','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASOND_3_21" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASOND<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASOND_3_21'); javascript:leftnavExpand('Menu_off_JASOND_3_21'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASOND_3_21'); layersShowOrHide('hidden','Lock_JASOND_3_21'); javascript:setImage('ExpandIcon_JASOND_3_21','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASOND_3_21" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASOND_3_21" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=44&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=44&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=44&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASOND_3_21'); javascript:leftnavExpand('Menu_off_JASOND_3_21'); " onmouseover="moveObject('Event_JASOND_3_21', event); setImage('ExpandIcon_JASOND_3_21','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASOND_3_21'); setImage('ExpandIcon_JASOND_3_21','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASOND_3_21',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASOND" class="pidVerification" id="pid-44" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=44"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=44&amp;subjectId=44&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=44"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row22" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JASONE" class="pidVerification" id="pid-45" href="ViewStudySubject?id=45">JASONE</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONE</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JASONE_1_22" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONE_1_22'); javascript:leftnavExpand('Menu_off_JASONE_1_22'); " onmouseover="layersShowOrHide('visible','Event_JASONE_1_22'); javascript:setImage('ExpandIcon_JASONE_1_22','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONE_1_22'); layersShowOrHide('hidden','Lock_JASONE_1_22'); javascript:setImage('ExpandIcon_JASONE_1_22','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONE_1_22" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONE<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONE_1_22'); javascript:leftnavExpand('Menu_off_JASONE_1_22'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONE_1_22'); layersShowOrHide('hidden','Lock_JASONE_1_22'); javascript:setImage('ExpandIcon_JASONE_1_22','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONE_1_22" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONE_1_22" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=45&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=45&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=45&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONE_1_22'); javascript:leftnavExpand('Menu_off_JASONE_1_22'); " onmouseover="moveObject('Event_JASONE_1_22', event); setImage('ExpandIcon_JASONE_1_22','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONE_1_22'); setImage('ExpandIcon_JASONE_1_22','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONE_1_22',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONE_3_22" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONE_3_22'); javascript:leftnavExpand('Menu_off_JASONE_3_22'); " onmouseover="layersShowOrHide('visible','Event_JASONE_3_22'); javascript:setImage('ExpandIcon_JASONE_3_22','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONE_3_22'); layersShowOrHide('hidden','Lock_JASONE_3_22'); javascript:setImage('ExpandIcon_JASONE_3_22','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONE_3_22" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONE<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONE_3_22'); javascript:leftnavExpand('Menu_off_JASONE_3_22'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONE_3_22'); layersShowOrHide('hidden','Lock_JASONE_3_22'); javascript:setImage('ExpandIcon_JASONE_3_22','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONE_3_22" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONE_3_22" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=45&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=45&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=45&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONE_3_22'); javascript:leftnavExpand('Menu_off_JASONE_3_22'); " onmouseover="moveObject('Event_JASONE_3_22', event); setImage('ExpandIcon_JASONE_3_22','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONE_3_22'); setImage('ExpandIcon_JASONE_3_22','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONE_3_22',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONE" class="pidVerification" id="pid-45" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=45"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=45&amp;subjectId=45&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=45"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row23" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JASONF" class="pidVerification" id="pid-46" href="ViewStudySubject?id=46">JASONF</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONF</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASONF_1_23" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONF_1_23'); javascript:leftnavExpand('Menu_off_JASONF_1_23'); " onmouseover="layersShowOrHide('visible','Event_JASONF_1_23'); javascript:setImage('ExpandIcon_JASONF_1_23','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONF_1_23'); layersShowOrHide('hidden','Lock_JASONF_1_23'); javascript:setImage('ExpandIcon_JASONF_1_23','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONF_1_23" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONF<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONF_1_23'); javascript:leftnavExpand('Menu_off_JASONF_1_23'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONF_1_23'); layersShowOrHide('hidden','Lock_JASONF_1_23'); javascript:setImage('ExpandIcon_JASONF_1_23','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONF_1_23" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONF_1_23" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=46&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=46&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=46&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONF_1_23'); javascript:leftnavExpand('Menu_off_JASONF_1_23'); " onmouseover="moveObject('Event_JASONF_1_23', event); setImage('ExpandIcon_JASONF_1_23','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONF_1_23'); setImage('ExpandIcon_JASONF_1_23','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONF_1_23',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONF_3_23" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONF_3_23'); javascript:leftnavExpand('Menu_off_JASONF_3_23'); " onmouseover="layersShowOrHide('visible','Event_JASONF_3_23'); javascript:setImage('ExpandIcon_JASONF_3_23','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONF_3_23'); layersShowOrHide('hidden','Lock_JASONF_3_23'); javascript:setImage('ExpandIcon_JASONF_3_23','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONF_3_23" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONF<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONF_3_23'); javascript:leftnavExpand('Menu_off_JASONF_3_23'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONF_3_23'); layersShowOrHide('hidden','Lock_JASONF_3_23'); javascript:setImage('ExpandIcon_JASONF_3_23','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONF_3_23" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONF_3_23" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=46&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=46&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=46&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONF_3_23'); javascript:leftnavExpand('Menu_off_JASONF_3_23'); " onmouseover="moveObject('Event_JASONF_3_23', event); setImage('ExpandIcon_JASONF_3_23','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONF_3_23'); setImage('ExpandIcon_JASONF_3_23','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONF_3_23',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONF" class="pidVerification" id="pid-46" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=46"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=46&amp;subjectId=46&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=46"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row24" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JASONG" class="pidVerification" id="pid-47" href="ViewStudySubject?id=47">JASONG</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONG</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASONG_1_24" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONG_1_24'); javascript:leftnavExpand('Menu_off_JASONG_1_24'); " onmouseover="layersShowOrHide('visible','Event_JASONG_1_24'); javascript:setImage('ExpandIcon_JASONG_1_24','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONG_1_24'); layersShowOrHide('hidden','Lock_JASONG_1_24'); javascript:setImage('ExpandIcon_JASONG_1_24','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONG_1_24" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONG<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONG_1_24'); javascript:leftnavExpand('Menu_off_JASONG_1_24'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONG_1_24'); layersShowOrHide('hidden','Lock_JASONG_1_24'); javascript:setImage('ExpandIcon_JASONG_1_24','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONG_1_24" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONG_1_24" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=47&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=47&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=47&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONG_1_24'); javascript:leftnavExpand('Menu_off_JASONG_1_24'); " onmouseover="moveObject('Event_JASONG_1_24', event); setImage('ExpandIcon_JASONG_1_24','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONG_1_24'); setImage('ExpandIcon_JASONG_1_24','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONG_1_24',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONG_3_24" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONG_3_24'); javascript:leftnavExpand('Menu_off_JASONG_3_24'); " onmouseover="layersShowOrHide('visible','Event_JASONG_3_24'); javascript:setImage('ExpandIcon_JASONG_3_24','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONG_3_24'); layersShowOrHide('hidden','Lock_JASONG_3_24'); javascript:setImage('ExpandIcon_JASONG_3_24','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONG_3_24" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONG<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONG_3_24'); javascript:leftnavExpand('Menu_off_JASONG_3_24'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONG_3_24'); layersShowOrHide('hidden','Lock_JASONG_3_24'); javascript:setImage('ExpandIcon_JASONG_3_24','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONG_3_24" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONG_3_24" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=47&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=47&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=47&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONG_3_24'); javascript:leftnavExpand('Menu_off_JASONG_3_24'); " onmouseover="moveObject('Event_JASONG_3_24', event); setImage('ExpandIcon_JASONG_3_24','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONG_3_24'); setImage('ExpandIcon_JASONG_3_24','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONG_3_24',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONG" class="pidVerification" id="pid-47" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=47"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=47&amp;subjectId=47&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=47"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row25" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JASONH" class="pidVerification" id="pid-48" href="ViewStudySubject?id=48">JASONH</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONH</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASONH_1_25" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONH_1_25'); javascript:leftnavExpand('Menu_off_JASONH_1_25'); " onmouseover="layersShowOrHide('visible','Event_JASONH_1_25'); javascript:setImage('ExpandIcon_JASONH_1_25','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONH_1_25'); layersShowOrHide('hidden','Lock_JASONH_1_25'); javascript:setImage('ExpandIcon_JASONH_1_25','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONH_1_25" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONH<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONH_1_25'); javascript:leftnavExpand('Menu_off_JASONH_1_25'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONH_1_25'); layersShowOrHide('hidden','Lock_JASONH_1_25'); javascript:setImage('ExpandIcon_JASONH_1_25','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONH_1_25" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONH_1_25" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=48&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=48&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=48&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONH_1_25'); javascript:leftnavExpand('Menu_off_JASONH_1_25'); " onmouseover="moveObject('Event_JASONH_1_25', event); setImage('ExpandIcon_JASONH_1_25','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONH_1_25'); setImage('ExpandIcon_JASONH_1_25','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONH_1_25',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONH_3_25" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONH_3_25'); javascript:leftnavExpand('Menu_off_JASONH_3_25'); " onmouseover="layersShowOrHide('visible','Event_JASONH_3_25'); javascript:setImage('ExpandIcon_JASONH_3_25','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONH_3_25'); layersShowOrHide('hidden','Lock_JASONH_3_25'); javascript:setImage('ExpandIcon_JASONH_3_25','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONH_3_25" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONH<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONH_3_25'); javascript:leftnavExpand('Menu_off_JASONH_3_25'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONH_3_25'); layersShowOrHide('hidden','Lock_JASONH_3_25'); javascript:setImage('ExpandIcon_JASONH_3_25','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONH_3_25" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONH_3_25" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=48&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=48&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=48&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONH_3_25'); javascript:leftnavExpand('Menu_off_JASONH_3_25'); " onmouseover="moveObject('Event_JASONH_3_25', event); setImage('ExpandIcon_JASONH_3_25','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONH_3_25'); setImage('ExpandIcon_JASONH_3_25','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONH_3_25',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONH" class="pidVerification" id="pid-48" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=48"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=48&amp;subjectId=48&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=48"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row26" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JASONI" class="pidVerification" id="pid-49" href="ViewStudySubject?id=49">JASONI</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONI</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASONI_1_26" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONI_1_26'); javascript:leftnavExpand('Menu_off_JASONI_1_26'); " onmouseover="layersShowOrHide('visible','Event_JASONI_1_26'); javascript:setImage('ExpandIcon_JASONI_1_26','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONI_1_26'); layersShowOrHide('hidden','Lock_JASONI_1_26'); javascript:setImage('ExpandIcon_JASONI_1_26','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONI_1_26" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONI<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONI_1_26'); javascript:leftnavExpand('Menu_off_JASONI_1_26'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONI_1_26'); layersShowOrHide('hidden','Lock_JASONI_1_26'); javascript:setImage('ExpandIcon_JASONI_1_26','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONI_1_26" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONI_1_26" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=49&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=49&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=49&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONI_1_26'); javascript:leftnavExpand('Menu_off_JASONI_1_26'); " onmouseover="moveObject('Event_JASONI_1_26', event); setImage('ExpandIcon_JASONI_1_26','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONI_1_26'); setImage('ExpandIcon_JASONI_1_26','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONI_1_26',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONI_3_26" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONI_3_26'); javascript:leftnavExpand('Menu_off_JASONI_3_26'); " onmouseover="layersShowOrHide('visible','Event_JASONI_3_26'); javascript:setImage('ExpandIcon_JASONI_3_26','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONI_3_26'); layersShowOrHide('hidden','Lock_JASONI_3_26'); javascript:setImage('ExpandIcon_JASONI_3_26','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONI_3_26" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONI<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONI_3_26'); javascript:leftnavExpand('Menu_off_JASONI_3_26'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONI_3_26'); layersShowOrHide('hidden','Lock_JASONI_3_26'); javascript:setImage('ExpandIcon_JASONI_3_26','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONI_3_26" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONI_3_26" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=49&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=49&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=49&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONI_3_26'); javascript:leftnavExpand('Menu_off_JASONI_3_26'); " onmouseover="moveObject('Event_JASONI_3_26', event); setImage('ExpandIcon_JASONI_3_26','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONI_3_26'); setImage('ExpandIcon_JASONI_3_26','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONI_3_26',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONI" class="pidVerification" id="pid-49" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=49"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=49&amp;subjectId=49&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=49"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row27" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JASONJ" class="pidVerification" id="pid-50" href="ViewStudySubject?id=50">JASONJ</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONJ</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASONJ_1_27" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONJ_1_27'); javascript:leftnavExpand('Menu_off_JASONJ_1_27'); " onmouseover="layersShowOrHide('visible','Event_JASONJ_1_27'); javascript:setImage('ExpandIcon_JASONJ_1_27','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONJ_1_27'); layersShowOrHide('hidden','Lock_JASONJ_1_27'); javascript:setImage('ExpandIcon_JASONJ_1_27','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONJ_1_27" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONJ<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONJ_1_27'); javascript:leftnavExpand('Menu_off_JASONJ_1_27'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONJ_1_27'); layersShowOrHide('hidden','Lock_JASONJ_1_27'); javascript:setImage('ExpandIcon_JASONJ_1_27','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONJ_1_27" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONJ_1_27" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=50&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=50&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=50&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONJ_1_27'); javascript:leftnavExpand('Menu_off_JASONJ_1_27'); " onmouseover="moveObject('Event_JASONJ_1_27', event); setImage('ExpandIcon_JASONJ_1_27','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONJ_1_27'); setImage('ExpandIcon_JASONJ_1_27','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONJ_1_27',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONJ_3_27" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONJ_3_27'); javascript:leftnavExpand('Menu_off_JASONJ_3_27'); " onmouseover="layersShowOrHide('visible','Event_JASONJ_3_27'); javascript:setImage('ExpandIcon_JASONJ_3_27','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONJ_3_27'); layersShowOrHide('hidden','Lock_JASONJ_3_27'); javascript:setImage('ExpandIcon_JASONJ_3_27','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONJ_3_27" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONJ<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONJ_3_27'); javascript:leftnavExpand('Menu_off_JASONJ_3_27'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONJ_3_27'); layersShowOrHide('hidden','Lock_JASONJ_3_27'); javascript:setImage('ExpandIcon_JASONJ_3_27','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONJ_3_27" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONJ_3_27" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=50&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=50&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=50&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONJ_3_27'); javascript:leftnavExpand('Menu_off_JASONJ_3_27'); " onmouseover="moveObject('Event_JASONJ_3_27', event); setImage('ExpandIcon_JASONJ_3_27','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONJ_3_27'); setImage('ExpandIcon_JASONJ_3_27','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONJ_3_27',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONJ" class="pidVerification" id="pid-50" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=50"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=50&amp;subjectId=50&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=50"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row28" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JASONK" class="pidVerification" id="pid-51" href="ViewStudySubject?id=51">JASONK</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONK</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASONK_1_28" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONK_1_28'); javascript:leftnavExpand('Menu_off_JASONK_1_28'); " onmouseover="layersShowOrHide('visible','Event_JASONK_1_28'); javascript:setImage('ExpandIcon_JASONK_1_28','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONK_1_28'); layersShowOrHide('hidden','Lock_JASONK_1_28'); javascript:setImage('ExpandIcon_JASONK_1_28','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONK_1_28" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONK<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONK_1_28'); javascript:leftnavExpand('Menu_off_JASONK_1_28'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONK_1_28'); layersShowOrHide('hidden','Lock_JASONK_1_28'); javascript:setImage('ExpandIcon_JASONK_1_28','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONK_1_28" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONK_1_28" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=51&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=51&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=51&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONK_1_28'); javascript:leftnavExpand('Menu_off_JASONK_1_28'); " onmouseover="moveObject('Event_JASONK_1_28', event); setImage('ExpandIcon_JASONK_1_28','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONK_1_28'); setImage('ExpandIcon_JASONK_1_28','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONK_1_28',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONK_3_28" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONK_3_28'); javascript:leftnavExpand('Menu_off_JASONK_3_28'); " onmouseover="layersShowOrHide('visible','Event_JASONK_3_28'); javascript:setImage('ExpandIcon_JASONK_3_28','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONK_3_28'); layersShowOrHide('hidden','Lock_JASONK_3_28'); javascript:setImage('ExpandIcon_JASONK_3_28','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONK_3_28" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONK<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONK_3_28'); javascript:leftnavExpand('Menu_off_JASONK_3_28'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONK_3_28'); layersShowOrHide('hidden','Lock_JASONK_3_28'); javascript:setImage('ExpandIcon_JASONK_3_28','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONK_3_28" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONK_3_28" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=51&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=51&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=51&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONK_3_28'); javascript:leftnavExpand('Menu_off_JASONK_3_28'); " onmouseover="moveObject('Event_JASONK_3_28', event); setImage('ExpandIcon_JASONK_3_28','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONK_3_28'); setImage('ExpandIcon_JASONK_3_28','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONK_3_28',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONK" class="pidVerification" id="pid-51" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=51"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=51&amp;subjectId=51&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=51"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row29" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JASONL" class="pidVerification" id="pid-52" href="ViewStudySubject?id=52">JASONL</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONL</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JASONL_1_29" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONL_1_29'); javascript:leftnavExpand('Menu_off_JASONL_1_29'); " onmouseover="layersShowOrHide('visible','Event_JASONL_1_29'); javascript:setImage('ExpandIcon_JASONL_1_29','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONL_1_29'); layersShowOrHide('hidden','Lock_JASONL_1_29'); javascript:setImage('ExpandIcon_JASONL_1_29','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONL_1_29" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONL<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONL_1_29'); javascript:leftnavExpand('Menu_off_JASONL_1_29'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONL_1_29'); layersShowOrHide('hidden','Lock_JASONL_1_29'); javascript:setImage('ExpandIcon_JASONL_1_29','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONL_1_29" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONL_1_29" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=52&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=52&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=52&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONL_1_29'); javascript:leftnavExpand('Menu_off_JASONL_1_29'); " onmouseover="moveObject('Event_JASONL_1_29', event); setImage('ExpandIcon_JASONL_1_29','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONL_1_29'); setImage('ExpandIcon_JASONL_1_29','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONL_1_29',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONL_3_29" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONL_3_29'); javascript:leftnavExpand('Menu_off_JASONL_3_29'); " onmouseover="layersShowOrHide('visible','Event_JASONL_3_29'); javascript:setImage('ExpandIcon_JASONL_3_29','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONL_3_29'); layersShowOrHide('hidden','Lock_JASONL_3_29'); javascript:setImage('ExpandIcon_JASONL_3_29','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONL_3_29" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONL<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONL_3_29'); javascript:leftnavExpand('Menu_off_JASONL_3_29'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONL_3_29'); layersShowOrHide('hidden','Lock_JASONL_3_29'); javascript:setImage('ExpandIcon_JASONL_3_29','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONL_3_29" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONL_3_29" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=52&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=52&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=52&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONL_3_29'); javascript:leftnavExpand('Menu_off_JASONL_3_29'); " onmouseover="moveObject('Event_JASONL_3_29', event); setImage('ExpandIcon_JASONL_3_29','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONL_3_29'); setImage('ExpandIcon_JASONL_3_29','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONL_3_29',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONL" class="pidVerification" id="pid-52" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=52"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=52&amp;subjectId=52&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=52"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row30" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JASONM" class="pidVerification" id="pid-53" href="ViewStudySubject?id=53">JASONM</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONM</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASONM_1_30" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONM_1_30'); javascript:leftnavExpand('Menu_off_JASONM_1_30'); " onmouseover="layersShowOrHide('visible','Event_JASONM_1_30'); javascript:setImage('ExpandIcon_JASONM_1_30','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONM_1_30'); layersShowOrHide('hidden','Lock_JASONM_1_30'); javascript:setImage('ExpandIcon_JASONM_1_30','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONM_1_30" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONM<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONM_1_30'); javascript:leftnavExpand('Menu_off_JASONM_1_30'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONM_1_30'); layersShowOrHide('hidden','Lock_JASONM_1_30'); javascript:setImage('ExpandIcon_JASONM_1_30','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONM_1_30" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONM_1_30" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=53&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=53&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=53&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONM_1_30'); javascript:leftnavExpand('Menu_off_JASONM_1_30'); " onmouseover="moveObject('Event_JASONM_1_30', event); setImage('ExpandIcon_JASONM_1_30','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONM_1_30'); setImage('ExpandIcon_JASONM_1_30','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONM_1_30',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONM_3_30" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONM_3_30'); javascript:leftnavExpand('Menu_off_JASONM_3_30'); " onmouseover="layersShowOrHide('visible','Event_JASONM_3_30'); javascript:setImage('ExpandIcon_JASONM_3_30','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONM_3_30'); layersShowOrHide('hidden','Lock_JASONM_3_30'); javascript:setImage('ExpandIcon_JASONM_3_30','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONM_3_30" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONM<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONM_3_30'); javascript:leftnavExpand('Menu_off_JASONM_3_30'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONM_3_30'); layersShowOrHide('hidden','Lock_JASONM_3_30'); javascript:setImage('ExpandIcon_JASONM_3_30','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONM_3_30" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONM_3_30" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=53&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=53&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=53&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONM_3_30'); javascript:leftnavExpand('Menu_off_JASONM_3_30'); " onmouseover="moveObject('Event_JASONM_3_30', event); setImage('ExpandIcon_JASONM_3_30','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONM_3_30'); setImage('ExpandIcon_JASONM_3_30','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONM_3_30',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONM" class="pidVerification" id="pid-53" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=53"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=53&amp;subjectId=53&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=53"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row31" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JASONN" class="pidVerification" id="pid-54" href="ViewStudySubject?id=54">JASONN</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONN</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JASONN_1_31" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONN_1_31'); javascript:leftnavExpand('Menu_off_JASONN_1_31'); " onmouseover="layersShowOrHide('visible','Event_JASONN_1_31'); javascript:setImage('ExpandIcon_JASONN_1_31','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONN_1_31'); layersShowOrHide('hidden','Lock_JASONN_1_31'); javascript:setImage('ExpandIcon_JASONN_1_31','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONN_1_31" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONN<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONN_1_31'); javascript:leftnavExpand('Menu_off_JASONN_1_31'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONN_1_31'); layersShowOrHide('hidden','Lock_JASONN_1_31'); javascript:setImage('ExpandIcon_JASONN_1_31','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONN_1_31" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONN_1_31" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=54&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=54&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=54&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONN_1_31'); javascript:leftnavExpand('Menu_off_JASONN_1_31'); " onmouseover="moveObject('Event_JASONN_1_31', event); setImage('ExpandIcon_JASONN_1_31','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONN_1_31'); setImage('ExpandIcon_JASONN_1_31','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONN_1_31',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONN_3_31" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONN_3_31'); javascript:leftnavExpand('Menu_off_JASONN_3_31'); " onmouseover="layersShowOrHide('visible','Event_JASONN_3_31'); javascript:setImage('ExpandIcon_JASONN_3_31','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONN_3_31'); layersShowOrHide('hidden','Lock_JASONN_3_31'); javascript:setImage('ExpandIcon_JASONN_3_31','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONN_3_31" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONN<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONN_3_31'); javascript:leftnavExpand('Menu_off_JASONN_3_31'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONN_3_31'); layersShowOrHide('hidden','Lock_JASONN_3_31'); javascript:setImage('ExpandIcon_JASONN_3_31','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONN_3_31" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONN_3_31" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=54&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=54&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=54&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONN_3_31'); javascript:leftnavExpand('Menu_off_JASONN_3_31'); " onmouseover="moveObject('Event_JASONN_3_31', event); setImage('ExpandIcon_JASONN_3_31','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONN_3_31'); setImage('ExpandIcon_JASONN_3_31','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONN_3_31',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONN" class="pidVerification" id="pid-54" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=54"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=54&amp;subjectId=54&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=54"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row32" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JASONO" class="pidVerification" id="pid-55" href="ViewStudySubject?id=55">JASONO</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONO</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JASONO_1_32" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONO_1_32'); javascript:leftnavExpand('Menu_off_JASONO_1_32'); " onmouseover="layersShowOrHide('visible','Event_JASONO_1_32'); javascript:setImage('ExpandIcon_JASONO_1_32','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONO_1_32'); layersShowOrHide('hidden','Lock_JASONO_1_32'); javascript:setImage('ExpandIcon_JASONO_1_32','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONO_1_32" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONO<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONO_1_32'); javascript:leftnavExpand('Menu_off_JASONO_1_32'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONO_1_32'); layersShowOrHide('hidden','Lock_JASONO_1_32'); javascript:setImage('ExpandIcon_JASONO_1_32','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONO_1_32" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONO_1_32" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=55&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=55&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=55&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONO_1_32'); javascript:leftnavExpand('Menu_off_JASONO_1_32'); " onmouseover="moveObject('Event_JASONO_1_32', event); setImage('ExpandIcon_JASONO_1_32','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONO_1_32'); setImage('ExpandIcon_JASONO_1_32','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONO_1_32',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONO_3_32" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONO_3_32'); javascript:leftnavExpand('Menu_off_JASONO_3_32'); " onmouseover="layersShowOrHide('visible','Event_JASONO_3_32'); javascript:setImage('ExpandIcon_JASONO_3_32','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONO_3_32'); layersShowOrHide('hidden','Lock_JASONO_3_32'); javascript:setImage('ExpandIcon_JASONO_3_32','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONO_3_32" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONO<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONO_3_32'); javascript:leftnavExpand('Menu_off_JASONO_3_32'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONO_3_32'); layersShowOrHide('hidden','Lock_JASONO_3_32'); javascript:setImage('ExpandIcon_JASONO_3_32','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONO_3_32" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONO_3_32" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=55&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=55&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=55&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONO_3_32'); javascript:leftnavExpand('Menu_off_JASONO_3_32'); " onmouseover="moveObject('Event_JASONO_3_32', event); setImage('ExpandIcon_JASONO_3_32','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONO_3_32'); setImage('ExpandIcon_JASONO_3_32','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONO_3_32',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONO" class="pidVerification" id="pid-55" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=55"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=55&amp;subjectId=55&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=55"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row33" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JASONP" class="pidVerification" id="pid-56" href="ViewStudySubject?id=56">JASONP</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONP</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASONP_1_33" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONP_1_33'); javascript:leftnavExpand('Menu_off_JASONP_1_33'); " onmouseover="layersShowOrHide('visible','Event_JASONP_1_33'); javascript:setImage('ExpandIcon_JASONP_1_33','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONP_1_33'); layersShowOrHide('hidden','Lock_JASONP_1_33'); javascript:setImage('ExpandIcon_JASONP_1_33','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONP_1_33" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONP<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONP_1_33'); javascript:leftnavExpand('Menu_off_JASONP_1_33'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONP_1_33'); layersShowOrHide('hidden','Lock_JASONP_1_33'); javascript:setImage('ExpandIcon_JASONP_1_33','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONP_1_33" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONP_1_33" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=56&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=56&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=56&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONP_1_33'); javascript:leftnavExpand('Menu_off_JASONP_1_33'); " onmouseover="moveObject('Event_JASONP_1_33', event); setImage('ExpandIcon_JASONP_1_33','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONP_1_33'); setImage('ExpandIcon_JASONP_1_33','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONP_1_33',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONP_3_33" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONP_3_33'); javascript:leftnavExpand('Menu_off_JASONP_3_33'); " onmouseover="layersShowOrHide('visible','Event_JASONP_3_33'); javascript:setImage('ExpandIcon_JASONP_3_33','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONP_3_33'); layersShowOrHide('hidden','Lock_JASONP_3_33'); javascript:setImage('ExpandIcon_JASONP_3_33','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONP_3_33" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONP<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONP_3_33'); javascript:leftnavExpand('Menu_off_JASONP_3_33'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONP_3_33'); layersShowOrHide('hidden','Lock_JASONP_3_33'); javascript:setImage('ExpandIcon_JASONP_3_33','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONP_3_33" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONP_3_33" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=56&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=56&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=56&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONP_3_33'); javascript:leftnavExpand('Menu_off_JASONP_3_33'); " onmouseover="moveObject('Event_JASONP_3_33', event); setImage('ExpandIcon_JASONP_3_33','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONP_3_33'); setImage('ExpandIcon_JASONP_3_33','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONP_3_33',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONP" class="pidVerification" id="pid-56" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=56"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=56&amp;subjectId=56&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=56"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row34" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JASONQ" class="pidVerification" id="pid-57" href="ViewStudySubject?id=57">JASONQ</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONQ</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JASONQ_1_34" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONQ_1_34'); javascript:leftnavExpand('Menu_off_JASONQ_1_34'); " onmouseover="layersShowOrHide('visible','Event_JASONQ_1_34'); javascript:setImage('ExpandIcon_JASONQ_1_34','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONQ_1_34'); layersShowOrHide('hidden','Lock_JASONQ_1_34'); javascript:setImage('ExpandIcon_JASONQ_1_34','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONQ_1_34" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONQ<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONQ_1_34'); javascript:leftnavExpand('Menu_off_JASONQ_1_34'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONQ_1_34'); layersShowOrHide('hidden','Lock_JASONQ_1_34'); javascript:setImage('ExpandIcon_JASONQ_1_34','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONQ_1_34" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONQ_1_34" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=57&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=57&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=57&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONQ_1_34'); javascript:leftnavExpand('Menu_off_JASONQ_1_34'); " onmouseover="moveObject('Event_JASONQ_1_34', event); setImage('ExpandIcon_JASONQ_1_34','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONQ_1_34'); setImage('ExpandIcon_JASONQ_1_34','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONQ_1_34',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONQ_3_34" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONQ_3_34'); javascript:leftnavExpand('Menu_off_JASONQ_3_34'); " onmouseover="layersShowOrHide('visible','Event_JASONQ_3_34'); javascript:setImage('ExpandIcon_JASONQ_3_34','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONQ_3_34'); layersShowOrHide('hidden','Lock_JASONQ_3_34'); javascript:setImage('ExpandIcon_JASONQ_3_34','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONQ_3_34" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONQ<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONQ_3_34'); javascript:leftnavExpand('Menu_off_JASONQ_3_34'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONQ_3_34'); layersShowOrHide('hidden','Lock_JASONQ_3_34'); javascript:setImage('ExpandIcon_JASONQ_3_34','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONQ_3_34" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONQ_3_34" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=57&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=57&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=57&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONQ_3_34'); javascript:leftnavExpand('Menu_off_JASONQ_3_34'); " onmouseover="moveObject('Event_JASONQ_3_34', event); setImage('ExpandIcon_JASONQ_3_34','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONQ_3_34'); setImage('ExpandIcon_JASONQ_3_34','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONQ_3_34',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONQ" class="pidVerification" id="pid-57" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=57"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=57&amp;subjectId=57&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=57"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row35" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JASONR" class="pidVerification" id="pid-58" href="ViewStudySubject?id=58">JASONR</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONR</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASONR_1_35" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONR_1_35'); javascript:leftnavExpand('Menu_off_JASONR_1_35'); " onmouseover="layersShowOrHide('visible','Event_JASONR_1_35'); javascript:setImage('ExpandIcon_JASONR_1_35','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONR_1_35'); layersShowOrHide('hidden','Lock_JASONR_1_35'); javascript:setImage('ExpandIcon_JASONR_1_35','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONR_1_35" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONR<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONR_1_35'); javascript:leftnavExpand('Menu_off_JASONR_1_35'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONR_1_35'); layersShowOrHide('hidden','Lock_JASONR_1_35'); javascript:setImage('ExpandIcon_JASONR_1_35','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONR_1_35" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONR_1_35" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=58&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=58&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=58&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONR_1_35'); javascript:leftnavExpand('Menu_off_JASONR_1_35'); " onmouseover="moveObject('Event_JASONR_1_35', event); setImage('ExpandIcon_JASONR_1_35','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONR_1_35'); setImage('ExpandIcon_JASONR_1_35','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONR_1_35',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONR_3_35" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONR_3_35'); javascript:leftnavExpand('Menu_off_JASONR_3_35'); " onmouseover="layersShowOrHide('visible','Event_JASONR_3_35'); javascript:setImage('ExpandIcon_JASONR_3_35','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONR_3_35'); layersShowOrHide('hidden','Lock_JASONR_3_35'); javascript:setImage('ExpandIcon_JASONR_3_35','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONR_3_35" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONR<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONR_3_35'); javascript:leftnavExpand('Menu_off_JASONR_3_35'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONR_3_35'); layersShowOrHide('hidden','Lock_JASONR_3_35'); javascript:setImage('ExpandIcon_JASONR_3_35','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONR_3_35" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONR_3_35" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=58&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=58&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=58&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONR_3_35'); javascript:leftnavExpand('Menu_off_JASONR_3_35'); " onmouseover="moveObject('Event_JASONR_3_35', event); setImage('ExpandIcon_JASONR_3_35','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONR_3_35'); setImage('ExpandIcon_JASONR_3_35','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONR_3_35',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONR" class="pidVerification" id="pid-58" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=58"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=58&amp;subjectId=58&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=58"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row36" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JASONS" class="pidVerification" id="pid-59" href="ViewStudySubject?id=59">JASONS</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONS</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JASONS_1_36" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONS_1_36'); javascript:leftnavExpand('Menu_off_JASONS_1_36'); " onmouseover="layersShowOrHide('visible','Event_JASONS_1_36'); javascript:setImage('ExpandIcon_JASONS_1_36','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONS_1_36'); layersShowOrHide('hidden','Lock_JASONS_1_36'); javascript:setImage('ExpandIcon_JASONS_1_36','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONS_1_36" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONS<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONS_1_36'); javascript:leftnavExpand('Menu_off_JASONS_1_36'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONS_1_36'); layersShowOrHide('hidden','Lock_JASONS_1_36'); javascript:setImage('ExpandIcon_JASONS_1_36','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONS_1_36" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONS_1_36" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=59&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=59&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=59&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONS_1_36'); javascript:leftnavExpand('Menu_off_JASONS_1_36'); " onmouseover="moveObject('Event_JASONS_1_36', event); setImage('ExpandIcon_JASONS_1_36','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONS_1_36'); setImage('ExpandIcon_JASONS_1_36','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONS_1_36',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONS_3_36" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONS_3_36'); javascript:leftnavExpand('Menu_off_JASONS_3_36'); " onmouseover="layersShowOrHide('visible','Event_JASONS_3_36'); javascript:setImage('ExpandIcon_JASONS_3_36','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONS_3_36'); layersShowOrHide('hidden','Lock_JASONS_3_36'); javascript:setImage('ExpandIcon_JASONS_3_36','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONS_3_36" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONS<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONS_3_36'); javascript:leftnavExpand('Menu_off_JASONS_3_36'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONS_3_36'); layersShowOrHide('hidden','Lock_JASONS_3_36'); javascript:setImage('ExpandIcon_JASONS_3_36','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONS_3_36" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONS_3_36" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=59&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=59&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=59&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONS_3_36'); javascript:leftnavExpand('Menu_off_JASONS_3_36'); " onmouseover="moveObject('Event_JASONS_3_36', event); setImage('ExpandIcon_JASONS_3_36','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONS_3_36'); setImage('ExpandIcon_JASONS_3_36','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONS_3_36',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONS" class="pidVerification" id="pid-59" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=59"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=59&amp;subjectId=59&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=59"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row37" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JASONT" class="pidVerification" id="pid-60" href="ViewStudySubject?id=60">JASONT</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONT</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASONT_1_37" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONT_1_37'); javascript:leftnavExpand('Menu_off_JASONT_1_37'); " onmouseover="layersShowOrHide('visible','Event_JASONT_1_37'); javascript:setImage('ExpandIcon_JASONT_1_37','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONT_1_37'); layersShowOrHide('hidden','Lock_JASONT_1_37'); javascript:setImage('ExpandIcon_JASONT_1_37','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONT_1_37" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONT<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONT_1_37'); javascript:leftnavExpand('Menu_off_JASONT_1_37'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONT_1_37'); layersShowOrHide('hidden','Lock_JASONT_1_37'); javascript:setImage('ExpandIcon_JASONT_1_37','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONT_1_37" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONT_1_37" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=60&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=60&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=60&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONT_1_37'); javascript:leftnavExpand('Menu_off_JASONT_1_37'); " onmouseover="moveObject('Event_JASONT_1_37', event); setImage('ExpandIcon_JASONT_1_37','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONT_1_37'); setImage('ExpandIcon_JASONT_1_37','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONT_1_37',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONT_3_37" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONT_3_37'); javascript:leftnavExpand('Menu_off_JASONT_3_37'); " onmouseover="layersShowOrHide('visible','Event_JASONT_3_37'); javascript:setImage('ExpandIcon_JASONT_3_37','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONT_3_37'); layersShowOrHide('hidden','Lock_JASONT_3_37'); javascript:setImage('ExpandIcon_JASONT_3_37','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONT_3_37" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONT<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONT_3_37'); javascript:leftnavExpand('Menu_off_JASONT_3_37'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONT_3_37'); layersShowOrHide('hidden','Lock_JASONT_3_37'); javascript:setImage('ExpandIcon_JASONT_3_37','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONT_3_37" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONT_3_37" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=60&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=60&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=60&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONT_3_37'); javascript:leftnavExpand('Menu_off_JASONT_3_37'); " onmouseover="moveObject('Event_JASONT_3_37', event); setImage('ExpandIcon_JASONT_3_37','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONT_3_37'); setImage('ExpandIcon_JASONT_3_37','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONT_3_37',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONT" class="pidVerification" id="pid-60" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=60"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=60&amp;subjectId=60&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=60"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row38" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JASONU" class="pidVerification" id="pid-61" href="ViewStudySubject?id=61">JASONU</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONU</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JASONU_1_38" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONU_1_38'); javascript:leftnavExpand('Menu_off_JASONU_1_38'); " onmouseover="layersShowOrHide('visible','Event_JASONU_1_38'); javascript:setImage('ExpandIcon_JASONU_1_38','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONU_1_38'); layersShowOrHide('hidden','Lock_JASONU_1_38'); javascript:setImage('ExpandIcon_JASONU_1_38','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONU_1_38" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONU<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONU_1_38'); javascript:leftnavExpand('Menu_off_JASONU_1_38'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONU_1_38'); layersShowOrHide('hidden','Lock_JASONU_1_38'); javascript:setImage('ExpandIcon_JASONU_1_38','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONU_1_38" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONU_1_38" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=61&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=61&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=61&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONU_1_38'); javascript:leftnavExpand('Menu_off_JASONU_1_38'); " onmouseover="moveObject('Event_JASONU_1_38', event); setImage('ExpandIcon_JASONU_1_38','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONU_1_38'); setImage('ExpandIcon_JASONU_1_38','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONU_1_38',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONU_3_38" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONU_3_38'); javascript:leftnavExpand('Menu_off_JASONU_3_38'); " onmouseover="layersShowOrHide('visible','Event_JASONU_3_38'); javascript:setImage('ExpandIcon_JASONU_3_38','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONU_3_38'); layersShowOrHide('hidden','Lock_JASONU_3_38'); javascript:setImage('ExpandIcon_JASONU_3_38','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONU_3_38" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONU<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONU_3_38'); javascript:leftnavExpand('Menu_off_JASONU_3_38'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONU_3_38'); layersShowOrHide('hidden','Lock_JASONU_3_38'); javascript:setImage('ExpandIcon_JASONU_3_38','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONU_3_38" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONU_3_38" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=61&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=61&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=61&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONU_3_38'); javascript:leftnavExpand('Menu_off_JASONU_3_38'); " onmouseover="moveObject('Event_JASONU_3_38', event); setImage('ExpandIcon_JASONU_3_38','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONU_3_38'); setImage('ExpandIcon_JASONU_3_38','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONU_3_38',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONU" class="pidVerification" id="pid-61" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=61"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=61&amp;subjectId=61&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=61"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row39" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JASONV" class="pidVerification" id="pid-62" href="ViewStudySubject?id=62">JASONV</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONV</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JASONV_1_39" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONV_1_39'); javascript:leftnavExpand('Menu_off_JASONV_1_39'); " onmouseover="layersShowOrHide('visible','Event_JASONV_1_39'); javascript:setImage('ExpandIcon_JASONV_1_39','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONV_1_39'); layersShowOrHide('hidden','Lock_JASONV_1_39'); javascript:setImage('ExpandIcon_JASONV_1_39','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONV_1_39" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONV<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONV_1_39'); javascript:leftnavExpand('Menu_off_JASONV_1_39'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONV_1_39'); layersShowOrHide('hidden','Lock_JASONV_1_39'); javascript:setImage('ExpandIcon_JASONV_1_39','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONV_1_39" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONV_1_39" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=62&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=62&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=62&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONV_1_39'); javascript:leftnavExpand('Menu_off_JASONV_1_39'); " onmouseover="moveObject('Event_JASONV_1_39', event); setImage('ExpandIcon_JASONV_1_39','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONV_1_39'); setImage('ExpandIcon_JASONV_1_39','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONV_1_39',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONV_3_39" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONV_3_39'); javascript:leftnavExpand('Menu_off_JASONV_3_39'); " onmouseover="layersShowOrHide('visible','Event_JASONV_3_39'); javascript:setImage('ExpandIcon_JASONV_3_39','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONV_3_39'); layersShowOrHide('hidden','Lock_JASONV_3_39'); javascript:setImage('ExpandIcon_JASONV_3_39','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONV_3_39" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONV<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONV_3_39'); javascript:leftnavExpand('Menu_off_JASONV_3_39'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONV_3_39'); layersShowOrHide('hidden','Lock_JASONV_3_39'); javascript:setImage('ExpandIcon_JASONV_3_39','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONV_3_39" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONV_3_39" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=62&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=62&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=62&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONV_3_39'); javascript:leftnavExpand('Menu_off_JASONV_3_39'); " onmouseover="moveObject('Event_JASONV_3_39', event); setImage('ExpandIcon_JASONV_3_39','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONV_3_39'); setImage('ExpandIcon_JASONV_3_39','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONV_3_39',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONV" class="pidVerification" id="pid-62" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=62"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=62&amp;subjectId=62&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=62"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row40" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JASONW" class="pidVerification" id="pid-63" href="ViewStudySubject?id=63">JASONW</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JASONW</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JASONW_1_40" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONW_1_40'); javascript:leftnavExpand('Menu_off_JASONW_1_40'); " onmouseover="layersShowOrHide('visible','Event_JASONW_1_40'); javascript:setImage('ExpandIcon_JASONW_1_40','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONW_1_40'); layersShowOrHide('hidden','Lock_JASONW_1_40'); javascript:setImage('ExpandIcon_JASONW_1_40','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONW_1_40" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONW<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONW_1_40'); javascript:leftnavExpand('Menu_off_JASONW_1_40'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONW_1_40'); layersShowOrHide('hidden','Lock_JASONW_1_40'); javascript:setImage('ExpandIcon_JASONW_1_40','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONW_1_40" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONW_1_40" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=63&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=63&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=63&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONW_1_40'); javascript:leftnavExpand('Menu_off_JASONW_1_40'); " onmouseover="moveObject('Event_JASONW_1_40', event); setImage('ExpandIcon_JASONW_1_40','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONW_1_40'); setImage('ExpandIcon_JASONW_1_40','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONW_1_40',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JASONW_3_40" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JASONW_3_40'); javascript:leftnavExpand('Menu_off_JASONW_3_40'); " onmouseover="layersShowOrHide('visible','Event_JASONW_3_40'); javascript:setImage('ExpandIcon_JASONW_3_40','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONW_3_40'); layersShowOrHide('hidden','Lock_JASONW_3_40'); javascript:setImage('ExpandIcon_JASONW_3_40','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JASONW_3_40" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JASONW<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JASONW_3_40'); javascript:leftnavExpand('Menu_off_JASONW_3_40'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JASONW_3_40'); layersShowOrHide('hidden','Lock_JASONW_3_40'); javascript:setImage('ExpandIcon_JASONW_3_40','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JASONW_3_40" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JASONW_3_40" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=63&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=63&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=63&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JASONW_3_40'); javascript:leftnavExpand('Menu_off_JASONW_3_40'); " onmouseover="moveObject('Event_JASONW_3_40', event); setImage('ExpandIcon_JASONW_3_40','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JASONW_3_40'); setImage('ExpandIcon_JASONW_3_40','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JASONW_3_40',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JASONW" class="pidVerification" id="pid-63" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=63"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=63&amp;subjectId=63&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=63"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row41" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JEAN" class="pidVerification" id="pid-27" href="ViewStudySubject?id=27">JEAN</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JEAN</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JEAN_1_41" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JEAN_1_41'); javascript:leftnavExpand('Menu_off_JEAN_1_41'); " onmouseover="layersShowOrHide('visible','Event_JEAN_1_41'); javascript:setImage('ExpandIcon_JEAN_1_41','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEAN_1_41'); layersShowOrHide('hidden','Lock_JEAN_1_41'); javascript:setImage('ExpandIcon_JEAN_1_41','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JEAN_1_41" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JEAN<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JEAN_1_41'); javascript:leftnavExpand('Menu_off_JEAN_1_41'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEAN_1_41'); layersShowOrHide('hidden','Lock_JEAN_1_41'); javascript:setImage('ExpandIcon_JEAN_1_41','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JEAN_1_41" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JEAN_1_41" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=27&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=27&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=27&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JEAN_1_41'); javascript:leftnavExpand('Menu_off_JEAN_1_41'); " onmouseover="moveObject('Event_JEAN_1_41', event); setImage('ExpandIcon_JEAN_1_41','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JEAN_1_41'); setImage('ExpandIcon_JEAN_1_41','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JEAN_1_41',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JEAN_3_41" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JEAN_3_41'); javascript:leftnavExpand('Menu_off_JEAN_3_41'); " onmouseover="layersShowOrHide('visible','Event_JEAN_3_41'); javascript:setImage('ExpandIcon_JEAN_3_41','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEAN_3_41'); layersShowOrHide('hidden','Lock_JEAN_3_41'); javascript:setImage('ExpandIcon_JEAN_3_41','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JEAN_3_41" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JEAN<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JEAN_3_41'); javascript:leftnavExpand('Menu_off_JEAN_3_41'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEAN_3_41'); layersShowOrHide('hidden','Lock_JEAN_3_41'); javascript:setImage('ExpandIcon_JEAN_3_41','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JEAN_3_41" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JEAN_3_41" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=27&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=27&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=27&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JEAN_3_41'); javascript:leftnavExpand('Menu_off_JEAN_3_41'); " onmouseover="moveObject('Event_JEAN_3_41', event); setImage('ExpandIcon_JEAN_3_41','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JEAN_3_41'); setImage('ExpandIcon_JEAN_3_41','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JEAN_3_41',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JEAN" class="pidVerification" id="pid-27" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=27"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=27&amp;subjectId=27&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=27"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row42" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JEAN2" class="pidVerification" id="pid-29" href="ViewStudySubject?id=29">JEAN2</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JEAN2</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JEAN2_1_42" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JEAN2_1_42'); javascript:leftnavExpand('Menu_off_JEAN2_1_42'); " onmouseover="layersShowOrHide('visible','Event_JEAN2_1_42'); javascript:setImage('ExpandIcon_JEAN2_1_42','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEAN2_1_42'); layersShowOrHide('hidden','Lock_JEAN2_1_42'); javascript:setImage('ExpandIcon_JEAN2_1_42','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JEAN2_1_42" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JEAN2<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JEAN2_1_42'); javascript:leftnavExpand('Menu_off_JEAN2_1_42'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEAN2_1_42'); layersShowOrHide('hidden','Lock_JEAN2_1_42'); javascript:setImage('ExpandIcon_JEAN2_1_42','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JEAN2_1_42" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JEAN2_1_42" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=29&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=29&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=29&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JEAN2_1_42'); javascript:leftnavExpand('Menu_off_JEAN2_1_42'); " onmouseover="moveObject('Event_JEAN2_1_42', event); setImage('ExpandIcon_JEAN2_1_42','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JEAN2_1_42'); setImage('ExpandIcon_JEAN2_1_42','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JEAN2_1_42',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JEAN2_3_42" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JEAN2_3_42'); javascript:leftnavExpand('Menu_off_JEAN2_3_42'); " onmouseover="layersShowOrHide('visible','Event_JEAN2_3_42'); javascript:setImage('ExpandIcon_JEAN2_3_42','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEAN2_3_42'); layersShowOrHide('hidden','Lock_JEAN2_3_42'); javascript:setImage('ExpandIcon_JEAN2_3_42','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JEAN2_3_42" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JEAN2<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JEAN2_3_42'); javascript:leftnavExpand('Menu_off_JEAN2_3_42'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEAN2_3_42'); layersShowOrHide('hidden','Lock_JEAN2_3_42'); javascript:setImage('ExpandIcon_JEAN2_3_42','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JEAN2_3_42" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JEAN2_3_42" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=29&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=29&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=29&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JEAN2_3_42'); javascript:leftnavExpand('Menu_off_JEAN2_3_42'); " onmouseover="moveObject('Event_JEAN2_3_42', event); setImage('ExpandIcon_JEAN2_3_42','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JEAN2_3_42'); setImage('ExpandIcon_JEAN2_3_42','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JEAN2_3_42',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JEAN2" class="pidVerification" id="pid-29" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=29"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=29&amp;subjectId=29&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=29"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row43" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JEAN3" class="pidVerification" id="pid-31" href="ViewStudySubject?id=31">JEAN3</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JEAN3</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_JEAN3_1_43" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JEAN3_1_43'); javascript:leftnavExpand('Menu_off_JEAN3_1_43'); " onmouseover="layersShowOrHide('visible','Event_JEAN3_1_43'); javascript:setImage('ExpandIcon_JEAN3_1_43','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEAN3_1_43'); layersShowOrHide('hidden','Lock_JEAN3_1_43'); javascript:setImage('ExpandIcon_JEAN3_1_43','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JEAN3_1_43" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JEAN3<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JEAN3_1_43'); javascript:leftnavExpand('Menu_off_JEAN3_1_43'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEAN3_1_43'); layersShowOrHide('hidden','Lock_JEAN3_1_43'); javascript:setImage('ExpandIcon_JEAN3_1_43','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JEAN3_1_43" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JEAN3_1_43" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=31&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=31&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=31&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JEAN3_1_43'); javascript:leftnavExpand('Menu_off_JEAN3_1_43'); " onmouseover="moveObject('Event_JEAN3_1_43', event); setImage('ExpandIcon_JEAN3_1_43','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JEAN3_1_43'); setImage('ExpandIcon_JEAN3_1_43','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JEAN3_1_43',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JEAN3_3_43" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JEAN3_3_43'); javascript:leftnavExpand('Menu_off_JEAN3_3_43'); " onmouseover="layersShowOrHide('visible','Event_JEAN3_3_43'); javascript:setImage('ExpandIcon_JEAN3_3_43','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEAN3_3_43'); layersShowOrHide('hidden','Lock_JEAN3_3_43'); javascript:setImage('ExpandIcon_JEAN3_3_43','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JEAN3_3_43" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JEAN3<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JEAN3_3_43'); javascript:leftnavExpand('Menu_off_JEAN3_3_43'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEAN3_3_43'); layersShowOrHide('hidden','Lock_JEAN3_3_43'); javascript:setImage('ExpandIcon_JEAN3_3_43','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JEAN3_3_43" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JEAN3_3_43" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=31&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=31&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=31&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JEAN3_3_43'); javascript:leftnavExpand('Menu_off_JEAN3_3_43'); " onmouseover="moveObject('Event_JEAN3_3_43', event); setImage('ExpandIcon_JEAN3_3_43','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JEAN3_3_43'); setImage('ExpandIcon_JEAN3_3_43','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JEAN3_3_43',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JEAN3" class="pidVerification" id="pid-31" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=31"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=31&amp;subjectId=31&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=31"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row44" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JEANA" class="pidVerification" id="pid-35" href="ViewStudySubject?id=35">JEANA</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JEANA</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JEANA_1_44" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JEANA_1_44'); javascript:leftnavExpand('Menu_off_JEANA_1_44'); " onmouseover="layersShowOrHide('visible','Event_JEANA_1_44'); javascript:setImage('ExpandIcon_JEANA_1_44','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEANA_1_44'); layersShowOrHide('hidden','Lock_JEANA_1_44'); javascript:setImage('ExpandIcon_JEANA_1_44','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JEANA_1_44" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JEANA<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JEANA_1_44'); javascript:leftnavExpand('Menu_off_JEANA_1_44'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEANA_1_44'); layersShowOrHide('hidden','Lock_JEANA_1_44'); javascript:setImage('ExpandIcon_JEANA_1_44','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JEANA_1_44" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JEANA_1_44" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=35&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=35&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=35&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JEANA_1_44'); javascript:leftnavExpand('Menu_off_JEANA_1_44'); " onmouseover="moveObject('Event_JEANA_1_44', event); setImage('ExpandIcon_JEANA_1_44','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JEANA_1_44'); setImage('ExpandIcon_JEANA_1_44','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JEANA_1_44',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JEANA_3_44" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JEANA_3_44'); javascript:leftnavExpand('Menu_off_JEANA_3_44'); " onmouseover="layersShowOrHide('visible','Event_JEANA_3_44'); javascript:setImage('ExpandIcon_JEANA_3_44','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEANA_3_44'); layersShowOrHide('hidden','Lock_JEANA_3_44'); javascript:setImage('ExpandIcon_JEANA_3_44','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JEANA_3_44" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JEANA<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JEANA_3_44'); javascript:leftnavExpand('Menu_off_JEANA_3_44'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEANA_3_44'); layersShowOrHide('hidden','Lock_JEANA_3_44'); javascript:setImage('ExpandIcon_JEANA_3_44','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JEANA_3_44" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JEANA_3_44" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=35&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=35&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=35&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JEANA_3_44'); javascript:leftnavExpand('Menu_off_JEANA_3_44'); " onmouseover="moveObject('Event_JEANA_3_44', event); setImage('ExpandIcon_JEANA_3_44','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JEANA_3_44'); setImage('ExpandIcon_JEANA_3_44','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JEANA_3_44',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JEANA" class="pidVerification" id="pid-35" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=35"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=35&amp;subjectId=35&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=35"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row45" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="JEANAA" class="pidVerification" id="pid-36" href="ViewStudySubject?id=36">JEANAA</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JEANAA</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JEANAA_1_45" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JEANAA_1_45'); javascript:leftnavExpand('Menu_off_JEANAA_1_45'); " onmouseover="layersShowOrHide('visible','Event_JEANAA_1_45'); javascript:setImage('ExpandIcon_JEANAA_1_45','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEANAA_1_45'); layersShowOrHide('hidden','Lock_JEANAA_1_45'); javascript:setImage('ExpandIcon_JEANAA_1_45','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JEANAA_1_45" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JEANAA<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JEANAA_1_45'); javascript:leftnavExpand('Menu_off_JEANAA_1_45'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEANAA_1_45'); layersShowOrHide('hidden','Lock_JEANAA_1_45'); javascript:setImage('ExpandIcon_JEANAA_1_45','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JEANAA_1_45" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JEANAA_1_45" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=36&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=36&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=36&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JEANAA_1_45'); javascript:leftnavExpand('Menu_off_JEANAA_1_45'); " onmouseover="moveObject('Event_JEANAA_1_45', event); setImage('ExpandIcon_JEANAA_1_45','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JEANAA_1_45'); setImage('ExpandIcon_JEANAA_1_45','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JEANAA_1_45',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JEANAA_3_45" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JEANAA_3_45'); javascript:leftnavExpand('Menu_off_JEANAA_3_45'); " onmouseover="layersShowOrHide('visible','Event_JEANAA_3_45'); javascript:setImage('ExpandIcon_JEANAA_3_45','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEANAA_3_45'); layersShowOrHide('hidden','Lock_JEANAA_3_45'); javascript:setImage('ExpandIcon_JEANAA_3_45','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JEANAA_3_45" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JEANAA<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JEANAA_3_45'); javascript:leftnavExpand('Menu_off_JEANAA_3_45'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEANAA_3_45'); layersShowOrHide('hidden','Lock_JEANAA_3_45'); javascript:setImage('ExpandIcon_JEANAA_3_45','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JEANAA_3_45" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JEANAA_3_45" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=36&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=36&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=36&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JEANAA_3_45'); javascript:leftnavExpand('Menu_off_JEANAA_3_45'); " onmouseover="moveObject('Event_JEANAA_3_45', event); setImage('ExpandIcon_JEANAA_3_45','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JEANAA_3_45'); setImage('ExpandIcon_JEANAA_3_45','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JEANAA_3_45',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JEANAA" class="pidVerification" id="pid-36" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=36"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=36&amp;subjectId=36&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=36"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row46" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="JEANS" class="pidVerification" id="pid-34" href="ViewStudySubject?id=34">JEANS</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_JEANS</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_JEANS_1_46" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JEANS_1_46'); javascript:leftnavExpand('Menu_off_JEANS_1_46'); " onmouseover="layersShowOrHide('visible','Event_JEANS_1_46'); javascript:setImage('ExpandIcon_JEANS_1_46','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEANS_1_46'); layersShowOrHide('hidden','Lock_JEANS_1_46'); javascript:setImage('ExpandIcon_JEANS_1_46','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JEANS_1_46" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JEANS<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JEANS_1_46'); javascript:leftnavExpand('Menu_off_JEANS_1_46'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEANS_1_46'); layersShowOrHide('hidden','Lock_JEANS_1_46'); javascript:setImage('ExpandIcon_JEANS_1_46','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JEANS_1_46" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JEANS_1_46" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=34&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=34&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=34&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JEANS_1_46'); javascript:leftnavExpand('Menu_off_JEANS_1_46'); " onmouseover="moveObject('Event_JEANS_1_46', event); setImage('ExpandIcon_JEANS_1_46','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JEANS_1_46'); setImage('ExpandIcon_JEANS_1_46','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JEANS_1_46',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_JEANS_3_46" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_JEANS_3_46'); javascript:leftnavExpand('Menu_off_JEANS_3_46'); " onmouseover="layersShowOrHide('visible','Event_JEANS_3_46'); javascript:setImage('ExpandIcon_JEANS_3_46','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEANS_3_46'); layersShowOrHide('hidden','Lock_JEANS_3_46'); javascript:setImage('ExpandIcon_JEANS_3_46','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_JEANS_3_46" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: JEANS<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_JEANS_3_46'); javascript:leftnavExpand('Menu_off_JEANS_3_46'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_JEANS_3_46'); layersShowOrHide('hidden','Lock_JEANS_3_46'); javascript:setImage('ExpandIcon_JEANS_3_46','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_JEANS_3_46" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_JEANS_3_46" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=34&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=34&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=34&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_JEANS_3_46'); javascript:leftnavExpand('Menu_off_JEANS_3_46'); " onmouseover="moveObject('Event_JEANS_3_46', event); setImage('ExpandIcon_JEANS_3_46','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_JEANS_3_46'); setImage('ExpandIcon_JEANS_3_46','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_JEANS_3_46',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="JEANS" class="pidVerification" id="pid-34" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=34"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=34&amp;subjectId=34&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=34"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row47" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="KETCHUP" class="pidVerification" id="pid-20" href="ViewStudySubject?id=20">KETCHUP</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_KETCHUP</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_KETCHUP_1_47" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_KETCHUP_1_47'); javascript:leftnavExpand('Menu_off_KETCHUP_1_47'); " onmouseover="layersShowOrHide('visible','Event_KETCHUP_1_47'); javascript:setImage('ExpandIcon_KETCHUP_1_47','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_KETCHUP_1_47'); layersShowOrHide('hidden','Lock_KETCHUP_1_47'); javascript:setImage('ExpandIcon_KETCHUP_1_47','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_KETCHUP_1_47" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: KETCHUP<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_KETCHUP_1_47'); javascript:leftnavExpand('Menu_off_KETCHUP_1_47'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_KETCHUP_1_47'); layersShowOrHide('hidden','Lock_KETCHUP_1_47'); javascript:setImage('ExpandIcon_KETCHUP_1_47','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_KETCHUP_1_47" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_KETCHUP_1_47" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=20&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=20&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=20&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_KETCHUP_1_47'); javascript:leftnavExpand('Menu_off_KETCHUP_1_47'); " onmouseover="moveObject('Event_KETCHUP_1_47', event); setImage('ExpandIcon_KETCHUP_1_47','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_KETCHUP_1_47'); setImage('ExpandIcon_KETCHUP_1_47','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_KETCHUP_1_47',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_KETCHUP_3_47" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_KETCHUP_3_47'); javascript:leftnavExpand('Menu_off_KETCHUP_3_47'); " onmouseover="layersShowOrHide('visible','Event_KETCHUP_3_47'); javascript:setImage('ExpandIcon_KETCHUP_3_47','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_KETCHUP_3_47'); layersShowOrHide('hidden','Lock_KETCHUP_3_47'); javascript:setImage('ExpandIcon_KETCHUP_3_47','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_KETCHUP_3_47" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: KETCHUP<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_KETCHUP_3_47'); javascript:leftnavExpand('Menu_off_KETCHUP_3_47'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_KETCHUP_3_47'); layersShowOrHide('hidden','Lock_KETCHUP_3_47'); javascript:setImage('ExpandIcon_KETCHUP_3_47','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_KETCHUP_3_47" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_KETCHUP_3_47" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=20&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=20&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=20&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_KETCHUP_3_47'); javascript:leftnavExpand('Menu_off_KETCHUP_3_47'); " onmouseover="moveObject('Event_KETCHUP_3_47', event); setImage('ExpandIcon_KETCHUP_3_47','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_KETCHUP_3_47'); setImage('ExpandIcon_KETCHUP_3_47','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_KETCHUP_3_47',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="KETCHUP" class="pidVerification" id="pid-20" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=20"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=20&amp;subjectId=20&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=20"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row48" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="LANAA" class="pidVerification" id="pid-38" href="ViewStudySubject?id=38">LANAA</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_LANAA</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_LANAA_1_48" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_LANAA_1_48'); javascript:leftnavExpand('Menu_off_LANAA_1_48'); " onmouseover="layersShowOrHide('visible','Event_LANAA_1_48'); javascript:setImage('ExpandIcon_LANAA_1_48','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_LANAA_1_48'); layersShowOrHide('hidden','Lock_LANAA_1_48'); javascript:setImage('ExpandIcon_LANAA_1_48','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_LANAA_1_48" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: LANAA<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_LANAA_1_48'); javascript:leftnavExpand('Menu_off_LANAA_1_48'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_LANAA_1_48'); layersShowOrHide('hidden','Lock_LANAA_1_48'); javascript:setImage('ExpandIcon_LANAA_1_48','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_LANAA_1_48" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_LANAA_1_48" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=38&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=38&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=38&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_LANAA_1_48'); javascript:leftnavExpand('Menu_off_LANAA_1_48'); " onmouseover="moveObject('Event_LANAA_1_48', event); setImage('ExpandIcon_LANAA_1_48','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_LANAA_1_48'); setImage('ExpandIcon_LANAA_1_48','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_LANAA_1_48',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_LANAA_3_48" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_LANAA_3_48'); javascript:leftnavExpand('Menu_off_LANAA_3_48'); " onmouseover="layersShowOrHide('visible','Event_LANAA_3_48'); javascript:setImage('ExpandIcon_LANAA_3_48','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_LANAA_3_48'); layersShowOrHide('hidden','Lock_LANAA_3_48'); javascript:setImage('ExpandIcon_LANAA_3_48','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_LANAA_3_48" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: LANAA<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_LANAA_3_48'); javascript:leftnavExpand('Menu_off_LANAA_3_48'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_LANAA_3_48'); layersShowOrHide('hidden','Lock_LANAA_3_48'); javascript:setImage('ExpandIcon_LANAA_3_48','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_LANAA_3_48" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_LANAA_3_48" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=38&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=38&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=38&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_LANAA_3_48'); javascript:leftnavExpand('Menu_off_LANAA_3_48'); " onmouseover="moveObject('Event_LANAA_3_48', event); setImage('ExpandIcon_LANAA_3_48','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_LANAA_3_48'); setImage('ExpandIcon_LANAA_3_48','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_LANAA_3_48',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="LANAA" class="pidVerification" id="pid-38" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=38"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=38&amp;subjectId=38&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=38"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row49" class="odd" onmouseover="this.className='highlight'" onmouseout="this.className='odd'">
        <td><a name="MARGHERITA" class="pidVerification" id="pid-25" href="ViewStudySubject?id=25">MARGHERITA</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_MARGHERI</td>
        <td style="display: none;"></td>
        <td>
<div id="Lock_MARGHERITA_1_49" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_MARGHERITA_1_49'); javascript:leftnavExpand('Menu_off_MARGHERITA_1_49'); " onmouseover="layersShowOrHide('visible','Event_MARGHERITA_1_49'); javascript:setImage('ExpandIcon_MARGHERITA_1_49','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_MARGHERITA_1_49'); layersShowOrHide('hidden','Lock_MARGHERITA_1_49'); javascript:setImage('ExpandIcon_MARGHERITA_1_49','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_MARGHERITA_1_49" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: MARGHERITA<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_MARGHERITA_1_49'); javascript:leftnavExpand('Menu_off_MARGHERITA_1_49'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_MARGHERITA_1_49'); layersShowOrHide('hidden','Lock_MARGHERITA_1_49'); javascript:setImage('ExpandIcon_MARGHERITA_1_49','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_MARGHERITA_1_49" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_MARGHERITA_1_49" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=25&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=25&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=25&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_MARGHERITA_1_49'); javascript:leftnavExpand('Menu_off_MARGHERITA_1_49'); " onmouseover="moveObject('Event_MARGHERITA_1_49', event); setImage('ExpandIcon_MARGHERITA_1_49','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_MARGHERITA_1_49'); setImage('ExpandIcon_MARGHERITA_1_49','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_MARGHERITA_1_49',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_MARGHERITA_3_49" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_MARGHERITA_3_49'); javascript:leftnavExpand('Menu_off_MARGHERITA_3_49'); " onmouseover="layersShowOrHide('visible','Event_MARGHERITA_3_49'); javascript:setImage('ExpandIcon_MARGHERITA_3_49','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_MARGHERITA_3_49'); layersShowOrHide('hidden','Lock_MARGHERITA_3_49'); javascript:setImage('ExpandIcon_MARGHERITA_3_49','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_MARGHERITA_3_49" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: MARGHERITA<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_MARGHERITA_3_49'); javascript:leftnavExpand('Menu_off_MARGHERITA_3_49'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_MARGHERITA_3_49'); layersShowOrHide('hidden','Lock_MARGHERITA_3_49'); javascript:setImage('ExpandIcon_MARGHERITA_3_49','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_MARGHERITA_3_49" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_MARGHERITA_3_49" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=25&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=25&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=25&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_MARGHERITA_3_49'); javascript:leftnavExpand('Menu_off_MARGHERITA_3_49'); " onmouseover="moveObject('Event_MARGHERITA_3_49', event); setImage('ExpandIcon_MARGHERITA_3_49','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_MARGHERITA_3_49'); setImage('ExpandIcon_MARGHERITA_3_49','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_MARGHERITA_3_49',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="MARGHERITA" class="pidVerification" id="pid-25" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=25"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=25&amp;subjectId=25&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=25"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr id="findSubjects_row50" class="even" onmouseover="this.className='highlight'" onmouseout="this.className='even'">
        <td><a name="MOZZARELLA" class="pidVerification" id="pid-2" href="ViewStudySubject?id=2">MOZZARELLA</a></td>
        <td style="display: none;">NAPLES</td>
        <td style="display: none;">Available</td>
        <td style="display: none;">SS_MOZZAREL</td>
        <td style="display: none;">Created</td>
        <td>
<div id="Lock_MOZZARELLA_1_50" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_MOZZARELLA_1_50'); javascript:leftnavExpand('Menu_off_MOZZARELLA_1_50'); " onmouseover="layersShowOrHide('visible','Event_MOZZARELLA_1_50'); javascript:setImage('ExpandIcon_MOZZARELLA_1_50','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_MOZZARELLA_1_50'); layersShowOrHide('hidden','Lock_MOZZARELLA_1_50'); javascript:setImage('ExpandIcon_MOZZARELLA_1_50','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_MOZZARELLA_1_50" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: MOZZARELLA<br>Event: Visits<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_MOZZARELLA_1_50'); javascript:leftnavExpand('Menu_off_MOZZARELLA_1_50'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_MOZZARELLA_1_50'); layersShowOrHide('hidden','Lock_MOZZARELLA_1_50'); javascript:setImage('ExpandIcon_MOZZARELLA_1_50','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_MOZZARELLA_1_50" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_MOZZARELLA_1_50" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=2&amp;studyEventDefinition=1"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=2&amp;studyEventDefinition=1</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=2&amp;studyEventDefinition=1">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_MOZZARELLA_1_50'); javascript:leftnavExpand('Menu_off_MOZZARELLA_1_50'); " onmouseover="moveObject('Event_MOZZARELLA_1_50', event); setImage('ExpandIcon_MOZZARELLA_1_50','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_MOZZARELLA_1_50'); setImage('ExpandIcon_MOZZARELLA_1_50','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_MOZZARELLA_1_50',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td>
<div id="Lock_MOZZARELLA_3_50" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;"><a href="javascript:leftnavExpand('Menu_on_MOZZARELLA_3_50'); javascript:leftnavExpand('Menu_off_MOZZARELLA_3_50'); " onmouseover="layersShowOrHide('visible','Event_MOZZARELLA_3_50'); javascript:setImage('ExpandIcon_MOZZARELLA_3_50','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_MOZZARELLA_3_50'); layersShowOrHide('hidden','Lock_MOZZARELLA_3_50'); javascript:setImage('ExpandIcon_MOZZARELLA_3_50','images/icon_blank.gif'); "><img src="images/spacer.gif" border="0" height="30" width="50"></a></div><table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td><div id="Event_MOZZARELLA_3_50" style="position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px; float: left;"><div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="tablebox_center"><div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
<table border="0" cellpadding="0" cellspacing="0">
<tbody><tr valign="top">
<td class="table_header_row_left">Participant: MOZZARELLA<br>Event: Visits 2<br><br><b>not scheduled<br></b></td>
<td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_MOZZARELLA_3_50'); javascript:leftnavExpand('Menu_off_MOZZARELLA_3_50'); " onclick="layersShowOrHide('hidden','Lock_all'); layersShowOrHide('hidden','Event_MOZZARELLA_3_50'); layersShowOrHide('hidden','Lock_MOZZARELLA_3_50'); javascript:setImage('ExpandIcon_MOZZARELLA_3_50','images/icon_blank.gif'); ">X</a></td>
</tr>
<tr id="Menu_off_MOZZARELLA_3_50" style="display: all">
<td class="table_cell_left" colspan="2"><i>Click for more options</i></td>
</tr>
<tr id="Menu_on_MOZZARELLA_3_50" style="display: none">
<td colspan="2">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tbody><tr valign="top">
<td class="table_cell_left"><a href="CreateNewStudyEvent?studySubjectId=2&amp;studyEventDefinition=3"><span border="0" align="left" class="icon icon-clock2"> <span>CreateNewStudyEvent?studySubjectId=2&amp;studyEventDefinition=3</span>&nbsp;&nbsp;</span></a><a href="CreateNewStudyEvent?studySubjectId=2&amp;studyEventDefinition=3">Schedule</a></td>
</tr>
</tbody></table></td>
</tr>
</tbody></table></div></div></div></div></div></div></div></div></div></div></div><a href="javascript:leftnavExpand('Menu_on_MOZZARELLA_3_50'); javascript:leftnavExpand('Menu_off_MOZZARELLA_3_50'); " onmouseover="moveObject('Event_MOZZARELLA_3_50', event); setImage('ExpandIcon_MOZZARELLA_3_50','images/icon_expand.gif');" onmouseout="layersShowOrHide('hidden','Event_MOZZARELLA_3_50'); setImage('ExpandIcon_MOZZARELLA_3_50','images/icon_blank.gif');" onclick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_MOZZARELLA_3_50',event); "><span class="icon icon-clock" style="padding-top: 2px; padding-bottom: 3px;"><span style="color: #668cff; padding-left: 0px; font-size: 13px;"></span></span></a></td></tr></tbody></table></td>
        <td><a name="MOZZARELLA" class="pidVerification" id="pid-2" onmouseup="javascript:setImage('bt_View1','icon icon-search');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ViewStudySubject?id=2"><span hspace="2" border="0" title="View" alt="View" class="icon icon-search" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-cancel');" onmousedown="javascript:setImage('bt_View1','icon icon-cancel');" href="RemoveStudySubject?action=confirm&amp;id=2&amp;subjectId=2&amp;studyId=62"><span hspace="2" border="0" title="Remove" alt="View" class="icon icon-cancel" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;<a onmouseup="javascript:setImage('bt_View1','icon icon-icon-reassign3');" onmousedown="javascript:setImage('bt_View1','icon icon-search');" href="ReassignStudySubject?id=2"><span hspace="2" border="0" title="Reassign" alt="View" class="icon icon-icon-reassign3" name="bt_Reassign1"></span></a>&nbsp;&nbsp;&nbsp;</td>
    </tr>
    </tbody>
    <tbody>
    <tr class="statusBar">
        <td align="left" colspan="8">Results 1 - 50 of 63.</td>
    </tr>
    </tbody>
</table>