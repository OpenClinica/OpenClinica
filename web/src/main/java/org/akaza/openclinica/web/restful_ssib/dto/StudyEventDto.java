package org.akaza.openclinica.web.restful_ssib.dto;

import java.util.Date;

import org.akaza.openclinica.domain.datamap.StudyEvent;

import java.util.Date;

/**
 * Created by S004256 on 27/09/2016.
 */
public class StudyEventDto {

	private int studyEventId;
	private Date startDate;
	private int eventDefinitionCRFId;
	private int subjectId;
	private Integer statusId;
	private String eventName;

	public StudyEventDto() {
	}

	public StudyEventDto(
		StudyEvent studyEvent) {

		if (studyEvent == null) {
			return;
		}

		this.studyEventId =
			studyEvent.
				getStudyEventId();
		this.startDate =
			studyEvent.
				getDateStart();
		this.eventDefinitionCRFId =
			studyEvent.
				getStudyEventDefinition().
				getStudyEventDefinitionId();
		this.subjectId =
			studyEvent.
				getStudySubject().
				getStudySubjectId();
		this.statusId =
			studyEvent.
				getStatusId();
		this.eventName =
			studyEvent.
				getStudyEventDefinition().
				getName();
	}

	public int getStudyEventId() {
		return studyEventId;
	}

	public void setStudyEventId(int studyEventId) {
		this.studyEventId = studyEventId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public int getEventDefinitionCRFId() {
		return eventDefinitionCRFId;
	}

	public void setEventDefinitionCRFId(int eventDefinitionCRFId) {
		this.eventDefinitionCRFId = eventDefinitionCRFId;
	}

	public int getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}

	public Integer getStatusId() {
		return statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

        public String getEventName() {
		return eventName;
        }

	public void setEventName(
		String eventName) {
		this.eventName =
			eventName;
	}
}
