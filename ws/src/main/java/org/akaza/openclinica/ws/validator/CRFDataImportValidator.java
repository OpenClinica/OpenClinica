package org.akaza.openclinica.ws.validator;


import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.ws.bean.BaseStudyDefinitionBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.sql.DataSource;

public class CRFDataImportValidator extends AbstractValidator implements Validator {

    public CRFDataImportValidator(DataSource dataSource) {
        this.dataSource = dataSource;
        helper = new BaseVSValidatorImplementation();
    }

    @SuppressWarnings("rawtypes")
    public boolean supports(Class clazz) {
       // return CRFDataImportBean.class.equals(clazz);
    	return BaseStudyDefinitionBean.class.equals(clazz);
    	
    }

    public void validate(Object obj, Errors e) {
    	//CRFDataImportBean crfDataImportBean = (CRFDataImportBean) obj;
    	BaseStudyDefinitionBean crfDataImportBean = (BaseStudyDefinitionBean) obj;
    	
        if (crfDataImportBean.getStudyUniqueId() == null ) {
        	 e.reject("studyEventDefinitionRequestValidator.study_does_not_exist");
             return;
        }
        Status[] included_status= new Status[]{Status.AVAILABLE ,  Status.PENDING};
        StudyBean study = helper.verifyStudyByOID( getStudyDAO(), crfDataImportBean.getStudyUniqueId(), included_status, e);
        if (study == null) return; 
        boolean isRoleVerified = helper.verifyRole(crfDataImportBean.getUser(), study.getId(), -1, Role.MONITOR, e);
        if ( !isRoleVerified ) return;
//        StudyBean study = getStudyDAO().findByOid(crfDataImportBean.getStudyUniqueId());
//        if (study == null) {
//        	  e.reject("subjectTransferValidator.study_does_not_exist", new Object[] { crfDataImportBean.getStudyUniqueId() }, "Study identifier you specified "
//                      + crfDataImportBean.getStudyUniqueId() + " does not correspond to a valid study.");
//              return;  
//        }
//      //validate study status
//        if ( !( study.getStatus().isAvailable() ||  study.getStatus().isPending() )) {
//            e.reject("subjectTransferValidator.study_status_wrong", new Object[] { crfDataImportBean.getStudyUniqueId() }, "Study "
//            		+ crfDataImportBean.getStudyUniqueId() +" has wrong status.");
//            return;
//        }
//       //not monitor
//        StudyUserRoleBean role = crfDataImportBean.getUser().getRoleByStudy(study);
//        if (role.getId() == 0 || role.getRole().equals(Role.MONITOR) || role.getStatus() != Status.AVAILABLE) {
//        	 e.reject("studyEventDefinitionRequestValidator.insufficient_permissions",
//             "You do not have sufficient privileges to proceed with this operation.");
//        	 return;
//        }
//      
        crfDataImportBean.setStudy(study);

    }

    public StudyDAO getStudyDAO() {
        return this.studyDAO != null ? studyDAO : new StudyDAO(dataSource);
    }

   

    public UserAccountDAO getUserAccountDAO() {
        return this.userAccountDAO != null ? userAccountDAO : new UserAccountDAO(dataSource);
    }

}
