<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:useBean scope="request" id="section" class="org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<jsp:useBean scope="request" id="displayItem" class="org.akaza.openclinica.bean.submit.DisplayItemBean" />
<jsp:useBean scope="request" id="responseOptionBean" class="org.akaza.openclinica.bean.submit.ResponseOptionBean" />
<jsp:useBean scope='request' id='formMessages' class='java.util.HashMap'/>

<c:set var="inputType" value="${displayItem.metadata.responseSet.responseType.name}" />
<c:set var="functionType" value="${displayItem.metadata.responseSet.options[0].value}"/>
<c:set var="itemId" value="${displayItem.item.id}" />
<c:set var="numOfDate" value="${param.key}" />
<c:set var="isLast" value="${param.isLast}" />
<c:set var="isFirst" value="${param.isFirst}" />
<c:set var="repeatParentId" value="${param.repeatParentId}" />
<c:set var="rowCount" value="${param.rowCount}" />
<c:set var="inputName" value="${repeatParentId}_[${repeatParentId}]input${itemId}" />
<c:set var="parsedInputName" value="${repeatParentId}_${rowCount}input${itemId}" />
<c:set var="manualInputName" value="${repeatParentId}_manual${rowCount}input${itemId}" />
<c:set var="isHorizontal" value="${param.isHorizontal}" />
<c:set var="defValue" value="${param.defaultValue}" />
<%-- What is the including JSP (e.g., doubleDataEntry)--%>
<c:set var="originJSP" value="${param.originJSP}" />
<c:set var="hasDataFlag" value="${hasDataFlag}" />
<c:set var="ddeEntered" value="${requestScope['ddeEntered']}" />
<!-- for the rows in model, input name processed by back-end servlet, needs to change them back to the name got from form, so we can show error frame around the input -->
<c:set var="autoParsedInputName" value="${repeatParentId}_${rowCount - manualRows}input${itemId}" />

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

<c:if test="${isLast == false && rowCount==0}">
  <c:set var="inputName" value="${repeatParentId}_${rowCount}input${itemId}" />
</c:if>

<c:if test="${isLast == false && rowCount >0}">
  <c:set var="inputName" value="${repeatParentId}_manual${rowCount}input${itemId}" />
  <c:set var="parsedInputName" value="${repeatParentId}_manual${rowCount}input${itemId}" />
  <c:set var="autoParsedInputName" value="${repeatParentId}_manual${rowCount - manualRows}input${itemId}" />		
</c:if>

<%-- for tab index. must start from 1, not 0--%>
<c:set var="tabNum" value="${param.tabNum+1}" />

<%-- text input value--%>
<c:choose>
  <c:when test="${(originJSP eq 'doubleDataEntry' ||
  (! (originJSP eq 'administrativeEditing'))) && (ddeEntered || (! hasDataFlag))
  && (ddeEntered || (! sessionScope['groupHasData'])) &&
  empty displayItem.metadata.responseSet.value}">
    <c:set var="inputTxtValue" value="${defValue}"/>
  </c:when>
  <c:otherwise>
    <c:set var="inputTxtValue" value="${displayItem.metadata.responseSet.value}"/>
  </c:otherwise>
</c:choose>

<%--  for setting errors, AE only required one check but others can be more 'elastic' --%>
<%--  have removed the check for AE, instead have changed the auto parsed name  --%>
<c:forEach var="frmMsg" items="${formMessages}">
   <c:if test="${(frmMsg.key eq parsedInputName) || (frmMsg.key eq autoParsedInputName)}">
	<!-- setting error to true here, <c:out value="${parsedInputName}"/>, auto <c:out value="${autoParsedInputName}"/>, 
	row count <c:out value="${rowCount}"/>, manual rows <c:out value="${manualRows}"/> -->
	<%-- that can create a 'mirror' effect when an autoparsed input name gets picked up twice --%>
    <c:set var="isInError" value="${true}" />
    <c:set var="errorTxtMessage" value="${frmMsg.value}" />
    <c:set var="errorTxtMessage" value='<%= StringEscapeUtils.escapeJavaScript(pageContext.getAttribute("errorTxtMessage").toString()) %>' />
	<%-- can we pop off the messages so that they dont repeat? --%>
	
  </c:if>
</c:forEach>

<%-- end of for each loop for setting errors  --%>

 <c:if test="${isInError}">
      <c:set var="errorFlag" value="1"/><!--  use in discrepancy note-->
  </c:if>

<c:if test='${inputType=="file"}'>
	<label for="<c:out value="${inputName}"/>"></label>
	<input type="hidden" id="fileItemBegin" name="begin<c:out value="${inputName}"/>" value="<c:out value="${itemId}"/>">
	<c:set var="pathAndName" value="${displayItem.data.value}"/>
	<c:choose>
	<c:when test="${inputTxtValue==null || empty inputTxtValue}">
		<div id="div<c:out value="${inputName}"/>" name="myDiv">
			<input type="text" id="ft<c:out value="${inputName}"/>" name="fileText<c:out value="${inputName}"/>" disabled class="disabled">
			<input type="button" id="up<c:out value="${inputName}"/>" name="uploadFile<c:out value="${inputName}"/>" value="<fmt:message key="click_to_upload" bundle="${resword}"/>" onClick="javascript:openDocWindow('UploadFile?submitted=no&itemId=<c:out value="${itemId}"/>&inputName=<c:out value="${inputName}"/>')">
			<input type="hidden" id="fa<c:out value="${inputName}"/>" name="fileAction<c:out value="${inputName}"/>" value="upload">
		</div>
		<input type="hidden" id="<c:out value="${inputName}"/>" name="<c:out value="${inputName}"/>" value="<c:out value="${inputTxtValue}"/>">
	</c:when>
	<c:otherwise>
		<div id="div<c:out value="${inputName}"/>" name="myDiv">
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

<c:if test='${inputType == "text"}'>
  <%-- add for error messages --%>
  <label for="<c:out value="${inputName}"/>"></label>
  <c:choose>
    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" name="<c:out value="${inputName}"/>" value="<c:out value="${inputTxtValue}"/>" />
    </c:when>
    <c:otherwise>
      <input id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
        "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" name="<c:out value="${inputName}"/>" value="<c:out value="${inputTxtValue}"/>" />
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
  <c:choose>
    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span><textarea class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="<c:out value="${inputName}"/>" rows="5" cols="40"><c:out value="${inputTxtValue}"/></textarea>
    </c:when>
    <c:otherwise>
      <textarea id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="<c:out value="${inputName}"/>" rows="5" cols="40"><c:out value="${inputTxtValue}"/></textarea>
    </c:otherwise>
  </c:choose>
</c:if>
<c:if test='${inputType == "checkbox"}'>
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
          <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="checkbox" name="<c:out value="${inputName}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <br/>
        </c:when>
        <c:otherwise>
          <input id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="checkbox" name="<c:out value="${inputName}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <br/>
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
        <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="checkbox" name="<c:out value="${inputName}"/>" value="<c:out value="${responseOptionBean.value}" />" <c:out value="${checked}"/> />
      </c:when>
      <c:otherwise>
        <input id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="checkbox" name="<c:out value="${inputName}"/>" value="<c:out value="${responseOptionBean.value}" />" <c:out value="${checked}"/> />
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
          <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onclick="if(detectIEWindows(navigator.userAgent)){this.checked=true; unCheckSiblings(this,'vertical');}" onChange="if(! detectIEWindows(navigator.userAgent)){this.className='changedField';} javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="radio" name="<c:out value="${inputName}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /><c:if test="${! isHorizontal}"><c:out value="${option.text}" /></c:if> <br/>
        </c:when>
        <c:otherwise>
          <input id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="if(! detectIEWindows(navigator.userAgent)){this.className='changedField';} javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" onclick="if(detectIEWindows(navigator.userAgent)){this.checked=true; unCheckSiblings(this,'vertical');}" type="radio" name="<c:out value="${inputName}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:if test="${! isHorizontal}"><c:out value="${option.text}" /></c:if> <br/>
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
        <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onclick="if(detectIEWindows(navigator.userAgent)){this.checked=true; unCheckSiblings(this,'horizontal');}" onChange="if(! detectIEWindows(navigator.userAgent)){this.className='changedField';} javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="radio" name="<c:out value="${inputName}"/>" value="<c:out value="${responseOptionBean.value}" />" <c:out value="${checked}"/> />
      </c:when>
      <c:otherwise>
        <input id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onclick="if(detectIEWindows(navigator.userAgent)){this.checked=true; unCheckSiblings(this,'horizontal');}" onChange="if(! detectIEWindows(navigator.userAgent)){this.className='changedField';} javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="radio" name="<c:out value="${inputName}"/>" value="<c:out value="${responseOptionBean.value}" />" <c:out value="${checked}"/> />
      </c:otherwise>
    </c:choose>
  </c:if>
</c:if>



<c:if test='${inputType == "single-select"}'>

  <label for="<c:out value="${inputName}"/>"></label>
   <%-- determine whether a default value exists --%>
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

    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span>
      <select class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="<c:out value="${inputName}"/>" class="formfield">
          
          <c:if test="${printDefaultFirst}">
             <option value="<c:out value="" />" selected="selected"
                            <c:out value=""/> ><c:out value="${displayItem.metadata.defaultValue}" />
             </option>
          </c:if>
          
          <%-- taken from showItemInput.jsp, somebody kind of forgot to put the options in there but added the </select>--%>
        <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
          <%--<c:choose>
            <c:when test="${count==selectedOption}">
              <c:set var="checked" value="selected" />
            </c:when>
            <c:otherwise>
              <c:set var="checked" value="" />
            </c:otherwise>
          </c:choose>--%>
          <c:choose>
              <c:when test="${! printDefaultFirst}">
                  <option value="<c:out value="${option.value}" />" <c:out value="${checked}"/>
                          <c:if test="${option.selected}">
                              selected="selected"
                          </c:if>
                          >
                      <c:out value="${option.text}" />
                  </option>
              </c:when>
              <c:otherwise>
                  <option value="${option.value}">
                      <c:out value="${option.text}" />
                  </option>

              </c:otherwise>
          </c:choose>

                    <c:set var="count" value="${count+1}"/>
                </c:forEach>
            </select>
        </c:when>


    <c:otherwise>
      <%-- This code was moved to line 200: <c:choose>
        <c:when test="${displayItem.metadata.defaultValue != '' &&
                displayItem.metadata.defaultValue != null}">
          <c:set var="printDefault" value="true"/>
        </c:when>
        <c:otherwise><c:set var="printDefault" value="false"/></c:otherwise>
      </c:choose>--%>
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
      <select id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="<c:out value="${inputName}"/>" class="formfield">
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
  <c:choose>
    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span><select  class="aka_input_error" id="<c:out value="${inputName}"/>" multiple  tabindex=
      "<c:out value="${tabNum}"/>" name="<c:out value="${inputName}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');">
    </c:when>
    <c:otherwise>
      <select id="<c:out value="${inputName}"/>" multiple  tabindex=
      "<c:out value="${tabNum}"/>" name="<c:out value="${inputName}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');">
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
					
					// document.crfForm.<c:out value="${inputName}"/>.value = e.data.substring(e.data.indexOf(":") + 1);
					// document.crfForm.<c:out value="${parsedInputName}"/>.value = e.data.substring(e.data.indexOf(":") + 1);
				}
			</script> 
			
		    <c:choose>
		        <c:when test="${isInError}">
		            <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" readonly="readonly" onChange=
		          "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" name="<c:out value="${inputName}" />" value="<c:out value="${inputTxtValue}"/>" />
		        </c:when>
		        <c:otherwise>
		            <input class="aka_input_readonly" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" readonly="readonly" onChange=
		              "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" name="<c:out value="${inputName}" />" value="<c:out value="${inputTxtValue}"/>" />
		        </c:otherwise>
		    </c:choose>
		</c:when>
		<c:otherwise>
			<input type="hidden" name="input<c:out value="${itemId}"/>" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
			<label for="<c:out value="${inputName}"/>"></label>
			<c:choose>
				<c:when test="${isInError}">
					<span class="aka_exclaim_error">! </span><input class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" class="disabled" disabled="disabled" name="<c:out value="${inputName}"/>" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
				</c:when>
				<c:otherwise>
					<input id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
							"this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" class="disabled" disabled="disabled" name="<c:out value="${inputName}"/>" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
	</c:choose>
</c:if>
<c:if test="${displayItem.metadata.required}">
  <span class="alert">*</span>
</c:if>
<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}">
  <c:choose>
    <c:when test="${displayItem.numDiscrepancyNotes > 0}">
      <c:set var="imageFileName" value="icon_Note" />
    </c:when>
    <c:otherwise>
      <c:set var="imageFileName" value="icon_noNote" />
    </c:otherwise>
  </c:choose>

  <c:choose>
  <c:when test="${originJSP eq 'administrativeEditing'}">

   <a tabindex="<c:out value="${tabNum + 1000}"/>" href="#" onClick=
    "openDNoteWindow('ViewDiscrepancyNote?subjectId=<c:out value="${studySubject.id}" />&itemId=<c:out value="${itemId}" />&id=<c:out value="${displayItem.data.id}"/>&name=itemData&field=<c:out value="${parsedInputName}"/>&column=value&enterData=1&writeToDB=1','spanAlert-<c:out value="${parsedInputName}"/>','<c:out value="${errorTxtMessage}"/>'); return false;"
    ><img id="flag_<c:out value="${inputName}"/>" name="flag_<c:out value="${inputName}"/>" src=
    "images/<c:out value="${imageFileName}"/>.gif" border="0" alt=
    "<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"
     ></a>

  </c:when>
  <c:otherwise>

  <a tabindex="<c:out value="${tabNum + 1000}"/>" href="#" onClick=
    "openDNWindow('CreateDiscrepancyNote?subjectId=<c:out value="${studySubject.id}" />&itemId=<c:out value="${itemId}" />&groupLabel=<c:out value="${displayItem.metadata.groupLabel}"/>&sectionId=<c:out value="${displayItem.metadata.sectionId}"/>&id=<c:out value="${displayItem.data.id}"/>&name=itemData&field=<c:out value="${inputName}"/>&column=value&enterData=1&errorFlag=<c:out value="${errorFlag}"/>','spanAlert-<c:out value="${inputName}"/>','<c:out value="${errorTxtMessage}"/>'); return false;"
    ><img id="flag_<c:out value="${inputName}"/>" name="flag_<c:out value="${inputName}"/>" src=
    "images/<c:out value="${imageFileName}"/>.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"
    ></a>

   </c:otherwise>
   </c:choose>
</c:if>
<%-- we won't need this if we're not embedding error messages
<br><c:import url="../showMessage.jsp"><c:param name="key" value=
              "${inputName}" /></c:import>    --%>
<%--
adding units...
 if(responseName.equalsIgnoreCase("text") ||
      responseName.equalsIgnoreCase("textarea") ||
      responseName.equalsIgnoreCase("single-select") ||
      responseName.equalsIgnoreCase("multi-select")){

       td = this.addUnits(td,displayBean);
       //td = this.addRightItemText(td,displayBean);
    }
    if(responseName.equalsIgnoreCase("radio") ||
      responseName.equalsIgnoreCase("checkbox") ){
      String grLabel = displayBean.getMetadata().getGroupLabel();
      boolean grouped = (grLabel != null && (! "".equalsIgnoreCase(grLabel)) &&
      (! grLabel.equalsIgnoreCase("ungrouped")));

      if(! grouped) {
         td = this.addUnits(td,displayBean);
      }  else {
        //the radio or checkbox does appear in a group table
        //Do not add units if the layout is horizontal
        if(! displayBean.getMetadata().getResponseLayout().
          equalsIgnoreCase("Horizontal")){
           td = this.addUnits(td,displayBean);
        }
--%>
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