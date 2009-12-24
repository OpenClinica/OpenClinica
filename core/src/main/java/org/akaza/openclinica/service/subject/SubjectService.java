package org.akaza.openclinica.service.subject;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

public class SubjectService implements SubjectServiceInterface {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    SubjectDAO subjectDao;
    StudySubjectDAO studySubjectDao;
    UserAccountDAO userAccountDao;
    StudyDAO studyDao;
    DataSource dataSource;

    public SubjectService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SubjectService(SessionManager sessionManager) {
        this.dataSource = sessionManager.getDataSource();
    }

    public boolean validate(SubjectTransferBean subjectTransferBean) throws OpenClinicaSystemException {
        String studySubjectId = subjectTransferBean.getStudySubjectId();
        if (studySubjectId == null || studySubjectId.length() < 1) {
            logger.info("studySubjectId is required.");
            throw new OpenClinicaSystemException("studySubjectId is required.");
            // return false;
        } else if (studySubjectId.length() > 30) {
            throw new OpenClinicaSystemException("studySubjectId should not be longer than 30.");
            // return false;
        }
        StudyDAO stdao = new StudyDAO(this.getDataSource());
        StudyBean study = stdao.findByUniqueIdentifier(subjectTransferBean.getStudyOid());
        if (study == null) {
            throw new OpenClinicaSystemException("Study you specified does not exist");
        }

        UserAccountBean ua = subjectTransferBean.getOwner();
        StudyUserRoleBean role = ua.getRoleByStudy(study);
        if (role.getId() == 0 || role.getRole().equals(Role.MONITOR)) {
            throw new OpenClinicaSystemException("You do not have sufficient priviliges to run this service");
        }

        if (subjectTransferBean.getSiteIdentifier() != null) {
            study = stdao.findSiteByUniqueIdentifier(subjectTransferBean.getStudyOid(), subjectTransferBean.getSiteIdentifier());
        }
        subjectTransferBean.setStudy(study);
        if (study == null) {
            throw new OpenClinicaSystemException("Site you specified does not exist");
        }
        int handleStudyId = study.getParentStudyId() > 0 ? study.getParentStudyId() : study.getId();
        org.akaza.openclinica.dao.service.StudyParameterValueDAO spvdao = new StudyParameterValueDAO(this.getDataSource());
        StudyParameterValueBean studyParameter = spvdao.findByHandleAndStudy(handleStudyId, "subjectPersonIdRequired");
        String personId = subjectTransferBean.getPersonId();
        if ("required".equals(studyParameter.getValue()) && (personId == null || personId.length() < 1)) {
            throw new OpenClinicaSystemException("personId is required for the study: " + study.getName());
            // return false;
        }

        if (personId != null && personId.length() > 255) {
            throw new OpenClinicaSystemException("personId should not be longer than 255.");
            // return false;
        }

        String idSetting = "";
        StudyParameterValueBean subjectIdGenerationParameter = spvdao.findByHandleAndStudy(handleStudyId, "subjectIdGeneration");
        idSetting = subjectIdGenerationParameter.getValue();
        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
            int nextLabel = getStudySubjectDao().findTheGreatestLabel() + 1;
            subjectTransferBean.setStudySubjectId(new Integer(nextLabel).toString());
        }

        String secondaryId = subjectTransferBean.getSecondaryId();
        if (secondaryId != null && secondaryId.length() > 30) {
            throw new OpenClinicaSystemException("secondaryId should not be longer than 30.");
            // return false;
        }
        String gender = subjectTransferBean.getGender() + "";
        studyParameter = spvdao.findByHandleAndStudy(handleStudyId, "genderRequired");
        if ("true".equals(studyParameter.getValue()) && (gender == null || gender.length() < 1)) {
            throw new OpenClinicaSystemException("gender is required for the study:" + study.getName());
            // return false;
        }

        Date dateOfBirth = subjectTransferBean.getDateOfBirth();
        String yearOfBirth = subjectTransferBean.getYearOfBirth();
        studyParameter = spvdao.findByHandleAndStudy(handleStudyId, "collectDob");
        if ("1".equals(studyParameter.getValue()) && (dateOfBirth == null)) {
            throw new OpenClinicaSystemException("date Of Birth is required:" + study.getName());
            // return false;
        } else if ("2".equals(studyParameter.getValue()) && (yearOfBirth == null)) {
            throw new OpenClinicaSystemException("Year Of Birth is required:" + study.getName());
        } else if ("2".equals(studyParameter.getValue()) && (yearOfBirth != null)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                subjectTransferBean.setDateOfBirth(sdf.parse(subjectTransferBean.getYearOfBirth()));
            } catch (ParseException e) {
                throw new OpenClinicaSystemException("Year Of Birth not Valid:" + study.getName());
            }
        }

        Date enrollmentDate = subjectTransferBean.getEnrollmentDate();
        if (enrollmentDate == null) {
            throw new OpenClinicaSystemException("enrollmentDate is required.");
            // return false;
        } else {
            if ((new Date()).compareTo(enrollmentDate) < 0) {
                throw new OpenClinicaSystemException("enrollmentDate should be in the past.");
                // return false;
            }
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.subject.SubjectServiceInterface#createSubject(org.akaza.openclinica.bean.submit.SubjectBean,
     * org.akaza.openclinica.bean.managestudy.StudyBean)
     */
    public String createSubject(SubjectBean subjectBean, StudyBean studyBean, Date enrollmentDate, String secondaryId) {
        //studyBean = getStudyDao().findByUniqueIdentifier(studyBean.getIdentifier()); // Need to
        if (subjectBean.getUniqueIdentifier() != null && getSubjectDao().findByUniqueIdentifier(subjectBean.getUniqueIdentifier()).getId() != 0) {
            subjectBean = getSubjectDao().findByUniqueIdentifier(subjectBean.getUniqueIdentifier());
        } else {
            subjectBean.setStatus(Status.AVAILABLE);
            subjectBean = getSubjectDao().create(subjectBean);
        }

        StudySubjectBean studySubject = createStudySubject(subjectBean, studyBean, enrollmentDate, secondaryId);
        getStudySubjectDao().createWithoutGroup(studySubject);
        return studySubject.getLabel();
    }

    private StudySubjectBean createStudySubject(SubjectBean subject, StudyBean studyBean, Date enrollmentDate, String secondaryId) {
        StudySubjectBean studySubject = new StudySubjectBean();
        studySubject.setSecondaryLabel(secondaryId);
        studySubject.setOwner(getUserAccount());
        studySubject.setEnrollmentDate(enrollmentDate);
        studySubject.setLabel(subject.getLabel());
        subject.setLabel(null);
        studySubject.setSubjectId(subject.getId());
        studySubject.setStudyId(studyBean.getId());
        studySubject.setStatus(Status.AVAILABLE);
        return studySubject;

    }

    public void validateSubjectTransfer(SubjectTransferBean subjectTransferBean) {
        // TODO: Validate here
    }

    /**
     * Getting the first user account from the database. This would be replaced by an authenticated user who is doing the SOAP requests .
     * 
     * @return UserAccountBean
     */
    private UserAccountBean getUserAccount() {

        UserAccountBean user = new UserAccountBean();
        user.setId(1);
        return user;
    }

    /**
     * @return the subjectDao
     */
    public SubjectDAO getSubjectDao() {
        subjectDao = subjectDao != null ? subjectDao : new SubjectDAO(dataSource);
        return subjectDao;
    }

    /**
     * @return the subjectDao
     */
    public StudyDAO getStudyDao() {
        studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
        return studyDao;
    }

    /**
     * @return the subjectDao
     */
    public StudySubjectDAO getStudySubjectDao() {
        studySubjectDao = studySubjectDao != null ? studySubjectDao : new StudySubjectDAO(dataSource);
        return studySubjectDao;
    }

    /**
     * @return the UserAccountDao
     */
    public UserAccountDAO getUserAccountDao() {
        userAccountDao = userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
        return userAccountDao;
    }

    /**
     * @return the datasource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param datasource
     *            the datasource to set
     */
    public void setDatasource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}