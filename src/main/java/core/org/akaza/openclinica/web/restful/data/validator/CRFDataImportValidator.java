package core.org.akaza.openclinica.web.restful.data.validator;

import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.StudyBuildService;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.web.restful.data.bean.BaseStudyDefinitionBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import org.springframework.validation.Validator;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

public class CRFDataImportValidator implements Validator {

    DataSource dataSource;
    StudyDao studyDao;
    UserAccountDAO userAccountDAO;
    BaseVSValidatorImplementation helper;
    RestfulServiceHelper restfulServiceHelper;
    StudyBuildService studyBuildService;

    public RestfulServiceHelper getRestfulServiceHelper(){
    	if(restfulServiceHelper ==null) {
    		restfulServiceHelper = new RestfulServiceHelper(dataSource, studyBuildService, studyDao);
    	}
    	
    	return restfulServiceHelper;
    }
    public CRFDataImportValidator(DataSource dataSource, StudyDao studyDao, StudyBuildService studyBuildService) {
        this.dataSource = dataSource;
        this.studyDao = studyDao;
        this.studyBuildService = studyBuildService;
        helper = new BaseVSValidatorImplementation();
    }

    @SuppressWarnings("rawtypes")
    public boolean supports(Class clazz) {
       // return CRFDataImportBean.class.equals(clazz);
    	return BaseStudyDefinitionBean.class.equals(clazz);
    	
    }

    public void validate(Object obj, Errors e, HttpServletRequest request) {
    	//CRFDataImportBean crfDataImportBean = (CRFDataImportBean) obj;
    	BaseStudyDefinitionBean crfDataImportBean = (BaseStudyDefinitionBean) obj;
    	
        if (crfDataImportBean.getStudyUniqueId() == null ) {
        	 e.reject("studyEventDefinitionRequestValidator.study_does_not_exist");
             return;
        }
        Status[] included_status= new Status[]{Status.AVAILABLE ,  Status.PENDING};
        Study study = helper.verifyStudyByOID( studyDao, crfDataImportBean.getStudyUniqueId(), included_status, e);
        if (study == null) return; 
      
        String userName = crfDataImportBean.getUser().getName();
        String study_oid = study.getOc_oid();
        boolean isRoleVerified = this.getRestfulServiceHelper().verifyRole(userName, study_oid, null, e);
        if ( !isRoleVerified ) {
        	 /**
             * log error into log file 
             */ 
            String studySubjectOID = study_oid;
            String originalFileName = request.getHeader("originalFileName");
        	// sample file name like:originalFileName_123.txt,pipe_delimited_local_skip_2.txt
            if(originalFileName != null) {
            	String recordNum = null;
            	if(originalFileName !=null) {
            		recordNum = originalFileName.substring(originalFileName.lastIndexOf("_")+1,originalFileName.indexOf("."));
            		originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf("_"));
            	}
            	String msg = e.getAllErrors().get(0).getCode() +":" + e.getAllErrors().get(0).getDefaultMessage();
            	msg = recordNum + "|" + studySubjectOID + "|FAILED|" + msg;
        		this.getRestfulServiceHelper().getImportDataHelper().writeToMatchAndSkipLog(originalFileName, msg,request);
        		
            }
        	
        	
            return;
        }
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

    public UserAccountDAO getUserAccountDAO() {
        return this.userAccountDAO != null ? userAccountDAO : new UserAccountDAO(dataSource);
    }

    public void validate(Object obj, Errors e) {
    	//CRFDataImportBean crfDataImportBean = (CRFDataImportBean) obj;
    	BaseStudyDefinitionBean crfDataImportBean = (BaseStudyDefinitionBean) obj;
    	
        if (crfDataImportBean.getStudyUniqueId() == null ) {
        	 e.reject("studyEventDefinitionRequestValidator.study_does_not_exist");
             return;
        }
        Status[] included_status= new Status[]{Status.AVAILABLE ,  Status.PENDING};
        Study study = helper.verifyStudyByOID( studyDao, crfDataImportBean.getStudyUniqueId(), included_status, e);
        if (study == null) return; 
      
        String userName = crfDataImportBean.getUser().getName();
        String study_oid = study.getOc_oid();
        boolean isRoleVerified = this.getRestfulServiceHelper().verifyRole(userName, study_oid, null, e);
        if ( !isRoleVerified ) {
        	
        	
        	return;
        }

        crfDataImportBean.setStudy(study);

    }
}
