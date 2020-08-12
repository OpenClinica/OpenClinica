package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class EventService implements EventServiceInterface {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    OdmImportService odmImportService;
    @Autowired
    ResolutionStatusDao resolutionStatusDao;
    @Autowired
    EventCrfDao eventCrfDao;
    @Autowired
    DiscrepancyNoteDao discrepancyNoteDao;
    @Autowired
    DnItemDataMapDao dnItemDataMapDao;
    @Autowired
    private ItemDataDao itemDataDao;
    @Autowired
    StudyEventDao studyEventDao;
    @Autowired
    StudySubjectDao studySubjectDao;
    @Autowired
    StudyEventDefinitionDao studyEventDefinitionDao;
    @Autowired
    FormLayoutDao formLayoutDao;
    @Autowired
    EventDefinitionCrfDao eventDefinitionCrfDao;

    @SuppressWarnings( {"unchecked", "rawtypes"} )
    public void archiveEventDefinition(StudyEventDefinition sed, UserAccount userAccount, Study study) {
        archiveEventDefn(sed, userAccount);
        List<StudyEvent> studyEvents = studyEventDao.findAllByEventDefinition(sed.getOc_oid());
        for (StudyEvent studyEvent : studyEvents) {
            unSignStudyEvent(studyEvent, userAccount);

            archiveStudyEvent(studyEvent, userAccount);
            List<EventCrf> eventCrfs = eventCrfDao.findAllByStudyEvent(studyEvent.getStudyEventId());
            for (EventCrf eventCrf : eventCrfs) {
                closeDnsByEventCrf(eventCrf, userAccount, study);
            }
        }
    }


    @SuppressWarnings( {"unchecked", "rawtypes"} )
    public void unArchiveEventDefinition(StudyEventDefinition sed, UserAccount userAccount) {
        unArchiveEventDefn(sed, userAccount);
        List<StudyEvent> studyEvents = studyEventDao.findAllByEventDefinition(sed.getOc_oid());
        for (StudyEvent studyEvent : studyEvents) {
            unSignStudyEvent(studyEvent, userAccount);
            unArchiveStudyEvent(studyEvent, userAccount);
        }
    }


    public void archiveEventForm(EventDefinitionCrf edc, UserAccount userAccount, Study study) {
        archiveEventDefnCrf(edc, userAccount);
        logger.info("Archive All Event Crf by Event Form");
        List<EventCrf> eventCrfs = eventCrfDao.findallByStudyEventOIdAndCrfOId(edc.getStudyEventDefinition().getOc_oid(), edc.getCrf().getOcOid());
        for (EventCrf eventCrf : eventCrfs) {
            if (isUnsigningStudyEventConditionMetWhenArchivingEventForm(eventCrf))
                unSignStudyEvent(eventCrf.getStudyEvent(), userAccount);

            archiveEventCrf(eventCrf, userAccount);
            closeDnsByEventCrf(eventCrf, userAccount, study);
        }
    }


    public void unArchiveEventForm(EventDefinitionCrf edc, UserAccount userAccount) {
        unArchiveEventDefnCrf(edc, userAccount);
        logger.info("Restoring Archived event_crfs By Event Form");
        unsignStudyEventsForNotStartedAndRequiredForms(edc,userAccount);

        List<EventCrf> eventCrfs = eventCrfDao.findallByStudyEventOIdAndCrfOId(edc.getStudyEventDefinition().getOc_oid(), edc.getCrf().getOcOid());
        for (EventCrf eventCrf : eventCrfs) {
            if (isUnsigningStudyEventConditionMetWhenUnArchivingEventForm(eventCrf)
                    || isUnsigningStudyEventConditionMetWhenUnArchivingEventForm(eventCrf, edc))
                unSignStudyEvent(eventCrf.getStudyEvent(), userAccount);

            unArchiveEventCrf(eventCrf, userAccount);
        }
    }


    public void archiveFormLayout(FormLayout formLayout, UserAccount userAccount) {
        logger.debug("Archiving Form Layout {}", formLayout.getCrf().getName() + " " + formLayout.getName());
        if (!formLayout.getStatus().equals(Status.DELETED) && !formLayout.getStatus().equals(Status.AUTO_DELETED)) {
            formLayout.setStatus(Status.DELETED);
            formLayout.setUpdateId(userAccount.getUserId());
            formLayout.setDateUpdated(new Date());
            formLayoutDao.saveOrUpdate(formLayout);
        }
    }

    public void archiveEventDefn(StudyEventDefinition sed, UserAccount userAccount) {
        if (!sed.getStatus().equals(Status.DELETED) && !sed.getStatus().equals(Status.AUTO_DELETED)) {
            sed.setStatus(Status.DELETED);
            sed.setUpdateId(userAccount.getUserId());
            sed.setDateUpdated(new Date());
            studyEventDefinitionDao.saveOrUpdate(sed);
            logger.info("Archiving Event Definition {}", sed.getOc_oid());
        }
    }

    public void unArchiveEventDefn(StudyEventDefinition sed, UserAccount userAccount) {
        if (!sed.getStatus().equals(Status.AVAILABLE)) {
            sed.setStatus(Status.AVAILABLE);
            sed.setUpdateId(userAccount.getUserId());
            sed.setDateUpdated(new Date());
            studyEventDefinitionDao.saveOrUpdate(sed);
            logger.info("UnArchiving Event Definition {}", sed.getOc_oid());
        }
    }

    public void archiveEventDefnCrf(EventDefinitionCrf edc, UserAccount userAccount) {
        if (edc.getStatusId() != Status.DELETED.getCode() && edc.getStatusId() != Status.AUTO_DELETED.getCode()) {
            edc.setStatusId(Status.DELETED.getCode());
            edc.setUpdateId(userAccount.getUserId());
            edc.setDateUpdated(new Date());
            eventDefinitionCrfDao.saveOrUpdate(edc);
            logger.info("Archiving Event Definition Crf {}", edc.getEventDefinitionCrfId());
        }
    }

    public void unArchiveEventDefnCrf(EventDefinitionCrf edc, UserAccount userAccount) {
        if (edc.getStatusId() != Status.AVAILABLE.getCode()) {
            edc.setStatusId(Status.AVAILABLE.getCode());
            edc.setUpdateId(userAccount.getUserId());
            edc.setDateUpdated(new Date());
            eventDefinitionCrfDao.saveOrUpdate(edc);
            logger.info("UnArchiving Event Definition Crf {}", edc.getEventDefinitionCrfId());
        }
    }


    public void archiveStudyEvent(StudyEvent studyEvent, UserAccount userAccount) {
        if (!studyEvent.isCurrentlyArchived()) {
            studyEvent.setArchived(Boolean.TRUE);
            studyEvent.setUpdateId(userAccount.getUserId());
            studyEvent.setDateUpdated(new Date());
            studyEventDao.saveOrUpdate(studyEvent);
            logger.debug("Archiving Study Event {}", studyEvent.getStudyEventId());
        }
    }

    public void unArchiveStudyEvent(StudyEvent studyEvent, UserAccount userAccount) {
        if (studyEvent.isCurrentlyArchived()) {
            studyEvent.setArchived(Boolean.FALSE);
            studyEvent.setUpdateId(userAccount.getUserId());
            studyEvent.setDateUpdated(new Date());
            studyEventDao.saveOrUpdate(studyEvent);
            logger.debug("UnArchiving Study Event {}", studyEvent.getStudyEventId());
        }
    }

    public void archiveEventCrf(EventCrf eventCrf, UserAccount userAccount) {
        if (!eventCrf.isCurrentlyArchived()) {
            eventCrf.setArchived(Boolean.TRUE);
            eventCrf.setUpdateId(userAccount.getUserId());
            eventCrf.setDateUpdated(new Date());
            eventCrfDao.saveOrUpdate(eventCrf);
            logger.debug("Archiving Event Crf {}", eventCrf.getEventCrfId());
        }
    }

    public void unArchiveEventCrf(EventCrf eventCrf, UserAccount userAccount) {
        if (eventCrf.isCurrentlyArchived()) {
            eventCrf.setArchived(Boolean.FALSE);
            eventCrf.setUpdateId(userAccount.getUserId());
            eventCrf.setDateUpdated(new Date());
            eventCrfDao.saveOrUpdate(eventCrf);
            logger.debug("UnArchiving Event Crf {}", eventCrf.getEventCrfId());
        }
    }

    public void closeDnsByEventCrf(EventCrf eventCrf, UserAccount userAccount, Study study) {
        List<ItemData> itemDatas = itemDataDao.findAllByEventCrf(eventCrf.getEventCrfId());
        for (ItemData itemData : itemDatas) {
            closeDns(userAccount, study, itemData);
        }
    }

    public void closeDns(UserAccount userAccount, Study study, ItemData itemData) {

        List<DiscrepancyNote> parentDiscrepancyNotes = discrepancyNoteDao.findParentNotesByItemData(itemData.getItemDataId());
        ResolutionStatus closedModifiedresolutionStatus = new ResolutionStatus(core.org.akaza.openclinica.bean.core.ResolutionStatus.CLOSED_MODIFIED.getId());
        ResolutionStatus closedResolutionStatus = new ResolutionStatus(core.org.akaza.openclinica.bean.core.ResolutionStatus.CLOSED.getId());
        String detailedNotes = "The item has been removed, this Query has been Closed.";
        DiscrepancyNoteType queryType = new DiscrepancyNoteType(3);
        for (DiscrepancyNote parentDiscrepancyNote : parentDiscrepancyNotes) {
            if (parentDiscrepancyNote.getResolutionStatus().getResolutionStatusId() != (closedModifiedresolutionStatus.getResolutionStatusId())
                    && parentDiscrepancyNote.getResolutionStatus().getResolutionStatusId() != (closedResolutionStatus.getResolutionStatusId())
                    && parentDiscrepancyNote.getDiscrepancyNoteType().getDiscrepancyNoteTypeId()==queryType.getDiscrepancyNoteTypeId()) {

                DiscrepancyNote childDiscrepancyNote = new DiscrepancyNote();
                childDiscrepancyNote.setParentDiscrepancyNote(parentDiscrepancyNote);
                childDiscrepancyNote.setDiscrepancyNoteType(parentDiscrepancyNote.getDiscrepancyNoteType());
                childDiscrepancyNote.setThreadUuid(parentDiscrepancyNote.getThreadUuid());
                childDiscrepancyNote.setResolutionStatus(closedModifiedresolutionStatus);  // closed_modified
                childDiscrepancyNote.setStudy(study);
                childDiscrepancyNote.setUserAccount(userAccount);
                childDiscrepancyNote.setUserAccountByOwnerId(userAccount);
                childDiscrepancyNote.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
                childDiscrepancyNote.setDateCreated(new Date());
                childDiscrepancyNote.setDetailedNotes(detailedNotes);
                discrepancyNoteDao.saveOrUpdate(childDiscrepancyNote);
                saveQueryItemDatamap(childDiscrepancyNote, itemData);

                parentDiscrepancyNote.setDetailedNotes(detailedNotes);
                parentDiscrepancyNote.setResolutionStatus(closedModifiedresolutionStatus);// closed_modified
                discrepancyNoteDao.saveOrUpdate(parentDiscrepancyNote);
                logger.debug("Closing Queries for item data {}", itemData.getItemDataId());
            }
        }
    }


    public void saveQueryItemDatamap(DiscrepancyNote discrepancyNote, ItemData itemData) {
        // Create Mapping for new Discrepancy Note
        DnItemDataMapId dnItemDataMapId = new DnItemDataMapId();
        dnItemDataMapId.setDiscrepancyNoteId(discrepancyNote.getDiscrepancyNoteId());
        dnItemDataMapId.setItemDataId(itemData.getItemDataId());
        dnItemDataMapId.setColumnName("value");

        DnItemDataMap mapping = new DnItemDataMap();
        mapping.setDnItemDataMapId(dnItemDataMapId);
        mapping.setItemData(itemData);
        mapping.setActivated(false);
        mapping.setDiscrepancyNote(discrepancyNote);
        dnItemDataMapDao.saveOrUpdate(mapping);
    }

    public void unSignStudyEvent(StudyEvent studyEvent, UserAccount userAccount) {
        if(studyEvent.isCurrentlySigned()) {
            studyEvent.setSigned(Boolean.FALSE);
            studyEvent.setUpdateId(userAccount.getUserId());
            studyEvent.setDateUpdated(new Date());
            studyEventDao.saveOrUpdate(studyEvent);
        }
            unSignStudySubject(studyEvent.getStudySubject(), userAccount);

    }

    public void unSignStudySubject(StudySubject studySubject, UserAccount userAccount) {
       if(studySubject.getStatus().isSigned()) {
           studySubject.setStatus(Status.AVAILABLE);
           studySubject.setUpdateId(userAccount.getUserId());
           studySubject.setDateUpdated(new Date());
           studySubjectDao.saveOrUpdate(studySubject);
       }
    }

    public boolean isUnsigningStudyEventConditionMetWhenUnArchivingEventForm(EventCrf eventCrf) {
        if (eventCrf.getStudyEvent().isCurrentlySigned()
                && (eventCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED) || eventCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY))
                && !eventCrf.isCurrentlyRemoved()) return true;
        return false;
    }

    public boolean isUnsigningStudyEventConditionMetWhenUnArchivingEventForm(EventCrf eventCrf, EventDefinitionCrf edc) {

        if (eventCrf.getStudyEvent().isCurrentlySigned()
                && eventCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.NOT_STARTED)) {
            setEdcForSiteSubjectsIfExists(edc, eventCrf.getStudySubject());
            if (edc.getRequiredCrf().equals(Boolean.TRUE)) return true;
        }
        return false;
    }


    public boolean isUnsigningStudyEventConditionMetWhenArchivingEventForm(EventCrf eventCrf) {
        if (eventCrf.getStudyEvent().isCurrentlySigned()
                && eventCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)
                && !eventCrf.isCurrentlyRemoved())
            return true;
        return false;
    }


    public void unsignStudyEventsForNotStartedAndRequiredForms(EventDefinitionCrf edc, UserAccount userAccount){
            List<StudyEvent> studyEvents = studyEventDao.findAllByEventDefinition(edc.getStudyEventDefinition().getOc_oid());
            for (StudyEvent studyEvent : studyEvents) {
                setEdcForSiteSubjectsIfExists(edc,studyEvent.getStudySubject());
                EventCrf eventCrf = eventCrfDao.findByStudyEventOIdStudySubjectOIdCrfOId(studyEvent.getStudyEventDefinition().getOc_oid(), studyEvent.getStudySubject().getLabel(), edc.getCrf().getOcOid(), studyEvent.getSampleOrdinal());
                if (edc.getRequiredCrf() && eventCrf == null) {
                    unSignStudyEvent(studyEvent, userAccount);
                }
            }
    }

    private void setEdcForSiteSubjectsIfExists(EventDefinitionCrf edc, StudySubject studySubject) {
        Study subjectStudy = studySubject.getStudy();
        if (subjectStudy.isSite()) {
            EventDefinitionCrf site_edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(
                    edc.getStudyEventDefinition().getStudyEventDefinitionId(), edc.getCrf().getCrfId(), subjectStudy.getStudyId());
            if (site_edc != null) {
                edc = site_edc;
            }
        }
    }


}