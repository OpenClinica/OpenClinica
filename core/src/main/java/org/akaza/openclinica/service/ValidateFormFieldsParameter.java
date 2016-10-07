package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.springframework.validation.Errors;

public class ValidateFormFieldsParameter {
    private Errors errors;
    private CRFVersionBean version;
    private String submittedCrfName;
    private String submittedCrfVersionName;
    private String submittedCrfVersionDescription;
    private String submittedRevisionNotes;
    private String submittedXformText;

    public ValidateFormFieldsParameter(Errors errors, CRFVersionBean version, String submittedCrfName, String submittedCrfVersionName,
            String submittedCrfVersionDescription, String submittedRevisionNotes, String submittedXformText) {
        this.errors = errors;
        this.version = version;
        this.submittedCrfName = submittedCrfName;
        this.submittedCrfVersionName = submittedCrfVersionName;
        this.submittedCrfVersionDescription = submittedCrfVersionDescription;
        this.submittedRevisionNotes = submittedRevisionNotes;
        this.submittedXformText = submittedXformText;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }

    public CRFVersionBean getVersion() {
        return version;
    }

    public void setVersion(CRFVersionBean version) {
        this.version = version;
    }

    public String getSubmittedCrfName() {
        return submittedCrfName;
    }

    public void setSubmittedCrfName(String submittedCrfName) {
        this.submittedCrfName = submittedCrfName;
    }

    public String getSubmittedCrfVersionName() {
        return submittedCrfVersionName;
    }

    public void setSubmittedCrfVersionName(String submittedCrfVersionName) {
        this.submittedCrfVersionName = submittedCrfVersionName;
    }

    public String getSubmittedCrfVersionDescription() {
        return submittedCrfVersionDescription;
    }

    public void setSubmittedCrfVersionDescription(String submittedCrfVersionDescription) {
        this.submittedCrfVersionDescription = submittedCrfVersionDescription;
    }

    public String getSubmittedRevisionNotes() {
        return submittedRevisionNotes;
    }

    public void setSubmittedRevisionNotes(String submittedRevisionNotes) {
        this.submittedRevisionNotes = submittedRevisionNotes;
    }

    public String getSubmittedXformText() {
        return submittedXformText;
    }

    public void setSubmittedXformText(String submittedXformText) {
        this.submittedXformText = submittedXformText;
    }
}