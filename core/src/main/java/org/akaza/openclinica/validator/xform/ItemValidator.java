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
    private CrfBean crf = null;

    public ItemValidator(ItemDao itemDao, ItemDataType oldDataType, CrfBean crf) {
        this.itemDao = itemDao;
        this.oldDataType = oldDataType;
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

        if (oldDataType != null && oldDataType.getId() != item.getItemDataType().getId()) {
            errors.rejectValue(item.getName(), "item.invaliddatatypechange", "Item data type cannot change between versions.");
        }

    }
}
