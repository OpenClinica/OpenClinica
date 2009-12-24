<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.StudyRow" />

<c:choose>
  <c:when test="${currRow.bean.status.name eq 'available'}">
    <c:set var="className" value="aka_green_highlight"/>
  </c:when>
  <c:when test="${currRow.bean.status.name eq 'removed'}">
    <c:set var="className" value="aka_red_highlight"/>
  </c:when>
</c:choose>
<tr valign="top">   
      <td class="table_cell_left"><c:out value="${currRow.bean.name}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.identifier}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.oid}"/></td>           
      <td class="table_cell"><c:out value="${currRow.bean.principalInvestigator}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.facilityName}"/>&nbsp;</td>         
      <td class="table_cell"><fmt:formatDate value="${currRow.bean.createdDate}" pattern="${dteFormat}"/></td>
      <td class="table_cell <c:out value='${className}'/>"><c:out value="${currRow.bean.status.name}"/></td>
      <td class="table_cell">
       <table border="0" cellpadding="0" cellspacing="0">
		<tr>
		 <td><a href="ViewSite?id=<c:out value="${currRow.bean.id}"/>"
			onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
			onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img 
			name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
		</td>
	  <c:if test="${ readOnly != 'true' }">	       
      <c:choose>
       <c:when test="${(!currRow.bean.status.deleted)}">
          <c:if test="${!study.status.locked}">
          <td><a href="InitUpdateSubStudy?id=<c:out value="${currRow.bean.id}"/>"
			onMouseDown="javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');"
			onMouseUp="javascript:setImage('bt_Edit1','images/bt_Edit.gif');"><img
			name="bt_Edit1" src="images/bt_Edit.gif" border="0" alt="<fmt:message key="edit" bundle="${resword}"/>" title="<fmt:message key="edit" bundle="${resword}"/>" align="left" hspace="6"></a>
		  </td>
           <td><a href="RemoveSite?action=confirm&id=<c:out value="${currRow.bean.id}"/>"
			onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
			onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><img 
			name="bt_Remove1" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
		 </td>
         </c:if>
       </c:when>
       <c:otherwise>
        <c:if test="${!study.status.locked}">
        <td><a href="RestoreSite?action=confirm&id=<c:out value="${currRow.bean.id}"/>"
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
   
