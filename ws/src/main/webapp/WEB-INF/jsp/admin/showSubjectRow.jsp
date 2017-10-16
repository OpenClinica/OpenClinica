<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.DisplaySubjectRow" />
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<tr valign="top">        
      <td class="table_cell_left"><c:out value="${currRow.bean.subject.name}"/>&nbsp;</td>
      <td class="table_cell"><c:out value="${currRow.bean.studySubjectIds}"/>&nbsp;</td>
      <td class="table_cell"><c:out value="${currRow.bean.subject.gender}"/>&nbsp;</td>
      <td class="table_cell"><fmt:formatDate value="${currRow.bean.subject.createdDate}" pattern="${dteFormat}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.subject.owner.name}"/></td>
      <td class="table_cell"><fmt:formatDate value="${currRow.bean.subject.updatedDate}" pattern="${dteFormat}"/>&nbsp;</td>
      <td class="table_cell"><c:out value="${currRow.bean.subject.updater.name}"/>&nbsp;</td>
      <td class="table_cell"><c:out value="${currRow.bean.subject.status.name}"/></td>
      <td class="table_cell">
       <table border="0" cellpadding="0" cellspacing="0">
      <tr>
      <td>
      <a href="ViewSubject?id=<c:out value="${currRow.bean.subject.id}"/>"
	  onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
	  onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img 
	  name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
	  </td>
          <c:if test="${study.status.available}">
          <c:choose>
            <c:when test="${!currRow.bean.subject.status.deleted}">
                <td><a href="UpdateSubject?action=show&id=<c:out value="${currRow.bean.subject.id}"/>"
			        onMouseDown="javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');"
			        onMouseUp="javascript:setImage('bt_Edit1','images/bt_Edit.gif');"><img
			        name="bt_Edit1" src="images/bt_Edit.gif" border="0" alt="<fmt:message key="edit" bundle="${resword}"/>" title="<fmt:message key="edit" bundle="${resword}"/>" align="left" hspace="6"></a>
		        </td>
                <td><a href="RemoveSubject?action=confirm&id=<c:out value="${currRow.bean.subject.id}"/>"
			        onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
			        onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><img
			        name="bt_Remove1" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
		        </td>
           </c:when>
          <c:otherwise>
             <td>
                <a href="RestoreSubject?action=confirm&id=<c:out value="${currRow.bean.subject.id}"/>"
		            onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
		            onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"><img
		            name="bt_Restore3" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
		      </td>
            </c:otherwise>
          </c:choose>
          </c:if>    

      </tr>
      </table>
   </td>    
  </tr>  
  
  
  
