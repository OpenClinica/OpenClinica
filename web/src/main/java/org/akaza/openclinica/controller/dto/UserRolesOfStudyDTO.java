package org.akaza.openclinica.controller.dto;

import java.util.ArrayList;
import java.util.HashMap;
import org.akaza.openclinica.bean.login.ErrorObject;

public class UserRolesOfStudyDTO {
    private String uniqueProtocolID;
    private ArrayList<HashMap> userRoles;
    private ArrayList<ErrorObject> errors;
    private String message;

    public UserRolesOfStudyDTO() {
        super();
    }

    public String getUniqueProtocolID() {
        return uniqueProtocolID;
    }

    public void setUniqueProtocolID(String uniqueProtocolID) {
        this.uniqueProtocolID = uniqueProtocolID;
    }

    public ArrayList<HashMap> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(ArrayList userRoles) {
        this.userRoles = userRoles;
    }

    public ArrayList<ErrorObject> getErrors() {
        return errors;
    }
    
    public void setErrors(ArrayList<ErrorObject> errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
