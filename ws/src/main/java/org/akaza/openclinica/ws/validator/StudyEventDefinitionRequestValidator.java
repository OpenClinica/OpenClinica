package org.akaza.openclinica.ws.validator;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.ws.bean.BaseStudyDefinitionBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.sql.DataSource;

public class StudyEventDefinitionRequestValidator extends AbstractValidator implements Validator {

    StudySubjectDAO studySubjectDAO;
    StudyEventDefinitionDAO studyEventDefinitionDAO;

    public StudyEventDefinitionRequestValidator(DataSource dataSource) {
        this.dataSource = dataSource;
        helper = new BaseVSValidatorImplementation();
    }

    @SuppressWarnings("rawtypes")
    public boolean supports(Class clazz) {
        return BaseStudyDefinitionBean.class.equals(clazz);
    }

    public void validate(Object obj, Errors e) {
    	BaseStudyDefinitionBean studyEventDefinitionRequestBean = (BaseStudyDefinitionBean) obj;

        if (studyEventDefinitionRequestBean.getStudyUniqueId() == null && studyEventDefinitionRequestBean.getSiteUniqueId() == null) {
            e.reject("studyEventDefinitionRequestValidator.invalid_study_identifier");
            return;
        }
        StudyBean study = null;int study_id = -1;
        if (studyEventDefinitionRequestBean.getStudyUniqueId() != null ) {
	        study = helper.verifyStudy(getStudyDAO(), studyEventDefinitionRequestBean.getStudyUniqueId(),
	        		null, e);
	        if ( study == null){ return; }
	        study_id = study.getId();
        }
        StudyBean site;int site_id = -1;
        if ( studyEventDefinitionRequestBean.getSiteUniqueId() != null) {
        	site = helper.verifySite(getStudyDAO(), studyEventDefinitionRequestBean.getStudyUniqueId(),
        			studyEventDefinitionRequestBean.getSiteUniqueId(), null, e);
        	if (site == null){return;}
        	site_id = site.getId();
        }
        // get public study for this schema study
        if (study != null) {
            StudyBean publicStudy = getPublicStudy(study.getIdentifier());
            study_id = publicStudy.getId();
        }
        boolean isRoleVerified = helper.verifyRole(studyEventDefinitionRequestBean.getUser(), study_id, site_id,  e);
    
    }

    public StudyDAO getStudyDAO() {
        return this.studyDAO != null ? studyDAO : new StudyDAO(dataSource);
    }

    public StudySubjectDAO getStudySubjectDAO() {
        return this.studySubjectDAO != null ? studySubjectDAO : new StudySubjectDAO(dataSource);
    }

    public StudyEventDefinitionDAO getStudyEventDefinitionDAO() {
        return this.studyEventDefinitionDAO != null ? studyEventDefinitionDAO : new StudyEventDefinitionDAO(dataSource);
    }

    public UserAccountDAO getUserAccountDAO() {
        return this.userAccountDAO != null ? userAccountDAO : new UserAccountDAO(dataSource);
    }

}
