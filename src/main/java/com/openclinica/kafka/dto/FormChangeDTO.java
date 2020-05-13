package com.openclinica.kafka.dto;

public class FormChangeDTO {
    private String customerUuid;
    private String studyUuid;
    private String studyEnvironmentUuid;
    private String studyOid;
    private String siteOid;
    private String participantId;
    private String participantOid;
    private String formOid;
    private String eventOid;
    private int eventRepeatKey;
    private String createdDate;
    private String updateDate;
    private String formWorkflowStatus;

    public String getCustomerUuid() { return customerUuid; }
    public void setCustomerUuid(String customerUuid) { this.customerUuid = customerUuid; }

    public String getStudyUuid() { return studyUuid; }
    public void setStudyUuid(String studyUuid) { this.studyUuid = studyUuid; }

    public String getStudyEnvironmentUuid() { return studyEnvironmentUuid; }
    public void setStudyEnvironmentUuid(String studyEnvironmentUuid) { this.studyEnvironmentUuid = studyEnvironmentUuid; }

    public String getStudyOid() {
        return studyOid;
    }
    public void setStudyOid(String studyOid) {
        this.studyOid = studyOid;
    }

    public String getSiteOid() {
        return siteOid;
    }
    public void setSiteOid(String siteOid) {
        this.siteOid = siteOid;
    }

    public String getParticipantId() {
        return participantId;
    }
    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public String getParticipantOid() {
        return participantOid;
    }
    public void setParticipantOid(String participantOid) {
        this.participantOid = participantOid;
    }

    public String getFormOid() {
        return formOid;
    }
    public void setFormOid(String formOid) {
        this.formOid = formOid;
    }

    public String getEventOid() {
        return eventOid;
    }
    public void setEventOid(String eventOid) {
        this.eventOid = eventOid;
    }

    public int getEventRepeatKey() { return eventRepeatKey; }
    public void setEventRepeatKey(int eventRepeatKey) { this.eventRepeatKey = eventRepeatKey; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getUpdateDate() {  return updateDate; }
    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }

    public String getFormWorkflowStatus() { return formWorkflowStatus; }
    public void setFormWorkflowStatus(String formWorkflowStatus) { this.formWorkflowStatus = formWorkflowStatus; }
}
