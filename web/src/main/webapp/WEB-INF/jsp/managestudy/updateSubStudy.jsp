<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<jsp:include page="../include/managestudy-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>
<jsp:useBean scope='request' id='parentStudy' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='session' id='newStudy' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="facRecruitStatusMap" class="java.util.HashMap"/>
<jsp:useBean scope="request" id="statuses" class="java.util.ArrayList"/>
<jsp:useBean scope="session" id="parentName" class="java.lang.String"/>
<jsp:useBean scope='session' id='definitions' class='java.util.ArrayList'/>
<jsp:useBean scope='session' id='sdvOptions' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='messages' class='java.util.HashMap'/>
<c:set var="startDate" value="" />
<c:set var="endDate" value="" />
<c:set var="protocolDateVerification" value="" />

<c:forEach var="presetValue" items="${presetValues}">
	<c:if test='${presetValue.key == "startDate"}'>
		<c:set var="startDate" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "endDate"}'>
		<c:set var="endDate" value="${presetValue.value}" />
	</c:if>
    <c:if test='${presetValue.key == "protocolDateVerification"}'>
		<c:set var="protocolDateVerification" value="${presetValue.value}" />	
	</c:if>	
</c:forEach>

<script type="text/JavaScript" language="JavaScript">
function updateVersionSelection(vsIds, index, count) {
	var s = "vs"+count;
	var mvs = document.getElementById(s);
	if(vsIds.length>0) {
		for(i=0; i<mvs.length; ++i) {
			var t = "," + mvs.options[i].value + ",";
			if(vsIds.indexOf(t)>= 0 ) {
				mvs.options[i].selected = true;
			} else {
				mvs.options[i].selected = false;
			}
		}
		mvs.options[index].selected = true;
	} else {
		for(i=0; i<mvs.length; ++i) {
			mvs.options[i].selected = true;
		}
	}
}
//make sure current chosen default version among those selected versions.
function updateThis(multiSelEle, count) {
	var s = "dv"+count;
	var currentDefault = document.getElementById(s);
	for(i=0; i<multiSelEle.length; ++i) {
		if(multiSelEle.options[i].value == currentDefault.options[currentDefault.selectedIndex].value) {
			multiSelEle.options[i].selected = true;
		}
	}
}
    function leftnavExpand(strLeftNavRowElementName){
      var objLeftNavRowElement;

      objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
      if (objLeftNavRowElement != null) {
        if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
          objLeftNavRowElement.display = (objLeftNavRowElement.display == "none" ) ? "" : "none";
          objExCl = MM_findObj("excl_"+strLeftNavRowElementName);
          if(objLeftNavRowElement.display == "none"){
              objExCl.src = "images/bt_Expand.gif";
          }else{
              objExCl.src = "images/bt_Collapse.gif";
          }
        }
      }
</script>

<h1><span class="title_manage">
<fmt:message key="update_site_details" bundle="${resword}"/>: <c:out value="${newStudy.name}"/>
</span></h1>
<br><br>

<jsp:include page="../include/alertbox.jsp" />
<form action="UpdateSubStudy" method="post">
<input type="hidden" name="action" value="confirm">

<c:set var="defCount" value="0"/>
<c:forEach var="def" items="${definitions}">
	<c:set var="defCount" value="${defCount+1}"/>
	<c:choose>
	<c:when test="${def.status.id==5 || def.status.id==7}">
	&nbsp&nbsp&nbsp&nbsp<b><img name="ExpandGroup3" src="images/bt_Collapse.gif" border="0"> <c:out value="${def.name}"/> &nbsp&nbsp&nbsp&nbsp (<fmt:message key="this_removed" bundle="${resword}"/>)</b>
	</c:when>
	<c:otherwise>
	&nbsp&nbsp&nbsp&nbsp<b><a href="javascript:leftnavExpand('sed<c:out value="${defCount}"/>');">
    <img id="excl_sed<c:out value="${defCount}"/>" src="images/bt_Expand.gif" border="0" width="12px"> <c:out value="${def.name}"/></b></a>
	</c:otherwise>
	</c:choose>
	
	<c:choose>
	<c:when test="${def.populated ==true}">
    	<div id="sed<c:out value="${defCount}"/>" style="display: all">
	</c:when>
	<c:otherwise>
    	<div id="sed<c:out value="${defCount}"/>" style="display: none">	
	</c:otherwise>
	</c:choose>

	<!-- These DIVs define shaded box borders -->
 	<div style="width: 100%">
	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
	<div class="textbox_center" style="display: none;">
	<table border="0" cellpadding="0" cellspacing="0">
		<tr><td class="table_header_column" colspan="3">Name</td><td><c:out value="${def.name}"/></td></tr>
		<tr><td class="table_header_column" colspan="3">Description</td><td><c:out value="${def.description}"/></td></tr>
	</table>
	</div>
  	</div></div></div></div></div></div></div></div>
	</div>

	<div class="table_title_manage"><fmt:message key="CRFs" bundle="${resword}"/></div>
	<div style="width: 100%">
	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B">
	<div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
	<div class="textbox_center">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<c:set var="count" value="0"/>
		<c:forEach var="edc" items="${def.crfs}">
		<c:set var="num" value="${count}-${edc.id}" />
		<tr valign="top" bgcolor="#F5F5F5">
		    <td class="table_header_column" colspan="10"><c:out value="${edc.crfName}"/></td>
		</tr>

		<c:if test="${edc.status.id==1}">
		<c:choose>
	    <c:when test="${fn:length(edc.selectedVersionIds)>0}">
			<c:set var="idList" value="${edc.selectedVersionIdList}"/>
			<c:set var="selectedIds" value=",${edc.selectedVersionIds},"/>
	    </c:when>
	    <c:otherwise>
			<c:set var="idList" value=""/>
			<c:set var="selectedIds" value=""/>
		</c:otherwise>
		</c:choose>
		<tr valign="top">
	    	<td class="table_cell"><fmt:message key="required" bundle="${resword}"/>:
		    <c:choose>
	            <c:when test="${edc.requiredCRF == true}">
	                <input type="checkbox" checked name="requiredCRF<c:out value="${num}"/>" value="yes">
	            </c:when>
	            <c:otherwise>
	                <input type="checkbox" name="requiredCRF<c:out value="${num}"/>" value="yes">
	            </c:otherwise>
	        </c:choose>
	    	</td>
	    	<td></td>

		    <td class="table_cell" colspan="2"><fmt:message key="default_version" bundle="${resword}"/>:
		    <select name="defaultVersionId<c:out value="${num}"/>" id="dv<c:out value="${num}"/>" onclick="updateVersionSelection('<c:out value="${selectedIds}"/>',document.getElementById('dv<c:out value="${num}"/>').selectedIndex, '<c:out value="${num}"/>')">
	            <c:forEach var="version" items="${edc.versions}">
	            <c:choose>
	            <c:when test="${edc.defaultVersionId == version.id}">
	            <option value="<c:out value="${version.id}"/>" selected><c:out value="${version.name}"/>
	                </c:when>
	                <c:otherwise>
	            <option value="<c:out value="${version.id}"/>"><c:out value="${version.name}"/>
	                </c:otherwise>
	                </c:choose>
	                </c:forEach>
        	</select>
		    </td>
		</tr>
		<tr valign="top">
		    <td class="table_cell" colspan="3"><fmt:message key="version_selection" bundle="${resword}"/>&nbsp:
		    <select multiple name="versionSelection<c:out value="${num}"/>" id="vs<c:out value="${num}"/>" onclick="updateThis(document.getElementById('vs<c:out value="${num}"/>'), '<c:out value="${num}"/>')" size="${fn:length(edc.versions)}">
	            <c:forEach var="version" items="${edc.versions}">
	            	<c:choose>
		            <c:when test="${fn:length(idList) > 0}">
		            	<c:set var="versionid" value=",${version.id},"/>
			            <c:choose>
		            	<c:when test="${version.id == defaultVersionId}">
		            		<option value="<c:out value="${version.id}"/>" selected><c:out value="${version.name}"/>
		            	</c:when>
		            	<c:otherwise>
		            		<c:choose>
					        <c:when test="${fn:contains(selectedIds,versionid)}">
					            <option value="<c:out value="${version.id}"/>" selected><c:out value="${version.name}"/>
					        </c:when>
					        <c:otherwise>
					            <option value="<c:out value="${version.id}"/>"><c:out value="${version.name}"/>
					        </c:otherwise>
					        </c:choose>
					    </c:otherwise>
				        </c:choose>
				    </c:when>
		            <c:otherwise>
		            	<option value="<c:out value="${version.id}"/>" selected><c:out value="${version.name}"/>
			        </c:otherwise>
			        </c:choose>
	            </c:forEach>
        	</select>
		    </td>

		    <td class="table_cell" colspan="1"><fmt:message key="hidden_crf" bundle="${resword}"/> :
		    <c:choose>
	            <c:when test="${!edc.hideCrf}">
	                <input type="checkbox" name="hideCRF<c:out value="${num}"/>" value="yes">
	            </c:when>
	            <c:otherwise><input checked="checked" type="checkbox" name="hideCRF<c:out value="${num}"/>" value="yes"></c:otherwise>
	        </c:choose>
		    </td>

		    <td class="table_cell" colspan="6" ><fmt:message key="sdv_option" bundle="${resword}"/>:
		    <select name="sdvOption<c:out value="${num}"/>">
	            <c:set var="index" value="1"/>
	            <c:forEach var="sdv" items="${sdvOptions}">
	            	<c:choose>
	            	<c:when test="${edc.sourceDataVerification.code == index}">
	            		<option value="${index}" selected><c:out value="${sdv}"/>
	                </c:when>
	                <c:otherwise>
	            		<option value="${index}"><c:out value="${sdv}"/>
	                </c:otherwise>
	                </c:choose>
	            	<c:set var="index" value="${index+1}"/>
	            </c:forEach>
        	</select>
		    </td>
		</tr>

   <c:choose>
    <c:when test="${participateFormStatus == 'enabled' && edc.participantForm == true}">

				<tr valign="top">		
        <td class="table_cell" colspan="2">
        <fmt:message key="participant_form" bundle="${resword}"/>:
        <c:choose>
            <c:when test="${edc.participantForm == true}">
                <input type="checkbox" disabled checked name="participantForm<c:out value="${num}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <input type="checkbox" disabled name="participantForm<c:out value="${num}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>
         </c:when>  
 </c:choose>
   <c:choose>
    <c:when test="${participateFormStatus == 'enabled' && edc.participantForm == true && edc.allowAnonymousSubmission == true}">
    
        <td class="table_cell" colspan="2">
        <fmt:message key="allow_anonymous_submission" bundle="${resword}"/>:
        <c:choose>
            <c:when test="${edc.allowAnonymousSubmission == true}">
                <input type="checkbox" disabled checked name="allowAnonymousSubmission<c:out value="${num}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <input type="checkbox" disabled name="allowAnonymousSubmission<c:out value="${num}"/>" value="yes">
            </c:otherwise>
        </c:choose>
            <td class="table_cell" colspan="6">
        <fmt:message key="submission_url" bundle="${resword}"/>:  ${participantUrl}
                <input type="text"  name="submissionUrl<c:out value="${num}"/>" value="${edc.submissionUrl}"/>
          <c:set var="summary" value="submissionUrl${num}"/>
          <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="${summary}"/></jsp:include>
                          <br />
                <c:choose>
                  <c:when test="${edc.allowAnonymousSubmission == true && def.repeating == true  && edc.offline == true}">
                <fmt:message key="offline" bundle="${resword}"/>:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;           
                    <input type="checkbox" name="offline<c:out value="${count}"/>" value="yes" disabled checked>
                  </c:when>
                  <c:when test="${edc.allowAnonymousSubmission == true && def.repeating == true  && edc.offline == false}">
                <fmt:message key="offline" bundle="${resword}"/>:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;           
                    <input type="checkbox" name="offline<c:out value="${count}"/>" value="yes" disabled>
                  </c:when>
                </c:choose>
          
        </td>
     </c:when>  
            <c:otherwise>
        <td class="table_cell" colspan="8"> </td>
            </c:otherwise>
 </c:choose>
  
</tr>
				
		<c:set var="count" value="${count+1}"/>
		</c:if>
		<tr><td class="table_divider" colspan="8">&nbsp;</td></tr>
		</c:forEach>
	</table>
	</div>
  	</div></div></div></div></div></div></div></div>
	</div>
	</div><br>
</c:forEach>

<br><br>
  <table border="0" cellpadding="0" cellspacing="0">
  <tr><td><input type="submit" name="save" value="<fmt:message key="save" bundle="${resword}"/>" class="button_long">
    </td><td>
  <input type="button" onclick="confirmCancel('ListSite');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
	</td></tr></table>
</form>
<DIV ID="testdiv1" STYLE="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></DIV>
<jsp:include page="../include/footer.jsp"/>
