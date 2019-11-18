<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>



<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>


<c:choose>
   <c:when test="${userRole.manageStudy && module eq 'manage'}">
    <c:import url="../include/managestudy-header.jsp"/>
   </c:when>
   <c:otherwise>
    <c:import url="../include/submit-header.jsp"/>
   </c:otherwise>
  </c:choose>

<link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-3.1.0.min.js"></script>

<script type="text/javascript">
    function onInvokeAction(id,action) {
        if(id.indexOf('listDiscNotesForCRF') == -1)  {
        setExportToLimit(id, '');
        }
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        location.href = '${pageContext.request.contextPath}/ListDiscNotesForCRFServlet? + module=manage&defId=' + '${defId}&' + parameterString;
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

      <fmt:message key="select_subject_view_more_details" bundle="${restext}"/>

    </div>

    </td>

  </tr>
  <tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">

    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

    <fmt:message key="instructions" bundle="${restext}"/>

    </td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<!-- the object inside the array is StudySubjectBean-->
<jsp:useBean scope='request' id='table' class='core.org.akaza.openclinica.web.bean.EntityBeanTable'/>
<%-- eventDefinitionId passed into the servlet --%>




<h1>
<c:choose>
   <c:when test="${userRole.manageStudy && module=='manage'}">
    <span class="title_manage">
    <fmt:message key="manage_all_discrepancy_notes_in" bundle="${restext}"/> <c:out value="${study.name}"/>
    <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/monitor-and-manage-data/notes-and-discrepancies')"><img src="images/bt_Help_Manage.gif" border="0" alt="<fmt:message key="help" bundle="${restext}"/>" title="<fmt:message key="help" bundle="${restext}"/>"></a></span></h1>
   </c:when>
   <c:otherwise>
    <span class="title_manage">
    <fmt:message key="view_all_discrepancy_notes_in" bundle="${restext}"/> <c:out value="${study.name}"/>

<a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/monitor-and-manage-data/notes-and-discrepancies')"><img src="images/bt_Help_Manage.gif" border="0" alt="<fmt:message key="help" bundle="${restext}"/>" title="<fmt:message key="help" bundle="${restext}"/>"></a></span></h1>
   </c:otherwise>
  </c:choose>

<div class="dnKey"><strong><fmt:message key="Filter_by_status" bundle="${resword}"/>
:</strong>

    <a href="ListDiscNotesForCRFServlet?type=${param.type}&module=${module}&defId=<c:out value="${eventDefinitionId}"/>&tab=1" <c:if test="${param.type == 50}">style="color:green"</c:if>><fmt:message key="all_notes" bundle="${resterm}"/></a>&nbsp;

    <a href="ListDiscNotesForCRFServlet?resolutionStatus=1&type=${param.type}&tab=${param.tab}&module=${module}&defId=<c:out value="${eventDefinitionId}"/>"><span class="icon icon-flag red" border="0"
                      alt="<fmt:message key="Open" bundle="${resterm}"/>" title="<fmt:message key="Open" bundle="${resterm}"/>"/></a> (<fmt:message key="Open" bundle="${resterm}"/>)&nbsp;

    <a href="ListDiscNotesForCRFServlet?resolutionStatus=2&type=${param.type}&tab=${param.tab}&module=${module}&defId=<c:out value="${eventDefinitionId}"/>"><span class="icon icon icon-flag orange" border="0"
                      alt="<fmt:message key="Updated" bundle="${resterm}"/>" title="<fmt:message key="Updated" bundle="${resterm}"/>"/></a> (<fmt:message key="Updated" bundle="${resterm}"/>)&nbsp;

    <a href="ListDiscNotesForCRFServlet?resolutionStatus=3&type=${param.type}&tab=${param.tab}&module=${module}&defId=<c:out value="${eventDefinitionId}"/>"><span class="icon icon icon-flag green" border="0"
                          alt="<fmt:message key="Resolved" bundle="${resterm}"/>" title="<fmt:message key="Resolved" bundle="${resterm}"/>"/></a> (<fmt:message key="Resolved" bundle="${resterm}"/>)&nbsp;

    <a href="ListDiscNotesForCRFServlet?resolutionStatus=4&type=${param.type}&tab=${param.tab}&module=${module}&defId=<c:out value="${eventDefinitionId}"/>"><span class="icon icon-flag black" border="0"
                             alt="<fmt:message key="Closed" bundle="${resterm}"/>" title="<fmt:message key="Closed" bundle="${resterm}"/>"/></a> (<fmt:message key="Closed" bundle="${resterm}"/>)&nbsp;

    <a href="ListDiscNotesForCRFServlet?resolutionStatus=5&type=${param.type}&tab=${param.tab}&module=${module}&defId=<c:out value="${eventDefinitionId}"/>"><span class="icon icon-flag-empty blue" border="0"
                             alt="<fmt:message key="Not_Applicable" bundle="${resterm}"/>" title="<fmt:message key="Not_Applicable" bundle="${resterm}"/>"/></a> (<fmt:message key="Not_Applicable" bundle="${resterm}"/>)&nbsp;

     <br />
    <%-- filterSummary['status'] returns a List of Strings--%>
    <c:if test="${filterSummary != null && ! (empty filterSummary['status'])}">
        <fmt:message key="You_have_filtered_status" bundle="${resword}"/>
        <c:forEach var="statusName" items="${filterSummary['status']}">
            <strong>${statusName}; </strong>
        </c:forEach>
        <a href="ListDiscNotesForCRFServlet?type=${param.type}&module=${module}&defId=<c:out value="${eventDefinitionId}"/>&tab=1"><fmt:message key="Clear_status_filter" bundle="${resword}"/></a>
    </c:if>
</div>
<div class="dnKey"><strong><fmt:message key="Filter_by_note_type" bundle="${resword}"/>
:</strong>
    <a href="ListDiscNotesForCRFServlet?module=${module}&defId=${eventDefinitionId}&tab=${param.tab}&type=50" <c:if test="${param.type == 50}">style="color:green"</c:if>><fmt:message key="all_notes" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ListDiscNotesForCRFServlet?module=${module}&resolutionStatus=${param.resolutionStatus}&defId=${eventDefinitionId}&tab=${param.tab}&type=2" <c:if test="${param.type == 2}">style="color:green"</c:if>><fmt:message key="Annotation" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ListDiscNotesForCRFServlet?module=${module}&resolutionStatus=${param.resolutionStatus}&defId=${eventDefinitionId}&tab=${param.tab}&type=1" <c:if test="${param.type == 1}">style="color:green"</c:if>><fmt:message key="Failed_Validation_Check" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ListDiscNotesForCRFServlet?module=${module}&resolutionStatus=${param.resolutionStatus}&defId=${eventDefinitionId}&tab=${param.tab}&type=3" <c:if test="${param.type == 3}">style="color:green"</c:if>><fmt:message key="query" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ListDiscNotesForCRFServlet?module=${module}&resolutionStatus=${param.resolutionStatus}&defId=${eventDefinitionId}&tab=${param.tab}&type=4" <c:if test="${param.type == 4}">style="color:green"</c:if>><fmt:message key="reason_for_change" bundle="${resterm}"/></a>
     <br />
    <c:if test="${filterSummary != null  && ! (empty filterSummary['type'])}">
        <fmt:message key="You_have_filtered_type" bundle="${resword}"/>
        <c:forEach var="typeName" items="${filterSummary['type']}">
            <strong>${typeName}; </strong>
        </c:forEach>
        <a href="ListDiscNotesForCRFServlet?module=${module}&resolutionStatus=${param.resolutionStatus}&defId=${eventDefinitionId}&tab=${param.tab}&type=50"><fmt:message key="Clear_type_filter" bundle="${resword}"/></a>
    </c:if>
</div>





<!--<p>The following is a list of all the subjects enrolled in the
 study, together with the status of each subject's
study events.  Select any subject to view more details and to enter subject event data.
You may also enroll a new subject and add a new study event:

<div class="homebox_bullets"><a href="AddNewSubject?instr=1">Enroll a New Subject</a></div><br>

<div class="homebox_bullets"><a href="CreateNewStudyEvent">Add a New Study Event</a></div><br>
-->

<!---study event definition tabs -->
<%--
<table border="0" cellpadding="0" cellspacing="0">
   <tr>
  <td style="padding-left: 12px" valign="bottom">
        <!---start allevents tab here -->
    <div id="Tab0NotSelected" style="display:none">
  <div class="tab_BG"><div class="tab_L"><div class="tab_R">

<c:choose>
   <c:when test="${userRole.manageStudy}">
    <a class="tabtext" href="ListDiscNotesSubjectServlet?module=manage&type=${param.type}&resolutionStatus=${param.resolutionStatus}" onclick="javascript:HighlightTab(0);"><fmt:message key="all_events" bundle="${restext}"/></a>
   </c:when>
   <c:otherwise>
    <a class="tabtext" href="ListDiscNotesSubjectServlet?module=submit&type=${param.type}&resolutionStatus=${param.resolutionStatus}" onclick="javascript:HighlightTab(0);"><fmt:message key="all_events" bundle="${restext}"/></a>
   </c:otherwise>
  </c:choose>


  </div></div></div>
  </div>
  <div id="Tab0Selected">
  <div class="tab_BG"><div class="tab_L"><div class="tab_R">

<c:choose>
   <c:when test="${userRole.manageStudy}">
    <a class="tabtext" href="ListDiscNotesSubjectServlet?module=manage&type=${param.type}&resolutionStatus=${param.resolutionStatus}" onclick="javascript:HighlightTab(0);"><fmt:message key="all_events" bundle="${restext}"/></a>
   </c:when>
   <c:otherwise>
    <a class="tabtext" href="ListDiscNotesSubjectServlet?module=submit&type=${param.type}&resolutionStatus=${param.resolutionStatus}" onclick="javascript:HighlightTab(0);"><fmt:message key="all_events" bundle="${restext}"/></a>
   </c:otherwise>
  </c:choose>

  </div></div></div>
  </div>
         <!---end allevents tab here -->
    </td>
  <td align="right" style="padding-left: 12px; display: none" id="TabsBack"><a href="javascript:TabsBack()"><img src="images/arrow_back.gif" border="0"></a></td>
  <td align="right" style="padding-left: 12px; display: all" id="TabsBackDis"><img src="images/arrow_back_dis.gif" border="0"></td>


<script language="JavaScript">
<!--

// Total number of tabs (one for each CRF)
var TabsNumber = <c:out value="${allDefsNumber}"/>;

// Number of tabs to display at a time
var TabsShown = 3;

// Labels to display on each tab (name of CRF)
var TabLabel = new Array(TabsNumber)
var TabFullName = new Array(TabsNumber)
var TabDefID = new Array(TabsNumber)
   <c:set var="count" value="0"/>
 <c:forEach var="def" items="${allDefsArray}">
   TabFullName[<c:out value="${count}"/>]= "<c:out value="${def.name}"/>";

   TabLabel[<c:out value="${count}"/>]= "<c:out value="${def.name}"/>";
   if (TabLabel[<c:out value="${count}"/>].length>12)
   {
    var shortName = TabLabel[<c:out value="${count}"/>].substring(0,11);
    TabLabel[<c:out value="${count}"/>]= shortName + '...';
   }
   TabDefID[<c:out value="${count}"/>]= "<c:out value="${def.id}"/>";
   <c:set var="count" value="${count+1}"/>
 </c:forEach>

DisplaySectionTabs()

function DisplaySectionTabs()
  {
  TabID=1;

  while (TabID<=TabsNumber)

    {
    defID = TabDefID[TabID-1];
    url = "ListDiscNotesForCRFServlet?module=${module}&type=${param.type}&resolutionStatus=${param.resolutionStatus}&defId=" + defID + "&tab=" + TabID;
    currTabID = <c:out value="${tabId}"/>;
    if (TabID<=TabsShown)
      {
      document.write('<td valign="bottom" id="Tab' + TabID + '" style="display: all">');
      }
    else
      {
      document.write('<td valign="bottom" id="Tab' + TabID + '" style="display: none">');
      }
      if (TabID != currTabID) {
    document.write('<div id="Tab' + TabID + 'NotSelected" style="display:all"><div class="tab_BG"><div class="tab_L"><div class="tab_R">');
    document.write('<a class="tabtext" title="' + TabFullName[(TabID-1)] + '" href=' + url + ' onclick="javascript:HighlightTab(' + TabID + ');">' + TabLabel[(TabID-1)] + '</a></div></div></div></div>');
    document.write('<div id="Tab' + TabID + 'Selected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext">' + TabLabel[(TabID-1)] + '</span></div></div></div></div>');
    document.write('</td>');
    }
    else {
    //alert(TabID);
    document.write('<div id="Tab' + TabID + 'NotSelected" style="display:all"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h">');
    document.write('<span class="tabtext">' + TabLabel[(TabID-1)] + '</span></div></div></div></div>');
    document.write('<div id="Tab' + TabID + 'Selected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext">' + TabLabel[(TabID-1)] + '</span></div></div></div></div>');
    document.write('</td>');
    }

    TabID++

    }
  }


//-->
</script>
  <td align="right" id="TabsNextDis" style="display: none"><img src="images/arrow_next_dis.gif" border="0"></td>
  <td align="right" id="TabsNext"><a href="javascript:TabsForward()"><img src="images/arrow_next.gif" border="0"></a></td>

   </tr>
</table>

<c:import url="../include/showTableWithTab.jsp">
  <c:param name="rowURL" value="showDNotesForCRFs.jsp" />
  <c:param name="groupNum" value="${groupSize}"/>
  <c:param name="eventDefCRFNum" value="${eventDefCRFSize}"/>
    <c:param name="resolutionStatus" value="${param.resolutionStatus}"/>
    <c:param name="discNoteType" value="${discrepancyNoteType}"/>
    <c:param name="module" value="${module}"/>
    <c:param name="studyHasDiscNotes" value="${studyHasDiscNotes}"/>
    <c:param name="suppressAddSubject" value="true"/>
</c:import>
--%>

<br><br>

<div id="listDiscNotesDiv">
    <form  action="${pageContext.request.contextPath}/ListDiscNotesForCRFServlet">
        <input type="hidden" name="module" value="submit">
        <input type="hidden" name="defId" value="${defId}">
        ${listDiscNotesForCRFHtml}
    </form>
</div>

