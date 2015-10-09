package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.admin.AuditBean;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.admin.AuditDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.cdisc.ns.odm.v130_api.ODM;
import org.cdisc.ns.odm.v130_api.ODMcomplexTypeDefinitionClinicalData;
import org.cdisc.ns.odm.v130_api.ODMcomplexTypeDefinitionSubjectData;
import org.cdisc.ns.odm.v130_api.ODMcomplexTypeDefinitionStudyEventData;
import org.cdisc.ns.odm.v130_api.ODMcomplexTypeDefinitionFormData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping(value = "/odmk")
public class OdmController {

	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;

	@Autowired
	ServletContext context;

	@Autowired
	RuleController ruleController;

	public static final String FORM_CONTEXT = "ecid";
	ParticipantPortalRegistrar participantPortalRegistrar;
	StudyDAO sdao;

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * @api {get} /pages/odmk/studies/:studyOid/metadata Get Study Metadata
	 * @apiName getStudyMetadata
	 * @apiPermission admin
	 * @apiVersion 1.0.0
	 * @apiParam {String} studyOid Study Oid.
	 * @apiGroup Study
	 * @apiDescription This API is to get the Metadata of the provided Study
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "studyOid": "S_BL101",
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {	
	 *                    The whole Study Metadata
	 *                    }
	 */

	@RequestMapping(value = "/studies/{study}/metadata", method = RequestMethod.GET)
	public ModelAndView getStudyMetadata(Model model, HttpSession session, @PathVariable("study") String studyOid, HttpServletResponse response) throws Exception {
		if (!mayProceed(studyOid))
			return null;
		return ruleController.studyMetadata(model, session, studyOid, response);
	}

	/**
	 * This URL needs to change ... Right now security disabled on this ... You can call this with http://localhost:8080/OpenClinica-web-MAINLINE-SNAPSHOT /pages/odmk/studies/S_DEFAULTS1/events
	 *
	 * @param studyOid
	 * @return
	 * @throws Exception
	 */
	/**
	 * @api {get} /pages/odmk/study/:studyOid/studysubject/:studySubjectOid/events Get Event
	 * @apiName getEvent
	 * @apiPermission admin
	 * @apiVersion 1.0.0
	 * @apiParam {String} studyOid Study Oid.
	 * @apiParam {String} studySubjectOid Study Subject Oid
	 * @apiGroup Study Event
	 * @apiDescription This API is to get the Event with earliest start date and ordinal
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "studyOid": "S_BL101",
	 *                  "studySubjectOid": "SS_DYN101"
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "id": null,
	 *                    "signature": [],
	 *                    "clinicalData": [{
	 *                    "annotations": [],
	 *                    "subjectData": [{
	 *                    "annotation": [],
	 *                    "signature": null,
	 *                    "status": "available",
	 *                    "dateOfBirth": null,
	 *                    "uniqueIdentifier": null,
	 *                    "studyEventData": [{
	 *                    "annotation": [],
	 *                    "signature": null,
	 *                    "status": null,
	 *                    "eventName": "Scoring Visit",
	 *                    "studyEventRepeatKey": null,
	 *                    "endDate": null,
	 *                    "formData": [{
	 *                    "annotation": [],
	 *                    "signature": null,
	 *                    "status": "Not Started",
	 *                    "interviewerName": null,
	 *                    "formOID": "F_SCORING2_CRF_V10",
	 *                    "itemGroupData": [],
	 *                    "url": "http://localhost:8006/::YYYF?iframe=true&ecid=a480dc4479409f6fe99a03d472f5cf77f4f12fb2b5ac471b9d35d737d934b042",
	 *                    "version": null,
	 *                    "transactionType": null,
	 *                    "auditRecord": null,
	 *                    "archiveLayoutRef": null,
	 *                    "formDataElementExtension": [],
	 *                    "formRepeatKey": null,
	 *                    "interviewDate": null,
	 *                    "formName": "Scoring2_CRF",
	 *                    "versionDescription": "Scoring2",
	 *                    "statusChangeTimeStamp": null
	 *                    }],
	 *                    "studyEventOID": null,
	 *                    "transactionType": null,
	 *                    "auditRecord": null,
	 *                    "studyEventDataElementExtension": [],
	 *                    "studyEventLocation": null,
	 *                    "startDate": "2015-08-27 12:00:00.0",
	 *                    "subjectAgeAtEvent": null
	 *                    }],
	 *                    "studySubjectID": "DYN101",
	 *                    "transactionType": null,
	 *                    "yearOfBirth": null,
	 *                    "auditRecord": null,
	 *                    "investigatorRef": null,
	 *                    "siteRef": null,
	 *                    "subjectDataElementExtension": [],
	 *                    "subjectKey": "SS_DYN101",
	 *                    "secondaryID": null,
	 *                    "sex": null
	 *                    }],
	 *                    "studyName": "Baseline Study 101",
	 *                    "studyOID": "S_BL101",
	 *                    "metaDataVersionOID": null,
	 *                    "auditRecords": [],
	 *                    "signatures": []
	 *                    }],
	 *                    "fileType": null,
	 *                    "fileOID": null,
	 *                    "description": null,
	 *                    "study": [],
	 *                    "association": [],
	 *                    "odmversion": null,
	 *                    "creationDateTime": null,
	 *                    "adminData": [],
	 *                    "referenceData": [],
	 *                    "granularity": null,
	 *                    "archival": null,
	 *                    "priorFileOID": null,
	 *                    "asOfDateTime": null,
	 *                    "originator": null,
	 *                    "sourceSystem": null,
	 *                    "sourceSystemVersion": null
	 *                    }
	 */

	@RequestMapping(value = "/study/{studyOid}/studysubject/{studySubjectOid}/events", method = RequestMethod.GET)
	public @ResponseBody ODM getEvent(@PathVariable("studyOid") String studyOid, @PathVariable("studySubjectOid") String studySubjectOid) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));

		return getODM(studyOid, studySubjectOid);
	}

	private ODM getODM(String studyOID, String subjectKey) {
		ODM odm = new ODM();
		String ssoid = subjectKey;
		if (ssoid == null) {
			return null;
		}

		StudyEventDAO eventDAO = new StudyEventDAO(dataSource);
		CRFVersionDAO versionDAO = new CRFVersionDAO(dataSource);
		StudyDAO studyDAO = new StudyDAO(dataSource);
		StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
		EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
		ItemDataDAO itemDataDAO = new ItemDataDAO(dataSource);
		CRFDAO crfDAO = new CRFDAO(dataSource);
		List<ODMcomplexTypeDefinitionFormData> formDatas = new ArrayList<>();
		try {
			// Retrieve crfs for next event
			StudyEventBean nextEvent = (StudyEventBean) eventDAO.getNextScheduledEvent(ssoid);
			logger.debug("Found event: " + nextEvent.getName() + " - ID: " + nextEvent.getId());
			StudySubjectBean studySubjectBean = studySubjectDAO.findByOid(ssoid);
			ArrayList<CRFVersionBean> crfs = getCRFVersionBean(studySubjectBean, nextEvent);

			List<EventCRFBean> eventCrfs = eventCRFDAO.findAllByStudyEvent(nextEvent);
			StudyBean study = studyDAO.findByOid(studyOID);
			if (!mayProceed(studyOID, studySubjectBean))
				return odm;
			// Only return info for CRFs that are not started, completed, or started but do not have any
			// saved item data associated with them.
			for (CRFVersionBean crfVersion : crfs) {
				boolean itemDataExists = false;
				boolean validStatus = true;
				for (EventCRFBean eventCrf : eventCrfs) {
					if (eventCrf.getCRFVersionId() == crfVersion.getId()) {
						int eventStatus = eventCrf.getStatus().getId();
						if (eventStatus != 1 && eventStatus != 2)
							validStatus = false;
						if (eventStatus == 1 && itemDataDAO.findAllByEventCRFId(eventCrf.getId()).size() > 0)
							itemDataExists = true;
					}
				}
				if (!itemDataExists && validStatus) {
					String formUrl = createEnketoUrl(studyOID, crfVersion, nextEvent, ssoid);
					formDatas.add(getFormDataPerCrf(crfVersion, nextEvent, eventCrfs, crfDAO, formUrl));
				}
			}
			return createOdm(study, studySubjectBean, nextEvent, formDatas);

		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.error(ExceptionUtils.getStackTrace(e));
		}

		return odm;

	}

	private ArrayList<CRFVersionBean> getCRFVersionBean(StudySubjectBean studySubjectBean, StudyEventBean nextEvent) {
		EventDefinitionCRFDAO edcdao;

		Integer studyId = studySubjectBean.getStudyId();
		StudyDAO sdao = new StudyDAO<String, ArrayList>(dataSource);
		StudyBean studyBean = (StudyBean) sdao.findByPK(studyId);

		ArrayList<EventDefinitionCRFBean> eventDefCrfs = null;
		ArrayList<EventDefinitionCRFBean> parentEventDefCrfs = null;
		ArrayList<EventDefinitionCRFBean> netEventDefinitionCrfs = new ArrayList<EventDefinitionCRFBean>();
		ArrayList<CRFVersionBean> crfs = new ArrayList<CRFVersionBean>();
		Integer pStudyId = 0;
		edcdao = new EventDefinitionCRFDAO(dataSource);
		eventDefCrfs = (ArrayList<EventDefinitionCRFBean>) edcdao.findAllActiveByEventDefinitionIdandStudyId(nextEvent.getStudyEventDefinitionId(), studyId);

		StudyBean parentStudy = getParentStudy(studyBean.getOid());
		pStudyId = parentStudy.getId();

		edcdao = new EventDefinitionCRFDAO(dataSource);
		parentEventDefCrfs = (ArrayList<EventDefinitionCRFBean>) edcdao.findAllActiveByEventDefinitionIdandStudyId(nextEvent.getStudyEventDefinitionId(), pStudyId);

		boolean found;
		for (EventDefinitionCRFBean parentEventDefinitionCrf : parentEventDefCrfs) {
			found = false;
			for (EventDefinitionCRFBean eventDefinitionCrf : eventDefCrfs) {
				if (parentEventDefinitionCrf.getId() == eventDefinitionCrf.getParentId()) { //
					found = true;
					netEventDefinitionCrfs.add(eventDefinitionCrf);
					break;
				}
			}
			if (!found) {
				netEventDefinitionCrfs.add(parentEventDefinitionCrf);
			}
		}
		// netEventDefinitionCrfs = eventDefCrfs;

		sortList(netEventDefinitionCrfs);

		CRFVersionDAO cvdao = new CRFVersionDAO<String, ArrayList>(dataSource);
		for (EventDefinitionCRFBean eventDefinitionCrf : netEventDefinitionCrfs) {
			CRFVersionBean cvBean = (CRFVersionBean) cvdao.findByPK(eventDefinitionCrf.getDefaultVersionId());
			crfs.add(cvBean);
		}

		return crfs;
	}

	private StudyEventDefinitionBean getStudyEventDefinitionBean(int ID) {
		StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(dataSource);
		StudyEventDefinitionBean studyEventDefinitionBean = (StudyEventDefinitionBean) seddao.findByPK(ID);
		return studyEventDefinitionBean;
	}

	private ODM createOdm(StudyBean study, StudySubjectBean studySubjectBean, StudyEventBean nextEvent, List<ODMcomplexTypeDefinitionFormData> formDatas) {
		ODM odm = new ODM();

		ODMcomplexTypeDefinitionClinicalData clinicalData = generateClinicalData(study);
		ODMcomplexTypeDefinitionSubjectData subjectData = generateSubjectData(studySubjectBean);
		ODMcomplexTypeDefinitionStudyEventData studyEventData = generateStudyEventData(nextEvent);
		// Create the object graph
		studyEventData.getFormData().addAll(formDatas);
		subjectData.getStudyEventData().add(studyEventData);
		clinicalData.getSubjectData().add(subjectData);
		odm.getClinicalData().add(clinicalData);

		return odm;
	}

	private String createEnketoUrl(String studyOID, CRFVersionBean crfVersion, StudyEventBean nextEvent, String ssoid) throws Exception {
		PFormCache cache = PFormCache.getInstance(context);
		String enketoURL = cache.getPFormURL(studyOID, crfVersion.getOid());
		String contextHash = cache.putSubjectContext(ssoid, String.valueOf(nextEvent.getStudyEventDefinitionId()), String.valueOf(nextEvent.getSampleOrdinal()), crfVersion.getOid());

		String url = enketoURL + "&" + FORM_CONTEXT + "=" + contextHash;
		logger.debug("Enketo URL for " + crfVersion.getName() + "= " + url);
		return url;

	}

	private ODMcomplexTypeDefinitionFormData getFormDataPerCrf(CRFVersionBean crfVersion, StudyEventBean nextEvent, List<EventCRFBean> eventCrfs, CRFDAO crfDAO, String formUrl) {
		EventCRFBean selectedEventCRFBean = null;
		CRFBean crfBean = (CRFBean) crfDAO.findByVersionId(crfVersion.getId());
		for (EventCRFBean eventCRFBean : eventCrfs) {
			if (eventCRFBean.getCRFVersionId() == crfVersion.getId()) {
				selectedEventCRFBean = eventCRFBean;
				break;
			}
		}
		return generateFormData(crfVersion, nextEvent, selectedEventCRFBean, crfBean, formUrl);

	}

	private ODMcomplexTypeDefinitionClinicalData generateClinicalData(StudyBean study) {
		ODMcomplexTypeDefinitionClinicalData clinicalData = new ODMcomplexTypeDefinitionClinicalData();
		clinicalData.setStudyName(study.getName());
		clinicalData.setStudyOID(study.getOid());
		return clinicalData;
	}

	private ODMcomplexTypeDefinitionSubjectData generateSubjectData(StudySubjectBean studySubject) {
		ODMcomplexTypeDefinitionSubjectData subjectData = new ODMcomplexTypeDefinitionSubjectData();
		subjectData.setSubjectKey(studySubject.getOid());
		subjectData.setStudySubjectID(studySubject.getLabel());
		subjectData.setStatus(studySubject.getStatus().getName());
		return subjectData;
	}

	private ODMcomplexTypeDefinitionStudyEventData generateStudyEventData(StudyEventBean studyEvent) {
		ODMcomplexTypeDefinitionStudyEventData studyEventData = new ODMcomplexTypeDefinitionStudyEventData();
		studyEventData.setStartDate(studyEvent.getDateStarted().toString());
		StudyEventDefinitionBean studyEventDefBean = getStudyEventDefinitionBean(studyEvent.getStudyEventDefinitionId());
		studyEventData.setEventName(studyEventDefBean.getName());
		return studyEventData;
	}

	private ODMcomplexTypeDefinitionFormData generateFormData(CRFVersionBean crfVersionBean, StudyEventBean nextEvent, EventCRFBean eventCRFBean, CRFBean crfBean, String formUrl) {
		ODMcomplexTypeDefinitionFormData formData = new ODMcomplexTypeDefinitionFormData();
		formData.setFormOID(crfVersionBean.getOid());
		formData.setFormName(crfBean.getName());
		formData.setVersionDescription(crfVersionBean.getDescription());
		formData.setUrl(formUrl);
		if (eventCRFBean == null) {
			formData.setStatus("Not Started");
		} else {
			formData.setStatus(eventCRFBean.getStatus().getName());
			AuditDAO auditDAO = new AuditDAO(dataSource);
			List<AuditBean> auditBeans = (List<AuditBean>) auditDAO.findEventCRFAudit(eventCRFBean.getId());
			if (auditBeans.size() > 0) {
				formData.setStatusChangeTimeStamp(auditBeans.get(0).getAuditDate().toString());
			}
		}
		return formData;
	}

	/**
	 * Currently not used, but keep here for future unit test
	 * 
	 * @param clazz
	 * @param odm
	 * @return
	 * @throws Exception
	 */
	private String generateXmlFromObj(Class clazz, ODM odm) throws Exception {

		JAXBContext context = JAXBContext.newInstance(clazz);

		Marshaller m = context.createMarshaller();
		StringWriter w = new StringWriter();

		m.marshal(odm, w);
		return w.toString();
	}

	@SuppressWarnings("unchecked")
	private void sortList(ArrayList<EventDefinitionCRFBean> edcBeans) {

		Collections.sort(edcBeans, new Comparator() {

			public int compare(Object o1, Object o2) {

				Integer x1 = ((EventDefinitionCRFBean) o1).getOrdinal();
				Integer x2 = ((EventDefinitionCRFBean) o2).getOrdinal();
				int sComp = x1.compareTo(x2);

				return sComp;
			}
		});
	}

	private StudyBean getStudy(String oid) {
		sdao = new StudyDAO(dataSource);
		StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
		return studyBean;
	}

	private StudyBean getParentStudy(String studyOid) {
		StudyBean study = getStudy(studyOid);
		if (study.getParentStudyId() == 0) {
			return study;
		} else {
			StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
			return parentStudy;
		}

	}

	private boolean mayProceed(String studyOid, StudySubjectBean ssBean) throws Exception {
		boolean accessPermission = false;
		logger.info("  studySubjectStatus: " + ssBean.getStatus().getName());
		System.out.println("  studySubjectStatus: " + ssBean.getStatus().getName());
		if (mayProceed(studyOid) && ssBean.getStatus() == Status.AVAILABLE) {
			accessPermission = true;
		}
		return accessPermission;
	}

	private boolean mayProceed(String studyOid) throws Exception {
		boolean accessPermission = false;
		StudyBean study = getParentStudy(studyOid);
		StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
		StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "participantPortal");
		participantPortalRegistrar = new ParticipantPortalRegistrar();
		String pManageStatus = participantPortalRegistrar.getRegistrationStatus(studyOid).toString(); // ACTIVE , PENDING , INACTIVE
		String participateStatus = pStatus.getValue().toString(); // enabled , disabled
		String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
		System.out.println("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus);
		logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus);
		if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE")) {
			accessPermission = true;
		}
		return accessPermission;
	}

}
