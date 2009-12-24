package org.akaza.openclinica.controller.helper.table;

/**
 * The container or bean representing a row in the sdv table.
 */
public class SubjectAggregateContainer {

    private String sdvStatus;
    private String studySubjectId;
    private String personId;
    private String siteId;
    private String studySubjectStatus;
    private String numberCRFComplete;
    private String numberOfCRFsSDV;
    private String totalEventCRF;
    private String group;
    private String actions;

    public SubjectAggregateContainer() {
        sdvStatus = "";
        studySubjectId = "";
        personId = "";
        studySubjectStatus = "";
        numberCRFComplete = "";
        numberOfCRFsSDV = "";
        actions = "";
        group = "";
        siteId = "";
        totalEventCRF = "";
    }

    public String getSdvStatus() {
        return sdvStatus;
    }

    public void setSdvStatus(String sdvStatus) {
        this.sdvStatus = sdvStatus;
    }

    public String getStudySubjectId() {
        return studySubjectId;
    }

    public void setStudySubjectId(String studySubjectId) {
        this.studySubjectId = studySubjectId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getStudySubjectStatus() {
        return studySubjectStatus;
    }

    public void setStudySubjectStatus(String studySubjectStatus) {
        this.studySubjectStatus = studySubjectStatus;
    }

    public String getNumberCRFComplete() {
        return numberCRFComplete;
    }

    public void setNumberCRFComplete(String numberCRFComplete) {
        this.numberCRFComplete = numberCRFComplete;
    }

    public String getNumberOfCRFsSDV() {
        return numberOfCRFsSDV;
    }

    public void setNumberOfCRFsSDV(String numberOfCRFsSDV) {
        this.numberOfCRFsSDV = numberOfCRFsSDV;
    }

    public String getTotalEventCRF() {
        return totalEventCRF;
    }

    public void setTotalEventCRF(String totalEventCRF) {
        this.totalEventCRF = totalEventCRF;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }
}
