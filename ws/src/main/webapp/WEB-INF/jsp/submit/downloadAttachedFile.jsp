<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="java.io.*" %>
<jsp:useBean scope='session' id='pageMessage' class='java.lang.String'/>
<jsp:useBean scope='request' id='downloadStatus' class='java.lang.String'/>

<html>
<head></head>
<body>
	<c:choose>
	<c:when test="${downloadStatus == 'true'}">
		<%
		  String path = (String)request.getAttribute("generate");
		  System.out.println("file path found at jsp "+path);
		  if ( path != null) {
		  	ServletOutputStream sos = null;
		  	BufferedOutputStream bos = null;
		  	InputStream is = null;
		  	BufferedInputStream bis = null;
		    try {
		      response.setContentType("application/download");
		      response.setHeader("Cache-Control", "max-age=0");
		      //response.setHeader("Content-disposition",
		      //                   "attachment; filename=\"" + path + "\"");
		      sos = response.getOutputStream();

		      bos = new BufferedOutputStream(sos);
		      java.io.File local = new java.io.File(path);
		      is = new FileInputStream(local);
		      bis = new BufferedInputStream(is);
		      int length = (int) local.length();
		      int bytesRead;
		      byte[] buff = new byte[length];

		      while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
		      	bos.write(buff, 0, bytesRead);
		      }

		    } catch (Exception ee) {
		      ee.printStackTrace();
		      sos.print("Error streaming data! :"+ee.getMessage());
		    } finally {
		    	if( bis != null ) {
		        	bis.close();
		        }
		    	if( is != null ) {
		        	is.close();
		        }
		        if( bos != null ) {
		        	bos.close();
		        }
		        if( sos != null ) {
		            sos.flush();
		            sos.close();
		        }
		    }
		  }
		%>
	</c:when>
	<c:otherwise>
		<br><br><br><br><br>
		<c:forEach var="message" items="${pageMessages}">
		 <c:out value="${message}" escapeXml="false"/>
		</c:forEach>
	</c:otherwise>
	</c:choose>


</body>
</html>
