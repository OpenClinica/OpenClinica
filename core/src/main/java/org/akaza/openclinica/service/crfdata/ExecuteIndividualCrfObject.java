package org.akaza.openclinica.service.crfdata;

import java.util.List;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.service.dto.Crf;
import org.apache.commons.fileupload.FileItem;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionFormLayoutDef;
import org.springframework.validation.Errors;

public class ExecuteIndividualCrfObject {
    public Crf crf;
    public List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs;
    public Errors errors;
    public List<FileItem> items;
    public StudyBean currentStudy;
    public UserAccountBean ub;
    public boolean odmImport;
    public String crfName;
    public String crfDescription;

    public ExecuteIndividualCrfObject(Crf crf, List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs, Errors errors, List<FileItem> items,
            StudyBean currentStudy, UserAccountBean ub, boolean odmImport, String crfName, String crfDescription) {
        this.crf = crf;
        this.formLayoutDefs = formLayoutDefs;
        this.errors = errors;
        this.items = items;
        this.currentStudy = currentStudy;
        this.ub = ub;
        this.odmImport = odmImport;
        this.crfName = crfName;
        this.crfDescription = crfDescription;
    }
}