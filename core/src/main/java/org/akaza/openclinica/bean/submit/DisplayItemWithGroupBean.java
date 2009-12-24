/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import java.util.ArrayList;
import java.util.List;

public class DisplayItemWithGroupBean implements Comparable {
    private DisplayItemBean singleItem;

    private DisplayItemGroupBean itemGroup;

    // this is an array of same item groups, which reflects multiple item group
    // rows on the front end
    private List<DisplayItemGroupBean> itemGroups = new ArrayList<DisplayItemGroupBean>();
    // always keeps the group rows from DB, so we can keep track of which row is
    // removed
    private List<DisplayItemGroupBean> dbItemGroups = new ArrayList<DisplayItemGroupBean>();

    private int ordinal;

    private boolean inGroup;

    private String pageNumberLabel;

    public DisplayItemWithGroupBean() {
        this.singleItem = new DisplayItemBean();
        this.itemGroup = new DisplayItemGroupBean();
        this.pageNumberLabel = "";
    }

    /**
     * @return the dbItemGroups
     */
    public List<DisplayItemGroupBean> getDbItemGroups() {
        return dbItemGroups;
    }

    /**
     * @param dbItemGroups
     *            the dbItemGroups to set
     */
    public void setDbItemGroups(List<DisplayItemGroupBean> dbItemGroups) {
        this.dbItemGroups = dbItemGroups;
    }

    /**
     * @return the itemGroups
     */
    public List<DisplayItemGroupBean> getItemGroups() {
        return itemGroups;
    }

    /**
     * @param itemGroups
     *            the itemGroups to set
     */
    public void setItemGroups(List<DisplayItemGroupBean> itemGroups) {
        this.itemGroups = itemGroups;
    }

    /**
     * @return the pageNumberLabel
     */
    public String getPageNumberLabel() {
        return pageNumberLabel;
    }

    /**
     * @param pageNumberLabel
     *            the pageNumberLabel to set
     */
    public void setPageNumberLabel(String pageNumberLabel) {
        this.pageNumberLabel = pageNumberLabel;
    }

    /**
     * @return the inGroup
     */
    public boolean isInGroup() {
        return inGroup;
    }

    /**
     * @param inGroup
     *            the inGroup to set
     */
    public void setInGroup(boolean inGroup) {
        this.inGroup = inGroup;
    }

    /**
     * @return the itemGroup
     */
    public DisplayItemGroupBean getItemGroup() {
        return itemGroup;
    }

    /**
     * @param itemGroup
     *            the itemGroup to set
     */
    public void setItemGroup(DisplayItemGroupBean itemGroup) {
        this.itemGroup = itemGroup;
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
     * @return the singleItem
     */
    public DisplayItemBean getSingleItem() {
        return singleItem;
    }

    /**
     * @param singleItem
     *            the singleItem to set
     */
    public void setSingleItem(DisplayItemBean singleItem) {
        this.singleItem = singleItem;
    }

    public int compareTo(Object o) {
        if (!o.getClass().equals(this.getClass())) {
            return 0;
        }

        DisplayItemWithGroupBean arg = (DisplayItemWithGroupBean) o;
        return getOrdinal() - arg.getOrdinal();
    }

}
