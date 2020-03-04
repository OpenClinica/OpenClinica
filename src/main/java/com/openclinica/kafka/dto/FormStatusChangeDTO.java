package com.openclinica.kafka.dto;

public class FormStatusChangeDTO {
  private String realm;
  private String studyOid;
  private String siteOid;
  private String participantId;
  private String participantOid;
  private String formOid;
  private String eventOid;

  public String getRealm() { return realm; }

  public void setRealm(String realm) { this.realm = realm; }

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
}
