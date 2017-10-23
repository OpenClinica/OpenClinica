<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="respage"/>

<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
 <c:import url="../include/admin-header.jsp"/>
</c:when>
<c:otherwise>
 <c:import url="../include/managestudy-header.jsp"/>
</c:otherwise>
</c:choose>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<span class="icon icon-caret-down gray"></span>
		</a>
		<fmt:message key="instructions" bundle="${restext}"/>
		<div class="sidebar_tab_content">
			<!--<fmt:message key="import_rule_side_bar_instructions" bundle="${restext}"/>-->
            <fmt:message key="rules_Import_info" bundle="${respage}"/>
		</div>
	</td>
</tr>

<tr id="sidebar_Instructions_closed" style="display: none">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<span class="icon icon-caret-right gray"></span>
		</a>
		<fmt:message key="instructions" bundle="${restext}"/>
	</td>
</tr>



<jsp:include page="../include/sideInfo.jsp"/>


<jsp:useBean scope='session' id='version' class='org.akaza.openclinica.bean.submit.CRFVersionBean'/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='crfName' class='java.lang.String'/>

 <c:out value="${crfName}"/>

<c:choose>
	<c:when test="${userBean.sysAdmin && module=='admin'}">
		<h1><span class="title_manage">
	</c:when>
	<c:otherwise>
		<h1>
		<span class="title_manage">
	</c:otherwise>
</c:choose>

<fmt:message key="import_rule_data" bundle="${resworkflow}"/> ${study.name}
</h1>



<form action="ImportRule?action=confirm" method="post" ENCTYPE="multipart/form-data">
<div style="width: 600px">

<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">
	<tr>
		<fmt:message key="xml_file_to_upload" bundle="${resterm}"/>:&nbsp;<input type="file" name="xml_file" >
	</tr>
	<input type="hidden" name="crfId" value="<c:out value="${version.crfId}"/>">
</table>
</div>
</div></div></div></div></div></div></div></div>
</div>

<input type="submit" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_long">
</form>

<div>&nbsp;</div>
<div class="homebox_bullets"><a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/rules')"><b><fmt:message key="rule_import_rules_documentation" bundle="${resterm}"/></b></a></div></br>
<!--<div class="homebox_bullets"><a href="ImportRule?action=downloadtemplateWithNotes"><fmt:message key="rule_import_all_actions_with_notes" bundle="${resterm}"/></a></div><br/> -->
<div class="homebox_bullets"><a href="ImportRule?action=downloadtemplate"><b><fmt:message key="rule_import_all_actions_without_notes" bundle="${resterm}"/></b></a></div>

<!-- @pgawade 13-April-2011 - Fix for issue #8877: Removed the Rule Designer link from import Rule Data page
as the link is provided on Build Study page tasks -> Create Rules -> Actions  
<span class="table_title_Admin">Build Rules</span>
<div>&nbsp;</div>
<div class="homebox_bullets"><a href="${designerURL}access?host=${hostPath}&app=${contextPath}&study_oid=${study.oid}&provider_user=${userBean.name}">Designer</a></div><br/>
-->
<jsp:include page="../include/footer.jsp"/>