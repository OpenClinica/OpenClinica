/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2010 Akaza Research
 */

package org.akaza.openclinica.domain.crfdata;

import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Mapping to scd_item_metadata table
 * @author ywang
 *
 */
@Entity
@Table(name="scd_item_metadata")
@GenericGenerator(name="id-generator", strategy="native", parameters={ @Parameter(name="sequence", value="scd_item_metadata_id_seq") })
public class SCDItemMetadataBean extends AbstractMutableDomainObject {
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + controlItemFormMetadataId;
        result = prime * result + (controlItemName == null ? 0 : controlItemName.hashCode());
        result = prime * result + (message == null ? 0 : message.hashCode());
        result = prime * result + (optionValue == null ? 0 : optionValue.hashCode());
        result = prime * result + scdItemFormMetadataId;
        result = prime * result + scdItemId;
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
        SCDItemMetadataBean other = (SCDItemMetadataBean) obj;
        if (controlItemFormMetadataId != other.controlItemFormMetadataId)
            return false;
        if (controlItemName == null) {
            if (other.controlItemName != null)
                return false;
        } else if (!controlItemName.equals(other.controlItemName))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (optionValue == null) {
            if (other.optionValue != null)
                return false;
        } else if (!optionValue.equals(other.optionValue))
            return false;
        if (scdItemFormMetadataId != other.scdItemFormMetadataId)
            return false;
        if (scdItemId != other.scdItemId)
            return false;
        return true;
    }
    private Integer scdItemFormMetadataId;
    private Integer controlItemFormMetadataId;
    private String controlItemName;
    private String optionValue;
    private String message;
    
    private int scdItemId = 0; 
    
    public SCDItemMetadataBean() {
        scdItemFormMetadataId = 0;
        controlItemFormMetadataId = 0;
        controlItemName = "";
        optionValue = "";
        message="";

    }
    
    public Integer getScdItemFormMetadataId() {
        return scdItemFormMetadataId;
    }
    public void setScdItemFormMetadataId(int scdItemFormMetadataId) {
        this.scdItemFormMetadataId = scdItemFormMetadataId;
    }
    public Integer getControlItemFormMetadataId() {
        return controlItemFormMetadataId;
    }
    public void setControlItemFormMetadataId(int controlItemFormMetadataId) {
        this.controlItemFormMetadataId = controlItemFormMetadataId;
    }
    public String getControlItemName() {
        return controlItemName;
    }
    public void setControlItemName(String controlItemName) {
        this.controlItemName = controlItemName;
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
    
    @Transient
    public int getScdItemId() {
        return scdItemId;
    }
    public void setScdItemId(int scdItemId) {
        this.scdItemId = scdItemId;
    }
}