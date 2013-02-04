package org.akaza.openclinica.web.crfdata;

import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.Validation;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;

/*
 * Helper methods will be placed in this class - DRY
 */
public class ImportHelper {

    /**
     * @param dib
     *            A DisplayItemBean representing an input on the CRF.
     * @return The name of the input in the HTML form.
     */
    public final String getInputName(DisplayItemBean dib) {
        ItemBean ib = dib.getItem();
        String inputName = "input" + ib.getId();
        return inputName;
    }

    /**
     * Peform validation on a item which has a RADIO or SINGLESELECTresponse
     * type. This function checks that the input isn't blank, and that its value
     * comes from the controlled vocabulary (ResponseSetBean) in the
     * DisplayItemBean.
     * 
     * @param v
     *            The Validator to add validations to.
     * @param dib
     *            The DisplayItemBean to validate.
     * @return The DisplayItemBean which is validated.
     */
    public DisplayItemBean validateDisplayItemBeanSingleCV(DiscrepancyValidator v, DisplayItemBean dib, String inputName) {
        if (StringUtil.isBlank(inputName)) {
            inputName = getInputName(dib);
        }
        ItemFormMetadataBean ibMeta = dib.getMetadata();
        ItemDataBean idb = dib.getData();
        if (StringUtil.isBlank(idb.getValue())) {
            if (ibMeta.isRequired()) {
                v.addValidation(inputName, Validator.IS_REQUIRED);
            }
        } else {
            // commented out because it should be a hard edit check, tbh 05/2008
            // v.addValidation(inputName,
            // Validator.IN_RESPONSE_SET_SINGLE_VALUE,
            // dib.getMetadata().getResponseSet());
        }

        return dib;
    }

    /**
     * Peform validation on a item which has a RADIO or SINGLESELECTresponse
     * type. This function checks that the input isn't blank, and that its value
     * comes from the controlled vocabulary (ResponseSetBean) in the
     * DisplayItemBean.
     * 
     * @param v
     *            The Validator to add validations to.
     * @param dib
     *            The DisplayItemBean to validate.
     * @return The DisplayItemBean which is validated.
     */
    public DisplayItemBean validateDisplayItemBeanMultipleCV(DiscrepancyValidator v, DisplayItemBean dib, String inputName) {
        if (StringUtil.isBlank(inputName)) {
            inputName = getInputName(dib);
        }
        ItemFormMetadataBean ibMeta = dib.getMetadata();
        ItemDataBean idb = dib.getData();
        if (StringUtil.isBlank(idb.getValue())) {
            if (ibMeta.isRequired()) {
                v.addValidation(inputName, Validator.IS_REQUIRED);
            }
        } else {
            // comment this out since it should be a hard edit check, tbh
            // 05/2008
            // v.addValidation(inputName, Validator.IN_RESPONSE_SET,
            // dib.getMetadata().getResponseSet());
        }
        return dib;
    }

    /**
     * Peform validation on a item which has a TEXT or TEXTAREA response type.
     * If the item has a null value, it's automatically validated. Otherwise,
     * it's checked against its data type.
     * 
     * @param v
     *            The Validator to add validations to.
     * @param dib
     *            The DisplayItemBean to validate.
     * @return The DisplayItemBean which is validated.
     */
    public DisplayItemBean validateDisplayItemBeanText(DiscrepancyValidator discValidator, DisplayItemBean dib, String inputName) {

        if (StringUtil.isBlank(inputName)) {// for single items
            inputName = getInputName(dib);
        }
        ItemBean ib = dib.getItem();
        ItemFormMetadataBean ibMeta = dib.getMetadata();
        ItemDataType idt = ib.getDataType();
        ItemDataBean idb = dib.getData();

        boolean isNull = false;
        /*
         * ArrayList nullValues = edcb.getNullValuesList(); for (int i = 0; i <
         * nullValues.size(); i++) { NullValue nv = (NullValue)
         * nullValues.get(i); if (nv.getName().equals(fp.getString(inputName))) {
         * isNull = true; } }
         */

        if (!isNull) {
            if (StringUtil.isBlank(idb.getValue())) {
                // check required first
                if (ibMeta.isRequired()) {
                	discValidator.addValidation(inputName, Validator.IS_REQUIRED);
                }
            } else {

                if (idt.equals(ItemDataType.ST)) {
                    // a string's size could be more than 255, which is more
                    // than
                    // the db field length
                	discValidator.addValidation(inputName, Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 3999);

                } else if (idt.equals(ItemDataType.INTEGER)) {
                    // hard edit check, will comment out for now, tbh 05/2008
                   // v.addValidation(inputName, Validator.IS_AN_INTEGER);
                    // v.alwaysExecuteLastValidation(inputName);

                } else if (idt.equals(ItemDataType.REAL)) {
                    // hard edit check, will comment out for now, tbh 05/08
                    // v.addValidation(inputName, Validator.IS_A_NUMBER);
                    // v.alwaysExecuteLastValidation(inputName);
                } else if (idt.equals(ItemDataType.BL)) {
                    // there is no validation here since this data type is
                    // explicitly
                    // allowed to be null
                    // if the string input for this field parses to a non-zero
                    // number, the
                    // value will be true; otherwise, 0
                } else if (idt.equals(ItemDataType.BN)) {

                } else if (idt.equals(ItemDataType.SET)) {
                    // v.addValidation(inputName, Validator.NO_BLANKS_SET);
                	discValidator.addValidation(inputName, Validator.IN_RESPONSE_SET_SINGLE_VALUE, dib.getMetadata().getResponseSet());
                }

                else if (idt.equals(ItemDataType.DATE)) {
                    // hard edit check, will comment out for now, tbh 05/08
                     //v.addValidation(inputName, Validator.IS_A_DATE);
                    // v.alwaysExecuteLastValidation(inputName);
                }

                String customValidationString = dib.getMetadata().getRegexp();
                if (!StringUtil.isBlank(customValidationString)) {
                    Validation customValidation = null;

                    if (customValidationString.startsWith("func:")) {
                        try {
                            customValidation = Validator.processCRFValidationFunction(customValidationString);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (customValidationString.startsWith("regexp:")) {
                        try {
                            customValidation = Validator.processCRFValidationRegex(customValidationString);
                        } catch (Exception e) {
                        }
                    }

                    if (customValidation != null) {
                        customValidation.setErrorMessage(dib.getMetadata().getRegexpErrorMsg());
                        discValidator.addValidation(inputName, customValidation);
                    }
                }
            }
        }
        return dib;
    }
}
