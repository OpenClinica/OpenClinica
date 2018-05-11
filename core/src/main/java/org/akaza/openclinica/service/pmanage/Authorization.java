package org.akaza.openclinica.service.pmanage;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Authorization {
    private Long id = null;
    private Study study = null;
    private AuthorizationStatus authorizationStatus = null;
    private String pformUrl = null;
    private String pformApiKey = null;

    public Study getStudy() {
        return study;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public AuthorizationStatus getAuthorizationStatus() {
        return authorizationStatus;
    }

    public void setAuthorizationStatus(AuthorizationStatus authorizationStatus) {
        this.authorizationStatus = authorizationStatus;
    }

    public String getPformUrl() {
        return pformUrl;
    }

    public void setPformUrl(String pformUrl) {
        this.pformUrl = pformUrl;
    }

    public String getPformApiKey() {
        return pformApiKey;
    }

    public void setPformApiKey(String pformApiKey) {
        this.pformApiKey = pformApiKey;
    }

}
