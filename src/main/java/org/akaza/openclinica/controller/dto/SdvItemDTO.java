package org.akaza.openclinica.controller.dto;

import java.util.Date;

public class SdvItemDTO {
    private int itemDataId;
    private int itemId;
    private String name;
    private String label;
    private String description;
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
    private int openQueriesCount;
    private boolean isCalculateItem;

    public boolean isCalculateItem() {
        return isCalculateItem;
    }

    public void setCalculateItem(boolean calculateItem) {
        isCalculateItem = calculateItem;
    }

    public int getItemDataId() {
        return itemDataId;
    }

    public void setItemDataId(int itemDataId) {
        this.itemDataId = itemDataId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public int getOpenQueriesCount() {
        return openQueriesCount;
    }

    public void setOpenQueriesCount(int openQueriesCount) {
        this.openQueriesCount = openQueriesCount;
    }
}
