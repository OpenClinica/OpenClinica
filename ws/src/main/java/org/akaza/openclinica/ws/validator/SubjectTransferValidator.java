package org.akaza.openclinica.ws.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.ws.bean.SubjectStudyDefinitionBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class SubjectTransferValidator implements Validator {

    DataSource dataSource;
    StudyDAO studyDAO;
    SubjectDAO subjectDao;
    StudySubjectDAO studySubjectDAO;
    StudyParameterValueDAO studyParameterValueDAO;
    UserAccountDAO userAccountDAO;
    BaseVSValidatorImplementation helper;

    public SubjectTransferValidator(DataSource dataSource) {
        this.dataSource = dataSource;
        helper = new BaseVSValidatorImplementation();
    }

    @Override
    public boolean supports(Class clazz) {
        return SubjectTransferBean.class.equals(clazz);
    }

    public void validateIsSubjectExists(Object obj, Errors e) {
    	SubjectStudyDefinitionBean subjectStudyBean = (SubjectStudyDefinitionBean) obj;

        if (subjectStudyBean.getStudyUniqueId() == null ) {
            e.reject("studyEventDefinitionRequestValidator.study_does_not_exist");
            return;
        }
        Status[] included_status= new Status[]{Status.AVAILABLE };
        StudyBean study = helper.verifyStudy(getStudyDAO(), subjectStudyBean.getStudyUniqueId(), included_status, e);
        if (study == null) return;
        subjectStudyBean.setStudy(study);
        StudyBean site = null;int site_id = -1;
        
        if (subjectStudyBean.getSiteUniqueId() != null) {
        	site = helper.verifySite(getStudyDAO(), subjectStudyBean.getStudyUniqueId(), subjectStudyBean.getSiteUniqueId(), included_status, e);
            if (site == null) { return;	        }
            site_id = site.getId();
            subjectStudyBean.setStudy(site);
 	      
        }
        boolean isRoleVerified =  helper.verifyRole(subjectStudyBean.getUser(), study.getId(), site_id, e);//.verifyUser(subjectStudyBean.getUser(), getUserAccountDAO(), study.getId(), site_id,   e) ;
       if ( !isRoleVerified){ return;}
//        StudyBean study = getStudyDAO().findByUniqueIdentifier(subjectStudyBean.getStudyUniqueId());
//        if (study == null) {
//            e.reject("subjectTransferValidator.study_does_not_exist", new Object[] { subjectStudyBean.getStudyUniqueId() }, "Study identifier you specified "
//                + subjectStudyBean.getStudyUniqueId() + " does not correspond to a valid study.");
//            return;
//        }
//        else{        subjectStudyBean.setStudy(study);}
//   
        
        
        
//        StudyBean site = null;
//        if (subjectStudyBean.getSiteUniqueId() != null) {
//            site = getStudyDAO().findSiteByUniqueIdentifier(subjectStudyBean.getStudyUniqueId(), subjectStudyBean.getSiteUniqueId());
// 	        if (site == null) {
//	            e.reject("subjectTransferValidator.site_does_not_exist", new Object[] { subjectStudyBean.getSiteUniqueId() },
//	                    "Site identifier you specified does not correspond to a valid site.");
//	            return;
//	        }
// 	       subjectStudyBean.setStudy(site);
//       }
        
//        UserAccountBean ua = subjectStudyBean.getUser();
//        StudyUserRoleBean role = ua.getRoleByStudy(study);
//        if (role.getId() == 0 ) {
//            e.reject("subjectTransferValidator.insufficient_permissions", "You do not have sufficient privileges to proceed with this operation.");
//            return;
//        }
//        
        
        
        
        
        
        String studySubjectId = subjectStudyBean.getSubjectLabel();
        if (studySubjectId == null || studySubjectId.length() < 1) {
            e.reject("subjectTransferValidator.studySubjectId_required");
            return;
        } else if (studySubjectId.length() > 30) {
            e.reject("subjectTransferValidator.studySubjectId_invalid_length", new Object[] { studySubjectId }, "studySubjectId: " + studySubjectId
                + " cannot be longer than 30 characters.");
            return;
        } 
        
        StudySubjectBean studySubject = getStudySubjectDAO().findByLabelAndStudy(subjectStudyBean.getSubjectLabel(), subjectStudyBean.getStudy());
      
        //it is not null but label null
        if (studySubject == null || studySubject.getOid()== null) {
            e.reject("studyEventTransferValidator.study_subject_does_not_exist", new Object[] { subjectStudyBean.getSubjectLabel(), subjectStudyBean.getStudy().getName() },
                    "StudySubject label you specified " + subjectStudyBean.getSubjectLabel() + " does not correspond to a study "+subjectStudyBean.getStudy().getName());
         
            return;
        }
        else
        {
        	subjectStudyBean.setSubjectOIDId(studySubject.getOid());
        }
        	
    	
    }
    public StudyBean getPublicStudy(String uniqueId) {
        StudyDAO studyDAO = new StudyDAO(dataSource);
        String studySchema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchema("public");
        StudyBean study = studyDAO.findByUniqueIdentifier(uniqueId);
        CoreResources.setRequestSchema(studySchema);
        return study;
    }
    @Override
    public void validate(Object obj, Errors e) {
        SubjectTransferBean subjectTransferBean = (SubjectTransferBean) obj;

        if (subjectTransferBean.getStudyOid() == null) {
            e.reject("studyEventTransferValidator.study_does_not_exist");
            return;
        }
        
        Status[] included_status= new Status[]{Status.AVAILABLE };
        StudyBean study = helper.verifyStudy(getStudyDAO(), subjectTransferBean.getStudyOid(), included_status, e);
        if (study == null) return;
        StudyBean site = null;int site_id = -1;
        subjectTransferBean.setStudy(study);
    
        if (subjectTransferBean.getSiteIdentifier() != null) {
        	site = helper.verifySite(getStudyDAO(), subjectTransferBean.getStudyOid(),subjectTransferBean.getSiteIdentifier(), included_status, e);
            if (site == null) { return;	        }
            site_id = site.getId();
            subjectTransferBean.setStudy(site);
        }
        // get public study for this schema study
        StudyBean publicStudy = getPublicStudy(study.getIdentifier());
        boolean isRoleVerified = helper.verifyRole(subjectTransferBean.getOwner(), publicStudy.getId(), site_id, Role.MONITOR, e);
        if ( !isRoleVerified){ return;}

        int handleStudyId = study.getParentStudyId() > 0 ? study.getParentStudyId() : study.getId();
        StudyParameterValueBean studyParameter = getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "subjectPersonIdRequired");
        String personId = subjectTransferBean.getPersonId();

        //personId 3 cases: 
//        	a. requiered: personId != null && personId.length() > 255
//        	b. optional: can be provided but can be missed
//        	c. not-used: personId==null
        
        if (personId.contains("<") || personId.contains(">")) {
            e.reject("subjectTransferValidator.person_id_can_not_contain_html_lessthan_or_greaterthan_elements");
            return;
        }

        if ("required".equals(studyParameter.getValue()) && (personId == null || personId.length() < 1)) {
            e.reject("subjectTransferValidator.personId_required", new Object[] { study.getName() }, "personId is required for the study: " + study.getName());
            return;
        }
        if ("not used".equals(studyParameter.getValue()) && !(personId == null ||  personId.length() <1 )) {
            e.reject("subjectTransferValidator.personId_not_used", new Object[] { study.getName() }, "personId is not used for the study: " + study.getName());
            return;
        }

        if (personId != null && personId.length() > 255) {
            e.reject("subjectTransferValidator.personId_invalid_length", new Object[] { personId }, "personId: " + personId
                + " cannot be longer than 255 characters.");
            return;
        }
// verify that personId is unique 
         if (subjectTransferBean.getPersonId() != null && subjectTransferBean.getPersonId().length()>0){
//	         SubjectBean subjectWithSamePersonId = getSubjectDao().findByUniqueIdentifierAndStudy( subjectTransferBean.getPersonId(), study.getId());
	         SubjectBean subjectWithSamePersonId = getSubjectDao().findByUniqueIdentifierAndAnyStudy( subjectTransferBean.getPersonId(), study.getId());
        		
		   if ( subjectWithSamePersonId.getId() !=0 ) {              
		   	 
		   		 e.reject("subjectTransferValidator.personId_duplicated", new Object[] { personId }, 
		   				 "A subject with the Person ID: "+personId+" is already enrolled in this study. ");
		   	     return;
		   	 }
         
         }

        
        StudyParameterValueBean subjectIdGenerationParameter = getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "subjectIdGeneration");
        String idSetting = subjectIdGenerationParameter.getValue();
        if (!(idSetting.equals("auto editable") || idSetting.equals("auto non-editable"))) {
        	String studySubjectId = subjectTransferBean.getStudySubjectId();
            if (studySubjectId == null || studySubjectId.length() < 1) {
                e.reject("subjectTransferValidator.studySubjectId_required");
                return;
            } else if (studySubjectId.length() > 30) {
                e.reject("subjectTransferValidator.studySubjectId_invalid_length", new Object[] { studySubjectId }, "studySubjectId: " + studySubjectId
                    + " cannot be longer than 30 characters.");
                return;
            } else    
            {  // checks whether there is a subject with same id inside current study
            	StudySubjectBean subjectWithSame = getStudySubjectDAO().findByLabelAndStudy(studySubjectId, study);
            	 
            	 if ( subjectWithSame.getLabel().equals(studySubjectId) )
            	 {
            		 e.reject("subjectTransferValidator.subject_duplicated_label", new Object[] { studySubjectId, study.getIdentifier() }, 
            				 "studySubjectId: " + studySubjectId
            	                + " already exists for "+study.getIdentifier() +" study .");
            	            return;
            	 }
            }

            if (studySubjectId.contains("<") || studySubjectId.contains(">")) {
                e.reject("subjectTransferValidator.study_subject_id_can_not_contain_html_lessthan_or_greaterthan_elements");
                return;
            }

        }

        String secondaryId = subjectTransferBean.getSecondaryId();
        if (secondaryId != null && secondaryId.length() > 30) {
            e.reject("subjectTransferValidator.secondaryId_invalid_length", new Object[] { secondaryId }, "secondaryId: " + secondaryId
                + " cannot be longer than 30 characters.");
            return;
        }
        if (secondaryId.contains("<") || secondaryId.contains(">")) {
            e.reject("subjectTransferValidator.secondary_id_can_not_contain_html_lessthan_or_greaterthan_elements");
            return;
        }

        String gender = String.valueOf(subjectTransferBean.getGender());
        studyParameter = getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "genderRequired");
        if ("true".equals(studyParameter.getValue()) ) {
        	if(gender == null || gender.length() < 1) {
            e.reject("subjectTransferValidator.gender_required", new Object[] { study.getName() }, "Gender is required for the study: " + study.getName());
            return;
	        }
        	if (!"m".equals(gender) && !"f".equals(gender)) {
                   e.reject("subjectTransferValidator.gender_is_m_or_f");
                   return;
            } 
	    }
        else{
        	if (gender.trim().length() > 0 && !("m".equals(gender) || "f".equals(gender))) {
                e.reject("subjectTransferValidator.gender_is_m_or_f");
                return;
            } 
        }
       

        Date dateOfBirth = subjectTransferBean.getDateOfBirth();
        String yearOfBirth = subjectTransferBean.getYearOfBirth();
        studyParameter = getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "collectDob");
        if ("1".equals(studyParameter.getValue()) && (dateOfBirth == null)) {
            e.reject("subjectTransferValidator.dateOfBirth_required", new Object[] { study.getName() },
                    "Date of birth is required for the study " + study.getName());
            return;
        } else if ("2".equals(studyParameter.getValue()) && (yearOfBirth == null)) {
            e.reject("subjectTransferValidator.yearOfBirth_required", new Object[] { study.getName() },
                    "Year of birth is required for the study " + study.getName());
            return;
        } else if ("2".equals(studyParameter.getValue()) && (yearOfBirth != null)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                subjectTransferBean.setDateOfBirth(sdf.parse(subjectTransferBean.getYearOfBirth()));
            } catch (ParseException xe) {
                e.reject("subjectTransferValidator.yearOfBirth_invalid", new Object[] { yearOfBirth }, "Year of birth: " + yearOfBirth + " is not valid");
                return;
            }
            //not used
        }else if ("3".equals(studyParameter.getValue())) {
        	if( dateOfBirth != null  ||  !(yearOfBirth == null || yearOfBirth.length()<1)) {
	            e.reject("subjectTransferValidator.date_of_birth_notused", new Object[] { study.getName() },
	                    "Date of Birth is not used for the study " + study.getName());
	            return;
        } }

        Date enrollmentDate = subjectTransferBean.getEnrollmentDate();
        if (enrollmentDate == null) {
            e.reject("subjectTransferValidator.enrollmentDate_required");
            return;
        } else {
            if ((new Date()).compareTo(enrollmentDate) < 0) {
                e.reject("subjectTransferValidator.enrollmentDate_should_be_in_past");
                return;
            }
        }
    }

    
    
    public StudyDAO getStudyDAO() {
        return this.studyDAO != null ? studyDAO : new StudyDAO(dataSource);
    }

    public StudySubjectDAO getStudySubjectDAO() {
        return this.studySubjectDAO != null ? studySubjectDAO : new StudySubjectDAO(dataSource);
    }

    public StudyParameterValueDAO getStudyParameterValueDAO() {
        return this.studyParameterValueDAO != null ? studyParameterValueDAO : new StudyParameterValueDAO(dataSource);
    }
    public UserAccountDAO getUserAccountDAO() {
        return this.userAccountDAO != null ? userAccountDAO : new UserAccountDAO(dataSource);
    }
    public SubjectDAO getSubjectDao() {
       return this.subjectDao != null ? subjectDao : new SubjectDAO(dataSource);
        
    }
}
