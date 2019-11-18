package org.akaza.openclinica.controller.dto;

import java.util.ArrayList;

/**
 * Created by krikorkrumlian on 9/18/17.
 */
public class StudyEnvStatusDTO {

    private String studyEnvUuid;
    private String status;
    private ArrayList<SiteStatusDTO> siteStatuses = new ArrayList<>();

    public String getStudyEnvUuid() {
        return studyEnvUuid;
    }

    public void setStudyEnvUuid(String studyEnvUuid) {
        this.studyEnvUuid = studyEnvUuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        status = status != null && status.equals("pending") ? "design" : status;
        status = status.toUpperCase();
        this.status = status;
    }

    public ArrayList<SiteStatusDTO> getSiteStatuses() {
        return siteStatuses;
    }

    public void setSiteStatuses(ArrayList<SiteStatusDTO> siteStatuses) {
        this.siteStatuses = siteStatuses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StudyEnvStatusDTO that = (StudyEnvStatusDTO) o;

        if (studyEnvUuid != null ? !studyEnvUuid.equals(that.studyEnvUuid) : that.studyEnvUuid != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        return siteStatuses != null ? siteStatuses.equals(that.siteStatuses) : that.siteStatuses == null;
    }

    @Override
    public int hashCode() {
        int result = studyEnvUuid != null ? studyEnvUuid.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (siteStatuses != null ? siteStatuses.hashCode() : 0);
        return result;
    }
}
