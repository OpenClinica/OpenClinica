/**
 *
 */
package org.akaza.openclinica.controller.dto;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;

/**
 * @author joekeremian
 *
 */
public class CommonEventContainerDTO {

    // Hibernate objects
    private FormLayout formLayout;
    private StudySubject studySubject;
    private StudyEventDefinition studyEventDefinition;
    private UserAccount userAccount;
    private EventCrf eventCrf;
    // JDBC objects
    private FormLayoutBean formLayoutBean;
    private StudySubjectBean studySubjectBean;
    private StudyEventDefinitionBean studyEventDefinitionBean;
    private UserAccountBean userAccountBean;
    private EventCRFBean eventCRFBean;
    // ids
    private int eventCrfId;
    private Integer maxOrdinal;


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

    public FormLayoutBean getFormLayoutBean() {
        return formLayoutBean;
    }

    public void setFormLayoutBean(FormLayoutBean formLayoutBean) {
        this.formLayoutBean = formLayoutBean;
    }

    public StudySubjectBean getStudySubjectBean() {
        return studySubjectBean;
    }

    public void setStudySubjectBean(StudySubjectBean studySubjectBean) {
        this.studySubjectBean = studySubjectBean;
    }

    public StudyEventDefinitionBean getStudyEventDefinitionBean() {
        return studyEventDefinitionBean;
    }

    public void setStudyEventDefinitionBean(StudyEventDefinitionBean studyEventDefinitionBean) {
        this.studyEventDefinitionBean = studyEventDefinitionBean;
    }

    public UserAccountBean getUserAccountBean() {
        return userAccountBean;
    }

    public void setUserAccountBean(UserAccountBean userAccountBean) {
        this.userAccountBean = userAccountBean;
    }

    public EventCRFBean getEventCRFBean() {
        return eventCRFBean;
    }

    public void setEventCRFBean(EventCRFBean eventCRFBean) {
        this.eventCRFBean = eventCRFBean;
    }
}
