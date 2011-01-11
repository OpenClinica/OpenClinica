<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%
ArrayList entities = request.getParameter("entities");
Iterator it = entities.iterator();
Integer value = new Integer(0);

while (it.hasNext()) {
	EntityBean e = (EntityBean) it.next();
	String selected = "";
	
	if (e.getID() == value.intValue()) {
		selected = "selected";
	}
	
%>
	<option value="<%= e.getId() %>" <%= selected %> ><%= e.getName() %></option>
<%
}
%>
