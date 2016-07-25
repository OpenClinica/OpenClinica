package org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;

import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.odmbeans.AuditLogsBean;
import org.akaza.openclinica.bean.odmbeans.DiscrepancyNotesBean;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;

public class StudyEventDataBean {
    private ArrayList<FormDataBean> formData;
    private String studyEventOID;
    private String studyEventRepeatKey;
    private AuditLogsBean auditLogs;
    private DiscrepancyNotesBean discrepancyNotes;
    private StudyEventDefinition studyEventDefinition;

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


}
