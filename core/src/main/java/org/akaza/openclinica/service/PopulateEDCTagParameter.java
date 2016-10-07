package org.akaza.openclinica.service;

import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;
import org.akaza.openclinica.domain.user.UserAccount;
import org.openclinica.ns.odm_ext_v130.v31_sb.OCodmComplexTypeDefinitionConfigurationParameters;

public class PopulateEDCTagParameter {
    private EventDefinitionCrf eventDefinitionCrf;
    private UserAccount userAccount;
    private OCodmComplexTypeDefinitionConfigurationParameters conf;
    private int tagId;
    private String crfPath;
    private EventDefinitionCrfTag eventDefinitionCrfTag;

    public PopulateEDCTagParameter(EventDefinitionCrf eventDefinitionCrf, UserAccount userAccount, OCodmComplexTypeDefinitionConfigurationParameters conf,
            int tagId, String crfPath, EventDefinitionCrfTag eventDefinitionCrfTag) {
        this.eventDefinitionCrf = eventDefinitionCrf;
        this.userAccount = userAccount;
        this.conf = conf;
        this.tagId = tagId;
        this.crfPath = crfPath;
        this.eventDefinitionCrfTag = eventDefinitionCrfTag;
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

    public OCodmComplexTypeDefinitionConfigurationParameters getConf() {
        return conf;
    }

    public void setConf(OCodmComplexTypeDefinitionConfigurationParameters conf) {
        this.conf = conf;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public String getCrfPath() {
        return crfPath;
    }

    public void setCrfPath(String crfPath) {
        this.crfPath = crfPath;
    }

    public EventDefinitionCrfTag getEventDefinitionCrfTag() {
        return eventDefinitionCrfTag;
    }

    public void setEventDefinitionCrfTag(EventDefinitionCrfTag eventDefinitionCrfTag) {
        this.eventDefinitionCrfTag = eventDefinitionCrfTag;
    }
}