package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;

import javax.sql.DataSource;

public class EventService implements EventServiceInterface {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    SubjectDAO subjectDao;
    StudySubjectDAO studySubjectDao;
    UserAccountDAO userAccountDao;
    StudyEventDefinitionDAO studyEventDefinitionDao;
    StudyEventDAO studyEventDao;
    StudyDAO studyDao;
    DataSource dataSource;

    public EventService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public EventService(SessionManager sessionManager) {
        this.dataSource = sessionManager.getDataSource();
    }

    public HashMap<String, String> validateAndSchedule(String studySubjectId, String studyUniqueId, String siteUniqueId, String eventDefinitionOID,
            String location, Date startDateTime, Date endDateTime, UserAccountBean user) throws OpenClinicaSystemException {

        // Non Business Validation
        if (studyUniqueId == null || studyUniqueId.length() < 1) {
            logger.info("studyUniqueId is required.");
            throw new OpenClinicaSystemException("studyUniqueId is required.");
        }

        // Business Validation
        StudyBean study = getStudyDao().findByUniqueIdentifier(studyUniqueId);

        if (study == null) {
            logger.info("Study Not Found");
            throw new OpenClinicaSystemException("Study Not Found");
        }

        Boolean hasPriviledge = true;
        int parentStudyId = study.getId();
        StudyUserRoleBean role = user.getRoleByStudy(study);
        if (role.getId() == 0 || role.getRole().equals(Role.MONITOR)) {
            hasPriviledge = false;
        }

        if (siteUniqueId != null) {
            study = getStudyDao().findSiteByUniqueIdentifier(studyUniqueId, siteUniqueId);
        }

        if (!hasPriviledge) {
            role = user.getRoleByStudy(study);
            if (role.getId() == 0 || role.getRole().equals(Role.MONITOR)) {
                throw new OpenClinicaSystemException("You do not have sufficient priviliges to run this service");
            }
        }

        // Non Business Validation
        if (studySubjectId == null || studySubjectId.length() < 1) {
            logger.info("studySubjectId is required.");
            throw new OpenClinicaSystemException("studySubjectId is required.");
        }
        StudySubjectBean studySubject = getStudySubjectDao().findByLabelAndStudy(studySubjectId, study);
        if (studySubject == null) {
            logger.info("Study Subject Not Found");
            throw new OpenClinicaSystemException("Study Subject Not Found");
        }

        // Non Business Validation
        if (eventDefinitionOID == null || eventDefinitionOID.length() < 1) {
            logger.info("eventDefinitionOID is required.");
            throw new OpenClinicaSystemException("eventDefinitionOID is required.");
        }
        StudyEventDefinitionBean studyEventDefinition = getStudyEventDefinitionDao().findByOidAndStudy(eventDefinitionOID, study.getId(), parentStudyId);
        if (studyEventDefinition == null) {
            logger.info("Study Event Definition Not Found");
            throw new OpenClinicaSystemException("Study Event Definition Not Found");
        }

        if (startDateTime == null) {
            logger.info("startDateTime required");
            throw new OpenClinicaSystemException("startDateTime required");
        }
        if (location == null) {
            logger.info("location required");
            throw new OpenClinicaSystemException("location required");
        }

        Integer studyEventOrdinal = scheduleEvent(user, studySubject, studyEventDefinition, study, startDateTime, endDateTime, location);
        HashMap<String, String> h = new HashMap<String, String>();
        h.put("eventDefinitionOID", eventDefinitionOID);
        h.put("studyEventOrdinal", studyEventOrdinal.toString());
        h.put("studySubjectOID", studySubject.getOid());
        return h;

    }

    Integer scheduleEvent(UserAccountBean user, StudySubjectBean studySubject, StudyEventDefinitionBean studyEventDefinition, StudyBean study,
            Date startDateTime, Date endDateTime, String location) throws OpenClinicaSystemException {

        Integer studyEventOrdinal = null;
        if (canSubjectScheduleAnEvent(studyEventDefinition, studySubject)) {

            StudyEventBean studyEvent = new StudyEventBean();
            studyEvent.setStudyEventDefinitionId(studyEventDefinition.getId());
            studyEvent.setStudySubjectId(studySubject.getId());
            studyEvent.setLocation(location);
            studyEvent.setDateStarted(startDateTime);
            studyEvent.setDateEnded(endDateTime);
            studyEvent.setOwner(user);
            studyEvent.setStatus(Status.AVAILABLE);
            studyEvent.setSubjectEventStatus(SubjectEventStatus.SCHEDULED);
            studyEvent.setSampleOrdinal(getStudyEventDao().getMaxSampleOrdinal(studyEventDefinition, studySubject) + 1);
            studyEvent = (StudyEventBean) getStudyEventDao().create(studyEvent);
            studyEventOrdinal = studyEvent.getSampleOrdinal();

        } else {
            throw new OpenClinicaSystemException("Cannot schedule an event for this Subject");
        }
        return studyEventOrdinal;

    }

    public boolean canSubjectScheduleAnEvent(StudyEventDefinitionBean studyEventDefinition, StudySubjectBean studySubject) {

        if (studyEventDefinition.isRepeating()) {
            return true;
        }
        if (getStudyEventDao().findAllByDefinitionAndSubject(studyEventDefinition, studySubject).size() > 0) {
            return false;
        }
        return true;
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
     * @return the StudyEventDefinitionDao
     */
    public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        studyEventDefinitionDao = studyEventDefinitionDao != null ? studyEventDefinitionDao : new StudyEventDefinitionDAO(dataSource);
        return studyEventDefinitionDao;
    }

    /**
     * @return the StudyEventDao
     */
    public StudyEventDAO getStudyEventDao() {
        studyEventDao = studyEventDao != null ? studyEventDao : new StudyEventDAO(dataSource);
        return studyEventDao;
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