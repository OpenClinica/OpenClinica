<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="resmessages"/>


<jsp:include page="include/managestudy_top_pages.jsp"/>

<!-- move the alert message to the sidebar-->
<jsp:include page="include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="../images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${restext}"/></b>

        <div class="sidebar_tab_content">

            <fmt:message key="design_implement_sdv_study_subject" bundle="${restext}"/>

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="../images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${restext}"/></b>

    </td>
</tr>
<jsp:include page="include/sideInfo.jsp"/>
<link rel="stylesheet" href="../includes/jmesa/jmesa.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery.jmesa.js"></script>
  <script type="text/javascript" language="JavaScript" src="../includes/jmesa/jquery-migrate-1.1.1.js"></script>
<script type="text/javascript">
    function onInvokeAction(id,action) {
        setExportToLimit(id, '');
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        //location.href = '${pageContext.request.contextPath}/ViewCRF?module=manage&crfId=' + '${crf.id}&' + parameterString;
    }
</script>

<h1><span class="title_manage">
<fmt:message key="sdv_sdv_for" bundle="${resword}"/> <c:out value="${study.name}"/>
    <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/monitor-and-manage-data')">
        <img src="../images/bt_Help_Manage.gif" border="0" alt="<fmt:message key="help" bundle="${restext}"/>" title="<fmt:message key="help" bundle="${restext}"/>"></a>
</span></h1>

<div id="searchFilterSDV">
    <table border="0" cellpadding="0" cellspacing="0">
        <tr>
            <td valign="bottom" id="Tab1'">
              <div id="Tab1Selected"><div class="tab_BG"><div class="tab_L"><div class="tab_R">
					<a class="tabtext" title="<fmt:message key="view_by_event_CRF" bundle="${resword}"/>" href='viewAllSubjectSDVtmp?sdv_restore=true&studyId=${studyId}' onclick="javascript:HighlightTab(1);"><fmt:message key="view_by_event_CRF" bundle="${resword}"/></a></div></div></div></div>                   
                <div id="Tab1NotSelected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext">View By Event CRF</span></div></div></div></div></td>

            <td valign="bottom" id="Tab2'">
                <div id="Tab2NotSelected"><div class="tab_BG"><div class="tab_L"><div class="tab_R">
					 <a class="tabtext" title="<fmt:message key="view_by_studysubjectID" bundle="${resword}"/>" href='viewSubjectAggregate?studyId=${studyId}' onclick="javascript:HighlightTab(2);"><fmt:message key="view_by_studysubjectID" bundle="${resword}"/></a></div></div></div></div>
                    
                <div id="Tab2Selected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext"><fmt:message key="view_by_studysubjectID" bundle="${resword}"/></span></div></div></div></div></td>
        </tr>
    </table>
    <script language="JavaScript">
        HighlightTab(2);
    </script>
    </div>

<script type="text/javascript">
    function prompt(formObj,theStudySubjectId){
        var bool = confirm(
                "<fmt:message key="uncheck_sdv" bundle="${resmessages}"/>");
        if(bool){
            formObj.action='${pageContext.request.contextPath}/pages/unSdvStudySubject';
            formObj.theStudySubjectId.value=theStudySubjectId;
            formObj.submit();
        }
    }
</script>
<div id="subjectSDV">
    <form name='sdvForm' action="${pageContext.request.contextPath}/pages/viewSubjectAggregate">
        <%--<fmt:message key="select_all_on_page" bundle="${resword}"/> <input type=checkbox name='checkSDVAll' onclick='selectAllChecks(this.form)'/>
        <br />--%>
        <input type="hidden" name="studyId" value="${param.studyId}">
        <input type="hidden" name="studySubjectId" value="${param.studySubjectId}">
        <%--This value will be set by an onclick handler associated with an SDV button --%>
        <input type="hidden" name="theStudySubjectId" value="0">
        <%-- the destination JSP page after removal or adding SDV for an eventCRF --%>
        <input type="hidden" name="redirection" value="viewSubjectAggregate">

        ${sdvTableAttribute}
        <br />
            <c:if test="${!(study.status.locked)}">
              <input type="submit" name="sdvAllFormSubmit" class="button_medium" value="<fmt:message key="sdv_all_checked" bundle="${resword}"/>" onclick="this.form.method='POST';this.form.action='${pageContext.request.contextPath}/pages/sdvStudySubjects';this.form.submit();"/>
           </c:if>
        <%--<input type="submit" name="sdvAllFormCancel" class="button_medium" value="Cancel" onclick="this.form.action='${pageContext.request.contextPath}/pages/viewSubjectAggregate';this.form.submit();"/>
    </form>--%>
    <script type="text/javascript">hideCols('s_sdv',[2,3,4])</script>

</div>
<jsp:include page="include/footer.jsp"/>
</body>
</html>