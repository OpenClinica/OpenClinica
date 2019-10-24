package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import core.org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;
import core.org.akaza.openclinica.domain.user.UserAccount;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionConfigurationParameters;

public class EDCTagDTO {
    private EventDefinitionCrf eventDefinitionCrf;
    private UserAccount userAccount;
    private OCodmComplexTypeDefinitionConfigurationParameters conf;
    private int tagId;
    private String crfPath;
    private EventDefinitionCrfTag eventDefinitionCrfTag;

    public EDCTagDTO(EDCTagDTO edcTagObj) {
        this.eventDefinitionCrf = edcTagObj.eventDefinitionCrf;
        this.userAccount = edcTagObj.userAccount;
        this.conf = edcTagObj.conf;
        this.tagId = edcTagObj.tagId;
        this.crfPath = edcTagObj.crfPath;
        this.eventDefinitionCrfTag = edcTagObj.eventDefinitionCrfTag;
    }

    public EDCTagDTO() {
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