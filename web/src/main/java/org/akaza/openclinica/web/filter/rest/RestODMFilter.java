package org.akaza.openclinica.web.filter.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;


import com.sun.jersey.server.impl.application.WebApplicationContext;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;



public class RestODMFilter implements ContainerRequestFilter,ResourceFilter {

	@Context
	HttpServletRequest request;
	@Context
	HttpServletResponse response;
	  
	WebApplicationContext context;
	String studyOIDS;
	
	 public static ResourceBundle  restext;
	 
	 private static String GlOBAL_STUDY_OID = "*";
	
	@Override
	
	public ContainerRequest filter(ContainerRequest containerRequest) {
		request.setAttribute("requestSchema", "public");
		// get tenant schema

		UserAccountBean userBean = (UserAccountBean)request.getSession().getAttribute("userBean");	
		
		
		
		String studyOID = containerRequest.getPathSegments().get(3).getPath();

		//parse to get studyOID
	
		
		if(studyOID.equals(GlOBAL_STUDY_OID))
		{
			if(checkAuth(userBean)) return containerRequest;
			
				
		}
	
		else{
			StudyBean studyBean = getStudyByOID(studyOID,getDataSource());
			request.setAttribute("requestSchema", studyBean.getSchemaName());
			if(checkAuth(studyBean,userBean)) return containerRequest;
			else
			{
				if(studyBean.getParentStudyId()!=0){
				int parentStudyID = studyBean.getParentStudyId();
				studyBean = getStudyByID(parentStudyID,getDataSource());
				if(checkAuth(studyBean,userBean))return containerRequest;
			}
			}   
			
	        request.setAttribute(SecureController.PAGE_MESSAGE, "You don't have correct permission in your current Study.");
		}
        
        

		throw new WebApplicationException(Response.Status.FORBIDDEN);
	
	}


	
	
	private Boolean checkAuth(UserAccountBean userBean) {
		Boolean auth = false;
		
		  ArrayList userRoles = userBean.getRoles();
	        for (int i = 0; (i < userRoles.size() && auth==false); i++) {
	            StudyUserRoleBean studyRole = (StudyUserRoleBean) userRoles.get(i);

				if(studyRole.getRole().equals(Role.ADMIN) || studyRole.getRole().equals(Role.COORDINATOR) ||studyRole.getRole().equals(Role.STUDYDIRECTOR))
				{
					auth = true;
					
				}
	        }
		return auth;
	}




	private Boolean checkAuth(StudyBean studyBean,UserAccountBean userBean){
		Boolean auth = false;
		StudyUserRoleBean studyRole = getRoleByStudy(studyBean,getDataSource(),userBean);
		Role r = studyRole.getRole();
			if (r != null) {
            // r = userBean.getActiveStudyRole();
            if (r != null && (r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR )  )) {
               auth = true;
            }
            //else if(userBean.isTechAdmin()||userBean.isSysAdmin())
            //{
                if(r!=null && (r.equals(Role.ADMIN)||r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR) || r.equals(Role.INVESTIGATOR)||r.equals(Role.MONITOR)||r.equals(Role.RESEARCHASSISTANT)||r.equals(Role.RESEARCHASSISTANT2) ) ){

                        auth = true;
            	}
            //}
        }
			return auth;
	}
		
	private DataSource getDataSource(){
	return (DataSource) SpringServletAccess.getApplicationContext(request.getSession().getServletContext()).getBean("dataSource");	
	}
	
	private StudyBean getStudyByOID(String OID,DataSource ds){
		StudyDAO studyDAO= new StudyDAO(ds);
		return studyDAO.findByOid(OID);
	}
	private StudyUserRoleBean getRoleByStudy(StudyBean studyBean,DataSource ds,UserAccountBean userBean){
		UserAccountDAO userAccountDAO = new UserAccountDAO(ds);
		return userAccountDAO.findRoleByUserNameAndStudyId(userBean.getName(), studyBean.getId());
		
	}
	private StudyBean getStudyByID(int id,DataSource ds){
		StudyDAO studyDAO = new StudyDAO(ds);
		return (StudyBean) studyDAO.findByPK(id);
	}
	@Override
	public ContainerRequestFilter getRequestFilter() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public ContainerResponseFilter getResponseFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	

	}
