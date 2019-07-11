package org.akaza.openclinica.service;

import java.io.Serializable;

/**
 * A DTO for the OCUser entity.
 */
public class OCParticipantDTO extends AbstractAuditingDTO implements Serializable {

    private String firstName;
    private String lastName;

    private String email;

    private boolean inviteParticipant;

    private boolean inviteViaSms;

    private String phoneNumber;

    private String identifier;

    private boolean resetAccessCode;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isInviteParticipant() {
        return inviteParticipant;
    }

    public void setInviteParticipant(boolean inviteParticipant) {
        this.inviteParticipant = inviteParticipant;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isInviteViaSms() {
        return inviteViaSms;
    }

    public void setInviteViaSms(boolean inviteViaSms) {
        this.inviteViaSms = inviteViaSms;
    }

    public boolean isResetAccessCode() {
        return resetAccessCode;
    }

    public void setResetAccessCode(boolean resetAccessCode) {
        this.resetAccessCode = resetAccessCode;
    }
}