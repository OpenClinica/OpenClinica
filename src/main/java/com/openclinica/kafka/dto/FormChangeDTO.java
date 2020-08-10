package com.openclinica.kafka.dto;

public class FormChangeDTO {
    private String customerUuid;
    private String studyUuid;
    private String studyEnvironmentUuid;
    private String studyOid;
    private String siteOid;
    private String participantId;
    private String participantOid;
    private String eventOid;
    private int eventRepeatKey;
    private String formOid;
    private String formCreatedDate;
    private String formUpdatedDate;
    private String formCreatedBy;
    private String formUpdatedBy;
    private String formWorkflowStatus;
    private String formSdvStatus;
    private String formRemoved;
    private String formArchived;
    private String formRequired;
    private String formRelevant;
    private String formEditable;

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

    public String getFormCreatedDate() { return formCreatedDate; }
    public void setFormCreatedDate(String formCreatedDate) { this.formCreatedDate = formCreatedDate; }

    public String getFormUpdatedDate() {  return formUpdatedDate; }
    public void setFormUpdatedDate(String formUpdatedDate) { this.formUpdatedDate = formUpdatedDate; }

    public String getFormCreatedBy() { return formCreatedBy; }
    public void setFormCreatedBy(String formCreatedBy) { this.formCreatedBy = formCreatedBy; }

    public String getFormUpdatedBy() { return formUpdatedBy; }
    public void setFormUpdatedBy(String formUpdatedBy) { this.formUpdatedBy = formUpdatedBy; }

    public String getFormWorkflowStatus() { return formWorkflowStatus; }
    public void setFormWorkflowStatus(String formWorkflowStatus) { this.formWorkflowStatus = formWorkflowStatus; }

    public String getFormSdvStatus() { return formSdvStatus; }
    public void setFormSdvStatus(String formSdvStatus) { this.formSdvStatus = formSdvStatus; }

    public String getFormRemoved() { return formRemoved; }
    public void setFormRemoved(String formRemoved) { this.formRemoved = formRemoved; }

    public String getFormArchived() { return formArchived; }
    public void setFormArchived(String formArchived) { this.formArchived = formArchived; }

    public String getFormRequired() { return formRequired; }
    public void setFormRequired(String formRequired) { this.formRequired = formRequired; }

    public String getFormRelevant() { return formRelevant; }
    public void setFormRelevant(String formRelevant) { this.formRelevant = formRelevant; }

    public String getFormEditable() { return formEditable; }
    public void setFormEditable(String formEditable) { this.formEditable = formEditable; }
}
