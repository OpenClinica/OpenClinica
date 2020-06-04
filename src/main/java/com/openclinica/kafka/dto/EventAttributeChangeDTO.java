package com.openclinica.kafka.dto;

public class EventAttributeChangeDTO {
    private String customerUuid;
    private String studyUuid;
    private String studyEnvironmentUuid;
    private String studyOid;
    private String siteOid;
    private String participantId;
    private String participantOid;
    private String eventOid;
    private int eventRepeatKey;
    private String eventStartDate;
    private String eventWorkflowStatus;
    private String eventRemoved;
    private String eventArchived;
    private String eventLocked;
    private String eventSigned;

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

    public String getEventOid() {
        return eventOid;
    }
    public void setEventOid(String eventOid) {
        this.eventOid = eventOid;
    }

    public int getEventRepeatKey() { return eventRepeatKey; }
    public void setEventRepeatKey(int eventRepeatKey) { this.eventRepeatKey = eventRepeatKey; }

    public String getEventStartDate() { return eventStartDate; }
    public void setEventStartDate(String eventStartDate) { this.eventStartDate = eventStartDate; }

    public String getEventWorkflowStatus() { return eventWorkflowStatus; }
    public void setEventWorkflowStatus(String eventWorkflowStatus) { this.eventWorkflowStatus = eventWorkflowStatus; }

    public String getEventRemoved() { return eventRemoved; }
    public void setEventRemoved(String eventRemoved) { this.eventRemoved = eventRemoved; }

    public String getEventArchived() { return eventArchived; }
    public void setEventArchived(String eventArchived) { this.eventArchived = eventArchived; }

    public String getEventLocked() { return eventLocked; }
    public void setEventLocked(String eventLocked) { this.eventLocked = eventLocked; }

    public String getEventSigned() { return eventSigned; }
    public void setEventSigned(String eventSigned) { this.eventSigned = eventSigned; }

    @Override
    public String toString() {
        return "FormStatusChangeDTO{" +
            "customerUuid='" + customerUuid + '\'' +
            ", studyUuid='" + studyUuid + '\'' +
            ", studyEnvironmentUuid='" + studyEnvironmentUuid + '\'' +
            ", studyOid='" + studyOid + '\'' +
            ", siteOid='" + siteOid + '\'' +
            ", participantId='" + participantId + '\'' +
            ", participantOid='" + participantOid + '\'' +
            ", eventOid='" + eventOid + '\'' +
            '}';
    }
}
