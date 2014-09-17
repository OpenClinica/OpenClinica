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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dbItemGroups == null) ? 0 : dbItemGroups.hashCode());
        result = prime * result + (inGroup ? 1231 : 1237);
        result = prime * result + ((itemGroup == null) ? 0 : itemGroup.hashCode());
        result = prime * result + ((itemGroups == null) ? 0 : itemGroups.hashCode());
        result = prime * result + ordinal;
        result = prime * result + ((pageNumberLabel == null) ? 0 : pageNumberLabel.hashCode());
        result = prime * result + ((singleItem == null) ? 0 : singleItem.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DisplayItemWithGroupBean other = (DisplayItemWithGroupBean) obj;
        if (dbItemGroups == null) {
            if (other.dbItemGroups != null)
                return false;
        } else if (!dbItemGroups.equals(other.dbItemGroups))
            return false;
        if (inGroup != other.inGroup)
            return false;
        if (itemGroup == null) {
            if (other.itemGroup != null)
                return false;
        } else if (!itemGroup.equals(other.itemGroup))
            return false;
        if (itemGroups == null) {
            if (other.itemGroups != null)
                return false;
        } else if (!itemGroups.equals(other.itemGroups))
            return false;
        if (ordinal != other.ordinal)
            return false;
        if (pageNumberLabel == null) {
            if (other.pageNumberLabel != null)
                return false;
        } else if (!pageNumberLabel.equals(other.pageNumberLabel))
            return false;
        if (singleItem == null) {
            if (other.singleItem != null)
                return false;
        } else if (!singleItem.equals(other.singleItem))
            return false;
        return true;
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
