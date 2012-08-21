<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:useBean scope="request" id="section" class="org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<jsp:useBean scope="request" id="sec" class="org.akaza.openclinica.bean.submit.SectionBean" />
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>


<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>


<h1><span class="title_manage">

<!-- Preview CRF Version for <c:out value="${section.crf.name}" /> <c:out value="${section.crfVersion.name}" /> -->
<c:if test="${studySubject != null && studySubject.id>0}">
  <c:choose>
    <c:when test="${EventCRFBean.stage.name=='initial data entry'}">
	   <img src="images/icon_InitialDE.gif" alt="Initial Data Entry" title="Initial Data Entry">
    </c:when>
    <c:when test="${EventCRFBean.stage.name=='initial data entry complete'}">
	   <img src="images/icon_InitialDEcomplete.gif" alt="Initial Data Entry Complete" title="Initial Data Entry Complete">
    </c:when>
    <c:when test="${EventCRFBean.stage.name=='double data entry'}">
	   <img src="images/icon_DDE.gif" alt="Double Data Entry" title="Double Data Entry">
    </c:when>
    <c:when test="${EventCRFBean.stage.name=='data entry complete'}">
	   <img src="images/icon_DEcomplete.gif" alt="Data Entry Complete" title="Data Entry Complete">
    </c:when>
	<c:when test="${EventCRFBean.stage.name=='administrative editing'}">
	   <img src="images/icon_AdminEdit.gif" alt="Administrative Editing" title="Administrative Editing">
    </c:when>
    <c:when test="${EventCRFBean.stage.name=='locked'}">
	   <img src="images/icon_Locked.gif" alt="Locked" title="Locked">
    </c:when>
    <c:otherwise>
	   <img src="images/icon_Invalid.gif" alt="Invalid" title="Invalid">
	</c:otherwise>
  </c:choose>
  </c:if>
  </span></h1></span>

<script type="text/javascript" language="JavaScript">
// <![CDATA[
function getSib(theSibling){
  var sib;
  do {
   sib  = theSibling.previousSibling;
   if(sib.nodeType != 1){
     theSibling = sib;
   }
  } while(! (sib.nodeType == 1))

  return sib;
}
// ]]>
</script>


<c:if test="${studySubject != null && studySubject.id>0}">
<p>
<div class="homebox_bullets"><a href="ViewEventCRF?id=<c:out value="${EventCRFBean.id}"/>&studySubId=<c:out value="${studySubject.id}"/>">View Event CRF Properties</a></div>
<p>
<p>
<div class="homebox_bullets" style="width:117">


</div>
</c:if>


</span>

<c:if test="${studySubject != null && studySubject.id>0}">

<table border="0" cellpadding="0" cellspacing="0">
	<tr id="CRF_infobox_closed"  style="display: none;">
		<td style="padding-top: 3px; padding-left: 6px; width: 90px;" nowrap>
		<a href="javascript:leftnavExpand('CRF_infobox_closed'); leftnavExpand('CRF_infobox_open');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b>CRF Info</b>

		</td>
	</tr>
	<tr id="CRF_infobox_open" style="display: all">

		<td>
		<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="bottom">
				<table border="0" cellpadding="0" cellspacing="0" width="100">
					<tr>
						<td nowrap>
						<div class="tab_BG_h"><div class="tab_R_h" style="padding-right: 0px;"><div class="tab_L_h" style="padding: 3px 11px 0px 6px; text-align: left;">
						<a href="javascript:leftnavExpand('CRF_infobox_closed'); leftnavExpand('CRF_infobox_open');"><img src="images/sidebar_collapse.gif" border="0" align="right"></a>

						<b>CRF Info</b>

						</div></div></div>
						</td>
					</tr>
				</table>
				</td>
			</tr>
			<tr>

				<td valign="top">
	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TR"><div class="box_BL"><div class="box_BR">

		<div class="tablebox_center">


<table border="0" cellpadding="0" cellspacing="0" width="650" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
<tr>
<c:out value="${section.crf.name}" /> <c:out value="${section.crfVersion.name}" />&nbsp;&nbsp;
<c:choose>
    <c:when test="${EventCRFBean.stage.name=='initial data entry'}">
	   <img src="images/icon_InitialDE.gif" alt="Initial Data Entry" title="Initial Data Entry">
    </c:when>
    <c:when test="${EventCRFBean.stage.name=='initial data entry complete'}">
	   <img src="images/icon_InitialDEcomplete.gif" alt="Initial Data Entry Complete" title="Initial Data Entry Complete">
    </c:when>
    <c:when test="${EventCRFBean.stage.name=='double data entry'}">
	   <img src="images/icon_DDE.gif" alt="Double Data Entry" title="Double Data Entry">
    </c:when>
    <c:when test="${EventCRFBean.stage.name=='data entry complete'}">
	   <img src="images/icon_DEcomplete.gif" alt="Data Entry Complete" title="Data Entry Complete">
    </c:when>
	<c:when test="${EventCRFBean.stage.name=='administrative editing'}">
	   <img src="images/icon_AdminEdit.gif" alt="Administrative Editing" title="Administrative Editing">
    </c:when>
    <c:when test="${EventCRFBean.stage.name=='locked'}">
	   <img src="images/icon_Locked.gif" alt="Locked" title="Locked">
    </c:when>
    <c:otherwise>
	   <img src="images/icon_Invalid.gif" alt="Invalid" title="Invalid">
	</c:otherwise>
  </c:choose>
<tr>
<tr>
 <td class="table_cell_noborder" ><b><fmt:message key="study_subject_ID" bundle="${resword}"/>:</b><br></td>
 <td class="table_cell_noborder" ><c:out value="${studySubject.label}"/><br>
 </td>
 <c:choose>
 <c:when test="${study.studyParameterConfig.personIdShownOnCRF == 'true'}">

 <td class="table_cell_top" ><b><fmt:message key="person_ID" bundle="${resword}"/>:</b><br></td>
 <td class="table_cell_noborder" ><c:out value="${subject.uniqueIdentifier}"/><br></td>
 </c:when>
 <c:otherwise>
 <td class="table_cell_top" ><b><fmt:message key="person_ID" bundle="${resword}"/>:</b><br></td>
 <td class="table_cell_noborder" ><fmt:message key="na" bundle="${resword}"/></td>
 </c:otherwise>
 </c:choose>
</tr>
<tr>
 <td class="table_cell_noborder"><b><fmt:message key="study_site" bundle="${resword}"/>:</b><br></td>
 <td class="table_cell_noborder" ><c:out value="${studyTitle}"/><br></td>
 <td class="table_cell_top" ><b><fmt:message key="age" bundle="${resword}"/>:</b><br></td>
 <td class="table_cell_noborder"><c:choose><c:when test="${age!=''}"><c:out value="${age}"/></c:when>
	 <c:otherwise> <fmt:message key="na" bundle="${resword}"/></c:otherwise></c:choose><br></td>
</tr>
<tr>
 <td class="table_cell_noborder" ><b><fmt:message key="event" bundle="${resword}"/>:</b></td>
 <td class="table_cell_noborder" ><c:out value="${studyEvent.studyEventDefinition.name}"/> (<fmt:formatDate value="${studyEvent.dateStarted}" pattern="${dteFormat}"/>)</td>
 <td class="table_cell_top" ><b><fmt:message key="date_of_birth" bundle="${resword}"/>:</b><br></td>
 <td class="table_cell_noborder" ><fmt:formatDate value="${subject.dateOfBirth}" pattern="${dteFormat}"/><br></td>
</tr>
<tr>
  <td class="table_cell_noborder" ><b><fmt:message key="interviewer" bundle="${resword}"/>:</b></td>
  <td class="table_cell_noborder" ><c:out value="${EventCRFBean.interviewerName}"/> (<fmt:formatDate value="${EventCRFBean.dateInterviewed}" pattern="${dteFormat}"/>)</td>
  <td class="table_cell_top" ><b><fmt:message key="Gender" bundle="${resword}"/>:</b></td>
  <td class="table_cell_noborder" ><c:choose>
    <c:when test="${subject.gender==109}"><fmt:message key="M" bundle="${resword}"/></c:when>
    <c:when test="${subject.gender==102}"><fmt:message key="F" bundle="${resword}"/></c:when>
    <c:otherwise><c:out value="${subject.gender}"/></c:otherwise>
</c:choose></td>
</tr>
</table>



<span ID="spanAlert-interviewDate" class="alert"></span>
</td>
				</tr>
			</table>

	  	</td>
	</tr>
				</table>

		</div>

	</div></div></div></div></div></div></div>

</c:if>
<br>
<form name="sForm">
<!-- section tabs here -->
<table border="0" cellpadding="0" cellspacing="0">
   <tr>
<c:if test="${fn:length(toc.sections) gt 1}">
	<td align="right" style="padding-left: 12px; display: none" id="TabsBack">
	<a href="javascript:TabsBack()"><img src="images/arrow_back.gif" border="0"></a></td>
	<td align="right" style="padding-left: 12px; display: all" id="TabsBackDis"><img src="images/arrow_back_dis.gif" border="0"></td>
</c:if>

<script langauge="JavaScript">
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
DisplaySectionTabs();
selectTabs(${tabId},${sectionNum},'crfHeaderTabs');

function DisplaySectionTabs()
	{
	TabID=1;

	while (TabID<=TabsNumber)

		{
		sectionId = TabSectionId[TabID-1];
		<c:choose>
		<c:when test="${studySubject != null && studySubject.id>0}">
		 url = "SectionPreview?crfId="+<c:out value="${crfId}"/> +"&tabId=" + TabID;

		</c:when>
		<c:otherwise>
		 url = "SectionPreview?crfId="+<c:out value="${crfId}"/> +"&tabId=" + TabID;

		</c:otherwise>
		</c:choose>
		currTabID = <c:out value="${tabId}"/>;
		if (TabID<=TabsShown)
			{
			document.write('<td class="crfHeaderTabs" valign="bottom" id="Tab' + TabID + '">');
			}
		else
			{
			document.write('<td class="crfHeaderTabs" valign="bottom" id="Tab' + TabID + '">');
			}
		if (TabID != currTabID) {
		document.write('<div id="Tab' + TabID + 'NotSelected" style="display:all"><div class="tab_BG"><div class="tab_L"><div class="tab_R">');
	    document.write('<a class="tabtext" title="' + TabFullName[(TabID-1)] + '" href=' + url + '>' + TabLabel[(TabID-1)] + '</a></div></div></div></div>');
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


function gotoLink() {
var OptionIndex=document.sForm.sectionSelect.selectedIndex;
window.location = document.sForm.sectionSelect.options[OptionIndex].value;
}
//-->
</script>
<%-- if only one section show no arrows & section jump --%>
<c:if test="${fn:length(toc.sections) gt 1}">
	<td align="right"id="TabsNextDis" style="display: none"><img src="images/arrow_next_dis.gif" border="0"></td>
	<td align="right"id="TabsNext" style="display: all"><a href="javascript:TabsForward()"><img src="images/arrow_next.gif" border="0"></a></td>
    <td>&nbsp;
     <div class="formfieldM_BG_noMargin">
     <select class="formfieldM" name="sectionSelect" size="1" onchange="gotoLink();">
       <c:set var="tabCount" value="1"/>
        <option selected>-- <fmt:message key="select_to_jump" bundle="${resword}"/> --</option>
       <c:forEach var="sec" items="${toc.sections}" >
         <c:choose>
		  <c:when test="${studySubject != null && studySubject.id>0}">
		  <c:set var="tabUrl" value ="SectionPreview?crfId=${crfId}&tabId=${tabCount}"/>
    	  </c:when>
		  <c:otherwise>
		    <c:set var="tabUrl" value ="SectionPreview?crfId=${crfId}&tabId=${tabCount}"/>
    	  </c:otherwise>
		  </c:choose>
        <option value="<c:out value="${tabUrl}"/>"><c:out value="${sec.name}"/></option>
        <c:set var="tabCount" value="${tabCount+1}"/>
        </c:forEach>
        </select>
        </div>
     </td>
     </c:if>
   </tr>
</table>

 </form>

<c:choose>
  <c:when test="${studySubject != null && studySubject.id>0}">
    <jsp:include page="../submit/showFixedSection.jsp" />
  </c:when>
  <c:otherwise>
    <jsp:include page="../managestudy/showSectionWCPreview.jsp" />
  </c:otherwise>
</c:choose>
