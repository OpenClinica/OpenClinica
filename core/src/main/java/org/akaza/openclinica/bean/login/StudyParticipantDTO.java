package org.akaza.openclinica.bean.login;


public class StudyParticipantDTO {
	
    private String subjectKey;
    private String subjectOid;
    private String status;
    private String createdBy;
    private String createdAt;
    private String lastModified;
    private String lastModifiedBy;

	public String getSubjectKey() {
		return subjectKey;
	}
	public void setSubjectKey(String subjectKey) {
		this.subjectKey = subjectKey;
	}

	public String getSubjectOid() {	return subjectOid; }
	public void setSubjectOid(String subjectOid) { this.subjectOid = subjectOid; }

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

}
