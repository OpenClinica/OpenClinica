/**
 * 
 */
package org.akaza.openclinica.controller.dto;

import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;

/**
 * @author joekeremian
 *
 */
public class CommonEventContainerDTO {

    private FormLayout formLayout;
    private StudySubject studySubject;
    private int eventCrfId;
    private StudyEventDefinition studyEventDefinition;
    private Integer maxOrdinal;
    private UserAccount userAccount;
    private EventCrf eventCrf;



    public StudySubject getStudySubject() {
        return studySubject;
    }

    public void setStudySubject(StudySubject studySubject) {
        this.studySubject = studySubject;
    }


    public int getEventCrfId() {
        return eventCrfId;
    }

    public void setEventCrfId(int eventCrfId) {
        this.eventCrfId = eventCrfId;
    }

    public FormLayout getFormLayout() {
        return formLayout;
    }

    public void setFormLayout(FormLayout formLayout) {
        this.formLayout = formLayout;
    }

    public StudyEventDefinition getStudyEventDefinition() {
        return studyEventDefinition;
    }

    public void setStudyEventDefinition(StudyEventDefinition studyEventDefinition) {
        this.studyEventDefinition = studyEventDefinition;
    }

    public Integer getMaxOrdinal() {
        return maxOrdinal;
    }

    public void setMaxOrdinal(Integer maxOrdinal) {
        this.maxOrdinal = maxOrdinal;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public EventCrf getEventCrf() {
        return eventCrf;
    }

    public void setEventCrf(EventCrf eventCrf) {
        this.eventCrf = eventCrf;
    }
}
