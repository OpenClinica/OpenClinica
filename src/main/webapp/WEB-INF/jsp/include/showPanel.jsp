<%@ page contentType="text/html; charset=UTF-8" %>
<%-- calling syntax:
	(assuming showTable.jsp and userRow.jsp are in the same directory)
	<c:import url="../include/showPanel.jsp">
		<c:param name="panelURL" value="userPanel.jsp" />
	</c:import>
	alternatively:
	<c:import url="../include/showPanel.jsp"/>
	(this will pull all information from the panel
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:useBean scope="session" id="panel" class="org.akaza.openclinica.view.StudyInfoPanel" />

<%
	String url = request.getParameter("panelURL");
	if (url == null) {
	
%><c:forEach var='line' items='${panel.data}'>
		<b><c:out value='${line.key}'/>:</b>&nbsp;
		<c:out value='${line.value}'/>
	    <br/><br/>
  </c:forEach> 

<%  } else {
%><jsp:include page="<c:out var='request.panelURL'/>"/>
<%  }  %>
