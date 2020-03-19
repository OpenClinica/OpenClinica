package org.akaza.openclinica.controller.openrosa.processor;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.service.StudyBuildService;
import org.akaza.openclinica.controller.openrosa.QueryService;
import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.controller.openrosa.SubmissionContainer.FieldRequestTypeEnum;
import org.akaza.openclinica.controller.openrosa.SubmissionProcessorChain.ProcessorEnum;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.xform.XformParserHelper;
import core.org.akaza.openclinica.service.randomize.RandomizationService;
import org.akaza.openclinica.domain.enumsupport.SdvStatus;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static core.org.akaza.openclinica.service.crfdata.EnketoUrlService.ENKETO_ORDINAL;

@Component
@Order(value = 7)
public class FSItemProcessor extends AbstractItemProcessor implements Processor {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private QueryService queryService;
    @Autowired
    private ItemDataDao itemDataDao;
    @Autowired
    private EventCrfDao eventCrfDao;
    @Autowired
    private StudyEventDao studyEventDao;
    @Autowired
    private StudySubjectDao studySubjectDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemFormMetadataDao itemFormMetadataDao;
    @Autowired
    private ItemGroupMetadataDao itemGroupMetadataDao;
    @Autowired
    private ItemGroupDao itemGroupDao;
    @Autowired
    private CrfVersionDao crfVersionDao;
    @Autowired
    private XformParserHelper xformParserHelper;
    @Autowired
    FormLayoutMediaDao formLayoutMediaDao;
    @Autowired
    AuditLogEventDao auditLogEventDao;
    @Autowired
    RepeatCountDao repeatCountDao;

    @Autowired
    private RandomizationService randomizationService;
    @Autowired
    private StudyBuildService studyBuildService;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String STUDYEVENT = "study_event";
    public static final String STUDYSUBJECT = "study_subject";
    public static final String REPEATCOUNT = "_count";

    public static final String OC_CONTACTDATA = "oc:contactdata";
    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    public static final String SECONDARYID = "secondaryid";
    public static final String EMAIL = "email";
    public static final String MOBILENUMBER = "mobilenumber";

    public static final String US_PHONE_PREFIX = "+1 ";
	public static final String US_PHONE_PATTERN = "^[0-9]{10,10}$";


    public ProcessorEnum process(SubmissionContainer container) throws Exception {

        logger.info("Executing FSItem Processor.");

        // TODO keep this flag
        if (container.isFieldSubmissionFlag() != true)
            return ProcessorEnum.PROCEED;
        ArrayList<HashMap> listOfUploadFilePaths = container.getListOfUploadFilePaths();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(container.getRequestBody()));
        Document doc = db.parse(is);
        Set<Node> itemNodeSet = new HashSet();
        Set<Node> repeatNodeSet = new HashSet();
        Node itemNode = null;
        Node repeatNode = null;
        NodeList instanceNodeList = doc.getElementsByTagName("instance");
        // Instance loop
        for (int i = 0; i < instanceNodeList.getLength(); i = i + 1) {
            Node instanceNode = instanceNodeList.item(i);
            if (instanceNode instanceof Element) {

                repeatNodeSet = xformParserHelper.instanceEnketoAttr(instanceNode, repeatNodeSet);
                if (repeatNodeSet.size() != 0) {
                    repeatNode = repeatNodeSet.iterator().next();
                }
                ItemGroup itemGroup = null;
                if (container.getRequestType() == FieldRequestTypeEnum.DELETE_FIELD) {
                    List<String> instanceItemsPath = new ArrayList<>();
                    instanceItemsPath = xformParserHelper.instanceItemPaths(instanceNode, instanceItemsPath, "", null);
                    List<ItemGroup> itemGroups = itemGroupDao.findByCrfVersionId(container.getCrfVersion().getCrfVersionId());
                    int idx = instanceItemsPath.get(0).lastIndexOf("/");
                    String rPath = instanceItemsPath.get(0).substring(idx + 1);
                    for (ItemGroup ig : itemGroups) {
                        if (ig.getLayoutGroupPath() != null && ig.getLayoutGroupPath().equals(rPath)) {
                            itemGroup = ig;
                            break;
                        }
                    }
                }

                itemNodeSet = xformParserHelper.instanceItemNodes(instanceNode, itemNodeSet);
                if (itemNodeSet.size() != 0) {
                    itemNode = itemNodeSet.iterator().next();

                    for (int j = 0; j < itemNode.getAttributes().getLength(); j++) {
                        Attr attr = (Attr) itemNode.getAttributes().item(j);
                        if (attr.getNodeName().equals(OC_CONTACTDATA)) {
                            saveContactData(attr.getNodeValue(),itemNode.getTextContent(), container);
                            return ProcessorEnum.PROCEED;
                        }
                    }

                    processFieldSubmissionGroupItems(listOfUploadFilePaths, repeatNode, itemNode, container, itemGroup);
                }
            }
        }

        return ProcessorEnum.PROCEED;

    }

    private void processFieldSubmissionGroupItems(ArrayList<HashMap> listOfUploadFilePaths, Node repeatNode, Node itemNode, SubmissionContainer container,
            ItemGroup itemGroup) throws Exception {
        String itemName;
        Integer itemOrdinal = 1;
        String itemValue;

        // Node repeatGroupNode = itemNode.getParentNode();
        if (repeatNode != null) {
            final NamedNodeMap attributes = repeatNode.getAttributes();
            // check to see if groupNode has any enketo attributes
            for (int attrIndex = 0; attrIndex < attributes.getLength(); attrIndex++) {
                if (attributes.item(attrIndex).getNodeName().equals(ENKETO_ORDINAL)) {
                    logger.debug("found enketo attribute");
                    logger.debug(attributes.item(attrIndex).getNodeName());
                    logger.debug(attributes.item(attrIndex).getNodeValue());
                    itemOrdinal = new Integer(attributes.item(attrIndex).getNodeValue());
                }
            }
        } else {
            itemOrdinal = 1;
        }

        FormLayout formLayout = container.getFormLayout();
        CrfVersion crfVersion = crfVersionDao.findAllByCrfId(formLayout.getCrf().getCrfId()).get(0);
        container.setCrfVersion(crfVersion);
        Item item = null;
        ItemGroupMetadata igm = null;

        if (container.getRequestType() == FieldRequestTypeEnum.DELETE_FIELD) {
            List<ItemGroupMetadata> igms = itemGroupMetadataDao.findByItemGroupCrfVersion(itemGroup.getItemGroupId(), crfVersion.getCrfVersionId());

            for (ItemGroupMetadata ig : igms) {
                ItemData existingItemData = itemDataDao.findByItemEventCrfOrdinal(ig.getItem().getItemId(), container.getEventCrf().getEventCrfId(),
                        itemOrdinal);
                int maxRowCount = itemDataDao.getMaxCountByEventCrfGroup(container.getEventCrf().getEventCrfId(), ig.getItemGroup().getItemGroupId());

                // ItemData existingItemData = lookupFieldItemData(itemGroup, itemOrdinal, container);
                if (existingItemData != null) {
                    existingItemData.setDeleted(true);
                    existingItemData.setValue("");
                    existingItemData.setOldStatus(existingItemData.getStatus());
                    existingItemData.setUserAccount(container.getUser());
                    existingItemData.setStatus(Status.AVAILABLE);
                    existingItemData.setUpdateId(container.getUser().getUserId());
                    existingItemData.setInstanceId(container.getInstanceId());
                    existingItemData = itemDataDao.saveOrUpdate(existingItemData);
                    updateEventSubjectStatusIfSigned(container);
                    resetSdvStatus(container);

                    // Close discrepancy notes
                    closeItemDiscrepancyNotes(container, existingItemData);
                } else if (itemOrdinal < maxRowCount) {
                    ItemData newItemData = createItemData(ig.getItem(), "", itemOrdinal, container);
                    newItemData.setDeleted(true);
                    newItemData = itemDataDao.saveOrUpdate(newItemData);
                    updateEventSubjectStatusIfSigned(container);
                }
            }
            return;
        }

        // igm = itemGroupMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());

        // Item loop
        QueryServiceHelperBean helperBean = new QueryServiceHelperBean();
        if (queryService.getQueryAttribute(helperBean, itemNode) != null) {
            queryService.process(helperBean, container, itemNode, itemOrdinal);
        } else if (shouldProcessItemNode(itemNode)) {

            itemName = itemNode.getNodeName().trim();
            itemValue = itemNode.getTextContent();

            item = itemDao.findByNameCrfId(itemNode.getNodeName(), crfVersion.getCrf().getCrfId());
            if (item != null) {
                ItemFormMetadata itemFormMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());

                // Convert space separated Enketo multiselect values to comma separated OC multiselect values
                Integer responseTypeId = itemFormMetadata.getResponseSet().getResponseType().getResponseTypeId();
                if (responseTypeId == 3 || responseTypeId == 7) {
                    itemValue = itemValue.replaceAll(" ", ",");
                }
                if (responseTypeId == 4) {
                    /*
                     * for (HashMap uploadFilePath : listOfUploadFilePaths) {
                     * if ((boolean) uploadFilePath.containsKey(itemValue) && itemValue != "") {
                     * itemValue = (String) uploadFilePath.get(itemValue);
                     * break;
                     * }
                     * }
                     */ FormLayoutMedia media = formLayoutMediaDao.findByEventCrfIdAndFileName(container.getEventCrf().getEventCrfId(), itemValue);
                    if (media == null) {
                        media = new FormLayoutMedia();
                    }
                    media.setName(itemValue);
                    media.setFormLayout(formLayout);
                    media.setEventCrfId(container.getEventCrf().getEventCrfId());
                    media.setPath("attached_files/" + container.getStudy().getOc_oid() + "/");

                    formLayoutMediaDao.saveOrUpdate(media);
                }

                ItemData newItemData = createItemData(item, itemValue, itemOrdinal, container);
                Errors itemErrors = validateItemData(newItemData, item, responseTypeId);
                if (itemErrors.hasErrors()) {
                    container.getErrors().addAllErrors(itemErrors);
                    throw new Exception("Item validation error.  Rolling back submission changes.");
                }

                ItemData existingItemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), container.getEventCrf().getEventCrfId(), itemOrdinal);
                ItemData randomizeDataCheck = null;
                if (existingItemData == null) {
                    newItemData.setStatus(Status.UNAVAILABLE);
                    itemDataDao.saveOrUpdate(newItemData);
                    updateEventSubjectStatusIfSigned(container);
                    resetSdvStatus(container);
                    randomizeDataCheck = newItemData;
                } else if (!existingItemData.getValue().equals(newItemData.getValue())) {
                    // Existing item. Value changed. Update existing value.
                    existingItemData.setInstanceId(container.getInstanceId());
                    existingItemData.setValue(newItemData.getValue());
                    existingItemData.setUpdateId(container.getUser().getUserId());
                    existingItemData.setDateUpdated(new Date());
                    itemDataDao.saveOrUpdate(existingItemData);
                    updateEventSubjectStatusIfSigned(container);
                    resetSdvStatus(container);
                    randomizeDataCheck = existingItemData;
                }
                // check for Randomization
                try {
                    checkRandomization(randomizeDataCheck, container);
                } catch (Exception e) {
                    logger.error("<RANDOMIZE> Failed checkRandomization for form: " + formLayout.getCrf().getOcOid() + e);
                }
            } else {
                logger.error("Failed to lookup item: '" + itemName + "'.  Continuing with submission.");
                int lastIndexOf = itemName.lastIndexOf(REPEATCOUNT);
                ItemGroup iGroup = null;
                if(lastIndexOf!=-1) {
                    iGroup = itemGroupDao.findByCrfAndGroupLayout(formLayout.getCrf(), itemName.substring(0, lastIndexOf));
                    if (iGroup != null) {
                        saveOrUpdateRepeatCount(container, itemName, itemValue);
                    }
                }else {
                    logger.error("Field Submission failed ");
                    throw new Exception(" Field Submission failed due to Item '" + itemName + "' does not exist in form");
                }
            }
        }
    }

    private void checkRandomization(ItemData thisItemData, SubmissionContainer container) throws Exception {
        Study parentPublicStudy = studyBuildService.getParentPublicStudy(thisItemData.getEventCrf().getStudySubject().getStudy().getOc_oid());
        Map<String, String> subjectContext =  container.getSubjectContext();
        String accessToken = subjectContext.get("accessToken");
        String studySubjectOID = container.getSubject().getOcOid();
        randomizationService.processRandomization(parentPublicStudy, accessToken, studySubjectOID, thisItemData);
    }

    private boolean shouldProcessItemNode(Node itemNode) {
        return itemNode instanceof Element && !itemNode.getNodeName().endsWith(".HEADER") && !itemNode.getNodeName().endsWith(".SUBHEADER")
                && !itemNode.getNodeName().equals("OC.REPEAT_ORDINAL") && !itemNode.getNodeName().equals("OC.STUDY_SUBJECT_ID")
                && !itemNode.getNodeName().equals("OC.STUDY_SUBJECT_ID_CONFIRM");
    }

    private ItemData lookupFieldItemData(ItemGroup itemGroup, Integer ordinal, SubmissionContainer container) {
        return itemDataDao.findByEventCrfGroupOrdinal(container.getEventCrf(), itemGroup.getItemGroupId(), ordinal);
    }

    private void resetSdvStatus(SubmissionContainer container) {
        EventCrf eventCrf = container.getEventCrf();
        if(eventCrf.getSdvStatus() == null)
            eventCrf.setSdvStatus(SdvStatus.NOT_VERIFIED);
        else if(eventCrf.getSdvStatus() != null && eventCrf.getSdvStatus() == SdvStatus.VERIFIED)
            eventCrf.setSdvStatus(SdvStatus.CHANGED_AFTER_VERIFIED);
        eventCrf.setSdvUpdateId(container.getUser().getUserId());
        eventCrfDao.saveOrUpdate(eventCrf);
    }

    private void updateEventSubjectStatusIfSigned(SubmissionContainer container) {
        StudyEvent studyEvent = container.getEventCrf().getStudyEvent();
        if (studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SIGNED) ) {
            String eventOldworkflowStatus = StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED.toString();
            AuditLogEvent eventAuditLogEvent = new AuditLogEvent();
            eventAuditLogEvent.setAuditTable(STUDYEVENT);
            eventAuditLogEvent.setEntityId(studyEvent.getStudyEventId());
            eventAuditLogEvent.setEntityName("Status");
            eventAuditLogEvent.setAuditLogEventType(new AuditLogEventType(31));
            eventAuditLogEvent.setNewValue(String.valueOf(StudyEventWorkflowStatusEnum.SIGNED));

            List<AuditLogEvent> eventAles = auditLogEventDao.findByParam(eventAuditLogEvent);
            for (AuditLogEvent audit : eventAles) {
                eventOldworkflowStatus = audit.getOldValue();
                break;
            }
            studyEvent.setUpdateId(container.getUser().getUserId());
            studyEvent.setDateUpdated(new Date());
            studyEventDao.saveOrUpdate(studyEvent);
        }
        StudySubject studySubject = container.getSubject();

        // This code previously existed within the above if block. This resulted in a bug where if an event was scheduled
        // for a participant that was already signed then entering data in that new event would bypass the logic. By moving
        // it here the subject status should be properly updated, regardless of the status of any other events.
        if (studySubject.getStatus() == Status.SIGNED) {
            String subjectOldStatusId = "1";
            AuditLogEvent subjectAuditLogEvent = new AuditLogEvent();
            subjectAuditLogEvent.setAuditTable(STUDYSUBJECT);
            subjectAuditLogEvent.setEntityId(studySubject.getStudySubjectId());
            subjectAuditLogEvent.setEntityName("Status");
            subjectAuditLogEvent.setAuditLogEventType(new AuditLogEventType(3));
            subjectAuditLogEvent.setNewValue(String.valueOf(StudyEventWorkflowStatusEnum.SIGNED));

            List<AuditLogEvent> subjectAles = auditLogEventDao.findByParam(subjectAuditLogEvent);
            for (AuditLogEvent audit : subjectAles) {
                subjectOldStatusId = audit.getOldValue();
                break;
            }
            studySubject.setStatus(Status.getByCode(Integer.valueOf(subjectOldStatusId)));
            studySubject.setUpdateId(container.getUser().getUserId());
            studySubject.setDateUpdated(new Date());
            studySubjectDao.saveOrUpdate(studySubject);
        }
    }

    public void saveOrUpdateRepeatCount(SubmissionContainer container, String itemName, String itemValue) {
        RepeatCount repeatCount = repeatCountDao.findByEventCrfIdAndRepeatName(container.getEventCrf().getEventCrfId(), itemName);
        if (repeatCount == null) {
            repeatCount = new RepeatCount();
            repeatCount.setEventCrf(container.getEventCrf());
            repeatCount.setGroupName(itemName);
            repeatCount.setGroupCount(itemValue);
            repeatCount.setDateCreated(new Date());
            repeatCount.setUserAccount(container.getUser());
            repeatCountDao.saveOrUpdate(repeatCount);
        } else if (repeatCount != null && !repeatCount.getGroupCount().equals(itemValue)) {
            repeatCount.setGroupCount(itemValue);
            repeatCount.setUpdateId(container.getUser().getUserId());
            repeatCount.setDateUpdated(new Date());
            repeatCountDao.saveOrUpdate(repeatCount);
        }
    }

    private void saveContactData(String attrValue, String itemValue,SubmissionContainer container) {
        StudySubject studySubject= container.getSubject();
        setStudySubjectDetail(studySubject);
        studySubject.setUpdateId(container.getUser().getUserId());
        studySubject.setDateUpdated(new Date());

        StudySubjectDAO ssdao = new StudySubjectDAO(dataSource);
        UserAccountDAO udao = new UserAccountDAO(dataSource);
        UserAccountBean userAccountBean=(UserAccountBean) udao.findByPK(container.getUser().getUserId());

        StudySubjectBean studySubjectBean=(StudySubjectBean) ssdao.findByPK(studySubject.getStudySubjectId());
        studySubjectBean.setUpdatedDate(new Date());
        studySubjectBean.setUpdater(userAccountBean);
        ssdao.update(studySubjectBean);

        if (attrValue.equals(FIRSTNAME)) {
            studySubject.getStudySubjectDetail().setFirstName(itemValue);
            studySubjectDao.saveOrUpdate(studySubject);
        } else if (attrValue.equals(LASTNAME)) {
            studySubject.getStudySubjectDetail().setLastName(itemValue);
            studySubjectDao.saveOrUpdate(studySubject);
        } else if (attrValue.equals(SECONDARYID)) {
            studySubject.getStudySubjectDetail().setIdentifier(itemValue);
            studySubjectDao.saveOrUpdate(studySubject);
        } else if (attrValue.equals(EMAIL)) {
            studySubject.getStudySubjectDetail().setEmail(itemValue);
            studySubjectDao.saveOrUpdate(studySubject);
        } else if (attrValue.equals(MOBILENUMBER)) {
            Pattern usPhonePattern = Pattern.compile(US_PHONE_PATTERN);
            Matcher usPhoneMatch = usPhonePattern.matcher(itemValue);
            if(usPhoneMatch.matches() && itemValue.length()==10) {
                itemValue=US_PHONE_PREFIX+itemValue;
            }
            studySubject.getStudySubjectDetail().setPhone(itemValue);
            studySubjectDao.saveOrUpdate(studySubject);
        }
    }

    private void setStudySubjectDetail(StudySubject studySubject) {
        if (studySubject.getStudySubjectDetail() == null) {
            StudySubjectDetail studySubjectDetail = new StudySubjectDetail();
            studySubject.setStudySubjectDetail(studySubjectDetail);
        }

    }


}