package org.akaza.openclinica.controller.dto;

import java.util.Date;

public class SdvItemDTO {
    private String name;
    private String briefDescription;
    private int ordinal;
    private boolean isRepeatingGroup;
    private String value;
    private Date lastModifiedDate;
    private boolean lastModifiedDateHasOnlyDate;
    private String lastModifiedUserName;
    private String lastModifiedUserFirstName;
    private String lastModifiedUserLastName;
    private String sdvStatus;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedUserName() {
        return lastModifiedUserName;
    }

    public void setLastModifiedUserName(String lastModifiedUserName) {
        this.lastModifiedUserName = lastModifiedUserName;
    }

    public String getLastModifiedUserFirstName() {
        return lastModifiedUserFirstName;
    }

    public void setLastModifiedUserFirstName(String lastModifiedUserFirstName) {
        this.lastModifiedUserFirstName = lastModifiedUserFirstName;
    }

    public String getLastModifiedUserLastName() {
        return lastModifiedUserLastName;
    }

    public void setLastModifiedUserLastName(String lastModifiedUserLastName) {
        this.lastModifiedUserLastName = lastModifiedUserLastName;
    }

    public String getSdvStatus() {
        return sdvStatus;
    }

    public void setSdvStatus(String sdvStatus) {
        this.sdvStatus = sdvStatus;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public boolean isRepeatingGroup() {
        return isRepeatingGroup;
    }

    public void setRepeatingGroup(boolean repeatingGroup) {
        isRepeatingGroup = repeatingGroup;
    }

    public boolean isLastModifiedDateHasOnlyDate() {
        return lastModifiedDateHasOnlyDate;
    }

    public void setLastModifiedDateHasOnlyDate(boolean lastModifiedDateHasOnlyDate) {
        this.lastModifiedDateHasOnlyDate = lastModifiedDateHasOnlyDate;
    }
}
