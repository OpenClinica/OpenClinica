<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<jsp:useBean scope="request" id="section" class="org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<jsp:useBean scope="request" id="displayItem" class="org.akaza.openclinica.bean.submit.DisplayItemBean" />

<c:set var="inputType" value="${displayItem.metadata.responseSet.responseType.name}" />
<c:set var="itemId" value="${displayItem.item.id}" />
<c:set var="numOfDate" value="${param.key}" />
<c:set var="defValue" value="${param.defaultValue}" />
<c:set var="respLayout" value="${param.respLayout}" />

<c:if test="${(respLayout eq 'Horizontal' || respLayout eq 'horizontal')}">
  <c:set var="isHorizontal" value="${true}" />
</c:if>

<c:choose>
  <c:when test="${empty displayItem.metadata.responseSet.value}">
    <c:set var="inputTxtValue" value="${defValue}"/>
  </c:when>
  <c:otherwise>
     <c:set var="inputTxtValue" value="${displayItem.metadata.responseSet.value}"/>
  </c:otherwise>
</c:choose>

<%-- for tab index. must start from 1, not 0--%>
<c:set var="tabNum" value="${param.tabNum+1}" />

<c:if test='${inputType=="file"}'>
	<input type="text" disabled>
	<input type="button" value="<fmt:message key="click_to_upload" bundle="${resword}"/>">
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
	<input tabindex="<c:out value="${tabNum}"/>" style="background:white;color:#4D4D4D;" onChange=
    "this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="text" name="input<c:out value="${itemId}" />" value="<c:out value="${inputTxtValue}"/>" />
	<c:if test="${displayItem.item.itemDataTypeId==9 || displayItem.item.itemDataTypeId==10}"><!-- date type-->
	 <A HREF="#" onmouseover="Calendar.setup({inputField  : getSib(this.previousSibling), ifFormat    : '<fmt:message key="date_format_calender" bundle="${resformat}"/>', button      : 'anchor<c:out value="${itemId}"/>' });"
        NAME="anchor<c:out value="${itemId}"/>" ID="anchor<c:out value="${itemId}"/>">
         <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" /></a>
	 <%-- TODO l10n for the above line? --%>
	 <c:set var="numOfDate" value="${numOfDate+1}" />
	</c:if>
</c:if>
<c:if test='${inputType == "textarea"}'>
	<textarea tabindex="<c:out value="${tabNum}"/>" disabled style="background:white;color:#4D4D4D;" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="input<c:out value="${itemId}" />" rows="5" cols="40"><c:out value="${inputTxtValue}"/></textarea>
</c:if>
<c:if test='${inputType == "checkbox"}'>
	<c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
		<c:choose>
			<c:when test="${option.selected}"><c:set var="checked" value="checked" /></c:when>
			 <c:when test="${option.text eq inputTxtValue}"><c:set var="checked" value="checked" />
      </c:when>
      <c:otherwise><c:set var="checked" value="" /></c:otherwise>
		</c:choose>
    <%-- handle multiple values --%>
    <c:forTokens items="${inputTxtValue}" delims=","  var="_item">
      <c:if test="${(option.text eq _item) || (option.value eq _item)}"><c:set var="checked" value="checked" />
      </c:if>
    </c:forTokens>
    <input tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="checkbox" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/>  /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
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
		<input tabindex="<c:out value="${tabNum}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" type="radio" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/>  /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
	</c:forEach>
</c:if>
<c:if test='${inputType == "single-select"}'>
    <c:choose>
      <c:when test="${displayItem.metadata.defaultValue != '' &&
              displayItem.metadata.defaultValue != null}">
        <c:set var="printDefault" value="true"/>
      </c:when>
      <c:otherwise><c:set var="printDefault" value="false"/></c:otherwise>
    </c:choose>

    <select tabindex="<c:out value="${tabNum}"/>" style="background:white;color:#4D4D4D;" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');" name="input<c:out value="${itemId}"/>" class="formfield">
        <c:choose>
        <c:when test="${printDefault == 'true'}">
        <option value="<c:out value="" />" <c:out value=""/> ><c:out value="${displayItem.metadata.defaultValue}" /></option>
        <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
			<c:choose>
				<c:when test="${option.selected}"><c:set var="checked" value="selected" /></c:when>
				 <c:when test="${option.text eq inputTxtValue}"><c:set var="checked" value="selected" />
      </c:when>
        <c:otherwise><c:set var="checked" value="" /></c:otherwise>
			</c:choose>
			<option value="<c:out value="${option.value}" />" <c:out value="${checked}"/> >
                <c:out value="${option.text}" /></option>
		</c:forEach>
         </c:when>
         <c:otherwise>
             <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
                 <c:choose>
                     <c:when test="${option.selected}"><c:set var="checked" value="selected" /></c:when>
                      <c:when test="${option.text eq inputTxtValue}"><c:set var="checked" value="selected" />
           </c:when>
             <c:otherwise><c:set var="checked" value="" /></c:otherwise>
                 </c:choose>
                 <option value="<c:out value="${option.value}" />" <c:out value="${checked}"/> >
                     <c:out value="${option.text}" /></option>
             </c:forEach>
         </c:otherwise>
       </c:choose>
    </select>
</c:if>
<c:if test='${inputType == "multi-select"}'>
	<select multiple  tabindex="<c:out value="${tabNum}"/>" style="background:white;color:#4D4D4D;" name="input<c:out value="${itemId}"/>" onChange="this.className='changedField'; javascript:setImage('DataStatus_top','images/icon_UnsavedData.gif'); javascript:setImage('DataStatus_bottom','images/icon_UnsavedData.gif');">
		<c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
			<c:choose>
				<c:when test="${option.selected}"><c:set var="checked" value="selected" /></c:when>
				 <c:when test="${option.text eq inputTxtValue}"><c:set var="checked" value="selected" />
      </c:when>
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
	<input type="hidden" name="input<c:out value="${itemId}"/>" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" /><input type="text" disabled="disabled" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
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
	<td valign="top">
   <%-- <a tabindex="<c:out value="${tabNum + 1000}"/>" href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?id=<c:out value="${displayItem.data.id}"/>&name=itemData&field=input<c:out value="${itemId}" />&column=value','spanAlert-input<c:out value="${itemId}"/>'); return false;"
		>--%><img name="flag_input<c:out value="${itemId}" />" src="images/<c:out value="${imageFileName}"/>.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"
		><!--</a>--></td>
</c:if>
<c:if test="${displayItem.metadata.required}">
    <td valign="top"><span class="alert">*</span></td>
</c:if>
