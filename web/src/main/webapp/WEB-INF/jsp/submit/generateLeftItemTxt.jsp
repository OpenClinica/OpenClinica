<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ page import="core.org.akaza.openclinica.logic.score.*, java.util.HashMap, java.util.ArrayList, core.org.akaza.openclinica.bean.submit.ResponseOptionBean, core.org.akaza.openclinica.bean.submit.ItemBean" %>

<c:set var="inputType" value="${param.inputType}" />
<c:set var="itemId" value="${param.itemId}" />
<c:set var="function" value="${param.function}"/>
<c:set var="linkText" value="${param.linkText}"/>
<c:set var="side" value="${param.side}"/>

<!-- PAGE GENERATED BY generateLeftItemTxt.jsp <c:out value="${inputType}"/> <c:out value="${itemId}"/> -->
<c:choose>
	<c:when test="${inputType eq 'calculation'}">
	<!-- PAGE GENERATED BY generateLeftItemTxt.jsp, inside first loop -->
	<%-- find out if it is find external value or not --%>
	
	<!-- FOUND: FUNCTION <c:out value="${function}"/> -->
	<c:forTokens items="${function}" delims="\"" begin="0" end="0" var="functionName">
	<!-- FOUND: FUNCTION NAME <c:out value="${functionName}"/> -->
	<c:choose>
		<c:when test="${(functionName eq 'func: getExternalValue(' || functionName eq 'func: getexternalvalue(') && (linkText ne '')}">
		<%-- TODO find out if we are generating left or right links? --%>
			<c:forTokens items="${function}" delims="\"" begin="1" end="1" var="functionUrl">
			<c:set var="mainUrl" value = "${functionUrl}?item=mainForm.input${itemId}"/>
				<c:forTokens items="${function}" delims="#)" begin="1" end="1" var="functionSide">
				<!-- FOUND: FUNCTION SIDE <c:out value="${functionSide}"/> -->
				<c:choose>
				<c:when test="${side eq functionSide}">
					
					<c:forTokens items="${function}" delims="#)" begin="2" end="2" var="functionWidth">
						<c:forTokens items="${function}" delims="#)" begin="3" end="3" var="functionHeight">
							<a href="#" onclick="javascript:window.open('<c:out value="${mainUrl}"/>','_Blank','width=<c:out value="${functionWidth}"/>,height=<c:out value="${functionHeight}"/>,scrollbars=1'); return false;"><c:out value="${linkText}"/></a>
						</c:forTokens>
					</c:forTokens>
				</c:when>
				<c:otherwise>
					<c:out value="${linkText}" escapeXml="false"/>
				</c:otherwise>
				</c:choose>
				</c:forTokens>
			</c:forTokens>
		</c:when>
		<c:otherwise>
			<c:out value="${linkText}" escapeXml="false"/>
		</c:otherwise>
	</c:choose>

	</c:forTokens>

	</c:when>
	<c:otherwise>
		<%-- generate normally --%>
		<c:out value="${linkText}" escapeXml="false"/>
	</c:otherwise>

</c:choose>