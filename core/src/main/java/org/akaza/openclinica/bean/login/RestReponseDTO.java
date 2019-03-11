package org.akaza.openclinica.bean.login;

import java.util.ArrayList;

public class RestReponseDTO {
	private ArrayList<String> errors;
    public ArrayList<String> getErrors() {
		return errors;
	}
	public void setErrors(ArrayList<String> errors) {
		this.errors = errors;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	private String message;
}
