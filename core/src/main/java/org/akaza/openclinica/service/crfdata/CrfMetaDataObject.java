package org.akaza.openclinica.service.crfdata;

import java.util.List;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.domain.xform.XformContainer;
import org.akaza.openclinica.domain.xform.dto.Html;
import org.akaza.openclinica.service.dto.Crf;
import org.akaza.openclinica.service.dto.Version;
import org.apache.commons.fileupload.FileItem;
import org.springframework.validation.Errors;

public class CrfMetaDataObject {
    public Crf crf;
    public Version version;
    public XformContainer container;
    public StudyBean currentStudy;
    public UserAccountBean ub;
    public Html html;
    public String xform;
    public List<FileItem> formItems;
    public Errors errors;
    public String formLayoutUrl;
    public String crfName;
    public String crfDescription;

    public CrfMetaDataObject(Crf crf, Version version, XformContainer container, StudyBean currentStudy, UserAccountBean ub, Html html, String xform,
            List<FileItem> formItems, Errors errors, String fromLayoutUrl, String crfName, String crfDescription) {
        this.crf = crf;
        this.version = version;
        this.container = container;
        this.currentStudy = currentStudy;
        this.ub = ub;
        this.html = html;
        this.xform = xform;
        this.formItems = formItems;
        this.errors = errors;
        this.formLayoutUrl = fromLayoutUrl;
        this.crfName = crfName;
        this.crfDescription = crfDescription;
    }
}