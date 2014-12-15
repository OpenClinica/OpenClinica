package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.admin.AuditBean;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.admin.AuditDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
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

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @RequestMapping(value = "/studies/{study}/metadata", method = RequestMethod.GET)
    public ModelAndView getStudyMetadata(Model model, HttpSession session, @PathVariable("study") String studyOid, HttpServletResponse response) throws Exception {
        return ruleController.studyMetadata(model,session,studyOid,response);
    }

	/**
	 * This URL needs to change ... Right now security disabled on this ... You
	 * can call this with
	 * http://localhost:8080/OpenClinica-web-MAINLINE-SNAPSHOT
	 * /pages/odmk/studies/S_DEFAULTS1/events
	 *
	 * @param studyOid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/study/{studyOid}/studysubject/{studySubjectOid}/events", method = RequestMethod.GET)
	public @ResponseBody ODM getEvent(@PathVariable("studyOid") String studyOid, @PathVariable("studySubjectOid") String studySubjectOid)
			throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));

		return getODM(studyOid, studySubjectOid);
	}

	private ODM getODM(String studyOID, String subjectKey) {

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
			ArrayList<CRFVersionBean> crfs = versionDAO.findDefCRFVersionsByStudyEvent(nextEvent.getStudyEventDefinitionId());
			List<EventCRFBean> eventCrfs = eventCRFDAO.findAllByStudyEvent(nextEvent);
			StudyBean study = studyDAO.findByOid(studyOID);
			StudySubjectBean studySubjectBean = studySubjectDAO.findByOid(ssoid);

			// Only return info for CRFs that are not started, completed, or started but do not have any
			// saved item data associated with them.
			for (CRFVersionBean crfVersion : crfs) {
				boolean itemDataExists = false;
				boolean validStatus = true;
				for (EventCRFBean eventCrf:eventCrfs)
				{
					if (eventCrf.getCRFVersionId() == crfVersion.getId())
					{
						int eventStatus = eventCrf.getStatus().getId();
						if (eventStatus != 1 && eventStatus != 2) validStatus = false;
						if (eventStatus == 1 && itemDataDAO.findAllByEventCRFId(eventCrf.getId()).size() > 0) itemDataExists = true;
					}
				}
				if (!itemDataExists && validStatus)
				{
					String formUrl = createEnketoUrl(studyOID, crfVersion, nextEvent, ssoid);
					formDatas.add(getFormDataPerCrf(crfVersion, nextEvent, eventCrfs, crfDAO, formUrl));				
				}
			}
			return createOdm(study, studySubjectBean, nextEvent, formDatas);

		} catch (Exception e) {
			logger.debug(e.getMessage());
			logger.debug(ExceptionUtils.getStackTrace(e));
		}

		return null;

	}

	private StudyEventDefinitionBean getStudyEventDefinitionBean(int ID) {
		StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(dataSource);
		StudyEventDefinitionBean studyEventDefinitionBean = (StudyEventDefinitionBean) seddao.findByPK(ID);
		return studyEventDefinitionBean;
	}

	private ODM createOdm(StudyBean study, StudySubjectBean studySubjectBean, StudyEventBean nextEvent,
			List<ODMcomplexTypeDefinitionFormData> formDatas) {
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
		String contextHash = cache.putSubjectContext(ssoid, String.valueOf(nextEvent.getStudyEventDefinitionId()),
				String.valueOf(nextEvent.getSampleOrdinal()), crfVersion.getOid());

		String url = enketoURL + "?" + FORM_CONTEXT + "=" + contextHash;
		logger.debug("Enketo URL for " + crfVersion.getName() + "= " + url);
		return url;

	}

	private ODMcomplexTypeDefinitionFormData getFormDataPerCrf(CRFVersionBean crfVersion, StudyEventBean nextEvent,
			List<EventCRFBean> eventCrfs, CRFDAO crfDAO, String formUrl) {
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

	private ODMcomplexTypeDefinitionFormData generateFormData(CRFVersionBean crfVersionBean, StudyEventBean nextEvent,
			EventCRFBean eventCRFBean, CRFBean crfBean, String formUrl) {
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
			}			}
		return formData;
	}

    /**
     * Currently not used, but keep here for future unit test
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


}
