package org.akaza.openclinica.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.service.crfdata.BeanPropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * Servlet for creating a user account.
 *
 * @author ssachs
 */
public class PformSubmissionService {

	public static String study_oid = "S_BL101";
	public static Integer studySubjectId = 10;
	public static Integer studyId = 4;
	public static Integer studyEventDefnId = 4;
	public static Integer studyEventOrdinal = 3;

	public static final String INPUT_USER_SOURCE = "userSource";
	public static final String INPUT_USERNAME = study_oid.trim() + studySubjectId;
	public static final String INPUT_FIRST_NAME = "particiapant";
	public static final String INPUT_LAST_NAME = "User";
	public static final String INPUT_EMAIL = "email";
	public static final String INPUT_INSTITUTION = "PFORM";
	public static final String INPUT_STUDY = "activeStudy";
	public static final String INPUT_ROLE = "role";
	public static final String INPUT_TYPE = "type";
	public static final String INPUT_DISPLAY_PWD = "displayPwd";
	public static final String INPUT_RUN_WEBSERVICES = "runWebServices";
	public static final String USER_ACCOUNT_NOTIFICATION = "notifyPassword";
//	 private static final logger logger = loggerFactory.getlogger(PformSubmissionService.class);
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());


	DataSource ds;

	UserAccountDAO udao;
	StudyDAO sdao;
	StudySubjectDAO ssdao;
	StudyEventDAO sedao;
	EventCRFDAO ecdao;
	CRFVersionDAO cvdao;
	ItemDataDAO iddao;
	ItemDAO idao;
	AuthoritiesDao authoritiesDao;
	ApplicationContext applicationContext;
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}



	public PformSubmissionService(DataSource ds , AuthoritiesDao authoritiesDao)
	{
		this.ds = ds;
	    this.authoritiesDao=authoritiesDao;
	}



	private UserAccountBean createUserAccount(UserAccountBean userAccountBean) throws Exception {
		UserAccountBean rootUserAccount = getUserAccount("root");

		UserAccountBean createdUserAccountBean = new UserAccountBean();

		createdUserAccountBean.setName(INPUT_USERNAME);
		createdUserAccountBean.setFirstName(INPUT_FIRST_NAME);
		createdUserAccountBean.setLastName(INPUT_LAST_NAME);
		createdUserAccountBean.setEmail(INPUT_EMAIL);
		createdUserAccountBean.setInstitutionalAffiliation(INPUT_INSTITUTION);
		createdUserAccountBean.setActiveStudyId(studyId);

		String password = null;
		String passwordHash = UserAccountBean.LDAP_PASSWORD;
		createdUserAccountBean.setPasswd(passwordHash);
		createdUserAccountBean.setPasswdTimestamp(null);
		createdUserAccountBean.setLastVisitDate(null);
		createdUserAccountBean.setActiveStudyId(studyId);
		createdUserAccountBean.setStatus(Status.DELETED);
		createdUserAccountBean.setPasswdChallengeQuestion("");
		createdUserAccountBean.setPasswdChallengeAnswer("");
		createdUserAccountBean.setPhone("");
		createdUserAccountBean.setOwner(rootUserAccount);
		createdUserAccountBean.setRunWebservices(false);

		Role r = Role.RESEARCHASSISTANT2;
		createdUserAccountBean = addActiveStudyRole(createdUserAccountBean, studyId, r, rootUserAccount);
		UserType type = UserType.get(2);
		createdUserAccountBean.addUserType(type);
		createdUserAccountBean = (UserAccountBean) udao.create(createdUserAccountBean);

	//	 AuthoritiesDao authoritiesDao1 = (AuthoritiesDao) getApplicationContext().getBean("authoritiesDao");
		 authoritiesDao.saveOrUpdate(new AuthoritiesBean(createdUserAccountBean.getName()));
		// System.out.println("username id: "+authoritiesDao.findById(3).getUsername());
    //     System.out.println("authorities current session:  "+authoritiesDao.getCurrentSession());
		return userAccountBean;
	}

	private UserAccountBean addActiveStudyRole(UserAccountBean createdUserAccountBean, int studyId, Role r, UserAccountBean rootUserAccount) {

		StudyUserRoleBean studyUserRole = new StudyUserRoleBean();
		studyUserRole.setStudyId(studyId);
		studyUserRole.setRoleName(r.getName());
		studyUserRole.setStatus(Status.AUTO_DELETED);
		studyUserRole.setOwner(rootUserAccount);
		createdUserAccountBean.addRole(studyUserRole);

		return createdUserAccountBean;
	}

	private UserAccountBean getUserAccount(String userName) {
		udao = new UserAccountDAO(ds);
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(userName);
		return userAccountBean;
	}

	private StudyEventBean getStudyEvent() {
		sedao = new StudyEventDAO(ds);
		StudyEventBean studyEventBean = (StudyEventBean) sedao.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectId,
				studyEventDefnId, studyEventOrdinal);
		return studyEventBean;
	}

	private StudySubjectBean getStudySubject() {
		ssdao = new StudySubjectDAO(ds);
		StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByPK(studySubjectId);
		return studySubjectBean;
	}

	private CRFVersionBean getCRFVersion(String crfVersionOid) {
		cvdao = new CRFVersionDAO(ds);
		CRFVersionBean crfVersionBean = (CRFVersionBean) cvdao.findByOid(crfVersionOid);
		return crfVersionBean;
	}

	private ArrayList<EventCRFBean> getEventCrf(String crfVersionOid) {
		ecdao = new EventCRFDAO(ds);
		ArrayList<EventCRFBean> eventCrfBeanList = ecdao.findByEventSubjectVersion(getStudyEvent(), getStudySubject(),
				getCRFVersion(crfVersionOid));
		return eventCrfBeanList;
	}

	private ArrayList<ItemDataBean> getItemDataRecords(int eventCRFId) {
		iddao = new ItemDataDAO(ds);
		ArrayList<ItemDataBean> itemDataBeanList = iddao.findAllByEventCRFId(eventCRFId);
		return itemDataBeanList;
	}

	public void saveProcess() throws Exception {
		UserAccountBean userAccountBean = getUserAccount(INPUT_USERNAME);
		System.out.println("------------------------------------------------");
		System.out.println("------------------------------------------------");

		if (!userAccountBean.isActive() &&  getStudySubject().isActive()) {		
			userAccountBean = createUserAccount(userAccountBean);
			System.out.println("New User Account is created");
			logger.info("***New User Account is created***");
		} else if (!getStudySubject().isActive()){
			System.out.println(" Study Subject Does not exist in the system ");
			logger.info("***Study Subject Does not exist in the system***");
		}else{	
			System.out.println(" User Account already exist in the system ");
			logger.info("***User Account already exist in the system***");

		}

		StudyEventBean studyEventBean = getStudyEvent();
		if (studyEventBean.isActive()) {
			readDownloadFile();
		} else {
			System.out.println("StudyEvent Does not exist... Throw Error Message");
			logger.info("***StudyEvent Does not exist... Throw Error Message***");
		}

	}

	private EventCRFBean createEventCRF(String crfVersionOid) {

		EventCRFBean ecBean = new EventCRFBean();
		ecBean.setAnnotations("");
		ecBean.setCreatedDate(new Date());
		ecBean.setCRFVersionId(getCRFVersion(crfVersionOid).getId());
		ecBean.setInterviewerName("");
		ecBean.setDateInterviewed(null);
		ecBean.setOwner(getUserAccount(INPUT_USERNAME));
		ecBean.setStatus(Status.AVAILABLE);
		ecBean.setCompletionStatusId(1);
		ecBean.setStudySubjectId(getStudySubject().getId());
		ecBean.setStudyEventId(getStudyEvent().getId());
		ecBean.setValidateString("");
		ecBean.setValidatorAnnotations("");
        ecBean.setUpdater(getUserAccount(INPUT_USERNAME));
        ecBean.setUpdatedDate(new Date());
        
		ecBean = (EventCRFBean) ecdao.create(ecBean);
		ecBean = (EventCRFBean) ecdao.update(ecBean);
		 logger.debug("*********CREATED EVENT CRF");
         
		return ecBean;
	}

	private EventCRFBean updateEventCRF(EventCRFBean ecBean) {

        ecBean.setUpdater(getUserAccount(INPUT_USERNAME));
        ecBean.setUpdatedDate(new Date());
        
		ecBean = (EventCRFBean) ecdao.update(ecBean);
		 logger.debug("*********UPDATED EVENT CRF");
         
		return ecBean;
	}
	
	
	private void createItemData(String itemOID, String itemValue, EventCRFBean eventCrfBean, String crfVersionOID) {
		idao = new ItemDAO(ds);
		System.out.println("item Oid:  " + itemOID + "   itemValue:  " + itemValue);
		logger.info("item Oid:  " + itemOID + "   itemValue:  " + itemValue);

		ArrayList<ItemBean> iBean = (ArrayList<ItemBean>) idao.findByOid(itemOID);

		ItemDataBean itemDataBean = new ItemDataBean();

		itemDataBean.setItemId(iBean.get(0).getId());
		itemDataBean.setEventCRFId(eventCrfBean.getId());
		itemDataBean.setValue(itemValue);
		itemDataBean.setCreatedDate(new Date());
		itemDataBean.setStatus(Status.AVAILABLE);
		itemDataBean.setOrdinal(1);
		itemDataBean.setOwner(getUserAccount(INPUT_USERNAME));
		iddao = new ItemDataDAO(ds);
		itemDataBean = (ItemDataBean) iddao.create(itemDataBean);
		 logger.debug("*********CREATED ITEM DATA Record");

	}

	private void readDownloadFile() throws Exception {
		HashMap<String, String> hashmap = new HashMap<String, String>();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse("enketoSubmittedData.xml");

		NodeList nodeList = doc.getElementsByTagName("instance");

		for (int i = 0; i < nodeList.getLength(); i++) {

			Node node = nodeList.item(i);

			if (node instanceof Element) {

				NodeList childNodes = node.getChildNodes();
				for (int j = 1; j < childNodes.getLength(); j = j + 2) {
					Node cnode = childNodes.item(j);
					String crfVersionOID = cnode.getNodeName().trim();
					System.out.println("crf_version_ :  " + crfVersionOID);
					logger.info("***crf_version_ :  " + crfVersionOID+ " *** ");

					EventCRFBean eventCrfBean;
					if (getEventCrf(crfVersionOID).isEmpty()) {
						eventCrfBean = createEventCRF(crfVersionOID);
						System.out.println(" New EventCrf is created");
						logger.info("***New EventCrf is created***");

					} else {
						eventCrfBean = getEventCrf(crfVersionOID).get(0);
						eventCrfBean = updateEventCRF(eventCrfBean);
						System.out.println(" Existing EventCrf ");
						logger.info("***Existing EventCrf***");

					}

					if (getItemDataRecords(getEventCrf(crfVersionOID).get(0).getId()).isEmpty()) {

						if (cnode instanceof Element) {

							NodeList childNodes1 = cnode.getChildNodes();

							for (int k = 1; k < childNodes1.getLength(); k = k + 2) {
								Node cnode1 = childNodes1.item(k);
								String itemOID = cnode1.getNodeName().trim();
								String itemValue = cnode1.getTextContent().trim();

								createItemData(itemOID, itemValue, eventCrfBean, crfVersionOID);
							}
						}
					} else {
						System.out.println(" Existing Item Data , No New Item Data is added.  ");
						logger.info("***Existing Item Data , No New Item Data is added***");


					}
				}
			}
		}

	}

}
