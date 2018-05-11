package org.akaza.openclinica.validator.xform;

import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemDataType;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class ItemValidator implements Validator {

    private ItemDao itemDao = null;
    private ItemDataType newDataType = null;
    private ItemDataType oldDataType = null;

    public ItemValidator(ItemDao itemDao, ItemDataType oldDataType, ItemDataType newDataType) {
        this.itemDao = itemDao;
        this.oldDataType = oldDataType;
        this.newDataType = newDataType;
    }

    public boolean supports(Class<?> clazz) {
        return Item.class.equals(clazz);
    }

    public void validate(Object target, Errors errors) {
        Item item = (Item) target;

        // Reject if item name is empty
        ValidationUtils.rejectIfEmpty(errors, "name", "crf_val_item_nameempty");
        // Reject if item name is a reserved word
        if (item.getName().equals("OC.REPEAT_ORDINAL")) {
            errors.rejectValue("name", "crf_item_reserveditemname", item.getName());
        }
        // Reject if item data type changes from a previous version
        if (oldDataType != null && oldDataType.getItemDataTypeId() != newDataType.getItemDataTypeId()) {
            errors.rejectValue("itemDataType", "crf_val_item_invaliddatatypechange", item.getName());
        }

    }
}
