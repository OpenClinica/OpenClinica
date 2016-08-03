<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="resnote"/>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="../include/managestudy-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${resword}"/></b>

        <div class="sidebar_tab_content">

            <fmt:message key="A_study_event_definition_describes_a_type_of_study_event"  bundle="${resword}"/> <fmt:message key="please_consult_the_ODM"  bundle="${resword}"/>
        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${resword}"/></b>

    </td>
</tr>
<jsp:include page="../include/sideInfo.jsp"/>


<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='definition' class='org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean'/>
<jsp:useBean scope='session' id='eventDefinitionCRFs' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='sdvOptions' class='java.util.ArrayList'/>
<script type="text/JavaScript" language="JavaScript">
    <!--
    function myCancel() {

        cancelButton=document.getElementById('cancel');
        if ( cancelButton != null) {
            if(confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>')) {
                window.location.href="ListEventDefinition";
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    function showMe(count,idField) {
        switch(idField) {
            case "participantForm" :
                var elEvaluator = document.getElementsByName('participantForm'+count)[0];
                var elChangeVisibility = document.getElementById('enabledIfParticipantForm'+count);
                var elAllowAnonymousSubmission = document.getElementsByName('allowAnonymousSubmission'+count)[0];
                if (elEvaluator.checked) {
                    elChangeVisibility.removeAttribute("style");
                } else {
                    elAllowAnonymousSubmission.checked = false;
                    elChangeVisibility.setAttribute("style", "display: none;");
                }
                // break;
            case "allowAnonymousSubmission" :
                var elEvaluator = document.getElementsByName('allowAnonymousSubmission'+count)[0];
                var elChangeVisibility = document.getElementById('enabledIfAllowAnonymousSubmission'+count);
                if (elEvaluator.checked) {
                    elChangeVisibility.removeAttribute("style");
                } else {
                    elChangeVisibility.setAttribute("style", "display: none;");
                }
                break;
        }
    }

    //-->
</script>
<h1><span class="title_manage">
<fmt:message key="update_SED" bundle="${resword}"/>
</span></h1>
<ol>
    <fmt:message key="list_create_SED_for"  bundle="${resword}"/>
</ol>
<P>* <fmt:message key="indicates_required_field" bundle="${resword}"/></P>

<form action="UpdateEventDefinition" method="post">
<input type="hidden" name="action" value="confirm">
<div style="width: 600px">
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

        <div class="textbox_center">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr valign="top"><td class="formlabel"><fmt:message key="name" bundle="${resword}"/>:</td><td>
                    <div class="formfieldXL_BG"><input type="text" name="name" value="<c:out value="${definition.name}"/>" class="formfieldXL"></div>
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="name"/></jsp:include>
                </td><td class="formlabel">*</td></tr>
                <tr valign="top"><td class="formlabel"><fmt:message key="description" bundle="${resword}"/>:</td><td>
                    <div class="formtextareaXL4_BG">
                        <textarea class="formtextareaXL4" name="description" rows="4" cols="50"><c:out value="${definition.description}"/></textarea>
                    </div>
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="description"/></jsp:include>
                </td></tr>

                <tr valign="top"><td class="formlabel"><fmt:message key="repeating" bundle="${resword}"/>:</td><td>
                    <c:choose>
                        <c:when test="${definition.repeating == true}">
                            <input type="radio" checked name="repeating" value="1"><fmt:message key="yes" bundle="${resword}"/>
                            <input type="radio" name="repeating" value="0"><fmt:message key="no" bundle="${resword}"/>
                        </c:when>
                        <c:otherwise>
                            <input type="radio" name="repeating" value="1"><fmt:message key="yes" bundle="${resword}"/>
                            <input type="radio" checked name="repeating" value="0"><fmt:message key="no" bundle="${resword}"/>
                        </c:otherwise>
                    </c:choose>
                </td></tr>

                <tr valign="top"><td class="formlabel"><fmt:message key="type" bundle="${resword}"/>:</td><td>
                    <div class="formfieldXL_BG"> <select name="type" class="formfieldXL">
                        <c:choose>
                        <c:when test="${definition.type == 'common'}">
                        <option value="scheduled"><fmt:message key="scheduled" bundle="${resword}"/>
                        <option value="unscheduled"><fmt:message key="unscheduled" bundle="${resword}"/>
                        <option value="common" selected><fmt:message key="common" bundle="${resword}"/>
                            </c:when>
                            <c:when test="${definition.type == 'unscheduled'}">
                        <option value="scheduled"><fmt:message key="scheduled" bundle="${resword}"/>
                        <option value="unscheduled" selected><fmt:message key="unscheduled" bundle="${resword}"/>
                        <option value="common"><fmt:message key="common" bundle="${resword}"/>
                            </c:when>
                            <c:otherwise>
                        <option value="scheduled" selected><fmt:message key="scheduled" bundle="${resword}"/>
                        <option value="unscheduled"><fmt:message key="unscheduled" bundle="${resword}"/>
                        <option value="common"><fmt:message key="common" bundle="${resword}"/>
                            </c:otherwise>
                            </c:choose>
                    </select></div>
                </td></tr>

                <tr valign="top"><td class="formlabel"><fmt:message key="category" bundle="${resword}"/>:</td><td>
                    <div class="formfieldXL_BG"><input type="text" name="category" value="<c:out value="${definition.category}"/>" class="formfieldXL"></div>
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="category"/></jsp:include>
                </td></tr>
            </table>
        </div>
    </div></div></div></div></div></div></div></div>

</div>
<br>
<div class="table_title_manage"><fmt:message key="CRFs" bundle="${resword}"/></div>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B">
<div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tr>
    <td class="table_cell" colspan="4" align="right">
        <a href="AddCRFToDefinition"><fmt:message key="add_a_new_CRF" bundle="${resword}"/></a>
    </td>
</tr>
<c:set var="count" value="0"/>


<c:forEach var="edc" items="${eventDefinitionCRFs}">
<%-- have to clear out a lot of values here, tbh 102007 --%>
<c:set var="hasNI" value="0"/>
<c:set var="hasNA" value="0"/>
<c:set var="hasUNK" value="0"/>
<c:set var="hasNASK" value="0"/>
<c:set var="hasASKU" value="0"/>
<c:set var="hasNAV" value="0"/>
<c:set var="hasOTH" value="0"/>
<c:set var="hasPINF" value="0"/>
<c:set var="hasNINF" value="0"/>
<c:set var="hasMSK" value="0"/>
<c:set var="hasNP" value="0"/>
<c:set var="hasNPE" value="0"/>
<%-- above added by tbh, 102007 --%>  

<input type="hidden" name="id<c:out value="${count}"/>" value="<c:out value="${edc.id}"/>">
<input type="hidden" name="crfId<c:out value="${count}"/>" value="<c:out value="${edc.crfId}"/>">
<c:set var="status" value="0"/>
<c:if test="${edc.status.id==1}"> <c:set var="status" value="1"/> </c:if>

<tr valign="top" bgcolor="#F5F5F5">
    <td class="table_header_column" colspan="3"><c:out value="${edc.crfName}"/></td>
    <td class="table_cell">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <c:choose>
                    <c:when test="${status==1}">
                        <td><a href="RemoveCRFFromDefinition?id=<c:out value="${edc.crfId}"/>"
                               onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
                               onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><img
                                name="bt_Remove1" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
                        </td>
                    </c:when>
                    <c:otherwise>
                        <td><a href="RestoreCRFFromDefinition?id=<c:out value="${edc.crfId}"/>"
                               onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
                               onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"><img
                                name="bt_Restore3" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
                        </td>
                    </c:otherwise>
                </c:choose>
            </tr>
        </table>
    </td>
</tr>
<c:if test="${status==1}">
<tr valign="top">

    <td class="table_cell" colspan="1"><fmt:message key="required" bundle="${resword}"/>:
        <c:choose>
            <c:when test="${edc.requiredCRF == true}">
                <input type="checkbox" checked name="requiredCRF<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <input type="checkbox" name="requiredCRF<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>

    <td class="table_cell" colspan="1"><fmt:message key="double_data_entry" bundle="${resword}"/>:
        <c:choose>
            <c:when test="${edc.doubleEntry == true}">
                <c:set var="msg" value="You are choosing to have this CRF go through one pass of data entry instead of having it go through Double Data Entry. Before choosing this option, ensure that all Subject`s who have data entry for this CRF are not in one of the following 2 phases:\n\n1. The event CRF is in a status of Double Data Entry Started\n2. The event CRF is in a status of Initial Data Entry Completed.\n\nIf the CRFs are in one of those two phases, data entry will not be allowed to continue. You will have to change the configuration back to Double Data Entry.\n\nSelect OK to remove the DDE configuration. Select Cancel to keep the DDE configuration."/>
                <input type="checkbox" onclick="javascript:return confirm('<c:out value="${msg}"/>');" checked name="doubleEntry<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <input type="checkbox" name="doubleEntry<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>

    <td class="table_cell" colspan="1"><fmt:message key="password_required" bundle="${resword}"/>:
        <c:choose>
            <c:when test="${edc.electronicSignature == true}">
                <input type="checkbox" checked name="electronicSignature<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <input type="checkbox" name="electronicSignature<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>

        <%--<td class="table_cell"><fmt:message key="enforce_decision_conditions" bundle="${resword}"/>:
          <c:choose>
           <c:when test="${edc.decisionCondition == true}">
             <input type="checkbox" checked name="decisionCondition<c:out value="${count}"/>" value="yes">
            </c:when>
           <c:otherwise>
             <input type="checkbox" name="decisionCondition<c:out value="${count}"/>" value="yes">
           </c:otherwise>
          </c:choose>
        </td>--%>

    <td class="table_cell" colspan="1"><fmt:message key="default_version" bundle="${resword}"/>:
        <select name="defaultVersionId<c:out value="${count}"/>">
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
    <td class="table_cell" colspan="1">
        <fmt:message key="hidden_crf" bundle="${resword}"/>:
        <c:choose>
            <c:when test="${! edc.hideCrf}">
                <input type="checkbox" name="hideCRF<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise><input checked="checked" type="checkbox" name="hideCRF<c:out value="${count}"/>" value="yes"></c:otherwise>
        </c:choose>
    </td>
        
    </td>
 
     <td class="table_cell" colspan="3"><fmt:message key="sdv_option" bundle="${resword}"/>:
            <select name="sdvOption<c:out value="${count}"/>">
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
  <tr valign="top">
    
    <c:choose>
      <c:when test="${participateFormStatus == 'enabled'}">
 
        <td class="table_cell" colspan="1">
          <fmt:message key="participant_form" bundle="${resword}"/>:
          <c:choose>
            <c:when test="${edc.participantForm == true}">
      <c:choose>
       <c:when test="${definition.repeating == true }">
              <input type="checkbox" name="participantForm<c:out value="${count}"/>" value="yes" onclick="showMe(<c:out value="${count}"/>,'participantForm')" checked>
       </c:when>
         <c:otherwise>
              <input type="checkbox" checked name="participantForm<c:out value="${count}"/>" value="yes" >
         </c:otherwise>
       </c:choose>
            
            </c:when>
            
        <c:otherwise>
       
        <c:choose>
          <c:when test="${definition.repeating == true }">
                <input type="checkbox" name="participantForm<c:out value="${count}"/>" value="yes" onclick="showMe(<c:out value="${count}"/>,'participantForm')">
          </c:when>
         <c:otherwise>
              <input type="checkbox" name="participantForm<c:out value="${count}"/>" value="yes" >
         </c:otherwise>
        </c:choose>
               
            </c:otherwise>
          </c:choose>
        </td>

        <td class="table_cell" colspan="1">
          <c:choose>
            <c:when test="${edc.participantForm == true}">
              <span id="enabledIfParticipantForm<c:out value="${count}"/>">
                <c:choose>
                
                  <c:when test="${edc.allowAnonymousSubmission == true}">
                  
                        <c:choose>
       <c:when test="${definition.repeating == true }">
           <fmt:message key="allow_anonymous_submission" bundle="${resword}"/>:
           <input type="checkbox" name="allowAnonymousSubmission<c:out value="${count}"/>" value="yes" onclick="showMe(<c:out value="${count}"/>,'allowAnonymousSubmission')" checked>
       </c:when>
         <c:otherwise>
         </c:otherwise>
       </c:choose>
            </c:when>
                  
                  <c:otherwise>

                        <c:choose>
       <c:when test="${definition.repeating == true }">
           <fmt:message key="allow_anonymous_submission" bundle="${resword}"/>:
                    <input type="checkbox" name="allowAnonymousSubmission<c:out value="${count}"/>" value="yes" onclick="showMe(<c:out value="${count}"/>,'allowAnonymousSubmission')">
       </c:when>
         <c:otherwise>
         </c:otherwise>
       </c:choose>


                  </c:otherwise>
                  
                </c:choose>
              </span>
            </c:when>
            <c:otherwise>
              <span id="enabledIfParticipantForm<c:out value="${count}"/>" style="display : none">
                <fmt:message key="allow_anonymous_submission" bundle="${resword}"/>:
                <c:choose>
                  <c:when test="${edc.allowAnonymousSubmission == true}">
                    <input type="checkbox" name="allowAnonymousSubmission<c:out value="${count}"/>" value="yes" onclick="showMe(<c:out value="${count}"/>,'allowAnonymousSubmission')" checked>
                  </c:when>
                  <c:otherwise>
                    <input type="checkbox" name="allowAnonymousSubmission<c:out value="${count}"/>" value="yes" onclick="showMe(<c:out value="${count}"/>,'allowAnonymousSubmission')">
                  </c:otherwise>
                </c:choose>
              </span>
            </c:otherwise>
          </c:choose>
        </td>

        <td class="table_cell" colspan="2">
          <c:choose>
            <c:when test="${edc.participantForm == true && definition.repeating == true && edc.allowAnonymousSubmission == true}">
              <span id="enabledIfAllowAnonymousSubmission<c:out value="${count}"/>">
                
                <fmt:message key="submission_url" bundle="${resword}"/>: ${participantUrl}
                <input type="text" name="submissionUrl<c:out value="${count}"/>" value="${edc.submissionUrl}">               
                <c:set var="summary" value="submissionUrl${count}"/>
                <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="${summary}"/></jsp:include>
                 <br />
                <c:choose>
                  <c:when test="${edc.allowAnonymousSubmission == true && definition.repeating == true  && edc.offline == true}">
                <fmt:message key="offline" bundle="${resword}"/>:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;           
                    <input type="checkbox" name="offline<c:out value="${count}"/>" value="yes"  checked>
                  </c:when>
                  <c:when test="${edc.allowAnonymousSubmission == true && definition.repeating == true  && edc.offline != true}">
                <fmt:message key="offline" bundle="${resword}"/>:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;           
                    <input type="checkbox" name="offline<c:out value="${count}"/>" value="yes" >
                  </c:when>
                </c:choose>
                
              </span>
            </c:when>
            <c:otherwise>
              <span id="enabledIfAllowAnonymousSubmission<c:out value="${count}"/>" style="display : none">
                <fmt:message key="submission_url" bundle="${resword}"/>: ${participantUrl}
                <input type="text" name="submissionUrl<c:out value="${count}"/>" value="${edc.submissionUrl}">
                <c:set var="summary" value="submissionUrl${count}"/>
                <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="${summary}"/></jsp:include>
                <c:choose>
                  <c:when test="${definition.repeating == true }">
                          <br />
                    <fmt:message key="offline" bundle="${resword}"/>:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;           
                    <input type="checkbox" name="offline<c:out value="${count}"/>" value="yes" >                    
                  </c:when>
                </c:choose>                
              </span>
            </c:otherwise>
          </c:choose>
        </td>



      </c:when>
    </c:choose>

</tr>

<tr valign="top">
    <td class="table_cell" colspan="4"><a href="<fmt:message key="nullValue" bundle="${resformat}"/>" target="def_win" onClick="openDefWindow('<fmt:message key="nullValue" bundle="${resformat}"/>'); return false;"><fmt:message key="null_values" bundle="${resword}"/></a>:<c:out value="${edc.nullValues}"/></td>
</tr>

<c:forEach var="nv" items="${edc.nullFlags}">
    <c:if test="${nv.key == 'NI' && nv.value == '1'}">
        <c:set var="hasNI" value="1"/>
    </c:if>
    <c:if test="${nv.key == 'NA' && nv.value == '1'}">
        <c:set var="hasNA" value="1"/>
    </c:if>
    <c:if test="${nv.key == 'UNK' && nv.value == '1'}">
        <c:set var="hasUNK" value="1"/>
    </c:if>
    <c:if test="${nv.key == 'NASK' && nv.value == '1'}">
        <c:set var="hasNASK" value="1"/>
    </c:if>
    <c:if test="${nv.key == 'ASKU' && nv.value == '1'}">
        <c:set var="hasASKU" value="1"/>
    </c:if>
    <c:if test="${nv.key == 'NAV' && nv.value == '1'}">
        <c:set var="hasNAV" value="1"/>
    </c:if>
    <c:if test="${nv.key == 'OTH' && nv.value == '1'}">
        <c:set var="hasOTH" value="1"/>
    </c:if>
    <c:if test="${nv.key == 'PINF' && nv.value == '1'}">
        <c:set var="hasPINF" value="1"/>
    </c:if>
    <c:if test="${nv.key == 'NINF' && nv.value == '1'}">
        <c:set var="hasNINF" value="1"/>
    </c:if>
    <c:if test="${nv.key == 'MSK' && nv.value == '1'}">
        <c:set var="hasMSK" value="1"/>
    </c:if>
    <c:if test="${nv.key == 'NP' && nv.value == '1'}">
        <c:set var="hasNP" value="1"/>
    </c:if>
    <c:if test="${nv.key == 'NPE' && nv.value == '1'}">
        <c:set var="hasNPE" value="1"/>
    </c:if>
</c:forEach>


<tr valign="top">

    <td class="table_cell">
        <c:choose>
            <c:when test="${hasNI == 1}">
                <fmt:message key="NI" bundle="${resword}"/><input type="checkbox" checked name="ni<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <fmt:message key="NI" bundle="${resword}"/><input type="checkbox" name="ni<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>


    <td class="table_cell">
        <c:choose>
            <c:when test="${hasNA == 1}">
                <fmt:message key="NA" bundle="${resword}"/><input type="checkbox" checked name="na<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <fmt:message key="NA" bundle="${resword}"/><input type="checkbox" name="na<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>


    <td class="table_cell">
        <c:choose>
            <c:when test="${hasUNK == 1}">
                <fmt:message key="UNK" bundle="${resword}"/><input type="checkbox" checked name="unk<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <fmt:message key="UNK" bundle="${resword}"/><input type="checkbox" name="unk<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>

    <td class="table_cell">
        <c:choose>
            <c:when test="${hasNASK == 1}">
                <fmt:message key="NASK" bundle="${resword}"/><input type="checkbox" checked name="nask<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <fmt:message key="NASK" bundle="${resword}"/><input type="checkbox" name="nask<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>
</tr>
<tr>

    <td class="table_cell">
        <c:choose>
            <c:when test="${hasASKU== 1}">
                <fmt:message key="ASKU" bundle="${resword}"/><input type="checkbox" checked name="asku<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <fmt:message key="ASKU" bundle="${resword}"/><input type="checkbox" name="asku<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>


    <td class="table_cell">
        <c:choose>
            <c:when test="${hasNAV == 1}">
                <fmt:message key="NAV" bundle="${resword}"/><input type="checkbox" checked name="nav<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <fmt:message key="NAV" bundle="${resword}"/><input type="checkbox" name="nav<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>

    <td class="table_cell">
        <c:choose>
            <c:when test="${hasOTH == 1}">
                <fmt:message key="OTH" bundle="${resword}"/><input type="checkbox" checked name="oth<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <fmt:message key="OTH" bundle="${resword}"/><input type="checkbox" name="oth<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>

    <td class="table_cell">
        <c:choose>
            <c:when test="${hasPINF == 1}">
                <fmt:message key="PINF" bundle="${resword}"/><input type="checkbox" checked name="pinf<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <fmt:message key="PINF" bundle="${resword}"/><input type="checkbox" name="pinf<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>

</tr>
<tr>
    <td class="table_cell">
        <c:choose>
            <c:when test="${hasNINF == 1}">
                <fmt:message key="NINF" bundle="${resword}"/><input type="checkbox" checked name="ninf<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <fmt:message key="NINF" bundle="${resword}"/><input type="checkbox" name="ninf<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>

    <td class="table_cell">
        <c:choose>
            <c:when test="${hasMSK == 1}">
                <fmt:message key="MSK" bundle="${resword}"/><input type="checkbox" checked name="msk<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <fmt:message key="MSK" bundle="${resword}"/><input type="checkbox" name="msk<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>

    <td class="table_cell">
        <c:choose>
            <c:when test="${hasNP == 1}">
                <fmt:message key="NP" bundle="${resword}"/><input type="checkbox" checked name="np<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <fmt:message key="NP" bundle="${resword}"/><input type="checkbox" name="np<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>
    
    <td class="table_cell">
        <c:choose>
            <c:when test="${hasNPE == 1}">
                <fmt:message key="NPE" bundle="${resword}"/><input type="checkbox" checked name="npe<c:out value="${count}"/>" value="yes">
            </c:when>
            <c:otherwise>
                <fmt:message key="NPE" bundle="${resword}"/><input type="checkbox" name="npe<c:out value="${count}"/>" value="yes">
            </c:otherwise>
        </c:choose>
    </td>

</tr>
<tr><td class="table_divider" colspan="4">&nbsp;</td></tr>
</c:if>
<c:set var="count" value="${count+1}"/>
</c:forEach>





</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<br>
<table border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td>
            <input type="submit" name="Submit" value="<fmt:message key="confirm" bundle="${resword}"/>" class="button_medium">
        </td>
        <td>
            <input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_long" onClick="javascript:myCancel();"/></td>
    </tr></table>
</form>
<br><br>

<!-- EXPANDING WORKFLOW BOX -->

<table border="0" cellpadding="0" cellspacing="0" style="position: relative; left: -14px;">
    <tr>
        <td id="sidebar_Workflow_closed" style="display: none">
            <a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="images/<fmt:message key="image_dir" bundle="${resformat}"/>/tab_Workflow_closed.gif" border="0"></a>
        </td>
        <td id="sidebar_Workflow_open" style="display: all">
            <table border="0" cellpadding="0" cellspacing="0" class="workflowBox">
                <tr>
                    <td class="workflowBox_T" valign="top">
                        <table border="0" cellpadding="0" cellspacing="0">
                            <tr>
                                <td class="workflow_tab">
                                    <a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

                                    <b><fmt:message key="workflow" bundle="${resword}"/></b>

                                </td>
                            </tr>
                        </table>
                    </td>
                    <td class="workflowBox_T" align="right" valign="top"><img src="images/workflowBox_TR.gif"></td>
                </tr>
                <tr>
                    <td colspan="2" class="workflowbox_B">
                        <div class="box_R"><div class="box_B"><div class="box_BR">
                            <div class="workflowBox_center">


                                <!-- Workflow items -->

                                <table border="0" cellpadding="0" cellspacing="0">
                                    <tr>
                                        <td>

                                            <!-- These DIVs define shaded box borders -->
                                            <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

                                                <div class="textbox_center" align="center">
    
                            <span class="title_manage">         
                        
                            <b><fmt:message key="enter_definition_name_and_description" bundle="${resword}"/><br><br></b>
                                    
                            </span>

                                                </div>
                                            </div></div></div></div></div></div></div></div>

                                        </td>
                                        <td><img src="images/arrow.gif"></td>
                                        <td>

                                            <!-- These DIVs define shaded box borders -->
                                            <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

                                                <div class="textbox_center" align="center">

                            <span class="title_manage">
                             <fmt:message key="add_CRFs_to_definition" bundle="${resword}"/><br><br>
                            </span>

                                                </div>
                                            </div></div></div></div></div></div></div></div>

                                        </td>
                                        <td><img src="images/arrow.gif"></td>
                                        <td>

                                            <!-- These DIVs define shaded box borders -->
                                            <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

                                                <div class="textbox_center" align="center">

                            <span class="title_manage">
                             <fmt:message key="edit_properties_for_each_CRF" bundle="${resword}"/><br><br>
                            </span>

                                                </div>
                                            </div></div></div></div></div></div></div></div>

                                        </td>
                                        <td><img src="images/arrow.gif"></td>
                                        <td>

                                            <!-- These DIVs define shaded box borders -->
                                            <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

                                                <div class="textbox_center" align="center">

                            <span class="title_manage">
                             <fmt:message key="confirm_and_submit_definition" bundle="${resword}"/><br><br>
                            </span>

                                                </div>
                                            </div></div></div></div></div></div></div></div>

                                        </td>
                                    </tr>
                                </table>


                                <!-- end Workflow items -->

                            </div>
                        </div></div></div>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>

<!-- END WORKFLOW BOX -->
<jsp:include page="../include/footer.jsp"/>
