<%--
  This is the file that must be bundled within viewGroupSectionsPrint.jsp.
  It is only necessary if the
  CRF is associated with a study subject.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<c:if test="${studySubject != null && studySubject.id>0}">

  <table border="0" cellpadding="0" cellspacing="0" width="650" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
    <tr>
      <td class="table_cell_noborder" style="color: #789EC5"><b><fmt:message key="study_subject_ID" bundle="${resword}"/>:</b><br></td>
      <td class="table_cell_noborder" style="color: #789EC5"><c:out value="${studySubject.label}"/><br>
      </td>
      <c:choose>
        <c:when test="${study.studyParameterConfig.personIdShownOnCRF == 'true'}">
          <td class="table_cell_top" style="color: #789EC5"><b><fmt:message key="person_ID" bundle="${resword}"/>:</b><br></td>
          <td class="table_cell_noborder" style="color: #789EC5"><c:out value="${subject.uniqueIdentifier}"/><br></td>

        </c:when>
        <c:otherwise>
          <td class="table_cell_top" style="color: #789EC5"><b><fmt:message key="person_ID" bundle="${resword}"/>:</b><br></td>
          <td class="table_cell_noborder" style="color: #789EC5"><fmt:message key="N/A" bundle="${resword}"/></td>

        </c:otherwise>
      </c:choose>
    </tr>
    <tr>
      <td class="table_cell_noborder" style="color: #789EC5"><b><fmt:message key="study_site" bundle="${resword}"/>:</b><br></td>
      <td class="table_cell_noborder" style="color: #789EC5"><c:out value="${studyTitle}"/><br></td>
      <td class="table_cell_top" style="color: #789EC5"><b><fmt:message key="age" bundle="${resword}"/>:</b><br></td>
      <td class="table_cell_noborder" style="color: #789EC5"><c:choose><c:when test="${age!=''}"><c:out value="${age}"/></c:when>
        <c:otherwise> <fmt:message key="N/A" bundle="${resword}"/></c:otherwise></c:choose><br></td>
    </tr>
    <tr>
      <td class="table_cell_noborder" style="color: #789EC5"><b><fmt:message key="event" bundle="${resword}"/>:</b></td>
      <td class="table_cell_noborder" style="color: #789EC5"><c:out value="${studyEvent.studyEventDefinition.name}"/> (<fmt:formatDate value="${studyEvent.dateStarted}" pattern="${dteFormat}"/>)</td>
      <td class="table_cell_top" style="color: #789EC5"><b><fmt:message key="date_of_birth" bundle="${resword}"/>:</b><br></td>
      <td class="table_cell_noborder" style="color: #789EC5"><fmt:formatDate value="${subject.dateOfBirth}" pattern="${dteFormat}"/><br></td>
    </tr>
    <tr>
      <td class="table_cell_noborder" style="color: #789EC5"><b><fmt:message key="interviewer" bundle="${resword}"/>:</b></td>
      <td class="table_cell_noborder" style="color: #789EC5"><c:out value="${EventCRFBean.interviewerName}"/> (<fmt:formatDate value="${EventCRFBean.dateInterviewed}" pattern="${dteFormat}"/>)</td>
      <td class="table_cell_top" style="color: #789EC5"><b><fmt:message key="gender" bundle="${resword}"/>:</b></td>
      <td class="table_cell_noborder" style="color: #789EC5"><c:choose>
        <c:when test="${subject.gender==109}"><fmt:message key="M" bundle="${resword}"/></c:when>
        <c:when test="${subject.gender==102}"><fmt:message key="F" bundle="${resword}"/></c:when>
        <c:otherwise><c:out value="${subject.gender}"/></c:otherwise>
      </c:choose></td>
    </tr>
  </table>

</c:if>