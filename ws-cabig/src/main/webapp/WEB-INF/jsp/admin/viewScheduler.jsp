<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<jsp:include page="../include/tech-admin-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>


<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>

<h1><span class="title_manage">
<fmt:message key="view_scheduled_task" bundle="${resword}"/>
</span></h1>
<a href="ViewScheduler?action=create"><fmt:message key="click_here_to_start_up" bundle="${restext}"/></a>

<jsp:include page="../include/alertbox.jsp" />



<jsp:include page="../include/footer.jsp"/>
