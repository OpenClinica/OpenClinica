package org.akaza.openclinica.bean.login;

public class ResponseSuccessSiteDTO {

	private String uniqueSiteProtocolID;
	private String siteOid;
	private String message;

	public String getUniqueSiteProtocolID() {
		return uniqueSiteProtocolID;
	}

	public void setUniqueSiteProtocolID(String uniqueSiteProtocolID) {
		this.uniqueSiteProtocolID = uniqueSiteProtocolID;
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
