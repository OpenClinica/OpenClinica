<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:useBean scope="request" id="section" class="org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<jsp:useBean scope="request" id="displayItem" class="org.akaza.openclinica.bean.submit.DisplayItemBean" />

<c:set var="inputType" value="${displayItem.metadata.responseSet.responseType.name}" />
<c:set var="itemId" value="${displayItem.item.id}" />

<%-- BWP>> change select and radio inputs to checkboxes; 2/29/2008 >>--%>

<c:if test='${inputType=="file"}'>
	<c:out value="${displayItem.data.value}"/>
</c:if>
<c:if test='${inputType == "instant-calculation"}'>
	<c:out value="${displayItem.data.value}"/>
</c:if>
<c:if test='${inputType == "text"}'>
  <c:out value="${displayItem.metadata.responseSet.value}"/>
</c:if>
<c:if test='${inputType == "textarea"}'>
  <textarea name="input<c:out value="${itemId}" />" rows="5" cols="40" disabled style="background:white;color:#4D4D4D;"><c:out value="${displayItem.metadata.responseSet.value}"/></textarea>
</c:if>
<c:if test='${inputType == "checkbox"}'>
  <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
    <c:choose>
      <c:when test="${option.selected}"><c:set var="checked" value="checked" /></c:when>
      <c:otherwise><c:set var="checked" value="" /></c:otherwise>
    </c:choose>
    <input type="checkbox" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> disabled="disabled" /> <c:out value="${option.text}" /> <br/>
  </c:forEach>
</c:if>
<c:if test='${inputType == "radio"}'>
  <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
    <c:choose>
      <c:when test="${option.selected}"><c:set var="checked" value="checked" /></c:when>
      <c:otherwise><c:set var="checked" value="" /></c:otherwise>
    </c:choose>
    <input type="checkbox" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> disabled="disabled" /> <c:out value="${option.text}" /> <br/>
  </c:forEach>
</c:if>
<c:if test='${inputType == "single-select"}'>
  <c:choose>
    <c:when test="${displayItem.metadata.defaultValue != '' && displayItem.metadata.defaultValue != null}">
      <c:set var="printDefault" value="true"/>
    </c:when>
    <c:otherwise><c:set var="printDefault" value="false"/></c:otherwise>
  </c:choose>
  <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
    <c:if test="${printDefault=='true'}">
      <c:if test="${displayItem.metadata.defaultValue == option.text || displayItem.metadata.defaultValue == option.value}">
        <c:set var="printDefault" value="false"/>
      </c:if>
    </c:if>
  </c:forEach>
  <c:choose>
    <c:when test="${printDefault == 'true'}">
      <input type="checkbox" name="input<c:out value=""/>" value="<c:out value="" />" <c:out value=""/> disabled="disabled" /> <c:out value="${displayItem.metadata.defaultValue}" /> <br/>
      <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
        <c:choose>
          <c:when test="${option.selected}"><c:set var="checked" value="checked" /></c:when>
          <c:otherwise><c:set var="checked" value="" /></c:otherwise>
        </c:choose>
        <input type="checkbox" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> disabled="disabled" /> <c:out value="${option.text}" /> <br/>
      </c:forEach>
    </c:when>
    <c:otherwise>
      <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
        <c:choose>
          <c:when test="${option.selected}"><c:set var="checked" value="checked" /></c:when>
          <c:otherwise><c:set var="checked" value="" /></c:otherwise>
        </c:choose>
        <input type="checkbox" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> disabled="disabled" /> <c:out value="${option.text}" /> <br/>
      </c:forEach>
    </c:otherwise>
  </c:choose>
</c:if>
<%-- BWP>> 1835: change to checkboxes so that the element prints --%>
<c:if test='${inputType == "multi-select"}'>
  <%--<select multiple name="input<c:out value="${itemId}"/>" disabled style="background:white;color:#4D4D4D;">
    <c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
      <c:choose>
        <c:when test="${option.selected}"><c:set var="checked" value="selected" /></c:when>
        <c:otherwise><c:set var="checked" value="" /></c:otherwise>
      </c:choose>
      <option value="<c:out value="${option.value}" />" <c:out value="${checked}"/> ><c:out value="${option.text}" /></option>
    </c:forEach>
  </select>--%>
<c:forEach var="option" items="${displayItem.metadata.responseSet.options}">
    <c:choose>
      <c:when test="${option.selected}"><c:set var="checked" value="checked" /></c:when>
      <c:otherwise><c:set var="checked" value="" /></c:otherwise>
    </c:choose>
    <input type="checkbox" name="input<c:out value="${itemId}"/>" value="<c:out value="${option.value}" />" <c:out value="${checked}"/> disabled="disabled" /> <c:out value="${option.text}" /> <br/>
  </c:forEach>
</c:if>
<c:if test='${inputType == "calculation" || inputType == "group-calculation"}'>
	<input type="hidden" name="input<c:out value="${itemId}"/>" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" /><input type="text" disabled="disabled" value="<c:out value="${displayItem.metadata.responseSet.value}"/>" />
 </c:if>
