<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="core.org.akaza.openclinica.logic.importdata.*" %>
<%@ page import="org.akaza.openclinica.controller.dto.LogFileDTO" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterms"/>
<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
 <c:import url="../include/admin-header.jsp"/>
</c:when>
<c:otherwise>
 <c:import url="../include/submit-header.jsp">
 <c:param name="isSpringController" value="true" />
 </c:import>
</c:otherwise>
</c:choose>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<span class="icon icon-caret-down gray"></span>
		</a>
		<fmt:message key="instructions" bundle="${restext}"/>
		<div class="sidebar_tab_content">
			<fmt:message key="upload_side_bar_instructions" bundle="${restext}"/>
		</div>
	</td>
</tr>

<tr id="sidebar_Instructions_closed" style="display: none">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<span class="icon icon-caret-right gray"></span>
		</a>
		<fmt:message key="instructions" bundle="${restext}"/>
	</td>
</tr>



<jsp:include page="../include/sideInfo.jsp"/>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-3.1.0.min.js"></script>
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.16/css/jquery.dataTables.min.css"/>
<script type="text/JavaScript" language="JavaScript" src="//cdnjs.cloudflare.com/ajax/libs/handlebars.js/4.0.11/handlebars.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdnjs.cloudflare.com/ajax/libs/moment.js/2.8.4/moment.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdn.datatables.net/1.10.16/js/jquery.dataTables.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdn.datatables.net/plug-ins/1.10.16/sorting/datetime-moment.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdn.datatables.net/plug-ins/1.10.16/api/fnSortNeutral.js"></script>


<jsp:useBean scope='session' id='version' class='core.org.akaza.openclinica.bean.submit.CRFVersionBean'/>
<jsp:useBean scope='session' id='userBean' class='core.org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='crfName' class='java.lang.String'/>

 <c:out value="${crfName}"/>

<c:choose>
	<c:when test="${userBean.sysAdmin && module=='admin'}">
		<h1><span class="title_manage">
	</c:when>
	<c:otherwise>
		<h1>
		<span class="title_submit">
	</c:otherwise>
</c:choose>

<fmt:message key="list_log_file" bundle="${resworkflow}"/>
<a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/submit-data-module-overview/import-data')">
    <span class=""></span>
</a></h1>
<p><fmt:message key="read_log_instructions" bundle="${restext}"/></p>



<br/>

        <table name="reports" id="logs_table">
		<thead>
			<tr>
				<th width=12.5% align="center" bgcolor="green">LogFile Name</th>
				<th width=12.5% align="center" bgcolor="green">Job Type</th>
				<th width=12.5% align="center" bgcolor="green">Uploaded on</th>
				<th width=12.5% align="center" bgcolor="green">Uploaded by</th> 
				<th width=12.5% align="center" bgcolor="green">Completed on</th>         
				<th width=12.5% align="center" bgcolor="green">Action</th>
			</tr>
		</thead>
		<tbody>
        <%
    
	String uri = request.getScheme() + "://" +   
             request.getServerName() +       
             ":" +                           
             request.getServerPort() +       
             request.getContextPath()+"/";       
    String [] fileNames = null;
	String fname=null;
	String jobType = "";
	String jobTypeDescrption = "";
	String parent = ""; 
	String user = ""; 
	
	// Import pipe delemited data logo file 
    List<LogFileDTO> LogFileDTOs = (List) request.getAttribute("AllLogfileList");
    
	int i = 0;
    int num=0;
    for (i=0; i < LogFileDTOs.size(); i++)
    {

        if(!LogFileDTOs.get(i).getFile().isDirectory())
        {
        	 
            fname = LogFileDTOs.get(i).getFile().getName();
           
            // example:C:\tools\apache-tomcat-7.0.93\openclinica.data\study-event-schedule\11\root_1            
            String ppath = LogFileDTOs.get(i).getFile().getParent();
            int pos = ppath.indexOf(LogFileDTOs.get(i).getParentRootDir());
            // substring include study and user,like:study-event-schedule\11\root_1
            String suStr = ppath.substring(pos);
            int posStart= suStr.indexOf(File.separatorChar);           
			String path = suStr.substring(posStart+1);
			
			pos = path.lastIndexOf(File.separatorChar);
            String parentNm = pos < 0 || pos == path.length() ? "" : path.substring(pos + 1);
			String studyId = pos < 0 || pos == path.length() ? "" : path.substring(0,pos);
			
			
            if(fname.endsWith("_log.txt"))
            {
                
                    { 
                    	jobType=LogFileDTOs.get(i).getJobType();
                    	jobTypeDescrption = LogFileDTOs.get(i).getJobTypeDescrption();
						parent= LogFileDTOs.get(i).getFile().getParent();
						int index1= parent.lastIndexOf("\\");
						int index2= parent.lastIndexOf("_");
								
						user = parent.substring(index1+1, index2);
						
                        %>
                        <tr bgcolor="lightgray">
                            <td width=12.5% align="center">
                                <%=fname%>
                            </td>

                            <td width=12.5% align="center">
									<%=jobTypeDescrption%>
                            </td>

                            <td width=12.5%  align="center">
									<%=new java.util.Date(LogFileDTOs.get(i).getFileCreatedTime())%>
                            </td>
                             <td width=12.5%  align="center">
									<%=user%>
                            </td>
							 <td width=12.5%  align="center">
									 <%
									long passTime1 = System.currentTimeMillis() - LogFileDTOs.get(i).getFile().lastModified();
									
									if(passTime1 >= 1000){
										
									%> 
									<%=new java.util.Date(LogFileDTOs.get(i).getFile().lastModified())%>
									<%}else{%>
									processing...
									<%}%>
                            </td>
                            <td width=12.5%  align="center">
							    <%
								long passTime = System.currentTimeMillis() - LogFileDTOs.get(i).getFile().lastModified();
                                
   						        if(passTime >= 2000){
									
								%> 
								
                                <a target="_new" href="<%=uri%>pages/Log/processFiles?fromUrl=listLog&action=download&type=<%=jobType%>&fileId=<%=fname%>&studyId=<%=studyId%>&parentNm=<%=parentNm%>">
									<span style="padding-right:3px; padding-top: 3px; display:inline-block;">
									    <img title="Download" class="icon" src="../../images/bt_Download.gif"></img>
									</span>
								</a>								
								<a href="<%=uri%>pages/Log/processFiles?fromUrl=listLog&action=delete&type=<%=jobType%>&fileId=<%=fname%>&studyId=<%=studyId%>&parentNm=<%=parentNm%>" onClick='return confirm("Please confirm that you want to delete log file <%=fname%>");'>
																			 
									<span style="padding-right:3px; padding-top: 3px; display:inline-block;">
									    <img title="Delete" class="icon" src="../../images/bt_Delete.gif"></img>
									</span>
								</a>
								<%
								} else{
								%>
								processing...
								<%}%>
                            </td>
                        </tr>

                        <%
                    }
                }
        }
    }
    {%>
	 </tbody>
	 </table> 
	
	<%}%>

<script>
$(document).ready( function () {
    $('#logs_table').DataTable();
} );
</script>
<jsp:include page="../include/footer.jsp"/>