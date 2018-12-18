package org.akaza.openclinica.service;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by yogi on 9/6/17.
 */
public class KeycloakUser {
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private LinkedHashMap<String, Object> userContext;
    public KeycloakUser(AccessToken token) {
        this.userId = token.getSubject();
        Map<String, Object> otherClaims = token.getOtherClaims();
        userContext = (LinkedHashMap<String, Object>) otherClaims.get("https://www.openclinica.com/userContext");
        email = token.getEmail();
        nickname = token.getNickName();
        givenName = token.getGivenName();
        familyName = token.getFamilyName();
    }
    private String nickname;
    private String userId;
    private String givenName;
    private String familyName;
    private String email;
    public String getNickname() {
        return nickname;
    }

    public String getUserId() {
        return userId;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public LinkedHashMap<String, Object> getUserContext() {
        return userContext;
    }

    public String getEmail() {
        return email;
    }
}
