package org.akaza.openclinica.controller.openrosa.processor;

import static org.akaza.openclinica.service.crfdata.EnketoUrlService.ENKETO_ORDINAL;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akaza.openclinica.controller.openrosa.QueryService;
import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.controller.openrosa.SubmissionContainer.FieldRequestTypeEnum;
import org.akaza.openclinica.controller.openrosa.SubmissionProcessorChain.ProcessorEnum;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.ItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupMetadataDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.akaza.openclinica.domain.xform.XformParserHelper;
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
    private ItemDao itemDao;
    @Autowired
    private ItemFormMetadataDao itemFormMetadataDao;
    @Autowired
    private ItemGroupMetadataDao itemGroupMetadataDao;
    @Autowired
    private ItemGroupDao itemGroupDao;
    @Autowired
    private CrfVersionDao crfVersionDao;

    XformParserHelper xformParserHelper = new XformParserHelper();

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

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
                itemNodeSet = xformParserHelper.instanceItemNodes(instanceNode, itemNodeSet);
                if (itemNodeSet.size() != 0) {
                    itemNode = itemNodeSet.iterator().next();
                    processFieldSubmissionGroupItems(listOfUploadFilePaths, repeatNode, itemNode, container);
                }

            }
        }

        return ProcessorEnum.PROCEED;

    }

    private void processFieldSubmissionGroupItems(ArrayList<HashMap> listOfUploadFilePaths, Node repeatNode, Node itemNode, SubmissionContainer container)
            throws Exception {
        String itemName;
        Integer itemOrdinal = 0;
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

        CrfVersion crfVersion = container.getCrfVersion();
        Item item = itemDao.findByNameCrfId(itemNode.getNodeName(), crfVersion.getCrf().getCrfId());
        ItemGroupMetadata igm = itemGroupMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());
        ItemGroup itemGroup = igm.getItemGroup();

        if (container.getRequestType() == FieldRequestTypeEnum.DELETE_FIELD) {
            ItemData existingItemData = lookupFieldItemData(itemGroup, itemOrdinal, container);

            existingItemData.setDeleted(true);
            existingItemData.setValue("");
            existingItemData.setOldStatus(existingItemData.getStatus());
            existingItemData.setUserAccount(container.getUser());
            existingItemData.setStatus(Status.AVAILABLE);
            existingItemData.setUpdateId(container.getUser().getUserId());
            existingItemData = itemDataDao.saveOrUpdate(existingItemData);

            // Close discrepancy notes
            closeItemDiscrepancyNotes(container, existingItemData);
            return;
        }

        // Item loop
        QueryServiceHelperBean helperBean = new QueryServiceHelperBean();
        if (queryService.getQueryAttribute(helperBean, itemNode) != null) {
            queryService.process(helperBean, container, itemNode, itemOrdinal);
        } else if (shouldProcessItemNode(itemNode)) {

            itemName = itemNode.getNodeName().trim();
            itemValue = itemNode.getTextContent();

            if (item == null) {
                logger.error("Failed to lookup item: '" + itemName + "'.  Continuing with submission.");
            }

            ItemFormMetadata itemFormMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), container.getCrfVersion().getCrfVersionId());

            // Convert space separated Enketo multiselect values to comma separated OC multiselect values
            Integer responseTypeId = itemFormMetadata.getResponseSet().getResponseType().getResponseTypeId();
            if (responseTypeId == 3 || responseTypeId == 7) {
                itemValue = itemValue.replaceAll(" ", ",");
            }
            if (responseTypeId == 4) {
                for (HashMap uploadFilePath : listOfUploadFilePaths) {
                    if ((boolean) uploadFilePath.containsKey(itemValue) && itemValue != "") {
                        itemValue = (String) uploadFilePath.get(itemValue);
                        break;
                    }
                }
            }

            ItemData newItemData = createItemData(item, itemValue, itemOrdinal, container.getEventCrf(), container.getStudy(), container.getSubject(),
                    container.getUser());
            Errors itemErrors = validateItemData(newItemData, item, responseTypeId);
            if (itemErrors.hasErrors()) {
                container.getErrors().addAllErrors(itemErrors);
                throw new Exception("Item validation error.  Rolling back submission changes.");
            }

            ItemData existingItemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), container.getEventCrf().getEventCrfId(), itemOrdinal);
            if (existingItemData == null) {
                newItemData.setStatus(Status.UNAVAILABLE);
                itemDataDao.saveOrUpdate(newItemData);

            } else if (existingItemData.getValue().equals(newItemData.getValue())) {

            } else {
                // Existing item. Value changed. Update existing value.
                existingItemData.setValue(newItemData.getValue());
                existingItemData.setUpdateId(container.getUser().getUserId());
                existingItemData.setDateUpdated(new Date());
                itemDataDao.saveOrUpdate(existingItemData);
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

}