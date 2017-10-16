<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
 
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>  
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/> 

 <c:set var="imagePathPrefix" value="${imagePathPrefix}" />
 <c:set var="module" value="${param.module}"/>
<br><br>

<!-- EXPANDING WORKFLOW BOX -->

<table border="0" cellpadding="0" cellspacing="0" style="position: relative; left: -14px;">
	<tr>
		<td id="sidebar_Workflow_closed" style="display: none">
		<a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="${imagePathPrefix}images/<fmt:message key="image_dir" bundle="${resformat}"/>/tab_Workflow_closed.gif" border="0"></a>
	</td>
	<td id="sidebar_Workflow_open" style="display: all">
	<table border="0" cellpadding="0" cellspacing="0" class="workflowBox">
		<tr>
			<td class="workflowBox_T" valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="workflow_tab">
					<a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="${imagePathPrefix}images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

					<b><fmt:message key="workflow" bundle="${resword}"/></b>

					</td>
				</tr>
			</table>
			</td>
			<td class="workflowBox_T" align="right" valign="top"><img src="${imagePathPrefix}images/workflowBox_TR.gif"></td>
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
                      <td><img src="${imagePathPrefix}images/arrow.gif"></td>
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
