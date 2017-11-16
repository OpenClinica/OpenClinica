package org.akaza.openclinica.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by krikorkrumlian on 11/14/17.
 */
public class AbstractAuditingEntity {

    @JsonProperty("createdBy")
    String createdBy;

    @JsonProperty("lastModifiedBy")
    String lastModifiedBy;

    @JsonProperty("createdDate")
    Date createdDate;

    @JsonProperty("lastModifiedDate")
    Date lastModifiedDate;


    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }


}
