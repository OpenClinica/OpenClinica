package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
            unArchiveStudyEvent(studyEvent, userAccount);
        }
    }


    public void archiveEventForm(EventDefinitionCrf edc, UserAccount userAccount, Study study) {
        archiveEventDefnCrf(edc, userAccount);
        logger.info("Archive All Event Crf by Event Form");
        List<EventCrf> eventCrfs = eventCrfDao.findallByStudyEventOIdAndCrfOId(edc.getStudyEventDefinition().getOc_oid(), edc.getCrf().getOcOid());
        for (EventCrf eventCrf : eventCrfs) {
            archiveEventCrf(eventCrf, userAccount);
            closeDnsByEventCrf(eventCrf, userAccount, study);
        }
    }


    public void unArchiveEventForm(EventDefinitionCrf edc, UserAccount userAccount) {
        unArchiveEventDefnCrf(edc, userAccount);
        logger.info("Restoring Archived event_crfs By Event Form");
        List<EventCrf> eventCrfs = eventCrfDao.findallByStudyEventOIdAndCrfOId(edc.getStudyEventDefinition().getOc_oid(), edc.getCrf().getOcOid());
        for (EventCrf eventCrf : eventCrfs) {
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
        for (DiscrepancyNote parentDiscrepancyNote : parentDiscrepancyNotes) {
            if (parentDiscrepancyNote.getResolutionStatus().getResolutionStatusId() != (closedModifiedresolutionStatus.getResolutionStatusId())
                    && parentDiscrepancyNote.getResolutionStatus().getResolutionStatusId() != (closedResolutionStatus.getResolutionStatusId())) {

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


}