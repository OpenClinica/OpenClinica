<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.UserAccountRow" />

<tr valign="top" bgcolor="#F5F5F5">
	<td class="table_cell_left">
		<c:choose>
			<c:when test='${currRow.bean.status.deleted}'>
				<font color='gray'><c:out value="${currRow.bean.name}" /></font>
			</c:when>
			<c:otherwise>
				<c:out value="${currRow.bean.name}" />
			</c:otherwise>
		</c:choose>
	</td>
	<td class="table_cell"><c:out value="${currRow.bean.firstName}" /></td>
	<td class="table_cell"><c:out value="${currRow.bean.lastName}" /></td>
	<td class="table_cell"><c:out value="${currRow.bean.status.name}" /></td>
	
	<%-- ACTIONS --%>
	<td class="table_cell">
	 <table border="0" cellpadding="0" cellspacing="0">
	 <tr>
		<c:choose>
		<c:when test='${(currRow.bean.techAdmin && !(userBean.techAdmin))}'>
			<%-- put in grayed-out buttons here? --%>
				<%--<td><img src="images/bt_Remove_i.gif" alt="Access denied" title="Access denied"></img></td>
				<td><img src="images/bt_Remove_i.gif" alt="Access denied" title="Access denied"></img></td>
				<td><img src="images/bt_Remove_i.gif" alt="Access denied" title="Access denied"></img></td> --%>
			</c:when>
			<c:otherwise>
				<c:choose>
				<c:when test='${currRow.bean.status.deleted}'>
					<c:set var="confirmQuestion">
					  <fmt:message key="are_you_sure_you_want_to_restore" bundle="${resword}">
					    <fmt:param value="${currRow.bean.name}"/>
					  </fmt:message>
					</c:set> 
					
					<c:set var="onClick" value="return confirm('${confirmQuestion}');"/>
					<td><a href="DeleteUser?action=4&userId=<c:out value="${currRow.bean.id}"/>" onClick="<c:out value="${onClick}" />"
					onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
				    onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');">	<img name="bt_Restore3" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
				   	</td>
			</c:when>
			<c:otherwise>
				<td><a href="ViewUserAccount?userId=<c:out value="${currRow.bean.id}"/>"
					onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
					onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"
					><img name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a></td>
				<td><a href="EditUserAccount?userId=<c:out value="${currRow.bean.id}"/>"
					onMouseDown="javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');"
					onMouseUp="javascript:setImage('bt_Edit1','images/bt_Edit.gif');"
					><img name="bt_Edit1" src="images/bt_Edit.gif" border="0" alt="<fmt:message key="edit" bundle="${resword}"/>" title="<fmt:message key="edit" bundle="${resword}"/>" align="left" hspace="6"></a></td>
			    <td><a href="SetUserRole?action=confirm&userId=<c:out value="${currRow.bean.id}"/>"
		          onMouseDown="javascript:setImage('bt_SetRole1','images/bt_SetRole_d.gif');"
		          onMouseUp="javascript:setImage('bt_SetRole1','images/bt_SetRole.gif');"><img 
		          name="bt_SetRole1" src="images/bt_SetRole.gif" border="0" alt="<fmt:message key="set_role" bundle="${resword}"/>" title="<fmt:message key="set_role" bundle="${resword}"/>" align="left" hspace="6"></a>
		        </td>		
		
				<c:set var="confirmQuestion">
				 <fmt:message key="are_you_sure_you_want_to_remove" bundle="${resword}">
				   <fmt:param value="${currRow.bean.name}"/>
				 </fmt:message>
				</c:set> 
				
				<c:set var="onClick" value="return confirm('${confirmQuestion}');"/>
				<td><a href="DeleteUser?action=3&userId=<c:out value="${currRow.bean.id}"/>" onClick="<c:out value="${onClick}" />"
					onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
					onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');">
					<img name="bt_Remove1" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
				&nbsp;</td>
				<c:if test='${currRow.bean.status.locked}'>
				<td><a href="UnLockUser?userId=<c:out value="${currRow.bean.id}"/>"
                    onMouseDown="javascript:setImage('bt_Unlock1','images/bt_Unlock.gif');"
                    onMouseUp="javascript:setImage('bt_Unlock1','images/bt_Unlock.gif');"
                    ><img name="bt_Unlock1" src="images/bt_Unlock.gif" border="0" alt="<fmt:message key="unlock" bundle="${resword}"/>" title="<fmt:message key="unlock" bundle="${resword}"/>" align="left" hspace="6"></a>
                </td>
                </c:if>
			</c:otherwise>
			</c:choose>			
			
			</c:otherwise>
		</c:choose>	
		</tr>
		</table>
	</td>
</tr>
<%--<c:choose>
		<c:when test='${(currRow.bean.techAdmin && !(userBean.techAdmin))}'>
		</c:when>
		<c:otherwise>--%>
<%-- start test for tech admin above here --%>
<c:choose>
	<c:when test="${empty currRow.bean.roles}">
		<tr valign="top">
			<td class="table_cell_left">&nbsp;</td>
			<td class="table_cell" colspan="3"><i><fmt:message key="no_roles_assigned" bundle="${resword}"/></i></td>
			<td class="table_cell">&nbsp;</td>
		</tr>
	</c:when>
	<c:otherwise>
		<c:forEach var="sur" items="${currRow.bean.roles}">
			<c:choose>
				<c:when test='${sur.studyName != ""}'>
					<c:set var="study" value="${sur.studyName}" />
				</c:when>
				<c:otherwise>
					<c:set var="study" value="Study ${sur.studyId}" />				
				</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test='${sur.status.deleted}'>
					<c:set var="actionName" >
						<fmt:message key="restore" bundle="${resword}"/>
					</c:set>
					<c:set var="actionId" value="4" />
				</c:when>
				<c:otherwise>
					<c:set var="actionName">
						<fmt:message key="delete" bundle="${resword}"/>
					</c:set>
					<c:set var="actionId" value="3" />
				</c:otherwise>
			</c:choose>
			<c:set var="confirmQuestion"> 
			<fmt:message key="are_you_want_to_the_role_for" bundle="${restext}">
				<fmt:param value="${actionName}"/>
				<fmt:param value="${sur.role.description}"/>
				<fmt:param value="${study}"/>
			</fmt:message>
			</c:set>
			<c:set var="onClick" value="return confirm('${confirmQuestion}');"/>
			<tr valign="top">
				<td class="table_cell_left">&nbsp;</td>
				<td class="table_cell" colspan="3" >
					<c:if test='${sur.status.deleted}'>
						<font color='gray'>
					</c:if>
					<c:choose>
						<c:when test='${sur.studyName != ""}'><c:out value="${sur.studyName}" /></c:when>
						<c:otherwise>Study <c:out value="${sur.studyId}" /></c:otherwise>
					</c:choose>
					- 
					  <c:if test="${sur.parentStudyId > 0}">
                        <fmt:message key="${siteRoleMap[sur.role.id] }" bundle="${resterm}"></fmt:message>
                      </c:if>
                      <c:if test="${sur.parentStudyId == 0}">
                        <fmt:message key="${studyRoleMap[sur.role.id] }" bundle="${resterm}"></fmt:message>
                      </c:if>
					<c:if test='${sur.status.deleted}'>
						</font>
					</c:if>
				</td>
				<td class="table_cell">
					<c:if test='${!sur.status.deleted}'>
						<a href="EditStudyUserRole?studyId=<c:out value="${sur.studyId}" />&userName=<c:out value="${currRow.bean.name}"/>"
							onMouseDown="javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');"
							onMouseUp="javascript:setImage('bt_Edit1','images/bt_Edit.gif');"
							><img name="bt_Edit1" src="images/bt_Edit.gif" border="0" alt="<fmt:message key="edit" bundle="${resword}"/>" title="<fmt:message key="edit" bundle="${resword}"/>" align="left" hspace="6"></a>
					</c:if>
					<c:choose>
					<c:when test='${sur.status.deleted}'>
						<a href="DeleteStudyUserRole?studyId=<c:out value="${sur.studyId}" />&userName=<c:out value="${currRow.bean.name}"/>&action=4" onClick="<c:out value="${onClick}" />"
							onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
						    onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"
						   	><img name="bt_Restore3" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
					</c:when>
					<c:otherwise>
						<a href="DeleteStudyUserRole?studyId=<c:out value="${sur.studyId}" />&userName=<c:out value="${currRow.bean.name}"/>&action=3" onClick="<c:out value="${onClick}" />"
							onMouseDown="javascript:setImage('bt_Remove3','images/bt_Remove_d.gif');"
						    onMouseUp="javascript:setImage('bt_Remove3','images/bt_Remove.gif');"
						   	><img name="bt_Remove3" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
					</c:otherwise>
					</c:choose>
				</td>
			</tr>
		</c:forEach>
	</c:otherwise>
	<%-- end test of tech admin below here --%>
</c:choose>
<%--</c:otherwise>
</c:choose> --%>
