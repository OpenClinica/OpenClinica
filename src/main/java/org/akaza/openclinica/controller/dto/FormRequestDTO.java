package org.akaza.openclinica.controller.dto;

public class FormRequestDTO {

	private String subjectKey;
	private String studyEventOID;
	private String studyEventRepeatKey;
	private String formOID;
	private String formWorkflowStatus;
	private String required;
	private String relevant;
	private String editable;

	public String getSubjectKey() {
		return subjectKey;
	}
	public void setSubjectKey(String subjectKey) {
		this.subjectKey = subjectKey;
	}
		
	public String getStudyEventOID() {
		return studyEventOID;
	}
	public void setStudyEventOID(String studyEventOID) {
		this.studyEventOID = studyEventOID;
	}

	public String getStudyEventRepeatKey() { return studyEventRepeatKey; }
	public void setStudyEventRepeatKey(String studyEventRepeatKey) { this.studyEventRepeatKey = studyEventRepeatKey;}

	public String getFormOID() { return formOID;}
	public void setFormOID(String formOID) { this.formOID = formOID; }

	public String getFormWorkflowStatus() {	return formWorkflowStatus; }
	public void setFormWorkflowStatus(String formWorkflowStatus) { this.formWorkflowStatus = formWorkflowStatus; }

	public String getRequired() { return required; }
	public void setRequired(String required) { this.required = required; }

	public String getRelevant() { return relevant; }
	public void setRelevant(String relevant) { this.relevant = relevant; }

	public String getEditable() { return editable; }
	public void setEditable(String editable) { this.editable = editable; }
}
