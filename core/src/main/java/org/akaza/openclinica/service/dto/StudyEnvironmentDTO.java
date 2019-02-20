package org.akaza.openclinica.service.dto;


import org.akaza.openclinica.domain.enumsupport.StudyEnvironmentStatus;
import org.akaza.openclinica.service.AbstractAuditingDTO;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the StudyEnvironment entity.
 */
public class StudyEnvironmentDTO extends AbstractAuditingDTO implements Serializable {

    @NotNull
    private String uuid;

    private String oid;

    private String studyUuid;

    private String studyName;

    @NotNull
    private StudyEnvironmentStatus status;

    private String environmentName;

    private boolean isPublished;

    private String latestVersionName;

    private ZonedDateTime latestVersionPublishedDate;

    private String latestVersionPublishedBy;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public String getStudyUuid() {
        return studyUuid;
    }

    public void setStudyUuid(String studyUuid) {
        this.studyUuid = studyUuid;
    }

    public StudyEnvironmentStatus getStatus() {
        return status;
    }

    public void setStatus(StudyEnvironmentStatus status) {
        this.status = status;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public String getLatestVersionName() {
        return latestVersionName;
    }

    public void setLatestVersionName(String latestVersionName) {
        this.latestVersionName = latestVersionName;
    }

    public ZonedDateTime getLatestVersionPublishedDate() {
        return latestVersionPublishedDate;
    }

    public void setLatestVersionPublishedDate(ZonedDateTime latestVersionPublishedDate) {
        this.latestVersionPublishedDate = latestVersionPublishedDate;
    }

    public String getLatestVersionPublishedBy() {
        return latestVersionPublishedBy;
    }

    public void setLatestVersionPublishedBy(String latestVersionPublishedBy) {
        this.latestVersionPublishedBy = latestVersionPublishedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StudyEnvironmentDTO studyEnvironmentDTO = (StudyEnvironmentDTO) o;

        if ( ! Objects.equals(uuid, studyEnvironmentDTO.uuid)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public String toString() {
        return "StudyEnvironmentDTO{" +
            ", uuid='" + uuid + "'" +
            ", oid='" + oid + "'" +
            ", status='" + status + "'" +
            ", isPublished='" + isPublished + "'" +
            '}';
    }
}
