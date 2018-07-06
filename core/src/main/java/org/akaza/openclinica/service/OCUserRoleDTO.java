package org.akaza.openclinica.service;

import java.util.LinkedList;
import java.util.List;

/**
 * DTO that wraps user info and Study/Site level roles
 * @author svadla@openclinica.com
 */
public class OCUserRoleDTO {
    private OCUserDTO userInfo;
    private List<StudyEnvironmentRoleDTO> roles = new LinkedList<>();

    public OCUserDTO getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(OCUserDTO userInfo) {
        this.userInfo = userInfo;
    }

    public List<StudyEnvironmentRoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<StudyEnvironmentRoleDTO> roles) {
        this.roles = roles;
    }
}
