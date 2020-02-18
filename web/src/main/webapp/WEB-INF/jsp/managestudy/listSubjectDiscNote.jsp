<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>

<c:choose>
    <c:when test="${module eq 'manage'}">
        <jsp:include page="../include/managestudy-header.jsp"/>
        <c:set var="moduleStr" value="manage"/>
    </c:when>
    <c:otherwise>
    	<jsp:include page="../include/submit-header.jsp"/>
    	<c:set var="moduleStr" value="submit"/>
    </c:otherwise>
</c:choose>

<link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script>

<script type="text/javascript">
    function onInvokeAction(id,action) {
        if(id.indexOf('listDiscNotes') == -1)  {
        setExportToLimit(id, '');
        }
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        location.href = '${pageContext.request.contextPath}/ListDiscNotesSubjectServlet?module=submit&'+ parameterString;
    }
</script>



<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${restext}"/></b>

        <div class="sidebar_tab_content">

            <fmt:message key="select_subject_view_more_details_" bundle="${restext}"/>
            <br><br>
            <fmt:message key="click_download_disc" bundle="${restext}"/>

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${restext}"/></b>

    </td>
</tr>
<jsp:include page="../include/sideInfo.jsp"/>

<!-- the object inside the array is StudySubjectBean-->
<jsp:useBean scope='request' id='table' class='org.akaza.openclinica.web.bean.EntityBeanTable'/>
<%-- Map objects used for disc note summary statistics--%>
<c:set var="summaryMap" value="${summaryMap}" />
<c:set var="mapKeys" value="${mapKeys}" />

<%-- A summary of how any filters are being used; this value will be null if no filters have been
applied on resolution status or type--%>
<c:set var="filterSummary" value="${filterSummary}" />



<h1><span class="title_manage"><fmt:message key="manage_all_discrepancy_notes_in" bundle="${restext}"/> <c:out value="${study.name}"/></span></h1>
<!--Message about [] repeating events symbol; the key to filtering the flag icons and Disc Note types -->
<div class="dnKey"><strong><fmt:message key="Filter_by_status" bundle="${resword}"/>
    :</strong>

    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&type=${param.type}" <c:if test="${param.type == 50}">style="color:green"</c:if>><fmt:message key="all_notes" bundle="${resterm}"/></a>&nbsp;

    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&resolutionStatus=1&type=${param.type}"><img
      name="icon_Note" src="images/icon_Note.gif" border="0"
      alt="<fmt:message key="Open" bundle="${resterm}"/>" title="<fmt:message key="Open" bundle="${resterm}"/>"/></a> (<fmt:message key="Open" bundle="${resterm}"/>)&nbsp;

    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&resolutionStatus=2&type=${param.type}"><img
      name="icon_flagYellow" src="images/icon_flagYellow.gif" border="0"
      alt="<fmt:message key="Updated" bundle="${resterm}"/>" title="<fmt:message key="Updated" bundle="${resterm}"/>"/></a> (<fmt:message key="Updated" bundle="${resterm}"/>)&nbsp;

    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&resolutionStatus=3&type=${param.type}"><img
      name="icon_flagGreen" src="images/icon_flagGreen.gif" border="0"
      alt="<fmt:message key="Resolved" bundle="${resterm}"/>" title="<fmt:message key="Resolved" bundle="${resterm}"/>"/></a> (<fmt:message key="Resolved" bundle="${resterm}"/>)&nbsp;

    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&resolutionStatus=4&type=${param.type}"><img
      name="icon_flagBlack" src="images/icon_flagBlack.gif" border="0"
      alt="<fmt:message key="Closed" bundle="${resterm}"/>" title="<fmt:message key="Closed" bundle="${resterm}"/>"/></a> (<fmt:message key="Closed" bundle="${resterm}"/>)&nbsp;

    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&resolutionStatus=5&type=${param.type}"><img
      name="icon_flagNA" src="images/icon_flagWhite.gif" border="0"
      alt="<fmt:message key="Not_Applicable" bundle="${resterm}"/>" title="<fmt:message key="Not_Applicable" bundle="${resterm}"/>"/></a> (<fmt:message key="Not_Applicable" bundle="${resterm}"/>)&nbsp;

    &nbsp;<strong>[#] = <fmt:message key="Repeated_events" bundle="${resword}"/></strong>
    <br />
    <%-- filterSummary['status'] returns a List of Strings--%>
    <c:if test="${filterSummary != null && ! (empty filterSummary['status'])}">
        <fmt:message key="You_have_filtered_status" bundle="${resword}"/>
        <c:forEach var="statusName" items="${filterSummary['status']}">
            <strong>${statusName}; </strong>
        </c:forEach>
        <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&type=${param.type}"><fmt:message key="Clear_status_filter" bundle="${resword}"/></a>
    </c:if>
</div>

<div class="dnKey"><strong><fmt:message key="Filter_by_note_type" bundle="${resword}"/>
    :</strong>
    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&defId=${eventDefinitionId}&type=50" <c:if test="${param.type == 50}">style="color:green"</c:if>><fmt:message key="all_notes" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&defId=${eventDefinitionId}&type=2&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.type == 2}">style="color:green"</c:if>><fmt:message key="Annotation" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&defId=${eventDefinitionId}&type=1&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.type == 1}">style="color:green"</c:if>><fmt:message key="Failed_Validation_Check" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&defId=${eventDefinitionId}&type=3&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.type == 3}">style="color:green"</c:if>><fmt:message key="query" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&defId=${eventDefinitionId}&type=4&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.type == 4}">style="color:green"</c:if>><fmt:message key="reason_for_change" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <br />
    <c:if test="${filterSummary != null  && ! (empty filterSummary['type'])}">
        <fmt:message key="You_have_filtered_type" bundle="${resword}"/>
        <c:forEach var="typeName" items="${filterSummary['type']}">
            <strong>${typeName}; </strong>
        </c:forEach>
        <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&resolutionStatus=${param.resolutionStatus}&defId=${eventDefinitionId}&type=50"><fmt:message key="Clear_type_filter" bundle="${resword}"/></a>
    </c:if>
</div>
<div><a id="sumBoxParent" href="javascript:void(0)" onclick="showSummaryBox('sumBox',document.getElementById('sumBoxParent'),'<fmt:message key="show_summary_statistics" bundle="${resword}"/>','<fmt:message key="hide_summary_statistics" bundle="${resword}"/>')"> <img name="ExpandGroup1" src="images/bt_Expand.gif" border="0"><fmt:message key="show_summary_statistics" bundle="${resword}"/></a> </div>
<div id="sumBox" class="summaryBox" style="display:none;">
    <h3><fmt:message key="summary_statistics" bundle="${resword}"/></h3>
    <c:if test="${empty summaryMap}"><fmt:message key="There_are_no_discrepancy_notes" bundle="${resword}"/></c:if>
    <c:forEach var="mapkey"  varStatus="status" items="${mapKeys}">
        <c:if test="${summaryMap[mapkey]['Total'] > 0}">
            <span style="float:left;margin:5px">
            <strong> ${mapkey}</strong>:  ${summaryMap[mapkey]["Total"]} <br />

            <c:forEach var="statusType" items="${summaryMap[mapkey]}">
                <c:if test="${! ('Total' eq statusType.key)}">
                    ${statusType.key}: ${statusType.value} <br />
                </c:if>
            </c:forEach>
        </c:if>
        </span>
    </c:forEach>
</div>

<!--<p>List of all subjects with their enrollment dates and status of study events. Select any subject for details on his/her subject record and study events or assign or reassign him/her to a different study/site.</p>

<p>The list of subjects in the current study/site is shown below.</p>

<div class="homebox_bullets"><a href="AddNewSubject">Enroll a New Subject</a></div>
<p></p>
-->




<%-- added 11-2007 per rjenkins' request, tbh --%>

<!-- Invisible div to block other icons when menus are expanded -->

<script language="JavaScript">

    <!--

    document.write('<div id="Lock_all" style="position: absolute; visibility: hidden; z-index: 2; width: ' + (document.body.clientWidth - 180) + 'px; height: ' + (document.body.clientHeight - 271) + 'px; top: 243px; left: 180px;">');

    document.write('<img src="images/spacer.gif" style="width:' + (document.body.clientWidth - 180) + 'px; height:' + (document.body.clientHeight - 271) + 'px;" border="0">');

    document.write('</div>');

    //-->

</script>

<form  action="${pageContext.request.contextPath}/ListDiscNotesSubjectServlet?module=submit" style="clear:left; float:left;">
        <input type="hidden" name="module" value="submit">
        ${listDiscNotesHtml}
    </form>



<br><br>

<!-- EXPANDING WORKFLOW BOX -->
<div style="clear:left">
    <table border="0" cellpadding="0" cellspacing="0" style="position: relative; left: -14px;">
        <tr>
            <td id="sidebar_Workflow_closed" style="display: none">
                <a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="images/<fmt:message key="image_dir" bundle="${resformat}"/>/tab_Workflow_closed.gif" border="0"></a>
            </td>
            <td id="sidebar_Workflow_open" style="display: all">
                <table border="0" cellpadding="0" cellspacing="0" class="workflowBox">
                    <tr>
                        <td class="workflowBox_T" valign="top">
                            <table border="0" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td class="workflow_tab">
                                        <a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

                                        <b><fmt:message key="workflow" bundle="${restext}"/></b>

                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td class="workflowBox_T" align="right" valign="top"><img src="images/workflowBox_TR.gif"></td>
                    </tr>
                    <tr>
                        <td colspan="2" class="workflowbox_B">
                            <div class="box_R"><div class="box_B"><div class="box_BR">
                                <div class="workflowBox_center">


                                    <!-- Workflow items -->

                                    <table border="0" cellpadding="0" cellspacing="0">
                                        <tr>
                                            <td>

                                                <!-- These DIVs define shaded box borders -->
                                                <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

                                                    <div class="textbox_center" align="center">

                                                        <c:choose>
                                                        <c:when test="${userRole.manageStudy}">
                               <span class="title_manage">
                               <a href="ManageStudy"><fmt:message key="manage_study" bundle="${resworkflow}"/></a>
                             </c:when>
                             <c:otherwise>
                               <span class="title_submit">
                               <a href="ListStudySubjects"><fmt:message key="submit_data" bundle="${resworkflow}"/></a>
                             </c:otherwise>
                             </c:choose>




							</span>

                                                    </div>
                                                </div></div></div></div></div></div></div></div>

                                            </td>
                                            <td><img src="images/arrow.gif"></td>
                                            <td>

                                                <!-- These DIVs define shaded box borders -->
                                                <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

                                                    <div class="textbox_center" align="center">

                                                        <c:choose>
                                                        <c:when test="${userRole.manageStudy}">
                               <span class="title_manage">
                             </c:when>
                             <c:otherwise>
                               <span class="title_submit">
                             </c:otherwise>
                             </c:choose>

							<b><fmt:message key="list_discrepancy_notes" bundle="${restext}"/></b>

							</span>

                                                    </div>
                                                </div></div></div></div></div></div></div></div>

                                            </td>
                                        </tr>
                                    </table>


                                    <!-- end Workflow items -->

                                </div>
                            </div></div></div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</div>

<!-- END WORKFLOW BOX -->

<%--<c:import url="../include/workflow.jsp">
   <c:param name="module" value="manage"/>
</c:import>--%>

<jsp:include page="../include/footer.jsp"/>

