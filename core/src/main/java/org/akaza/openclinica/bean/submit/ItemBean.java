/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.oid.ItemOidGenerator;
import org.akaza.openclinica.bean.oid.OidGenerator;

import java.util.ArrayList;

/**
 * <P>
 * ItemBean.java.
 *
 * @author thickerson
 */
public class ItemBean extends AuditableEntityBean implements Comparable {
    private String description = "";

    private String units = "";

    private boolean phiStatus = false;

    private int itemDataTypeId = 0;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((crfName == null) ? 0 : crfName.hashCode());
        result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
        result = prime * result + ((datasetItemMapKey == null) ? 0 : datasetItemMapKey.hashCode());
        result = prime * result + defId;
        result = prime * result + ((defName == null) ? 0 : defName.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + itemDataTypeId;
        result = prime * result + ((itemMeta == null) ? 0 : itemMeta.hashCode());
        result = prime * result + ((itemMetas == null) ? 0 : itemMetas.hashCode());
        result = prime * result + ((itemDataElements == null) ? 0 : itemDataElements.hashCode());
        result = prime * result + itemReferenceTypeId;
        result = prime * result + ((oid == null) ? 0 : oid.hashCode());
        result = prime * result + ((oidGenerator == null) ? 0 : oidGenerator.hashCode());
        result = prime * result + (phiStatus ? 1231 : 1237);
        result = prime * result + (selected ? 1231 : 1237);
        result = prime * result + statusId;
        result = prime * result + ((units == null) ? 0 : units.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ItemBean other = (ItemBean) obj;
        if (crfName == null) {
            if (other.crfName != null)
                return false;
        } else if (!crfName.equals(other.crfName))
            return false;
        if (dataType == null) {
            if (other.dataType != null)
                return false;
        } else if (!dataType.equals(other.dataType))
            return false;
        if (datasetItemMapKey == null) {
            if (other.datasetItemMapKey != null)
                return false;
        } else if (!datasetItemMapKey.equals(other.datasetItemMapKey))
            return false;
        if (defId != other.defId)
            return false;
        if (defName == null) {
            if (other.defName != null)
                return false;
        } else if (!defName.equals(other.defName))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (itemDataTypeId != other.itemDataTypeId)
            return false;
        if (itemMeta == null) {
            if (other.itemMeta != null)
                return false;
        } else if (!itemMeta.equals(other.itemMeta))
            return false;
        if (itemMetas == null) {
            if (other.itemMetas != null)
                return false;
        } else if (!itemMetas.equals(other.itemMetas))
            return false;
        
        
        if (itemReferenceTypeId != other.itemReferenceTypeId)
            return false;
        if (oid == null) {
            if (other.oid != null)
                return false;
        } else if (!oid.equals(other.oid))
            return false;
        if (oidGenerator == null) {
            if (other.oidGenerator != null)
                return false;
        } else if (!oidGenerator.equals(other.oidGenerator))
            return false;
        if (phiStatus != other.phiStatus)
            return false;
        if (selected != other.selected)
            return false;
        if (statusId != other.statusId)
            return false;
        if (units == null) {
            if (other.units != null)
                return false;
        } else if (!units.equals(other.units))
            return false;
        return true;
    }

    private ItemDataType dataType;

    private int itemReferenceTypeId = 0;

    private int statusId = 1;

    private ItemFormMetadataBean itemMeta;// not in DB, for display

    private ArrayList itemMetas;// not in DB, one item can have multiple meta
    private ArrayList<ItemDataBean>  itemDataElements;

    private boolean selected = false; // not in DB, used for creating dataset

    private String defName = ""; // not in DB
    private int defId; //not in DB
    private String crfName = ""; // not in DB

    private String oid;
    private OidGenerator oidGenerator;

    private String datasetItemMapKey = ""; // which is

    // study_event_defintion_id+"_"+item_id;
    // not in DB - YW 3-7-2008

    public ItemBean() {
        dataType = ItemDataType.ST;
        itemMetas = new ArrayList();
        
        this.oidGenerator = new ItemOidGenerator();
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the itemDataTypeId.
     */
    public int getItemDataTypeId() {
        return dataType.getId();
    }

    /**
     * @param itemDataTypeId
     *            The itemDataTypeId to set.
     */
    public void setItemDataTypeId(int itemDataTypeId) {
        dataType = ItemDataType.get(itemDataTypeId);
        // this.itemDataTypeId = itemDataTypeId;
    }

    /**
     * @return Returns the itemReferenceTypeId.
     */
    public int getItemReferenceTypeId() {
        return itemReferenceTypeId;
    }

    /**
     * @param itemReferenceTypeId
     *            The itemReferenceTypeId to set.
     */
    public void setItemReferenceTypeId(int itemReferenceTypeId) {
        this.itemReferenceTypeId = itemReferenceTypeId;
    }

    /**
     * @return Returns the phiStatus.
     */
    public boolean isPhiStatus() {
        return phiStatus;
    }

    /**
     * @param phiStatus
     *            The phiStatus to set.
     */
    public void setPhiStatus(boolean phiStatus) {
        this.phiStatus = phiStatus;
    }

    /**
     * @return Returns the statusId.
     */
    public int getStatusId() {
        return statusId;
    }

    /**
     * @param statusId
     *            The statusId to set.
     */
    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    /**
     * @return Returns the units.
     */
    public String getUnits() {
        return units;
    }

    /**
     * @param units
     *            The units to set.
     */
    public void setUnits(String units) {
        this.units = units;
    }

    /**
     * @return Returns the dataType.
     */
    public ItemDataType getDataType() {
        return dataType;
    }

    /**
     * @param dataType
     *            The dataType to set.
     */
    public void setDataType(ItemDataType dataType) {
        this.dataType = dataType;
    }

    /**
     * @return Returns the itemMeta.
     */
    public ItemFormMetadataBean getItemMeta() {
        return itemMeta;
    }

    /**
     * @param itemMeta
     *            The itemMeta to set.
     */
    public void setItemMeta(ItemFormMetadataBean itemMeta) {
        this.itemMeta = itemMeta;
    }

    /**
     * @return Returns the itemMetas.
     */
    public ArrayList getItemMetas() {
        return itemMetas;
    }

    /**
     * @param itemMetas
     *            The itemMetas to set.
     */
    public void setItemMetas(ArrayList itemMetas) {
        this.itemMetas = itemMetas;
    }
    /**
     * @return Returns the itemMetas.
     */
    public ArrayList<ItemDataBean> getItemDataElements() {
        return itemDataElements;
    }
    public void addItemDataElement(ItemDataBean el) {
        if ( itemDataElements == null){
        	itemDataElements = new ArrayList<ItemDataBean>();
        }
        itemDataElements.add(el);
    }

    /**
     * @param itemMetas
     *            The itemMetas to set.
     */
    public void setItemDataElements(ArrayList<ItemDataBean> itemDataElements) {
        this.itemDataElements = itemDataElements;
    }

    
    /**
     * @return Returns the selected.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * @param selected
     *            The selected to set.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int compareTo(Object o) {
        if (!o.getClass().equals(this.getClass())) {
            return 0;
        }

        ItemBean arg = (ItemBean) o;
        if (!getItemMetas().isEmpty() && !arg.getItemMetas().isEmpty()) {
            ItemFormMetadataBean m1 = (ItemFormMetadataBean) getItemMetas().get(0);
            ItemFormMetadataBean m2 = (ItemFormMetadataBean) arg.getItemMetas().get(0);
            return m1.getOrdinal() - m2.getOrdinal();
        }
        //fix here 
        else if (!itemDataElements.isEmpty() && !arg.getItemDataElements().isEmpty()) {
            ItemDataBean m1 = (ItemDataBean) getItemDataElements().get(0);
            ItemDataBean m2 = (ItemDataBean) arg.getItemDataElements().get(0);
            return m1.getOrdinal() - m2.getOrdinal();
        }
        
        else {
            return getName().compareTo(arg.getName());
        }
    }

    /**
     * @return Returns the defName.
     */
    public String getDefName() {
        return defName;
    }

    /**
     * @param defName
     *            The defName to set.
     */
    public void setDefName(String defName) {
        this.defName = defName;
    }

    /**
     * @return Returns the crfName.
     */
    public String getCrfName() {
        return crfName;
    }

    /**
     * @param crfName
     *            The crfName to set.
     */
    public void setCrfName(String crfName) {
        this.crfName = crfName;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public OidGenerator getOidGenerator() {
        return oidGenerator;
    }

    public void setOidGenerator(OidGenerator oidGenerator) {
        this.oidGenerator = oidGenerator;
    }

    public String getDatasetItemMapKey() {
        return datasetItemMapKey;
    }

    public void setDatasetItemMapKey(String key) {
        this.datasetItemMapKey = key;
    }

    public int getDefId() {
        return defId;
    }

    public void setDefId(int defId) {
        this.defId = defId;
    }
}
