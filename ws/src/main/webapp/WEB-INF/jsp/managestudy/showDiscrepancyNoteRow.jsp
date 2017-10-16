<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<c:set var="eblRowCount" value="${param.eblRowCount}" />
<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.DiscrepancyNoteRow" />
<%--
    remember the ordering here: 
	public static final int COL_SUBJECT = 0; 
    public static final int COL_DATE_CREATED = 1;
    public static final int COL_EVENT_DATE = 2;
    public static final int COL_EVENT = 3;  
    public static final int COL_CRF = 4;
    public static final int COL_ENTITY_NAME = 5;
    public static final int COL_ENTITY_VALUE = 6;
    public static final int COL_DESCRIPTION =7; 
    public static final int COL_DETAILS = 8;
    public static final int COL_NUM_CHILDREN = 9;
    adding a new column here; assigned user = 9,5
	public static final int COL_RES_STATUS = 10;	
	public static final int COL_TYPE = 11;		
	public static final int COL_ENTITY_TYPE =12;		
	public static final int COL_OWNER = 13;			
	public static final int COL_ACTIONS = 14;

	BWP added date_updated; 08/04/2008
--%>
<tr valign="top">
    <td class="table_cell_left"><c:out value="${currRow.bean.subjectName}" /></td>
    <td class="table_cell"><fmt:formatDate value="${currRow.bean.createdDate}" pattern="${dteFormat}"/></td>
    <td class="table_cell"><fmt:formatDate value="${currRow.bean.updatedDate}" pattern="${dteFormat}"/></td>
    <td class="table_cell">
     <c:if test="${currRow.bean.eventStart != null}">
       <fmt:formatDate value="${currRow.bean.eventStart}" pattern="${dteFormat}"/>
     </c:if>&nbsp;
    </td>
    <td class="table_cell"><c:out value="${currRow.bean.eventName}" />&nbsp;</td>
    <td class="table_cell"><c:out value="${currRow.bean.crfName}" />&nbsp;</td>
    <td class="table_cell">
	  <c:choose>
	  <c:when test="${currRow.bean.entityType=='itemData'}">
	    <a href="javascript: openDocWindow('ViewItemDetail?itemId=<c:out value="${currRow.bean.itemId}"/>')"><c:out value="${currRow.bean.entityName}" /></a>&nbsp;
	  </c:when>
	  <c:otherwise>
	    <c:out value="${currRow.bean.entityName}"/>&nbsp;
	  </c:otherwise>
	  </c:choose>
	</td>        
   	<td class="table_cell"><c:out value="${currRow.bean.entityValue}" />&nbsp;</td>
    <td class="table_cell"><c:out value="${currRow.bean.description}" /></td>
    <td class="table_cell" width="400">		
	 <c:out value="${currRow.bean.detailedNotes}" />&nbsp; 
	</td>
	<td class="table_cell" align="right">
		<c:choose>
		<c:when test="${currRow.numChildren == 0 }">
			<c:out value="${currRow.numChildren}" />
		</c:when>
		<c:otherwise>
			<%-- added to address an issue which appears only part of the time, to be continued --%>
			<c:out value="${currRow.numChildren}" />
		</c:otherwise>
		</c:choose></td>
	<td class="table_cell" align="right">
		<c:choose>
			<c:when test="${currRow.bean.assignedUserId > 0}">
				<c:out value="${currRow.bean.assignedUser.firstName}"/> <c:out value="${currRow.bean.assignedUser.lastName}"/> (<c:out value="${currRow.bean.assignedUser.name}"/>)
			</c:when>
			<c:otherwise></c:otherwise>
		</c:choose>
	</td>
    <td class="table_cell" style="display: none" id="Groups_0_10_<c:out value="${eblRowCount+1}"/>"><c:out value="${currRow.status.name}" /></td>
    <td class="table_cell" style="display: none" id="Groups_0_11_<c:out value="${eblRowCount+1}"/>"><c:out value="${currRow.type.name}" /></td>   
   
    <td class="table_cell" style="display: none" id="Groups_0_12_<c:out value="${eblRowCount+1}"/>"><c:out value="${currRow.bean.entityType}" />&nbsp;</td>
	
	<td class="table_cell" style="display: none" id="Groups_0_13_<c:out value="${eblRowCount+1}"/>"><c:out value="${currRow.bean.owner.name}" /></td>	
	
	<%-- ACTIONS --%>
	<td class="table_cell">
	    <table border="0" cellpadding="0" cellspacing="0">
	    <tr>
	    
	    <td>
			<a href="#" onclick="openVNoteWindow('ViewNote?id=<c:out value="${currRow.bean.id}"/>')"
			  onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
			  onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"
			  ><img name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view_discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="view_discrepancy_note" bundle="${resword}"/>" align="left" hspace="6"></a>
		</td>		
		<c:if test="${((!currRow.status.closed && !currRow.status.notApplicable))&&(!study.status.locked)}">
		<td>
			<c:choose>
				<c:when test="${currRow.bean.entityType != 'eventCrf'}">
					<a href="ResolveDiscrepancy?noteId=<c:out value="${currRow.bean.id}"/>"
					onMouseDown="javascript:setImage('bt_Reassign1','images/bt_Reassign_d.gif');"
					onMouseUp="javascript:setImage('bt_Reassign1','images/bt_Reassign.gif');"
					><img name="bt_Resolve1" src="images/bt_Reassign.gif" border="0" alt="<fmt:message key="resolve_discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="resolve_discrepancy_note" bundle="${resword}"/>" align="left" hspace="6"></a>
				</c:when>
				<c:otherwise>
					<c:if test="${currRow.bean.stageId == 5 }">

						<a href="ResolveDiscrepancy?noteId=<c:out value="${currRow.bean.id}"/>"
						onMouseDown="javascript:setImage('bt_Reassign1','images/bt_Reassign_d.gif');"
						onMouseUp="javascript:setImage('bt_Reassign1','images/bt_Reassign.gif');"
						><img name="bt_Resolve1" src="images/bt_Reassign.gif" border="0" alt="<fmt:message key="resolve_discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="resolve_discrepancy_note" bundle="${resword}"/>" align="left" hspace="6"></a>
		    		</c:if>
				</c:otherwise>
			</c:choose>
		</td>
		</c:if>
		</tr>
		</table>			
	</td>
</tr>
