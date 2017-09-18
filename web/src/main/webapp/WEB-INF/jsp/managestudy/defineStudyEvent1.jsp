<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>	
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>


<jsp:include page="../include/managestudy-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		<div class="sidebar_tab_content">
       
        <fmt:message key="A_study_event_definition_describes_a_type_of_study_event"  bundle="${resword}"/> <fmt:message key="please_consult_the_ODM"  bundle="${resword}"/> 
		</div>

		</td>
	
	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='definition' class='org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean'/>
<script type="text/JavaScript" language="JavaScript">
  <!--
 function myCancel() {
 
    cancelButton=document.getElementById('cancel');
    if ( cancelButton != null) {
      if(confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>')) {
        window.location.href="ListEventDefinition";
       return true;
      } else {
        return false;
       }
     }
     return true;       
  }
   //-->
</script>
<h1 style="margin:0em"><span class="title_manage">
<fmt:message key="create_SED_for"  bundle="${resword}"/> <c:out value="${study.name}"/>
</span></h1>
	<ol>
    	<fmt:message key="list_create_SED_for"  bundle="${resword}"/>
	</ol>
	<br>

* <fmt:message key="indicates_required_field" bundle="${resword}"/><br>
<form action="DefineStudyEvent" method="post">
<input type="hidden" name="actionName" value="next">
<input type="hidden" name="pageNum" value="1">
<div style="width: 600px">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">
  <tr valign="top"><td class="formlabel"><fmt:message key="name" bundle="${resword}"/>:</td><td>  
  <div class="formfieldXL_BG"><input type="text" name="name" value="<c:out value="${definition.name}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="name"/></jsp:include>
  </td><td class="formlabel">*</td></tr>
  <tr valign="top"><td class="formlabel"><fmt:message key="description" bundle="${resword}"/>:</td><td>  
  <div class="formtextareaXL4_BG">
  <textarea class="formtextareaXL4" name="description" rows="4" cols="50"><c:out value="${definition.description}"/></textarea>
  </div>  
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="description"/></jsp:include>
  </td></tr>
 
 <tr valign="top"><td class="formlabel"><fmt:message key="repeating" bundle="${resword}"/>:</td><td>
  <c:choose>
   <c:when test="${definition.repeating == true}">
    <input type="radio" checked name="repeating" value="1"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="repeating" value="0"><fmt:message key="no" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <input type="radio" name="repeating" value="1"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="repeating" value="0"><fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td></tr>
  
  <tr valign="top"><td class="formlabel"><fmt:message key="type" bundle="${resword}"/>:</td><td>
    <div class="formfieldXL_BG"><select name="type" class="formfieldXL">        
       <c:choose>
        <c:when test="${definition.type == 'common'}">          
         <option value="scheduled"><fmt:message key="scheduled" bundle="${resword}"/>
         <option value="unscheduled"><fmt:message key="unscheduled" bundle="${resword}"/>
         <option value="common" selected><fmt:message key="common" bundle="${resword}"/>
        </c:when>        
        <c:when test="${definition.type == 'unscheduled'}">       
         <option value="scheduled"><fmt:message key="scheduled" bundle="${resword}"/>
         <option value="unscheduled" selected><fmt:message key="unscheduled" bundle="${resword}"/>
         <option value="common"><fmt:message key="common" bundle="${resword}"/>
        </c:when>
        <c:otherwise>        
         <option value="scheduled" selected><fmt:message key="scheduled" bundle="${resword}"/>
         <option value="unscheduled"><fmt:message key="unscheduled" bundle="${resword}"/>
         <option value="common"><fmt:message key="common" bundle="${resword}"/>
        </c:otherwise>
       </c:choose>       
    </select></div>
   </td></tr>
  
  <tr valign="top"><td class="formlabel"><fmt:message key="category" bundle="${resword}"/>:</td><td>  
   <div class="formfieldXL_BG"><input type="text" name="category" value="<c:out value="${definition.category}"/>" class="formfieldXL"></div>
   <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="category"/></jsp:include>
  </td></tr>
 

</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
<input type="submit" name="Submit" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_long">
</td>
<td>
<input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_long" onClick="javascript:myCancel();"/></td>
</tr></table>
</form>
<br><br>

<jsp:include page="../include/footer.jsp"/>
