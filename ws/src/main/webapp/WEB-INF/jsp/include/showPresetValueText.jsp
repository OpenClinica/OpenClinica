<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<jsp:useBean scope='request' id='presetValues' class='java.util.HashMap'/>
<%
String fieldName = request.getParameter("fieldName");
Object fieldValue;

if (presetValues.containsKey(fieldName)) {
	fieldValue = presetValues.get(fieldName);
	%>
<%= fieldValue.toString() %>
	<%
}
%>
