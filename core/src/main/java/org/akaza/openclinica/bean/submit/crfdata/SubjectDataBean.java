package org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;

public class SubjectDataBean {
    private ArrayList<StudyEventDataBean> studyEventData;
    private String subjectOID;
    
    public SubjectDataBean() {
        studyEventData = new ArrayList<StudyEventDataBean>();
    }

    public String getSubjectOID() {
        return subjectOID;
    }

    public void setSubjectOID(String subjectOID) {
        this.subjectOID = subjectOID;
    }

    public ArrayList<StudyEventDataBean> getStudyEventData() {
        return studyEventData;
    }

    public void setStudyEventData(ArrayList<StudyEventDataBean> studyEventData) {
        this.studyEventData = studyEventData;
    }
}
