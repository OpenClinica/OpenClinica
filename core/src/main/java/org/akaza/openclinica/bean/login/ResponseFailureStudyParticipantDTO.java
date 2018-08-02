package org.akaza.openclinica.bean.login;

import java.util.ArrayList;

public class ResponseFailureStudyParticipantDTO {

	private ArrayList<String> message;
	private ArrayList<String> params;
	
	public ArrayList getMessage() {
		if(message == null) {
			message = new ArrayList<String>();
		}
		return message;
	}
	public void setMessage(ArrayList message) {
		this.message = message;
	}
	public ArrayList getParams() {
		if(params ==null) {
			params = new ArrayList<String>();
		}
		return params;
	}
	public void setParams(ArrayList params) {
		this.params = params;
	}

}
