package com.openclinica.kafka.dto;

public class StudyPublishDTO {
    private String customerUuid;
    private String studyUuid;
    private String studyEnvironmentUuid;

    public String getCustomerUuid() { return customerUuid; }
    public void setCustomerUuid(String customerUuid) { this.customerUuid = customerUuid; }

    public String getStudyUuid() { return studyUuid; }
    public void setStudyUuid(String studyUuid) { this.studyUuid = studyUuid; }

    public String getStudyEnvironmentUuid() { return studyEnvironmentUuid; }
    public void setStudyEnvironmentUuid(String studyEnvironmentUuid) { this.studyEnvironmentUuid = studyEnvironmentUuid; }

    @Override
    public String toString() {
        return "StudyPublishDTO{" +
            "customerUuid='" + customerUuid + '\'' +
            ", studyUuid='" + studyUuid + '\'' +
            ", studyEnvironmentUuid='" + studyEnvironmentUuid + '\'' +
            '}';
    }
}
