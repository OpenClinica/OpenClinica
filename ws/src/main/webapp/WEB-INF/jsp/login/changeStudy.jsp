<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="../include/home-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope="request" id="studies" class="java.util.ArrayList"/>
<jsp:useBean scope="session" id="study" class="org.akaza.openclinica.bean.managestudy.StudyBean"/>

<h1><span class="title_manage"><fmt:message key="change_your_current_study" bundle="${restext}"/></span></h1>

<c:choose>
 <c:when test="${study != null && study.id>0}">
  <p><fmt:message key="your_current_active_study_is" bundle="${restext}"/> <c:out value="${study.name}"/>,
   <c:choose>
    <c:when test="${!userRole.invalid}">
     <fmt:message key="with_a_role_of" bundle="${restext}"/> <c:out value="${userRole.role.description}"/>.
    </c:when>
    <c:otherwise>
     <fmt:message key="but_no_role" bundle="${restext}"/>
    </c:otherwise>
   </c:choose>
 </c:when>
<c:otherwise>
 <p> <fmt:message key="currently_no_have_any_study" bundle="${restext}"/></p>
</c:otherwise>
</c:choose>


<c:choose>
<c:when test="${!empty studies}">


<form action="ChangeStudy" method="post">
<input type="hidden" name="action" value="confirm">
<p><fmt:message key="please_choose_study_list" bundle="${restext}"/>:</P>
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
     <table border="0" cellpadding="0" cellspacing="0" width="100%">
       <c:forEach var="studyRole" items="${studies}">
        <c:choose>
         <c:when test="${study.id == studyRole.studyId}">

           <c:choose>
            <c:when test="${studyRole.parentStudyId > 0}">
               <c:if test="${!studyRole.invalid}">
                 <tr>
                   <td class="table_cell">&nbsp;&nbsp;<img src="images/bullet.gif">
                   <input type="radio" checked name="studyId" value="<c:out value="${studyRole.studyId}"/>">
                   <c:out value="${studyRole.studyName}"/> (<c:out value="${studyRole.role.description}"/>)</td>
                 </tr>
               </c:if>
            </c:when>
            <c:otherwise>
                <c:if test="${!studyRole.invalid}">
                 <tr>
                 <td class="table_cell"><input type="radio" checked name="studyId" value="<c:out value="${studyRole.studyId}"/>">
                 <b><c:out value="${studyRole.studyName}"/> (<c:out value="${studyRole.role.description}"/>)</b></td>
                 </tr>
               </c:if>
                <c:if test="${studyRole.invalid}">
                 <tr><td class="table_cell"><b>&nbsp;<c:out value="${studyRole.studyName}"/></b></td></tr>
               </c:if>
            </c:otherwise>
           </c:choose>


         </c:when>
         <c:otherwise>
          <c:choose>
            <c:when test="${studyRole.parentStudyId > 0}">
               <c:if test="${!studyRole.invalid}">
                 <tr>
                  <td class="table_cell">&nbsp;&nbsp;<img src="images/bullet.gif"><input type="radio" name="studyId" value="<c:out value="${studyRole.studyId}"/>">
                  <c:out value="${studyRole.studyName}"/> (<c:out value="${studyRole.role.description}"/>)</td>
                 </tr>
               </c:if>
            </c:when>
            <c:otherwise>
                <c:if test="${!studyRole.invalid}">
                 <tr>
                  <td class="table_cell"><input type="radio" name="studyId" value="<c:out value="${studyRole.studyId}"/>">
                  <b><c:out value="${studyRole.studyName}"/> (<c:out value="${studyRole.role.description}"/>)</b></td>
                 </tr>
               </c:if>
                <c:if test="${studyRole.invalid}">
                 <tr><td class="table_cell"><b>&nbsp;<c:out value="${studyRole.studyName}"/></b></td></tr>
               </c:if>
            </c:otherwise>
           </c:choose>
         </c:otherwise>
        </c:choose>

     </c:forEach>

    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="studyId"/></jsp:include>
   </table>
   <br>
	</div>
	</div></div></div></div></div></div></div></div>
  <br>
  <p>
      <input type="submit" name="Submit" value="<fmt:message key="change_study" bundle="${resword}"/>" class="button_long">
      <input type="button" onclick="confirmCancel('MainMenu');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>

  </p>


</form>
</c:when>
<c:otherwise>
  <p><i><fmt:message key="no_other_studies_and_roles_available" bundle="${restext}"/></i> <a href="MainMenu"><fmt:message key="go_back" bundle="${restext}"/></a></p>
</c:otherwise>
</c:choose>
<jsp:include page="../include/footer.jsp"/>
