package org.akaza.openclinica.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;

import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.login.UserDTO;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/userinfo")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class UserInfoController {

	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;

	@Autowired
	ServletContext context;


	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	public static final String INPUT_EMAIL = "";
	public static final String INPUT_INSTITUTION = "PFORM";
	UserAccountDAO udao;
	StudyDAO sdao;
	StudySubjectDAO ssdao;
	UserDTO uDTO;
	AuthoritiesDao authoritiesDao;
	ParticipantPortalRegistrar participantPortalRegistrar;
    private AuditUserLoginDao auditUserLoginDao;

	
    /**
     * @api {get} /pages/userinfo/study/:studyOid/crc Retrieve a user account - crc
     * @apiName getCrcAccountBySession
     * @apiPermission Module participate - enabled & admin
     * @apiVersion 3.12.2
     * @apiGroup User Account
     * @apiDescription Retrieves the crc user account associated with the current session.
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": " S_BL101",
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

    @RequestMapping(value = "/study/{studyOid}/crc", method = RequestMethod.GET)
    public ResponseEntity<UserDTO> getCrcAccountBySession(@PathVariable("studyOid") String studyOid) throws Exception {

        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principle = null;
        if (auth != null) principle = auth.getPrincipal();

        sdao = new StudyDAO(dataSource);
        StudyBean currentStudy = sdao.findByOid(studyOid);
        udao = new UserAccountDAO(dataSource);
        UserAccountBean userAccount = (UserAccountBean) udao.findByUserName(((User)principle).getUsername());

        uDTO = null;

        StudyBean parentStudy = getParentStudy(currentStudy.getOid());
        Integer pStudyId = parentStudy.getId();
        String oid = parentStudy.getOid();

        if (isStudyASiteLevelStudy(currentStudy.getOid()))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (!mayProceed(oid))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (isStudyDoesNotExist(oid))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (isCRCUserAccountDoesNotExist(userAccount.getName()))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (doesCRCNotHaveStudyAccessRole(userAccount.getName(), pStudyId))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        buildUserDTO(userAccount);
        return new ResponseEntity<UserDTO>(uDTO, headers,org.springframework.http.HttpStatus.OK);
    }


	public Boolean isCRCHasAccessToStudySubject(String studyOid, String crcUserName, String studySubjectId) {
		uDTO = null;
		System.out.println("I'm in getAccount4");
		StudyBean parentStudy = getParentStudy(studyOid);
		Integer pStudyId = parentStudy.getId();
		String oid = parentStudy.getOid();

		if (isStudySubjecAndCRCRolesMatch(studySubjectId, crcUserName, studyOid))
			return true;

		return false;
	}

    @RequestMapping(value = "/auditcrc", method = RequestMethod.POST)
    public ResponseEntity<HashMap> auditcrc(@RequestBody HashMap<String, String> requestMap) throws Exception {
       HashMap map = new HashMap();
       
       String crcUserName = requestMap.get("crcUserName");
       String studyOid = requestMap.get("studyOid");
       String studySubjectId = requestMap.get("studySubjectId");
       
       StudyBean parentStudy = getParentStudy(studyOid);
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


public Boolean isApiKeyExist(String uuid) {
    UserAccountDAO udao = new UserAccountDAO(dataSource);
    UserAccountBean uBean = (UserAccountBean) udao.findByApiKey(uuid);
    if (uBean == null || !uBean.isActive()) {
        return false;
    } else {
        return true;
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
    uDTO.setEmail(userAccountBean.getEmail());
    return uDTO;
}

private Map<String,Object> buildMap(UserAccountBean userAccountBean) {
    Map<String,Object> map = new HashMap<String,Object>();
    map.put("fName",userAccountBean.getFirstName());
    map.put("lName",userAccountBean.getLastName());
    map.put("mobile",userAccountBean.getPhone());
    map.put("userName",userAccountBean.getName());
    map.put("accessCode",userAccountBean.getAccessCode());
    map.put("password",userAccountBean.getPasswd());
    map.put("email",userAccountBean.getEmail());
    return map;
}

	private UserAccountBean getUserAccount(String userName) {
		udao = new UserAccountDAO(dataSource);
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(userName);
		return userAccountBean;
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

	private StudySubjectBean getStudySubject(String label, StudyBean study) {
		ssdao = new StudySubjectDAO(dataSource);
		StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByLabelAndStudy(label, study);
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

	private Boolean isStudyASiteLevelStudy(String studyOid) {
		StudyBean studyBean = getStudy(studyOid);
		if (studyBean.getParentStudyId() != 0) {
			logger.info("***Study provided in the URL is a Site study***");
			System.out.println("***Study provided in the URL is a Site study***");
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
		StudyBean study = getParentStudy(studyId);
		Integer pStudyId = study.getId();

		String pUserName = study.getOid() + "." + studySubjectOid;
		System.out.println("participate Username: " + pUserName);
		map.put("pUserName", pUserName);
		map.put("pStudyId", pStudyId.toString());
		map.put("studySubjectOid", studySubjectOid);

		return map;
	}

	private Boolean doesCRCNotHaveStudyAccessRole(String crcUserName, Integer pStudyId) {
		boolean found = false;
		ArrayList<StudyUserRoleBean> studyUserRoleBeans = (ArrayList<StudyUserRoleBean>) udao.findAllRolesByUserName(crcUserName);
		for (StudyUserRoleBean studyUserRoleBean : studyUserRoleBeans) {
			StudyBean study = getParentStudy(studyUserRoleBean.getStudyId());
			System.out.println("-------------");
			System.out.println("Study Id to compare: " + studyUserRoleBean.getStudyId());
			System.out.println("parent study Id to compare: " + study.getId());
			System.out.println("Parent Study Id    " + pStudyId);
			System.out.println("Role: " + studyUserRoleBean.getRoleName());
			System.out.println("Status :" + studyUserRoleBean.getStatus().getId());

			if ((study.getId() == pStudyId) && (studyUserRoleBean.getRoleName().equals("ra") || studyUserRoleBean.getRoleName().equals("ra2")) && studyUserRoleBean.getStatus().isAvailable()) {
				found = true;
				System.out.println("if found :" + found);
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

	private Boolean doesStudySubjecAndCRCRolesMatch(String crcUserName, Integer subjectStudyId) {
		boolean found = false;
		ArrayList<StudyUserRoleBean> studyUserRoleBeans = (ArrayList<StudyUserRoleBean>) udao.findAllRolesByUserName(crcUserName);
		for (StudyUserRoleBean studyUserRoleBean : studyUserRoleBeans) {
			System.out.println("-------------");
			System.out.println("StudySubject Study Id to compare to   " + subjectStudyId);

			if (studyUserRoleBean.getStudyId() == getParentStudy(subjectStudyId).getId()) {
				subjectStudyId = getParentStudy(subjectStudyId).getId();
				System.out.println("StudySubject Parent Study Id to compare to Overwritten    " + subjectStudyId);
			}

			System.out.println("CRC Study Id to compare to : " + studyUserRoleBean.getStudyId());
			System.out.println("Role: " + studyUserRoleBean.getRoleName());
			System.out.println("Status :" + studyUserRoleBean.getStatus().getId());

			if ((studyUserRoleBean.getStudyId() == subjectStudyId) && (studyUserRoleBean.getRoleName().equals("ra") || studyUserRoleBean.getRoleName().equals("ra2"))
					&& studyUserRoleBean.getStatus().isAvailable()) {
				found = true;
				System.out.println("if found :" + found);
				break;
			}
		}
		if (!found) {
			logger.info("*** CRC Role does not match with StudySubject assignment ***");
			System.out.println("*** CRC Role does not match with StudySubject assignment ***");
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

		StudyBean parentStudy = getParentStudy(studyOid);
		Integer studyIdFromStudyOid = parentStudy.getId();
		StudySubjectBean studySubjectBean = getStudySubject(studySubjectId, parentStudy);
		Integer studyIdFromStudySubjectId = studySubjectBean.getStudyId();

		System.out.println("    ------------------     ");
		System.out.println("studyIdFromStudyOid :  " + studyIdFromStudyOid);

		System.out.println("studySubjectId:  " + studySubjectId);
		System.out.println("studyIdFromStudySubjectId:  " + studyIdFromStudySubjectId);

		return doesStudySubjecAndCRCRolesMatch(crcUserName, studyIdFromStudySubjectId);

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
		logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus + "   siteStatus: " + siteStatus);
		if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && siteStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE")) {
			accessPermission = true;
		}

		return accessPermission;
	}

}
