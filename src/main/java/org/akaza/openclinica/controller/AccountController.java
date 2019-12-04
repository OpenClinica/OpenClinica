package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.core.UserType;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.login.UserDTO;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.SpringServletAccess;
import core.org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import core.org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import core.org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import core.org.akaza.openclinica.domain.user.AuthoritiesBean;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping(value = "/accounts")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class AccountController {

	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;

	@Autowired
	ServletContext context;

	//@Autowired
	//AuthenticationManager authenticationManager;

	public static final String FORM_CONTEXT = "ecid";

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	public static final String INPUT_EMAIL = "";
	public static final String INPUT_INSTITUTION = "PFORM";
	UserAccountDAO udao;

	StudySubjectDAO ssdao;
	UserDTO uDTO;
	AuthoritiesDao authoritiesDao;
	ParticipantPortalRegistrar participantPortalRegistrar;
    private AuditUserLoginDao auditUserLoginDao;

    private StudyDao studyDao;
	
	/**
	 * @api {post} /pages/accounts/login Retrieve a user account
	 * @apiName getAccountByUserName
	 * @apiPermission admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} username OC login Username.
	 * @apiParam {String} password OC login Password .
	 * @apiGroup User Account
	 * @apiDescription Retrieve a user account
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "username": "usera",
	 *                  "password": "password"
	 *                  }
	 * @apiErrorExample {json} Error-Response:
	 *                  HTTP/1.1 401 Bad Credentials
	 *                  {
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "lastName": "User",
	 *                    "username": "root",
	 *                    "roles": [
	 *                    {"roleName": "director", "studyOID": "S_DEFAULTS1"},
	 *                    {"roleName": "Data Specialist", "studyOID": "S_JAPSTUDY_5293"}
	 *                    ],
	 *                    "firstName": "Root",
	 *                    "password": "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8",
	 *                    "apiKey": "6e8b69f6fb774e899f9a6c349c5adace"
	 *                    }
	 */



/*	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<HashMap> getAccountByUserName(@RequestBody HashMap<String, String> requestMap) throws Exception {

		System.out.println("I'm in getAccountByUserName");
		String userName = requestMap.get("username");
		String password = requestMap.get("password");

		Authentication authentication = new UsernamePasswordAuthenticationToken(userName, password);

		try {
			authentication = authenticationManager.authenticate(authentication);
		} catch (Exception bce) {
			return new ResponseEntity<HashMap>(new HashMap(), org.springframework.http.HttpStatus.UNAUTHORIZED);
		}

		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
		StudyDAO studyDAO = new StudyDAO(dataSource);
		HashMap<String, Object> userDTO = new HashMap<String, Object>();

		UserAccountBean userAccountBean = (UserAccountBean) userAccountDAO.findByUserName(userName);
		if (null != userAccountBean) {
			userDTO.put("username", userName);
			userDTO.put("password", userAccountBean.getPasswd());
			userDTO.put("firstName", userAccountBean.getFirstName());
			userDTO.put("lastName", userAccountBean.getLastName());
			userDTO.put("apiKey", userAccountBean.getApiKey());

			ArrayList<HashMap<String, String>> rolesDTO = new ArrayList<>();
			for (StudyUserRoleBean role : (List<StudyUserRoleBean>) userAccountBean.getRoles()) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("roleName", role.getRoleName());
				map.put("studyOID", ((Study) studyDAO.findByPK(role.getStudyId())).getOid());
				rolesDTO.add(map);
			}
			userDTO.put("roles", rolesDTO);
		} else {
			return new ResponseEntity<HashMap>(new HashMap(), org.springframework.http.HttpStatus.UNAUTHORIZED);

		}
		return new ResponseEntity<HashMap>(userDTO, org.springframework.http.HttpStatus.OK);
	}*/

	/**
	 * @api {get} /pages/accounts/study/:studyOid/crc/:crcUserName Retrieve a user account - crc
	 * @apiName getAccount1
	 * @apiPermission Module participate - enabled & admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} studyOid Study Oid.
	 * @apiParam {String} crcUserName CRC Username .
	 * @apiGroup User Account
	 * @apiDescription Retrieves the crc user account with the given crcUserName and studyOid
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "studyOid": " S_BL101",
	 *                  "crcUserName": "crc_user"
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "lName": "Jackson",
	 *                    "mobile": "",
	 *                    "accessCode": "",
	 *                    "apiKey": "6e8b69f6fb774e899f9a6c349c5adace",
	 *                    "password": "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8",
	 *                    "email": "abc@yahoo.com",
	 *                    "userName": "crc_user",
	 *                    "studySubjectId": null,
	 *                    "fName": "joe"
	 *                    }
	 */

	@RequestMapping(value = "/study/{studyOid}/crc/{crcUserName}", method = RequestMethod.GET)
	public ResponseEntity<UserDTO> getAccount1(@PathVariable("studyOid") String studyOid, @PathVariable("crcUserName") String crcUserName) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		uDTO = null;
		logger.debug("I'm in getAccount1");

		Study parentStudy = getParentStudy(studyOid);
		Integer pStudyId = parentStudy.getStudyId();
		String oid = parentStudy.getOc_oid();

		if (isStudyASiteLevelStudy(studyOid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (!mayProceed(oid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (isStudyDoesNotExist(oid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (isCRCUserAccountDoesNotExist(crcUserName))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (doesCRCNotHaveStudyAccessRole(crcUserName, pStudyId))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(crcUserName);
		buildUserDTO(userAccountBean);
		return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
	}

	public Boolean isCRCHasAccessToStudySubject(String studyOid, String crcUserName, String studySubjectId) {
		uDTO = null;
		logger.debug("I'm in getAccount4");
		Study parentStudy = getParentStudy(studyOid);
		String oid = parentStudy.getOc_oid();

		if (isStudySubjecAndCRCRolesMatch(studySubjectId, crcUserName, studyOid))
			return true;

		return false;
	}

	/**
	 * @api {get} /pages/accounts/study/:studyOid/accesscode/:accessCode Retrieve a user account - participant
	 * @apiName getAccount2
	 * @apiPermission Module participate - enabled & admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} studyOid Study Oid.
	 * @apiParam {String} accessCode Participant Access code .
	 * @apiGroup User Account
	 * @apiDescription Retrieves the participant user account with the given accessCode and studyOid
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "studyOid": " S_BL101",
	 *                  "accessCode": "yfzqpvDpiJftIZgNDphvxg=="
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "lName": "",
	 *                    "mobile": "jLGQwxkuVpPBLJCtnLdrAw==",
	 *                    "accessCode": "yfzqpvDpiJftIZgNDphvxg==",
	 *                    "password": "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8",
	 *                    "email": "XzJadh3l3V7uUoPCggbSoIfoNW8IQU3qsvrtHfJH7J0=",
	 *                    "userName": "S_BL101.SS_SUBA101",
	 *                    "studySubjectId": null,
	 *                    "fName": "07hQGfwT6LRXk0rLLYwkviwNdOEycnj4lOjrNMBdesk="
	 *                    }
	 */

	@RequestMapping(value = "/study/{studyOid}/accesscode/{accessCode}", method = RequestMethod.GET)
	public ResponseEntity<UserDTO> getAccount2(@PathVariable("studyOid") String studyOid, @PathVariable("accessCode") String accessCode) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		uDTO = null;
		logger.debug("I'm in getAccount2");

		accessCode = URLDecoder.decode(accessCode, "UTF-8");

		Study parentStudy = getParentStudy(studyOid);
		String oid = parentStudy.getOc_oid();

		if (isStudyASiteLevelStudy(studyOid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (!mayProceed(oid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (isStudyDoesNotExist(oid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (isAccessCodeIsNull(accessCode))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		UserAccountBean accessCodeAccountBean = getAccessCodeAccount(accessCode);
		if (!accessCodeAccountBean.isActive())
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        // Since 3.8, openclinica participate needs to be able to use api from openclinica using api_key
        // Copied from UserAccountController.java
        // This code should've been in liquibase migration for better readability.
        if (accessCodeAccountBean.getApiKey() == null || accessCodeAccountBean.getApiKey().isEmpty()) {
            String apiKey = null;
            do {
                apiKey = getRandom32ChApiKey();
            } while (isApiKeyExist(apiKey));
            accessCodeAccountBean.setEnableApiKey(true);
            accessCodeAccountBean.setApiKey(apiKey);
            updateUserAccount(accessCodeAccountBean);
        }

		buildUserDTO(accessCodeAccountBean);
        // Client want to trade access_code for api_key, for later usage of our api.
        if (accessCodeAccountBean.isEnableApiKey()) {
            uDTO.setApiKey(accessCodeAccountBean.getApiKey());
        }
		return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
	}

	/**
	 * @api {get} /pages/accounts/study/:studyOid/studysubject/:studySubjectId Retrieve a user account - participant
	 * @apiName getAccount3
	 * @apiPermission Module participate - enabled & admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} studyOid Study Oid.
	 * @apiParam {String} studySubjectId Study Subject Id .
	 * @apiGroup User Account
	 * @apiDescription Retrieves the participant user account with the given studySubjectId and studyOid
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "studyOid": " S_BL101",
	 *                  "studySubjectId": "Sub100"
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "lName": "",
	 *                    "mobile": "JTaa7WGRdH5dGs42XyTrgA==",
	 *                    "accessCode": "5s02UFpiMBijWuzaxSOojg==",
	 *                    "password": "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8",
	 *                    "email": "XzJadh3l3V7uUoPCggbSoIfoNW8IQU3qsvrtHfJH7J0=",
	 *                    "userName": "S_BL101.SS_SUB100",
	 *                    "studySubjectId": null,
	 *                    "fName": "pdyGCN1CdAKIGOUEERz/yQ=="
	 *                    }
	 */
	@RequestMapping(value = "/study/{studyOid}/studysubject/{studySubjectId}", method = RequestMethod.GET)
	public ResponseEntity<UserDTO> getAccount3(@PathVariable("studyOid") String studyOid, @PathVariable("studySubjectId") String studySubjectId) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		uDTO = null;
		logger.debug("I'm in getAccount3");

		Study parentStudy = getParentStudy(studyOid);
		String oid = parentStudy.getOc_oid();

		StudySubjectBean studySubjectBean = getStudySubject(studySubjectId, parentStudy);

		if (isStudyASiteLevelStudy(studyOid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (!mayProceed(oid, studySubjectBean))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (isStudyDoesNotExist(oid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		if (isStudySubjectDoesNotExist(studySubjectBean))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		// build UserName
		HashMap<String, String> mapValues = buildParticipantUserName(studySubjectBean);
		String pUserName = mapValues.get("pUserName"); // Participant User Name

		udao = new UserAccountDAO(dataSource);
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(pUserName);
		if (!userAccountBean.isActive()) {
			uDTO = new UserDTO();
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
		} else {
			buildUserDTO(userAccountBean);
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
		}
	}

	/**
	 * @api {post} /pages/accounts/ Create a user account - participant
	 * @apiName createParticipantUserAccount
	 * @apiPermission Module participate - enabled & admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} studyOid Study Oid.
	 * @apiParam {String} studySubjectId Study Subject Id .
	 * @apiParam {String} fName First Name
	 * @apiParam {String} lName Last Name
	 * @apiParam {String} mobile Mobile Phone
	 * @apiParam {String} accessCode Access Code
	 * @apiParam {String} crcUserName CRC UserName
	 * @apiParam {String} email Email Address
	 *
	 * @apiGroup User Account
	 * @apiDescription Creates a participant user account
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "studyOid": "S_BL101",
	 *                  "studySubjectId": "Sub100",
	 *                  "fName": "Dany",
	 *                  "lName": "Keegan",
	 *                  "mobile": "617 865 4567",
	 *                  "accessCode": "5s02UFpiMBijWuzaxSOojg==",
	 *                  "crcUserName": "crc_user",
	 *                  "email": "abc@yahoo.com"
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "studySubjectId": null,
	 *                    "email": "abc@yahoo.com",
	 *                    "accessCode": "5s02UFpiMBijWuzaxSOojg==",
	 *                    "password": "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8",
	 *                    "userName": "S_BL101.SS_SUB100",
	 *                    "fName": "Dany",
	 *                    "lName": "Keegan",
	 *                    "mobile": "617 865 4567"
	 *                    }
	 */
	/**
	 * @api {post} /pages/accounts/ Update a user account - participant
	 * @apiName updateParticipantUserAccount
	 * @apiPermission Module participate - enabled & admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} studyOid Study Oid.
	 * @apiParam {String} studySubjectId Study Subject Id .
	 * @apiParam {String} fName First Name
	 * @apiParam {String} lName Last Name
	 * @apiParam {String} mobile Mobile Phone
	 * @apiParam {String} accessCode Access Code
	 * @apiParam {String} crcUserName CRC UserName
	 * @apiParam {String} email Email Address
	 *
	 * @apiGroup User Account
	 * @apiDescription Updates a participant user account
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "studyOid": "S_BL101",
	 *                  "studySubjectId": "Sub100",
	 *                  "fName": "Dany",
	 *                  "lName": "Keegan",
	 *                  "mobile": "617 865 4567",
	 *                  "accessCode": "5s02UFpiMBijWuzaxSOojg==",
	 *                  "crcUserName": "crc_user",
	 *                  "email": "abc@yahoo.com"
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "studySubjectId": null,
	 *                    "email": "abc@yahoo.com",
	 *                    "accessCode": "5s02UFpiMBijWuzaxSOojg==",
	 *                    "password": "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8",
	 *                    "userName": "S_BL101.SS_SUB100",
	 *                    "fName": "Dany",
	 *                    "lName": "Keegan",
	 *                    "mobile": "617 865 4567"
	 *                    }
	 */

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<UserDTO> createOrUpdateAccount(@RequestBody HashMap<String, String> map) throws Exception {
		uDTO = null;
		logger.debug("I'm in createOrUpdateAccount");

		Study parentStudy = getParentStudy(map.get("studyOid"));
		String oid = parentStudy.getOc_oid();

		String studySubjectId = map.get("studySubjectId");
		String fName = map.get("fName");
		String lName = map.get("lName");
		String mobile = map.get("mobile");
		String accessCode = map.get("accessCode");
		String crcUserName = map.get("crcUserName");
		String email = map.get("email");

		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		logger.debug("************** You are in the Rest Service ***************");


		UserAccountBean uBean = null;

		StudySubjectBean studySubjectBean = getStudySubject(studySubjectId, parentStudy);
		UserAccountBean ownerUserAccount = getUserAccount(crcUserName);

		if (!mayProceed(oid, studySubjectBean))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (isStudyDoesNotExist(oid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		if (isStudySubjectDoesNotExist(studySubjectBean))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		if (isFistNameInValid(fName))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		if (isPhoneFieldIsNull(mobile) && isEmailIsNull(email))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		if (isAccessCodeIsNull(accessCode))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		if (isAccessCodeExistInSystem(accessCode))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		// build UserName
		HashMap<String, String> mapValues = buildParticipantUserName(studySubjectBean);
		String pUserName = mapValues.get("pUserName"); // Participant User Name
		String studySubjectOid = mapValues.get("studySubjectOid");
		Integer pStudyId = Integer.valueOf(mapValues.get("pStudyId"));

		if (isCRCUserAccountDoesNotExist(crcUserName))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		// Verify CRC_user has the appropriate role as 'data entry person'or 'data entry person 2' and have access to the specific study/site
		// This also verifies that fact that the CRC and the Participant both have access to same study/site
		if (doesCRCNotHaveStudyAccessRole(crcUserName, pStudyId))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		// Participant user account create (if does not exist in user table) or Update(if exist in user table)
		uBean = buildUserAccount(oid, studySubjectOid, fName, lName, mobile, accessCode, ownerUserAccount, pUserName, email);
		UserAccountBean participantUserAccountBean = getUserAccount(pUserName);
		if (!participantUserAccountBean.isActive()) {
			createUserAccount(uBean);
			uBean.setUpdater(uBean.getOwner());
			updateUserAccount(uBean);
			disableUserAccount(uBean);
			logger.info("***New User Account is created***");
			uDTO = buildUserDTO(uBean);
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);

		} else {
			uBean.setId(getUserAccount(uBean.getName()).getId());
			uBean.setUpdater(uBean.getOwner());
			updateUserAccount(uBean);
			logger.info("***User Account already exist in the system and data is been Updated ***");
			uDTO = buildUserDTO(uBean);
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
		}
	}

	/**
	 * @api {post} /pages/accounts/timezone Update subject time zone
	 * @apiName updateTimezone
	 * @apiPermission admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} studyOid Study Oid.
	 * @apiParam {String} studySubjectId Study Subject Oid .
	 * @apiParam {String} timeZone Time Zone .
	 * @apiGroup Subject
	 * @apiDescription Updates the subject time zone
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "studyOid": "S_BL101",
	 *                  "studySubjectId": "SS_SUB100",
	 *                  "timeZone": "America/New_York"
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    }
	 */

	@RequestMapping(value = "/timezone", method = RequestMethod.POST)
	public ResponseEntity<UserDTO> updateTimezone(@RequestBody HashMap<String, String> map) throws Exception {
		uDTO = null;
		logger.debug("I'm in update Timezone method");

		Study parentStudy = getParentStudy(map.get("studyOid"));
		String oid = parentStudy.getOc_oid();

		String studySubjectId = map.get("studySubjectId");
		String timeZone = map.get("timeZone");

		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		logger.debug("************** You are in the Rest Service ***************");

		StudySubjectBean studySubjectBean = getStudySubjectByOidAndStudy(studySubjectId, parentStudy.getStudyId());
		HashMap<String, String> mapValues = buildParticipantUserName(studySubjectBean);
		String pUserName = mapValues.get("pUserName"); // Participant User Name

		udao = new UserAccountDAO(dataSource);
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(pUserName);

		if (studySubjectBean.isActive()) {
			studySubjectBean.setTime_zone(timeZone);
			studySubjectBean.setUpdater(userAccountBean);
			updateStudySubjectBean(studySubjectBean);
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
		}
		return null;
	}

	private UserDTO buildUserDTO(UserAccountBean userAccountBean) {
		uDTO = new UserDTO();
		uDTO.setfName(userAccountBean.getFirstName());
		uDTO.setlName(userAccountBean.getLastName());
		uDTO.setMobile(userAccountBean.getPhone());
		uDTO.setUserName(userAccountBean.getName());
		uDTO.setAccessCode(userAccountBean.getAccessCode());
		uDTO.setPassword(userAccountBean.getPasswd());
		uDTO.setEmail(userAccountBean.getEmail());
		return uDTO;
	}

	private UserAccountBean buildUserAccount(String studyOid, String studySubjectOid, String fName, String lName, String mobile, String accessCode, UserAccountBean ownerUserAccount, String pUserName,
			String email) throws Exception {

		UserAccountBean createdUserAccountBean = new UserAccountBean();

		createdUserAccountBean.setName(pUserName);
		createdUserAccountBean.setFirstName(fName);
		createdUserAccountBean.setLastName(lName);
		createdUserAccountBean.setEmail(INPUT_EMAIL);
		createdUserAccountBean.setInstitutionalAffiliation(INPUT_INSTITUTION);
		createdUserAccountBean.setLastVisitDate(null);
		createdUserAccountBean.setActiveStudyId(getStudy(studyOid).getStudyId());
		createdUserAccountBean.setPasswdTimestamp(null);
		createdUserAccountBean.setPasswdChallengeQuestion("");
		createdUserAccountBean.setPasswdChallengeAnswer("");
		createdUserAccountBean.setOwner(ownerUserAccount);
		createdUserAccountBean.setRunWebservices(false);
		createdUserAccountBean.setPhone(mobile);
		createdUserAccountBean.setAccessCode(accessCode);
		createdUserAccountBean.setPasswd("5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8");
		createdUserAccountBean.setEmail(email);

        // Since 3.8, openclinica participate needs to be able to use api from openclinica using api_key
        // Copied from UserAccountController.java
        String apiKey = null;
        do {
            apiKey = getRandom32ChApiKey();
        } while (isApiKeyExist(apiKey));
        createdUserAccountBean.setEnableApiKey(true);
        createdUserAccountBean.setApiKey(apiKey);

		Role r = Role.RESEARCHASSISTANT2;
		createdUserAccountBean = addActiveStudyRole(createdUserAccountBean, getStudy(studyOid).getStudyId(), r, ownerUserAccount);
		UserType type = UserType.get(2);
		createdUserAccountBean.addUserType(type);

		authoritiesDao = (AuthoritiesDao) SpringServletAccess.getApplicationContext(context).getBean("authoritiesDao");
		authoritiesDao.saveOrUpdate(new AuthoritiesBean(createdUserAccountBean.getName()));

		return createdUserAccountBean;
	}

	private void createUserAccount(UserAccountBean userAccountBean) {
		udao.create(userAccountBean);
	}

	private void updateUserAccount(UserAccountBean userAccountBean) {
		udao.update(userAccountBean);
	}

	private void disableUserAccount(UserAccountBean userAccountBean) {
		udao.delete(userAccountBean);
	}

	private UserAccountBean addActiveStudyRole(UserAccountBean createdUserAccountBean, int studyId, Role r, UserAccountBean ownerUserAccount) {
		StudyUserRoleBean studyUserRole = new StudyUserRoleBean();
		studyUserRole.setStudyId(studyId);
		studyUserRole.setRoleName(r.getName());
		studyUserRole.setStatus(Status.AUTO_DELETED);
		studyUserRole.setOwner(ownerUserAccount);
		createdUserAccountBean.addRole(studyUserRole);
		createdUserAccountBean.setLockCounter(3);
		createdUserAccountBean.setAccountNonLocked(false);
		return createdUserAccountBean;
	}

	private ArrayList<UserAccountBean> getUserAccountByStudy(String userName, ArrayList allStudies) {
		udao = new UserAccountDAO(dataSource,getStudyDao());
		ArrayList<UserAccountBean> userAccountBeans = udao.findStudyByUser(userName, allStudies);
		return userAccountBeans;
	}

	private UserAccountBean getUserAccount(String userName) {
		udao = new UserAccountDAO(dataSource);
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(userName);
		return userAccountBean;
	}

	private UserAccountBean getAccessCodeAccount(String accessCode) {
		udao = new UserAccountDAO(dataSource);
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByAccessCode(accessCode);
		return userAccountBean;
	}

	private Study getStudy(String oid) {
		Study studyBean = (Study) getStudyDao().findByOcOID(oid);
		return studyBean;
	}

	private Study getStudy(Integer id) {
		Study studyBean = (Study) getStudyDao().findByPK(id);
		return studyBean;
	}

	private StudySubjectBean getStudySubjectByOidAndStudy(String oid, int studyId) {
		ssdao = new StudySubjectDAO(dataSource);
		StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByOidAndStudy(oid, studyId);
		return studySubjectBean;
	}

	private StudySubjectBean getStudySubject(String label, Study study) {
		ssdao = new StudySubjectDAO(dataSource);
		StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByLabelAndStudy(label, study);
		return studySubjectBean;
	}

	private StudySubjectBean getStudySubject(String oid) {
		ssdao = new StudySubjectDAO(dataSource);
		StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByOid(oid);
		return studySubjectBean;
	}

	private void updateStudySubjectBean(StudySubjectBean sBean) {
		ssdao = new StudySubjectDAO(dataSource);
		ssdao.update(sBean);
	}

	private Boolean isStudyDoesNotExist(String studyOid) {
		Study studyBean = getStudy(studyOid);
		if (studyBean == null) {
			logger.info("***Study  Does Not Exist ***");
			return true;
		}
		return false;
	}

	private Boolean isStudyASiteLevelStudy(String studyOid) {
		Study studyBean = getStudy(studyOid);
		if (studyBean.isSite()) {
			logger.info("***Study provided in the URL is a Site study***");
			return true;
		}
		return false;
	}

	private Boolean isStudySubjectDoesNotExist(StudySubjectBean studySubjectBean) {
		if (studySubjectBean == null || !studySubjectBean.isActive()) {
			logger.info("***Study Subject Does Not Exist OR the Study Subject is not associated with the Study_Oid in the URL   ***");
			return true;
		}
		return false;
	}

	private Boolean isFistNameInValid(String fName) {
		if (fName.length() < 1) {
			logger.info("***     First Name length is less than 1 characters    ***");
			return true;
		}
		return false;
	}

	private Boolean isPhoneFieldIsNull(String mobile) {
		if (mobile.length() == 0) {
			logger.info("***     Phone # is a Required Field   ***");
			return true;
		}
		return false;
	}

	private Boolean isAccessCodeIsNull(String accessCode) {
		if (accessCode.length() == 0) {
			logger.info("***Access Code is a Required field and can't be null ***");
			return true;
		}
		return false;
	}

	private Boolean isAccessCodeExistInSystem(String accessCode) {
		UserAccountBean accessCodeAccountBean = getAccessCodeAccount(accessCode);
		if (accessCodeAccountBean.isActive()) {
			logger.info("***Access Code already Exist in the User Table ***");
			return true;
		}
		return false;
	}

	private Boolean isCRCUserAccountDoesNotExist(String crcUserName) {
		UserAccountBean ownerUserAccount = getUserAccount(crcUserName);
		if (!ownerUserAccount.isActive()) {
			logger.info("***  CRC user acount does not Exist in the User Table ***");
			return true;
		}
		return false;
	}

	private Boolean isEmailIsNull(String email) {
		if (email.length() == 0) {
			logger.info("***Email Address is a Required field and can't be null ***");
			return true;
		}
		return false;
	}

	private HashMap buildParticipantUserName(StudySubjectBean studySubjectBean) {
		HashMap<String, String> map = new HashMap();
		String studySubjectOid = studySubjectBean.getOid();
		Integer studyId = studySubjectBean.getStudyId();
		Study study = getParentStudy(studyId);
		Integer pStudyId = study.getStudyId();

		String pUserName = study.getOc_oid() + "." + studySubjectOid;
		logger.info("participate Username: {}",pUserName);
		map.put("pUserName", pUserName);
		map.put("pStudyId", pStudyId.toString());
		map.put("studySubjectOid", studySubjectOid);

		return map;
	}

	private Boolean doesCRCNotHaveStudyAccessRole(String crcUserName, Integer pStudyId) {
		boolean found = false;
		ArrayList<StudyUserRoleBean> studyUserRoleBeans = (ArrayList<StudyUserRoleBean>) udao.findAllRolesByUserName(crcUserName);
		for (StudyUserRoleBean studyUserRoleBean : studyUserRoleBeans) {
			Study study = getParentStudy(studyUserRoleBean.getStudyId());
			logger.info("---------------");
			logger.info("Study Id to compare:{} ", studyUserRoleBean.getStudyId());
			logger.info("parent study Id to compare: {}",study.getStudyId());
			logger.info("Parent Study Id   {}" ,pStudyId);
			logger.info("Role: {}" , studyUserRoleBean.getRoleName());
			logger.info("Status : {}" , studyUserRoleBean.getStatus().getId());

			if ((study.getStudyId() == pStudyId) && (studyUserRoleBean.getRoleName().equals("ra") || studyUserRoleBean.getRoleName().equals("ra2")) && studyUserRoleBean.getStatus().isAvailable()) {
				found = true;
				logger.info("if found : {}",found);
				break;
			}
		}
		if (!found) {
			logger.info("*** CRC Does not have access to the study/site OR CRC Does not have 'Data Entry Person' role ***");
			return true;
		}
		return false;
	}

	private Boolean doesStudySubjecAndCRCRolesMatch(String crcUserName, Integer subjectStudyId) {
		boolean found = false;
		ArrayList<StudyUserRoleBean> studyUserRoleBeans = (ArrayList<StudyUserRoleBean>) udao.findAllRolesByUserName(crcUserName);
		for (StudyUserRoleBean studyUserRoleBean : studyUserRoleBeans) {
			logger.info("StudySubject Study Id to compare to   {}" , subjectStudyId);

			if (studyUserRoleBean.getStudyId() == getParentStudy(subjectStudyId).getStudyId()) {
				subjectStudyId = getParentStudy(subjectStudyId).getStudyId();
				logger.info("StudySubject Parent Study Id to compare to Overwritten    " + subjectStudyId);
			}
			logger.info("CRC Study Id to compare to : {}" , studyUserRoleBean.getStudyId());
			logger.info("Role: {}",studyUserRoleBean.getStudyId());
			logger.info("Status: {}",studyUserRoleBean.getStatus().getId());


			if ((studyUserRoleBean.getStudyId() == subjectStudyId) && (studyUserRoleBean.getRoleName().equals("ra") || studyUserRoleBean.getRoleName().equals("ra2"))
					&& studyUserRoleBean.getStatus().isAvailable()) {
				found = true;
				logger.info("if found : {}",found);
				break;
			}
		}
		if (!found) {
			logger.info("*** CRC Role does not match with StudySubject assignment ***");
			return true;
		}
		return false;
	}

	private Boolean isStudySubjecAndCRCRolesMatch(String studySubjectId, String crcUserName, String studyOid) {
		// crc is siteA studySubject is siteA , pass (same site)
		// crc is siteA studySubject is siteB , Fail
		// crc is siteA studySubject is study , Fail

		// crc is study studySubject is siteA , pass
		// crc is study studySubject is siteB , pass
		// crc is study studySubject is study , pass

		Study parentStudy = getParentStudy(studyOid);
		Integer studyIdFromStudyOid = parentStudy.getStudyId();
		StudySubjectBean studySubjectBean = getStudySubject(studySubjectId, parentStudy);
		Integer studyIdFromStudySubjectId = studySubjectBean.getStudyId();

		logger.info(" ----------- ");
		logger.info("studyIdFromStudyOid : {} " , studyIdFromStudyOid);
		logger.info("studySubjectId:  {}" , studySubjectId);
		logger.info("studyIdFromStudySubjectId:  {}" , studyIdFromStudySubjectId);

		return doesStudySubjecAndCRCRolesMatch(crcUserName, studyIdFromStudySubjectId);

	}

	private Study getParentStudy(Integer studyId) {
		Study study = getStudy(studyId);
		if (!study.isSite()) {
			return study;
		} else {
			Study parentStudy = study.getStudy();
			return parentStudy;
		}

	}

	private Study getParentStudy(String studyOid) {
		Study study = getStudy(studyOid);
		if (!study.isSite()) {
			return study;
		} else {
			Study parentStudy = study.getStudy();
			return parentStudy;
		}

	}

	private boolean mayProceed(String studyOid, StudySubjectBean ssBean) throws Exception {
		boolean accessPermission = false;
		if (ssBean.isActive()) {
			logger.info("  studySubjectStatus: " + ssBean.getStatus().getName());
			if (mayProceed(studyOid) && ssBean.getStatus() == Status.AVAILABLE) {
				accessPermission = true;
			}
		}
		return accessPermission;
	}

	private boolean mayProceed(String studyOid) throws Exception {
		boolean accessPermission = false;
		Study siteStudy = getStudy(studyOid);
		Study study = getParentStudy(studyOid);
		StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
		StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getStudyId(), "participantPortal");
		participantPortalRegistrar = new ParticipantPortalRegistrar();
		String pManageStatus = participantPortalRegistrar.getRegistrationStatus(study.getOc_oid()).toString(); // ACTIVE , PENDING , INACTIVE
		String participateStatus = pStatus.getValue().toString(); // enabled , disabled
		String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
		String siteStatus = siteStudy.getStatus().getName().toString(); // available , pending , frozen , locked
		logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus + "   siteStatus: " + siteStatus);
		if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && siteStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE")) {
			accessPermission = true;
		}

		return accessPermission;
	}

	@RequestMapping(value = "/study/{studyOid}", method = RequestMethod.GET)
	public ResponseEntity<ArrayList<UserDTO>> getAllParticipantPerStudy(@PathVariable("studyOid") String studyOid) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		ArrayList<UserDTO> uDTOs = null;
		logger.debug("I'm in getAllParticipantPerStudy");

		Study parentStudy = getParentStudy(studyOid);
		String oid = parentStudy.getOc_oid();

		if (isStudyDoesNotExist(oid))
			return new ResponseEntity<ArrayList<UserDTO>>(uDTOs, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		// build UserName
		// HashMap<String, String> mapValues = buildParticipantUserName(studySubjectBean);

		udao = new UserAccountDAO(dataSource);
		ArrayList<UserAccountBean> uBeans = (ArrayList<UserAccountBean>) udao.findAllParticipantsByStudyOid(oid);
		if (uBeans != null) {
			uDTOs = new ArrayList<>();
			for (UserAccountBean uBean : uBeans) {
				UserDTO uDTO = new UserDTO();

				String username = uBean.getName();
				String studySubjectOid = username.substring(username.indexOf(".") + 1);
				ssdao = new StudySubjectDAO<>(dataSource);
				String studySubjectId = ssdao.findByOid(studySubjectOid).getLabel();

				uDTO.setfName(uBean.getFirstName());
				uDTO.setEmail(uBean.getEmail());
				uDTO.setMobile(uBean.getPhone());
				uDTO.setAccessCode(uBean.getAccessCode());
				uDTO.setUserName(uBean.getName());
				uDTO.setPassword(uBean.getPasswd());
				uDTO.setlName(uBean.getLastName());
				uDTO.setStudySubjectId(studySubjectId);

				uDTOs.add(uDTO);
			}
			return new ResponseEntity<ArrayList<UserDTO>>(uDTOs, org.springframework.http.HttpStatus.OK);
		} else {
			return new ResponseEntity<ArrayList<UserDTO>>(uDTOs, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public ResponseEntity<UserDTO> updateAccount(@RequestBody HashMap<String, String> map) throws Exception {
		uDTO = null;
		logger.debug("I'm in UpdateAccount");

		Study parentStudy = getParentStudy(map.get("studyOid"));
		String oid = parentStudy.getOc_oid();

		String studySubjectId = map.get("studySubjectId");
		String fName = map.get("fName");
		String lName = map.get("lName");
		String mobile = map.get("mobile");
		String accessCode = map.get("accessCode");
		String crcUserName = map.get("crcUserName");
		String email = map.get("email");

		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		logger.debug("******************     You are in the Update Rest Service   *****************");

		UserAccountBean uBean = null;

		StudySubjectBean studySubjectBean = getStudySubject(studySubjectId, parentStudy);
		UserAccountBean ownerUserAccount = getUserAccount(crcUserName);

		// build UserName
		HashMap<String, String> mapValues = buildParticipantUserName(studySubjectBean);
		String pUserName = mapValues.get("pUserName"); // Participant User Name
		String studySubjectOid = mapValues.get("studySubjectOid");
		Integer pStudyId = Integer.valueOf(mapValues.get("pStudyId"));

		// Participant user account create (if does not exist in user table) or Update(if exist in user table)
		uBean = buildUserAccount(oid, studySubjectOid, fName, lName, mobile, accessCode, ownerUserAccount, pUserName, email);
		UserAccountBean participantUserAccountBean = getUserAccount(pUserName);
		if (!participantUserAccountBean.isActive()) {
			createUserAccount(uBean);
			uBean.setUpdater(uBean.getOwner());
			updateUserAccount(uBean);
			disableUserAccount(uBean);
			logger.info("***New User Account is created***");
			uDTO = buildUserDTO(uBean);
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);

		} else {
			uBean.setId(getUserAccount(uBean.getName()).getId());
			uBean.setUpdater(uBean.getOwner());
			updateUserAccount(uBean);
			logger.info("***User Account already exist in the system and data is been Updated ***");
			uDTO = buildUserDTO(uBean);
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
		}
	}

	   @RequestMapping(value = "/auditcrc", method = RequestMethod.POST)
	    public ResponseEntity<HashMap> auditcrc(@RequestBody HashMap<String, String> requestMap) throws Exception {
           HashMap map = new HashMap();
           
           String crcUserName = requestMap.get("crcUserName");
           String studyOid = requestMap.get("studyOid");
           String studySubjectId = requestMap.get("studySubjectId");
           
           Study parentStudy = getParentStudy(studyOid);
           StudySubjectBean studySubjectBean = getStudySubject(studySubjectId, parentStudy);

           // build UserName
           HashMap<String, String> mapValues = buildParticipantUserName(studySubjectBean);
           String pUserName = mapValues.get("pUserName"); // Participant User Name
           AuditUserLoginBean auditUserLogin =new AuditUserLoginBean();
           UserAccountBean userAccount =getUserAccount(crcUserName); 
                      
           auditUserLogin.setUserName(userAccount.getName());
           auditUserLogin.setLoginStatus(LoginStatus.ACCESS_CODE_VIEWED);
           auditUserLogin.setLoginAttemptDate(new Date());
           auditUserLogin.setUserAccountId(userAccount != null ? userAccount.getId() : null);
           auditUserLogin.setDetails(pUserName);
           
           
           
           getAuditUserLoginDao().save(auditUserLogin);
           
           

	       
	       return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);
	    }

	    public AuditUserLoginDao getAuditUserLoginDao() {
	        auditUserLoginDao =
	            this.auditUserLoginDao != null ? auditUserLoginDao : (AuditUserLoginDao) SpringServletAccess.getApplicationContext(context).getBean(
	                    "auditUserLoginDao");
	        return auditUserLoginDao;
	    }
	    public StudyDao getStudyDao(){
			this.studyDao =
					this.studyDao != null ? studyDao : (StudyDao) SpringServletAccess.getApplicationContext(context).getBean(
							"studyDaoDomain");
			return studyDao;
		}

	
    public Boolean isApiKeyExist(String uuid) {
        UserAccountDAO udao = new UserAccountDAO(dataSource);
        UserAccountBean uBean = (UserAccountBean) udao.findByApiKey(uuid);
        if (uBean == null || !uBean.isActive()) {
            return false;
        } else {
            return true;
        }
    }

    public String getRandom32ChApiKey() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }
}
