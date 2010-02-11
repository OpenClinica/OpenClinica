package org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;

public class StudyEventDataBean {
    private ArrayList<FormDataBean> formData;
    private String studyEventOID;
    private String studyEventRepeatKey = "1";// to prevent nulls, tbh 01/2010
    
    public StudyEventDataBean() {
        formData = new ArrayList<FormDataBean>();
    }

    public String getStudyEventRepeatKey() {
        return studyEventRepeatKey;
    }

    public void setStudyEventRepeatKey(String studyEventRepeatKey) {
        this.studyEventRepeatKey = studyEventRepeatKey;
    }

    public String getStudyEventOID() {
        return studyEventOID;
    }

    public void setStudyEventOID(String studyEventOID) {
        this.studyEventOID = studyEventOID;
    }

    public ArrayList<FormDataBean> getFormData() {
        return formData;
    }

    public void setFormData(ArrayList<FormDataBean> formData) {
        this.formData = formData;
    }
}
