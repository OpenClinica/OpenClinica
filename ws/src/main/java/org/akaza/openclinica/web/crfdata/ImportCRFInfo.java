package org.akaza.openclinica.web.crfdata;

public class ImportCRFInfo {
    private String studySubjectOID;
    private String studyEventOID;
    private String formOID;
    private Integer eventCRFID;
    private boolean processImport;
    private String postImportStatus;

    public ImportCRFInfo(String studySubjectOID, String studyEventOID, String formOID) {
        this.studySubjectOID = studySubjectOID;
        this.studyEventOID = studyEventOID;
        this.formOID = formOID;
        this.eventCRFID = null;
        this.processImport = true;
        this.postImportStatus = "Completed";
    }

    public String getStudySubjectOID() {
        return studySubjectOID;
    }

    public void setStudySubjectOID(String studySubjectOID) {
        this.studySubjectOID = studySubjectOID;
    }

    public String getStudyEventOID() {
        return studyEventOID;
    }

    public void setStudyEventOID(String studyEventOID) {
        this.studyEventOID = studyEventOID;
    }

    public String getFormOID() {
        return formOID;
    }

    public void setFormOID(String formOID) {
        this.formOID = formOID;
    }

    public Integer getEventCRFID() {
        return eventCRFID;
    }

    public void setEventCRFID(Integer eventCRFID) {
        this.eventCRFID = eventCRFID;
    }

    public boolean isProcessImport() {
        return processImport;
    }

    public void setProcessImport(boolean processImport) {
        this.processImport = processImport;
    }

    public String getPostImportStatus() {
        return postImportStatus;
    }

    public void setPostImportStatus(String postImportStatus) {
        this.postImportStatus = postImportStatus;
    }

}
