<%@tag body-content="scriptless" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<c:set var="module" value="${param.module}"/>
<c:set var="trail" value="${sessionScope['trail']}"/>
<!-- EXPANDING WORKFLOW BOX
removed:  style="position: relative; left: -14px;"-->

<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td id="sidebar_Workflow_closed" style="display: none">
    <a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="../images/<fmt:message key="image_dir" bundle="${resformat}"/>/tab_Workflow_closed.gif" border="0" alt=""></a>
</td>
<td id="sidebar_Workflow_open">
<table border="0" cellpadding="0" cellspacing="0" class="workflowBox">
<tr>
    <td class="workflowBox_T" valign="top">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td class="workflow_tab">
                    <a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="../images/sidebar_collapse.gif" border="0" align="right" hspace="10" alt=""></a>
                    <!--sidebar: <img src="../images/sidebar_collapse.gif"/>-->
                    <b><fmt:message key="workflow" bundle="${resword}"/></b>

                </td>
            </tr>
        </table>
    </td>
    <td class="workflowBox_T" align="right" valign="top"><img src="../images/workflowBox_TR.gif" alt=""></td>
</tr>
<tr>
    <td colspan="2" class="workflowbox_B">
        <div class="box_R"><div class="box_B"><div class="box_BR">
            <div class="workflowBox_center">


                <!-- Workflow items -->

                <table border="0" cellpadding="0" cellspacing="0">
                    <tr>

                        <c:if test="${trail != null || trail.size > 0}">
                            <c:set var="activeBreadcrumbDisplayed" value="${0}" />
                            <c:forEach var="breadcrumb" items="${trail}" varStatus="status">


                                <td>

                                    <!-- These DIVs define shaded box borders -->
                                    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

                                        <div class="textbox_center" align="center">

                                            <c:choose>
                                            <c:when test="${module=='manage'}">
							   <span class="title_manage">
							  </c:when>
							  <c:when test="${module=='submit'}">
							   <span class="title_submit">
							  </c:when>
							  <c:when test="${module=='admin'}">
							   <span class="title_admin">
							  </c:when>
							  <c:when test="${module=='extract'}">
							   <span class="title_extract">
							  </c:when>
							  <c:otherwise>
							    <span class="title_home">
							  </c:otherwise>

							</c:choose>

				             <%-- completed breadcrumb --%>
					         <c:if test="${breadcrumb.status.id == 1}">
                                 <a href="<c:out value="${breadcrumb.url}"/>">
                                     <c:out value="${breadcrumb.name}"/></a>
                             </c:if>
		                      <%-- unavailable breadcrumb --%>
		                     <c:if test="${breadcrumb.status.id == 2}">
                                 <%-- if the breadcrumb is unavailable but the active breadcrumb was not
                                                      already displayed, this means the user cannot return to that step.
                                                      we still want to display the breadcrumb as completed, because the
                                                      user has completed the step.
                                              --%>

                                 <c:out value="${breadcrumb.name}"/>

                             </c:if>
							  <%-- active breadcrumb --%>
							 <c:if test="${breadcrumb.status.id == 4}">

                                 <b><c:out value="${breadcrumb.name}"/></b>

                                 <c:set var="activeBreadcrumbDisplayed" value="${1}" />
                             </c:if>

							</span>

                                        </div>
                                    </div></div></div></div></div></div></div></div>

                                </td>
                                <c:if test="${!status.last}">
                                    <td><img src="../images/arrow.gif" alt=""></td>
                                </c:if>


                            </c:forEach>
                        </c:if>
                    </tr>
                </table>


                <!-- end Workflow items -->

            </div>
        </div></div></div>
    </td>
</tr>
</table>
</td>
</tr>
</table>

<!-- END WORKFLOW BOX -->
