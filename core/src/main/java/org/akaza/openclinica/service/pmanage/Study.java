package org.akaza.openclinica.service.pmanage;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Study {
    private String instanceUrl = null;
    private String studyOid = null;
    private String host = null;
    private Organization organization = null;

    public String getInstanceUrl() {
        return instanceUrl;
    }

    public void setInstanceUrl(String instanceUrl) {
        this.instanceUrl = instanceUrl;
    }

    public String getStudyOid() {
        return studyOid;
    }

    public void setStudyOid(String studyOid) {
        this.studyOid = studyOid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
