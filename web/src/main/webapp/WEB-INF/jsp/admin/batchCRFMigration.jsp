<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<fmt:setBundle basename="org.akaza.openclinica.i18n.format"
	var="resformat" />
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext" />
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword" />
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow"
	var="resworkflow" />
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages"
	var="resmessages" />

<c:set var="dteFormat">
	<fmt:message key="date_format_string" bundle="${resformat}" />
</c:set>

<jsp:include page="../include/managestudy-header.jsp" />
<jsp:include page="../include/sideAlert.jsp" />

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
	<td class="sidebar_tab"><a
		href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img
			src="images/sidebar_collapse.gif" border="0" align="right"
			hspace="10"></a> <b><fmt:message key="instructions"
				bundle="${restext}" /></b>

		<div class="sidebar_tab_content"></div></td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
	<td class="sidebar_tab"><a
		href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img
			src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>
		<b><fmt:message key="instructions" bundle="${resword}" /></b>
		<div class="sidebar_tab_content">
			<fmt:message key="choose_crf_migration_batch_instruction_key"
				bundle="${resword}" />
		</div></td>
</tr>



<jsp:include page="../include/sideInfo.jsp" />
<jsp:useBean scope="request" id="displayStudy"
	class="org.akaza.openclinica.bean.admin.DisplayStudyBean" />
<jsp:useBean scope="session" id="study"
	class="org.akaza.openclinica.bean.managestudy.StudyBean" />
<jsp:useBean scope="request" id="subject"
	class="org.akaza.openclinica.bean.submit.SubjectBean" />
<jsp:useBean scope="request" id="studySub"
	class="org.akaza.openclinica.bean.managestudy.StudySubjectBean" />
<jsp:useBean scope='session' id='userBean'
	class='org.akaza.openclinica.bean.login.UserAccountBean' />
<jsp:useBean scope='request' id='crf'
	class='org.akaza.openclinica.bean.admin.CRFBean' />
<jsp:useBean scope='request' id='siteList' class='java.util.ArrayList' />
<jsp:useBean scope='request' id='eventList' class='java.util.ArrayList' />



<h1>
	<span class="title_manage"> <fmt:message
			key="batch_crf_version_migration_for" bundle="${resworkflow}"/>&nbsp;<c:out value="${crf.name}"/>
	</span>
</h1>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</script>
<script type="text/javascript">
	function madeAjaxCall() {
		$
				.ajax({
					type : "post",
					url : "${pageContext.request.contextPath}/pages/api/v1/forms/migrate/preview",
					cache : false,
					data : 'selectedSourceVersion='
							+ $("#selectedSourceVersion").val()
							+ "&selectedTargetVersion="
							+ $("#selectedTargetVersion").val()
							+ "&selectedSites=" + $("#selectedSites").val()
							+ "&selectedEvents=" + $("#selectedEvents").val()
							+ "&studyOid=" + $("#studyOid").val(),
					dataType : 'json',
					success : function(obj) {
						$('#result').empty();
						$('#result').append(obj.reportPreview);
						if (obj.subjectCount == 0) {
							$('.button_xlong').css('display', 'none');
						} else {
							$('.button_xlong').css('display', 'block');
						}
					},
					error : function(e) {
						alert('Error: ' + e);
					}
				});
	}

	$(function() {

		$('#selectedSourceVersion').change(function() {
			$('.button_xlong').css('display', 'none');
			$('#result').empty();			
		});
	});

	$(function() {
		$('#selectedTargetVersion').change(function() {
			$('.button_xlong').css('display', 'none');
			$('#result').empty();
		});
	});

	$(function() {
		$('#selectedSites').change(function() {
			$('.button_xlong').css('display', 'none');
			$('#result').empty();
		});
	});

	$(function() {
		$('#selectedEvents').change(function() {
			$('.button_xlong').css('display', 'none');
			$('#result').empty();
		});
	});
</script>



<form
	action="${pageContext.request.contextPath}/pages/api/v1/forms/migrate/run"
	method="post">
	<input type="hidden" name="studyOid" id="studyOid" value="${study.oid}">
	<input type="hidden" name="crfId" id="crfId" value="${crf.id}">


	<table cellpadding="2" cellspacing="2" border="0" class="dataTable">

		<tr></tr>
		<tr>
			<td><fmt:message key="current_version_of" bundle="${resword}"/>&nbsp;<c:out value="${crf.name}"/>&#58;</td>
			<td><select name="selectedSourceVersion"
				id="selectedSourceVersion">
					<option value="-1">-Select-</option>
					<c:forEach var="version" items="${crf.versions}">
						<option value="<c:out value="${version.oid}"/>">&nbsp;
							<c:out value="${version.name}" />&nbsp;
						</option>
					</c:forEach>
			</select></td>
		</tr>
		<tr>
			<td><fmt:message key="new_version_of" bundle="${resword}"/>&nbsp;<c:out value="${crf.name}"/>&#58;</td>
			<td><select name="selectedTargetVersion"
				id="selectedTargetVersion">
					<option value="-1">-Select-</option>
					<c:forEach var="version" items="${crf.versions}">
						<option value="<c:out value="${version.oid}" />">&nbsp;
							<c:out value="${version.name}" />&nbsp;
						</option>
					</c:forEach>
			</select></td>
		</tr>
		<tr></tr>
		<tr>
			<td><fmt:message key="list_of_sites" bundle="${resword}"/>&#58;</td>
			<td><select name="selectedSites" id="selectedSites"
				multiple="multiple">
					<option value="<c:out value="-1" />" selected="selected">&nbsp;
						<c:out value="-All-" />&nbsp;
					</option>
					<c:forEach var="site" items="${siteList}">
						<option value="<c:out value="${site.oid}"/>">&nbsp;
							<c:out value="${site.name}"/>&nbsp;
						</option>
					</c:forEach>
			</select></td>
		</tr>
		<tr>
			<td><fmt:message key="list_of_event_definitions"
					bundle="${resword}"/>&#58;</td>
			<td><select name="selectedEvents" id="selectedEvents"
				multiple="multiple">
					<option value="<c:out value="-1" />" selected="selected">&nbsp;
						<c:out value="-All-" />&nbsp;
					</option>
					<c:forEach var="event" items="${eventList}">
						<option value="<c:out value="${event.oid}"/>">&nbsp;
							<c:out value="${event.name}"/>&nbsp;
						</option>
					</c:forEach>
			</select></td>
		</tr>
		<tr>
			<td><input type="button" onclick="madeAjaxCall();"
				value="<fmt:message key="preview" bundle="${resword}"/>"
				class="button_long"></td>


			<td><input type="submit" name="submit" style="display: none"
				value="<fmt:message key="migrate" bundle="${resword}"/>"
				class="button_xlong"></td>

		</tr>

	</table>

</form>
<div id="result"></div>
<jsp:include page="../include/footer.jsp" />

