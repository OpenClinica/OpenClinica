<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>


<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="eventlist" class="java.util.HashMap"/>
<jsp:useBean scope="request" id="subjectAgeAtEvent" class="java.lang.String"/>

<h1><span class="title_manage"><fmt:message key="create_dataset" bundle="${resword}"/>: <fmt:message key="select_event_attributes" bundle="${resword}"/></span></h1>

<P><jsp:include page="../showInfo.jsp"/></P>

<jsp:include page="createDatasetBoxes.jsp" flush="true">
<jsp:param name="selectStudyEvents" value="1"/>
</jsp:include>

<P><jsp:include page="../showMessage.jsp"/></P>

<p><fmt:message key="please_select_one_CRF_from_the" bundle="${restext}"/> <fmt:message key="left_side_info_panel" bundle="${restext}"/>
<fmt:message key="select_items_in_CRF_include_dataset" bundle="${restext}"/>
</p>
<p><fmt:message key="click_event_subject_attributes_specify" bundle="${restext}"/></p>


<form action="CreateDataset" method="post" name="cl">
<input type="hidden" name="action" value="beginsubmit"/>
<input type="hidden" name="crfId" value="0">
<input type="hidden" name="eventAttr" value="1">

   <p>
    <c:choose>
     <c:when test="${newDataset.showEventLocation}">
       <input type="checkbox" checked name="location" value="yes">
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="location" value="yes">
     </c:otherwise>
    </c:choose>
    <fmt:message key="event_location" bundle="${resword}"/>
   </p>
   <p>
    <c:choose>
     <c:when test="${newDataset.showEventStart}">
       <input type="checkbox" checked name="start" value="yes">
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="start" value="yes">
     </c:otherwise>
    </c:choose>
   <fmt:message key="start_date" bundle="${resword}"/>
   </p>
   <p>
    <c:choose>
     <c:when test="${newDataset.showEventEnd}">
       <input type="checkbox" checked name="end" value="yes">
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="end" value="yes">
     </c:otherwise>
    </c:choose>
   <fmt:message key="end_date" bundle="${resword}"/>
   </p>
   <p>
    <c:choose>
     <c:when test="${newDataset.showEventStatus}">
       <input type="checkbox" checked name="event_status" value="yes">
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="event_status" value="yes">
     </c:otherwise>
    </c:choose>
   Event Status
   </p>
   <c:if test="${subjectAgeAtEvent == 1}">
   <p>
	
   <c:choose>
     <c:when test="${newDataset.showSubjectAgeAtEvent}">
       <input type="checkbox" checked name="age_at_event" value="yes">
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="age_at_event" value="yes">
     </c:otherwise>
   </c:choose>
   <fmt:message key="subject_age_at_event" bundle="${resword}"/>
   </p>
   </c:if>

<table border="0" cellpadding="0" cellspacing="0" >
  <tr>
   <td><input type="submit" name="save" value="<fmt:message key="save_and_add_more_items" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="submit" name="saveContinue" value="<fmt:message key="save_and_define_scope" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="button" onclick="confirmCancel('ViewDatasets');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/></td>
  </tr>
</table>
</form>

<jsp:include page="../include/footer.jsp"/>
