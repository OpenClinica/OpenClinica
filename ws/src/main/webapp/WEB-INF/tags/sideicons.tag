<%@tag body-content="scriptless" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="reswords"/>

 <b><fmt:message key="icon_key" bundle="${reswords}"/></b><br clear="all">
 <br/>
<div class="icons_image"><img src="images/icon_DEcomplete.gif" class="icon_space"
                               alt="<fmt:message key="completed" bundle='${reswords}'/>" align="bottom"> <fmt:message key="completed" bundle="${reswords}"/></div>

<div class="icons_image"><img src="images/icon_InitialDE.gif" class="icon_space"
                               alt="<fmt:message key="started" bundle='${reswords}'/>" align="bottom"> <fmt:message key="started" bundle="${reswords}"/></div>
<!--  see studyInfoPanel.java -->
<c:if test="${showDDEIcon}">
<div class="icons_image"><img src="images/icon_DDE.gif" class="icon_space"
                               alt="<fmt:message key="double_data_entry_start" bundle='${reswords}'/>" align="bottom"> <fmt:message key="double_data_entry_start" bundle="${reswords}"/></div>
 </c:if>

<div class="icons_image"><img src="images/icon_InitialDEcomplete.gif" class="icon_space"
                               alt="<fmt:message key="initial_data_entry_complete" bundle='${reswords}'/>" align="bottom"> <fmt:message key="initial_data_entry_complete" bundle="${reswords}"/></div>

<div class="icons_image"><img src="images/icon_NotStarted.gif" class="icon_space"
                               alt="<fmt:message key="not_started" bundle='${reswords}'/>" align="bottom"> <fmt:message key="not_started" bundle="${reswords}"/></div>

<div class="icons_image"><img src="images/icon_Scheduled.gif" class="icon_space"
                               alt="<fmt:message key="scheduled" bundle='${reswords}'/>" align="bottom"> <fmt:message key="scheduled" bundle="${reswords}"/></div>

<div class="icons_image"><img src="images/icon_Stopped.gif" class="icon_space"
                               alt="<fmt:message key="stopped" bundle='${reswords}'/>" align="bottom"> <fmt:message key="stopped" bundle="${reswords}"/></div>

<div class="icons_image"><img src="images/icon_Skipped.gif" class="icon_space"
                               alt="<fmt:message key="skipped" bundle='${reswords}'/>" align="bottom"> <fmt:message key="skipped" bundle="${reswords}"/></div>

<div class="icons_image"><img src="images/icon_Locked.gif" class="icon_space"
                               alt="<fmt:message key="locked" bundle='${reswords}'/>" align="bottom"> <fmt:message key="locked" bundle="${reswords}"/></div>

<div class="icons_image"><img src="images/icon_Invalid.gif" class="icon_space"
                               alt="<fmt:message key="invalid" bundle='${reswords}'/>" align="bottom"> <fmt:message key="invalid" bundle="${reswords}"/></div>

<div class="icons_image"><img src="images/bt_View.gif" class="icon_space"
                               alt="<fmt:message key="view" bundle='${reswords}'/>" align="bottom"> <fmt:message key="view" bundle="${reswords}"/></div>

<div class="icons_image"><img src="images/bt_Edit.gif" class="icon_space"
                               alt="<fmt:message key="edit" bundle='${reswords}'/>" align="bottom"> <fmt:message key="edit" bundle="${reswords}"/></div>

<c:if test="${userRole.manageStudy}">
<div class="icons_image"><img src="images/bt_Restore.gif" class="icon_space"
                               alt="<fmt:message key="restore" bundle='${reswords}'/>" align="bottom"> <fmt:message key="restore" bundle="${reswords}"/></div>

<div class="icons_image"><img src="images/bt_Remove.gif" class="icon_space"
                               alt="<fmt:message key="remove" bundle='${reswords}'/>" align="bottom"> <fmt:message key="remove" bundle="${reswords}"/></div>

<div class="icons_image"><img src="images/bt_Reassign.gif" class="icon_space"
                               alt="<fmt:message key="reassign" bundle='${reswords}'/>" align="bottom"> <fmt:message key="reassign" bundle="${reswords}"/></div>

<div class="icons_image"><img src="images/icon_Signed.gif" class="icon_space"
                               alt="<fmt:message key="sign" bundle='${reswords}'/>" align="bottom"> <fmt:message key="sign" bundle="${reswords}"/></div>
  </c:if>


         <div class="sidebar_tab_content">

             <a href="#" onClick="openDefWindow('help/allicons.html'); return false;"><fmt:message key="view_all_icons" bundle="${reswords}"/></a>

         </div>
