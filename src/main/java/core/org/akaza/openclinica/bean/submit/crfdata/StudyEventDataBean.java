package core.org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;

import core.org.akaza.openclinica.bean.odmbeans.AuditLogsBean;
import core.org.akaza.openclinica.bean.odmbeans.DiscrepancyNotesBean;
import core.org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.akaza.openclinica.service.ImportValidationServiceImpl;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.lang3.BooleanUtils;

public class StudyEventDataBean {
    private static final String STATUS_ATTRIBUTE_TRUE = "Yes";
    private static final String STATUS_ATTRIBUTE_FALSE = "No";
    private ArrayList<FormDataBean> formData;
    private ArrayList<SignatureBean> signatures;
    private String studyEventOID;
    private String studyEventRepeatKey;
    private AuditLogsBean auditLogs;
    private DiscrepancyNotesBean discrepancyNotes;
    private StudyEventDefinition studyEventDefinition;
    private String startDate;
    private String endDate;
    private String eventStatus;
    private StudyEventWorkflowStatusEnum workflowStatus;
    private String signed;

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

    public ArrayList<SignatureBean> getSignatures() {
        return signatures;
    }

    public void setSignatures(ArrayList<SignatureBean> signatures) {
        this.signatures = signatures;
    }

    public String getSignedString() {
        return signed;
    }

    public void setSignedString(String signed) {
        this.signed = signed;
    }

    public Boolean getSigned(){
        try {
            return ImportValidationServiceImpl.getStatusAttribute(this.signed);
        } catch (OpenClinicaSystemException ose){
            throw new OpenClinicaSystemException("FAILED", ErrorConstants.ERR_SIGNED_STATUS_INVALID);
        }

    }

    public void setSigned(Boolean signed){
        if(signed == null)
            this.signed = null;
        else if(BooleanUtils.isTrue(signed))
            this.signed = STATUS_ATTRIBUTE_TRUE;
        else
            this.signed = STATUS_ATTRIBUTE_FALSE;
    }
}
