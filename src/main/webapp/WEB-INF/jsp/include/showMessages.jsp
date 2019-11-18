<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<jsp:useBean scope='request' id='messages' class='java.util.HashMap'/>
<%
String key = request.getParameter("key");
if (messages.containsKey(key)) {
	java.util.ArrayList messageList = (java.util.ArrayList) messages.get(key);

	if (messageList !=null) {
	  for (int messagecount = 0; messagecount < messageList.size(); messagecount++) {
		String message = (String) messageList.get(messagecount);
%>
<font color="red"><%= message %></font><br/>
<%
	  }
    }
}
%>
