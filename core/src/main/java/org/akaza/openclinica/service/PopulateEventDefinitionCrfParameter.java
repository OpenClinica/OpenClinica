package org.akaza.openclinica.service;

import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
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
    private OCodmComplexTypeDefinitionConfigurationParameters conf;
    private Study study;
    private StudyEventDefinition studyEventDefinition;
    private ODMcomplexTypeDefinitionFormRef odmFormRef;

    public PopulateEventDefinitionCrfParameter(EventDefinitionCrf eventDefinitionCrf, UserAccount userAccount, CrfBean crf, CrfVersion crfVersion,
            OCodmComplexTypeDefinitionConfigurationParameters conf, Study study, StudyEventDefinition studyEventDefinition,
            ODMcomplexTypeDefinitionFormRef odmFormRef) {
        this.eventDefinitionCrf = eventDefinitionCrf;
        this.userAccount = userAccount;
        this.crf = crf;
        this.crfVersion = crfVersion;
        this.conf = conf;
        this.study = study;
        this.studyEventDefinition = studyEventDefinition;
        this.odmFormRef = odmFormRef;
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
}