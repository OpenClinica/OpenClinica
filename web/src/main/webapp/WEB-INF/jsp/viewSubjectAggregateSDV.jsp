<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="include/managestudy_top_pages.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${restext}"/></b>

        <div class="sidebar_tab_content">

            <fmt:message key="design_implement_sdv" bundle="${restext}"/>

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${restext}"/></b>

    </td>
</tr>
<jsp:include page="include/sideInfo.jsp"/>
<link rel="stylesheet" href="../includes/jmesa/jmesa.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery-1.2.3.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jmesa.js"></script>

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

<div id="searchFilterSDV">
    <table border="0" cellpadding="0" cellspacing="0">
        <tr>
            <td valign="bottom" id="Tab1'">
                <div id="Tab1NotSelected"><div class="tab_BG"><div class="tab_L"><div class="tab_R">
                    <a class="tabtext" title="<fmt:message key="view_by_event_CRF" bundle="${resword}"/>" href='viewAllSubjectSDV?studyId=49&decorator=mydecorator' onclick="javascript:HighlightTab(1);"><fmt:message key="view_by_event_CRF" bundle="${resword}"/></a></div></div></div></div>
                <div id="Tab1Selected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext"><fmt:message key="view_by_event_CRF" bundle="${resword}"/></span></div></div></div></div></td>

            <td valign="bottom" id="Tab2'">
                <div id="Tab2NotSelected"><div class="tab_BG"><div class="tab_L"><div class="tab_R">
					<a class="tabtext" title="<fmt:message key="view_by_studysubjectID" bundle="${resword}"/>" href='viewSubjectSDV?studyId=${studyId}&studySubjectId=${studySubjectId}&decorator=mydecorator' onclick="javascript:HighlightTab(2);"><fmt:message key="view_by_studysubjectID" bundle="${resword}"/></a></div></div></div></div>
                <div id="Tab2Selected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext"><fmt:message key="view_by_studysubjectID" bundle="${resword}"/></span></div></div></div></div></td>
                    
        </tr>
    </table>
    <script language="JavaScript">
        HighlightTab(3);
    </script>

<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center">
<form method="POST" action="ViewStudyEvents" name="control">
<table border="0" cellpadding="0" cellspacing="0">
<tr valign="top"><b><fmt:message key="filter_events_by" bundle="${resword}"/>:</b></tr>
<tr valign="top">
  <td ><fmt:message key="study_subject" bundle="${resword}"/>:</td>
  <td colspan="2">
      <div class="formfieldL_BG" style="margin-right:10px">
         <input type="text" name="study_subject_id" class="formfieldS" />
     </div>
   </td>
   <td><fmt:message key="study_subject_status" bundle="${resword}"/>:</td>
   <td>
   <div class="formfieldM_BG" style="margin-right:10px">
     <select name="studySubjectStatusId" class="formfieldM">
      <option value="0">--<fmt:message key="all" bundle="${resword}"/>--</option>
      <c:forEach var="studySubjectStatus" items="${studySubjectStatuses}">
                <option value="<c:out value="${studySubjectStatus.id}"/>"><c:out value="${studySubjectStatus.name}"/>
      </c:forEach>
     </select>
   </div>
   </td>
     <td><fmt:message key="study_subject_group" bundle="${resword}"/>:</td>
   <td>
   <div class="formfieldM_BG" style="margin-right:10px">
     <select name="studySubjectGroup" class="formfieldM">
      <option value="0">--<fmt:message key="all" bundle="${resword}"/>--</option>
      <c:forEach var="studySubjectGroup" items="${studySubjectGroups}">

                <option value="<c:out value="${studySubjectGroup.id}"/>"><c:out value="${studySubjectGroup.name}"/>

    </c:forEach>
     </select>
   </div>
   </td>
  </tr>
<tr valign="top">
 <td><fmt:message key="sdv_requirement_type" bundle="${resword}"/>: </td>
 <td colspan="5"> <div class="formfieldM_BG">
     <select name="sdvRequireType" class="formfieldM">
      <option value="0">--<fmt:message key="all" bundle="${resword}"/>--</option>
      <c:forEach var="sdvRequireType" items="${sdvRequireTypes}">

                <option value="<c:out value="${sdvRequireType.id}"/>"><c:out value="${sdvRequireType.name}"/>
      </c:forEach>
     </select>
   </div>
  </td>
 </tr>
<tr valign="top">
 <td><fmt:message key="sdv_required_percent_complete" bundle="${resword}"/>: </td>
 <td colspan="4"> <div class="formfieldM_BG">
     <c:set var="percentages">10,25,50,75,100</c:set>
     <select name="sdvRequiredPercentComplete" class="formfieldM">

      <option value="0">--<fmt:message key="all" bundle="${resword}"/>--</option>
      <c:forTokens items="${percentages}" delims="," var="percentage">
                <option value="<c:out value="${percentage}"/>"><c:out value="${percentage}"/>
      </c:forTokens>
     </select>
   </div>
  </td>
    <td><input type="submit" name="submit" value="<fmt:message key="apply_filter" bundle="${resword}"/>" class="button_medium"></td>
 </tr>
</table>
</form>
</div>
</div></div></div></div></div></div></div></div>
</div>

<div id="subjectSDV">
    <a href="javascript:void(0)"
       onclick="if(this.innerHTML.indexOf('<fmt:message key="show_more" bundle="${resword}"/>') == -1)
    {hideCols('s_sdv',[2]);}
    else {hideCols('s_sdv',[2],true);} toggleName(this);"><fmt:message key="show_more" bundle="${resword}"/></a>
    <form  action="${pageContext.request.contextPath}/pages/viewSubjectAggregateSDV">
        <input type="hidden" name="studyId" value="${param.studyId}">
<!--        <input type="hidden" name="decorator" value="mydecorator"> -->
        ${sdvTableAttribute}
    </form>
    <script type="text/javascript">hideCols('s_sdv',[2])</script>
</div>

<%-- view all subjects ends here --%>
<br>

<c:import url="include/workflow.jsp">
    <c:param name="module" value="manage"/>
</c:import>

<jsp:include page="include/footer.jsp"/>
