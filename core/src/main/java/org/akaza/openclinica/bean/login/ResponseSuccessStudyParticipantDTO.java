package org.akaza.openclinica.bean.login;

public class ResponseSuccessStudyParticipantDTO {
	
	private String subjectKey;
	private String subjectOid;
	private String status;
	
	public String getSubjectKey() {
		return subjectKey;
	}
	public void setSubjectKey(String subjectKey) {
		this.subjectKey = subjectKey;
	}

	public String getSubjectOid() { return subjectOid; }
	public void setSubjectOid(String subjectOid) { this.subjectOid = subjectOid; }

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}	
	
}

