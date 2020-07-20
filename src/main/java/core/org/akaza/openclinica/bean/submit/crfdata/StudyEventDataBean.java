package core.org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;

import core.org.akaza.openclinica.bean.odmbeans.AuditLogsBean;
import core.org.akaza.openclinica.bean.odmbeans.DiscrepancyNotesBean;
import core.org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;

public class StudyEventDataBean {
    private ArrayList<FormDataBean> formData;
    private String studyEventOID;
    private String studyEventRepeatKey;
    private AuditLogsBean auditLogs;
    private DiscrepancyNotesBean discrepancyNotes;
    private StudyEventDefinition studyEventDefinition;
    private String startDate;
    private String endDate;
    private String eventStatus;
    private StudyEventWorkflowStatusEnum workflowStatus;

    public StudyEventDataBean() {
        formData = new ArrayList<FormDataBean>();
        auditLogs = new AuditLogsBean();
        discrepancyNotes = new DiscrepancyNotesBean();
    }

    public String getStudyEventRepeatKey() {
        return studyEventRepeatKey;
    }

    public void setStudyEventRepeatKey(String studyEventRepeatKey) {
        this.studyEventRepeatKey = studyEventRepeatKey;
    }

    public String getStudyEventOID() {
        return studyEventOID;
    }

    public void setStudyEventOID(String studyEventOID) {
        this.studyEventOID = studyEventOID;
    }

    public ArrayList<FormDataBean> getFormData() {
        return formData;
    }

    public void setFormData(ArrayList<FormDataBean> formData) {
        this.formData = formData;
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

	public StudyEventDefinition getStudyEventDefinition() {
	return studyEventDefinition;
}

    public void setStudyEventDefinition(StudyEventDefinition studyEventDefinition) {
	this.studyEventDefinition = studyEventDefinition;
}

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    public StudyEventWorkflowStatusEnum getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(StudyEventWorkflowStatusEnum workflowStatus) {
        this.workflowStatus = workflowStatus;

    }

    public void setWorkflowStatusAsString(String workflowStatus){
        if(workflowStatus == null)
            workflowStatus = "";
        this.setWorkflowStatus(StudyEventWorkflowStatusEnum.getByEnglishDescription(workflowStatus.toLowerCase()));
    }

    public String getWorkflowStatusAsString(){
        if(this.workflowStatus == null)
            return null;
        return this.workflowStatus.getDisplayValue();
    }
}
