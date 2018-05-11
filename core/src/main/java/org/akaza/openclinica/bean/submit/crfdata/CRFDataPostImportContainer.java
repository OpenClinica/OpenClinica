package org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;

/**
 * CRFDataPostImportContainer, meant to serve as the 'ClinicalData' tag in CRF Data Import. Will contain the following:
 * -- SubjectData -- StudyEventData -- FormData -- ItemGroupData -- ItemData Note that each list will have 1 to n
 * elements, and each element is contained inside its parent element.
 * 
 * @author thickerson, 04/2008
 * 
 */
public class CRFDataPostImportContainer {

    private ArrayList<SubjectDataBean> subjectData;
    private String studyOID;
    private UpsertOnBean upsertOn;

    public String getStudyOID() {
        return studyOID;
    }

    public void setStudyOID(String studyOID) {
        this.studyOID = studyOID;
    }

    public ArrayList<SubjectDataBean> getSubjectData() {
        return subjectData;
    }

    public void setSubjectData(ArrayList<SubjectDataBean> subjectData) {
        this.subjectData = subjectData;
    }

    public UpsertOnBean getUpsertOn() {
        return upsertOn;
    }

    public void setUpsertOn(UpsertOnBean upsertOn) {
        this.upsertOn = upsertOn;
    }

}
