<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%
Integer value = (Integer) request.getParameter("value");  // hmm... that won't work...
List terms = (List) request.getParameter("terms");
Iterator it = terms.iterator();

while (it.hasNext()) {
	Term t = (Term) it.next();
	String selected = "";
	
	if (t.getID() == value.intValue()) {
		selected = "selected";
	}
	
%>
	<option value="<%= t.getId() %>" <%= selected %> ><%= t.getName() %></option>
<%
}
%>
