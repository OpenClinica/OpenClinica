<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>


<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>


<h1><span class="title_manage"><fmt:message key="create_dataset" bundle="${resword}"/>: <fmt:message key="apply_dataset_filter" bundle="${resword}"/></span></h1>

<P><jsp:include page="../showInfo.jsp"/></P>

<jsp:include page="createDatasetBoxes.jsp" flush="true">
<jsp:param name="chooseFilter" value="1"/>
</jsp:include>
<p><fmt:message key="select_a_filter_to_apply" bundle="${restext}"/>
<img name="bt_Export1" src="images/bt_Export.gif" border="0" alt="<fmt:message key="apply_filter" bundle="${resword}"/>" hspace="6"> <fmt:message key="icon_or_nothing" bundle="${resword}"/></p>

<c:import url="../include/showTable.jsp"><c:param name="rowURL" value="applyFilterRow.jsp" /></c:import>


<form action="ApplyFilter" method="post">
<input type="hidden" name="action" value="validate"/>
<%--<input type="submit" name="submit" value="Create New Filter" class="button_xlong"/>
--%>
<input type="submit" name="submit" value="<fmt:message key="skip_apply_filter_and_save" bundle="${resword}"/>" class="button_xlong"/>


</form>
<jsp:include page="../include/footer.jsp"/>
