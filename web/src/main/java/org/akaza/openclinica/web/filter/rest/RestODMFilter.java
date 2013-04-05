package org.akaza.openclinica.web.filter.rest;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.odmbeans.UserBean;
import org.akaza.openclinica.control.SpringServletAccess;
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
	  
	WebApplicationContext context;
	String studyOIDS;
	

	
	@Override
	
	public ContainerRequest filter(ContainerRequest containerRequest) {
		UserAccountBean userBean = (UserAccountBean)request.getSession().getAttribute("userBean");	
		if(userBean.isSysAdmin())
			return containerRequest;
		else{
		String studyOID = containerRequest.getPathSegments().get(3).getPath();
		
		//parse to get studyOID
		StudyBean studyBean = getStudyByOID(studyOID,getDataSource());
		
		
		if(checkAuth(studyBean,userBean)) return containerRequest;
		else
		{
			if(studyBean.getParentStudyId()!=0){
			int parentStudyID = studyBean.getParentStudyId();
			studyBean = getStudyByID(parentStudyID,getDataSource());
			 
			if(checkAuth(studyBean,userBean))return containerRequest;
		}
		
		}   
		}
		throw new WebApplicationException(Response.Status.FORBIDDEN);

	}

	
	
	private Boolean checkAuth(StudyBean studyBean,UserAccountBean userBean){
		Boolean auth = false;
		StudyUserRoleBean studyRole = getRoleByStudy(studyBean,getDataSource(),userBean);
		Role r = studyRole.getRole();
			if (r != null) {
            // r = userBean.getActiveStudyRole();
            if (r != null && (r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR) ||
                    r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT) ||r.equals(Role.MONITOR) )) {
                return true;
            }
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
