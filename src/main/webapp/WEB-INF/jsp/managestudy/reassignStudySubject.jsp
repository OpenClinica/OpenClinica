<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:include page="../include/managestudy-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

		<fmt:message key="instructions" bundle="${restext}"/>

		<div class="sidebar_tab_content"> 
		
		</div>

		</td>
	
	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>

		<fmt:message key="instructions" bundle="${restext}"/>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<!-- org.akaza.openclinica.bean.core.AuditableEntityBean -->

<jsp:useBean scope="request" id="displayStudy" class="core.org.akaza.openclinica.bean.admin.DisplayStudyBean"/>
<jsp:useBean scope="session" id="study" class="core.org.akaza.openclinica.domain.datamap.Study"/>
<jsp:useBean scope="request" id="subject" class="core.org.akaza.openclinica.bean.submit.SubjectBean"/>
<jsp:useBean scope="request" id="studySub" class="core.org.akaza.openclinica.bean.managestudy.StudySubjectBean"/>
<h1><span class="title_manage">
<fmt:message key="reassign_study_subject" bundle="${resworkflow}"/> 
</span></h1>
<form action="ReassignStudySubject" method="post">
<input type="hidden" name="action" value="confirm">
<input type="hidden" name="id" value="<c:out value="${studySub.id}"/>">
 
 <div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">

 <tr>
   <td class="table_header_column"><fmt:message key="study_subject_ID" bundle="${resword}"/></td>
   <td class="table_cell"><c:out value="${studySub.label}"/></td>
 </tr>

  <c:choose>
    <c:when test='${study.study != null && study.study.studyId > 0}'>
      <tr valign="top">
        <td class="table_header_column"><fmt:message key="study_name" bundle="${resword}"/>:</td>
        <td class="table_cell"><c:out value="${study.study.name}"/></td>
      </tr>
    </c:when>
    <c:otherwise>
      <tr valign="top">
        <td class="table_header_column"><fmt:message key="study_name" bundle="${resword}"/>:</td>
        <td class="table_cell"><c:out value="${study.name}"/></td>
      </tr>
    </c:otherwise>
  </c:choose>
  <tr valign="top">
    <td class="table_header_column">
      <fmt:message key="created_by" bundle="${resword}"/>:
    </td>
    <td class="table_cell">
      <c:out value="${studySub.owner.name}"/>
    </td>
  </tr>
  <tr valign="top">
    <td class="table_header_column">
      <fmt:message key="date_created" bundle="${resword}"/>:
    </td>
    <td class="table_cell">
      <fmt:formatDate value="${studySub.createdDate}" pattern="${dteFormat}"/>
    </td>
  </tr>
  <tr valign="top">
    <td class="table_header_column">
      <fmt:message key="last_updated_by" bundle="${resword}"/>:
    </td>
    <td class="table_cell">
      <c:out value="${studySub.updater.name}"/>&nbsp;
    </td>
  </tr>
  <tr valign="top">
    <td class="table_header_column">
      <fmt:message key="date_updated" bundle="${resword}"/>:
    </td>
    <td class="table_cell">
      <fmt:formatDate value="${studySub.updatedDate}" pattern="${dteFormat}"/>&nbsp;
    </td>
  </tr>
 </table>
 </div>
</div></div></div></div></div></div></div></div>
</div>
<br>
<strong><fmt:message key="please_choose_a_study_in_the_following_list2" bundle="${restext}"/></strong>
<br><br>
    
   <table border="0" cellpadding="0" cellspacing="0"> 
   <tr>
    <td style="padding-left:25px;">
      <input type="radio" checked name="studyId" value="<c:out value="${displayStudy.parent.studyId}"/>" class="invisible">
      <c:out value="${displayStudy.parent.name}"/>
      <c:if test="${displayStudy.parent.studyId==studySub.studyId }">
        <b><i><fmt:message key="currently_in" bundle="${restext}"/></i></b>
      </c:if>
      <br><br>
    </td>
  </tr> 
  <c:forEach var="child" items="${displayStudy.children}">
    <tr>
      <td style="padding-left:100px;">
        <c:choose> 	 
          <c:when test="${child.studyId==studySub.studyId }">
            <div class="homebox_bullets">
              <input type="radio" checked name="studyId" value="<c:out value="${child.studyId}"/>">
              <c:out value="${child.name}"/>
              <b><i><fmt:message key="currently_in" bundle="${restext}"/></i></b>
            </div>
          </c:when> 	 
          <c:otherwise>          
            <c:if test="${child.status.available}"> 
              <div class="homebox_bullets">
                <input type="radio" name="studyId" value="<c:out value="${child.studyId}"/>">
                <c:out value="${child.name}"/>
              </div>
            </c:if>
            <c:if test="${child.status.locked}">
              <div class="homebox_bullets">
                <input type="radio" disabled="true" name="studyId" value="<c:out value="${child.studyId}"/>">
                <c:out value="${child.name}"/>
              </div>
            </c:if>
          </c:otherwise>
        </c:choose>       
      </td>
    </tr>
  </c:forEach>
</table>
  <p><input type="submit" name="Submit" value="<fmt:message key="reassign_subject" bundle="${resword}"/>" class="button_long">
      <input type="button" onclick="confirmCancel('ListStudySubjects');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
  </p>


</form>
 <br><br>


<jsp:include page="../include/footer.jsp"/>
