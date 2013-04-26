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
<jsp:useBean scope='request' id='studies' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='roles' class='java.util.HashMap'/>
<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />

<c:set var="userName" value="" />
<c:set var="firstName" value="" />
<c:set var="lastName" value="" />
<c:set var="email" value="" />
<c:set var="institutionalAffiliation" value="" />
<c:set var="activeStudyId" value="${0}" />
<c:set var="roleId" value="${0}" />
<c:set var="userTypeId" value="${2}" />
<c:set var="displayPwd" value="no" />

<c:forEach var="presetValue" items="${presetValues}">
    <c:if test='${presetValue.key == "userSource"}'>
        <c:set var="userSource" value="${presetValue.value}" />
    </c:if>
	<c:if test='${presetValue.key == "userName"}'>
		<c:set var="userName" value="${presetValue.value}" />
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
	<c:if test='${presetValue.key == "activeStudy"}'>
		<c:set var="activeStudyId" value="${presetValue.value}" />
	</c:if>
    <c:if test="${activeStudyId == 0}">
        <c:set var="activeStudyId" value="${activeStudy}"/>
    </c:if>
    <c:if test='${presetValue.key == "role"}'>
		<c:set var="roleId" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "type"}'>
		<c:set var="userTypeId" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "displayPwd"}'>
		<c:set var="displayPwd" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "runWebServices"}'>
        <c:set var="runWebServices" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "notifyPassword"}'>
        <c:set var="notifyPassword" value="${presetValue.value}" />
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
 function sendUrl() {
    document.getElementById('changeRoles').value = 'true';
    document.forms[1].submit();
 }
</script>

<h1><span class="title_manage"><fmt:message key="create_a_user_account" bundle="${resword}"/></span></h1>

<fmt:message key="field_required" bundle="${resword}"/>
<form action="CreateUserAccount" method="post">
<jsp:include page="../include/showSubmitted.jsp" />

<%
java.lang.String fieldName;
java.lang.String fieldValue;
int selectedValue;
%>
<div style="width: 450px">

<!-- These DIVs define shaded box borders -->

	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

		<div class="tablebox_center">


<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script> 

<script type="text/javascript">
jQuery.noConflict();

function handleUserSource() {
    var userTypeVal = jQuery("input[name='userSource']:checked").val();
    if (userTypeVal == "ldap") {
        jQuery(".passwordRow").hide();
        jQuery(".webservicesRow").hide();
        jQuery(".ldapSelect").show();
        jQuery("#userName").attr('readonly', 'true');
    } else {
        jQuery(".passwordRow").show();
        jQuery(".webservicesRow").show();
        jQuery(".ldapSelect").hide();
        jQuery("#userName").removeAttr('readonly');
    }
}
</script>
<c:if test="${ldapEnabled}">
<script type="text/javascript">

jQuery(document).ready(function() {
    jQuery("input[name='userSource']").click(function() {
        	handleUserSource();
    	jQuery("#userName").val('');
    });
    
    jQuery(".ldapSelect").click(function() {
        jQuery.blockUI({
            message: jQuery("#listLdapUsersForm"), css:{
                left: "300px",
                top: "10px"
            }
        });
    });
    
    jQuery("#closeLdapSelect").click(function() {
        jQuery.unblockUI();
    });
    handleUserSource();   
   
});
function handleUserSource() {
    var userTypeVal = jQuery("input:radio[name='userSource']:checked").val();
   
    if (userTypeVal == "ldap") {
        jQuery(".passwordRow").hide();
        jQuery(".webservicesRow").hide();
        jQuery(".ldapSelect").show();
        jQuery("#userName").attr('readonly', 'true');
    } else {
        jQuery(".passwordRow").show();
        jQuery(".webservicesRow").show();
        jQuery(".ldapSelect").hide();
        jQuery("#userName").removeAttr('readonly');
    }
}
</script>
</c:if>
		<!-- Table Contents -->
<input type="hidden" id="changeRoles" name="changeRoles">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<tr>
	<c:if test="${ldapEnabled}">
    <tr valign="top">
        <td class="formlabel"><fmt:message key="createUserAccount.userSource.label" bundle="${resword}"/></td>
        <td valign="top">
            <input type="radio" name="userSource" value="ldap"<c:if test="${empty userSource or userSource eq 'ldap' or userSource eq '' }"> checked="checked"</c:if>> 
            <fmt:message key="createUserAccount.userSource.ldap.label" bundle="${resword}"/> - 
            <input type="radio" name="userSource" value="local"<c:if test="${userSource eq 'local' }"> checked="checked"</c:if>> 
            <fmt:message key="createUserAccount.userSource.local.label" bundle="${resword}"/> 
        </td>
    </tr>
    </c:if>
    <c:if test="${not ldapEnabled}">
        <input type="hidden" name="userSource" value="local"/>
    </c:if>
    
   
	<tr valign="top">
		<td class="formlabel"><fmt:message key="username2" bundle="${resword}"/>:</td>
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top"><div class="formfieldM_BG">
						<input type="text" id="userName" name="userName" value="<c:out value="${userName}"/>" size="20" class="formfieldM" />
					</div></td>
					<td><c:if test="${ldapEnabled}"><a class="ldapSelect" href="#"><img alt="<fmt:message key="createUserAccount.user.lookupLdap.tooltip" bundle="${resword}"/>" 
					   title="<fmt:message key="createUserAccount.user.lookupLdap.tooltip" bundle="${resword}"/>" src="images/create_new.gif" border="0"></a>
					   </c:if>
					   *</td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="userName" /></jsp:include></td>
				</tr>
			</table>
		</td>
	</tr>


	<tr valign="top">
		<td class="formlabel"><fmt:message key="first_name" bundle="${resword}"/>:</td>
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top"><div class="formfieldM_BG">
						<input type="text" id="firstName" name="firstName" value="<c:out value="${firstName}"/>" size="20" class="formfieldM" />
					</div></td>
					<td>*</td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="firstName" /></jsp:include></td>
				</tr>
			</table>
		</td>
	</tr>

	<tr valign="top">
		<td class="formlabel"><fmt:message key="last_name" bundle="${resword}"/>:</td>
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top"><div class="formfieldM_BG">
					<input type="text" id="lastName" name="lastName" value="<c:out value="${lastName}"/>" size="20" class="formfieldM" />
					</div></td>
					<td>*</td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="lastName" /></jsp:include></td>
				</tr>
			</table>
		</td>
	</tr>


	<tr valign="top">
		<td class="formlabel"><fmt:message key="email" bundle="${resword}"/>:</td>
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top"><div class="formfieldM_BG">
						<input type="text" id="email" name="email" value="<c:out value="${email}"/>" size="20" class="formfieldM" />
					</div></td>
					<td>(<fmt:message key="username@institution" bundle="${resword}"/>) *</td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="email" /></jsp:include></td>
				</tr>
			</table>
		</td>
	</tr>


	<tr valign="top">
		<td class="formlabel"><fmt:message key="institutional_affiliation" bundle="${resword}"/>:</td>
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top"><div class="formfieldM_BG">
						<input type="text" id="institutionalAffiliation" name="institutionalAffiliation" value="<c:out value="${institutionalAffiliation}"/>" size="20" class="formfieldM" />
					</div></td>
					<td>*</td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="institutionalAffiliation" /></jsp:include></td>
				</tr>
			</table>
		</td>
	</tr>

	<tr valign="top">
	  	<td class="formlabel"><fmt:message key="active_study" bundle="${resword}"/>:</td>
<!-- EDIT !! -->
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
                        <div class="formfieldXL_BG">
                        <select name="activeStudy" id="activeStudy" class="formfieldXL" onchange="sendUrl();">
							<option value="0">-<fmt:message key="select" bundle="${resword}"/>-</option>

                            <c:forEach var="study" items="${studies}">
								<c:choose>
									<c:when test="${activeStudy == study.id}">
										<c:choose>
										<c:when test="${study.parentStudyId>0}">
											<option value='<c:out value="${study.id}" />' selected>&nbsp;&nbsp;&nbsp;&nbsp;<c:out value="${study.name}" /></option>
										</c:when>
										<c:otherwise>
											<option value='<c:out value="${study.id}" />' selected><c:out value="${study.name}" /></option>
										</c:otherwise>
										</c:choose>
									</c:when>
									<c:otherwise>
										<c:choose>
										<c:when test="${study.parentStudyId>0}">
											<option value='<c:out value="${study.id}" />'>&nbsp;&nbsp;&nbsp;&nbsp;<c:out value="${study.name}" /></option>
										</c:when>
										<c:otherwise>
											<option value='<c:out value="${study.id}" />'><c:out value="${study.name}" /></option>
										</c:otherwise>
										</c:choose>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
					</div>
                    </td>
					<td>*</td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="activeStudy" /></jsp:include></td>
				</tr>
			</table>
		</td>
	</tr>

	<tr valign="top">
	  	<td class="formlabel"><fmt:message key="role" bundle="${resword}"/>:</td>
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top"><div class="formfieldM_BG">
						<select name="role" id="role" class="formfieldM">
							<option value="0">-<fmt:message key="select" bundle="${resword}"/>-</option>
							<c:forEach var="currRole" items="${roles}">
								<c:choose>
									<c:when test="${roleId == currRole.key}">
										<option value='<c:out value="${currRole.key}" />' selected><c:out value="${currRole.value}" /></option>
									</c:when>
									<c:otherwise>
										<option value='<c:out value="${currRole.key}" />'><c:out value="${currRole.value}" /></option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
					</div></td>
					<td>*</td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="role" /></jsp:include></td>
				</tr>
			</table>
		</td>
	</tr>
	<tr valign="top">
	  	<td class="formlabel"><fmt:message key="user_type" bundle="${resword}"/>:</td>
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top"><div class="formfieldM_BG">
						<select name="type" id="type" class="formfieldM">
						<c:forEach var="currType" items="${types}">
								<c:choose>
									<c:when test="${userTypeId == currType.id}">
										<option value='<c:out value="${currType.id}" />' selected><c:out value="${currType.name}" /></option>
									</c:when>
									<c:otherwise>
										<option value='<c:out value="${currType.id}" />'><c:out value="${currType.name}" /></option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
					</div></td>
					<td>&nbsp;</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr valign="top" class="webservicesRow">
        <td class="formlabel"><fmt:message key="can_run_web_services" bundle="${resword}"/>:</td>
        <td valign="top">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td valign="top">
                        <br><input type="checkbox" name="runWebServices" id="runWebServices" value="1"
                            <c:if test="${runWebServices != 0}">checked</c:if>
                        >
                    </td>
                    <td> </td>
                </tr>
                <tr>
                    <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="runWebServices" /></jsp:include></td>
                </tr>
            </table>
        </td>
    </tr>
    <tr valign="top" class="passwordRow">
	  <td class="formlabel"><fmt:message key="user_password_generated" bundle="${resword}"/>:</td>
	  	<td>
	  	<c:choose>
         <c:when test="${notifyPassword eq 'email'}">
            <input type="radio" id="displayPwd0" checked name="displayPwd" value="no"><fmt:message key="send_user_password_via_email" bundle="${resword}"/>
            <br><input type="radio" id="displayPwd1" name="displayPwd" value="yes"><fmt:message key="show_user_password_to_admin" bundle="${resword}"/>
         </c:when>
         <c:otherwise>
            <%--<input type="radio" id="displayPwd0" name="displayPwd" value="no"><fmt:message key="send_user_password_via_email" bundle="${resword}"/>--%>
            <br><input type="radio" checked id="displayPwd1" checked name="displayPwd" value="yes"><fmt:message key="show_user_password_to_admin" bundle="${resword}"/>
         </c:otherwise>
       </c:choose>
      </td>
	</tr>
</table>
	</div>

	</div></div></div></div></div></div></div></div>

	</div>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
<input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium" /><br/>
</td>
<td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onClick="javascript:myCancel();"/>
</td>
</tr>
</table>
</form>

<div id="listLdapUsersForm" style="display:none;">
<div style="background:#FFFFFF;">
<iframe src="pages/listLdapUsers" width="500" height="350" frameborder="0">
</iframe>
</div>
</div>
<jsp:include page="../include/footer.jsp"/>
