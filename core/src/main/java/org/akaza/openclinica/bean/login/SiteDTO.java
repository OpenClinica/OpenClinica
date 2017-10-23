package org.akaza.openclinica.bean.login;

import org.akaza.openclinica.bean.core.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SiteDTO {
	private String uniqueSiteProtocolID;
	private String briefTitle;
	private String briefDescription;
	private String principalInvestigator;

	private Integer expectedTotalEnrollment;
    private String siteOid;
    private ArrayList<ErrorObject> errors;
    private String message;
    private Status status;
    private FacilityInfo facilityInfo;

	public SiteDTO() {
		super();
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getBriefDescription() {
		return briefDescription;
	}

	public void setBriefDescription(String briefDescription) {
		this.briefDescription = briefDescription;
	}

	public String getUniqueSiteProtocolID() {
		return uniqueSiteProtocolID;
	}

	public void setUniqueSiteProtocolID(String uniqueSiteProtocolID) {
		this.uniqueSiteProtocolID = uniqueSiteProtocolID;
	}

    public FacilityInfo getFacilityInfo() {
        return facilityInfo;
    }

    public void setFacilityInfo(FacilityInfo facilityInfo) {
        this.facilityInfo = facilityInfo;
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


	public Integer getExpectedTotalEnrollment() {
		return expectedTotalEnrollment;
	}


	public void setExpectedTotalEnrollment(Integer expectedTotalEnrollment) {
		this.expectedTotalEnrollment = expectedTotalEnrollment;
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


}
