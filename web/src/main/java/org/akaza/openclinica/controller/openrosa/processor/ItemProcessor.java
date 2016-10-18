package org.akaza.openclinica.controller.openrosa.processor;

import org.akaza.openclinica.controller.openrosa.ItemItemDataContainer;
import org.akaza.openclinica.controller.openrosa.PformValidator;
import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;

import static org.akaza.openclinica.controller.openrosa.SubmissionProcessorChain.ProcessorEnum;
import static org.akaza.openclinica.service.crfdata.EnketoUrlService.ENKETO_ORDINAL;

@Component
@Order(value=6)
public class ItemProcessor implements Processor {

    @Autowired
    private ItemDataDao itemDataDao;
    
    @Autowired
    private ItemDao itemDao;
    
    @Autowired
    private ItemGroupDao itemGroupDao;
    
    @Autowired
    private ItemGroupMetadataDao itemGroupMetadataDao;
    
    @Autowired
    private ItemFormMetadataDao itemFormMetadataDao;
    
    @Autowired
    private CrfVersionDao crfVersionDao;
    
    @Autowired
    private DiscrepancyNoteDao discrepancyNoteDao;
    
    @Autowired
    private ResolutionStatusDao resolutionStatusDao;
    
    @Autowired
    private DiscrepancyNoteTypeDao discrepancyNoteTypeDao;
    
    @Autowired
    private DnItemDataMapDao dnItemDataMapDao;
    
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private CrfVersion crfVersion;
    private EventCrf eventCrf;
    private ArrayList<ItemData> itemDataList;
    private SubmissionContainer container;
    private boolean fieldSubmissionFlag;

    public ProcessorEnum process(SubmissionContainer container, boolean fieldSubmissionFlag) throws Exception {
        logger.info("Executing Item Processor.");
        this.container = container;
        this.fieldSubmissionFlag = fieldSubmissionFlag;
        ArrayList<HashMap> listOfUploadFilePaths =container.getListOfUploadFilePaths();

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

                        crfVersion = crfVersionDao.findByOcOID(container.getSubjectContext().get("crfVersionOID"));
                        eventCrf = container.getEventCrf();
                        itemDataList = new ArrayList<ItemData>();

                        HashMap<Integer,Set<Integer>> groupOrdinalMapping = new HashMap<Integer,Set<Integer>>();
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
                                if (fieldSubmissionFlag) {
                                    processFieldSubmissionGroupItems(listOfUploadFilePaths, groupOrdinalMapping, groupNode, itemGroup);
                                } else {
                                    processGroupItems(listOfUploadFilePaths, groupOrdinalMapping, groupNode, itemGroup);
                                }
                            }
                        }
                        //}

                            // Delete rows that have been removed
                            removeDeletedRows(groupOrdinalMapping);
                        }
                    }
                }
            }
            return ProcessorEnum.PROCEED;
        }

    private void processGroupItems(ArrayList<HashMap> listOfUploadFilePaths, HashMap<Integer, Set<Integer>> groupOrdinalMapping, Node groupNode, ItemGroup itemGroup) throws Exception {
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

                ItemGroupMetadata itemGroupMeta = itemGroupMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());
                ItemFormMetadata itemFormMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());
                Integer itemOrdinal = getItemOrdinal(groupNode, itemGroupMeta.isRepeatingGroup(),itemDataList,item);

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

                // Build set of submitted row numbers to be used to find deleted DB rows later
                Set<Integer> ordinals = groupOrdinalMapping.get(itemGroup.getItemGroupId());
                ordinals.add(itemOrdinal);
                groupOrdinalMapping.put(itemGroup.getItemGroupId(),ordinals);

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
                    itemDataDao.saveOrUpdate(newItemData);
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

    private void processFieldSubmissionGroupItems(ArrayList<HashMap> listOfUploadFilePaths, HashMap<Integer, Set<Integer>> groupOrdinalMapping, Node groupNode, ItemGroup itemGroup) throws Exception {
        String itemName;
        String itemValue;
        Integer itemOrdinal = null;
        // does groupNode have any enketo attributes
        final NamedNodeMap attributes = groupNode.getAttributes();
        boolean enketoOrdinalFound = false;
        for (int attrIndex = 0; attrIndex < attributes.getLength(); attrIndex++) {
            System.out.println(attributes.item(attrIndex).getNodeName());
            System.out.println(attributes.item(attrIndex).getNodeValue());
            if (attributes.item(attrIndex).getNodeName().equals(ENKETO_ORDINAL)) {
                System.out.println("found enketo attribute");
                enketoOrdinalFound = true;
                itemOrdinal = new Integer(attributes.item(attrIndex).getNodeValue());
            }
        }

        if (!enketoOrdinalFound) {
            // set the ordinal as 1
            itemOrdinal = 1;
        }
        if (itemGroup != null && !groupOrdinalMapping.containsKey(itemGroup.getItemGroupId())) {
            groupOrdinalMapping.put(itemGroup.getItemGroupId(),new TreeSet<Integer>());
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

                ItemGroupMetadata itemGroupMeta = itemGroupMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());
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

                // Build set of submitted row numbers to be used to find deleted DB rows later
                Set<Integer> ordinals = groupOrdinalMapping.get(itemGroup.getItemGroupId());
                ordinals.add(itemOrdinal);
                groupOrdinalMapping.put(itemGroup.getItemGroupId(),ordinals);

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
                    itemDataDao.saveOrUpdate(newItemData);
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
        return itemNode instanceof Element && !itemNode.getNodeName().endsWith(".HEADER") && !itemNode.getNodeName().endsWith(".SUBHEADER") && !itemNode.getNodeName()
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

    private ItemGroup lookupItemGroup(String groupNodeName, CrfVersion crfVersion) {
        if (crfVersion.getXform() == null || crfVersion.getXform().equals("")) {
            return itemGroupDao.findByOcOID(groupNodeName);
        } else {
            return itemGroupDao.findByNameCrfId(groupNodeName, crfVersion.getCrf());
        }
    }

    private ItemData createItemData(Item item, String itemValue, Integer itemOrdinal, EventCrf eventCrf, Study study,
            StudySubject studySubject, UserAccount user) {
        ItemData itemData = new ItemData();
        itemData.setItem(item);
        itemData.setEventCrf(eventCrf);
        itemData.setValue(itemValue);
        itemData.setDateCreated(new Date());
        itemData.setStatus(Status.AVAILABLE);
        itemData.setOrdinal(itemOrdinal);
        itemData.setUserAccount(user);
        itemData.setDeleted(false);
        return itemData;
    }
    
    private Errors validateItemData(ItemData itemData, Item item, Integer responseTypeId) {
        ItemItemDataContainer container = new ItemItemDataContainer(item, itemData, responseTypeId);
        DataBinder dataBinder = new DataBinder(null);
        Errors errors = dataBinder.getBindingResult();
        PformValidator pformValidator = new PformValidator();
        pformValidator.validate(container, errors);
        return errors;
    }
    
    private Integer getItemOrdinal(Node groupNode, boolean isRepeating, ArrayList<ItemData> itemDataList, Item item) {
        if (!isRepeating) return 1;

        int ordinal = -1;
        NodeList items = groupNode.getChildNodes();
        for(int i=0; i<items.getLength();i++){
            Node xmlItem = items.item(i);
            if (xmlItem instanceof Element && ((Element) xmlItem).getTagName().equals("OC.REPEAT_ORDINAL") && !((Element) xmlItem).getTextContent().equals(""))
                ordinal = Integer.valueOf(((Element)xmlItem).getTextContent());
        }

        // Enketo specific code here due to Enketo behavior of defaulting in values from first repeat on new repeating
        // group row entries, including the OC.REPEAT_ORDINAL value.
        // If the current value of OC.REPEAT_ORDINAL already exists in the ItemDataBean list for this Item, the current
        // value must be reset to -1 as this is a new repeating group row.
        for (ItemData itemdata:itemDataList) {
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
            for (ItemData itemData:itemDatas) {
                if (!groupOrdinalMapping.get(itemGroupId).contains(itemData.getOrdinal()) && !itemData.isDeleted()){
                    itemData.setDeleted(true);
                    itemData.setValue("");
                    itemData.setOldStatus(itemData.getStatus());
                    itemData.setUserAccount(container.getUser());
                    itemData.setStatus(Status.AVAILABLE);
                    itemData.setUpdateId(container.getUser().getUserId());
                    itemData = itemDataDao.saveOrUpdate(itemData);

                    //Close discrepancy notes
                    closeItemDiscrepancyNotes(itemData);
                }
            }
        }
    }
    
    private void closeItemDiscrepancyNotes(ItemData itemData) {

        ResourceBundleProvider.updateLocale(container.getLocale());
        ResourceBundle resword = ResourceBundleProvider.getWordsBundle(container.getLocale());

        // Notes & Discrepancies must be set to "closed" when event CRF is deleted
        // parentDiscrepancyNoteList is the list of the parent DNs records only
        List<DiscrepancyNote> parentDiscrepancyNoteList = discrepancyNoteDao.findParentNotesByItemData(itemData.getItemDataId());
        for (DiscrepancyNote parentDiscrepancyNote : parentDiscrepancyNoteList) {
            if (parentDiscrepancyNote.getResolutionStatus().getResolutionStatusId() != 4) { // if the DN's resolution status is not set to Closed
                String description = resword.getString("dn_auto-closed_description");
                String detailedNotes =resword.getString("dn_auto_closed_item_detailed_notes");
                // create new DN record , new DN Map record , also update the parent record
                DiscrepancyNote dn = new DiscrepancyNote();
                ResolutionStatus resStatus = resolutionStatusDao.findByResolutionStatusId(4);
                dn.setStudy(container.getStudy());
                dn.setEntityType("itemData");
                dn.setDescription(description);
                dn.setDetailedNotes(detailedNotes);
                dn.setDiscrepancyNoteType(parentDiscrepancyNote.getDiscrepancyNoteType()); // set to parent DN Type Id
                dn.setResolutionStatus(resStatus); // set to closed
                dn.setUserAccount(container.getUser());
                dn.setUserAccountByOwnerId(container.getUser());
                dn.setParentDiscrepancyNote(parentDiscrepancyNote);
                dn.setDateCreated(new Date());
                dn = discrepancyNoteDao.saveOrUpdate(dn);

                // Create Mapping for new Discrepancy Note
                DnItemDataMapId dnItemDataMapId = new DnItemDataMapId();
                dnItemDataMapId.setDiscrepancyNoteId(dn.getDiscrepancyNoteId());
                dnItemDataMapId.setItemDataId(itemData.getItemDataId());
                dnItemDataMapId.setStudySubjectId(container.getSubject().getStudySubjectId());
                dnItemDataMapId.setColumnName("value");

                DnItemDataMap mapping = new DnItemDataMap();
                mapping.setDnItemDataMapId(dnItemDataMapId);
                mapping.setItemData(itemData);
                mapping.setStudySubject(container.getSubject());
                mapping.setActivated(false);
                mapping.setDiscrepancyNote(dn);
                dnItemDataMapDao.saveOrUpdate(mapping);

                DiscrepancyNote itemParentNote = discrepancyNoteDao.findByDiscrepancyNoteId(dn.getParentDiscrepancyNote().getDiscrepancyNoteId());
                itemParentNote.setResolutionStatus(resStatus);
                itemParentNote.setUserAccount(container.getUser());
                discrepancyNoteDao.saveOrUpdate(itemParentNote);
            }
        }

        // Deactivate existing mappings for this ItemData
        List<DnItemDataMap> existingMappings = dnItemDataMapDao.findByItemData(itemData.getItemDataId());
        for (DnItemDataMap mapping:existingMappings) {
            mapping.setActivated(false);
            dnItemDataMapDao.saveOrUpdate(mapping);
        }
    }
}
