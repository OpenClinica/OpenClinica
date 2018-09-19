package org.akaza.openclinica.service;

/**
 * Created by yogi on 6/22/17.
 */


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.akaza.openclinica.service.AbstractAuditingDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.akaza.openclinica.service.UserServiceRoleType;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the StudyEnvironmentRole entity.
 */
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A DTO for the StudyEnvironmentRole entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudyEnvironmentRoleDTO extends AbstractAuditingDTO implements Serializable {

    private String uuid;

    private String siteUuid;

    private String studyEnvironmentUuid;

    @NotNull
    private String roleUuid;

    // TODO: @NotNull
    private String dynamicRoleUuid;
    private String dynamicRoleName;

    private String baseRoleName;

    private String baseRoleUuid;

    private String roleName;

    private RoleType roleType;

    private List<PermissionDTO> permissions;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getDynamicRoleUuid() {
        return dynamicRoleUuid;
    }

    public void setDynamicRoleUuid(String dynamicRoleUuid) {
        this.dynamicRoleUuid = dynamicRoleUuid;
    }

    public String getBaseRoleName() {
        return baseRoleName;
    }

    public void setBaseRoleName(String baseRoleName) {
        this.baseRoleName = baseRoleName;
    }

    public String getBaseRoleUuid() {
        return baseRoleUuid;
    }

    public void setBaseRoleUuid(String baseRoleUuid) {
        this.baseRoleUuid = baseRoleUuid;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public List<PermissionDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionDTO> permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StudyEnvironmentRoleDTO studyEnvironmentRoleDTO = (StudyEnvironmentRoleDTO) o;

        if ( ! Objects.equals(uuid, studyEnvironmentRoleDTO.uuid)) { return false; }

        return true;
    }
    public String getDynamicRoleName() {
        return dynamicRoleName;
    }

    public void setDynamicRoleName(String dynamicRoleName) {
        this.dynamicRoleName = dynamicRoleName;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public String toString() {
        return "StudyEnvironmentRoleDTO{" +
                "uuid=" + uuid +
                ", studyEnvironmentUuid='" + studyEnvironmentUuid + "'" +
                ", roleUuid='" + roleUuid + "'" +
                ", dynamicRoleUuid='" + dynamicRoleUuid + "'" +
                ", roleType='" + roleType + "'" +
                ", dynamicRoleName='" + dynamicRoleName + "'" +
                '}';
    }
}