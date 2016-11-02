package org.akaza.openclinica.controller.openrosa.processor;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.ItemFormMetadataDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static org.akaza.openclinica.controller.openrosa.SubmissionContainer.FieldRequestTypeEnum;
import static org.akaza.openclinica.controller.openrosa.SubmissionProcessorChain.ProcessorEnum;
import static org.akaza.openclinica.service.crfdata.EnketoUrlService.ENKETO_ORDINAL;

@Component
@Order(value=7)
public class FSItemProcessor extends AbstractItemProcessor implements Processor {

    @Autowired
    private ItemDataDao itemDataDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemFormMetadataDao itemFormMetadataDao;
    @Autowired
    private CrfVersionDao crfVersionDao;
    
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private CrfVersion crfVersion;
    private EventCrf eventCrf;
    private SubmissionContainer container;

    public ProcessorEnum process(SubmissionContainer container) throws Exception {
        logger.info("Executing FSItem Processor.");
        this.container = container;
        ArrayList<HashMap> listOfUploadFilePaths = container.getListOfUploadFilePaths();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(container.getRequestBody()));
        Document doc = db.parse(is);
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

                        crfVersion = container.getCrfVersion();
                        eventCrf = container.getEventCrf();

                        NodeList groupNodeList = crfNode.getChildNodes();

                        // Group loop
                        for (int k = 0; k < groupNodeList.getLength(); k = k + 1) {
                            Node groupNode = groupNodeList.item(k);
                            if (groupNode instanceof Element && !groupNode.getNodeName().startsWith("SECTION_")) {
                                groupNodeName = groupNode.getNodeName();
                                ItemGroup itemGroup = lookupItemGroup(groupNodeName, crfVersion);
                                if (itemGroup == null) {
                                    logger.error("Failed to lookup item group: '" + groupNodeName + "'.  Continuing with submission.");
                                    continue;
                                }
                                processFieldSubmissionGroupItems(listOfUploadFilePaths, groupNode, itemGroup);
                            }
                        }
                    }
                }
            }
        }
        return ProcessorEnum.PROCEED;
    }

    private void processFieldSubmissionGroupItems(ArrayList<HashMap> listOfUploadFilePaths, Node groupNode, ItemGroup itemGroup) throws Exception {
        String itemName;
        String itemValue;
        Integer itemOrdinal = null;

        final NamedNodeMap attributes = groupNode.getAttributes();
        boolean enketoOrdinalFound = false;
        // check to see if groupNode has any enketo attributes
        for (int attrIndex = 0; attrIndex < attributes.getLength(); attrIndex++) {
            if (attributes.item(attrIndex).getNodeName().equals(ENKETO_ORDINAL)) {
                logger.debug("found enketo attribute");
                logger.debug(attributes.item(attrIndex).getNodeName());
                logger.debug(attributes.item(attrIndex).getNodeValue());
                enketoOrdinalFound = true;
                itemOrdinal = new Integer(attributes.item(attrIndex).getNodeValue());
            }
        }

        if (!enketoOrdinalFound) {
            // set the ordinal as 1
            itemOrdinal = 1;
        }

        if (container.getRequestType() == FieldRequestTypeEnum.DELETE_FIELD) {
                ItemData existingItemData = lookupFieldItemData(itemGroup, itemOrdinal);

                existingItemData.setDeleted(true);
                existingItemData.setValue("");
                existingItemData.setOldStatus(existingItemData.getStatus());
                existingItemData.setUserAccount(container.getUser());
                existingItemData.setStatus(Status.AVAILABLE);
                existingItemData.setUpdateId(container.getUser().getUserId());
                existingItemData = itemDataDao.saveOrUpdate(existingItemData);

                //Close discrepancy notes
                closeItemDiscrepancyNotes(container, existingItemData);
                return;
        }

        NodeList itemNodeList = groupNode.getChildNodes();
        // Item loop
        for (int m = 0; m < itemNodeList.getLength(); m = m + 1) {
            Node itemNode = itemNodeList.item(m);
            if (ShouldProcessItemNode(itemNode)) {

                itemName = itemNode.getNodeName().trim();
                itemValue = itemNode.getTextContent();

                Item item = lookupItem(itemName, crfVersion);

                if (item == null) {
                    logger.error("Failed to lookup item: '" + itemName + "'.  Continuing with submission.");
                    continue;
                }

                ItemFormMetadata itemFormMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());

                // Convert space separated Enketo multiselect values to comma separated OC multiselect values
                Integer responseTypeId = itemFormMetadata.getResponseSet().getResponseType().getResponseTypeId();
                if (responseTypeId == 3 || responseTypeId == 7) {
                    itemValue = itemValue.replaceAll(" ", ",");
                }
                if (responseTypeId == 4) {
                    for (HashMap  uploadFilePath : listOfUploadFilePaths){
                        if ((boolean) uploadFilePath.containsKey(itemValue)  && itemValue!=""){
                            itemValue = (String) uploadFilePath.get(itemValue);
                            break;
                        }

                    }
                }


                ItemData newItemData = createItemData(item, itemValue, itemOrdinal, eventCrf, container.getStudy(),
                        container.getSubject(), container.getUser());
                Errors itemErrors = validateItemData(newItemData, item, responseTypeId);
                if (itemErrors.hasErrors()) {
                    container.getErrors().addAllErrors(itemErrors);
                    throw new Exception("Item validation error.  Rolling back submission changes.");
                }

                ItemData existingItemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), eventCrf.getEventCrfId(), itemOrdinal);
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
    }

    private boolean ShouldProcessItemNode(Node itemNode) {
        return itemNode instanceof Element && !itemNode.getNodeName().endsWith(".HEADER")
                && !itemNode.getNodeName().endsWith(".SUBHEADER") && !itemNode.getNodeName()
                .equals("OC.REPEAT_ORDINAL") && !itemNode.getNodeName().equals("OC.STUDY_SUBJECT_ID") && !itemNode.getNodeName()
                .equals("OC.STUDY_SUBJECT_ID_CONFIRM");
    }

    private Item lookupItem(String itemName, CrfVersion crfVersion) {
        if (crfVersion.getXform() == null || crfVersion.getXform().equals("")) { 
            return itemDao.findByOcOID(itemName);
        } else { 
            return itemDao.findByNameCrfId(itemName, crfVersion.getCrf().getCrfId());
        }
    }

    private ItemData lookupFieldItemData(ItemGroup itemGroup, Integer ordinal) {
        return itemDataDao.findByEventCrfGroupOrdinal(eventCrf, itemGroup.getItemGroupId(), ordinal);
    }

}
