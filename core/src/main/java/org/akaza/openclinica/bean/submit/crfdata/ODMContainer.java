package org.akaza.openclinica.bean.submit.crfdata;

/**
 * ODM Container, the surrounding tag for Clinical Data together with meta data
 * 
 * @author thickerson, 04/2008
 * 
 */
public class ODMContainer {

    private CRFDataPostImportContainer crfDataPostImportContainer;
    private String subjectUniqueIdentifier;
    private String studyUniqueIdentifier;

    public CRFDataPostImportContainer getCrfDataPostImportContainer() {
        return crfDataPostImportContainer;
    }

    public void setCrfDataPostImportContainer(CRFDataPostImportContainer crfDataPostImportContainer) {
        this.crfDataPostImportContainer = crfDataPostImportContainer;
    }

    public String getSubjectUniqueIdentifier() {
        return subjectUniqueIdentifier;
    }

    public void setSubjectUniqueIdentifier(String subjectUniqueIdentifier) {
        this.subjectUniqueIdentifier = subjectUniqueIdentifier;
    }

    public String getStudyUniqueIdentifier() {
        return studyUniqueIdentifier;
    }

    public void setStudyUniqueIdentifier(String studyUniqueIdentifier) {
        this.studyUniqueIdentifier = studyUniqueIdentifier;
    }

}
