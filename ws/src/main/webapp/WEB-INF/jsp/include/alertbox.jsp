<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/> 


<!-- Alert Box -->

		<!-- These DIVs define shaded box borders -->
			
			<c:choose>
			<c:when test="${userBean!= null && userBean.id>0}">             
            <jsp:include page="../include/showPageMessages.jsp" />
            </c:when>
            <c:otherwise> 
             <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

			<div class="alertbox_center">
            <fmt:message key="have_logged_out_application" bundle="${resword}"/>
            
				<br><br></div>

			</div></div></div></div></div></div></div></div>
            
            </c:otherwise>
            </c:choose>
       

	<!-- End Alert Box -->
