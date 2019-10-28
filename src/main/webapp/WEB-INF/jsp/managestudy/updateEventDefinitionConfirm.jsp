<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>


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

<jsp:useBean scope='session' id='userBean' class='core.org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='definition' class='core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean'/>
<jsp:useBean scope='session' id='eventDefinitionCRFs' class='java.util.ArrayList'/>
<h1><span class="title_manage"><fmt:message key="confirm_event_definition_updates" bundle="${resword}"/></span></h1>
<div style="width: 600px">
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

        <div class="tablebox_center">
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr valign="top"><td class="table_header_column_top"><fmt:message key="name" bundle="${resword}"/>:</td><td class="table_cell">
                    <c:out value="${definition.name}"/>
                </td></tr>
                <tr valign="top"><td class="table_header_column"><fmt:message key="description" bundle="${resword}"/>:</td><td class="table_cell">
                    <c:out value="${definition.description}"/>&nbsp;
                </td></tr>

                <tr valign="top"><td class="table_header_column"><fmt:message key="repeating" bundle="${resword}"/>:</td><td class="table_cell">
                    <c:choose>
                        <c:when test="${definition.repeating == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
                        <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
                    </c:choose>
                </td></tr>

                <tr valign="top"><td class="table_header_column"><fmt:message key="type" bundle="${resword}"/>:</td><td class="table_cell">
                    <c:out value="${definition.type}"/>
                </td></tr>

                <tr valign="top"><td class="table_header_column"><fmt:message key="category" bundle="${resword}"/>:</td><td class="table_cell">
                    <c:out value="${definition.category}"/>&nbsp;
                </td></tr>
            </table>
        </div>
    </div></div></div></div></div></div></div></div>

</div>
<br>
<span class="table_title_manage"><fmt:message key="CRFs" bundle="${resword}"/></span>
<div style="width: 900px">
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

        <div class="tablebox_center">
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr valign="top">
                    <td class="table_header_row_left"><fmt:message key="name" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="required" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="double_data_entry" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="password_required" bundle="${resword}"/></td>
                    <!-- <td class="table_header_row"><fmt:message key="enforce_decision_conditions" bundle="${resword}"/></td>-->
                    <td class="table_header_row"><fmt:message key="default_version" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="null_values" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="hidden_crf" bundle="${resword}"/></td>
  


                 <c:choose>
                      <c:when test="${participateFormStatus == 'enabled'}">
                    <td class="table_header_row"><fmt:message key="participant_form" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="allow_anonymous_submission" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="submission_url" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="offline" bundle="${resword}"/></td>
                </c:when>  
              </c:choose>
  
                    <td class="table_header_row"><fmt:message key="sdv_option" bundle="${resword}"/></td>

                </tr>

                <c:forEach var ="crf" items="${eventDefinitionCRFs}">
                    <tr>
                        <td class="table_cell_left"><c:out value="${crf.crfName}"/></td>



                        <td class="table_cell">
                            <c:choose>
                                <c:when test="${crf.requiredCRF == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
                                <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
                            </c:choose>
                        </td>

                        <td class="table_cell">
                            <c:choose>
                                <c:when test="${crf.doubleEntry == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
                                <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
                            </c:choose>
                        </td>

                        <td class="table_cell">
                            <c:choose>
                                <c:when test="${crf.electronicSignature == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
                                <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
                            </c:choose>
                        </td>


                            <%--<td class="table_cell">
                              <c:choose>
                               <c:when test="${crf.decisionCondition == true}"> Yes </c:when>
                               <c:otherwise> No </c:otherwise>
                              </c:choose>
                            </td>--%>

                        <td class="table_cell">
                            <c:out value="${crf.defaultVersionName}"/>
                        </td>
                        <td class="table_cell">
                            <c:out value="${crf.nullValues}"/>&nbsp;
                        </td>

                        <td class="table_cell"><c:out value="${crf.status.name}"/></td>

                        <td class="table_cell">
                            <c:choose>
                                <c:when test="${crf.hideCrf == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
                                <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
                            </c:choose>
                        </td>
                       
  <c:choose>
    <c:when test="${participateFormStatus == 'enabled'}">
   <td class="table_cell">
     <c:choose>
      <c:when test="${crf.participantForm == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
      <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
     </c:choose>
    </td>
   <td class="table_cell">
     <c:choose>
      <c:when test="${crf.participantForm == true && crf.allowAnonymousSubmission == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
      <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
     </c:choose>
    </td>
    
     <c:choose>    
       <c:when test="${crf.participantForm == true && crf.allowAnonymousSubmission == true && crf.submissionUrl !='' }">    
      <td class="table_cell"><c:out value="${participantUrl}${crf.submissionUrl}"/></td>
      </c:when>   
       <c:otherwise>    
         <td class="table_cell"><c:out value=""/></td>
      </c:otherwise>
      </c:choose>
    
   <td class="table_cell">
     <c:choose>
           <c:when test="${crf.participantForm == true && crf.allowAnonymousSubmission == true && crf.offline == true  && definition.repeating == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
           <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
     </c:choose>
   </td>
    
    
    
   </c:when>  
 </c:choose>



		<td class="table_cell"><fmt:message key="${crf.sourceDataVerification.description}" bundle="${resterm}"/></td> 
						

                    </tr>
                </c:forEach>

            </table>
        </div>
    </div></div></div></div></div></div></div></div>

</div>
<br>
<table border="0" cellpadding="0" cellspacing="0">
   <tr>
        <td>
            <form action="UpdateEventDefinition" method="POST">
                <input type="hidden" name="action" value="submit">
                <input type="submit" name="submit" value="<fmt:message key="confirm_and_finish" bundle="${resword}"/>" class="button_long">
            </form>
        </td>
        <td>
            <form action="UpdateEventDefinition" method="POST">
                <input type="hidden" name="action" value="cancel">
                <input type="submit" name="submit" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium">
            </form>
        </td>
    </tr>
</table>
<br><br>
<jsp:include page="../include/footer.jsp"/>
