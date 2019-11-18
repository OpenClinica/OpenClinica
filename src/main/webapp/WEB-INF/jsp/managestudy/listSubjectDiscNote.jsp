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

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

        <fmt:message key="instructions" bundle="${restext}"/>

        <div class="sidebar_tab_content">

            <fmt:message key="select_subject_view_more_details_" bundle="${restext}"/>
            <br><br>
            <fmt:message key="click_download_disc" bundle="${restext}"/>

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>

        <fmt:message key="instructions" bundle="${restext}"/>

    </td>
</tr>
<jsp:include page="../include/sideInfo.jsp"/>

<!-- the object inside the array is StudySubjectBean-->
<jsp:useBean scope='request' id='table' class='core.org.akaza.openclinica.web.bean.EntityBeanTable'/>
<%-- Map objects used for disc note summary statistics--%>
<c:set var="summaryMap" value="${summaryMap}" />
<c:set var="mapKeys" value="${mapKeys}" />

<%-- A summary of how any filters are being used; this value will be null if no filters have been
applied on resolution status or type--%>
<c:set var="filterSummary" value="${filterSummary}" />



<h1>
    <c:choose>
    <c:when test="${module eq 'manage'}"><span class="title_manage"></c:when>
    <c:otherwise><span class="title_manage"></c:otherwise>
        </c:choose>
<fmt:message key="manage_all_discrepancy_notes_in" bundle="${restext}"/> <c:out value="${study.name}"/>
    <c:choose>
        <c:when test="${module eq 'manage'}">
            <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/monitor-and-manage-data/notes-and-discrepancies')">
            <img src="images/bt_Help_Manage.gif" border="0" alt="<fmt:message key="help" bundle="${restext}"/>" title="<fmt:message key="help" bundle="${restext}"/>">
            </a>
        </c:when>
        <c:otherwise>
            <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/monitor-and-manage-data/notes-and-discrepancies')">
            <img src="images/bt_Help_Manage.gif" border="0" alt="<fmt:message key="help" bundle="${restext}"/>" title="<fmt:message key="help" bundle="${restext}"/>">
            </a>
        </c:otherwise>
    </c:choose>
</span></h1>
<!--Message about [] repeating events symbol; the key to filtering the flag icons and Disc Note types -->
<div class="dnKey"><strong><fmt:message key="Filter_by_status" bundle="${resword}"/>
    :</strong>

    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&type=${param.type}" <c:if test="${param.type == 50}">style="color:green"</c:if>><fmt:message key="all_notes" bundle="${resterm}"/></a>&nbsp;

    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&resolutionStatus=1&type=${param.type}"><span class="icon icon-flag red"  border="0"
      alt="<fmt:message key="Open" bundle="${resterm}"/>" title="<fmt:message key="Open" bundle="${resterm}"/>"/></a> (<fmt:message key="Open" bundle="${resterm}"/>)&nbsp;

    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&resolutionStatus=2&type=${param.type}"><span class="icon icon icon-flag orange" border="0"
      alt="<fmt:message key="Updated" bundle="${resterm}"/>" title="<fmt:message key="Updated" bundle="${resterm}"/>"/></a> (<fmt:message key="Updated" bundle="${resterm}"/>)&nbsp;

    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&resolutionStatus=3&type=${param.type}"><span class="icon icon icon-flag green" border="0"
      alt="<fmt:message key="Resolved" bundle="${resterm}"/>" title="<fmt:message key="Resolved" bundle="${resterm}"/>"/></a> (<fmt:message key="Resolved" bundle="${resterm}"/>)&nbsp;

    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&resolutionStatus=4&type=${param.type}"><span class="icon icon-flag black" border="0"
      alt="<fmt:message key="Closed" bundle="${resterm}"/>" title="<fmt:message key="Closed" bundle="${resterm}"/>"/></a> (<fmt:message key="Closed" bundle="${resterm}"/>)&nbsp;

    <a href="ListDiscNotesSubjectServlet?module=${moduleStr}&resolutionStatus=5&type=${param.type}"><span class="icon icon-flag-empty blue" border="0"
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


<jsp:include page="../include/footer.jsp"/>

