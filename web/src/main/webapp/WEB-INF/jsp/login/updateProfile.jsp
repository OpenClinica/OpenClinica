<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<%@ page import="org.akaza.openclinica.i18n.util.ResourceBundleProvider" %>
<c:import url="../include/home-header.jsp">
    <c:param name="profilePage" value="yes"/>
</c:import>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">

    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

    <fmt:message key="instructions" bundle="${resword}"/>

    <div class="sidebar_tab_content">

    </div>

    </td>

  </tr>
  <tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">

    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>

    <fmt:message key="instructions" bundle="${resword}"/>

    </td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope="request" id="studies" class="java.util.ArrayList"/>
<jsp:useBean scope="request" id="mustChangePass" class="java.lang.String"/>
<jsp:useBean scope="session" id="study" class="org.akaza.openclinica.bean.managestudy.StudyBean"/>
<jsp:useBean scope="session" id="userBean1" class="org.akaza.openclinica.bean.login.UserAccountBean"/>


<h1><span class="title_manage">
    <fmt:message key="change_user_profile" bundle="${resword}"/>
    <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/working-openclinica')"><span class="icon icon-question-circle gray"></span></a></span></h1><br/>
<strong>
<fmt:message key="browser_locale" bundle="${resword}"/>
 <c:set var="language"  value="<%=ResourceBundleProvider.getLocale().getDisplayLanguage()%>"></c:set>
<c:set var="country"  value="<%=ResourceBundleProvider.getLocale().getDisplayCountry()%>"></c:set>
                        <c:out value="${language}"/>
            <c:if test="${country!=''}">
            /
            <c:out value="${country}"/>
            </c:if>
    &nbsp;<fmt:message key="language" bundle="${resword}"/></strong>
<br><br>
<form action="UpdateProfile" method="post">
<fmt:message key="field_required" bundle="${resword}"/><br>
<input type="hidden" name="action" value="confirm">
<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0">
  <tr><td class="formlabel"><fmt:message key="first_name" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="text" name="firstName" value="<c:out value="${userBean1.firstName}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="firstName"/></jsp:include></td><td class="formlabel">*</td></tr>
  <tr><td class="formlabel"><fmt:message key="last_name" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="text" name="lastName" value="<c:out value="${userBean1.lastName}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="lastName"/></jsp:include></td><td class="formlabel">*</td></tr>
  <tr><td class="formlabel"><fmt:message key="email" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="text" name="email" value="<c:out value="${userBean1.email}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="email"/></jsp:include></td><td class="formlabel">*</td></tr>
  <tr valign="bottom"><td class="formlabel"><fmt:message key="institutional_affiliation" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="text" name="institutionalAffiliation" value="<c:out value="${userBean1.institutionalAffiliation}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="institutionalAffiliation"/></jsp:include></td><td class="formlabel">*</td></tr>
  <tr><td class="formlabel"><fmt:message key="default_active_study" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
   <c:set var="activeStudy1" value="${userBean1.activeStudyId}"/>
    <select name="activeStudyId" class="formfieldXL">
      <c:if test="${activeStudy1 ==0}"><option value="">-<fmt:message key="select" bundle="${resword}"/>-</option></c:if>
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
  </td></tr>
  <c:if test="${not userBean1.ldapUser}">
  <tr valign="bottom"><td class="formlabel"><fmt:message key="password_challenge_question" bundle="${resword}"/>:</td><td>
  <div class="formfieldXL_BG">
  <select name="passwdChallengeQuestion" class="formfieldXL">
  <c:set var="question1" value="Mother's Maiden Name"/>
  <c:choose>
       <c:when test="${userBean1.passwdChallengeQuestion == question1}">
            <option selected><fmt:message key="mother_maiden_name" bundle="${resword}"/></option>
            <option><fmt:message key="favourite_pet" bundle="${resword}"/></option>
            <option><fmt:message key="city_of_birth" bundle="${resword}"/></option>
            <option><fmt:message key="favorite_color" bundle="${resword}"/></option>
        </c:when>
        <c:when test="${userBean1.passwdChallengeQuestion == 'Favorite Animal'}">
            <option selected><fmt:message key="favourite_pet" bundle="${resword}"/></option>
            <option><fmt:message key="city_of_birth" bundle="${resword}"/></option>
            <option><fmt:message key="mother_maiden_name" bundle="${resword}"/></option>
            <option><fmt:message key="favorite_color" bundle="${resword}"/></option>
        </c:when>
        <c:when test="${userBean1.passwdChallengeQuestion == 'City of Birth'}">
               <option><fmt:message key="favourite_pet" bundle="${resword}"/></option>
               <option selected><fmt:message key="city_of_birth" bundle="${resword}"/></option>
               <option><fmt:message key="mother_maiden_name" bundle="${resword}"/></option>
               <option><fmt:message key="favorite_color" bundle="${resword}"/></option>
        </c:when>
        <c:otherwise>
               <option><fmt:message key="favourite_pet" bundle="${resword}"/></option>
               <option><fmt:message key="city_of_birth" bundle="${resword}"/></option>
               <option><fmt:message key="mother_maiden_name" bundle="${resword}"/></option>
               <option selected><fmt:message key="favorite_color" bundle="${resword}"/></option>
        </c:otherwise>
   </c:choose>
   </select>
   </div></td><td class="formlabel">*</td></tr>
  <tr><td class="formlabel"><fmt:message key="password_challenge_answer" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="passwdChallengeAnswer" value="<c:out value="${userBean1.passwdChallengeAnswer}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="passwdChallengeAnswer"/></jsp:include></td><td class="formlabel">*</td></tr>

  <tr><td class="formlabel"><fmt:message key="old_password" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="password" name="oldPasswd" value="" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="oldPasswd"/></jsp:include></td><td class="formlabel">*</td></tr>
  <tr><td class="formlabel"><fmt:message key="new_password" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="password" name="passwd" value="" class="formfieldXL"></div>
  <c:if test="${mustChangePass != 'yes'}"><fmt:message key="leave_in_blank" bundle="${resword}"/></c:if>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="passwd"/></jsp:include></td></tr>
  <tr><td class="formlabel"><fmt:message key="confirm_new_password" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="password" name="passwd1" value="" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="passwd1"/></jsp:include></td></tr>
  </c:if>
  <tr><td class="formlabel"><fmt:message key="phone" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="text" name="phone" value="<c:out value="${userBean1.phone}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="phone"/></jsp:include></td><td class="formlabel">*</td></tr>

</table>
</div>

</div></div></div></div></div></div></div></div>

</div>
<input type="submit" name="Submit" value="<fmt:message key="confirm_profile_changes" bundle="${resword}"/>" class="button_long">
<input type="button" onclick="confirmCancel('MainMenu');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
</form>
<jsp:include page="../include/footer.jsp"/>
