package org.akaza.openclinica.service;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the request object for retrieving a new access token from Auth0.
 * See <a href= "https://auth0.com/docs/api/management/v2/tokens#automate-the-process">this page</a> for information about what each of the fields in this class means.
 * @author svadla@openclinica.com
 */
public class TokenRequestDTO {
    private String grantType;
    private String clientId;
    private String clientSecret;
    private String audience;
    private String username;
    private String password;
    private String realm;

    @JsonProperty("grant_type")
    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public TokenRequestDTO grantType(String grantType) {
        this.grantType = grantType;
        return this;
    }

    @JsonProperty("client_id")
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public TokenRequestDTO clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @JsonProperty("client_secret")
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public TokenRequestDTO clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public TokenRequestDTO audience(String audience) {
        this.audience = audience;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public TokenRequestDTO username(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public TokenRequestDTO password(String password) {
        this.password = password;
        return this;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public TokenRequestDTO realm(String realm) {
        this.realm = realm;
        return this;
    }
}
