package org.akaza.openclinica.controller.helper;

/**
 * The UserType enumeration.
 */
public enum UserType {
    USER("User"),
    BUSINESS_ADMIN("Business Admin"),
    TECH_ADMIN("Tech Admin");

    String name;

    UserType(String name) {
        this.name = name;
    }

    /**
     * @return user-friendly name of the user type.
     */
    public String getName() {
        return name;
    }
}
