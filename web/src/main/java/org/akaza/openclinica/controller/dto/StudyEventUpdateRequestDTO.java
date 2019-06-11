package org.akaza.openclinica.controller.dto;

import org.akaza.openclinica.domain.datamap.SubjectEventStatus;

public class StudyEventUpdateRequestDTO {

	private String subjectKey;
	private String studyEventOID;
	private String studyEventRepeatKey;
	private String startDate;
	private String endDate;
	private String eventStatus;

		
	public String getStudyEventOID() {
		return studyEventOID;
	}
	public void setStudyEventOID(String studyEventOID) {
		this.studyEventOID = studyEventOID;
	}
	
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getSubjectKey() {
		return subjectKey;
	}

	public void setSubjectKey(String subjectKey) {
		this.subjectKey = subjectKey;
	}


    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

	public String getStudyEventRepeatKey() {
		return studyEventRepeatKey;
	}

	public void setStudyEventRepeatKey(String studyEventRepeatKey) {
		this.studyEventRepeatKey = studyEventRepeatKey;
	}
}
