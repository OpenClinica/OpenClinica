package org.akaza.openclinica.domain.xform;

public class XformItem {
    String itemPath = null;
    String itemName = null;
    String itemSectionLabel = null;
    String itemDescription = null;
    String itemGroup = null;
    String itemResponseType = null;
    String itemDataType = null;

    public String getItemPath() {
        return itemPath;
    }

    public void setItemPath(String itemPath) {
        this.itemPath = itemPath;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemSectionLabel() {
        return itemSectionLabel;
    }

    public void setItemSectionLabel(String itemSectionLabel) {
        this.itemSectionLabel = itemSectionLabel;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(String itemGroup) {
        this.itemGroup = itemGroup;
    }

    public String getItemResponseType() {
        return itemResponseType;
    }

    public void setItemResponseType(String itemResponseType) {
        this.itemResponseType = itemResponseType;
    }

    public String getItemDataType() {
        return itemDataType;
    }

    public void setItemDataType(String itemDataType) {
        this.itemDataType = itemDataType;
    }

}
