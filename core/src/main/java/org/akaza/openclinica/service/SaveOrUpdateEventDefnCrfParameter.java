package org.akaza.openclinica.service;

import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.user.UserAccount;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionFormRef;
import org.openclinica.ns.odm_ext_v130.v31_sb.OCodmComplexTypeDefinitionConfigurationParameters;

public class SaveOrUpdateEventDefnCrfParameter {
    private UserAccount userAccount;
    private Study study;
    private StudyEventDefinition studyEventDefinition;
    private CrfBean crf;
    private CrfVersion crfVersion;
    private EventDefinitionCrf eventDefinitionCrf;
    private OCodmComplexTypeDefinitionConfigurationParameters conf;
    private ODMcomplexTypeDefinitionFormRef odmFormRef;

    public SaveOrUpdateEventDefnCrfParameter(UserAccount userAccount, Study study, StudyEventDefinition studyEventDefinition, CrfBean crf,
            CrfVersion crfVersion, EventDefinitionCrf eventDefinitionCrf, OCodmComplexTypeDefinitionConfigurationParameters conf,
            ODMcomplexTypeDefinitionFormRef odmFormRef) {
        this.userAccount = userAccount;
        this.study = study;
        this.studyEventDefinition = studyEventDefinition;
        this.crf = crf;
        this.crfVersion = crfVersion;
        this.eventDefinitionCrf = eventDefinitionCrf;
        this.conf = conf;
        this.odmFormRef = odmFormRef;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
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

    public EventDefinitionCrf getEventDefinitionCrf() {
        return eventDefinitionCrf;
    }

    public void setEventDefinitionCrf(EventDefinitionCrf eventDefinitionCrf) {
        this.eventDefinitionCrf = eventDefinitionCrf;
    }

    public OCodmComplexTypeDefinitionConfigurationParameters getConf() {
        return conf;
    }

    public void setConf(OCodmComplexTypeDefinitionConfigurationParameters conf) {
        this.conf = conf;
    }

    public ODMcomplexTypeDefinitionFormRef getOdmFormRef() {
        return odmFormRef;
    }

    public void setOdmFormRef(ODMcomplexTypeDefinitionFormRef odmFormRef) {
        this.odmFormRef = odmFormRef;
    }
}