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

public class StudyMetadataRequestValidator extends AbstractValidator implements Validator {

    StudySubjectDAO studySubjectDAO;
    StudyEventDefinitionDAO studyEventDefinitionDAO;

    public StudyMetadataRequestValidator(DataSource dataSource) {
        this.dataSource = dataSource;
        helper = new BaseVSValidatorImplementation();
    }

    @SuppressWarnings("rawtypes")
    public boolean supports(Class clazz) {
        return BaseStudyDefinitionBean.class.equals(clazz);
    }

    public void validate(Object obj, Errors e) {
    	BaseStudyDefinitionBean studyMetadataRequest = (BaseStudyDefinitionBean) obj;

        if (studyMetadataRequest.getStudyUniqueId() == null ){//&& studyMetadataRequest.getSiteUniqueId() == null) {
        	 e.reject("studyEventDefinitionRequestValidator.study_does_not_exist");
             return;
        }
        StudyBean study = helper.verifyStudy(getStudyDAO(), studyMetadataRequest.getStudyUniqueId(), null, e);
        if (study == null) return;
        int site_id = -1;StudyBean site;
        if (studyMetadataRequest.getSiteUniqueId() != null) {
        	site = helper.verifySite(getStudyDAO(), studyMetadataRequest.getStudyUniqueId(), studyMetadataRequest.getSiteUniqueId(), null, e);
       
        	if ( site!=null){site_id = site.getId();}
        }
        helper.verifyUser(studyMetadataRequest.getUser(), getUserAccountDAO(), study.getId(), site_id,   e) ;
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
