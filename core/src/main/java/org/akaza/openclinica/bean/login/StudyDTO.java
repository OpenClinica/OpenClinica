package org.akaza.openclinica.bean.login;

import java.util.ArrayList;
import java.util.HashMap;

public class StudyDTO {
	private String uniqueProtocolID;
	private String briefTitle;
	private String principalInvestigator;
	private String briefSummary;
	private String sponsor;
	private String protocolType;
	private String startDate;
	private String expectedTotalEnrollment;
	private String status;
	private String studyOid;
    private ArrayList<ErrorObject> errors;
    private String message;
    private ArrayList<HashMap> assignUserRoles;
	
	public StudyDTO() {
		super();
	}


	public String getUniqueProtocolID() {
		return uniqueProtocolID;
	}

	public void setUniqueProtocolID(String uniqueProtocolID) {
		this.uniqueProtocolID = uniqueProtocolID;
	}


	public String getBriefTitle() {
		return briefTitle;
	}


	public void setBriefTitle(String briefTitle) {
		this.briefTitle = briefTitle;
	}


	public String getPrincipalInvestigator() {
		return principalInvestigator;
	}

	public void setPrincipalInvestigator(String principalInvestigator) {
		this.principalInvestigator = principalInvestigator;
	}

	public String getBriefSummary() {
		return briefSummary;
	}

	public void setBriefSummary(String briefSummary) {
		this.briefSummary = briefSummary;
	}

	public String getSponsor() {
		return sponsor;
	}

	public void setSponsor(String sponsor) {
		this.sponsor = sponsor;
	}

	public String getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType;
	}


	public String getStartDate() {
		return startDate;
	}


	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}


	public String getExpectedTotalEnrollment() {
		return expectedTotalEnrollment;
	}


	public void setExpectedTotalEnrollment(String expectedTotalEnrollment) {
		this.expectedTotalEnrollment = expectedTotalEnrollment;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getStudyOid() {
		return studyOid;
	}


	public void setStudyOid(String studyOid) {
		this.studyOid = studyOid;
	}


	public ArrayList<ErrorObject> getErrors() {
		return errors;
	}


	public void setErrors(ArrayList<ErrorObject> errors) {
		this.errors = errors;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public ArrayList<HashMap> getAssignUserRoles() {
		return assignUserRoles;
	}


	public void setAssignUserRoles(ArrayList<HashMap> assignUserRoles) {
		this.assignUserRoles = assignUserRoles;
	}



}
