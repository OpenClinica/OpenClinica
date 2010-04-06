package org.akaza.openclinica.validator.rule.action;

import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.domain.rule.action.InsertActionBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.logic.expressionTree.ExpressionTreeHelper;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.List;

import javax.sql.DataSource;

public class InsertActionValidator implements Validator {

    ItemDAO itemDAO;
    DataSource dataSource;

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
            String p = "properties[" + i + "]";
            PropertyBean propertyBean = insertActionBean.getProperties().get(i);
            ValidationUtils.rejectIfEmpty(e, p + "oid", "oid.empty");
            ValidationUtils.rejectIfEmpty(e, p + "value", "value.empty");
            List<ItemBean> itemBeans = getItemDAO().findByOid(propertyBean.getOid());
            if (itemBeans.size() != 1) {
                e.rejectValue(p + "oid", "oid.invalid");
            }
            switch (itemBeans.get(0).getItemDataTypeId()) {
            case 6: { //ItemDataType.INTEGER
                try {
                    Integer.valueOf(propertyBean.getValue());
                } catch (NumberFormatException nfe) {
                    e.rejectValue(p + "value", "value.invalid.integer");
                }
                break;
            }
            case 7: { //ItemDataType.REAL
                try {
                    Float.valueOf(propertyBean.getValue());
                } catch (NumberFormatException nfe) {
                    e.rejectValue(p + "value", "value.invalid.float");
                }
                break;
            }
            case 9: { //ItemDataType.DATE
                if (!ExpressionTreeHelper.isDateyyyyMMdd(propertyBean.getValue())) {
                    e.rejectValue(p + "value", "value.invalid.date");
                }
                break;
            }
            case 10: { //ItemDataType.PDATE
                try {
                    Float.valueOf(propertyBean.getValue());
                } catch (NumberFormatException nfe) {
                    e.rejectValue(p + "value", "value.invalid.float");
                }
                break;
            }
            case 11: { //ItemDataType.FILE
                e.rejectValue(p + "value", "value.notSupported.file");
                break;
            }

            default:
                break;
            }
        }
    }

    public ItemDAO getItemDAO() {
        return this.itemDAO != null ? itemDAO : new ItemDAO(dataSource);
    }

    public void setItemDAO(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }

}
