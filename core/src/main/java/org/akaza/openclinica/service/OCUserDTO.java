package org.akaza.openclinica.service;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the OCUser entity.
 */
public class OCUserDTO extends AbstractAuditingDTO implements Serializable {

    private String uuid;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String email;

    private String phoneNumber;

    private boolean inviteParticipant;

    @NotNull
    private String username;

    @NotNull
    private UserType userType;

    private String externalUserId;

    private String organization;

    private UserStatus status;

    private Instant lastSuccessfulLogin;

    private String identifier;

    private String participantId;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Instant getLastSuccessfulLogin() {
        return lastSuccessfulLogin;
    }

    public void setLastSuccessfulLogin(Instant lastSuccessfulLogin) {
        this.lastSuccessfulLogin = lastSuccessfulLogin;
    }


    public boolean isInviteParticipant() {
        return inviteParticipant;
    }

    public void setInviteParticipant(boolean inviteParticipant) {
        this.inviteParticipant = inviteParticipant;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OCUserDTO oCUserDTO = (OCUserDTO) o;

        if ( ! Objects.equals(uuid, oCUserDTO.uuid)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public String toString() {
        return "OCUserDTO{" +
                ", uuid='" + getUuid() + "'" +
                ", firstName='" + getFirstName() + "'" +
                ", lastName='" + getLastName() + "'" +
                ", username='" + getUsername() + "'" +
                ", email='" + getEmail() + "'" +
                ", phoneNumber='" + getPhoneNumber() + "'" +
                ", externalUserId='" + getExternalUserId() + "'" +
                ", organization='" + getOrganization() + "'" +
                ", status='" + getStatus() + "'" +
                ", lastSuccessfulLogin='" + getLastSuccessfulLogin() + "'" +
                "}";
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}