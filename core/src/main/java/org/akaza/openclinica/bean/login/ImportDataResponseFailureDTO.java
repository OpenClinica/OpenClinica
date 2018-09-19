package org.akaza.openclinica.bean.login;

import java.util.ArrayList;

public class ImportDataResponseFailureDTO {
	private String message;	
	
	private ArrayList<ErrorMessage> errors;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ArrayList<ErrorMessage> getErrors() {
		return errors;
	}

	public void setErrors(ArrayList<ErrorMessage> errors) {
		this.errors = errors;
	}
	
}
