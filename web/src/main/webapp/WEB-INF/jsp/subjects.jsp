<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="resmessages"/>


<jsp:include page="include/managestudy_top_pages.jsp"/>

<!-- move the alert message to the sidebar-->
<jsp:include page="include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray border="0" align="right" hspace="10"></span></a>

        <fmt:message key="instructions" bundle="${restext}"/>

        <div class="sidebar_tab_content">

            <fmt:message key="design_implement_sdv_study_subject" bundle="${restext}"/>

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></span></a>

        <fmt:message key="instructions" bundle="${restext}"/>

    </td>
</tr>
<jsp:include page="include/sideInfo.jsp"/>
<link rel="stylesheet" href="../includes/jmesa/jmesa.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery.jmesa.js"></script>
<script type="text/javascript" language="JavaScript" src="../includes/jmesa/jquery-migrate-1.1.1.js"></script>
<script type="text/javascript">
    function onInvokeAction(id,action) {
        setExportToLimit(id, '');
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        //location.href = '${pageContext.request.contextPath}/ViewCRF?module=manage&crfId=' + '${crf.id}&' + parameterString;
    }
</script>

<h1>
    <span class="title_manage"><fmt:message key="sdv_sdv_for" bundle="${resword}"/> <c:out value="${study.name}"/></span>
</h1>
JSON path to call via AJAX to fetch study subject data - ${jsonPath} <br/>
XML path to call via AJAX to fetch study subject data - ${xmlPath}
<jsp:include page="include/footer.jsp"/>
</body>
</html>