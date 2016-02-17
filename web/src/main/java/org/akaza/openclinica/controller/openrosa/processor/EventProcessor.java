package org.akaza.openclinica.controller.openrosa.processor;

import java.util.Date;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.dao.hibernate.CompletionStatusDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.datamap.SubjectEventStatus;
import org.akaza.openclinica.domain.user.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class EventProcessor implements Processor, Ordered {

    @Autowired
    StudyEventDao studyEventDao;
    
    @Autowired
    StudyEventDefinitionDao studyEventDefinitionDao;
    
    @Autowired
    EventCrfDao eventCrfDao;
    
    @Autowired
    CrfVersionDao crfVersionDao;
    
    @Autowired
    CompletionStatusDao completionStatusDao;
    
    @Autowired
    EventDefinitionCrfDao eventDefinitionCrfDao;
    
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public void process(SubmissionContainer container) throws Exception {
        System.out.println("Executing Event Processor.");
        Errors errors = container.getErrors();

        //Create study event if it doesn't exist
        StudySubject studySubject = container.getSubject();
        StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByStudyEventDefinitionId(Integer.valueOf(container.getSubjectContext().get("studyEventDefinitionID")));
        Integer ordinal = Integer.valueOf(container.getSubjectContext().get("studyEventOrdinal"));
        StudyEvent existingEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDefinition.getOc_oid(),ordinal,studySubject.getStudySubjectId());
        if (existingEvent == null) {
            container.setStudyEvent(createStudyEvent(studySubject,studyEventDefinition,ordinal,container.getUser()));
        } else container.setStudyEvent(existingEvent);

        //Create event crf if it doesn't exist
        if (studyEventDefinition.getStatus() != Status.AVAILABLE) {
            logger.info("This Crf Version has a Status Not available in this Study Event Defn");
            errors.reject("This Crf Version has a Status Not available in this Study Event Defn");
            throw new Exception("This Crf Version has a Status Not available in this Study Event Defn");
        }
        
        CrfVersion crfVersion = crfVersionDao.findByOcOID(container.getSubjectContext().get("crfVersionOID"));
        EventCrf existingEventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdCrfId(container.getStudyEvent().getStudyEventId(), container.getSubject().getStudySubjectId(), crfVersion.getCrf().getCrfId());
        if (existingEventCrf == null) {
            logger.info("***New EventCrf is created***");
            //create event crf
            container.setEventCrf(createEventCrf(crfVersion,container.getStudyEvent(),container.getSubject(),container.getUser()));
        } else if (existingEventCrf.getCrfVersion().getOcOid().equals(crfVersion.getOcOid())) {
            logger.info("***  Existing EventCrf with same CRF Version  ***");
            //use existing event crf
            container.setEventCrf(existingEventCrf);
        } else {
            // different version already exists. log error and abort submission
            errors.reject("Existing EventCrf with other CRF version");
            logger.info("***  Existing EventCrf with other CRF version  ***");
            throw new Exception("***  Existing EventCrf with other CRF version  ***");
        }
        
        
        //TODO:  May need to move this to a new processor that runs at the end
        // Update the EventCrf and StudyEvent to the proper status.
        // Don't do it in the initial save so it will have the expected audit trail entries.
        boolean isAnonymous = false;
        if (container.getSubjectContext().get("studySubjectOID") == null) isAnonymous = true;
        container.setEventCrf(updateEventCrf(container.getEventCrf(), container.getStudy(), studySubject, container.getUser(), isAnonymous));
        container.setStudyEvent(updateStudyEvent(container.getStudyEvent(), studyEventDefinition, container.getStudy(), studySubject, container.getUser(), isAnonymous));
    }

    @Override
    public int getOrder() {
        return 3;
    }

    private StudyEvent createStudyEvent(StudySubject studySubject, StudyEventDefinition studyEventDefinition, Integer ordinal, UserAccount user) {
        StudyEvent studyEvent = new StudyEvent();
        studyEvent.setStudySubject(studySubject);
        studyEvent.setStudyEventDefinition(studyEventDefinition);
        studyEvent.setSampleOrdinal(ordinal);
        studyEvent.setStatusId(Status.AVAILABLE.getCode());
        studyEvent.setUserAccount(user);
        studyEvent.setDateStart(new Date());
        studyEvent.setSubjectEventStatusId(SubjectEventStatus.SCHEDULED.getCode());
        studyEventDao.saveOrUpdate(studyEvent);
        studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDefinition.getOc_oid(),ordinal,studySubject.getStudySubjectId());
        return studyEvent;
    }
    
    private EventCrf createEventCrf(CrfVersion crfVersion, StudyEvent studyEvent, StudySubject studySubject, UserAccount user) {
        EventCrf eventCrf = new EventCrf();
        Date currentDate = new Date();
        eventCrf.setAnnotations("");
        eventCrf.setDateCreated(currentDate);
        eventCrf.setCrfVersion(crfVersion);
        eventCrf.setInterviewerName("");
        eventCrf.setDateInterviewed(null);
        eventCrf.setUserAccount(user);
        eventCrf.setStatusId(Status.AVAILABLE.getCode());
        eventCrf.setCompletionStatus(completionStatusDao.findByCompletionStatusId(1));//setCompletionStatusId(1);
        eventCrf.setStudySubject(studySubject);
        eventCrf.setStudyEvent(studyEvent);
        eventCrf.setValidateString("");
        eventCrf.setValidatorAnnotations("");
        eventCrf.setUpdateId(user.getUserId());
        eventCrf.setDateUpdated(new Date());
        eventCrfDao.saveOrUpdate(eventCrf);
        eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdCrfVersionId(studyEvent.getStudyEventId(), studySubject.getStudySubjectId(), crfVersion.getCrfVersionId());
        logger.debug("*********CREATED EVENT CRF");
        return eventCrf;
    }

    private StudyEvent updateStudyEvent(StudyEvent studyEvent, StudyEventDefinition studyEventDefinition, Study study, StudySubject studySubject, UserAccount user, boolean isAnonymous) {
        SubjectEventStatus newStatus = null;
        int crfCount = 0;
        int completedCrfCount = 0;

        if (!isAnonymous) {
            if (studyEvent.getSubjectEventStatusId().intValue() == SubjectEventStatus.SCHEDULED.getCode().intValue()) newStatus = SubjectEventStatus.DATA_ENTRY_STARTED;
        } else {
            // Get a count of CRFs defined for the event
            if (study.getStudy() != null )
                crfCount = eventDefinitionCrfDao.findSiteAvailableByStudyEventDefStudy(studyEventDefinition.getStudyEventDefinitionId(),study.getStudy().getStudyId()).size();
            else
                crfCount = eventDefinitionCrfDao.findAvailableByStudyEventDefStudy(studyEventDefinition.getStudyEventDefinitionId(),study.getStudyId()).size();
            // Get a count of completed CRFs for the event
            completedCrfCount = eventCrfDao.findByStudyEventStatus(studyEvent.getStudyEventId(), Status.UNAVAILABLE.getCode()).size();

            if (crfCount == completedCrfCount){
                if (studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SCHEDULED.getCode() || studyEvent.getSubjectEventStatusId() == SubjectEventStatus.DATA_ENTRY_STARTED.getCode()) {
                    newStatus = SubjectEventStatus.COMPLETED;
                }
            } else if (studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SCHEDULED.getCode()) {
                newStatus = SubjectEventStatus.DATA_ENTRY_STARTED;
            }
        }

        if (newStatus != null) {
            studyEvent.setUpdateId(user.getUserId());
            studyEvent.setDateUpdated(new Date());
            studyEvent.setSubjectEventStatusId(newStatus.getCode());
            studyEvent = studyEventDao.saveOrUpdate(studyEvent);
            logger.debug("*********UPDATED STUDY EVENT ");
        }
        return studyEvent;
    }

    /**
     * Update Status in Event CRF Table
     *
     * @param ecBean
     * @param studyBean
     * @param studySubjectBean
     * @param isAnonymous
     * @return
     */
    private EventCrf updateEventCrf(EventCrf eventCrf, Study study, StudySubject studySubject, UserAccount user, boolean isAnonymous) {
        eventCrf.setUpdateId(user.getUserId());
        eventCrf.setDateUpdated(new Date());
        if (isAnonymous) eventCrf.setStatusId(Status.UNAVAILABLE.getCode());
        else eventCrf.setStatusId(Status.AVAILABLE.getCode());
        eventCrf = eventCrfDao.saveOrUpdate(eventCrf);
        logger.debug("*********UPDATED EVENT CRF");
        return eventCrf;
    }

}
