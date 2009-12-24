<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<!-- Main Content Area -->
<div align="justify" style="width: 630px; height: 340px;; background:#FFFFFF; padding:5px 8px 0px 8px">
<h1><span class="title_manage"><fmt:message key="contact_openclinica_administrator" bundle="${restext}"/></span></h1>
<p><fmt:message key="fill_out_form_to_contact" bundle="${restext}"/></p>
<p><fmt:message key="all_fields_are_required" bundle="${resword}"/></p>

<form action="${pageContext.request.contextPath}/Contact" method="post">
<input type="hidden" name="action" value="submit">
<!-- These DIVs define shaded box borders -->
<div>
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">
  <tr><td class="formlabel"><fmt:message key="your_name" bundle="${resword}"/>:</td>
  	<td><div class="formfieldXL_BG"><input type="text" name="name" value="<c:out value="${name}"/>" class="formfieldXL"></div>
  	<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="name"/></jsp:include></td>
  </tr>
  <tr><td class="formlabel"><fmt:message key="your_email" bundle="${resword}"/>:</td>
      <td><div class="formfieldXL_BG"><input type="text" name="email" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="email"/></jsp:include></td>
  </tr>
  <tr><td class="formlabel"><fmt:message key="subject_of_your_question" bundle="${resword}"/>:</td>
  	<td><div class="formfieldXL_BG"><input type="text" name="subject" value="<c:out value="${subject}"/>" class="formfieldXL"></div>
  	<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="subject"/></jsp:include></td>
  </tr>
  <tr><td class="formlabel"><fmt:message key="your_message" bundle="${resword}"/>:</td>
 	<td><div class="formtextareaXL4_BG"><textarea name="message" rows="4" cols="50" class="formtextareaXL4"><c:out value="${message}"/></textarea></div>
 	<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="message"/></jsp:include></td>
 </tr>
</table>
</div>
</div></div></div></div></div></div></div></div>
</div>
<table border="0" cellpadding="0">
 <tr><td>
 <input type="submit" name="submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium">
 </td>
 <td><input type="button" id="cancel" name="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onclick="javascript:window.location.href='login'"></td>
 </tr>
 </table>
</form>
</div>
