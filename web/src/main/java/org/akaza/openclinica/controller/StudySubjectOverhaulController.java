package org.akaza.openclinica.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.SSOverhaulDTO;
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
import org.akaza.openclinica.service.Page;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudySubjectOverhaulController {

	@Autowired
	private StudyEventDao studyEventDao;

	@Autowired
	private StudySubjectDao studySubjectDao;

	@Autowired
	private StudyDao studyDao;

	@Autowired
	private StudyEventDefinitionDao studyEventDefintionDao;

	@Autowired
	private CrfDao crfDao;

	@Autowired
	private EventCrfDao eventCrfDao;

	@Autowired
	private EventDefinitionCrfDao eventDefinitionCrfDao;

	@Autowired
	private UserAccountDao userAccountDao;

	@Autowired
	private PageLayoutDao pageLayoutDao;

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	private final String COMMON = "common";

	/**
	 * Schedule new event Overhaul and add a form to event
	 * 
	 * @param request
	 * @param studyOid
	 * @param studyEventDefinitionOid
	 * @param crfOid
	 * @param studySubjectOid
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@SuppressWarnings("null")
	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(value = "/api/addAnotherForm", method = RequestMethod.POST)
	public ResponseEntity<SSOverhaulDTO> scheduleNewEvent(HttpServletRequest request, @RequestParam("studyoid") String studyOid,
			@RequestParam("studyeventdefinitionoid") String studyEventDefinitionOid, @RequestParam("crfoid") String crfOid,
			@RequestParam("studysubjectoid") String studySubjectOid) throws IOException, URISyntaxException {

		SSOverhaulDTO obj = null;
		request.setAttribute("requestSchema", "public");
		HttpSession session = request.getSession();
		Study publicstudy = studyDao.findByOcOID(studyOid);
		UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
		if (ub == null) {
			logger.error("userAccount with username {} is null", ub.getName());
			return new ResponseEntity<SSOverhaulDTO>(obj, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		}
		UserAccount userAccount = userAccountDao.findById(ub.getId());
		if (userAccount == null) {
			logger.error("userAccount with username {} is null", userAccount.getUserName());
			return new ResponseEntity<SSOverhaulDTO>(obj, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		}

		request.setAttribute("requestSchema", publicstudy.getSchemaName());
		Study study = studyDao.findByOcOID(studyOid);

		if (study == null) {
			logger.error("Study with Oid {} is null", study.getOc_oid());
			return new ResponseEntity<SSOverhaulDTO>(obj, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		} else if (study.getStudy() == null) {
			logger.info("the study with Oid {} is a Parent study", study.getOc_oid());
		} else {
			logger.info("the study with Oid {} is a Site study", study.getOc_oid());
		}

		StudySubject studySubject = studySubjectDao.findByOcOID(studySubjectOid);
		if (studySubject == null) {
			logger.error("StudySubject with Oid {} is null", studySubject.getOcOid());
			return new ResponseEntity<SSOverhaulDTO>(obj, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		}
		StudyEventDefinition studyEventDefinition = studyEventDefintionDao.findByOcOID(studyEventDefinitionOid);
		if (studyEventDefinition == null) {
			logger.error("StudyEventDefinition with Oid {} is null", studyEventDefinition.getOc_oid());
			return new ResponseEntity<SSOverhaulDTO>(obj, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		} else if (!studyEventDefinition.getType().equals(COMMON)) {
			logger.error("StudyEventDefinition with Oid {} is not a Common Type Event", studyEventDefinition.getOc_oid());
			return new ResponseEntity<SSOverhaulDTO>(obj, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		}
		CrfBean crf = crfDao.findByOcOID(crfOid);
		if (crf == null) {
			logger.error("Crf with Oid {} is null", crf.getOcOid());
			return new ResponseEntity<SSOverhaulDTO>(obj, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		}

		EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(),
				crf.getCrfId(), study.getStudyId());
		if (edc == null) {
			edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(),
					study.getStudy().getStudyId());
		}
		if (edc == null) {
			logger.error("EventDefinitionCrf for StudyEventDefinition Oid {},Crf Oid {} and Study Oid {}is null", studyEventDefinition.getOc_oid(),
					crf.getOcOid(), study.getOc_oid());
			return new ResponseEntity<SSOverhaulDTO>(obj, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		}
		FormLayout formLayout = edc.getFormLayout();
		if (formLayout == null) {
			logger.error("FormLayout with Oid {} is null", formLayout.getOcOid());
			return new ResponseEntity<SSOverhaulDTO>(obj, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
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
						return new ResponseEntity<SSOverhaulDTO>(obj, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
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
			return new ResponseEntity<SSOverhaulDTO>(obj, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		}

		String url = "/EnketoFormServlet?formLayoutId=" + formLayout.getFormLayoutId() + "&studyEventId=" + studyEvent.getStudyEventId()
				+ "&eventCrfId=0&originatingPage=ViewStudySubject%3Fid%3D" + studySubject.getStudySubjectId() + "&mode=edit";

		obj = new SSOverhaulDTO();
		obj.setUrl(url);
		return new ResponseEntity<SSOverhaulDTO>(obj, org.springframework.http.HttpStatus.OK);
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

	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(value = "/api/studies/{studyoid}/pages/{name}", method = RequestMethod.GET)
	public ResponseEntity<Page> getPageLayout(HttpServletRequest request, @PathVariable("studyoid") String studyOid, @PathVariable("name") String name) {
		Page page = null;
		PageLayout pageLayout = pageLayoutDao.findByPageLayoutName(name);
		if (pageLayout != null) {
			page = (Page) SerializationUtils.deserialize(pageLayout.getDefinition());
			logger.info("Page Object retrieved from database with page name: {}", pageLayout.getName());
		}
		return new ResponseEntity<Page>(page, org.springframework.http.HttpStatus.OK);
	}
}
