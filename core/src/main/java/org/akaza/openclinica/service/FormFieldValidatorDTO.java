package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.springframework.validation.Errors;

public class FormFieldValidatorDTO {
    private Errors errors;
    private CRFVersionBean version;
    private FormLayoutBean formLayoutBean;
    private String submittedCrfName;
    private String submittedCrfVersionName;
    private String submittedCrfVersionDescription;
    private String submittedRevisionNotes;
    private String submittedXformText;

    public FormFieldValidatorDTO(FormFieldValidatorDTO formFieldValidatorObj) {
        this.errors = formFieldValidatorObj.errors;
        this.version = formFieldValidatorObj.version;
        this.submittedCrfName = formFieldValidatorObj.submittedCrfName;
        this.submittedCrfVersionName = formFieldValidatorObj.submittedCrfVersionName;
        this.submittedCrfVersionDescription = formFieldValidatorObj.submittedCrfVersionDescription;
        this.submittedRevisionNotes = formFieldValidatorObj.submittedRevisionNotes;
        this.submittedXformText = formFieldValidatorObj.submittedXformText;
    }

    public FormFieldValidatorDTO() {
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

    public FormLayoutBean getFormLayoutBean() {
        return formLayoutBean;
    }

    public void setFormLayoutBean(FormLayoutBean formLayoutBean) {
        this.formLayoutBean = formLayoutBean;
    }

}