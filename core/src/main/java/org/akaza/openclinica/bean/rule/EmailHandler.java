package org.akaza.openclinica.bean.rule;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.apache.commons.validator.EmailValidator;
import org.exolab.castor.mapping.GeneralizedFieldHandler;

/**
 * The FieldHandler for the Date class
 *
 */
public class EmailHandler extends GeneralizedFieldHandler {

	/**
	 * Creates a new MyDateHandler instance
	 */
	public EmailHandler() {
		super();
	}

	/**
	 * This method is used to convert the value when the getValue method is called. The getValue method will obtain the actual field value from given 'parent' object. This convert method is then
	 * invoked with the field's value. The value returned from this method will be the actual value returned by getValue method.
	 *
	 * @param value
	 *            the object value to convert after performing a get operation
	 * @return the converted value.
	 */
	@Override
	public Object convertUponGet(Object value) {
		return value;
	}

	/**
	 * This method is used to convert the value when the setValue method is called. The setValue method will call this method to obtain the converted value. The converted value will then be used as
	 * the value to set for the field.
	 *
	 * @param value
	 *            the object value to convert before performing a set operation
	 * @return the converted value.
	 */
	@Override
	public Object convertUponSet(Object value) {

		Boolean areEmailsValid = true;
		String[] emails = ((String) value).split(",");
		for (String str : emails) {

			if (str.trim().startsWith("$") && !str.trim().equals("${participant}")) {
				throw new OpenClinicaSystemException("The  \"" + value + " \" you provided is not Valid, Please provide valid comma seperated addresses.");

			} else if (!str.trim().startsWith("$")) {
				areEmailsValid= isValidEmailAddress(str.trim());
		//		areEmailsValid = EmailValidator.getInstance().isValid(str.trim());
				if (!areEmailsValid) {
					throw new OpenClinicaSystemException("Email Address : \"" + str.trim() + " \" you provided is not Valid, Please provide valid comma seperated addresses.");
				}

			}

		}
		return value;
	}

	/**
	 * Returns the class type for the field that this GeneralizedFieldHandler converts to and from. This should be the type that is used in the object model.
	 *
	 * @return the class type of of the field
	 */
	@Override
	public Class getFieldType() {
		return String.class;
	}

	/**
	 * Creates a new instance of the object described by this field.
	 *
	 * @param parent
	 *            The object for which the field is created
	 * @return A new instance of the field's value
	 * @throws IllegalStateException
	 *             This field is a simple type and cannot be instantiated
	 */
	@Override
	public Object newInstance(Object parent) throws IllegalStateException {
		// -- Since it's marked as a string...just return null,
		// -- it's not needed.
		return null;
	}

	public static boolean isValidEmailAddress(String email) {
		   boolean result = true;
		   try {
		      InternetAddress emailAddr = new InternetAddress(email);
		      emailAddr.validate();
		   } catch (AddressException ex) {
		      result = false;
		   }
		   return result;
		}

	
}