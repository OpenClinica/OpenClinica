<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<script type="text/javascript" src="includes/wz_tooltip/wz_tooltip.js"></script>

<jsp:include page="../include/submit-header-inactive.jsp"/>

<jsp:include page="../include/userbox-inactive.jsp"/>
<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">
         <fmt:message key="enter_data_in_the_form" bundle="${restext}"/>
		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/submitSideInfo-inactive.jsp"/>

<jsp:useBean scope="request" id="section" class=
  "org.akaza.openclinica.bean.submit.DisplaySectionBean" />

<%-- set button text depending on whether or not the user is confirming values --%>
<c:choose>
	<c:when test="${section.checkInputs}">
		<c:set var="buttonAction" value="Save" />
		<c:set var="checkInputsValue" value="1" />
	</c:when>
	<c:otherwise>
		<c:set var="buttonAction" value="Confirm values" />
		<c:set var="checkInputsValue" value="0" />
	</c:otherwise>
</c:choose>

<table width="75%"><tr><td>
<h1><span class="title_manage"> <b> <c:out value="${toc.crf.name}" /> <c:out value="${toc.crfVersion.name}" />
         <c:choose>
            <c:when test="${eventCRF.stage.initialDE}">
                <img src="images/icon_InitialDE.gif" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>"
                     title="<fmt:message key="initial_data_entry" bundle="${resword}"/>">
            </c:when>
            <c:when
              test="${eventCRF.stage.initialDE_Complete}">
                <img src="images/icon_InitialDEcomplete.gif"
                     alt="<fmt:message key="initial_data_entry_complete" bundle="${resword}"/>"
                     title="<fmt:message key="initial_data_entry_complete" bundle="${resword}"/>">
            </c:when>
            <c:when test="${eventCRF.stage.doubleDE}">
                <img src="images/icon_DDE.gif" alt="<fmt:message key="double_data_entry" bundle="${resword}"/>"
                     title="<fmt:message key="double_data_entry" bundle="${resword}"/>">
            </c:when>
            <c:when test="${eventCRF.stage.doubleDE_Complete}">
                <img src="images/icon_DEcomplete.gif" alt="<fmt:message key="data_entry_complete" bundle="${resword}"/>"
                     title="<fmt:message key="data_entry_complete" bundle="${resword}"/>">
            </c:when>
            <c:when test="${eventCRF.stage.admin_Editing}">
                <img src="images/icon_AdminEdit.gif"
                     alt="<fmt:message key="administrative_editing" bundle="${resword}"/>" title="<fmt:message key="administrative_editing" bundle="${resword}"/>">
            </c:when>
            <c:when test="${eventCRF.stage.locked}">
                <img src="images/icon_Locked.gif" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>">
            </c:when>
            <c:when test="${eventCRF.stage.invalid}">
                <img src="images/icon_Invalid.gif" alt="<fmt:message key="invalid" bundle="${resword}"/>" title="<fmt:message key="invalid" bundle="${resword}"/>">
            </c:when>
            <c:otherwise>
              
            </c:otherwise>
        </c:choose></b>  &nbsp;&nbsp;</span> </h1> </td><td>
		<h1><span class="title_manage"> <c:out value="${studySubject.label}" />&nbsp;&nbsp; </span></h1></td></tr></table>
</div>

<form name="crfForm" method="POST" action="InitialDataEntry" onLoad = document.getElementById('CRF_infobox_closed').style.display='block';document.getElementById('CRF_infobox_open').style.display='none'" >
<input type="hidden" name="eventCRFId" value="<c:out value="${section.eventCRF.id}"/>" />
<input type="hidden" name="sectionId" value="<c:out value="${section.section.id}"/>" />
<input type="hidden" name="checkInputs" value="<c:out value="${checkInputsValue}"/>" />
<input type="hidden" name="tab" value="<c:out value="${tabId}"/>" />


<c:import url="interviewer.jsp"/>
<br><br>
<c:set var="sectionNum" value="0"/>
<c:forEach var="section" items="${toc.sections}">
<c:set var="sectionNum" value="${sectionNum+1}"/>
</c:forEach>



<!-- section tabs here -->
<table border="0" cellpadding="0" cellspacing="0">
   <tr>
	<td align="right" style="padding-left: 12px; display: none" id="TabsBack">
    <a href="javascript:TabsBack()"><img src="images/arrow_back.gif" border="0"></a></td>
	<td align="right" style="padding-left: 12px" id="TabsBackDis">
    <img src="images/arrow_back_dis.gif" border="0"/></td>


<script language="JavaScript">
<!--

// Total number of tabs (one for each CRF)
var TabsNumber = <c:out value="${sectionNum}"/>;


// Number of tabs to display at a time
var TabsShown = 3;


// Labels to display on each tab (name of CRF)
var TabLabel = new Array(TabsNumber)
var TabFullName = new Array(TabsNumber)
var TabSectionId = new Array(TabsNumber)
<c:set var="count" value="0"/>
<c:forEach var="section" items="${toc.sections}">
    TabFullName[<c:out value="${count}"/>]="<c:out value="${section.label}"/> (<c:out value="${section.numItemsCompleted}"/>/<c:out value="${section.numItems}" />)";

 	TabSectionId[<c:out value="${count}"/>]= <c:out value="${section.id}"/>;

 	TabLabel[<c:out value="${count}"/>]="<c:out value="${section.label}"/>";
    if (TabLabel[<c:out value="${count}"/>].length>8) {
      var shortName = TabLabel[<c:out value="${count}"/>].substring(0,7);
      TabLabel[<c:out value="${count}"/>]= shortName + '...' + "<span style='font-weight: normal;'>(<c:out value="${section.numItemsCompleted}"/>/<c:out value="${section.numItems}" />)</span>";
   } else {
     TabLabel[<c:out value="${count}"/>]="<c:out value="${section.label}"/> " + "<span style='font-weight: normal;'>(<c:out value="${section.numItemsCompleted}"/>/<c:out value="${section.numItems}" />)</span>";
   }

     <c:set var="count" value="${count+1}"/>
</c:forEach>
DisplaySectionTabs()



function DisplaySectionTabs()
	{
	TabID=1;

	while (TabID<=TabsNumber)

		{
		sectionId = TabSectionId[TabID-1];
		url = "InitialDataEntry?eventCRFId=" + <c:out value="${section.eventCRF.id}"/> + "&sectionId=" + sectionId + "&tab=" + TabID;
		currTabID = <c:out value="${tabId}"/>;

		if (TabID<=TabsShown)
			{
			document.write('<td valign="bottom" id="Tab' + TabID + '" style="display: all" >');
			}
		else
			{
			document.write('<td valign="bottom" id="Tab' + TabID + '" style="display: none" >');
			}
		if (TabID != currTabID) {
		document.write('<div id="Tab' + TabID + 'NotSelected" style="display:all"><div class="tab_BG"><div class="tab_L"><div class="tab_R">');
		document.write('<a class="tabtext" title="' + TabFullName[(TabID-1)] + '" href=' + url + ' onclick="return checkSectionStatus();">' + TabLabel[(TabID-1)] + '</a></div></div></div></div>');
		document.write('<div id="Tab' + TabID + 'Selected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext">' + TabLabel[(TabID-1)] + '</span></div></div></div></div>');
		document.write('</td>');
		}
		else {
		document.write('<div id="Tab' + TabID + 'NotSelected" style="display:all"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h">');
		document.write('<span class="tabtext">' + TabLabel[(TabID-1)] + '</span></div></div></div></div>');
		document.write('<div id="Tab' + TabID + 'Selected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext">' + TabLabel[(TabID-1)] + '</span></div></div></div></div>');
		document.write('</td>');
		}

		TabID++

		}
	}

function checkDataStatus() {

    objImage=document.getElementById('status_top');
    if (objImage != null && objImage.src.indexOf('images/icon_UnsavedData.gif')>0) {
       return confirm('<fmt:message key="you_have_unsaved_data" bundle="${resword}"/>');
    }

    return true;
}
function gotoLink() {

var OptionIndex=document.crfForm.sectionName.selectedIndex;
if (checkDataStatus()) {
  window.location = document.crfForm.sectionName.options[OptionIndex].value;
}
}


//-->
</script>

	<td align="right"id="TabsNextDis" style="display: none"><img src="images/arrow_next_dis.gif" border="0"></td>
	<td align="right"id="TabsNext"><a href="javascript:TabsForward()"><img src="images/arrow_next.gif" border="0"></a></td>
    <td>&nbsp;
       <div class="formfieldM_BG_noMargin"><select class="formfieldM" name="sectionName" size="1" onchange="gotoLink();">
       <c:set var="tabCount" value="1"/>
        <option selected>-- <fmt:message key="select_to_jump" bundle="${resword}"/> --</option>
       <c:forEach var="sec" items="${toc.sections}" >
        <c:set var="tabUrl" value = "InitialDataEntry?eventCRFId=${section.eventCRF.id}&sectionId=${sec.id}&tab=${tabCount}"/>
        <option value="<c:out value="${tabUrl}"/>"><c:out value="${sec.name}"/></option>
        <c:set var="tabCount" value="${tabCount+1}"/>
        </c:forEach>
        </select>
        </div>
     </td>
   </tr>
</table>

<jsp:include page="../include/showSubmitted.jsp" />

<jsp:include page="showSection.jsp" />

</form>

<c:import url="instructionsEnterData.jsp">
	<c:param name="currStep" value="dataEntry" />
</c:import>

<DIV ID="testdiv1" STYLE="position:absolute;visibility:hidden;background-color:white;"></DIV>


<jsp:include page="../include/footer-inactive.jsp"/>
