<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<c:set var="count" value="${param.eblRowCount}" />
<c:set var="eblRowCount" value="${param.eblRowCount}" />
<c:set var="isFirstLink" value="${param.isFirstLink}" />

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.StudyEventDefinitionRow" />

<c:set var="last" value="${defSize-1}" />
 <c:choose>
  <c:when test="${count==last}">
    <c:set var="nextRow" value="${allRows[count]}" />
  </c:when>
  <c:otherwise>
    <c:set var="nextRow" value="${allRows[count+1]}" />
  </c:otherwise>
 </c:choose>
<tr valign="top">     
      <td class="table_cell_left">
      <%--<c:out value="${currRow.bean.ordinal}"/>--%>
      <c:choose>
      <c:when test="${isFirstLink}">
      <c:choose>
        <c:when test="${count==0}">
            <c:choose>
            <c:when test="${defSize>1}">
            <a href="ChangeDefinitionOrdinal?current=<c:out value="${nextRow.bean.id}"/>&previous=<c:out value="${currRow.bean.id}"/>"><img src="images/bt_sort_descending.gif" border="0" alt="<fmt:message key="move_down" bundle="${resword}"/>" title="<fmt:message key="move_down" bundle="${resword}"/>" /></a>
           </c:when>
           <c:otherwise>
            &nbsp;
           </c:otherwise>
           </c:choose>
        </c:when>
        <c:when test="${count==last}">
           <a href="ChangeDefinitionOrdinal?current=<c:out value="${currRow.bean.id}"/>&previous=<c:out value="${prevRow.bean.id}"/>"><img src="images/bt_sort_ascending.gif" alt="<fmt:message key="move_up" bundle="${resword}"/>" title="<fmt:message key="move_up" bundle="${resword}"/>" border="0"/></a>         
        </c:when>
        <c:otherwise>
          <a href="ChangeDefinitionOrdinal?current=<c:out value="${currRow.bean.id}"/>&previous=<c:out value="${prevRow.bean.id}"/>"><img src="images/bt_sort_ascending.gif" alt="<fmt:message key="move_up" bundle="${resword}"/>" title="<fmt:message key="move_up" bundle="${resword}"/>" border="0" /></a>
          <a href="ChangeDefinitionOrdinal?previous=<c:out value="${currRow.bean.id}"/>&current=<c:out value="${nextRow.bean.id}"/>"><img src="images/bt_sort_descending.gif" alt="<fmt:message key="move_down" bundle="${resword}"/>" title="<fmt:message key="move_down" bundle="${resword}"/>" border="0" /></a>
        </c:otherwise>
      </c:choose>
        </c:when>
      </c:choose>

      </td>   
      <td class="table_cell"><c:out value="${currRow.bean.name}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.oid}"/></td>
      <td class="table_cell">
        <c:choose>
         <c:when test="${currRow.bean.repeating == true}"><fmt:message key="yes" bundle="${resword}"/></c:when>
         <c:otherwise><fmt:message key="no" bundle="${resword}"/></c:otherwise> 
        </c:choose>
      </td>
      <td class="table_cell"><fmt:message key="${currRow.bean.type}" bundle="${resword}"/>&nbsp;</td>
      <td class="table_cell"><c:out value="${currRow.bean.category}"/>&nbsp;</td>
      <td class="table_cell"> 
        <c:choose>
         <c:when test="${currRow.bean.populated == true}"><fmt:message key="yes" bundle="${resword}"/></c:when>
         <c:otherwise><fmt:message key="no" bundle="${resword}"/></c:otherwise> 
        </c:choose>
       </td>      
      <td class="table_cell"><fmt:formatDate value="${currRow.bean.createdDate}" pattern="${dteFormat}"/><br>(<c:out value="${currRow.bean.owner.name}"/>)</td>
      <td class="table_cell"><fmt:formatDate value="${currRow.bean.updatedDate}" pattern="${dteFormat}"/><br>(<c:out value="${currRow.bean.updater.name}"/>)</td>
      <td class="table_cell" style="display: none;white-space:nowrap" id="Groups_0_9_<c:out value="${eblRowCount+1}"/>">
        <c:forEach var="entry" items="${currRow.bean.crfsWithDefaultVersion}" varStatus="status">
            <c:out value="${entry.key}"/>
            <c:if test="${status.last == false}">
                <hr>
            </c:if>
        </c:forEach>
      </td>
      <td class="table_cell" style="display: none;white-space:nowrap" id="Groups_0_10_<c:out value="${eblRowCount+1}"/>">
        <c:forEach var="entry" items="${currRow.bean.crfsWithDefaultVersion}" varStatus="status">
            <c:out value="${entry.value}"/>
            <c:if test="${status.last == false}">
                <hr>
            </c:if>
        </c:forEach>
      </td>

      <td class="table_cell">
       <table border="0" cellpadding="0" cellspacing="0">
		<tr>
		 <td>
	      <a href="ViewEventDefinition?id=<c:out value="${currRow.bean.id}"/>"
			onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
			onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img 
		    name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
		 </td>
     
     <c:if test="${study.parentStudyId <= 0 && readOnly != 'true' }"> 
     
      <c:if test="${userBean.sysAdmin || userRole.manageStudy}">
       <c:choose>
        <c:when test="${currRow.bean.status.available}">
        <c:if test="${!study.status.locked}">
        <td><a href="InitUpdateEventDefinition?id=<c:out value="${currRow.bean.id}"/>"
			onMouseDown="javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');"
			onMouseUp="javascript:setImage('bt_Edit1','images/bt_Edit.gif');"><img 
			name="bt_Edit1" src="images/bt_Edit.gif" border="0" alt="<fmt:message key="edit" bundle="${resword}"/>" title="<fmt:message key="edit" bundle="${resword}"/>" align="left" hspace="6"></a>
		  </td>
        <td><a href="RemoveEventDefinition?action=confirm&id=<c:out value="${currRow.bean.id}"/>"
			onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
			onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><img 
			name="bt_Remove1" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
		</td>
        </c:if>
        <%--remove this for now until we clarify the new requirement-01/17/2008
		<c:if test="${currRow.bean.lockable}">
		  <td><a href="LockEventDefinition?action=confirm&id=<c:out value="${currRow.bean.id}"/>"
			onMouseDown="javascript:setImage('bt_Lock1','images/bt_Lock_d.gif');"
			onMouseUp="javascript:setImage('bt_Lock1','images/bt_Lock.gif');"><img 
			name="bt_Lock1" src="images/bt_Lock.gif" border="0" alt="<fmt:message key="lock" bundle="${resword}"/>" title="<fmt:message key="lock" bundle="${resword}"/>" align="left" hspace="6"></a>
		  </td>
		</c:if>  --%>      
        </c:when>
        <c:otherwise>
        <c:if test="${currRow.bean.status.deleted && (!study.status.locked)}">
         <td><a href="RestoreEventDefinition?action=confirm&id=<c:out value="${currRow.bean.id}"/>"
			onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
			onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"><img 
			name="bt_Restore3" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
		 </td> 
        
        </c:if>
        </c:otherwise>
       </c:choose>     
      </c:if>
      <%--
       <c:if test="${userBean.sysAdmin &&  currRow.bean.status.locked}">             
        <td><a href="UnlockEventDefinition?action=confirm&id=<c:out value="${currRow.bean.id}"/>"
			onMouseDown="javascript:setImage('bt_Unlock1','images/bt_Unlock_d.gif');"
			onMouseUp="javascript:setImage('bt_Unlock1','images/bt_Unlock.gif');"><img 
			name="bt_Unlock1" src="images/bt_Unlock.gif" border="0" alt="<fmt:message key="unlock" bundle="${resword}"/>" title="<fmt:message key="unlock" bundle="${resword}"/>" align="left" hspace="6"></a>
		  </td>       
       </c:if>--%>
    
    </c:if>
    
       </tr>
       </table>
      </td>       
    </tr>
