<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:useBean scope='request' id='strResStatus' class='java.lang.String' />
<jsp:useBean scope='request' id='strUserAccountId' class='java.lang.String' />
<jsp:useBean scope='request' id='writeToDB' class='java.lang.String' />
<jsp:useBean scope='request' id='unlock' class='java.lang.String' />
<jsp:useBean scope='request' id='autoView' class='java.lang.String' />
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean' />
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<html>
<head>
<c:set var="contextPath" value="${fn:replace(pageContext.request.requestURL, fn:substringAfter(pageContext.request.requestURL, pageContext.request.contextPath), '')}" />
<title><fmt:message key="openclinica" bundle="${resword}"/>- <fmt:message key="add_discrepancy_note" bundle="${resword}"/></title>
<link rel="stylesheet" href="includes/styles.css" type="text/css">
<script language="JavaScript" src="includes/global_functions_javascript.js"></script>

<script language="JavaScript" src="includes/CalendarPopup.js"></script>

<style type="text/css">

.popup_BG { background-image: url(images/main_BG.gif);
	background-repeat: repeat-x;
	background-position: top;
	background-color: #FFFFFF;
	}


</style>

<script language="JavaScript">
<!--
function leftnavExpand(strLeftNavRowElementName){

    var objLeftNavRowElement;

    objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
    if (objLeftNavRowElement != null) {
        if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
        objLeftNavRowElement.display = (objLeftNavRowElement.display == "none" ) ? "" : "none";
    }
}

function hide(strLeftNavRowElementName){

    var objLeftNavRowElement;

    objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
    if (objLeftNavRowElement != null) {
        if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
        objLeftNavRowElement.display = "none";
    }
}

function setStatus(typeId,filter1,nw,ud,rs,cl,na) {
	objtr1=document.getElementById('res1');
	objtr2=document.getElementById('resStatusId');
	
	if (typeId == 2|| typeId ==4) {//annotation or reason for change
  		objtr2.disabled = true;
  		objtr2.options.length = 0;
  		objtr2.options[0]=new Option(na, '5');
	} else {
		objtr2.disabled = false;
		objtr2.options.length = 0;
		objtr2.options[0]=new Option(nw, '1');
		if(filter1=="22" || (filter1=="2" && typeId==1)) {
	  		objtr2.options[1]=new Option(rs, '3');
		} else if(filter1=="1") {
			objtr2.options[1]=new Option(ud,'2');
			objtr2.options[2]=new Option(cl,'4');
		} else {
			objtr2.options[1]=new Option(ud,'2');
			objtr2.options[2]=new Option(rs,'3');
			objtr2.options[3]=new Option(cl,'4');
		}
	} 
}

function setResStatus(resStatusId, destinationUserId) {
	objtr1=document.getElementById('resStatusId');
	objtr2=document.getElementById('userAccountId');
	objtr3=document.getElementById('xxx');
	objtr4=document.getElementById('typeId');

	if (resStatusId == 3 || resStatusId == 4) { //Resolutiuon proposed or Closed
		// objtr2.disabled = false;
		objtr2.value = destinationUserId;
		// disable?
		objtr2.disabled = false;
		objtr3.removeAttribute('disabled');
		//objtr3.value = destinationUserId;
		// disable?
		//objtr3.disabled = false;
	}

	if (resStatusId == 5 && objtr4.value == 4) { // Not applicable AND Reason for Change
		objtr1.disabled = true;
	}
}


function setElements(typeId, user1, user2,filter1,nw,ud,rs,cl,na) {
	setStatus(typeId,filter1,nw,ud,rs,cl,na);
	if(typeId == 3) {//query
		leftnavExpand(user1);
		leftnavExpand(user2);	
	}else {
		hide(user1);
		hide(user2);
	}
}
//-->
</script>
</head>
<body style="margin: 0px 12px 0px 12px;" onload="javascript:setStatus('<c:out value="${discrepancyNote.discrepancyNoteTypeId}"/>','<c:out value="${whichResStatus}"/>','<fmt:message key="New" bundle="${resterm}"/>','<fmt:message key="Updated" bundle="${resterm}"/>','<fmt:message key="Resolution_Proposed" bundle="${resterm}"/>','<fmt:message key="Closed" bundle="${resterm}"/>','<fmt:message key="Not_Applicable" bundle="${resterm}"/>');">
<%-- needs to run at first to possibly gray out the drop down, tbh 02/2010--%>
<div style="float: left;"><h1 class="title_manage"><c:out value="${entityName}"/>: <fmt:message key="add_discrepancy_note" bundle="${resword}"/></h1></div>
<div style="float: right;"><a href="#" onclick="javascript:window.close();"><img name="close_box" alt="<fmt:message key="Close_Box" bundle="${resword}"/>" src="images/bt_Remove.gif" class="icon_dnBox"></a></div>
<div style="clear:both;"></div> 
<div class="alert">
<c:forEach var="message" items="${pageMessages}">
 <c:out value="${message}" escapeXml="false"/>
</c:forEach>
</div>         
<form name="noteForm" method="POST" action="CreateDiscrepancyNote">
<jsp:include page="../include/showSubmitted.jsp" />
<input type="hidden" name="name" value="<c:out value="${discrepancyNote.entityType}"/>">
<input type="hidden" name="column" value="<c:out value="${discrepancyNote.column}"/>">
<input type="hidden" name="parentId" value="<c:out value="${discrepancyNote.parentDnId}"/>">
<input type="hidden" name="id" value="<c:out value="${discrepancyNote.entityId}"/>">
<input type="hidden" name="subjectId" value="<c:out value="${param.subjectId}"/>">
<input type="hidden" name="field" value="<c:out value="${discrepancyNote.field}"/>">
<input type="hidden" name="writeToDB" value="<c:out value="${writeToDB}" />">
<input type="hidden" name="monitor" value="<c:out value="${monitor}" />">

<input type="hidden" name="enterData" value="<c:out value="${enterData}" />">
<input type="hidden" name="eventCRFId" value="<c:out value="${eventCRFId}"/>">

          
<c:set var="name" value="${discrepancyNote.entityType}"/>
<!-- Entity box -->
<table border="0" cellpadding="0" cellspacing="0" style="float:left;">
	<tr><td valign="bottom">
	<table border="0" cellpadding="0" cellspacing="0">
    	<tr><td nowrap style="padding-right: 20px;">
            <div class="tab_BG_h"><div class="tab_R_h" style="padding-right: 0px;"><div class="tab_L_h" style="padding: 3px 11px 0px 6px; text-align: left;">
	        <b><c:choose>
	            <c:when test="${entityName != '' && entityName != null }">
	                  "<c:out value="${entityName}"/>"
	            </c:when>
	            <c:otherwise>
	                <%-- nothing here; if entityName is blank --%>
	            </c:otherwise>
	        </c:choose>
			<fmt:message key="Properties" bundle="${resword}"/>:</b>
			</div></div></div>
		</td></tr>
    </table>
    </td></tr>
    <tr><td valign="top">
		<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TR"><div class="box_BL"><div class="box_BR">
		<div class="textbox_center">
		<table border="0" cellpadding="0" cellspacing="0">
        <tr>
	        <td class="table_cell_noborder"><fmt:message key="subject" bundle="${resword}"/>:&nbsp;&nbsp;</td>
	        <td class="table_cell_noborder"><b><c:out value="${discrepancyNote.subjectName}"/></b></td>
	        <td class="table_cell_noborder" style="padding-left: 40px;"><fmt:message key="event" bundle="${resword}"/>:&nbsp;&nbsp;</td>
	        <td class="table_cell_noborder"><b><c:out value="${discrepancyNote.eventName =='' ? 'N/A' : discrepancyNote.eventName}"/></b></td>
    	</tr>
        <tr>
            <td class="table_cell_noborder"><fmt:message key="event_date" bundle="${resword}"/>:&nbsp;&nbsp;</td>
            <td class="table_cell_noborder">
                <c:choose>
                    <c:when test="${discrepancyNote.eventStart == null}">
                        <b><fmt:message key="N/A" bundle="${resword}"/></b>
                    </c:when>
                    <c:otherwise>
                        <b><fmt:formatDate value="${discrepancyNote.eventStart}" pattern="${dteFormat}"/></b>
                    </c:otherwise>
                </c:choose>
            </td>
            <td class="table_cell_noborder" style="padding-left: 40px;"><fmt:message key="CRF" bundle="${resword}"/>:&nbsp;&nbsp;</td>
            <td class="table_cell_noborder"><b><c:out value="${discrepancyNote.crfName == '' ? 'N/A' : discrepancyNote.crfName}"/></b></td>
        </tr>
        <tr>
        	<td class="table_cell_noborder"><fmt:message key="Current_Value" bundle="${resword}"/>:&nbsp;&nbsp;</td>
            <td class="table_cell_noborder"><b><c:out value="${entityValue}"/></b></td>
            <td class="table_cell_noborder" style="padding-left: 40px;"><fmt:message key="More" bundle="${resword}"/>:&nbsp;&nbsp;</td>
            <td class="table_cell_noborder">
            <c:choose>
            <c:when test="${name eq 'itemData' ||name eq 'ItemData'}">
                <a href="javascript: openDocWindow('ViewItemDetail?itemId=<c:out value="${item.id}"/>')">
                <fmt:message key="Data_Dictionary" bundle="${resword}"/></a>
            </c:when>
            <c:otherwise>
                <b><fmt:message key="N/A" bundle="${resword}"/></b>
            </c:otherwise>
            </c:choose>
            </td>
        </tr>
        </table>
        </div>
        </div></div></div></div></div></div></div>
	</td></tr>
</table>

<div style="clear:both;"></div> 
<h3 class="title_manage"><fmt:message key="add_note" bundle="${resword}"/></h3>

<!-- dn box -->
<div style="width: 418;">	
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center">

	<div class="dnBoxCol1 dnBoxText"><fmt:message key="description" bundle="${resword}"/>:<span class="alert">*</span></div>
	<div class="dnBoxCol2 dnBoxText">
		<span id="description">
			<div class="formfieldXL_BG"><input type="text" name="description" value="<c:out value="${discrepancyNote.description}"/>" class="formfieldXL"></div>
			<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="description"/></jsp:include>
		</span>
	</div>
	
	<div class="dnBoxCol1 dnBoxText"><fmt:message key="detailed_note" bundle="${resword}"/>:</div>
	<div class="dnBoxCol2 dnBoxText">
		<div class="formtextareaXL4_BG">
	  		<textarea name="detailedDes" rows="4" cols="50" class="formtextareaXL4"><c:out value="${discrepancyNote.detailedNotes}"/></textarea>
		</div>
		<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="detailedDes"/></jsp:include>
	</div>
	
	<div class="dnBoxCol1 dnBoxText"><fmt:message key="type" bundle="${resword}"/>:<span class="alert">*</span></div>
	<div class="dnBoxCol2 dnBoxText"><div class="formfieldL_BG">
		<c:choose>
		<c:when test="${parentId > 0}">
			<input type="hidden" name="typeId" value="${param.typeId}"/>
			<select name="pTypeId" id="pTypeId" class="formfieldL" disabled>
				<option value="<c:out value="${param.typeId}"/>" selected><c:out value="${param.typeName}"/>
			</select>
		</c:when>
		<c:otherwise>
			<c:set var="typeId1" value="${discrepancyNote.discrepancyNoteTypeId}"/>
			<select name="typeId" id="typeId" class="formfieldL" onchange ="javascript:setElements(this.options[selectedIndex].value, 'user1', 'user2','<c:out value="${whichResStatus}"/>','<fmt:message key="New" bundle="${resterm}"/>','<fmt:message key="Updated" bundle="${resterm}"/>','<fmt:message key="Resolution_Proposed" bundle="${resterm}"/>','<fmt:message key="Closed" bundle="${resterm}"/>','<fmt:message key="Not_Applicable" bundle="${resterm}"/>');">
				<c:forEach var="type" items="${discrepancyTypes}">
				<c:choose>
				<c:when test="${typeId1 == type.id}">
				 	<c:choose>
				    <c:when test="${study.status.frozen && (type.id==2 || type.id==4)}">
						<option value="<c:out value="${type.id}"/>" disabled="true" selected ><c:out value="${type.name}"/>
				    </c:when>
				    <c:otherwise>
				   		<option value="<c:out value="${type.id}"/>" selected ><c:out value="${type.name}"/>
				    </c:otherwise>
				    </c:choose>
				 </c:when>
				 <c:otherwise>
					<c:choose>
					<c:when test="${study.status.frozen && (type.id==2 || type.id==4)}">
						<option value="<c:out value="${type.id}"/>" disabled="true"><c:out value="${type.name}"/>
					</c:when>
					<c:otherwise>
						<option value="<c:out value="${type.id}"/>"><c:out value="${type.name}"/>
					</c:otherwise>
					</c:choose>
				 </c:otherwise>
				</c:choose>
				</c:forEach>
			</select>
			<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="typeId"/></jsp:include>
		</c:otherwise>
		</c:choose>
	</div></div>
		
	<span id="res1${parentId}">
		<div class="dnBoxCol1 dnBoxText"><fmt:message key="Set_to_Status" bundle="${resword}"/>:<span class="alert">*</span></div>
		<div class="dnBoxCol2 dnBoxText"><div class="formfieldL_BG">
			<c:set var="resStatusIdl" value="${discrepancyNote.resolutionStatusId}"/>
		    <select name="resStatusId" id="resStatusId" class="formfieldL">
				<c:set var="resStatuses" value="${resolutionStatuses}"/>
				<c:forEach var="status" items="${resStatuses}">
					<c:choose>
					<c:when test="${resStatusIdl == status.id}">
					   <option value="<c:out value="${status.id}"/>" selected ><c:out value="${status.name}"/>
					</c:when>
					<c:otherwise>
					   <option value="<c:out value="${status.id}"/>" ><c:out value="${status.name}"/>
					</c:otherwise>
					</c:choose>
				</c:forEach>
			</select></div>
		    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="resStatusId"/></jsp:include>
		</div>
	</span>

	<c:choose>
	<c:when test="${(parent == null || parent.id ==0 || unlock == 1) && autoView == 0}">
    	<span id="user1" style="display:none">
	</c:when>
	<c:otherwise>
		<span id="user1" style="display:block">
  	</c:otherwise>
	</c:choose>
		<div class="dnBoxCol1 dnBoxText"><fmt:message key="assign_to_user" bundle="${resword}"/>:</div>
		<div class="dnBoxCol2 dnBoxText"><div class="formfieldL_BG">
			<c:choose>
			<c:when test='${discrepancyNote.assignedUserId != ""}'>
				<c:set var="userAccountId1" value="${discrepancyNote.assignedUserId}"/>
			</c:when>
			<c:otherwise>
				<c:set var="userAccountId1" value="0"/>
			</c:otherwise>
			</c:choose>
			<select name="userAccountId" id="userAccountId" class="formfieldL" >
				<option value="0">
		  		<c:forEach var="user" items="${userAccounts}">
		   		<c:choose>
		     	<c:when test="${userAccountId1 == user.userAccountId}">
		       		<option value="<c:out value="${user.userAccountId}"/>" selected><c:out value="${user.lastName}"/>, <c:out value="${user.firstName}"/> (<c:out value="${user.userName}"/>)
		     	</c:when>
		     	<c:otherwise>
		       		<option value="<c:out value="${user.userAccountId}"/>"><c:out value="${user.lastName}"/>, <c:out value="${user.firstName}"/> (<c:out value="${user.userName}"/>)
		     	</c:otherwise>
		   		</c:choose>
		 		</c:forEach>
			</select>
			</div>
		  	<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="userAccountId"/></jsp:include>
		</div>
	</span>
		
	<c:choose>
	<c:when test="${(parent == null || parent.id ==0 || unlock == 1) && autoView == 0}">
		<span id="user2" style="display:none">
	</c:when>
	<c:otherwise>
		<span id="user2" style="display:block">
	</c:otherwise>
	</c:choose>
	<div class="dnBoxCol1 dnBoxText"><fmt:message key="email_assigned_user" bundle="${resword}"/>:</div>
	<div class="dnBoxCol2 dnBoxText"><input name="sendEmail" value="1" type="checkbox"/></div>	
	</span>
	
	<c:set var= "noteEntityType" value="${discrepancyNote.entityType}"/>
	<c:if test="${enterData == '1' || canMonitor == '1' || noteEntityType != 'itemData' }">
		<c:choose>
		<c:when test="${writeToDB eq '1'}">
			<div class="dnBoxCol2"><input type="submit" name="SubmitExit" value="<fmt:message key="submit_close" bundle="${resword}"/>" class="button_medium" onclick="javascript:setValue('close<c:out value="${parentId}"/>','true');"></div>
		</c:when>
		<c:otherwise>
			<div class="dnBoxCol2"><input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium"></div>
		</c:otherwise>
		</c:choose>
	</c:if>
		
</div>
</div></div></div></div></div></div></div>
</div>

</form>
</body>
</html>
