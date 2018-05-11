package org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;

public class ImportItemGroupDataBean {
    private ArrayList<ImportItemDataBean> itemData;
    private String itemGroupOID;
    private String itemGroupRepeatKey;
    private String itemGroupName;

    public ImportItemGroupDataBean() {
        itemData = new ArrayList<ImportItemDataBean>();
    }

    public String getItemGroupRepeatKey() {
        return itemGroupRepeatKey;
    }

    public void setItemGroupRepeatKey(String itemGroupRepeatKey) {
        this.itemGroupRepeatKey = itemGroupRepeatKey;
    }

    public String getItemGroupOID() {
        return itemGroupOID;
    }

    public void setItemGroupOID(String itemGroupOID) {
        this.itemGroupOID = itemGroupOID;
    }

    public ArrayList<ImportItemDataBean> getItemData() {
        return itemData;
    }

    public void setItemData(ArrayList<ImportItemDataBean> itemData) {
        this.itemData = itemData;
    }

    public String getItemGroupName() {
        return itemGroupName;
    }

    public void setItemGroupName(String itemGroupName) {
        this.itemGroupName = itemGroupName;
    }

}
