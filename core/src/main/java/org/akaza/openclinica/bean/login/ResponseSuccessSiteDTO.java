package org.akaza.openclinica.bean.login;

public class ResponseSuccessSiteDTO {

	private String uniqueSiteStudyID;
	private String siteOid;
	private String message;

	public String getUniqueSiteStudyID() {
		return uniqueSiteStudyID;
	}

	public void setUniqueSiteStudyID(String uniqueSiteStudyID) {
		this.uniqueSiteStudyID = uniqueSiteStudyID;
	}

	public String getSiteOid() {
		return siteOid;
	}

	public void setSiteOid(String siteOid) {
		this.siteOid = siteOid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
