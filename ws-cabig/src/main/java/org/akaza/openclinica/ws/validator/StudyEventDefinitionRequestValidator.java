package org.akaza.openclinica.ws.validator;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.ws.bean.StudyEventDefinitionRequestBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.sql.DataSource;

public class StudyEventDefinitionRequestValidator implements Validator {

    DataSource dataSource;
    StudyDAO studyDAO;
    StudySubjectDAO studySubjectDAO;
    StudyEventDefinitionDAO studyEventDefinitionDAO;
    UserAccountDAO userAccountDAO;

    public StudyEventDefinitionRequestValidator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SuppressWarnings("rawtypes")
    public boolean supports(Class clazz) {
        return StudyEventDefinitionRequestBean.class.equals(clazz);
    }

    public void validate(Object obj, Errors e) {
        StudyEventDefinitionRequestBean studyEventDefinitionRequestBean = (StudyEventDefinitionRequestBean) obj;

        if (studyEventDefinitionRequestBean.getStudyUniqueId() == null && studyEventDefinitionRequestBean.getSiteUniqueId() == null) {
            e.reject("studyEventDefinitionRequestValidator.invalid_study_identifier");
            return;
        }
        if (studyEventDefinitionRequestBean.getStudyUniqueId() != null && studyEventDefinitionRequestBean.getSiteUniqueId() == null) {
            StudyBean study = getStudyDAO().findByUniqueIdentifier(studyEventDefinitionRequestBean.getStudyUniqueId());
            if (study == null) {
                e.reject("studyEventDefinitionRequestValidator.invalid_study_identifier");
                return;
            }
        }
        if (studyEventDefinitionRequestBean.getStudyUniqueId() != null && studyEventDefinitionRequestBean.getSiteUniqueId() == null) {
            StudyBean study = getStudyDAO().findByUniqueIdentifier(studyEventDefinitionRequestBean.getStudyUniqueId());
            if (study == null) {
                e.reject("studyEventDefinitionRequestValidator.invalid_study_identifier");
                return;
            }
            StudyUserRoleBean studySur = getUserAccountDAO().findRoleByUserNameAndStudyId(studyEventDefinitionRequestBean.getUser().getName(), study.getId());
            if (studySur.getStatus() != Status.AVAILABLE) {
                e.reject("studyEventDefinitionRequestValidator.insufficient_permissions",
                        "You do not have sufficient privileges to proceed with this operation.");
                return;
            }
        }
        if (studyEventDefinitionRequestBean.getStudyUniqueId() != null && studyEventDefinitionRequestBean.getSiteUniqueId() != null) {
            StudyBean study = getStudyDAO().findByUniqueIdentifier(studyEventDefinitionRequestBean.getStudyUniqueId());
            StudyBean site = getStudyDAO().findByUniqueIdentifier(studyEventDefinitionRequestBean.getSiteUniqueId());
            if (study == null || site == null || site.getParentStudyId() != study.getId()) {
                e.reject("studyEventDefinitionRequestValidator.invalid_study_identifier_site_identifier");
                return;
            }
            StudyUserRoleBean siteSur = getUserAccountDAO().findRoleByUserNameAndStudyId(studyEventDefinitionRequestBean.getUser().getName(), site.getId());
            if (siteSur.getStatus() != Status.AVAILABLE) {
                e.reject("studyEventDefinitionRequestValidator.insufficient_permissions",
                        "You do not have sufficient privileges to proceed with this operation.");
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

    public StudyEventDefinitionDAO getStudyEventDefinitionDAO() {
        return this.studyEventDefinitionDAO != null ? studyEventDefinitionDAO : new StudyEventDefinitionDAO(dataSource);
    }

    public UserAccountDAO getUserAccountDAO() {
        return this.userAccountDAO != null ? userAccountDAO : new UserAccountDAO(dataSource);
    }

}
