<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:useBean scope="request" id="section" class="org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<jsp:useBean scope="request" id="displayItem" class="org.akaza.openclinica.bean.submit.DisplayItemBean" />
<jsp:useBean scope="request" id="responseOptionBean" class="org.akaza.openclinica.bean.submit.ResponseOptionBean" />
<jsp:useBean scope='request' id='formMessages' class='java.util.HashMap'/>
<script type="text/JavaScript" language="JavaScript" src="includes/instant_onchange.js"></script>

<script lang="Javascript">
function genToolTips(itemId){
	var resStatus = new Array();
	var detailedNotes= new Array();
	var discrepancyType = new Array();
	var updatedDates = new Array();
	var i=0;
	var discNotes = new Array();
	var title = '<fmt:message key="tooltip_title1" bundle="${resword}"/>';
	var parentDnIds = new Array();
    var totNotes = 0;
	var footNote = '<fmt:message key="footNote" bundle="${resword}"/>';
    var auditLog = '';
	 		<c:set var="discrepancyNotes" value="1"/>
	    	<c:forEach var="itemsSection" items="${section.items}">

	   			if("${itemsSection.item.id}"== itemId)
	   			{

				<c:set var="notesSize" value="${itemsSection.totNew}"/>
	   			title = "<c:out value="${itemsSection.item.name}"/>";
	   				<c:set  var="discrepancyNotes" value="${itemsSection.discrepancyNotes}"/>
	        		<c:forEach var="discrepancyNotes" items="${discrepancyNotes}">
		             resStatus[i] =<c:out value="${discrepancyNotes.resolutionStatusId}"/>;
			      	    detailedNotes[i] ="<c:out value="${discrepancyNotes.description}"/>";
			      	    discrepancyType[i] = "<c:out value="${discrepancyNotes.disType.name}"/>";
			      	    updatedDates[i] = "<c:out value="${discrepancyNotes.createdDate}"/>";
						parentDnIds[i] = "<c:out value="${discrepancyNotes.parentDnId}"/>";
			   	    i++;

			   	 	</c:forEach>
					totNotes = 	 ${notesSize};
           			   if(totNotes >0) footNote = totNotes + " " + '<fmt:message key="foot_threads" bundle="${resword}"/>' + " " + '<fmt:message key="footNote_threads" bundle="${resword}"/>';
                       if("${itemsSection.data.auditLog}" == "true"){
                           auditLog = '<fmt:message key="audit_exist" bundle="${resword}" />';
                       }
	   			}

	    </c:forEach>
    //including tool tips for grouped items
        <c:forEach var="group" items="${section.displayFormGroups}">
	    	<c:forEach var="itemsSection" items="${group.items}">

	   			if("${itemsSection.item.id}"== itemId)
	   			{

				<c:set var="notesSize" value="${itemsSection.totNew}"/>
	   			title = "<c:out value="${itemsSection.item.name}"/>";
	   				<c:set  var="discrepancyNotes" value="${itemsSection.discrepancyNotes}"/>
	        		<c:forEach var="discrepancyNotes" items="${discrepancyNotes}">
		             resStatus[i] =<c:out value="${discrepancyNotes.resolutionStatusId}"/>;
			      	    detailedNotes[i] ="<c:out value="${discrepancyNotes.description}"/>";
			      	    discrepancyType[i] = "<c:out value="${discrepancyNotes.disType.name}"/>";
			      	    updatedDates[i] = "<c:out value="${discrepancyNotes.createdDate}"/>";
						parentDnIds[i] = "<c:out value="${discrepancyNotes.parentDnId}"/>";
			   	    i++;

			   	 	</c:forEach>
					totNotes = 	 ${notesSize};
           			   if(totNotes >0) footNote = totNotes + " " + '<fmt:message key="foot_threads" bundle="${resword}"/>' + " " + '<fmt:message key="footNote_threads" bundle="${resword}"/>';
                       if("${itemsSection.data.auditLog}" == "true"){
                           auditLog = '<fmt:message key="audit_exist" bundle="${resword}" />';
                       }
	   			}

	    </c:forEach>
    </c:forEach>

		  var htmlgen =
	          '<div class=\"tooltip\">'+
	          '<table  width="95%">'+
	          ' <tr><td  align=\"center\" class=\"header1\">' +title+
	          ' </td></tr><tr></tr></table><table  style="border-collapse:collapse" cellspacing="0" cellpadding="0" width="95%" >'+
	          drawRows(i,resStatus,detailedNotes,discrepancyType,updatedDates,parentDnIds)+
	          '</table><table width="95%"  class="tableborder" align="left">'+
	          '</table><table><tr></tr></table>'+
	          '<table width="95%"><tbody><td height="30" colspan="3"><span class=\"note\">'+footNote +'</span>'+
	          '</td></tr>'+
              '<tr><td align=\"center\">'+ auditLog +'</td></tr>' +
              '</tbody></table></table></div>';
		  return htmlgen;
	}

</script>
<%-- Some javascript functions for handling file data type -- ywang Dec.,2008 --%>
<script lang="Javascript">
function replaceSwitch(eventCRFId,itemId,id,attribute,str1,str2,filename,filePathName,status) {
	var rp = document.getElementById(id+itemId);
	var div = document.getElementById('div'+itemId);
	var a = document.getElementById('a'+itemId);
	var ft = document.getElementById('ft'+itemId);
	var up = document.getElementById('up'+itemId);
	<%-- uploadLink is different from showItemInput.jsp --%>
	var uploadLink = 'UploadFile?submitted=no&inputName=' + itemId;
	var downloadLink = 'DownloadAttachedFile?eventCRFId=' + eventCRFId + '&fileName=' + filePathName;
	if(rp.getAttribute('value')=="<fmt:message key="replace" bundle="${resword}"/>") {
		if(a) {
			div.appendChild(a);
			div.removeChild(a);
		}
		if(!ft) {
			var new_ft = document.createElement('input');
			new_ft.setAttribute("id","ft"+itemId);
			new_ft.setAttribute("type","text");
			new_ft.setAttribute("name","fileText"+itemId);
			new_ft.setAttribute("disabled","disabled");
			div.appendChild(new_ft);
			var new_up = document.createElement('input');
			new_up.setAttribute("id", "up"+itemId);
			new_up.setAttribute("type", "button");
			new_up.setAttribute("name", "uploadFile"+itemId);
			new_up.setAttribute("value", '<fmt:message key="click_to_upload" bundle="${resword}"/>');
			new_up.onclick = function(){var itemid=itemId; javascript:openDocWindow(uploadLink)};
			div.appendChild(new_up);
		}
		var fa = document.getElementById('fa'+itemId);
		fa.setAttribute("value","upload");
		div.appendChild(fa);
		switchStr(itemId,"rm","value",'<fmt:message key="cancel_remove" bundle="${resword}"/>','<fmt:message key="remove" bundle="${resword}"/>');
	} else if(rp.getAttribute('value')=='<fmt:message key="cancel_replace" bundle="${resword}"/>') {
		if(ft) {
			div.appendChild(ft);
			div.appendChild(up);
			div.removeChild(ft);
			div.removeChild(up);
		}
		if(!a) {
			if(status=='found') {
				var new_a = document.createElement('a');
				new_a.href = downloadLink;
				new_a.setAttribute("id","a"+itemId);
				new_a.appendChild(document.createTextNode(filename));
				div.appendChild(new_a);
			} else if(status=='notFound') {
				var new_a = document.createElement('del');
				new_a.setAttribute("id","a"+itemId);
				new_a.innerHTML = filename;
				div.appendChild(new_a);
			}
		}
		var fa = document.getElementById('fa'+itemId);
		fa.setAttribute("value","noAction");
		div.appendChild(fa);
	}
	switchAttribute(itemId,id,attribute,str1,str2);
}

function removeSwitch(eventCRFId,itemId,id,attribute,str1,str2,filename,filePathName,status) {
	var rm = document.getElementById(id+itemId);
	var div = document.getElementById('div'+itemId);
	var a = document.getElementById('a'+itemId);
	var ft = document.getElementById('ft'+itemId);
	var up = document.getElementById('up'+itemId);
	var input = document.getElementById(itemId);
	if(!input) {
		//which means this is a single item with itemId being a number
		input = document.getElementById('input'+itemId);
	}
	var downloadLink = 'DownloadAttachedFile?eventCRFId=' + eventCRFId + '&fileName=' + filePathName;
	if(rm.getAttribute('value')=='<fmt:message key="remove" bundle="${resword}"/>') {
		input.setAttribute("value","");
		if(a) {
			div.appendChild(a);
			div.removeChild(a);
		}
		if(ft) {
			div.appendChild(ft);
			div.appendChild(up);
			div.removeChild(ft);
			div.removeChild(up);
			switchStr(itemId,"rp","value",'<fmt:message key="cancel_replace" bundle="${resword}"/>','<fmt:message key="replace" bundle="${resword}"/>');
		}
		var new_a = document.createElement('del');
		new_a.setAttribute("id","a"+itemId);
		if(navigator.appName=="Microsoft Internet Explorer") {
			new_a.style.setAttribute("color","red");
		} else {
			new_a.setAttribute("style","color:red");
		}
		new_a.innerHTML = filename;
		div.appendChild(new_a);
		var fa = document.getElementById('fa'+itemId);
		fa.setAttribute("value","erase");
		div.appendChild(fa);
	} else if(rm.getAttribute('value')=='<fmt:message key="cancel_remove" bundle="${resword}"/>') {
		input.setAttribute("value",filename);
		if(a) {
			div.appendChild(a);
			div.removeChild(a);
			if(status=='found') {
				var new_a = document.createElement('a');
				new_a.href = downloadLink;
				new_a.setAttribute("id","a"+itemId);
				new_a.appendChild(document.createTextNode(filename));
				div.appendChild(new_a);
			} else if(status=='notFound') {
				var new_a = document.createElement('del');
				new_a.setAttribute("id","a"+itemId);
				new_a.innerHTML = filename;
				div.appendChild(new_a);
			}
		}
		var fa = document.getElementById('fa'+itemId);
		fa.setAttribute("value","noAction");
		div.appendChild(fa);
	}

	switchAttribute(itemId,id,attribute,str1,str2);
}

function switchAttribute(itemId, id, attribute, str1, str2) {
	var e = document.getElementById(id+itemId);
	if(e.getAttribute(attribute)==str1) {
		e.setAttribute(attribute,str2);
	}else if(e.getAttribute(attribute)==str2) {
		e.setAttribute(attribute,str1);
	}
}

function switchStr(itemId, id,attribute,str1,str2) {
	var e = document.getElementById(id+itemId);
	if(e.getAttribute(attribute)==str1) {
		e.setAttribute(attribute,str2);
	}
}
</script>

<c:set var="inputType" value="${displayItem.metadata.responseSet.responseType.name}" />
<c:set var="functionType" value="${displayItem.metadata.responseSet.options[0].value}"/>
<c:set var="itemId" value="${displayItem.item.id}" />
<c:set var="numOfDate" value="${param.key}" />
<c:set var="isLast" value="${param.isLast}" />
<c:set var="isNewItem" value="${param.isNewItem}" />

<c:set var="isFirst" value="${param.isFirst}" />
<c:set var="repeatParentId" value="${param.repeatParentId}" />
<c:set var="rowCount" value="${param.rowCount}" />
<c:set var="inputName" value="${repeatParentId}_[${repeatParentId}]input${itemId}" />
<c:set var="parsedInputName" value="${repeatParentId}_${rowCount}input${itemId}" />
<c:set var="isHorizontal" value="${param.isHorizontal}" />
<c:set var="defValue" value="${param.defaultValue}" />



<%-- What is the including JSP (e.g., doubleDataEntry)--%>
<c:set var="originJSP" value="${param.originJSP}" />
<c:set var="hasDataFlag" value="${hasDataFlag}" />
<c:set var="ddeEntered" value="${requestScope['ddeEntered']}" />
<c:if test="${isNewItem eq true }"> 
	<c:set property = "isNewItem" target="${displayItem }" value="true"/>
</c:if>

<c:if test="${isLast == false && rowCount==0}">
  <c:set var="inputName" value="${repeatParentId}_${rowCount}input${itemId}" />
</c:if>

<c:if test="${isLast == false && rowCount >0}">
  <c:set var="inputName" value="${repeatParentId}_manual${rowCount}input${itemId}" />
  <c:set var="parsedInputName" value="${repeatParentId}_manual${rowCount}input${itemId}" />
</c:if>
<c:set var="item_data_id"  value="${displayItem.data.id}" />
<c:if test="${item_data_id == 0}">
   <c:set var="item_data_id"  value="-1" />
</c:if> 
<c:set var="isLocked" value="${param.isLocked}" />

<!--  is a data's value is blank, so monitor can enter discrepancy note -->
<c:set var="isBlank" value="0" />

<%-- for tab index. must start from 1, not 0--%>
<c:set var="tabNum" value="${param.tabNum+1}" />

 <c:if test="${empty displayItem.data.value}">
        <c:set var="isBlank" value="1" />
 </c:if>
 
 <c:if test="${! empty formMessages}">
 	<c:set var="pageHasErrors" value="${true}" />
 </c:if>
  <c:set var="eventCRFId" value="${section.eventCRF.id}"/>
<%-- 24-May-2012 fix for issue #13822 do not display default values when page is displayed back with validation errors --%>
<c:choose>

  <c:when test="${(originJSP eq 'doubleDataEntry' ||
  (! (originJSP eq 'administrativeEditing'))) && (ddeEntered || (! hasDataFlag))
  && (ddeEntered || (! sessionScope['groupHasData'])) &&
  empty displayItem.metadata.responseSet.value && !pageHasErrors}">
    <c:set var="inputTxtValue" value="${defValue}"/>
  </c:when>
 
 <c:otherwise>
   <c:set var="inputTxtValue" value="${displayItem.metadata.responseSet.value}"/>
   </c:otherwise>
</c:choose>


<c:forEach var="frmMsg" items="${formMessages}">
   <c:if test="${(frmMsg.key eq parsedInputName)}">
    <c:set var="isInError" value="${true}" />
    <c:set var="errorTxtMessage" value="${frmMsg.value}" />
  </c:if>
</c:forEach>

<c:if test="${isNewItem eq true}">
 <c:set var="isInError" value="${false}" />
 </c:if>

 <c:if test="${isInError}">
      <c:set var="errorFlag" value="1"/><!--  use in discrepancy note-->
 </c:if>


<c:if test='${inputType=="file"}'>
	<label for="<c:out value="${inputName}"/>"></label>
	<c:set var="pathAndName" value="${displayItem.data.value}"/>
	<c:choose>
	<c:when test="${inputTxtValue==null || empty inputTxtValue || isLast}">
		<div id="div<c:out value="${inputName}"/>" name="myDiv">
		<c:choose>
    	<c:when test="${isInError}">
      		<span class="aka_exclaim_error">! </span><input class="aka_input_error" type="text" id="ft<c:out value="${inputName}"/>" name="fileText<c:out value="${inputName}"/>" disabled class="disabled">
		</c:when>
		<c:otherwise>
			<input type="text" id="ft<c:out value="${inputName}"/>" name="fileText<c:out value="${inputName}"/>" disabled class="disabled">
		</c:otherwise>
		</c:choose>
			<input type="button" id="up<c:out value="${inputName}"/>" name="uploadFile<c:out value="${inputName}"/>" value="<fmt:message key="click_to_upload" bundle="${resword}"/>" onClick="javascript:openDocWindow('UploadFile?submitted=no&itemId=<c:out value="${itemId}"/>&inputName=<c:out value="${inputName}"/>')">
			<input type="hidden" id="fa<c:out value="${inputName}"/>" name="fileAction<c:out value="${inputName}"/>" value="upload">
		</div>
		<c:choose>
		<c:when test="${isLast}">
        <input type="hidden" id="<c:out value="${inputName}"/>" name="<c:out value="${inputName}"/>" value >
		</c:when>
		<c:otherwise>
        <input type="hidden" id="<c:out value="${inputName}"/>" name="<c:out value="${inputName}"/>" value="<c:out value="${inputTxtValue}"/>">
		</c:otherwise>
		</c:choose>
	</c:when>
	<c:otherwise>
		<div id="div<c:out value="${inputName}"/>" name="myDiv">
		<c:if test="${isInError}">
      		<span class="<c:out value="aka_exclaim_error"/>">! </span>
      	</c:if>
		<c:choose>
		<c:when test="${fn:contains(inputTxtValue, 'fileNotFound#')}">
			<c:set var="inputTxtValue" value="${fn:substringAfter(inputTxtValue,'fileNotFound#')}"/>
			<del id="a<c:out value="${inputName}"/>"><c:out value="${inputTxtValue}"/></del>
			<input type="hidden" id="hidft<c:out value="${inputName}"/>" name="fileText<c:out value="${inputName}"/>" disabled class="disabled">
			<input type="hidden" id="hidup<c:out value="${inputName}"/>" name="uploadFile<c:out value="${inputName}"/>" value="<fmt:message key="click_to_upload" bundle="${resword}"/>" onClick="javascript:openDocWindow('UploadFile?submitted=no&itemId=<c:out value="${itemId}"/>&inputName=<c:out value="${inputName}"/>')">
		</div><br>
		<input id="rp<c:out value="${inputName}"/>" type="button" value="<fmt:message key="replace" bundle="${resword}"/>" onClick="replaceSwitch('<c:out value="${section.eventCRF.id}"/>','<c:out value="${inputName}"/>','rp','value','Replace','Cancel Replace','<c:out value="${inputTxtValue}"/>','<c:out value="${fn:replace(pathAndName,'+','%2B')}"/>','notFound')">
		<input id="rm<c:out value="${inputName}"/>" type="button" value="<fmt:message key="remove" bundle="${resword}"/>" onClick="removeSwitch('<c:out value="${section.eventCRF.id}"/>','<c:out value="${inputName}"/>','rm','value','Remove','Cancel Remove','<c:out value="${inputTxtValue}"/>','<c:out value="${fn:replace(pathAndName,'+','%2B')}"/>','notFound')">
		</c:when>
		<c:otherwise>
			<c:set var="prefilename" value="${displayItem.data.value}"/>
			<a href="DownloadAttachedFile?eventCRFId=<c:out value="${section.eventCRF.id}"/>&fileName=<c:out value="${fn:replace(prefilename,'+','%2B')}"/>" id="a<c:out value="${inputName}"/>"><c:out value="${inputTxtValue}"/></a>
			<input type="hidden" id="hidft<c:out value="${inputName}"/>" name="fileText<c:out value="${inputName}"/>" disabled class="disabled">
			<input type="hidden" id="hidup<c:out value="${inputName}"/>" name="uploadFile<c:out value="${inputName}"/>" value="<fmt:message key="click_to_upload" bundle="${resword}"/>" onClick="javascript:openDocWindow('UploadFile?submitted=no&itemId=<c:out value="${itemId}"/>&inputName=<c:out value="${inputName}"/>')">
		</div><br>
		<input id="rp<c:out value="${inputName}"/>" type="button" value="<fmt:message key="replace" bundle="${resword}"/>" onClick="replaceSwitch('<c:out value="${section.eventCRF.id}"/>','<c:out value="${inputName}"/>','rp','value','Replace','Cancel Replace','<c:out value="${inputTxtValue}"/>','<c:out value="${fn:replace(pathAndName,'+','%2B')}"/>','found')">
		<input id="rm<c:out value="${inputName}"/>" type="button" value="<fmt:message key="remove" bundle="${resword}"/>" onClick="removeSwitch('<c:out value="${section.eventCRF.id}"/>','<c:out value="${inputName}"/>','rm','value','Remove','Cancel Remove','<c:out value="${inputTxtValue}"/>','<c:out value="${fn:replace(pathAndName,'+','%2B')}"/>','found')">
		</c:otherwise>
		</c:choose>
		<input type="hidden" id="<c:out value="${inputName}"/>" name="<c:out value="${inputName}"/>" value="<c:out value="${inputTxtValue}"/>">
		<input type="hidden" id="fa<c:out value="${inputName}"/>" name="fileAction<c:out value="${inputName}"/>" value="noAction">
	</c:otherwise>
	</c:choose>
</c:if>
<c:if test='${inputType == "instant-calculation"}'>
  <label for="<c:out value="${inputName}"/>"></label>
  <input type="hidden" id="<c:out value="${inputName}"/>" name="<c:out value="${inputName}"/>" value="<c:out value="${inputTxtValue}"/>" >
  <c:choose>
    <c:when test="${isInError && !hasShown}">
      <span class="aka_exclaim_error">! </span><input class="aka_input_error"  id="show<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
      "this.className='changedField'; manualChange('<c:out value="${inputName}"/>'); javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');"
      type="text" name="show<c:out value="${inputName}"/>" value="<c:out value="${inputTxtValue}"/>" />
    </c:when>
    <c:otherwise>
      <input id="show<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
        "this.className='changedField'; manualChange('<c:out value="${inputName}"/>'); javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif','<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');"
        type="text" name="show<c:out value="${inputName}"/>" value="<c:out value="${inputTxtValue}"/>" />
    </c:otherwise>
  </c:choose>
</c:if>
<c:if test='${inputType == "text"}'>
  <%-- add for error messages --%>
  <label for="<c:out value="${inputName}"/>"></label>
  <input type="hidden" id="defValue<c:out value="${inputName}"/>" name="defValue<c:out value="${inputName}"/>" value="<c:out value="${defValue}"/>"/>
  <c:choose>
    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>"
      onChange="this.className='changedField';sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="text" name="<c:out value="${inputName}"/>" value="<c:out value="${inputTxtValue}"/>" />
    </c:when>
    <c:otherwise>
      <input id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
        "this.className='changedField'; sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />');javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="text" name="<c:out value="${inputName}"/>" value="<c:out value="${inputTxtValue}"/>" />
    </c:otherwise>
  </c:choose>
  <c:if test="${displayItem.item.itemDataTypeId==9 || displayItem.item.itemDataTypeId==10}"><!-- date type-->
    <A HREF="#"
       onmouseover="Calendar.setup({inputField  : getSib(this.previousSibling), ifFormat    : '<fmt:message key="date_format_calender" bundle="${resformat}"/>', button      : 'anchor<c:out value="${inputName}"/>' });"
       NAME="anchor<c:out value="${inputName}"/>"
       ID="anchor<c:out value="${inputName}"/>">
        <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0"/>

    </a>
    <%-- TODO l10n for the above line? --%>
    <c:set var="numOfDate" value="${numOfDate+1}"/>
  </c:if>
</c:if>
<c:if test='${inputType == "textarea"}'>
  <label for="<c:out value="${inputName}"/>"></label>
  <input type="hidden" id="defValue<c:out value="${inputName}"/>" name="defValue<c:out value="${inputName}"/>" value="<c:out value="${defValue}"/>"/>
  <c:choose>
    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span><textarea class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>"
      onChange="this.className='changedField'; sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" name="<c:out value="${inputName}"/>" rows="5" cols="40"><c:out value="${inputTxtValue}"/></textarea>
    </c:when>
    <c:otherwise>
      <textarea id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" title="<c:out value="${defValue}"/>"
      onChange="this.className='changedField';sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" name="<c:out value="${inputName}"/>" rows="5" cols="40"><c:out value="${inputTxtValue}"/></textarea>
    </c:otherwise>
  </c:choose>
</c:if>
<c:if test='${inputType == "checkbox"}'>
<%-- prepare default values for check box --%>
<c:set var="defValuesForCheckBox" value=""/>
<c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
     	<c:set var="defValuesForCheckBox" value="${defValuesForCheckBox}|||||${option.value}.....${option.text}" />
</c:forEach>
<c:choose>
<c:when test="${! empty defValue}">
	<c:set var="defValuesForCheckBox" value="${defValue}_____${defValuesForCheckBox}" />
</c:when>
<c:otherwise >
	<c:set var="defValuesForCheckBox" value="" />
</c:otherwise>
</c:choose>
 <input type="hidden" id="defValue<c:out value="${inputName}"/>" name="defValue<c:out value="${inputName}"/>" value="<c:out value="${defValuesForCheckBox}"/>"/>

  <c:if test="${! isHorizontal}">
    <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
      <c:choose>
        <c:when test="${option.selected}"><c:set var="checked" value="checked" /></c:when>
        <c:when test="${(option.text eq inputTxtValue) || (option.value eq inputTxtValue)}"><c:set var="checked" value="checked" />
        </c:when>
        <c:otherwise><c:set var="checked" value="" /></c:otherwise>
      </c:choose>
      <%-- handle multiple values --%>
      <c:forTokens items="${inputTxtValue}" delims=","  var="_item">
        <c:if test="${(option.text eq _item) || (option.value eq _item)}"><c:set var="checked" value="checked" />
        </c:if>
      </c:forTokens>
      <label for="<c:out value="${inputName}"/>"></label>
      <c:choose>
        <c:when test="${isInError}">
          <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>"
          onChange="this.className='changedField';sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="checkbox" name="<c:out value="${inputName}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <br/>
        </c:when>
        <c:otherwise>
          <input id="<c:out value="${inputName}"/>" 
		  tabindex="<c:out value="${tabNum}"/>" 
		  onChange="this.className='changedField'; sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />');setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="checkbox" name="<c:out value="${inputName}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <br/>
       </c:otherwise>
      </c:choose>
    </c:forEach>
  </c:if>
  <c:if test="${isHorizontal}">
    <%-- only one respOption displayed here, one per TD cell --%>
    <c:choose>
      <c:when test="${responseOptionBean.selected}"><c:set var="checked" value="checked" /></c:when>
      <c:when test="${(responseOptionBean.text eq inputTxtValue) || (responseOptionBean.value eq inputTxtValue)}"><c:set var="checked" value="checked" />
      </c:when>
      <c:otherwise><c:set var="checked" value="" /></c:otherwise>
    </c:choose>
    <%-- handle multiple values --%>
      <c:forTokens items="${inputTxtValue}" delims=","  var="_item">
        <c:if test="${(responseOptionBean.text eq _item) || (responseOptionBean.value eq _item)}"><c:set var="checked" value="checked" />
        </c:if>
      </c:forTokens>
    <label for="<c:out value="${inputName}"/>"></label>
    <c:choose>
      <c:when test="${isInError}">
        <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>"
        onChange="this.className='changedField';sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="checkbox" name="<c:out value="${inputName}"/>" value="<c:out value="${responseOptionBean.value}" />" <c:out value="${checked}"/> />
      </c:when>
      <c:otherwise>
        <input id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>"
        onChange="this.className='changedField';sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="checkbox" name="<c:out value="${inputName}"/>" value="<c:out value="${responseOptionBean.value}" />" <c:out value="${checked}"/> />
      </c:otherwise>
    </c:choose>

  </c:if>
</c:if>
<c:if test='${inputType == "radio"}'>
  <c:if test="${! isHorizontal}">
    <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
      <c:choose>
        <c:when test="${option.selected}"><c:set var="checked" value="checked" /></c:when>
        <c:when test="${(option.text eq inputTxtValue) || (option.value eq inputTxtValue)}"><c:set var="checked" value="checked" />
        </c:when>
        <c:otherwise><c:set var="checked" value="" /></c:otherwise>
      </c:choose>
      <label for="<c:out value="${inputName}"/>"></label>
      <c:choose>
        <c:when test="${isInError}">
          <!-- this.className='changedField';-->
          <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onclick="if(detectIEWindows(navigator.userAgent)){this.checked=true; unCheckSiblings(this,'vertical');}"
          onChange="if(! detectIEWindows(navigator.userAgent)){this.className='changedField';}sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="radio" name="<c:out value="${inputName}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /><c:if test="${! isHorizontal}"><c:out value="${option.text}" /></c:if> <br/>
        </c:when>
        <c:otherwise>
          <input id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>"
          onChange="if(! detectIEWindows(navigator.userAgent)){this.className='changedField';}sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" onclick="if(detectIEWindows(navigator.userAgent)){this.checked=true; unCheckSiblings(this,'vertical');}" type="radio" name="<c:out value="${inputName}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:if test="${! isHorizontal}"><c:out value="${option.text}" /></c:if> <br/>
        </c:otherwise>
      </c:choose>
    </c:forEach>
  </c:if>
  <c:if test="${isHorizontal}">
    <c:choose>
      <c:when test="${responseOptionBean.selected}"><c:set var="checked" value="checked" /></c:when>
      <c:when test="${(responseOptionBean.text eq inputTxtValue) || (responseOptionBean.value eq inputTxtValue)}"><c:set var="checked" value="checked" />
      </c:when>
      <c:otherwise><c:set var="checked" value="" /></c:otherwise>
    </c:choose>
    <%-- Only have one of these per radio button--%>
    <label for="<c:out value="${inputName}"/>"></label>
    <c:choose>
      <c:when test="${isInError}">
        <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onclick="if(detectIEWindows(navigator.userAgent)){this.checked=true; unCheckSiblings(this,'horizontal');}"
        onChange="if(! detectIEWindows(navigator.userAgent)){this.className='changedField';}sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="radio" name="<c:out value="${inputName}"/>" value="<c:out value="${responseOptionBean.value}" />" <c:out value="${checked}"/> />
      </c:when>
      <c:otherwise>
        <input id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onclick="if(detectIEWindows(navigator.userAgent)){this.checked=true; unCheckSiblings(this,'horizontal');}"
        onChange="if(! detectIEWindows(navigator.userAgent)){this.className='changedField';}sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="radio" name="<c:out value="${inputName}"/>" value="<c:out value="${responseOptionBean.value}" />" <c:out value="${checked}"/> />
      </c:otherwise>
    </c:choose>
  </c:if>
</c:if>



<c:if test='${inputType == "single-select"}'>

  <label for="<c:out value="${inputName}"/>"></label>
  <input type="hidden" id="defValue<c:out value="${inputName}"/>" name="defValue<c:out value="${inputName}"/>" value="<c:out value="${defValue}"/>"/>
 
  <c:choose>
  	<c:when test="${displayItem.metadata.defaultValue != '' &&
                displayItem.metadata.defaultValue != null}">
    	<c:set var="printDefault" value="true"/>
    </c:when>
    <c:otherwise><c:set var="printDefault" value="false"/></c:otherwise>
  </c:choose>
 <%-- 24-May-2012 fix for issue #13772 --%> 
   <%-- determine whether a default value is not included in response options; if it's not, then
include the default value first in the select list --%>
    <c:if test="${printDefault}">
        <c:set var="printDefaultFirst" value="true"/>
        <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
            <c:if test="${option.text eq displayItem.metadata.defaultValue}">
                <c:set var="printDefaultFirst" value="false"/>
            </c:if>
        </c:forEach>
    </c:if>
    
  <c:choose>
    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span>
      <select class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>"
      onChange="this.className='changedField'; sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" name="<c:out value="${inputName}"/>" class="formfield">
         
        <c:if test="${printDefaultFirst}">
             <option value="<c:out value="" />" selected="selected"
                            <c:out value=""/> ><c:out value="${displayItem.metadata.defaultValue}" />
             </option>
          </c:if>
    <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
                <option value="<c:out value="${option.value}" />"
                        <c:if test="${option.selected}"> selected="selected"</c:if>>
                    <c:out value="${option.text}" />
                </option>
        <c:set var="count" value="${count+1}"/>
    </c:forEach>
      </select>
    </c:when>

    <c:otherwise>

      <c:set var="selectedOption" value="-1"/>
      <c:set var="count" value="0"/>
      <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
        <c:if test="${option.selected}"><c:set var="selectedOption" value="${count}" /></c:if>
        <c:if test="${printDefault=='true'}">
          <c:if test="${displayItem.metadata.defaultValue == option.text || displayItem.metadata.defaultValue == option.value}">
            <c:set var="printDefault" value="false"/>
            <c:if test="${selectedOption==-1}"><c:set var="selectedOption" value="${count}"/></c:if>
          </c:if>
        </c:if>
        <c:set var="count" value="${count+1}"/>
      </c:forEach>
      <select id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>"
      onChange="this.className='changedField';sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" name="<c:out value="${inputName}"/>" class="formfield">
        <c:choose>
          <c:when test="${printDefault == 'true'}">
            <c:set var="count" value="0"/>
            <option value="<c:out value="" />" <c:out value=""/> ><c:out value="${displayItem.metadata.defaultValue}" /></option>
            <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
              <c:choose>
                <c:when test="${count==selectedOption}"><c:set var="checked" value="selected" /></c:when>
                <c:otherwise><c:set var="checked" value="" /></c:otherwise>
              </c:choose>
              <option value="<c:out value="${option.value}" />" <c:out value="${checked}"/> ><c:out value="${option.text}" /></option>
              <c:set var="count" value="${count+1}"/>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <c:set var="count" value="0"/>
            <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
              <c:choose>
                <c:when test="${count==selectedOption}"><c:set var="checked" value="selected" /></c:when>
                <c:otherwise><c:set var="checked" value="" /></c:otherwise>
              </c:choose>
              <option value="<c:out value="${option.value}" />" <c:out value="${checked}"/> ><c:out value="${option.text}" /></option>
              <c:set var="count" value="${count+1}"/>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </select>
    </c:otherwise>
  </c:choose>
</c:if>
<c:if test='${inputType == "multi-select"}'>
  <label for="<c:out value="${inputName}"/>"></label>
     <input type="hidden" id="defValue<c:out value="${inputName}"/>" name="defValue<c:out value="${inputName}"/>" value="<c:out value="${defValue}"/>"/>
  
  <c:choose>
    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span><select  class="aka_input_error" id="<c:out value="${inputName}"/>" multiple  tabindex=
      "<c:out value="${tabNum}"/>" name="<c:out value="${inputName}"/>"
      onChange="this.className='changedField';sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');">
    </c:when>
    <c:otherwise>
      <select id="<c:out value="${inputName}"/>" multiple  tabindex=
      "<c:out value="${tabNum}"/>" name="<c:out value="${inputName}"/>"
      onChange="this.className='changedField';sameRepGrpInstant('<c:out value="${inputName}"/>', '<c:out value="${itemId}"/>', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStr}" />', '<c:out value="${displayItem.instantFrontStrGroup.sameRepGrpFrontStr.frontStrDelimiter.code}" />'); javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');">
    </c:otherwise>
  </c:choose>
  <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
    <c:choose>
      <c:when test="${option.selected}"><c:set var="checked" value="selected" /></c:when>
      <c:otherwise><c:set var="checked" value="" /></c:otherwise>
    </c:choose>
    <%-- handle multiple values --%>
    <c:forTokens items="${inputTxtValue}" delims=","  var="_item">
      <c:if test="${(option.text eq _item) || (option.value eq _item)}"><c:set var="checked" value="selected" />
      </c:if>
    </c:forTokens>
    <option value="<c:out value="${option.value}" />" <c:out value="${checked}"/> ><c:out value="${option.text}" /></option>
  </c:forEach>
  </select>
</c:if>

<c:if test='${inputType == "calculation" || inputType == "group-calculation"}'>
	<%-- need to test for coding function here, tbh  --%>
	<c:set var="isAnExternalValue" value="0"/>
	<c:forTokens items="${functionType}" delims="\"" begin="0" end="0" var="functionName">
		<c:if test="${functionName eq 'func: getExternalValue(' || functionName eq 'func: getexternalvalue('}">
			<c:set var="isAnExternalValue" value="1"/>
		</c:if>
	</c:forTokens>
	<c:choose>
		<c:when test="${isAnExternalValue == '1'}">
			<c:set var="rowCountPlusOne" value="${rowCount + 10}"/>
			<label for="<c:out value="${inputName}"/>"></label>

			<%-- above for test, adding javascript below to support FF3 tbh 03/2007 --%>
			<script>
				if (window.attachEvent)
				{
					window.attachEvent("onmessage", receiver<c:out value="${parsedInputName}"/>); // for IE
				}
				else
				{
					window.addEventListener("message", receiver<c:out value="${parsedInputName}"/>, false); // for FF
				}
				function receiver<c:out value="${parsedInputName}"/>(e) {
					// alert(e.origin + ": " + e.source + " said " + e.data);
					if (e.data.substring(0,e.data.indexOf(":")) != 'mainForm.<c:out value="${inputName}"/>')
					{
						// alert(e.origin + ": said " + e.data);
						for (i = 0; i <= <c:out value="${rowCountPlusOne}"/> ; i++ )
						{
							// alert('trying: ' + 'mainForm.<c:out value="${repeatParentId}"/>_' + i + 'input<c:out value="${itemId}"/>');
							if (e.data.substring(0,e.data.indexOf(":")) == 'mainForm.<c:out value="${repeatParentId}"/>_' + i + 'input<c:out value="${itemId}"/>')
							{
								// alert(e.origin + ": but we found " + e.data);
								var inputName2 = '<c:out value="${repeatParentId}"/>_' + i + 'input<c:out value="${itemId}"/>';
								eval('document.crfForm.' + inputName2 + '.value = e.data.substring(e.data.indexOf(":") + 1);');
							}
							// added per bug #3861
							if (e.data.substring(0,e.data.indexOf(":")) == 'mainForm.<c:out value="${repeatParentId}"/>_manual' + i + 'input<c:out value="${itemId}"/>')
							{
								// alert(e.origin + ": but we found " + e.data);
								var inputName3 = '<c:out value="${repeatParentId}"/>_manual' + i + 'input<c:out value="${itemId}"/>';
								eval('document.crfForm.' + inputName3 + '.value = e.data.substring(e.data.indexOf(":") + 1);');
							}

						}

						return;
					}

				}
			</script>

		    <c:choose>
		        <c:when test="${isInError}">
      				<span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" readonly="readonly" onChange=
		          		"this.className='changedField'; javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="text" name="<c:out value="${inputName}" />" value="<c:out value="${inputTxtValue}"/>" />
		        </c:when>
		        <c:otherwise>
		            <input class="aka_input_readonly" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" readonly="readonly" onChange=
		              "this.className='changedField'; javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="text" name="<c:out value="${inputName}" />" value="<c:out value="${inputTxtValue}"/>" />
		        </c:otherwise>
		    </c:choose>
		</c:when>
		<c:otherwise>
			<input type="hidden" name="input<c:out value="${itemId}"/>" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
			<label for="<c:out value="${inputName}"/>"></label>
			<c:choose>
				<c:when test="${isInError}">
      				<span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="text" class="disabled" disabled="disabled" name="<c:out value="${inputName}"/>" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
				</c:when>
				<c:otherwise>
				<%-- new row should be empty --%>
				<c:choose>
					<c:when test="${isNewItem eq true }">
							<input id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
							"this.className='changedField'; javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="text" class="disabled" disabled="disabled" name="<c:out value="${inputName}"/>" value="" />
    		
					</c:when>
				
					<c:otherwise>
					
						<input id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
							"this.className='changedField'; javascript:setImageWithTitle('DataStatus_top','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>'); javascript:setImageWithTitle('DataStatus_bottom','images/icon_UnsavedData.gif', '<fmt:message key="changed_not_saved" bundle="${restext}"/>');" type="text" class="disabled" disabled="disabled" name="<c:out value="${inputName}"/>" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
    		</c:otherwise>
					</c:choose>
				
				
				
					</c:otherwise>
			</c:choose>
		</c:otherwise>
	</c:choose>
</c:if>
<c:if test="${displayItem.metadata.required}">
  <span class="alert">*</span>
</c:if>
<c:if test="${study.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
    <c:choose>
    <c:when test="${displayItem.discrepancyNoteStatus == 0}">
        <c:set var="imageFileName" value="icon_noNote" />
    </c:when>
    <c:when test="${displayItem.discrepancyNoteStatus == 1}">
        <c:set var="imageFileName" value="icon_Note" />
    </c:when>
    <c:when test="${displayItem.discrepancyNoteStatus == 2}">
        <c:set var="imageFileName" value="icon_flagYellow" />
    </c:when>
    <c:when test="${displayItem.discrepancyNoteStatus == 3}">
        <c:set var="imageFileName" value="icon_flagGreen" />
    </c:when>
    <c:when test="${displayItem.discrepancyNoteStatus == 4}">
        <c:set var="imageFileName" value="icon_flagBlack" />
    </c:when>
    <c:when test="${displayItem.discrepancyNoteStatus == 5}">
        <c:set var="imageFileName" value="icon_flagWhite" />
    </c:when>
    
    <c:otherwise>
    </c:otherwise>
  </c:choose>
  <c:choose>
    <c:when test="${displayItem.numDiscrepancyNotes > 0  and isNewItem != true}">

    <a tabindex="<c:out value="${tabNum + 1000}"/>" href="#"   onmouseover="callTip(genToolTips(${itemId}));"
           onmouseout="UnTip();" onClick=
    "openDNoteWindow('ViewDiscrepancyNote?rowCount=${param.rowCount}&eventCRFId=${eventCRFId}&isGroup=1&subjectId=<c:out value="${studySubject.id}" />&itemId=<c:out value="${itemId}" />&id=<c:out value="${item_data_id}"/>&name=itemData&field=<c:out value="${parsedInputName}"/>&column=value&monitor=1&writeToDB=1&errorFlag=<c:out value="${errorFlag}"/>&isLocked=<c:out value="${isLocked}"/>','spanAlert-<c:out value="${parsedInputName}"/>','<c:out value="${errorTxtMessage}"/>'); return false;"
    ><img id="flag_<c:out value="${inputName}"/>" name="flag_input<c:out value="${inputName}" />" src=
    "images/<c:out value="${imageFileName}"/>.gif" border="0" alt=
    "<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"
     ></a>


    </c:when>
    <c:otherwise>
     <c:if test="${(isLocked == null) || (isLocked eq 'no')}">
      <c:set var="imageFileName" value="icon_noNote" />
  		 
       
         <c:set var="eventName" value="${toc.studyEventDefinition.name}"/>
         <c:set var="eventDate" value="${toc.studyEvent.dateStarted}"/>
         <c:set var="crfName" value="${toc.crf.name} ${toc.crfVersion.name}"/>
		
		
       <a tabindex="<c:out value="${tabNum + 1000}"/>" href="#"  onmouseover="callTip(genToolTips(${itemId}));"
           onmouseout="UnTip();" onClick=
    "openDNWindow('CreateDiscrepancyNote?rowCount=${param.rowCount}&eventCRFId=${eventCRFId}&isGroup=1&subjectId=<c:out value="${studySubject.id}" />&itemId=<c:out value="${itemId}" />&groupLabel=<c:out value="${displayItem.metadata.groupLabel}"/>&sectionId=<c:out value="${displayItem.metadata.sectionId}"/>&id=<c:out value="${item_data_id}"/>&name=itemData&field=<c:out value="${inputName}"/>&column=value&monitor=1&errorFlag=<c:out value="${errorFlag}"/>&isLocked=<c:out value="${isLocked}"/>&eventName=${eventName}&eventDate=${eventDate}&crfName=${crfName}','spanAlert-<c:out value="${inputName}"/>','<c:out value="${errorTxtMessage}"/>'); return false;"
    ><img id="flag_<c:out value="${inputName}"/>" name="flag_<c:out value="${inputName}"/>" src=
    "images/<c:out value="${imageFileName}"/>.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"
    ></a>
    </c:if>
    </c:otherwise>
  </c:choose>



</c:if>

<c:if test='${inputType == "text"|| inputType == "textarea" ||
inputType == "multi-select" || inputType == "single-select" ||
inputType == "calculation" }'>
  <c:if test="${! (displayItem.item.units eq '')}">
    (<c:out value="${displayItem.item.units}"/>)
  </c:if>
</c:if>
<c:if test='${inputType == "radio"|| inputType == "checkbox"}'>
  <c:if test="${! isHorizontal}">
    <c:if test="${! (displayItem.item.units eq '')}">
      (<c:out value="${displayItem.item.units}"/>)
    </c:if>
  </c:if>
</c:if>