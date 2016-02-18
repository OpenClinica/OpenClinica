package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.login.UserDTO;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.managestudy.EventDefinitionCrfTagService;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

@Controller
@RequestMapping(value = "/api/v1/anonymousform")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class AnonymousFormController {

	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;

	@Autowired
	ServletContext context;

	public static final String FORM_CONTEXT = "ecid";
	ParticipantPortalRegistrar participantPortalRegistrar;

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	UserAccountDAO udao;
	StudyDAO sdao;


	/**
	 * @api {post} /pages/api/v1/anonymousform/form Retrieve anonymous form URL
	 * @apiName getEnketoForm
	 * @apiPermission Module participate - enabled
	 * @apiVersion 3.8.0
	 * @apiParam {String} studyOid Study Oid
	 * @apiParam {String} submissionUri Submission Url
	 * @apiGroup Form
	 * @apiDescription Retrieve anonymous form url.
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "studyOid": "S_BL101",
	 *                  "submissionUri": "abcde"
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "http://localhost:8006/::YYYi?iframe=true&ecid=abb764d026830e98b895ece6d9dcaf3c5e817983cc00a4ebfaabcb6c3700b4d5"
	 *                    }
	 */

	@RequestMapping(value = "/form", method = RequestMethod.POST)
	public ResponseEntity<String> getEnketoForm(@RequestBody HashMap<String, String> map) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		String formUrl = null;
		System.out.println("I'm in EnketoForm Rest Method");
		String studyOid = map.get("studyOid");

		if (!mayProceed(studyOid))
			return new ResponseEntity<String>(formUrl, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		String submissionUri = map.get("submissionUri");
		if (submissionUri != "" && submissionUri != null) {


			StudyBean parentStudy = getParentStudy(studyOid);
			StudyBean study = getStudy(studyOid);

			EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(dataSource);
			StudyEventDefinitionDAO sedao = new StudyEventDefinitionDAO(dataSource);
			ArrayList<EventDefinitionCRFBean> edcBeans = edcdao.findAllSubmissionUriAndStudyId(submissionUri, study.getId());
			EventDefinitionCrfTagService eventDefinitionCrfTagService = (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");

			if (edcBeans.size() != 0) {
				EventDefinitionCRFBean edcBean = edcBeans.get(0);
				StudyEventDefinitionBean sed = (StudyEventDefinitionBean) sedao.findByPK(edcBean.getStudyEventDefinitionId());
				CRFVersionDAO cvdao = new CRFVersionDAO<>(dataSource);
				CRFVersionBean crfVersionBean = (CRFVersionBean) cvdao.findByPK(edcBean.getDefaultVersionId());
                StudyBean sBean = (StudyBean) sdao.findByPK(edcBean.getStudyId());
                CRFDAO cdao = new CRFDAO(dataSource);
                CRFBean crf = (CRFBean) cdao.findByPK(edcBean.getCrfId());
                String crfPath=sed.getOid()+"."+crf.getOid();
                String offline = eventDefinitionCrfTagService.getEventDefnCrfOfflineStatus(2,crfPath,true) ? "true" : "false";
				formUrl = createAnonymousEnketoUrl(sBean.getOid(), crfVersionBean ,edcBean.getStudyEventDefinitionId()) + "&offline="+offline;
				System.out.println("FormUrl:  " + formUrl);
				return new ResponseEntity<String>(formUrl, org.springframework.http.HttpStatus.OK);
			} else {
				return new ResponseEntity<String>(formUrl, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
			}
		} else {
			return new ResponseEntity<String>(formUrl, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		}
	}

	private StudyBean getParentStudy(Integer studyId) {
		StudyBean study = getStudy(studyId);
		if (study.getParentStudyId() == 0) {
			return study;
		} else {
			StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
			return parentStudy;
		}

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

	private StudyBean getStudy(Integer id) {
		sdao = new StudyDAO(dataSource);
		StudyBean studyBean = (StudyBean) sdao.findByPK(id);
		return studyBean;
	}

	private StudyBean getStudy(String oid) {
		sdao = new StudyDAO(dataSource);
		StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
		return studyBean;
	}

	private String createAnonymousEnketoUrl(String studyOID, CRFVersionBean crfVersion, int studyEventDefinitionId) throws Exception {
		StudyBean parentStudyBean = getParentStudy(studyOID);
		PFormCache cache = PFormCache.getInstance(context);
		String enketoURL = cache.getPFormURL(parentStudyBean.getOid(), crfVersion.getOid());
		String contextHash = cache.putAnonymousFormContext(studyOID, crfVersion.getOid(),studyEventDefinitionId);

		String url = enketoURL + "&" + FORM_CONTEXT + "=" + contextHash;
		logger.debug("Enketo URL for " + crfVersion.getName() + "= " + url);
		return url;

	}
	private boolean mayProceed(String studyOid) throws Exception {
		boolean accessPermission = false;
		StudyBean siteStudy = getStudy(studyOid);
		StudyBean study = getParentStudy(studyOid);
		StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
		StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "participantPortal");
		participantPortalRegistrar = new ParticipantPortalRegistrar();
		String pManageStatus = participantPortalRegistrar.getRegistrationStatus(study.getOid()).toString(); // ACTIVE , PENDING , INACTIVE
		String participateStatus = pStatus.getValue().toString(); // enabled , disabled
		String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
		String siteStatus = siteStudy.getStatus().getName().toString(); // available , pending , frozen , locked
		System.out.println("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus + "   siteStatus: " + siteStatus);
		logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus  + "   siteStatus: " + siteStatus);
		if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && siteStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE")) {
			accessPermission = true;
		}

		return accessPermission;
	}

}
