/**
 * 
 */
package org.akaza.openclinica.service;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.ViewStudySubjectDTO;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfDao;
import org.akaza.openclinica.dao.hibernate.PageLayoutDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.PageLayout;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.datamap.SubjectEventStatus;
import org.akaza.openclinica.domain.user.UserAccount;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author joekeremian
 *
 */

public class ViewStudySubjectServiceImpl implements ViewStudySubjectService {
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	private final String COMMON = "common";

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

		ViewStudySubjectDTO viewStudySubjectDTO = null;

		request.setAttribute("requestSchema", "public");
		HttpSession session = request.getSession();
		Study publicstudy = studyDao.findByOcOID(studyOid);
		UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
		if (ub == null) {
			logger.error("userAccount with username {} is null", ub.getName());
			return null;
		}
		UserAccount userAccount = userAccountDao.findById(ub.getId());
		if (userAccount == null) {
			logger.error("userAccount with username {} is null", userAccount.getUserName());
			return null;
		}

		request.setAttribute("requestSchema", publicstudy.getSchemaName());
		Study study = studyDao.findByOcOID(studyOid);

		if (study == null) {
			logger.error("Study with Oid {} is null", study.getOc_oid());
			return null;
		} else if (study.getStudy() == null) {
			logger.info("the study with Oid {} is a Parent study", study.getOc_oid());
		} else {
			logger.info("the study with Oid {} is a Site study", study.getOc_oid());
		}

		StudySubject studySubject = studySubjectDao.findByOcOID(studySubjectOid);
		if (studySubject == null) {
			logger.error("StudySubject with Oid {} is null", studySubject.getOcOid());
			return null;
		}
		StudyEventDefinition studyEventDefinition = studyEventDefintionDao.findByOcOID(studyEventDefinitionOid);
		if (studyEventDefinition == null) {
			logger.error("StudyEventDefinition with Oid {} is null", studyEventDefinition.getOc_oid());
			return null;
		} else if (!studyEventDefinition.getType().equals(COMMON)) {
			logger.error("StudyEventDefinition with Oid {} is not a Common Type Event", studyEventDefinition.getOc_oid());
			return null;
		}
		CrfBean crf = crfDao.findByOcOID(crfOid);
		if (crf == null) {
			logger.error("Crf with Oid {} is null", crf.getOcOid());
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
			logger.error("FormLayout with Oid {} is null", formLayout.getOcOid());
			return null;
		}

		List<StudyEvent> studyEvents = studyEventDao.fetchListByStudyEventDefOID(studyEventDefinitionOid, studySubject.getStudySubjectId());
		Integer maxOrdinal = null;
		StudyEvent studyEvent = null;
		if (studyEvents != null && studyEvents.size() != 0) {
			maxOrdinal = studyEventDao.findMaxOrdinalByStudySubjectStudyEventDefinition(studySubject.getStudySubjectId(),
					studyEventDefinition.getStudyEventDefinitionId());

			if (!studyEventDefinition.getRepeating()) {
				logger.error("StudyEventDefinition with Oid {} is Non Repeating", studyEventDefinition.getOc_oid());

				for (StudyEvent sEvent : studyEvents) {
					List<EventCrf> eventCrfs = (List<EventCrf>) eventCrfDao.findByStudyEventIdStudySubjectIdCrfId(sEvent.getStudyEventId(),
							studySubject.getStudySubjectId(), crf.getCrfId());
					if (eventCrfs.size() != 0) {
						logger.error("EventCrf with StudyEventDefinition Oid {},Crf Oid {} and StudySubjectOid {} already exist in the System",
								studyEventDefinition.getOc_oid(), crf.getOcOid(), studySubject.getOcOid());
						return null;
					}
				}
			}
		} else {
			logger.error("No previous study event found for this studyEventDef Oid {} and subject Oid{}", studyEventDefinition.getOc_oid(),
					studySubject.getOcOid());
			maxOrdinal = new Integer(0);
		}

		studyEvent = scheduleNewStudyEvent(studySubject, studyEventDefinition, maxOrdinal, userAccount);

		if (studyEvent == null) {
			logger.error("StudyEvent with studyEventId {} is null", studyEvent.getStudyEventId());
			return null;
		}

		String url = "/EnketoFormServlet?formLayoutId=" + formLayout.getFormLayoutId() + "&studyEventId=" + studyEvent.getStudyEventId()
				+ "&eventCrfId=0&originatingPage=ViewStudySubject%3Fid%3D" + studySubject.getStudySubjectId() + "&mode=edit";

		viewStudySubjectDTO = new ViewStudySubjectDTO();
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
		studyEvent.setSampleOrdinal(maxOrdinal + new Integer(1));
		studyEvent.setSubjectEventStatusId(SubjectEventStatus.NOT_SCHEDULED.getCode());
		studyEvent.setStatusId(Status.AVAILABLE.getCode());
		studyEvent.setStudySubject(studySubject);
		studyEvent.setDateCreated(new Date());
		studyEvent.setUserAccount(userAccount);
		studyEvent.setDateStart(null);
		studyEvent.setStartTimeFlag(new Boolean(false));
		studyEvent.setEndTimeFlag(new Boolean(false));
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

	public EventDefinitionCrfDao getEventDefinitionCrfDao() {
		return eventDefinitionCrfDao;
	}

	public void setEventDefinitionCrfDao(EventDefinitionCrfDao eventDefinitionCrfDao) {
		this.eventDefinitionCrfDao = eventDefinitionCrfDao;
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

	public StudyEventDefinitionDao getStudyEventDefintionDao() {
		return studyEventDefintionDao;
	}

	public void setStudyEventDefintionDao(StudyEventDefinitionDao studyEventDefintionDao) {
		this.studyEventDefintionDao = studyEventDefintionDao;
	}

	public PageLayoutDao getPageLayoutDao() {
		return pageLayoutDao;
	}

	public void setPageLayoutDao(PageLayoutDao pageLayoutDao) {
		this.pageLayoutDao = pageLayoutDao;
	}

}
