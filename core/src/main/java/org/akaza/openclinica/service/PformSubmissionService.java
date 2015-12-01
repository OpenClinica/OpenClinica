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
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
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
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseSetBean;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.DynamicsItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.RuleActionPropertyDao;
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
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;
import org.akaza.openclinica.domain.crfdata.DynamicsItemGroupMetadataBean;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
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
/**
 * @author joekeremian
 *
 */
/**
 * @author joekeremian
 * 
 */
public class PformSubmissionService {

    // public static String studySubjectOid = "SS_30";
    // public static String studyEventDefnOid = "SE_NEWEVENT";
    // public static Integer studyEventOrdinal = 1;

    public static final String INPUT_USER_SOURCE = "userSource";
    public static final String INPUT_FIRST_NAME = "Participant";
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

    DataSource ds;
    private RuleActionPropertyDao ruleActionPropertyDao;
    private DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao;
    private DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;

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
    ItemGroupDAO igdao;
    ItemGroupMetadataDAO igmdao;

    public PformSubmissionService(DataSource ds, AuthoritiesDao authoritiesDao) {
        this.ds = ds;
        this.authoritiesDao = authoritiesDao;
    }

    /**
     * This method will generate new UserName combining Study and Study Subject OIDs
     * 
     * @param studyBean
     * @param studySubjectBean
     * @return
     */
    public String getInputUsername(StudyBean studyBean, StudySubjectBean studySubjectBean) {
        String inputUserName = null;
        if (studySubjectBean != null) {
            if (studyBean.getParentStudyId() > 0)
                studyBean = getStudy(studyBean.getParentStudyId());

            inputUserName = studyBean.getOid() + "." + studySubjectBean.getOid();
        }
        return inputUserName;
    }

    private int getCountCompletedEventCrfsInAStudyEvent(StudyEventBean seBean) {
        int count = 0;
        count = ecdao.findAllByStudyEventAndStatus(seBean, Status.UNAVAILABLE).size();
        return count;
    }

    private int getCountCrfsInAEventDefCrf(Integer studyEventDefinitionId, Integer studyId) {
        int count = 0;
        edcdao = new EventDefinitionCRFDAO(ds);
        count = edcdao.findAllDefIdandStudyId(studyEventDefinitionId, studyId).size();
        return count;
    }

    private EventDefinitionCRFBean getCrfVersionStatusInAEventDefCrf(String crfVersionOid, StudyBean studyBean, StudyEventBean studyEventBean) {
        edcdao = new EventDefinitionCRFDAO(ds);
        EventDefinitionCRFBean eventDefinitionCRFBean = edcdao.findByStudyEventIdAndCRFVersionId(studyBean, studyEventBean.getId(),
                getCRFVersion(crfVersionOid).getId());
        return eventDefinitionCRFBean;
    }

    private int getCountCrfsInAEventDefCrfForSite(Integer studyEventDefinitionId, Integer studyId) {
        int count = 0;
        edcdao = new EventDefinitionCRFDAO(ds);
        count = edcdao.findAllDefnIdandStudyIdForSite(studyEventDefinitionId, studyId).size();
        return count;
    }

    private StudyBean getStudy(Integer id) {
        sdao = new StudyDAO(ds);
        StudyBean studyBean = (StudyBean) sdao.findByPK(id);
        return studyBean;
    }

    private StudyBean getStudy(StudySubjectBean studySubjectBean) {
        sdao = new StudyDAO(ds);
        StudyBean studyBean = (StudyBean) sdao.findByStudySubjectId(studySubjectBean.getId());
        return studyBean;
    }

    private StudySubjectBean getStudySubject(String oid) {
        ssdao = new StudySubjectDAO(ds);
        StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByOid(oid);
        return studySubjectBean;
    }

    private StudySubjectBean getStudySubject(Integer id) {
        ssdao = new StudySubjectDAO(ds);
        StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByPK(id);
        return studySubjectBean;
    }

    private UserAccountBean getUserAccount(String userName) {
        udao = new UserAccountDAO(ds);
        UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(userName);
        return userAccountBean;
    }

    private StudyEventDefinitionBean getStudyEventDefn(int id) {
        seddao = new StudyEventDefinitionDAO(ds);
        StudyEventDefinitionBean studyEventDefinitionBean = (StudyEventDefinitionBean) seddao.findByPK(id);
        return studyEventDefinitionBean;
    }

    private StudyEventBean getStudyEvent(StudySubjectBean studySubjectBean, StudyEventDefinitionBean studyEventDefinitionBean, int studyEventOrdinal) {
        sedao = new StudyEventDAO(ds);
        StudyEventBean studyEventBean = (StudyEventBean) sedao.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                studyEventDefinitionBean.getId(), studyEventOrdinal);
        return studyEventBean;
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

    private ArrayList<EventCRFBean> getEventCrf(String crfVersionOid, StudyEventBean studyEventBean, StudySubjectBean studySubjectBean) {
        ecdao = new EventCRFDAO(ds);
        ArrayList<EventCRFBean> eventCrfBeanList = ecdao.findByEventSubjectVersion(studyEventBean, studySubjectBean, getCRFVersion(crfVersionOid));
        return eventCrfBeanList;
    }

    // private ArrayList<ItemDataBean> getItemDataRecord(int itemDataId) {
    // iddao = new ItemDataDAO(ds);
    // ArrayList<ItemDataBean> itemDataBeanList = iddao.findAllByEventCRFId(eventCRFId);
    // return itemDataBeanList;
    // }

    private ArrayList<ItemDataBean> getItemDataRecords(int eventCRFId) {
        iddao = new ItemDataDAO(ds);
        ArrayList<ItemDataBean> itemDataBeanList = iddao.findAllByEventCRFId(eventCRFId);
        return itemDataBeanList;
    }

    private ItemBean getItemRecord(String itemName, CRFVersionBean crfVersion) {
        idao = new ItemDAO(ds);
        ItemBean itemBean = (ItemBean) idao.findByNameAndCRFId(itemName, crfVersion.getCrfId());
        return itemBean;
    }

    private ArrayList<ItemBean> getItemRecord(String itemOID) {
        idao = new ItemDAO(ds);
        ArrayList<ItemBean> itemBean = (ArrayList<ItemBean>) idao.findByOid(itemOID);
        return itemBean;
    }

    private ItemFormMetadataBean getItemFromMetadata(Integer itemId, Integer crfVersionId) {
        ifmdao = new ItemFormMetadataDAO(ds);
        ItemFormMetadataBean ifmBean = (ItemFormMetadataBean) ifmdao.findByItemIdAndCRFVersionId(itemId, crfVersionId);
        return ifmBean;
    }

    /**
     * Main Method to Start Saving Process the Pform Submission
     * 
     * @param body
     * @param studySubjectOid
     * @param studyEventDefnId
     * @param studyEventOrdinal
     * @return
     * @throws Exception
     */
    public Errors saveProcess(String body, String studySubjectOid, Integer studyEventDefnId, Integer studyEventOrdinal, CRFVersionBean crfVersion)
            throws Exception {
        Errors errors = instanciateErrors();
        // Study Subject Validation check
        StudySubjectBean studySubjectBean = getStudySubject(studySubjectOid);
        if (studySubjectBean == null) {
            errors.reject("Study Subject Does not exist in the system ");
            return errors;
        }
        // User Account Create or Bypass
        StudyBean studyBean = getStudy(studySubjectBean);
        UserAccountBean userAccountBean = getUserAccount(getInputUsername(studyBean, studySubjectBean));
        if (!userAccountBean.isActive() && studySubjectBean.isActive()) {
            // userAccountBean = createUserAccount(userAccountBean, studyBean, studySubjectBean);
            logger.info("***  User Account Does Not Exist in the System  ***");
            errors.reject("  User Account Does Not Exist in the System  ");
            return errors;
        } else {
            logger.info("***User Account already exist in the system***");
        }

        // Study Event Validation check (System must include events that are
        // scheduled or started)
        StudyEventDefinitionBean studyEventDefinitionBean = getStudyEventDefn(studyEventDefnId);
        StudyEventBean studyEventBean = getStudyEvent(studySubjectBean, studyEventDefinitionBean, studyEventOrdinal);
        if (studyEventBean.isActive()
                && (studyEventBean.getSubjectEventStatus() == SubjectEventStatus.SCHEDULED || studyEventBean.getSubjectEventStatus() == SubjectEventStatus.DATA_ENTRY_STARTED)) {
            // Read and Parse Payload from Pform
            if (crfVersion.getXform() != null && !crfVersion.getXform().equals(""))
                errors = readDownloadFileNew(body, errors, studyBean, studyEventBean, studySubjectBean, studyEventDefinitionBean, crfVersion, userAccountBean);
            else
                errors = readDownloadFile(body, errors, studyBean, studyEventBean, studySubjectBean, studyEventDefinitionBean, userAccountBean);
        } else {
            logger.info("***StudyEvent has a Status Other than Scheduled or Started ***");
            errors.reject("StudyEvent has a Status Other than  Scheduled or Started");
            // return errors;
        }
        return errors;
    }

    /**
     * Update Study Event to Data Entry Started / Completed
     * 
     * @param seBean
     * @param status
     * @param studyBean
     * @param studySubjectBean
     * @return
     */
    private StudyEventBean updateStudyEvent(StudyEventBean seBean, SubjectEventStatus status, StudyBean studyBean, StudySubjectBean studySubjectBean) {
        seBean.setUpdater(getUserAccount(getInputUsername(studyBean, studySubjectBean)));
        seBean.setUpdatedDate(new Date());
        seBean.setSubjectEventStatus(status);
        seBean = (StudyEventBean) sedao.update(seBean);
        logger.debug("*********UPDATED STUDY EVENT ");
        return seBean;
    }

    /**
     * Create Event CRF (Insert a record in event_crf table)
     * 
     * @param crfVersionOid
     * @param studyBean
     * @param studyEventBean
     * @param studySubjectBean
     * @return
     */
    private EventCRFBean createEventCRF(String crfVersionOid, StudyBean studyBean, StudyEventBean studyEventBean, StudySubjectBean studySubjectBean) {
        String inputUsername = getInputUsername(studyBean, studySubjectBean);
        EventCRFBean ecBean = new EventCRFBean();
        ecBean.setAnnotations("");
        ecBean.setCreatedDate(new Date());
        ecBean.setCRFVersionId(getCRFVersion(crfVersionOid).getId());
        ecBean.setInterviewerName("");
        ecBean.setDateInterviewed(null);
        ecBean.setOwner(getUserAccount(inputUsername));
        ecBean.setStatus(Status.AVAILABLE);
        ecBean.setCompletionStatusId(1);
        ecBean.setStudySubjectId(studySubjectBean.getId());
        ecBean.setStudyEventId(studyEventBean.getId());
        ecBean.setValidateString("");
        ecBean.setValidatorAnnotations("");
        ecBean.setUpdater(getUserAccount(inputUsername));
        ecBean.setUpdatedDate(new Date());
        ecBean = (EventCRFBean) ecdao.create(ecBean);
        logger.debug("*********CREATED EVENT CRF");
        return ecBean;
    }

    /**
     * Update Status in Event CRF Table
     * 
     * @param ecBean
     * @param studyBean
     * @param studySubjectBean
     * @return
     */
    private EventCRFBean updateEventCRF(EventCRFBean ecBean, StudyBean studyBean, StudySubjectBean studySubjectBean) {
        String inputUsername = getInputUsername(studyBean, studySubjectBean);
        ecBean.setUpdater(getUserAccount(inputUsername));
        ecBean.setUpdatedDate(new Date());
        ecBean.setStatus(Status.AVAILABLE);
        ecBean = (EventCRFBean) ecdao.update(ecBean);
        logger.debug("*********UPDATED EVENT CRF");
        return ecBean;
    }

    /**
     * Create a single item data bean record , but not insert in table yet
     * 
     * @param itemBean
     * @param itemValue
     * @param itemOrdinal
     * @param eventCrfBean
     * @param studyBean
     * @param studySubjectBean
     * @return
     */
    private ItemDataBean createItemData(ItemBean itemBean, String itemValue, Integer itemOrdinal, EventCRFBean eventCrfBean, StudyBean studyBean,
            StudySubjectBean studySubjectBean) {
        logger.info("item Oid:  " + itemBean.getOid() + "   itemValue:  " + itemValue + "  itemOrdinal:  " + itemOrdinal);
        ItemDataBean itemDataBean = new ItemDataBean();
        itemDataBean.setItemId(itemBean.getId());
        itemDataBean.setEventCRFId(eventCrfBean.getId());
        itemDataBean.setValue(itemValue);
        itemDataBean.setCreatedDate(new Date());
        itemDataBean.setStatus(Status.UNAVAILABLE);
        itemDataBean.setOrdinal(itemOrdinal);
        itemDataBean.setOwner(getUserAccount(getInputUsername(studyBean, studySubjectBean)));
        return itemDataBean;
    }

    /**
     * Instantiate an Error object
     * 
     * @return
     */
    public Errors instanciateErrors() {
        DataBinder dataBinder = new DataBinder(null);
        Errors errors = dataBinder.getBindingResult();
        return errors;
    }

    /**
     * Errors Object to Validate Item Data
     * 
     * @param itemDataBean
     * @param itemBean
     * @param responseTypeId
     * @return
     */
    public Errors validateItemData(ItemDataBean itemDataBean, ItemBean itemBean, Integer responseTypeId) {
        ItemItemDataContainer container = new ItemItemDataContainer(itemBean, itemDataBean, responseTypeId);
        DataBinder dataBinder = new DataBinder(container);
        Errors errors = dataBinder.getBindingResult();
        PformValidator pformValidator = new PformValidator();
        pformValidator.validate(container, errors);
        return errors;
    }

    /**
     * Check for CRF Version if exist in system if submitted same version twice or other versions of the same CRF
     * 
     * @param crfVersionOID
     * @param errors
     * @param studyBean
     * @param studyEventBean
     * @param studySubjectBean
     * @return
     */
    private EventCRFBean getCrfVersionCheck(String crfVersionOID, Errors errors, StudyBean studyBean, StudyEventBean studyEventBean,
            StudySubjectBean studySubjectBean) {
        EventCRFBean eventCrfBean = null;
        boolean isSameCrfVersion = false;
        boolean isEventCrfInOC = false;

        // Verify that the Crf Version has an available status in the Study
        // Event Defn
        if (getCrfVersionStatusInAEventDefCrf(crfVersionOID, studyBean, studyEventBean).getStatus().getId() != 1) {
            logger.info("This Crf Version has a Status Not available in this Study Event Defn");
            errors.reject("This Crf Version has a Status Not available in this Study Event Defn");
            return null;
        }

        if (!getEventCrf(crfVersionOID, studyEventBean, studySubjectBean).isEmpty()) {
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
                if (!getEventCrf(crfVersionBean.getOid(), studyEventBean, studySubjectBean).isEmpty()) {
                    isEventCrfInOC = true;
                    isSameCrfVersion = false;
                }
            }

        }

        eventCrfBean = checkIfEventCrfInOC(isEventCrfInOC, isSameCrfVersion, crfVersionOID, errors, studyBean, studyEventBean, studySubjectBean);
        return eventCrfBean;
    }

    /**
     * Check if Event CRF exist or not in the system , if not , create ,
     * 
     * @param isEventCrfInOC
     * @param isSameCrfVersion
     * @param crfVersionOID
     * @param errors
     * @param studyBean
     * @param studyEventBean
     * @param studySubjectBean
     * @return
     */
    private EventCRFBean checkIfEventCrfInOC(boolean isEventCrfInOC, boolean isSameCrfVersion, String crfVersionOID, Errors errors, StudyBean studyBean,
            StudyEventBean studyEventBean, StudySubjectBean studySubjectBean) {
        EventCRFBean eventCrfBean = null;
        if (!isEventCrfInOC) {
            // Execute Creating New Event Crf
            eventCrfBean = createEventCRF(crfVersionOID, studyBean, studyEventBean, studySubjectBean);
            // Continue creating Item Data
            logger.info("***New EventCrf is created***");

        } else if (isEventCrfInOC && isSameCrfVersion) {
            // If Same CRF version is tried to submit to OC for the same subject
            // , event..
            eventCrfBean = getEventCrf(crfVersionOID, studyEventBean, studySubjectBean).get(0); // ///////////////
            logger.info("***  Existing EventCrf with same CRF Version  ***");
        } else {
            // If Another CRF version is tried to submit to OC for the same
            // subject , event ..
            eventCrfBean = null;
            errors.reject("Existing EventCrf with other CRF version");
            return null;
        }
        return eventCrfBean;
    }

    /**
     * Read from Pform Submission Payload or the Body
     * 
     * @param body
     * @param errors
     * @param studyBean
     * @param studyEventBean
     * @param studySubjectBean
     * @return
     * @throws Exception
     */
    private Errors readDownloadFile(String body, Errors errors, StudyBean studyBean, StudyEventBean studyEventBean, StudySubjectBean studySubjectBean,
            StudyEventDefinitionBean studyEventDefinitionBean, UserAccountBean userAccountBean) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(body));
        Document doc = db.parse(is);
        Integer itemOrdinal = 1;
        String itemOID;
        String itemValue;
        String groupNodeName = "";

        NodeList instanceNodeList = doc.getElementsByTagName("instance");
        for (int i = 0; i < instanceNodeList.getLength(); i = i + 1) {
            Node instanceNode = instanceNodeList.item(i);

            if (instanceNode instanceof Element) {
                NodeList crfNodeList = instanceNode.getChildNodes();
                for (int j = 0; j < crfNodeList.getLength(); j = j + 1) {
                    Node crfNode = crfNodeList.item(j);
                    if (crfNode instanceof Element) {
                        String crfVersionOID = crfNode.getNodeName().trim();
                        logger.info("***crf_version_ :  " + crfVersionOID + " *** ");

                        EventCRFBean eventCrfBean = getCrfVersionCheck(crfVersionOID, errors, studyBean, studyEventBean, studySubjectBean);
                        ArrayList<ItemDataBean> itemDataBeanList = new ArrayList<ItemDataBean>();
                        iddao = new ItemDataDAO(ds);

                        if (eventCrfBean == null)
                            return errors;
                        if (eventCrfBean != null) {

                            NodeList groupNodeList = crfNode.getChildNodes();
                            for (int k = 0; k < groupNodeList.getLength(); k = k + 1) {
                                Node groupNode = groupNodeList.item(k);

                                if (groupNode instanceof Element && !groupNode.getNodeName().startsWith("SECTION_")) {

                                    if (groupNode.getNodeName() != groupNodeName) {
                                        itemOrdinal = 1;
                                    } else {
                                        itemOrdinal++;
                                    }
                                    groupNodeName = groupNode.getNodeName();

                                    NodeList itemNodeList = groupNode.getChildNodes();

                                    for (int m = 0; m < itemNodeList.getLength(); m = m + 1) {
                                        Node itemNode = itemNodeList.item(m);
                                        if (itemNode instanceof Element && !itemNode.getNodeName().endsWith(".HEADER")
                                                && !itemNode.getNodeName().endsWith(".SUBHEADER")) {

                                            itemOID = itemNode.getNodeName().trim();
                                            itemValue = itemNode.getTextContent();

                                            ArrayList<ItemBean> iBean = getItemRecord(itemOID);
                                            CRFVersionBean cvBean = getCRFVersion(crfVersionOID);
                                            Integer itemId = iBean.get(0).getId();
                                            Integer crfVersionId = cvBean.getId();
                                            ItemFormMetadataBean ifmBean = getItemFromMetadata(itemId, crfVersionId);
                                            Integer responseTypeId = ifmBean.getResponseSet().getResponseType().getId();

                                            if (responseTypeId == 3 || responseTypeId == 7) {
                                                itemValue = itemValue.replaceAll(" ", ",");
                                            }

                                            idao = new ItemDAO(ds);

                                            ArrayList<ItemBean> itemBeanList = (ArrayList<ItemBean>) idao.findByOid(itemOID);
                                            ItemBean itemBean = itemBeanList.get(0);

                                            ItemDataBean itemDataBean = createItemData(itemBean, itemValue, itemOrdinal, eventCrfBean, studyBean,
                                                    studySubjectBean);
                                            errors = validateItemData(itemDataBean, itemBean, responseTypeId);
                                            if (errors.hasErrors()) {
                                                return errors;
                                            } else {
                                                itemDataBeanList.add(itemDataBean);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!errors.hasErrors()) {
                            // Create Item Data Bean by inserting one row at
                            // a time to Item Data table
                            for (ItemDataBean itemDataBean1 : itemDataBeanList) {
                                iddao.setFormatDates(false);
                                ItemDataBean existingValue = iddao.findByItemIdAndEventCRFIdAndOrdinal(itemDataBean1.getItemId(),
                                        itemDataBean1.getEventCRFId(), itemDataBean1.getOrdinal());
                                iddao.setFormatDates(true);
                                if (!existingValue.isActive()) {
                                    iddao.create(itemDataBean1);
                                } else if (existingValue.getValue().equals(itemDataBean1.getValue())) {
                                    // Value unchanged. Do nothing.
                                } else {
                                    itemDataBean1.setId(existingValue.getId());
                                    itemDataBean1.setUpdaterId(userAccountBean.getId());
                                    iddao.updateValue(itemDataBean1);
                                }
                            }
                            
                            // Update Event Crf Bean and change the status to Completed
                            eventCrfBean = updateEventCRF(eventCrfBean, studyBean, studySubjectBean);
                        }
                    }
                }
            }
        }
        return errors;
    }

    /**
     * Read from Pform Submission Payload or the Body
     * 
     * @param body
     * @param errors
     * @param studyBean
     * @param studyEventBean
     * @param studySubjectBean
     * @return
     * @throws Exception
     */
    private Errors readDownloadFileNew(String body, Errors errors, StudyBean studyBean, StudyEventBean studyEventBean, StudySubjectBean studySubjectBean,
            StudyEventDefinitionBean studyEventDefinitionBean, CRFVersionBean crfVersion, UserAccountBean userAccountBean) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(body));
        Document doc = db.parse(is);
        Integer itemOrdinal = 1;
        String itemName;
        String itemValue;
        String groupNodeName = "";

        NodeList instanceNodeList = doc.getElementsByTagName("instance");
        // Instance loop
        for (int i = 0; i < instanceNodeList.getLength(); i = i + 1) {
            Node instanceNode = instanceNodeList.item(i);
            if (instanceNode instanceof Element) {
                NodeList crfNodeList = instanceNode.getChildNodes();
                // Form loop
                for (int j = 0; j < crfNodeList.getLength(); j = j + 1) {
                    Node crfNode = crfNodeList.item(j);
                    if (crfNode instanceof Element) {
                        logger.info("***crf_version_ :  " + crfVersion.getOid() + " *** ");

                        EventCRFBean eventCrfBean = getCrfVersionCheck(crfVersion.getOid(), errors, studyBean, studyEventBean, studySubjectBean);
                        ArrayList<ItemDataBean> itemDataBeanList = new ArrayList<ItemDataBean>();
                        iddao = new ItemDataDAO(ds);

                        if (eventCrfBean == null)
                            return errors;
                        if (eventCrfBean != null) {

                            NodeList groupNodeList = crfNode.getChildNodes();
                            // Group loop
                            for (int k = 0; k < groupNodeList.getLength(); k = k + 1) {
                                Node groupNode = groupNodeList.item(k);

                                if (groupNode instanceof Element && !groupNode.getNodeName().startsWith("SECTION_")) {

                                    if (groupNode.getNodeName() != groupNodeName) {
                                        itemOrdinal = 1;
                                    } else {
                                        itemOrdinal++;
                                    }
                                    groupNodeName = groupNode.getNodeName();

                                    NodeList itemNodeList = groupNode.getChildNodes();
                                    // Item loop
                                    for (int m = 0; m < itemNodeList.getLength(); m = m + 1) {
                                        Node itemNode = itemNodeList.item(m);
                                        if (itemNode instanceof Element && !itemNode.getNodeName().endsWith(".HEADER")
                                                && !itemNode.getNodeName().endsWith(".SUBHEADER")) {

                                            itemName = itemNode.getNodeName().trim();
                                            itemValue = itemNode.getTextContent();

                                            ItemBean iBean = getItemRecord(itemName, crfVersion);
                                            CRFVersionBean cvBean = getCRFVersion(crfVersion.getOid());
                                            Integer itemId = iBean.getId();
                                            Integer crfVersionId = cvBean.getId();
                                            ItemFormMetadataBean ifmBean = getItemFromMetadata(itemId, crfVersionId);
                                            Integer responseTypeId = ifmBean.getResponseSet().getResponseType().getId();

                                            if (responseTypeId == 3 || responseTypeId == 7) {
                                                itemValue = itemValue.replaceAll(" ", ",");
                                            }

                                            idao = new ItemDAO(ds);

                                            ItemDataBean itemDataBean = createItemData(iBean, itemValue, itemOrdinal, eventCrfBean, studyBean, studySubjectBean);
                                            errors = validateItemData(itemDataBean, iBean, responseTypeId);
                                            if (errors.hasErrors()) {
                                                return errors;
                                            } else {
                                                itemDataBeanList.add(itemDataBean);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!errors.hasErrors()) {
                            // Create Item Data Bean by inserting one row at
                            // a time to Item Data table
                            for (ItemDataBean itemDataBean1 : itemDataBeanList) {
                                iddao.setFormatDates(false);
                                ItemDataBean existingValue = iddao.findByItemIdAndEventCRFIdAndOrdinal(itemDataBean1.getItemId(),
                                        itemDataBean1.getEventCRFId(), itemDataBean1.getOrdinal());
                                iddao.setFormatDates(true);
                                if (!existingValue.isActive()) {
                                    iddao.create(itemDataBean1);
                                } else if (existingValue.getValue().equals(itemDataBean1.getValue())) {
                                    // Value unchanged. Do nothing.
                                } else {
                                    itemDataBean1.setId(existingValue.getId());
                                    itemDataBean1.setUpdaterId(userAccountBean.getId());
                                    iddao.updateValue(itemDataBean1);
                                }

                            }
                            
                            // Update Event Crf Bean and change the status to Completed
                            eventCrfBean = updateEventCRF(eventCrfBean, studyBean, studySubjectBean);
                        }
                    }
                }
            }
        }
        return errors;
    }

    @SuppressWarnings("null")
    private void setDynItemFormMetadata(CRFVersionBean crfVersionBean, EventCRFBean eventCrfBean, ItemDataBean itemDataBean, ArrayList<Integer> ruleList) {
        iddao = new ItemDataDAO(ds);
        ItemBean itemBean = (ItemBean) idao.findByPK(itemDataBean.getItemId());
        ArrayList<PropertyBean> propertyBeans = null;
        propertyBeans = getItemPropertyBean(itemBean.getOid());
        RuleBean ruleBean;
        if (propertyBeans.size() != 0) {
            for (PropertyBean propertyBean : propertyBeans) {
                logger.info("property bean oid:   " + propertyBean.getOid());
                RuleActionBean ruleActionBean = propertyBean.getRuleActionBean();
                if (ruleActionBean.getActionType().getCode() == 3 && ruleActionBean.getRuleSetRule().getStatus().getCode() == 1) {
                    ruleBean = ruleActionBean.getRuleSetRule().getRuleBean();
                    getItemFormMetaDataList(itemDataBean, itemBean, eventCrfBean, crfVersionBean);
                }

            }
        }

    }

    private void setDynItemGroupMetadata(CRFVersionBean crfVersionBean, EventCRFBean eventCrfBean) {
        igdao = new ItemGroupDAO(ds);
        ArrayList<Integer> ruleList = new ArrayList<Integer>();
        ArrayList<ItemGroupBean> itemGroupBeans = (ArrayList<ItemGroupBean>) igdao.findGroupByCRFVersionID(crfVersionBean.getId());
        for (ItemGroupBean itemGroupBean : itemGroupBeans) {
            ArrayList<PropertyBean> propertyBeans = null;
            propertyBeans = getGroupPropertyBean(itemGroupBean.getOid());

            if (propertyBeans.size() != 0) {
                for (PropertyBean propertyBean : propertyBeans) {
                    logger.info("property bean oid:   " + propertyBean.getOid());
                    RuleActionBean ruleActionBean = propertyBean.getRuleActionBean();
                    if (ruleActionBean.getActionType().getCode() == 3 && ruleActionBean.getRuleSetRule().getStatus().getCode() == 1) {
                        getItemGroupMetaDataList(itemGroupBean, eventCrfBean, crfVersionBean);
                    }
                }
            }
        }

    }

    private void getItemGroupMetaDataList(ItemGroupBean itemGroupBean, EventCRFBean eventCrfBean, CRFVersionBean crfVersionBean) {
        igmdao = new ItemGroupMetadataDAO(ds);
        ArrayList<ItemGroupMetadataBean> itemGroupMetadataBeans = (ArrayList<ItemGroupMetadataBean>) igmdao.findMetaByGroupAndCrfVersion(itemGroupBean.getId(),
                crfVersionBean.getId());
        DynamicsItemGroupMetadataBean dynamicsItemGroupMetadataBean = null;
        for (ItemGroupMetadataBean itemGroupMetadataBean : itemGroupMetadataBeans) {
            DynamicsItemGroupMetadataBean dynGrpBean = getDynamicsItemGroupMetadataDao().findByMetadataBean(itemGroupMetadataBean, eventCrfBean);
            if (dynGrpBean == null) {
                dynamicsItemGroupMetadataBean = createDynamicsItemGroupMetadataBean(itemGroupBean, eventCrfBean, itemGroupMetadataBean);
                saveDynamicGroupMeta(dynamicsItemGroupMetadataBean);
            }
        }
    }

    private void getItemFormMetaDataList(ItemDataBean itemDataBean, ItemBean itemBean, EventCRFBean eventCrfBean, CRFVersionBean crfVersionBean) {
        DynamicsItemFormMetadataBean dynamicsItemFormMetadataBean = null;
        ItemFormMetadataBean itemFormMetadataBean = getItemFromMetadata(itemBean.getId(), crfVersionBean.getId());

        DynamicsItemFormMetadataBean dynBean = getDynamicsItemFormMetadataDao().findByMetadataBean(itemFormMetadataBean, eventCrfBean, itemDataBean);
        if (dynBean == null) {
            dynamicsItemFormMetadataBean = createDynamicsItemFormMetadataBean(itemDataBean, itemBean, eventCrfBean, crfVersionBean);
            saveDynamicItemFormMeta(dynamicsItemFormMetadataBean);
        }
    }

    private DynamicsItemFormMetadataBean createDynamicsItemFormMetadataBean(ItemDataBean itemDataBean, ItemBean itemBean, EventCRFBean eventCrfBean,
            CRFVersionBean crfVersionBean) {

        ItemFormMetadataBean itemFormMetadataBean = getItemFromMetadata(itemBean.getId(), crfVersionBean.getId());

        DynamicsItemFormMetadataBean dynamicsItemFormMetadataBean = new DynamicsItemFormMetadataBean();
        dynamicsItemFormMetadataBean.setEventCrfId(eventCrfBean.getId());
        dynamicsItemFormMetadataBean.setCrfVersionId(crfVersionBean.getId());
        dynamicsItemFormMetadataBean.setItemFormMetadataId(itemFormMetadataBean.getId());
        dynamicsItemFormMetadataBean.setItemId(itemBean.getId());
        dynamicsItemFormMetadataBean.setShowItem(true);
        dynamicsItemFormMetadataBean.setItemDataId(itemDataBean.getId());
        dynamicsItemFormMetadataBean.setVersion(0);
        dynamicsItemFormMetadataBean.setPassedDde(0);

        return dynamicsItemFormMetadataBean;
    }

    private DynamicsItemGroupMetadataBean createDynamicsItemGroupMetadataBean(ItemGroupBean itemGroupBean, EventCRFBean eventCrfBean,
            ItemGroupMetadataBean itemGroupMetadataBean) {

        DynamicsItemGroupMetadataBean dynamicsItemGroupMetadataBean = new DynamicsItemGroupMetadataBean();
        dynamicsItemGroupMetadataBean.setEventCrfId(eventCrfBean.getId());
        dynamicsItemGroupMetadataBean.setItemGroupId(itemGroupBean.getId());
        dynamicsItemGroupMetadataBean.setItemGroupMetadataId(itemGroupMetadataBean.getId());
        dynamicsItemGroupMetadataBean.setShowGroup(true);
        dynamicsItemGroupMetadataBean.setVersion(0);
        dynamicsItemGroupMetadataBean.setPassedDde(0);

        return dynamicsItemGroupMetadataBean;
    }

    private void saveDynamicItemFormMeta(DynamicsItemFormMetadataBean dynamicsItemFormMetadataBean) {
        getDynamicsItemFormMetadataDao().saveOrUpdate(dynamicsItemFormMetadataBean);
    }

    private void saveDynamicGroupMeta(DynamicsItemGroupMetadataBean dynamicsItemGroupMetadataBean) {
        getDynamicsItemGroupMetadataDao().saveOrUpdate(dynamicsItemGroupMetadataBean);
    }

    private ArrayList<PropertyBean> getGroupPropertyBean(String groupOid) {
        ArrayList<PropertyBean> propertyBeans = null;
        propertyBeans = getRuleActionPropertyDao().findByOid(groupOid);
        return propertyBeans;
    }

    private ArrayList<PropertyBean> getItemPropertyBean(String itemOid) {
        ArrayList<PropertyBean> propertyBeans = null;
        propertyBeans = getRuleActionPropertyDao().findByOid(itemOid);
        return propertyBeans;
    }

    public RuleActionPropertyDao getRuleActionPropertyDao() {
        return ruleActionPropertyDao;
    }

    public void setRuleActionPropertyDao(RuleActionPropertyDao ruleActionPropertyDao) {
        this.ruleActionPropertyDao = ruleActionPropertyDao;
    }

    public DynamicsItemGroupMetadataDao getDynamicsItemGroupMetadataDao() {
        return dynamicsItemGroupMetadataDao;
    }

    public void setDynamicsItemGroupMetadataDao(DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao) {
        this.dynamicsItemGroupMetadataDao = dynamicsItemGroupMetadataDao;
    }

    public DynamicsItemFormMetadataDao getDynamicsItemFormMetadataDao() {
        return dynamicsItemFormMetadataDao;
    }

    public void setDynamicsItemFormMetadataDao(DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao) {
        this.dynamicsItemFormMetadataDao = dynamicsItemFormMetadataDao;
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

    private StudyBean getStudy(String oid) {
        sdao = new StudyDAO(ds);
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

}