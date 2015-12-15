package org.akaza.openclinica.bean.login;

public class UserDTO {

private String fName ;
private String lName ;
private String mobile;
private String userName;
private String password;
private String accessCode;
private String apiKey;
private String email;
private String studySubjectId;


public String getStudySubjectId() {
	return studySubjectId;
}
public void setStudySubjectId(String studySubjectId) {
	this.studySubjectId = studySubjectId;
}
public String getfName() {
	return fName;
}
public void setfName(String fName) {
	this.fName = fName;
}
public String getlName() {
	return lName;
}
public void setlName(String lName) {
	this.lName = lName;
}
public String getMobile() {
	return mobile;
}
public void setMobile(String mobile) {
	this.mobile = mobile;
}
public String getUserName() {
	return userName;
}
public void setUserName(String userName) {
	this.userName = userName;
}
public String getAccessCode() {
	return accessCode;
}
public void setAccessCode(String accessCode) {
	this.accessCode = accessCode;
}
public String getApiKey() {
    return apiKey;
}
public void setApiKey(String value) {
    this.apiKey = value;
}
public String getPassword() {
	return password;
}
public void setPassword(String password) {
	this.password = password;
}
public String getEmail() {
	return email;
}
public void setEmail(String email) {
	this.email = email;
}

	
}
