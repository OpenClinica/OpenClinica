package org.akaza.openclinica.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventService implements EventServiceInterface {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    SubjectDAO subjectDao;
    StudySubjectDAO studySubjectDao;
    UserAccountDAO userAccountDao;
    StudyEventDefinitionDAO studyEventDefinitionDao;
    StudyEventDAO studyEventDao;
    StudyDAO studyDao;
    EventDefinitionCRFDAO eventDefinitionCRFDao;
    EventCRFDAO eventCrfDao;
    ItemDataDAO itemDataDao;
    DataSource dataSource;
    FormLayoutDAO formLayoutDao;
    CRFDAO crfDao;
    DiscrepancyNoteDAO discrepancyNoteDao;

    public EventService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public EventService(SessionManager sessionManager) {
        this.dataSource = sessionManager.getDataSource();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void removeStudyEventDefn(int defId, int userId) {
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(defId);
        UserAccountBean ub = (UserAccountBean) getUserAccountDao().findByPK(userId);
        // find all event defn CRFs
        ArrayList<EventDefinitionCRFBean> eventDefinitionCRFs = (ArrayList) getEventDefinitionCRFDao().findAllByDefinition(defId);
        // finds all study events
        ArrayList<StudyEventBean> events = (ArrayList) getStudyEventDao().findAllByDefinition(sed.getId());

        sed.setStatus(Status.DELETED);
        sed.setUpdater(ub);
        sed.setUpdatedDate(new Date());
        getStudyEventDefinitionDao().update(sed);

        // remove all event defn crfs
        for (int j = 0; j < eventDefinitionCRFs.size(); j++) {
            EventDefinitionCRFBean edc = (EventDefinitionCRFBean) eventDefinitionCRFs.get(j);
            if (!edc.getStatus().equals(Status.DELETED) || !edc.getStatus().equals(Status.AUTO_DELETED)) {
                edc.setStatus(Status.AUTO_DELETED);
                edc.setUpdater(ub);
                edc.setUpdatedDate(new Date());
                getEventDefinitionCRFDao().update(edc);
            }
        }

        // remove all study events
        for (int j = 0; j < events.size(); j++) {
            StudyEventBean event = (StudyEventBean) events.get(j);
            if (!event.getStatus().equals(Status.DELETED) || !event.getStatus().equals(Status.AUTO_DELETED)) {
                event.setStatus(Status.AUTO_DELETED);
                event.setUpdater(ub);
                event.setUpdatedDate(new Date());
                getStudyEventDao().update(event);

                // remove all event crfs
                ArrayList eventCRFs = getEventCRFDao().findAllByStudyEvent(event);
                for (int k = 0; k < eventCRFs.size(); k++) {
                    EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                    if (!eventCRF.getStatus().equals(Status.DELETED) || !eventCRF.getStatus().equals(Status.AUTO_DELETED)) {
                        eventCRF.setStatus(Status.AUTO_DELETED);
                        eventCRF.setUpdater(ub);
                        eventCRF.setUpdatedDate(new Date());
                        getEventCRFDao().update(eventCRF);

                        // remove all item data
                        ArrayList itemDatas = getItemDataDao().findAllByEventCRFId(eventCRF.getId());
                        for (int a = 0; a < itemDatas.size(); a++) {
                            ItemDataBean item = (ItemDataBean) itemDatas.get(a);
                            if (!item.getStatus().equals(Status.DELETED) || !item.getStatus().equals(Status.AUTO_DELETED)) {
                                item.setStatus(Status.AUTO_DELETED);
                                item.setUpdater(ub);
                                item.setUpdatedDate(new Date());
                                getItemDataDao().update(item);
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void restoreStudyEventDefn(int defId, int userId) {
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(defId);
        UserAccountBean ub = (UserAccountBean) getUserAccountDao().findByPK(userId);
        // find all Event Defn CRFs
        ArrayList<EventDefinitionCRFBean> eventDefinitionCRFs = (ArrayList) getEventDefinitionCRFDao().findAllByDefinition(defId);
        // finds all events
        ArrayList<StudyEventBean> events = (ArrayList) getStudyEventDao().findAllByDefinition(sed.getId());

        sed.setStatus(Status.AVAILABLE);
        sed.setUpdater(ub);
        sed.setUpdatedDate(new Date());
        getStudyEventDefinitionDao().update(sed);

        // restore all event defn crfs
        for (int j = 0; j < eventDefinitionCRFs.size(); j++) {
            EventDefinitionCRFBean edc = (EventDefinitionCRFBean) eventDefinitionCRFs.get(j);
            CRFBean crf = (CRFBean) getCrfDao().findByPK(edc.getCrfId());
            if (edc.getStatus().equals(Status.AUTO_DELETED) || edc.getStatus().equals(Status.DELETED)) {
                edc.setStatus(Status.AVAILABLE);
                edc.setUpdater(ub);
                edc.setUpdatedDate(new Date());
                getEventDefinitionCRFDao().update(edc);
            }
        }

        // restore all study events
        for (int j = 0; j < events.size(); j++) {
            StudyEventBean event = (StudyEventBean) events.get(j);
            if (event.getStatus().equals(Status.AUTO_DELETED) || event.getStatus().equals(Status.DELETED)) {
                event.setStatus(Status.AVAILABLE);
                event.setUpdater(ub);
                event.setUpdatedDate(new Date());
                getStudyEventDao().update(event);

                // restore all event crf
                ArrayList eventCRFs = getEventCRFDao().findAllByStudyEvent(event);
                for (int k = 0; k < eventCRFs.size(); k++) {
                    EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                    if (eventCRF.getStatus().equals(Status.AUTO_DELETED) || eventCRF.getStatus().equals(Status.DELETED)) {
                        eventCRF.setStatus(Status.AVAILABLE);
                        eventCRF.setUpdater(ub);
                        eventCRF.setUpdatedDate(new Date());
                        getEventCRFDao().update(eventCRF);

                        // restore all item data
                        ArrayList itemDatas = getItemDataDao().findAllByEventCRFId(eventCRF.getId());
                        for (int a = 0; a < itemDatas.size(); a++) {
                            ItemDataBean item = (ItemDataBean) itemDatas.get(a);
                            if (item.getStatus().equals(Status.AUTO_DELETED) || item.getStatus().equals(Status.DELETED)) {
                                item.setStatus(Status.AVAILABLE);
                                item.setUpdater(ub);
                                item.setUpdatedDate(new Date());
                                getItemDataDao().update(item);
                            }
                        }
                    }
                }
            }
        }
    }

    public void removeCrfFromEventDefinition(int eventDefnCrfId, int defId, int userId, int studyId) {
        StudyBean study = (StudyBean) getStudyDao().findByPK(studyId);
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(defId);
        EventDefinitionCRFBean edc = (EventDefinitionCRFBean) getEventDefinitionCRFDao().findByPK(eventDefnCrfId);
        UserAccountBean ub = (UserAccountBean) getUserAccountDao().findByPK(userId);
        removeAllEventsItems(edc, sed, ub, study);
    }

    public void restoreCrfFromEventDefinition(int eventDefnCrfId, int defId, int userId) {
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(defId);
        EventDefinitionCRFBean edc = (EventDefinitionCRFBean) getEventDefinitionCRFDao().findByPK(eventDefnCrfId);
        UserAccountBean ub = (UserAccountBean) getUserAccountDao().findByPK(userId);
        restoreAllEventsItems(edc, sed, ub);
    }

    public void removeAllEventsItems(EventDefinitionCRFBean edc, StudyEventDefinitionBean sed, UserAccountBean ub, StudyBean study) {
        CRFBean crf = (CRFBean) getCrfDao().findByPK(edc.getCrfId());
        // Getting Study Events
        ArrayList seList = getStudyEventDao().findAllByStudyEventDefinitionAndCrfOids(sed.getOid(), crf.getOid());
        for (int j = 0; j < seList.size(); j++) {
            StudyEventBean seBean = (StudyEventBean) seList.get(j);
            // Getting Event CRFs
            ArrayList ecrfList = getEventCRFDao().findAllByStudyEventAndCrfOrCrfVersionOid(seBean, crf.getOid());
            for (int k = 0; k < ecrfList.size(); k++) {
                EventCRFBean ecrfBean = (EventCRFBean) ecrfList.get(k);
                ecrfBean.setOldStatus(ecrfBean.getStatus());
                ecrfBean.setStatus(Status.AUTO_DELETED);
                ecrfBean.setUpdater(ub);
                ecrfBean.setUpdatedDate(new Date());
                getEventCRFDao().update(ecrfBean);
                // Getting Item Data
                ArrayList itemData = getItemDataDao().findAllByEventCRFId(ecrfBean.getId());
                // remove all the item data
                for (int a = 0; a < itemData.size(); a++) {
                    ItemDataBean item = (ItemDataBean) itemData.get(a);
                    if (!item.getStatus().equals(Status.DELETED)) {
                        item.setOldStatus(item.getStatus());
                        item.setStatus(Status.AUTO_DELETED);
                        item.setUpdater(ub);
                        item.setUpdatedDate(new Date());
                        getItemDataDao().update(item);
                        List dnNotesOfRemovedItem = getDiscrepancyNoteDao().findExistingNotesForItemData(item.getId());
                        if (!dnNotesOfRemovedItem.isEmpty()) {
                            DiscrepancyNoteBean itemParentNote = null;
                            for (Object obj : dnNotesOfRemovedItem) {
                                if (((DiscrepancyNoteBean) obj).getParentDnId() == 0) {
                                    itemParentNote = (DiscrepancyNoteBean) obj;
                                }
                            }
                            DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
                            if (itemParentNote != null) {
                                dnb.setParentDnId(itemParentNote.getId());
                                dnb.setDiscrepancyNoteTypeId(itemParentNote.getDiscrepancyNoteTypeId());
                            }
                            dnb.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
                            dnb.setStudyId(study.getId());
                            dnb.setAssignedUserId(ub.getId());
                            dnb.setOwner(ub);
                            dnb.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
                            dnb.setEntityId(item.getId());
                            dnb.setColumn("value");
                            dnb.setCreatedDate(new Date());
                            dnb.setDescription("The item has been removed, this Discrepancy Note has been Closed.");
                            getDiscrepancyNoteDao().create(dnb);
                            getDiscrepancyNoteDao().createMapping(dnb);
                            itemParentNote.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
                            getDiscrepancyNoteDao().update(itemParentNote);
                        }
                    }
                }
            }
        }
    }

    public void restoreAllEventsItems(EventDefinitionCRFBean edc, StudyEventDefinitionBean sed, UserAccountBean ub) {
        CRFBean crf = (CRFBean) getCrfDao().findByPK(edc.getCrfId());
        // All Study Events
        ArrayList seList = getStudyEventDao().findAllByStudyEventDefinitionAndCrfOids(sed.getOid(), crf.getOid());
        for (int j = 0; j < seList.size(); j++) {
            StudyEventBean seBean = (StudyEventBean) seList.get(j);
            // All Event CRFs
            ArrayList ecrfList = getEventCRFDao().findAllByStudyEventAndCrfOrCrfVersionOid(seBean, crf.getOid());
            for (int k = 0; k < ecrfList.size(); k++) {
                EventCRFBean ecrfBean = (EventCRFBean) ecrfList.get(k);
                ecrfBean.setStatus(ecrfBean.getOldStatus());
                ecrfBean.setUpdater(ub);
                ecrfBean.setUpdatedDate(new Date());
                getEventCRFDao().update(ecrfBean);
                // All Item Data
                ArrayList itemData = getItemDataDao().findAllByEventCRFId(ecrfBean.getId());
                // remove all the item data
                for (int a = 0; a < itemData.size(); a++) {
                    ItemDataBean item = (ItemDataBean) itemData.get(a);
                    if (item.getStatus().equals(Status.DELETED) || item.getStatus().equals(Status.AUTO_DELETED)) {
                        item.setStatus(item.getOldStatus());
                        item.setUpdater(ub);
                        item.setUpdatedDate(new Date());
                        getItemDataDao().update(item);
                    }
                }
            }
        }

    }

    public HashMap<String, String> scheduleEvent(UserAccountBean user, Date startDateTime, Date endDateTime, String location, String studyUniqueId,
            String siteUniqueId, String eventDefinitionOID, String studySubjectId) throws OpenClinicaSystemException {

        // Business Validation
        StudyBean study = getStudyDao().findByUniqueIdentifier(studyUniqueId);
        int parentStudyId = study.getId();
        if (siteUniqueId != null) {
            study = getStudyDao().findSiteByUniqueIdentifier(studyUniqueId, siteUniqueId);
        }
        StudyEventDefinitionBean studyEventDefinition = getStudyEventDefinitionDao().findByOidAndStudy(eventDefinitionOID, study.getId(), parentStudyId);
        StudySubjectBean studySubject = getStudySubjectDao().findByLabelAndStudy(studySubjectId, study);

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
            studyEvent = (StudyEventBean) getStudyEventDao().create(studyEvent, true);
            studyEventOrdinal = studyEvent.getSampleOrdinal();

        } else {
            throw new OpenClinicaSystemException("Cannot schedule an event for this Subject");
        }

        HashMap<String, String> h = new HashMap<String, String>();
        h.put("eventDefinitionOID", eventDefinitionOID);
        h.put("studyEventOrdinal", studyEventOrdinal.toString());
        h.put("studySubjectOID", studySubject.getOid());
        return h;

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

    public Logger getLogger() {
        return logger;
    }

    public EventDefinitionCRFDAO getEventDefinitionCRFDao() {
        eventDefinitionCRFDao = eventDefinitionCRFDao != null ? eventDefinitionCRFDao : new EventDefinitionCRFDAO(dataSource);
        return eventDefinitionCRFDao;
    }

    @SuppressWarnings("rawtypes")
    public EventCRFDAO getEventCRFDao() {
        eventCrfDao = eventCrfDao != null ? eventCrfDao : new EventCRFDAO(dataSource);
        return eventCrfDao;
    }

    public ItemDataDAO getItemDataDao() {
        itemDataDao = itemDataDao != null ? itemDataDao : new ItemDataDAO(dataSource);
        return itemDataDao;
    }

    public FormLayoutDAO getFormLayoutDao() {
        formLayoutDao = formLayoutDao != null ? formLayoutDao : new FormLayoutDAO(dataSource);
        return formLayoutDao;
    }

    public CRFDAO getCrfDao() {
        crfDao = crfDao != null ? crfDao : new CRFDAO(dataSource);
        return crfDao;
    }

    public DiscrepancyNoteDAO getDiscrepancyNoteDao() {
        discrepancyNoteDao = discrepancyNoteDao != null ? discrepancyNoteDao : new DiscrepancyNoteDAO(dataSource);
        return discrepancyNoteDao;
    }

}