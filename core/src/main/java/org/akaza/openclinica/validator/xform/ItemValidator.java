package org.akaza.openclinica.validator.xform;

import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemDataType;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class ItemValidator implements Validator {

    private ItemDao itemDao = null;
    private ItemDataType oldDataType = null;
    private ItemDataType newDataType = null;
    private CrfBean crf = null;

    public ItemValidator(ItemDao itemDao, ItemDataType oldDataType, ItemDataType newDataType, CrfBean crf) {
        this.itemDao = itemDao;
        this.oldDataType = oldDataType;
        this.newDataType = newDataType;
        this.crf = crf;
    }

    public boolean supports(Class<?> clazz) {
        return Item.class.equals(clazz);
    }

    public void validate(Object target, Errors errors) {
        Item item = (Item) target;

        ValidationUtils.rejectIfEmpty(errors, "name", "name.empty");

        // Item existingItem = itemDao.findByNameCrfId(item.getName(), crf.getCrfId());

        // if (existingItem == null)
        // return;

        if (oldDataType != null && oldDataType.getItemDataTypeId() != newDataType.getItemDataTypeId()) {
            errors.rejectValue("itemDataType", "item.invaliddatatypechange", item.getName());
        }

    }
}
