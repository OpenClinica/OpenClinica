<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>


<h1><span class="title_manage"><fmt:message key="create_datasets" bundle="${resword}"/>: <fmt:message key="dataset_filters_applied" bundle="${resword}"/></span></h1>

<P><jsp:include page="../showInfo.jsp"/></P>
<p><fmt:message key="select_filters_to_apply" bundle="${restext}"/></p>
<form action="CreateDataset" method="post">
<input type="hidden" name="action" value="beginsubmit"/>
<input type="checkbox" name="all"> <fmt:message key="filter_1" bundle="${resword}"/><br/>

<center>
<input type="submit" name="remove" value="<fmt:message key="remove_filter" bundle="${resword}"/>" class="button_xlong"/>
<input type="submit" name="apply" value="<fmt:message key="apply_new_filter" bundle="${resword}"/>" class="button_xlong"/>
<input type="submit" name="export" value="<fmt:message key="save_and_export" bundle="${resword}"/>" class="button_xlong"/>
</center>
</form>
<jsp:include page="../include/footer.jsp"/>
