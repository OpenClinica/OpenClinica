package org.akaza.openclinica.service;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the Permission entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionDTO extends AbstractAuditingDTO implements Serializable {

    private String uuid;

    private String tagId;

    private String tagName;

    private String operation;

    private String studyUuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getStudyUuid() {
        return studyUuid;
    }

    public void setStudyUuid(String studyUuid) {
        this.studyUuid = studyUuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PermissionDTO permissionDTO = (PermissionDTO) o;
        if(permissionDTO.getUuid() == null || getUuid() == null) {
            return false;
        }
        return Objects.equals(getUuid(), permissionDTO.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }

    @Override
    public String toString() {
        return "PermissionDTO{" +
            ", uuid='" + getUuid() + "'" +
            ", tagId='" + getTagId() + "'" +
            ", operation='" + getOperation() + "'" +
            ", studyUuid='" + getStudyUuid() + "'" +
            "}";
    }
}
