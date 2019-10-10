<%@ page contentType="text/html; charset=UTF-8" %>
<!-- NOT USED----------------------->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:include page="../login-include/header-login.jsp"/>
<!-- the object inside the array is StudySubjectBean-->
<jsp:useBean scope="request" id="subjects" class="java.util.ArrayList"/>

<p class="title">
<fmt:message key="list_all_subject_in" bundle="${resword}"/><c:out value="${study.name}"/>
</p>

<p><a href="AddNewSubject"><fmt:message key="enroll_a_new_subject" bundle="${resword}"/>:</a></p>

<jsp:include page="../include/showPageMessages.jsp" />
<table border="0" cellpadding="5">
<tr valign="top" bgcolor="#EBEBEB">
   <td class="text"><fmt:message key="ID" bundle="${resword}"/><fmt:message key="ID" bundle="${resword}"/>:</td>
   <td class="text"><fmt:message key="unique_identifier" bundle="${resword}"/></td>
   <td class="text"><fmt:message key="study_subject_label" bundle="${resword}"/></td>
   <td class="text"><fmt:message key="gender" bundle="${resword}"/></td>
   <td class="text"><fmt:message key="study_name" bundle="${resword}"/></td>
   <td class="text"><fmt:message key="date_created" bundle="${resword}"/></td>
   <td class="text"><fmt:message key="owner" bundle="${resword}"/></td>
   <td class="text"><fmt:message key="date_last_updated" bundle="${resword}"/></td> 
   <td class="text"><fmt:message key="last_updated_by" bundle="${resword}"/></td>
   <td class="text"><fmt:message key="status" bundle="${resword}"/></td>
   <td></td>     
  </tr>
 <c:choose>
 <c:when test="${!empty subjects}">
 <c:forEach var="studySubject" items="${subjects}">
   <tr valign="top">   
      <td class="text"><c:out value="${studySubject.id}"/></td>
      <td class="text"><c:out value="${studySubject.uniqueIdentifier}"/></td>
      <td class="text"><c:out value="${studySubject.label}"/></td> 
      <td class="text"><c:out value="${studySubject.gender}"/></td> 
      <td class="text"><c:out value="${studySubject.studyName}"/></td> 
      <td class="text"><fmt:formatDate value="${studySubject.createdDate}" pattern="${dteFormat}"/></td>
      <td class="text"><c:out value="${studySubject.owner.name}"/></td> 
      <td class="text"><c:out value="${studySubject.updatedDate}"/></td> 
      <td class="text"><c:out value="${studySubject.updater.name}"/></td> 
      <td class="text"><c:out value="${studySubject.status.name}"/></td>
      <td>
      <a href="ViewStudySubject?id=<c:out value="${studySubject.id}"/>&subjectId=<c:out value="${studySubject.subjectId}"/>&studyId=<c:out value="${studySubject.studyId}"/>"><fmt:message key="view" bundle="${resword}"/></a>
      <c:choose>
       <c:when test="${!studySubject.status.deleted}">
        /<a href="RemoveStudySubject?action=confirm&id=<c:out value="${studySubject.id}"/>"><fmt:message key="remove" bundle="${resword}"/></a>
        /<fmt:message key="copy" bundle="${resword}"/>
        /<fmt:message key="reassign" bundle="${resword}"/>
       </c:when>
       <c:otherwise>
        /<a href="RestoreStudySubject?action=confirm&id=<c:out value="${studySubject.id}"/>"><fmt:message key="restore" bundle="${resword}"/></a>
       </c:otherwise>
      </c:choose>
      </td>
   </tr>
   
 </c:forEach>
 </c:when>
 <c:otherwise>
  <i><fmt:message key="currently_no_subjects" bundle="${resword}"/></i>
 </c:otherwise>
 </c:choose> 

</table>

<jsp:include page="../login-include/footer.jsp"/>
