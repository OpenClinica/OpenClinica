package org.akaza.openclinica.bean.login;

import java.util.ArrayList;

public class ImportDataResponseSuccessDTO {
	private String message;	
	private ArrayList<String> detailMessages;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public ArrayList<String> getDetailMessages() {
		return detailMessages;
	}
	public void setDetailMessages(ArrayList<String> detailMessages) {
		this.detailMessages = detailMessages;
	}
	
}
