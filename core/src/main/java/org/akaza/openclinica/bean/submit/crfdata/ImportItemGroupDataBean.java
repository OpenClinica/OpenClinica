package org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;

public class ImportItemGroupDataBean {
    private ArrayList<ImportItemDataBean> itemData;
    private String itemGroupOID;
    private String itemGroupRepeatKey;
    private int itemRGkey;
    
    public ImportItemGroupDataBean() {
        itemData = new ArrayList<ImportItemDataBean>();
    }

    public String getItemGroupRepeatKey() {
        return itemGroupRepeatKey;
    }


	public int getItemRGkey() {
		return itemRGkey;
	}

	public void setItemRGkey(int itemRGkey) {
		this.itemRGkey = itemRGkey;
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
}
