<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>


<jsp:include page="../include/managestudy-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<c:url var="paginatingQuery" value="${paginatingQuery}"/>
<c:set var="hasExtendedQuery" value="${! (paginatingQuery == null || paginatingQuery eq '')}"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${restext}"/></b>

        <div class="sidebar_tab_content">

            <fmt:message key="select_subject_view_more_details" bundle="${restext}"/>

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



<h1><span class="title_manage"><fmt:message key="manage_all_subjects_in" bundle="${restext}"/> <c:out value="${study.name}"/></span></h1>

<!--<p>List of all subjects with their enrollment dates and status of study events. Select any subject for details on his/her subject record and study events or assign or reassign him/her to a different study/site.</p>

<p>The list of subjects in the current study/site is shown below.</p>

<div class="homebox_bullets"><a href="AddNewSubject">Enroll a New Subject</a></div>
<p></p>
-->
<!---study event definition tabs -->
<table border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td style="padding-left: 12px" valign="bottom">
            <div id="Tab0NotSelected" style="display:none">
                <div class="tab_BG"><div class="tab_L"><div class="tab_R">
                    <c:choose>
                    <c:when test="${! hasExtendedQuery}">
                        <a class="tabtext" href="ListStudySubjects" onclick="javascript:HighlightTab(0);"><fmt:message key="all_events" bundle="${restext}"/></a>
                    </c:when>
                    <c:otherwise>
                    <a class="tabtext" href="ListStudySubjects?${paginatingQuery}" onclick="javascript:HighlightTab(0);">
                        </c:otherwise>
                        </c:choose>
                </div></div></div>
            </div>
            <div id="Tab0Selected" style="display:all">
                <div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h">

                    <span class="tabtext"><fmt:message key="all_events" bundle="${restext}"/></span>

                </div></div></div>
            </div>
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

            DisplayEventTabs()

            function DisplayEventTabs()
            {
                TabID=1;
                var extendedQuery = '${paginatingQuery}';

                while (TabID<=TabsNumber)

                {
                    defID = TabDefID[TabID-1];
                <c:choose>
                <c:when test="${hasExtendedQuery}">
                    url = "ListEventsForSubject?module=manage&defId=" + defID + "&tab=" + TabID + "&" + extendedQuery;
                </c:when>
                <c:otherwise>
                    url = "ListEventsForSubject?module=manage&defId=" + defID + "&tab=" + TabID;
                </c:otherwise>
                </c:choose>
                    if (TabID<=TabsShown)
                    {
                        document.write('<td valign="bottom" id="Tab' + TabID + '" style="display: all">');
                    }
                    else
                    {
                        document.write('<td valign="bottom" id="Tab' + TabID + '" style="display: none">');
                    }
                    document.write('<div id="Tab' + TabID + 'NotSelected" style="display:all"><div class="tab_BG"><div class="tab_L"><div class="tab_R">');
                    document.write('<a class="tabtext" title="' + TabFullName[(TabID-1)] + '" href=' + url + ' onclick="javascript:HighlightTab(' + TabID + ');">' + TabLabel[(TabID-1)] + '</a></div></div></div></div>');
                    document.write('<div id="Tab' + TabID + 'Selected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext">' + TabLabel[(TabID-1)] + '</span></div></div></div></div>');
                    document.write('</td>');

                    TabID++

                }
            }


            //-->
        </script>

        <td align="right"id="TabsNextDis" style="display: none"><img src="images/arrow_next_dis.gif" border="0"></td>
        <td align="right"id="TabsNext" style="display: all"><a href="javascript:TabsForward()"><img src="images/arrow_next.gif" border="0"></a></td>

    </tr>
</table>

<script language="JavaScript">
    <!--

    function showSubjectRow(strLeftNavRowElementName, groupNum, subjectRowID1,subjectRowID2 ){

        var objLeftNavRowElement;

        objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
        if (objLeftNavRowElement != null) {
            if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
            if (objLeftNavRowElement.display == "none" ){
                HideGroups(0,groupNum,10);
            }
        }
        leftnavExpand(subjectRowID1);
        leftnavExpand(subjectRowID2);
    }

    //-->
</script>

<%-- added 11-2007 per rjenkins' request, tbh --%>

<!-- Invisible div to block other icons when menus are expanded -->

<script language="JavaScript">

    <!--

    document.write('<div id="Lock_all" style="position: absolute; visibility: hidden; z-index: 2; width: ' + (document.body.clientWidth - 180) + 'px; height: ' + (document.body.clientHeight - 271) + 'px; top: 243px; left: 180px;">');

    document.write('<img src="images/spacer.gif" style="width:' + (document.body.clientWidth - 180) + 'px; height:' + (document.body.clientHeight - 271) + 'px;" border="0">');

    document.write('</div>');

    //-->

</script>


<c:import url="../include/showTableWithTabForSubject.jsp">
    <c:param name="rowURL" value="showStudySubjectRow.jsp" />
    <c:param name="groupNum" value="${groupSize}"/>
</c:import>

<c:import url="../submit/addNewSubjectExpress.jsp">
</c:import>

<br><br>

<c:import url="../include/workflow.jsp">
    <c:param name="module" value="manage"/>
</c:import>

<jsp:include page="../include/footer.jsp"/>

