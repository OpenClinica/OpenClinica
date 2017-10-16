<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/> 
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:useBean scope="session" id="newUserBean" class="org.akaza.openclinica.bean.login.UserAccountBean"/>
<jsp:useBean scope="session" id="roles" class="java.util.ArrayList"/>
<jsp:useBean scope="request" id="studies" class="java.util.ArrayList"/>
<jsp:useBean scope="request" id="otherStudy" class="java.lang.String"/>

<jsp:include page="../login-include/login-header.jsp"/>

<jsp:include page="../login-include/request-sidebar.jsp"/>
<!-- Main Content Area -->

<h1><fmt:message key="request_an_openclinica_user_account" bundle="${resword}"/></h1>
<P>
<fmt:message key="please_provide_the_information_below_to_request" bundle="${resword}"/>
<p>
<jsp:include page="../login-include/login-alertbox.jsp"/>


<form action="RequestAccount" method="post"> 
<fmt:message key="all_fields_are_required" bundle="${resword}"/><br>
<input type="hidden" name="action" value="confirm"> 
<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">

<table border="0" cellpadding="0">
  <tr><td class="formlabel"><fmt:message key="user_name" bundle="${resword}"/>:</td><td>

<div class="formfieldXL_BG"><input type="text" name="name" value="<c:out value="${newUserBean.name}"/>" class="formfieldXL"></div>
<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="name"/></jsp:include></td></tr>

  <tr><td class="formlabel"><fmt:message key="first_name" bundle="${resword}"/>:</td><td>

<div class="formfieldXL_BG"><input type="text" name="firstName" value="<c:out value="${newUserBean.firstName}"/>" class="formfieldXL"></div>

<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="firstName"/></jsp:include></td></tr>

  <tr><td class="formlabel"><fmt:message key="last_name" bundle="${resword}"/>:</td><td>
<div class="formfieldXL_BG"><input type="text" name="lastName" value="<c:out value="${newUserBean.lastName}"/>" class="formfieldXL"></div>

<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="lastName"/></jsp:include></td></tr>
  <tr><td class="formlabel"><fmt:message key="email" bundle="${resword}"/>:</td><td>

<div class="formfieldXL_BG"><input type="text" name="email" value="<c:out value="${newUserBean.email}"/>" class="formfieldXL"></div>
<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="email"/></jsp:include></td></tr>
  <tr><td class="formlabel"><fmt:message key="confirm_email" bundle="${resword}"/>:</td><td>
 <div class="formfieldXL_BG"><input type="text" name="email2" value="" class="formfieldXL"></div>
<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="email2"/></jsp:include></td></tr>

 <tr><td class="formlabel"><fmt:message key="institutional_affiliation" bundle="${resword}"/>:</td><td>
  <div class="formfieldXL_BG"><input type="text" name="institutionalAffiliation" value="<c:out value="${newUserBean.institutionalAffiliation}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="institutionalAffiliation"/></jsp:include></td></tr>
<%-- BWP 1/29/09 issue 3255 remove this select>> <tr><td class="formlabel"><fmt:message key="default_active_study" bundle="${resword}"/>:</td><td>
  <c:set var="activeStudy1" value="${newUserBean.activeStudyId}"/>   
   <div class="formfieldXL_BG">
   <select name="activeStudyId" class="formfieldXL">
      <c:forEach var="study" items="${studies}">    
       <c:choose>
        <c:when test="${activeStudy1 == study.id}">   
         <option value="<c:out value="${study.id}"/>" selected><c:out value="${study.name}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${study.id}"/>"><c:out value="${study.name}"/>      
        </c:otherwise>
       </c:choose> 
    </c:forEach>
   </select></div>
</td></tr> >>--%>
<tr>
<td class="formlabel">
  <fmt:message key="If_the_study_you_are_interested" bundle="${resword}"/>
</td>
 <td><div class="formtextareaXL4_BG">
<textarea name="otherStudy" rows="4" cols="50" class="formtextareaXL4"><c:out value="${otherStudy}"/>&nbsp;</textarea>
</div></td></tr>
  
  <tr><td class="formlabel"><fmt:message key="Role_of_Access_Requested_Pending_Approval" bundle="${resword}"/></td><td class="text">
   <c:set var="role1" value="${newUserBean.activeStudyRole}"/>  
   <div class="formfieldXL_BG"> 
   <select name="activeStudyRole" class="formfieldXL">
      <c:forEach var="userRole" items="${roles}">    
       <c:choose>
        <c:when test="${role1.id == userRole.id}">   
         <option value="<c:out value="${userRole.id}"/>" selected><c:out value="${userRole.description}"/>
        </c:when>
        <c:otherwise>         
         <option value="<c:out value="${userRole.id}"/>"><c:out value="${userRole.description}"/>      
        </c:otherwise>
       </c:choose> 
    </c:forEach>
   </select>
   </div>
  </td></tr> 
 

</table>
</div>

</div></div></div></div></div></div></div></div>

</div>
<table border="0" cellpadding="0">
 <tr><td>
 <input type="submit" name="submit" value="<fmt:message key="confirm_account_request" bundle="${resword}"/>" class="button_xlong">
 </td>
 <td><input type="button" name="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onclick="javascript:window.location.href='MainMenu'"></td>
 </tr> 
 </table>
 
</form>

<jsp:include page="../login-include/login-footer.jsp"/>
