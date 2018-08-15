package org.akaza.openclinica.bean.login;

import java.util.ArrayList;

public class ImportDataResponseDTO {
	private String message;	
	private ArrayList<String> detailMessages;
	private String id;
	private ArrayList<ErrorObject> errors;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ArrayList<ErrorObject> getErrors() {
		return errors;
	}
	public void setErrors(ArrayList<ErrorObject> errors) {
		this.errors = errors;
	}
	public ArrayList<String> getDetailMessages() {
		return detailMessages;
	}
	public void setDetailMessages(ArrayList<String> detailMessages) {
		this.detailMessages = detailMessages;
	}
}
