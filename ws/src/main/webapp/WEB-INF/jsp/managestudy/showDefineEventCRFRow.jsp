<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<c:set var="count" value="${param.eblRowCount}" />
<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.CRFRow" />   

   <input type="hidden" name="id<c:out value="${count}"/>" value="<c:out value="${currRow.bean.id}"/>">  
   <input type="hidden" name="name<c:out value="${count}"/>" value="<c:out value="${currRow.bean.name}"/>">  
   
   <tr valign="top">        
      <td class="table_cell_left"><c:out value="${currRow.bean.name}"/></td>
      <td class="table_cell"><fmt:formatDate value="${currRow.bean.createdDate}" pattern="${dteFormat}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.owner.name}"/></td>
      <td class="table_cell">
       <c:choose>
        <c:when test="${currRow.bean.updatedDate != null}">
          <fmt:formatDate value="${currRow.bean.updatedDate}" pattern="${dteFormat}"/>
        </c:when>
        <c:otherwise>
          &nbsp;
        </c:otherwise>          
       </c:choose> 
      </td>
      <td class="table_cell"><c:out value="${currRow.bean.updater.name}"/>&nbsp;</td>
      <td class="table_cell" align="center">
      <c:choose>
       <c:when test="${!currRow.bean.selected}">
         <input type="checkbox" <c:if test="${not empty tmpCRFIdMap[currRow.bean.id]}">checked="checked" </c:if> 
          name="selected<c:out value="${count}"/>" value="yes">
       </c:when>
       <c:otherwise>
        <img src="images/icon_DEcomplete.gif" alt="CRF Selected" title="CRF Selected">
       </c:otherwise>
      </c:choose>
      </td>          
    <c:set var="count" value="${count+1}"/>
   </tr>
