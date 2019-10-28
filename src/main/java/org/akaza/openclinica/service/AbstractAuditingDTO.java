package org.akaza.openclinica.service;

import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Base abstract class for DTO which will hold definitions for created, last modified by and created,
 * last modified by date.
 */
public abstract class AbstractAuditingDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ReadOnlyProperty
    private String createdBy;

    @ReadOnlyProperty
    private ZonedDateTime createdDate = ZonedDateTime.now();

    private String lastModifiedBy;

    private ZonedDateTime lastModifiedDate = ZonedDateTime.now();

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public ZonedDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(ZonedDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
