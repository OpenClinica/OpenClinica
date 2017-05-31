package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.domain.xform.XformContainer;
import org.akaza.openclinica.service.dto.Form;
import org.akaza.openclinica.service.dto.FormVersion;
import org.springframework.validation.Errors;

public class CrfMetaDataObject {
    public Form crf;
    public FormVersion version;
    public XformContainer container;
    public StudyBean currentStudy;
    public UserAccountBean ub;
    public Errors errors;
    public String formLayoutUrl;

    public CrfMetaDataObject(Form crf, FormVersion version, XformContainer container, StudyBean currentStudy, UserAccountBean ub, Errors errors,
            String formLayoutUrl) {
        this.crf = crf;
        this.version = version;
        this.container = container;
        this.currentStudy = currentStudy;
        this.ub = ub;
        this.formLayoutUrl = formLayoutUrl;
        this.errors = errors;
    }

}