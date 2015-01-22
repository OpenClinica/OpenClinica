package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.admin.AuditBean;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.login.UserDTO;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.dao.admin.AuditDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.web.pform.PFormCache;
import org.akaza.openclinica.web.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.httpclient.HttpStatus;
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
import org.springframework.http.ResponseEntity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping(value = "/accounts")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class AccountController {

	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;

	@Autowired
	ServletContext context;

	public static final String FORM_CONTEXT = "ecid";

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	public static final String INPUT_EMAIL = "";
	public static final String INPUT_INSTITUTION = "PFORM";
	UserAccountDAO udao;
	StudyDAO sdao;
	StudySubjectDAO ssdao;
	UserDTO uDTO;
	AuthoritiesDao authoritiesDao;
	ParticipantPortalRegistrar participantPortalRegistrar;

	@RequestMapping(value = "/study/{studyOid}/crc/{crcUserName}", method = RequestMethod.GET)
	public ResponseEntity<UserDTO> getAccount1(@PathVariable("studyOid") String studyOid, @PathVariable("crcUserName") String crcUserName) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		uDTO = null;
		
		if (!mayProceed(studyOid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (isStudyDoesNotExist(studyOid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (isCRCUserAccountDoesNotExist(crcUserName))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		StudyBean study = getStudy(studyOid);
		Integer studyId = study.getId();
		Integer pStudyId = 0;

		if (!sdao.isAParent(studyId)) {
			StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
			pStudyId = parentStudy.getId();
		}

		if (doesCRCNotHaveStudyAccessRole(crcUserName, studyId, pStudyId))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(crcUserName);
		buildUserDTO(userAccountBean);
		return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
	}

	@RequestMapping(value = "/study/{studyOid}/accesscode/{accessCode}", method = RequestMethod.GET)
	public ResponseEntity<UserDTO> getAccount2(@PathVariable("studyOid") String studyOid, @PathVariable("accessCode") String accessCode) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		uDTO = null;
		
		if (!mayProceed(studyOid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);


		if (isStudyDoesNotExist(studyOid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (isAccessCodeIsNull(accessCode))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		UserAccountBean accessCodeAccountBean = getAccessCodeAccount(accessCode);
		if (!accessCodeAccountBean.isActive())
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		buildUserDTO(accessCodeAccountBean);
		return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
	}

	@RequestMapping(value = "/study/{studyOid}/studysubject/{studySubjectId}", method = RequestMethod.GET)
	public ResponseEntity<UserDTO> getAccount3(@PathVariable("studyOid") String studyOid, @PathVariable("studySubjectId") String studySubjectId) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		uDTO = null;
		StudyBean studyBean = getStudy(studyOid);
		StudySubjectBean studySubjectBean = getStudySubject(studySubjectId, studyBean);

		if (!mayProceed(studyOid,studySubjectBean))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (isStudyDoesNotExist(studyOid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		if (isStudySubjectDoesNotExist(studySubjectBean))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		// build UserName
		HashMap<String, String> mapValues = buildParticipantUserName(studySubjectBean);
		String pUserName = mapValues.get("pUserName"); // Participant User Name

		udao = new UserAccountDAO(dataSource);
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(pUserName);
		if (!userAccountBean.isActive()) {
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		} else {
			buildUserDTO(userAccountBean);
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<UserDTO> createOrUpdateAccount(@RequestBody HashMap<String, String> map) throws Exception {
		uDTO = null;
		String studyOid = map.get("studyOid");
		String studySubjectId = map.get("studySubjectId");
		String fName = map.get("fName");
		String lName = map.get("lName");
		String mobile = map.get("mobile");
		String accessCode = map.get("accessCode");
		String crcUserName = map.get("crcUserName");

		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		System.out.println("******************     You are in the Rest Service   *****************");

		UserAccountBean uBean = null;

		StudyBean studyBean = getStudy(studyOid);
		StudySubjectBean studySubjectBean = getStudySubject(studySubjectId, studyBean);
		UserAccountBean ownerUserAccount = getUserAccount(crcUserName);

		if (!mayProceed(studyOid,studySubjectBean))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		if (isStudyDoesNotExist(studyOid))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		if (isStudySubjectDoesNotExist(studySubjectBean))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		if (isFistName2orLessCharacters(fName))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		if (isPhoneFieldIsNull(mobile))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		if (isAccessCodeIsNull(accessCode))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		if (isAccessCodeExistInSystem(accessCode))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		// build UserName
		HashMap<String, String> mapValues = buildParticipantUserName(studySubjectBean);
		String pUserName = mapValues.get("pUserName"); // Participant User Name
		String studySubjectOid = mapValues.get("studySubjectOid");
		Integer studyId = Integer.valueOf(mapValues.get("studyId"));
		Integer pStudyId = Integer.valueOf(mapValues.get("pStudyId"));

		if (isCRCUserAccountDoesNotExist(crcUserName))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		// Verify CRC_user has the appropriate role as 'data entry person'or 'data entry person 2' and have access to the specific study/site
		// This also verifies that fact that the CRC and the Participant both have access to same study/site
		if (doesCRCNotHaveStudyAccessRole(crcUserName, studyId, pStudyId))
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		// Participant user account create (if does not exist in user table) or Update(if exist in user table)
		uBean = buildUserAccount(studyOid, studySubjectOid, fName, lName, mobile, accessCode, ownerUserAccount, pUserName);
		UserAccountBean participantUserAccountBean = getUserAccount(pUserName);
		if (!participantUserAccountBean.isActive()) {
			createUserAccount(uBean);
			uBean.setUpdater(uBean.getOwner());
			updateUserAccount(uBean);
			disableUserAccount(uBean);
			logger.info("***New User Account is created***");
			System.out.println("***New User Account is created***");
			uDTO = buildUserDTO(uBean);
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);

		} else {
			uBean.setId(getUserAccount(uBean.getName()).getId());
			uBean.setUpdater(uBean.getOwner());
			updateUserAccount(uBean);
			logger.info("***User Account already exist in the system and data is been Updated ***");
			System.out.println("***User Account already exist in the system and data is been Updated ***");
			uDTO = buildUserDTO(uBean);
			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
		}
	}

	private UserDTO buildUserDTO(UserAccountBean userAccountBean) {
		uDTO = new UserDTO();
		uDTO.setfName(userAccountBean.getFirstName());
		uDTO.setlName(userAccountBean.getLastName());
		uDTO.setMobile(userAccountBean.getPhone());
		uDTO.setUserName(userAccountBean.getName());
		uDTO.setAccessCode(userAccountBean.getAccessCode());
		uDTO.setPassword(userAccountBean.getPasswd());
		return uDTO;
	}

	private UserAccountBean buildUserAccount(String studyOid, String studySubjectOid, String fName, String lName, String mobile, String accessCode, UserAccountBean ownerUserAccount, String pUserName)
			throws Exception {

		UserAccountBean createdUserAccountBean = new UserAccountBean();

		createdUserAccountBean.setName(pUserName);
		createdUserAccountBean.setFirstName(fName);
		createdUserAccountBean.setLastName(lName);
		createdUserAccountBean.setEmail(INPUT_EMAIL);
		createdUserAccountBean.setInstitutionalAffiliation(INPUT_INSTITUTION);
		createdUserAccountBean.setLastVisitDate(null);
		createdUserAccountBean.setActiveStudyId(getStudy(studyOid).getId());
		createdUserAccountBean.setPasswdTimestamp(null);
		createdUserAccountBean.setPasswdChallengeQuestion("");
		createdUserAccountBean.setPasswdChallengeAnswer("");
		createdUserAccountBean.setOwner(ownerUserAccount);
		createdUserAccountBean.setRunWebservices(false);
		createdUserAccountBean.setPhone(mobile);
		createdUserAccountBean.setAccessCode(accessCode);

		Role r = Role.RESEARCHASSISTANT2;
		createdUserAccountBean = addActiveStudyRole(createdUserAccountBean, getStudy(studyOid).getId(), r, ownerUserAccount);
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
		udao = new UserAccountDAO(dataSource);
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

	private StudyBean getStudy(String oid) {
		sdao = new StudyDAO(dataSource);
		StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
		return studyBean;
	}

	private StudySubjectBean getStudySubject(String label, StudyBean study) {
		ssdao = new StudySubjectDAO(dataSource);
		StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByLabelAndStudy(label, study);
		return studySubjectBean;
	}

	private StudySubjectBean getStudySubject(String oid) {
		ssdao = new StudySubjectDAO(dataSource);
		StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByOid(oid);
		return studySubjectBean;
	}

	private Boolean isStudyDoesNotExist(String studyOid) {
		StudyBean studyBean = getStudy(studyOid);
		if (studyBean == null) {
			logger.info("***Study  Does Not Exist ***");
			System.out.println("***Study  Does Not Exist ***");
			return true;
		}
		return false;
	}

	private Boolean isStudySubjectDoesNotExist(StudySubjectBean studySubjectBean) {
		if (studySubjectBean == null || !studySubjectBean.isActive()) {
			logger.info("***Study Subject Does Not Exist OR the Study Subject is not associated with the Study_Oid in the URL   ***");
			System.out.println("***Study Subject Does Not Exist OR the Study Subject is not associated with the Study_Oid in the URL    ***");
			return true;
		}
		return false;
	}

	private Boolean isFistName2orLessCharacters(String fName) {
		if (fName.length() < 3) {
			logger.info("***     First Name length is less than 2 characters    ***");
			System.out.println("***     First Name length is less than 2 characters    ***");
			return true;
		}
		return false;
	}

	private Boolean isPhoneFieldIsNull(String mobile) {
		if (mobile.length() == 0) {
			logger.info("***     Phone # is a Required Field   ***");
			System.out.println("***     Phone # is a Required Field   ***");
			return true;
		}
		return false;
	}

	private Boolean isAccessCodeIsNull(String accessCode) {
		if (accessCode.length() == 0) {
			logger.info("***Access Code is a Required field and can't be null ***");
			System.out.println("***Access Code is a Required field and can't be null ***");
			return true;
		}
		return false;
	}

	private Boolean isAccessCodeExistInSystem(String accessCode) {
		UserAccountBean accessCodeAccountBean = getAccessCodeAccount(accessCode);
		if (accessCodeAccountBean.isActive()) {
			logger.info("***Access Code already Exist in the User Table ***");
			System.out.println("***Access Code already Exist in the User Table ***");
			return true;
		}
		return false;
	}

	private Boolean isCRCUserAccountDoesNotExist(String crcUserName) {
		UserAccountBean ownerUserAccount = getUserAccount(crcUserName);
		if (!ownerUserAccount.isActive()) {
			logger.info("***  CRC user acount does not Exist in the User Table ***");
			System.out.println("***  CRC user acount does not Exist in the User Table ***");
			return true;
		}
		return false;
	}

	private HashMap buildParticipantUserName(StudySubjectBean studySubjectBean) {
		HashMap<String, String> map = new HashMap();
		String studySubjectOid = studySubjectBean.getOid();
		Integer studyId = studySubjectBean.getStudyId();
		StudyBean study = (StudyBean) sdao.findByPK(studyId);
		Integer pStudyId = 0;

		if (!sdao.isAParent(studyId)) {
			StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
			pStudyId = parentStudy.getId();
			study = (StudyBean) sdao.findByPK(pStudyId);
		}

		String pUserName = study.getOid() + "." + studySubjectOid;
		map.put("pUserName", pUserName);
		map.put("studyId", studyId.toString());
		map.put("pStudyId", pStudyId.toString());
		map.put("studySubjectOid", studySubjectOid);

		System.out.println(pUserName);
		return map;
	}

	private Boolean doesCRCNotHaveStudyAccessRole(String crcUserName, Integer studyId, Integer pStudyId) {
		boolean found = false;
		ArrayList<StudyUserRoleBean> studyUserRoleBeans = (ArrayList<StudyUserRoleBean>) udao.findAllRolesByUserName(crcUserName);
		for (StudyUserRoleBean studyUserRoleBean : studyUserRoleBeans) {
			System.out.println(studyUserRoleBean.getStudyId());
			System.out.println("-------------");
			System.out.println("     " + studyId);
			System.out.println("     " + pStudyId);
			System.out.println(studyUserRoleBean.getRoleName());
			System.out.println("-------------");
			if ((studyUserRoleBean.getStudyId() == studyId || studyUserRoleBean.getStudyId() == pStudyId)
					&& (studyUserRoleBean.getRoleName().equals("ra") || studyUserRoleBean.getRoleName().equals("ra2"))) {
				found = true;
				break;
			}
		}
		if (!found) {
			logger.info("*** CRC Does not have access to the study/site OR CRC Does not have 'Data Entry Person' role ***");
			System.out.println("*** CRC Does not have access to the study/site  OR CRC Does not have 'Data Entry Person' role  ***");
			return true;
		}
		return false;
	}

	private StudyBean getParentStudy(String studyOid) {
		StudyBean study = getStudy(studyOid);
		Integer studyId = study.getId();
		Integer pStudyId = 0;
		if (!sdao.isAParent(studyId)) {
			StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
			pStudyId = parentStudy.getId();
			study = (StudyBean) sdao.findByPK(pStudyId);
		}
		return study;
	}

	private boolean mayProceed(String studyOid , StudySubjectBean ssBean) throws Exception {
		boolean accessPermission = false;
		StudyBean study = getParentStudy(studyOid);
		StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
		StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(),"participantPortal");
		 participantPortalRegistrar=new ParticipantPortalRegistrar();
		String pManageStatus =participantPortalRegistrar.getRegistrationStatus(studyOid).toString();   // ACTIVE , PENDING , INACTIVE
		String participateStatus = pStatus.getValue().toString();         // enabled , disabled
		String studyStatus = study.getStatus().getName().toString();      // available , pending , frozen , locked
		logger.info("pManageStatus: "+ pManageStatus + "  participantStatus: " + participateStatus+ "   studyStatus: " + studyStatus + "  studySubjectStatus: "+ssBean.getStatus().getName());
		System.out.println("pManageStatus: "+ pManageStatus + "  participantStatus: " + participateStatus+ "   studyStatus: " + studyStatus + "  studySubjectStatus: "+ssBean.getStatus().getName());
		if (participateStatus.equals("enabled") && studyStatus.equals("available") && pManageStatus.equals("ACTIVE") && ssBean.getStatus()==Status.AVAILABLE) {
			accessPermission = true;
		}
		
		return accessPermission;
	}

	private boolean mayProceed(String studyOid ) throws Exception {
		boolean accessPermission = false;
		StudyBean study = getParentStudy(studyOid);
		StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
		StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(),"participantPortal");
		participantPortalRegistrar = new ParticipantPortalRegistrar();
		String pManageStatus =participantPortalRegistrar.getRegistrationStatus(studyOid).toString();   // ACTIVE , PENDING , INACTIVE
		String participateStatus = pStatus.getValue().toString();         // enabled , disabled
		String studyStatus = study.getStatus().getName().toString();      // available , pending , frozen , locked
		System.out.println ("pManageStatus: "+ pManageStatus + "  participantStatus: " + participateStatus+ "   studyStatus: " + studyStatus);
		logger.info("pManageStatus: "+ pManageStatus + "  participantStatus: " + participateStatus+ "   studyStatus: " + studyStatus);
		if (participateStatus.equals("enabled") && studyStatus.equals("available") && pManageStatus.equals("ACTIVE") ) {
			accessPermission = true;
		}
		
		return accessPermission;
	}

}
