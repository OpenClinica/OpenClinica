package core.org.akaza.openclinica.bean.submit.crfdata;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class ImportItemGroupDataBean {
    private ArrayList<ImportItemDataBean> itemData;
    private String itemGroupOID;
    private String itemGroupRepeatKey;
    private String itemGroupName;
    private boolean itemGroupRemoved;

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

    public boolean isItemGroupRemoved() {
        return itemGroupRemoved;
    }

    public void setItemGroupRemoved(boolean itemGroupRemoved) {
        this.itemGroupRemoved = itemGroupRemoved;
    }

    public void setItemGroupRemovedAsString(String itemGroupRemoved) {
        if(StringUtils.equalsIgnoreCase(itemGroupRemoved, "yes"))
            this.itemGroupRemoved = true;
        else
            this.itemGroupRemoved = false;
    }

    public String getItemGroupRemovedAsString(){
        if(isItemGroupRemoved())
            return "yes";
        else
            return "no";
    }
}
