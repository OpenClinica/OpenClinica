/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import java.util.Date;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;

/**
 * <P>
 * EventCRFBean, the object that collects data on a subject while filling out a
 * CRF. Previous equivalent was individualInstrumentBean in version v.1.
 *
 * @author thickerson
 */
public class EventCRFBean extends AuditableEntityBean {
    private int studyEventId = 0;
    private int CRFVersionId = 0;
    private int formLayoutId = 0;
    private Date dateInterviewed;
    private String interviewerName = "";
    private int completionStatusId = 0;
    // private int statusId =1;
    private Status status;
    private org.akaza.openclinica.domain.Status nexGenStatus;
    private String annotations = "";
    private Date dateCompleted;
    private int validatorId = 0;
    private Date dateValidate;
    private Date dateValidateCompleted;
    private String validatorAnnotations = "";
    private String validateString = "";
    private int studySubjectId = 0;
    private boolean electronicSignatureStatus = false;
    private boolean sdvStatus = false;
    private int sdvUpdateId = 0;
    // the following are not in the table
    private String studySubjectName = "";
    private String eventName = "";
    private String studyName = "";
    private int eventOrdinal = 1;

    private StudySubjectBean studySubject;
    private StudyEventBean studyEvent;
    private FormLayoutBean formLayout;
    // the following properties are not in the table; they are meant for
    // convenience
    private CRFBean crf = new CRFBean();
    private CRFVersionBean crfVersion = new CRFVersionBean();
    private DataEntryStage stage;

    public EventCRFBean() {
        stage = DataEntryStage.INVALID;
        status = Status.INVALID;
        nexGenStatus = org.akaza.openclinica.domain.Status.INVALID;
    }

    public EventCRFBean(EventCRFBean eventCRFBean) {
        super();
        this.studyEventId = eventCRFBean.getStudyEventId();
        this.CRFVersionId = eventCRFBean.getCRFVersionId();
        this.dateInterviewed = eventCRFBean.getDateInterviewed();
        this.interviewerName = eventCRFBean.getInterviewerName();
        this.completionStatusId = eventCRFBean.getCompletionStatusId();
        this.status = eventCRFBean.getStatus();
        this.nexGenStatus = eventCRFBean.getNexGenStatus();
        this.annotations = eventCRFBean.getAnnotations();
        this.dateCompleted = eventCRFBean.getDateCompleted();
        this.validatorId = eventCRFBean.getValidatorId();
        this.dateValidate = eventCRFBean.getDateValidate();
        this.dateValidateCompleted = eventCRFBean.getDateValidateCompleted();
        this.validatorAnnotations = eventCRFBean.getValidatorAnnotations();
        this.validateString = eventCRFBean.getValidateString();
        this.studySubjectId = eventCRFBean.getStudySubjectId();
        this.electronicSignatureStatus = eventCRFBean.isElectronicSignatureStatus();
        this.sdvStatus = eventCRFBean.isSdvStatus();
        this.studySubjectName = eventCRFBean.getStudySubjectName();
        this.eventName = eventCRFBean.getEventName();
        this.studyName = eventCRFBean.getStudyName();
        this.eventOrdinal = eventCRFBean.getEventOrdinal();
        this.crf = eventCRFBean.getCrf();
        this.crfVersion = eventCRFBean.getCrfVersion();
        this.stage = eventCRFBean.getStage();
        this.ownerId = eventCRFBean.getOwnerId();
        this.createdDate = eventCRFBean.getCreatedDate();
        this.updaterId = eventCRFBean.getUpdaterId();
        this.formLayoutId = eventCRFBean.getFormLayoutId();
    }

    public EventCRFBean copy() {
        EventCRFBean newObj = new EventCRFBean(this);
        return newObj;

    }

    public boolean isSdvStatus() {
        return sdvStatus;
    }

    public void setSdvStatus(boolean sdvStatus) {
        this.sdvStatus = sdvStatus;
    }

    public org.akaza.openclinica.domain.Status getNexGenStatus() {
        return nexGenStatus;
    }

    public void setNexGenStatus(org.akaza.openclinica.domain.Status nexGenStatus) {
        this.nexGenStatus = nexGenStatus;
    }

    /**
     * @return Returns the annotations.
     */
    public String getAnnotations() {
        return annotations;
    }

    /**
     * @param annotations
     *            The annotations to set.
     */
    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    /**
     * @return Returns the completionStatusId.
     */
    public int getCompletionStatusId() {
        return completionStatusId;
    }

    /**
     * @param completionStatusId
     *            The completionStatusId to set.
     */
    public void setCompletionStatusId(int completionStatusId) {
        this.completionStatusId = completionStatusId;
    }

    /**
     * @return Returns the cRFVersionId.
     */
    public int getCRFVersionId() {
        return CRFVersionId;
    }

    /**
     * @param versionId
     *            The cRFVersionId to set.
     */
    public void setCRFVersionId(int versionId) {
        CRFVersionId = versionId;
    }

    /**
     * @return Returns the dateCompleted.
     */
    public Date getDateCompleted() {
        return dateCompleted;
    }

    /**
     * @param dateCompleted
     *            The dateCompleted to set.
     */
    public void setDateCompleted(Date dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    /**
     * @return Returns the dateInterviewed.
     */
    public Date getDateInterviewed() {
        return dateInterviewed;
    }

    /**
     * @param dateInterviewed
     *            The dateInterviewed to set.
     */
    public void setDateInterviewed(Date dateInterviewed) {
        this.dateInterviewed = dateInterviewed;
    }

    /**
     * @return Returns the dateValidate.
     */
    public Date getDateValidate() {
        return dateValidate;
    }

    /**
     * @param dateValidate
     *            The dateValidate to set.
     */
    public void setDateValidate(Date dateValidate) {
        this.dateValidate = dateValidate;
    }

    /**
     * @return Returns the dateValidateCompleted.
     */
    public Date getDateValidateCompleted() {
        return dateValidateCompleted;
    }

    /**
     * @param dateValidateCompleted
     *            The dateValidateCompleted to set.
     */
    public void setDateValidateCompleted(Date dateValidateCompleted) {
        this.dateValidateCompleted = dateValidateCompleted;
    }

    /**
     * @return Returns the interviewerName.
     */
    public String getInterviewerName() {
        return interviewerName;
    }

    /**
     * @param interviewerName
     *            The interviewerName to set.
     */
    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    /**
     * @return Returns the status.
     */
    @Override
    public Status getStatus() {
        return status;
    }

    /**
     * @deprecated
     * @return Returns the statusId.
     */
    @Deprecated
    public int getStatusId() {
        return status.getId();
    }

    /**
     * @param status
     *            The status to set.
     */
    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @deprecated
     * @param statusId
     *            The statusId to set.
     */
    @Deprecated
    public void setStatusId(int statusId) {
        status = Status.get(statusId);
    }

    /**
     * @return Returns the studyEventId.
     */
    public int getStudyEventId() {
        return studyEventId;
    }

    /**
     * @param studyEventId
     *            The studyEventId to set.
     */
    public void setStudyEventId(int studyEventId) {
        this.studyEventId = studyEventId;
    }

    /**
     * @return Returns the studySubjectId.
     */
    public int getStudySubjectId() {
        return studySubjectId;
    }

    /**
     * @param studySubjectId
     *            The studySubjectId to set.
     */
    public void setStudySubjectId(int studySubjectId) {
        this.studySubjectId = studySubjectId;
    }

    /**
     * @return Returns the validateString.
     */
    public String getValidateString() {
        return validateString;
    }

    /**
     * @param validateString
     *            The validateString to set.
     */
    public void setValidateString(String validateString) {
        this.validateString = validateString;
    }

    /**
     * @return Returns the validatorAnnotations.
     */
    public String getValidatorAnnotations() {
        return validatorAnnotations;
    }

    /**
     * @param validatorAnnotations
     *            The validatorAnnotations to set.
     */
    public void setValidatorAnnotations(String validatorAnnotations) {
        this.validatorAnnotations = validatorAnnotations;
    }

    /**
     * @return Returns the validatorId.
     */
    public int getValidatorId() {
        return validatorId;
    }

    /**
     * @param validatorId
     *            The validatorId to set.
     */
    public void setValidatorId(int validatorId) {
        this.validatorId = validatorId;
    }

    /**
     * Uses the status and created/updated dates to determine which stage the
     * Event CRF is in.
     *
     * @return The Event CRF's data entry stage.
     */
    public DataEntryStage getStage() {
        if (stage != null) {
            if (!stage.equals(DataEntryStage.INVALID)) {
                return stage;
            }
        }

        if (!active || !status.isActive()) {
            stage = DataEntryStage.UNCOMPLETED;
        }

        if (status.equals(Status.AVAILABLE)) {
            stage = DataEntryStage.INITIAL_DATA_ENTRY;
        }

        if (status.equals(Status.PENDING)) {
            if (validatorId != 0) {
                stage = DataEntryStage.DOUBLE_DATA_ENTRY;
            } else {
                stage = DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE;
            }
        }

        if (status.equals(Status.UNAVAILABLE)) {
            stage = DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE;
        }

        if (status.equals(Status.LOCKED)) {
            stage = DataEntryStage.LOCKED;
        }

        return stage;
    }

    /**
     * The problem with the above data entry stage getter is that you can never
     * set the stage back to 'invalid'.
     *
     * invalidate, allowing us to invalidate the stage again
     */
    // public DataEntryStage getStage(boolean invalidate) {
    // if (stage != null) {
    // if (invalidate) {
    // stage = DataEntryStage.INVALID;
    // return stage;
    // } else {
    // return stage;
    // }
    // }
    // return getStage();
    // }
    public void setStage(DataEntryStage stage) {
        this.stage = stage;
    }

    /**
     * @return Returns the crf.
     */
    public CRFBean getCrf() {
        return crf;
    }

    /**
     * @param crf
     *            The crf to set.
     */
    public void setCrf(CRFBean crf) {
        this.crf = crf;
    }

    /**
     * @return Returns the crfVersion.
     */
    public CRFVersionBean getCrfVersion() {
        return crfVersion;
    }

    /**
     * @param crfVersion
     *            The crfVersion to set.
     */
    public void setCrfVersion(CRFVersionBean crfVersion) {
        this.crfVersion = crfVersion;
    }

    /**
     * @return Returns the electronicSignatureStatus.
     */
    public boolean isElectronicSignatureStatus() {
        return electronicSignatureStatus;
    }

    /**
     * @param electronicSignatureStatus
     *            The electronicSignatureStatus to set.
     */
    public void setElectronicSignatureStatus(boolean electronicSignatureStatus) {
        this.electronicSignatureStatus = electronicSignatureStatus;
    }

    /**
     * @return the eventName
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * @param eventName
     *            the eventName to set
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * @return the studyName
     */
    public String getStudyName() {
        return studyName;
    }

    /**
     * @param studyName
     *            the studyName to set
     */
    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    /**
     * @return the studySubjectName
     */
    public String getStudySubjectName() {
        return studySubjectName;
    }

    /**
     * @param studySubjectName
     *            the studySubjectName to set
     */
    public void setStudySubjectName(String studySubjectName) {
        this.studySubjectName = studySubjectName;
    }

    public void setEventOrdinal(int i) {
        this.eventOrdinal = i;
    }

    public int getEventOrdinal() {
        return this.eventOrdinal;
    }

    public int getSdvUpdateId() {
        return sdvUpdateId;
    }

    public void setSdvUpdateId(int sdvUpdateId) {
        this.sdvUpdateId = sdvUpdateId;
    }

    public StudySubjectBean getStudySubject() {
        return studySubject;
    }

    public void setStudySubject(StudySubjectBean studySubject) {
        this.studySubject = studySubject;
    }

    public StudyEventBean getStudyEvent() {
        return studyEvent;
    }

    public void setStudyEvent(StudyEventBean studyEvent) {
        this.studyEvent = studyEvent;
    }

    public int getFormLayoutId() {
        return formLayoutId;
    }

    public void setFormLayoutId(int formLayoutId) {
        this.formLayoutId = formLayoutId;
    }

    public FormLayoutBean getFormLayout() {
        return formLayout;
    }

    public void setFormLayout(FormLayoutBean formLayout) {
        this.formLayout = formLayout;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + formLayoutId;
        result = prime * result + studyEventId;
        result = prime * result + studySubjectId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventCRFBean other = (EventCRFBean) obj;
        if (formLayoutId != other.formLayoutId)
            return false;
        if (studyEventId != other.studyEventId)
            return false;
        if (studySubjectId != other.studySubjectId)
            return false;
        return true;
    }

}
