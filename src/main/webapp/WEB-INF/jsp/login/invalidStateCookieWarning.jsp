<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="core.org.akaza.openclinica.logic.importdata.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="java.net.URLEncoder" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterms"/>
<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
  <c:import url="../include/admin-header.jsp"/>
</c:when>
<c:otherwise>
   <c:import url="../include/submit-header.jsp"/>
</c:otherwise>
</c:choose>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open">
  <td class="sidebar_tab">
	<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
	  <span class="icon icon-caret-down gray"></span>
	</a>
	<fmt:message key="instructions" bundle="${restext}"/>
	<div class="sidebar_tab_content"></div>
  </td>
</tr>

<tr id="sidebar_Instructions_closed" style="display: none">
  <td class="sidebar_tab">
	<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
	  <span class="icon icon-caret-right gray"></span>
	</a>
    <fmt:message key="instructions" bundle="${restext}"/>
  </td>
</tr>

<jsp:include page="../include/sideInfo.jsp"/>

<div class="boxed">
  <div class="formlabel" align="left"><h3 class="addNewSubjectTitle"><fmt:message key="redirect_login" bundle="${resword}"/></h3></div>
  <br>
  <hr>
  <table class="center">
    <tr align="center">
      <td>
        <a href="/OpenClinica/pages/logout"><button type="button">Logout</button></a>
      </td>
      &nbsp;&nbsp;&nbsp;
      <td>
        <a href="/OpenClinica/MainMenu"><button type="button">Continue</button></a>
      </td>
    </tr>
  </table>
  <br>
  <hr class="light-line"/>
  <br>
  <div class="description">
    <span class="icon icon-info blue"></span>
    <fmt:message key="bookmark_link" bundle="${resword}"/>
    <a href="/OpenClinica/MainMenu"><p id="url"></p></a>
  </div>
</div>

<jsp:include page="../include/footer.jsp"/>

<script>
  $(document).ready(function(){
    var url = window.location.href;
    var n = url.indexOf("/OpenClinica/InvalidStateCookieWarning");
    url = url.substring(0, n);
    document.getElementById('url').innerHTML = url;
  })
</script>

<style>
	.boxed .description {
		text-align: center;
	}
  .boxed {
    box-sizing: border-box;
    width: 35%;
    border: solid grey 5px;
    padding: 50px;
    margin-top: 10%;
    margin-left: 20%;
  }
  .boxed .msg {
    font-style: normal;
    font-size: 18px;
    font-family: "Open Sans", "Helvetica Neue", Arial, Helvetica, "Yu Gothic", Meiryo, sans-serif;
  }

  hr.light-line {
    border: 0;
    height: 0;
    border-top: 1px solid rgba(0, 0, 0, 0.1);
    border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  }
  table.center {
    margin-left:auto;
    margin-right:auto;
  }
</style>
