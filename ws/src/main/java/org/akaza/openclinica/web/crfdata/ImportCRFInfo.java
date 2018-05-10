package org.akaza.openclinica.web.crfdata;

import org.akaza.openclinica.bean.core.DataEntryStage;

public class ImportCRFInfo {
    private String studyOID;
    private String studySubjectOID;
    private String studyEventOID;
    private String formOID;
    private Integer eventCRFID;
    private boolean processImport;
    private DataEntryStage postImportStage;
    private DataEntryStage preImportStage;

    public ImportCRFInfo(String studyOID, String studySubjectOID, String studyEventOID, String formOID) {
        this.studyOID = studyOID;
        this.studySubjectOID = studySubjectOID;
        this.studyEventOID = studyEventOID;
        this.formOID = formOID;
        this.eventCRFID = null;
        this.processImport = true;
        this.postImportStage = DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE;
        this.preImportStage = null;
    }

    public String getStudyOID() {
        return studyOID;
    }

    public void setStudyOID(String studyOID) {
        this.studyOID = studyOID;
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

    public DataEntryStage getPostImportStage() {
        return postImportStage;
    }

    public void setPostImportStage(DataEntryStage postImportStage) {
        this.postImportStage = postImportStage;
    }

    public DataEntryStage getPreImportStage() {
        return preImportStage;
    }

    public void setPreImportStage(DataEntryStage preImportStage) {
        this.preImportStage = preImportStage;
    }

}
