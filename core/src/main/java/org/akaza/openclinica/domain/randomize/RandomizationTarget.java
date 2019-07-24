package org.akaza.openclinica.domain.randomize;

public class RandomizationTarget {

    private String eventOID;
    private String formOID;
    private String formLayoutID;
    private String itemGroupOID;
    private String itemOID;

    public static final String TARGET_EVENT = "target.event.OID";
    public static final String TARGET_FORM = "target.form.OID";
    public static final String TARGET_VERSION = "target.version.ID";
    public static final String TARGET_ITEM_GROUP = "target.itemGroup.OID";
    public static final String TARGET_ITEM = "target.item.OID";

    public String getEventOID() {
        return eventOID;
    }

    public void setEventOID(String eventOID) {
        this.eventOID = eventOID;
    }

    public String getFormOID() {
        return formOID;
    }

    public void setFormOID(String formOID) {
        this.formOID = formOID;
    }

    public String getFormLayoutID() {
        return formLayoutID;
    }

    public void setFormLayoutID(String formLayoutID) {
        this.formLayoutID = formLayoutID;
    }

    public String getItemGroupOID() {
        return itemGroupOID;
    }

    public void setItemGroupOID(String itemGroupOID) {
        this.itemGroupOID = itemGroupOID;
    }

    public String getItemOID() {
        return itemOID;
    }

    public void setItemOID(String itemOID) {
        this.itemOID = itemOID;
    }
}
