<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td>

			<!-- These DIVs define shaded box borders -->
				<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

					<div class="textbox_center" align="center">

					<span class="title_submit">
					<c:choose>
					<c:when test='${param.selectCrf=="1"}'>
 						<b><fmt:message key="select" bundle="${resword}"/><br> <fmt:message key="CRF" bundle="${resword}"/></b>
						</c:when>
						<c:otherwise>
 							<fmt:message key="select" bundle="${resword}"/><br> <fmt:message key="CRF" bundle="${resword}"/>
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

					<span class="title_submit">
					<c:choose>
					<c:when test='${param.selectSection=="1"}'>
 						<b><fmt:message key="select" bundle="${resword}"/><br> <fmt:message key="section" bundle="${resword}"/></b>
						</c:when>
						<c:otherwise>
 						<fmt:message key="select" bundle="${resword}"/><br> <fmt:message key="section" bundle="${resword}"/>
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

					<span class="title_submit">
					<c:choose>
					<c:when test='${param.selectParameters=="1"}'>
 						<b><fmt:message key="select" bundle="${resword}"/><br> <fmt:message key="parameters" bundle="${resword}"/></b>
						</c:when>
						<c:otherwise>
 						<fmt:message key="select" bundle="${resword}"/><br> <fmt:message key="parameters" bundle="${resword}"/>
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

					<span class="title_submit">
					<c:choose>
					<c:when test='${param.selectValue=="1"}'>
 						<b><fmt:message key="specify" bundle="${resword}"/><br> <fmt:message key="criteria" bundle="${resword}"/></b>
						</c:when>
						<c:otherwise>
 						<fmt:message key="specify" bundle="${resword}"/><br> <fmt:message key="criteria" bundle="${resword}"/>
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

					<span class="title_submit">
					<c:choose>
					<c:when test='${param.save=="1"}'>
 						<b><fmt:message key="save" bundle="${resword}"/><br> <fmt:message key="And" bundle="${resword}"/><br> <fmt:message key="exit" bundle="${resword}"/></b>
						</c:when>
						<c:otherwise>
 						<fmt:message key="save" bundle="${resword}"/><br> <fmt:message key="And" bundle="${resword}"/><br> <fmt:message key="exit" bundle="${resword}"/>
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
