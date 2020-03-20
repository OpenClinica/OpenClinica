package org.akaza.openclinica.controller.helper.table;

import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;

import java.util.Date;

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
    private Date eventDate;
    private String enrollmentDate;
    private String studySubjectStatus;
    private String crfName;
    private String crfVersion;
    //100% Required, Partial Required, 100% and Partial, Not Required
    private String sdvRequirementDefinition;
    private String crfStatus;
    private String subjectEventStatus;
    private Date lastUpdatedDate;
    private String lastUpdatedBy;
    private String sdvStatusActions;
    private String numberOfCRFsSDV;
    private String percentageOfCRFsSDV;
    private String group;
    private String openQueries;
    private EventCrfWorkflowStatusEnum crfWorkflowStatus;
    private StudyEventWorkflowStatusEnum eventWorkflowStatus;

    public SubjectSDVContainer() {
        sdvStatus = "";
        studySubjectId = "";
        studyIdentifier = "";
        personId = "";
        secondaryId = "";
        eventName = "";
        eventDate = null;
        enrollmentDate = "";
        studySubjectStatus = "";
        crfName = "";
        sdvRequirementDefinition = "";
        crfStatus = "";
        subjectEventStatus = "";
        lastUpdatedDate = null;
        lastUpdatedBy = "";
        sdvStatusActions = "";
        numberOfCRFsSDV = "";
        percentageOfCRFsSDV = "";
        group = "";
        openQueries = "";
        crfVersion = "";

    }

    public String getSubjectEventStatus() {
        return subjectEventStatus;
    }

    public void setSubjectEventStatus(String subjectEventStatus) {
        this.subjectEventStatus = subjectEventStatus;
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

    public String getCrfName() {
        return crfName;
    }

    public void setCrfName(String crfName) {
        this.crfName = crfName;
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

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
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

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
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

    public String getOpenQueries() {
        return openQueries;
    }

    public void setOpenQueries(String openQueries) {
        this.openQueries = openQueries;
    }

    public String getCrfVersion() {
        return crfVersion;
    }

    public void setCrfVersion(String crfVersion) {
        this.crfVersion = crfVersion;
    }

    public EventCrfWorkflowStatusEnum getCrfWorkflowStatus() {
        return crfWorkflowStatus;
    }

    public void setCrfWorkflowStatus(EventCrfWorkflowStatusEnum crfWorkflowStatus) {
        this.crfWorkflowStatus = crfWorkflowStatus;
    }

    public StudyEventWorkflowStatusEnum getEventWorkflowStatus() {
        return eventWorkflowStatus;
    }

    public void setEventWorkflowStatus(StudyEventWorkflowStatusEnum eventWorkflowStatus) {
        this.eventWorkflowStatus = eventWorkflowStatus;
    }
}
