<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<%--
Usage:

jsp:include page=""
jsp:param name="selectStudyEvents" value="1"
/jsp:include

--%>
<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td>

			<!-- These DIVs define shaded box borders -->
				<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

					<div class="textbox_center" align="center">

					<span class="title_extract">
					<c:choose>
						<c:when test='${param.selectStudyEvents=="1"}'>
 						<b><fmt:message key="select_items_or_event_subject_attributes" bundle="${resword}"/></b>
						</c:when>
						<c:otherwise>
 						<fmt:message key="select_items_or_event_subject_attributes" bundle="${resword}"/>
						</c:otherwise> 
					</c:choose>
					<br><br>
					</span>

					</div>

				</div></div></div></div></div></div></div></div>

			</td>
			<td><img src="images/arrow.gif"></td>
			<td>

			<!-- These DIVs define shaded box borders -->
				<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

					<div class="textbox_center" align="center">

					<span class="title_extract">
					<c:choose>
						<c:when test='${param.longitudinalScope=="1"}'>
 						<b><fmt:message key="define_temporal_scope" bundle="${resword}"/></b>
						</c:when>
						<c:otherwise>
 						<fmt:message key="define_temporal_scope" bundle="${resword}"/>
						</c:otherwise> 
					</c:choose>
					
					
					<br><br>
					</span>

					</div>

				</div></div></div></div></div></div></div></div>

			</td>
			<td><img src="images/arrow.gif"></td>
			<!--<td>-->

			<!-- These DIVs define shaded box borders -->
			<!--	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">-->

			<!--	<div class="textbox_center" align="center">-->

			<!--	<span class="title_extract">-->
			<%--		<c:choose>
						<c:when test='${param.chooseFilter=="1"}'>
 						<b>Choose<br> Filter</b>
						</c:when>
						<c:otherwise>
 						Choose<br> Filter
						</c:otherwise> 
					</c:choose>
			--%>
			<!--	<br><br>
					</span>

					</div>

				</div></div></div></div></div></div></div></div>

			</td>-->
			<!--<td><img src="images/arrow.gif"></td>-->
			<td>

			<!-- These DIVs define shaded box borders -->
				<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

					<div class="textbox_center" align="center">

					<span class="title_extract">
					<c:choose>
						<c:when test='${param.specifyMetadata=="1"}'>
 						<b><fmt:message key="specify_dataset_properties" bundle="${resword}"/></b>
						</c:when>
						<c:otherwise>
 						<fmt:message key="specify_dataset_properties" bundle="${resword}"/>
						</c:otherwise> 
					</c:choose>
					
					<br><br>
					</span>

					</div>

				</div></div></div></div></div></div></div></div>

			</td>
			<td><img src="images/arrow.gif"></td>
			<td>

			<!-- These DIVs define shaded box borders -->
				<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

					<div class="textbox_center" align="center">

					<span class="title_extract">
					<c:choose>
						<c:when test='${param.saveAndExport=="1"}'>
						<b><fmt:message key="save_and_export" bundle="${resword}"/></b>
						</c:when>
						<c:otherwise>
 						<fmt:message key="save_and_export" bundle="${resword}"/>
						</c:otherwise> 
					</c:choose>
					
					
					<br><br>
					</span>

					</div>

				</div></div></div></div></div></div></div></div>

			</td>
		</tr>
	</table>

	<br>
