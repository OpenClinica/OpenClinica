<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>


<jsp:include page="../include/managestudy-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
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

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='definition' class='org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean'/>
<jsp:useBean scope='request' id='sdvOptions' class='java.util.ArrayList'/>
<h1><span class="title_manage"><fmt:message key="define_study_event"  bundle="${resword}"/> - <fmt:message key="selected_CRFs"  bundle="${resword}"/> - <fmt:message key="selected_default_version"  bundle="${resword}"/></span></h1>
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

<form action="DefineStudyEvent" method="post">
    <input type="hidden" name="actionName" value="confirm">
    <div style="width: 900px">
        <!-- These DIVs define shaded box borders -->
        <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

            <div class="textbox_center">
                <table border="0" cellpadding="0" cellspacing="0" width="100%">
                    <c:set var="count" value="0"/>

                    <c:forEach var ="crf" items="${definition.crfs}">
                        <input type="hidden" name="crfId<c:out value="${count}"/>" value="<c:out value="${crf.id}"/>">
                        <input type="hidden" name="crfName<c:out value="${count}"/>" value="<c:out value="${crf.name}"/>">

                        <tr valign="top" bgcolor="#F5F5F5">
                            <td class="table_header_column" colspan="4"><c:out value="${crf.name}"/></td>
                        </tr>

                        <tr valign="top">

                            <td class="table_cell" colspan="1"><fmt:message key="required" bundle="${resword}"/>:<input type="checkbox" checked name="requiredCRF<c:out value="${count}"/>" value="yes"></td>

                            <td class="table_cell" colspan="1"><fmt:message key="double_data_entry" bundle="${resword}"/>:<input type="checkbox" name="doubleEntry<c:out value="${count}"/>" value="yes"></td>

                            <td class="table_cell" colspan="1"><fmt:message key="password_required" bundle="${resword}"/>:<input type="checkbox" name="electronicSignature<c:out value="${count}"/>" value="yes"></td>

                                <%-- <td class="table_cell"><fmt:message key="enforce_decision_conditions" bundle="${restext}"/>:<input type="checkbox" name="decisionCondition<c:out value="${count}"/>"  checked value="yes"></td>--%>

                            <td class="table_cell" colspan="1"><fmt:message key="default_version" bundle="${resword}"/>:

                                <select name="defaultVersionId<c:out value="${count}"/>">
                                    <c:forEach var="version" items="${crf.versions}">
                                    <option value="<c:out value="${version.id}"/>"><c:out value="${version.name}"/>
                                        </c:forEach>
                                </select>
                            </td></tr>
                            
                        <tr valign="top">
                            <td class="table_cell" colspan="1"><fmt:message key="hidden_crf" bundle="${resword}"/>:<input type="checkbox" name="hiddenCrf<c:out value="${count}"/>" value="yes"></td>


                            <td class="table_cell" colspan="3"><fmt:message key="sdv_option" bundle="${resword}"/>:
                                <select name="sdvOption<c:out value="${count}"/>">
                                    <c:set var="index" value="1"/>
                                    <c:forEach var="sdv" items="${sdvOptions}">
                                        <c:choose>
                                        <c:when test="${index == 3}">
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
    
       <c:choose>
       <c:when test="${definition.repeating == true }">
            <td class="table_cell" colspan="1"><fmt:message key="participant_form" bundle="${resword}"/>:<input type="checkbox" name="participantForm<c:out value="${count}"/>" value="yes" onclick="showMe(<c:out value="${count}"/>,'participantForm')"></td>                                      
       </c:when>
         <c:otherwise>
            <td class="table_cell" colspan="1"><fmt:message key="participant_form" bundle="${resword}"/>:<input type="checkbox" name="participantForm<c:out value="${count}"/>" value="yes" ></td>
         </c:otherwise>
       </c:choose>
    
    
      <td class="table_cell" colspan="1">
        <span id="enabledIfParticipantForm<c:out value="${count}"/>" style="display : none">
          <fmt:message key="allow_anonymous_submission" bundle="${resword}"/>:<input type="checkbox" name="allowAnonymousSubmission<c:out value="${count}"/>" value="yes" onclick="showMe(<c:out value="${count}"/>,'allowAnonymousSubmission')">
        </span>
      </td>
      <td class="table_cell" colspan="2">
        <span id="enabledIfAllowAnonymousSubmission<c:out value="${count}"/>" style="display : none">
          <fmt:message key="submission_url" bundle="${resword}"/>: ${participantUrl}<input type="text" name="submissionUrl<c:out value="${count}"/>" value="">
                <c:choose>
                  <c:when test="${definition.repeating == true }">
                          <br />
                    <fmt:message key="offline" bundle="${resword}"/>:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;           
                    <input type="checkbox" name="offline<c:out value="${count}"/>" value="yes" >                    
                  </c:when>
                </c:choose>
          
        </span>
      </td>
   </c:when>  
 </c:choose>

                        </tr>

                            


                        <tr valign="top">
                            <td class="table_header_column" colspan="4"><fmt:message key="choose_null_values"  bundle="${resword}"/> (<a href="<fmt:message key="nullValue" bundle="${resformat}"/>" target="def_win" onClick="openDefWindow('<fmt:message key="nullValue" bundle="${resformat}"/>'); return false;"><fmt:message key="what_is_null_value"  bundle="${resword}"/></a>)</td>
                        </tr>

                        <tr valign="top">

                            <td class="table_cell"><fmt:message key="NI" bundle="${resword}"/><input type="checkbox" name="ni<c:out value="${count}"/>" value="yes"></td>

                            <td class="table_cell"><fmt:message key="NA" bundle="${resword}"/><input type="checkbox" name="na<c:out value="${count}"/>" value="yes"></td>

                            <td class="table_cell"><fmt:message key="UNK" bundle="${resword}"/><input type="checkbox" name="unk<c:out value="${count}"/>" value="yes"></td>

                            <td class="table_cell"><fmt:message key="NASK" bundle="${resword}"/><input type="checkbox" name="nask<c:out value="${count}"/>" value="yes"></td>
                        </tr>
                        <tr valign="top">

                            <td class="table_cell"><fmt:message key="ASKU" bundle="${resword}"/><input type="checkbox" name="asku<c:out value="${count}"/>" value="yes"></td>

                            <td class="table_cell"><fmt:message key="NAV" bundle="${resword}"/><input type="checkbox" name="nav<c:out value="${count}"/>" value="yes"></td>

                            <td class="table_cell"><fmt:message key="OTH" bundle="${resword}"/><input type="checkbox" name="oth<c:out value="${count}"/>" value="yes"></td>

                            <td class="table_cell"><fmt:message key="PINF" bundle="${resword}"/><input type="checkbox" name="pinf<c:out value="${count}"/>" value="yes"></td>
                        </tr>
                        <tr valign="top">

                            <td class="table_cell"><fmt:message key="NINF" bundle="${resword}"/><input type="checkbox" name="ninf<c:out value="${count}"/>" value="yes"></td>

                            <td class="table_cell"><fmt:message key="MSK" bundle="${resword}"/><input type="checkbox" name="msk<c:out value="${count}"/>" value="yes"></td>

                            <td class="table_cell"><fmt:message key="NP" bundle="${resword}"/><input type="checkbox" name="np<c:out value="${count}"/>" value="yes"></td>

                            <td class="table_cell"><fmt:message key="NPE" bundle="${resword}"/><input type="checkbox" name="npe<c:out value="${count}"/>" value="yes"></td>

                            <td class="table_cell">&nbsp;</td>
                        </tr>
                        <c:set var="count" value="${count+1}"/>
                        <tr><td class="table_divider" colspan="4">&nbsp;</td></tr>
                    </c:forEach>

                </table>
            </div>
        </div></div></div></div></div></div></div></div>
    </div>

    <table border="0" cellpadding="0" cellspacing="0">
        <tr>
            <td>
                <input type="submit" name="Submit" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_long">
            </td>
            <td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_long" onClick="javascript:myCancel();"/></td></td>
        </tr>
    </table>
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
                        
                            <fmt:message key="enter_definition_name_and_description" bundle="${resword}"/><br><br>
                                    
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
                             <b><fmt:message key="edit_properties_for_each_CRF" bundle="${resword}"/><br><br></b>
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
