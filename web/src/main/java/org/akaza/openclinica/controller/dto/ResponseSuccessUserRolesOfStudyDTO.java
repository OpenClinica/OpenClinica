package org.akaza.openclinica.controller.dto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import org.akaza.openclinica.bean.login.ResponseSuccessStudyDTO;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;

public class ResponseSuccessUserRolesOfStudyDTO extends ResponseSuccessStudyDTO {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private final ArrayList<HashMap> userRoles = new ArrayList<>();
    private SimpleDateFormat dateFormatter = null;

    public ArrayList<HashMap> getUserRoles() {
        return userRoles;
    }

    public void addUserRole(StudyUserRoleBean userRole) {
        HashMap userRoleJson = new HashMap();
        userRoleJson.put("username", userRole.getUserName());
        userRoleJson.put("role", userRole.getSerializedRoleName());
        userRoleJson.put("createdDate", getDateFormatter().format(userRole.getCreatedDate()));
        userRoleJson.put("updatedDate", getDateFormatter().format(userRole.getUpdatedDate()));
        userRoleJson.put("statusId", userRole.getStatus().getId());
        userRoleJson.put("updaterId", userRole.getUpdater().getId());
        userRoleJson.put("ownerId", userRole.getOwner().getId());
        userRoles.add(userRoleJson);
    }

    public SimpleDateFormat getDateFormatter() {
        if (dateFormatter == null) {
            this.dateFormatter = new SimpleDateFormat(DATE_FORMAT);
        }
        return dateFormatter;
    }

    public void setDateFormatter(SimpleDateFormat formatter) {
        this.dateFormatter = formatter;
    }
}
