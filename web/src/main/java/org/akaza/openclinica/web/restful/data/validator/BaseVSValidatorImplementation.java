package org.akaza.openclinica.web.restful.data.validator;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.springframework.validation.Errors;

public class BaseVSValidatorImplementation implements BaseWSValidatorInterface{

	
	
	public StudyBean verifyStudy( StudyDAO dao, String study_id, Status[] included_status,
			 Errors errors){
		
		StudyBean study = dao.findByUniqueIdentifier(study_id);
        if (study == null) {
            errors.reject("studyEventTransferValidator.study_does_not_exist", new Object[] { study_id },
                    "Study identifier you specified " + study_id + " does not correspond to a valid study.");
            return null;
         }
        //validate study status
        if ( included_status != null){
        	for (Status cur_status: included_status)
        	{
        		if ( study.getStatus()== cur_status ){
        			return study;
        		}
        	}
        	//here status did not work
        	errors.reject("subjectTransferValidator.study_status_wrong", new Object[] { study_id }, "Study "
            		+ study_id +" has wrong status.");
        	return null;
        }
        return study;
      
	}
	
	public StudyBean verifyStudyByOID( StudyDAO dao, String study_id, Status[] included_status,
			 Errors errors){
		
		StudyBean study = dao.findByOid(study_id);
       if (study == null) {
           errors.reject("studyEventTransferValidator.study_does_not_exist", new Object[] { study_id },
                   "Study identifier you specified " + study_id + " does not correspond to a valid study.");
           return null;
        }
       //validate study status
       if ( included_status != null){
       	for (Status cur_status: included_status)
       	{
       		if ( study.getStatus()== cur_status ){
       			return study;
       		}
       	}
       	//here status did not work
       	errors.reject("subjectTransferValidator.study_status_wrong", new Object[] { study_id }, "Study "
           		+ study_id +" has wrong status.");
       	return null;
       }
       return study;
     
	}
	
	public StudyBean verifySite( StudyDAO dao, String study_id, String site_id, Status[] included_status,
			 Errors errors){
		
		 if ( site_id == null) return null;
		 StudyBean site = dao.findSiteByUniqueIdentifier(study_id, site_id);
         // verification 
         // go from here : study should be available or in design - verify
         if ( site == null){
     	            errors.reject("subjectTransferValidator.site_does_not_exist", new Object[] { site_id },
	                    "Site identifier you specified does not correspond to a valid site.");
	            return null;
	        }
         //validate study status
         if ( included_status != null){
         	for (Status cur_status: included_status)
         	{
         		if ( site.getStatus()== cur_status ){
         			return site;
         		}
         	}
         	//here status did not work
         	errors.reject("subjectTransferValidator.site_status_wrong", new Object[] { site_id }, "Site "
            		+ site.getName() +" has wrong status. Subject can be added to a site with 'AVAILABLE' or 'PENDING' status.");
         	return null;
         }
         return site;
         
	}
	public StudyBean verifyStudySubject(String study_id, String subjectId, int max_length, Errors errors){
		//verify that subjectId is not null
		
		//verify that subjectid<max_length if max_length>0
		
		//verify that subject id belongs to study
		return null;
	}
	public boolean verifyRole(UserAccountBean user,  int study_id,
			int site_id, Role excluded_role, Errors errors) {
		// TODO Auto-generated method stub
		// check for site role & user permission if ok -> return yes,
		//if no-> check for study permissions & role
		StudyUserRoleBean role = null;
		
		if ( site_id > -1){
			role = user.getRoleByStudy( site_id);
	        if (role.getId() != 0){
	        	if ( excluded_role == null || ( excluded_role != null && !role.getRole().equals(excluded_role))) {
	        		return true;
	        	}
	        }
		}
        role = user.getRoleByStudy( study_id);
        if (role.getId() != 0){
        	if ( excluded_role == null || ( excluded_role != null && !role.getRole().equals(excluded_role))) {
        		return true;
        	}
        }
	  errors.reject("studyEventTransferValidator.insufficient_permissions", "You do not have sufficient privileges to proceed with this operation.");
      return false;
        
	}
	public boolean verifyRole(UserAccountBean user, int study_id, int site_id,  Errors errors) {
		// TODO Auto-generated method stub
		// check for site role & user permission if ok -> return yes,
		//if no-> check for study permissions & role
		StudyUserRoleBean role = null;
		
		if ( site_id > -1){
			role = user.getRoleByStudy( site_id);
	        if (role.getId() != 0){
	        		return true;
	        	}
	        
		}
		
        role = user.getRoleByStudy( study_id);
        if (role.getId() != 0){
        	return true;
        }
	  errors.reject("studyEventTransferValidator.insufficient_permissions", "You do not have sufficient privileges to proceed with this operation.");
      return false;
        
	}
	
	
	public boolean verifyUser(UserAccountBean user, UserAccountDAO userAccountDao,int study_id, int site_id,  Errors errors) {
		// TODO Auto-generated method stub
		// check for site role & user permission if ok -> return yes,
		//if no-> check for study permissions & role
		StudyUserRoleBean siteSur;
		if ( site_id > -1){
			   siteSur = userAccountDao.findRoleByUserNameAndStudyId(user.getName(), site_id);
		       if (siteSur.getStatus() != Status.AVAILABLE) {
		           errors.reject("studyEventDefinitionRequestValidator.insufficient_permissions",
		                   "You do not have sufficient privileges to proceed with this operation.");
		           return false;
		       }
	        
		}
		  siteSur = userAccountDao.findRoleByUserNameAndStudyId(user.getName(), study_id);
	       if (siteSur.getStatus() != Status.AVAILABLE) {
	           errors.reject("studyEventDefinitionRequestValidator.insufficient_permissions",
	                   "You do not have sufficient privileges to proceed with this operation.");
	           return false;
	       }
	    return true;
        
	}
	
	   
	
}
