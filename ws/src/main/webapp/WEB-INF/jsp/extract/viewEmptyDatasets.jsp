<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/> 
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="datasets" class="java.util.ArrayList"/>

<h1><span class="title_manage"><fmt:message key="view_datasets" bundle="${resword}"/></span></h1>

<P><jsp:include page="../include/showPageMessages.jsp"/></P>

	<p><fmt:message key="currently_no_datasets" bundle="${restext}"/></p>

<p><center>
<a href="ViewDatasets"><fmt:message key="show_all_datasets" bundle="${resword}"/></a> |
<a href="CreateDataset"><fmt:message key="create_dataset" bundle="${resword}"/></a>
</center></p>


<jsp:include page="../include/footer.jsp"/>
