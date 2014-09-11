/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.domain.crfdata;

/**
 * For origin and destination items of instant-calculation func:onchange
 */
//ywang (Aug., 2011)
public class InstantOnChangePairContainer {
    private Integer destItemFormMetadataId;
    private Integer destSectionId;
    private String destItemGroupOid;
    private Integer destItemId;
    private Boolean destRepeating;
    private Boolean destUngrouped;
    private String optionValue;
    private Integer originSectionId;
    private String originItemGroupOid;
    private Integer originItemId;
    private Boolean originRepeating;
    private Boolean originUngrouped;

    public Integer getDestItemFormMetadataId() {
        return destItemFormMetadataId;
    }
    public void setDestItemFormMetadataId(Integer destItemFormMetadataId) {
        this.destItemFormMetadataId = destItemFormMetadataId;
    }
    public Integer getDestSectionId() {
        return destSectionId;
    }
    public void setDestSectionId(Integer destSectionId) {
        this.destSectionId = destSectionId;
    }
    public String getDestItemGroupOid() {
        return destItemGroupOid;
    }
    public void setDestItemGroupOid(String destItemGroupOid) {
        this.destItemGroupOid = destItemGroupOid;
    }
    public Integer getDestItemId() {
        return destItemId;
    }
    public void setDestItemId(Integer destItemId) {
        this.destItemId = destItemId;
    }
    public Boolean getDestRepeating() {
        return destRepeating;
    }
    public void setDestRepeating(Boolean destRepeating) {
        this.destRepeating = destRepeating;
    }
    public Boolean getDestUngrouped() {
        return destUngrouped;
    }
    public void setDestUngrouped(Boolean destUngrouped) {
        this.destUngrouped = destUngrouped;
    }
    public String getOptionValue() {
        return optionValue;
    }
    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }
    public Integer getOriginSectionId() {
        return originSectionId;
    }
    public void setOriginSectionId(Integer originSectionId) {
        this.originSectionId = originSectionId;
    }
    public String getOriginItemGroupOid() {
        return originItemGroupOid;
    }
    public void setOriginItemGroupOid(String originItemGroupOid) {
        this.originItemGroupOid = originItemGroupOid;
    }
    public Integer getOriginItemId() {
        return originItemId;
    }
    public void setOriginItemId(Integer originItemId) {
        this.originItemId = originItemId;
    }
    public Boolean getOriginRepeating() {
        return originRepeating;
    }
    public void setOriginRepeating(Boolean originRepeating) {
        this.originRepeating = originRepeating;
    }
    public Boolean getOriginUngrouped() {
        return originUngrouped;
    }
    public void setOriginUngrouped(Boolean originUngrouped) {
        this.originUngrouped = originUngrouped;
    }
}