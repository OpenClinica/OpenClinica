package org.akaza.openclinica.bean.login;

import java.util.List;

public class ResponseSuccessListAllParticipantsByStudyDTO {

	private String studyOid;
	private String siteOid;
	private String message;
	protected List<StudyParticipantDTO> studySubjects;
	
	public String getStudyOid() {
		return studyOid;
	}
	public void setStudyOid(String studyOid) {
		this.studyOid = studyOid;
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
	public List<StudyParticipantDTO> getStudySubjects() {
		return studySubjects;
	}
	public void setStudySubjects(List<StudyParticipantDTO> studySubjects) {
		this.studySubjects = studySubjects;
	}
	
}
