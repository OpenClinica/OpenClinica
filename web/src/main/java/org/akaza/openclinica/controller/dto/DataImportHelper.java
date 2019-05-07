package org.akaza.openclinica.controller.dto;

import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;

import java.util.List;

public class DataImportHelper {

    List<DataImportReport> dataImportReports;
    DataImportReport dataImportReport;
    UserAccount userAccount;

    StudySubject studySubject;
    StudyEventDefinition studyEventDefinition;
    StudyEvent studyEvent;
    CrfBean crf;
    EventCrf eventCrf;
    FormLayout formLayout;
    ImportItemGroupDataBean itemGroupDataBean;
    ItemGroup itemGroup;

    String studyEventRepeatKey;
    String itemGroupRepeatKey;
    String fileName;

    String studyOid;
    String subjectKey;
    String studyEventOID;
    String formOID;
    String formLayoutOID;
    String itemGroupOID;
    String itemOID;
    String formStatus;
    JobDetail jobDetail;



    public List<DataImportReport> getDataImportReports() {
        return dataImportReports;
    }

    public void setDataImportReports(List<DataImportReport> dataImportReports) {
        this.dataImportReports = dataImportReports;
    }

    public DataImportReport getDataImportReport() {
        return dataImportReport;
    }

    public void setDataImportReport(DataImportReport dataImportReport) {
        this.dataImportReport = dataImportReport;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public StudySubject getStudySubject() {
        return studySubject;
    }

    public void setStudySubject(StudySubject studySubject) {
        this.studySubject = studySubject;
    }

    public StudyEventDefinition getStudyEventDefinition() {
        return studyEventDefinition;
    }

    public void setStudyEventDefinition(StudyEventDefinition studyEventDefinition) {
        this.studyEventDefinition = studyEventDefinition;
    }

    public CrfBean getCrf() {
        return crf;
    }

    public void setCrf(CrfBean crf) {
        this.crf = crf;
    }

    public FormLayout getFormLayout() {
        return formLayout;
    }

    public void setFormLayout(FormLayout formLayout) {
        this.formLayout = formLayout;
    }

    public ImportItemGroupDataBean getItemGroupDataBean() {
        return itemGroupDataBean;
    }

    public void setItemGroupDataBean(ImportItemGroupDataBean itemGroupDataBean) {
        this.itemGroupDataBean = itemGroupDataBean;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStudyOid() {
        return studyOid;
    }

    public void setStudyOid(String studyOid) {
        this.studyOid = studyOid;
    }

    public String getStudyEventOID() {
        return studyEventOID;
    }

    public void setStudyEventOID(String studyEventOID) {
        this.studyEventOID = studyEventOID;
    }

    public String getFormStatus() {
        return formStatus;
    }

    public void setFormStatus(String formStatus) {
        this.formStatus = formStatus;
    }

    public JobDetail getJobDetail() {
        return jobDetail;
    }

    public void setJobDetail(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
    }

    public StudyEvent getStudyEvent() {
        return studyEvent;
    }

    public void setStudyEvent(StudyEvent studyEvent) {
        this.studyEvent = studyEvent;
    }

    public EventCrf getEventCrf() {
        return eventCrf;
    }

    public void setEventCrf(EventCrf eventCrf) {
        this.eventCrf = eventCrf;
    }

    public ItemGroup getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(ItemGroup itemGroup) {
        this.itemGroup = itemGroup;
    }

    public String getSubjectKey() {
        return subjectKey;
    }

    public void setSubjectKey(String subjectKey) {
        this.subjectKey = subjectKey;
    }

    public String getStudyEventRepeatKey() {
        return studyEventRepeatKey;
    }

    public void setStudyEventRepeatKey(String studyEventRepeatKey) {
        this.studyEventRepeatKey = studyEventRepeatKey;
    }

    public String getItemGroupRepeatKey() {
        return itemGroupRepeatKey;
    }

    public void setItemGroupRepeatKey(String itemGroupRepeatKey) {
        this.itemGroupRepeatKey = itemGroupRepeatKey;
    }

    public String getFormOID() {
        return formOID;
    }

    public void setFormOID(String formOID) {
        this.formOID = formOID;
    }

    public String getFormLayoutOID() {
        return formLayoutOID;
    }

    public void setFormLayoutOID(String formLayoutOID) {
        this.formLayoutOID = formLayoutOID;
    }

    public String getItemGroupOID() {
        return itemGroupOID;
    }

    public void setItemGroupOID(String itemGroupOID) {
        this.itemGroupOID = itemGroupOID;
    }

    public String getItemOID() {
        return itemOID;
    }

    public void setItemOID(String itemOID) {
        this.itemOID = itemOID;
    }
}
