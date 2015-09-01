package org.akaza.openclinica.validator.xform;

import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataTypeDao;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemDataType;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class ItemValidator implements Validator {

    private ItemDao itemDao = null;
    private ItemDataTypeDao itemDataTypeDao = null;
    private Item oldItem = null;

    public ItemValidator(ItemDao itemDao, ItemDataTypeDao itemDataTypeDao, Item oldItem) {
        this.itemDao = itemDao;
        this.itemDataTypeDao = itemDataTypeDao;
        this.oldItem = oldItem;
    }

    public boolean supports(Class<?> clazz) {
        return Item.class.equals(clazz);
    }

    public void validate(Object target, Errors errors) {
        Item item = (Item) target;
        ItemDataType oldDataType = (oldItem != null) ? itemDataTypeDao.findByItemId(oldItem.getItemId()) : null;
        ItemDataType newDataType = itemDataTypeDao.findByItemId(item.getItemId());

        // Reject if item name is empty
        ValidationUtils.rejectIfEmpty(errors, "name", "crf_val_item_nameempty");
        // Reject if item data type changes from a previous version
        if (oldDataType != null && oldDataType.getItemDataTypeId() != newDataType.getItemDataTypeId()) {
            errors.rejectValue("itemDataType", "crf_val_item_invaliddatatypechange", item.getName());
        }

    }
}
