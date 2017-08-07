<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>

<c:choose>
    <c:when test="${module eq 'manage'}">
        <jsp:include page="../include/managestudy-header.jsp"/>
    </c:when>
    <c:otherwise><jsp:include page="../include/submit-header.jsp"/>
    </c:otherwise>
</c:choose>

<link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">
<link rel="stylesheet" href="includes/font-awesome-4.7.0/css/font-awesome.css">
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script>
<script type="text/javascript">
    function onInvokeAction(id,action) {
        if(id.indexOf('listNotes') == -1)  {
        setExportToLimit(id, '');
        }
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        location.href = '${pageContext.request.contextPath}/ViewNotes?'+ parameterString;
    }
    function openPopup() {
        openDocWindow(window.location.href +'&print=yes')
    }
</script>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open">
        <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></a>

        <fmt:message key="instructions" bundle="${resword}"/>

        <div class="sidebar_tab_content">
            <fmt:message key="this_section_allows_a_study_manager_to_view_and_resolve" bundle="${restext}"/>
        </div>

        </td>

    </tr>
    <tr id="sidebar_Instructions_closed" style="display: none">
        <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></a>

        <fmt:message key="instructions" bundle="${resword}"/>

        </td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='table' class='org.akaza.openclinica.web.bean.EntityBeanTable'/>
<jsp:useBean scope='request' id='message' class='java.lang.String'/>

<h1><c:choose>
        <c:when test="${module eq 'manage'}"><span class="title_manage"></c:when>
        <c:otherwise><span class="title_manage"></c:otherwise>
        </c:choose>
    <fmt:message key="queries" bundle="${resword}"/>
    <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/monitor-and-manage-data/notes-and-discrepancies')">
        <c:choose>
        <c:when test="${module eq 'manage'}"><span class=""></span></c:when>
        <c:otherwise><span class=""></span></c:otherwise></c:choose></a>
 <%--<a href="javascript:openDocWindow('ViewNotes?print=yes')"--%>
<%--<a href="javascript:onInvokeAction('listNotes','filter')"--%>
    <%--onMouseDown="javascript:setImage('bt_Print0','images/bt_Print_d.gif');"--%>
    <%--onMouseUp="javascript:setImage('bt_Print0','images/bt_Print.gif');">--%>
    <%--<img name="bt_Print0" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print" bundle="${resword}"/>"></a>--%>
</span></h1><br/>
<%--
<div class="dnKey"><strong><fmt:message key="Filter_by_status" bundle="${resword}"/>
:</strong>

    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&type=50" <c:if test="${param.type == 50}">style="color:green"</c:if>>All Notes</a>&nbsp;

    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&resolutionStatus=1&discNoteType=${param.discNoteType}"><img
                      name="icon_Note" src="images/icon_Note.gif" border="0"
                      alt="<fmt:message key="Open" bundle="${resterm}"/>" title="<fmt:message key="Open" bundle="${resterm}"/>"/></a> (<fmt:message key="Open" bundle="${resterm}"/>)&nbsp;

    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&resolutionStatus=2&discNoteType=${param.discNoteType}"><img
                      name="icon_flagYellow" src="images/icon_flagYellow.gif" border="0"
                      alt="<fmt:message key="Updated" bundle="${resterm}"/>" title="<fmt:message key="Updated" bundle="${resterm}"/>"/></a> (<fmt:message key="Updated" bundle="${resterm}"/>)&nbsp;

    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&resolutionStatus=3&discNoteType=${param.discNoteType}"><img
                          name="icon_flagGreen" src="images/icon_flagGreen.gif" border="0"
                          alt="<fmt:message key="Resolved" bundle="${resterm}"/>" title="<fmt:message key="Resolved" bundle="${resterm}"/>"/></a> (<fmt:message key="Resolved" bundle="${resterm}"/>)&nbsp;

    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&resolutionStatus=4&discNoteType=${param.discNoteType}"><img
                             name="icon_flagBlack" src="images/icon_flagBlack.gif" border="0"
                             alt="<fmt:message key="Closed" bundle="${resterm}"/>" title="<fmt:message key="Closed" bundle="${resterm}"/>"/></a> (<fmt:message key="Closed" bundle="${resterm}"/>)&nbsp;

     <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&resolutionStatus=5&discNoteType=${param.type}"><img
      name="icon_flagNA" src="images/icon_flagWhite.gif" border="0"
      alt="<fmt:message key="Not_Applicable" bundle="${resterm}"/>" title="<fmt:message key="Not_Applicable" bundle="${resterm}"/>"/></a> (<fmt:message key="Not_Applicable" bundle="${resterm}"/>)&nbsp;
</div>

<div class="dnKey"><strong><fmt:message key="Filter_by_note_type" bundle="${resword}"/>
:</strong>
     <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&discNoteType=50&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.discNoteType == 50}">style="color:green"</c:if>><fmt:message key="all_notes" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&discNoteType=2&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.discNoteType == 2}">style="color:green"</c:if>><fmt:message key="Annotation" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&discNoteType=1&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.discNoteType == 1}">style="color:green"</c:if>><fmt:message key="Failed_Validation_Check" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&discNoteType=3&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.discNoteType == 3}">style="color:green"</c:if>><fmt:message key="query" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&discNoteType=4&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.discNoteType == 4}">style="color:green"</c:if>><fmt:message key="reason_for_change" bundle="${resterm}"/></a>

</div>
%-->


<%--<c:set var="module" value="${module}" scope="request"/>--%>
<%--
<c:import url="showNotesTable.jsp">
    <c:param name="rowURL" value="showDiscrepancyNoteRow.jsp" />
</c:import>
<br><br>
--%>

<div><a id="sumBoxParent" href="javascript:void(0)"
        onclick="showSummaryBox('sumBox',document.getElementById('sumBoxParent'),
        '<fmt:message key="show_summary_statistics" bundle="${resword}"/>',
        '<fmt:message key="hide_summary_statistics" bundle="${resword}"/>')">
    <img name="ExpandGroup1" src="images/bt_Collapse.gif" border="0">
    <fmt:message key="hide_summary_statistics" bundle="${resword}"/></a>
</div>
<div style="display:block; width:600px;">
    <%--<h3>Summary statistics</h3>--%>
    <c:if test="${empty summaryMap}"><fmt:message key="There_are_no_discrepancy_notes" bundle="${resword}"/></c:if>
    <!-- NEW Summary-->
    <table border="0" cellspacing="0" class="summaryTable" style="width:600px;">
        <tr><td>&nbsp;</td>
            <td>&nbsp;</td>
            <td align="center"><strong>Query</strong></td>
        </tr>
            <c:forEach var="status" items="${mapKeys}">
                <tr>
                    <td><strong>${status.name}</strong></td>
                    <td><span class="${status.iconFilePath}" border="0" align="right"></td>
                    <td align="center"> ${summaryMap[status.name]['Total']}</td>
                </tr>
            </c:forEach>
        <tr>
            <td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>
        </tr>
        <tr>
            <td>&nbsp;</td><td>&nbsp;</td>
            <td align="center">${grandTotal}</td>
        </tr>
    </table>
    <!-- End Of New Summary -->
</div>

<form  action="${pageContext.request.contextPath}/ViewNotes" style="clear:left; float:left;">
        <input type="hidden" name="module" value="submit">
        ${viewNotesHtml}
    </form>

<jsp:include page="../include/footer.jsp"/>
