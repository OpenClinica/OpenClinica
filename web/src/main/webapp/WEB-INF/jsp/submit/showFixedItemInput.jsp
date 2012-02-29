<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:useBean scope="request" id="section" class="org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<jsp:useBean scope="request" id="displayItem" class="org.akaza.openclinica.bean.submit.DisplayItemBean" />

<c:set var="inputType" value="${displayItem.metadata.responseSet.responseType.name}" />
<c:set var="itemId" value="${displayItem.item.id}" />
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

<c:if test='${inputType=="file"}'>
	<c:choose>
	<c:when test="${empty displayItem.data.value}">
		<input type="text" name="input<c:out value="${itemId}"/>" style="background:white;color:#4D4D4D;" value="">
	</c:when>
	<c:otherwise>
		<c:set var="prefilename" value="${inputTxtValue}"/>
		<a href="DownloadAttachedFile?eventCRFId=<c:out value="${section.eventCRF.id}"/>&fileName=<c:out value="${fn:replace(prefilename,'+','%2B')}"/>" id="a<c:out value="${itemId}"/>"><c:out value="${inputTxtValue}"/></a>
		<c:choose>
		<c:when test="${fn:contains(inputTxtValue, 'fileNotFound#')}">
			<del><c:out value="${fn:substringAfter(inputTxtValue,'fileNotFound#')}"/></del>
		</c:when>
		<c:otherwise>
			<c:out value="${inputTxtValue}"/>
		</c:otherwise>
		</c:choose>
	</c:otherwise>
	</c:choose>
</c:if>
<c:if test='${inputType == "instant-calculation"}'>
	<input type="text" name="input<c:out value="${itemId}" />" value="<c:out value="${inputTxtValue}"/>" style="background:white;color:#4D4D4D;"/>
</c:if>
<c:if test='${inputType == "text"}'>
	<input type="text" name="input<c:out value="${itemId}" />" value="<c:out value="${inputTxtValue}"/>" style="background:white;color:#4D4D4D;"/>
</c:if>
<c:if test='${inputType == "textarea"}'>
	<textarea name="input<c:out value="${itemId}" />" rows="5" cols="40" style="background:white;color:#4D4D4D;"><c:out value="${inputTxtValue}"/></textarea>
</c:if>
<c:if test='${inputType == "checkbox"}'>
	<c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
		<c:choose>
			<c:when test="${option.selected}"><c:set var="checked" value="checked" /></c:when>
			 <c:when test="${option.text eq inputTxtValue}"><c:set var="checked" value="checked" />
      </c:when>
      <c:otherwise><c:set var="checked" value="" /></c:otherwise>
		</c:choose>
		<input type="checkbox" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
	</c:forEach>
</c:if>
<c:if test='${inputType == "radio"}'>
	<c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
		<c:choose>
			<c:when test="${option.selected}"><c:set var="checked" value="checked" /></c:when>
			 <c:when test="${option.text eq inputTxtValue}"><c:set var="checked" value="checked" />
      </c:when>
      <c:otherwise><c:set var="checked" value="" /></c:otherwise>
		</c:choose>
		<input type="radio" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> /> <c:out value="${option.text}" /> <c:if test="${! isHorizontal}"><br/></c:if>
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

    <select name="input<c:out value="${itemId}"/>" class="formfield" style="background:white;color:#4D4D4D;">
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
			<option value="<c:out value="${option.value}" />"
                    <c:out value="${checked}"/> ><c:out value="${option.text}" /></option>
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
			<option value="<c:out value="${option.value}" />"
                    <c:out value="${checked}"/> ><c:out value="${option.text}" /></option>
		</c:forEach>

        </c:otherwise>

      </c:choose>

    </select>
</c:if>
<c:if test='${inputType == "multi-select"}'>
	<select multiple name="input<c:out value="${itemId}"/>" style="background:white;color:#4D4D4D;">
		<c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
			<c:choose>
				<c:when test="${option.selected}"><c:set var="checked" value="selected" /></c:when>
				 <c:when test="${option.text eq inputTxtValue}"><c:set var="checked" value="selected" />
      </c:when>
        <c:otherwise><c:set var="checked" value="" /></c:otherwise>
			</c:choose>
			<option value="<c:out value="${option.value}" />" <c:out value="${checked}"/> ><c:out value="${option.text}" /></option>
		</c:forEach>
	</select>
</c:if>
<c:if test='${inputType == "calculation" || inputType == "group-calculation"}'>
	<input type="hidden" name="input<c:out value="${itemId}"/>" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" /><input type="text" disabled="disabled" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
</c:if>