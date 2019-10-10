<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>



<c:set var="eblRowCount" value="${param.eblRowCount}" />
<!--row number: <c:out value="${eblRowCount}"/> -->

<jsp:useBean scope="request" id="currRow" class="core.org.akaza.openclinica.web.bean.DisplayStudySubjectRow" />
<jsp:useBean scope='session' id='userRole' class='core.org.akaza.openclinica.bean.login.StudyUserRoleBean' />
<c:set var="groups" value="5"/>
	<c:forEach var="group" items="${currRow.bean.studyGroups}">
	<c:set var="groups" value="${groups+1}"/> 
</c:forEach>  
  <tr valign="top">
      <!--<td class="table_cell"><c:out value="${currRow.bean.studySubject.uniqueIdentifier}"/></td>-->
      <c:choose>
      <c:when test ="${currRow.sortingColumn >= 1 && currRow.sortingColumn < groups}">
	      <td class="table_cell_left"><c:out value="${currRow.bean.studySubject.label}"/>&nbsp;</td> 
	      <td class="table_cell" style="display: all" id="Groups_0_1_<c:out value="${eblRowCount+1}"/>">
	      <c:choose>
	        <c:when test="${currRow.bean.studySubject.status.id==1 || currRow.bean.studySubject.status.id==8 }">
             <c:if test="${currRow.bean.studySubject.status.id==1}"><fmt:message key="active" bundle="${resword}"/></c:if>
             <c:if test="${currRow.bean.studySubject.status.id==8}"><fmt:message key="signed" bundle="${resword}"/></c:if>
	        </c:when>
	        <c:otherwise>
	        <fmt:message key="inactive" bundle="${resword}"/>
	        </c:otherwise>
	      </c:choose>
	      </td>
                <td class="table_cell" style="display: all" id="Groups_0_2_<c:out value="${eblRowCount+1}"/>">
                    <c:out value="${currRow.bean.studySubject.oid}"/>&nbsp;</td>
        <td class="table_cell" style="display: all" id="Groups_0_3_<c:out value="${eblRowCount+1}"/>">
            <c:out value="${currRow.bean.studySubject.gender}"/>&nbsp;</td>
        <td class="table_cell" style="display: all" id="Groups_0_4_<c:out value="${eblRowCount+1}"/>">
            <c:out value="${currRow.bean.studySubject.secondaryLabel}"/>&nbsp;</td>
	      <c:set var="groupCount" value="5"/>
	      <c:forEach var="group" items="${currRow.bean.studyGroups}">
	         <td class="table_cell" style="display: all" id="Groups_0_<c:out value="${groupCount}"/>_<c:out value="${eblRowCount+1}"/>"><c:out value="${group.studyGroupName}"/>&nbsp;</td>
	       <c:set var="groupCount" value="${groupCount+1}"/>
	      </c:forEach>   
      </c:when>
      <c:otherwise>
      <td class="table_cell_left"><c:out value="${currRow.bean.studySubject.label}"/>&nbsp;</td> 
	      <td class="table_cell" style="display: none" id="Groups_0_1_<c:out value="${eblRowCount+1}"/>">
	      <c:choose>
	         <c:when test="${currRow.bean.studySubject.status.id==1 || currRow.bean.studySubject.status.id==8 }">
             <c:if test="${currRow.bean.studySubject.status.id==1}"><fmt:message key="active" bundle="${resword}"/></c:if>
             <c:if test="${currRow.bean.studySubject.status.id==8}"><fmt:message key="signed" bundle="${resword}"/></c:if>
            </c:when>
	        <c:otherwise>
	        <fmt:message key="inactive" bundle="${resword}"/>
	        </c:otherwise>
	      </c:choose>
	      </td>
          <td class="table_cell" style="display: none" id="Groups_0_2_<c:out value="${eblRowCount+1}"/>"><c:out value="${currRow.bean.studySubject.oid}"/>&nbsp;</td>
          <td class="table_cell" style="display: none" id="Groups_0_3_<c:out value="${eblRowCount+1}"/>"><c:out value="${currRow.bean.studySubject.gender}"/>&nbsp;</td>
          <td class="table_cell" style="display: none" id="Groups_0_4_<c:out value="${eblRowCount+1}"/>"><c:out value="${currRow.bean.studySubject.secondaryLabel}"/>&nbsp;</td>

	      <c:set var="groupCount" value="5"/>
	      <c:forEach var="group" items="${currRow.bean.studyGroups}">
	         <td class="table_cell" style="display: none" id="Groups_0_<c:out value="${groupCount}"/>_<c:out value="${eblRowCount+1}"/>"><c:out value="${group.studyGroupName}"/>&nbsp;</td>
	       <c:set var="groupCount" value="${groupCount+1}"/>
	      </c:forEach>   
      </c:otherwise>   
     </c:choose>
     
      <!--
      <td class="table_cell"><fmt:formatDate value="${currRow.bean.studySubject.enrollmentDate}" dateStyle="short"/>&nbsp;</td>
      -->
     
      <c:set var="prevDefId" value="1"/>
      <c:set var="currDefId" value="1"/>
      <c:set var="repeatNum" value="0"/>
      <c:set var="count" value="${0}"/>
      
      <c:forEach var="event" items="${currRow.bean.studyEvents}">  
       <c:set var="currEvent" scope="request" value="${event}" />           
        <td class="table_cell">      
        <table border="0" cellpadding="0" cellspacing="0">
         <tr valign="top">
         <td>       
           <c:import url="../submit/eventLayer.jsp">
           <c:param name="colCount" value="${count}"/>
           <c:param name="rowCount" value="${eblRowCount}"/>
           <c:param name="eventId" value="${event.id}"/>
           <c:param name="eventStatus" value="${event.subjectEventStatus.name}"/>
           <c:param name="eventName" value="${event.studyEventDefinition.name}"/>
           <c:param name="eventDefId" value="${event.studyEventDefinition.id}"/>
           <c:param name="subjectId" value="${currRow.bean.studySubject.id}"/>
           <c:param name="subjectName" value="${currRow.bean.studySubject.label}"/>
           <c:param name="eventSysStatus" value="${currRow.bean.studySubject.status.name}"/>
            <c:param name="module" value="submit"/>
         </c:import>
        <c:choose>
        <c:when test="${event.subjectEventStatus.id==1}">  
         
	      <span class="icon icon-clock2"  border="0" style="position: relative; left: 7px;">	         
         </c:when>
        <c:when test="${event.subjectEventStatus.id==2}">
         
         <span class="icon icon-doc"  border="0" style="position: relative; left: 7px;">     
        
        </c:when>
        <c:when test="${event.subjectEventStatus.id==3}">
       
         <span class="icon icon-pencil-squared orange"  border="0" style="position: relative; left: 7px;">     
        
        </c:when>
        <c:when test="${event.subjectEventStatus.id==4}">
         
         <span class="icon icon-ok" border="0" style="position: relative; left: 7px;">   
        
        </c:when>  
        <c:when test="${event.subjectEventStatus.id==5}">  
            
         <span class="icon icon-stop-circle red" border="0" style="position: relative; left: 7px;"> 
       
        </c:when>   
        <c:when test="${event.subjectEventStatus.id==6}">
         
         <span class="icon icon-redo" border="0" style="position: relative; left: 7px;">   
        
        </c:when>
        <c:when test="${event.subjectEventStatus.id==7}">
        
         <span class="icon icon-icon-locked" border="0" style="position: relative; left: 7px;">   
        </c:when>

         <c:when test="${event.subjectEventStatus.id==8}">

          <span class="icon icon-icon-sign"  border="0" style="position: relative; left: 7px;">	         
        </c:when>

        </c:choose> 
         </a><img name="ExpandIcon_<c:out value="${currRow.bean.studySubject.label}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>" src="images/icon_blank.gif" width="15" height="15" style="position: relative; left: 8px;">      
        
        
        </td>
                
         <c:if test="${event.repeatingNum>1}">
         <td valign="top">
          &nbsp;(<c:out value="${event.repeatingNum}"/>)
         </td>
         </c:if>
         </tr>
         </table>&nbsp;
        </td>
     
       <c:set var="count" value="${count+1}"/>
       </c:forEach>
	    
      <td class="table_cell">
      <table border="0" cellpadding="0" cellspacing="0">
      <tr>
      <td>
      <a href="ViewStudySubject?id=<c:out value="${currRow.bean.studySubject.id}"/>"
	  onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
	  onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img 
	  name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
	  </td>
	  <c:if test="${!userRole.monitor}">
		  <c:choose>
		   <c:when test="${!currRow.bean.studySubject.status.deleted}">
             <c:if test="${study.status.available}">
             <td><a href="RemoveStudySubject?action=confirm&id=<c:out value="${currRow.bean.studySubject.id}"/>&subjectId=<c:out value="${currRow.bean.studySubject.subjectId}"/>&studyId=<c:out value="${currRow.bean.studySubject.studyId}"/>"
	                           onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
	                           onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><img
	                      name="bt_Remove1" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
	         </td>
               </c:if>
             <td>
	          <c:if test="${currRow.bean.isStudySignable && study.status.available}">
	                <a href="SignStudySubject?id=<c:out value="${currRow.bean.studySubject.id}"/>"
	                      onMouseDown="javascript:setImage('icon_signed','images/icon_Signed.gif');"
	                       onMouseUp="javascript:setImage('icon_signed','images/icon_Signed.gif');">
	                        <img src="images/icon_Signed.gif" border="0"
	                             alt="<fmt:message key="sign" bundle="${resword}"/>"
	                              title="<fmt:message key="sign" bundle="${resword}"/>"
	                                align="left" hspace="6">
	                  </a>
	            </c:if>
	         </td>	
	         </c:when>
	         <c:otherwise>
	           <td>
                   <c:if test="${study.status.available}">
                   <a href="RestoreStudySubject?action=confirm&id=<c:out value="${currRow.bean.studySubject.id}"/>&subjectId=<c:out value="${currRow.bean.studySubject.subjectId}"/>&studyId=<c:out value="${currRow.bean.studySubject.studyId}"/>"
	                     onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
	                     onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"><img
	                     name="bt_Restore3" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
                   </c:if>
               </td>
	         </c:otherwise>         
	      </c:choose>           
      </c:if>
      </tr>
      </table>&nbsp;
      </td>
   </tr>
   
