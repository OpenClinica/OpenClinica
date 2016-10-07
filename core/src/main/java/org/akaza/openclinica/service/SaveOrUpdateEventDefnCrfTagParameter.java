package org.akaza.openclinica.service;

import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.user.UserAccount;
import org.openclinica.ns.odm_ext_v130.v31_sb.OCodmComplexTypeDefinitionConfigurationParameters;

public class SaveOrUpdateEventDefnCrfTagParameter {
    private UserAccount userAccount;
    private StudyEventDefinition studyEventDefinition;
    private CrfBean crf;
    private EventDefinitionCrf eventDefinitionCrf;
    private OCodmComplexTypeDefinitionConfigurationParameters conf;

    public SaveOrUpdateEventDefnCrfTagParameter(SaveOrUpdateEventDefnCrfTagParameterParameter paramObj) {
        this.userAccount = paramObj.getUserAccount();
        this.studyEventDefinition = paramObj.getStudyEventDefinition();
        this.crf = paramObj.getCrf();
        this.eventDefinitionCrf = paramObj.getEventDefinitionCrf();
        this.conf = paramObj.getConf();
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