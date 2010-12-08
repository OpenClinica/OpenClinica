<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean scope='request' id='strResStatus' class='java.lang.String' />
<jsp:useBean scope='request' id='strUserAccountId' class='java.lang.String' />
<jsp:useBean scope='request' id='writeToDB' class='java.lang.String' />
<jsp:useBean scope='request' id='unlock' class='java.lang.String' />
<jsp:useBean scope='request' id='autoView' class='java.lang.String' />
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean' />
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<html>
<head>
<title><fmt:message key="openclinica" bundle="${resword}"/>- <fmt:message key="add_discrepancy_note" bundle="${resword}"/></title>
<link rel="stylesheet" href="includes/styles.css" type="text/css">
<script language="JavaScript" src="includes/global_functions_javascript.js"></script>

<script language="JavaScript" src="includes/CalendarPopup.js"></script>

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



    //-->
</script>

<style type="text/css">

.popup_BG { background-image: url(images/main_BG.gif);
	background-repeat: repeat-x;
	background-position: top;
	background-color: #FFFFFF;
	}


</style>

<script language="JavaScript">
<!--
function setStatus(typeId) {
	objtr1=document.getElementById('res1');
	objtr2=document.getElementById('resStatusId');
	if (typeId == 2|| typeId ==4) {//annotation or reason for change

	  	objtr2.value=5;
	  	objtr2.disabled = true;
	} else if (typeId == 3) { //query
		objtr2.value=1; //new
		objtr2.disabled = false;
	} else {
  		if (objtr2.value ==5 && objtr2.disabled) {
   			objtr2.value=1;
  		}

  		objtr2.disabled = false;

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


function setElements(typeId, user1, user2) {
	setStatus(typeId);
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
<body class="popup_BG" onload="javascript:setStatus(<c:out value="${discrepancyNote.discrepancyNoteTypeId}"/>);">
<%-- needs to run at first to possibly gray out the drop down, tbh 02/2010--%>
<div style="float: left;"><h1 class="title_manage"><fmt:message key="add_discrepancy_note" bundle="${resword}"/></h1></div>
<div style="float: right;"><p><a href="#" onclick="javascript:window.close();"><img name="close_window" alt="close_window" src="images/bt_Remove.gif" style="width:18px"></a></a></p></div>
<br clear="all">
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
<input type="hidden" name="new" value="<c:out value="${new}" />">
<input type="hidden" name="enterData" value="<c:out value="${enterData}" />">
<div>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
&nbsp;
</td>
</tr>
</table>
           
            
<c:set var="name" value="${discrepancyNote.entityType}"/>
<!-- Entity box -->
<table border="0" cellpadding="0" cellspacing="0" style="float:left;">
	<tr><td valign="bottom">
	<table border="0" cellpadding="0" cellspacing="0">
    	<tr><td nowrap style="padding-right: 20px;">
            <div class="tab_BG_h"><div class="tab_R_h" style="padding-right: 0px;"><div class="tab_L_h" style="padding: 3px 11px 0px 6px; text-align: left;">
			<b>
			<c:choose>
			    <c:when test="${name eq 'itemData' ||name eq 'ItemData'}">
			        <a href="javascript: openDocWindow('ViewItemDetail?itemId=<c:out value="${item.id}"/>')"><c:out value="${item.name}"/></a>
			    </c:when>
			    <c:otherwise>
			        <c:choose>
			            <c:when test="${entityName != '' && entityName != null }">
			                  <c:out value="${entityName}"/>  =  <c:out value="${entityValue}"/>
			            </c:when>
			            <c:otherwise>
			                <%-- nothing here; if entityName is blank --%>
			            </c:otherwise>
			        </c:choose>
			    </c:otherwise>
			</c:choose>
			</b>
			</div></div></div>
			</td></tr>
    </table>
    </td></tr>
    <tr><td valign="top">
		<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TR"><div class="box_BL"><div class="box_BR">
		<div class="textbox_center">
		<table border="0" cellpadding="0" cellspacing="0">
        <tr>
	        <td class="table_cell_noborder" style="color: #789EC5"><b><fmt:message key="subject" bundle="${resword}"/>:&nbsp;&nbsp;</b></td>
	        <td class="table_cell_noborder" style="color: #789EC5"><c:out value="${discrepancyNote.subjectName}" /></td>
	        <td class="table_cell_noborder" style="color: #789EC5; padding-left: 40px;"><b><fmt:message key="event" bundle="${resword}"/>:&nbsp;&nbsp;</b></td>
	        <td class="table_cell_noborder" style="color: #789EC5"><c:out value="${discrepancyNote.eventName}"/></td>
    	</tr>
        <tr>
            <td class="table_cell_noborder" style="color: #789EC5"><b><fmt:message key="event_date" bundle="${resword}"/>:&nbsp;&nbsp;</b></td>
            <td class="table_cell_noborder" style="color: #789EC5"><fmt:formatDate value="${discrepancyNote.eventStart}" pattern="${dteFormat}"/></td>
            <td class="table_cell_noborder" style="color: #789EC5; padding-left: 40px;"><b><fmt:message key="CRF" bundle="${resword}"/>:&nbsp;&nbsp;</b></td>
            <td class="table_cell_noborder" style="color: #789EC5"><c:out value="${discrepancyNote.crfName}"/></td>
        </tr>
        </table>
        </div>
        </div></div></div></div></div></div></div>
	</td></tr>
</table>

<br><br><br><br><br>
<h3 class="title_manage"><fmt:message key="add_note" bundle="${resword}"/></h3>

<!-- dn table -->
    <table border="0" cellpadding="0" cellspacing="0" style="float:left;">
	<td valign="top">
	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TR"><div class="box_BL"><div class="box_BR">
	<div class="textbox_center">
	<table border="0">
		<tr valign="top">
		<td class="table_cell_noborder"><fmt:message key="description" bundle="${resword}"/>:</td>
		<td class="table_cell_noborder">
		<div class="formfieldL_BG"><input type="text" name="description" value="<c:out value="${discrepancyNote.description}"/>" class="formfieldL"></div>
		<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="description"/></jsp:include>
		</td>
		
		<td class="table_cell_noborder">
		<table>
			<td class="table_cell_noborder"><fmt:message key="type" bundle="${resword}"/>:</td>
			<td class="table_cell_noborder" width="60%"><div class="formfieldL_BG">
			<c:choose>
			<c:when test="${parentId > 0}">
				<input type="hidden" name="typeId" value="${param.typeId}"/>
				<select name="pTypeId" id="pTypeId" class="formfieldL" disabled>
					<option value="<c:out value="${param.typeId}"/>" selected><c:out value="${param.typeName}"/>
				</select>
			</c:when>
			<c:otherwise>
				<c:set var="typeId1" value="${discrepancyNote.discrepancyNoteTypeId}"/>
				<c:choose>
				<c:when test="${whichResStatus == 2}">
					<select name="typeId" id="typeId" class="formfieldL" onchange ="javascript:setElements(this.options[selectedIndex].value, 'user1', 'user2');">
						<c:forEach var="type" items="${discrepancyTypes2}">
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
				</c:when>
				<c:otherwise>
					<select name="typeId" id="typeId" class="formfieldL" onchange ="javascript:setElements(this.options[selectedIndex].value, 'user1', 'user2');">
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
				</c:otherwise>
				</c:choose>
				<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="typeId"/></jsp:include>
			</c:otherwise>
			</c:choose>
			</div></td>
		</table>
		</td>
		</tr>
		
		<tr valign="top">
		<td  class="table_cell_noborder"><fmt:message key="detailed_note" bundle="${resword}"/>:</td>
		<td class="table_cell_noborder">
		<div class="formtextareaL4_BG">
		  <textarea name="detailedDes" rows="4" cols="50" class="formtextareaL4"><c:out value="${discrepancyNote.detailedNotes}"/></textarea>
		</div>
		<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="detailedDes"/></jsp:include>
		&nbsp;</td>
		
		<td class="table_cell_noborder">
		<table border="0" cellpadding="0" cellspacing="0">
		<tr valign="top" id="res1">
		    <td  class="table_cell_noborder"><fmt:message key="resolution_status" bundle="${resword}"/>:</td>
		    <td class="table_cell_noborder"><div class="formfieldL_BG">
			<c:set var="resStatusId1" value="${discrepancyNote.resolutionStatusId}"/>
		    <select name="resStatusId" id="resStatusId" class="formfieldL" onchange="javascript:setResStatus(3, <c:out value="${discrepancyNote.ownerId}"/>);">
				<c:choose>
				<c:when test="${whichResStatus == 2 && param.typeId == 3 && parentId > 0}">
					<c:set var="resStatuses" value="${resolutionStatuses2}"/>
				</c:when>
				<c:otherwise>
					<c:set var="resStatuses" value="${resolutionStatuses}"/>
				</c:otherwise>
				</c:choose>
				<c:forEach var="status" items="${resStatuses}">
					<c:choose>
					<c:when test="${resStatusId1 == status.id}">
					   <option value="<c:out value="${status.id}"/>" selected ><c:out value="${status.name}"/>
					</c:when>
					<c:otherwise>
					   <option value="<c:out value="${status.id}"/>" ><c:out value="${status.name}"/>
					</c:otherwise>
					</c:choose>
				</c:forEach>
			</select></div>
		    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="resStatusId"/></jsp:include></td>
		</tr>
		
		
		<c:choose>
		<c:when test="${(parent == null || parent.id ==0 || unlock == 1) && autoView == 0}">
        	<tr valign="top" id="user1" style="display:none">
		</c:when>
		<c:otherwise>
			<tr valign="top">
      	</c:otherwise>
		</c:choose>
		<c:if test="${discrepancyNote.discrepancyNoteTypeId != 1 || (discrepancyNote.discrepancyNoteTypeId==1 && discrepancyNote.parentDnId>0)}">
			<td class="table_cell_noborder"><fmt:message key="assign_to_user" bundle="${resword}"/>:</td>
			<td class="table_cell_noborder"><div class="formfieldL_BG">
			<c:choose>
			<c:when test='${discrepancyNote.assignedUserId != ""}'>
				<c:set var="userAccountId1" value="${discrepancyNote.assignedUserId}"/>
			</c:when>
			<c:otherwise>
				<c:set var="userAccountId1" value="0"/>
			</c:otherwise>
			</c:choose>
			<span id="xxx" disabled>
			<select name="userAccountId" id="userAccountId" class="formfieldL" >
		  		<c:forEach var="user" items="${userAccounts}">
		   		<c:choose>
		     	<c:when test="${userAccountId1 == user.userAccountId}">
		       		<option value="<c:out value="${user.userAccountId}"/>" selected><c:out value="${user.firstName}"/> <c:out value="${user.lastName}"/> (<c:out value="${user.userName}"/>)
		     	</c:when>
		     	<c:otherwise>
		       		<option value="<c:out value="${user.userAccountId}"/>"><c:out value="${user.firstName}"/> <c:out value="${user.lastName}"/> (<c:out value="${user.userName}"/>)
		     	</c:otherwise>
		   		</c:choose>
		 		</c:forEach>
			</select>
			</span>
			<input type="hidden" name="userAccountId" value="<c:out value="${userAccountId1}"/>"/>		
			<c:out value="${userAccountId}"/>
			</div>
		  	<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="userAccountId"/></jsp:include></td>
			</tr>
		
			<c:choose>
			<c:when test="${(parent == null || parent.id ==0 || unlock == 1) && autoView == 0}">
				<tr valign="top" id="user2" style="display:none">
			</c:when>
			<c:otherwise>
				<tr valign="top">
			</c:otherwise>
			</c:choose>
			<%-- should be an option for checked, unchecked, disabled--%>
			<td align="right"><input name="sendEmail" value="1" type="checkbox"/></td>
			<td  class="table_cell_noborder"><fmt:message key="email_assigned_user" bundle="${resword}"/></td>		
			</tr>
		</c:if>
		
		<tr>
		<c:set var= "noteEntityType" value="${discrepancyNote.entityType}"/>
		<c:if test="${enterData == '1' || canMonitor == '1' || noteEntityType != 'itemData' }">
			<c:choose>
			<c:when test="${writeToDB eq '1'}">
				<td><input type="submit" name="SubmitExit" value="<fmt:message key="submit_close" bundle="${resword}"/>" class="button_medium" style="width:90px" onclick="javascript:setValue('close<c:out value="${parentId}"/>','true');"></td>
			</c:when>
			<c:otherwise>
				<td><input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium" style="width:80px"></td>
			</c:otherwise>
			</c:choose>
		</c:if>
		</tr>
		
		<c:if test="${parentId==0}">
			<tr valign="top">
				<td class="table_cell_left">
                	<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="newChildAdded"/></jsp:include>	
                </td>
			</tr>
		</c:if>
	</table>
    </div>
	</div></div></div></div></div></div></div>
	</td>
</table>
</form>
</body>
</html>
