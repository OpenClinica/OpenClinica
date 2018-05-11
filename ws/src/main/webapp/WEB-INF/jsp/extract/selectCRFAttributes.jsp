<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="eventlist" class="java.util.HashMap"/>

<h1><span class="title_manage"><fmt:message key="create_dataset" bundle="${resword}"/>: <fmt:message key="select_CRF_attributes" bundle="${resword}"/></span></h1>

<P><jsp:include page="../showInfo.jsp"/></P>

<jsp:include page="createDatasetBoxes.jsp" flush="true">
<jsp:param name="selectStudyEvents" value="1"/>
</jsp:include>

<P><jsp:include page="../showMessage.jsp"/></P>

<fmt:message key="instructions_extract_select_CRF_and_group" bundle="${resword}"/>

<form action="CreateDataset" method="post" name="cl">
<input type="hidden" name="action" value="beginsubmit"/>
<input type="hidden" name="crfId" value="0">
<input type="hidden" name="CRFAttr" value="1">

   <p>
    <c:choose>
     <c:when test="${newDataset.showCRFstatus}">
       <input type="checkbox" checked name="crf_status" value="yes">  
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="crf_status" value="yes">
     </c:otherwise>
    </c:choose>
    <fmt:message key="status" bundle="${resword}"/>
   </p>
   <p>   
    <c:choose>
     <c:when test="${newDataset.showCRFversion}">
       <input type="checkbox" checked name="crf_version" value="yes">  
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="crf_version" value="yes">
     </c:otherwise>
    </c:choose>
   <fmt:message key="version_name" bundle="${resword}"/>
   </p>
   <p>
    <c:choose>
     <c:when test="${newDataset.showCRFinterviewerName}">
       <input type="checkbox" checked name="interviewer" value="yes">  
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="interviewer" value="yes">
     </c:otherwise>
    </c:choose>   
   <fmt:message key="interviewer_name" bundle="${resword}"/>
   </p>
   <p>
    <c:choose>
     <c:when test="${newDataset.showCRFinterviewerDate}">
       <input type="checkbox" checked name="interviewer_date" value="yes">  
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="interviewer_date" value="yes">
     </c:otherwise>
    </c:choose>   
   <fmt:message key="interview_date" bundle="${resword}"/>
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