<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/> 
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<%--<jsp:useBean scope="session" id="panel" class="org.akaza.openclinica.view.StudyInfoPanel" />--%>


<!-- Sidebar Contents after alert-->

	
<c:choose>
 <c:when test="${userBean != null && userBean.id>0}">	
 <tr id="sidebar_Info_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Info_open'); leftnavExpand('sidebar_Info_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="info" bundle="${resword}"/></b>

		</td>
  </tr>
 <tr id="sidebar_Info_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Info_open'); leftnavExpand('sidebar_Info_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="info" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">

			<span style="color: #789EC5">

	

 <%-- begin standard study info --%>
 <%--	<c:if test="${panel.studyInfoShown}">--%> 
	<c:choose>
	<c:when test="${study.parentStudyId>0}">
	<b><fmt:message key="site" bundle="${resword}"/>:</b>&nbsp; 
	 <a href="ViewSite?id=<c:out value="${study.id}"/>">
	</c:when>
	<c:otherwise>
<b><fmt:message key="study" bundle="${resword}"/>:</b>&nbsp;  
	 <a href="ViewStudy?id=<c:out value="${study.id}"/>&viewFull=yes">
	</c:otherwise>
	</c:choose>
	<c:out value="${study.name}"/></a>

	<br><br>	
	
	<b><fmt:message key="subject" bundle="${resword}"/>:</b>&nbsp; 
	 <c:out value="${studySubject.label}"/>
	<br><br>

	<b><fmt:message key="study_event" bundle="${resword}"/></b>: &nbsp;
	<c:choose>
	 <c:when test="${toc != null}">	 
	  <a href="EnterDataForStudyEvent?eventId=<c:out value="${toc.studyEvent.id}"/>"><c:out value="${toc.studyEventDefinition.name}"/></a>
	 </c:when>
	 <c:otherwise>
	  <c:out value="${studyEvent.studyEventDefinition.name}"/>	   
	 </c:otherwise>
	</c:choose>
	<br><br>
	
     <b><fmt:message key="location" bundle="${resword}"/></b>: 
     <c:choose>
	 <c:when test="${toc != null}">	 
	   <c:out value="${toc.studyEvent.location}"/>
	 </c:when>
	 <c:otherwise>
	  <c:out value="${studyEvent.location}"/>	   
	 </c:otherwise>
	</c:choose>    
     <br><br>
     
     
     <b><fmt:message key="start_date1" bundle="${resword}"/></b>:
     <c:choose>
	 <c:when test="${toc != null}">	 
	    <fmt:formatDate value="${toc.studyEvent.dateStarted}" pattern="${dteFormat}"/>
   	 </c:when>
	 <c:otherwise>
	  <fmt:formatDate value="${studyEvent.dateStarted}" pattern="${dteFormat}"/>
	 </c:otherwise>
	 </c:choose>
      <br><br>
      
    
	<c:if test="${toc != null}">	 
     <b><fmt:message key="CRF" bundle="${resword}"/></b>: 
      <a href="ViewCRF?crfId=<c:out value="${toc.crf.id}"/>"> <c:out value="${toc.crf.name}"/> <c:out value="${toc.crfVersion.name}"/> </a>
      <br><br>
    </c:if>  
    
  <%-- </c:if>--%>
 <script language="JavaScript">
       <!--
         function leftnavExpand(strLeftNavRowElementName){

	       var objLeftNavRowElement;

           objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
           if (objLeftNavRowElement != null) {
             if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; } 
	           objLeftNavRowElement.display = (objLeftNavRowElement.display == "none" ) ? "" : "none";		
	         }
           }

       //-->
     </script>  
     
   	</div>

	</td>
	</tr>
	<tr id="sidebar_Info_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Info_open'); leftnavExpand('sidebar_Info_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="info" bundle="${resword}"/></b>

		</td>
	</tr>
   
  
  
  <c:if test="${panel.submitDataModule}">    
  <tr id="sidebar_StudyEvents_open">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_StudyEvents_open'); leftnavExpand('sidebar_StudyEvents_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>
		<b><fmt:message key="study_events" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">  
           <c:import url="../include/submitDataSide.jsp"/>	
     	</div>

		</td>
	</tr>
	
	<tr id="sidebar_StudyEvents_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_StudyEvents_open'); leftnavExpand('sidebar_StudyEvents_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="study_events" bundle="${resword}"/></b>

		</td>
	</tr>
  </c:if> 
  <c:if test="${panel.iconInfoShown}">
	 <c:import url="../include/sideIconsDataEntry.jsp"/>
	</c:if>
</table>	
 	
</c:when>
<c:otherwise>
    <br><br>
	<a href="MainMenu"><fmt:message key="login" bundle="${resword}"/></a>	
	<br><br>
	<a href="RequestAccount"><fmt:message key="request_an_account" bundle="${resword}"/></a>
	<br><br>
	<a href="RequestPassword"><fmt:message key="forgot_password" bundle="${resword}"/></a>
</c:otherwise>
</c:choose>


<!-- End Sidebar Contents -->

				<br><img src="images/spacer.gif" width="120" height="1">

				</td>
				<td class="content" valign="top">

