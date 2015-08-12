package org.akaza.openclinica.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.login.UserDTO;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/api")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)

public class UserAccountService {

	
    DataSource dataSource;
	
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	UserAccountDAO udao;
	StudyDAO sdao;
	StudySubjectDAO ssdao;
	UserDTO uDTO;

	AuthoritiesDao authoritiesDao;

	public UserAccountService(DataSource dataSource, AuthoritiesDao authoritiesDao) {
		this.dataSource = dataSource;
		this.authoritiesDao = authoritiesDao;
	}


	@RequestMapping(value = "/v1/createorupdateuseraccount", method = RequestMethod.POST)
	public ResponseEntity<UserDTO> createOrUpdateAccount(HttpServletRequest request,@RequestBody HashMap<String, String> map) throws Exception {
		uDTO = null;
		System.out.println("I'm in createOrUpdateAccount");

		StudyBean parentStudy = getParentStudy(map.get("studyOid"));
		String oid = parentStudy.getOid();

	//	String apiKey= map.get("apiKey");
		String username = map.get("username");
		String fName = map.get("fName");
		String lName = map.get("lName");
		String institution = map.get("institution");
		String email = map.get("email");
        String studyName = map.get("study_name");
        String roleName = map.get("role_name");
        String userType = map.get("user_type");
        String authorizeSoap = map.get("authorize_soap");    // true  or false
        
        // generate password
        String password ="" ;  //generate
        String apiKey ="";    //generate
        
        StudyBean study = getStudyByName(studyName);
		if (study.getParentStudyId() == 0) {
			// is a parent study , should apply parent Roles
            if (roleName.equals(Role.INVALID)){
			// DataManager , StudyDirector , Data Specialist , Monitor , Data Entry Person
            	
            	
		} else {
			//is a site study  ,  should apply site Roles
			// Investigator ,  Monitor ,Clinical Research Coordinator Data Entry Person
		}
        
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		System.out.println("******************     You are in the Rest Service   *****************");

		UserAccountBean uBean = null;
	   //  UserAccountBean ownerUserAccount = getUserAccountByApiKey(apiKey);
	     UserAccountBean ownerUserAccount = (UserAccountBean) request.getSession().getAttribute("userBean");
         if(!ownerUserAccount.isActive())
 			return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.BAD_REQUEST);

		// build UserName

	
        uBean = buildUserAccount(username, fName, lName, password ,institution,studyName, ownerUserAccount, email , apiKey);
		
        UserAccountBean userAccountBean = getUserAccount(username);
		if (!userAccountBean.isActive()) {
			createUserAccount(uBean);
			uBean.setUpdater(uBean.getOwner());
			updateUserAccount(uBean);
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


	private UserAccountBean buildUserAccount(String username, String fName, String lName, String password,String institution,String name, UserAccountBean ownerUserAccount,
			String email, String apiKey) throws Exception {

		UserAccountBean createdUserAccountBean = new UserAccountBean();

		createdUserAccountBean.setName(username);
		createdUserAccountBean.setFirstName(fName);
		createdUserAccountBean.setLastName(lName);
		createdUserAccountBean.setEmail(username);
		createdUserAccountBean.setInstitutionalAffiliation(institution);
		createdUserAccountBean.setLastVisitDate(null);
		createdUserAccountBean.setActiveStudyId(getStudyByName(name).getId());
		createdUserAccountBean.setPasswdTimestamp(null);
		createdUserAccountBean.setPasswdChallengeQuestion("");
		createdUserAccountBean.setPasswdChallengeAnswer("");
		createdUserAccountBean.setOwner(ownerUserAccount);
		createdUserAccountBean.setRunWebservices(false);
		createdUserAccountBean.setPhone("");
		createdUserAccountBean.setAccessCode("");
		createdUserAccountBean.setPasswd(password);
		createdUserAccountBean.setEmail(email);
		createdUserAccountBean.setEnableApiKey(true);
		createdUserAccountBean.setApiKey(apiKey);

		Role r = Role.RESEARCHASSISTANT2;
		createdUserAccountBean = addActiveStudyRole(createdUserAccountBean, getStudyByName(name).getId(), r, ownerUserAccount);
		UserType type = UserType.get(2);
		createdUserAccountBean.addUserType(type);

		authoritiesDao.saveOrUpdate(new AuthoritiesBean(createdUserAccountBean.getName()));

		return createdUserAccountBean;
	}

	private void createUserAccount(UserAccountBean userAccountBean) {
		udao.create(userAccountBean);
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

	private StudyBean getParentStudy(Integer studyId) {
		StudyBean study = getStudy(studyId);
		if (study.getParentStudyId() == 0) {
			return study;
		} else {
			StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
			return parentStudy;
		}

	}
	
	private StudyBean getStudyByName(String name) {
		sdao = new StudyDAO(dataSource);
		StudyBean studyBean = (StudyBean) sdao.findByName(name);
		return studyBean;
	}

	private StudyBean getStudy(String oid) {
		sdao = new StudyDAO(dataSource);
		StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
		return studyBean;
	}

	private StudyBean getStudy(Integer id) {
		sdao = new StudyDAO(dataSource);
		StudyBean studyBean = (StudyBean) sdao.findByPK(id);
		return studyBean;
	}
	private UserAccountBean addActiveStudyRole(UserAccountBean createdUserAccountBean, int studyId, Role r, UserAccountBean ownerUserAccount) {
		StudyUserRoleBean studyUserRole = new StudyUserRoleBean();
		studyUserRole.setStudyId(studyId);
		studyUserRole.setRoleName(r.getName());
		studyUserRole.setStatus(Status.AVAILABLE);
		studyUserRole.setOwner(ownerUserAccount);
		createdUserAccountBean.addRole(studyUserRole);
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
	
	private UserAccountBean getUserAccountByApiKey(String apiKey) {
		udao = new UserAccountDAO(dataSource);
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByApiKey(apiKey);
		return userAccountBean;
	}

	private void updateUserAccount(UserAccountBean userAccountBean) {
		udao.update(userAccountBean);
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
	
}
