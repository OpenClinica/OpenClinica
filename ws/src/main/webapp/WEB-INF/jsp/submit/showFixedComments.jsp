<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:useBean scope="request" id="section" class="org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<jsp:useBean scope="request" id="annotations" class="java.lang.String" />

<div style="width: 100%">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<!-- Table Contents -->

<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<tr>
		<td valign="top" class="table_header_column_top"><fmt:message key="comments_or_annotations" bundle="${restext}"/>:</td>
    </tr>
    <tr>		
		<td valign="top" class="table_cell_top">
			<textarea name="annotations" rows="8" cols="50" disabled style="background:white;color:#4D4D4D;"><c:out value="${annotations}" /></textarea>
		</td>
	</tr>
</table>

</div>
</div></div></div></div></div></div></div></div>
</div>
