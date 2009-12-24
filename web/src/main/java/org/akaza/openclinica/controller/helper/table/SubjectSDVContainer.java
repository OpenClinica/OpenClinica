package org.akaza.openclinica.controller.helper.table;

/**
 * The container or bean representing a row in the sdv table.
 */
public class SubjectSDVContainer {

    private String sdvStatus;

    public String getSdvStatus() {
        return sdvStatus;
    }

    public void setSdvStatus(String sdvStatus) {
        this.sdvStatus = sdvStatus;
    }

    private String studySubjectId;
    private String studyIdentifier;
    private String personId;
    private String secondaryId;
    private String eventName;
    private String eventDate;
    private String enrollmentDate;
    private String studySubjectStatus;
    private String crfNameVersion;
    //100% Required, Partial Required, 100% and Partial, Not Required
    private String sdvRequirementDefinition;
    private String crfStatus;
    private String studyEventStatus;
    private String lastUpdatedDate;
    private String lastUpdatedBy;
    private String sdvStatusActions;
    private String numberOfCRFsSDV;
    private String percentageOfCRFsSDV;
    private String group;

    public SubjectSDVContainer() {
        sdvStatus = "";
        studySubjectId = "";
        studyIdentifier = "";
        personId = "";
        secondaryId = "";
        eventName = "";
        eventDate = "";
        enrollmentDate = "";
        studySubjectStatus = "";
        crfNameVersion = "";
        sdvRequirementDefinition = "";
        crfStatus = "";
        studyEventStatus = "";
        lastUpdatedDate = "";
        lastUpdatedBy = "";
        sdvStatusActions = "";
        numberOfCRFsSDV = "";
        percentageOfCRFsSDV = "";
        group = "";
    }

    public String getStudyEventStatus() {
        return studyEventStatus;
    }

    public void setStudyEventStatus(String studyEventStatus) {
        this.studyEventStatus = studyEventStatus;
    }

    public String getStudyIdentifier() {
        return studyIdentifier;
    }

    public void setStudyIdentifier(String studyIdentifier) {
        this.studyIdentifier = studyIdentifier;
    }

    public String getSdvRequirementDefinition() {
        return sdvRequirementDefinition;
    }

    public void setSdvRequirementDefinition(String sdvRequirementDefinition) {
        this.sdvRequirementDefinition = sdvRequirementDefinition;
    }

    public String getNumberOfCRFsSDV() {
        return numberOfCRFsSDV;
    }

    public void setNumberOfCRFsSDV(String numberOfCRFsSDV) {
        this.numberOfCRFsSDV = numberOfCRFsSDV;
    }

    public String getPercentageOfCRFsSDV() {
        return percentageOfCRFsSDV;
    }

    public void setPercentageOfCRFsSDV(String percentageOfCRFsSDV) {
        this.percentageOfCRFsSDV = percentageOfCRFsSDV;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getCrfNameVersion() {
        return crfNameVersion;
    }

    public void setCrfNameVersion(String crfNameVersion) {
        this.crfNameVersion = crfNameVersion;
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

    public String getSecondaryId() {
        return secondaryId;
    }

    public void setSecondaryId(String secondaryId) {
        this.secondaryId = secondaryId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getStudySubjectStatus() {
        return studySubjectStatus;
    }

    public void setStudySubjectStatus(String studySubjectStatus) {
        this.studySubjectStatus = studySubjectStatus;
    }

    public String getCrfStatus() {
        return crfStatus;
    }

    public void setCrfStatus(String crfStatus) {
        this.crfStatus = crfStatus;
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(String lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public String getSdvStatusActions() {
        return sdvStatusActions;
    }

    public void setSdvStatusActions(String sdvStatusActions) {
        this.sdvStatusActions = sdvStatusActions;
    }
}
