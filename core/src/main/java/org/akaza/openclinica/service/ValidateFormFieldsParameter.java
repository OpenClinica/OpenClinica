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

    public ValidateFormFieldsParameter(ValidateFormFieldsParameter validateFormFieldsParameter) {
        this.errors = validateFormFieldsParameter.errors;
        this.version = validateFormFieldsParameter.version;
        this.submittedCrfName = validateFormFieldsParameter.submittedCrfName;
        this.submittedCrfVersionName = validateFormFieldsParameter.submittedCrfVersionName;
        this.submittedCrfVersionDescription = validateFormFieldsParameter.submittedCrfVersionDescription;
        this.submittedRevisionNotes = validateFormFieldsParameter.submittedRevisionNotes;
        this.submittedXformText = validateFormFieldsParameter.submittedXformText;
    }

    public ValidateFormFieldsParameter() {
        // TODO Auto-generated constructor stub
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