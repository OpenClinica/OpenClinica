package core.org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;
import java.util.Date;

import core.org.akaza.openclinica.bean.odmbeans.AuditLogsBean;
import core.org.akaza.openclinica.bean.odmbeans.DiscrepancyNotesBean;
import core.org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;

public class FormDataBean {
    private ArrayList<ImportItemGroupDataBean> itemGroupData;
    private AuditLogsBean auditLogs;
    private DiscrepancyNotesBean discrepancyNotes;
    private String formOID;
    private String EventCRFStatus;
    private String reasonForChangeForCompleteForms;
    private String sdvStatusString;


    private String formLayoutName;
    private Date createdDate;
    private Date updatedDate;
    private String createdBy;
    private String updatedBy;
    private EventCrfWorkflowStatusEnum workflowStatus;

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

    public String getReasonForChangeForCompleteForms() {
        return reasonForChangeForCompleteForms;
    }

    public void setReasonForChangeForCompleteForms(String reasonForChangeForCompleteForms) {
        this.reasonForChangeForCompleteForms = reasonForChangeForCompleteForms;
    }

    public EventCrfWorkflowStatusEnum getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(EventCrfWorkflowStatusEnum workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public void setWorkflowStatusAsString(String workflowStatus){
        if(workflowStatus == null){
            return;
        }
        EventCrfWorkflowStatusEnum statusEnum = EventCrfWorkflowStatusEnum.getByEnglishDescription  (workflowStatus.toLowerCase());
        if(statusEnum == null)   //Setting to EventCRFStatus if it is not able to map any EventCrfWorkflowStatusEnum, this is done for throwing errorCode.formStatusNotValid later in the code flow
            this.EventCRFStatus = workflowStatus;
        this.workflowStatus = statusEnum;
    }

    public String getWorkflowStatusAsString(){
        if(this.workflowStatus == null)
            return null;
        return this.workflowStatus.getDisplayValue();
    }

    public String getSdvStatusString() {
        return sdvStatusString;
    }

    public void setSdvStatusString(String sdvStatusString) {
        this.sdvStatusString = sdvStatusString;
    }
}