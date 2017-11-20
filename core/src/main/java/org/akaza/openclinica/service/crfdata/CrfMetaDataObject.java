package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.domain.xform.XformContainer;
import org.akaza.openclinica.service.dto.Form;
import org.akaza.openclinica.service.dto.FormVersion;
import org.springframework.validation.Errors;

public class CrfMetaDataObject {
    public Form crf;
    public FormVersion version;
    public XformContainer container;
    public Study study;
    public UserAccount ub;
    public Errors errors;
    public String formLayoutUrl;

    public CrfMetaDataObject(Form crf, FormVersion version, XformContainer container, Study study, UserAccount ub, Errors errors, String formLayoutUrl) {
        this.crf = crf;
        this.version = version;
        this.container = container;
        this.study = study;
        this.ub = ub;
        this.formLayoutUrl = formLayoutUrl;
        this.errors = errors;
    }

}