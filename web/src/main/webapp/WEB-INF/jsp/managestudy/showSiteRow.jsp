<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.StudyRow" />

<c:choose>
  <c:when test="${currRow.bean.status.name eq 'available'}">
    <c:set var="className" value="aka_green_highlight"/>
  </c:when>
  <c:when test="${currRow.bean.status.name eq 'removed'}">
    <c:set var="className" value="aka_red_highlight"/>
  </c:when>
</c:choose>
<tr valign="top">   
      <td class="table_cell_left"><c:out value="${currRow.bean.name}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.identifier}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.oid}"/></td>           
      <td class="table_cell"><c:out value="${currRow.bean.principalInvestigator}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.facilityCity}, ${currRow.bean.facilityState} ${currRow.bean.facilityZip} ${currRow.bean.facilityCountry}"/>&nbsp;</td>         
      <td class="table_cell"><fmt:formatDate value="${currRow.bean.createdDate}" pattern="${dteFormat}"/></td>
      <td class="table_cell <c:out value='${className}'/>"><c:out value="${currRow.bean.status.name}"/></td>
      <td class="table_cell">
       <table border="0" cellpadding="0" cellspacing="0">
    <tr>
    <c:if test="${!study.status.locked}">
     <td><a href="InitUpdateSubStudy?id=<c:out value="${currRow.bean.id}"/>"
      onMouseDown="javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');"
      onMouseUp="javascript:setImage('bt_Edit1','images/bt_Edit.gif');"><span
      name="bt_Edit1" class="icon icon-pencil" border="0" alt="<fmt:message key="update_event_definitions_for_site" bundle="${resword}"/>: ${currRow.bean.name}" title="<fmt:message key="update_event_definitions_for_site" bundle="${resword}"/>: ${currRow.bean.name}" align="left" hspace="6"></a>
     </td>
    </c:if>
   </tr>
  </table>
 </td>
   </tr>
   
