<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.io.*" %>

<%
  String path = (String)request.getAttribute("generate");
  System.out.println("file path found at jsp "+path);
  if ( path != null) {
  	ServletOutputStream sos = null;
  	BufferedOutputStream bos = null;
  	InputStream is = null;
  	BufferedInputStream bis = null;
    try {
        if (!path.endsWith(".html")) {
      		response.setContentType("application/download");
        }
      response.setHeader("Pragma", "public");
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
