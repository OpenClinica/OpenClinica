<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>


<jsp:include page="../include/extract-header.jsp"/>


<%--<jsp:include page="../include/sidebar.jsp"/>--%>
<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">

    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></a>

    <fmt:message key="instructions" bundle="${resword}"/>

    <div class="sidebar_tab_content">

    </div>

    </td>

  </tr>
  <tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">

    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></a>

    <fmt:message key="instructions" bundle="${resword}"/>

    </td>
  </tr>

<jsp:include page="../include/createDatasetSideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="eventlist" class="java.util.HashMap"/>
<jsp:useBean scope="request" id="subjectAgeAtEvent" class="java.lang.String"/>

<h1><span class="title_manage"><fmt:message key="create_dataset" bundle="${resword}"/>: <fmt:message key="select_subject_attributes" bundle="${resword}"/></span></h1>

<p><fmt:message key="please_select_one_CRF_from_the" bundle="${restext}"/> <fmt:message key="left_side_info_panel" bundle="${restext}"/><fmt:message key="select_items_in_CRF_include_dataset" bundle="${restext}"/>
</p>
<p><fmt:message key="click_event_subject_attributes_specify" bundle="${restext}"/></p>


<form action="CreateDataset" method="post" name="cl">
<input type="hidden" name="action" value="beginsubmit"/>
<input type="hidden" name="crfId" value="0">
<input type="hidden" name="subAttr" value="1">


   <p>
    <c:choose>
      <c:when test="${newDataset.showSubjectDob}">
        <input type="checkbox" checked name="dob" value="yes">
      </c:when>
      <c:otherwise>
        <input type="checkbox" name="dob" value="yes">
      </c:otherwise>
    </c:choose>

   <c:choose>
    <c:when test="${study.studyParameterConfig.collectDob != '2'}">
     <fmt:message key="date_of_birth" bundle="${resword}"/>
    </c:when>
    <c:otherwise>
     <fmt:message key="year_of_birth" bundle="${resword}"/>
    </c:otherwise>
   </c:choose>
   </p>
  <%--</c:if>--%>

   <p>
   <c:choose>
     <c:when test="${newDataset.showSubjectGender}">
       <input type="checkbox" checked name="gender" value="yes">
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="gender" value="yes">
     </c:otherwise>
   </c:choose>   
   <fmt:message key="gender" bundle="${resword}"/>
   </p>
   <!-- below added 07/09/2007, tbh -->
   <p>
   <c:choose>
     <c:when test="${newDataset.showSubjectStatus}">
       <input type="checkbox" checked name="subj_status" value="yes">
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="subj_status" value="yes">
     </c:otherwise>
   </c:choose>   
   <fmt:message key="subject_status" bundle="${resword}"/>
   </p>
   <p>
   <c:choose>
     <c:when test="${newDataset.showSubjectUniqueIdentifier}">
       <input type="checkbox" checked name="unique_identifier" value="yes">
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="unique_identifier" value="yes">
     </c:otherwise>
   </c:choose>   
   <fmt:message key="subject_unique_ID" bundle="${resword}"/>
   </p>
   <p>
   <c:choose>
     <c:when test="${newDataset.showSubjectSecondaryId}">
       <input type="checkbox" checked name="subj_secondary_id" value="yes">
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="subj_secondary_id" value="yes">
     </c:otherwise>
   </c:choose>
   <fmt:message key="secondary_ID" bundle="${resword}"/>
   </p>
  
<table border="0" cellpadding="0" cellspacing="0" >
  <tr>
   <td><input type="submit" name="save" value="<fmt:message key="save_and_add_more_items" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="submit" name="saveContinue" value="<fmt:message key="save_and_define_scope" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="button" onclick="confirmCancel('ViewDatasets');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/></td>      
  </tr>
</table>
</form>

<jsp:include page="../include/footer.jsp"/>
