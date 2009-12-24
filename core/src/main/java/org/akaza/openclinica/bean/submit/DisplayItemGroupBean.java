package org.akaza.openclinica.bean.submit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: May 7, 2007
 */
public class DisplayItemGroupBean implements Comparable {
    private ItemGroupBean itemGroupBean;
    private ItemGroupMetadataBean groupMetaBean;
    private List<DisplayItemBean> items;
    private int ordinal;
    private String editFlag;// add, edit or remove
    private int formInputOrdinal;
    private boolean isAuto;// if it is auto generated from rep model

    public DisplayItemGroupBean() {
        this.itemGroupBean = new ItemGroupBean();
        this.groupMetaBean = new ItemGroupMetadataBean();
        this.items = new ArrayList<DisplayItemBean>();
        ordinal = 0;
        editFlag = "";
        formInputOrdinal = 0;
        isAuto = true;
    }

    public ItemGroupBean getItemGroupBean() {
        return itemGroupBean;
    }

    /**
     * @return the groupMetaBean
     */
    public ItemGroupMetadataBean getGroupMetaBean() {
        return groupMetaBean;
    }

    /**
     * @param groupMetaBean
     *            the groupMetaBean to set
     */
    public void setGroupMetaBean(ItemGroupMetadataBean groupMetaBean) {
        this.groupMetaBean = groupMetaBean;
    }

    public void setItemGroupBean(ItemGroupBean formGroupBean) {
        this.itemGroupBean = formGroupBean;
    }

    public List<DisplayItemBean> getItems() {
        return items;
    }

    public void setItems(List<DisplayItemBean> items) {
        this.items = items;
    }

    /**
     * @return the ordinal
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * @param ordinal
     *            the ordinal to set
     */
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    /**
     * @return the editFlag
     */
    public String getEditFlag() {
        return editFlag;
    }

    /**
     * @param editFlag
     *            the editFlag to set
     */
    public void setEditFlag(String editFlag) {
        this.editFlag = editFlag;
    }

    /**
     * @return the formInputOrdinal
     */
    public int getFormInputOrdinal() {
        return formInputOrdinal;
    }

    /**
     * @param formInputOrdinal
     *            the formInputOrdinal to set
     */
    public void setFormInputOrdinal(int formInputOrdinal) {
        this.formInputOrdinal = formInputOrdinal;
    }

    /**
     * @return the isAuto
     */
    public boolean isAuto() {
        return isAuto;
    }

    /**
     * @param isAuto
     *            the isAuto to set
     */
    public void setAuto(boolean isAuto) {
        this.isAuto = isAuto;
    }

    public int compareTo(Object o) {
        if (!o.getClass().equals(this.getClass())) {
            return 0;
        }

        DisplayItemGroupBean arg = (DisplayItemGroupBean) o;
        return getOrdinal() - arg.getOrdinal();
    }

}
