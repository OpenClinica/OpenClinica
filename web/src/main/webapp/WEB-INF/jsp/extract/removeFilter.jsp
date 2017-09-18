<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/> 
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="filter" class="org.akaza.openclinica.bean.extract.FilterBean"/>

<h1><span class="title_manage"><fmt:message key="remove_filter" bundle="${resword}"/>: <c:out value="${filter.name}"/></span></h1>

<P><jsp:include page="../showInfo.jsp"/></P>

<P><fmt:message key="please_review_the_filter_properties_below" bundle="${restext}"/>  
</P>

<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">   
  <tr valign="top"><td class="table_header_column"><fmt:message key="description" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${filter.description}"/>
  </td></tr>
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="owner" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${filter.owner.name}"/>
  </td></tr> 
 
  <tr valign="top"><td class="table_header_column"><fmt:message key="date_created" bundle="${resword}"/>:</td><td class="table_cell">
  <fmt:formatDate value="${filter.createdDate}" pattern="${dteFormat}"/>
  </td></tr>
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="date_last_updated" bundle="${resword}"/>:</td><td class="table_cell">
  <fmt:formatDate value="${filter.updatedDate}" pattern="${dteFormat}"/>
  </td></tr>
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="status" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${filter.status.name}"/>
  </td></tr>
  
  

</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<br>


<form action="RemoveFilter" method="post">

	<input type="hidden" name="filterId" value="<c:out value="${filter.id}"/>"/>
	
	<input type="submit" name="action" value="<fmt:message key="remove_this_filter" bundle="${resword}"/>" class="button_xlong"/>
	&nbsp;<input type="submit" name="action" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_xlong"/>

</form>

<%--
<P><a href="CreateFiltersOne">View Filters</a> | 
<a href="CreateFiltersOne?action=begin&submit=Create New Filter">Create New Filter</a></P>
--%>
<jsp:include page="../include/footer.jsp"/>
