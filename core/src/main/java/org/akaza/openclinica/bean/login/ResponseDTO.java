package org.akaza.openclinica.bean.login;

import java.util.ArrayList;

public class ResponseDTO {
	
	private ArrayList<ErrorObject> errors;
    private String message;
    
	public ArrayList<ErrorObject> getErrors() {
		return errors;
	}
	public void setErrors(ArrayList<ErrorObject> errors) {
		this.errors = errors;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}

