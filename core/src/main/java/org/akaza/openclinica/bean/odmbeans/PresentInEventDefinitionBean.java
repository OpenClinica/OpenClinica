/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 
 *
 */

package org.akaza.openclinica.bean.odmbeans;

public class PresentInEventDefinitionBean {
	// attributes
	private String studyEventOid;
	private String isDefaultVersion;
	private String nullValues;
	private String passwordRequired;
	private String doubleDataEntry;
	private String hideCrf;
	private String sourceDataVerification;
	private String participantForm;
    private String allowAnonymousSubmission;
    private String submissionUrl;
    private String offline;

	public String getStudyEventOid() {
		return studyEventOid;
	}

	public void setStudyEventOid(String studyEventOid) {
		this.studyEventOid = studyEventOid;
	}

	public String getIsDefaultVersion() {
		return isDefaultVersion;
	}

	public void setIsDefaultVersion(String isDefaultVersion) {
		this.isDefaultVersion = isDefaultVersion;
	}

	public String getNullValues() {
		return nullValues;
	}

	public void setNullValues(String nullValues) {
		this.nullValues = nullValues;
	}

	public String getPasswordRequired() {
		return passwordRequired;
	}

	public void setPasswordRequired(String passwordRequired) {
		this.passwordRequired = passwordRequired;
	}

	public String getDoubleDataEntry() {
		return doubleDataEntry;
	}

	public void setDoubleDataEntry(String doubleDataEntry) {
		this.doubleDataEntry = doubleDataEntry;
	}

	public String getHideCrf() {
		return hideCrf;
	}

	public void setHideCrf(String hideCrf) {
		this.hideCrf = hideCrf;
	}

	public String getSourceDataVerification() {
		return sourceDataVerification;
	}

	public void setSourceDataVerification(String sourceDataVerification) {
		this.sourceDataVerification = sourceDataVerification;
	}

	public String getParticipantForm() {
		return participantForm;
	}

	public void setParticipantForm(String participantForm) {
		this.participantForm = participantForm;
	}

    public String getAllowAnonymousSubmission() {
        return allowAnonymousSubmission;
    }

    public void setAllowAnonymousSubmission(String allowAnonymousSubmission) {
        this.allowAnonymousSubmission = allowAnonymousSubmission;
    }

    public String getSubmissionUrl() {
        return submissionUrl;
    }

    public void setSubmissionUrl(String submissionUrl) {
        this.submissionUrl = submissionUrl;
    }

    public String getOffline() {
        return offline;
    }

    public void setOffline(String offline) {
        this.offline = offline;
    }

}