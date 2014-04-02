<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:useBean scope="request" id="section" class="org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<jsp:useBean scope="request" id="displayItem" class="org.akaza.openclinica.bean.submit.DisplayItemBean" />
<jsp:useBean scope="request" id="responseOptionBean" class="org.akaza.openclinica.bean.submit.ResponseOptionBean" />
<jsp:useBean scope='request' id='formMessages' class='java.util.HashMap'/>

<script language="javascript">
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
                  '</td></tr>' +
                  '<tr><td align=\"center\">'+ auditLog +'</td></tr>' +
                  '</tbody></table></table></div>';
              return htmlgen;
        }

</script>
<c:set var="contextPath" value="${fn:replace(pageContext.request.requestURL, fn:substringAfter(pageContext.request.requestURL, pageContext.request.contextPath), '')}" />
<c:set var="inputType" value="${displayItem.metadata.responseSet.responseType.name}" />
<c:set var="itemId" value="${displayItem.item.id}" />
<c:set var="numOfDate" value="${param.key}" />
<c:set var="isLast" value="${param.isLast}" />
<c:set var="isFirst" value="${param.isFirst}" />
<c:set var="repeatParentId" value="${param.repeatParentId}" />
<c:set var="rowCount" value="${param.rowCount}" />
<c:set var="inputName" value="${repeatParentId}_[${repeatParentId}]input${itemId}" />
<c:set var="parsedInputName" value="${repeatParentId}_${rowCount}input${itemId}" />
<c:set var="isHorizontal" value="${param.isHorizontal}" />
<c:set var="defValue" value="${param.defaultValue}" />
<c:set var="totNew" value="${displayItem.totNew}"/>
<c:set var="totUpdated" value="${displayItem.totUpdated}"/>
<c:set var="totRes" value="${displayItem.totRes}"/>
<c:set var="totClosed" value="${displayItem.totClosed}"/>
<c:set var="totNA" value="${displayItem.totNA}"/>
<%-- What is the including JSP (e.g., doubleDataEntry)--%>
<c:set var="originJSP" value="${param.originJSP}" />
<c:set var="hasDataFlag" value="${hasDataFlag}" />
<c:set var="ddeEntered" value="${requestScope['ddeEntered']}" />
<!-- for the rows in model, input name processed by back-end servlet, needs to change them back to the name got from form, so we can show error frame around the input -->
<c:set var="autoParsedInputName" value="${repeatParentId}_${rowCount - manualRows}input${itemId}" />

<c:if test="${isLast == false && rowCount==0}">
  <c:set var="inputName" value="${repeatParentId}_${rowCount}input${itemId}" />
</c:if>

<c:if test="${isLast == false && rowCount >0}">
  <c:set var="inputName" value="${repeatParentId}_manual${rowCount}input${itemId}" />
  <c:set var="parsedInputName" value="${repeatParentId}_manual${rowCount}input${itemId}" />
</c:if>

<c:set var="isLocked" value="${param.isLocked}" />

<!--  is a data's value is blank, so monitor can enter discrepancy note -->
<c:set var="isBlank" value="0" />

<%-- for tab index. must start from 1, not 0--%>
<c:set var="tabNum" value="${param.tabNum+1}" />

 <c:if test="${empty displayItem.data.value}">
        <c:set var="isBlank" value="1" />
 </c:if>
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

<c:forEach var="frmMsg" items="${formMessages}">
   <c:if test="${(frmMsg.key eq parsedInputName) || (frmMsg.key eq autoParsedInputName)}">
    <c:set var="isInError" value="${true}" />
    <c:set var="errorTxtMessage" value="${frmMsg.value}" />
  </c:if>
</c:forEach>

 <c:if test="${isInError}">
      <c:set var="errorFlag" value="1"/><!--  use in discrepancy note-->
 </c:if>


<c:if test='${inputType=="file"}'>
	<label for="<c:out value="${inputName}"/>"></label>
	<c:choose>
	<c:when test="${empty displayItem.data.value}">
		<input type="text" id="ft<c:out value="${inputName}"/>" name="fileText<c:out value="${inputName}"/>" value="">
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
			<a href="DownloadAttachedFile?eventCRFId=<c:out value="${section.eventCRF.id}"/>&fileName=${fn:replace(fn:replace(filename,'+','%2B'),sep,sep2)}" id="a<c:out value="${itemId}"/>"><c:out value="${inputTxtValue}"/></a>
		</c:otherwise>
		</c:choose>
	</c:otherwise>
	</c:choose>
</c:if>
<c:if test='${inputType == "instant-calculation"}'>
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
</c:if>
<c:if test='${inputType == "text"}'>
  <%-- <c:out value="txt item"/> --%>
  <%-- add for error messages --%>
  <label for="<c:out value="${inputName}"/>"></label>
  <input type="hidden" id="defValue<c:out value="${inputName}"/>" name="defValue<c:out value="${inputName}"/>" value="<c:out value="${defValue}"/>"/>
 
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
        <img src="<c:out value="${contextPath}" />/images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0"/>

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
      <span class="aka_exclaim_error">! </span><textarea class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="<c:out value="${inputName}"/>" rows="5" cols="40"><c:out value="${inputTxtValue}"/></textarea>
    </c:when>
    <c:otherwise>
      <textarea id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="<c:out value="${inputName}"/>" rows="5" cols="40"><c:out value="${inputTxtValue}"/></textarea>
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
  <input type="hidden" id="defValue<c:out value="${inputName}"/>" name="defValue<c:out value="${inputName}"/>" value="<c:out value="${defValue}"/>"/>
 
  <c:choose>

    <c:when test="${isInError}">
      <span class="aka_exclaim_error">! </span>
      <select class="aka_input_error" id="<c:out value="${inputName}"/>" tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="<c:out value="${inputName}"/>" class="formfield">
          <%-- taken from showItemInput.jsp, somebody kind of forgot to put the options in there but added the </select>--%>
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
      </select>
    </c:when>

    <c:otherwise>
      <c:choose>
        <c:when test="${displayItem.metadata.defaultValue != '' &&
                displayItem.metadata.defaultValue != null}">
          <c:set var="printDefault" value="true"/>
        </c:when>
        <c:otherwise><c:set var="printDefault" value="false"/></c:otherwise>
      </c:choose>
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
     <input type="hidden" id="defValue<c:out value="${inputName}"/>" name="defValue<c:out value="${inputName}"/>" value="<c:out value="${defValue}"/>"/>
  
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
    <c:when test="${displayItem.numDiscrepancyNotes > 0}">
 
    <a tabindex="<c:out value="${tabNum + 1000}"/>" href="#" onmouseover="callTip(genToolTips(${itemId}));"
           onmouseout="UnTip()" onClick=
    "openDNoteWindow('ViewDiscrepancyNote?subjectId=<c:out value="${studySubject.id}" />&itemId=<c:out value="${itemId}" />&id=<c:out value="${displayItem.data.id}"/>&name=itemData&field=<c:out value="${parsedInputName}"/>&column=value&monitor=1&writeToDB=1&errorFlag=<c:out value="${errorFlag}"/>&isLocked=<c:out value="${isLocked}"/>','spanAlert-<c:out value="${parsedInputName}"/>','<c:out value="${errorTxtMessage}"/>'); return false;"
    ><img id="flag_<c:out value="${inputName}"/>" name="flag_input<c:out value="${inputName}" />" src=
    "<c:out value="${contextPath}" />/images/<c:out value="${imageFileName}"/>.gif" border="0" alt=
    "<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"
     ></a>

    </c:when>
    <c:otherwise>
    <c:set var="notLocked" value="no"/>
     <c:if test="${isLocked eq notLocked}">
      <c:set var="imageFileName" value="icon_noNote" />
 
     <a tabindex="<c:out value="${tabNum + 1000}"/>" href="#"  onmouseover="callTip(genToolTips(${itemId}));"
           onmouseout="UnTip()" onClick=
    "openDNWindow('CreateDiscrepancyNote?subjectId=<c:out value="${studySubject.id}" />&itemId=<c:out value="${itemId}" />&groupLabel=<c:out value="${displayItem.metadata.groupLabel}"/>&sectionId=<c:out value="${displayItem.metadata.sectionId}"/>&id=<c:out value="${displayItem.data.id}"/>&name=itemData&field=<c:out value="${parsedInputName}"/>&column=value&monitor=1&writeToDB=1&errorFlag=<c:out value="${errorFlag}"/>&isLocked=<c:out value="${isLocked}"/>','spanAlert-<c:out value="${parsedInputName}"/>','<c:out value="${errorTxtMessage}"/>'); return false;"
    ><img id="flag_<c:out value="${inputName}"/>" name="flag_<c:out value="${inputName}"/>" src=
    "<c:out value="${contextPath}" />/images/<c:out value="${imageFileName}"/>.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"
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