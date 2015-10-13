package org.akaza.openclinica.bean.login;

public class ResponseSuccessStudyDTO {

	private String message;
	private String uniqueProtocolID;
	private String studyOid;

	public String getUniqueProtocolID() {
		return uniqueProtocolID;
	}

	public void setUniqueProtocolID(String uniqueProtocolID) {
		this.uniqueProtocolID = uniqueProtocolID;
	}

	public String getStudyOid() {
		return studyOid;
	}

	public void setStudyOid(String studyOid) {
		this.studyOid = studyOid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
