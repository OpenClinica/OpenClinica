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
    private int scdItemFormMetadataId = 0;
    private int controlItemFormMetadataId = 0;
    private String controlItemName = "";
    private String optionValue = "";
    private String message="";
    
    private int scdItemId = 0; 
    
    public int getScdItemFormMetadataId() {
        return scdItemFormMetadataId;
    }
    public void setScdItemFormMetadataId(int scdItemFormMetadataId) {
        this.scdItemFormMetadataId = scdItemFormMetadataId;
    }
    public int getControlItemFormMetadataId() {
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