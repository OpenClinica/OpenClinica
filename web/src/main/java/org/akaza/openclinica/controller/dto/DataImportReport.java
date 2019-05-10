package org.akaza.openclinica.controller.dto;

public class DataImportReport {
    private String SubjectKey;
    private String StudySubjectID;
    private String StudyEventOID;
    private String StudyEventRepeatKey;
    private String FormOID;
    private String FormLayoutOID;
    private String itemGroupOID;
    private String itemGroupRepeatKey;
    private String itemOID;
    private String value;
    private String Status;
    private String Message;


    public DataImportReport(String subjectKey, String studySubjectID, String studyEventOID, String studyEventRepeatKey, String formOID, String formLayoutOID, String itemGroupOID, String itemGroupRepeatKey, String itemOID,String value ,String status, String message) {
        SubjectKey = subjectKey;
        StudySubjectID = studySubjectID;
        StudyEventOID = studyEventOID;
        StudyEventRepeatKey = studyEventRepeatKey;
        FormOID = formOID;
        FormLayoutOID = formLayoutOID;
        this.itemGroupOID = itemGroupOID;
        this.itemGroupRepeatKey = itemGroupRepeatKey;
        this.itemOID = itemOID;
        this.value = value;
        Status = status;
        Message = message;
    }

    public String getSubjectKey() {
        return SubjectKey;
    }

    public void setSubjectKey(String subjectKey) {
        SubjectKey = subjectKey;
    }

    public String getStudyEventOID() {
        return StudyEventOID;
    }

    public void setStudyEventOID(String studyEventOID) {
        StudyEventOID = studyEventOID;
    }

    public String getFormOID() {
        return FormOID;
    }

    public void setFormOID(String formOID) {
        FormOID = formOID;
    }

    public String getFormLayoutOID() {
        return FormLayoutOID;
    }

    public void setFormLayoutOID(String formLayoutOID) {
        FormLayoutOID = formLayoutOID;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
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
        return StudyEventRepeatKey;
    }

    public void setStudyEventRepeatKey(String studyEventRepeatKey) {
        StudyEventRepeatKey = studyEventRepeatKey;
    }

    public String getItemGroupRepeatKey() {
        return itemGroupRepeatKey;
    }

    public void setItemGroupRepeatKey(String itemGroupRepeatKey) {
        this.itemGroupRepeatKey = itemGroupRepeatKey;
    }

    public String getStudySubjectID() {
        return StudySubjectID;
    }

    public void setStudySubjectID(String studySubjectID) {
        StudySubjectID = studySubjectID;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
