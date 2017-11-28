package org.akaza.openclinica.controller.openrosa.processor;

import static org.akaza.openclinica.service.crfdata.EnketoUrlService.ENKETO_ORDINAL;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.controller.openrosa.QueryService;
import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.controller.openrosa.SubmissionContainer.FieldRequestTypeEnum;
import org.akaza.openclinica.controller.openrosa.SubmissionProcessorChain.ProcessorEnum;
import org.akaza.openclinica.dao.hibernate.AuditLogEventDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutMediaDao;
import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.ItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.akaza.openclinica.domain.datamap.AuditLogEventType;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.FormLayoutMedia;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.xform.XformParserHelper;
import org.akaza.openclinica.service.CustomRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
@Order(value = 7)
public class FSItemProcessor extends AbstractItemProcessor implements Processor {

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

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String STUDYEVENT = "study_event";
    public static final String STUDYSUBJECT = "study_subject";

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
                if (existingItemData == null) {
                    newItemData.setStatus(Status.UNAVAILABLE);
                    itemDataDao.saveOrUpdate(newItemData);
                    updateEventSubjectStatusIfSigned(container);
                    resetSdvStatus(container);

                } else if (existingItemData.getValue().equals(newItemData.getValue())) {

                } else {
                    // Existing item. Value changed. Update existing value.
                    existingItemData.setInstanceId(container.getInstanceId());
                    existingItemData.setValue(newItemData.getValue());
                    existingItemData.setUpdateId(container.getUser().getUserId());
                    existingItemData.setDateUpdated(new Date());
                    itemDataDao.saveOrUpdate(existingItemData);
                    updateEventSubjectStatusIfSigned(container);
                    resetSdvStatus(container);
                }
            } else {
                logger.error("Failed to lookup item: '" + itemName + "'.  Continuing with submission.");
                throw new CustomRuntimeException("Item does not exist", null);
            }
        }
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
        eventCrf.setSdvStatus(false);
        eventCrf.setSdvUpdateId(container.getUser().getUserId());
        eventCrfDao.saveOrUpdate(eventCrf);
    }

    private void updateEventSubjectStatusIfSigned(SubmissionContainer container) {
        StudyEvent studyEvent = container.getEventCrf().getStudyEvent();
        if (studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SIGNED.getId()) {
            String eventOldStatusId = "3";
            AuditLogEvent eventAuditLogEvent = new AuditLogEvent();
            eventAuditLogEvent.setAuditTable(STUDYEVENT);
            eventAuditLogEvent.setEntityId(studyEvent.getStudyEventId());
            eventAuditLogEvent.setEntityName("Status");
            eventAuditLogEvent.setAuditLogEventType(new AuditLogEventType(31));
            eventAuditLogEvent.setNewValue(String.valueOf(SubjectEventStatus.SIGNED.getId()));

            List<AuditLogEvent> eventAles = auditLogEventDao.findByParam(eventAuditLogEvent);
            for (AuditLogEvent audit : eventAles) {
                eventOldStatusId = audit.getOldValue();
                break;
            }
            studyEvent.setSubjectEventStatusId(Integer.valueOf(eventOldStatusId));
            studyEvent.setUpdateId(container.getUser().getUserId());
            studyEvent.setDateUpdated(new Date());
            studyEventDao.saveOrUpdate(studyEvent);
            StudySubject studySubject = container.getSubject();

            if (studySubject.getStatus() == Status.SIGNED) {
                String subjectOldStatusId = "1";
                AuditLogEvent subjectAuditLogEvent = new AuditLogEvent();
                subjectAuditLogEvent.setAuditTable(STUDYSUBJECT);
                subjectAuditLogEvent.setEntityId(studySubject.getStudySubjectId());
                subjectAuditLogEvent.setEntityName("Status");
                subjectAuditLogEvent.setAuditLogEventType(new AuditLogEventType(3));
                subjectAuditLogEvent.setNewValue(String.valueOf(SubjectEventStatus.SIGNED.getId()));

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
    }

}