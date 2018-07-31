package org.akaza.openclinica.controller.helper;

/**
 * Created by yogi on 6/22/17.
 */


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.akaza.openclinica.service.AbstractAuditingDTO;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the StudyEnvironmentRole entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudyEnvironmentRoleDTO extends AbstractAuditingDTO implements Serializable {

    private String uuid;

    private String studyUuid;

    private String siteUuid;

    private String studyEnvironmentUuid;

    @NotNull private String roleUuid;

    private String roleName;

    @NotNull private UserServiceRoleType roleType;

    private String ownerFirstName;

    private String ownerLastName;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getStudyUuid() {
        return studyUuid;
    }

    public void setStudyUuid(String studyUuid) {
        this.studyUuid = studyUuid;
    }

    public String getStudyEnvironmentUuid() {
        return studyEnvironmentUuid;
    }

    public void setStudyEnvironmentUuid(String studyEnvironmentUuid) {
        this.studyEnvironmentUuid = studyEnvironmentUuid;
    }

    public String getSiteUuid() {
        return siteUuid;
    }

    public void setSiteUuid(String siteUuid) {
        this.siteUuid = siteUuid;
    }

    public String getRoleUuid() {
        return roleUuid;
    }

    public void setRoleUuid(String roleUuid) {
        this.roleUuid = roleUuid;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public UserServiceRoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(UserServiceRoleType roleType) {
        this.roleType = roleType;
    }

    public String getOwnerFirstName() {
        return ownerFirstName;
    }

    public void setOwnerFirstName(String ownerFirstName) {
        this.ownerFirstName = ownerFirstName;
    }

    public String getOwnerLastName() {
        return ownerLastName;
    }

    public void setOwnerLastName(String ownerLastName) {
        this.ownerLastName = ownerLastName;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StudyEnvironmentRoleDTO studyEnvironmentRoleDTO = (StudyEnvironmentRoleDTO) o;

        if (!Objects.equals(uuid, studyEnvironmentRoleDTO.uuid)) {
            return false;
        }

        return true;
    }

    @Override public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override public String toString() {
        return "StudyEnvironmentRoleDTO{" + "uuid=" + uuid + ", studyUuid='" + studyUuid + "'" + ", studyEnvironmentUuid='" + studyEnvironmentUuid + "'"
                + ", roleUuid='" + roleUuid + "'" + ", roleType='" + roleType + "'" + ", roleType='" + ownerFirstName + "'" + ", roleType='" + ownerLastName
                + "'" + '}';
    }
}