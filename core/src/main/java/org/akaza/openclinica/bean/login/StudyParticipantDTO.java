package org.akaza.openclinica.bean.login;

import javax.validation.constraints.NotNull;

public class StudyParticipantDTO {
	
    private String subjectKey;
    private String subjectOid;
    private String status;
    private String createdBy;
    private String createdAt;
    private String lastModified;
    private String lastModifiedBy;
    private String firstName;
    private String lastName;
    private String secondaryID;
    
    public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getSecondaryID() {
		return secondaryID;
	}
	public void setSecondaryID(String secondaryID) {
		this.secondaryID = secondaryID;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	private String email;
    private String mobileNumber;
    

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
