<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"
    prefix="fn" %>

<link rel="shortcut icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/favicon.ico">


<jsp:useBean scope="request" id="section" class="org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<jsp:useBean scope="request" id="displayItem" class="org.akaza.openclinica.bean.submit.DisplayItemBean" />
<jsp:useBean scope='request' id='formMessages' class='java.util.HashMap'/>
<jsp:useBean scope='request' id='exitTo' class='java.lang.String'/>
<jsp:useBean scope='request' id='nameNotes' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='intrvDates' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='existingNameNotes' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='existingIntrvDateNotes' class='java.util.ArrayList'/>


<script type="text/javascript" src="includes/wz_tooltip/wz_tooltip.js"></script>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<c:set var="interviewer" value="${toc.eventCRF.interviewerName}" />
<c:set var="interviewDate" value="${toc.eventCRF.dateInterviewed}" />
<c:set var="itemId" value="${displayItem.item.id}" />
<c:set var="contextPath" value="${fn:replace(pageContext.request.requestURL, fn:substringAfter(pageContext.request.requestURL, pageContext.request.contextPath), '')}" />

<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script>
<style type="text/css">

.tooltip {
		
	width:100%;
	
}

</style>


<script type="text/javascript" language="javascript">



    //If someone closes the browser on data entry stage, the following request should be
    //sent to the server to make this CRF available for data entry.
var closing = true;
    function clsWin() {
        if(closing) {
            jQuery.post("CheckCRFLocked?userId=<c:out value="${userBean.id}"/>&exitTo=<c:out value="${exitTo}" />", function(data){
                return;
            });
        }
    }
    jQuery(document).ready(function(){
       jQuery("a").click(function(event){
           closing = false;
       });
       jQuery("input").click(function(event){
           closing = false;
       });
       jQuery("select").click(function(event){
           closing = false;
        });
       //jquery('.CRF_infobox_closed').show();
      
     //  jQuery('#nameNote1').mouseover(function(event){
       //  jQuery.getJSON("InitialDataEntry",{ name:0 },function(discrepancyNote){
    	//		alert('w'+discrepancyNote);
        //});
       // });
      
       });
     




   
      function genToolTipFromArray(flag){
    	  var resStatus = new Array();
    	  var detailedNotes= new Array();
    	  var discrepancyType = new Array();
    	  var updatedDates = new Array();
		   var parentDnids = new Array();
		   var totNotes = 0;
		   var footNote = '<fmt:message key="footNote" bundle="${resword}"/>';
    	  var i=0;
    	  var discNotes = new Array();
    	  var title = '<fmt:message key="tooltip_title1" bundle="${resword}"/>';
    	  	if(flag =='interviewNotes')
    	     	{
    	     	<c:forEach var="discrepancyNoteBeans" items="${nameNotes}">
    	     		resStatus[i]=<c:out value="${discrepancyNoteBeans.resolutionStatusId}"/>;
    	     		detailedNotes[i]= '<c:out value="${discrepancyNoteBeans.description}"/>';   			
    	  			discrepancyType[i] = '<c:out value="${discrepancyNoteBeans.disType.name}"/>';
    	  			updatedDates[i]= '<c:out value="${discrepancyNoteBeans.createdDate}"/>';
					parentDnids[i] = '<c:out value="${discrepancyNoteBeans.parentDnId}"/>';
    	  			i++;
    	  	 	</c:forEach>
    	  	 	title = '<fmt:message key="tooltip_name_title" bundle="${resword}"/>';
				totNotes = ${fn:length(existingNameNotes)};
				if(totNotes >0) footNote = totNotes + " " + '<fmt:message key="foot_threads" bundle="${resword}"/>' + " " + '<fmt:message key="footNote_threads" bundle="${resword}"/>';
    	     	}
    	   	else if(flag =='dateNotes')
    	     {
    	     	<c:forEach var="discrepancyNoteBeans" items="${intrvDates}">
    	     		resStatus[i]=<c:out value="${discrepancyNoteBeans.resolutionStatusId}"/>;
    	     		detailedNotes[i]= '<c:out value="${discrepancyNoteBeans.description}"/>';   			
    	  			discrepancyType[i] = '<c:out value="${discrepancyNoteBeans.disType.name}"/>';
    	  			updatedDates[i]= '<c:out value="${discrepancyNoteBeans.createdDate}"/>';
					parentDnids[i] = '<c:out value="${discrepancyNoteBeans.parentDnId}"/>';
    	  			i++;
    	     	</c:forEach>
    	   title = '<fmt:message key="tooltip_name_title" bundle="${resword}"/>';
		   totNotes = ${fn:length(existingIntrvDateNotes)};
		   
	if(totNotes >0) footNote = totNotes + " " + '<fmt:message key="foot_threads" bundle="${resword}"/>' + " " + '<fmt:message key="footNote_threads" bundle="${resword}"/>';
    	   }
    	
	
    		   var htmlgen = 
		 	          '<div class=\"tooltip\">'+
		 	          '<table  width="250">'+
		 	          ' <tr><td  align=\"center\" class=\"header1\">'+title +
		 	          ' </td></tr><tr></tr></table><table  style="border-collapse:collapse" cellspacing="0" cellpadding="0" width="225" >'+
		 	          drawRows(i,resStatus,detailedNotes,discrepancyType,updatedDates,parentDnids)+
		 	          '</table><table width="250"  class="tableborder" align="left">'+  	
		 	          '</table><table><tr></tr></table>'+
		 	          '<table width="200"><tbody><td height="50" colspan="3">'+						
						'<span class=\"note\">'+footNote +'</span>'+
						
						
		 	          
		 	         
		 	          '</td></tr></tbody></table></table></div>';
		  return htmlgen;
    }
    
      function drawRows(i,resStatus,detailedNotes,discrepancyType,updatedDates,parentDnIds)
      {
     	var row = '';
     	var noteType = '';
     		for(var x=0;x<i;x++)
     		{
     		
     	
     			if(resStatus[x]=='1')
     			{
				if(parentDnIds[x] == '0')
					{
						row+='<tr> <td class=\"label\"></td><td colspan = "3" class=\"borderlabel\" nowrap >&nbsp;'+detailedNotes[x].substring(0,60)+'...</td></tr>';
					}
				else
     				row+='<tr> <td class=\"label\"><img src="images/icon_Note.gif" width="16" height="13" alt="Note"></td>'+'<td  width="180" align="left" class=\"label\" nowrap>&nbsp;<fmt:message key="open" bundle="${resword}"/>: &nbsp;'+discrepancyType[x] +'&nbsp;'+updatedDates[x]+'</td></tr><tr><td class=\"borderlabel\"></td><td class=\"borderlabel\" nowrap >&nbsp;'+detailedNotes[x].substring(0,60)+'...</td></tr>';
     			}
     			else if(resStatus[x]=='2')
     			{
				if(parentDnIds[x] == '0')
					{
						row+='<tr> <td class=\"label\"></td><td colspan = "3" class=\"borderlabel\" nowrap >&nbsp;'+detailedNotes[x].substring(0,60)+'...</td></tr>';
					}
				else
     				row+='<tr > <td  class=\"label\"><img src="images/icon_flagYellow.gif" width="16" height="13" alt="Note"></td>'+'<td width="180"  align="left" class=\"label\" nowrap>&nbsp;<fmt:message key="updated" bundle="${resword}"/>: &nbsp;'+discrepancyType[x] +'&nbsp;'+updatedDates[x]+'</td></tr><tr><td class=\"borderlabel\"></td><td  class=\"borderlabel\" nowrap>&nbsp;'+detailedNotes[x].substring(0,60)+'...</td></tr>';
     			}
     			else if(resStatus[x]=='3')
     			{
     				if(parentDnIds[x] == '0')
					{
						row+='<tr> <td class=\"label\"></td><td colspan = "3" class=\"borderlabel\" nowrap >&nbsp;'+detailedNotes[x].substring(0,60)+'...</td></tr>';
					}
				else
					row+='<tr> <td class=\"label\"><img src="images/icon_flagGreen.gif" width="16" height="13" alt="Note"></td>'+'<td  width="180"  align="left" class=\"label\" nowrap>&nbsp;<fmt:message key="resolved" bundle="${resword}"/>: &nbsp;'+discrepancyType[x] +'&nbsp;'+updatedDates[x]+'</td></tr><tr><td class=\"borderlabel\"></td><td  class=\"borderlabel\" nowrap>&nbsp;'+detailedNotes[x].substring(0,60)+'...</td></tr>';
     			}
     			else if(resStatus[x]=='4')
     			{
				if(parentDnIds[x] == '0')
					{
						row+='<tr> <td class=\"label\"></td><td colspan = "3" class=\"borderlabel\" nowrap >&nbsp;'+detailedNotes[x].substring(0,60)+'...</td></tr>';
					}
				else
     				row+='<tr> <td  class=\"label\"><img src="images/icon_flagBlack.gif" width="16" height="13" alt="Note"></td>'+'<td  width="180" align="left" class=\"label\" nowrap>&nbsp;<fmt:message key="closed" bundle="${resword}"/>: &nbsp;'+discrepancyType[x] +'&nbsp;'+updatedDates[x]+'</td></tr><tr><td class=\"borderlabel\"></td><td class=\"borderlabel\" nowrap>&nbsp;'+detailedNotes[x].substring(0,60)+'...</td></tr>';
     			}
     			else if(resStatus[x]=='5')
     		{
			if(parentDnIds[x] == '0')
					{
						row+='<tr> <td class=\"label\"></td><td colspan = "3" class=\"borderlabel\" nowrap >&nbsp;'+detailedNotes[x].substring(0,60)+'...</td></tr>';
					}
				else
     			row+='<tr> <td width="16"  class=\"label\"><img src="images/icon_flagWhite.gif" width="16" height="13" alt="Note"></td>'+'<td width="180"  align="left" class=\"label\" nowrap>&nbsp; <fmt:message key="not_applicable" bundle="${resword}"/>: &nbsp;'+discrepancyType[x] +'&nbsp;'+updatedDates[x]+'</td></tr><tr><td class=\"borderlabel\"></td><td class=\"borderlabel\" nowrap>&nbsp;'+detailedNotes[x].substring(0,60)+'...</td></tr>';
     			}
     			
     			
     		}
     	
     	return row;
     }
      
function callTip(html)
{
	Tip(html,BGCOLOR,'#FFFFE5',BORDERCOLOR,'' );
}

</script>

<%--<c:set var="inputVal" value="input${itemId}" />--%>

<%--<c:set var="hasNameNote" value="${param.hasNameNote}"/>
<c:set var="hasDateNote" value="${param.hasDateNote}"/>--%>
<c:set var="contextPath" value="${fn:replace(pageContext.request.requestURL, fn:substringAfter(pageContext.request.requestURL, pageContext.request.contextPath), '')}" />
<c:forEach var="presetValue" items="${presetValues}">
    <c:if test='${presetValue.key == "interviewer"}'>
        <c:set var="interviewer" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "interviewDate"}'>
        <c:set var="interviewDate" value="${presetValue.value}" />
    </c:if>
</c:forEach>
<!-- End of Alert Box -->
<table border="0" cellpadding="0" cellspacing="0" onLoad="">

<c:choose>
    <c:when test="${study.studyParameterConfig.interviewerNameRequired == 'yes' || study.studyParameterConfig.interviewDateRequired == 'yes'}">
        <tr id="CRF_infobox_closed" style="display:none;">
            <td style="padding-top: 3px; padding-left: 6px; width: 250px;" nowrap>
                <a href="javascript:leftnavExpand('CRF_infobox_closed'); leftnavExpand('CRF_infobox_open');">
                    <img src="<c:out value="${contextPath}" />/images/sidebar_expand.gif" align="left" border="0" hspace="10">
                    <b><fmt:message key="CRF_info" bundle="${resword}"/></b>
                </a>
            </td>
        </tr>
        <tr id="CRF_infobox_open">
        <td>
        <table border="0" cellpadding="0" cellspacing="0">
        <tr>
            <td valign="bottom">
                <table border="0" cellpadding="0" cellspacing="0" width="100">
                    <tr>
                        <td nowrap>
                            <div class="tab_BG_h">
                                <div class="tab_R_h" style="padding-right: 40px;">
                                <div class="tab_L_h" style="padding: 3px 11px 0px 6px; text-align: left;">

                                <a href="javascript:leftnavExpand('CRF_infobox_closed'); leftnavExpand('CRF_infobox_open');">
                                    <img src="<c:out value="${contextPath}" />/images/sidebar_collapse.gif" align="left" border="0" hspace="10">
                                    <b><fmt:message key="CRF_info" bundle="${resword}"/></b>
                                </a>

                            </div></div></div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </c:when>
    <c:otherwise>
        <tr id="CRF_infobox_closed">
            <td style="padding-top: 3px; padding-left: 6px; width: 250px;" nowrap>
                <a href="javascript:leftnavExpand('CRF_infobox_closed'); leftnavExpand('CRF_infobox_open');">
                    <img src="<c:out value="${contextPath}" />/images/sidebar_expand.gif" align="left" border="0" hspace="10">
                    <b><fmt:message key="CRF_info" bundle="${resword}"/></b>
                </a>
            </td>
        </tr>
        <tr id="CRF_infobox_open" style="display: none;">
        <td>
        <table border="0" cellpadding="0" cellspacing="0">
        <tr>
            <td valign="bottom">
                <table border="0" cellpadding="0" cellspacing="0" width="100">
                    <tr>
                        <td nowrap>
                            <div class="tab_BG_h">
                                <div class="tab_R_h" style="padding-right: 40px;">
                                <div class="tab_L_h" style="padding: 3px 11px 0px 6px; text-align: left;">

                                <a href="javascript:leftnavExpand('CRF_infobox_closed'); leftnavExpand('CRF_infobox_open');">
                                    <img src="<c:out value="${contextPath}" />/images/sidebar_collapse.gif" align="left" border="0" hspace="10">
                                    <b><fmt:message key="CRF_info" bundle="${resword}"/></b>
                                </a>
                            </div></div></div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </c:otherwise>
</c:choose>
<tr>
<td valign="top">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">

<table border="0" cellpadding="0" cellspacing="0" width="100%">
<!-- <tr>
    <td colspan="2" class="table_header_row_left">
        <b> <c:out value="${toc.crf.name}" /> <c:out value="${toc.crfVersion.name}" />
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
               --> <!-- leave blank --><!-- 
            </c:otherwise>
        </c:choose></b>

    </td>
    <td class="table_header_row" style="color: #789EC5"><b><fmt:message key="crf_notes" bundle="${resword}"/>:</b> </td>
    <td class="table_header_row_left" style="color: #789EC5"><font color="#CC0000"><c:out value="${openNum}"/> <fmt:message key="open" bundle="${resword}"/></font>, <font color="#D4A718"><c:out value="${updatedNum}"/> <fmt:message key="updated" bundle="${resword}"/></font>, <font color="#7CB98F"><c:out value="${resolvedNum}"/> <fmt:message key="resolved" bundle="${resword}"/></font>,
        <br><font color="#000000"><c:out value="${closedNum}"/> <fmt:message key="closed" bundle="${resword}"/></font>, <font color="#000000"><c:out value="${notAppNum}"/> <fmt:message key="not_applicable" bundle="${resword}"/></font>
    </td>

</tr> -->
<!-- <tr>
    <td class="table_cell_left" style="color: #789EC5">
        <b><fmt:message key="study_subject_ID" bundle="${resword}"/>:</b><br />
    </td>
    <td class="table_cell_left" style="color: #789EC5">
        <c:out value="${studySubject.label}" /><br />
    </td>
    <c:choose>
        <c:when test="${study.studyParameterConfig.personIdShownOnCRF == 'true'}">
            <td class="table_cell" style="color: #789EC5">
                <b><fmt:message key="person_ID" bundle="${resword}"/>:</b><br />
            </td>
            <td class="table_cell_left" style="color: #789EC5">
                <c:out value="${subject.uniqueIdentifier}" /><br />
            </td>

        </c:when>
        <c:otherwise>
            <td class="table_cell" style="color: #789EC5"><b></td>
            <td class="table_cell_left" style="color: #789EC5"></td>
        </c:otherwise>
    </c:choose>
</tr>
--><!-- 
<tr>
    <c:choose>
        <c:when test="${study.studyParameterConfig.secondaryLabelViewable == 'true'}">
            <td class="table_cell" style="color: #789EC5">
                <b><fmt:message key="secondary_ID" bundle="${resword}"/>:</b><br />
            </td>
            <td class="table_cell_left" style="color: #789EC5">
                <c:out value="${studySubject.secondaryLabel}" /><br />
            </td>
        </c:when>
        <c:otherwise>
            <td class="table_cell" style="color: #789EC5"><b></td>
            <td class="table_cell_left" style="color: #789EC5"></td>
        </c:otherwise>
    </c:choose>
            <td class="table_cell" style="color: #789EC5"><b></td>
            <td class="table_cell_left" style="color: #789EC5"></td>
</tr>
-->
<tr>
<!--event-->
  <td class="table_cell_noborder" >
        <b><fmt:message key="event" bundle="${resword}"/>:</b>
    </td>
    <td >
      <c:out value="${toc.studyEventDefinition.name}" />&nbsp;(<fmt:formatDate
      value="${toc.studyEvent.dateStarted}" pattern="${dteFormat}" />)
    </td>
	<td class="table_cell_top" >
<span><b><fmt:message key="gender" bundle="${resword}"/>:</b></span>
        
        </td>
         <td class="table_cell_noborder" style="padding-left:3px">
	       <c:choose>
	                   <c:when test="${subject.gender==109}"><fmt:message key="M" bundle="${resword}"/></c:when>
	                   <c:when test="${subject.gender==102}"><fmt:message key="F" bundle="${resword}"/></c:when>
	                   <c:otherwise>
	                       <c:out value="${subject.gender}" />
	                   </c:otherwise>
        </c:choose>
    </td>
	

   
</tr>

<tr>
  <!-- Occurence id-->
        <td class="table_cell_noborder" >
<c:if test="${toc.studyEventDefinition.repeating}">
   
            <b><fmt:message key="occurrence_number" bundle="${resword}"/>:</b>
</c:if>     
	 </td>
		
        <td class="table_cell_noborder" >
<c:if test="${toc.studyEventDefinition.repeating}">         
		 <c:out value="${toc.studyEvent.sampleOrdinal}" />
   </c:if> 
   </td>



   <!-- Age at Enrollment-->
   <td class="table_cell_top" >

        <b><fmt:message key="age_at_enrollment" bundle="${resword}"/>:</b><br>

    </td>
     <td class="table_cell_noborder" >
        <c:out value="${age}" /><br>
    </td>
	
	</tr>
	
	<tr>
		<!--study-->
	 <td class="table_cell_noborder" >

        <b><fmt:message key="study" bundle="${resword}"/>:</b><br>
    </td>
	
    <td class="table_cell_noborder" >
     <c:out value="${studyTitle}" /><br>
    </td>
    <td class="table_cell_top" >
        <c:if test="${study.studyParameterConfig.collectDob != '3'}">
            <c:choose>
                <c:when test="${study.studyParameterConfig.collectDob =='1'}">
                    <b><fmt:message key="date_of_birth" bundle="${resword}"/>:</b><br />
                </c:when>
                <c:otherwise>
                    <b><fmt:message key="year_of_birth" bundle="${resword}"/>:</b><br />                    
                </c:otherwise>
            </c:choose>
        </c:if>
    </td>
     <td class="table_cell_noborder" >
          <c:if test="${study.studyParameterConfig.collectDob != '3'}">
                    <%-- BWP 3105 Until the SubjectBean uses the Calendar object to represent
         the date of birth, we will have to use the Date.getYear() deprecated method.--%>
    
                <c:choose>
                    <c:when test="${study.studyParameterConfig.collectDob == '2' && subject.dateOfBirth.year != null}">${subject.dateOfBirth.year + 1900}</c:when>
                    <c:otherwise> <fmt:formatDate value="${subject.dateOfBirth}" pattern="${dteFormat}" /></c:otherwise>
                </c:choose>
                <%-->> --%>
            </c:if>
    
            
            <br />
        </td>

    </tr>
	<tr>
	<!--site to be implemented -->
	          <td class="table_cell_noborder" >

        <b><fmt:message key="site" bundle="${resword}"/>:</b><br>
    </td>
	
    <td class="table_cell_noborder" >
        <c:if test="${study.parentStudyId > '0'}">
            <c:out value="${siteTitle}" /><br>
        </c:if>
        <c:if test="${study.parentStudyId == '0'}">
            <fmt:message key="na" bundle="${resword}"/><br>
        </c:if>
    </td>
	<!--person id todo -->
       <c:choose>
        <c:when test="${study.studyParameterConfig.personIdShownOnCRF == 'true'}">
            <td class="table_cell_top">
                <b><fmt:message key="person_ID" bundle="${resword}"/>:</b><br />
            </td>
            <td class="table_cell_noborder">
                <c:out value="${subject.uniqueIdentifier}" /><br />
            </td>

        </c:when>
        <c:otherwise>
            <td class="table_cell"></td>
            <td class="table_cell_left" ></td>
        </c:otherwise>
    </c:choose>
	</tr>
<tr>
 


       
    

</tr>
<%--<tr>
  <td class="table_cell_noborder" style="color: #789EC5">

  </td>
  <td class="table_cell_noborder" style="color: #789EC5">
  </td>
  <td class="table_cell_top" style="color: #789EC5">
    <b><fmt:message key="gender" bundle="${resword}"/>:</b>
  </td>
  <td class="table_cell_noborder" style="color: #789EC5">
    <c:choose>
      <c:when test="${subject.gender==109}">M</c:when>
      <c:when test="${subject.gender==102}">F</c:when>
      <c:otherwise>
        <c:out value="${subject.gender}" />
      </c:otherwise>
    </c:choose>
  </td>
</tr>--%>
<%-- find out whether the item is involved with an error message, and if so, outline the
form element in red <c:out value="FORMMESSAGES: ${formMessages} "/><br/>--%>

<c:forEach var="frmMsg" items="${formMessages}">
    <c:if test="${frmMsg.key eq 'interviewer'}">
        <c:set var="isInError_Int" value="${true}" />
    </c:if>
    <c:if test="${frmMsg.key eq 'interviewDate'}">
        <c:set var="isInError_Dat" value="${true}" />
    </c:if>
</c:forEach>

<tr>
<td class="table_cell_left" nowrap>
    <c:if test="${study.studyParameterConfig.interviewerNameRequired != 'not_used'}">
    <c:choose>
        <c:when test="${isInError_Int}">
            <fmt:message key="interviewer_name" bundle="${resword}"/>: <span class="aka_exclaim_error">! </span> &nbsp;
            
        </c:when>

        <c:otherwise>
            <fmt:message key="interviewer_name" bundle="${resword}"/>:
            <c:if test="${study.studyParameterConfig.interviewerNameRequired=='yes'}">
                *
            </c:if>
            &nbsp;
        </c:otherwise>
    </c:choose>
    </c:if>
</td>
<td class="table_cell_left">
    <c:if test="${study.studyParameterConfig.interviewerNameRequired != 'not_used'}">
    <table border="0" cellpadding="0" cellspacing="0">
        <tr>

            <td valign="top">
                <!--  formfieldM_BG-->

                <c:choose>
                <c:when
                  test="${study.studyParameterConfig.interviewerNameEditable=='true'}">
                <c:choose>
                <c:when test="${isInError_Int}">
                <div class="aka_input_error">
                    <label for="interviewer"></label><input id="interviewer" type="text" name="interviewer" size="15"
                                                            value="<c:out value="${interviewer}" />" class="aka_input_error">
                    </c:when>
                    <c:otherwise>
                    <div class=" formfieldM_BG">
                        <input type="text" name="interviewer" size="15"
                               value="<c:out value="${interviewer}" />" class="formfieldM">
                        </c:otherwise>
                        </c:choose>
                        </c:when>
                        <c:otherwise>
                        <div class=" formfieldM_BG">
                            <input type="text" disabled size="15"
                                   value="<c:out value="${interviewer}" />" class="formfieldM">
                            <input type="hidden" name="interviewer"
                                   value="<c:out value="${interviewer}" />">
                            </c:otherwise>
                            </c:choose></div>
                        <%--BWP>>new error message design:  <jsp:include page="../showMessage.jsp">
                          <jsp:param name="key" value="interviewer" />
                        </jsp:include>--%>
            </td>
            <td valign="top" nowrap>
                <c:set var="isNewDN" value="${hasNameNote eq 'yes' ? 0 : 1}"/>

                <c:if test="${study.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
                    <%--<c:if test="${! (enclosingPage eq 'viewSectionData')}">--%>
                <c:choose>
                  <c:when test="${nameNoteResStatus == 0}">
                      <c:set var="imageFileName" value="icon_noNote" />
                  </c:when>
                  <c:when test="${nameNoteResStatus == 1}">
                      <c:set var="imageFileName" value="icon_Note" />
                  </c:when>
                  <c:when test="${nameNoteResStatus == 2}">
                      <c:set var="imageFileName" value="icon_flagYellow" />
                  </c:when>
                  <c:when test="${nameNoteResStatus == 3}">
                      <c:set var="imageFileName" value="icon_flagGreen" />
                  </c:when>
                  <c:when test="${nameNoteResStatus == 4}">
                      <c:set var="imageFileName" value="icon_flagBlack" />
                  </c:when>
                  <c:when test="${nameNoteResStatus == 5}">
                      <c:set var="imageFileName" value="icon_flagWhite" />
                  </c:when>
                  <c:otherwise>
                  </c:otherwise>
                </c:choose>

                <c:choose>
                 <c:when test="${hasNameNote eq 'yes'}">
                <a href="#" id="nameNote1"
           onmouseout="UnTip();"onmouseover="callTip(genToolTipFromArray('interviewNotes'));" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySubject.id}&itemId=${itemId}&id=${InterviewerNameNote.eventCRFId}&name=${InterviewerNameNote.entityType}&field=interviewer&column=${InterviewerNameNote.column}&enterData=${enterData}&monitor=${monitor}&blank=${blank}','spanAlert-interviewDate'); return false;">
                    <img id="flag_interviewer" name="flag_interviewer" src="<c:out value="${contextPath}" />/images/<c:out value="${imageFileName}"/>.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                    </c:when>
                    <c:otherwise>
                    <a id="nameNote1" href="#"
           onmouseout="UnTip();"onmouseover="callTip(genToolTipFromArray('interviewNotes'));" onClick="openDSNoteWindow('CreateDiscrepancyNote?subjectId=${studySubject.id}&viewData=y&id=<c:out value="${toc.eventCRF.id}"/>&name=eventCrf&field=interviewer&column=interviewer_name&writeToDB=1&new=${isNewDN}','spanAlert-interviewer'); return false;">
                        <img id="flag_interviewer" name="flag_interviewer" src="<c:out value="${contextPath}" />/images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>">
                        </c:otherwise>
                        </c:choose>
                    </a>
                        <%--</c:if>--%>
                    </c:if>
            </td>
        </tr>
        <tr>
            <td valign="top">
                <span ID="spanAlert-interviewer" class="alert"></span>
            </td>
        </tr>
    </table>
    </c:if>
</td>

<td class="table_cell" nowrap>
    <c:if test="${study.studyParameterConfig.interviewDateRequired != 'not_used'}">
    <c:choose>
        <c:when test="${isInError_Dat}">
            <fmt:message key="interview_date" bundle="${resword}"/>: <span class="aka_exclaim_error">! </span>&nbsp;<br />
            <%--(<fmt:message key="date_format" bundle="${resformat}"/>)--%>
        </c:when>
        <c:otherwise>
            <fmt:message key="interview_date" bundle="${resword}"/>:
            <c:if test="${study.studyParameterConfig.interviewDateRequired=='yes'}">
                *
            </c:if>&nbsp;<br />
            <%--(<fmt:message key="date_format" bundle="${resformat}"/>)--%>
        </c:otherwise>
    </c:choose>
    </c:if>
</td><!--</a>-->
<td class="table_cell_left"> 
    <c:if test="${study.studyParameterConfig.interviewDateRequired != 'not_used'}">
    <table border="0" cellpadding="0" cellspacing="0">

        <tr>
            <%----%>
            <td valign="top">
                <c:choose>
                <c:when
                  test="${study.studyParameterConfig.interviewDateEditable=='true'}">
                <c:choose>
                <c:when test="${isInError_Dat}">
                <div class="aka_input_error">
                    <label for="interviewDate"></label>
                    <input id="interviewDate" type="text" name="interviewDate" size="15"
                           value="<c:out value="${interviewDate}" />" class="aka_input_error">
                    </c:when>
                    <c:otherwise>
                    <div class="formfieldM_BG">
                        <input id="interviewDate" type="text" name="interviewDate" size="15"
                               value="<c:out value="${interviewDate}" />" class="formfieldM">
                        </c:otherwise>
                        </c:choose>
                        </c:when>
                        <c:otherwise>
                        <div class="formfieldM_BG">
                            <input id="interviewDate" type="text" disabled size="15"
                                   value="<c:out value="${interviewDate}" />" class="formfieldM">
                            <input type="hidden" name="interviewDate"
                                   value="<c:out value="${interviewDate}" />">
                            </c:otherwise>
                            </c:choose>

                        </div>
                        <%-- BWP>>new error message design: <jsp:include page="../showMessage.jsp">
                          <jsp:param name="key" value="interviewDate" />
                        </jsp:include>--%>
            </td>
            <%--        document.getElementById('testdiv1').style.top=(parseInt(document.getElementById('testdiv1').style.top) - 10)+'px'; --%>
            <td valign="top" nowrap>

                <a href="#">
                    <img src="<c:out value="${contextPath}" />/images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="interviewDateTrigger" /></a>
                <script type="text/javascript">
                    Calendar.setup({inputField  : "interviewDate", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "interviewDateTrigger" });
                </script>

                <c:if test="${study.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
                    <%-- ViewDiscrepancyNote?writeToDB=1&subjectId=<c:out value="${discrepancyNote.subjectId}"/>&itemId=<c:out value="${item.id}"/>&id=<c:out value="${discrepancyNote.entityId}"/>&name=<c:out value="${discrepancyNote.entityType}"/>&field=<c:out value="${discrepancyNote.field}"/>&column=<c:out value="${discrepancyNote.column}"/>&enterData=<c:out value="${enterData}"/>&monitor=<c:out value="${monitor}"/>&blank=<c:out value="${blank}"/>" --%>
                    <%--BWP: 2808 related>>
                    and switched it back for 2898--%>
                    <%-- <c:if test="${! (enclosingPage eq 'viewSectionData')}">--%>
                <c:set var="isNewDNDate" value="${hasDateNote eq 'yes' ? 0 : 1}"/>
                <c:choose>
                  <c:when test="${IntrvDateNoteResStatus == 0}">
                      <c:set var="imageFileName" value="icon_noNote" />
                  </c:when>
                  <c:when test="${IntrvDateNoteResStatus == 1}">
                      <c:set var="imageFileName" value="icon_Note" />
                  </c:when>
                  <c:when test="${IntrvDateNoteResStatus == 2}">
                      <c:set var="imageFileName" value="icon_flagYellow" />
                  </c:when>
                  <c:when test="${IntrvDateNoteResStatus == 3}">
                      <c:set var="imageFileName" value="icon_flagGreen" />
                  </c:when>
                  <c:when test="${IntrvDateNoteResStatus == 4}">
                      <c:set var="imageFileName" value="icon_flagBlack" />
                  </c:when>
                  <c:when test="${IntrvDateNoteResStatus == 5}">
                      <c:set var="imageFileName" value="icon_flagWhite" />
                  </c:when>
                  <c:otherwise>
                  </c:otherwise>
                </c:choose>

                <c:choose>
                <c:when test="${hasDateNote eq 'yes'}">
                <a href="#"  onmouseover="callTip(genToolTipFromArray('dateNotes') );"
           onmouseout="UnTip();"  onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySubject.id}&itemId=${itemId}&id=${InterviewerDateNote.eventCRFId}&name=${InterviewerDateNote.entityType}&field=interviewDate&column=${InterviewerDateNote.column}&enterData=${enterData}&monitor=${monitor}&blank=${blank}','spanAlert-interviewDate'); return false;">
                    <img id="flag_interviewDate" name="flag_interviewDate" src="<c:out value="${contextPath}" />/images/<c:out value="${imageFileName}"/>.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>"  >
                    </c:when>
                    <c:otherwise>
                    <a href="#"  onmouseover="callTip(genToolTipFromArray('dateNotes') );"
           onmouseout="UnTip();" onClick="openDNoteWindow('CreateDiscrepancyNote?subjectId=${studySubject.id}&id=<c:out value="${toc.eventCRF.id}"/>&name=eventCrf&field=interviewDate&column=date_interviewed&writeToDB=1&new=${isNewDNDate}','spanAlert-interviewDate'); return false;">
                        <img id="flag_interviewDate" name="flag_interviewDate" src="<c:out value="${contextPath}" />/images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>"  >
                        </c:otherwise>
                        </c:choose>
                    </a>
                        <%--  </c:if>--%>
                    </c:if>
            </td>
        </tr>
        <tr>
            <td valign="top">
                <span ID="spanAlert-interviewDate" class="alert"></span>
            </td>
        </tr>
    </table>
	</c:if>
</td>
</tr>

<tr>

  <td colspan="5" valign="top" class="table_cell_left"  ><b>Discrepancy Notes on this CRF:</b></td>
 
</tr>

<tr>
<table border="0" cellspacing="1" cellpadding="0" width="100%">
  <tr>
  <td valign="top" align="center" class="table_cell_left" style="border-right:1px solid #E6E6E6;color:#CC0000;" width="20%"><fmt:message key="open" bundle="${resword}"/></td>
  <td valign="top" align="center" class="table_cell_left" style="border-right:1px solid #E6E6E6;color:#D4A718;" width="20%"><fmt:message key="updated" bundle="${resword}"/></td>
  <td valign="top" align="center" class="table_cell_left" style="border-right:1px solid #E6E6E6;color:#7CB98F;" width="20%"> <fmt:message key="resolved" bundle="${resword}"/></td>
  <td valign="top" align="center" class="table_cell_left" style="border-right:1px solid #E6E6E6;color:black;" width="20%"><fmt:message key="closed" bundle="${resword}"/></td>
  <td valign="top" align="center" class="table_cell_left"  style="border-right:1px solid #E6E6E6;color:black" width="20%"> <fmt:message key="not_applicable" bundle="${resword}"/></td>
  </tr>
  <tr>
  <td valign="top" align="center" class="table_cell_left" style="border-right:1px solid #E6E6E6;color:#CC0000;" width="20%"><c:out value="${openNum}"/></td>
    <td valign="top" align="center" class="table_cell_left" style="border-right:1px solid #E6E6E6;color:#D4A718;" width="20%"><c:out value="${updatedNum}"/></td>
    <td valign="top" align="center" class="table_cell_left" style="border-right:1px solid #E6E6E6;color:#7CB98F;;" width="20%"><c:out value="${resolvedNum}"/></td>
    <td valign="top" align="center" class="table_cell_left" style="border-right:1px solid #E6E6E6;color:black;" width="20%"><c:out value="${closedNum}"/></td>
  <td valign="top" align="center" class="table_cell_left"  style="border-right:1px solid #E6E6E6;color:black" width="20%"><c:out value="${notAppNum}"/></td>
  </tr>
  </table>
</tr>

</table>

</div>

</div></div></div></div></div></div></div>


</td>
</tr>


</table>
