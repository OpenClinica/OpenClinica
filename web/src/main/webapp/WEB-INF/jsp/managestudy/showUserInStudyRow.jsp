<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.StudyUserRoleRow"/>

<tr valign="top">
    <td class="table_cell_left"><c:out value="${currRow.bean.userName}"/></td>
    <td class="table_cell"><c:out value="${currRow.bean.firstName}"/></td>
    <td class="table_cell"><c:out value="${currRow.bean.lastName}"/></td>
    <td class="table_cell">
        <c:if test="${currRow.bean.parentStudyId > 0}">
            <fmt:message key="${siteRoleMap[currRow.bean.role.id] }" bundle="${resterm}"></fmt:message>
        </c:if>
        <c:if test="${currRow.bean.parentStudyId == 0}">
            <c:out value="${currRow.bean.role.description}"/>
        </c:if>

    </td>
    <td class="table_cell"><c:out value="${currRow.bean.studyName}"/></td>
    <td class="table_cell"><c:out value="${currRow.bean.status.name}"/></td>
    <td class="table_cell">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td><a href="ViewStudyUser?name=<c:out value="${currRow.bean.userName}"/>&studyId=<c:out value="${currRow.bean.studyId}"/>"
                       onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
                       onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');">
                    <img name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
                </td>
                <c:choose>
                    <c:when test="${!currRow.bean.status.deleted}">
                        <td><a href="SetStudyUserRole?action=confirm&name=<c:out value="${currRow.bean.userName}"/>&studyId=<c:out value="${currRow.bean.studyId}"/>"
                               onMouseDown="javascript:setImage('bt_SetRole1','images/bt_SetRole_d.gif');"
                               onMouseUp="javascript:setImage('bt_SetRole1','images/bt_SetRole.gif');">
                            <img name="bt_SetRole1" src="images/bt_SetRole.gif" border="0" alt="<fmt:message key="set_role" bundle="${resword}"/>" title="<fmt:message key="set_role" bundle="${resword}"/>" align="left" hspace="6"></a>
                        </td>
                        <td><a href="RemoveStudyUserRole?action=confirm&name=<c:out value="${currRow.bean.userName}"/>&studyId=<c:out value="${currRow.bean.studyId}"/>"
                               onMouseDown="javascript:setImage('bt_RemoveRole1','images/bt_RemoveRole_d.gif');"
                               onMouseUp="javascript:setImage('bt_RemoveRole1','images/bt_RemoveRole.gif');">
                            <img name="bt_RemoveRole1" src="images/bt_RemoveRole.gif" border="0" alt="<fmt:message key="remove_role" bundle="${resword}"/>" title="<fmt:message key="remove_role" bundle="${resword}"/>" align="left" hspace="6"></a>
                        </td>
                    </c:when>
                    <c:otherwise>
                        <td><a href="RestoreStudyUserRole?action=confirm&name=<c:out value="${currRow.bean.userName}"/>&studyId=<c:out value="${currRow.bean.studyId}"/>"
                               onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
                               onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');">
                            <img name="bt_Restore3" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
                        </td>
                    </c:otherwise>
                </c:choose>
            </tr>
        </table>
    </td>
</tr>
   
