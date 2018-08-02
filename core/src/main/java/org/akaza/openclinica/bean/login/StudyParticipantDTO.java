package org.akaza.openclinica.bean.login;


import java.util.ArrayList;

public class StudyParticipantDTO {
	
    private String subjectKey;    
      
	private ArrayList<String> message;

	public String getSubjectKey() {
		return subjectKey;
	}

	public void setSubjectKey(String subjectKey) {
		this.subjectKey = subjectKey;
	}

	public ArrayList<String> getMessage() {
		return message;
	}

	public void setMessage(ArrayList<String> message) {
		this.message = message;
	}
    
    
}
