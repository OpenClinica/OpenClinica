package org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;
import java.util.Date;

import org.akaza.openclinica.bean.odmbeans.AuditLogsBean;
import org.akaza.openclinica.bean.odmbeans.DiscrepancyNotesBean;

public class FormDataBean {
    private ArrayList<ImportItemGroupDataBean> itemGroupData;
    private AuditLogsBean auditLogs;
    private DiscrepancyNotesBean discrepancyNotes;
    private String formOID;
    private String EventCRFStatus;
    private String formLayoutName;
    private Date createdDate;
    private Date updatedDate;
    private String createdBy;
    private String updatedBy;

    public FormDataBean() {
        itemGroupData = new ArrayList<>();
        auditLogs = new AuditLogsBean();
        discrepancyNotes = new DiscrepancyNotesBean();
    }

    public String getFormOID() {
        return formOID;
    }

    public void setFormOID(String formOID) {
        this.formOID = formOID;
    }

    public String getEventCRFStatus() {
        return EventCRFStatus;
    }

    public void setEventCRFStatus(String eventCRFStatus) {
        EventCRFStatus = eventCRFStatus;
    }

    public ArrayList<ImportItemGroupDataBean> getItemGroupData() {
        return itemGroupData;
    }

    public void setItemGroupData(ArrayList<ImportItemGroupDataBean> itemGroupData) {
        this.itemGroupData = itemGroupData;
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

    public String getFormLayoutName() {
        return formLayoutName;
    }

    public void setFormLayoutName(String formLayoutName) {
        this.formLayoutName = formLayoutName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

}
