/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.submit;


/**
 * This class collects a set of information for a simple conditional display
 */
public class SimpleConditionalDisplayPair {
    private Integer SCDItemId = 0;
    private String SCDItemName = "";
    private String optionValue = "";
    private String controlItemName = "";
    private Integer controlItemId = 0;
    private String message = "";
    private Integer SCDItemFormMetadataId = 0;
    
    
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

    public Integer getSCDItemFormMetadataId() {
        return SCDItemFormMetadataId;
    }

    public void setSCDItemFormMetadataId(Integer sCDItemFormMetadataId) {
        SCDItemFormMetadataId = sCDItemFormMetadataId;
    }
}