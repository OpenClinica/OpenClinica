package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.admin.AuditBean;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.odmbeans.StudyEventDefBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.admin.AuditDAO;
import org.akaza.openclinica.dao.admin.AuditEventDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudySubject;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping(value = "/odmss")
public class OdmStudySubjectController {

	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;

	@Autowired
	CoreResources coreResources;

	@Autowired
	ServletContext context;

	@Autowired
	AccountController accountController;

	StudyDAO sdao;
	ParticipantPortalRegistrar participantPortalRegistrar;
	public static final String FORM_CONTEXT = "ecid";

	private MessageSource messageSource;
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * This URL needs to change ... Right now security disabled on this ... You can call this with http://localhost:8080/OpenClinica-web-MAINLINE-SNAPSHOT /pages/odmk/studies/S_DEFAULTS1/events
	 *
	 * @param studyOid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/study/{studyOid}/crc/{crcUserName}/studysubject/{studySubjectId}", method = RequestMethod.GET)
	public @ResponseBody ODM getSubjectODM(@PathVariable("studyOid") String studyOid, @PathVariable("crcUserName") String crcUserName, @PathVariable("studySubjectId") String studySubjectLabel)
			throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));

		return getODM(studyOid, studySubjectLabel, crcUserName);
	}

	private ODM getODM(String studyOID, String studySubjectLabel, String crcUserName) {

		StudyDAO studyDAO = new StudyDAO(dataSource);
		StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
		StudyBean studyBean = null;
		StudySubjectBean studySubjectBean = null;
		try {
			// Retrieve crfs for next event
			studyBean = studyDAO.findByOid(studyOID);
			if (studyBean != null) {
				studySubjectBean = (StudySubjectBean) studySubjectDAO.findByLabelAndStudy(studySubjectLabel, studyBean);
				if (!mayProceed(studyOID, studySubjectBean))
					return null;

				if (studySubjectBean.getId() != 0 && !accountController.isCRCHasAccessToStudySubject(studyOID, crcUserName, studySubjectLabel)) {
					return createOdm(studyBean, studySubjectBean);

				} else if (studySubjectBean.getId() != 0 && accountController.isCRCHasAccessToStudySubject(studyOID, crcUserName, studySubjectLabel)) {
					return null;

				} else {
					return createOdm(studyBean, null);
				}
			} else {
				return createOdm(null, null);
			}
		} catch (Exception e) {
			logger.debug(e.getMessage());
			logger.debug(ExceptionUtils.getStackTrace(e));
		}

		return null;

	}

	private ODM createOdm(StudyBean studyBean, StudySubjectBean studySubjectBean) {
		ODM odm = new ODM();
		ODMcomplexTypeDefinitionClinicalData clinicalData = null;
		if (studyBean != null) {
			clinicalData = generateClinicalData(studyBean);

			if (studySubjectBean != null) {
				ODMcomplexTypeDefinitionSubjectData subjectData = generateSubjectData(studySubjectBean);
				clinicalData.getSubjectData().add(subjectData);
			}
			odm.getClinicalData().add(clinicalData);
		}
		return odm;
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

	public static boolean isAjaxRequest(String requestedWith) {
		return requestedWith != null ? "XMLHttpRequest".equals(requestedWith) : false;
	}

	public static boolean isAjaxUploadRequest(HttpServletRequest request) {
		return request.getParameter("ajaxUpload") != null;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
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
		StudyBean study = getParentStudy(studyOid);
		StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
		StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "participantPortal");
		participantPortalRegistrar = new ParticipantPortalRegistrar();
		String pManageStatus = participantPortalRegistrar.getRegistrationStatus(studyOid).toString(); // ACTIVE , PENDING , INACTIVE
		String participateStatus = pStatus.getValue().toString(); // enabled , disabled
		String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
		logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus + "  studySubjectStatus: " + ssBean.getStatus().getName());
		System.out
				.println("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus + "  studySubjectStatus: " + ssBean.getStatus().getName());
		if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE") && ssBean.getStatus() == Status.AVAILABLE) {
			accessPermission = true;
		}
		return accessPermission;
	}

}
