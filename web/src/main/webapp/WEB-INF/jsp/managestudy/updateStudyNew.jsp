<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.admin" var="resadmin"/>

<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<c:choose>
	<c:when test="${userRole.role.id > 3}">
		<jsp:include page="../include/home-header.jsp"/>
	</c:when>
	<c:otherwise>
	
		<jsp:include page="../include/admin-header.jsp"/>
	</c:otherwise>
</c:choose>

  <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery-1.9.1.min.js"></script>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<jsp:useBean scope="request" id="facRecruitStatusMap" class="java.util.HashMap"/>
<jsp:useBean scope="request" id="statuses" class="java.util.ArrayList"/>
<jsp:useBean scope ="request" id="studyPhaseMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="studyTypes" class="java.util.ArrayList"/>

<jsp:useBean scope ="request" id="interPurposeMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="allocationMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="maskingMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="controlMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="assignmentMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="endpointMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="interTypeMap" class="java.util.HashMap"/>
<jsp:useBean scope ="request" id="interventions" class="java.util.ArrayList"/>
<jsp:useBean scope ="request" id="interventionError" class="java.lang.String"/>

<jsp:useBean scope="request" id="obserPurposeMap" class ="java.util.HashMap"/>
<jsp:useBean scope="request" id="durationMap" class ="java.util.HashMap"/>
<jsp:useBean scope="request" id="selectionMap" class ="java.util.HashMap"/>
<jsp:useBean scope="request" id="timingMap" class ="java.util.HashMap"/>
<jsp:useBean scope="request" id="isInterventional" class ="java.lang.String"/>
<jsp:useBean scope="request" id="studyId" class ="java.lang.String"/>



<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">
		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>
<jsp:useBean scope='request' id='parentStudy' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='request' id='studyToView' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='request' id='sitesToView' class='java.util.ArrayList'/>





<script language="JavaScript">
       <!--
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
       
           function registerPManage(event){
               var regURL = 'pages/pmanage/regSubmit?studyoid=' + "${studyToView.oid}";
               jQuery.ajax({
        	       type:'GET',
        	       url: regURL,
        	       success: function(data){
                       jQuery('#pManageDiv').html('Registration: ' + data);
        	    }});
           }
           
           function togglePManage(show){
        	   if (show) jQuery('#pManageDiv').show();
        	   else jQuery('#pManageDiv').hide();
           }
       //-->
 </script>

<h1><span class="title_manage">
<fmt:message key="update_study_details" bundle="${resword}"/> <c:out value="${studyToView.name}"/>
</span></h1>
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


<br><br>

<br>
<form action="UpdateStudyNew" method="post">
<input type=hidden name="action" value="submit">
<input type=hidden name="studyId" value="<c:out value="${studyId}"/>">
<a href="javascript:leftnavExpand('sectiona');>
    <img id="excl_sectiona" src="images/bt_Collapse.gif" border="0"> <span class="table_title_Admin">
    <fmt:message key="study_description_status" bundle="${resword}"/>  </span></a>
<div id="sectiona" style="display: ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
    <div class="textbox_center">
        <table border="0" cellpadding="0" cellspacing="0" width="450">
          <tr valign="top">
              <td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId'); return false;"><b><fmt:message key="unique_protocol_ID" bundle="${resword}"/></b>:</a></td><td><div class="formfieldXL_BG">
          <input type="text" name="uniqueProId" value="<c:out value="${studyToView.identifier}"/>" class="formfieldXL"></div>
          <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="uniqueProId"/></jsp:include></td>
              <td width="10%" >*</td></tr>

          <tr valign="top"><td class="formlabel"><b><fmt:message key="brief_title" bundle="${resword}"/></b>:</td><td><div class="formfieldXL_BG">
          <input type="text" name="name" value="<c:out value="${studyToView.name}"/>" class="formfieldXL"></div>
          <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="name"/></jsp:include></td><td>*</td></tr>

          <tr valign="top"><td class="formlabel"><b><fmt:message key="official_title" bundle="${resword}"/></b>:</td><td><div class="formfieldXL_BG">
          <input type="text" name="officialTitle" value="<c:out value="${studyToView.officialTitle}"/>" class="formfieldXL"></div>
           <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="officialTitle"/></jsp:include>
          </td>
          <td>&nbsp;</td>
          </tr>

          <c:choose>
           <c:when test="${studyToView.parentStudyId == 0}">
              <c:set var="key" value="study_system_status"/>
           </c:when>
           <c:otherwise>
               <c:set var="key" value="site_system_status"/>
           </c:otherwise>
          </c:choose>
          <tr valign="top">
              <td class="formlabel"><fmt:message key="${key}" bundle="${resword}"/>:</td>
              <td><div class="formfieldXL_BG">
           <c:set var="dis" value="${parentStudy.name!='' && !parentStudy.status.available}"/>
           <c:set var="status1" value="${studyToView.status.id}"/>
          <select class="formfieldXL" name="statusId" disabled="true">
            <c:forEach var="status" items="${statuses}">
             <c:choose>
              <c:when test="${status1 == status.id}">
               <option value="<c:out value="${status.id}"/>" selected><c:out value="${status.name}"/>
              </c:when>
              <c:otherwise>
               <option value="<c:out value="${status.id}"/>"><c:out value="${status.name}"/>
              </c:otherwise>
             </c:choose>
           </c:forEach>
           </select>
           <input type=hidden name="status" value="${status1}">

                  <%--<select name="statusId" class="formfieldXL" disabled="true">--%>
              <%--<c:forEach var="status" items="${statuses}">--%>
               <%--<c:choose>--%>
                <%--<c:when test="${status1 == status.id}">--%>
                 <%--<option value="<c:out value="${status.id}"/>" selected>--%>
                     <%--<c:if test="${status.id == 4}">--%>
                         <%--<fmt:message key="design" bundle="${resword}"/>--%>
                     <%--</c:if>--%>
                     <%--<c:if test="${status.id != 4}">--%>
                         <%--<c:out value="${status.name}"/>--%>
                     <%--</c:if>--%>
                <%--</c:when>--%>
                <%--<c:otherwise>--%>
                 <%--<option value="<c:out value="${status.id}"/>">--%>
                      <%--<c:if test="${status.id == 4}">--%>
                          <%--<fmt:message key="design" bundle="${resword}"/>--%>
                      <%--</c:if>--%>
                      <%--<c:if test="${status.id != 4}">--%>
                          <%--<c:out value="${status.name}"/>--%>
                      <%--</c:if>--%>
                <%--</c:otherwise>--%>
               <%--</c:choose>--%>
            <%--</c:forEach>--%>
           <%--</select>--%>

              </div>
          </td><td>*</td></tr>
          <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#SecondaryIds" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#SecondaryIds'); return false;"><b><fmt:message key="secondary_IDs" bundle="${resword}"/></b>:</a><br>(<fmt:message key="separate_by_commas" bundle="${resword}"/>)</td>
          <td> <div class="formtextareaXL4_BG">
           <textarea class="formtextareaXL4" name="secondProId" rows="4" cols="50"><c:out value="${studyToView.secondaryIdentifier}"/></textarea></div>
           <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="secondProId"/></jsp:include>
          </td>
          <td>&nbsp;</td>
          </tr>

          <tr valign="top"><td class="formlabel"><fmt:message key="principal_investigator" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
          <input type="text" name="prinInvestigator" value="<c:out value="${studyToView.principalInvestigator}"/>" class="formfieldXL"></div>
          <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="prinInvestigator"/></jsp:include></td><td>*</td></tr>

            <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#BriefSummary" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#BriefSummary'); return false;"><fmt:message key="brief_summary" bundle="${resword}"/>:</a></td><td><div class="formtextareaXL4_BG">
            <textarea class="formtextareaXL4" name="description" rows="4" cols="50"><c:out value="${studyToView.summary}"/></textarea></div>
            <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="description"/></jsp:include></td><td >*</td></tr>

             <tr valign="top"><td class="formlabel"><fmt:message key="detailed_description" bundle="${resword}"/>:</td><td>
             <div class="formtextareaXL4_BG"><textarea class="formtextareaXL4" name="protocolDescription" rows="4" cols="50"><c:out value="${studyToView.protocolDescription}"/></textarea></div>
              <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="protocolDescription"/></jsp:include>
            </td>
             <td>&nbsp;</td>
             </tr>
            <tr valign="top"><td class="formlabel"><fmt:message key="sponsor" bundle="${resword}"/>:</td><td>
            <div class="formfieldXL_BG"><input type="text" name="sponsor" value="<c:out value="${studyToView.sponsor}"/>" class="formfieldXL"></div>
            <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="sponsor"/></jsp:include></td><td >*</td></tr>

            <tr valign="top"><td class="formlabel"><fmt:message key="collaborators" bundle="${resword}"/>:<br>(<fmt:message key="separate_by_commas" bundle="${resword}"/>)</td><td>
            <div class="formtextareaXL4_BG"><textarea class="formtextareaXL4" name="collaborators" rows="4" cols="50"><c:out value="${studyToView.collaborators}"/></textarea></div>
             <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="collaborators"/></jsp:include>
            </td>
            <td>&nbsp;</td>
            </tr>

            <!-- section B-->
          <tr valign="top"><td class="formlabel"><fmt:message key="study_phase" bundle="${resword}"/>:</td><td>
           <c:set var="phase1" value="${studyToView.phase}"/>
           <div class="formfieldXL_BG"><select name="phase" class="formfieldXL">
            <c:forEach var="phase" items="${studyPhaseMap}">
                 <c:set var="phasekey">
                     <fmt:message key="${phase.key}" bundle="${resadmin}"/>
                 </c:set>
                <c:choose>
                 <c:when test="${phase1 == phasekey}">
                  <option value="<c:out value="${phase.key}"/>" selected><c:out value="${phase.value}"/>
                 </c:when>
                 <c:otherwise>
                  <option value="<c:out value="${phase.key}"/>"><c:out value="${phase.value}"/>
                 </c:otherwise>
                </c:choose>
             </c:forEach>
           </select></div>
           </td><td>*</td></tr>

           <tr valign="top"><td class="formlabel"><fmt:message key="protocol_type" bundle="${resword}"/>:</td><td>
           <c:set var="type1" value="observational"/>
           <c:choose>
            <c:when test="${studyToView.protocolTypeKey == type1}">
             <input type="radio" checked name="protocolType" value="observational" disabled><fmt:message key="observational" bundle="${resword}"/>
            </c:when>
            <c:otherwise>
             <input type="radio" checked name="protocolType" value="interventional" disabled><fmt:message key="interventional" bundle="${resword}"/>
            </c:otherwise>
           </c:choose>
           </td>
           <td>&nbsp;</td>
           </tr>



           <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#VerificationDate" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#VerificationDate'); return false;"><fmt:message key="protocol_verification" bundle="${resword}"/>:</a></td><td>

           <div class="formfieldXL_BG">
               <input type="text" name="protocolDateVerification" value="<c:out value="${protocolDateVerification}"/>" class="formfieldM" id="protocolDateVerificationField"></div>
           <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="protocolDateVerification"/></jsp:include></td>
           <td><A HREF="#" >
               <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="protocolDateVerificationTrigger"/>
               <script type="text/javascript">
               Calendar.setup({inputField  : "protocolDateVerificationField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "protocolDateVerificationTrigger" });
               </script>

           </a></td></tr>

           <tr valign="top"><td class="formlabel"><fmt:message key="study_start_date" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">

           <input type="text" name="startDate" value="<c:out value="${startDate}" />" class="formfieldXL" id="startDateField"></div>
           <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="startDate"/></jsp:include>
           </td>
           <td>
           <A HREF="#" >
               <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="startDateTrigger"/>
               <script type="text/javascript">
               Calendar.setup({inputField  : "startDateField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "startDateTrigger" });
               </script>

           </a>
               *</td></tr>

           <tr valign="top"><td class="formlabel"><fmt:message key="study_completion_date" bundle="${resword}"/>:</td>
               <td><div class="formfieldXL_BG">
           <input type="text" name="endDate" value="<c:out value="${endDate}" />" class="formfieldXL" id="endDateField"></div>
           <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="endDate"/></jsp:include></td>
           <td>
               <A HREF="#">
                   <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="endDateTrigger"/>
                   <script type="text/javascript">
                   Calendar.setup({inputField  : "endDateField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "endDateTrigger" });
                   </script>

               </a>
           </td></tr>

             <!-- From Update Page 3 -->

          <c:if test='${isInterventional==1}'>
          <tr valign="top">
              <td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#IntPurpose" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#IntPurpose'); return false;">
              <fmt:message key="purpose" bundle="${resword}"/></a>:</td>
              <td>
                <c:set var="purpose1" value="${studyToView.purpose}"/>
                <div class="formfieldXL_BG"><select name="purpose" class="formfieldXL">
                <c:forEach var="purpose" items="${interPurposeMap}">
                    <c:set var="purposekey">
                        <fmt:message key="${purpose.key}" bundle="${resadmin}"/>
                    </c:set>
                    <c:choose>
                        <c:when test="${purpose1 == purposekey}">
                        <option value="<c:out value="${purpose.key}"/>" selected><c:out value="${purpose.value}"/>
                        </c:when>
                        <c:otherwise>
                        <option value="<c:out value="${purposekey}"/>"><c:out value="${purpose.value}"/>
                        </c:otherwise>
                    </c:choose>
                    </c:forEach>
                </select></div>
          <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="purpose"/></jsp:include></td>
          </tr>
          <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#IntAllocation" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#IntAllocation'); return false;">
          <fmt:message key="allocation" bundle="${resword}"/></a>:</td><td>
           <c:set var="allocation1" value="${studyToView.allocation}"/>
          <div class="formfieldXL_BG"><select name="allocation" class="formfieldXL">
           <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
           <c:forEach var="allocation" items="${allocationMap}">
               <c:set var="allocationkey">
                <fmt:message key="${allocation.key}" bundle="${resadmin}"/>
               </c:set>
               <c:choose>
                <c:when test="${allocation1 == allocationkey}">
                 <option value="<c:out value="${allocation.key}"/>" selected><c:out value="${allocation.value}"/>
                </c:when>
                <c:otherwise>
                 <option value="<c:out value="${allocation.key}"/>"><c:out value="${allocation.value}"/>
                </c:otherwise>
               </c:choose>
            </c:forEach>
          </select></div>
          </td></tr>

          <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#IntMasking" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#IntMasking'); return false;">
              <fmt:message key="masking" bundle="${resword}"/></a>:</td><td>
          <c:set var="masking1" value="${studyToView.masking}"/>
          <div class="formfieldXL_BG"><select name="masking" class="formfieldXL">
           <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
           <c:forEach var="masking" items="${maskingMap}">
               <c:set var="maskingkey">
                <fmt:message key="${masking.key}" bundle="${resadmin}"/>
               </c:set>

               <c:choose>
                <c:when test="${masking1 == maskingkey}">
                 <option value="<c:out value="${masking.key}"/>" selected><c:out value="${masking.value}"/>
                </c:when>
                <c:otherwise>
                 <option value="<c:out value="${masking.key}"/>"><c:out value="${masking.value}"/>
                </c:otherwise>
               </c:choose>
            </c:forEach>
          </select></div>
          </td></tr>
          <tr valign="top"><td class="formlabel"><fmt:message key="control" bundle="${resword}"/>:</td><td>
             <c:set var="control1" value="${studyToView.control}"/>
             <div class="formfieldXL_BG"><select name="control" class="formfieldXL">
             <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
              <c:forEach var="control" items="${controlMap}">
                 <c:set var="controlkey">
                  <fmt:message key="${control.key}" bundle="${resadmin}"/>
                 </c:set>
                 <c:choose>
                  <c:when test="${control1 == controlkey}">
                   <option value="<c:out value="${control.key}"/>" selected><c:out value="${control.value}"/>
                  </c:when>
                  <c:otherwise>
                   <option value="<c:out value="${control.key}"/>"><c:out value="${control.value}"/>
                  </c:otherwise>
                 </c:choose>
              </c:forEach>
            </select></div>
           </td></tr>
           <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#IntDesign" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#IntDesign'); return false;">
           <fmt:message key="intervention_model" bundle="${resword}"/></a>:</td><td>
			<%-- was assignment, tbh --%>
           <c:set var="assignment1" value="${studyToView.assignment}"/>
            <div class="formfieldXL_BG"><select name="assignment" class="formfieldXL">
            <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
             <c:forEach var="assignment" items="${assignmentMap}">
                <c:set var="assignmentkey">
                 <fmt:message key="${assignment.key}" bundle="${resadmin}"/>
                </c:set>

                <c:choose>
                 <c:when test="${assignment1 == assignmentkey}">
                  <option value="<c:out value="${assignment.key}"/>" selected><c:out value="${assignment.value}"/>
                 </c:when>
                 <c:otherwise>
                  <option value="<c:out value="${assignment.key}"/>"><c:out value="${assignment.value}"/>
                 </c:otherwise>
                </c:choose>
             </c:forEach>
           </select></div>
           </td></tr>
          <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#IntEndpoints" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#IntEndpoints'); return false;">
          <fmt:message key="study_classification" bundle="${resword}"/></a>:</td><td>
			<%-- was endpoint, tbh --%>
           <c:set var="endpoint1" value="${studyToView.endpoint}"/>
           <div class="formfieldXL_BG"><select name="endpoint" class="formfieldXL">
           <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
            <c:forEach var="endpoint" items="${endpointMap}">
               <c:set var="endpointkey">
                <fmt:message key="${endpoint.key}" bundle="${resadmin}"/>
               </c:set>

               <c:choose>
                <c:when test="${endpoint1 == endpointkey}">
                 <option value="<c:out value="${endpoint.key}"/>" selected><c:out value="${endpoint.value}"/>
                </c:when>
                <c:otherwise>
                 <option value="<c:out value="${endpoint.key}"/>"><c:out value="${endpoint.value}"/>
                </c:otherwise>
               </c:choose>
            </c:forEach>
          </select></div>
          </td></tr>

          </c:if>
          <c:if test="${isInterventional==0}">
             <!-- End From Update Page 3 -->
             <!-- condition for isInterventional should be applied -->
             <!-- From Update Page 4 -->
          <tr valign="bottom"><td class="formlabel"><fmt:message key="purpose" bundle="${resword}"/>:</td><td>
           <c:set var="purpose1" value="${studyToView.purpose}"/>
          <div class="formfieldXL_BG"><select name="purpose" class="formfieldXL">
           <c:forEach var="purpose" items="${obserPurposeMap}">
           <c:set var="purposekey">
           <fmt:message key="${purpose.key}" bundle="${resadmin}"/>
           </c:set>

               <c:choose>
                <c:when test="${purpose1 == purposekey}">
                 <option value="<c:out value="${purpose.key}"/>" selected><c:out value="${purpose.value}"/>
                </c:when>
                <c:otherwise>
                 <option value="<c:out value="${purpose.key}"/>"><c:out value="${purpose.value}"/>
                </c:otherwise>
               </c:choose>
            </c:forEach>
          </select></div>
          <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="purpose"/></jsp:include></td>
          <td valign="top">*</td></tr>

          <tr valign="bottom"><td class="formlabel"><fmt:message key="duration" bundle="${resword}"/>:</td><td>
           <c:set var="longitudinal">
            <fmt:message key="longitudinal" bundle="${resadmin}"/>
           </c:set>

          <c:choose>
           <c:when test="${studyToView.duration ==longitudinal}">
            <input type="radio" checked name="duration" value="longitudinal"><fmt:message key="longitudinal" bundle="${resword}"/>:
            <input type="radio" name="duration" value="cross-sectional"><fmt:message key="cross_sectional" bundle="${resword}"/>:
           </c:when>
           <c:otherwise>
            <input type="radio" name="duration" value="longitudinal"><fmt:message key="longitudinal" bundle="${resword}"/>:
            <input type="radio" checked name="duration" value="cross-sectional"><fmt:message key="cross_sectional" bundle="${resword}"/>:
           </c:otherwise>
          </c:choose>
          </td>
          <td>&nbsp;</td>
          </tr>

          <tr valign="bottom"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#EligibilitySamplingMethod" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#EligibilitySamplingMethod'); return false;">
          <fmt:message key="selection" bundle="${resword}"/></a></td><td>
          <c:set var="selection1" value="${studyToView.selection}"/>
          <div class="formfieldXL_BG">
          <select name="selection" class="formfieldXL">
           <c:forEach var="selection" items="${selectionMap}">
           <c:set var="selectionkey">
            <fmt:message key="${selection.key}" bundle="${resadmin}"/>
           </c:set>

               <c:choose>
                <c:when test="${selection1 == selectionkey}">
                 <option value="<c:out value="${selection.key}"/>" selected><c:out value="${selection.value}"/>
                </c:when>
                <c:otherwise>
                 <option value="<c:out value="${selection.key}"/>"><c:out value="${selection.value}"/>
                </c:otherwise>
               </c:choose>
            </c:forEach>
          </select></div>
          </td>
          <td>&nbsp;</td>
          </tr>

          <tr valign="bottom"><td class="formlabel"><fmt:message key="timing" bundle="${resword}"/></td><td>
           <c:set var="timing1" value="${studyToView.timing}"/>
           <div class="formfieldXL_BG">
           <select name="timing" class="formfieldXL">
            <c:forEach var="timing" items="${timingMap}">
           <c:set var="timingkey">
            <fmt:message key="${timing.key}" bundle="${resadmin}"/>
           </c:set>
               <c:choose>
                <c:when test="${timing1 == timingkey}">
                 <option value="<c:out value="${timing.key}"/>" selected><c:out value="${timing.value}"/>
                </c:when>
                <c:otherwise>
                 <option value="<c:out value="${timing.key}"/>"><c:out value="${timing.value}"/>
                </c:otherwise>
               </c:choose>
             </c:forEach>
           </select></div>
          </td>
          <td>&nbsp;</td>
          </tr>
          <!-- End of the condition -->
            <!-- End From Update Page 4 -->
         </c:if>


          </table>
          </div>
          </div></div></div></div></div></div></div></div>

          </div>
</div></div></div></div></div></div></div></div>
</div>
</div>
<br>

<div style="font-family: Tahoma, Arial, Helvetica, Sans-Serif;font-size:17px;">

    <fmt:message key="expand_each_section" bundle="${restext}"/>
</div>
    <br>

<a href="javascript:leftnavExpand('sectionc');">
	<img id="excl_sectionc" src="images/bt_Expand.gif" border="0"> <span class="table_title_Admin">
         <fmt:message key="conditions_and_eligibility" bundle="${resword}"/></span></a>
<div id="sectionc" style="display:none ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
    <div class="textbox_center">
    <table border="0" cellpadding="0">

      <tr valign="top"><td class="formlabel"><fmt:message key="conditions" bundle="${resword}"/>:</td><td>
      <div class="formfieldXL_BG"><input type="text" name="conditions" value="<c:out value="${studyToView.conditions}"/>" class="formfieldXL">
      </div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="conditions"/></jsp:include>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="keywords" bundle="${resword}"/>:<br>(<fmt:message key="separate_by_commas" bundle="${resword}"/>)</td><td>
       <div class="formtextareaXL4_BG"><textarea name="keywords" rows="4" cols="50"  class="formtextareaXL4"><c:out value="${studyToView.keywords}"/></textarea>
       </div>
        <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="keywords"/></jsp:include>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="eligibility_criteria" bundle="${resword}"/>:</td><td>
       <div class="formtextareaXL4_BG"><textarea name="eligibility" rows="4" cols="50" class="formtextareaXL4"><c:out value="${studyToView.eligibility}"/></textarea>
       </div>
       <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="eligibility"/></jsp:include>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="gender" bundle="${resword}"/>:</td><td>
      <div class="formfieldXL_BG"><select name="gender" class="formfieldXL">
      <c:set var="female">
          <fmt:message key="female" bundle="${resword}"/>
      </c:set>
      <c:set var="male">
          <fmt:message key="male" bundle="${resword}"/>
      </c:set>

      <c:choose>
       <c:when test="${studyToView.gender == female}">
        <option value="both"><fmt:message key="both" bundle="${resword}"/></option>
        <option value="male"><fmt:message key="male" bundle="${resword}"/></option>
        <option value="female" selected><fmt:message key="female" bundle="${resword}"/></option>
       </c:when>
       <c:when test="${studyToView.gender == male}">
        <option value="both"><fmt:message key="both" bundle="${resword}"/></option>
        <option value="male" selected><fmt:message key="male" bundle="${resword}"/></option>
        <option value="female"><fmt:message key="female" bundle="${resword}"/></option>
       </c:when>
       <c:otherwise>
        <option value="both" selected><c:out value="${studyToView.gender}"/></option>
        <option value="male"><fmt:message key="male" bundle="${resword}"/></option>
        <option value="female"><fmt:message key="female" bundle="${resword}"/></option>
       </c:otherwise>

      </c:choose>
      </select></div>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="minimum_age" bundle="${resword}"/>:</td><td>
      <div class="formfieldXL_BG"><input type="text" name="ageMin" value="<c:out value="${studyToView.ageMin}"/>" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="ageMin"/></jsp:include></td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="maximum_age" bundle="${resword}"/>:</td><td>
      <div class="formfieldXL_BG"><input type="text" name="ageMax" value="<c:out value="${studyToView.ageMax}"/>" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="ageMax"/></jsp:include></td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="healthy_volunteers_accepted" bundle="${resword}"/>:</td><td>
       <div class="formfieldXL_BG"><select name="healthyVolunteerAccepted" class="formfieldXL">
        <c:choose>
         <c:when test="${studyToView.healthyVolunteerAccepted == true}">
          <option value="1" selected><fmt:message key="yes" bundle="${resword}"/></option>
          <option value="0"><fmt:message key="no" bundle="${resword}"/></option>
         </c:when>
         <c:otherwise>
          <option value="1"><fmt:message key="yes" bundle="${resword}"/></option>
          <option value="0" selected><fmt:message key="no" bundle="${resword}"/></option>
         </c:otherwise>
        </c:choose>
       </select></div>
      </td></tr>


      <tr valign="top"><td class="formlabel"><fmt:message key="expected_total_enrollment" bundle="${resword}"/>:</td><td>
      <div class="formfieldXL_BG"><input type="text" name="expectedTotalEnrollment" value="<c:out value="${studyToView.expectedTotalEnrollment}"/>" class="formfieldXL"">
       </div><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="expectedTotalEnrollment"/></jsp:include>
      </td><td>*</td></tr>

      </table>
    </div>
</div></div></div></div></div></div></div></div>
</div>
</div>
<br>

<a href="javascript:leftnavExpand('sectiond');">
	<img id="excl_sectiond" src="images/bt_Expand.gif" border="0">
    <span class="table_title_Admin">
        <fmt:message key="facility_information" bundle="${resword}"/></span></a>
<div id="sectiond" style="display:none ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
    <div class="textbox_center">
    <table border="0" cellpadding="5">
    <tr valign="top"><td class="formlabel"><fmt:message key="facility_name" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
      <input type="text" name="facName" value="<c:out value="${studyToView.facilityName}"/>" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facName"/></jsp:include>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="facility_city" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
      <input type="text" name="facCity" value="<c:out value="${studyToView.facilityCity}"/>" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facCity"/></jsp:include>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="facility_state_province" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
      <input type="text" name="facState" value="<c:out value="${studyToView.facilityState}"/>" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facState"/></jsp:include>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="postal_code" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
      <input type="text" name="facZip" value="<c:out value="${studyToView.facilityZip}"/>" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facZip"/></jsp:include>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="facility_country" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
      <input type="text" name="facCountry" value="<c:out value="${studyToView.facilityCountry}"/>" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facCountry"/></jsp:include>
      </td></tr>

      <!--<tr valign="top"><td class="formlabel"><fmt:message key="facility_recruitment_status" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
      <c:set var="facStatus" value="${studyToView.facilityRecruitmentStatus}"/>
      <select class="formfieldXL" name="facRecStatus">
       <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
       <c:forEach var="recStatus" items="${facRecruitStatusMap}">
           <c:choose>
            <c:when test="${facStatus == recStatus.key}">
             <option value="<c:out value="${recStatus.key}"/>" selected><c:out value="${recStatus.value}"/>
            </c:when>
            <c:otherwise>
             <option value="<c:out value="${recStatus.key}"/>"><c:out value="${recStatus.value}"/>
            </c:otherwise>
           </c:choose>
        </c:forEach>
      </select> </div>
      </td></tr>
      -->
      <tr valign="top"><td class="formlabel"><fmt:message key="facility_contact_name" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
      <input type="text" name="facConName" value="<c:out value="${studyToView.facilityContactName}"/>" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facConName"/></jsp:include>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="facility_contact_degree" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
      <input type="text" name="facConDegree" value="<c:out value="${studyToView.facilityContactDegree}"/>" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facConDegree"/></jsp:include>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="facility_contact_phone" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
      <input type="text" name="facConPhone" value="<c:out value="${studyToView.facilityContactPhone}"/>" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facConPhone"/></jsp:include>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="facility_contact_email" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
      <input type="text" name="facConEmail" value="<c:out value="${studyToView.facilityContactEmail}"/>" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facConEmail"/></jsp:include></td></tr>


    </table>
    </div>
</div></div></div></div></div></div></div></div>
</div>
</div>
<br>


 <a href="javascript:leftnavExpand('sectione');">
 	<img id="excl_sectione" src="images/bt_Expand.gif" border="0"> <span class="table_title_Admin">
           <fmt:message key="related_infomation" bundle="${resword}"/></span></a>
<div id="sectione" style="display:none ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
    <div class="textbox_center">
    <table border="0" cellpadding="0">

      <tr valign="top"><td class="formlabel"><fmt:message key="MEDLINE_identifier" bundle="${resword}"/>:</td><td>
      <div class="formfieldXL_BG"><input type="text" name="medlineIdentifier" value="<c:out value="${studyToView.medlineIdentifier}"/>" class="formfieldXL"></div>
        <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="medlineIdentifier"/></jsp:include>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="results_reference" bundle="${resword}"/>:</td><td>
       <div class="formfieldS_BG"><select name="resultsReference" class="formfieldS">
        <c:choose>
         <c:when test="${studyToView.resultsReference == true}">
          <option value="1" selected><fmt:message key="yes" bundle="${resword}"/></option>
          <option value="0"><fmt:message key="no" bundle="${resword}"/></option>
         </c:when>
         <c:otherwise>
          <option value="1"><fmt:message key="yes" bundle="${resword}"/></option>
          <option value="0" selected><fmt:message key="no" bundle="${resword}"/></option>
         </c:otherwise>
        </c:choose>
       </select></div>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="URL_reference" bundle="${resword}"/></td><td>
      <div class="formfieldXL_BG"><input type="text" name="url" value="<c:out value="${studyToView.url}"/>" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="url"/></jsp:include>
      </td></tr>

      <tr valign="top"><td class="formlabel"><fmt:message key="URL_description" bundle="${resword}"/></td><td>
      <div class="formfieldXL_BG"><input type="text" name="urlDescription" value="<c:out value="${studyToView.urlDescription}"/>" class="formfieldXL"></div>
      <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="urlDescription"/></jsp:include>
      </td></tr>

    </table>
    </div>
</div></div></div></div></div></div></div></div>

</div>
</div>
<br>
<a href="javascript:leftnavExpand('sectionf');">
	<img id="excl_sectionf" src="images/bt_Expand.gif" border="0"> <span class="table_title_Admin">
    <fmt:message key="study_parameter_configuration" bundle="${resword}"/></span></a>
<div id="sectionf" style="display:none ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">

  <tr valign="top"><td class="formlabel"><fmt:message key="collect_subject_date_of_birth" bundle="${resword}"/>:</td><td>
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.collectDob == '1'}">
    <input type="radio" checked name="collectDob" value="1"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="collectDob" value="2"><fmt:message key="only_year_of_birth" bundle="${resword}"/>
     <input type="radio" name="collectDob" value="3"><fmt:message key="not_used" bundle="${resword}"/>
   </c:when>
   <c:when test="${studyToView.studyParameterConfig.collectDob == '2'}">
    <input type="radio" name="collectDob" value="1"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="collectDob" value="2"><fmt:message key="only_year_of_birth" bundle="${resword}"/>
     <input type="radio" name="collectDob" value="3"><fmt:message key="not_used" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <input type="radio" name="collectDob" value="1"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="collectDob" value="2"><fmt:message key="only_year_of_birth" bundle="${resword}"/>
    <input type="radio" checked name="collectDob" value="3"><fmt:message key="not_used" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="allow_discrepancy_management" bundle="${resword}"/>:</td><td>
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.discrepancyManagement == 'false'}">
    <input type="radio" name="discrepancyManagement" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="discrepancyManagement" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <input type="radio" checked name="discrepancyManagement" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="discrepancyManagement" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="gender_required" bundle="${resword}"/>:</td><td>
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.genderRequired == 'false'}">
    <input type="radio" name="genderRequired" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="genderRequired" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <input type="radio" checked name="genderRequired" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="genderRequired" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>
  <tr><td>&nbsp;</td></tr>
  <tr valign="top"><td class="formlabel"><fmt:message key="subject_person_ID_required" bundle="${resword}"/>:</td><td>
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.subjectPersonIdRequired == 'required'}">
    <input type="radio" checked name="subjectPersonIdRequired" value="required"><fmt:message key="required" bundle="${resword}"/>
    <input type="radio" name="subjectPersonIdRequired" value="optional"><fmt:message key="optional" bundle="${resword}"/>
    <input type="radio" name="subjectPersonIdRequired" value="not used"><fmt:message key="not_used" bundle="${resword}"/>
   </c:when>
    <c:when test="${studyToView.studyParameterConfig.subjectPersonIdRequired == 'optional'}">
    <input type="radio" name="subjectPersonIdRequired" value="required"><fmt:message key="required" bundle="${resword}"/>
    <input type="radio" checked name="subjectPersonIdRequired" value="optional"><fmt:message key="optional" bundle="${resword}"/>
    <input type="radio" name="subjectPersonIdRequired" value="not used"><fmt:message key="not_used" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <input type="radio" name="subjectPersonIdRequired" value="required"><fmt:message key="required" bundle="${resword}"/>
    <input type="radio" name="subjectPersonIdRequired" value="optional"><fmt:message key="optional" bundle="${resword}"/>
    <input type="radio" checked name="subjectPersonIdRequired" value="not used"><fmt:message key="not_used" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>



  <tr valign="top"><td class="formlabel"><fmt:message key="show_person_id_on_crf_header" bundle="${resword}"/>:</td><td>
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.personIdShownOnCRF == 'true'}">
    <input type="radio" checked name="personIdShownOnCRF" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="personIdShownOnCRF" value="false"><fmt:message key="no" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="personIdShownOnCRF" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="personIdShownOnCRF" value="false"><fmt:message key="no" bundle="${resword}"/>

   </c:otherwise>
  </c:choose>
  </td>
  </tr>


  <tr valign="top"><td class="formlabel"><fmt:message key="how_to_generate_the_study_subject_ID" bundle="${resword}"/>:</td><td>
    <c:choose>
    <c:when test="${studyToView.studyParameterConfig.subjectIdGeneration == 'manual'}">
     <input type="radio" checked name="subjectIdGeneration" value="manual"><fmt:message key="manual_entry" bundle="${resword}"/>
     <input type="radio" name="subjectIdGeneration" value="auto editable"><fmt:message key="auto_generated_and_editable" bundle="${resword}"/>
     <input type="radio" name="subjectIdGeneration" value="auto non-editable"><fmt:message key="auto_generated_and_non_editable" bundle="${resword}"/>
    </c:when>
     <c:when test="${studyToView.studyParameterConfig.subjectIdGeneration == 'auto editable'}">
     <input type="radio" name="subjectIdGeneration" value="manual"><fmt:message key="manual_entry" bundle="${resword}"/>
     <input type="radio" checked name="subjectIdGeneration" value="auto editable"><fmt:message key="auto_generated_and_editable" bundle="${resword}"/>
     <input type="radio" name="subjectIdGeneration" value="auto non-editable"><fmt:message key="auto_generated_and_non_editable" bundle="${resword}"/>
    </c:when>
    <c:otherwise>
     <input type="radio" name="subjectIdGeneration" value="manual"><fmt:message key="manual_entry" bundle="${resword}"/>
     <input type="radio" name="subjectIdGeneration" value="auto editable"><fmt:message key="auto_generated_and_editable" bundle="${resword}"/>
     <input type="radio" checked name="subjectIdGeneration" value="auto non-editable"><fmt:message key="auto_generated_and_non_editable" bundle="${resword}"/>
    </c:otherwise>
   </c:choose>
   </td>
   </tr>



   <tr><td>&nbsp;</td></tr>
   <tr valign="top">
       <td class="formlabel"><fmt:message key="when_entering_data_entry_interviewer" bundle="${resword}"/></td><td>
            <input type="radio" <c:if test="${studyToView.studyParameterConfig.interviewerNameRequired== 'yes'}">checked</c:if> name="interviewerNameRequired" value="yes"><fmt:message key="yes" bundle="${resword}"/>
            <input type="radio" <c:if test="${studyToView.studyParameterConfig.interviewerNameRequired== 'no'}">checked</c:if> name="interviewerNameRequired" value="no"><fmt:message key="no" bundle="${resword}"/>
            <input type="radio" <c:if test="${studyToView.studyParameterConfig.interviewerNameRequired== 'not_used'}">checked</c:if> name="interviewerNameRequired" value="not_used"><fmt:message key="not_used" bundle="${resword}"/>
       </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="interviewer_name_default_as_blank" bundle="${resword}"/></td><td>
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.interviewerNameDefault== 'blank'}">
    <input type="radio" checked name="interviewerNameDefault" value="blank"><fmt:message key="blank" bundle="${resword}"/>
    <input type="radio" name="interviewerNameDefault" value="pre-populated"><fmt:message key="pre_populated_from_active_user" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="interviewerNameDefault" value="blank"><fmt:message key="blank" bundle="${resword}"/>
    <input type="radio" checked name="interviewerNameDefault" value="re-populated"><fmt:message key="pre_populated_from_active_user" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="interviewer_name_editable" bundle="${resword}"/></td><td>
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.interviewerNameEditable== 'true'}">
    <input type="radio" checked name="interviewerNameEditable" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="interviewerNameEditable" value="false"><fmt:message key="no" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="interviewerNameEditable" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="interviewerNameEditable" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="interviewer_date_required" bundle="${resword}"/></td><td>
    <input type="radio" <c:if test="${studyToView.studyParameterConfig.interviewDateRequired== 'yes'}"> checked </c:if> name="interviewDateRequired" value="yes"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" <c:if test="${studyToView.studyParameterConfig.interviewDateRequired== 'no'}"> checked </c:if> name="interviewDateRequired" value="no"><fmt:message key="no" bundle="${resword}"/>
    <input type="radio" <c:if test="${studyToView.studyParameterConfig.interviewDateRequired== 'not_used'}"> checked </c:if> name="interviewDateRequired" value="not_used"><fmt:message key="not_used" bundle="${resword}"/>
  </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="interviewer_date_default_as_blank" bundle="${resword}"/></td><td>
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.interviewDateDefault== 'blank'}">
    <input type="radio" checked name="interviewDateDefault" value="blank"><fmt:message key="blank" bundle="${resword}"/>
    <input type="radio" name="interviewDateDefault" value="pre-populated"><fmt:message key="pre_populated_from_SE" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="interviewDateDefault" value="blank"><fmt:message key="blank" bundle="${resword}"/>
    <input type="radio" checked name="interviewDateDefault" value="re-populated"><fmt:message key="pre_populated_from_SE" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="interviewer_date_editable" bundle="${resword}"/></td><td>
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.interviewDateEditable== 'true'}">
    <input type="radio" checked name="interviewDateEditable" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="interviewDateEditable" value="false"><fmt:message key="no" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="interviewDateEditable" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="interviewDateEditable" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="secondary_label_viewable" bundle="${resword}"/></td><td>
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.secondaryLabelViewable== 'true'}">
    <input type="radio" checked name="secondaryLabelViewable" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="secondaryLabelViewable" value="false"><fmt:message key="no" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="secondaryLabelViewable" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="secondaryLabelViewable" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="forced_reason_for_change" bundle="${resword}"/></td><td>
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.adminForcedReasonForChange== 'true'}">
    <input type="radio" checked name="adminForcedReasonForChange" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" name="adminForcedReasonForChange" value="false"><fmt:message key="no" bundle="${resword}"/>

   </c:when>
   <c:otherwise>
    <input type="radio" name="adminForcedReasonForChange" value="true"><fmt:message key="yes" bundle="${resword}"/>
    <input type="radio" checked name="adminForcedReasonForChange" value="false"><fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>


  <tr valign="top">
      <td class="formlabel"><fmt:message key="event_location_required" bundle="${resword}"/></td><td>
            <input type="radio" <c:if test="${studyToView.studyParameterConfig.eventLocationRequired== 'required'}"> checked </c:if> name="eventLocationRequired" value="required"><fmt:message key="required" bundle="${resword}"/>
            <input type="radio" <c:if test="${studyToView.studyParameterConfig.eventLocationRequired== 'optional'}"> checked </c:if> name="eventLocationRequired" value="optional"><fmt:message key="optional" bundle="${resword}"/>
            <input type="radio" <c:if test="${studyToView.studyParameterConfig.eventLocationRequired== 'not_used'}"> checked </c:if> name="eventLocationRequired" value="not_used"><fmt:message key="not_used" bundle="${resword}"/>
      </td>
  </tr>

</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
</div>
<br>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
 <input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_long">
</td>
<td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onClick="javascript:confirmCancel('pages/studymodule');"/></td>
</tr>
</table>


</form>

<br>


<br>

<br>
 <c:import url="../include/workflow.jsp">
  <c:param name="module" value="admin"/>
 </c:import>
<jsp:include page="../include/footer.jsp"/>
