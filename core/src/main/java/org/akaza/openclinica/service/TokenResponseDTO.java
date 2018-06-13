package org.akaza.openclinica.service;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the response received from Auth0 when the create token API is called.
 * @author svadla@openclinica.com
 */
public class TokenResponseDTO {
    private String accessToken;
    private int expirationTime;
    private String scope;
    private String tokenType;

    /**
     * @return the access token that can be used for calling Auth0 API.
     */
    @JsonProperty("access_token")
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the access token.
     * @param accessToken the value to set
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * @return expiration time (in secs) of the token.
     */
    @JsonProperty("expires_in")
    public int getExpirationTime() {
        return expirationTime;
    }

    /**
     * Sets the expiration time.
     * @param expirationTime the value to set
     */
    public void setExpirationTime(int expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * @return the scopes that represent the APIs to which the user has access.
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the scope.
     * @param scope the value to set
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * @return type of the token. E.g. "Bearer"
     */
    @JsonProperty("token_type")
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Sets the token type.
     * @param tokenType the value to set
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
