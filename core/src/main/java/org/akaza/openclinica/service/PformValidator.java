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
		String origValue=container.getItemDataBean().getValue();
	    Integer responseTypeId= container.getResponseTypeId();
        Integer itemDataTypeId =container.getItemBean().getItemDataTypeId();
        System.out.println("item data type id:  "+itemDataTypeId);
        
	    if (responseTypeId==3 || responseTypeId==7){
            String[] values = origValue.split(",");            
            for(String value : values){
            	subValidator(itemDataTypeId, value, e);
                System.out.println(value);        
            }
            }else{
            	subValidator(itemDataTypeId, origValue, e);            	
            }
	}
	
	
 public void subValidator(Integer itemDataTypeId, String value , Errors e){	
        if (value != null &&  value != "") {
			
			switch (itemDataTypeId) {
	        case 6: { //ItemDataType.INTEGER
	            try {	 Integer.valueOf(value);
	                    System.out.println("I'm in Integer data type");
	            } catch (NumberFormatException nfe) {
	                e.reject("value.invalid.Integer");
	            }
	            break;
	        }
	        case 7: { //ItemDataType.REAL
	            try {
	                Float.valueOf(value);
                    System.out.println("I'm in Real data type");
	            } catch (NumberFormatException nfe) {
	                e.reject("value.invalid.float");
	            }
	            break;
	        }
	        case 9: { //ItemDataType.DATE
                System.out.println("I'm in Date data type");
	            if (!ExpressionTreeHelper.isDateyyyyMMddDashes(value)) {
	                e.reject("value.invalid.date");
	            }
	            break;
	        }
	        case 10: { //ItemDataType.PDATE
	        	e.reject("value.notSupported.pdate");
	            break;
	        }
	        case 11: { //ItemDataType.FILE
	            e.reject("value.notSupported.file");
	            break;
	        }

	        default:
	            break;
	        }

	    }
	}
        
        
        
        
        
        
	
}
