package org.akaza.openclinica.service;

import org.akaza.openclinica.domain.rule.action.InsertActionBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PformValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return ItemItemDataContainer.class.equals(clazz);
	}

	@Override
	public void  validate(Object target, Errors errors) {
		ItemItemDataContainer container = (ItemItemDataContainer) target;
        String itemValue=container.getItemDataBean().getValue();
		
        if (container.getItemDataBean().getValue() != null &&  container.getItemDataBean().getValue() != "") {
			
			switch (container.getItemBean().getItemDataTypeId()) {
			   // Boolean
			case 1:
				break;
			case 2:
				break;
			case 3:
				break;
			case 4:
				break;
			   // String      String does not require validation
			case 5:
				break;
			// Integer
			case 6:
				try{
				    int in= Integer.valueOf(itemValue);
				    if (in==(int)in){
				      //  System.out.println("integer"+(int)in);
				    }
				}catch(Exception e){
				   // System.out.println(itemValue+ "  is not an INTEGER number");
				    errors.reject(itemValue);
				   break; 
				}
				// Real
			case 7:
				try{
				    double d= Double.valueOf(itemValue);
				    if (d==(double)d){
				    //    System.out.println("double"+(double)d);
				    }
				}catch(Exception e){
				//    System.out.println(itemValue+ "  is not a REAL number");
				    errors.reject(itemValue);
				   break; 
				}
			case 8:
				break;
				// Date      The validation of Date and PDate are covered within ItemDataDAO create EntityBean method 
			case 9:
				break;
				// pDate      The validation of Date and pDate are covered within ItemDataDAO create EntityBean method 
			case 10:
				break;
				// File
			case 11:
				break;
			case 12:
				break;

			}
		}
	}
}
