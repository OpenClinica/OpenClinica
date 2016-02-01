package org.akaza.openclinica.controller.openrosa;

import org.akaza.openclinica.logic.expressionTree.ExpressionTreeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PformValidator implements Validator {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public boolean supports(Class<?> clazz) {
        return ItemItemDataContainer.class.equals(clazz);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.validation.Validator#validate(java.lang.Object,
     * org.springframework.validation.Errors)
     */
    @Override
    public void validate(Object target, Errors e) {
        ItemItemDataContainer container = (ItemItemDataContainer) target;
        String origValue = container.getItemData().getValue();
        Integer responseTypeId = container.getResponseTypeId();
        Integer itemDataTypeId = container.getItem().getItemDataType().getItemDataTypeId();
        logger.info("*** Data type id:  ***" + itemDataTypeId);

        if (responseTypeId == 3 || responseTypeId == 7) {
            String[] values = origValue.split(",");
            for (String value : values) {
                subValidator(itemDataTypeId, value.trim(), e);
            }
        } else {
            subValidator(itemDataTypeId, origValue, e);

        }
    }

    public void subValidator(Integer itemDataTypeId, String value, Errors e) {
        if (value != null && value != "") {

            switch (itemDataTypeId) {
            case 5: { // ItemDataType.STRING
                    if (value.length()>3999){                   
                    e.reject("value.invalid.STRING");
                    logger.info(value +"  ***   value.invalid.STRING    ** TEXT VALUE IS OVER 3999 Characters*");
                    }
                break;
            }
            case 6: { // ItemDataType.INTEGER
                try {
                    Integer.valueOf(value);
                } catch (NumberFormatException nfe) {
                    e.reject("value.invalid.Integer");
                    logger.info(value +"  ***value.invalid.INTEGER***");
                }
                break;
            }
            case 7: { // ItemDataType.REAL
                try {
                    Float.valueOf(value);
                } catch (NumberFormatException nfe) {
                    e.reject("value.invalid.float");
                    logger.info(value +"   ***value.invalid.REAL***");
                }
                break;
            }
            case 9: { // ItemDataType.DATE
                if (!ExpressionTreeHelper.isDateyyyyMMddDashes(value)) {
                    e.reject("value.invalid.date");
                    logger.info(value +"   ***value.invalid.DATE***");
                }
                break;
            }
            case 10: { // ItemDataType.PDATE
                if (!ExpressionTreeHelper.isDateyyyyMMddDashes(value) && !ExpressionTreeHelper.isDateyyyyMMDashes(value)
                        && !ExpressionTreeHelper.isDateyyyyDashes(value)) {
                    e.reject("value.invalid.pdate");
                    logger.info(value +"  ***value.invalid.PDATE***");
                }
                break;
            }
            case 11: { // ItemDataType.FILE
                e.reject("value.notSupported.file");
                break;
            }

            default:
                break;
            }

        }
    }

}
