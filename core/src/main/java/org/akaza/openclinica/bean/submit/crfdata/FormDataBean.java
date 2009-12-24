package org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;

public class FormDataBean {
    private ArrayList<ImportItemGroupDataBean> itemGroupData;

    private String formOID;
    
    public FormDataBean() {
        itemGroupData = new ArrayList<ImportItemGroupDataBean>();
    }

    public String getFormOID() {
        return formOID;
    }

    public void setFormOID(String formOID) {
        this.formOID = formOID;
    }

    public ArrayList<ImportItemGroupDataBean> getItemGroupData() {
        return itemGroupData;
    }

    public void setItemGroupData(ArrayList<ImportItemGroupDataBean> itemGroupData) {
        this.itemGroupData = itemGroupData;
    }
}
