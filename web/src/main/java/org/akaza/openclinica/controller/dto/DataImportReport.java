package org.akaza.openclinica.controller.dto;

public class DataImportReport {
    private String SubjectKey;
    private String StudyEventOID;
    private Integer StudyEventRepeatKey;
    private String FormOID;
    private String FormLayoutOID;
    private String itemGroupOID;
    private Integer itemGroupRepeatKey;
    private String itemOID;
    private String FormStatus;
    private String Status;
    private String Message;

    public DataImportReport(String subjectKey, String studyEventOID, int studyEventRepeatKey, String formOID, String formLayoutOID, String itemGroupOID, int itemGroupRepeatKey, String itemOID, String formStatus, String status, String message) {
        SubjectKey = subjectKey;
        StudyEventOID = studyEventOID;
        StudyEventRepeatKey = studyEventRepeatKey;
        FormOID = formOID;
        FormLayoutOID = formLayoutOID;
        this.itemGroupOID = itemGroupOID;
        this.itemGroupRepeatKey = itemGroupRepeatKey;
        this.itemOID = itemOID;
        FormStatus = formStatus;
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

    public String getFormStatus() {
        return FormStatus;
    }

    public void setFormStatus(String formStatus) {
        FormStatus = formStatus;
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

    public Integer getStudyEventRepeatKey() {
        return StudyEventRepeatKey;
    }

    public void setStudyEventRepeatKey(Integer studyEventRepeatKey) {
        StudyEventRepeatKey = studyEventRepeatKey;
    }

    public Integer getItemGroupRepeatKey() {
        return itemGroupRepeatKey;
    }

    public void setItemGroupRepeatKey(Integer itemGroupRepeatKey) {
        this.itemGroupRepeatKey = itemGroupRepeatKey;
    }
}
