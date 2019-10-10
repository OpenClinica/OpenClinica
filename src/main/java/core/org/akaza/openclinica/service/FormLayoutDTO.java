package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.domain.datamap.CrfBean;
import core.org.akaza.openclinica.domain.datamap.CrfVersion;
import core.org.akaza.openclinica.domain.datamap.FormLayout;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.service.dto.Form;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionFormDef;

public class FormLayoutDTO {
    private ODMcomplexTypeDefinitionFormDef odmFormDef;
    private UserAccount userAccount;
    private CrfVersion crfVersion;
    private FormLayout formLayout;
    private CrfBean crf;
    private String url;
    private Form[] fmCrfs;

    public FormLayoutDTO(FormLayoutDTO formLayoutObj) {
        this.odmFormDef = formLayoutObj.odmFormDef;
        this.userAccount = formLayoutObj.userAccount;
        this.crfVersion = formLayoutObj.crfVersion;
        this.crf = formLayoutObj.crf;
        this.url = formLayoutObj.url;
        this.fmCrfs = formLayoutObj.fmCrfs;
        this.formLayout = formLayoutObj.formLayout;
    }

    public FormLayoutDTO() {
        // TODO Auto-generated constructor stub
    }

    public ODMcomplexTypeDefinitionFormDef getOdmFormDef() {
        return odmFormDef;
    }

    public void setOdmFormDef(ODMcomplexTypeDefinitionFormDef odmFormDef) {
        this.odmFormDef = odmFormDef;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public CrfVersion getCrfVersion() {
        return crfVersion;
    }

    public void setCrfVersion(CrfVersion crfVersion) {
        this.crfVersion = crfVersion;
    }

    public CrfBean getCrf() {
        return crf;
    }

    public void setCrf(CrfBean crf) {
        this.crf = crf;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Form[] getFmCrfs() {
        return fmCrfs;
    }

    public void setFmCrfs(Form[] fmCrfs) {
        this.fmCrfs = fmCrfs;
    }

    public FormLayout getFormLayout() {
        return formLayout;
    }

    public void setFormLayout(FormLayout formLayout) {
        this.formLayout = formLayout;
    }

}