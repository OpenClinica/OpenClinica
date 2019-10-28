package org.akaza.openclinica.controller.helper;

import java.util.ArrayList;

public class ReportLog {

    private int subjectCount;
    private int eventCrfCount;
    private String reportPreview;
    private ArrayList<String> canNotMigrate;
    private ArrayList<String> errors;
    private ArrayList<String> logs;

    public ReportLog() {
        super();
        canNotMigrate = new ArrayList<String>();
        errors = new ArrayList<String>();
        logs = new ArrayList<String>();
    }

    public int getSubjectCount() {
        return subjectCount;
    }

    public void setSubjectCount(int subjectCount) {
        this.subjectCount = subjectCount;
    }

    public String getReportPreview() {
        return reportPreview;
    }

    public void setReportPreview(String reportPreview) {
        this.reportPreview = reportPreview;
    }

    public ArrayList<String> getCanNotMigrate() {
        return canNotMigrate;
    }

    public void setCanNotMigrate(ArrayList<String> canNotMigrate) {
        this.canNotMigrate = canNotMigrate;
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public void setErrors(ArrayList<String> errors) {
        this.errors = errors;
    }

    public int getEventCrfCount() {
        return eventCrfCount;
    }

    public void setEventCrfCount(int eventCrfCount) {
        this.eventCrfCount = eventCrfCount;
    }

    public ArrayList<String> getLogs() {
        return logs;
    }

    public void setLogs(ArrayList<String> logs) {
        this.logs = logs;
    }

}
