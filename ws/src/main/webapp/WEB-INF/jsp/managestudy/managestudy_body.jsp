<%--
  Created by IntelliJ IDEA.
  User: bruceperry
  Date: Nov 18, 2008
  Time: 12:44:19 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean' />
<jsp:useBean scope='session' id='userRole' class='org.akaza.openclinica.bean.login.StudyUserRoleBean' />

<jsp:useBean scope='request' id='sites' class='java.util.ArrayList' />
<jsp:useBean scope='request' id='seds' class='java.util.ArrayList' />
<jsp:useBean scope='request' id='users' class='java.util.ArrayList' />
<jsp:useBean scope='request' id='subs' class='java.util.ArrayList' />
<jsp:useBean id="audits" scope="request" class="java.util.ArrayList" />

<html>
<head><title>Manage Study</title>
    <script type="text/JavaScript" language="JavaScript" src="includes/Tabs.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/CalendarPopup.js"></script>
    <!-- Added for the new Calender -->
    <link rel="stylesheet" type="text/css" media="all" href="includes/new_cal/skins/aqua/theme.css" title="Aqua" />
    <script type="text/javascript" src="includes/new_cal/calendar.js"></script>
    <script type="text/javascript" src="includes/new_cal/lang/calendar-en.js"></script>
    <script type="text/javascript" src="includes/new_cal/calendar-setup.js"></script>
    <!-- End new Calender -->
    <script type="text/JavaScript" language="JavaScript" src="includes/prototype.js"></script>
</head>
<body>
<h1><span class="title_manage"><fmt:message key="manage_study" bundle="${resworkflow}"/></span></h1>
<noscript class="noscript">
    <ul>
        <li><a href="ListStudySubjects">Subjects</a></li>
        <li><a href="ListSubjectGroupClass">Groups</a></li>
        <li><a href="ViewStudyEvents?module=manage">Events</a></li>
        <li><a href="ListDiscNotesSubjectServlet?module=manage">Notes & Discrepancies</a></li>
        <li><a href="ViewRuleAssignment">Rules</a></li>
        <li><a href="ListStudyUser">Users</a></li>
        <li><a href="ListSite">Sites</a></li>
        <li><a href="ListEventDefinition">Event Definitions</a></li>
        <li><a href="ListCRF?module=manage">CRFs</a></li>
        <li><a href="AuditLogStudy">View Study Audit Logs</a></li>
    </ul>
</noscript>
<%-- 3057 removed this: <span style="font-size:12px"><fmt:message key="select_to_manage" bundle="${restext}"/></span>--%>
<h2><fmt:message key="recent_activity" bundle="${restext}"/> ${studyIdentifier}</h2>


<table border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td valign="top" width="330" style="padding-right: 20px">

            <div class="table_title_Manage"><fmt:message key="subjects" bundle="${resworkflow}"/>
                <c:choose>
                    <c:when test="${subsCount>0}">
                        (<c:out value="${subsCount}"/> <fmt:message key="of" bundle="${restext}"/> <c:out value="${allSubsCount}"/> <fmt:message key="shown" bundle="${restext}"/>)
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="currently_no_subjects" bundle="${restext}"/>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- These DIVs define shaded box borders -->
            <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

                <div class="tablebox_center">
                    <table border="0" cellpadding="0" cellspacing="0" width="100%">
                        <tr valign="top">
                            <td class="table_header_row_left"><fmt:message key="study_subject_ID" bundle="${resword}"/></td>
                            <td class="table_header_row"><fmt:message key="date_updated" bundle="${resword}"/></td>
                            <td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
                        </tr>
                        <c:forEach var="sub" items="${subs}">
                            <tr valign="top">
                                <td class="table_cell_left"><c:out value="${sub.label}"/></td>
                                <td class="table_cell">
                                    <c:choose>
                                        <c:when test="${sub.updatedDate != null}">
                                            <fmt:formatDate value="${sub.updatedDate}" pattern="${dteFormat}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatDate value="${sub.createdDate}" pattern="${dteFormat}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="table_cell"><c:out value="${sub.status.name}"/></td>
                            </tr>
                        </c:forEach>
                        <tr valign="top">
                            <td class="table_cell" align="right" colspan="3">
                                <c:if test="${subsCount>0}">
                                    <a href="ListStudySubjects"><fmt:message key="show_all" bundle="${resword}"/></a> |
                                </c:if>
                                <a href="AddNewSubject"><fmt:message key="add_new" bundle="${resword}"/></a> </td>
                        </tr>
                    </table>

                </div>

            </div></div></div></div></div></div></div></div>

        </td>
        <td valign="top" width="330" style="padding-right: 20px">

            <div class="table_title_Manage"><fmt:message key="users" bundle="${resword}"/>
                <c:choose>
                    <c:when test="${usersCount>0}">
                        (<c:out value="${usersCount}"/> <fmt:message key="of" bundle="${restext}"/> <c:out value="${allUsersCount}"/> <fmt:message key="shown" bundle="${restext}"/>)
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="currently_no_users" bundle="${restext}"/>
                    </c:otherwise>
                </c:choose></div>

            <!-- These DIVs define shaded box borders -->
            <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

                <div class="tablebox_center">
                    <table border="0" cellpadding="0" cellspacing="0" width="100%">
                        <tr valign="top">
                            <td class="table_header_row_left"><fmt:message key="user_name" bundle="${resword}"/></td>
                            <td class="table_header_row"><fmt:message key="role" bundle="${resword}"/></td>
                            <td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
                        </tr>
                        <c:forEach var="user" items="${users}">
                            <tr valign="top">
                                <td class="table_cell_left"><c:out value="${user.userName}"/></td>
                                <td class="table_cell"><c:out value="${user.role.description}"/></td>
                                <td class="table_cell"><c:out value="${user.status.name}"/></td>
                            </tr>
                        </c:forEach>
                        <tr valign="top">
                            <td class="table_cell" align="right" colspan="3"> <c:if test="${usersCount>0}"><a href="ListStudyUser"><fmt:message key="show_all" bundle="${resword}"/></a> | </c:if><a href="AssignUserToStudy"><fmt:message key="add_new" bundle="${resword}"/></a> </td>
                        </tr>
                    </table>

                </div>

            </div></div></div></div></div></div></div></div>


        </td>
    </tr>
</table><br>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td valign="top" width="330" style="padding-right: 20px">

    <div class="table_title_Manage"><fmt:message key="sites" bundle="${resword}"/>
        <c:choose>
            <c:when test="${sitesCount>0}">
                (<c:out value="${sitesCount}"/> <fmt:message key="of" bundle="${restext}"/> <c:out value="${allSitesCount}"/> <fmt:message key="shown" bundle="${restext}"/>)
            </c:when>
            <c:otherwise>
                <fmt:message key="currently_no_sites" bundle="${restext}"/>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- These DIVs define shaded box borders -->
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

        <div class="tablebox_center">
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr valign="top">
                    <td class="table_header_row_left"><fmt:message key="name" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="date_updated" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
                </tr>
                <c:choose>
                    <c:when test="${study.parentStudyId>0}">
                        <tr valign="top"><td class="table_cell" colspan="3"><fmt:message key="site_itself_cannot_have_sites" bundle="${restext}"/></td></tr>

                    </c:when>
                    <c:otherwise>

                        <c:forEach var="site" items="${sites}">
                            <tr valign="top">
                                <td class="table_cell_left"><c:out value="${site.name}"/></td>
                                <td class="table_cell">
                                    <c:choose>
                                        <c:when test="${site.updatedDate != null}">
                                            <fmt:formatDate value="${site.updatedDate}" pattern="${dteFormat}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatDate value="${site.createdDate}" pattern="${dteFormat}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="table_cell"><c:out value="${site.status.name}"/></td>
                            </tr>

                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                <c:if test="${study.parentStudyId==0}">
                    <tr valign="top">
                        <td class="table_cell" align="right" colspan="3">
                            <c:if test="${sitesCount>0}">
                                <a href="ListSite"><fmt:message key="show_all" bundle="${resword}"/></a> |
                            </c:if>
                            <a href="CreateSubStudy"><fmt:message key="add_new" bundle="${resword}"/></a> </td>
                    </tr>
                </c:if>
            </table>

        </div>

    </div></div></div></div></div></div></div></div>

</td>
<td valign="top" width="330" style="padding-right: 20px">

    <div class="table_title_Manage"><fmt:message key="study_event_definitions" bundle="${resworkflow}"/>
        <c:choose>
            <c:when test="${sedsCount>0}">
                (<c:out value="${sedsCount}"/> <fmt:message key="of" bundle="${restext}"/> <c:out value="${allSedsCount}"/> <fmt:message key="shown" bundle="${restext}"/>)
            </c:when>
            <c:otherwise>
                <fmt:message key="currently_no_definitions" bundle="${restext}"/>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- These DIVs define shaded box borders -->
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

        <div class="tablebox_center">
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr valign="top">
                    <td class="table_header_row_left"><fmt:message key="name" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="date_updated" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
                </tr>
                <c:choose>
                    <c:when test="${study.parentStudyId>0}">
                        <tr valign="top"><td class="table_cell" colspan="3"><fmt:message key="site_itself_cannot_have_definitions" bundle="${restext}"/></td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="def" items="${seds}">
                            <tr valign="top">
                                <td class="table_cell_left"><c:out value="${def.name}"/></td>
                                <td class="table_cell">
                                    <c:choose>
                                        <c:when test="${def.updatedDate != null}">
                                            <fmt:formatDate value="${def.updatedDate}" pattern="${dteFormat}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatDate value="${def.createdDate}" pattern="${dteFormat}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="table_cell"><c:out value="${def.status.name}"/></td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>

                <c:if test="${study.parentStudyId==0}">
                    <tr valign="top">
                        <td class="table_cell" align="right" colspan="3">
                            <c:if test="${sedsCount>0}">
                                <a href="ListEventDefinition"><fmt:message key="show_all" bundle="${resword}"/></a> |
                            </c:if>
                            <a href="DefineStudyEvent"><fmt:message key="add_new" bundle="${resword}"/></a>
                        </td>
                    </tr>
                </c:if>

            </table>

        </div>

    </div></div></div></div></div></div></div></div>


</td>
</tr>
<tr><!-- extra row added by tbh, to support direct link to study audit logs -->
    <td colspan="2">
        <!-- following code clipped from view study subject, tbh -->

        <div style="width: 250px">


            <div class="table_title_Manage">
                <%--BWP 3057: moved to top of page and managestudy-header.jsp --%>
                <%--<a name="log"><a href="AuditLogStudy"><fmt:message key="view_study_audit_log" bundle="${resword}"/></a></a>--%>

                <!--
  <a href="javascript:leftnavExpand('logs');javascript:setImage('ExpandGroup4','images/bt_Collapse.gif');"><img
	     name="ExpandGroup4" src="images/bt_Expand.gif" border="0"><fmt:message key="recent_activity_log" bundle="${restext}"/></a></a></div>
<div id="logs" style="display:none">
 <div style="width: 600px">-->

                <!-- These DIVs define shaded box borders -->
                <!--
		<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

			<div class="tablebox_center">

			<table border="0" cellpadding="0" cellspacing="0" width="100%">
				<tr>
							<td class="table_header_column"><fmt:message key="date" bundle="${resword}"/></td>
                            <td class="table_header_column"><fmt:message key="action_message" bundle="${resword}"/></td>
                            <td class="table_header_column"><fmt:message key="entity_operation" bundle="${resword}"/></td>
                            <td class="table_header_column"><fmt:message key="subject_unique_ID" bundle="${resword}"/>S</td>
                            <td class="table_header_column"><fmt:message key="updated_by" bundle="${resword}"/></td>
						</tr>
						<c:forEach var="audit" items="${audits}">
						  <tr>
							<td class="table_cell">
							<c:if test="${audit.auditDate != null}">
							 <fmt:formatDate value="${audit.auditDate}" pattern="${dteFormat}"/>
							</c:if>&nbsp;
							</td>
							<td class="table_cell"><c:out value="${audit.actionMessage}"/></td>
							<td class="table_cell"><c:out value="${audit.auditTable}"/></td>
							<td class="table_cell"><c:out value="${audit.subjectName}"/></td>
							<td class="table_cell"><c:out value="${audit.updater.name}"/></td>
						  </tr>
						</c:forEach>
						<tr valign="top">
					   <td class="table_cell" align="right" colspan="5">
					     <a href="AuditLogStudy"><fmt:message key="show_all" bundle="${resword}"/></a>
					  </td>
					  </tr>
				</table>




			</div>

		</div></div></div></div></div></div></div></div>

		</div>

		<br><br>
  </div>-->

                <!-- above code clipped from view study subject, tbh -->

    </td>
</tr>
</table>
</body>
</html>