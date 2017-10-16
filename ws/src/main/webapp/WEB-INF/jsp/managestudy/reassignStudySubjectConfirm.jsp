<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>    
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/> 
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/> 



<jsp:include page="../include/managestudy-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${restext}"/></b>

		<div class="sidebar_tab_content"> 
		
		</div>

		</td>
	
	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${restext}"/></b>

		</td>
  </tr>

<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope="request" id="newStudy" class="org.akaza.openclinica.bean.managestudy.StudyBean"/>
<jsp:useBean scope="request" id="subject" class="org.akaza.openclinica.bean.submit.SubjectBean"/>
<table>
<tr>
    <td>
<h1><span class="title_manage">
<fmt:message key="confirm_reassign_study_subject" bundle="${restext}"/>
</span></h1>

<form action="ReassignStudySubject" method="post">
<input type="hidden" name="action" value="submit">
<input type="hidden" name="id" value="<c:out value="${studySub.id}"/>">
<input type="hidden" name="studyId" value="<c:out value="${newStudy.id}"/>">
<p><fmt:message key="you_choose_to_reassign_subject2" bundle="${restext}"/> <b><c:out value="${subject.uniqueIdentifier}"/></b> <fmt:message key="to_study" bundle="${restext}"/>  <b><c:out value="${newStudy.name}"/></b>.</p>
<br>
<input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium"></td></tr>

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

                   <b><fmt:message key="workflow" bundle="${restext}"/></b>

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



                           <fmt:message key="manage_study" bundle="${resworkflow}"/>


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


                             <fmt:message key="manage_subjects" bundle="${resworkflow}"/>


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

                             <b> <fmt:message key="reassign_study_subject" bundle="${resworkflow}"/> </b>

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
   </td></tr>
</table>
   <jsp:include page="../include/footer.jsp"/>
