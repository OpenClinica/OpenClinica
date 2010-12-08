<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<jsp:useBean scope="request" id="itemAudits" class="java.util.ArrayList"/>

<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>
<html>
<head>
    <link rel="stylesheet" href="includes/styles.css" type="text/css">
</head>

  <body>
  <table border="0"><tr><td width="20">&nbsp;</td><td>
      <table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
          <tr>
              <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="audit_event" bundle="${resword}"/></b></td>
              <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date_time_of_server" bundle="${resword}"/></b></td>
              <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="user" bundle="${resword}"/></b></td>
              <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="value_type" bundle="${resword}"/></b></td>
              <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="old" bundle="${resword}"/></b></td>
              <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="new" bundle="${resword}"/></b></td>
          </tr>
          <c:forEach var="audit" items="${itemAudits}">
          <tr>
              <td class="table_header_column"><c:out value="${audit.auditEventTypeName}"/>&nbsp;</td>
              <td class="table_header_column"><fmt:formatDate value="${audit.auditDate}" type="both" pattern="${dtetmeFormat}" timeStyle="short"/>&nbsp;</td>
              <td class="table_header_column"><c:out value="${audit.userName}"/>&nbsp;</td>
              <td class="table_header_column"><c:out value="${audit.entityName}"/>&nbsp;</td>
              <td class="table_header_column"><c:out value="${audit.oldValue}"/>&nbsp;</td>
              <td class="table_header_column"><c:out value="${audit.newValue}"/>&nbsp;</td>
          </tr>
          </c:forEach>
          </table>
      </td>
      </tr>
      </table>

  </body>
</html>