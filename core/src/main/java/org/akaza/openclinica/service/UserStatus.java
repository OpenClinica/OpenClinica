package org.akaza.openclinica.service;


/**
 * The UserStatus enumeration.
 */
public enum UserStatus {
    INVALID(0, ""),
    ACTIVE(1, "Active"), INACTIVE(2, "Inactive"), CREATED(3, "Created"), INVITED(4, "Invited");

    UserStatus(int code, String value) {
        this.code = code;
        this.value = value;
    }

    private String value;
    private int code;

    public String getValue() {
        return value;
    }

    public int getCode() {
        return code;
    }

}