package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.*;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

@Controller
@RequestMapping(value = "/auth/api/v1/studies")
public class StudyController {

	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;

	public static ResourceBundle resadmin, resaudit, resexception, resformat, respage, resterm, restext, resword, resworkflow;

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	UserAccountDAO udao;
	StudyDAO sdao;
	StudyEventDefinitionDAO seddao;

	/**
	 * @api {post} /pages/auth/api/v1/studies/ Create a study
	 * @apiName createNewStudy
	 * @apiPermission Authenticate using api-key. admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} uniqueProtococlId Study unique protocol ID.
	 * @apiParam {String} briefTitle Brief Title .
	 * @apiParam {String} principalInvestigator Principal Investigator Name.
	 * @apiParam {Integer} expectedTotalEnrollment Expected Total Enrollment number
	 * @apiParam {String} sponsor Sponsor name.
	 * @apiParam {String} protocolType 'Interventional' or ' Observational'
	 * @apiParam {String} status 'Available' or 'Design'
	 * @apiParam {String} briefSummary Study Summary
	 * @apiParam {Date} startDate Start date
	 * @apiParam {Array} assignUserRoles Assign Users to Roles for this Study.
	 * @apiGroup Study
	 * @apiHeader {String} api_key Users unique access-key.
	 * @apiDescription This API is to create a New Study in OC.
	 *                 All the fields are required fields and can't be left blank.
	 *                 You need to provide your Api-key to be connected.
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "briefTitle": "Study Protocol ID Name",
	 *                  "principalInvestigator": "Principal Investigator Name",
	 *                  "expectedTotalEnrollment": "10",
	 *                  "sponsor": "Sponsor Name",
	 *                  "protocolType": "Interventional",
	 *                  "status": "available",
	 *                  "assignUserRoles": [
	 *                  { "username": "usera", "role": "Data Manager" },
	 *                  { "username": "userb", "role": "Study Director" },
	 *                  { "username": "userc", "role": "Data Specialist" },
	 *                  { "username": "userd", "role": "Monitor" },
	 *                  { "username": "usere", "role": "Data Entry Person" }
	 *                  ],
	 *                  "uniqueProtocolID": "Study Protocol ID",
	 *                  "briefSummary": "Study Summary",
	 *                  "startDate": "2011-11-11"
	 *                  }
	 * @apiErrorExample {json} Error-Response:
	 *                  HTTP/1.1 400 Bad Request
	 *                  {
	 *                  "message": "VALIDATION FAILED",
	 *                  "status": "available",
	 *                  "principalInvestigator": "Principal Investigator Name",
	 *                  "expectedTotalEnrollment": "10",
	 *                  "sponsor": "Sponsor Name",
	 *                  "protocolType": "Interventional",
	 *                  "errors": [
	 *                  {"field": "UniqueProtocolId","resource": "Study Object","code": "Unique Protocol Id exist in the System"}
	 *                  ],
	 *                  "startDate": "2011-11-11",
	 *                  "assignUserRoles": [
	 *                  {"username": "usera","role": "Data Manager"},
	 *                  {"username": "userb","role": "Study Director"},
	 *                  {"username": "userc","role": "Data Specialist"}
	 *                  ],
	 *                  "uniqueProtocolID": "Study Protocol ID",
	 *                  "briefTitle": "Study Protocol ID",
	 *                  "briefSummary": "Study Summary",
	 *                  "studyOid": null
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "message": "SUCCESS",
	 *                    "uniqueProtocolID": "Study Protocol ID",
	 *                    "studyOid": "S_STUDYPRO",
	 *                    }
	 */


	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<Object> createNewStudy(HttpServletRequest request, @RequestBody HashMap<String, Object> map) throws Exception {
		ArrayList<ErrorObject> errorObjects = new ArrayList();
		StudyBean studyBean = null;
		System.out.println("I'm in Create Study");
		ResponseEntity<Object> response = null;

		String validation_failed_message = "VALIDATION FAILED";
		String validation_passed_message = "SUCCESS";

		String uniqueProtocolID = (String) map.get("uniqueProtocolID");
		String name = (String) map.get("briefTitle");
		String principalInvestigator = (String) map.get("principalInvestigator");
		String briefSummary = (String) map.get("briefSummary");
		String sponsor = (String) map.get("sponsor");
		String protocolType = (String) map.get("protocolType");
		String startDate = (String) map.get("startDate");
		String expectedTotalEnrollment = (String) map.get("expectedTotalEnrollment");
		String status = (String) map.get("status");
		ArrayList<UserRole> assignUserRoles = (ArrayList<UserRole>) map.get("assignUserRoles");

		ArrayList<UserRole> userList = new ArrayList<>();

		if (assignUserRoles != null) {
			for (Object userRole : assignUserRoles) {
				UserRole uRole = new UserRole();
				uRole.setUsername((String) ((HashMap<String, Object>) userRole).get("username"));
				uRole.setRole((String) ((HashMap<String, Object>) userRole).get("role"));
				udao = new UserAccountDAO(dataSource);
				UserAccountBean assignedUserBean = (UserAccountBean) udao.findByUserName(uRole.getUsername());
				if (assignedUserBean == null || !assignedUserBean.isActive()) {
					ErrorObject errorOBject = createErrorObject("Study Object", "The Assigned Username " + uRole.getUsername() + " is not a Valid User", "Assigned User");
					errorObjects.add(errorOBject);
				}

				ResourceBundle resterm = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getTermsBundle();

				if (getStudyRole(uRole.getRole(), resterm) == null) {
					ErrorObject errorOBject = createErrorObject("Study Object", "Assigned Role for " + uRole.getUsername() + " is not a Valid Study Role", "Assigned Role");
					errorObjects.add(errorOBject);
				}
				userList.add(uRole);
			}
		}

		StudyDTO studyDTO = buildStudyDTO(uniqueProtocolID, name, briefSummary, principalInvestigator, sponsor, expectedTotalEnrollment, protocolType, status, startDate, userList);

		if (uniqueProtocolID == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "UniqueProtocolID");
			errorObjects.add(errorOBject);
		} else {
			uniqueProtocolID = uniqueProtocolID.trim();
		}
		if (name == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "BriefTitle");
			errorObjects.add(errorOBject);
		} else {
			name = name.trim();
		}
		if (principalInvestigator == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "PrincipalInvestigator");
			errorObjects.add(errorOBject);
		} else {
			principalInvestigator = principalInvestigator.trim();
		}
		if (briefSummary == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "BriefSummary");
			errorObjects.add(errorOBject);
		} else {
			briefSummary = briefSummary.trim();
		}
		if (sponsor == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "Sponsor");
			errorObjects.add(errorOBject);
		} else {
			sponsor = sponsor.trim();
		}
		if (protocolType == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "ProtocolType");
			errorObjects.add(errorOBject);
		} else {
			protocolType = protocolType.trim();
		}
		if (startDate == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "StartDate");
			errorObjects.add(errorOBject);
		} else {
			startDate = startDate.trim();
		}
		if (expectedTotalEnrollment == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "ExpectedTotalEnrollment");
			errorObjects.add(errorOBject);
		} else {
			expectedTotalEnrollment = expectedTotalEnrollment.trim();
		}
		if (status == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "Status");
			errorObjects.add(errorOBject);
		} else {
			status = status.trim();
		}

		if (assignUserRoles == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "AssignUserRoles");
			errorObjects.add(errorOBject);
		}

		if (status != null && !status.equalsIgnoreCase("available") && !status.equalsIgnoreCase("design") && !status.equals("")) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Status Field Should have 'Available' or 'Design' Status only, If left empty , will default to 'Design' Mode", "Status");
			errorObjects.add(errorOBject);
		}

		request.setAttribute("uniqueProId", uniqueProtocolID);
		request.setAttribute("name", name); // Brief Title
		request.setAttribute("prinInvestigator", principalInvestigator);
		request.setAttribute("description", briefSummary);
		request.setAttribute("sponsor", sponsor);
		request.setAttribute("startDate", startDate);
		request.setAttribute("expectedTotalEnrollment", expectedTotalEnrollment);
		request.setAttribute("status", status);

		String format = "yyyy-MM-dd";
		SimpleDateFormat formatter = null;
		Date formattedDate = null;
		if (startDate != "" && startDate != null) {
			try {
				formatter = new SimpleDateFormat(format);
				formattedDate = formatter.parse(startDate);
			} catch (ParseException e) {
				ErrorObject errorOBject = createErrorObject("Study Object", "The StartDate format is not a valid 'yyyy-MM-dd' format", "StartDate");
				errorObjects.add(errorOBject);
			}
			if (formattedDate != null) {
				if (!startDate.equals(formatter.format(formattedDate))) {
					ErrorObject errorOBject = createErrorObject("Study Object", "The StartDate format is not a valid 'yyyy-MM-dd' format", "StartDate");
					errorObjects.add(errorOBject);
				}
			}
		}

		UserAccountBean ownerUserAccount = getStudyOwnerAccount(request);
		if (ownerUserAccount == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "The Owner User Account is not Valid Account or Does not have Admin user type", "Owner Account");
			errorObjects.add(errorOBject);

		}

		Validator v0 = new Validator(request);
		v0.addValidation("name", Validator.NO_BLANKS);
		HashMap vError0 = v0.validate();
		if (!vError0.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Study Object", "This field cannot be blank.", "BriefTitle");
			errorObjects.add(errorOBject);
		}

		Validator v1 = new Validator(request);
		v1.addValidation("uniqueProId", Validator.NO_BLANKS);
		HashMap vError1 = v1.validate();
		if (!vError1.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Study Object", "This field cannot be blank.", "UniqueProtocolId");
			errorObjects.add(errorOBject);
		}
		Validator v2 = new Validator(request);
		v2.addValidation("description", Validator.NO_BLANKS);
		HashMap vError2 = v2.validate();
		if (!vError2.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Study Object", "This field cannot be blank.", "BriefSummary");
			errorObjects.add(errorOBject);
		}
		Validator v3 = new Validator(request);
		v3.addValidation("prinInvestigator", Validator.NO_BLANKS);
		HashMap vError3 = v3.validate();
		if (!vError3.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Study Object", "This field cannot be blank.", "PrincipleInvestigator");
			errorObjects.add(errorOBject);
		}
		Validator v4 = new Validator(request);
		v4.addValidation("sponsor", Validator.NO_BLANKS);
		HashMap vError4 = v4.validate();
		if (!vError4.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Study Object", "This field cannot be blank.", "Sponsor");
			errorObjects.add(errorOBject);
		}
		Validator v5 = new Validator(request);
		v5.addValidation("startDate", Validator.NO_BLANKS);
		HashMap vError5 = v5.validate();
		if (!vError5.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Study Object", "This field cannot be blank.", "StartDate");
			errorObjects.add(errorOBject);
		}

		Validator v6 = new Validator(request);
		HashMap vError6 = v6.validate();
		if (uniqueProtocolID != null)
			validateUniqueProId(request, vError6);
		if (!vError6.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Unique Protocol Id exist in the System", "UniqueProtocolId");
			errorObjects.add(errorOBject);
		}

		Validator v7 = new Validator(request);
		v7.addValidation("expectedTotalEnrollment", Validator.NO_BLANKS);
		HashMap vError7 = v7.validate();
		if (!vError7.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Study Object", "This field cannot be blank.", "ExpectedTotalEnrollment");
			errorObjects.add(errorOBject);
		}

		if (protocolType != null && !verifyProtocolTypeExist(protocolType)) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Protocol Type is not Valid", "ProtocolType");
			errorObjects.add(errorOBject);
		}

		studyDTO.setErrors(errorObjects);

		if (errorObjects != null && errorObjects.size() != 0) {
			studyDTO.setMessage(validation_failed_message);
			response = new ResponseEntity(studyDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
		} else {
			studyBean = buildStudyBean(uniqueProtocolID, name, briefSummary, principalInvestigator, sponsor, Integer.valueOf(expectedTotalEnrollment), protocolType, status, formattedDate,
					ownerUserAccount);

			StudyBean sBean = createStudy(studyBean, ownerUserAccount);
			studyDTO.setStudyOid(sBean.getOid());
			studyDTO.setMessage(validation_passed_message);

			StudyUserRoleBean sub = new StudyUserRoleBean();
			sub.setRole(Role.COORDINATOR);
			sub.setStudyId(sBean.getId());
			sub.setStatus(Status.AVAILABLE);
			sub.setOwner(ownerUserAccount);
			StudyUserRoleBean surb = createRole(ownerUserAccount, sub);

			ResourceBundle resterm = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getTermsBundle();

			for (UserRole userRole : userList) {
				sub = new StudyUserRoleBean();
				sub.setRole(getStudyRole(userRole.getRole(), resterm));
				sub.setStudyId(sBean.getId());
				sub.setStatus(Status.AVAILABLE);
				sub.setOwner(ownerUserAccount);
				udao = new UserAccountDAO(dataSource);
				UserAccountBean assignedUserBean = (UserAccountBean) udao.findByUserName(userRole.getUsername());
				surb = createRole(assignedUserBean, sub);
			}
            ResponseSuccessStudyDTO responseSuccess = new ResponseSuccessStudyDTO();
            responseSuccess.setMessage(studyDTO.getMessage());
            responseSuccess.setStudyOid(studyDTO.getStudyOid());
            responseSuccess.setUniqueProtocolID(studyDTO.getUniqueProtocolID());

			response = new ResponseEntity(responseSuccess, org.springframework.http.HttpStatus.OK);
		}
		return response;

	}

	/**
	 * @api {post} /pages/auth/api/v1/studies/:uniqueProtocolId/sites Create a site
	 * @apiName createNewSite
	 * @apiPermission Authenticate using api-key. admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} uniqueProtococlId Study unique protocol ID.
	 * @apiParam {String} briefTitle Brief Title .
	 * @apiParam {String} principalInvestigator Principal Investigator Name.
	 * @apiParam {Integer} expectedTotalEnrollment Expected Total Enrollment number
	 * @apiParam {String} secondaryProtocolID Site Secondary Protocol Id  (Optional)
	 * @apiParam {Date} startDate Start date
	 * @apiParam {Date} protocolDateVerification protocol Verification date
	 * @apiParam {Array} assignUserRoles Assign Users to Roles for this Study.
	 * @apiGroup Site
	 * @apiHeader {String} api_key Users unique access-key.
	 * @apiDescription Create a Site
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "briefTitle": "Site Protocol ID Name",
	 *                  "principalInvestigator": "Principal Investigator Name",
	 *                  "expectedTotalEnrollment": "10",
	 *                  "assignUserRoles": [
	 *                  { "username" : "userc", "role" : "Investigator"},
	 *                  { "username" : "userb", "role" : "Clinical Research Coordinator"},
	 *                  { "username" : "dm_normal", "role" : "Monitor"},
	 *                  { "username" : "sd_root", "role" : "Data Entry Person"}
	 *                  ],
	 *                  "uniqueProtocolID": "Site Protocol ID",
	 *                  "startDate": "2011-11-11",
	 *                  "secondaryProtocolID" : "Secondary Protocol ID 1" ,
	 *                  "protocolDateVerification" : "2011-10-14"
	 *                  }
	 *
	 * @apiErrorExample {json} Error-Response:
	 *                  HTTP/1.1 400 Bad Request
	 *                  {
	 *                  "message": "VALIDATION FAILED",
	 *                  "protocolDateVerification": "2011-10-14",
	 *                  "principalInvestigator": "Principal Investigator Name",
	 *                  "expectedTotalEnrollment": "10",
	 *                  "errors": [
	 *                  { "field": "UniqueProtocolId", "resource": "Site Object","code": "Unique Protocol Id exist in the System" }
	 *                  ],
	 *                  "secondaryProId": "Secondary Protocol ID 1",
	 *                  "siteOid": null,
	 *                  "briefTitle": "Site Protocol ID Name",
	 *                  "assignUserRoles": [
	 *                  { "role": "Investigator", "username": "userc"},
	 *                  { "role": "Clinical Research Coordinator", "username": "userb"},
	 *                  { "role": "Monitor","username": "dm_normal"},
	 *                  { "role": "Data Entry Person","username": "sd_root"}
	 *                  ],
	 *                  "uniqueSiteProtocolID": "Site Protocol ID",
	 *                  "startDate": "2011-11-11"
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "message": "SUCCESS",
	 *                    "siteOid": "S_SITEPROT",
	 *                    "uniqueSiteProtocolID": "Site Protocol IDqq"
	 *                    }
	 */

	@RequestMapping(value = "/{uniqueProtocolID}/sites", method = RequestMethod.POST)
	public ResponseEntity<Object> createNewSites(HttpServletRequest request, @RequestBody HashMap<String, Object> map, @PathVariable("uniqueProtocolID") String uniqueProtocolID) throws Exception {
		System.out.println("I'm in Create Sites ");
		ArrayList<ErrorObject> errorObjects = new ArrayList();
		StudyBean siteBean = null;
		ResponseEntity<Object> response = null;

		String validation_failed_message = "VALIDATION FAILED";
		String validation_passed_message = "SUCCESS";

		String name = (String) map.get("briefTitle");
		String principalInvestigator = (String) map.get("principalInvestigator");
		String uniqueSiteProtocolID = (String) map.get("uniqueProtocolID");
		String expectedTotalEnrollment = (String) map.get("expectedTotalEnrollment");
		String startDate = (String) map.get("startDate");
		String protocolDateVerification = (String) map.get("protocolDateVerification");
		String secondaryProId = (String) map.get("secondaryProtocolID");
		ArrayList<UserRole> assignUserRoles = (ArrayList<UserRole>) map.get("assignUserRoles");

		ArrayList<UserRole> userList = new ArrayList<>();
		if (assignUserRoles != null) {
			for (Object userRole : assignUserRoles) {
				UserRole uRole = new UserRole();
				uRole.setUsername((String) ((HashMap<String, Object>) userRole).get("username"));
				uRole.setRole((String) ((HashMap<String, Object>) userRole).get("role"));
				udao = new UserAccountDAO(dataSource);
				UserAccountBean assignedUserBean = (UserAccountBean) udao.findByUserName(uRole.getUsername());
				if (assignedUserBean == null || !assignedUserBean.isActive()) {
					ErrorObject errorOBject = createErrorObject("Study Object", "The Assigned Username " + uRole.getUsername() + " is not a Valid User", "Assigned User");
					errorObjects.add(errorOBject);
				}

				ResourceBundle resterm = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getTermsBundle();

				if (getSiteRole(uRole.getRole(), resterm) == null) {
					ErrorObject errorOBject = createErrorObject("Study Object", "Assigned Role for " + uRole.getUsername() + " is not a Valid Site Role", "Assigned Role");
					errorObjects.add(errorOBject);
				}
				userList.add(uRole);
			}
		}

		SiteDTO siteDTO = buildSiteDTO(uniqueSiteProtocolID, name, principalInvestigator, expectedTotalEnrollment, startDate, protocolDateVerification, secondaryProId, userList);

		if (uniqueSiteProtocolID == null) {
			ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "UniqueProtocolID");
			errorObjects.add(errorOBject);
		} else {
			uniqueSiteProtocolID = uniqueSiteProtocolID.trim();
		}
		if (name == null) {
			ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "BriefTitle");
			errorObjects.add(errorOBject);
		} else {
			name = name.trim();
		}
		if (principalInvestigator == null) {
			ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "PrincipalInvestigator");
			errorObjects.add(errorOBject);
		} else {
			principalInvestigator = principalInvestigator.trim();
		}
		if (startDate == null) {
			ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "StartDate");
			errorObjects.add(errorOBject);
		} else {
			startDate = startDate.trim();
		}
		if (protocolDateVerification == null) {
			ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "ProtocolDateVerification");
			errorObjects.add(errorOBject);
		} else {
			protocolDateVerification = protocolDateVerification.trim();
		}
		if (expectedTotalEnrollment == null) {
			ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "ExpectedTotalEnrollment");
			errorObjects.add(errorOBject);
		} else {
			expectedTotalEnrollment = expectedTotalEnrollment.trim();
		}
		if (secondaryProId != null) {
			secondaryProId = secondaryProId.trim();
		}

		if (assignUserRoles == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "AssignUserRoles");
			errorObjects.add(errorOBject);
		}

		request.setAttribute("uniqueProId", uniqueSiteProtocolID);
		request.setAttribute("name", name);
		request.setAttribute("prinInvestigator", principalInvestigator);
		request.setAttribute("expectedTotalEnrollment", expectedTotalEnrollment);
		request.setAttribute("startDate", startDate);
		request.setAttribute("protocolDateVerification", protocolDateVerification);
		request.setAttribute("secondProId", secondaryProId);

		String format = "yyyy-MM-dd";
		SimpleDateFormat formatter = null;
		Date formattedStartDate = null;
		Date formattedProtocolDate = null;

		if (startDate != "" && startDate != null) {
			try {
				formatter = new SimpleDateFormat(format);
				formattedStartDate = formatter.parse(startDate);
			} catch (ParseException e) {
				ErrorObject errorOBject = createErrorObject("Site Object", "The StartDate format is not a valid 'yyyy-MM-dd' format", "StartDate");
				errorObjects.add(errorOBject);
			}
			if (formattedStartDate != null) {
				if (!startDate.equals(formatter.format(formattedStartDate))) {
					ErrorObject errorOBject = createErrorObject("Site Object", "The StartDate format is not a valid 'yyyy-MM-dd' format", "StartDate");
					errorObjects.add(errorOBject);
				}
			}
		}

		if (protocolDateVerification != "" && protocolDateVerification != null) {
			try {
				formatter = new SimpleDateFormat(format);
				formattedProtocolDate = formatter.parse(protocolDateVerification);
			} catch (ParseException e) {
				ErrorObject errorOBject = createErrorObject("Site Object", "The Protocol Verification Date format is not a valid 'yyyy-MM-dd' format", "ProtocolDateVerification");
				errorObjects.add(errorOBject);
			}
			if (formattedProtocolDate != null) {
				if (!protocolDateVerification.equals(formatter.format(formattedProtocolDate))) {
					ErrorObject errorOBject = createErrorObject("Site Object", "The Protocol Verification Date format is not a valid 'yyyy-MM-dd' format", "ProtocolDateVerification");
					errorObjects.add(errorOBject);
				}
			}
		}

		StudyBean parentStudy = getStudyByUniqId(uniqueProtocolID);
		if (parentStudy == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "The Study Protocol Id provided in the URL is not a valid Protocol Id", "Unique Study Protocol Id");
			errorObjects.add(errorOBject);
		} else if (parentStudy.getParentStudyId() != 0) {
			ErrorObject errorOBject = createErrorObject("Study Object", "The Study Protocol Id provided in the URL is not a valid Study Protocol Id", "Unique Study Protocol Id");
			errorObjects.add(errorOBject);
		}

		UserAccountBean ownerUserAccount = null;

		if (parentStudy != null) {
			ownerUserAccount = getSiteOwnerAccount(request, parentStudy);
			if (ownerUserAccount == null) {
				ErrorObject errorOBject = createErrorObject("Site Object", "The Owner User Account is not Valid Account or Does not have rights to Create Sites", "Owner Account");
				errorObjects.add(errorOBject);
			}
		}

		Validator v1 = new Validator(request);
		v1.addValidation("uniqueProId", Validator.NO_BLANKS);
		HashMap vError1 = v1.validate();
		if (!vError1.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Site Object", "This field cannot be blank.", "UniqueProtocolId");
			errorObjects.add(errorOBject);
		}
		Validator v2 = new Validator(request);
		v2.addValidation("name", Validator.NO_BLANKS);
		HashMap vError2 = v2.validate();
		if (!vError2.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Site Object", "This field cannot be blank.", "BriefTitle");
			errorObjects.add(errorOBject);
		}
		Validator v3 = new Validator(request);
		v3.addValidation("prinInvestigator", Validator.NO_BLANKS);
		HashMap vError3 = v3.validate();
		if (!vError3.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Site Object", "This field cannot be blank.", "PrincipleInvestigator");
			errorObjects.add(errorOBject);
		}

		Validator v6 = new Validator(request);
		HashMap vError6 = v6.validate();
		if (uniqueProtocolID != null)
			validateUniqueProId(request, vError6);
		if (!vError6.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Site Object", "Unique Protocol Id exist in the System", "UniqueProtocolId");
			errorObjects.add(errorOBject);
		}

		Validator v7 = new Validator(request);
		v7.addValidation("expectedTotalEnrollment", Validator.NO_BLANKS);
		HashMap vError7 = v7.validate();
		if (!vError7.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Site Object", "This field cannot be blank.", "ExpectedTotalEnrollment");
			errorObjects.add(errorOBject);
		}

		if (request.getAttribute("name") != null && ((String) request.getAttribute("name")).length() > 100) {
			ErrorObject errorOBject = createErrorObject("Site Object", "BriefTitle Length exceeds the max length 100", "BriefTitle");
			errorObjects.add(errorOBject);
		}
		if (request.getAttribute("uniqueProId") != null && ((String) request.getAttribute("uniqueProId")).length() > 30) {
			ErrorObject errorOBject = createErrorObject("Site Object", "UniqueProtocolId Length exceeds the max length 30", "UniqueProtocolId");
			errorObjects.add(errorOBject);
		}
		if (request.getAttribute("prinInvestigator") != null && ((String) request.getAttribute("prinInvestigator")).length() > 255) {
			ErrorObject errorOBject = createErrorObject("Site Object", "PrincipleInvestigator Length exceeds the max length 255", "PrincipleInvestigator");
			errorObjects.add(errorOBject);
		}
		if (request.getAttribute("expectedTotalEnrollment") != null && Integer.valueOf((String) request.getAttribute("expectedTotalEnrollment")) <= 0) {
			ErrorObject errorOBject = createErrorObject("Site Object", "ExpectedTotalEnrollment Length can't be negative", "ExpectedTotalEnrollment");
			errorObjects.add(errorOBject);
		}

		siteDTO.setErrors(errorObjects);

		if (errorObjects != null && errorObjects.size() != 0) {
			siteDTO.setMessage(validation_failed_message);
			response = new ResponseEntity(siteDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
		} else {
			siteBean = buildSiteBean(uniqueSiteProtocolID, name, principalInvestigator, Integer.valueOf(expectedTotalEnrollment), formattedStartDate, formattedProtocolDate, secondaryProId,
					ownerUserAccount, parentStudy.getId());

			StudyBean sBean = createStudy(siteBean, ownerUserAccount);
			siteDTO.setSiteOid(sBean.getOid());
			siteDTO.setMessage(validation_passed_message);
			ResourceBundle resterm = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getTermsBundle();
			StudyUserRoleBean sub = null;
			for (UserRole userRole : userList) {
				sub = new StudyUserRoleBean();
				sub.setRole(getSiteRole(userRole.getRole(), resterm));
				sub.setStudyId(sBean.getId());
				sub.setStatus(Status.AVAILABLE);
				sub.setOwner(ownerUserAccount);
				udao = new UserAccountDAO(dataSource);
				UserAccountBean assignedUserBean = (UserAccountBean) udao.findByUserName(userRole.getUsername());
				StudyUserRoleBean surb = createRole(assignedUserBean, sub);
			}
            ResponseSuccessSiteDTO responseSuccess = new ResponseSuccessSiteDTO();
            responseSuccess.setMessage(siteDTO.getMessage());
            responseSuccess.setSiteOid(siteDTO.getSiteOid());
            responseSuccess.setUniqueSiteProtocolID(siteDTO.getUniqueSiteProtocolID());

			response = new ResponseEntity(responseSuccess, org.springframework.http.HttpStatus.OK);

		}
		return response;

	}

	/**
	 * @api {post} /pages/auth/api/v1/studies/:uniqueProtocolId/eventdefinitions Create a study event
	 * @apiName createEventDefinition
	 * @apiPermission Authenticate using api-key. admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} uniqueProtocolId Study unique protocol ID.
	 * @apiParam {String} name Event Name.
	 * @apiParam {String} description Event Description.
	 * @apiParam {String} category Category Name.
	 * @apiParam {Boolean} repeating 'True' or 'False'.
	 * @apiParam {String} type 'Scheduled' , 'UnScheduled' or 'Common'.
	 * @apiGroup Study Event
	 * @apiHeader {String} api_key Users unique access-key.
	 * @apiDescription Creates a study event definition.
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "name": "Event Name",
	 *                  "description": "Event Description",
	 *                  "category": "Category Name",
	 *                  "repeating": "true",
	 *                  "type":"Scheduled"
	 *                  }
	 * @apiErrorExample {json} Error-Response:
	 *                  HTTP/1.1 400 Bad Request
	 *                  {
	 *                  "name": "Event Name",
	 *                  "message": "VALIDATION FAILED",
	 *                  "type": "",
	 *                  "errors": [
	 *                  {"field": "Type","resource": "Event Definition Object","code": "Type Field should be Either 'Scheduled' , 'UnScheduled' or 'Common'"},
	 *                  {"field": "Type","resource": "Event Definition Object","code": "This field cannot be blank."}
	 *                  ],
	 *                  "category": "Category Name",
	 *                  "description": "Event Description",
	 *                  "eventDefOid": null,
	 *                  "repeating": "true"
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "message": "SUCCESS",
	 *                    "name": "Event Name",
	 *                    "eventDefOid": "SE_EVENTNAME"
	 *                    }
	 */
	@RequestMapping(value = "/{uniqueProtocolID}/eventdefinitions", method = RequestMethod.POST)
	public ResponseEntity<Object> createEventDefinition(HttpServletRequest request, @RequestBody HashMap<String, Object> map, @PathVariable("uniqueProtocolID") String uniqueProtocolID)
			throws Exception {
		System.out.println("I'm in Create Event Definition ");
		ArrayList<ErrorObject> errorObjects = new ArrayList();
		StudyEventDefinitionBean eventBean = null;
		ResponseEntity<Object> response = null;

		String validation_failed_message = "VALIDATION FAILED";
		String validation_passed_message = "SUCCESS";

		String name = (String) map.get("name");
		String description = (String) map.get("description");
		String category = (String) map.get("category");
		String type = (String) map.get("type");
		String repeating = (String) map.get("repeating");

		EventDefinitionDTO eventDefinitionDTO = buildEventDefnDTO(name, description, category, repeating, type);

		if (name == null) {
			ErrorObject errorOBject = createErrorObject("Event Definition Object", "Missing Field", "Name");
			errorObjects.add(errorOBject);
		} else {
			name = name.trim();
		}
		if (description == null) {
			ErrorObject errorOBject = createErrorObject("Event Definition Object", "Missing Field", "Description");
			errorObjects.add(errorOBject);
		} else {
			description = description.trim();
		}
		if (category == null) {
			ErrorObject errorOBject = createErrorObject("Event Definition Object", "Missing Field", "Category");
			errorObjects.add(errorOBject);
		} else {
			category = category.trim();
		}
		if (type == null) {
			ErrorObject errorOBject = createErrorObject("Event Definition Object", "Missing Field", "Type");
			errorObjects.add(errorOBject);
		} else {
			type = type.trim();
		}
		if (repeating == null) {
			ErrorObject errorOBject = createErrorObject("Event Definition Object", "Missing Field", "Repeating");
			errorObjects.add(errorOBject);
		} else {
			repeating = repeating.trim();
		}
		if (repeating != null) {
			if (!repeating.equalsIgnoreCase("true") && !repeating.equalsIgnoreCase("false")) {
				ErrorObject errorOBject = createErrorObject("Event Definition Object", "Repeating Field should be Either 'True' or 'False'", "Repeating");
				errorObjects.add(errorOBject);
			}
		}

		if (type != null) {
			if (!type.equalsIgnoreCase("scheduled") && !type.equalsIgnoreCase("unscheduled") && !type.equalsIgnoreCase("common")) {
				ErrorObject errorOBject = createErrorObject("Event Definition Object", "Type Field should be Either 'Scheduled' , 'UnScheduled' or 'Common'", "Type");
				errorObjects.add(errorOBject);
			}
		}

		request.setAttribute("name", name);
		request.setAttribute("description", description);
		request.setAttribute("category", category);
		request.setAttribute("type", type);
		request.setAttribute("repeating", repeating);

		StudyBean parentStudy = getStudyByUniqId(uniqueProtocolID);
		if (parentStudy == null) {
			ErrorObject errorOBject = createErrorObject("Event Definition Object", "The Study Protocol Id provided in the URL is not a valid Protocol Id", "Unique Study Protocol Id");
			errorObjects.add(errorOBject);
		} else if (parentStudy.getParentStudyId() != 0) {
			ErrorObject errorOBject = createErrorObject("Event Definition Object", "The Study Protocol Id provided in the URL is not a valid Study Protocol Id", "Unique Study Protocol Id");
			errorObjects.add(errorOBject);
		}

		UserAccountBean ownerUserAccount = getStudyOwnerAccount(request);
		if (ownerUserAccount == null) {
			ErrorObject errorOBject = createErrorObject("Study Object", "The Owner User Account is not Valid Account or Does not have Admin user type", "Owner Account");
			errorObjects.add(errorOBject);
		}

		Validator v1 = new Validator(request);
		v1.addValidation("name", Validator.NO_BLANKS);
		HashMap vError1 = v1.validate();
		if (!vError1.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Event Definition Object", "This field cannot be blank.", "Name");
			errorObjects.add(errorOBject);
		}

		if (name != null) {
			Validator v2 = new Validator(request);
			v2.addValidation("name", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
			HashMap vError2 = v2.validate();
			if (!vError2.isEmpty()) {
				ErrorObject errorOBject = createErrorObject("Event Definition Object", "The Length Should not exceed 2000.", "Name");
				errorObjects.add(errorOBject);
			}
		}
		if (description != null) {
			Validator v3 = new Validator(request);
			v3.addValidation("description", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
			HashMap vError3 = v3.validate();
			if (!vError3.isEmpty()) {
				ErrorObject errorOBject = createErrorObject("Event Definition Object", "The Length Should not exceed 2000.", "Description");
				errorObjects.add(errorOBject);
			}
		}
		if (category != null) {
			Validator v4 = new Validator(request);
			v4.addValidation("category", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
			HashMap vError4 = v4.validate();
			if (!vError4.isEmpty()) {
				ErrorObject errorOBject = createErrorObject("Event Definition Object", "The Length Should not exceed 2000.", "Category");
				errorObjects.add(errorOBject);
			}
		}
		Validator v5 = new Validator(request);
		v5.addValidation("repeating", Validator.NO_BLANKS);
		HashMap vError5 = v5.validate();
		if (!vError5.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Event Definition Object", "This field cannot be blank.", "Repeating");
			errorObjects.add(errorOBject);
		}

		Validator v6 = new Validator(request);
		v6.addValidation("type", Validator.NO_BLANKS);
		HashMap vError6 = v6.validate();
		if (!vError6.isEmpty()) {
			ErrorObject errorOBject = createErrorObject("Event Definition Object", "This field cannot be blank.", "Type");
			errorObjects.add(errorOBject);
		}

		eventDefinitionDTO.setErrors(errorObjects);

		if (errorObjects != null && errorObjects.size() != 0) {
			eventDefinitionDTO.setMessage(validation_failed_message);
			response = new ResponseEntity(eventDefinitionDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
		} else {
			eventBean = buildEventDefBean(name, description, category, type, repeating, ownerUserAccount, parentStudy);

			StudyEventDefinitionBean sedBean = createEventDefn(eventBean, ownerUserAccount);
			eventDefinitionDTO.setEventDefOid(sedBean.getOid());
			eventDefinitionDTO.setMessage(validation_passed_message);
		}
        ResponseSuccessEventDefDTO responseSuccess = new ResponseSuccessEventDefDTO();
        responseSuccess.setMessage(eventDefinitionDTO.getMessage());
        responseSuccess.setEventDefOid(eventDefinitionDTO.getEventDefOid());
        responseSuccess.setName(eventDefinitionDTO.getName());


		response = new ResponseEntity(responseSuccess, org.springframework.http.HttpStatus.OK);
		return response;

	}

	public Boolean verifyProtocolTypeExist(String protocolType) {
		ResourceBundle resadmin = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getAdminBundle();
		if (!protocolType.equals(resadmin.getString("interventional")) && !protocolType.equals(resadmin.getString("observational"))) {
			System.out.println("Protocol Type not supported");
			return false;
		}
		return true;
	}

	public StudyEventDefinitionBean buildEventDefBean(String name, String description, String category, String type, String repeating, UserAccountBean owner, StudyBean parentStudy) {

		StudyEventDefinitionBean sed = new StudyEventDefinitionBean();
        seddao = new StudyEventDefinitionDAO(dataSource);
        ArrayList defs = seddao.findAllByStudy(parentStudy);
        if (defs == null || defs.isEmpty()) {
            sed.setOrdinal(1);
        } else {
            int lastCount = defs.size() - 1;
            StudyEventDefinitionBean last = (StudyEventDefinitionBean) defs.get(lastCount);
            sed.setOrdinal(last.getOrdinal() + 1);
        }

		sed.setName(name);
		sed.setCategory(category);
		sed.setType(type.toLowerCase());
		sed.setDescription(description);
		sed.setRepeating(Boolean.valueOf(repeating));
		sed.setStudyId(parentStudy.getId());
		sed.setOwner(owner);
		sed.setStatus(Status.AVAILABLE);
		return sed;
	}

	public StudyBean buildSiteBean(String uniqueSiteProtocolId, String name, String principalInvestigator, int expectedTotalEnrollment, Date startDate, Date protocolDateVerification,
			String secondaryProId, UserAccountBean owner, int parentStudyId) {

		StudyBean study = new StudyBean();
		ResourceBundle resadmin = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getAdminBundle();

		study.setDatePlannedStart(startDate);
		study.setProtocolDateVerification(protocolDateVerification);
		study.setSecondaryIdentifier(secondaryProId);
		study.setIdentifier(uniqueSiteProtocolId);
		study.setName(name);
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
		sBean = (StudyBean) sdao.findByPK(sBean.getId());
		return sBean;
	}

	public StudyEventDefinitionBean createEventDefn(StudyEventDefinitionBean sedBean, UserAccountBean owner) {
		seddao = new StudyEventDefinitionDAO(dataSource);
		StudyEventDefinitionBean sdBean = (StudyEventDefinitionBean) seddao.create(sedBean);
		sdBean = (StudyEventDefinitionBean) seddao.findByPK(sdBean.getId());
		return sdBean;
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

	public void addValidationToDefinitionFields(Validator v) {

		v.addValidation("name", Validator.NO_BLANKS);
		v.addValidation("name", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
		v.addValidation("description", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
		v.addValidation("category", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);

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
			if (request.getAttribute("uniqueProId") != null && request.getAttribute("uniqueProId").equals(thisBean.getIdentifier())) {
				ResourceBundle resexception = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getExceptionsBundle();
				Validator.addError(errors, "uniqueProId", resexception.getString("unique_protocol_id_existed"));
				break;
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

	public StudyDTO buildStudyDTO(String uniqueProtocolID, String name, String briefSummary, String principalInvestigator, String sponsor, String expectedTotalEnrollment, String protocolType,
			String status, String startDate, ArrayList<UserRole> userList) {
		if (status != null) {
			if (status.equals(""))
				status = "design";
		}

		StudyDTO studyDTO = new StudyDTO();
		studyDTO.setUniqueProtocolID(uniqueProtocolID);
		studyDTO.setBriefTitle(name);
		studyDTO.setPrincipalInvestigator(principalInvestigator);
		studyDTO.setBriefSummary(briefSummary);
		studyDTO.setSponsor(sponsor);
		studyDTO.setProtocolType(protocolType);
		studyDTO.setStatus(status);
		studyDTO.setExpectedTotalEnrollment(expectedTotalEnrollment);
		studyDTO.setStartDate(startDate);
		studyDTO.setAssignUserRoles(userList);
		return studyDTO;
	}

	public SiteDTO buildSiteDTO(String uniqueSiteProtocolID, String name, String principalInvestigator, String expectedTotalEnrollment, String startDate, String protocolDateVerification,
			String secondaryProId, ArrayList<UserRole> userList) {

		SiteDTO siteDTO = new SiteDTO();
		siteDTO.setUniqueSiteProtocolID(uniqueSiteProtocolID);
		siteDTO.setBriefTitle(name);
		siteDTO.setPrincipalInvestigator(principalInvestigator);
		siteDTO.setExpectedTotalEnrollment(expectedTotalEnrollment);
		siteDTO.setStartDate(startDate);
		siteDTO.setSecondaryProId(secondaryProId);
		siteDTO.setProtocolDateVerification(protocolDateVerification);
		siteDTO.setAssignUserRoles(userList);
		return siteDTO;
	}

	public EventDefinitionDTO buildEventDefnDTO(String name, String description, String category, String repeating, String type) {
		EventDefinitionDTO eventDefinitionDTO = new EventDefinitionDTO();
		eventDefinitionDTO.setName(name);
		eventDefinitionDTO.setDescription(description);
		eventDefinitionDTO.setCategory(category);
		eventDefinitionDTO.setType(type);
		eventDefinitionDTO.setRepeating(repeating);

		return eventDefinitionDTO;
	}

	public StudyBean buildStudyBean(String uniqueProtocolId, String name, String briefSummary, String principalInvestigator, String sponsor, int expectedTotalEnrollment, String protocolType,
			String status, Date startDate, UserAccountBean owner) {

		StudyBean study = new StudyBean();
		ResourceBundle resadmin = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getAdminBundle();
		if (protocolType.equals(resadmin.getString("interventional"))) {
			study.setProtocolType("interventional");
		} else if (protocolType.equals(resadmin.getString("observational"))) {
			study.setProtocolType("observational");
		}
		ResourceBundle resword = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getWordsBundle();
		if (resword.getString("available").equalsIgnoreCase(status))
			study.setStatus(Status.AVAILABLE);
		else if (resword.getString("design").equalsIgnoreCase(status) || status.equals(""))
			study.setStatus(Status.PENDING);

		study.setIdentifier(uniqueProtocolId);
		study.setName(name);
		study.setPrincipalInvestigator(principalInvestigator);
		study.setSummary(briefSummary);
		study.setSponsor(sponsor);
		study.setExpectedTotalEnrollment(expectedTotalEnrollment);
		study.setDatePlannedStart(startDate);

		study.setOwner(owner);

		return study;
	}

	public ErrorObject createErrorObject(String resource, String code, String field) {
		ErrorObject errorOBject = new ErrorObject();
		errorOBject.setResource(resource);
		errorOBject.setCode(code);
		errorOBject.setField(field);
		return errorOBject;
	}

	public Role getStudyRole(String roleName, ResourceBundle resterm) {
		if (roleName.equalsIgnoreCase(resterm.getString("Study_Director").trim())) {
			return Role.STUDYDIRECTOR;
		} else if (roleName.equalsIgnoreCase(resterm.getString("Study_Coordinator").trim())) {
			return Role.COORDINATOR;
		} else if (roleName.equalsIgnoreCase(resterm.getString("Investigator").trim())) {
			return Role.INVESTIGATOR;
		} else if (roleName.equalsIgnoreCase(resterm.getString("Data_Entry_Person").trim())) {
			return Role.RESEARCHASSISTANT;
		} else if (roleName.equalsIgnoreCase(resterm.getString("Monitor").trim())) {
			return Role.MONITOR;
		} else
			return null;
	}

	public Role getSiteRole(String roleName, ResourceBundle resterm) {
		if (roleName.equalsIgnoreCase(resterm.getString("site_investigator").trim())) {
			return Role.INVESTIGATOR;
		} else if (roleName.equalsIgnoreCase(resterm.getString("site_Data_Entry_Person").trim())) {
			return Role.RESEARCHASSISTANT;
		} else if (roleName.equalsIgnoreCase(resterm.getString("site_monitor").trim())) {
			return Role.MONITOR;
		} else if (roleName.equalsIgnoreCase(resterm.getString("site_Data_Entry_Person2").trim())) {
			return Role.RESEARCHASSISTANT2;
		} else
			return null;
	}

}


