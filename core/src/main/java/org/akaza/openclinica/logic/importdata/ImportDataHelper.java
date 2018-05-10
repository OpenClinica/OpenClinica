package org.akaza.openclinica.logic.importdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ImportDataHelper the entire focus of this piece of code is to generate the
 * necessary EventCRFBeans after uploading XML to the Database. Currently being
 * used by ImportCRFDataServlet. Created as part of refactoring efforts.
 * 
 * @author Tom Hickerson, 04/2008
 * @category logic classes
 */
public class ImportDataHelper {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    protected SessionManager sm;
    protected UserAccountBean ub;

    public void setSessionManager(SessionManager sm) {
        this.sm = sm;
    }

    public void setUserAccountBean(UserAccountBean ub) {
        this.ub = ub;
    }

    public EventCRFBean createEventCRF(HashMap<String, String> importedObject) {

        EventCRFBean eventCrfBean = null;

        int studyEventId = importedObject.get("study_event_id") == null ? -1 : Integer.parseInt(importedObject.get("study_event_id"));

        String crfVersionName = importedObject.get("crf_version_name") == null ? "" : importedObject.get("crf_version_name").toString();
        String crfName = importedObject.get("crf_name") == null ? "" : importedObject.get("crf_name").toString();

        String eventDefinitionCRFName = importedObject.get("event_definition_crf_name") == null ? ""
                : importedObject.get("event_definition_crf_name").toString();
        String subjectName = importedObject.get("subject_name") == null ? "" : importedObject.get("subject_name").toString();
        String studyName = importedObject.get("study_name") == null ? "" : importedObject.get("study_name").toString();

        logger.info("found the following: study event id " + studyEventId + ", crf version name " + crfVersionName + ", crf name " + crfName
                + ", event def crf name " + eventDefinitionCRFName + ", subject name " + subjectName + ", study name " + studyName);
        // << tbh
        int eventCRFId = 0;

        EventCRFDAO eventCrfDao = new EventCRFDAO(sm.getDataSource());
        StudyDAO studyDao = new StudyDAO(sm.getDataSource());
        StudySubjectDAO studySubjectDao = new StudySubjectDAO(sm.getDataSource());
        StudyEventDefinitionDAO studyEventDefinistionDao = new StudyEventDefinitionDAO(sm.getDataSource());
        CRFVersionDAO crfVersionDao = new CRFVersionDAO(sm.getDataSource());
        FormLayoutDAO fldao = new FormLayoutDAO(sm.getDataSource());
        StudyEventDAO studyEventDao = new StudyEventDAO(sm.getDataSource());
        CRFDAO crfdao = new CRFDAO(sm.getDataSource());
        SubjectDAO subjectDao = new SubjectDAO(sm.getDataSource());

        StudyBean studyBean = (StudyBean) studyDao.findByName(studyName);
        // .findByPK(studyId);

        // generate the subject bean first, so that we can have the subject id
        // below...
        SubjectBean subjectBean = subjectDao// .findByUniqueIdentifierAndStudy(subjectName,
                // studyBean.getId());
                .findByUniqueIdentifier(subjectName);

        StudySubjectBean studySubjectBean = studySubjectDao.findBySubjectIdAndStudy(subjectBean.getId(), studyBean);
        // .findByLabelAndStudy(subjectName, studyBean);
        logger.info("::: found study subject id here: " + studySubjectBean.getId() + " with the following: subject ID " + subjectBean.getId()
                + " study bean name " + studyBean.getName());

        StudyEventBean studyEventBean = (StudyEventBean) studyEventDao.findByPK(studyEventId);
        // TODO need to replace, can't really replace

        logger.info("found study event status: " + studyEventBean.getStatus().getName());

        // [study] event should be scheduled, event crf should be not started

        FormLayoutBean formLayout = (FormLayoutBean) fldao.findByFullName(crfVersionName, crfName);
        List<CRFVersionBean> crfVersions = crfVersionDao.findAllByCRFId(formLayout.getCrfId());
        CRFVersionBean crfVersion = crfVersions.get(0);
        // .findByPK(crfVersionId);
        // replaced by findByName(name, version)

        logger.info("found crf version name here: " + crfVersion.getName());

        EntityBean crf = crfdao.findByPK(crfVersion.getCrfId());

        logger.info("found crf name here: " + crf.getName());

        // trying it again up here since down there doesn't seem to work, tbh
        StudyEventDefinitionBean studyEventDefinitionBean = (StudyEventDefinitionBean) studyEventDefinistionDao.findByName(eventDefinitionCRFName);
        // .findByEventDefinitionCRFId(eventDefinitionCRFId);
        // replaced by findbyname

        if (studySubjectBean.getId() <= 0 && studyEventBean.getId() <= 0 && crfVersion.getId() <= 0 && studyBean.getId() <= 0
                && studyEventDefinitionBean.getId() <= 0) {
            logger.info("Throw an Exception, One of the provided ids is not valid");
        }

        // >> tbh repeating items:
        ArrayList eventCrfBeans = eventCrfDao.findByEventSubjectVersion(studyEventBean, studySubjectBean, crfVersion);
        // TODO repeating items here? not yet
        if (eventCrfBeans.size() > 1) {
            logger.info("found more than one");
        }
        if (!eventCrfBeans.isEmpty() && eventCrfBeans.size() == 1) {
            eventCrfBean = (EventCRFBean) eventCrfBeans.get(0);
            logger.info("This EventCrfBean was found");
        }
        if (!eventCrfBeans.isEmpty() && eventCrfBeans.size() > 1) {
            logger.info("Throw a System exception , result should either be 0 or 1");
        }

        if (eventCrfBean == null) {

            StudyBean studyWithSED = studyBean;
            if (studyBean.getParentStudyId() > 0) {
                studyWithSED = new StudyBean();
                studyWithSED.setId(studyBean.getParentStudyId());
            }

            AuditableEntityBean studyEvent = studyEventDao.findByPKAndStudy(studyEventId, studyWithSED);
            // TODO need to replace

            if (studyEvent.getId() <= 0) {
                logger.info("Hello Exception");
            }

            eventCrfBean = new EventCRFBean();
            // eventCrfBean.setCrf((CRFBean)crf);
            // eventCrfBean.setCrfVersion(crfVersion);
            if (eventCRFId == 0) {// no event CRF created yet
                // ???
                if (studyBean.getStudyParameterConfig().getInterviewerNameDefault().equals("blank")) {
                    eventCrfBean.setInterviewerName("");
                } else {
                    // default will be event's owner name
                    eventCrfBean.setInterviewerName(studyEventBean.getOwner().getName());
                }

                if (!studyBean.getStudyParameterConfig().getInterviewDateDefault().equals("blank")) {
                    if (studyEventBean.getDateStarted() != null) {
                        eventCrfBean.setDateInterviewed(studyEventBean.getDateStarted());// default
                        // date
                    } else {
                        // logger.info("evnet start date is null, so date
                        // interviewed is null");
                        eventCrfBean.setDateInterviewed(null);
                    }
                } else {
                    eventCrfBean.setDateInterviewed(null);
                }

                eventCrfBean.setAnnotations("");
                eventCrfBean.setCreatedDate(new Date());
                eventCrfBean.setCRFVersionId(crfVersion.getId());
                // eventCrfBean.setCrfVersion((CRFVersionBean)crfVersion);
                eventCrfBean.setOwner(ub);
                // eventCrfBean.setCrf((CRFBean)crf);
                eventCrfBean.setStatus(Status.AVAILABLE);
                eventCrfBean.setCompletionStatusId(1);
                // problem with the line below
                eventCrfBean.setStudySubjectId(studySubjectBean.getId());
                eventCrfBean.setStudyEventId(studyEventId);
                eventCrfBean.setValidateString("");
                eventCrfBean.setValidatorAnnotations("");
                eventCrfBean.setFormLayout(formLayout);

                try {
                    eventCrfBean = (EventCRFBean) eventCrfDao.create(eventCrfBean);
                    // TODO review
                    // eventCrfBean.setCrfVersion((CRFVersionBean)crfVersion);
                    // eventCrfBean.setCrf((CRFBean)crf);
                } catch (Exception ee) {
                    logger.info(ee.getMessage());
                    logger.info("throws with crf version id " + crfVersion.getId() + " and study event id " + studyEventId + " study subject id "
                            + studySubjectBean.getId());
                }
                // note that you need to catch an exception if the numbers are
                // bogus, ie you can throw an error here
                // however, putting the try catch allows you to pass which is
                // also bad
                // logger.info("CREATED EVENT CRF");
            } else {
                // there is an event CRF already, only need to update
                // is the status not started???

                logger.info("*** already-started event CRF with msg: " + eventCrfBean.getStatus().getName());
                if (eventCrfBean.getStatus().equals(Status.PENDING)) {
                    logger.info("Not Started???");
                }
                eventCrfBean = (EventCRFBean) eventCrfDao.findByPK(eventCRFId);
                eventCrfBean.setCRFVersionId(crfVersion.getId());

                eventCrfBean.setUpdatedDate(new Date());
                eventCrfBean.setUpdater(ub);
                eventCrfBean = (EventCRFBean) eventCrfDao.update(eventCrfBean);

                // eventCrfBean.setCrfVersion((CRFVersionBean)crfVersion);
                // eventCrfBean.setCrf((CRFBean)crf);
            }

            if (eventCrfBean.getId() <= 0) {
                logger.info("error");
            } else {
                // TODO change status here, tbh
                // 2/08 this part seems to work, tbh
                studyEventBean.setSubjectEventStatus(SubjectEventStatus.DATA_ENTRY_STARTED);
                studyEventBean.setUpdater(ub);
                studyEventBean.setUpdatedDate(new Date());
                studyEventDao.update(studyEventBean);

            }

        }
        eventCrfBean.setCrfVersion(crfVersion);
        eventCrfBean.setCrf((CRFBean) crf);

        // repeating?
        return eventCrfBean;
    }
}
