<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<jsp:include page="../include/admin-header.jsp"/>

<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js"></script>

      <script type="text/javascript">
        jQuery(document).ready(function() {
            jQuery('#addNewStudy').click(function() {
                jQuery.blockUI({ message: jQuery('#addNewStudyForm'), css:{left: "300px", top:"10px", cursor:'default' } });
            });

            jQuery('#closeDialogBox').click(function() {
                jQuery.unblockUI();
                $('#closeDialogBoxWarnings').empty();
            });
            // If there are warnings, we failed in a previous submission and should display the warnings on the popup window.
            var warnings = "${regMessages}";
            if (warnings.length > 0) {
            	jQuery.blockUI({ message: jQuery('#requestRandomizationForm'), css:{left: "300px", top:"10px" } });
            }
        });

        // Hide the popup window if the escape key is pressed
        jQuery(document).keyup(function(keyPressed) {
            if(keyPressed.keyCode === 27) {
                $('#closeDialogBoxWarnings').empty();
                jQuery.unblockUI();
            }
        });
    </script>

<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">
		<fmt:message key="studies_are_indicated_in_bold" bundle="${restext}"/>
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
<jsp:useBean scope='request' id='table' class='org.akaza.openclinica.web.bean.EntityBeanTable'/>

<h1><span class="title_manage"><fmt:message key="administer_studies" bundle="${resword}"/> <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/administer-study')"><img src="images/bt_Help_Manage.gif" border="0" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="help" bundle="${resword}"/>"></a></span></h1>

<div class="homebox_bullets"> <a href="javascript:;" id="addNewStudy" name="addNewStudy"><fmt:message key="create_a_new_study" bundle="${resword}"/></a></div>

<p>
<fmt:message key="studies_are_indicated_in_bold" bundle="${restext}"/>
</p>

    <div align="left" id="addNewStudyForm" class="add-new-study-div">  
    <div style="float: right;"> <a href="javascript:;" id="closeDialogBox" name="closeDialogBox"><fmt:message key="exit_add_new_study_window" bundle="${resword}"/></a></div>
            <form action="CreateStudy" method="post">
            <h1>
                <fmt:message key="add_new_study_title" bundle="${resword}"/>
            </h1>
            <p><fmt:message key="add_new_study_screen_content" bundle="${resword}"/></p>
            <br>
       

       <a href="CreateStudy"><input type="button" class="button_medium" value="Continue in OC3" /></a>       
       <a href="https://openclinica.com/upgrade" target="_blank">   <input type="button" class="button_long" value="Tell me more about OC4" /></a>       
           </form>
    </div>

<c:import url="../include/showTable.jsp"><c:param name="rowURL" value="showStudyRow.jsp" /></c:import>
<br><br>

<jsp:include page="../include/footer.jsp"/>
