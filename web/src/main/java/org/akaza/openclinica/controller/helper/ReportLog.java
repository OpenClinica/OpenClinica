package org.akaza.openclinica.controller.helper;

import java.util.ArrayList;
import java.util.Locale;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.apache.commons.lang.StringUtils;

public class ReportLog {

    private int subjectCount;
    private ArrayList<String> migrationCanNotPerformList;
    private ArrayList<String> errorList;
    private ArrayList<String> reportLogList;

    public ReportLog() {
        super();
        migrationCanNotPerformList = new ArrayList<String>();
        errorList = new ArrayList<String>();
        reportLogList = new ArrayList<String>();
    }

    public int getSubjectCount() {
        return subjectCount;
    }

    public void setSubjectCount(int subjectCount) {
        this.subjectCount = subjectCount;
    }

    public ArrayList<String> getMigrationCanNotPerformList() {
        return migrationCanNotPerformList;
    }

    public void setMigrationCanNotPerformList(ArrayList<String> migrationCanNotPerformList) {
        this.migrationCanNotPerformList = migrationCanNotPerformList;
    }

    public ArrayList<String> getErrorList() {
        return errorList;
    }

    public void setErrorList(ArrayList<String> errorList) {
        this.errorList = errorList;
    }

    public ArrayList<String> getReportLogList() {
        return reportLogList;
    }

    public void setReportLogList(ArrayList<String> reportLogList) {
        this.reportLogList = reportLogList;
    }


}
