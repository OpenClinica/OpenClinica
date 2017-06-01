package es.ssib.otic.epona.openclinica.restful.dto;

import java.io.Serializable;

public class CreateStudySubjectResultDto
	implements Serializable {
	
	private static final long serialVersionUID =
		0L;
	
	private String studySubjectOid; 
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
