package org.akaza.openclinica.controller.helper;

import java.util.ArrayList;

public class TransferObject {
    String studyOID;
    String sourceFormVersion;
    String targetFormVersion;
    ArrayList<String> sites;
    ArrayList<String> studyEventDefs;

    public String getStudyOID() {
        return studyOID;
    }

    public void setStudyOID(String studyOID) {
        this.studyOID = studyOID;
    }

    public String getSourceFormVersion() {
        return sourceFormVersion;
    }

    public void setSourceFormVersion(String sourceFormVersion) {
        this.sourceFormVersion = sourceFormVersion;
    }

    public String getTargetFormVersion() {
        return targetFormVersion;
    }

    public void setTargetFormVersion(String targetFormVersion) {
        this.targetFormVersion = targetFormVersion;
    }

    public ArrayList<String> getSites() {
        return sites;
    }

    public void setSites(ArrayList<String> selectedSiteArrayList) {
        this.sites = selectedSiteArrayList;
    }

    public ArrayList<String> getStudyEventDefs() {
        return studyEventDefs;
    }

    public void setStudyEventDefs(ArrayList<String> selectedEventArrayList) {
        this.studyEventDefs = selectedEventArrayList;
    }

}
