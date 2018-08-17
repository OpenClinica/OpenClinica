package org.akaza.openclinica.bean.login;

import java.util.ArrayList;

public class ImportDataResponseDTO {
	private String message;	
	private ArrayList<String> detailMessages;
	
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
	public ArrayList<String> getDetailMessages() {
		return detailMessages;
	}
	public void setDetailMessages(ArrayList<String> detailMessages) {
		this.detailMessages = detailMessages;
	}
}
