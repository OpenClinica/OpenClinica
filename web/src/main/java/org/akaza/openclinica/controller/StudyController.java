package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.login.UserDTO;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@Controller
@RequestMapping(value = "/auth/api/v1/studies")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class StudyController {

	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;

	@Autowired
	ServletContext context;

	@Autowired
	AuthenticationManager authenticationManager;


	public static ResourceBundle resadmin, resaudit, resexception, resformat, respage, resterm, restext, resword, resworkflow;

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	UserAccountDAO udao;
	StudyDAO sdao;

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<Object> createNewStudy(HttpServletRequest request, @RequestBody HashMap<String, Object> map) throws Exception {
		StudyBean studyDTO = null;
		System.out.println("I'm in Create Study");
		String message = " field is missing from Study Json object";
		String uniqueProtocolID = (String) map.get("UniqueProtocolID");
		String name = (String) map.get("BriefTitle");
		String principalInvestigator = (String) map.get("PrincipalInvestigator");
		String briefSummary = (String) map.get("BriefSummary");
		String sponsor = (String) map.get("Sponsor");
		String protocolType = (String) map.get("ProtocolType");
		String startDate = (String) map.get("StartDate");
		String expectedTotalEnrollment = (String) map.get("ExpectedTotalEnrollment");
		ArrayList sites = (ArrayList) map.get("sites");

		if (uniqueProtocolID == null)
			return new ResponseEntity("UniqueProtocolID" + message, org.springframework.http.HttpStatus.BAD_REQUEST);
		if (name == null)
			return new ResponseEntity("BriefTitle (Study Name)" + message, org.springframework.http.HttpStatus.BAD_REQUEST);
		if (principalInvestigator == null)
			return new ResponseEntity("PrincipalInvestigator" + message, org.springframework.http.HttpStatus.BAD_REQUEST);
		if (briefSummary == null)
			return new ResponseEntity("BriefSummary (Description)" + message, org.springframework.http.HttpStatus.BAD_REQUEST);
		if (sponsor == null)
			return new ResponseEntity("Sponsor" + message, org.springframework.http.HttpStatus.BAD_REQUEST);
		if (protocolType == null)
			return new ResponseEntity("ProtocolType" + message, org.springframework.http.HttpStatus.BAD_REQUEST);
		if (startDate == null)
			return new ResponseEntity("StartDate" + message, org.springframework.http.HttpStatus.BAD_REQUEST);
		if (expectedTotalEnrollment == null)
			return new ResponseEntity("ExpectedTotalEnrollment" + message, org.springframework.http.HttpStatus.BAD_REQUEST);

		uniqueProtocolID = uniqueProtocolID.trim();
		name = name.trim();
		principalInvestigator = principalInvestigator.trim();
		briefSummary = briefSummary.trim();
		sponsor = sponsor.trim();
		protocolType = protocolType.trim();
		startDate = startDate.trim();
		expectedTotalEnrollment = expectedTotalEnrollment.trim();

		request.setAttribute("uniqueProId", uniqueProtocolID);
		request.setAttribute("name", name); // Brief Title
		request.setAttribute("prinInvestigator", principalInvestigator);
		request.setAttribute("description", briefSummary);
		request.setAttribute("sponsor", sponsor);
		request.setAttribute("startDate", startDate);
		request.setAttribute("expectedTotalEnrollment", expectedTotalEnrollment);

		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		Date formattedDate = formatter.parse(startDate);

		UserAccountBean ownerUserAccount = getStudyOwnerAccount(request);
		if (ownerUserAccount == null)
			return new ResponseEntity("The Owner User Account is not Valid Account or Does not have Admin user type", org.springframework.http.HttpStatus.BAD_REQUEST);

		Validator v = new Validator(request);
		addValidationToStudyFields(v);
		HashMap errors = v.validate();
		validateUniqueProId(request, errors);

		if (!errors.isEmpty()) {
			logger.info("Validation Error(s): " + errors.toString());
			System.out.println("Validation Errors: " + errors.toString());
			return new ResponseEntity(errors.toString(), org.springframework.http.HttpStatus.BAD_REQUEST);
		}

		if (!verfiyProtocolTypeExist(protocolType))
			return new ResponseEntity("Protocol Type is not Valid", org.springframework.http.HttpStatus.BAD_REQUEST); // /////// check owner's ROLE

		studyDTO = buildStudy(uniqueProtocolID, name, principalInvestigator, protocolType, briefSummary, sponsor, ownerUserAccount, Integer.valueOf(expectedTotalEnrollment), formattedDate);

		StudyBean studyBean = createStudy(studyDTO, ownerUserAccount);
		StudyUserRoleBean sub = new StudyUserRoleBean();
		sub.setRole(Role.COORDINATOR);
		sub.setStudyId(studyBean.getId());
		sub.setStatus(Status.AVAILABLE);
		sub.setOwner(ownerUserAccount);
		StudyUserRoleBean surb = createRole(ownerUserAccount, sub);

		ResponseEntity<Object> response = createNewSites(request,sites, uniqueProtocolID);
		if (response.getStatusCode()==org.springframework.http.HttpStatus.BAD_REQUEST){
			return response;
		}
		
		return new ResponseEntity("SUCCESS", org.springframework.http.HttpStatus.OK);

	}

	@RequestMapping(value = "/{uniqueProtocolID}/sites", method = RequestMethod.POST)
	public ResponseEntity<Object> createNewSites(HttpServletRequest request, @RequestBody ArrayList sites, @PathVariable("uniqueProtocolID") String uniqueProtocolID) throws Exception {
		System.out.println("I'm in Create Sites ");
		String message = " field is missing from Site Json object";
		if (sites != null)
			for (Object site : sites) {

		StudyBean studyDTO = null;

		String name = (String) ((HashMap<String,Object>) site).get("BriefTitle");
		String principalInvestigator = (String) ((HashMap<String,Object>) site).get("PrincipalInvestigator");
		String uniqueSiteProtocolID = (String) ((HashMap<String,Object>) site).get("UniqueProtocolID");
		String expectedTotalEnrollment = (String) ((HashMap<String,Object>) site).get("ExpectedTotalEnrollment");
		String startDate = (String) ((HashMap<String,Object>) site).get("StartDate");
		String protocolDateVerification = (String) ((HashMap<String,Object>) site).get("ProtocolDateVerification");
		String secondaryProId = (String) ((HashMap<String,Object>) site).get("SecondaryProtocolID");

		if (uniqueProtocolID == null)
			return new ResponseEntity("UniqueProtocolID" + message, org.springframework.http.HttpStatus.BAD_REQUEST);
		if (name == null)
			return new ResponseEntity("BriefTitle" + message, org.springframework.http.HttpStatus.BAD_REQUEST);
		if (principalInvestigator == null)
			return new ResponseEntity("PrincipalInvestigator" + message, org.springframework.http.HttpStatus.BAD_REQUEST);
		if (startDate == null)
			return new ResponseEntity("StartDate" + message, org.springframework.http.HttpStatus.BAD_REQUEST);
		if (protocolDateVerification == null)
			return new ResponseEntity("ProtocolDateVerification" + message, org.springframework.http.HttpStatus.BAD_REQUEST);
		if (secondaryProId == null)
			return new ResponseEntity("SecondaryProtocolID" + message, org.springframework.http.HttpStatus.BAD_REQUEST);

		uniqueProtocolID = uniqueProtocolID.trim();
		name = name.trim();
		principalInvestigator = principalInvestigator.trim();
		startDate = startDate.trim();
		expectedTotalEnrollment = expectedTotalEnrollment.trim();
		protocolDateVerification = protocolDateVerification.trim();
		secondaryProId = secondaryProId.trim();
		uniqueSiteProtocolID = uniqueSiteProtocolID.trim();

		request.setAttribute("uniqueProId", uniqueSiteProtocolID);
		request.setAttribute("name", name);
		request.setAttribute("prinInvestigator", principalInvestigator);
		request.setAttribute("expectedTotalEnrollment", expectedTotalEnrollment);
		request.setAttribute("startDate", startDate);
		request.setAttribute("protocolDateVerification", protocolDateVerification);
		request.setAttribute("secondProId", secondaryProId);

		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		Date formattedStartDate = formatter.parse(startDate);
		Date formattedProtDateVer = formatter.parse(protocolDateVerification);

		StudyBean parentStudy = getStudyByUniqId(uniqueProtocolID);
		if (parentStudy.getParentStudyId() != 0)
			return new ResponseEntity("The Unique Protocol Id provided is not a valid Study Protocol Id", org.springframework.http.HttpStatus.BAD_REQUEST);

		UserAccountBean ownerUserAccount = getSiteOwnerAccount(request, parentStudy);
		if (ownerUserAccount == null)
			return new ResponseEntity("The Owner User Account is not Valid Account or Does not have rights to Create Sites", org.springframework.http.HttpStatus.BAD_REQUEST);

		Validator v = new Validator(request);
		addValidationToSiteFields(v);
		HashMap errors = v.validate();
		validateUniqueProId(request, errors);
		siteValidation(request, errors);

		if (!errors.isEmpty()) {
			logger.info("Validation Error: " + errors.toString());
			System.out.println("Validation Error: " + errors.toString());
			return new ResponseEntity("In Site "+errors.toString(), org.springframework.http.HttpStatus.BAD_REQUEST);
		}

		studyDTO = buildSubStudy(uniqueSiteProtocolID, name, principalInvestigator, ownerUserAccount, Integer.valueOf(expectedTotalEnrollment), parentStudy.getId(), secondaryProId,
				formattedProtDateVer, formattedStartDate);
		if (studyDTO == null)
			return new ResponseEntity(errors.toString(), org.springframework.http.HttpStatus.BAD_REQUEST); // study exists , update info

		StudyBean siteStudy = getStudyByUniqId(uniqueSiteProtocolID);
		if (siteStudy == null) {
			StudyBean studyBean = createStudy(studyDTO, ownerUserAccount);
			StudyUserRoleBean sub = new StudyUserRoleBean();
			sub.setRole(Role.COORDINATOR);
			sub.setStudyId(studyBean.getId());
			sub.setStatus(Status.AVAILABLE);
			sub.setOwner(ownerUserAccount);
			StudyUserRoleBean surb = createRole(ownerUserAccount, sub);
		}
			}
		return new ResponseEntity("SUCCESS", org.springframework.http.HttpStatus.OK);
	}

	public Boolean verfiyProtocolTypeExist(String protocolType) {
		ResourceBundle resadmin = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getAdminBundle();
		if (!protocolType.equals(resadmin.getString("interventional")) && !protocolType.equals(resadmin.getString("observational"))) {
			System.out.println("Protocol Type not supported");
			return false;
		}
		return true;
	}

	public StudyBean buildStudy(String uniqueProtocolID, String briefTitle, String principalInvestigator, String protocolType, String briefSummary, String sponsor, UserAccountBean owner,
			int expectedTotalEnrollment, Date datePlannedStart) {
		StudyBean study = new StudyBean();
		ResourceBundle resadmin = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getAdminBundle();
		if (protocolType.equals(resadmin.getString("interventional"))) {
			study.setProtocolType("interventional");
		} else if (protocolType.equals(resadmin.getString("observational"))) {
			study.setProtocolType("observational");
		}

		study.setIdentifier(uniqueProtocolID);
		study.setName(briefTitle);
		study.setPrincipalInvestigator(principalInvestigator);
		study.setSummary(briefSummary);
		study.setSponsor(sponsor);
		study.setExpectedTotalEnrollment(expectedTotalEnrollment);
		study.setDatePlannedStart(datePlannedStart);

		study.setOwner(owner);
		study.setStatus(Status.AVAILABLE);
		return study;
	}

	public StudyBean buildSubStudy(String uniqueProtocolID, String briefTitle, String principalInvestigator, UserAccountBean owner, int expectedTotalEnrollment, int parentStudyId,
			String secondaryIdentifier, Date protocolDateVerification, Date datePlannedStart) {
		StudyBean study = new StudyBean();
		ResourceBundle resadmin = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getAdminBundle();

		study.setDatePlannedStart(datePlannedStart);
		study.setProtocolDateVerification(protocolDateVerification);
		study.setSecondaryIdentifier(secondaryIdentifier);
		study.setIdentifier(uniqueProtocolID);
		study.setName(briefTitle);
		study.setPrincipalInvestigator(principalInvestigator);
		study.setExpectedTotalEnrollment(expectedTotalEnrollment);
		study.setParentStudyId(parentStudyId);
		study.setOwner(owner);
		study.setStatus(Status.AVAILABLE);
		return study;
	}

	public StudyBean createStudy(StudyBean studyBean, UserAccountBean owner) {
		sdao = new StudyDAO(dataSource);
		StudyBean sBean = (StudyBean) sdao.create(studyBean);
		return sBean;
	}

	public StudyUserRoleBean createRole(UserAccountBean ownerUserAccount, StudyUserRoleBean sub) {
		udao = new UserAccountDAO(dataSource);
		StudyUserRoleBean studyUserRoleBean = (StudyUserRoleBean) udao.createStudyUserRole(ownerUserAccount, sub);
		return studyUserRoleBean;
	}

	public StudyUserRoleBean createUserRole(UserAccountBean ownerUserAccount, StudyBean study) {
		udao = new UserAccountDAO(dataSource);
		StudyUserRoleBean surBean = udao.findRoleByUserNameAndStudyId(ownerUserAccount.getName(), study.getId());
		return surBean;
	}

	public StudyBean updateStudy(StudyBean studyBean, UserAccountBean owner) {
		sdao = new StudyDAO(dataSource);
		StudyBean sBean = (StudyBean) sdao.update(studyBean);
		return sBean;
	}

	public void addValidationToStudyFields(Validator v) {

		v.addValidation("name", Validator.NO_BLANKS);
		v.addValidation("uniqueProId", Validator.NO_BLANKS);
		v.addValidation("description", Validator.NO_BLANKS);
		v.addValidation("prinInvestigator", Validator.NO_BLANKS);
		v.addValidation("sponsor", Validator.NO_BLANKS);
		v.addValidation("startDate", Validator.NO_BLANKS);
		v.addValidation("startDate", Validator.IS_A_DATE);
	}

	public void addValidationToSiteFields(Validator v) {

		v.addValidation("name", Validator.NO_BLANKS);
		v.addValidation("uniqueProId", Validator.NO_BLANKS);
		v.addValidation("prinInvestigator", Validator.NO_BLANKS);
		v.addValidation("secondProId", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
		v.addValidation("protocolDateVerification", Validator.IS_A_DATE);
		v.addValidation("startDate", Validator.IS_A_DATE);

	}

	public void siteValidation(HttpServletRequest request, HashMap errors) {
		if (((String) request.getAttribute("name")).length() > 100) {
			Validator.addError(errors, "name", resexception.getString("maximum_lenght_name_100"));
		}
		if (((String) request.getAttribute("uniqueProId")).length() > 30) {
			Validator.addError(errors, "uniqueProId", resexception.getString("maximum_lenght_unique_protocol_30"));
		}
		if (((String) request.getAttribute("prinInvestigator")).length() > 255) {
			Validator.addError(errors, "prinInvestigator", resexception.getString("maximum_lenght_principal_investigator_255"));
		}
		if (Integer.valueOf((String) request.getAttribute("expectedTotalEnrollment")) <= 0) {
			Validator.addError(errors, "expectedTotalEnrollment", respage.getString("expected_total_enrollment_must_be_a_positive_number"));
		}

	}

	private UserAccountBean getUserAccount(String userName) {
		udao = new UserAccountDAO(dataSource);
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(userName);
		return userAccountBean;
	}

	private StudyBean getStudyByUniqId(String uniqueId) {
		sdao = new StudyDAO(dataSource);
		StudyBean studyBean = (StudyBean) sdao.findByUniqueIdentifier(uniqueId);
		return studyBean;
	}

	public void validateUniqueProId(HttpServletRequest request, HashMap errors) {
		StudyDAO studyDAO = new StudyDAO(dataSource);
		ArrayList<StudyBean> allStudies = (ArrayList<StudyBean>) studyDAO.findAll();
		for (StudyBean thisBean : allStudies) {
			if (request.getAttribute("uniqueProId").equals(thisBean.getIdentifier())) {
				ResourceBundle resexception = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getExceptionsBundle();
				Validator.addError(errors, "uniqueProId", resexception.getString("unique_protocol_id_existed"));
			}
		}

	}

	public UserAccountBean getStudyOwnerAccount(HttpServletRequest request) {
		UserAccountBean ownerUserAccount = (UserAccountBean) request.getSession().getAttribute("userBean");
		if (!ownerUserAccount.isTechAdmin() && !ownerUserAccount.isSysAdmin()) {
			logger.info("The Owner User Account is not Valid Account or Does not have Admin user type");
			System.out.println("The Owner User Account is not Valid Account or Does not have Admin user type");
			return null;
		}
		return ownerUserAccount;
	}

	public UserAccountBean getSiteOwnerAccount(HttpServletRequest request, StudyBean study) {
		UserAccountBean ownerUserAccount = (UserAccountBean) request.getSession().getAttribute("userBean");
		StudyUserRoleBean currentRole = createUserRole(ownerUserAccount, study);

		if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
			return ownerUserAccount;
		}

		return null;
	}

}
