<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="include/managestudy_top_pages.jsp"/>
<!-- should be extract study? -->

<!-- move the alert message to the sidebar-->
<jsp:include page="include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></a>

        <fmt:message key="instructions" bundle="${restext}"/>

        <div class="sidebar_tab_content">

            <fmt:message key="extract_instructions" bundle="${restext}"/>

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></a>

        <fmt:message key="instructions" bundle="${restext}"/>

    </td>
</tr>
<jsp:include page="include/sideInfo.jsp"/>

<div id="startBox" class="box_T">
<div class="box_L">
<div class="box_R">
<div class="box_B">
<div class="box_TL">
<div class="box_TR">
<div class="box_BL">
<div class="box_BR">
<div class="textbox_center">
<%-- TODO I18N --%>
<P><fmt:message key="extract_is_running" bundle="${restext}"/></P>

<ul>
	<li><a href="../ViewDatasets"><fmt:message key="back_to_datasets" bundle="${restext}"/></a></li>
	<li><a href="../ExportDataset?datasetId=${param.datasetId}"><fmt:message key="back_to_dataset" bundle="${restext}"/></a></li>
</ul>
</div>
</div>
</div>
</div>
</div>
</div>
</div>
</div>
</div>




<jsp:include page="include/footer.jsp"/>