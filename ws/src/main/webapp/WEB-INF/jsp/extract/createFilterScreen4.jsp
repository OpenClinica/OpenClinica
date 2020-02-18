<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>


<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>

<h1><span class="title_manage"><fmt:message key="create_filter" bundle="${resword}"/>: <fmt:message key="specify_criteria" bundle="${resword}"/></span></h1>
<P><jsp:include page="../include/showPageMessages.jsp"/></P>
<jsp:include page="createFilterBoxes.jsp">
	<jsp:param name="selectValue" value="1"/>
</jsp:include>
<form action="CreateFiltersTwo" name="cf2">
<input type="hidden" name="action" value="validatecriteria"/>


&nbsp;<b><fmt:message key="filter_preview" bundle="${resword}"/>:</b>&nbsp;&nbsp;
<c:out value='${cBean.name}'/>&nbsp;
<c:out value='${cvBean.name}'/><br/>
<br/>
&nbsp;<b><fmt:message key="filter_query_design" bundle="${resword}"/>:</b><br/>
&nbsp;<b><fmt:message key="logical_connector" bundle="${resword}"/>:</b>&nbsp;&nbsp;<select name="logical">
							<option value="and"><fmt:message key="and" bundle="${resword}"/>
							<option value="or"><fmt:message key="or" bundle="${resword}"/>
							</select><br/>
<table border="0" cellpadding="5">
<tr valign="top">
    <td class="text"><b><fmt:message key="parameter" bundle="${resword}"/></b></td>
    <td class="text"><b><fmt:message key="operator" bundle="${resword}"/></b></td>
    <td class="text"><b><fmt:message key="value" bundle="${resword}"/></b></td>
    <td class="text"><b><fmt:message key="remove2" bundle="${resword}"/></b></td>
</tr>
<c:forEach var='item' items='${questions}'>
<c:set var='response' value='${item.responseSet.options}'/>
<tr valign="top">
    <td class="text">
    		<c:out value='${item.questionNumberLabel}'/>
			<c:out value='${item.header}'/>
			<c:out value='${item.leftItemText}'/>
	</td>
	<%-- if/else block to determine where we are searching --%>
	<c:choose>
		<%-- this should mean text --%>
		<c:when test='${item.responseSet.label=="Text"}'>
			<td class="text">
				<select name="operator:<c:out value='${item.id}'/>">
    			<option value="equal to" selected><fmt:message key="equal_to" bundle="${resword}"/>
    			<option value="like"><fmt:message key="contains_the_text" bundle="${resword}"/>
    			<option value="not like"><fmt:message key="does_not_contain_the_text" bundle="${resword}"/>
    			<option value="not equal to"><fmt:message key="not_equal_to" bundle="${resword}"/>
    			</select>
    		</td>
    		<td class="text">
		    	<input type="text" name="value:<c:out value='${item.id}'/>"/>
    			&nbsp;
    			<c:out value='${item.rightItemText}'/>
    		</td>
		</c:when>
		<%-- this should mean a select dropdown --%>
		<c:otherwise>
			<td class="text"><select name="operator:<c:out value='${item.id}'/>">
    			<option value="equal to" selected><fmt:message key="equal_to" bundle="${resword}"/>
    			<option value="greater than"><fmt:message key="greater_than" bundle="${resword}"/>
    			<option value="less than"><fmt:message key="less_than" bundle="${resword}"/>
    			<option value="greater than or equal"><fmt:message key="greater_than_or_equal" bundle="${resword}"/>
		    	<option value="less than or equal"><fmt:message key="less_than_or_equal" bundle="${resword}"/>
    			<option value="not equal to"><fmt:message key="not_equal_to" bundle="${resword}"/>
		    	</select>
    		</td>
		    <td class="text">
		    	<select name="value:<c:out value='${item.id}'/>">
    			<option value="" selected>
    			<option value="N/A"><fmt:message key="N/A" bundle="${resword}"/>
    			<option value="No Response"><fmt:message key="no_response" bundle="${resword}"/>
    			<c:forEach var='option' items='${response}'>
    				<option value="<c:out value='${option.value}'/>">
    				<c:out value='${option.text}'/> - <c:out value='${option.value}'/>
    			</c:forEach>
    			</select>
    			&nbsp;
    			<c:out value='${item.rightItemText}'/>
    		</td>
		</c:otherwise>
	</c:choose>

    <td class="text">
    	<input type="checkbox" value="remove" name="remove:<c:out value='${item.id}'/>"/>
    </td>
</tr>
</c:forEach>
</table>
<center>
<input type="submit" name="submit" value="<fmt:message key="add_additional_parameters" bundle="${resword}"/>" class="button_xlong"/><br/>
<input type="submit" name="submit" value="<fmt:message key="specify_filter_metadata" bundle="${resword}"/>" class="button_xlong"/><br/>
</center>
</form>
<jsp:include page="../include/footer.jsp"/>
