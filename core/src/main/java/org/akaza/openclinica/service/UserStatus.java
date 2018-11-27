package org.akaza.openclinica.service;


/**
 * The UserStatus enumeration.
 */
public enum UserStatus {
    ACTIVE("Active"), INACTIVE("Inactive"), CREATED("Created"),INVITED("Invited");

       UserStatus(String value){
        this.value=value;
       }
        private String value;

    public String getValue() {
        return value;
    }


}