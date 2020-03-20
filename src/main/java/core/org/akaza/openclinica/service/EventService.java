package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class EventService implements EventServiceInterface {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	SubjectDAO subjectDao;
	StudySubjectDAO studySubjectDao;
	UserAccountDAO userAccountDao;
	StudyEventDefinitionDAO studyEventDefinitionDao;
	StudyEventDAO studyEventDao;

	EventDefinitionCRFDAO eventDefinitionCRFDao;
	EventCRFDAO eventCrfDao;
	ItemDataDAO itemDataDao;
	DataSource dataSource;
	FormLayoutDAO formLayoutDao;
	CRFDAO crfDao;
	DiscrepancyNoteDAO discrepancyNoteDao;
	@Autowired
	OdmImportService odmImportService;
	
	@Autowired
	private StudyDao studyDao;
	
	public EventService(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void removeStudyEventDefn(int defId, int userId, Study study) {
		StudyEventDefinitionBean sed = (StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(defId);
		UserAccountBean ub = getUserAccount();

		// finds all study events
		ArrayList<StudyEventBean> events = (ArrayList) getStudyEventDao().findAllByDefinition(sed.getId());

		sed.setStatus(Status.DELETED);
		sed.setUpdater(ub);
		sed.setUpdatedDate(new Date());
		getStudyEventDefinitionDao().update(sed);

		// remove all study events
		for (int j = 0; j < events.size(); j++) {
			StudyEventBean event = (StudyEventBean) events.get(j);
			if (event.getArchived()==null || ( event.getArchived() != null && !event.getArchived())) {
				event.setArchived(Boolean.TRUE);
				event.setUpdater(ub);
				event.setUpdatedDate(new Date());
				getStudyEventDao().update(event);

				// remove all event crfs
				ArrayList eventCRFs = getEventCRFDao().findAllByStudyEvent(event);
				for (int k = 0; k < eventCRFs.size(); k++) {
					EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);

					// remove all item data
					ArrayList itemDatas = getItemDataDao().findAllByEventCRFId(eventCRF.getId());
					for (int a = 0; a < itemDatas.size(); a++) {
						ItemDataBean itemData = (ItemDataBean) itemDatas.get(a);
						closeDns(ub, study, itemData);
					}
				}
			}
		}
	}

	@SuppressWarnings( {"unchecked", "rawtypes"} )
	public void restoreStudyEventDefn(int defId, int userId) {
		StudyEventDefinitionBean sed = (StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(defId);
		UserAccountBean ub = getUserAccount();
		// find all Event Defn CRFs
		ArrayList<EventDefinitionCRFBean> eventDefinitionCRFs = (ArrayList) getEventDefinitionCRFDao().findAllByDefinition(defId);
		// finds all events
		ArrayList<StudyEventBean> events = (ArrayList) getStudyEventDao().findAllByDefinition(sed.getId());

		sed.setStatus(Status.AVAILABLE);
		sed.setUpdater(ub);
		sed.setUpdatedDate(new Date());
		getStudyEventDefinitionDao().update(sed);

		// restore all study events
		for (int j = 0; j < events.size(); j++) {
			StudyEventBean event = (StudyEventBean) events.get(j);
			if (event.getArchived() != null && event.getArchived()) {
				event.setArchived(Boolean.FALSE);
				event.setUpdater(ub);
				event.setUpdatedDate(new Date());
				getStudyEventDao().update(event);
			}
		}
	}

	public void removeCrfFromEventDefinition(int eventDefnCrfId, int defId, int userId, int studyId) {
		Study study = (Study) studyDao.findByPK(studyId);
		StudyEventDefinitionBean sed = (StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(defId);
		EventDefinitionCRFBean edc = (EventDefinitionCRFBean) getEventDefinitionCRFDao().findByPK(eventDefnCrfId);
		UserAccountBean ub = getUserAccount();
		removeAllEventsItems(edc, sed, ub, study);
	}

	public void restoreCrfFromEventDefinition(int eventDefnCrfId, int defId, int userId) {
		StudyEventDefinitionBean sed = (StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(defId);
		EventDefinitionCRFBean edc = (EventDefinitionCRFBean) getEventDefinitionCRFDao().findByPK(eventDefnCrfId);
		UserAccountBean ub = getUserAccount();
		restoreAllEventsItems(edc, sed, ub);
	}

	public void removeAllEventsItems(EventDefinitionCRFBean edc, StudyEventDefinitionBean sed, UserAccountBean ub, Study study) {
		logger.info("Archive All Event Crf " );
		CRFBean crf = (CRFBean) getCrfDao().findByPK(edc.getCrfId());
		// Getting Study Events
		ArrayList seList = getStudyEventDao().findAllByStudyEventDefinitionAndCrfOids(sed.getOid(), crf.getOid());
		for (int j = 0; j < seList.size(); j++) {
			StudyEventBean seBean = (StudyEventBean) seList.get(j);
			// Getting Event CRFs
			ArrayList ecrfList = getEventCRFDao().findAllByStudyEventAndFormOrFormLayoutOid(seBean, crf.getOid());
			for (int k = 0; k < ecrfList.size(); k++) {
				EventCRFBean ecrfBean = (EventCRFBean) ecrfList.get(k);
				ecrfBean.setArchived(Boolean.TRUE);
				ecrfBean.setUpdater(ub);
				ecrfBean.setUpdatedDate(new Date());
				getEventCRFDao().update(ecrfBean);
				// Getting Item Data
				ArrayList itemDatas = getItemDataDao().findAllByEventCRFId(ecrfBean.getId());
				// remove all the item data
				for (int a = 0; a < itemDatas.size(); a++) {
					ItemDataBean itemData = (ItemDataBean) itemDatas.get(a);
						closeDns( ub ,  study ,  itemData);
				}
			}
		}
	}

	public void restoreAllEventsItems(EventDefinitionCRFBean edc, StudyEventDefinitionBean sed, UserAccountBean ub) {
		logger.info("Restoring Archived event_crfs" );
		CRFBean crf = (CRFBean) getCrfDao().findByPK(edc.getCrfId());
		// All Study Events
		ArrayList seList = getStudyEventDao().findAllByStudyEventDefinitionAndCrfOids(sed.getOid(), crf.getOid());
		for (int j = 0; j < seList.size(); j++) {
			StudyEventBean seBean = (StudyEventBean) seList.get(j);
			// All Event CRFs
			ArrayList ecrfList = getEventCRFDao().findAllByStudyEventAndFormOrFormLayoutOid(seBean, crf.getOid());
			for (int k = 0; k < ecrfList.size(); k++) {
				EventCRFBean ecrfBean = (EventCRFBean) ecrfList.get(k);
				ecrfBean.setArchived(Boolean.FALSE);
				ecrfBean.setUpdater(ub);
				ecrfBean.setUpdatedDate(new Date());
				getEventCRFDao().update(ecrfBean);

			}
		}

	}

	public HashMap<String, String> scheduleEvent(UserAccountBean user, Date startDateTime, Date endDateTime, String location, String studyUniqueId,
			String siteUniqueId, String eventDefinitionOID, String studySubjectId) throws OpenClinicaSystemException {

		// Business Validation
		Study study = studyDao.findByUniqueId(studyUniqueId);
		int parentStudyId = study.getStudyId();
		if (siteUniqueId != null) {
			study = studyDao.findSiteByUniqueIdentifier(studyUniqueId, siteUniqueId);
		}
		StudyEventDefinitionBean studyEventDefinition = getStudyEventDefinitionDao().findByOidAndStudy(eventDefinitionOID, study.getStudyId(), parentStudyId);
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
			studyEvent.setWorkflowStatus(StudyEventWorkflowStatusEnum.SCHEDULED);
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
	public StudySubjectDAO getStudySubjectDao() {
		studySubjectDao = studySubjectDao != null ? studySubjectDao : new StudySubjectDAO(dataSource);
		return studySubjectDao;
	}

	/**
	 * @return the UserAccountDao
	 */
	public UserAccountBean getUserAccount() {
		UserAccountBean ub = new UserAccountBean();
		ub.setId(1);
		ub.setName("root");
		return ub;
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
	 * @param dataSource
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

	public void closeDns(UserAccountBean ub , Study study , ItemDataBean itemData){
		List dnNotesOfRemovedItem = getDiscrepancyNoteDao().findParentNotesOnlyByItemData(itemData.getId());
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
				dnb.setThreadUuid(itemParentNote.getThreadUuid());
			}
			dnb.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId());  // set to closed-modified
			dnb.setStudyId(study.getStudyId());
			dnb.setAssignedUserId(ub.getId());
			dnb.setOwner(ub);
			dnb.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
			dnb.setEntityId(itemData.getId());
			dnb.setColumn("value");
			dnb.setCreatedDate(new Date());
			String detailedNotes="The item has been removed, this Query has been Closed.";
			dnb.setDetailedNotes(detailedNotes);
			getDiscrepancyNoteDao().create(dnb);
			getDiscrepancyNoteDao().createMapping(dnb);
			itemParentNote.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId());  // set to closed-modified
			itemParentNote.setDetailedNotes(detailedNotes);
			getDiscrepancyNoteDao().update(itemParentNote);
		}
	}

}