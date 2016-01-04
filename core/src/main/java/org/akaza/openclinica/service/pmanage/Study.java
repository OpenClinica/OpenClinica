package org.akaza.openclinica.service.pmanage;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Study {

    private long id;
    private String instanceUrl;
    private String studyOid;
    private Organization organization;
    private String host;
    private String OpenClinicaVersion;
    private String studyName;
    
    
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
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
    public Organization getOrganization() {
        return organization;
    }
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getOpenClinicaVersion() {
        return OpenClinicaVersion;
    }
    public void setOpenClinicaVersion(String openClinicaVersion) {
        OpenClinicaVersion = openClinicaVersion;
    }
    public String getStudyName() {
        return studyName;
    }
    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    
    }
