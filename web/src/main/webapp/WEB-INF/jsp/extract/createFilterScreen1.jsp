<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<h1><span class="title_manage"><fmt:message key="view_dataset_filters" bundle="${resword}"/></span></h1>

<P><jsp:include page="../include/showPageMessages.jsp"/></P>
<P>
<fmt:message key="for_the_current_study_site_include_filters" bundle="${restext}"/></P>
<c:import url="../include/showTable.jsp"><c:param name="rowURL" value="showFilterRow.jsp" /></c:import>

<jsp:include page="../include/footer.jsp"/>
