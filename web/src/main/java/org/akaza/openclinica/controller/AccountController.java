package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.admin.AuditBean;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
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
import org.akaza.openclinica.domain.user.AuthoritiesBean;
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
@RequestMapping(value = "/accounts")
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
	String message;

	/**
	 *
	 * @param studyOid
	 * @return
	 * @throws Exception
	 */
	

	
	
	@RequestMapping(value = "/study/{studyOid}/studysubjectoid/{studySubjectOid}/fname/{fName}/lname/{lName}/mobile/{mobile}/login/{loginName}/crc/{crcUserName}", method = RequestMethod.GET)
	public @ResponseBody UserAccountBean getEvent(@PathVariable("studyOid") String studyOid, @PathVariable("studySubjectOid") String studySubjectOid, @PathVariable("fName") String fName,
			@PathVariable("lName") String lName, @PathVariable("mobile") String mobile, @PathVariable("loginName") String loginName,@PathVariable("crcUserName") String crcUserName) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
  System.out.println("******************     You are in the Rest Service   *****************");
	System.out.println(studyOid + "  " +studySubjectOid + "  "+ fName +" "+ lName );	
  
  
  String userName = studyOid + "." + studySubjectOid;
		UserAccountBean uBean = null;

		StudySubjectBean studySubjectBean = getStudySubject(studySubjectOid);
		if (studySubjectBean != null) {

			UserAccountBean loginAccountBean = getLoginAccount(loginName);
			if (!loginAccountBean.isActive()) {

				UserAccountBean userAccountBean = getUserAccount(userName);

				if (!userAccountBean.isActive() && studySubjectBean.isActive()) {
					uBean = buildUserAccount(studyOid, studySubjectOid, fName, lName, mobile, loginName, crcUserName);
					createUserAccount(uBean);
					logger.info("***New User Account is created***");
				} else {
					uBean = buildUserAccount(studyOid, studySubjectOid, fName, lName, mobile, loginName, crcUserName);
					 uBean.setId(getUserAccount(uBean.getName()).getId());
					 uBean.setUpdater(uBean.getOwner());
					updateUserAccount(uBean);
					logger.info("***User Account already exist in the system and is Updated ***");
					System.out.println("***User Account already exist in the system and is Updated ***");
				}

			} else {
				logger.info("***LoginName already Exist in the User Table ***");
				System.out.println("***LoginName already Exist in the User Table ***");
				message = "LoginName already Exist in the User Table";
				return uBean;
			}

		} else {
			logger.info("***Study Subject Does Not Exist ***");
			message = "Study Subject Does Not Exist";
			return uBean;
		}

		return uBean;
	}

	
	
	
	private UserAccountBean buildUserAccount(String studyOid, String studySubjectOid, String fName, String lName, String mobile, String loginName, String crcUserName) throws Exception {
		UserAccountBean ownerUserAccount = getUserAccount(crcUserName);
		UserAccountBean createdUserAccountBean = new UserAccountBean();

		createdUserAccountBean.setName(studyOid + "." + studySubjectOid);
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
		createdUserAccountBean.setLoginName(loginName);

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

	private UserAccountBean getLoginAccount(String loginName) {
		udao = new UserAccountDAO(dataSource);
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByLoginName(loginName);
		return userAccountBean;
	}

	private StudyBean getStudy(String oid) {
		sdao = new StudyDAO(dataSource);
		StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
		return studyBean;
	}

	private StudySubjectBean getStudySubject(String oid) {
		ssdao = new StudySubjectDAO(dataSource);
		StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByOid(oid);
		return studySubjectBean;
	}

    @RequestMapping(value = "/study/{studyOid}/user/{username}", method = RequestMethod.GET)
    public @ResponseBody UserAccountBean getUser(@PathVariable("studyOid") String studyOid, @PathVariable("username") String username)
            throws Exception {
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
