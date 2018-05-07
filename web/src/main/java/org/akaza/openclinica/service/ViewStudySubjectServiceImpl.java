package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.ViewStudySubjectDTO;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * This Service class is used with View Study Subject Page
 *
 * @author joekeremian
 */

public class ViewStudySubjectServiceImpl implements ViewStudySubjectService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private StudyDao studyDao;
    private UserAccountDao userAccountDao;
    private StudySubjectDao studySubjectDao;
    private CrfDao crfDao;
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    private StudyEventDao studyEventDao;
    private EventCrfDao eventCrfDao;
    private StudyEventDefinitionDao studyEventDefintionDao;
    private PageLayoutDao pageLayoutDao;

    public ViewStudySubjectServiceImpl(StudyDao studyDao, UserAccountDao userAccountDao, StudySubjectDao studySubjectDao, CrfDao crfDao,
                                       EventDefinitionCrfDao eventDefinitionCrfDao, StudyEventDao studyEventDao, EventCrfDao eventCrfDao, StudyEventDefinitionDao studyEventDefintionDao,
                                       PageLayoutDao pageLayoutDao) {
        super();
        this.studyDao = studyDao;
        this.userAccountDao = userAccountDao;
        this.studySubjectDao = studySubjectDao;
        this.crfDao = crfDao;
        this.eventDefinitionCrfDao = eventDefinitionCrfDao;
        this.studyEventDao = studyEventDao;
        this.eventCrfDao = eventCrfDao;
        this.studyEventDefintionDao = studyEventDefintionDao;
        this.pageLayoutDao = pageLayoutDao;
    }

    public ViewStudySubjectDTO addNewForm(HttpServletRequest request, String studyOid, String studyEventDefinitionOid, String crfOid, String studySubjectOid) {
        final String COMMON = "common";

        request.setAttribute("requestSchema", "public");
        HttpSession session = request.getSession();

        Study publicStudy = studyDao.findByOcOID(studyOid);

        UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
        if (ub == null) {
            logger.error("userAccount  is null");
            return null;
        }
        UserAccount userAccount = userAccountDao.findById(ub.getId());
        if (userAccount == null) {
            logger.error("userAccount  is null");
            return null;
        }

        request.setAttribute("requestSchema", publicStudy.getSchemaName());
        Study study = studyDao.findByOcOID(studyOid);

        if (study == null) {
            logger.error("Study  is null");
            return null;
        } else if (study.getStudy() == null) {
            logger.debug("the study with Oid {} is a Parent study", study.getOc_oid());
        } else {
            logger.debug("the study with Oid {} is a Site study", study.getOc_oid());
        }

        StudySubject studySubject = studySubjectDao.findByOcOID(studySubjectOid);
        if (studySubject == null) {
            logger.error("StudySubject is null");
            return null;
        }
        StudyEventDefinition studyEventDefinition = studyEventDefintionDao.findByOcOID(studyEventDefinitionOid);
        if (studyEventDefinition == null) {
            logger.error("StudyEventDefinition is null");
            return null;
        } else if (!studyEventDefinition.getType().equals(COMMON)) {
            logger.error("StudyEventDefinition with Oid {} is not a Common Type Event", studyEventDefinition.getOc_oid());
            return null;
        }
        CrfBean crf = crfDao.findByOcOID(crfOid);
        if (crf == null) {
            logger.error("Crf is null");
            return null;
        }

        EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(),
                crf.getCrfId(), study.getStudyId());
        if (edc == null) {
            edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(),
                    study.getStudy().getStudyId());
        }
        if (edc == null || edc.getStatusId().equals(Status.DELETED.getCode()) || edc.getStatusId().equals(Status.AUTO_DELETED.getCode())) {
            logger.error("EventDefinitionCrf for StudyEventDefinition Oid {},Crf Oid {} and Study Oid {}is null or has Removed Status",
                    studyEventDefinition.getOc_oid(), crf.getOcOid(), study.getOc_oid());
            return null;
        }
        FormLayout formLayout = edc.getFormLayout();
        if (formLayout == null) {
            logger.error("FormLayout is null");
            return null;
        }

        List<StudyEvent> studyEvents = studyEventDao.fetchListByStudyEventDefOID(studyEventDefinitionOid, studySubject.getStudySubjectId());
        Integer maxOrdinal;
        StudyEvent studyEvent;
        int eventCrfId = 0;
        EventCrf eventCrf = null;
        if (studyEvents.size() == 0) {
            logger.debug("No previous study event found for this studyEventDef Oid {} and subject Oid{}", studyEventDefinition.getOc_oid(),
                    studySubject.getOcOid());
            maxOrdinal = 0;
        } else {
            maxOrdinal = studyEventDao.findMaxOrdinalByStudySubjectStudyEventDefinition(studySubject.getStudySubjectId(),
                    studyEventDefinition.getStudyEventDefinitionId());
        }

        if (!studyEventDefinition.getRepeating()) {
            logger.debug("StudyEventDefinition with Oid {} is Non Repeating", studyEventDefinition.getOc_oid());
            for (StudyEvent stEvent : studyEvents) {
                eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(stEvent.getStudyEventId(), studySubject.getStudySubjectId(), formLayout.getFormLayoutId());
                if (eventCrf != null) {
                    eventCrfId = eventCrf.getEventCrfId();
                    logger.debug("EventCrf with StudyEventDefinition Oid {},Crf Oid {} and StudySubjectOid {} already exist in the System",
                            studyEventDefinition.getOc_oid(), crf.getOcOid(), studySubject.getOcOid());
                    break;
                }
            }
        }
        if (eventCrfId == 0) {
            // schedule new Study Event
            studyEvent = scheduleNewStudyEvent(studySubject, studyEventDefinition, maxOrdinal, userAccount);
        } else {
            // use existing study Event
            studyEvent = eventCrf.getStudyEvent();
        }


        String url = "/EnketoFormServlet?formLayoutId=" + formLayout.getFormLayoutId() + "&studyEventId=" + studyEvent.getStudyEventId()
                + "&eventCrfId=" + eventCrfId + "&originatingPage=ViewStudySubject%3Fid%3D" + studySubject.getStudySubjectId() + "&mode=edit";

        ViewStudySubjectDTO viewStudySubjectDTO = new ViewStudySubjectDTO();
        viewStudySubjectDTO.setUrl(url);
        return viewStudySubjectDTO;
    }

    public Page getPage(HttpServletRequest request, String studyOid, String name) {
        Page page = null;
        Study publicstudy = studyDao.findByOcOID(studyOid);
        request.setAttribute("requestSchema", publicstudy.getSchemaName());
        PageLayout pageLayout = pageLayoutDao.findByPageLayoutName(name);
        if (pageLayout != null) {
            page = (Page) SerializationUtils.deserialize(pageLayout.getDefinition());
            logger.info("Page Object retrieved from database with page name: {}", pageLayout.getName());
        }

        return page;
    }


    /**
     * populate new study event object and save in db
     *
     * @param studySubject
     * @param studyEventDefinition
     * @param maxOrdinal
     * @param userAccount
     * @return
     */
    private StudyEvent scheduleNewStudyEvent(StudySubject studySubject, StudyEventDefinition studyEventDefinition, Integer maxOrdinal,
                                             UserAccount userAccount) {
        StudyEvent studyEvent = new StudyEvent();
        studyEvent.setStudyEventDefinition(studyEventDefinition);
        studyEvent.setSampleOrdinal(maxOrdinal + 1);
        studyEvent.setSubjectEventStatusId(SubjectEventStatus.NOT_SCHEDULED.getCode());
        studyEvent.setStatusId(Status.AVAILABLE.getCode());
        studyEvent.setStudySubject(studySubject);
        studyEvent.setDateCreated(new Date());
        studyEvent.setUserAccount(userAccount);
        studyEvent.setDateStart(null);
        studyEvent.setStartTimeFlag(false);
        studyEvent.setEndTimeFlag(false);
        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
        return studyEvent;

    }

    public StudyDao getStudyDao() {
        return studyDao;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public UserAccountDao getUserAccountDao() {
        return userAccountDao;
    }

    public void setUserAccountDao(UserAccountDao userAccountDao) {
        this.userAccountDao = userAccountDao;
    }

    public StudySubjectDao getStudySubjectDao() {
        return studySubjectDao;
    }

    public void setStudySubjectDao(StudySubjectDao studySubjectDao) {
        this.studySubjectDao = studySubjectDao;
    }

    public CrfDao getCrfDao() {
        return crfDao;
    }

    public void setCrfDao(CrfDao crfDao) {
        this.crfDao = crfDao;
    }


    public StudyEventDao getStudyEventDao() {
        return studyEventDao;
    }

    public void setStudyEventDao(StudyEventDao studyEventDao) {
        this.studyEventDao = studyEventDao;
    }

    public EventCrfDao getEventCrfDao() {
        return eventCrfDao;
    }

    public void setEventCrfDao(EventCrfDao eventCrfDao) {
        this.eventCrfDao = eventCrfDao;
    }


}
