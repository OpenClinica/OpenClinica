package org.akaza.openclinica.bean.admin;

import java.util.Date;

import org.akaza.openclinica.bean.core.EntityBean;

/*
 *
 * @author Krikor Krumlian 2/10/2007
 * @author jsampson
 *
 */

public class AuditBean extends EntityBean {

    private Date auditDate;
    private String auditTable;
    private int userId;
    private int entityId;
    private String entityName;
    private String reasonForChange;
    private int auditEventTypeId;
    private String oldValue;
    private String newValue;
    private int eventCRFId;
    private String userName;
    private String auditEventTypeName;
    private int studyEventId;

    private int itemDataTypeId;
    private int ordinal;
    private int eventCrfVersionId;
    private String crfName;
    private String crfVersionName;
    private String formLayoutName;
    private Date dateInterviewed;
    private String interviewerName;
    private int itemDataRepeatKey;
    private String details;

    public int getItemDataRepeatKey() {
        return itemDataRepeatKey;
    }

    public void setItemDataRepeatKey(int itemDataRepeatKey) {
        this.itemDataRepeatKey = itemDataRepeatKey;
    }

    public Date getDateInterviewed() {
        return dateInterviewed;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setDateInterviewed(Date dateInterviewed) {
        this.dateInterviewed = dateInterviewed;
    }

    public String getCrfName() {
        return crfName;
    }

    public void setCrfName(String crfName) {
        this.crfName = crfName;
    }

    public String getCrfVersionName() {
        return crfVersionName;
    }

    public void setCrfVersionName(String crfVersionName) {
        this.crfVersionName = crfVersionName;
    }

    public int getEventCrfVersionId() {
        return eventCrfVersionId;
    }

    public void setEventCrfVersionId(int eventCrfVersionId) {
        this.eventCrfVersionId = eventCrfVersionId;
    }

    public int getItemDataTypeId() {
        return this.itemDataTypeId;
    }

    public void setItemDataTypeId(int itemDataTypeId) {
        this.itemDataTypeId = itemDataTypeId;
    }

    public Date getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(Date auditDate) {
        this.auditDate = auditDate;
    }

    public int getAuditEventTypeId() {
        return auditEventTypeId;
    }

    public void setAuditEventTypeId(int auditEventTypeId) {
        this.auditEventTypeId = auditEventTypeId;
    }

    public String getAuditTable() {
        return auditTable;
    }

    public void setAuditTable(String auditTable) {
        this.auditTable = auditTable;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public int getEventCRFId() {
        return eventCRFId;
    }

    public void setEventCRFId(int eventCRFId) {
        this.eventCRFId = eventCRFId;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getReasonForChange() {
        return reasonForChange;
    }

    public void setReasonForChange(String reasonForChange) {
        this.reasonForChange = reasonForChange;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAuditEventTypeName() {
        return auditEventTypeName;
    }

    public void setAuditEventTypeName(String auditEventTypeName) {
        this.auditEventTypeName = auditEventTypeName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getStudyEventId() {
        return studyEventId;
    }

    public void setStudyEventId(int studyEventId) {
        this.studyEventId = studyEventId;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (auditTable == null ? 0 : auditTable.hashCode());
        result = PRIME * result + userId;
        result = PRIME * result + id;
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
        final AuditBean other = (AuditBean) obj;
        if (auditTable == null) {
            if (other.auditTable != null)
                return false;
        } else if (!auditTable.equals(other.auditTable))
            return false;
        if (userId != other.userId)
            return false;
        if (id != other.id)
            return false;
        return true;
    }

    public String getFormLayoutName() {
        return formLayoutName;
    }

    public void setFormLayoutName(String formLayoutName) {
        this.formLayoutName = formLayoutName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

}
