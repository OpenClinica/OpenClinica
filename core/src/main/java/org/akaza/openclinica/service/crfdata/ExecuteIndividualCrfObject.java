package org.akaza.openclinica.service.crfdata;

import java.util.List;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.domain.xform.XformContainer;
import org.akaza.openclinica.service.dto.Form;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionFormLayoutDef;
import org.springframework.validation.Errors;

public class ExecuteIndividualCrfObject {
    public Form form;
    public List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs;
    public Errors errors;
    public StudyBean currentStudy;
    public UserAccountBean ub;
    public boolean odmImport;
    public XformContainer container;

    public ExecuteIndividualCrfObject(Form form, List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs, Errors errors, StudyBean currentStudy,
            UserAccountBean ub, boolean odmImport, XformContainer container) {
        this.form = form;
        this.formLayoutDefs = formLayoutDefs;
        this.errors = errors;
        this.currentStudy = currentStudy;
        this.ub = ub;
        this.odmImport = odmImport;
        this.container = container;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public List<OCodmComplexTypeDefinitionFormLayoutDef> getFormLayoutDefs() {
        return formLayoutDefs;
    }

    public void setFormLayoutDefs(List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs) {
        this.formLayoutDefs = formLayoutDefs;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }

    public StudyBean getCurrentStudy() {
        return currentStudy;
    }

    public void setCurrentStudy(StudyBean currentStudy) {
        this.currentStudy = currentStudy;
    }

    public UserAccountBean getUb() {
        return ub;
    }

    public void setUb(UserAccountBean ub) {
        this.ub = ub;
    }

    public boolean isOdmImport() {
        return odmImport;
    }

    public void setOdmImport(boolean odmImport) {
        this.odmImport = odmImport;
    }

    public XformContainer getContainer() {
        return container;
    }

    public void setContainer(XformContainer container) {
        this.container = container;
    }

}