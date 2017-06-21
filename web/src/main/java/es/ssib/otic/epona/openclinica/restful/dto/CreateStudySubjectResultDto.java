package es.ssib.otic.epona.openclinica.restful.dto;

import java.io.Serializable;

public class CreateStudySubjectResultDto
	implements Serializable {
	
	private static final long serialVersionUID =
		0L;
	
	private String studySubjectOid; 
	private int studySubjectId;
	private String errorMessage;

	public CreateStudySubjectResultDto() {
	}
	
	public String getStudySubjectOid() {
		return
			this.studySubjectOid;
	}
	
	public void setStudySubjectOid(
		String studySubjectOid) {
		
		this.studySubjectOid =
			studySubjectOid;
	}

	public int getStudySubjectId() {
		return
			this.studySubjectId;
	}

	public void setStudySubjectId(
		int studySubjectId) {

		this.studySubjectId =
			studySubjectId;
	}

	public String getErrorMessage() {
		return
			this.errorMessage;
	}
	
	public void setErrorMessage(
		String errorMessage) {
		
		this.errorMessage =
			errorMessage;
	}
}
