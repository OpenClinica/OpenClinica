<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>


<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.StudyGroupClassRow" />


<tr valign="top">   
      <td class="table_cell_left"><c:out value="${currRow.bean.name}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.groupClassTypeName}"/></td>  
      <td class="table_cell"><c:out value="${currRow.bean.subjectAssignment}"/></td>     
      <td class="table_cell"><c:out value="${currRow.bean.studyName}"/></td>
       <td class="table_cell">
        <c:forEach var="studyGroup" items="${currRow.bean.studyGroups}">
          <c:out value="${studyGroup.name}"/><br>
        </c:forEach>  
       </td>
      <td class="table_cell"><c:out value="${currRow.bean.status.name}"/></td>
      <td class="table_cell">    
       <table border="0" cellpadding="0" cellspacing="0">
         <tr>
          <td>
           <a href="ViewSubjectGroupClass?id=<c:out value="${currRow.bean.id}"/>"
	         onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
	         onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img 
	         name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
	     </td>
	     
	     <c:if test="${study.parentStudyId <= 0 && readOnly != 'true' }">
	     
	     <c:choose>	  
          <c:when test="${!currRow.bean.status.deleted}">
           <c:if test="${!study.status.locked}">   
           <td><a href="UpdateSubjectGroupClass?id=<c:out value="${currRow.bean.id}"/>"
			onMouseDown="javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');"
			onMouseUp="javascript:setImage('bt_Edit1','images/bt_Edit.gif');"><img 
			name="bt_Edit1" src="images/bt_Edit.gif" border="0" alt="<fmt:message key="edit" bundle="${resword}"/>" title="<fmt:message key="edit" bundle="${resword}"/>" align="left" hspace="6"></a>
		   </td>
           <td><a href="RemoveSubjectGroupClass?action=confirm&id=<c:out value="${currRow.bean.id}"/>"
			onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
			onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><img 
			name="bt_Remove1" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
		   </td>		     
           </c:if>
          </c:when>
          <c:otherwise>
           <td>
             <c:if test="${!study.status.locked}">
             <a href="RestoreSubjectGroupClass?action=confirm&id=<c:out value="${currRow.bean.id}"/>"
		      onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
		      onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"><img 
		      name="bt_Restore3" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
		     </td>
             </c:if>
          </c:otherwise>
         </c:choose>
         
         </c:if>
         
	    </tr>
	  </table>
      </td>
   </tr>
   
