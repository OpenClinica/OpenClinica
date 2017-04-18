package org.akaza.openclinica.controller.openrosa.processor;

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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akaza.openclinica.controller.openrosa.ItemItemDataContainer;
import org.akaza.openclinica.controller.openrosa.PformValidator;
import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.DiscrepancyNoteDao;
import org.akaza.openclinica.dao.hibernate.DiscrepancyNoteTypeDao;
import org.akaza.openclinica.dao.hibernate.DnItemDataMapDao;
import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.ItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.ResolutionStatusDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import org.akaza.openclinica.domain.datamap.DnItemDataMap;
import org.akaza.openclinica.domain.datamap.DnItemDataMapId;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.akaza.openclinica.domain.datamap.ResolutionStatus;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
public class ItemProcessor implements Processor, Ordered {

    @Autowired
    ItemDataDao itemDataDao;
    
    @Autowired
    ItemDao itemDao;
    
    @Autowired
    ItemGroupDao itemGroupDao;
    
    @Autowired
    ItemGroupMetadataDao itemGroupMetadataDao;
    
    @Autowired
    ItemFormMetadataDao itemFormMetadataDao;
    
    @Autowired
    CrfVersionDao crfVersionDao;
    
    @Autowired
    DiscrepancyNoteDao discrepancyNoteDao;
    
    @Autowired
    ResolutionStatusDao resolutionStatusDao;
    
    @Autowired
    DiscrepancyNoteTypeDao discrepancyNoteTypeDao;
    
    @Autowired
    DnItemDataMapDao dnItemDataMapDao;
    
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public int getOrder() {
        return 4;
    }

    public void process(SubmissionContainer container) throws Exception {
        Date itemProcessorStart = new Date();
        logger.info("Executing Item Processor.");
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
                        Date crfVersionStart = new Date();
                        CrfVersion crfVersion = crfVersionDao.findByOcOID(container.getSubjectContext().get("crfVersionOID"));
                        Date crfVersionEnd = new Date();
                        logger.info("Database call to crf_version took " + (crfVersionEnd.getTime() - crfVersionStart.getTime()) + "milliseconds");
                        EventCrf eventCrf = container.getEventCrf();
                        ArrayList<ItemData> itemDataList = new ArrayList<ItemData>();

                        HashMap<Integer,Set<Integer>> groupOrdinalMapping = new HashMap<Integer,Set<Integer>>();
                        NodeList groupNodeList = crfNode.getChildNodes();
                        
                        Date bulkItemStart = new Date();
                        List<Item> items = itemDao.findAllByCrfId(crfVersion.getCrf().getCrfId());
                        Date bulkItemEnd = new Date();
                        logger.info("Bulk call to item took " + (bulkItemEnd.getTime() - bulkItemStart.getTime()) + "milliseconds and fetched " + items.size() + "rows.");

                        Date bulkItemDataStart = new Date();
                        List<ItemData> itemDatas = itemDataDao.findAllByEventCrf(container.getEventCrf().getEventCrfId());
                        Date bulkItemDataEnd = new Date();
                        logger.info("Bulk call to item_data took " + (bulkItemDataEnd.getTime() - bulkItemDataStart.getTime()) + "milliseconds and fetched " + itemDatas.size() + " rows.");

                        Date bulkItemGroupStart = new Date();
                        List<ItemGroup> itemGroups = itemGroupDao.findAllByCrfId(container.getEventCrf().getCrfVersion().getCrf());
                        Date bulkItemGroupEnd = new Date();
                        logger.info("Bulk call to item_group took " + (bulkItemGroupEnd.getTime() - bulkItemGroupStart.getTime()) + "milliseconds and fetched " + itemGroups.size() + "rows.");

                        Date bulkItemGroupMetadataStart = new Date();
                        List<ItemGroupMetadata> itemGroupMetadatas = itemGroupMetadataDao.findAllByCrfVersion(container.getEventCrf().getCrfVersion().getCrfVersionId());
                        Date bulkItemGroupMetadataEnd = new Date();
                        logger.info("Bulk call to item_group_metadata took " + (bulkItemGroupMetadataEnd.getTime() - bulkItemGroupMetadataStart.getTime()) + "milliseconds and fetched " + itemGroupMetadatas.size() + "rows.");

                        Date bulkItemFormMetadataStart = new Date();
                        List<ItemFormMetadata> itemFormMetadatas = itemFormMetadataDao.findAllByCrfVersion(container.getEventCrf().getCrfVersion().getCrfVersionId());
                        Date bulkItemFormMetadataEnd = new Date();
                        logger.info("Bulk call to item_form_metadata took " + (bulkItemFormMetadataEnd.getTime() - bulkItemFormMetadataStart.getTime()) + "milliseconds and fetched " + itemFormMetadatas.size() + "rows.");

                        
                        // Group loop
                        for (int k = 0; k < groupNodeList.getLength(); k = k + 1) {
                            Node groupNode = groupNodeList.item(k);
                            if (groupNode instanceof Element && !groupNode.getNodeName().startsWith("SECTION_")) {
                                groupNodeName = groupNode.getNodeName();
                                Date itemGroupStart = new Date();
                                ItemGroup itemGroup = lookupItemGroup(groupNodeName, crfVersion, itemGroups);
                                Date itemGroupEnd = new Date();
                                logger.info("Database call to item_group took " + (itemGroupEnd.getTime() - itemGroupStart.getTime()) + "milliseconds");
                                if (itemGroup == null) {
                                    logger.error("Failed to lookup item group: '" + groupNodeName + "'.  Continuing with submission.");
                                    continue;
                                }
                                
                                if (itemGroup != null && !groupOrdinalMapping.containsKey(itemGroup.getItemGroupId())) groupOrdinalMapping.put(itemGroup.getItemGroupId(),new TreeSet<Integer>());

                                NodeList itemNodeList = groupNode.getChildNodes();
                                // Item loop
                                for (int m = 0; m < itemNodeList.getLength(); m = m + 1) {
                                    Node itemNode = itemNodeList.item(m);
                                    if (itemNode instanceof Element && !itemNode.getNodeName().endsWith(".HEADER")
                                            && !itemNode.getNodeName().endsWith(".SUBHEADER")
                                            && !itemNode.getNodeName().equals("OC.REPEAT_ORDINAL")
                                            && !itemNode.getNodeName().equals("OC.STUDY_SUBJECT_ID")
                                            && !itemNode.getNodeName().equals("OC.STUDY_SUBJECT_ID_CONFIRM") ) {
                                        
                                        itemName = itemNode.getNodeName().trim();
                                        itemValue = itemNode.getTextContent();
                                       
                                        Date itemStart = new Date();
                                        Item item = lookupItem(itemName, crfVersion, items);
                                        Date itemEnd = new Date();
                                        logger.info(" Database call to item took " + (itemEnd.getTime() - itemStart.getTime()) + "milliseconds");
                   
                                        if (item == null) {
                                            logger.error("Failed to lookup item: '" + itemName + "'.  Continuing with submission.");
                                            continue;
                                        }

                                        Date itemMetadataStart = new Date();
                                        ItemGroupMetadata itemGroupMeta = lookupItemGroupMetadata(item.getItemId(), crfVersion.getCrfVersionId(), itemGroupMetadatas);
                                        ItemFormMetadata itemFormMetadata = lookupItemFormMetadata(item.getItemId(), crfVersion.getCrfVersionId(), itemFormMetadatas);
                                        Date itemMetadataEnd = new Date();
                                        logger.info(" Double Database call to Form & Group Metadata took " + (itemMetadataEnd.getTime() - itemMetadataStart.getTime()) + "milliseconds");

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
                                        Date existingItemDataStart = new Date();
                                        ItemData existingItemData = lookupItemData(item.getItemId(), eventCrf.getEventCrfId(), itemOrdinal,itemDatas);
                                        Date existingItemDataEnd = new Date();
                                        logger.info(" Existing row database call to item_data took " + (existingItemDataEnd.getTime() - existingItemDataStart.getTime()) + "milliseconds");
                                        if (existingItemData == null) {
                                            // No existing value, create new item.
                                            if (newItemData.getOrdinal() < 0) {
                                                newItemData.setOrdinal(itemDataDao.getMaxGroupRepeat(eventCrf.getEventCrfId(), item.getItemId()) + 1);
                                                groupOrdinalMapping.get(itemGroup.getItemGroupId()).add(newItemData.getOrdinal());
                                            }
                                            Date itemDataDoubleSaveStart = new Date();
                                            itemDataDao.saveOrUpdate(newItemData);
                                            newItemData.setStatus(Status.UNAVAILABLE);
                                            itemDataDao.saveOrUpdate(newItemData);
                                            Date itemDataDoubleSaveEnd = new Date();
                                            logger.info(" Double database call to item_data took " + (itemDataDoubleSaveEnd.getTime() - itemDataDoubleSaveStart.getTime()) + "milliseconds\n");

                                        } else if (existingItemData.getValue().equals(newItemData.getValue())) {
                                            // Existing item. Value unchanged. Do nothing.
                                        } else {
                                            // Existing item. Value changed. Update existing value.
                                            existingItemData.setValue(newItemData.getValue());
                                            existingItemData.setUpdateId(container.getUser().getUserId());
                                            existingItemData.setDateUpdated(new Date());
                                            Date itemDataSingleSaveStart = new Date();
                                            itemDataDao.saveOrUpdate(existingItemData);
                                            Date itemDataSingleSaveEnd = new Date();
                                            logger.info(" Single database call to item_data took " + (itemDataSingleSaveEnd.getTime() - itemDataSingleSaveStart.getTime()) + "milliseconds\n");
                                        }
                                    }
                                }
                            }
                        }
                        //}

                            // Delete rows that have been removed
                            Date deleteMultiItemDataStart = new Date();
                            removeDeletedRows(groupOrdinalMapping,eventCrf,crfVersion,container.getStudy(),container.getSubject(), container.getLocale(), container.getUser());
                            Date deleteMultiItemDataEnd = new Date();
                            logger.info("Multi-delete database call to item_data took " + (deleteMultiItemDataEnd.getTime() - deleteMultiItemDataStart.getTime()) + "milliseconds");
                        }
                    }
                }
            }
        Date itemProcessorEnd = new Date();
        logger.info("Finished executing normal Item Processor.  Took " + (itemProcessorEnd.getTime() - itemProcessorStart.getTime()) + "milliseconds.");
}
    
    private ItemFormMetadata lookupItemFormMetadata(Integer itemId, Integer crfVersionId, List<ItemFormMetadata> itemFormMetadataList) {
        for (ItemFormMetadata itemFormMetadata: itemFormMetadataList) {
            if (itemFormMetadata.getItem().getItemId() == itemId.intValue() && 
                    itemFormMetadata.getCrfVersionId().intValue() == crfVersionId.intValue()) return itemFormMetadata;
        }
        return null;
    }
    
    private ItemGroupMetadata lookupItemGroupMetadata(Integer itemId, Integer crfVersionId, List<ItemGroupMetadata> itemGroupMetadataList) {
        for (ItemGroupMetadata itemGroupMetadata: itemGroupMetadataList) {
            if (itemGroupMetadata.getItem().getItemId() == itemId.intValue() && 
                    itemGroupMetadata.getCrfVersion().getCrfVersionId() == crfVersionId.intValue()) return itemGroupMetadata;
        }
        return null;
    }
    
    private ItemData lookupItemData(Integer itemId, Integer eventCrfId, Integer itemOrdinal, List<ItemData> itemDataList) {
        for (ItemData itemData: itemDataList) {
            if (itemData.getItem().getItemId() == itemId && 
                    itemData.getEventCrf().getEventCrfId() == eventCrfId && 
                    itemData.getOrdinal() == itemOrdinal) return itemData;
        }
        return null;
    }

    private Item lookupItem(String itemName, CrfVersion crfVersion, List<Item> itemList) {
        if (crfVersion.getXform() == null || crfVersion.getXform().equals("")) { 
            return lookupItemByOid(itemName,itemList);
        } else { 
            return lookupItemByName(itemName, itemList);
        }
    }
    
    private Item lookupItemByOid(String oid, List<Item> itemList) {
        for (Item item: itemList) {
            if (item.getOcOid().equals(oid)) return item;
        }
        return null;
    }

    private Item lookupItemByName(String name, List<Item> itemList) {
        for (Item item: itemList) {
            if (item.getName().equals(name)) return item;
        }
        return null;
    }

    private ItemGroup lookupItemGroup(String groupNodeName, CrfVersion crfVersion, List<ItemGroup> itemGroupList) {
        if (crfVersion.getXform() == null || crfVersion.getXform().equals("")) {
            return lookupItemGroupByOId(groupNodeName, itemGroupList);
        } else {
            return lookupItemGroupByName(groupNodeName, itemGroupList);
        }
    }
    
    private ItemGroup lookupItemGroupByOId(String oid, List<ItemGroup> itemGroupList) {
        for (ItemGroup group:itemGroupList) {
            if (group.getOcOid().equals(oid)) return group;
        }
        return null;
    }

    private ItemGroup lookupItemGroupByName(String groupName, List<ItemGroup> itemGroupList) {
        for (ItemGroup group:itemGroupList) {
            if (group.getName().equals(groupName)) return group;
        }
        return null;
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

    private void removeDeletedRows(HashMap<Integer, Set<Integer>> groupOrdinalMapping, EventCrf eventCrf, CrfVersion crfVersion, Study study, StudySubject studySubject, Locale locale, UserAccount user) {
        Iterator<Integer> keys = groupOrdinalMapping.keySet().iterator();
        while (keys.hasNext()) {
            Integer itemGroupId = keys.next();
            List<ItemData> itemDatas = itemDataDao.findByEventCrfGroup(eventCrf.getEventCrfId(), itemGroupId);
            for (ItemData itemData:itemDatas) {
                if (!groupOrdinalMapping.get(itemGroupId).contains(itemData.getOrdinal()) && !itemData.isDeleted()){
                    itemData.setDeleted(true);
                    itemData.setValue("");
                    itemData.setOldStatus(itemData.getStatus());
                    itemData.setUserAccount(user);
                    itemData.setStatus(Status.AVAILABLE);
                    itemData.setUpdateId(user.getUserId());
                    itemData = itemDataDao.saveOrUpdate(itemData);

                    //Close discrepancy notes
                    closeItemDiscrepancyNotes(itemData, study, studySubject, locale, user);
                }
            }
        }
    }
    
    private void closeItemDiscrepancyNotes(ItemData itemData, Study study, StudySubject studySubject, Locale locale, UserAccount user) {

        ResourceBundleProvider.updateLocale(locale);
        ResourceBundle resword = ResourceBundleProvider.getWordsBundle(locale);

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
                dn.setStudy(study);
                dn.setEntityType("itemData");
                dn.setDescription(description);
                dn.setDetailedNotes(detailedNotes);
                dn.setDiscrepancyNoteType(parentDiscrepancyNote.getDiscrepancyNoteType()); // set to parent DN Type Id
                dn.setResolutionStatus(resStatus); // set to closed
                dn.setUserAccount(user);
                dn.setUserAccountByOwnerId(user);
                dn.setParentDiscrepancyNote(parentDiscrepancyNote);
                dn.setDateCreated(new Date());
                dn = discrepancyNoteDao.saveOrUpdate(dn);

                // Create Mapping for new Discrepancy Note
                DnItemDataMapId dnItemDataMapId = new DnItemDataMapId();
                dnItemDataMapId.setDiscrepancyNoteId(dn.getDiscrepancyNoteId());
                dnItemDataMapId.setItemDataId(itemData.getItemDataId());
                dnItemDataMapId.setStudySubjectId(studySubject.getStudySubjectId());
                dnItemDataMapId.setColumnName("value");

                DnItemDataMap mapping = new DnItemDataMap();
                mapping.setDnItemDataMapId(dnItemDataMapId);
                mapping.setItemData(itemData);
                mapping.setStudySubject(studySubject);
                mapping.setActivated(false);
                mapping.setDiscrepancyNote(dn);
                dnItemDataMapDao.saveOrUpdate(mapping);

                DiscrepancyNote itemParentNote = discrepancyNoteDao.findByDiscrepancyNoteId(dn.getParentDiscrepancyNote().getDiscrepancyNoteId());
                itemParentNote.setResolutionStatus(resStatus);
                itemParentNote.setUserAccount(user);
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
