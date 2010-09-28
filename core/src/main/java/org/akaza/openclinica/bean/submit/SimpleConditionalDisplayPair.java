/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import org.akaza.openclinica.bean.core.Status;

/**
 * This class collects information for one pair of a simple conditional display item and one control option value
 * 
 * @author ywang (Jun., 2010)
 *
 */
public class SimpleConditionalDisplayPair {
    private Integer SCDItemId;
    private String SCDItemName;
    private String optionValue;
    private String controlItemName;
    private Integer controlItemId;
    private String message;
    private Boolean isCurrentShown = false; //shown status that has been saved in database
    
    
    public Integer getSCDItemId() {
        return SCDItemId;
    }

    public void setSCDItemId(Integer SCDItemId) {
        this.SCDItemId = SCDItemId;
    }

    public String getSCDItemName() {
        return SCDItemName;
    }

    public void setSCDItemName(String SCDItemName) {
        this.SCDItemName = SCDItemName;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getControlItemName() {
        return controlItemName;
    }

    public void setControlItemName(String controlItemName) {
        this.controlItemName = controlItemName;
    }

    public Integer getControlItemId() {
        return controlItemId;
    }

    public void setControlItemId(Integer controlItemId) {
        this.controlItemId = controlItemId;
    }

    public Boolean isCurrentShown() {
        return isCurrentShown;
    }

    public void setIsCurrentShown(Boolean isCurrentShown) {
        this.isCurrentShown = isCurrentShown;
    }
    
    /**
     * Determined by item data status of a simple conditional display item
     * @param simpleConditionalDisplayItemData
     * @return
     */
    public static Boolean isCurrentShownItem(ItemDataBean simpleConditionalDisplayItemData) {
        Status itemDataStatus = simpleConditionalDisplayItemData.getStatus();
        return itemDataStatus!=null && itemDataStatus.getId()>0 && (itemDataStatus.getId()!=5||itemDataStatus.getId()!=7  ? true : false);
    }
}