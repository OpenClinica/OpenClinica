package org.akaza.openclinica.controller.openrosa;

import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static org.akaza.openclinica.controller.openrosa.SubmissionProcessorChain.ProcessorEnum;

public class SubmissionContainer {
    private String requestBody = null;
    private HashMap<String, String> subjectContext = null;
    private Study study = null;
    private StudyEvent studyEvent = null;
    private StudySubject subject = null;
    private UserAccount user = null;
    private EventCrf eventCrf = null;
    private CrfVersion crfVersion = null;
    private List<ItemData> items = null;
    private Errors errors = null;
    private Locale locale = null;
    private ArrayList<HashMap> listOfUploadFilePaths;
    private ProcessorEnum processorEnum;
    private boolean fieldSubmissionFlag;
    public enum FieldRequestTypeEnum {EDIT_FIELD, DELETE_FIELD, NEW_FIELD, FORM_FIELD};

    public FieldRequestTypeEnum getRequestType() {
        return requestType;
    }

    public void setRequestType(FieldRequestTypeEnum requestType) {
        this.requestType = requestType;
    }

    private FieldRequestTypeEnum requestType;

    public SubmissionContainer(Study study, String requestBody, HashMap<String, String> subjectContext, Errors errors,
            Locale locale,ArrayList<HashMap> listOfUploadFilePaths, FieldRequestTypeEnum requestType) {
        this.study = study;
        this.requestBody = requestBody;
        this.subjectContext = subjectContext;
        this.errors = errors;
        this.locale = locale;
        this.listOfUploadFilePaths=listOfUploadFilePaths;
        this.requestType = requestType;
    }

    public CrfVersion getCrfVersion() {
        return crfVersion;
    }

    public void setCrfVersion(CrfVersion crfVersion) {
        this.crfVersion = crfVersion;
    }

    public boolean isFieldSubmissionFlag() {
        return fieldSubmissionFlag;
    }

    public void setFieldSubmissionFlag(boolean fieldSubmissionFlag) {
        this.fieldSubmissionFlag = fieldSubmissionFlag;
    }

    public ProcessorEnum getProcessorEnum() {
        return processorEnum;
    }

    public void setProcessorEnum(ProcessorEnum processorEnum) {
        this.processorEnum = processorEnum;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public HashMap<String, String> getSubjectContext() {
        return subjectContext;
    }

    public void setSubjectContext(HashMap<String, String> subjectContext) {
        this.subjectContext = subjectContext;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public StudyEvent getStudyEvent() {
        return studyEvent;
    }

    public void setStudyEvent(StudyEvent studyEvent) {
        this.studyEvent = studyEvent;
    }

    public StudySubject getSubject() {
        return subject;
    }

    public void setSubject(StudySubject subject) {
        this.subject = subject;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public EventCrf getEventCrf() {
        return eventCrf;
    }

    public void setEventCrf(EventCrf eventCrf) {
        this.eventCrf = eventCrf;
    }

    public List<ItemData> getItems() {
        return items;
    }

    public void setItems(List<ItemData> items) {
        this.items = items;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public ArrayList<HashMap> getListOfUploadFilePaths() {
        return listOfUploadFilePaths;
    }

    public void setListOfUploadFilePaths(ArrayList<HashMap> listOfUploadFilePaths) {
        this.listOfUploadFilePaths = listOfUploadFilePaths;
    }

}
