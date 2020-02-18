<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:useBean scope='request' id='whichResStatus' class='java.lang.String' />
<jsp:useBean scope='session' id='boxDNMap'  class="java.util.HashMap"/>
<jsp:useBean scope='session' id='boxToShow'  class="java.lang.String"/>


<script language="JavaScript" src="includes/global_functions_javascript.js"></script>
<script language="JavaScript">
<!--
function showOnly(strLeftNavRowElementName){
    var objLeftNavRowElement;

    objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
    if (objLeftNavRowElement != null) {
        if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
        objLeftNavRowElement.display = "block";
    }
}

function boxShowWithDefault(id, index, defaultId, defaultValue){
	showOnly("box"+id);
	var objSelect = MM_findObj("resStatusId"+id);
	if(objSelect != null) {
		objSelect.selectedIndex = index;
		objSelect.options[index].value = defaultId;
		objSelect.options[index].text = defaultValue;
	}
}

function removeLinkText(id) {
	var a = document.getElementById(id);
	if(a!=null) {
		a.innerHTML = "";	
	}
}

function addLinkText(id,text) {
	var a = document.getElementById(id);
	if(a!=null) {
		a.innerHTML = text;	
	}
}

function removeText(id,text) {
	var a = document.getElementById(id);
	if(a!=null) {
		a.innerHTML = "";	
	}
	var p = document.getElementById('p');
	if(p!=null) {
		p.innerHTML = text;	
	}
}

function addText(id,text) {
	var p = document.getElementById('p');
	if(p!=null) {
		p.innerHTML = "<a id='a0'></a>";
		var a = document.getElementById(id);
		if(a!=null) {	
			a.innerHTML = text;
			a.href = "javascript:showOnly('box0New');javascript:removeText('a0','"+ text + "');";
		}
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
	    
function setElements(typeId,user1,user2,id,filter1,nw,ud,rs,cl,na) {
	setStatusWithId(typeId,id,filter1,nw,ud,rs,cl,na);
	if(typeId == 3) {//query
		leftnavExpand(user1+id);
		leftnavExpand(user2+id);	
	}else {
		hide(user1+id);
		hide(user2+id);
	}
}

function setValue(elementName, value) {
	var element = MM_findObj(elementName);
	if(element != null) {
		element.value = value;
	}
}

function timeOutWindow(close,duration) {
	if(close == 'true') {
		window.setTimeout('window.close()', duration);	
	}
}

function setStatusWithId(typeId,id,filter1,nw,ud,rs,cl,na) {
	objtr1=document.getElementById('res1'+id);
	objtr2=document.getElementById('resStatusId'+id);
	if (typeId == 2|| typeId ==4) {//annotation or reason for change
  		objtr2.disabled = true;
  		objtr2.options.length = 0;
  		objtr2.options[0]=new Option(na, '5');
	} else {
		objtr2.disabled = false;
		if(id > 0 ) {
		} else {
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
}
  
function scrollToY(id) {
	var element = MM_findObj(id);
	var ypos= 0;
	while(element != null) {
		ypos += element.offsetTop;
		element = element.offsetParent;	
	}
  	window.scrollTo(0,ypos);
}

function setYPos(id) {
	var y = window.pageYOffset ? 
			window.pageYOffset : document.documentElement.scrollTop ? 
			document.documentElement.scrollTop : document.body.scrollTop;
	setValue("ypos"+id,y);
}
//-->
</script>

<c:set var="parentId" value="${param.parentId}"/>
<c:set var="boxId" value="${param.boxId}"/>
<c:set var="displayAll" value="none" />
<c:set var="discrepancyNote" value="${boxDNMap[parentId]}"/>
<c:set var="autoView" value=""/>
<c:forEach var="boxDN" items="${boxDNMap}">
	<c:if test="${parentId==boxDN.key}">
		<c:set var="discrepancyNote" value="${boxDNMap[boxDN.key]}"/>
	</c:if>
</c:forEach>
<c:forEach var="av" items="${autoViews}">
	<c:if test="${parentId==av.key}">
		<c:set var="autoView" value="${autoViews[av.key]}"/>
	</c:if>
</c:forEach>
<c:if test="${parentId==boxToShow}">
	<c:set var="displayAll" value="block"/>
</c:if>
<form name="oneDNForm" method="POST" action="CreateOneDiscrepancyNote"> 
<table border="0" cellpadding="0" cellspacing="0" style="float:left;display:<c:out value="${displayAll}"/>" id="${boxId}">
	<input type="hidden" name="parentId" value="${parentId}"/>
	<input type="hidden" name="viewDNLink${parentId}" value="${viewDNLink}"/>
	<input type="hidden" name="id" value="${param.entityId}"/>
	<input type="hidden" name="name" value="${param.entityType}"/>
	<input type="hidden" name="field" value="${param.field}"/>
	<input type="hidden" name="column" value="${param.column}"/>
	<input type="hidden" name="close${parentId}" value=""/>
	<input type="hidden" name="ypos${parentId}" value="0"/>
	<input type="hidden" name="eventCRFId" value="${eventCRFId}"/>

	<td valign="top">
	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TR"><div class="box_BL"><div class="box_BR">
	<div class="textbox_center">
	<table border="0" width="580">
		<c:if test="${parentId>0}">
			<div style="float:left"><fmt:message key="respond_this_Discrepancy_Note" bundle="${restext}"/></div>
		</c:if>
		<div style="float:right">
			<c:choose>
			<c:when test="${parentId==0}">
				<a href="javascript:scrollToY('p');" onclick="javascript:leftnavExpand('<c:out value="${boxId}"/>');javascript:addText('a0','<b><fmt:message key="begin_new_thread" bundle="${resword}"/></b>');"><img name="close_box" alt="<fmt:message key="Close_Box" bundle="${resword}"/>" src="images/bt_Remove.gif" class="icon_dnBox"></a>
			</c:when>
			<c:otherwise>
				<a href="javascript:scrollToY('msg<c:out value="${parentId}"/>');" onclick="javascript:leftnavExpand('<c:out value="${boxId}"/>');"><img name="close_box" alt="<fmt:message key="Close_Box" bundle="${resword}"/>" src="images/bt_Remove.gif" class="icon_dnBox"></a>
			</c:otherwise>
			</c:choose>
		</div>
		<div style="clear:both;"></div> 
		<div class="dnBoxCol1-1"><fmt:message key="description" bundle="${resword}"/>:<span class="alert">*</span></div>
		<div class="dnBoxCol2-1">
			<span id="description${parentId}">
				<div class="formfieldXL_BG"><input type="text" id="description${parentId}id" name="description${parentId}" value="<c:out value="${discrepancyNote.description}"/>" class="formfieldXL"></div>
				<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="description${parentId}"/></jsp:include>
			</span>
		</div>
		<div class="dnBoxCol1"><fmt:message key="detailed_note" bundle="${resword}"/>:</div>
		<div class="dnBoxCol2">
			<div class="formtextareaXL4_BG">
		  		<textarea name="detailedDes${parentId}" rows="4" cols="50" class="formtextareaXL4"><c:out value="${discrepancyNote.detailedNotes}"/></textarea>
			</div>
			<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="detailedDes${parentId}"/></jsp:include>
		</div>
		
		<c:choose>
		<c:when test="${parentId > 0}">
			<input type="hidden" name="typeId${parentId}" value="${param.typeId}"/>
		</c:when>
		<c:otherwise>
			<div class="dnBoxCol1"><fmt:message key="type" bundle="${resword}"/>:<span class="alert">*</span></div>
			<div class="dnBoxCol2"><div class="formfieldL_BG">
				<c:set var="typeIdl" value="${discrepancyNote.discrepancyNoteTypeId}"/>
				<c:choose>
				<c:when test="${whichResStatus == 22 || whichResStatus == 1}">
					<select name="typeId${parentId}" id="typeId${parentId}" class="formfieldL" onchange ="javascript:setElements(this.options[selectedIndex].value, 'user1', 'user2','<c:out value="${parentId}"/>','<c:out value="${whichResStatus}"/>','<fmt:message key="New" bundle="${resterm}"/>','<fmt:message key="Updated" bundle="${resterm}"/>','<fmt:message key="Resolution_Proposed" bundle="${resterm}"/>','<fmt:message key="Closed" bundle="${resterm}"/>','<fmt:message key="Not_Applicable" bundle="${resterm}"/>');">
						<c:forEach var="type" items="${discrepancyTypes2}">
						<c:choose>
						<c:when test="${typeIdl == type.id}">
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
					<select name="typeId${parentId}" id="typeId${parentId}" class="formfieldL" onchange ="javascript:setElements(this.options[selectedIndex].value, 'user1', 'user2', '<c:out value="${parentId}"/>','<c:out value="${whichResStatus}"/>','<fmt:message key="New" bundle="${resterm}"/>','<fmt:message key="Updated" bundle="${resterm}"/>','<fmt:message key="Resolution_Proposed" bundle="${resterm}"/>','<fmt:message key="Closed" bundle="${resterm}"/>','<fmt:message key="Not_Applicable" bundle="${resterm}"/>');">
						<c:forEach var="type" items="${discrepancyTypes}">
						<c:choose>
						<c:when test="${typeIdl == type.id}">
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
				<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="typeId${parentId}"/></jsp:include>
			</div></div>
		</c:otherwise>
		</c:choose>
		
		<span id="res1${parentId}">
			<div class="dnBoxCol1"><fmt:message key="Set_to_Status" bundle="${resword}"/>:<span class="alert">*</span></div>
			<div class="dnBoxCol2">
				<div class="formfieldL_BG">
				<c:set var="resStatusIdl" value="${discrepancyNote.resolutionStatusId}"/>
			    <select name="resStatusId${parentId}" id="resStatusId${parentId}" class="formfieldL">
					<c:choose>
					<c:when test="${(parentId>0 || whichResStatus==2 && discrepancyNote.discrepancyNoteTypeId==3) || whichResStatus==1}">
						<c:set var="resStatuses" value="${resolutionStatuses}"/>
					</c:when>
					<c:otherwise>
						<%-- for FVC; for Query, it will be set per setElements function --%>
						<c:set var="resStatuses" value="${resolutionStatuses2}"/>
					</c:otherwise>
					</c:choose>
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
				</select>
				</div>
			</div>
		</span>
		
		<c:choose>
		<c:when test="${autoView == 0}">
        	<span id="user1${parentId}" style="display:none">
		</c:when>
		<c:otherwise>
			<span id="user1${parentId}" style="display:block">
      	</c:otherwise>
		</c:choose>
			<div class="dnBoxCol1"><fmt:message key="assign_to_user" bundle="${resword}"/>:</div>
			<div class="dnBoxCol2" class="formfieldL_BG">
				<div class="formfieldL_BG">
					<c:choose>
					<c:when test='${discrepancyNote.assignedUserId != ""}'>
						<c:set var="userAccountId1" value="${discrepancyNote.assignedUserId}"/>
					</c:when>
					<c:otherwise>
						<c:set var="userAccountId1" value="0"/>
					</c:otherwise>
					</c:choose>
					<select name="userAccountId${parentId}" id="userAccountId${parentId}" class="formfieldL" >
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
		  		<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="userAccountId${parentId}"/></jsp:include>
			</div>
		</span>
		
		<c:choose>
		<c:when test="${autoView == 0}">
			<span id="user2${parentId}" style="display:none">
		</c:when>
		<c:otherwise>
			<span id="user2${parentId}" style="display:block">
		</c:otherwise>
		</c:choose>
			<div class="dnBoxCol1"><fmt:message key="email_assigned_user" bundle="${resword}"/>:</div>
			<div class="dnBoxCol2"><input name="sendEmail${parentId}" value="1" type="checkbox"/></div>
		</span>
		
		<c:set var= "noteEntityType" value="${discrepancyNote.entityType}"/>
		<c:if test="${enterData == '1' || canMonitor == '1' || noteEntityType != 'itemData' }">
			<div class="dnBoxCol3">
				<input type="submit" id="submitBtn${parentId}" name="Submit${parentId}" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium" onclick="javascript:setYPos('<c:out value="${parentId}"/>');">
				<input type="submit" name="SubmitExit${parentId}" value="<fmt:message key="submit_exit" bundle="${resword}"/>" class="button_medium" onclick="javascript:setValue('close<c:out value="${parentId}"/>','true');javascript:setYPos('<c:out value="${parentId}"/>');">
			</div>
		</c:if>
		<c:if test="${parentId==0}">
				<div style="float:left">
                	<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="newChildAdded${parentId}"/></jsp:include>	
                </div>
		</c:if>
	</table>
    </div>
	</div></div></div></div></div></div></div>
	</td>
</table>
</form>
<div style="clear:both;"></div>
