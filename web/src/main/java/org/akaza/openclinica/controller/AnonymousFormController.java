package org.akaza.openclinica.controller;

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
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
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

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	UserAccountDAO udao;
	StudyDAO sdao;

	@RequestMapping(value = "/form", method = RequestMethod.POST)
	public ResponseEntity<String> getEnketoForm(@RequestBody HashMap<String, String> map) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		String formUrl = null;
		System.out.println("I'm in EnketoForm Rest Method");

		String submissionUri = map.get("submissionUri");
		if (submissionUri != "" && submissionUri != null) {
			String studyOid = map.get("studyOid");

			StudyBean parentStudy = getParentStudy(studyOid);
			StudyBean study = getStudy(studyOid);

			EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(dataSource);
			ArrayList<EventDefinitionCRFBean> edcBeans = edcdao.findAllSubmissionUriAndStudyId(submissionUri, study.getId());
			if (edcBeans.size() != 0) {
				EventDefinitionCRFBean edcBean = edcBeans.get(0);
				CRFVersionDAO cvdao = new CRFVersionDAO<>(dataSource);
				CRFVersionBean crfVersionBean = (CRFVersionBean) cvdao.findByPK(edcBean.getDefaultVersionId());
                StudyBean sBean = (StudyBean) sdao.findByPK(edcBean.getStudyId());
				
				formUrl = createAnonymousEnketoUrl(sBean.getOid(), crfVersionBean ,edcBean.getStudyEventDefinitionId());
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
		PFormCache cache = PFormCache.getInstance(context);
		String enketoURL = cache.getPFormURL(studyOID, crfVersion.getOid());
		String contextHash = cache.putAnonymousFormContext(studyOID, crfVersion.getOid(),studyEventDefinitionId);

		String url = enketoURL + "&" + FORM_CONTEXT + "=" + contextHash;
		logger.debug("Enketo URL for " + crfVersion.getName() + "= " + url);
		return url;

	}

}
