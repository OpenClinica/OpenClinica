package org.akaza.openclinica.domain.xform;

import java.util.List;

public class XformItem {
    String itemPath = null;
    String itemName = null;
    String itemDescription = null;
    String itemGroup = null;
    String itemResponseType = null;
    String itemDataType = null;
    String readonly = null;
    String relevant = null;
    boolean calculate = false;
    int itemOrderInForm;
    boolean required = false;
    String leftItemText = null;
    private String optionsText;
    private String optionsValues;
    private String itemOid;
    private boolean published = false;
    private List<String> versions;
    private String mediaType = "";

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

    public String getReadonly() {
        return readonly;
    }

    public void setReadonly(String readonly) {
        this.readonly = readonly;
    }

    public boolean isCalculate() {
        return calculate;
    }

    public void setCalculate(boolean calculate) {
        this.calculate = calculate;
    }

    public int getItemOrderInForm() {
        return itemOrderInForm;
    }

    public void setItemOrderInForm(int itemOrderInForm) {
        this.itemOrderInForm = itemOrderInForm;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getLeftItemText() {
        return leftItemText;
    }

    public void setLeftItemText(String leftItemText) {
        this.leftItemText = leftItemText;
    }

    public String getOptionsText() {
        return optionsText;
    }

    public void setOptionsText(String optionsText) {
        this.optionsText = optionsText;
    }

    public String getOptionsValues() {
        return optionsValues;
    }

    public void setOptionsValues(String optionsValues) {
        this.optionsValues = optionsValues;
    }

    public String getItemOid() {
        return itemOid;
    }

    public void setItemOid(String itemOid) {
        this.itemOid = itemOid;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public String getRelevant() {
        return relevant;
    }

    public void setRelevant(String relevant) {
        this.relevant = relevant;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

}
