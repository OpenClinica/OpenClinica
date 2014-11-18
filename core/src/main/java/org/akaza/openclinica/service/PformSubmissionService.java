package org.akaza.openclinica.service;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akaza.openclinica.bean.core.ResponseType;
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
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseSetBean;
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
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
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

	public static String studySubjectOid = "SS_30";
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
	ItemFormMetadataDAO ifmdao;
	AuthoritiesDao authoritiesDao;
	

	public PformSubmissionService(DataSource ds, AuthoritiesDao authoritiesDao) {
		this.ds = ds;
		this.authoritiesDao = authoritiesDao;
	}

	// bunch of Entity variable initialization happens here
	public StudySubjectBean getEntityVariables() {
		StudySubjectBean studySubjectBean = getStudySubject(studySubjectOid);

		if (studySubjectBean != null) {
			StudyBean studyBean = getStudy(studySubjectBean.getStudyId());
			if (studyBean.getParentStudyId() > 0)
				studyBean = getStudy(studyBean.getParentStudyId());

			this.setINPUT_USERNAME(studyBean.getOid() + "." + studySubjectOid);
			this.setStudyId(studySubjectBean.getStudyId());
			this.setStudySubjectId(studySubjectBean.getId());
			StudyEventDefinitionBean studyEventDefinitionBean = getStudyEventDefn(studyEventDefnOid);
			this.setStudyEventDefnId(studyEventDefinitionBean.getId());
		}
		return studySubjectBean;
	}

	// Create User Account , insert in User Account table , also insert records
	// in Authorities table
	private UserAccountBean createUserAccount(UserAccountBean userAccountBean) throws Exception {
		UserAccountBean rootUserAccount = getUserAccount("root");
		UserAccountBean createdUserAccountBean = new UserAccountBean();
		createdUserAccountBean.setName(getINPUT_USERNAME());
		createdUserAccountBean.setFirstName(INPUT_FIRST_NAME);
		createdUserAccountBean.setLastName(INPUT_LAST_NAME);
		createdUserAccountBean.setEmail(INPUT_EMAIL);
		createdUserAccountBean.setInstitutionalAffiliation(INPUT_INSTITUTION);
		createdUserAccountBean.setActiveStudyId(getStudyId());
		String passwordHash = UserAccountBean.LDAP_PASSWORD;
		createdUserAccountBean.setPasswd(passwordHash);
		createdUserAccountBean.setPasswdTimestamp(null);
		createdUserAccountBean.setLastVisitDate(null);
		createdUserAccountBean.setActiveStudyId(getStudyId());
		createdUserAccountBean.setStatus(Status.DELETED);
		createdUserAccountBean.setPasswdChallengeQuestion("");
		createdUserAccountBean.setPasswdChallengeAnswer("");
		createdUserAccountBean.setPhone("");
		createdUserAccountBean.setOwner(rootUserAccount);
		createdUserAccountBean.setRunWebservices(false);
		Role r = Role.RESEARCHASSISTANT2;
		createdUserAccountBean = addActiveStudyRole(createdUserAccountBean, getStudyId(), r, rootUserAccount);
		UserType type = UserType.get(2);
		createdUserAccountBean.addUserType(type);

		createdUserAccountBean = (UserAccountBean) udao.create(createdUserAccountBean);
		authoritiesDao.saveOrUpdate(new AuthoritiesBean(createdUserAccountBean.getName()));
		return userAccountBean;
	}

	// Create StudyUserRole records
	private UserAccountBean addActiveStudyRole(UserAccountBean createdUserAccountBean, int studyId, Role r, UserAccountBean rootUserAccount) {
		StudyUserRoleBean studyUserRole = new StudyUserRoleBean();
		studyUserRole.setStudyId(studyId);
		studyUserRole.setRoleName(r.getName());
		studyUserRole.setStatus(Status.AUTO_DELETED);
		studyUserRole.setOwner(rootUserAccount);
		createdUserAccountBean.addRole(studyUserRole);
		return createdUserAccountBean;
	}

	private int getCountCompletedEventCrfsInAStudyEvent(StudyEventBean seBean) {
		int count = 0;
		count = ecdao.findAllByStudyEventAndStatus(seBean, Status.UNAVAILABLE).size();
		return count;
	}

	private int getCountCrfsInAEventDefCrf() {
		int count = 0;
		edcdao = new EventDefinitionCRFDAO(ds);
		count = edcdao.findAllActiveByEventDefinitionId(getStudyEventDefnId()).size();
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
		StudyEventBean studyEventBean = (StudyEventBean) sedao.findByStudySubjectIdAndDefinitionIdAndOrdinal(getStudySubjectId(),
				getStudyEventDefnId(), studyEventOrdinal);
		return studyEventBean;
	}

	private StudySubjectBean getStudySubject() {
		ssdao = new StudySubjectDAO(ds);
		StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByPK(getStudySubjectId());
		return studySubjectBean;
	}

	private ArrayList<CRFVersionBean> getAllCRFVersionsByCrfId(String crfVersionOid) {
		cvdao = new CRFVersionDAO(ds);
		ArrayList<CRFVersionBean> crfVersionBeanList = (ArrayList) cvdao.findAllByCRF((getCRFVersion(crfVersionOid)).getCrfId());
		return crfVersionBeanList;
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

	private ArrayList <ItemBean> getItemRecord(String itemOID) {
		idao = new ItemDAO(ds);
		ArrayList <ItemBean> itemBean =  (ArrayList<ItemBean>) idao.findByOid(itemOID);
		return itemBean;
	}

	private ItemFormMetadataBean getItemFromMetadata(Integer itemId, Integer crfVersionId) {
		ifmdao = new ItemFormMetadataDAO(ds);
		ItemFormMetadataBean ifmBean = (ItemFormMetadataBean) ifmdao.findByItemIdAndCRFVersionId(itemId, crfVersionId);
		return ifmBean;
	}

	
	
	// Main Method to Start Saving Process the Pform Submission
	public Errors saveProcess(String body) throws Exception {
		System.out.println("------------------------------------------------");
		Errors errors = instanciateErrors();
		// Study Subject Validation check
		StudySubjectBean studySubjectBean = getEntityVariables();
		if (studySubjectBean == null) {
			System.out.println(" Study Subject Does not exist in the system ");
			errors.reject("Study Subject Does not exist in the system ");
			return errors;
		}
		// User Account Create or Bypass
		UserAccountBean userAccountBean = getUserAccount(getINPUT_USERNAME());
		if (!userAccountBean.isActive() && getStudySubject().isActive()) {
			userAccountBean = createUserAccount(userAccountBean);
			System.out.println("New User Account is created");
			logger.info("***New User Account is created***");
		} else {
			System.out.println(" User Account already exist in the system ");
			logger.info("***User Account already exist in the system***");
		}

		// Study Event Validation check (System must include events that are
		// scheduled or started)
		StudyEventBean studyEventBean = getStudyEvent();
		if (studyEventBean.isActive()
				&& (studyEventBean.getSubjectEventStatus() == SubjectEventStatus.SCHEDULED || studyEventBean.getSubjectEventStatus() == SubjectEventStatus.DATA_ENTRY_STARTED)) {
			// Read and Parse Payload from Pform
			errors = readDownloadFile(body, errors);
		} else {
			System.out.println("StudyEvent has a Status Other than Scheduled or Started ");
			logger.info("***StudyEvent has a Status Other than Scheduled or Started ***");
			errors.reject("StudyEvent has a Status Other than  Scheduled or Started");
			// return errors;
		}
		return errors;
	}

	// Update Study Event to Data Entry Started / Completed
	private StudyEventBean updateStudyEvent(StudyEventBean seBean, SubjectEventStatus status) {
		seBean.setUpdater(getUserAccount(getINPUT_USERNAME()));
		seBean.setUpdatedDate(new Date());
		seBean.setSubjectEventStatus(status);
		seBean = (StudyEventBean) sedao.update(seBean);
		logger.debug("*********UPDATED STUDY EVENT ");
		return seBean;
	}

	// Create Event CRF (Insert a record in event_crf table)
	private EventCRFBean createEventCRF(String crfVersionOid) {
		EventCRFBean ecBean = new EventCRFBean();
		ecBean.setAnnotations("");
		ecBean.setCreatedDate(new Date());
		ecBean.setCRFVersionId(getCRFVersion(crfVersionOid).getId());
		ecBean.setInterviewerName("");
		ecBean.setDateInterviewed(null);
		ecBean.setOwner(getUserAccount(getINPUT_USERNAME()));
		ecBean.setStatus(Status.AVAILABLE);
		ecBean.setCompletionStatusId(1);
		ecBean.setStudySubjectId(getStudySubject().getId());
		ecBean.setStudyEventId(getStudyEvent().getId());
		ecBean.setValidateString("");
		ecBean.setValidatorAnnotations("");
		ecBean.setUpdater(getUserAccount(getINPUT_USERNAME()));
		ecBean.setUpdatedDate(new Date());
		ecBean = (EventCRFBean) ecdao.create(ecBean);
		logger.debug("*********CREATED EVENT CRF");
		return ecBean;
	}

	// Update Status in Event CRF Table
	private EventCRFBean updateEventCRF(EventCRFBean ecBean) {
		ecBean.setUpdater(getUserAccount(getINPUT_USERNAME()));
		ecBean.setUpdatedDate(new Date());
		ecBean.setStatus(Status.UNAVAILABLE);
		ecBean = (EventCRFBean) ecdao.update(ecBean);
		logger.debug("*********UPDATED EVENT CRF");
		return ecBean;
	}

	// Create a single item data bean record , but not insert in table yet
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
		itemDataBean.setOwner(getUserAccount(getINPUT_USERNAME()));
		return itemDataBean;
	}

	// Instantiate an Error object
	public Errors instanciateErrors() {
		DataBinder dataBinder = new DataBinder(null);
		Errors errors = dataBinder.getBindingResult();
		return errors;
	}

	// Errors Object to Validate Item Data
	public Errors validateItemData(ItemDataBean itemDataBean, ItemBean itemBean,Integer responseTypeId) {
		ItemItemDataContainer container = new ItemItemDataContainer(itemBean, itemDataBean,responseTypeId);
		DataBinder dataBinder = new DataBinder(container);
		Errors errors = dataBinder.getBindingResult();
		PformValidator pformValidator = new PformValidator();
		pformValidator.validate(container, errors);
		return errors;
	}

	// Check for CRF Version if exist in system if submitted same version twice
	// or other versions of the same CRF
	private EventCRFBean getCrfVersionCheck(String crfVersionOID, Errors errors) {
		EventCRFBean eventCrfBean = null;
		boolean isSameCrfVersion = false;
		boolean isEventCrfInOC = false;

		if (!getEventCrf(crfVersionOID).isEmpty()) {
			isEventCrfInOC = true;
			isSameCrfVersion = true;
		} else {
			// Removing from the crf version list the version that already in
			// the system
			ArrayList<CRFVersionBean> crfVersionBeanList = getAllCRFVersionsByCrfId(crfVersionOID);
			for (CRFVersionBean crfVersionBean : crfVersionBeanList) {
				if (crfVersionBean.getOid() == crfVersionOID) {
					crfVersionBeanList.remove(crfVersionBean);
				}
			}

			for (CRFVersionBean crfVersionBean : crfVersionBeanList) {
				if (!getEventCrf(crfVersionBean.getOid()).isEmpty()) {
					isEventCrfInOC = true;
					isSameCrfVersion = false;
				}
			}

		}

		eventCrfBean = checkIfEventCrfInOC(isEventCrfInOC, isSameCrfVersion, crfVersionOID, errors);
		return eventCrfBean;
	}

	// Check if Event CRF exist or not in the system , if not , create ,
	private EventCRFBean checkIfEventCrfInOC(boolean isEventCrfInOC, boolean isSameCrfVersion, String crfVersionOID, Errors errors) {
		EventCRFBean eventCrfBean = null;
		if (!isEventCrfInOC) {
			// Execute Creating New Event Crf
			eventCrfBean = createEventCRF(crfVersionOID);
			// Continue creating Item Data
			System.out.println(" New EventCrf is created");
			logger.info("***New EventCrf is created***");

		} else if (isEventCrfInOC && isSameCrfVersion) {
			// If Same CRF version is tried to submit to OC for the same subject
			// , event..
			eventCrfBean = getEventCrf(crfVersionOID).get(0); // ///////////////
			System.out.println(" Existing EventCrf with same CRF Version");
			logger.info("***Existing EventCrf***");
			// If Item Data Exist in OC , then throw submission Failure
			if (!getItemDataRecords(eventCrfBean.getId()).isEmpty()) {
				System.out.println(" Existing Item Data , No New Item Data is added.  ");
				logger.info("***Existing Item Data , No New Item Data is added***");
				errors.reject("Existing Item Data , No New Item Data is added");
				return null;
			}

		} else {
			// If Another CRF version is tried to submit to OC for the same
			// subject , event ..
			eventCrfBean = null;
			System.out.println(" Existing EventCrf with other CRF version ");
			errors.reject("Existing EventCrf with other CRF version");
			return null;
		}
		return eventCrfBean;
	}

	// Read from Pform Submission Payload or the Body
	private Errors readDownloadFile(String body, Errors errors) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(body));
		Document doc = db.parse(is);

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

					EventCRFBean eventCrfBean = getCrfVersionCheck(crfVersionOID, errors);
					if (eventCrfBean == null)
						return errors;

					if (cnode instanceof Element && eventCrfBean != null) {
						NodeList childNodes1 = cnode.getChildNodes();
						ArrayList<ItemDataBean> itemDataBeanList = new ArrayList<ItemDataBean>();
						iddao = new ItemDataDAO(ds);

						for (int k = 1; k < childNodes1.getLength(); k = k + 2) {
							Node cnode1 = childNodes1.item(k);
							String itemOID = cnode1.getNodeName().trim();
							String itemValue = cnode1.getTextContent();
							
							ArrayList <ItemBean> iBean = getItemRecord(itemOID);
                            CRFVersionBean cvBean = getCRFVersion(crfVersionOID);
                            Integer itemId=iBean.get(0).getId();
                            Integer crfVersionId=cvBean.getId();
                            ItemFormMetadataBean ifmBean = getItemFromMetadata(itemId, crfVersionId);
                            Integer responseTypeId =ifmBean.getResponseSet().getResponseType().getId();
                            
                            if (responseTypeId==3 ||responseTypeId==7){
                            	itemValue=itemValue.replaceAll(" ", ",");
                            }
                            
                            System.out.println("Item OID: "+ itemOID +"     Response type:  " +ifmBean.getResponseSet().getResponseType().getId());
                                                         
                             

							idao = new ItemDAO(ds);

							ArrayList<ItemBean> itemBeanList = (ArrayList<ItemBean>) idao.findByOid(itemOID);
							ItemBean itemBean = itemBeanList.get(0);

							ItemDataBean itemDataBean = createItemData(itemBean, itemValue, eventCrfBean);
							errors = validateItemData(itemDataBean, itemBean,responseTypeId);
							if (errors.hasErrors()) {
								return errors;
							} else {
								itemDataBeanList.add(itemDataBean);
							}
						}
						if (!errors.hasErrors()) {
							for (ItemDataBean itemDataBean : itemDataBeanList) {
								// Create Item Data Bean by inserting one row at
								// a time to Item Data table
								iddao.create(itemDataBean);
								// Update Event Crf Bean and change the status
								// to Completed
								eventCrfBean = updateEventCRF(eventCrfBean);
								// Study Event status update
								if (getCountCompletedEventCrfsInAStudyEvent(getStudyEvent()) == getCountCrfsInAEventDefCrf()) {
									updateStudyEvent(getStudyEvent(), SubjectEventStatus.COMPLETED);
								} else {
									updateStudyEvent(getStudyEvent(), SubjectEventStatus.DATA_ENTRY_STARTED);
								}
							}
						}
					}
				}
			}

		}
		return errors;
	}

	public String getINPUT_USERNAME() {
		return INPUT_USERNAME;
	}

	public void setINPUT_USERNAME(String iNPUT_USERNAME) {
		INPUT_USERNAME = iNPUT_USERNAME;
	}

	public Integer getStudyId() {
		return studyId;
	}

	public void setStudyId(Integer studyId) {
		this.studyId = studyId;
	}

	public Integer getStudySubjectId() {
		return studySubjectId;
	}

	public void setStudySubjectId(Integer studySubjectId) {
		this.studySubjectId = studySubjectId;
	}

	public Integer getStudyEventDefnId() {
		return studyEventDefnId;
	}

	public void setStudyEventDefnId(Integer studyEventDefnId) {
		this.studyEventDefnId = studyEventDefnId;
	}

}
