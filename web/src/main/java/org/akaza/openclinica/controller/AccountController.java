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
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.web.pform.PFormCache;
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
	public static final String INPUT_EMAIL = "email";
	public static final String INPUT_INSTITUTION = "PFORM";
	UserAccountDAO udao;
	StudyDAO sdao;
	StudySubjectDAO ssdao;
	UserDTO uDTO;


	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<UserDTO> getUserInfo(@RequestBody HashMap<String, String> map) throws Exception {
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

		// Verify Study if Exist !!
		StudyBean studyBean = getStudy(studyOid);
		if (studyBean == null) {
			logger.info("***Study  Does Not Exist ***");
			System.out.println("***Study  Does Not Exist ***");
	        return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		}

		// Verify Study Subject Exist !!
		StudySubjectBean studySubjectBean = getStudySubject(studySubjectId, studyBean);
		if (studySubjectBean == null || !studySubjectBean.isActive()) {
			logger.info("***Study Subject Does Not Exist OR the Study Subject is not associated with the Study_Oid in the URL   ***");
			System.out.println("***Study Subject Does Not Exist OR the Study Subject is not associated with the Study_Oid in the URL    ***");
	        return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		}

		// First Name is a Required field and should have min 2 characters
		if (fName.length() < 3) {
			logger.info("***     First Name length is less than 2 characters    ***");
			System.out.println("***     First Name length is less than 2 characters    ***");
	        return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		}

		// Mobile Phone number is a Required field
		if (mobile.length() == 0) {
			logger.info("***     Phone # is a Required Field   ***");
			System.out.println("***     Phone # is a Required Field   ***");
	        return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		}

		// Verify Access Code already exist in table
		UserAccountBean loginAccountBean = getAccessCodeAccount(accessCode);
		if (loginAccountBean.isActive()) {
			logger.info("***Access Code already Exist in the User Table ***");
			System.out.println("***Access Code already Exist in the User Table ***");
	        return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		}

		// Build pUserName
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
		System.out.println(pUserName);

		// Verfiy if CRC user account exists
		UserAccountBean ownerUserAccount = getUserAccount(crcUserName);
		if (!ownerUserAccount.isActive()) {
			logger.info("***  CRC user acount does not Exist in the User Table ***");
			System.out.println("***  CRC user acount does not Exist in the User Table ***");
	        return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
		}

		// Verify CRC_user has the appropriate role as 'data entry person'or 'data entry person 2' and have access to the specific study/site
		// This also verifies that fact that the CRC and the Participant both have access to same study/site

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
	        return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		}

		// Participant user account create (if does not exist in user table) or Update(if exist in user table)
		uBean = buildUserAccount(studyOid, studySubjectOid, fName, lName, mobile, accessCode, ownerUserAccount, pUserName);
		UserAccountBean participantUserAccountBean = getUserAccount(pUserName);
		if (!participantUserAccountBean.isActive()) {
			createUserAccount(uBean);
			logger.info("***New User Account is created***");
			System.out.println("***New User Account is created***");
			  uDTO =buildUserDTO(uBean);
	        return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);

		} else {
			uBean.setId(getUserAccount(uBean.getName()).getId());
			uBean.setUpdater(uBean.getOwner());
			updateUserAccount(uBean);
			logger.info("***User Account already exist in the system and data is been Updated ***");
			System.out.println("***User Account already exist in the system and data is been Updated ***");
			  uDTO =buildUserDTO(uBean);
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
		createdUserAccountBean.setStatus(Status.DELETED);
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

		return createdUserAccountBean;
	}

	private void createUserAccount(UserAccountBean userAccountBean) {
		udao.create(userAccountBean);
	}

	private void updateUserAccount(UserAccountBean userAccountBean) {
		udao.update(userAccountBean);

	}

	private UserAccountBean addActiveStudyRole(UserAccountBean createdUserAccountBean, int studyId, Role r, UserAccountBean ownerUserAccount) {
		StudyUserRoleBean studyUserRole = new StudyUserRoleBean();
		studyUserRole.setStudyId(studyId);
		studyUserRole.setRoleName(r.getName());
		studyUserRole.setStatus(Status.AUTO_DELETED);
		studyUserRole.setOwner(ownerUserAccount);
		createdUserAccountBean.addRole(studyUserRole);
		return createdUserAccountBean;
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

	@RequestMapping(value = "/study/{studyOid}/user/{username}", method = RequestMethod.GET)
	public @ResponseBody UserAccountBean getUser(@PathVariable("studyOid") String studyOid, @PathVariable("username") String username) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));

		return getUserAccount(username);
	}

	private String generateXmlFromObj(Class clazz, ODM odm) throws Exception {

		JAXBContext context = JAXBContext.newInstance(clazz);

		Marshaller m = context.createMarshaller();
		StringWriter w = new StringWriter();

		m.marshal(odm, w);
		return w.toString();
	}

}
