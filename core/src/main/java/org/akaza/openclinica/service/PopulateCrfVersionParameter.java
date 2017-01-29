package org.akaza.openclinica.service;

import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.service.dto.Crf;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionFormDef;

public class PopulateCrfVersionParameter {
    private ODMcomplexTypeDefinitionFormDef odmFormDef;
    private UserAccount userAccount;
    private CrfVersion crfVersion;
    private FormLayout formLayout;
    private CrfBean crf;
    private String url;
    private Crf[] fmCrfs;

    public PopulateCrfVersionParameter(PopulateCrfVersionParameter paramObj) {
        this.odmFormDef = paramObj.odmFormDef;
        this.userAccount = paramObj.userAccount;
        this.crfVersion = paramObj.crfVersion;
        this.crf = paramObj.crf;
        this.url = paramObj.url;
        this.fmCrfs = paramObj.fmCrfs;
        this.formLayout = paramObj.formLayout;
    }

    public PopulateCrfVersionParameter() {
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

    public Crf[] getFmCrfs() {
        return fmCrfs;
    }

    public void setFmCrfs(Crf[] fmCrfs) {
        this.fmCrfs = fmCrfs;
    }

    public FormLayout getFormLayout() {
        return formLayout;
    }

    public void setFormLayout(FormLayout formLayout) {
        this.formLayout = formLayout;
    }

}