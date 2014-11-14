package org.akaza.openclinica.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.odmbeans.StudyEventDefBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.service.crfdata.BeanPropertyService;
import org.apache.log4j.spi.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Servlet for creating a user account.
 *
 * @author ssachs
 */
public class PformSubmissionService {

	public static String studySubjectOid = "SS_17";
	public static String studyEventDefnOid = "SE_NEWEVENT";
	public static Integer studyEventOrdinal = 1;

	public static final String INPUT_USER_SOURCE = "userSource";
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
	// private static final logger logger =
	// loggerFactory.getlogger(PformSubmissionService.class);
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	String INPUT_USERNAME;
	Integer studyId;
	Integer studySubjectId;
	Integer studyEventDefnId;

	DataSource ds;

	EventDefinitionCRFDAO edcdao;
	UserAccountDAO udao;
	StudyDAO sdao;
	StudyEventDefinitionDAO seddao;
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

	/*
	 * public PformSubmissionService(DataSource ds) { StudySubjectBean
	 * studySubjectBean = getStudySubject(studySubjectOid); this.INPUT_USERNAME
	 * = studySubjectBean.getStudy().getOid() + studySubjectOid; this.studyId =
	 * studySubjectBean.getStudyId(); this.studySubjectId =
	 * studySubjectBean.getId();
	 * 
	 * StudyEventDefinitionBean studyEventDefinitionBean =
	 * getStudyEventDefn(studyEventDefnOid); this.studyEventDefnId =
	 * studyEventDefinitionBean.getId(); this.ds = ds; }
	 */
	public PformSubmissionService(DataSource ds, AuthoritiesDao authoritiesDao) {
		this.ds = ds;
		this.authoritiesDao = authoritiesDao;

	}

	private StudySubjectBean getEntityVariables() {
		StudySubjectBean studySubjectBean = getStudySubject(studySubjectOid);

	       if (studySubjectBean !=null) {
          StudyBean studyBean = getStudy(studySubjectBean.getStudyId());
		if (studyBean.getParentStudyId() > 0)
			studyBean = getStudy(studyBean.getParentStudyId());

		this.INPUT_USERNAME = studyBean.getOid() + studySubjectOid;
		this.studyId = studySubjectBean.getStudyId();
		this.studySubjectId = studySubjectBean.getId();

		StudyEventDefinitionBean studyEventDefinitionBean = getStudyEventDefn(studyEventDefnOid);
		this.studyEventDefnId = studyEventDefinitionBean.getId();
	   }
	   return studySubjectBean;    
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

		authoritiesDao.saveOrUpdate(new AuthoritiesBean(createdUserAccountBean.getName()));
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

	private int getEventCrfCompletedInAStudyEventCount(StudyEventBean seBean) {
		int count = 0;
		count = ecdao.findAllByStudyEventAndStatus(seBean, Status.UNAVAILABLE).size();
		return count;
	}

	private int getEventDefCrfInAStudyEventDefCount() {
		int count = 0;
		edcdao = new EventDefinitionCRFDAO(ds);
		count = edcdao.findAllByEventDefinitionId(studyEventDefnId).size();
		return count;
	}

	private StudyBean getStudy(Integer id) {
		sdao = new StudyDAO(ds);
		StudyBean studyBean = (StudyBean) sdao.findByPK(id);
		return studyBean;
	}

	private StudySubjectBean getStudySubject(String oid) {
		ssdao = new StudySubjectDAO(ds);
		StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByOid(oid);
		return studySubjectBean;
	}

	private UserAccountBean getUserAccount(String userName) {
		udao = new UserAccountDAO(ds);
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(userName);
		return userAccountBean;
	}

	private StudyEventDefinitionBean getStudyEventDefn(String oid) {
		seddao = new StudyEventDefinitionDAO(ds);
		StudyEventDefinitionBean studyEventDefinitionBean = (StudyEventDefinitionBean) seddao.findByOid(oid);
		return studyEventDefinitionBean;
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

	public Errors saveProcess(String body) throws Exception {
	    Errors errors = null;
		StudySubjectBean studySubjectBean=getEntityVariables();
		if (studySubjectBean==null) {
			System.out.println(" Study Subject Does not exist in the system ");
			errors = instanciateError();
			errors.reject("Study Subject Does not exist in the system ");
           return errors;	
		}
		
		UserAccountBean userAccountBean = getUserAccount(INPUT_USERNAME);
		System.out.println("------------------------------------------------");
		System.out.println("------------------------------------------------");
		if (!userAccountBean.isActive() && getStudySubject().isActive()) {
			userAccountBean = createUserAccount(userAccountBean);
			System.out.println("New User Account is created");
			logger.info("***New User Account is created***");
		} else if (!getStudySubject().isActive()) {
			System.out.println(" Study Subject Does not exist in the system ");
			logger.info("***Study Subject Does not exist in the system***");
			errors = instanciateError();
			errors.reject("Study Subject Does not exist in the system ");
			return errors;
		
		} else {
			System.out.println(" User Account already exist in the system ");
			logger.info("***User Account already exist in the system***");

		}

		StudyEventBean studyEventBean = getStudyEvent();
		if (studyEventBean.isActive()) {
			errors = readDownloadFile(body);
		} else {
			System.out.println("StudyEvent Does not exist... Throw Error Message");
			logger.info("***StudyEvent Does not exist... Throw Error Message***");
			errors = instanciateError();
			errors.reject("StudyEvent Does not exist");
			return errors;
		}
		return errors;
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
		logger.debug("*********CREATED EVENT CRF");

		return ecBean;
	}

	private StudyEventBean updateStudyEvent(StudyEventBean seBean, SubjectEventStatus status) {

		seBean.setUpdater(getUserAccount(INPUT_USERNAME));
		seBean.setUpdatedDate(new Date());
		seBean.setSubjectEventStatus(status);

		seBean = (StudyEventBean) sedao.update(seBean);
		logger.debug("*********UPDATED STUDY EVENT ");

		return seBean;
	}

	private EventCRFBean updateEventCRF(EventCRFBean ecBean) {

		ecBean.setUpdater(getUserAccount(INPUT_USERNAME));
		ecBean.setUpdatedDate(new Date());
		ecBean.setStatus(Status.UNAVAILABLE);

		ecBean = (EventCRFBean) ecdao.update(ecBean);
		logger.debug("*********UPDATED EVENT CRF");

		return ecBean;
	}

	private ItemDataBean createItemData(ItemBean itemBean, String itemValue, EventCRFBean eventCrfBean) {

		System.out.println("item Oid:  " + itemBean.getOid() + "   itemValue:  " + itemValue);
		logger.info("item Oid:  " + itemBean.getOid() + "   itemValue:  " + itemValue);

		ItemDataBean itemDataBean = new ItemDataBean();
		itemDataBean.setItemId(itemBean.getId());
		itemDataBean.setEventCRFId(eventCrfBean.getId());
		itemDataBean.setValue(itemValue);
		itemDataBean.setCreatedDate(new Date());
		itemDataBean.setStatus(Status.UNAVAILABLE);
		itemDataBean.setOrdinal(1);
		itemDataBean.setOwner(getUserAccount(INPUT_USERNAME));

		return itemDataBean;
	}

	private Errors instanciateError() {
		DataBinder dataBinder = new DataBinder(null);
		Errors errors = dataBinder.getBindingResult();
		return errors;
	}
	
	
	private Errors validateItemData(ItemDataBean itemDataBean, ItemBean itemBean) {
		ItemItemDataContainer container = new ItemItemDataContainer(itemBean, itemDataBean);
		DataBinder dataBinder = new DataBinder(container);
		Errors errors = dataBinder.getBindingResult();
		PformValidator pformValidator = new PformValidator();
		pformValidator.validate(container, errors);
		return errors;
	}

	private Errors readDownloadFile(String body) throws Exception {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(body));
		Document doc = db.parse(is);
		Errors errors = null;

		NodeList nodeList = doc.getElementsByTagName("instance");
		for (int i = 0; i < nodeList.getLength(); i = i + 1) {

			Node node = nodeList.item(i);

			if (node instanceof Element) {

				NodeList childNodes = node.getChildNodes();
				for (int j = 0; j < childNodes.getLength(); j = j + 2) {
					Node cnode = childNodes.item(j);
					String crfVersionOID = cnode.getNodeName().trim();
					System.out.println("crf_version_ :  " + crfVersionOID);
					logger.info("***crf_version_ :  " + crfVersionOID + " *** ");

					EventCRFBean eventCrfBean;
					if (getEventCrf(crfVersionOID).isEmpty()) {
						eventCrfBean = createEventCRF(crfVersionOID);
						System.out.println(" New EventCrf is created");
						logger.info("***New EventCrf is created***");

					} else {
						eventCrfBean = getEventCrf(crfVersionOID).get(0);
						// eventCrfBean = updateEventCRF(eventCrfBean);
						System.out.println(" Existing EventCrf ");
						logger.info("***Existing EventCrf***");

					}

					if (getItemDataRecords(getEventCrf(crfVersionOID).get(0).getId()).isEmpty()) {

						if (cnode instanceof Element) {

							NodeList childNodes1 = cnode.getChildNodes();
							ArrayList<ItemDataBean> itemDataBeanList = new ArrayList<ItemDataBean>();
							iddao = new ItemDataDAO(ds);
							// Errors errors = null;

							for (int k = 1; k < childNodes1.getLength(); k = k + 2) {
								Node cnode1 = childNodes1.item(k);
								String itemOID = cnode1.getNodeName().trim();
								String itemValue = cnode1.getTextContent().trim();

								idao = new ItemDAO(ds);
								ArrayList<ItemBean> itemBeanList = (ArrayList<ItemBean>) idao.findByOid(itemOID);
								ItemBean itemBean = itemBeanList.get(0);

								ItemDataBean itemDataBean = createItemData(itemBean, itemValue, eventCrfBean);
								errors = validateItemData(itemDataBean, itemBean);
								if (errors.hasErrors()) {
									break;
								} else {
									itemDataBeanList.add(itemDataBean);
								}
							}
							if (!errors.hasErrors()) {
								for (ItemDataBean itemDataBean : itemDataBeanList) {
									iddao.create(itemDataBean);
									eventCrfBean = updateEventCRF(eventCrfBean);

									if (getEventCrfCompletedInAStudyEventCount(getStudyEvent()) == getEventDefCrfInAStudyEventDefCount()) {
										updateStudyEvent(getStudyEvent(), SubjectEventStatus.COMPLETED);
									} else {
										updateStudyEvent(getStudyEvent(), SubjectEventStatus.DATA_ENTRY_STARTED);
									}
								}
							} else {
								break;

							}
						}
					} else {
						
						System.out.println(" Existing Item Data , No New Item Data is added.  ");
						logger.info("***Existing Item Data , No New Item Data is added***");
    					errors = instanciateError();
						errors.reject("Existing Item Data , No New Item Data is added");
					}
				}
			}

		}
		return errors;
	}

}
