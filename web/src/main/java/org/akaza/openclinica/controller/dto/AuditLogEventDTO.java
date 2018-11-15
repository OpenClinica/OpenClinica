package org.akaza.openclinica.controller.dto;

import org.akaza.openclinica.domain.datamap.AuditLogEventType;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.service.AbstractAuditingDTO;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by krikorkrumlian on 9/18/17.
 */
    public class AuditLogEventDTO extends AbstractAuditingDTO implements Serializable {

   private String auditTable;
    private Integer userId;
    private Integer entityId;
    private String entityName;
    private String reasonForChange;
    private String oldValue;
    private String newValue;
    private Integer eventCrfId;
    private Integer studyEventId;
    private Integer eventCrfVersionId;
    private String instanceId;
    private String details;
    private int auditLogEventTypId;



    public String getAuditTable() {
        return auditTable;
    }

    public void setAuditTable(String auditTable) {
        this.auditTable = auditTable;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getReasonForChange() {
        return reasonForChange;
    }

    public void setReasonForChange(String reasonForChange) {
        this.reasonForChange = reasonForChange;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Integer getEventCrfId() {
        return eventCrfId;
    }

    public void setEventCrfId(Integer eventCrfId) {
        this.eventCrfId = eventCrfId;
    }

    public Integer getStudyEventId() {
        return studyEventId;
    }

    public void setStudyEventId(Integer studyEventId) {
        this.studyEventId = studyEventId;
    }

    public Integer getEventCrfVersionId() {
        return eventCrfVersionId;
    }

    public void setEventCrfVersionId(Integer eventCrfVersionId) {
        this.eventCrfVersionId = eventCrfVersionId;
    }


    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getAuditLogEventTypId() {
        return auditLogEventTypId;
    }

    public void setAuditLogEventTypId(int auditLogEventTypId) {
        this.auditLogEventTypId = auditLogEventTypId;
    }
}
