package org.akaza.openclinica.bean.login;

import java.util.List;

public class ResponseSuccessListAllParticipantsByStudyDTO {

	
	protected List<StudyParticipantDTO> studyParticipants;

	public List<StudyParticipantDTO> getStudyParticipants() {
		return studyParticipants;
	}

	public void setStudyParticipants(List<StudyParticipantDTO> studyParticipants) {
		this.studyParticipants = studyParticipants;
	}
	
	
	
}
