/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import org.akaza.openclinica.bean.core.NullValue;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.service.crfdata.SCDData;
import org.akaza.openclinica.service.crfdata.front.InstantOnChangeFrontStrGroup;

import java.io.File;
import java.util.ArrayList;

/**
 * @author ssachs
 */
public class DisplayItemBean implements Comparable {
    private ItemDataBean data;
    private ItemBean item;
    private ItemFormMetadataBean metadata;
    private String editFlag = "";// used for items in a group
    private ItemDataBean dbData; // used for DDE, items in a group
    private boolean isNewItem;
    private String fieldName;

    //adding totals here for display purposes

    private int totNew;
	private int totUpdated;
    private int totRes;
    private int totClosed;


	private int totNA;


	private ArrayList<DiscrepancyNoteBean> discrepancyNotes;

    // not in the database



	/**
     * Not a database column. The elements are DisplayItemBeans whose
     * metadata.parentId property equals the item.id property of this bean.
     * Furthermore, they must be ordered in increasing value of their
     * metadata.columnNumber property.
     */
    private ArrayList children;

    /**
     * Not a database column. Always equal to children.size(). Is primarily used
     * when displaying the data entry form.
     */
    private int numChildren;

    /**
     * Not a database column. Always equal to the metadata.columnNumber property
     * of the last element of children. Set to 0 by default.
     */
    private int numColumns;

    /**
     * Not a database column. The number of discrepancy notes related to this
     * item in the discrepancy note tables.
     */
    private int numDiscrepancyNotes = 0;

    /**
     * The event definition CRF which defines the event CRF within which this
     * bean's item form matadata resides. Used to determine which, if any, null
     * values should be added to the item's response set.
     */
    private EventDefinitionCRFBean eventDefinitionCRF;

    /**
     * Will hold the discrepancy note status for the item.
     */
    private int discrepancyNoteStatus;

    /**
     * It is true if a scd item will display because of chosen options.
     */
    private boolean isSCDtoBeShown = false;
    private SCDData scdData;
    /**
     * True, when an item should hide but take a blank spot.<br>
     * False, when an item should show or should hide without taking a spot.
     * By default, it is false.
     */
    private boolean blankDwelt;

    private InstantOnChangeFrontStrGroup instantFrontStrGroup;

    private void setProperties() {
        data = new ItemDataBean();
        item = new ItemBean();
        metadata = new ItemFormMetadataBean();
        children = new ArrayList();
        numChildren = 0;
        numColumns = 0;
        dbData = new ItemDataBean();
        isSCDtoBeShown = false;
        scdData = new SCDData();
        blankDwelt = false;
        instantFrontStrGroup = new InstantOnChangeFrontStrGroup();
        isNewItem=true;
    }

    public DisplayItemBean() {
        this.eventDefinitionCRF = new EventDefinitionCRFBean();
        setProperties();
    }

    // public DisplayItemBean(EventDefinitionCRFBean eventDefinitionCRF) {
    // this.eventDefinitionCRF = eventDefinitionCRF;
    // setProperties();
    // }

    /**
     * @return Returns the data.
     */
    public ItemDataBean getData() {
        return data;
    }

    /**
     * @param data
     *            The data to set.
     */
    public void setData(ItemDataBean data) {
        this.data = data;
    }

    /**
     * @return Returns the item.
     */
    public ItemBean getItem() {
        return item;
    }

    /**
     * @param item
     *            The item to set.
     */
    public void setItem(ItemBean item) {
        this.item = item;
    }

    /**
     * @return Returns the metadata.
     */
    public ItemFormMetadataBean getMetadata() {
        return metadata;
    }

    /**
     * @param metadata
     *            The metadata to set.
     */
    public void setMetadata(ItemFormMetadataBean metadata) {
        this.metadata = metadata;

        ResponseSetBean rsb = getMetadata().getResponseSet();
        // BWP 09/18/07 >> rsb cannot be null here because I added this line to
        // ItemFormMetadataBean's
        // constructor: responseSet=new ResponseSetBean();
        // logger.info("rsb = " + rsb);

        org.akaza.openclinica.bean.core.ResponseType rt = rsb.getResponseType();

        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.RADIO)
            || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {

            if (eventDefinitionCRF != null) {
                ArrayList nullValues = eventDefinitionCRF.getNullValuesList();
                for (int i = 0; i < nullValues.size(); i++) {
                    NullValue nv = (NullValue) nullValues.get(i);
                    ResponseOptionBean ro = new ResponseOptionBean();

                    ro.setValue(nv.getName());
                    ro.setText(nv.getDescription());
                    rsb.addOption(ro);
                }
            }
        }
        getMetadata().setResponseSet(rsb);
    }

    /**
     * @return Returns the children.
     */
    public ArrayList getChildren() {
        return children;
    }

    /**
     * Assumes the children are ordered by getMetadata().getColumnNumber() in
     * ascending order.
     *
     * @param children
     *            The children to set.
     */
    public void setChildren(ArrayList children) {
        this.children = children;
        numChildren = children.size();

        if (numChildren > 0) {
            DisplayItemBean dib = (DisplayItemBean) children.get(numChildren - 1);
            numColumns = dib.getMetadata().getColumnNumber();
        } else {
            numColumns = 1;
        }
    }

    /**
     * @return Returns the numChildren.
     */
    public int getNumChildren() {
        return numChildren;
    }

    /**
     * @return Returns the numColumns.
     */
    public int getNumColumns() {
        return numColumns;
    }

    /**
     * Allows DisplayItemBean objects to be sorted by their metadata's ordinal
     * value.
     *
     * @param o
     *            The object this bean is being compared to.
     * @return A negative number if o is a DisplayItemBean with a greater
     *         ordinal than this DisplayItemBean's 0 if o is not a
     *         DisplayItemBean or is a DisplayItemBean with an ordinal equal to
     *         this DisplayItemBean's A positive number if o is a
     *         DisplayItemBean with a lesser ordinal than this DisplayItemBean's
     */
    public int compareTo(Object o) {
        if (!o.getClass().equals(this.getClass())) {
            return 0;
        }

        DisplayItemBean arg = (DisplayItemBean) o;
        return getMetadata().getOrdinal() - arg.getMetadata().getOrdinal();
    }

    /**
     * Loads a set of values from the form into the bean. This means that the
     * selected property of the ResponseOptionBean objects
     * metadata.responseSet.opresponseOption value is set properly, and
     *
     * @param values
     */
    public void loadFormValue(ArrayList values) {
        ResponseSetBean rsb = getMetadata().getResponseSet();

        String valueForDB = "";
        String glue = "";

       // OC-8975 remove current/old value in ResponseSetBean, then update with form value
        if(rsb.getResponseType().equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX) ||
        		rsb.getResponseType().equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {
        	rsb.removeSelection();	
        }
        
        for (int i = 0; i < values.size(); i++) {
            String value = (String) values.get(i);

            if (value == null || value.equals("")) {
                continue;
            }

            rsb.setSelected(value.trim(), true);

            valueForDB += glue + value;
            glue = ",";
        }

        getMetadata().setResponseSet(rsb);
        getData().setValue(valueForDB);
    }

    public void loadFormValue(String value) {
        ResponseSetBean rsb = getMetadata().getResponseSet();
        org.akaza.openclinica.bean.core.ResponseType rt = rsb.getResponseType();

        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXT) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXTAREA) //|| rt.equals(org.akaza.openclinica.bean.core.ResponseType.CODING)
            || rt.equals(org.akaza.openclinica.bean.core.ResponseType.CALCULATION) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.GROUP_CALCULATION)
            || rt.equals(org.akaza.openclinica.bean.core.ResponseType.FILE) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.INSTANT_CALCULATION)) {
            rsb.setValue(value);
        } else {
            if (value != null) {
                rsb.setSelected(value.trim(), true);
            }
        }
        // logger.info("loadFormValue, line 241, DisplayItemBean
        // "+rsb.getResponseType().getName());
        getMetadata().setResponseSet(rsb);
        getData().setValue(value);// comment set by tbh, 112007
    }

    public void loadDBValue() {
        ResponseSetBean rsb = getMetadata().getResponseSet();
        org.akaza.openclinica.bean.core.ResponseType rt = rsb.getResponseType();
        String dbValue = getData().getValue();
        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {
            String dbValues[] = dbValue.split(",");

            if (dbValues != null) {
                for (String element : dbValues) {
                    if (element != null) {
                        rsb.setSelected(element.trim(), true);
                    }
                }
            }
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXT) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXTAREA) //|| rt.equals(org.akaza.openclinica.bean.core.ResponseType.CODING)
            || rt.equals(org.akaza.openclinica.bean.core.ResponseType.CALCULATION) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.GROUP_CALCULATION)
            || rt.equals(org.akaza.openclinica.bean.core.ResponseType.INSTANT_CALCULATION)) {
            rsb.setValue(dbValue);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.FILE)) {
            // Here assume dbValue from database should be a valid file pathname
            if (dbValue.length() > 0) {
                getDbData().setValue(dbValue);
                File f = new File(dbValue);
                String filename = f.getName();
                if (f.isFile()) {
                    rsb.setValue(filename);
                } else {
                    // File does not exist,
                    rsb.setValue("fileNotFound#" + filename);
                }
            } else {
                rsb.setValue(dbValue);
            }
        } else {
            if (dbValue != null) {
                dbValue = dbValue.trim();
            }
            rsb.setSelected(dbValue, true);
        }

        getMetadata().setResponseSet(rsb);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (blankDwelt ? 1231 : 1237);
        result = prime * result + (children == null ? 0 : children.hashCode());
        result = prime * result + (data == null ? 0 : data.hashCode());
        result = prime * result + (dbData == null ? 0 : dbData.hashCode());
        result = prime * result + discrepancyNoteStatus;
        result = prime * result + (discrepancyNotes == null ? 0 : discrepancyNotes.hashCode());
        result = prime * result + (editFlag == null ? 0 : editFlag.hashCode());
        result = prime * result + (eventDefinitionCRF == null ? 0 : eventDefinitionCRF.hashCode());
        result = prime * result + (instantFrontStrGroup == null ? 0 : instantFrontStrGroup.hashCode());
        result = prime * result + (isSCDtoBeShown ? 1231 : 1237);
        result = prime * result + (item == null ? 0 : item.hashCode());
        result = prime * result + (metadata == null ? 0 : metadata.hashCode());
        result = prime * result + numChildren;
        result = prime * result + numColumns;
        result = prime * result + numDiscrepancyNotes;
        result = prime * result + (scdData == null ? 0 : scdData.hashCode());
        result = prime * result + totClosed;
        result = prime * result + totNA;
        result = prime * result + totNew;
        result = prime * result + totRes;
        result = prime * result + totUpdated;
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
        DisplayItemBean other = (DisplayItemBean) obj;
        if (blankDwelt != other.blankDwelt)
            return false;
        if (children == null) {
            if (other.children != null)
                return false;
        } else if (!children.equals(other.children))
            return false;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        if (dbData == null) {
            if (other.dbData != null)
                return false;
        } else if (!dbData.equals(other.dbData))
            return false;
        if (discrepancyNoteStatus != other.discrepancyNoteStatus)
            return false;
        if (discrepancyNotes == null) {
            if (other.discrepancyNotes != null)
                return false;
        } else if (!discrepancyNotes.equals(other.discrepancyNotes))
            return false;
        if (editFlag == null) {
            if (other.editFlag != null)
                return false;
        } else if (!editFlag.equals(other.editFlag))
            return false;
        if (eventDefinitionCRF == null) {
            if (other.eventDefinitionCRF != null)
                return false;
        } else if (!eventDefinitionCRF.equals(other.eventDefinitionCRF))
            return false;
        if (instantFrontStrGroup == null) {
            if (other.instantFrontStrGroup != null)
                return false;
        } else if (!instantFrontStrGroup.equals(other.instantFrontStrGroup))
            return false;
        if (isSCDtoBeShown != other.isSCDtoBeShown)
            return false;
        if (item == null) {
            if (other.item != null)
                return false;
        } else if (!item.equals(other.item))
            return false;
        if (metadata == null) {
            if (other.metadata != null)
                return false;
        } else if (!metadata.equals(other.metadata))
            return false;
        if (numChildren != other.numChildren)
            return false;
        if (numColumns != other.numColumns)
            return false;
        if (numDiscrepancyNotes != other.numDiscrepancyNotes)
            return false;
        if (scdData == null) {
            if (other.scdData != null)
                return false;
        } else if (!scdData.equals(other.scdData))
            return false;
        if (totClosed != other.totClosed)
            return false;
        if (totNA != other.totNA)
            return false;
        if (totNew != other.totNew)
            return false;
        if (totRes != other.totRes)
            return false;
        if (totUpdated != other.totUpdated)
            return false;
        return true;
    }

    /**
     * @return Returns the eventDefinitionCRF.
     */
    public EventDefinitionCRFBean getEventDefinitionCRF() {
        return eventDefinitionCRF;
    }

    /**
     * @param eventDefinitionCRF
     *            The eventDefinitionCRF to set.
     */
    public void setEventDefinitionCRF(EventDefinitionCRFBean eventDefinitionCRF) {
        this.eventDefinitionCRF = eventDefinitionCRF;
    }

    /**
     * @return Returns the numDiscrepancyNotes.
     */
    public int getNumDiscrepancyNotes() {
        return numDiscrepancyNotes;
    }

    /**
     * @param numDiscrepancyNotes
     *            The numDiscrepancyNotes to set.
     */
    public void setNumDiscrepancyNotes(int numDiscrepancyNotes) {
        this.numDiscrepancyNotes = numDiscrepancyNotes;
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
     * @return the dbData
     */
    public ItemDataBean getDbData() {
        return dbData;
    }

    /**
     * @param dbData
     *            the dbData to set
     */
    public void setDbData(ItemDataBean dbData) {
        this.dbData = dbData;
    }

    public int getDiscrepancyNoteStatus() {
        return discrepancyNoteStatus;
    }

    public void setDiscrepancyNoteStatus(int discrepancyNoteStatus) {
        this.discrepancyNoteStatus = discrepancyNoteStatus;
    }

    public boolean getIsSCDtoBeShown() {
        return isSCDtoBeShown;
    }

    public void setIsSCDtoBeShown(boolean isSCDtoBeShown) {
        this.isSCDtoBeShown = isSCDtoBeShown;
    }

    public int getTotNew() {
		return totNew;
	}

	public void setTotNew(int totNew) {
		this.totNew = totNew;
	}

	public int getTotUpdated() {
		return totUpdated;
	}

	public void setTotUpdated(int totUpdated) {
		this.totUpdated = totUpdated;
	}

	public int getTotRes() {
		return totRes;
	}

	public void setTotRes(int totRes) {
		this.totRes = totRes;
	}

	public int getTotClosed() {
		return totClosed;
	}

	public void setTotClosed(int totClosed) {
		this.totClosed = totClosed;
	}
    public int getTotNA() {
		return totNA;
	}

	public void setTotNA(int totNA) {
		this.totNA = totNA;
	}
    public ArrayList<DiscrepancyNoteBean> getDiscrepancyNotes() {
		return discrepancyNotes;
	}

	public void setDiscrepancyNotes(ArrayList<DiscrepancyNoteBean> discrepancyNotes) {
		this.discrepancyNotes = discrepancyNotes;
	}


    public boolean isBlankDwelt() {
        return blankDwelt;
    }

    public void setBlankDwelt(boolean blankDwelt) {
        this.blankDwelt = blankDwelt;
    }

    public SCDData getScdData() {
        return scdData;
    }

    public void setScdData(SCDData scdData) {
        this.scdData = scdData;
    }

    public InstantOnChangeFrontStrGroup getInstantFrontStrGroup() {
        return instantFrontStrGroup;
    }

    public void setInstantFrontStrGroup(InstantOnChangeFrontStrGroup instantFrontStrGroup) {
        this.instantFrontStrGroup = instantFrontStrGroup;
    }

	public boolean getIsNewItem() {
		return isNewItem;
	}

	public void setIsNewItem(boolean isNewItem) {
		this.isNewItem = isNewItem;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
}
