package org.akaza.openclinica.bean.login;

public class StudyParticipantDetailDTO extends StudyParticipantDTO {

	private String firstName;
    private String lastName;
    private String secondaryID;
    private String email;
    private String mobileNumber;
    
    
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

}
