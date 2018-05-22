<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="org.springframework.web.util.HtmlUtils" %>
<jsp:useBean scope='request' id='presetValues' class='java.util.HashMap'/>
<%
String fieldName = request.getParameter("fieldName");
Object fieldValue;

if (presetValues.containsKey(fieldName)) {
	fieldValue = presetValues.get(fieldName);
	%>
<input type="hidden" name="<%=  HtmlUtils.htmlEscape(fieldName) %>" value="<%=  HtmlUtils.htmlEscape(fieldValue.toString()) %>" />
	<%
}
%>
