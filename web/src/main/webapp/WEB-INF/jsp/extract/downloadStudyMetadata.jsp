<%String path = String.valueOf(request.getAttribute("generate"));
  if ( path != null) {
    try {
      response.setContentType("application/xml");
      response.setHeader("Pragma", "public");
      out.print(path);
    } catch (Exception ee) {
      ee.printStackTrace();
    }
  }
  
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.io.*" %>
