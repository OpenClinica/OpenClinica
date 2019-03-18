package org.akaza.openclinica.controller.dto;

public class StudyEventScheduleDTO {

	private String ordinal;
	private String studyEventOID;
	private String subjectKey;			
	private String endDate;
	private String startDate;			
	private String studyOID;
	private String siteOID;
	int rowNum;
	
	public String getOrdinal() {
		return ordinal;
	}
	public void setOrdinal(String ordinal) {
		this.ordinal = ordinal;
	}
	public String getStudyEventOID() {
		return studyEventOID;
	}
	public void setStudyEventOID(String studyEventOID) {
		this.studyEventOID = studyEventOID;
	}
	public String getSubjectKey() {
		return subjectKey;
	}
	public void setSubjectKey(String subjectKey) {
		this.subjectKey = subjectKey;
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
	public String getStudyOID() {
		return studyOID;
	}
	public void setStudyOID(String studyOID) {
		this.studyOID = studyOID;
	}
	public String getSiteOID() {
		return siteOID;
	}
	public void setSiteOID(String siteOID) {
		this.siteOID = siteOID;
	}
	public int getRowNum() {
		return rowNum;
	}
	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}
}
