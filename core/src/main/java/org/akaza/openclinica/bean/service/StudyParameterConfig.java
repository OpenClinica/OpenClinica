/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.service;

import java.io.Serializable;

/**
 * This is a help class for each study, it saves all the parameter
 * configurations
 *
 * @author jxu
 *
 */
public class StudyParameterConfig implements Serializable{
    private String collectDob;

    private String discrepancyManagement;

    private String genderRequired;// true or false

    private String subjectPersonIdRequired; // required, optional or not used

    private String interviewerNameRequired;// required, optional

    private String interviewerNameDefault;// blank,pre-populated

    private String interviewerNameEditable;// editable or not

    private String interviewDateRequired;// required, optional

    private String interviewDateDefault; // blank, pre-populated

    private String interviewDateEditable;// editable or not

    private String subjectIdGeneration;// manual, auto non-editable, auto
    // editable,

    private String subjectIdPrefixSuffix;// auto with prefix/suffix, or not

    private String personIdShownOnCRF;// personal Id is shown on CRF header or
    // not

    private String secondaryLabelViewable;//Subject secondary label would be shown on CRF header

    private String adminForcedReasonForChange;//Administrative editing will not allow changes without a DN added 'reason for change'

    private String eventLocationRequired;
    
    private String participantPortal;

    private String randomization;

    private String enforceEnrollmentCap;
    
    public String getRandomization() {
        return randomization;
    }

    public void setRandomization(String randomization) {
        this.randomization = randomization;
    }

    
    public String getParticipantPortal() {
		return participantPortal;
	}

	public void setParticipantPortal(String participantPortal) {
		this.participantPortal = participantPortal;
	}

	public StudyParameterConfig() {
        collectDob = "1";
        discrepancyManagement = "true";
        genderRequired = "true";
        subjectPersonIdRequired = "required";
        interviewerNameRequired = "not_used";

        interviewerNameDefault = "blank";
        interviewerNameEditable = "true";
        interviewDateRequired = "not_used";
        interviewDateDefault = "blank"; // blank, pre-populated
        interviewDateEditable = "true";// editable or not
        subjectIdGeneration = "manual";// manual, auto non-editable, auto
        // editable,
        subjectIdPrefixSuffix = "true";
        personIdShownOnCRF = "false";
        secondaryLabelViewable = "false";
        adminForcedReasonForChange = "true";
        eventLocationRequired = "not_used";
        participantPortal="disabled";
        randomization="disabled";
        enforceEnrollmentCap = "false";
    }

	
	
    /**
     * @return Returns the collectDob.
     */
    public String getCollectDob() {
        return collectDob;
    }

    /**
     * @param collectDob
     *            The collectDob to set.
     */
    public void setCollectDob(String collectDob) {
        this.collectDob = collectDob;
    }

    /**
     * @return Returns the discrepancyManagement.
     */
    public String getDiscrepancyManagement() {
        return discrepancyManagement;
    }

    /**
     * @param discrepancyManagement
     *            The discrepancyManagement to set.
     */
    public void setDiscrepancyManagement(String discrepancyManagement) {
        this.discrepancyManagement = discrepancyManagement;
    }

    /**
     * @return Returns the genderRequired.
     */
    public String getGenderRequired() {
        return genderRequired;
    }

    /**
     * @param genderRequired
     *            The genderRequired to set.
     */
    public void setGenderRequired(String genderRequired) {
        this.genderRequired = genderRequired;
    }

    /**
     * @return Returns the interviewDateDefault.
     */
    public String getInterviewDateDefault() {
        return interviewDateDefault;
    }

    /**
     * @param interviewDateDefault
     *            The interviewDateDefault to set.
     */
    public void setInterviewDateDefault(String interviewDateDefault) {
        this.interviewDateDefault = interviewDateDefault;
    }

    /**
     * @return Returns the interviewDateEditable.
     */
    public String getInterviewDateEditable() {
        return interviewDateEditable;
    }

    /**
     * @param interviewDateEditable
     *            The interviewDateEditable to set.
     */
    public void setInterviewDateEditable(String interviewDateEditable) {
        this.interviewDateEditable = interviewDateEditable;
    }

    /**
     * @return Returns the interviewDateRequired.
     */
    public String getInterviewDateRequired() {
        return interviewDateRequired;
    }

    /**
     * @param interviewDateRequired
     *            The interviewDateRequired to set.
     */
    public void setInterviewDateRequired(String interviewDateRequired) {
        this.interviewDateRequired = interviewDateRequired;
    }

    /**
     * @return Returns the interviewerNameDefault.
     */
    public String getInterviewerNameDefault() {
        return interviewerNameDefault;
    }

    /**
     * @param interviewerNameDefault
     *            The interviewerNameDefault to set.
     */
    public void setInterviewerNameDefault(String interviewerNameDefault) {
        this.interviewerNameDefault = interviewerNameDefault;
    }

    /**
     * @return Returns the interviewerNameEditable.
     */
    public String getInterviewerNameEditable() {
        return interviewerNameEditable;
    }

    /**
     * @param interviewerNameEditable
     *            The interviewerNameEditable to set.
     */
    public void setInterviewerNameEditable(String interviewerNameEditable) {
        this.interviewerNameEditable = interviewerNameEditable;
    }

    /**
     * @return Returns the interviewerNameRequired.
     */
    public String getInterviewerNameRequired() {
        return interviewerNameRequired;
    }

    /**
     * @param interviewerNameRequired
     *            The interviewerNameRequired to set.
     */
    public void setInterviewerNameRequired(String interviewerNameRequired) {
        this.interviewerNameRequired = interviewerNameRequired;
    }

    /**
     * @return Returns the subjectIdGeneration.
     */
    public String getSubjectIdGeneration() {
        return subjectIdGeneration;
    }

    /**
     * @param subjectIdGeneration
     *            The subjectIdGeneration to set.
     */
    public void setSubjectIdGeneration(String subjectIdGeneration) {
        this.subjectIdGeneration = subjectIdGeneration;
    }

    /**
     * @return Returns the subjectIdPrefixSuffix.
     */
    public String getSubjectIdPrefixSuffix() {
        return subjectIdPrefixSuffix;
    }

    /**
     * @param subjectIdPrefixSuffix
     *            The subjectIdPrefixSuffix to set.
     */
    public void setSubjectIdPrefixSuffix(String subjectIdPrefixSuffix) {
        this.subjectIdPrefixSuffix = subjectIdPrefixSuffix;
    }

    /**
     * @return Returns the subjectPersonIdRequired.
     */
    public String getSubjectPersonIdRequired() {
        return subjectPersonIdRequired;
    }

    /**
     * @param subjectPersonIdRequired
     *            The subjectPersonIdRequired to set.
     */
    public void setSubjectPersonIdRequired(String subjectPersonIdRequired) {
        this.subjectPersonIdRequired = subjectPersonIdRequired;
    }

    /**
     * @return Returns the personIdShownOnCRF.
     */
    public String getPersonIdShownOnCRF() {
        return personIdShownOnCRF;
    }

    /**
     * @param personIdShownOnCRF
     *            The personIdShownOnCRF to set.
     */
    public void setPersonIdShownOnCRF(String personIdShownOnCRF) {
        this.personIdShownOnCRF = personIdShownOnCRF;
    }

    public String getSecondaryLabelViewable() {
        return secondaryLabelViewable;
    }

    public void setSecondaryLabelViewable(String secondaryLabelViewable) {
        this.secondaryLabelViewable = secondaryLabelViewable;
    }

	public String getAdminForcedReasonForChange() {
		return adminForcedReasonForChange;
	}

	public void setAdminForcedReasonForChange(String adminForcedReasonForChange) {
		this.adminForcedReasonForChange = adminForcedReasonForChange;
	}

    public String getEventLocationRequired() {
        return eventLocationRequired;
    }

    public void setEventLocationRequired(String eventLocationRequired) {
        this.eventLocationRequired = eventLocationRequired;
    }

    public String getEnforceEnrollmentCap() {
        return enforceEnrollmentCap;
    }

    public void setEnforceEnrollmentCap(String enforceEnrollmentCap) {
        this.enforceEnrollmentCap = enforceEnrollmentCap;
    }
}
