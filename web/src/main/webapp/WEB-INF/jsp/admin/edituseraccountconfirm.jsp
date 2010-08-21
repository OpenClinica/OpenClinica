<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<jsp:include page="../include/admin-header.jsp"/>


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

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='presetValues' class='java.util.HashMap' />

<c:set var="firstName" value="" />
<c:set var="lastName" value="" />
<c:set var="email" value="" />
<c:set var="institutionalAffiliation" value="" />
<c:set var="userTypeId" value="${0}" />
<c:set var="resetPassword" value="${0}" />
<c:set var="displayPwd" value="no" />

<c:forEach var="presetValue" items="${presetValues}">
    <c:if test='${presetValue.key == "stepNum"}'>
        <c:set var="stepNum" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "userId"}'>
        <c:set var="userId" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "firstName"}'>
        <c:set var="firstName" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "lastName"}'>
        <c:set var="lastName" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "email"}'>
        <c:set var="email" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "institutionalAffiliation"}'>
        <c:set var="institutionalAffiliation" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "userType"}'>
        <c:set var="userType" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "resetPassword"}'>
        <c:set var="resetPassword" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "displayPwd"}'>
        <c:set var="displayPwd" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "runWebServices"}'>
        <c:set var="runWebServices" value="${presetValue.value}" />
    </c:if>
</c:forEach>
<script type="text/JavaScript" language="JavaScript">
  <!--
 function myCancel() {

    cancelButton=document.getElementById('cancel');
    if ( cancelButton != null) {
      if(confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>')) {
        window.location.href="ListUserAccounts";
       return true;
      } else {
        return false;
       }
     }
     return true;

  }
   //-->
</script>
<h1><span class="title_manage"><fmt:message key="edit_a_user_account" bundle="${resword}"/> - <fmt:message key="confirmation_screen" bundle="${resword}"/></span></h1>

<form action="EditUserAccount" method="post">
<jsp:include page="../include/showSubmitted.jsp" />

<input type="hidden" name="userId" value='<c:out value="${userId}"/>'/>
<input type="hidden" name="stepNum" value='<c:out value="${stepNum}"/>'/>
<input type="hidden" name="firstName" value='<c:out value="${firstName}"/>'/>
<input type="hidden" name="lastName" value='<c:out value="${lastName}"/>'/>
<input type="hidden" name="email" value='<c:out value="${email}"/>'/>
<input type="hidden" name="institutionalAffiliation" value='<c:out value="${institutionalAffiliation}"/>'/>
<input type="hidden" name="userType" value='<c:out value="${userType}"/>'/>
<input type="hidden" name="resetPassword" value='<c:out value="${resetPassword}"/>'/>
<input type="hidden" name="displayPwd" value='<c:out value="${displayPwd}"/>'/>
<input type="hidden" name="runWebServices" value='<c:out value="${runWebServices}"/>'/>

<div style="width: 400px">

<!-- These DIVs define shaded box borders -->

    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

        <div class="tablebox_center">


        <!-- Table Contents -->

<table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
        <td class="table_header_column_top"><fmt:message key="user_name" bundle="${resword}"/>:</td>
        <td class="table_cell"><c:out value="${userName}" /></td>
    </tr>

    <tr valign="bottom">
        <td class="table_header_column"><fmt:message key="first_name" bundle="${resword}"/>:</td>
        <td class="table_cell"><c:out value="${firstName}" /></td>
    </tr>

  <tr valign="bottom">
    <td class="table_header_column"><fmt:message key="last_name" bundle="${resword}"/>:</td>
    <td class="table_cell"><c:out value="${lastName}" /></td>
  </tr>

  <tr valign="bottom">
    <td class="table_header_column"><fmt:message key="email" bundle="${resword}"/>:</td>
    <td class="table_cell"><c:out value="${email}" /></td>
  </tr>

  <tr valign="bottom">
    <td class="table_header_column"><fmt:message key="institutional_affiliation" bundle="${resword}"/>:</td>
    <td class="table_cell"><c:out value="${institutionalAffiliation}" /></td>
  </tr>

  <tr valign="bottom">
    <td class="table_header_column"><fmt:message key="user_type" bundle="${resword}"/>:</td>
    <td class="table_cell">
        <c:choose>
            <c:when test="${userType == 1}">
                <fmt:message key="business_administrator" bundle="${resword}"/>
            </c:when>
            <c:when test="${userType == 3}">
                <fmt:message key="technical_administrator" bundle="${resword}"/>
            </c:when>
            <c:otherwise>
                <fmt:message key="user" bundle="${resword}"/>
            </c:otherwise>
        </c:choose>
    </td>
  </tr>
  
  <tr>
        <td class="table_header_column"><fmt:message key="authorized_run_web_services" bundle="${resword}"/>?</td>
        <td class="table_cell">
            <c:choose>
                <c:when test="${runWebServices == 1}">
                    <fmt:message key="yes" bundle="${resword}"/>
                </c:when>
                <c:otherwise>
                    <fmt:message key="no" bundle="${resword}"/>
                </c:otherwise>
            </c:choose>
    </tr>


    <tr>
        <td class="table_header_column"><fmt:message key="reset_password?" bundle="${resword}"/></td>
        <td class="table_cell">
            <c:choose>
                <c:when test="${resetPassword == 1}">
                    <fmt:message key="yes" bundle="${resword}"/>,
                    <c:choose>
                      <c:when test="${displayPwd == 'no'}">
                       <fmt:message key="and_send_password_to_user_via_email" bundle="${resword}"/>
                      </c:when>
                      <c:otherwise>
                       <fmt:message key="and_show_password_to_system_admin" bundle="${resword}"/>
                      </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <fmt:message key="no" bundle="${resword}"/>
                </c:otherwise>
            </c:choose>
    </tr>

    </table>
    </div>

    </div></div></div></div></div></div></div></div>

    </div>
<br>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
<input type="submit" name="submit" value="<fmt:message key="back" bundle="${resword}"/>" class="button">
</td>
<td>
<input type="submit" name="submit" value="<fmt:message key="confirm" bundle="${resword}"/>" class="button">
</td>
<td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onClick="javascript:myCancel();"/>
</td>
</tr>
</table>
</form>

<c:import url="../include/workflow.jsp">
 <c:param name="module" value="admin"/>
</c:import>
<jsp:include page="../include/footer.jsp"/>