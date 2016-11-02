package org.akaza.openclinica.controller.openrosa.processor;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.List;

import static org.akaza.openclinica.controller.openrosa.SubmissionProcessorChain.ProcessorEnum;

@Component
@Order(value=5)
public class EventProcessor implements Processor {

    @Autowired StudyEventDao studyEventDao;
    
    @Autowired StudyEventDefinitionDao studyEventDefinitionDao;
    
    @Autowired EventCrfDao eventCrfDao;
    
    @Autowired CrfVersionDao crfVersionDao;
    
    @Autowired CompletionStatusDao completionStatusDao;
    
    @Autowired EventDefinitionCrfDao eventDefinitionCrfDao;
    
    @Autowired ItemDataDao itemDataDao;
    
    @Autowired StudyDao studyDao;
    
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public ProcessorEnum process(SubmissionContainer container) throws Exception {
        logger.info("Executing Event Processor.");
        Errors errors = container.getErrors();
        StudySubject studySubject = container.getSubject();
        StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByStudyEventDefinitionId(Integer.valueOf(container.getSubjectContext().get("studyEventDefinitionID")));
        CrfVersion crfVersion = crfVersionDao.findByOcOID(container.getSubjectContext().get("crfVersionOID"));
        container.setCrfVersion(crfVersion);
        boolean isAnonymous = false;
        if (container.getSubjectContext().get("studySubjectOID") == null) isAnonymous = true;

        //Create study event if it doesn't exist
        if (isAnonymous)
            processAnonymous(container,errors, studySubject, studyEventDefinition);
        else
            processParticipant(container,errors, studySubject, studyEventDefinition);
        
        //TODO:  May need to move this to a new processor that runs at the end
        // Update the EventCrf and StudyEvent to the proper status.
        // Don't do it in the initial save so it will have the expected audit trail entries.
        Study study = null;
        if (container.getSubjectContext().get("studyOID") != null)
            study = studyDao.findByOcOID(container.getSubjectContext().get("studyOID"));
        else
            study = container.getStudy();
        container.setEventCrf(updateEventCrf(container.getEventCrf(), study, studySubject, container.getUser(), isAnonymous));
        container.setStudyEvent(updateStudyEvent(container.getStudyEvent(), studyEventDefinition, study, studySubject, container.getUser(), isAnonymous));
        return ProcessorEnum.PROCEED;

    }

    
    private void processParticipant(SubmissionContainer container, Errors errors, StudySubject studySubject, StudyEventDefinition studyEventDefinition) throws Exception {
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

        EventCrf existingEventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdCrfId(container.getStudyEvent().getStudyEventId(),
                container.getSubject().getStudySubjectId(), container.getCrfVersion().getCrf().getCrfId());
        if (existingEventCrf == null) {
            logger.info("***New EventCrf is created***");
            //create event crf
            container.setEventCrf(createEventCrf(container.getCrfVersion(), container.getStudyEvent(),container.getSubject(),container.getUser()));
        } else if (existingEventCrf.getCrfVersion().getOcOid().equals(container.getCrfVersion().getOcOid())) {
            logger.info("***  Existing EventCrf with same CRF Version  ***");
            //use existing event crf
            container.setEventCrf(existingEventCrf);
        } else {
            // different version already exists. log error and abort submission
            errors.reject("Existing EventCrf with other CRF version");
            logger.info("***  Existing EventCrf with other CRF version  ***");
            throw new Exception("***  Existing EventCrf with other CRF version  ***");
        }

    }

    private void processAnonymous(SubmissionContainer container, Errors errors, StudySubject studySubject, StudyEventDefinition studyEventDefinition) throws Exception {
        Integer ordinal = 1; //Integer.valueOf(container.getSubjectContext().get("studyEventOrdinal"));
        Integer maxExistingOrdinal = studyEventDao.findMaxOrdinalByStudySubjectStudyEventDefinition(studySubject.getStudySubjectId(),studyEventDefinition.getStudyEventDefinitionId());
        CrfVersion crfVersion = crfVersionDao.findByOcOID(container.getSubjectContext().get("crfVersionOID"));

        while (ordinal <= maxExistingOrdinal + 1) {
            StudyEvent existingStudyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDefinition.getOc_oid(),ordinal,studySubject.getStudySubjectId());
            if (existingStudyEvent == null) {
                container.setStudyEvent(createStudyEvent(studySubject,studyEventDefinition,ordinal,container.getUser()));
                container.setEventCrf(createEventCrf(crfVersion,container.getStudyEvent(),container.getSubject(),container.getUser()));
                break;
            } else if (!existingStudyEvent.getStatusId().equals(Status.AVAILABLE.getCode())
                    || (!existingStudyEvent.getSubjectEventStatusId().equals(SubjectEventStatus.SCHEDULED.getCode())
                    && !existingStudyEvent.getSubjectEventStatusId().equals(SubjectEventStatus.NOT_SCHEDULED.getCode())
                    && !existingStudyEvent.getSubjectEventStatusId().equals(SubjectEventStatus.DATA_ENTRY_STARTED.getCode()))){
                if (studyEventDefinition.getRepeating()) {
                    ordinal++;
                    continue;
                } else {
                    errors.reject("Existing StudyEvent is not Available and EventDef is not repeating");
                    logger.info("***  Existing StudyEvent is not Available and EventDef is not repeating  ***");
                    throw new Exception("***  Existing StudyEvent is not Available and EventDef is not repeating  ***");
                }
            } else {
                EventCrf existingEventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdCrfId(existingStudyEvent.getStudyEventId(), container.getSubject().getStudySubjectId(), crfVersion.getCrf().getCrfId());
                if (existingEventCrf == null) {
                    container.setStudyEvent(existingStudyEvent);
                    container.setEventCrf(createEventCrf(crfVersion,container.getStudyEvent(),container.getSubject(),container.getUser()));
                    break;
                } else {
                    
                    List<ItemData> itemDataList = itemDataDao.findByEventCrfId(existingEventCrf.getEventCrfId());
                    if (existingEventCrf.getStatusId().equals(Status.AVAILABLE.getCode()) && itemDataList.size() == 0) {
                        container.setStudyEvent(existingStudyEvent);
                        container.setEventCrf(existingEventCrf);
                        break;
                    }else if (studyEventDefinition.getRepeating()) {
                        ordinal++;
                        continue;
                    } else {
                        errors.reject("Existing EventCRF is not usable and EventDef is not repeating");
                        logger.info("***  Existing EventCRF is not usable and EventDef is not repeating  ***");
                        throw new Exception("***  Existing EventCRF is not usable and EventDef is not repeating  ***");
                    }
                }
            }
        }
        if (container.getStudyEvent() == null || container.getEventCrf() == null) {
            errors.reject("Unable to identify StudyEvent or EventCrf.");
            logger.info("***  Unable to identify StudyEvent or EventCrf.  ***");
            throw new Exception("***  Unable to identify StudyEvent or EventCrf.  ***");
        }
    }
    
    private StudyEvent createStudyEvent(StudySubject studySubject, StudyEventDefinition studyEventDefinition, Integer ordinal, UserAccount user) {
        Date currentDate = new Date();
        StudyEvent studyEvent = new StudyEvent();
        studyEvent.setStudySubject(studySubject);
        studyEvent.setStudyEventDefinition(studyEventDefinition);
        studyEvent.setSampleOrdinal(ordinal);
        studyEvent.setStatusId(Status.AVAILABLE.getCode());
        studyEvent.setUserAccount(user);
        studyEvent.setDateStart(currentDate);
        studyEvent.setSubjectEventStatusId(SubjectEventStatus.SCHEDULED.getCode());
        studyEvent.setStartTimeFlag(false);
        studyEvent.setEndTimeFlag(false);
        studyEvent.setDateCreated(currentDate);
        studyEvent.setLocation("");
        StudyEventChangeDetails changeDetails = new StudyEventChangeDetails(true,true);
        StudyEventContainer container = new StudyEventContainer(studyEvent,changeDetails);
        studyEventDao.saveOrUpdateTransactional(container);
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
        eventCrf.setValidatorId(0);
        eventCrf.setOldStatusId(0);
        eventCrf.setSdvUpdateId(0);
        eventCrfDao.saveOrUpdate(eventCrf);
        eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdCrfVersionId(studyEvent.getStudyEventId(), studySubject.getStudySubjectId(), crfVersion.getCrfVersionId());
        logger.debug("*********CREATED EVENT CRF");
        return eventCrf;
    }

    private StudyEvent updateStudyEvent(StudyEvent studyEvent, StudyEventDefinition studyEventDefinition, Study study, StudySubject studySubject, UserAccount user, boolean isAnonymous) {
        SubjectEventStatus newStatus = null;
        int crfCount = 0;
        int hiddenSiteCrfCount = 0;
        int completedCrfCount = 0;

        if (!isAnonymous) {
            if (studyEvent.getSubjectEventStatusId().intValue() == SubjectEventStatus.SCHEDULED.getCode().intValue()) newStatus = SubjectEventStatus.DATA_ENTRY_STARTED;
        } else {
            // Get a count of CRFs defined for the event
            // TODO: What i need to do to fix this is get the study from the context
            // Then i need to query the site for hidden crfs then subtract that from the count of study defined crfs to get the crf count
            if (study.getStudy() != null ) {
                hiddenSiteCrfCount = eventDefinitionCrfDao.findSiteHiddenByStudyEventDefStudy(studyEventDefinition.getStudyEventDefinitionId(),study.getStudyId()).size();
                crfCount = eventDefinitionCrfDao.findAvailableByStudyEventDefStudy(studyEventDefinition.getStudyEventDefinitionId(),study.getStudy().getStudyId()).size();
            } else
                crfCount = eventDefinitionCrfDao.findAvailableByStudyEventDefStudy(studyEventDefinition.getStudyEventDefinitionId(),study.getStudyId()).size();
            // Get a count of completed CRFs for the event
            completedCrfCount = eventCrfDao.findByStudyEventStatus(studyEvent.getStudyEventId(), Status.UNAVAILABLE.getCode()).size();
            if ((crfCount - hiddenSiteCrfCount) == completedCrfCount){
                if (studyEvent.getSubjectEventStatusId().intValue() == SubjectEventStatus.SCHEDULED.getCode().intValue() || studyEvent.getSubjectEventStatusId().intValue() == SubjectEventStatus.DATA_ENTRY_STARTED.getCode().intValue()) {
                    newStatus = SubjectEventStatus.COMPLETED;
                }
            } else if (studyEvent.getSubjectEventStatusId().intValue() == SubjectEventStatus.SCHEDULED.getCode().intValue()) {
                newStatus = SubjectEventStatus.DATA_ENTRY_STARTED;
            }
        }

        if (newStatus != null) {
            studyEvent.setUpdateId(user.getUserId());
            studyEvent.setDateUpdated(new Date());
            studyEvent.setSubjectEventStatusId(newStatus.getCode());
            StudyEventChangeDetails changeDetails = new StudyEventChangeDetails(true,false);
            StudyEventContainer container = new StudyEventContainer(studyEvent,changeDetails);
            studyEvent = studyEventDao.saveOrUpdateTransactional(container);
            logger.debug("*********UPDATED STUDY EVENT ");
        }
        return studyEvent;
    }

    /**
     * Update Status in Event CRF Table
     *
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
