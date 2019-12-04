<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:useBean scope="request" id="currRow" class="core.org.akaza.openclinica.web.bean.StudyRow" />

<c:choose>
  <c:when test="${currRow.studyDTO.status eq 'available'}">
    <c:set var="className" value="aka_green_highlight"/>
  </c:when>
  <c:when test="${currRow.studyDTO.status eq 'removed'}">
    <c:set var="className" value="aka_red_highlight"/>
  </c:when>
</c:choose>
<tr valign="top">   
      <td class="table_cell_left"><c:out value="${currRow.studyDTO.briefTitle}"/></td>
      <td class="table_cell"><c:out value="${currRow.studyDTO.uniqueIdentifier}"/></td>
      <td class="table_cell"><c:out value="${currRow.studyDTO.studyOid}"/></td>
      <td class="table_cell"><c:out value="${currRow.studyDTO.principalInvestigator}"/></td>
      <td class="table_cell"><c:out value="${currRow.studyDTO.facilityCity}, ${currRow.studyDTO.facilityState} ${currRow.studyDTO.facilityZipcode} ${currRow.studyDTO.facilityCountry}"/>&nbsp;</td>
      <td class="table_cell"><fmt:formatDate value="${currRow.studyDTO.createdDate}" pattern="${dteFormat}"/></td>
      <td class="table_cell <c:out value='${className}'/>"><c:out value="${currRow.studyDTO.status}"/></td>
      <td class="table_cell">
       <table border="0" cellpadding="0" cellspacing="0">
    <tr>
    <c:if test="${!study.status.locked}">
     <td><a href="InitUpdateSubStudy?id=<c:out value="${currRow.studyDTO.uniqueProtocolID}"/>"
      onMouseDown="javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');"
      onMouseUp="javascript:setImage('bt_Edit1','images/bt_Edit.gif');"><span
      name="bt_Edit1" class="icon icon-pencil" border="0" alt="<fmt:message key="update_event_definitions_for_site" bundle="${resword}"/>: ${currRow.studyDTO.briefTitle}" title="<fmt:message key="update_event_definitions_for_site" bundle="${resword}"/>: ${currRow.studyDTO.briefTitle}" align="left" hspace="6"></a>
     </td>
    </c:if>
   </tr>
  </table>
 </td>
   </tr>
   
