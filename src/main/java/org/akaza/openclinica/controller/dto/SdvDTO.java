package org.akaza.openclinica.controller.dto;

import java.util.Date;
import java.util.List;

public class SdvDTO {
    private int participantId;
    private String siteName;
    private String eventName;
    private Date eventStartDate;
    private String sdvRequirement;
    private String sdvStatus;
    private String formName;
    private String formStatus;
    private Date itemsLastVerifiedDate;
    private List<SdvItemDTO> sdvItems;

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Date getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(Date eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public String getSdvRequirement() {
        return sdvRequirement;
    }

    public void setSdvRequirement(String sdvRequirement) {
        this.sdvRequirement = sdvRequirement;
    }

    public String getSdvStatus() {
        return sdvStatus;
    }

    public void setSdvStatus(String sdvStatus) {
        this.sdvStatus = sdvStatus;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getFormStatus() {
        return formStatus;
    }

    public void setFormStatus(String formStatus) {
        this.formStatus = formStatus;
    }

    public Date getItemsLastVerifiedDate() {
        return itemsLastVerifiedDate;
    }

    public void setItemsLastVerifiedDate(Date itemsLastVerifiedDate) {
        this.itemsLastVerifiedDate = itemsLastVerifiedDate;
    }

    public List<SdvItemDTO> getSdvItems() {
        return sdvItems;
    }

    public void setSdvItems(List<SdvItemDTO> sdvItems) {
        this.sdvItems = sdvItems;
    }
}
