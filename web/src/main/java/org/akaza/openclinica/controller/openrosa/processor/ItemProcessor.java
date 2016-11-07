package org.akaza.openclinica.controller.openrosa.processor;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.controller.openrosa.SubmissionProcessorChain.ProcessorEnum;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.ItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupMetadataDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.akaza.openclinica.domain.datamap.ItemMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
@Order(value=6)
public class ItemProcessor extends AbstractItemProcessor implements Processor {

    @Autowired private ItemDataDao itemDataDao;

    @Autowired private ItemDao itemDao;

    @Autowired private ItemGroupDao itemGroupDao;

    @Autowired private ItemGroupMetadataDao itemGroupMetadataDao;

    @Autowired private ItemFormMetadataDao itemFormMetadataDao;

    @Autowired private CrfVersionDao crfVersionDao;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private CrfVersion crfVersion;
    private EventCrf eventCrf;
    private ArrayList<ItemData> itemDataList;
    private SubmissionContainer container;
    private boolean fieldSubmissionFlag;

    public ProcessorEnum process(SubmissionContainer container) throws Exception {
        logger.info("Executing Item Processor.");
        this.container = container;
        this.fieldSubmissionFlag = container.isFieldSubmissionFlag();
        if (fieldSubmissionFlag) {
            return ProcessorEnum.PROCEED;
        }
        ArrayList<HashMap> listOfUploadFilePaths = container.getListOfUploadFilePaths();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(container.getRequestBody()));
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

                        crfVersion = container.getCrfVersion();
                        eventCrf = container.getEventCrf();
                        itemDataList = new ArrayList<ItemData>();

                        HashMap<Integer, Set<Integer>> groupOrdinalMapping = new HashMap<Integer, Set<Integer>>();
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
                                processGroupItems(listOfUploadFilePaths, groupOrdinalMapping, groupNode, itemGroup);
                            }
                        }
                        // Delete rows that have been removed
                        removeDeletedRows(groupOrdinalMapping);
                    }
                }
            }
        }
        return ProcessorEnum.PROCEED;
    }

    private void processGroupItems(ArrayList<HashMap> listOfUploadFilePaths, HashMap<Integer,
            Set<Integer>> groupOrdinalMapping, Node groupNode, ItemGroup itemGroup) throws Exception {
        String itemName;
        String itemValue;

        if (itemGroup != null && !groupOrdinalMapping.containsKey(itemGroup.getItemGroupId())) {
            groupOrdinalMapping.put(itemGroup.getItemGroupId(), new TreeSet<Integer>());
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
                ItemMetadata im = itemGroupMetadataDao.findMetadataByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());
                ItemGroupMetadata itemGroupMeta = im.getIgm();
                ItemFormMetadata itemFormMetadata = im.getIfm();
                        //ItemFormMetadata itemFormMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());
                Integer itemOrdinal = getItemOrdinal(groupNode, itemGroupMeta.isRepeatingGroup(), itemDataList, item);

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

                // Build set of submitted row numbers to be used to find deleted DB rows later
                Set<Integer> ordinals = groupOrdinalMapping.get(itemGroup.getItemGroupId());
                ordinals.add(itemOrdinal);
                groupOrdinalMapping.put(itemGroup.getItemGroupId(), ordinals);

                ItemData newItemData = createItemData(item, itemValue, itemOrdinal, eventCrf, container.getStudy(), container.getSubject(), container.getUser());
                Errors itemErrors = validateItemData(newItemData, item, responseTypeId);
                if (itemErrors.hasErrors()) {
                    container.getErrors().addAllErrors(itemErrors);
                    throw new Exception("Item validation error.  Rolling back submission changes.");
                } else {
                    itemDataList.add(newItemData);
                }
                ItemData existingItemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), eventCrf.getEventCrfId(), itemOrdinal);
                if (existingItemData == null) {
                    // No existing value, create new item.
                    if (newItemData.getOrdinal() < 0) {
                        newItemData.setOrdinal(itemDataDao.getMaxGroupRepeat(eventCrf.getEventCrfId(), item.getItemId()) + 1);
                        groupOrdinalMapping.get(itemGroup.getItemGroupId()).add(newItemData.getOrdinal());
                    }
                    newItemData.setStatus(Status.UNAVAILABLE);
                    itemDataDao.saveOrUpdate(newItemData);

                } else if (existingItemData.getValue().equals(newItemData.getValue())) {
                    // Existing item. Value unchanged. Do nothing.
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
        return itemNode instanceof Element
                && !itemNode.getNodeName().endsWith(".HEADER")
                && !itemNode.getNodeName().endsWith(".SUBHEADER")
                && !itemNode.getNodeName().equals("OC.REPEAT_ORDINAL")
                && !itemNode.getNodeName().equals("OC.STUDY_SUBJECT_ID")
                && !itemNode.getNodeName().equals("OC.STUDY_SUBJECT_ID_CONFIRM");
    }

    private Item lookupItem(String itemName, CrfVersion crfVersion) {
        if (crfVersion.getXform() == null || crfVersion.getXform().equals("")) {
            return itemDao.findByOcOID(itemName);
        } else {
            return itemDao.findByNameCrfId(itemName, crfVersion.getCrf().getCrfId());
        }
    }

    private Integer getItemOrdinal(Node groupNode, boolean isRepeating, ArrayList<ItemData> itemDataList, Item item) {
        if (!isRepeating)
            return 1;

        int ordinal = -1;
        NodeList items = groupNode.getChildNodes();
        for (int i = 0; i < items.getLength(); i++) {
            Node xmlItem = items.item(i);
            if (xmlItem instanceof Element && ((Element) xmlItem).getTagName().equals("OC.REPEAT_ORDINAL") && !((Element) xmlItem).getTextContent().equals(""))
                ordinal = Integer.valueOf(((Element) xmlItem).getTextContent());
        }

        // Enketo specific code here due to Enketo behavior of defaulting in values from first repeat on new repeating
        // group row entries, including the OC.REPEAT_ORDINAL value.
        // If the current value of OC.REPEAT_ORDINAL already exists in the ItemDataBean list for this Item, the current
        // value must be reset to -1 as this is a new repeating group row.
        for (ItemData itemdata : itemDataList) {
            if (itemdata.getItem().getItemId() == item.getItemId() && itemdata.getOrdinal() == ordinal) {
                ordinal = -1;
                break;
            }
        }
        return ordinal;
    }

    private void removeDeletedRows(HashMap<Integer, Set<Integer>> groupOrdinalMapping) {
        Iterator<Integer> keys = groupOrdinalMapping.keySet().iterator();
        while (keys.hasNext()) {
            Integer itemGroupId = keys.next();
            List<ItemData> itemDatas = itemDataDao.findByEventCrfGroup(eventCrf.getEventCrfId(), itemGroupId);
            for (ItemData itemData : itemDatas) {
                if (!groupOrdinalMapping.get(itemGroupId).contains(itemData.getOrdinal()) && !itemData.isDeleted()) {
                    itemData.setDeleted(true);
                    itemData.setValue("");
                    itemData.setOldStatus(itemData.getStatus());
                    itemData.setUserAccount(container.getUser());
                    itemData.setStatus(Status.AVAILABLE);
                    itemData.setUpdateId(container.getUser().getUserId());
                    itemData = itemDataDao.saveOrUpdate(itemData);

                    //Close discrepancy notes
                    closeItemDiscrepancyNotes(container, itemData);
                }
            }
        }
    }

}