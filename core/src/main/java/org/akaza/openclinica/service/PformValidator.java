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
	public void validate(Object target, Errors e) {
		ItemItemDataContainer container = (ItemItemDataContainer) target;
		String origValue = container.getItemDataBean().getValue();
		Integer responseTypeId = container.getResponseTypeId();
		Integer itemDataTypeId = container.getItemBean().getItemDataTypeId();
	//	System.out.println();
	//	System.out.print("Data type id:  " + itemDataTypeId);

		if (responseTypeId == 3 || responseTypeId == 7) {
			String[] values = origValue.split(",");
			for (String value : values) {
				subValidator(itemDataTypeId, value.trim(), e);
	//			System.out.print(" " + value);
			}
		} else {
			subValidator(itemDataTypeId, origValue, e);
	//		System.out.print(" " + origValue);

		}
	}

	public void subValidator(Integer itemDataTypeId, String value, Errors e) {
		if (value != null && value != "") {

			switch (itemDataTypeId) {
			case 6: { // ItemDataType.INTEGER
				try {
					Integer.valueOf(value);
		//			System.out.print(" Integer type");
				} catch (NumberFormatException nfe) {
		//			System.out.print(" Error");
					e.reject("value.invalid.Integer");
				}
				break;
			}
			case 7: { // ItemDataType.REAL
				try {
					Float.valueOf(value);
		//			System.out.print(" Real type");
				} catch (NumberFormatException nfe) {
		//			System.out.print(" Error");
					e.reject("value.invalid.float");
				}
				break;
			}
			case 9: { // ItemDataType.DATE
		//		System.out.print("  Date type");
				if (!ExpressionTreeHelper.isDateyyyyMMddDashes(value)) {
					System.out.print(" Error");
					e.reject("value.invalid.date");
				}
				break;
			}
			case 10: { // ItemDataType.PDATE
				e.reject("value.notSupported.pdate");
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
