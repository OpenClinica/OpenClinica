package org.akaza.openclinica.bean.login;

public class ParticipantDTO {

	private String fName;
	private String accessCode;
	private String studyName;
	private String studyURL;
	private String message;
	private String eventName;
	private String emailAccount;
	private String emailSubject = "This is the Email Subject";
	
	
	
	
	public String getEmailSubject() {
		return emailSubject;
	}
	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}
	public String getEmailAccount() {
		return emailAccount;
	}
	public void setEmailAccount(String emailAccount) {
		this.emailAccount = emailAccount;
	}
	public String getfName() {
		return fName;
	}
	public void setfName(String fName) {
		this.fName = fName;
	}
	public String getAccessCode() {
		return accessCode;
	}
	public void setAccessCode(String accessCode) {
		this.accessCode = accessCode;
	}
	public String getStudyName() {
		return studyName;
	}
	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}
	public String getStudyURL() {
		return studyURL;
	}
	public void setStudyURL(String studyURL) {
		this.studyURL = studyURL;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

}
