package org.akaza.openclinica.controller.dto;

public class DataImportReport {
    private String subjectKey;
    private String studySubjectID;
    private String studyEventOID;
    private String studyEventRepeatKey;
    private String formOID;
    private String itemGroupOID;
    private String itemGroupRepeatKey;
    private String itemOID;
    private String status;
    private String timeStamp;
    private String message;
    private Integer rowNumber;


    public DataImportReport(String subjectKey, String studySubjectID, String studyEventOID, String studyEventRepeatKey, String formOID, String itemGroupOID, String itemGroupRepeatKey, String itemOID, String status, String timeStamp, String message) {
        this.subjectKey = subjectKey;
        this.studySubjectID = studySubjectID;
        this.studyEventOID = studyEventOID;
        this.studyEventRepeatKey = studyEventRepeatKey;
        this.formOID = formOID;
        this.itemGroupOID = itemGroupOID;
        this.itemGroupRepeatKey = itemGroupRepeatKey;
        this.itemOID = itemOID;
        this.status = status;
        this.timeStamp = timeStamp;
        this.message = message;
    }

    public DataImportReport(Integer rowNumber, String studySubjectID, String studyEventOID, String studyEventRepeatKey,  String status, String message) {
        this.studySubjectID = studySubjectID;
        this.studyEventOID = studyEventOID;
        this.studyEventRepeatKey = studyEventRepeatKey;
        this.status = status;
        this.message = message;
        this.rowNumber=rowNumber;
    }


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

    public String getFormOID() {
        return formOID;
    }

    public void setFormOID(String formOID) {
        this.formOID = formOID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getItemGroupOID() {
        return itemGroupOID;
    }

    public void setItemGroupOID(String itemGroupOID) {
        this.itemGroupOID = itemGroupOID;
    }

    public String getItemOID() {
        return itemOID;
    }

    public void setItemOID(String itemOID) {
        this.itemOID = itemOID;
    }

    public String getStudyEventRepeatKey() {
        return studyEventRepeatKey;
    }

    public void setStudyEventRepeatKey(String studyEventRepeatKey) {
        this.studyEventRepeatKey = studyEventRepeatKey;
    }

    public String getItemGroupRepeatKey() {
        return itemGroupRepeatKey;
    }

    public void setItemGroupRepeatKey(String itemGroupRepeatKey) {
        this.itemGroupRepeatKey = itemGroupRepeatKey;
    }

    public String getStudySubjectID() {
        return studySubjectID;
    }

    public void setStudySubjectID(String studySubjectID) {
        this.studySubjectID = studySubjectID;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }
}
