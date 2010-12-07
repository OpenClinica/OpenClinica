<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<jsp:useBean scope="request" id="section" class="org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<jsp:useBean scope="request" id="displayItem" class="org.akaza.openclinica.bean.submit.DisplayItemBean" />
<jsp:useBean scope='request' id='formMessages' class='java.util.HashMap'/>

<style type="text/css">

.tooltip {
		
	width:100%;
	
}

</style>

<c:set var="inputType" value="${displayItem.metadata.responseSet.responseType.name}" />
<c:set var="functionType" value="${displayItem.metadata.responseSet.options[0].value}"/>
<c:set var="itemId" value="${displayItem.item.id}" />
<c:set var="inputVal" value="input${itemId}" />
<c:set var="numOfDate" value="${param.key}" />
<c:set var="defValue" value="${param.defaultValue}" />
<c:set var="respLayout" value="${param.respLayout}" />
<%-- What is the including JSP (e.g., doubleDataEntry)--%>
<c:set var="originJSP" value="${param.originJSP}" />
<c:set var="isForcedRFC" value="${param.isForcedRFC}" />

<c:set var="totNew" value="${displayItem.totNew}"/>
<c:set var="totUpdated" value="${displayItem.totUpdated}"/>
<c:set var="totRes" value="${displayItem.totRes}"/>
<c:set var="totClosed" value="${displayItem.totClosed}"/>
<c:set var="totNA" value="${displayItem.totNA}"/>
<%-- A boolean request attribute set in DataEntryServlet...--%>
<c:set var="hasDataFlag" value="${hasDataFlag}" />
<c:set var="ddeEntered" value="${requestScope['ddeEntered']}" />

<c:if test="${(respLayout eq 'Horizontal' || respLayout eq 'horizontal')}">
  <c:set var="isHorizontal" value="${true}" />
</c:if>

<%-- text input value; the default value is not displayed if the application has data, or is
 not originating from doubleDataEntry--%>
<c:if test="${hasDataFlag == null || empty hasDataFlag}">
  <c:set var="hasDataFlag" value="${false}"/></c:if>
<c:choose>
  <c:when test="${(originJSP eq 'doubleDataEntry' ||
  (! (originJSP eq 'administrativeEditing'))) && (ddeEntered || (! hasDataFlag))
  && (ddeEntered || (! sessionScope['groupHasData']))   &&
  empty displayItem.metadata.responseSet.value}">
    <c:set var="inputTxtValue" value="${defValue}"/>
  </c:when>
  <c:otherwise>
    <c:set var="inputTxtValue" value="${displayItem.metadata.responseSet.value}"/>
  </c:otherwise>
</c:choose>

<%-- for tab index. must start from 1, not 0--%>
<c:set var="tabNum" value="${param.tabNum+1}" />

<%-- find out whether the item is involved with an error message, and if so, outline the
form element in red --%>


<c:forEach var="frmMsg" items="${formMessages}">
  <c:if test="${frmMsg.key eq inputVal}">
    <c:set var="isInError" value="${true}" />
    <c:set var="errorTxtMessage" value="${frmMsg.value}" />
    <c:set var="errorTxtMessage" value='<%= StringEscapeUtils.escapeJavaScript(pageContext.getAttribute("errorTxtMessage").toString()) %>' />
  </c:if>
</c:forEach>

 <c:if test="${isInError}">
      <c:set var="errorFlag" value="1"/><!--  use in discrepancy note-->
  </c:if>

<script language="JavaScript" src="includes/global_functions_javascript.js"></script>


<%-- Some javascript functions for handling file data type -- ywang Dec.,2008 --%>
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
				       <%--if(totNotes >0) footNote = '<fmt:message key="footNote_threads" bundle="${resword}"/>'+ totNotes+ '<fmt:message key="foot_threads" bundle="${resword}"/>' ;--%>
                       if("${itemsSection.data.auditLog}" == "true"){
                           auditLog = '<fmt:message key="audit_exist" bundle="${resword}" />';
                       }
	   			}
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
	          '</td></tr>' +
              '<tr><td align=\"center\">'+ auditLog +'</td></tr>' +
              '</tbody></table></table></div>';
		  return htmlgen;
	}
  
function replaceSwitch(eventCRFId,itemId,id,attribute,str1,str2,filename,pathAndName,status) {
	var rp = document.getElementById(id+itemId);
	var div = document.getElementById('div'+itemId);
	var a = document.getElementById('a'+itemId);
	var ft = document.getElementById('ft'+itemId);
	var up = document.getElementById('up'+itemId);
	var uploadLink = 'UploadFile?submitted=no&itemId=' + itemId;
	var downloadLink = 'DownloadAttachedFile?eventCRFId=' + eventCRFId + '&fileName=' + pathAndName;
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
	} else if(rp.getAttribute('value')=="<fmt:message key="cancel_replace" bundle="${resword}"/>") {
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

function removeSwitch(eventCRFId,itemId,id,attribute,str1,str2,filename,pathAndName,status) {
	var rm = document.getElementById(id+itemId);
	var div = document.getElementById('div'+itemId);
	var a = document.getElementById('a'+itemId);
	var ft = document.getElementById('ft'+itemId);
	var up = document.getElementById('up'+itemId);
	var input = document.getElementById('input'+itemId);
	var downloadLink = 'DownloadAttachedFile?eventCRFId=' + eventCRFId + '&fileName=' + pathAndName;
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

function conditionalShow(strLeftNavRowElementName){
    var objLeftNavRowElement;

    objLeftNavRowElement = MM_findObj("t"+strLeftNavRowElementName);
    if (objLeftNavRowElement != null) {
        if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
		if (objLeftNavRowElement.display = "none") { objLeftNavRowElement.display = "";}
    }
    objLeftNavRowElement1 = MM_findObj("hd"+strLeftNavRowElementName);
    if (objLeftNavRowElement1 != null) {
        if (objLeftNavRowElement1.style) { objLeftNavRowElement1 = objLeftNavRowElement1.style; }
		if (objLeftNavRowElement1.display = "none") { objLeftNavRowElement1.display = "";}
    }
    objLeftNavRowElement2 = MM_findObj("sub"+strLeftNavRowElementName);
    if (objLeftNavRowElement2 != null) {
        if (objLeftNavRowElement2.style) { objLeftNavRowElement2 = objLeftNavRowElement2.style; }
		if (objLeftNavRowElement2.display = "none") { objLeftNavRowElement2.display = "";}
    }
}

function conditionalHide(strLeftNavRowElementName){
    var objLeftNavRowElement;

    objLeftNavRowElement = MM_findObj("t"+strLeftNavRowElementName);
    if (objLeftNavRowElement != null) {
        if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
        objLeftNavRowElement.display = "none";
    }
    objLeftNavRowElement1 = MM_findObj("hd"+strLeftNavRowElementName);
    if (objLeftNavRowElement1 != null) {
        if (objLeftNavRowElement1.style) { objLeftNavRowElement1 = objLeftNavRowElement1.style; }
        objLeftNavRowElement1.display = "none";
    }
    objLeftNavRowElement2 = MM_findObj("sub"+strLeftNavRowElementName);
    if (objLeftNavRowElement2 != null) {
        if (objLeftNavRowElement2.style) { objLeftNavRowElement2 = objLeftNavRowElement2.style; }
        objLeftNavRowElement2.display = "none";
    }
}

function selectControlShow(element,scdPairStr) {
	var showIds = [];
	var n = 0;
	var m = 0;
	var hideIds = [];
	var arr = scdPairStr.split('-----');
	for(var j=1; j<arr.length; j+=2) {
		hideIds[m] = arr[j];
	    for(var i = 0; i < element.options.length; i++){
	        if(element.options[i].selected) {
		        if(element.options[i].value==arr[j+1]){
			        showIds[n] = arr[j]; 
			        hideIds[m] = -1;
			        ++n;
	        	}
	        }
	    }
	    ++m;
	}
	for(var i=0; i<showIds.length; ++i) {
		conditionalShow(showIds[i]);
	}
	for(var i=0; i<hideIds.length; ++i) {
		if(hideIds[i] != -1) {
			conditionalHide(hideIds[i]);
		}
	}
}

function checkControlShow(element,scdPairStr) {
	var arr = scdPairStr.split('-----');
	for(var j=1; j<arr.length; j+=2) {
		if(element.value==arr[j+1]) {
			if(element.checked) {
				conditionalShow(arr[j]);
	        } else {
		      	conditionalHide(arr[j]);
	        }
	    }
	}
}

function radioControlShow(element,scdPairStr) {
	var m = 0;
	var hideIds = [];
	var arr = scdPairStr.split('-----');
	for(var j=1; j<arr.length; j+=2) {
		hideIds[m] = arr[j];
		if(element.value==arr[j+1]) {
			if(element.checked) {
				conditionalShow(arr[j]);
			    hideIds[m] = -1;
	        }
	    }
	    ++m;
	}
	for(var i=0; i<hideIds.length; ++i) {
		if(hideIds[i] != -1) {
			conditionalHide(hideIds[i]);
		}
	}
}
</script>

<%-- A way to deal with the lack of 'break' out of forEach loop--%>
<c:if test='${inputType=="file"}'>
	<label for="input<c:out value="${itemId}"/>"></label>
	<c:set var="pathAndName" value="${displayItem.data.value}"/>
	<c:choose>
	<c:when test="${inputTxtValue==null || empty inputTxtValue}">
		<input type="hidden" id="input<c:out value="${itemId}"/>" name="input<c:out value="${itemId}"/>" value="<c:out value="${inputTxtValue}"/>">
		<div id="div<c:out value="${itemId}"/>" name="myDiv">
			<input type="text" id="ft<c:out value="${itemId}"/>" name="fileText<c:out value="${itemId}"/>" disabled>
			<input type="button" id="up<c:out value="${itemId}"/>" name="uploadFile<c:out value="${itemId}"/>" value="<fmt:message key="click_to_upload" bundle="${resword}"/>" onClick="javascript:openDocWindow('UploadFile?submitted=no&itemId=<c:out value="${itemId}"/>')">
			<input type="hidden" id="fa<c:out value="${itemId}"/>" name="fileAction<c:out value="${itemId}"/>" value="upload">
		</div>
	</c:when>
	<c:otherwise>
		<div id="div<c:out value="${itemId}"/>" name="myDiv">
		<c:choose>
		<c:when test="${fn:contains(inputTxtValue, 'fileNotFound#')}">
			<c:set var="inputTxtValue" value="${fn:substringAfter(inputTxtValue,'fileNotFound#')}"/>
			<del id="a<c:out value="${itemId}"/>"><c:out value="${inputTxtValue}"/></del>
		</div><br>
		<input id="rp<c:out value="${itemId}"/>" type="button" value="<fmt:message key="replace" bundle="${resword}"/>" onClick="replaceSwitch('<c:out value="${section.eventCRF.id}"/>','<c:out value="${itemId}"/>','rp','value','Replace','Cancel Replace','<c:out value="${inputTxtValue}"/>','<c:out value="${fn:replace(pathAndName,'+','%2B')}"/>','notFound')">
		<input id="rm<c:out value="${itemId}"/>" type="button" value="<fmt:message key="remove" bundle="${resword}"/>" onClick="removeSwitch('<c:out value="${section.eventCRF.id}"/>','<c:out value="${itemId}"/>','rm','value','Remove','Cancel Remove','<c:out value="${inputTxtValue}"/>','<c:out value="${fn:replace(pathAndName,'+','%2B')}"/>','notFound')">
		</c:when>
		<c:otherwise>
			<c:set var="prefilename" value="${pathAndName}"/>
			<a href="DownloadAttachedFile?eventCRFId=<c:out value="${section.eventCRF.id}"/>&fileName=<c:out value="${fn:replace(prefilename,'+','%2B')}"/>" id="a<c:out value="${itemId}"/>"><c:out value="${inputTxtValue}"/></a>
		</div><br>
		<input id="rp<c:out value="${itemId}"/>" type="button" value="<fmt:message key="replace" bundle="${resword}"/>" onClick="replaceSwitch('<c:out value="${section.eventCRF.id}"/>','<c:out value="${itemId}"/>','rp','value','Replace','Cancel Replace','<c:out value="${inputTxtValue}"/>','<c:out value="${pathAndName}"/>','found')">
		<input id="rm<c:out value="${itemId}"/>" type="button" value="<fmt:message key="remove" bundle="${resword}"/>" onClick="removeSwitch('<c:out value="${section.eventCRF.id}"/>','<c:out value="${itemId}"/>','rm','value','Remove','Cancel Remove','<c:out value="${inputTxtValue}"/>','<c:out value="${pathAndName}"/>','found')">
		</c:otherwise>
		</c:choose>
		<input type="hidden" id="input<c:out value="${itemId}"/>" name="input<c:out value="${itemId}"/>" value="<c:out value="${inputTxtValue}"/>">
		<input type="hidden" id="fa<c:out value="${itemId}"/>" name="fileAction<c:out value="${itemId}"/>" value="noAction">
	</c:otherwise>
	</c:choose>
</c:if>

<c:choose>
	<c:when test="${hasShown}">
		<c:set var="exclaim" value="aka_exclaim_show"/>
		<c:set var="input" value="aka_input_show"/>

	</c:when>
	<c:otherwise>
		<c:set var="exclaim" value="aka_exclaim_error"/>
		<c:set var="input" value="aka_input_error"/>
	</c:otherwise>
</c:choose>

<c:if test='${inputType == "text"}'>
  <label for="input<c:out value="${itemId}"/>"></label>
  <c:choose>
    <c:when test="${isInError && !hasShown}">
      <span class="<c:out value="${exclaim}"/>">! </span><input class="<c:out value="${input}"/>" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
      "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" name="input<c:out value="${itemId}" />" value="<c:out value="${inputTxtValue}"/>" />
    </c:when>
    <c:otherwise>
      <input id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
        "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" name="input<c:out value="${itemId}" />" value="<c:out value="${inputTxtValue}"/>" />
    </c:otherwise>
  </c:choose>
  <c:if test="${displayItem.item.itemDataTypeId==9 || displayItem.item.itemDataTypeId==10}"><!-- date type-->
    <A HREF="#" onmouseover="Calendar.setup({inputField  : getSib(this.previousSibling), ifFormat    : '<fmt:message key="date_format_calender" bundle="${resformat}"/>', button      : 'anchor<c:out value="${itemId}"/>' });"
       NAME="anchor<c:out value="${itemId}"/>" ID="anchor<c:out value="${itemId}"/>">
        <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" ID="anchor<c:out value="${itemId}"/>"/></a>

    <c:set var="numOfDate" value="${numOfDate+1}"/>
  </c:if>
</c:if>
<c:if test='${inputType == "textarea"}'>
  <label for="input<c:out value="${itemId}"/>"></label>
  <c:choose>
    <c:when test="${isInError && !hasShown}">
      <span class="<c:out value="${exclaim}"/>">! </span><textarea class="<c:out value="${input}"/>" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="input<c:out value="${itemId}" />" rows="5" cols="40"><c:out value="${inputTxtValue}"/></textarea>
    </c:when>
    <c:otherwise>
      <textarea id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="input<c:out value="${itemId}" />" rows="5" cols="40"><c:out value="${inputTxtValue}"/></textarea>
    </c:otherwise>
  </c:choose>
</c:if>
<c:if test='${inputType == "checkbox"}'>
  <%-- What if the defaultValue is a comma- or space-separated value for
 multiple checkboxes or multi-select tags? --%>
  <c:set var="allChecked" value=""/>
  <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
    <c:choose>
      <c:when test="${option.selected}"><c:set var="checked" value="checked" />
      </c:when>
      <c:otherwise><c:set var="checked" value="" /></c:otherwise>
    </c:choose>
     <%-- handle multiple values --%>
    <c:forTokens items="${inputTxtValue}" delims=","  var="_item">
      <c:if test="${(option.text eq _item) || (option.value eq _item)}"><c:set var="checked" value="checked" />
      </c:if>
    </c:forTokens>
    <label for="input<c:out value="${itemId}"/>"></label>
    <c:choose>
      <c:when test="${isInError && !hasShown}">
        <span class="<c:out value="${exclaim}"/>">! </span>
        <c:choose>
		<c:when test="${fn:length(displayItem.SCDPairs)>0}">
			<c:set var="scdPairStr" value=""/>
			<c:forEach var="aPair" items="${displayItem.SCDPairs}">
				<c:set var="scdPairStr" value="${scdPairStr}-----${aPair.SCDItemId}-----${aPair.optionValue}"/>
	    	</c:forEach>
	    	<input class="<c:out value="${input}"/>" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onClick="javascript:checkControlShow(this, '<c:out value="${scdPairStr}"/>');" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="checkbox" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
       	</c:when>
		<c:otherwise>
			<input class="<c:out value="${input}"/>" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="checkbox" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
      	</c:otherwise>
		</c:choose>
      </c:when>
      <c:otherwise>
      	<c:choose>
		<c:when test="${fn:length(displayItem.SCDPairs)>0}">
			<c:set var="scdPairStr" value=""/>
			<c:forEach var="aPair" items="${displayItem.SCDPairs}">
				<c:set var="scdPairStr" value="${scdPairStr}-----${aPair.SCDItemId}-----${aPair.optionValue}"/>
	    	</c:forEach>
    		<input id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onClick="javascript:checkControlShow(this, '<c:out value="${scdPairStr}"/>');" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="checkbox" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:out value="${isChecked}"/> <c:if test="${! isHorizontal}"><br/></c:if>
      	</c:when>
		<c:otherwise>
			<input id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="checkbox" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
      	</c:otherwise>
		</c:choose>
      	</c:otherwise>
    </c:choose>
  </c:forEach>
</c:if>
<c:if test='${inputType == "radio"}'>
    <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
        <c:choose>
            <c:when test="${option.selected}"><c:set var="checked" value="checked" /></c:when>
            <c:when test="${(option.text eq inputTxtValue) || (option.value eq inputTxtValue)}"><c:set var="checked" value="checked" />
            </c:when>
            <c:otherwise><c:set var="checked" value="" /></c:otherwise>
        </c:choose>
        <label for="input<c:out value="${itemId}"/>"></label>
        <c:choose>
            <c:when test="${isInError && !hasShown}">
                <span class="<c:out value="${exclaim}"/>">! </span>
            	<c:choose>
            	<c:when test="${fn:length(displayItem.SCDPairs)>0}">
            		<c:set var="scdPairStr" value=""/>
					<c:forEach var="aPair" items="${displayItem.SCDPairs}">
						<c:set var="scdPairStr" value="${scdPairStr}-----${aPair.SCDItemId}-----${aPair.optionValue}"/>
	    			</c:forEach>
	    			<input class="<c:out value="${input}"/>" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onClick="javascript:radioControlShow(this, '<c:out value="${scdPairStr}"/>');"  onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="radio" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
		        </c:when>
		        <c:otherwise>
		        	<input class="<c:out value="${input}"/>" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="radio" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
		        </c:otherwise>
	        	</c:choose>
            </c:when>
            <c:otherwise>
            	<c:choose>
            	<c:when test="${fn:length(displayItem.SCDPairs)>0}">
            		<c:set var="scdPairStr" value=""/>
					<c:forEach var="aPair" items="${displayItem.SCDPairs}">
						<c:set var="scdPairStr" value="${scdPairStr}-----${aPair.SCDItemId}-----${aPair.optionValue}"/>
	    			</c:forEach>
		            <input id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>"  onClick="javascript:radioControlShow(this, '<c:out value="${scdPairStr}"/>');" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="radio" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
		        </c:when>
		        <c:otherwise>
		        	<input id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>"  onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="radio" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
		        </c:otherwise>
	        	</c:choose>
        	</c:otherwise>
        </c:choose>
    </c:forEach>
</c:if>

<%-- adding some spacing to make this more readable, tbh --%>

<c:if test='${inputType == "single-select"}'>
	<label for="input<c:out value="${itemId}"/>"></label>
    <c:choose>
        <c:when test="${displayItem.metadata.defaultValue != '' &&
                displayItem.metadata.defaultValue != null}">
            <c:set var="printDefault" value="true"/>
        </c:when>
        <c:otherwise>
            <c:set var="printDefault" value="false"/>
        </c:otherwise>
    </c:choose>

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
    <c:when test="${isInError && !hasShown}">
		<span class="<c:out value="${exclaim}"/>">! </span>
		<c:choose>
		<c:when test="${fn:length(displayItem.SCDPairs)>0}">
			<c:set var="scdPairStr" value=""/>
			<c:forEach var="aPair" items="${displayItem.SCDPairs}">
				<c:set var="scdPairStr" value="${scdPairStr}-----${aPair.SCDItemId}-----${aPair.optionValue}"/>
			</c:forEach>
			<select class="<c:out value="${input}"/> formfield" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="javascript:selectControlShow(this, '<c:out value="${scdPairStr}"/>'); javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="input<c:out value="${itemId}"/>">
		</c:when>
		<c:otherwise>
			<select class="<c:out value="${input}"/> formfield" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="input<c:out value="${itemId}"/>">
		</c:otherwise>
		</c:choose>
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

    <%--This code was placed at ln 149:    <c:choose>
        <c:when test="${displayItem.metadata.defaultValue != '' &&
                displayItem.metadata.defaultValue != null}">
          <c:set var="printDefault" value="true"/>
        </c:when>
      <c:otherwise>
      	<c:set var="printDefault" value="false"/>
      </c:otherwise>
    </c:choose>--%>

    <c:set var="selectedOption" value="-1"/>
    <c:set var="count" value="0"/>
    <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">

      <c:if test="${option.selected}">
      	<c:set var="selectedOption" value="${count}" />
      </c:if>

      <c:if test="${printDefault=='true'}">
        <c:if test="${displayItem.metadata.defaultValue == option.text || displayItem.metadata.defaultValue == option.value}">
          <c:set var="printDefault" value="false"/>
          <c:if test="${selectedOption==-1}"><c:set var="selectedOption" value="${count}"/></c:if>
        </c:if>
      </c:if>

      <c:set var="count" value="${count+1}"/>
    </c:forEach>
    <c:choose>
	<c:when test="${fn:length(displayItem.SCDPairs)>0}">
		<c:set var="scdPairStr" value=""/>
		<c:forEach var="aPair" items="${displayItem.SCDPairs}">
			<c:set var="scdPairStr" value="${scdPairStr}-----${aPair.SCDItemId}-----${aPair.optionValue}"/>
    	</c:forEach>
    	<select id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="javascript:selectControlShow(this, '<c:out value="${scdPairStr}"/>'); this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="input<c:out value="${itemId}"/>" class="formfield">
    </c:when>
	<c:otherwise>
		<select id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="input<c:out value="${itemId}"/>" class="formfield">
	</c:otherwise>
	</c:choose>
      <c:choose>

        <c:when test="${printDefault == 'true'}">
          <c:set var="count" value="0"/>
          <option value="<c:out value="" />"
                  <c:out value=""/> ><c:out value="${displayItem.metadata.defaultValue}" />
          </option>
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
  <label for="input<c:out value="${itemId}"/>"></label>
  <c:choose>
    <c:when test="${isInError && !hasShown}">
      	<span class="<c:out value="${exclaim}"/>">! </span>
		<c:choose>
		<c:when test="${fn:length(displayItem.SCDPairs)>0}">
			<c:set var="scdPairStr" value=""/>
			<c:forEach var="aPair" items="${displayItem.SCDPairs}">
				<c:set var="scdPairStr" value="${scdPairStr}-----${aPair.SCDItemId}-----${aPair.optionValue}"/>
			</c:forEach>
      		<select class="<c:out value="${input}"/>" id="input<c:out value="${itemId}"/>" multiple  tabindex="<c:out value="${tabNum}"/>" name="input<c:out value="${itemId}"/>" onChange="javascript:selectControlShow(this, '<c:out value="${scdPairStr}"/>'); javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');">
		</c:when>
		<c:otherwise>
      		<select class="<c:out value="${input}"/>" id="input<c:out value="${itemId}"/>" multiple  tabindex="<c:out value="${tabNum}"/>" name="input<c:out value="${itemId}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');">
  		</c:otherwise>
		</c:choose>
    </c:when>
    <c:otherwise>
    	<c:choose>
    	<c:when test="${fn:length(displayItem.SCDPairs)>0}">
	    	<c:set var="scdPairStr" value=""/>
			<c:forEach var="aPair" items="${displayItem.SCDPairs}">
				<c:set var="scdPairStr" value="${scdPairStr}-----${aPair.SCDItemId}-----${aPair.optionValue}"/>
	    	</c:forEach>
    		<select id="input<c:out value="${itemId}"/>" multiple  tabindex="<c:out value="${tabNum}"/>" name="input<c:out value="${itemId}"/>" onChange="javascript:selectControlShow(this, '<c:out value="${scdPairStr}"/>'); this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');">
  		</c:when>
  		<c:otherwise>
  			<select id="input<c:out value="${itemId}"/>" multiple  tabindex="<c:out value="${tabNum}"/>" name="input<c:out value="${itemId}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');">
  		</c:otherwise>
  		</c:choose>
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
                        <label for="input<c:out value="${itemId}"/>"></label>
                        <%--<c:out value="${inputTxtValue}"/>--%>
                        <%-- above for test, adding javascript below to support FF3 tbh 03/2007 --%>
                        <script>
                                if (window.attachEvent)
                                {
                                        window.attachEvent("onmessage", receiver<c:out value="${itemId}"/>); // for IE
                                }
                                else
                                {
                                        window.addEventListener("message", receiver<c:out value="${itemId}"/>, false); // for FF
                                }
                                function receiver<c:out value="${itemId}"/>(e) {
                                        if (e.data.substring(0,e.data.indexOf(":")) != 'mainForm.input<c:out value="${itemId}"/>')
                                        {
                                                return;
                                        }
                                        document.crfForm.input<c:out value="${itemId}"/>.value = e.data.substring(e.data.indexOf(":") + 1);
                                        // document.crfForm.input<c:out value="${itemId}"/>.value = e.data;
                                }
                        </script>
                        <%--
                        <input type="hidden" name="input<c:out value="${itemId}"/>" id="input<c:out value="${itemId}"/>" value="<c:out value="${inputTxtValue}"/>" />
                        --%>
                    <c:choose>
                        <c:when test="${isInError && !hasShown}">
                            <span class="<c:out value="${exclaim}"/>">! </span><input class="<c:out value="${input}"/>" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" readonly="readonly" onChange=
                          "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" name="input<c:out value="${itemId}" />" value="<c:out value="${inputTxtValue}"/>" />
                        </c:when>
                        <c:otherwise>
                            <input class="aka_input_readonly" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" readonly="readonly" onChange=
                              "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" name="input<c:out value="${itemId}" />" value="<c:out value="${inputTxtValue}"/>" />
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <label for="input<c:out value="${itemId}"/>"></label>
                    <input type="hidden" name="input<c:out value="${itemId}"/>" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
                    <c:choose>
                        <c:when test="${isInError && !hasShown}">
                            <span class="<c:out value="${exclaim}"/>">! </span><input class="<c:out value="${input}"/>" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
                          "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" class="disabled" disabled="disabled" name="input<c:out value="${itemId}" />" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
                        </c:when>
                        <c:otherwise>
                            <input id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
                              "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" class="disabled" disabled="disabled" name="input<c:out value="${itemId}" />" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
                        </c:otherwise>
                    </c:choose>
        </c:otherwise>
        </c:choose>
</c:if>
<c:if test="${displayItem.metadata.required}">
  <td valign="top"><span class="alert">*</span></td>
</c:if>

<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}">
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
   <c:when test="${originJSP eq 'administrativeEditing'}">

   <c:choose>
    <c:when test="${(displayItem.numDiscrepancyNotes > 0) && (isForcedRFC eq 'false')}">

       <td valign="top"><a tabindex="<c:out value="${tabNum + 1000}"/>" href="#"   onmouseover="callTip(genToolTips(${itemId}));"
           onmouseout="UnTip()" onClick=
    "openDNoteWindow('ViewDiscrepancyNote?subjectId=<c:out value="${studySubject.id}" />&isRfc=0&itemId=<c:out value="${itemId}" />&id=<c:out value="${displayItem.data.id}"/>&name=itemData&field=input<c:out value="${itemId}"/>&column=value&monitor=1&writeToDB=1','spanAlert-input<c:out value="${itemId}"/>','<c:out value="${errorTxtMessage}"/>'); return false;"
    ><img id="flag_input<c:out value="${itemId}" />" name="flag_input<c:out value="${itemId}" />" src=
    "images/<c:out value="${imageFileName}"/>.gif" border="0" alt=
    "<fmt:message key="discrepancy_note" bundle="${resword}"/>" 
     ></a></td>

    </c:when>
    <c:when test="${(displayItem.numDiscrepancyNotes > 0) && (isForcedRFC eq 'true')}">
	
		<td valign="top"><a tabindex="<c:out value="${tabNum + 1000}"/>" href="#"    onmouseover="callTip(genToolTips(${itemId}));"
           onmouseout="UnTip()" onClick=
    "openDNWindow('ViewDiscrepancyNote?subjectId=<c:out value="${studySubject.id}" />&isRfc=1&itemId=<c:out value="${itemId}" />&groupLabel=<c:out value="${displayItem.metadata.groupLabel}"/>&sectionId=<c:out value="${displayItem.metadata.sectionId}"/>&id=<c:out value="${displayItem.data.id}"/>&name=itemData&field=input<c:out value="${itemId}" />&column=value&enterData=1&writeToDB=0&errorFlag=<c:out value="${errorFlag}"/>','spanAlert-input<c:out value="${itemId}"/>','<c:out value="${errorTxtMessage}"/>'); return false;"
    ><img id="flag_input<c:out value="${itemId}" />" name="flag_input<c:out value="${itemId}" />" src=
    "images/<c:out value="${imageFileName}"/>.gif" border="0" alt=
    "<fmt:message key="discrepancy_note" bundle="${resword}"/>" 
      ></a></td>

	</c:when>
	<c:when test="${isForcedRFC eq 'true'}">
	
		<td valign="top"><a tabindex="<c:out value="${tabNum + 1000}"/>" href="#"    onmouseover="callTip(genToolTips(${itemId}));"
           onmouseout="UnTip()" onClick=
    "openDNWindow('CreateDiscrepancyNote?subjectId=<c:out value="${studySubject.id}" />&isRfc=1&itemId=<c:out value="${itemId}" />&groupLabel=<c:out value="${displayItem.metadata.groupLabel}"/>&sectionId=<c:out value="${displayItem.metadata.sectionId}"/>&id=<c:out value="${displayItem.data.id}"/>&name=itemData&field=input<c:out value="${itemId}" />&column=value&enterData=1&writeToDB=0&errorFlag=<c:out value="${errorFlag}"/>','spanAlert-input<c:out value="${itemId}"/>','<c:out value="${errorTxtMessage}"/>'); return false;"
    ><img id="flag_input<c:out value="${itemId}" />" name="flag_input<c:out value="${itemId}" />" src=
    "images/<c:out value="${imageFileName}"/>.gif" border="0" alt=
    "<fmt:message key="discrepancy_note" bundle="${resword}"/>" 
      ></a></td>

	</c:when>
    <c:otherwise>

       <td valign="top"><a tabindex="<c:out value="${tabNum + 1000}"/>" href="#"   onmouseover="callTip(genToolTips(${itemId}));"
           onmouseout="UnTip()" onClick=
    "openDNWindow('CreateDiscrepancyNote?subjectId=<c:out value="${studySubject.id}" />&itemId=<c:out value="${itemId}" />&groupLabel=<c:out value="${displayItem.metadata.groupLabel}"/>&sectionId=<c:out value="${displayItem.metadata.sectionId}"/>&id=<c:out value="${displayItem.data.id}"/>&name=itemData&field=input<c:out value="${itemId}" />&column=value&enterData=1&writeToDB=1&errorFlag=<c:out value="${errorFlag}"/>','spanAlert-input<c:out value="${itemId}"/>','<c:out value="${errorTxtMessage}"/>'); return false;"
    ><img id="flag_input<c:out value="${itemId}" />" name="flag_input<c:out value="${itemId}" />" src=
    "images/<c:out value="${imageFileName}"/>.gif" border="0" alt=
    "<fmt:message key="discrepancy_note" bundle="${resword}"/>" 
      ></a></td>

    </c:otherwise>
   </c:choose>

    </c:when>
    <c:otherwise>

    <td valign="top"><a tabindex="<c:out value="${tabNum + 1000}"/>" href="#"   onmouseover="callTip(genToolTips(${itemId}));"
           onmouseout="UnTip()" onClick=
    "openDNWindow('CreateDiscrepancyNote?subjectId=<c:out value="${studySubject.id}" />&itemId=<c:out value="${itemId}" />&groupLabel=<c:out value="${displayItem.metadata.groupLabel}"/>&sectionId=<c:out value="${displayItem.metadata.sectionId}"/>&id=<c:out value="${displayItem.data.id}"/>&name=itemData&field=input<c:out value="${itemId}" />&column=value&enterData=1&errorFlag=<c:out value="${errorFlag}"/>','spanAlert-input<c:out value="${itemId}"/>','<c:out value="${errorTxtMessage}"/>'); return false;"
    ><img id="flag_input<c:out value="${itemId}" />" name="flag_input<c:out value="${itemId}" />" src=
    "images/<c:out value="${imageFileName}"/>.gif" border="0" alt=
    "<fmt:message key="discrepancy_note" bundle="${resword}"/>"
    ></a></td>
    </c:otherwise>
    </c:choose>
</c:if>
