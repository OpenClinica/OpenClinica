package com.openclinica.kafka.dto;

public class ODMRefreshDTO {
    private String customerUuid;
    private String studyUuid;
    private String studyEnvironmentUuid;
    private String studyOid;
    private String siteOid;
    private String participantId;
    private String participantOid;

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
}
