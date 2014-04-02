<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<jsp:useBean scope="request" id="section" class="org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<jsp:useBean scope="request" id="displayItem" class="org.akaza.openclinica.bean.submit.DisplayItemBean" />
<jsp:useBean scope='request' id='formMessages' class='java.util.HashMap'/>
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
<c:set var="contextPath" value="${fn:replace(pageContext.request.requestURL, fn:substringAfter(pageContext.request.requestURL, pageContext.request.contextPath), '')}" />
<c:set var="inputType" value="${displayItem.metadata.responseSet.responseType.name}" />
<c:set var="itemId" value="${displayItem.item.id}" />
<c:set var="inputVal" value="input${itemId}" />
<c:set var="numOfDate" value="${param.key}" />
<c:set var="defValue" value="${param.defaultValue}" />
<c:set var="respLayout" value="${param.respLayout}" />

<%-- What is the including JSP (e.g., doubleDataEntry)--%>
<c:set var="originJSP" value="${param.originJSP}" />
<%-- A boolean request attribute set in DataEntryServlet...--%>
<c:set var="hasDataFlag" value="${hasDataFlag}" />
<c:set var="ddeEntered" value="${requestScope['ddeEntered']}" />

<c:set var="isLocked" value="${param.isLocked}" />

<c:set var="isBlank" value="0" />

<c:if test="${empty displayItem.data.value}">
        <c:set var="isBlank" value="1" />
</c:if>

<c:if test="${(respLayout eq 'Horizontal' || respLayout eq 'horizontal')}">
  <c:set var="isHorizontal" value="${true}" />
</c:if>

<%-- text input value; the default value is not displayed if the application has data, or is
 not originating from doubleDataEntry--%>
<c:if test="${hasDataFlag == null || empty hasDataFlag}">
  <c:set var="hasDataFlag" value="${false}"/>
</c:if>
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
  </c:if>
</c:forEach>

 <c:if test="${isInError}">
      <c:set var="errorFlag" value="1"/><!--  use in discrepancy note-->
 </c:if>

<%-- A way to deal with the lack of 'break' out of forEach loop--%>
<c:if test='${inputType=="file"}'>
	<label for="input<c:out value="${itemId}"/>"></label>
	<c:choose>
	<c:when test="${empty displayItem.data.value}">
		<input type="text" id="ft<c:out value="${itemId}"/>" name="fileText<c:out value="${itemId}"/>" value="">
		<input type="button" id="up<c:out value="${inputName}"/>" name="uploadFile<c:out value="${inputName}"/>" value="<fmt:message key="click_to_upload" bundle="${resword}"/>">
	</c:when>
	<c:otherwise>
		<c:choose>
		<c:when test="${fn:contains(inputTxtValue, 'fileNotFound#')}">
			<del><c:out value="${fn:substringAfter(inputTxtValue,'fileNotFound#')}"/></del>
		</c:when>
		<c:otherwise>
			<c:set var="filename" value="${displayItem.data.value}"/>
			<c:set var="sep" value="\\"/>
            <c:set var="sep2" value="\\\\"/>
			<a href="DownloadAttachedFile?eventCRFId=<c:out value="${section.eventCRF.id}"/>&&fileName=${fn:replace(fn:replace(filename,'+','%2B'),sep,sep2)}" id="a<c:out value="${itemId}"/>"><c:out value="${inputTxtValue}"/></a>
		</c:otherwise>
		</c:choose>
	</c:otherwise>
	</c:choose>
</c:if>

<c:if test='${inputType == "instant-calculation"}'>
  <label for="input<c:out value="${itemId}"/>"></label>
  <c:choose>
    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
      "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" name="input<c:out value="${itemId}" />" value="<c:out value="${inputTxtValue}"/>" />
    </c:when>
    <c:otherwise>
      <input id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
        "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" name="input<c:out value="${itemId}" />" value="<c:out value="${inputTxtValue}"/>" />
    </c:otherwise>
  </c:choose>
</c:if>

<c:if test='${inputType == "text"}'>

  <label for="input<c:out value="${itemId}"/>"></label>
  <c:choose>
    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
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
        <img src="<c:out value="${contextPath}" />/images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" ID="anchor<c:out value="${itemId}"/>"/></a>

    <c:set var="numOfDate" value="${numOfDate+1}"/>
  </c:if>
</c:if>
<c:if test='${inputType == "textarea"}'>
  <label for="input<c:out value="${itemId}"/>"></label>
  <c:choose>
    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span><textarea class="aka_input_error" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="input<c:out value="${itemId}" />" rows="5" cols="40"><c:out value="${inputTxtValue}"/></textarea>
    </c:when>
    <c:otherwise>
      <textarea id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="input<c:out value="${itemId}" />" rows="5" cols="40"><c:out value="${inputTxtValue}"/></textarea>
    </c:otherwise>
  </c:choose>
</c:if>
<c:if test='${inputType == "checkbox"}'>
  <%-- What if the defaultValue is a comma- or space-separated value for
 multiple checkboxes or multi-select tags? --%>
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
      <c:when test="${isInError}">
        <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="checkbox" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
      </c:when>
      <c:otherwise>
        <input id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="checkbox" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
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
      <c:when test="${isInError}">
        <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="radio" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
      </c:when><c:otherwise>
      <input id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="radio" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
    </c:otherwise>
    </c:choose>
  </c:forEach>
</c:if>

<%-- adding some spacing to make this more readable, tbh --%>

<c:if test='${inputType == "single-select"}'>

  <label for="input<c:out value="${itemId}"/>"></label>

  <c:choose>

    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span>
      <select class="aka_input_error formfield" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="input<c:out value="${itemId}"/>">

      <%-- taken from lines 195-203 below --%>

      <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
            <c:choose>
              <c:when test="${count==selectedOption}">
              	<c:set var="checked" value="selected" />
              </c:when>
              <c:otherwise>
              	<c:set var="checked" value="" />
              </c:otherwise>
            </c:choose>

            <option value="<c:out value="${option.value}" />" <c:out value="${checked}"/>
                    <c:if test="${option.selected}">
      	                selected="selected"
                    </c:if>
                    >
            	    <c:out value="${option.text}" />
            </option>

            <c:set var="count" value="${count+1}"/>
          </c:forEach>
    </c:when>

    <c:otherwise>

    <c:choose>
    <%--
       <c:when test="${(originJSP eq 'doubleDataEntry' ||
  (! (originJSP eq 'administrativeEditing'))) && (ddeEntered || (! hasDataFlag))
  && (ddeEntered || (! sessionScope['groupHasData'])) &&
  			displayItem.metadata.defaultValue != '' && displayItem.metadata.defaultValue != null}">
  	 --%>
  	 <c:when test="${
                      (originJSP eq 'doubleDataEntry' || (! (originJSP eq 'administrativeEditing')))
                       && (ddeEntered != false || ( hasDataFlag != true))
                       && (ddeEntered != false || ( sessionScope['groupHasData'] != true))
                       && displayItem.metadata.defaultValue != '' && displayItem.metadata.defaultValue != null}">
        <c:set var="printDefault" value="true"/>
      </c:when>
      <c:otherwise>
      	<c:set var="printDefault" value="false"/>
      </c:otherwise>
    </c:choose>

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

    <select id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="input<c:out value="${itemId}"/>" class="formfield">

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
  <label for="input<c:out value="${itemId}"/>"></label>
  <c:choose>
    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span><select class="aka_input_error" id="input<c:out value="${itemId}"/>" multiple  tabindex="<c:out value="${tabNum}"/>" name="input<c:out value="${itemId}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');">
    </c:when><c:otherwise>
    <select id="input<c:out value="${itemId}"/>" multiple  tabindex="<c:out value="${tabNum}"/>" name="input<c:out value="${itemId}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');">
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
<label for="input<c:out value="${itemId}"/>"></label>
	<input type="hidden" name="input<c:out value="${itemId}"/>" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
  <c:choose>
    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span><input class="aka_input_error" id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
      "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" class="disabled" disabled="disabled" name="input<c:out value="${itemId}" />" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
    </c:when>
    <c:otherwise>
      <input id="input<c:out value="${itemId}"/>" tabindex="<c:out value="${tabNum}"/>" onChange=
        "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" class="disabled" disabled="disabled" name="input<c:out value="${itemId}" />" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
    </c:otherwise>
  </c:choose>
</c:if>
<c:if test="${displayItem.metadata.required}">
  <td valign="top"><span class="alert">*</span></td>
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
    <c:when test="${displayItem.numDiscrepancyNotes > 0}">

      <td valign="top"><a tabindex="<c:out value="${tabNum + 1000}"/>" href="#"  onmouseover="callTip(genToolTips(${itemId}));"
           onmouseout="UnTip()" onClick=
    "openDNoteWindow('ViewDiscrepancyNote?subjectId=<c:out value="${studySubject.id}" />&itemId=<c:out value="${itemId}" />&id=<c:out value="${displayItem.data.id}"/>&name=itemData&field=input<c:out value="${itemId}"/>&column=value&monitor=1&writeToDB=1&isLocked=<c:out value="${isLocked}"/>','spanAlert-input<c:out value="${itemId}"/>','<c:out value="${errorTxtMessage}"/>'); return false;"
    ><img id="flag_input<c:out value="${itemId}"/>" name="flag_input<c:out value="${itemId}"/>" src=
    "<c:out value="${contextPath}" />/images/<c:out value="${imageFileName}"/>.gif" border="0" alt=
    "<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"
    ></a></td>

    </c:when>
    <c:otherwise>

     <c:if test="${isLocked eq 'no'}">
      <c:set var="imageFileName" value="icon_noNote" />

       <td valign="top"><a tabindex="<c:out value="${tabNum + 1000}"/>" href="#"  onmouseover="callTip(genToolTips(${itemId}));"
           onmouseout="UnTip()" onClick=
    "openDNWindow('CreateDiscrepancyNote?subjectId=<c:out value="${studySubject.id}" />&itemId=<c:out value="${itemId}" />&id=<c:out value="${displayItem.data.id}"/>&name=itemData&field=input<c:out value="${itemId}"/>&column=value&monitor=1&blank=<c:out value="${isBlank}"/>&writeToDB=1&errorFlag=<c:out value="${errorFlag}"/>&isLocked=<c:out value="${isLocked}"/>','spanAlert-input<c:out value="${itemId}"/>','<c:out value="${errorTxtMessage}"/>'); return false;"
    ><img id="flag_input<c:out value="${itemId}" />" name="flag_input<c:out value="${itemId}" />" src=
    "<c:out value="${contextPath}" />/images/<c:out value="${imageFileName}"/>.gif" border="0" alt=
    "<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"
    ></a></td>
    </c:if>
    </c:otherwise>
  </c:choose>



</c:if>
