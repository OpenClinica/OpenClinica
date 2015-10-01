package org.akaza.openclinica.bean.login;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SiteDTO {
	private String uniqueSiteProtocolID;
	private String briefTitle;
	private String principalInvestigator;
	private String expectedTotalEnrollment;
	private String startDate;
	private String protocolDateVerification;
	private String secondaryProId;
    private String siteOid;
    private ArrayList<ErrorObject> errors;
    private String message;
    private ArrayList<UserRole> assignUserRoles;


	public SiteDTO() {
		super();
	}


	public String getUniqueSiteProtocolID() {
		return uniqueSiteProtocolID;
	}

	public void setUniqueSiteProtocolID(String uniqueSiteProtocolID) {
		this.uniqueSiteProtocolID = uniqueSiteProtocolID;
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


	public String getExpectedTotalEnrollment() {
		return expectedTotalEnrollment;
	}


	public void setExpectedTotalEnrollment(String expectedTotalEnrollment) {
		this.expectedTotalEnrollment = expectedTotalEnrollment;
	}


	public String getStartDate() {
		return startDate;
	}


	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}


	public String getProtocolDateVerification() {
		return protocolDateVerification;
	}


	public void setProtocolDateVerification(String protocolDateVerification) {
		this.protocolDateVerification = protocolDateVerification;
	}


	public String getSecondaryProId() {
		return secondaryProId;
	}

	public void setSecondaryProId(String secondaryProId) {
		this.secondaryProId = secondaryProId;
	}


	public String getSiteOid() {
		return siteOid;
	}


	public void setSiteOid(String siteOid) {
		this.siteOid = siteOid;
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


	public ArrayList<UserRole> getAssignUserRoles() {
		return assignUserRoles;
	}


	public void setAssignUserRoles(ArrayList<UserRole> assignUserRoles) {
		this.assignUserRoles = assignUserRoles;
	}

}
