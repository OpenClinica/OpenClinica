<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>

<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>

<h1><span class="title_manage"><fmt:message key="create_filters" bundle="${resword}"/>: <fmt:message key="instructions" bundle="${resword}"/></span></h1>
<P>
<fmt:message key="throughout_the_next_few_screens" bundle="${restext}"/>
</P>

<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td>

			<!-- These DIVs define shaded box borders -->
				<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

					<div class="textbox_center" align="center">

					<span class="title_submit">
					<b><fmt:message key="select" bundle="${resword}"/><br> <fmt:message key="CRF" bundle="${resword}"/></b><br><br>
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
					<b><fmt:message key="select" bundle="${resword}"/><br> <fmt:message key="section" bundle="${resword}"/></b><br><br>
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
					<b><fmt:message key="select" bundle="${resword}"/><br> <fmt:message key="parameters" bundle="${resword}"/></b><br><br>
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
					<b><fmt:message key="specify" bundle="${resword}"/><br> <fmt:message key="criteria" bundle="${resword}"/></b><br><br>
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
					<b><fmt:message key="repeat_as_necessary" bundle="${resword}"/></b><br><br>
					</span>

					</div>

				</div></div></div></div></div></div></div></div>

			</td>
		</tr>
	</table>

	<br>
	<P><fmt:message key="create_a_filter_to_limit_subjects" bundle="${restext}"/></P>
<ol>
<li><fmt:message key="select_CRF" bundle="${resworkflow}"/>

<li><fmt:message key="select_section" bundle="${resworkflow}"/>

<li><fmt:message key="select_parameters" bundle="${resworkflow}"/>

<li><fmt:message key="select_connector_values_and_operators" bundle="${restext}"/>

<li><fmt:message key="repeat_steps_1_4" bundle="${restext}"/>
</ol>
<form action="CreateFiltersTwo" type="post">
<input type="hidden" name="action" value="begin"/>
<input type="submit" value="<fmt:message key="proceed_to_create_filter" bundle="${restext}"/>" class="button_xlong"/>
</form>
<jsp:include page="../include/footer.jsp"/>
