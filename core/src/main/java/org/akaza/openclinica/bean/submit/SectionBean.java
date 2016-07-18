/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import org.akaza.openclinica.bean.core.AuditableEntityBean;

import java.util.ArrayList;

/**
 * <P>
 * SectionBean.java, meant to collect items in a CRF on one page; organizational
 * collection of items per their metadata.
 *
 * @author thickerson
 *
 *
 */
public class SectionBean extends AuditableEntityBean {
    private int CRFVersionId;

    private String label;

    private String title;

    private String instructions;

    private String subtitle;

    private String pageNumberLabel;

    private int ordinal;

    private int parentId;

    private int borders;

    /**
     * How many items are in this section? Not in the database. Only used for
     * display.
     */
    private int numItems = 0;

    private ArrayList items;// no in DB

    private ArrayList<ItemGroupBean> groups; // YW, 08-21-2007, not in DB

    /**
     * How many items need validation? This is computed as the number of items
     * in the section whose status is pending. Not in the database. Only used
     * for display.
     */
    private int numItemsNeedingValidation = 0;

    /**
     * How many items are completed? This is computed as the number of items in
     * the section whose status is uncompleted. Not in the database. Only used
     * for display.
     */
    private int numItemsCompleted = 0;
    
    //if section contains simple conditional display item
    private boolean hasSCDItem;

    /**
     * The Section whose id == parentId. Not in the database. Only used for
     * display.
     */
    private SectionBean parent;

    public SectionBean() {
        CRFVersionId = 0;
        label = "";
        title = "";
        instructions = "";
        subtitle = "";
        pageNumberLabel = "";
        ordinal = 0;
        parentId = 0;
        hasSCDItem = false;

        // we do this so that we don't go into infinite recursion
        // however in getParent() we guarantee that the returned value
        // is never null
        parent = null;
        items = new ArrayList();
        groups = new ArrayList<ItemGroupBean>();
        borders=0;
    }

    public int getBorders() {
        return borders;
    }

    public void setBorders(int borders) {
        this.borders = borders;
    }

    /**
     * @return Returns the cRFVersionId.
     */
    public int getCRFVersionId() {
        return CRFVersionId;
    }

    /**
     * @param versionId
     *            The cRFVersionId to set.
     */
    public void setCRFVersionId(int versionId) {
        CRFVersionId = versionId;
    }

    /**
     * @return Returns the description.
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setInstructions(String description) {
        this.instructions = description;
    }

    /**
     * @return Returns the header.
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * @param header
     *            The header to set.
     */
    public void setSubtitle(String header) {
        this.subtitle = header;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return Returns the pageNumberLabel.
     */
    public String getPageNumberLabel() {
        return pageNumberLabel;
    }

    /**
     * @param pageNumberLabel
     *            The pageNumberLabel to set.
     */
    public void setPageNumberLabel(String pageNumberLabel) {
        this.pageNumberLabel = pageNumberLabel;
    }

    /**
     * @return Returns the parentId.
     */
    public int getParentId() {
        return parentId;
    }

    /**
     * @param parentId
     *            The parentId to set.
     */
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return Returns the ordinal.
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * @param ordinal
     *            The ordinal to set.
     */
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public void setName(String name) {
        setLabel(name);
    }

    /**
     * @return Returns the numItems.
     */
    public int getNumItems() {
        return numItems;
    }

    /**
     * @param numItems
     *            The numItems to set.
     */
    public void setNumItems(int numItems) {
        this.numItems = numItems;
    }

    /**
     * @return Returns the numItemsCompleted.
     */
    public int getNumItemsCompleted() {
        return numItemsCompleted;
    }

    /**
     * @param numItemsCompleted
     *            The numItemsCompleted to set.
     */
    public void setNumItemsCompleted(int numItemsCompleted) {
        this.numItemsCompleted = numItemsCompleted;
    }

    /**
     * @return Returns the numItemsNeedingValidation.
     */
    public int getNumItemsNeedingValidation() {
        return numItemsNeedingValidation;
    }

    /**
     * @param numItemsNeedingValidation
     *            The numItemsNeedingValidation to set.
     */
    public void setNumItemsNeedingValidation(int numItemsNeedingValidation) {
        this.numItemsNeedingValidation = numItemsNeedingValidation;
    }

    /**
     * @return Returns the parent.
     */
    public SectionBean getParent() {
        if (parent == null) {
            parent = new SectionBean();
        }
        return parent;
    }

    /**
     * @param parent
     *            The parent to set.
     */
    public void setParent(SectionBean parent) {
        this.parent = parent;
    }

    /**
     * @return Returns the items.
     */
    public ArrayList getItems() {
        return items;
    }

    /**
     * @param items
     *            The items to set.
     */
    public void setItems(ArrayList items) {
        this.items = items;
    }

    /**
     *
     * @return groups ArrayList<ItemGroupBean>
     */
    public ArrayList<ItemGroupBean> getGroups() {
        return groups;
    }

    /**
     *
     * @param groups
     */
    public void setGroups(ArrayList<ItemGroupBean> groups) {
        this.groups = groups;
    }

    public boolean hasSCDItem() {
        return hasSCDItem;
    }
    
    public void setHasSCDItem(boolean hasSCDItem) {
        this.hasSCDItem = hasSCDItem;
    }
    
}