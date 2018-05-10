package org.akaza.openclinica.bean.submit.crfdata;

import org.akaza.openclinica.bean.odmbeans.AuditLogsBean;
import org.akaza.openclinica.bean.odmbeans.DiscrepancyNotesBean;
import org.akaza.openclinica.bean.odmbeans.ElementRefBean;

public class ImportItemDataBean {
    private String itemOID;
    private String transactionType;
    private String value;
    private String isNull; // boolean, tbh?
    private ElementRefBean measurementUnitRef = new ElementRefBean();
    private String reasonForNull;
    private AuditLogsBean auditLogs = new AuditLogsBean();
    private DiscrepancyNotesBean discrepancyNotes = new DiscrepancyNotesBean();
    private boolean deleted;
    private String itemName;

    private boolean hasValueWithNull; // this is just a flag, it is not an attribute/element

    public String getItemOID() {
        return itemOID;
    }

    public void setItemOID(String itemOID) {
        this.itemOID = itemOID;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getIsNull() {
        return isNull;
    }

    public void setIsNull(String isNull) {
        this.isNull = isNull;
    }

    public ElementRefBean getMeasurementUnitRef() {
        return measurementUnitRef;
    }

    public void setMeasurementUnitRef(ElementRefBean measurementUnitRef) {
        this.measurementUnitRef = measurementUnitRef;
    }

    public String getReasonForNull() {
        return reasonForNull;
    }

    public void setReasonForNull(String reasonForNull) {
        this.reasonForNull = reasonForNull;
    }

    public AuditLogsBean getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(AuditLogsBean auditLogs) {
        this.auditLogs = auditLogs;
    }

    public DiscrepancyNotesBean getDiscrepancyNotes() {
        return discrepancyNotes;
    }

    public void setDiscrepancyNotes(DiscrepancyNotesBean discrepancyNotes) {
        this.discrepancyNotes = discrepancyNotes;
    }

    public boolean isHasValueWithNull() {
        return hasValueWithNull;
    }

    public void setHasValueWithNull(boolean hasValueWithNull) {
        this.hasValueWithNull = hasValueWithNull;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

}
