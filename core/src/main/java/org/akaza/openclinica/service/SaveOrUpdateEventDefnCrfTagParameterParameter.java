package org.akaza.openclinica.service;

import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.user.UserAccount;
import org.openclinica.ns.odm_ext_v130.v31_sb.OCodmComplexTypeDefinitionConfigurationParameters;

public class SaveOrUpdateEventDefnCrfTagParameterParameter {
    private UserAccount userAccount;
    private StudyEventDefinition studyEventDefinition;
    private CrfBean crf;
    private EventDefinitionCrf eventDefinitionCrf;
    private OCodmComplexTypeDefinitionConfigurationParameters conf;

    public SaveOrUpdateEventDefnCrfTagParameterParameter(UserAccount userAccount, StudyEventDefinition studyEventDefinition, CrfBean crf,
            EventDefinitionCrf eventDefinitionCrf, OCodmComplexTypeDefinitionConfigurationParameters conf) {
        this.userAccount = userAccount;
        this.studyEventDefinition = studyEventDefinition;
        this.crf = crf;
        this.eventDefinitionCrf = eventDefinitionCrf;
        this.conf = conf;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
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
}