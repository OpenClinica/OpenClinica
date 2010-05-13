package org.akaza.openclinica.validator.rule.action;

import org.akaza.openclinica.bean.core.NullValue;
import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.action.InsertActionBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.logic.expressionTree.ExpressionTreeHelper;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class InsertActionValidator implements Validator {

    ItemDAO itemDAO;
    ItemFormMetadataDAO itemFormMetadataDAO;
    DataSource dataSource;
    EventDefinitionCRFBean eventDefinitionCRFBean;
    ExpressionService expressionService;
    RuleSetBean ruleSetBean;

    public InsertActionValidator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * This Validator validates just Person instances
     */
    public boolean supports(Class clazz) {
        return InsertActionBean.class.equals(clazz);
    }

    public void validate(Object obj, Errors e) {
        InsertActionBean insertActionBean = (InsertActionBean) obj;
        for (int i = 0; i < insertActionBean.getProperties().size(); i++) {
            String p = "properties[" + i + "].";
            PropertyBean propertyBean = insertActionBean.getProperties().get(i);
            ValidationUtils.rejectIfEmpty(e, p + "oid", "oid.empty");

            String expression = getExpressionService().constructFullExpressionIfPartialProvided(propertyBean.getOid(), getRuleSetBean().getTarget().getValue());
            List<ItemBean> itemBeans = getItemDAO().findByOid(getExpressionService().getItemOid(expression));
            if (!getExpressionService().isInsertActionExpressionValid(propertyBean.getOid(), getRuleSetBean(), 3) || itemBeans.size() != 1) {
                e.rejectValue(p + "oid", "oid.invalid", "OID: " + propertyBean.getOid() + " is Invalid.");
            } else {

                if (propertyBean.getValueExpression() != null && propertyBean.getValueExpression().getValue() != null
                    && propertyBean.getValueExpression().getValue().length() != 0) {

                    String valueExpression =
                        getExpressionService().constructFullExpressionIfPartialProvided(propertyBean.getValueExpression().getValue(),
                                getRuleSetBean().getTarget().getValue());
                    List<ItemBean> valueItemBeans = getItemDAO().findByOid(getExpressionService().getItemOid(valueExpression));
                    if (!getExpressionService().isExpressionValid(propertyBean.getValueExpression().getValue(), getRuleSetBean(), 2)
                        || valueItemBeans.size() != 1) {
                        e.rejectValue(p + "valueExpression", "valueExpression.invalid", "Value provided for ValueExpression is Invalid");
                    }
                } else {
                    if (propertyBean.getValue() == null || propertyBean.getValue().length() > 0) {
                        ValidationUtils.rejectIfEmpty(e, p + "value", "value.empty");
                    } else {
                        checkValidity(itemBeans.get(0), propertyBean.getValue(), p, e);
                    }
                }

            }
        }
    }

    @SuppressWarnings("unchecked")
    private void checkValidity(ItemBean itemBean, String value, String index, Errors e) {
        Boolean result = false;
        List<ItemFormMetadataBean> itemFormMetadataBeans = getItemFormMetadataDAO().findAllByItemId(itemBean.getId());
        for (ItemFormMetadataBean itemFormMetadataBean : itemFormMetadataBeans) {

            if (itemFormMetadataBean.getResponseSet().getResponseType().equals(ResponseType.RADIO)
                || itemFormMetadataBean.getResponseSet().getResponseType().equals(ResponseType.SELECT)) {
                if (matchValueWithOptions(value, itemFormMetadataBean.getResponseSet().getOptions()) != null) {
                    result = true;
                    break;
                }
            }

            if (itemFormMetadataBean.getResponseSet().getResponseType().equals(ResponseType.CHECKBOX)
                || itemFormMetadataBean.getResponseSet().getResponseType().equals(ResponseType.SELECTMULTI)) {
                if (matchValueWithManyOptions(value, itemFormMetadataBean.getResponseSet().getOptions()) != null) {
                    result = true;
                    break;
                }
            }

            if (itemFormMetadataBean.getResponseSet().getResponseType().equals(ResponseType.TEXT)
                || itemFormMetadataBean.getResponseSet().getResponseType().equals(ResponseType.TEXTAREA)) {
                if (checkValidityBasedonNullValues(value, index, e)) {
                    result = true;
                    break;
                } else {
                    int errorCount = e.getErrorCount();
                    checkValidityBasedOnDataType(itemBean, value, index, e);
                    if (e.getErrorCount() == errorCount) {
                        result = true;
                        break;
                    }
                }
            }

            if (itemFormMetadataBean.getResponseSet().getResponseType().equals(ResponseType.CALCULATION)
                || itemFormMetadataBean.getResponseSet().getResponseType().equals(ResponseType.GROUP_CALCULATION)) {
                result = false;
                break;

            }

        }
        if (!result) {
            e.rejectValue(index + "value", "value.invalid");
        }
    }

    private String matchValueWithOptions(String value, List<ResponseOptionBean> options) {
        String returnedValue = null;
        if (!options.isEmpty()) {
            for (ResponseOptionBean responseOptionBean : options) {
                if (responseOptionBean.getValue().equals(value)) {
                    return responseOptionBean.getValue();

                }
            }
        }
        return returnedValue;
    }

    @SuppressWarnings("unchecked")
    private String matchValueWithManyOptions(String value, List<ResponseOptionBean> options) {
        String returnedValue = null;
        String entireOptions = "";
        String[] simValues = value.split(",");
        String simValue = value.replace(",", "");
        simValue = simValue.replace(" ", "");
        boolean checkComplete = true;

        if (!options.isEmpty()) {
            for (ResponseOptionBean responseOptionBean : options) {
                entireOptions += responseOptionBean.getValue();
            }
            // remove spaces, since they are causing problems:
            entireOptions = entireOptions.replace(" ", "");

            ArrayList nullValues = getEventDefinitionCRFBean().getNullValuesList();

            for (Object nullValue : nullValues) {
                NullValue nullValueTerm = (NullValue) nullValue;
                entireOptions += nullValueTerm.getName();
            }

            for (String sim : simValues) {
                sim = sim.replace(" ", "");
                checkComplete = entireOptions.contains(sim);// Pattern.matches(entireOptions,sim);
                if (!checkComplete) {
                    return returnedValue;
                }
            }
        }
        return value;
    }

    private Boolean checkValidityBasedonNullValues(String value, String index, Errors e) {

        return getEventDefinitionCRFBean().getNullValuesList().contains(NullValue.getByName(value)) ? true : false;
    }

    private void checkValidityBasedOnDataType(ItemBean itemBean, String value, String index, Errors e) {
        switch (itemBean.getItemDataTypeId()) {
        case 6: { //ItemDataType.INTEGER
            try {
                Integer.valueOf(value);
            } catch (NumberFormatException nfe) {
                e.rejectValue(index + "value", "value.invalid.integer");
            }
            break;
        }
        case 7: { //ItemDataType.REAL
            try {
                Float.valueOf(value);
            } catch (NumberFormatException nfe) {
                e.rejectValue(index + "value", "value.invalid.float");
            }
            break;
        }
        case 9: { //ItemDataType.DATE
            if (!ExpressionTreeHelper.isDateyyyyMMdd(value)) {
                e.rejectValue(index + "value", "value.invalid.date");
            }
            break;
        }
        case 10: { //ItemDataType.PDATE
            try {
                Float.valueOf(value);
            } catch (NumberFormatException nfe) {
                e.rejectValue(index + "value", "value.invalid.float");
            }
            break;
        }
        case 11: { //ItemDataType.FILE
            e.rejectValue(index + "value", "value.notSupported.file");
            break;
        }

        default:
            break;
        }

    }

    public ItemDAO getItemDAO() {
        return this.itemDAO != null ? itemDAO : new ItemDAO(dataSource);
    }

    public ItemFormMetadataDAO getItemFormMetadataDAO() {
        return this.itemFormMetadataDAO != null ? itemFormMetadataDAO : new ItemFormMetadataDAO(dataSource);
    }

    public EventDefinitionCRFBean getEventDefinitionCRFBean() {
        return eventDefinitionCRFBean;
    }

    public void setEventDefinitionCRFBean(EventDefinitionCRFBean eventDefinitionCRFBean) {
        this.eventDefinitionCRFBean = eventDefinitionCRFBean;
    }

    public ExpressionService getExpressionService() {
        return expressionService;
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public RuleSetBean getRuleSetBean() {
        return ruleSetBean;
    }

    public void setRuleSetBean(RuleSetBean ruleSetBean) {
        this.ruleSetBean = ruleSetBean;
    }
}
