package org.akaza.openclinica.service;

import java.text.MessageFormat;

import org.akaza.openclinica.domain.rule.action.InsertActionBean;
import org.akaza.openclinica.logic.expressionTree.ExpressionTreeHelper;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PformValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return ItemItemDataContainer.class.equals(clazz);
	}



	@Override
	public void  validate(Object target, Errors e) {
		ItemItemDataContainer container = (ItemItemDataContainer) target;
        String value=container.getItemDataBean().getValue();
		
        if (container.getItemDataBean().getValue() != null &&  container.getItemDataBean().getValue() != "") {
			
			switch (container.getItemBean().getItemDataTypeId()) {
	        case 6: { //ItemDataType.INTEGER
	            try {
	                Integer.valueOf(value);
	            } catch (NumberFormatException nfe) {
	                e.rejectValue(value, "value.invalid.integer");
	            }
	            break;
	        }
	        case 7: { //ItemDataType.REAL
	            try {
	                Float.valueOf(value);
	            } catch (NumberFormatException nfe) {
	                e.rejectValue(value, "value.invalid.float");
	            }
	            break;
	        }
	        case 9: { //ItemDataType.DATE
	            if (!ExpressionTreeHelper.isDateyyyyMMddDashes(value)) {
	                e.rejectValue(value, "value.invalid.date");
	            }
	            break;
	        }
	        case 10: { //ItemDataType.PDATE
	        	e.rejectValue(value, "value.notSupported.pdate");
	            break;
	        }
	        case 11: { //ItemDataType.FILE
	            e.rejectValue(value, "value.notSupported.file");
	            break;
	        }

	        default:
	            break;
	        }

	    }
	}
}
