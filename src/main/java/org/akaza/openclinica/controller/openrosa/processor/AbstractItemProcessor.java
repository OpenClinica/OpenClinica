package org.akaza.openclinica.controller.openrosa.processor;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.akaza.openclinica.controller.openrosa.ItemItemDataContainer;
import org.akaza.openclinica.controller.openrosa.PformValidator;
import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import core.org.akaza.openclinica.dao.hibernate.DiscrepancyNoteDao;
import core.org.akaza.openclinica.dao.hibernate.DnItemDataMapDao;
import core.org.akaza.openclinica.dao.hibernate.ItemGroupDao;
import core.org.akaza.openclinica.dao.hibernate.ResolutionStatusDao;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.CrfVersion;
import core.org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import core.org.akaza.openclinica.domain.datamap.DnItemDataMap;
import core.org.akaza.openclinica.domain.datamap.DnItemDataMapId;
import core.org.akaza.openclinica.domain.datamap.Item;
import core.org.akaza.openclinica.domain.datamap.ItemData;
import core.org.akaza.openclinica.domain.datamap.ItemGroup;
import core.org.akaza.openclinica.domain.datamap.ResolutionStatus;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
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
