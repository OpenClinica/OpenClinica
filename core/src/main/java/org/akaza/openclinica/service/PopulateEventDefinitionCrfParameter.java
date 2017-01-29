package org.akaza.openclinica.service;

import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.user.UserAccount;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionFormRef;
import org.openclinica.ns.odm_ext_v130.v31_sb.OCodmComplexTypeDefinitionConfigurationParameters;

public class PopulateEventDefinitionCrfParameter {
    private EventDefinitionCrf eventDefinitionCrf;
    private UserAccount userAccount;
    private CrfBean crf;
    private CrfVersion crfVersion;
    private FormLayout formLayout;
    private OCodmComplexTypeDefinitionConfigurationParameters conf;
    private Study study;
    private StudyEventDefinition studyEventDefinition;
    private ODMcomplexTypeDefinitionFormRef odmFormRef;

    public PopulateEventDefinitionCrfParameter(PopulateEventDefinitionCrfParameter paramObj) {
        this.eventDefinitionCrf = paramObj.eventDefinitionCrf;
        this.userAccount = paramObj.userAccount;
        this.crf = paramObj.crf;
        this.crfVersion = paramObj.crfVersion;
        this.conf = paramObj.conf;
        this.study = paramObj.study;
        this.studyEventDefinition = paramObj.studyEventDefinition;
        this.odmFormRef = paramObj.odmFormRef;
        this.formLayout = paramObj.formLayout;
    }

    public PopulateEventDefinitionCrfParameter() {
        // TODO Auto-generated constructor stub
    }

    public EventDefinitionCrf getEventDefinitionCrf() {
        return eventDefinitionCrf;
    }

    public void setEventDefinitionCrf(EventDefinitionCrf eventDefinitionCrf) {
        this.eventDefinitionCrf = eventDefinitionCrf;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public CrfBean getCrf() {
        return crf;
    }

    public void setCrf(CrfBean crf) {
        this.crf = crf;
    }

    public CrfVersion getCrfVersion() {
        return crfVersion;
    }

    public void setCrfVersion(CrfVersion crfVersion) {
        this.crfVersion = crfVersion;
    }

    public OCodmComplexTypeDefinitionConfigurationParameters getConf() {
        return conf;
    }

    public void setConf(OCodmComplexTypeDefinitionConfigurationParameters conf) {
        this.conf = conf;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public StudyEventDefinition getStudyEventDefinition() {
        return studyEventDefinition;
    }

    public void setStudyEventDefinition(StudyEventDefinition studyEventDefinition) {
        this.studyEventDefinition = studyEventDefinition;
    }

    public ODMcomplexTypeDefinitionFormRef getOdmFormRef() {
        return odmFormRef;
    }

    public void setOdmFormRef(ODMcomplexTypeDefinitionFormRef odmFormRef) {
        this.odmFormRef = odmFormRef;
    }

    public FormLayout getFormLayout() {
        return formLayout;
    }

    public void setFormLayout(FormLayout formLayout) {
        this.formLayout = formLayout;
    }

}