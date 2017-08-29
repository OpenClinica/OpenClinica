package org.akaza.openclinica.controller.openrosa.processor;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.akaza.openclinica.controller.openrosa.ItemItemDataContainer;
import org.akaza.openclinica.controller.openrosa.PformValidator;
import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.dao.hibernate.DiscrepancyNoteDao;
import org.akaza.openclinica.dao.hibernate.DnItemDataMapDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupDao;
import org.akaza.openclinica.dao.hibernate.ResolutionStatusDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import org.akaza.openclinica.domain.datamap.DnItemDataMap;
import org.akaza.openclinica.domain.datamap.DnItemDataMapId;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.akaza.openclinica.domain.datamap.ResolutionStatus;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

/**
 * Created by yogi on 10/24/16.
 */
public abstract class AbstractItemProcessor {
    @Autowired
    private DiscrepancyNoteDao discrepancyNoteDao;
    @Autowired
    private ResolutionStatusDao resolutionStatusDao;
    @Autowired
    private DnItemDataMapDao dnItemDataMapDao;
    @Autowired
    private ItemGroupDao itemGroupDao;

    protected void closeItemDiscrepancyNotes(SubmissionContainer container, ItemData itemData) {

        ResourceBundleProvider.updateLocale(container.getLocale());
        ResourceBundle resword = ResourceBundleProvider.getWordsBundle(container.getLocale());

        // Queries must be set to "closed" when event CRF is deleted
        // parentDiscrepancyNoteList is the list of the parent DNs records only
        List<DiscrepancyNote> parentDiscrepancyNoteList = discrepancyNoteDao.findParentNotesByItemData(itemData.getItemDataId());
        for (DiscrepancyNote parentDiscrepancyNote : parentDiscrepancyNoteList) {
            if (parentDiscrepancyNote.getResolutionStatus().getResolutionStatusId() != 4) { // if the DN's resolution
                                                                                            // status is not set to
                                                                                            // Closed
                String description = resword.getString("dn_auto-closed_description");
                String detailedNotes = resword.getString("dn_auto_closed_item_detailed_notes");
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
        for (DnItemDataMap mapping : existingMappings) {
            mapping.setActivated(false);
            dnItemDataMapDao.saveOrUpdate(mapping);
        }
    }

    protected ItemGroup lookupItemGroup(String groupNodeName, CrfVersion crfVersion) {
        if (crfVersion.getXform() == null || crfVersion.getXform().equals("")) {
            return itemGroupDao.findByOcOID(groupNodeName);
        } else {
            return itemGroupDao.findByNameCrfId(groupNodeName, crfVersion.getCrf());
        }
    }

    protected ItemData createItemData(Item item, String itemValue, Integer itemOrdinal, SubmissionContainer container) {
        ItemData itemData = new ItemData();
        itemData.setItem(item);
        itemData.setEventCrf(container.getEventCrf());
        itemData.setValue(itemValue);
        itemData.setDateCreated(new Date());
        itemData.setStatus(Status.AVAILABLE);
        itemData.setOrdinal(itemOrdinal);
        itemData.setUserAccount(container.getUser());
        itemData.setDeleted(false);
        itemData.setInstanceId(container.getInstanceId());
        return itemData;
    }

    protected Errors validateItemData(ItemData itemData, Item item, Integer responseTypeId) {
        ItemItemDataContainer container = new ItemItemDataContainer(item, itemData, responseTypeId);
        DataBinder dataBinder = new DataBinder(null);
        Errors errors = dataBinder.getBindingResult();
        PformValidator pformValidator = new PformValidator();
        pformValidator.validate(container, errors);
        return errors;
    }
}
