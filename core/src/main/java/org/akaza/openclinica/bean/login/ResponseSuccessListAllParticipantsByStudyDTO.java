package org.akaza.openclinica.bean.login;

import java.util.List;

public class ResponseSuccessListAllParticipantsByStudyDTO {

	
	protected List<StudyParticipantDTO> studyParticipants;
	protected String SiteOID;
	protected int SiteID;
	protected String SiteName;

	public int getSiteID() {
		return SiteID;
	}

	public void setSiteID(int siteID) {
		SiteID = siteID;
	}

	public String getSiteOID() {
		return SiteOID;
	}

	public void setSiteOID(String siteOID) {
		SiteOID = siteOID;
	}

	public String getSiteName() {
		return SiteName;
	}

	public void setSiteName(String siteName) {
		SiteName = siteName;
	}

	public List<StudyParticipantDTO> getStudyParticipants() {
		return studyParticipants;
	}

	public void setStudyParticipants(List<StudyParticipantDTO> studyParticipants) {
		this.studyParticipants = studyParticipants;
	}
	
	
	
}
