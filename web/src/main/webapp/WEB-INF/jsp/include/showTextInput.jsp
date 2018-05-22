<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="org.springframework.web.util.HtmlUtils" %>
<jsp:useBean scope='request' id='presetValues' class='java.util.HashMap'/>
<%
String fieldName = request.getParameter("fieldName");
String inputSize = request.getParameter("inputSize");
String fieldValue = "";

if (presetValues.containsKey(fieldName)) {
	fieldValue = (String) presetValues.get(fieldName);
}
%>
<input type="text" name="<%= HtmlUtils.htmlEscape(fieldName) %>" value="<%= HtmlUtils.htmlEscape(fieldValue) %>" size="<%= HtmlUtils.htmlEscape(inputSize) %>" />
