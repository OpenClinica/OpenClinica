package org.akaza.openclinica.controller.helper;

public enum PIIEnum {
    EmailAddress("EmailAddress"),
    FirstName("FirstName"),
    Identifier("Identifier"),
    LastName("LastName"),
    MobileNumber("MobileNumber"),
    ParticipantID("ParticipantID");

    String name;
    PIIEnum(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }
}
