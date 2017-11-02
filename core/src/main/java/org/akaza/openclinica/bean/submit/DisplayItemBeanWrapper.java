package org.akaza.openclinica.bean.submit;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Display Item Bean Wrapper, code to generate a front end view of errors
 * generated during Data Import.
 * 
 * @author kkrumlian, thickerson
 * 
 */
public class DisplayItemBeanWrapper {
    boolean isOverwrite = false;
    boolean isSavable = false;
    List<DisplayItemBean> displayItemBeans;
    HashMap validationErrors;
    // Values to display on the jsp
    String studyEventId;
    String crfVersionId;
    //
    String studyEventName;
    String studySubjectName;
    Date dateOfEvent;
    String nameOfEvent;
    String crfName;
    String crfVersionName;
    String studySubjectOid;
    String studyEventRepeatKey;
    EventCRFBean eventCrfBean;

    // need to add here
    // study_subject_id, date_of_event, name_of_event, crf_name and version

    public DisplayItemBeanWrapper(List<DisplayItemBean> displayItemBeans, boolean isSavable, boolean isOverwrite, HashMap validationErrors, String studyEventId,
            String crfVersionId, String studyEventName, String studySubjectName, Date dateOfEvent, String crfName, String crfVersionName,
            String studySubjectOid, String studyEventRepeatKey, EventCRFBean eventCrfBean) {
        this.isSavable = isSavable;
        this.isOverwrite = isOverwrite;
        this.displayItemBeans = displayItemBeans;
        this.validationErrors = validationErrors;
        this.studyEventId = studyEventId;
        this.crfVersionId = crfVersionId;
        this.studyEventName = studyEventName;
        this.studySubjectName = studySubjectName;
        this.dateOfEvent = dateOfEvent;
        this.crfName = crfName;
        this.crfVersionName = crfVersionName;
        this.studySubjectOid = studySubjectOid;
        this.studyEventRepeatKey = studyEventRepeatKey;
        this.eventCrfBean = eventCrfBean;
    }

    public HashMap getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(HashMap validationErrors) {
        this.validationErrors = validationErrors;
    }

    public List<DisplayItemBean> getDisplayItemBeans() {
        return displayItemBeans;
    }

    public boolean isSavable() {
        return isSavable;
    }

    public void setSavable(boolean isSavable) {
        this.isSavable = isSavable;
    }

    public String getStudySubjectOid() {
        return studySubjectOid;
    }

    public void setStudySubjectOid(String studySubjectOid) {
        this.studySubjectOid = studySubjectOid;
    }

    public String getCrfVersionId() {
        return crfVersionId;
    }

    public void setCrfVersionId(String crfVersionId) {
        this.crfVersionId = crfVersionId;
    }

    public String getStudyEventId() {
        return studyEventId;
    }

    public void setStudyEventId(String studyEventId) {
        this.studyEventId = studyEventId;
    }

    public String getStudyEventName() {
        return studyEventName;
    }

    public void setStudyEventName(String studyEventName) {
        this.studyEventName = studyEventName;
    }

    public String getStudySubjectName() {
        return studySubjectName;
    }

    public void setStudySubjectName(String studySubjectName) {
        this.studySubjectName = studySubjectName;
    }

    public Date getDateOfEvent() {
        return dateOfEvent;
    }

    public void setDateOfEvent(Date dateOfEvent) {
        this.dateOfEvent = dateOfEvent;
    }

    public String getNameOfEvent() {
        return nameOfEvent;
    }

    public void setNameOfEvent(String nameOfEvent) {
        this.nameOfEvent = nameOfEvent;
    }

    public String getCrfName() {
        return crfName;
    }

    public void setCrfName(String crfName) {
        this.crfName = crfName;
    }

    public String getCrfVersionName() {
        return crfVersionName;
    }

    public void setCrfVersionName(String crfVersionName) {
        this.crfVersionName = crfVersionName;
    }

    public boolean isOverwrite() {
        return isOverwrite;
    }

    public void setOverwrite(boolean isOverwrite) {
        this.isOverwrite = isOverwrite;
    }

    public String getStudyEventRepeatKey() {
        return studyEventRepeatKey;
    }

    public void setStudyEventRepeatKey(String studyEventRepeatKey) {
        this.studyEventRepeatKey = studyEventRepeatKey;
    }

    public EventCRFBean getEventCrfBean() {
        return eventCrfBean;
    }

    public void setEventCrfBean(EventCRFBean eventCrfBean) {
        this.eventCrfBean = eventCrfBean;
    }

}