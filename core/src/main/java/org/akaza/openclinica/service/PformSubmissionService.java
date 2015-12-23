package org.akaza.openclinica.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
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
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    DiscrepancyNoteDAO dndao;

    public PformSubmissionService(DataSource ds) {
        this.ds = ds;
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
    
    private int getCountCrfsInAEventDefCrf(Integer studyEventDefinitionId , Integer studyId) {
        int count = 0;
        edcdao = new EventDefinitionCRFDAO(ds);
        count = edcdao.findAllDefIdandStudyId(studyEventDefinitionId , studyId).size();
        return count;
    }

    private int getCountCrfsInAEventDefCrfForSite(Integer studyEventDefinitionId , Integer studyId) {
        int count = 0;
        edcdao = new EventDefinitionCRFDAO(ds);
        count = edcdao.findAllDefnIdandStudyIdForSite(studyEventDefinitionId , studyId).size();
        return count;
    }


    private EventDefinitionCRFBean getCrfVersionStatusInAEventDefCrf(String crfVersionOid, StudyBean studyBean, StudyEventBean studyEventBean) {
        edcdao = new EventDefinitionCRFDAO(ds);
        EventDefinitionCRFBean eventDefinitionCRFBean = edcdao.findByStudyEventIdAndCRFVersionId(studyBean, studyEventBean.getId(),
                getCRFVersion(crfVersionOid).getId());
        return eventDefinitionCRFBean;
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

    private StudyBean getStudy(String oid) {
        sdao = new StudyDAO(ds);
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
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

    private ItemGroupMetadataBean getItemGroupMetadata(Integer itemId, Integer crfVersionId) {
        igmdao = new ItemGroupMetadataDAO(ds);
        ItemGroupMetadataBean igmBean = (ItemGroupMetadataBean) igmdao.findByItemAndCrfVersion(itemId, crfVersionId);
        return igmBean;
    }

    /**
     * Main Method to Start Saving Process the Pform Submission
     * 
     * @param body
     * @param studySubjectOid
     * @param studyEventDefnId
     * @param studyEventOrdinal
     * @param locale 
     * @param isAnonymous 
     * @return
     * @throws Exception
     */
    public Errors saveProcess(String body, String studySubjectOid, Integer studyEventDefnId, Integer studyEventOrdinal, CRFVersionBean crfVersion, Locale locale, boolean isAnonymous)
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
                errors = readDownloadFileNew(body, errors, studyBean, studyEventBean, studySubjectBean, studyEventDefinitionBean, crfVersion, locale, isAnonymous);
            else
                errors = readDownloadFile(body, errors, studyBean, studyEventBean, studySubjectBean, studyEventDefinitionBean, locale, isAnonymous);
        } else {
            logger.info("***StudyEvent has a Status Other than Scheduled or Started ***");
            errors.reject("StudyEvent has a Status Other than  Scheduled or Started");
            // return errors;
        }
        return errors;
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
     * Update Study Event to Data Entry Started / Completed
     * 
     * @param seBean
     * @param status
     * @param studyBean
     * @param studySubjectBean
     * @return
     */
    private StudyEventBean updateStudyEvent(StudyEventBean seBean, StudyEventDefinitionBean sedBean, StudyBean studyBean, StudySubjectBean studySubjectBean, boolean isAnonymous) {
        SubjectEventStatus newStatus = null;
        int crfCount = 0;
        int completedCrfCount = 0;

        if (!isAnonymous) { 
            if (seBean.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)) newStatus = SubjectEventStatus.DATA_ENTRY_STARTED;
        } else {
            // Get a count of CRFs defined for the event
            if (studyBean.getParentStudyId()!=0)
                crfCount = getCountCrfsInAEventDefCrfForSite(sedBean.getId(),getParentStudy(studyBean.getOid()).getId());
            else 
                crfCount = getCountCrfsInAEventDefCrf(sedBean.getId(),studyBean.getId());
            // Get a count of completed CRFs for the event
            completedCrfCount = getCountCompletedEventCrfsInAStudyEvent(seBean);

            if (crfCount == completedCrfCount){
                if (seBean.getStatus().equals(SubjectEventStatus.SCHEDULED) || seBean.getStatus().equals(SubjectEventStatus.DATA_ENTRY_STARTED)) {
                    newStatus = SubjectEventStatus.COMPLETED;
                } 
            } else if (seBean.getStatus().equals(SubjectEventStatus.SCHEDULED)) {
                newStatus = SubjectEventStatus.DATA_ENTRY_STARTED;
            }
        }

        if (newStatus != null) {
            sedao = new StudyEventDAO(ds);
            seBean.setUpdater(getUserAccount(getInputUsername(studyBean, studySubjectBean)));
            seBean.setUpdatedDate(new Date());
            seBean.setSubjectEventStatus(newStatus);
            seBean = (StudyEventBean) sedao.update(seBean);
            logger.debug("*********UPDATED STUDY EVENT ");
        }
        return seBean;
    }

    /**
     * Update Status in Event CRF Table
     * 
     * @param ecBean
     * @param studyBean
     * @param studySubjectBean
     * @param isAnonymous 
     * @return
     */
    private EventCRFBean updateEventCRF(EventCRFBean ecBean, StudyBean studyBean, StudySubjectBean studySubjectBean, boolean isAnonymous) {
        String inputUsername = getInputUsername(studyBean, studySubjectBean);
        ecBean.setUpdater(getUserAccount(inputUsername));
        ecBean.setUpdatedDate(new Date());
        if (isAnonymous) ecBean.setStatus(Status.UNAVAILABLE);
        else ecBean.setStatus(Status.AVAILABLE);
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
     * @param isAnonymous 
     * @return
     * @throws Exception
     */
    private Errors readDownloadFile(String body, Errors errors, StudyBean studyBean, StudyEventBean studyEventBean, StudySubjectBean studySubjectBean,
            StudyEventDefinitionBean studyEventDefinitionBean, Locale locale, boolean isAnonymous) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(body));
        Document doc = db.parse(is);
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
                        CRFVersionBean cvBean = getCRFVersion(crfVersionOID);
                        logger.info("***crf_version_ :  " + crfVersionOID + " *** ");

                        EventCRFBean eventCrfBean = getCrfVersionCheck(crfVersionOID, errors, studyBean, studyEventBean, studySubjectBean);
                        ArrayList<ItemDataBean> itemDataBeanList = new ArrayList<ItemDataBean>();
                        iddao = new ItemDataDAO(ds);

                        HashMap<Integer,Set<Integer>> groupOrdinalMapping = new HashMap<Integer,Set<Integer>>();

                        if (eventCrfBean == null)
                            return errors;
                        if (eventCrfBean != null) {

                            NodeList groupNodeList = crfNode.getChildNodes();
                            for (int k = 0; k < groupNodeList.getLength(); k = k + 1) {
                                Node groupNode = groupNodeList.item(k);

                                if (groupNode instanceof Element && !groupNode.getNodeName().startsWith("SECTION_")) {

                                    groupNodeName = groupNode.getNodeName();
                                    ItemGroupBean itemGroup = getItemGroupByOID(groupNodeName);
                                    if (itemGroup != null && !groupOrdinalMapping.containsKey(itemGroup.getId())) groupOrdinalMapping.put(itemGroup.getId(),new TreeSet<Integer>());

                                    NodeList itemNodeList = groupNode.getChildNodes();

                                    for (int m = 0; m < itemNodeList.getLength(); m = m + 1) {
                                        Node itemNode = itemNodeList.item(m);
                                        if (itemNode instanceof Element && !itemNode.getNodeName().endsWith(".HEADER")
                                                && !itemNode.getNodeName().endsWith(".SUBHEADER")
                                                && !itemNode.getNodeName().equals("REPEAT_ORDINAL")) {

                                            itemOID = itemNode.getNodeName().trim();
                                            itemValue = itemNode.getTextContent();

                                            ArrayList<ItemBean> iBean = getItemRecord(itemOID);
                                            ItemGroupMetadataBean itemGroupMeta = getItemGroupMetadata(iBean.get(0).getId(),cvBean.getId());
                                            Integer itemOrdinal = getItemOrdinal(groupNode, itemGroupMeta.isRepeatingGroup(),itemDataBeanList,iBean.get(0));
                                            Integer itemId = iBean.get(0).getId();
                                            Integer crfVersionId = cvBean.getId();
                                            ItemFormMetadataBean ifmBean = getItemFromMetadata(itemId, crfVersionId);
                                            Integer responseTypeId = ifmBean.getResponseSet().getResponseType().getId();

                                            if (responseTypeId == 3 || responseTypeId == 7) {
                                                itemValue = itemValue.replaceAll(" ", ",");
                                            }

                                            // Build set of submitted row numbers to be used to find deleted DB rows later
                                            Set<Integer> ordinals = groupOrdinalMapping.get(itemGroup.getId());
                                            ordinals.add(itemOrdinal);
                                            groupOrdinalMapping.put(itemGroup.getId(),ordinals);

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
                                    if (itemDataBean1.getOrdinal() < 0) {
                                        itemDataBean1.setOrdinal(getNextItemDataOrdinal(itemDataBean1.getItemId(),eventCrfBean));
                                        ItemGroupBean itemGroup = getItemGroupByItemIdCrfVersionId(itemDataBean1.getItemId(),eventCrfBean.getCRFVersionId());
                                        groupOrdinalMapping.get(itemGroup.getId()).add(itemDataBean1.getOrdinal());
                                    }
                                    iddao.create(itemDataBean1);
                                } else if (existingValue.getValue().equals(itemDataBean1.getValue())) {
                                    // Value unchanged. Do nothing.
                                } else {
                                    itemDataBean1.setId(existingValue.getId());
                                    itemDataBean1.setUpdater(getUserAccount(getInputUsername(studyBean, studySubjectBean)));
                                    iddao.updateValue(itemDataBean1);
                                }
                            }
                            
                            // Delete rows that have been removed
                            removeDeletedRows(groupOrdinalMapping,eventCrfBean,cvBean,studyBean,studySubjectBean, locale);
                            
                            // Update Event Crf Bean and change the status to Completed
                            eventCrfBean = updateEventCRF(eventCrfBean, studyBean, studySubjectBean, isAnonymous);
                            
                            // Update Study Event to Data Entry Started or Completed
                            updateStudyEvent(studyEventBean, studyEventDefinitionBean, studyBean, studySubjectBean, isAnonymous);
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
     * @param locale 
     * @param isAnonymous 
     * @return
     * @throws Exception
     */
    private Errors readDownloadFileNew(String body, Errors errors, StudyBean studyBean, StudyEventBean studyEventBean, StudySubjectBean studySubjectBean,
            StudyEventDefinitionBean studyEventDefinitionBean, CRFVersionBean crfVersion, Locale locale, boolean isAnonymous) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(body));
        Document doc = db.parse(is);
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

                        HashMap<Integer,Set<Integer>> groupOrdinalMapping = new HashMap<Integer,Set<Integer>>();

                        if (eventCrfBean == null)
                            return errors;
                        if (eventCrfBean != null) {

                            NodeList groupNodeList = crfNode.getChildNodes();
                            // Group loop
                            for (int k = 0; k < groupNodeList.getLength(); k = k + 1) {
                                Node groupNode = groupNodeList.item(k);
                                if (groupNode instanceof Element && !groupNode.getNodeName().startsWith("SECTION_")) {
                                    groupNodeName = groupNode.getNodeName();
                                    ItemGroupBean itemGroup = getItemGroup(crfVersion.getId(), groupNodeName);
                                    if (itemGroup != null && !groupOrdinalMapping.containsKey(itemGroup.getId())) groupOrdinalMapping.put(itemGroup.getId(),new TreeSet<Integer>());

                                    NodeList itemNodeList = groupNode.getChildNodes();
                                    // Item loop
                                    for (int m = 0; m < itemNodeList.getLength(); m = m + 1) {
                                        Node itemNode = itemNodeList.item(m);
                                        if (itemNode instanceof Element && !itemNode.getNodeName().endsWith(".HEADER")
                                                && !itemNode.getNodeName().endsWith(".SUBHEADER")
                                                && !itemNode.getNodeName().equals("REPEAT_ORDINAL")) {

                                            itemName = itemNode.getNodeName().trim();
                                            itemValue = itemNode.getTextContent();

                                            ItemBean iBean = getItemRecord(itemName, crfVersion);
                                            ItemGroupMetadataBean itemGroupMeta = getItemGroupMetadata(iBean.getId(),crfVersion.getId());
                                            Integer itemOrdinal = getItemOrdinal(groupNode, itemGroupMeta.isRepeatingGroup(),itemDataBeanList,iBean);

                                            CRFVersionBean cvBean = getCRFVersion(crfVersion.getOid());
                                            Integer itemId = iBean.getId();
                                            Integer crfVersionId = cvBean.getId();
                                            ItemFormMetadataBean ifmBean = getItemFromMetadata(itemId, crfVersionId);
                                            Integer responseTypeId = ifmBean.getResponseSet().getResponseType().getId();

                                            if (responseTypeId == 3 || responseTypeId == 7) {
                                                itemValue = itemValue.replaceAll(" ", ",");
                                            }

                                            // Build set of submitted row numbers to be used to find deleted DB rows later
                                            Set<Integer> ordinals = groupOrdinalMapping.get(itemGroup.getId());
                                            ordinals.add(itemOrdinal);
                                            groupOrdinalMapping.put(itemGroup.getId(),ordinals);

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
                                        if (itemDataBean1.getOrdinal() < 0) {
                                            itemDataBean1.setOrdinal(getNextItemDataOrdinal(itemDataBean1.getItemId(),eventCrfBean));
                                            ItemGroupBean itemGroup = getItemGroupByItemIdCrfVersionId(itemDataBean1.getItemId(),eventCrfBean.getCRFVersionId());
                                            groupOrdinalMapping.get(itemGroup.getId()).add(itemDataBean1.getOrdinal());
                                        }
                                    iddao.create(itemDataBean1);
                                } else if (existingValue.getValue().equals(itemDataBean1.getValue())) {
                                    // Value unchanged. Do nothing.
                                } else {
                                    itemDataBean1.setId(existingValue.getId());
                                    itemDataBean1.setUpdater(getUserAccount(getInputUsername(studyBean, studySubjectBean)));
                                    iddao.updateValue(itemDataBean1);
                                }

                            }
                            
                            // Delete rows that have been removed
                            removeDeletedRows(groupOrdinalMapping,eventCrfBean,crfVersion,studyBean,studySubjectBean, locale);
                            
                            // Update Event Crf Bean and change the status to Completed
                            eventCrfBean = updateEventCRF(eventCrfBean, studyBean, studySubjectBean, isAnonymous);
                            
                            // Update Study Event to Data Entry Started or Completed
                            updateStudyEvent(studyEventBean, studyEventDefinitionBean, studyBean, studySubjectBean, isAnonymous);

                        }
                    }
                }
            }
        }
        return errors;
    }

    private void removeDeletedRows(HashMap<Integer, Set<Integer>> groupOrdinalMapping, EventCRFBean eventCrf, CRFVersionBean crfVersion, StudyBean studyBean, StudySubjectBean studySubjectBean, Locale locale) {
        Iterator<Integer> keys = groupOrdinalMapping.keySet().iterator();
        while (keys.hasNext()) {
            Integer itemGroupId = keys.next();
            List<ItemDataBean> itemDatas = getItemDataByEventCrfGroup(eventCrf.getId(),itemGroupId);
            for (ItemDataBean itemData:itemDatas) {
                if (!groupOrdinalMapping.get(itemGroupId).contains(itemData.getOrdinal()) && !itemData.isDeleted()){
                    itemData.setDeleted(true);
                    itemData.setValue("");
                    itemData.setOldStatus(itemData.getStatus());
                    itemData.setOwner(getUserAccount(getInputUsername(studyBean, studySubjectBean)));
                    itemData.setStatus(Status.AVAILABLE);
                    itemData.setUpdater(getUserAccount(getInputUsername(studyBean, studySubjectBean)));
                    iddao.updateUser(itemData);
                    iddao.update(itemData);                    
                    // Set update ID
                }
                
                //Close discrepancy notes
                closeItemDiscrepancyNotes(itemData, studyBean, studySubjectBean, locale);
                
            }
            
        }
        
    }

    private List<ItemDataBean> getItemDataByEventCrfGroup(int id, Integer itemGroupId) {
        iddao = new ItemDataDAO(ds);
        List<ItemDataBean> itemDatas= iddao.findAllByEventCRFIdAndItemGroupId(id,itemGroupId);
        return itemDatas;
    }

    private Integer getNextItemDataOrdinal(Integer itemId, EventCRFBean eventCrf) {
        iddao = new ItemDataDAO(ds);
        Integer maxOrdinal = iddao.getMaxOrdinalForGroupByItemAndEventCrf(itemId, eventCrf);
        return maxOrdinal + 1;
    }

    private ItemGroupBean getItemGroup(int id, String nodeName) {    
        igdao = new ItemGroupDAO(ds);
        return igdao.findGroupByGroupNameAndCrfVersionId(nodeName, id);
    }

    private ItemGroupBean getItemGroupByOID(String oid) {    
        igdao = new ItemGroupDAO(ds);
        return igdao.findByOid(oid);
    }

    private ItemGroupBean getItemGroupByItemIdCrfVersionId(Integer itemId, Integer crfVersionId) {    
        igdao = new ItemGroupDAO(ds);
        return igdao.findGroupByItemIdCrfVersionId(itemId, crfVersionId);
    }

    private Integer getItemOrdinal(Node groupNode, boolean isRepeating, ArrayList<ItemDataBean> itemDataBeanList, ItemBean iBean) {
        if (!isRepeating) return 1;
        
        int ordinal = -1;
        NodeList items = groupNode.getChildNodes();
        for(int i=0; i<items.getLength();i++){
            Node item = items.item(i);
            if (item instanceof Element && ((Element) item).getTagName().equals("REPEAT_ORDINAL") && !((Element) item).getTextContent().equals(""))
                ordinal = Integer.valueOf(((Element)item).getTextContent());
        }
        
        // Enketo specific code here due to Enketo behavior of defaulting in values from first repeat on new repeating
        // group row entries, including the REPEAT_ORDINAL value.
        // If the current value of REPEAT_ORDINAL already exists in the ItemDataBean list for this Item, the current 
        // value must be reset to -1 as this is a new repeating group row.
        for (ItemDataBean itemdata:itemDataBeanList) {
            if (itemdata.getItemId() == iBean.getId() && itemdata.getOrdinal() == ordinal) {
                ordinal = -1;
                break;
            }
        }

        return ordinal;
    }

    private void closeItemDiscrepancyNotes(ItemDataBean itemdata, StudyBean study, StudySubjectBean studySubject, Locale locale) {
        
        ResourceBundleProvider.updateLocale(locale);
        ResourceBundle resword = ResourceBundleProvider.getWordsBundle(locale);
        dndao = new DiscrepancyNoteDAO(ds);
            
        // Notes & Discrepancies must be set to "closed" when event CRF is deleted
        // parentDiscrepancyNoteList is the list of the parent DNs records only
        ArrayList<DiscrepancyNoteBean> parentDiscrepancyNoteList = dndao.findParentNotesOnlyByItemData(itemdata.getId());
        for (DiscrepancyNoteBean parentDiscrepancyNote : parentDiscrepancyNoteList) {
            if (parentDiscrepancyNote.getResolutionStatusId() != 4) { // if the DN's resolution status is not set to Closed
                String description = resword.getString("dn_auto-closed_description");
                String detailedNotes =resword.getString("dn_auto_closed_item_detailed_notes");
                // create new DN record , new DN Map record , also update the parent record
                DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
                dnb.setEntityId(itemdata.getId()); // this is needed for DN Map object
                dnb.setStudyId(study.getId());
                dnb.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
                dnb.setDescription(description);
                dnb.setDetailedNotes(detailedNotes);
                dnb.setDiscrepancyNoteTypeId(parentDiscrepancyNote.getDiscrepancyNoteTypeId()); // set to parent DN Type Id
                dnb.setResolutionStatusId(4); // set to closed
                dnb.setColumn("value"); // this is needed for DN Map object
                dnb.setAssignedUserId(getUserAccount(getInputUsername(study, studySubject)).getId());
                dnb.setOwner(getUserAccount(getInputUsername(study, studySubject)));
                dnb.setParentDnId(parentDiscrepancyNote.getId());
                dnb.setActivated(false);
                dnb = (DiscrepancyNoteBean) dndao.create(dnb); // create child DN
                dndao.createMapping(dnb); // create DN mapping

                DiscrepancyNoteBean itemParentNote = (DiscrepancyNoteBean) dndao.findByPK(dnb.getParentDnId());
                itemParentNote.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
                itemParentNote.setAssignedUserId(getUserAccount(getInputUsername(study, studySubject)).getId());
                dndao.update(itemParentNote); // update parent DN
                dndao.updateAssignedUser(itemParentNote); // update parent DN assigned user
            }
        }
        iddao = new ItemDataDAO(ds);
        ItemDataBean idBean = (ItemDataBean) iddao.findByPK(itemdata.getId());

        // Updating Dn_item_data_map actovated column into false for the existing DNs
        ArrayList<DiscrepancyNoteBean> dnBeans = dndao.findExistingNotesForItemData(itemdata.getId());
        if (dnBeans.size() != 0) {
            DiscrepancyNoteBean dnBean = new DiscrepancyNoteBean();
            dnBean.setEntityId(itemdata.getId());
            dnBean.setActivated(false);
            dndao.updateDnMapActivation(dnBean);
        }

    }
    
}